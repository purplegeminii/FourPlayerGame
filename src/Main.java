/**
 * @author Delali Nsiah-Asare
 * @version 1.0.0
 */

import java.io.*;
import java.util.*;
public class Main {
    public static final Scanner input = new java.util.Scanner(System.in);

    /**
     * Main method to play the game
     * @param args array args of class String
     */
    public static void main(String[] args) {

        Random rand = new Random();
        boolean guiMode = false;
        boolean autoMode = false;
        if (args != null) {
            for (String a : args) {
                if ("gui".equals(a)) guiMode = true;
                if ("auto".equals(a)) autoMode = true;
            }
        }

        // At startup decide whether to load saved JSON or start a new game
        int loadChoice = 2;
        if (args != null && args.length >= 1 && "auto".equals(args[0])) {
            // In auto mode we default to starting a new game (2) and skip prompting
            System.out.println("Auto mode detected — starting a new game (no load)");
            loadChoice = 2;
        } else {
            System.out.println("Load saved game from save_progress.json? 1=Load, 2=New (default 2):");
            try { loadChoice = input.nextInt(); } catch (Exception e) { input.nextLine(); loadChoice = 2; }
        }

        Player[] players = null;
        Floor currentFloor = null;
        // reference to GUI window (if requested)
        GameWindow gw = null;
        int player_to_atk;
        int monster_dealing_dmg;

        if (loadChoice == 1) {
            // Try to read saved JSON to determine saved player count
            try {
                String json = readAll("save_progress.json");
                String playersArray = getJsonArray(json, "players");
                List<String> savedPlayers = splitTopLevelObjects(playersArray);
                if (savedPlayers == null || savedPlayers.isEmpty()) throw new IOException("No players in save");
                players = new Player[savedPlayers.size()];
                for (int i = 0; i < players.length; i++) players[i] = new Player();
                currentFloor = loadGame(players);
                if (currentFloor == null) {
                    System.out.println("Failed to load save, starting a new game instead.");
                    loadChoice = 2; // fallthrough to new game
                } else {
                    // show loaded floor items
                    Monster[] floorMonsters = currentFloor.getMonsters();
                    currentFloor.displayItems();
                    // perform an initial attack exchange
                    player_to_atk = rand.nextInt(players.length);
                    monster_dealing_dmg = rand.nextInt(floorMonsters.length);
                    Monster.attack(player_to_atk, monster_dealing_dmg, players, currentFloor.getMonsters());
                }
            } catch (IOException ioe) {
                System.out.println("Could not read save: " + ioe.getMessage());
                loadChoice = 2;
            }
        }

        if (loadChoice != 1) {
            // New game flow
            int numPlayers;
            if (args != null && args.length >= 1 && "auto".equals(args[0])) {
                // Auto mode: randomly choose 1-4 players
                numPlayers = rand.nextInt(4) + 1;
                System.out.println("Auto mode: randomly selected " + numPlayers + " player(s)");
            } else {
                System.out.println("How many players will play? Enter a number (1-4):");
                numPlayers = 4;
                try {
                    numPlayers = Math.max(1, Math.min(4, input.nextInt()));
                } catch (Exception e) {
                    // if the user types something unexpected, default to 4
                    input.nextLine();
                    numPlayers = 4;
                }
            }

            // Choose the starter item type: 1=Consumable, 2=Equipment, 3=Generic Item
            int starterChoice = 1; // default to Consumable
            // If program will run in auto mode, avoid prompting and keep default
            if (args != null && args.length >= 1 && "auto".equals(args[0])) {
                starterChoice = 1;
            } else {
                System.out.println("Choose starter item type: 1=Consumable, 2=Equipment, 3=Item (default 1):");
                try {
                    int c = input.nextInt();
                    if (c >= 1 && c <= 3) starterChoice = c;
                } catch (Exception e) {
                    input.nextLine();
                    starterChoice = 1;
                }
            }

            players = new Player[numPlayers];
            for (int i = 0; i < numPlayers; i++) {
                players[i] = new Player();
                // Give each player exactly one starter item of chosen type
                switch (starterChoice) {
                    case 2 -> { // Equipment: pick a default slot
                        
                        String[] eqNames = {"Bronze Sword", "Iron Shield", "Mystic Robe", "Swift Boots"};
                        String name = eqNames[rand.nextInt(eqNames.length)];
                        String lname = name.toLowerCase();
                        String pos = null;
                        for (Map.Entry<String,String> e : Equipment.EQUIP_SLOT_MAP.entrySet()) {
                            if (lname.contains(e.getKey())) { pos = e.getValue(); break; }
                        }
                        if (pos == null) {
                            // fallback to random slot
                            String[] poss = {"head", "chest", "legs", "hands", "feet"};
                            pos = poss[rand.nextInt(poss.length)];
                        }
                        
                        Equipment eq = new Equipment(name, pos, 1);
                        eq.setItemType("Equipment");
                        players[i].addItem(eq);
                    }
                    case 3 -> { // generic Item
                        Item it = new Item("Starter Trinket", "Misc", 1);
                        it.setItemType("Misc");
                        players[i].addItem(it);
                    }
                    default -> { // Consumable
                        Consumable c = new Consumable("Starter Potion", "Consumable", 1);
                        c.setItemType("Consumable");
                        players[i].addItem(c);
                    }
                }
            }

            if (autoMode) {
                // Auto-mode: create bot players with names bot1..botN and random jobs
                for (int num = 0; num < players.length; num++) {
                    String botName = "bot" + (num + 1);
                    players[num].setPlayerName(botName);
                    int jobIndex = rand.nextInt(4); // 0=Warrior,1=Mage,2=Thief,3=Swordsman
                    players[num].setPlayerJob(jobIndex);
                    System.out.println("Auto-created player: " + botName + " (job " + jobIndex + ")");
                }
            } else {
                for (int num = 0; num < players.length; num++) {
                    System.out.println("\nPlayer" + (num+1) + " name:");
                    String pl_name = input.next();
                    players[num].setPlayerName(pl_name);
                    System.out.println(
                            "What job do you want to awaken with:"+"\n"+
                            "Warrior=0, Mage=1, Thief=2, Swordsman=3"
                    );
                    int jobIndex = input.nextInt();
                    players[num].setPlayerJob(jobIndex);
                }
            }
            Player.beginnerSummary(players);

            // GUI will be launched after we create the first Floor so it has a valid reference.


            // Floor 1 (using Floor class)
            System.out.println("All player(s) arrived on the first floor of the tower");
            System.out.println("Lurking monsters have noticed your presence");
            // create floor 1 with 4 monsters and 2 items
            currentFloor = new Floor(1, 4, 2);
            Monster[] floorMonsters = currentFloor.getMonsters();
            currentFloor.displayItems();
            // pick a random player and monster to perform an initial attack exchange
            player_to_atk = rand.nextInt(players.length);
            monster_dealing_dmg = rand.nextInt(floorMonsters.length);
            Monster.attack(player_to_atk, monster_dealing_dmg, players, currentFloor.getMonsters());
        }

        // If GUI mode requested, launch visualizer now that `currentFloor` exists.
        if (guiMode && currentFloor != null) {
            try {
                gw = new GameWindow(currentFloor, players);
            } catch (Throwable t) {
                System.out.println("Failed to launch GameWindow: " + t.getMessage());
                gw = null;
            }
            if (autoMode) {
                int turns = 50;
                int checkpoint = 5;
                if (args.length >= 2) {
                    try { turns = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
                }
                if (args.length >= 3) {
                    try { checkpoint = Math.max(1, Integer.parseInt(args[2])); } catch (NumberFormatException ignored) {}
                }
                final int tTurns = turns;
                final int tCheckpoint = checkpoint;
                final Player[] apPlayers = players;
                final Floor apFloor = currentFloor;
                new Thread(() -> autoPlay(apPlayers, apFloor, tTurns, tCheckpoint)).start();
            }
            // If GUI mode, we hand control to the GUI and exit the console-driven loop.
            try {
                while (gw != null && gw.isDisplayable()) {
                    Thread.sleep(200);
                }
            } catch (InterruptedException ignored) {}
            input.close();
            return;
        }

        // If program started with args `auto N`, run automated play for N turns
        if (args != null && args.length >= 1 && "auto".equals(args[0])) {
            int turns = 50;
            int checkpoint = 5; // default checkpoint every 5 turns
            if (args.length >= 2) {
                try { turns = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            }
            if (args.length >= 3) {
                try { checkpoint = Math.max(1, Integer.parseInt(args[2])); } catch (NumberFormatException ignored) {}
            }
            autoPlay(players, currentFloor, turns, checkpoint);
            input.close();
            return;
        }

        boolean gameOver = false;
        while (!gameOver) {
            // Play the current floor until cleared or all players die
            while (!currentFloor.isCleared()) {
                for (Player player : players) {
                    // Skip dead players
                    if (player.getHealthBar() <= 0) continue;

                    System.out.println("\n");
                    System.out.println(player.getPlayerName() + ", what action do you want to perform?");
                    Player.showGlobalActions();
                    // System.out.println("Note that choosing \"status\" would mean giving up your turn");
                    int player_response = input.nextInt();
                    if (player_response == 9) {
                        saveGame(players, currentFloor);
                    } else if (player_response == 10) {
                        Floor loaded = loadGame(players);
                        if (loaded != null) {
                            currentFloor = loaded;
                            System.out.println("Loaded floor " + currentFloor.getFloorNumber());
                        }
                    } else {
                        player.performAction(player, player_response, currentFloor, players);
                    }

                    // Monsters retaliate
                    Monster[] monstersArr = currentFloor.getMonsters();
                    if (monstersArr.length > 0) {
                        player_to_atk = rand.nextInt(players.length);
                        monster_dealing_dmg = rand.nextInt(monstersArr.length);
                        Monster.attack(player_to_atk, monster_dealing_dmg, players, monstersArr);
                    }

                    // Check if all players are dead
                    boolean allDead = true;
                    for (Player p : players) {
                        if (p.getHealthBar() > 0) { allDead = false; break; }
                    }
                    if (allDead) {
                        System.out.println("Every player has died... GAME OVER !!!");
                        gameOver = true;
                        break;
                    }
                }
                if (gameOver) break;
            }

            if (gameOver) break;

            // Floor cleared -> advance to next floor
            int nextFloor = currentFloor.getFloorNumber() + 1;
            System.out.println("Floor " + currentFloor.getFloorNumber() + " cleared! Proceeding to floor " + nextFloor + "...");
            // Scale monster/item counts as floors increase
            int monstersCount = Math.min(8, 3 + nextFloor);
            int itemsCount = Math.min(6, 2 + nextFloor / 2);
            currentFloor = new Floor(nextFloor, monstersCount, itemsCount);
            currentFloor.displayItems();
            if (gw != null) gw.setFloor(currentFloor);
            // initial monster reaction on arriving to the new floor
            Monster[] fm = currentFloor.getMonsters();
            if (fm.length > 0) {
                player_to_atk = rand.nextInt(players.length);
                monster_dealing_dmg = rand.nextInt(fm.length);
                Monster.attack(player_to_atk, monster_dealing_dmg, players, fm);
            }
        }



        input.close();
    }

    static void saveGame(Player[] players, Floor floor) {
        // Save game state as JSON only (replaces old plain-text save)
        try {
            saveGameJson(players, floor);
            System.out.println("Game saved to save_progress.json");
        } catch (IOException e) {
            System.out.println("Error saving JSON: " + e.getMessage());
        }
    }

    // Write a JSON representation of the current game state to `save_progress.json`.
    static void saveGameJson(Player[] players, Floor floor) throws IOException {
        try (PrintWriter jw = new PrintWriter(new FileWriter("save_progress.json"))) {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append("\n  \"floor\": ").append(floor.getFloorNumber()).append(',');
            sb.append("\n  \"players\": [\n");
            for (int pi = 0; pi < players.length; pi++) {
                Player p = players[pi];
                sb.append("    {");
                sb.append("\n      \"name\": \"").append(escapeJson(p.getPlayerName())).append("\",");
                sb.append("\n      \"level\": ").append(p.getPlayerLevel()).append(',');
                sb.append("\n      \"rank\": \"").append(p.getPlayerRank()).append("\",");
                sb.append("\n      \"experience\": ").append(p.getExperience()).append(',');
                // job index
                int jobIndex = -1;
                for (int j = 0; j < Player.jobs.length; j++) {
                    if (Player.jobs[j].equals(p.getPlayerJob())) { jobIndex = j; break; }
                }
                sb.append("\n      \"jobIndex\": ").append(jobIndex).append(',');
                sb.append("\n      \"hp\": ").append(p.getHealthBar()).append(',');
                sb.append("\n      \"mp\": ").append(p.getManaBar()).append(',');
                sb.append("\n      \"strength\": ").append(p.getStrength()).append(',');
                sb.append("\n      \"insight\": ").append(p.getInsight()).append(',');
                sb.append("\n      \"agility\": ").append(p.getAgility()).append(',');
                sb.append("\n      \"vitality\": ").append(p.getVitality()).append(',');
                sb.append("\n      \"talent\": ").append(p.getTalent()).append(',');

                // inventory
                sb.append("\n      \"inventory\": [");
                for (int i = 0; i < p.inventory.size(); i++) {
                    Item it = p.inventory.get(i);
                    sb.append("\n        {");
                    sb.append("\"name\": \"").append(escapeJson(it.itemName)).append("\",");
                    sb.append(" \"type\": \"").append(escapeJson(it.itemType)).append("\",");
                    sb.append(" \"rank\": \"").append(escapeJson(it.itemRank)).append("\",");
                    sb.append(" \"statBoost\": ").append(it.statBoost);
                    if (it instanceof Equipment) {
                        sb.append(", \"slot\": \"").append(escapeJson(((Equipment)it).bodyPOS)).append("\"");
                    } else {
                        sb.append(", \"slot\": null");
                    }
                    sb.append(" }");
                    if (i < p.inventory.size()-1) sb.append(',');
                }
                sb.append("\n      ],");

                // equipped (object mapping slot -> item)
                sb.append("\n      \"equipped\": {");
                int ei = 0;
                for (Map.Entry<String, Equipment> e : p.equipped.entrySet()) {
                    String slot = e.getKey();
                    Equipment eq = e.getValue();
                    sb.append("\n        \"").append(escapeJson(slot)).append("\": {");
                    sb.append("\"name\": \"").append(escapeJson(eq.itemName)).append("\",");
                    sb.append(" \"type\": \"").append(escapeJson(eq.itemType)).append("\",");
                    sb.append(" \"rank\": \"").append(escapeJson(eq.itemRank)).append("\",");
                    sb.append(" \"statBoost\": ").append(eq.statBoost).append(',');
                    sb.append(" \"slot\": \"").append(escapeJson(eq.bodyPOS)).append("\" }");
                    if (ei < p.equipped.size()-1) sb.append(',');
                    ei++;
                }
                sb.append("\n      }\n    }");
                if (pi < players.length-1) sb.append(',');
                sb.append('\n');
            }
            sb.append("  ]\n}");

            jw.print(sb.toString());
        }
    }

    // Escape JSON string values simply
    static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    static Floor loadGame(Player[] players) {
        // Load from JSON save (`save_progress.json`). Returns a new Floor or null on error.
        try {
            return loadGameJson(players);
        } catch (IOException e) {
            System.out.println("Load failed: " + e.getMessage());
            return null;
        }
    }

    // Read save_progress.json and populate players; returns constructed Floor
    static Floor loadGameJson(Player[] players) throws IOException {
        String json = readAll("save_progress.json");
        if (json == null || json.isEmpty()) throw new IOException("Empty JSON save file");
        // parse floor
        int floorNum = getJsonInt(json, "floor");

        String playersArray = getJsonArray(json, "players");
        List<String> playerObjects = splitTopLevelObjects(playersArray);
        for (int pidx = 0; pidx < players.length && pidx < playerObjects.size(); pidx++) {
            String pjson = playerObjects.get(pidx);
            Player p = players[pidx];
            p.setPlayerName(getJsonString(pjson, "name"));
            p.setPlayerLevel(getJsonInt(pjson, "level"));
            String rankStr = getJsonString(pjson, "rank");
            if (rankStr != null && rankStr.length() > 0) p.setPlayerRank(rankStr.charAt(0));
            int jobIndex = getJsonInt(pjson, "jobIndex");
            p.setPlayerJob(jobIndex);
            p.setHealthBar(getJsonInt(pjson, "hp"));
            p.setManaBar(getJsonInt(pjson, "mp"));
            p.setStrength(getJsonInt(pjson, "strength"));
            p.setInsight(getJsonInt(pjson, "insight"));
            p.setAgility(getJsonInt(pjson, "agility"));
            p.setVitality(getJsonInt(pjson, "vitality"));
            p.setTalent(getJsonInt(pjson, "talent"));
            // experience (if present)
            p.setExperience(getJsonInt(pjson, "experience"));

            // inventory
            String invArray = getJsonArray(pjson, "inventory");
            List<String> invItems = splitTopLevelObjects(invArray);
            p.inventory.clear();
            for (String itj : invItems) {
                String name = getJsonString(itj, "name");
                String type = getJsonString(itj, "type");
                String rank = getJsonString(itj, "rank");
                int boost = getJsonInt(itj, "statBoost");
                String slot = getJsonString(itj, "slot");
                Item it;
                if (slot != null && !slot.equals("null") && slot.length() > 0) {
                    it = new Equipment(name, slot, 1);
                } else if ("Consumable".equals(type)) {
                    it = new Consumable(name, type, 1);
                } else {
                    it = new Item(name, type, 1);
                }
                it.setItemRank(rank);
                it.setStatBoost(boost);
                it.setItemType(type);
                p.inventory.add(it);
            }

            // equipped
            String equippedObj = getJsonObject(pjson, "equipped");
            Map<String, String> equippedEntries = splitObjectEntries(equippedObj);
            p.equipped.clear();
            for (Map.Entry<String, String> en : equippedEntries.entrySet()) {
                String slot = en.getKey();
                String eqjson = en.getValue();
                String name = getJsonString(eqjson, "name");
                String type = getJsonString(eqjson, "type");
                String rank = getJsonString(eqjson, "rank");
                int boost = getJsonInt(eqjson, "statBoost");
                String pos = getJsonString(eqjson, "slot");
                Equipment eq = new Equipment(name, pos, 1);
                eq.setItemRank(rank);
                eq.setStatBoost(boost);
                eq.setItemType(type);
                p.equipped.put(slot, eq);
            }
            p.recalculateEquipBoosts();
            p.setPlayerPowerLevel();
        }

        Floor floor = new Floor(floorNum, 4, 2);
        System.out.println("Game loaded from save_progress.json");
        return floor;
    }

    // Automated play mode: perform randomized safe actions without using System.in prompts.
    static void autoPlay(Player[] players, Floor floor, int turns, int checkpoint) {
        java.util.Random rand = new java.util.Random();
        System.out.println("Starting auto-play for " + turns + " turns on floor " + floor.getFloorNumber());
        boolean gameOver = false;
        for (int t = 0; t < turns; t++) {
            System.out.println("--- Auto turn " + (t+1) + " ---");
            if (floor.isCleared()) {
                // Advance to next floor automatically during auto-play
                int nextFloor = floor.getFloorNumber() + 1;
                System.out.println("Floor " + floor.getFloorNumber() + " cleared during auto-play. Proceeding to floor " + nextFloor + "...");
                int monstersCount = Math.min(8, 3 + nextFloor);
                int itemsCount = Math.min(6, 2 + nextFloor / 2);
                floor = new Floor(nextFloor, monstersCount, itemsCount);
                floor.displayItems();
                // initial monster reaction on arriving to the new floor
                Monster[] fm = floor.getMonsters();
                if (fm.length > 0) {
                    int player_to_atk = rand.nextInt(players.length);
                    int monster_dealing_dmg = rand.nextInt(fm.length);
                    Monster.attack(player_to_atk, monster_dealing_dmg, players, fm);
                }
                // continue the auto-play loop on the new floor
            }
            Monster[] mons = floor.getMonsters();
            for (Player p : players) {
                // if player is dead, skip
                if (p.getHealthBar() <= 0) continue;

                int action = rand.nextInt(9); // choose among safe actions 0,1,3,4,5,6,7,8 and occasionally 9/10 handled below
                switch (action) {
                    case 0 -> { // attack
                        // pick a reasonable job-specific act_choice = 0
                        int act_choice = 0;
                        // pick a random alive monster index
                        int mcount = mons.length;
                        int idx = -1;
                        for (int i = 0; i < mcount; i++) {
                            if (mons[i] != null && mons[i].getRemainingHealth() > 0) { idx = i; break; }
                        }
                        if (idx == -1) { break; }
                        System.out.println(p.getPlayerName() + " (auto) attacks monster index " + idx);
                        Player.attack(p, act_choice, idx, floor);
                    }
                    case 1 -> { // status
                        p.shortPlayerStatus();
                    }
                    case 3 -> { // inventory
                        p.showInventory();
                    }
                    case 4 -> { // equip gear if any equipment in inventory
                        int chosen = -1;
                        for (int i = 0; i < p.inventory.size(); i++) {
                            if (p.inventory.get(i) instanceof Equipment) { chosen = i; break; }
                        }
                        if (chosen >= 0) {
                            System.out.println(p.getPlayerName() + " (auto) equips inventory index " + chosen);
                            p.equipFromInventory(chosen);
                        }
                    }
                    case 5 -> { // drink potion if any
                        int chosen = -1;
                        for (int i = 0; i < p.inventory.size(); i++) {
                            Item it = p.inventory.get(i);
                            if (it instanceof Consumable || "Consumable".equals(it.itemType)) { chosen = i; break; }
                        }
                        if (chosen >= 0) {
                            Item it = p.inventory.get(chosen);
                            System.out.println(p.getPlayerName() + " (auto) uses consumable " + it.itemName);
                            if (it instanceof Consumable) {
                                ((Consumable) it).use(p);
                            } else {
                                Consumable c = new Consumable(it.itemName, it.itemType, floor.getFloorNumber());
                                c.use(p);
                            }
                            p.inventory.remove(chosen);
                        }
                    }
                    case 6 -> { // use skill (no-op)
                        System.out.println(p.getPlayerName() + " (auto) uses a skill (no-op)");
                    }
                    case 7 -> { // pickup items
                        java.util.List<Item> fit = floor.getItems();
                        if (fit != null && !fit.isEmpty()) {
                            int ix = rand.nextInt(fit.size());
                            Item taken = fit.remove(ix);
                            p.addItem(taken);
                            System.out.println(p.getPlayerName() + " (auto) picked up " + taken.itemName);
                        }
                    }
                    case 8 -> { // inspect: show floor items or inventory
                        if (!floor.getItems().isEmpty()) floor.displayItems(); else p.showInventory();
                    }
                    default -> {}
                }

                // monsters retaliate after each player
                Monster[] cur = floor.getMonsters();
                if (cur.length > 0) {
                    int attacker = rand.nextInt(cur.length);
                    int targetPlayer = rand.nextInt(players.length);
                    // Monster.attack(player_to_atk, monster_dealing_dmg, players, floorMonsters)
                    Monster.attack(targetPlayer, attacker, players, cur);
                }
                // allow auto-play to auto-equip better gear from inventory
                autoEquipBest(p);

                // quick check for game over
                Player.gameStatus(players);
                // stop auto-play early if all players are dead
                boolean allDead = true;
                for (Player pp : players) {
                    if (pp.getHealthBar() > 0) { allDead = false; break; }
                }
                if (allDead) {
                    System.out.println("All players have died. Ending auto-play early.");
                    gameOver = true;
                    break;
                }
            }
            if (gameOver) break;

            // Print checkpoint summary every `checkpoint` turns (checkpoint > 0)
            if (checkpoint > 0 && (t + 1) % checkpoint == 0) {
                System.out.println("\n=== Post-turn-" + checkpoint + " Summary (turn " + (t+1) + ") ===");
                for (Player p : players) {
                    System.out.println("\nPlayer: " + p.getPlayerName());
                    p.shortPlayerStatus();
                    System.out.println("Inventory:");
                    p.showInventory();
                    System.out.println("Equipped:");
                    if (p.equipped == null || p.equipped.isEmpty()) {
                        System.out.println("  None");
                    } else {
                        for (Map.Entry<String, Equipment> en : p.equipped.entrySet()) {
                            String slot = en.getKey();
                            Equipment eq = en.getValue();
                            if (eq != null) {
                                System.out.println("  " + slot + ": " + eq.itemName + " (boost: " + eq.statBoost + ")");
                            }
                        }
                    }
                }
                System.out.println("=== End Post-turn-" + checkpoint + " Summary ===\n");
            }
        }
        // At the end of auto-play, persist auto-save to a separate file
        try {
            saveAutoGame(players, floor);
        } catch (Exception e) {
            System.out.println("Auto-save failed: " + e.getMessage());
        }
        System.out.println("Auto-play finished.");
    }

    // Save auto-play progress to `save_auto_progress.json` (wrapper)
    static void saveAutoGame(Player[] players, Floor floor) throws IOException {
        saveGameJsonToFile(players, floor, "save_auto_progress.json");
        System.out.println("Auto-play progress saved to save_auto_progress.json");
    }

    // Helper to write JSON to an arbitrary filename
    static void saveGameJsonToFile(Player[] players, Floor floor, String filename) throws IOException {
        try (PrintWriter jw = new PrintWriter(new FileWriter(filename))) {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append("\n  \"floor\": ").append(floor.getFloorNumber()).append(',');
            sb.append("\n  \"players\": [\n");
            for (int pi = 0; pi < players.length; pi++) {
                Player p = players[pi];
                sb.append("    {");
                sb.append("\n      \"name\": \"").append(escapeJson(p.getPlayerName())).append("\",");
                sb.append("\n      \"level\": ").append(p.getPlayerLevel()).append(',');
                sb.append("\n      \"rank\": \"").append(p.getPlayerRank()).append("\",");
                // job index
                int jobIndex = -1;
                for (int j = 0; j < Player.jobs.length; j++) {
                    if (Player.jobs[j].equals(p.getPlayerJob())) { jobIndex = j; break; }
                }
                sb.append("\n      \"jobIndex\": ").append(jobIndex).append(',');
                sb.append("\n      \"hp\": ").append(p.getHealthBar()).append(',');
                sb.append("\n      \"mp\": ").append(p.getManaBar()).append(',');
                sb.append("\n      \"strength\": ").append(p.getStrength()).append(',');
                sb.append("\n      \"insight\": ").append(p.getInsight()).append(',');
                sb.append("\n      \"agility\": ").append(p.getAgility()).append(',');
                sb.append("\n      \"vitality\": ").append(p.getVitality()).append(',');
                sb.append("\n      \"talent\": ").append(p.getTalent()).append(',');
                sb.append("\n      \"experience\": ").append(p.getExperience()).append(',');

                // inventory
                sb.append("\n      \"inventory\": [");
                for (int i = 0; i < p.inventory.size(); i++) {
                    Item it = p.inventory.get(i);
                    sb.append("\n        {");
                    sb.append("\"name\": \"").append(escapeJson(it.itemName)).append("\",");
                    sb.append(" \"type\": \"").append(escapeJson(it.itemType)).append("\",");
                    sb.append(" \"rank\": \"").append(escapeJson(it.itemRank)).append("\",");
                    sb.append(" \"statBoost\": ").append(it.statBoost);
                    if (it instanceof Equipment) {
                        sb.append(", \"slot\": \"").append(escapeJson(((Equipment)it).bodyPOS)).append("\"");
                    } else {
                        sb.append(", \"slot\": null");
                    }
                    sb.append(" }");
                    if (i < p.inventory.size()-1) sb.append(',');
                }
                sb.append("\n      ],");

                // equipped
                sb.append("\n      \"equipped\": {");
                int ei = 0;
                for (Map.Entry<String, Equipment> e : p.equipped.entrySet()) {
                    String slot = e.getKey();
                    Equipment eq = e.getValue();
                    sb.append("\n        \"").append(escapeJson(slot)).append("\": {");
                    sb.append("\"name\": \"").append(escapeJson(eq.itemName)).append("\",");
                    sb.append(" \"type\": \"").append(escapeJson(eq.itemType)).append("\",");
                    sb.append(" \"rank\": \"").append(escapeJson(eq.itemRank)).append("\",");
                    sb.append(" \"statBoost\": ").append(eq.statBoost).append(',');
                    sb.append(" \"slot\": \"").append(escapeJson(eq.bodyPOS)).append("\" }");
                    if (ei < p.equipped.size()-1) sb.append(',');
                    ei++;
                }
                sb.append("\n      }\n    }");
                if (pi < players.length-1) sb.append(',');
                sb.append('\n');
            }
            sb.append("  ]\n}");

            jw.print(sb.toString());
        }
    }

    // --- JSON helpers for save/load ---
    static String readAll(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    static int getJsonInt(String json, String key) {
        String v = getJsonRaw(json, key);
        if (v == null) return 0;
        v = v.trim();
        // remove trailing commas
        if (v.endsWith(",")) v = v.substring(0, v.length()-1).trim();
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return 0; }
    }

    static String getJsonString(String json, String key) {
        String v = getJsonRaw(json, key);
        if (v == null) return "";
        v = v.trim();
        if (v.startsWith("\"")) {
            // find closing quote
            int i = 1;
            StringBuilder sb = new StringBuilder();
            while (i < v.length()) {
                char c = v.charAt(i);
                if (c == '\\' && i+1 < v.length()) { sb.append(v.charAt(i+1)); i += 2; continue; }
                if (c == '"') break;
                sb.append(c); i++;
            }
            return sb.toString();
        }
        // not a quoted string (null or number)
        if (v.startsWith("null")) return "null";
        return v.split("[\\\\n\\\\r,}]")[0];
    }

    static String getJsonRaw(String json, String key) {
        String needle = "\"" + key + "\"";
        int pos = json.indexOf(needle);
        if (pos == -1) return null;
        int colon = json.indexOf(':', pos + needle.length());
        if (colon == -1) return null;
        int start = colon + 1;
        // return remainder of line/section
        int end = json.indexOf('\n', start);
        if (end == -1) end = json.length();
        return json.substring(start, end).trim();
    }

    static String getJsonArray(String json, String key) {
        String needle = "\"" + key + "\"";
        int pos = json.indexOf(needle);
        if (pos == -1) return "[]";
        int bracket = json.indexOf('[', pos);
        if (bracket == -1) return "[]";
        int depth = 0;
        int i = bracket;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) { return json.substring(bracket, i+1); } }
        }
        return "[]";
    }

    static String getJsonObject(String json, String key) {
        String needle = "\"" + key + "\"";
        int pos = json.indexOf(needle);
        if (pos == -1) return "{}";
        int brace = json.indexOf('{', pos);
        if (brace == -1) return "{}";
        int depth = 0;
        int i = brace;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') { depth--; if (depth == 0) { return json.substring(brace, i+1); } }
        }
        return "{}";
    }

    // Auto-equip best equipment found in a player's inventory for each slot.
    // This scans the inventory for Equipment items, picks the highest `computeStatDelta()`
    // per slot, and equips it if it's strictly better than the currently equipped item.
    static void autoEquipBest(Player p) {
        if (p == null) return;
        // map slot -> best inventory index and its value
        java.util.Map<String, Integer> bestIndex = new java.util.HashMap<>();
        java.util.Map<String, Integer> bestValue = new java.util.HashMap<>();
        for (int i = 0; i < p.inventory.size(); i++) {
            Item it = p.inventory.get(i);
            if (it instanceof Equipment) {
                Equipment eq = (Equipment) it;
                int val = eq.computeStatDelta();
                String slot = eq.bodyPOS;
                Integer curBest = bestValue.get(slot);
                if (curBest == null || val > curBest) {
                    bestValue.put(slot, val);
                    bestIndex.put(slot, i);
                }
            }
        }

        // Prepare list of (slot, index) pairs and sort by index desc so removal doesn't shift earlier indices
        java.util.List<java.util.Map.Entry<String, Integer>> entries = new java.util.ArrayList<>(bestIndex.entrySet());
        entries.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));

        for (java.util.Map.Entry<String, Integer> en : entries) {
            String slot = en.getKey();
            int idx = en.getValue();
            if (idx < 0 || idx >= p.inventory.size()) continue;
            Item cand = p.inventory.get(idx);
            if (!(cand instanceof Equipment)) continue;
            Equipment candidate = (Equipment) cand;
            Equipment current = p.equipped.get(slot);
            int curVal = (current == null) ? 0 : current.computeStatDelta();
            int candVal = candidate.computeStatDelta();
            if (candVal > curVal) {
                // equip candidate (equipFromInventory will handle unequip and stat updates)
                p.equipFromInventory(idx);
            }
        }
    }

    static List<String> splitTopLevelObjects(String arrayJson) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < arrayJson.length() && arrayJson.charAt(i) != '[') i++;
        if (i >= arrayJson.length()) return out;
        i++; // skip '['
        while (i < arrayJson.length()) {
            while (i < arrayJson.length() && Character.isWhitespace(arrayJson.charAt(i))) i++;
            if (i >= arrayJson.length() || arrayJson.charAt(i) == ']') break;
            if (arrayJson.charAt(i) == '{') {
                int depth = 0;
                int start = i;
                for (; i < arrayJson.length(); i++) {
                    char c = arrayJson.charAt(i);
                    if (c == '{') depth++;
                    else if (c == '}') { depth--; if (depth == 0) { i++; break; } }
                }
                out.add(arrayJson.substring(start, i));
                // skip comma
                while (i < arrayJson.length() && (arrayJson.charAt(i) == ',' || Character.isWhitespace(arrayJson.charAt(i)))) i++;
                continue;
            }
            i++;
        }
        return out;
    }

    // Split an object like { "slot": { ... }, "slot2": { ... } } into map slot->objectString
    static Map<String, String> splitObjectEntries(String objJson) {
        Map<String,String> map = new LinkedHashMap<>();
        int i = 0;
        // find first '{'
        while (i < objJson.length() && objJson.charAt(i) != '{') i++;
        if (i >= objJson.length()) return map;
        i++; // after '{'
        while (i < objJson.length()) {
            while (i < objJson.length() && Character.isWhitespace(objJson.charAt(i))) i++;
            if (i >= objJson.length() || objJson.charAt(i) == '}') break;
            // read key
            if (objJson.charAt(i) == '"') {
                int kstart = i+1;
                int kend = objJson.indexOf('"', kstart);
                if (kend == -1) break;
                String key = objJson.substring(kstart, kend);
                i = kend+1;
                // skip to ':'
                while (i < objJson.length() && objJson.charAt(i) != ':') i++;
                if (i >= objJson.length()) break;
                i++;
                // skip whitespace
                while (i < objJson.length() && Character.isWhitespace(objJson.charAt(i))) i++;
                if (i < objJson.length() && objJson.charAt(i) == '{') {
                    int depth = 0;
                    int start = i;
                    for (; i < objJson.length(); i++) {
                        char c = objJson.charAt(i);
                        if (c == '{') depth++;
                        else if (c == '}') { depth--; if (depth == 0) { i++; break; } }
                    }
                    String val = objJson.substring(start, i);
                    map.put(key, val);
                }
                // skip comma/whitespace
                while (i < objJson.length() && (objJson.charAt(i) == ',' || Character.isWhitespace(objJson.charAt(i)))) i++;
                continue;
            }
            i++;
        }
        return map;
    }
}