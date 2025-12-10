import javax.swing.*;
import java.awt.*;
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
    // UI controls
    private JComboBox<String> playerSelector;
    private JPanel controlsPanel;
    private JLabel selectedMonsterLabel;
    private int selectedMonster = -1;

    public GameWindow(Floor floor, Player[] players) {
        super("FourPlayerGame - Visualizer");
        this.floor = floor;
        this.players = players;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 640);
        setLocationRelativeTo(null);

        // Layout: drawing on left/center, controls on right
        DrawingPanel dp = new DrawingPanel();
        dp.setPreferredSize(new Dimension(720, 600));
        add(dp, BorderLayout.CENTER);

        buildControls();
        add(controlsPanel, BorderLayout.EAST);

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
            p.showInventory();
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
            if (floor != null && !floor.getItems().isEmpty()) floor.displayItems(); else p.showInventory();
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
}
