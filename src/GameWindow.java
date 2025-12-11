import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
// import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Minimal Swing visualization for the tower floor, players and monsters.
 * This is intentionally simple — a static top-down layout that repaints periodically.
 */
public class GameWindow extends JFrame {
    private Floor floor;
    private Player[] players;
    private final int tileSize = 48;
    // equipment slots loaded from config/equipment_slots.properties
    private static final Map<String, String> equipmentSlots = new LinkedHashMap<>();
    static {
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File f = new java.io.File("config/equipment_slots.properties");
            if (f.exists() && f.isFile()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
                    props.load(fis);
                }
                for (String name : props.stringPropertyNames()) {
                    equipmentSlots.put(name.toLowerCase(), props.getProperty(name));
                }
            }
        } catch (Exception ignored) {}
    }
    // UI controls
    private JComboBox<String> playerSelector;
    private JPanel controlsPanel;
    private JLabel selectedMonsterLabel;
    private int selectedMonster = -1;
    // in-GUI combat log
    private JTextArea combatLog;

    // static reference to active GameWindow so global System.out can be routed here
    private static volatile GameWindow activeInstance = null;
    private static volatile boolean consoleHooked = false;
    private static PrintStream originalOut = System.out;

    public GameWindow(Floor floor, Player[] players) {
        super("FourPlayerGame - Visualizer");
        this.floor = floor;
        this.players = players;

        // register active instance for global logging
        activeInstance = this;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        // Layout: drawing on left/center, controls on right
        DrawingPanel dp = new DrawingPanel();
        dp.setPreferredSize(new Dimension(720, 600));
        add(dp, BorderLayout.CENTER);

        buildControls();
        add(controlsPanel, BorderLayout.EAST);

        // create and add combat log area at bottom of controls
        combatLog = new JTextArea();
        combatLog.setEditable(false);
        combatLog.setLineWrap(true);
        combatLog.setWrapStyleWord(true);
        combatLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(combatLog);
        logScroll.setPreferredSize(new Dimension(240, 180));
        controlsPanel.add(logScroll, BorderLayout.SOUTH);

        // Repaint periodically (use javax.swing.Timer to avoid ambiguity with java.util.Timer)
        javax.swing.Timer t = new javax.swing.Timer(300, e -> dp.repaint());
        t.start();

        setVisible(true);
    }

    // Allow external update of the floor reference so UI reflects game progression.
    public synchronized void setFloor(Floor floor) {
        this.floor = floor;
    }

    // Allow external update of players array (e.g., when players are created/modified)
    public synchronized void setPlayers(Player[] players) {
        this.players = players;
    }

    // Build right-side control panel with player selector and action buttons
    private void buildControls() {
        controlsPanel = new JPanel();
        controlsPanel.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        top.add(new JLabel("Active Player:"), BorderLayout.NORTH);
        playerSelector = new JComboBox<>();
        updatePlayerSelector();
        top.add(playerSelector, BorderLayout.CENTER);

        controlsPanel.add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(0,1,6,6));
        buttons.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // Action buttons mapping to the same action codes used by the console
        addButton(buttons, "Attack", e -> doPlayerAction(0));
        addButton(buttons, "Status", e -> doPlayerAction(1));
        addButton(buttons, "Inventory", e -> doPlayerAction(3));
        addButton(buttons, "Equip", e -> doPlayerAction(4));
        addButton(buttons, "Use Item", e -> doPlayerAction(5));
        addButton(buttons, "Use Skill", e -> doPlayerAction(6));
        addButton(buttons, "Pickup Item", e -> doPlayerAction(7));
        addButton(buttons, "Inspect", e -> doPlayerAction(8));

        // Selected monster display
        selectedMonsterLabel = new JLabel("Selected monster: -");
        top.add(selectedMonsterLabel, BorderLayout.SOUTH);

        // Save / Load
        addButton(buttons, "Save Game", e -> {
            if (players != null && floor != null) {
                Main.saveGame(players, floor);
            }
        });
        addButton(buttons, "Load Game", e -> {
            if (players != null) {
                Floor loaded = Main.loadGame(players);
                if (loaded != null) {
                    setFloor(loaded);
                    JOptionPane.showMessageDialog(this, "Loaded floor " + loaded.getFloorNumber());
                } else {
                    JOptionPane.showMessageDialog(this, "Load failed");
                }
            }
        });

        controlsPanel.add(buttons, BorderLayout.CENTER);
    }

    private void addButton(JPanel parent, String text, ActionListener l) {
        JButton b = new JButton(text);
        b.addActionListener(l);
        parent.add(b);
    }

    private void updatePlayerSelector() {
        SwingUtilities.invokeLater(() -> {
            playerSelector.removeAllItems();
            if (players != null) {
                for (Player p : players) {
                    if (p == null) continue;
                    playerSelector.addItem(p.getPlayerName());
                }
            }
        });
    }

    // Execute a player action from the GUI using the same integer codes as console
    private void doPlayerAction(int actionCode) {
        int idx = playerSelector.getSelectedIndex();
        if (idx < 0 || players == null || idx >= players.length) {
            JOptionPane.showMessageDialog(this, "No player selected");
            return;
        }
        Player p = players[idx];
        if (p.getHealthBar() <= 0) { JOptionPane.showMessageDialog(this, "Player is dead"); return; }

        if (actionCode == 0) { // Attack: ask for monster index
            Monster[] mons = (floor == null) ? new Monster[0] : floor.getMonsters();
            java.util.List<String> opts = new ArrayList<>();
            for (int i = 0; i < mons.length; i++) {
                Monster m = mons[i];
                String label = i + ": " + (m == null ? "(none)" : m.getMonsterName() + " (HP:" + m.getRemainingHealth() + ")");
                opts.add(label);
            }
            if (opts.isEmpty()) { JOptionPane.showMessageDialog(this, "No monsters to attack"); return; }
            int chosen = selectedMonster;
            if (chosen < 0 || chosen >= opts.size()) {
                String choice = (String) JOptionPane.showInputDialog(this, "Choose monster:", "Attack", JOptionPane.PLAIN_MESSAGE, null, opts.toArray(), opts.get(0));
                if (choice == null) return;
                chosen = Integer.parseInt(choice.split(":")[0]);
            }
            // Ask user which skill (act_choice) to use for this job, then perform the attack directly.
            int act_choice = 0;
            try {
                String[] skills = Player.getSkillNamesForJob(p.getPlayerJob());
                if (skills != null && skills.length > 0) {
                    String choice = (String) JOptionPane.showInputDialog(this, "Choose a skill:", "Skill", JOptionPane.PLAIN_MESSAGE, null, skills, skills[0]);
                    if (choice != null) {
                        // find index
                        for (int si = 0; si < skills.length; si++) if (skills[si].equals(choice)) { act_choice = si; break; }
                    } else {
                        return; // cancelled
                    }
                } else {
                    act_choice = 0;
                }
            } catch (Exception ex) {
                act_choice = 0;
            }

            // perform attack using Player.attack
            try {
                Player.attack(p, act_choice, chosen, floor);
            } catch (Exception ex) {
                // If reflection call needed fallback, try reflective invocation
                try {
                    java.lang.reflect.Method mth = Player.class.getMethod("attack", Player.class, int.class, int.class, Floor.class);
                    mth.invoke(null, p, act_choice, chosen, floor);
                } catch (Exception ignore) {}
            }
        } else if (actionCode == 1) {
            // Show player status in a GUI dialog (scrollable)
            String status = p.getFullStatusString();
            JTextArea ta = new JTextArea(status);
            ta.setEditable(false);
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(420, 320));
            JOptionPane.showMessageDialog(this, sp, p.getPlayerName() + " - Status", JOptionPane.PLAIN_MESSAGE);
        } else if (actionCode == 3) {
            // Show inventory using the interactive item-list dialog (inspect/equip/use/drop)
            if (p.inventory == null || p.inventory.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inventory is empty");
            } else {
                showItemListDialog(p, p.inventory, p.getPlayerName() + " - Inventory", true);
            }
        } else if (actionCode == 4) {
            // choose equipment from inventory
            int chosen = chooseInventoryIndex(p, "Choose equipment index to equip", true);
            if (chosen >= 0) p.equipFromInventory(chosen);
        } else if (actionCode == 5) {
            int chosen = chooseInventoryIndex(p, "Choose consumable index to use", false);
            if (chosen >= 0) {
                Item it = p.inventory.get(chosen);
                if (it instanceof Consumable) {
                    ((Consumable) it).use(p);
                } else {
                    Consumable c = new Consumable(it.itemName, it.itemType, floor == null ? 1 : floor.getFloorNumber());
                    c.use(p);
                }
                p.inventory.remove(chosen);
            }
        } else if (actionCode == 6) {
            // skill use -- best-effort: call Player.useSkill or performAction with code 6
            try {
                p.performAction(p, 6, floor, players);
            } catch (Exception ex) { /* ignore */ }
        } else if (actionCode == 7) {
            if (floor != null && floor.getItems() != null && !floor.getItems().isEmpty()) {
                int ix = new Random().nextInt(floor.getItems().size());
                Item taken = floor.getItems().remove(ix);
                p.addItem(taken);
                JOptionPane.showMessageDialog(this, p.getPlayerName() + " picked up " + taken.itemName);
            } else {
                JOptionPane.showMessageDialog(this, "No items to pick up");
            }
        } else if (actionCode == 8) {
            // Inspect: open the interactive item-list dialog for floor items or inventory
            String[] choices = new String[]{"Floor Items", "Inventory"};
            String sel = (String) JOptionPane.showInputDialog(this, "Inspect which source?", "Inspect",
                    JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            if (sel == null) return; // cancelled
            if ("Floor Items".equals(sel)) {
                if (floor == null || floor.getItems() == null || floor.getItems().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No items on the floor to inspect");
                } else {
                    // pass the actual floor items list so actions (pick up) can modify it
                    showItemListDialog(p, floor.getItems(), "Inspect - Floor Items", false);
                }
            } else {
                if (p.inventory == null || p.inventory.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Inventory is empty");
                } else {
                    showItemListDialog(p, p.inventory, "Inspect - Inventory", true);
                }
            }
        }

        // repaint and refresh selector
        updatePlayerSelector();
        selectedMonsterLabel.setText("Selected monster: " + selectedMonster);
        // If this action consumed a turn, let monsters retaliate and handle floor progression
        if (actionCode == 0 || actionCode == 4 || actionCode == 5 || actionCode == 6 || actionCode == 7) {
            postPlayerTurn();
        }
        repaint();
    }

    // Helper to refresh UI components on the EDT
    private void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            updatePlayerSelector();
            selectedMonsterLabel.setText("Selected monster: " + selectedMonster);
            repaint();
        });
    }

    /**
     * Append a message to the in-GUI combat log (thread-safe).
     */
    public void appendLog(String msg) {
        if (msg == null) return;
        SwingUtilities.invokeLater(() -> {
            if (combatLog == null) return;
            combatLog.append(msg);
            if (!msg.endsWith("\n")) combatLog.append("\n");
            combatLog.setCaretPosition(combatLog.getDocument().getLength());
        });
    }

    /**
     * Install a System.out redirect that also writes to the GUI combat log when available.
     * Safe to call multiple times; only the first call installs the hook.
     */
    public static synchronized void enableGuiLogging() {
        if (consoleHooked) return;
        originalOut = System.out;
        OutputStream os = new OutputStream() {
            private StringBuilder buf = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                // forward to original stdout
                originalOut.write(b);
                char c = (char) b;
                buf.append(c);
                if (c == '\n') {
                    String line = buf.toString();
                    buf.setLength(0);
                    if (activeInstance != null) activeInstance.appendLog(line);
                }
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                originalOut.write(b, off, len);
                String s = new String(b, off, len);
                buf.append(s);
                int idx;
                while ((idx = buf.indexOf("\n")) >= 0) {
                    String line = buf.substring(0, idx+1);
                    buf.delete(0, idx+1);
                    if (activeInstance != null) activeInstance.appendLog(line);
                }
            }
        };
        PrintStream ps = new PrintStream(os, true);
        System.setOut(ps);
        consoleHooked = true;
    }

    public static void setActiveInstance(GameWindow gw) {
        activeInstance = gw;
    }

    // Called after a player performs a consuming action: monsters retaliate once and we check for floor clear
    private void postPlayerTurn() {
        // Run retaliation and model updates; retaliation may modify model so ensure UI refresh afterwards.
        monsterRetaliation();
        // auto-equip best gear for all players (keep behavior similar to autoPlay)
        if (players != null) {
            for (Player p : players) {
                if (p != null) Main.autoEquipBest(p);
            }
        }
        // check for floor cleared
        if (floor != null && floor.isCleared()) {
            int nextFloor = floor.getFloorNumber() + 1;
            JOptionPane.showMessageDialog(this, "Floor " + floor.getFloorNumber() + " cleared! Proceeding to floor " + nextFloor + "...");
            int monstersCount = Math.min(8, 3 + nextFloor);
            int itemsCount = Math.min(6, 2 + nextFloor / 2);
            Floor newFloor = new Floor(nextFloor, monstersCount, itemsCount);
            newFloor.displayItems();
            setFloor(newFloor);
            // ensure UI shows new floor items immediately
            refreshUI();
        }
        // quick game over check
        if (players != null) Player.gameStatus(players);
        // final refresh after post-turn updates
        refreshUI();
    }

    private void monsterRetaliation() {
        if (floor == null || players == null) return;
        Monster[] mons = floor.getMonsters();
        if (mons == null || mons.length == 0) return;
        java.util.List<Integer> aliveMons = new ArrayList<>();
        for (int i = 0; i < mons.length; i++) if (mons[i] != null && mons[i].getRemainingHealth() > 0) aliveMons.add(i);
        if (aliveMons.isEmpty()) return;
        java.util.List<Integer> alivePlayers = new ArrayList<>();
        for (int i = 0; i < players.length; i++) if (players[i] != null && players[i].getHealthBar() > 0) alivePlayers.add(i);
        if (alivePlayers.isEmpty()) return;
        int mIdx = aliveMons.get(new Random().nextInt(aliveMons.size()));
        int pIdx = alivePlayers.get(new Random().nextInt(alivePlayers.size()));

        // Run the attack off the EDT to avoid blocking the UI; refresh UI after it completes.
        new Thread(() -> {
            try {
                Monster.attack(pIdx, mIdx, players, mons);
            } catch (Exception ignored) {}
            // schedule UI refresh on EDT
            SwingUtilities.invokeLater(() -> {
                updatePlayerSelector();
                selectedMonsterLabel.setText("Selected monster: " + selectedMonster);
                repaint();
            });
        }, "monster-retaliation").start();
    }

    private int chooseInventoryIndex(Player p, String title, boolean equipmentOnly) {
        if (p.inventory == null || p.inventory.isEmpty()) { JOptionPane.showMessageDialog(this, "Inventory empty"); return -1; }
        java.util.List<String> opts = new ArrayList<>();
        for (int i = 0; i < p.inventory.size(); i++) {
            Item it = p.inventory.get(i);
            if (equipmentOnly && !(it instanceof Equipment)) continue;
            opts.add(i + ": " + it.itemName + " (" + it.itemType + ")");
        }
        if (opts.isEmpty()) { JOptionPane.showMessageDialog(this, "No matching items"); return -1; }
        String sel = (String) JOptionPane.showInputDialog(this, title, "Select", JOptionPane.PLAIN_MESSAGE, null, opts.toArray(), opts.get(0));
        if (sel == null) return -1;
        return Integer.parseInt(sel.split(":")[0]);
    }

    private class DrawingPanel extends JPanel {
        private java.util.List<Rectangle> monsterRects = new ArrayList<>();

        public DrawingPanel() {
            // allow clicking on monsters to select them
            this.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    int mx = e.getX();
                    int my = e.getY();
                    selectedMonster = -1;
                    for (int i = 0; i < monsterRects.size(); i++) {
                        Rectangle r = monsterRects.get(i);
                        if (r != null && r.contains(mx, my)) {
                            selectedMonster = i;
                            break;
                        }
                    }
                    selectedMonsterLabel.setText("Selected monster: " + selectedMonster);
                    repaint();
                }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // background
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Snapshot shared model references to avoid races while painting
            Floor localFloor = GameWindow.this.floor;
            Player[] snapshotPlayers = (GameWindow.this.players == null) ? new Player[0] : GameWindow.this.players.clone();

            // Draw floor info at top
            g2.setColor(Color.WHITE);
            g2.drawString("Floor: " + (localFloor == null ? "-" : localFloor.getFloorNumber()), 10, 18);

            // Players row (use snapshot)
            int px = 10;
            int py = 30;
            for (Player p : snapshotPlayers) {
                if (p == null) continue;
                g2.setColor(p.getHealthBar() > 0 ? Color.BLUE : Color.GRAY);
                g2.fillOval(px, py, 36, 36);
                g2.setColor(Color.WHITE);
                g2.drawString(p.getPlayerName() + " (HP:" + p.getHealthBar() + ")", px + 42, py + 18);
                py += 40;
            }

            // Monsters grid - draw as tiles with name and HP bar; record rectangles for click selection
            int startX = 10;
            int startY = Math.max(py + 10, 160);
            Monster[] mons = (localFloor == null) ? new Monster[0] : localFloor.getMonsters();
            monsterRects.clear();
            if (mons != null) {
                int cols = Math.max(1, (getWidth() - 240) / (tileSize + 12)); // leave room for controls
                int i = 0;
                for (Monster m : mons) {
                    int col = i % cols;
                    int row = i / cols;
                    int x = startX + col * (tileSize + 12);
                    int y = startY + row * (tileSize + 20);

                    // draw tile background
                    g2.setColor(new Color(80,80,80));
                    g2.fillRoundRect(x-4, y-6, tileSize+8, tileSize+28, 8, 8);

                    if (m == null || m.getRemainingHealth() <= 0) {
                        // dead monster
                        g2.setColor(Color.GRAY);
                        g2.fillRect(x, y, tileSize, tileSize);
                        g2.setColor(Color.BLACK);
                        g2.drawString("DEAD", x+6, y+tileSize/2);
                    } else {
                        // monster body
                        g2.setColor(Color.RED.darker());
                        g2.fillOval(x, y, tileSize, tileSize);
                        // name above
                        g2.setColor(Color.WHITE);
                        String name = m.getMonsterName();
                        g2.drawString(name, x, y - 2);
                        // HP bar below
                        int barW = tileSize;
                        int barH = 8;
                        int barX = x;
                        int barY = y + tileSize + 6;
                        // use actual monster max HP for correct proportions when available
                        int guessedMax = Math.max(1, m.getMaxHealth());
                        float frac = Math.max(0f, Math.min(1f, m.getRemainingHealth() / (float) guessedMax));
                        g2.setColor(Color.DARK_GRAY);
                        g2.fillRect(barX, barY, barW, barH);
                        g2.setColor(new Color(200,40,40));
                        g2.fillRect(barX, barY, Math.max(1, (int)(barW * frac)), barH);
                        g2.setColor(Color.WHITE);
                        g2.drawRect(barX, barY, barW, barH);
                        g2.drawString("HP:" + m.getRemainingHealth(), barX, barY + barH + 12);
                    }

                    // highlight if selected
                    if (i == selectedMonster) {
                        g2.setColor(Color.YELLOW);
                        Stroke old = g2.getStroke();
                        g2.setStroke(new BasicStroke(3));
                        g2.drawRect(x-2, y-2, tileSize+4, tileSize+4);
                        g2.setStroke(old);
                    }

                    monsterRects.add(new Rectangle(x-4, y-6, tileSize+8, tileSize+34));
                    i++;
                }
            }

            // Items list
            java.util.List<Item> items = (localFloor == null || localFloor.getItems() == null) ? Collections.emptyList() : new ArrayList<>(localFloor.getItems());
            if (!items.isEmpty()) {
                int ix = getWidth() - 200;
                int iy = 40;
                g2.setColor(Color.GREEN);
                g2.drawString("Floor Items:", ix, iy);
                iy += 12;
                for (Item it : items) {
                    if (it == null) continue;
                    String name = (it.getItemName() == null) ? "(unknown)" : it.getItemName();
                    g2.drawString("- " + name, ix, iy);
                    iy += 14;
                }
            }
        }
    }

    // Helper to show a modal dialog with item details
    private void showItemDetailsDialog(Item it, String title) {
        if (it == null) {
            JOptionPane.showMessageDialog(this, "No item data available", "Inspect", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("--- Item Details ---\n");
        sb.append("Name: ").append(it.itemName).append("\n");
        sb.append("Type: ").append(it.itemType).append("\n");
        sb.append("Rank: ").append(it.itemRank).append("\n");
        sb.append("Base boost: ").append(it.statBoost).append("\n");
        if (it instanceof Consumable) {
            sb.append("Consumable: restores ").append(it.statBoost).append(" to its target stat\n");
        }
        if (it instanceof Equipment) {
            Equipment eq = (Equipment) it;
            sb.append("Slot: ").append(eq.bodyPOS).append("\n");
            sb.append("Computed stat delta if equipped: ").append(eq.computeStatDelta()).append("\n");
        }
        sb.append("--------------------\n");

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(420, 240));
        JOptionPane.showMessageDialog(this, sp, title, JOptionPane.PLAIN_MESSAGE);
    }

    // Interactive item list dialog with actions (Inspect, Equip/Use, Pick Up, Drop)
    private void showItemListDialog(Player p, java.util.List<Item> items, String title, boolean isInventory) {
        if (items == null || items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items to show", title, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(520, 360);
        dialog.setLocationRelativeTo(this);

        DefaultListModel<Item> model = new DefaultListModel<>();
        for (Item it : items) model.addElement(it);
        JList<Item> list = new JList<>(model);
        list.setCellRenderer(new ItemCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(list);
        dialog.add(sp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton inspectBtn = new JButton("Inspect");
        JButton primaryBtn = new JButton(isInventory ? "Equip/Use" : "Pick Up");
        JButton dropBtn = new JButton("Drop");
        JButton closeBtn = new JButton("Close");
        inspectBtn.setEnabled(false);
        primaryBtn.setEnabled(false);
        dropBtn.setEnabled(false);

        btnPanel.add(inspectBtn);
        btnPanel.add(primaryBtn);
        if (isInventory) btnPanel.add(dropBtn);
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        list.addListSelectionListener(e -> {
            boolean sel = !list.isSelectionEmpty();
            inspectBtn.setEnabled(sel);
            dropBtn.setEnabled(sel && isInventory);
            primaryBtn.setEnabled(sel);
            if (sel) {
                Item it = list.getSelectedValue();
                if (it instanceof Equipment) primaryBtn.setText("Equip");
                else if (it instanceof Consumable) primaryBtn.setText(isInventory ? "Use" : "Pick Up");
                else primaryBtn.setText(isInventory ? "Use" : "Pick Up");
            }
        });

        inspectBtn.addActionListener(a -> {
            Item it = list.getSelectedValue();
            if (it != null) showItemDetailsDialog(it, "Inspect - " + it.itemName);
        });

        primaryBtn.addActionListener(a -> {
            int si = list.getSelectedIndex();
            if (si < 0) return;
            Item it = model.getElementAt(si);
            if (isInventory) {
                // operate on player's inventory
                if (it instanceof Equipment) {
                    // equip by inventory index
                    p.equipFromInventory(si);
                    // remove from model if item was removed from inventory
                    model.remove(si);
                } else if (it instanceof Consumable) {
                    ((Consumable) it).use(p);
                    model.remove(si);
                    p.inventory.remove(it);
                } else {
                    // Generic use: remove and attempt to use
                    model.remove(si);
                    p.inventory.remove(it);
                }
            } else {
                // pick up from floor -> remove from floor list and add to player inventory
                if (floor != null && floor.getItems() != null) {
                    int idx = items.indexOf(it);
                    if (idx >= 0) {
                        Item taken = items.remove(idx);
                        p.addItem(taken);
                        JOptionPane.showMessageDialog(dialog, p.getPlayerName() + " picked up " + taken.itemName);
                        model.remove(si);
                    }
                }
            }
            refreshUI();
        });

        dropBtn.addActionListener(a -> {
            int si = list.getSelectedIndex();
            if (si < 0) return;
            Item it = model.getElementAt(si);
            int confirm = JOptionPane.showConfirmDialog(dialog, "Drop " + it.itemName + "?", "Drop", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            // move to floor if available
            if (floor != null && floor.getItems() != null) {
                p.inventory.remove(it);
                floor.getItems().add(it);
            } else {
                p.inventory.remove(it);
            }
            model.remove(si);
            refreshUI();
        });

        closeBtn.addActionListener(a -> dialog.dispose());

        dialog.setVisible(true);
    }

    // Simple colored icon base; specific types draw different shapes. Shapes derive from slot mapping when possible.
    private static class TypeIcon implements Icon {
        private final Color color;
        private final int w, h;
        private final String itemName;
        private final String typeStr;
        private final String slot;
        public TypeIcon(String itemName, String type, Color c, int w, int h) {
            this.itemName = itemName == null ? "" : itemName.toLowerCase();
            this.typeStr = type == null ? "" : type.toLowerCase();
            this.color = c;
            this.w = w;
            this.h = h;
            this.slot = getSlotForItem(this.itemName, this.typeStr);
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            String s = slot == null ? "" : slot.toLowerCase();
            // weapon-like: draw a small blade and handle when slot indicates hands or similar
            if (s.contains("hands") || s.contains("bracer") || s.contains("glove") || this.typeStr.contains("sword") || this.typeStr.contains("axe") || this.typeStr.contains("dagger")) {
                int cx = x + 2, cy = y + 2;
                g2.fillRect(cx + 6, cy + 1, Math.max(2, w - 8), 4);
                int tipX = cx + 6 + Math.max(2, w - 8);
                int[] xs = { tipX, tipX + 4, tipX };
                int[] ys = { cy + 1, cy + 3, cy + 5 };
                g2.fillPolygon(xs, ys, 3);
                g2.setColor(color.darker());
                g2.fillRect(cx, cy + 6, 6, 2);
            }
            // armor/chest/head/feet -> draw a shield-like polygon
            else if (s.contains("chest") || s.contains("head") || s.contains("feet") || s.contains("plate") || s.contains("armor") || this.typeStr.contains("armor") || this.typeStr.contains("shield")) {
                int cx = x + w/2;
                int[] xs = { cx, x + w - 2, x + w/2, x + 2 };
                int[] ys = { y + 2, y + h/3, y + h - 2, y + h/3 };
                g2.fillPolygon(xs, ys, 4);
                g2.setColor(color.darker());
                g2.drawPolygon(xs, ys, 4);
            }
            // necklace / ring -> draw small gem-like circle
            else if (s.contains("finger") || s.contains("neck") || this.typeStr.contains("ring") || this.typeStr.contains("amulet")) {
                int gx = x + 2, gy = y + 2, gw = w - 4, gh = h - 4;
                g2.fillOval(gx, gy, gw, gh);
                g2.setColor(color.darker());
                g2.drawOval(gx, gy, gw, gh);
            }
            // potion/consumable: draw a small bottle
            else if (this.typeStr.contains("potion") || this.typeStr.contains("consum") || this.typeStr.contains("bottle")) {
                int bx = x + 2, by = y + 2, bw = w - 4, bh = h - 6;
                g2.fillOval(bx, by, bw, bh);
                g2.setColor(color.darker());
                g2.fillRect(bx + bw/3, by - 2, bw/3, 3);
            }
            // default: colored circle
            else {
                g2.fillOval(x, y, w, h);
            }
            g2.dispose();
        }
        public int getIconWidth() { return w; }
        public int getIconHeight() { return h; }
    }

    // Custom cell renderer to show small icon, name and colored rank label
    private static class ItemCellRenderer implements ListCellRenderer<Item> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list, Item value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel p = new JPanel(new BorderLayout(6, 2));
            p.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
            Icon iconObj = getIconForItem(value == null ? "" : value.itemName, value == null ? "" : value.itemType, getRankColor(value), 14, 14);
            JLabel icon = new JLabel(iconObj);
            JPanel left = new JPanel(new BorderLayout());
            left.add(icon, BorderLayout.WEST);
            JLabel name = new JLabel(value.itemName == null ? "(unknown)" : value.itemName);
            name.setFont(name.getFont().deriveFont(Font.BOLD, 12f));
            left.add(name, BorderLayout.CENTER);
            p.add(left, BorderLayout.CENTER);
            String rankStr = String.valueOf(value.itemRank);
            JLabel rank = new JLabel(rankStr);
            rank.setForeground(getRankColor(value));
            p.add(rank, BorderLayout.EAST);
            if (isSelected) {
                p.setBackground(list.getSelectionBackground());
                p.setForeground(list.getSelectionForeground());
            } else {
                p.setBackground(list.getBackground());
                p.setForeground(list.getForeground());
            }
            return p;
        }
        private static Color getRankColor(Item it) {
            return GameWindow.getRankColorStatic(it);
        }
    }

    // Icon cache and loader (tries resources/icons/<name>.png). Falls back to TypeIcon (programmatic)
    private static final Map<String, Icon> iconCache = new HashMap<>();

    private static Icon getIconForItem(String itemName, String itemType, Color color, int w, int h) {
        String keyBase = (itemName == null ? "" : itemName.toLowerCase()).replaceAll("\\s+", "_");
        String slot = getSlotForItem(itemName, itemType);
        String typeKey = (itemType == null ? "" : itemType.toLowerCase()).replaceAll("\\s+", "_");
        java.util.List<String> tries = new ArrayList<>();
        if (keyBase != null && !keyBase.isEmpty()) tries.add(keyBase);
        if (slot != null && !slot.isEmpty()) tries.add(slot);
        if (typeKey != null && !typeKey.isEmpty()) tries.add(typeKey);
        // common fallbacks
        tries.add("weapon"); tries.add("armor"); tries.add("potion"); tries.add("consumable"); tries.add("default");

        for (String t : tries) {
            String cacheKey = t + "|" + w + "x" + h;
            if (iconCache.containsKey(cacheKey)) return iconCache.get(cacheKey);
            // try png file in resources/icons/
            String pngPath = "resources/icons/" + t + ".png";
            java.io.File f = new java.io.File(pngPath);
            if (f.exists() && f.isFile()) {
                try {
                    Image img = new ImageIcon(pngPath).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    ImageIcon ii = new ImageIcon(img);
                    iconCache.put(cacheKey, ii);
                    return ii;
                } catch (Exception ignored) {}
            }
        }
        // no image found: fallback to programmatic TypeIcon
        TypeIcon ti = new TypeIcon(itemName, itemType, color, w, h);
        return ti;
    }

    // Derive a color from item rank or type; fallback to gray
    private static Color getRankColorStatic(Item it) {
        if (it == null) return Color.GRAY;
        String r = String.valueOf(it.itemRank == null ? "" : it.itemRank);
        // try numeric rank
        try {
            int v = Integer.parseInt(r);
            if (v >= 5) return new Color(212, 175, 55); // gold
            if (v >= 4) return new Color(186, 85, 211); // purple
            if (v >= 3) return new Color(65, 105, 225); // blue
            return new Color(120,120,120);
        } catch (Exception ignored) {}
        // text-based ranks
        String lr = r.toLowerCase();
        if (lr.contains("legend") || lr.contains("gold")) return new Color(212, 175, 55);
        if (lr.contains("epic") || lr.contains("purple") || lr.contains("rare")) return new Color(186, 85, 211);
        if (lr.contains("rare") || lr.contains("blue")) return new Color(65, 105, 225);
        return new Color(120,120,120);
    }

    // Determine equipment slot for an item using loaded equipmentSlots mapping
    private static String getSlotForItem(String itemName, String itemType) {
        if ((itemName == null || itemName.isEmpty()) && (itemType == null || itemType.isEmpty())) return null;
        String name = itemName == null ? "" : itemName.toLowerCase();
        String type = itemType == null ? "" : itemType.toLowerCase();
        for (Map.Entry<String,String> e : equipmentSlots.entrySet()) {
            String key = e.getKey();
            if ((name != null && name.contains(key)) || (type != null && type.contains(key))) {
                return e.getValue();
            }
        }
        return null;
    }
}
