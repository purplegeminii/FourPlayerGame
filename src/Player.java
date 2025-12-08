/**
 * @author Delali Nsiah-Asare
 * @version 1.0.0
 */

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Player {
    private static final java.util.Scanner input = Main.input;
    private int healthBar;
    private int manaBar;
    private int strength;
    private int insight;
    private int agility;
    private int vitality;
    private int talent;
    private int playerLevel;
    private String playerName;
    private static final char[] ranks = {'F', 'E', 'D', 'C', 'B', 'A'};
    private char playerRank;
    private int playerPowerLevel;
    public static final String[] jobs = {"WARRIOR", "MAGE", "THIEF", "SWORDSMAN"};
    private String playerJob;
    private static final String[] global_actions = {
        "attack", "status", "pause", "inventory", "equip gear", "drink potion", "use skill", "pickup items", "inspect item", "save game", "load game"
    };
    // Skills declared as ordered maps: skill name -> Skill metadata
    private static final java.util.LinkedHashMap<String, Skill> warrior_act = new java.util.LinkedHashMap<>() {{
        put("punch", new Skill(2, "A quick low-cost strike", "punch"));
        put("bull charge", new Skill(10, "A heavy area charge that hits all monsters", "bull_charge"));
    }};
    private static final java.util.LinkedHashMap<String, Skill> mage_act = new java.util.LinkedHashMap<>() {{
        put("elemental blast", new Skill(5, "A single-target elemental burst", "elemental_blast"));
        put("team heal", new Skill(15, "Heals all allies for a moderate amount", "team_heal"));
    }};
    private static final java.util.LinkedHashMap<String, Skill> thief_act = new java.util.LinkedHashMap<>() {{
        put("back stab", new Skill(8, "A high-damage single-target backstab", "back_stab"));
        put("mana poison", new Skill(6, "Steals MP from a target (not implemented)", "mana_poison"));
    }};
    private static final java.util.LinkedHashMap<String, Skill> swordsman_act = new java.util.LinkedHashMap<>() {{
        put("slash", new Skill(3, "A reliable slash attack", "slash"));
        put("critical seal", new Skill(12, "A powerful strike that can crit", "critical_seal"));
    }};

    // Helper: get skill name by index from an ordered map
    private static String getSkillNameByIndex(java.util.Map<String,Skill> map, int index) {
        if (map == null || map.isEmpty()) return "";
        index = Math.max(0, Math.min(index, map.size()-1));
        int i = 0;
        for (String k : map.keySet()) {
            if (i == index) return k;
            i++;
        }
        return "";
    }

    // Helper: get Skill object by index
    private static Skill getSkillByIndex(java.util.Map<String,Skill> map, int index) {
        if (map == null || map.isEmpty()) return null;
        index = Math.max(0, Math.min(index, map.size()-1));
        int i = 0;
        for (java.util.Map.Entry<String,Skill> e : map.entrySet()) {
            if (i == index) return e.getValue();
            i++;
        }
        return null;
    }

    // Helper: print skill options with indices and metadata
    private static void printSkillOptions(java.util.Map<String,Skill> map) {
        int i = 0;
        for (java.util.Map.Entry<String,Skill> e : map.entrySet()) {
            Skill s = e.getValue();
            System.out.println(i + " => " + e.getKey() + " (cost: " + s.cost + ") - " + s.description);
            i++;
        }
    }

    Random rand = new Random();
    ArrayList<Item> inventory = new ArrayList<>();
    HashMap<String, Equipment> equipped = new HashMap<>();
    private HashMap<String, Integer> equipBoosts = new HashMap<>();
    // Experience and leveling
    private int experience = 0;
    // Base XP unit for a single level increment. Using linear curve (BASE_XP * (level-1))
    // keeps progression reasonable for early playtesting.
    private static final int BASE_XP = 50;
    private static final int[] RANK_THRESHOLDS = {5, 10, 20, 35, 55};

    public Player() {

        this.setPlayerLevel(1);
        this.setPlayerRank();
        this.setStrength(rand.nextInt(11));
        this.setInsight(rand.nextInt(11));
        this.setAgility(rand.nextInt(11));
        this.setAgility(rand.nextInt(11));
        this.setVitality(rand.nextInt(11));
        this.setTalent(rand.nextInt(11));
        this.setHealthBar();
        this.setManaBar();
        this.setPlayerPowerLevel();

        this.playerName = "no name yet";
        this.playerLevel = this.getPlayerLevel();
        this.playerRank = ranks[0];
        this.playerJob = this.getPlayerJob();
        this.strength = this.getStrength();
        this.insight = this.getInsight();
        this.agility = this.getAgility();
        this.vitality = this.getVitality();
        this.talent = this.getTalent();
        this.healthBar = this.getHealthBar();
        this.manaBar = this.getManaBar();
        this.playerPowerLevel = this.getPlayerPowerLevel();
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setInsight(int insight) {
        this.insight = insight;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public void setTalent(int talent) {
        this.talent = talent;
    }

    public int getStrength() {
        return this.strength;
    }

    public int getInsight() {
        return this.insight;
    }

    public int getAgility() {
        return this.agility;
    }

    public int getVitality() {
        return this.vitality;
    }

    public int getTalent() {
        return this.talent;
    }

    public void setHealthBar() {
        this.healthBar = getMaxHealth();
    }

    public void setHealthBar(int newHP) {
        if (newHP <= 0) {
            this.healthBar = 0;
        } else {
            this.healthBar = Math.min(newHP, getMaxHealth());
        }
    }

    public int getHealthBar() {
        return this.healthBar;
    }

    public void setManaBar() {
        this.manaBar = getMaxMana();
    }

    public void setManaBar(int newMP) {
        if (newMP <= 0) {
            this.manaBar = 0;
        } else {
            this.manaBar = Math.min(newMP, getMaxMana());
        }
    }

    /**
     * Compute maximum health derived from stats (does not change current health).
     * @return max HP
     */
    public int getMaxHealth() {
        int vit = this.getVitality() * 8;
        int str = this.getStrength() * 6;
        return (vit + str) * 10;
    }

    /**
     * Compute maximum mana derived from stats (does not change current mana).
     * @return max MP
     */
    public int getMaxMana() {
        int ins = this.getInsight() * 3;
        int tal = this.getTalent() * 2;
        return (ins + tal) * 5;
    }

    public int getManaBar() {
        return this.manaBar;
    }

    public void setPlayerPowerLevel() {
        int lvl;
        lvl = (this.getManaBar() + this.getHealthBar()) * 7;
        this.playerPowerLevel = lvl;
    }

    public int getPlayerPowerLevel() {
        return this.playerPowerLevel;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public char getPlayerRank() {
        return this.playerRank;
    }

    public void setPlayerRank() {
        this.playerRank = Player.ranks[rand.nextInt(ranks.length)];
    }

    public void setPlayerRank(char rank) {
        this.playerRank = rank;
    }

    /**
     * non-static method to access playerLevel member variable
     * @return int
     */
    public int getPlayerLevel() {
        return this.playerLevel;
    }

    /**
     * @param playerLevel player's level
     */
    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    /**
     * Add an item to the player's inventory.
     * @param it item to add
     */
    public void addItem(Item it) {
        this.inventory.add(it);
        System.out.println("Added to inventory: " + it.itemName + " (" + it.itemType + ")");
    }

    /**
     * Print the player's inventory contents.
     */
    public void showInventory() {
        if (this.inventory.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        System.out.println("Inventory:");
        for (int i = 0; i < this.inventory.size(); i++) {
            Item it = this.inventory.get(i);
            if (it instanceof Equipment) {
                Equipment eq = (Equipment) it;
                int delta = eq.computeStatDelta();
                System.out.println(i + " => " + it.itemName + " (" + it.itemType + ") - slot: " + eq.bodyPOS + " - computed boost: " + delta);
            } else {
                System.out.println(i + " => " + it.itemName + " (" + it.itemType + ") - boost: " + it.statBoost);
            }
        }
    }

    private void inspectItemDetails(Item it) {
        System.out.println("--- Item Details ---");
        System.out.println("Name: " + it.itemName);
        System.out.println("Type: " + it.itemType);
        System.out.println("Rank: " + it.itemRank);
        System.out.println("Base boost: " + it.statBoost);
        if (it instanceof Consumable) {
            System.out.println("Consumable: restores " + it.statBoost + " to its target stat");
        }
        if (it instanceof Equipment) {
            Equipment eq = (Equipment) it;
            System.out.println("Slot: " + eq.bodyPOS);
            System.out.println("Computed stat delta if equipped: " + eq.computeStatDelta());
        }
        System.out.println("--------------------");
    }

    /**
     * Equip an Equipment item from the inventory by index.
     * @param inventoryIndex index in inventory
     */
    public void equipFromInventory(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= this.inventory.size()) {
            System.out.println("Invalid inventory index.");
            return;
        }
        Item it = this.inventory.get(inventoryIndex);
        if (!(it instanceof Equipment)) {
            System.out.println("Item at index is not equipment.");
            return;
        }
        Equipment eq = (Equipment) it;
        String slot = eq.bodyPOS;

        // If something already equipped in that slot, unequip it first
        if (this.equipped.containsKey(slot)) {
            this.unequip(slot);
        }

        // compute stat delta from equipment.statBoost; scale down to player stat range
        int delta = eq.computeStatDelta();

        switch (slot) {
            case "head" -> this.setInsight(this.getInsight() + delta);
            case "chest" -> this.setVitality(this.getVitality() + delta);
            case "legs" -> this.setAgility(this.getAgility() + delta);
            case "hands" -> this.setStrength(this.getStrength() + delta);
            case "feet" -> this.setAgility(this.getAgility() + delta);
            default -> this.setStrength(this.getStrength() + delta);
        }

        this.equipped.put(slot, eq);
        this.equipBoosts.put(slot, delta);
        // remove from inventory since it's now equipped
        this.inventory.remove(inventoryIndex);
        // refresh derived stats
        this.setHealthBar();
        this.setManaBar();
        this.setPlayerPowerLevel();
        System.out.println("Equipped " + eq.itemName + " to slot " + slot + " (+" + delta + ")");
    }

    /**
     * Unequip equipment in the given slot (if any) and return it to inventory.
     * @param slot equipment slot name
     */
    public void unequip(String slot) {
        if (!this.equipped.containsKey(slot)) {
            System.out.println("No equipment in slot " + slot);
            return;
        }
        Equipment eq = this.equipped.remove(slot);
        int delta = this.equipBoosts.remove(slot);

        switch (slot) {
            case "head" -> this.setInsight(this.getInsight() - delta);
            case "chest" -> this.setVitality(this.getVitality() - delta);
            case "legs" -> this.setAgility(this.getAgility() - delta);
            case "hands" -> this.setStrength(this.getStrength() - delta);
            case "feet" -> this.setAgility(this.getAgility() - delta);
            default -> this.setStrength(this.getStrength() - delta);
        }

        this.inventory.add(eq);
        // refresh derived stats
        this.setHealthBar();
        this.setManaBar();
        this.setPlayerPowerLevel();
        System.out.println("Unequipped " + eq.itemName + " from slot " + slot + ", returned to inventory.");
    }

    void recalculateEquipBoosts() {
        this.equipBoosts.clear();
        for (Map.Entry<String, Equipment> e : this.equipped.entrySet()) {
            int delta = e.getValue().computeStatDelta();
            this.equipBoosts.put(e.getKey(), delta);
        }
    }

    // Sum of all equipment boosts (used to scale damage)
    public int getTotalEquipBoost() {
        int total = 0;
        for (Integer v : this.equipBoosts.values()) {
            if (v != null) total += v;
        }
        return total;
    }

    public void setPlayerJob(int jobIndex) {
        this.playerJob = Player.jobs[jobIndex];
    }

    public String getPlayerJob() {
        return this.playerJob;
    }

    /**
     * Static method that shows the players names and their respective jobs they awakened with
     * @param players array of players
     */
    public static void beginnerSummary(Player[] players) {
        System.out.println("\nPlayers summary:");
        for (int i = 0; i < players.length; i++) {
            System.out.println("player" + (i+1) + ": " + players[i].getPlayerName() + " awakened as a " + players[i].getPlayerJob());
        }
    }

    /**
     * non-static method that displays the full status of a player
     */
    public void fullPlayerStatus() {
        System.out.println("________________________________________"+"\n"
                         + "| Name: "+this.getPlayerName()+"\n"
                         + "| Level: "+this.getPlayerLevel()+"\n"
                         + "| Rank: "+this.getPlayerRank()+"\n"
                         + "| Job: "+this.getPlayerJob()+"\n"
                         + "|"+"\n"
                         + "| HP: "+this.getHealthBar()+"\n"
                         + "| MP: "+this.getManaBar()+"\n"
                         + "|"+"\n"
                         + "| Strength: "+this.getStrength()+"\n"
                         + "| Insight: "+this.getInsight()+"\n"
                         + "| Agility: "+this.getAgility()+"\n"
                         + "| Vitality: "+this.getVitality()+"\n"
                         + "| Talent: "+this.getTalent()+"\n"
                         + "|"+"\n"
                         + "| Power Level: "+this.getPlayerPowerLevel()+"\n"
                         + "| Equipped: "+getEquippedSummary()+"\n"
                         + "________________________________________"
                );
    }

    private String getEquippedSummary() {
        if (this.equipped == null || this.equipped.isEmpty()) return "(none)";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Equipment> e : this.equipped.entrySet()) {
            sb.append("[").append(e.getKey()).append(": ").append(e.getValue().itemName).append("] ");
        }
        return sb.toString().trim();
    }

    /**
     * non-static method that displays the summarized status of a player
     */
    public void shortPlayerStatus() {
        System.out.println("________________________________________"+"\n"
                         + "| Name: "+this.getPlayerName()+"\n"
                         + "| Level: "+this.getPlayerLevel()+"\n"
                         + "| Rank: "+this.getPlayerRank()+"\n"
                         + "| Job: "+this.getPlayerJob()+"\n"
                         + "|"+"\n"
                         + "| HP: "+this.getHealthBar()+"\n"
                         + "| MP: "+this.getManaBar()+"\n"
                         + "________________________________________");
    }

    /**
     * Static method that displays GAME OVER when all players die
     * @param players array of players
     */
    public static void gameStatus(Player[] players) {
        boolean allDead = true;
        for (Player p : players) {
            if (p.getHealthBar() > 0) { allDead = false; break; }
        }
        if (allDead) System.out.println("Every player has died... GAME OVER !!!");
    }

    public static void showPauseScreen(Player current_player) {
        java.util.Scanner keyboard = input;
        int player_choice;
        System.out.println("the pause screen shows info on every job and its various attacks");
        System.out.println("Do you want to your full status? yes=0 no=1");
        player_choice = keyboard.nextInt();
        if (player_choice == 0) {
            current_player.fullPlayerStatus();
        }
    }

    public static void showGlobalActions() {
        int i = 0;
        for (String globalAction : global_actions) {
            System.out.print(i + " => " + globalAction + ", ");
            i++;
        }
        System.out.println("\n");
    }

    // --- Experience / Level / Rank methods ---
    public int getExperience() { return this.experience; }
    public void setExperience(int xp) { this.experience = Math.max(0, xp); }

    // private int xpForLevel(int level) {
    //     // Return total XP required to reach `level`.
    //     // Level 1 requires 0 XP, Level 2 requires BASE_XP, Level 3 requires 2*BASE_XP, etc.
    //     if (level <= 1) return 0;
    //     return BASE_XP * (level - 1);
    // }

    /**
     * XP required to advance from the current level to the next level.
     * We use a simple increasing requirement: BASE_XP * currentLevel.
     * Example: level 1 -> 2 requires BASE_XP * 1, level 2 -> 3 requires BASE_XP * 2, etc.
     */
    private int xpToNextLevel() {
        return Math.max(1, BASE_XP * this.playerLevel);
    }

    public void gainExperience(int xp) {
        if (xp <= 0) return;
        this.experience += xp;
        System.out.println(this.getPlayerName() + " gained " + xp + " XP (pool: " + this.experience + "). Next level requires " + xpToNextLevel() + " XP.");
        // allow level-ups that consume XP from the pool
        checkLevelUp();
    }

    private void checkLevelUp() {
        // allow multi-level ups in one call; consume XP from the pool for each level
        while (this.experience >= xpToNextLevel()) {
            int cost = xpToNextLevel();
            this.experience -= cost;
            this.playerLevel++;
            levelUp();
        }
    }

    private void levelUp() {
        int primaryGain = 1 + new java.util.Random().nextInt(2); // 1-2
        int secondaryGain = new java.util.Random().nextInt(2); // 0-1
        switch (this.getPlayerJob()) {
            case "WARRIOR" -> { this.setStrength(this.getStrength() + primaryGain); this.setVitality(this.getVitality() + secondaryGain); }
            case "MAGE" -> { this.setInsight(this.getInsight() + primaryGain); this.setTalent(this.getTalent() + secondaryGain); }
            case "THIEF" -> { this.setAgility(this.getAgility() + primaryGain); this.setStrength(this.getStrength() + secondaryGain); }
            case "SWORDSMAN" -> { this.setStrength(this.getStrength() + primaryGain); this.setAgility(this.getAgility() + secondaryGain); }
            default -> { this.setStrength(this.getStrength() + 1); }
        }

        int restoreHP = Math.max(1, getMaxHealth() / 4);
        int restoreMP = Math.max(1, getMaxMana() / 4);
        this.setHealthBar(this.getHealthBar() + restoreHP);
        this.setManaBar(this.getManaBar() + restoreMP);
        this.setPlayerPowerLevel();

        System.out.println(this.getPlayerName() + " leveled up to " + this.getPlayerLevel() +
                "! +" + primaryGain + " primary, +" + secondaryGain + " secondary. Restored " + restoreHP + " HP and " + restoreMP + " MP.");

        checkRankUp();
    }

    private void checkRankUp() {
        // find current rank index
        int idx = 0;
        for (int i = 0; i < ranks.length; i++) if (ranks[i] == this.playerRank) { idx = i; break; }
        if (idx >= ranks.length - 1) return; // already top rank
        int nextThreshold = (idx < RANK_THRESHOLDS.length) ? RANK_THRESHOLDS[idx] : Integer.MAX_VALUE;
        if (this.playerLevel >= nextThreshold) {
            promoteRank(idx + 1);
        }
    }

    private void promoteRank(int newRankIndex) {
        if (newRankIndex < 0 || newRankIndex >= ranks.length) return;
        char oldRank = this.playerRank;
        this.playerRank = ranks[newRankIndex];
        this.setStrength(this.getStrength() + 2 + new java.util.Random().nextInt(3));
        this.setVitality(this.getVitality() + 1 + new java.util.Random().nextInt(3));
        this.setPlayerPowerLevel();
        System.out.println(this.getPlayerName() + " has been promoted from rank " + oldRank + " to " + this.playerRank + "! Stat bonus applied.");
    }

    /**
     * static method performs the attack actions of a player
     * @param current_player current player
     * @param act_choice player's choice of action
     * @param monster_to_atk index of monster to attack
     * @param floorMonsters array of monsters on a floor
     */
    public static void attack(Player current_player, int act_choice, int monster_to_atk, Floor floor) {
        Monster[] floorMonsters = floor.getMonsters();
        System.out.println(current_player.getPlayerName()+" is attacking "+floorMonsters[monster_to_atk].getMonsterName());

        // announce action (use skill name from maps)
        switch (current_player.getPlayerJob()) {
            case "WARRIOR" -> System.out.println(current_player.getPlayerName() + " has used " + getSkillNameByIndex(warrior_act, act_choice));
            case "MAGE" -> System.out.println(current_player.getPlayerName() + " has used " + getSkillNameByIndex(mage_act, act_choice));
            case "THIEF" -> System.out.println(current_player.getPlayerName() + " has used " + getSkillNameByIndex(thief_act, act_choice));
            case "SWORDSMAN" -> System.out.println(current_player.getPlayerName() + " has used " + getSkillNameByIndex(swordsman_act, act_choice));
            default -> System.out.println(current_player.getPlayerName() + " performs an action.");
        }

        // compute damage based on job, stats, level and equipped boosts
        java.util.Random r = new java.util.Random();
        int equipBoost = current_player.getTotalEquipBoost();
        int weaponBonus = 0;
        try {
            Equipment weq = current_player.equipped.get("hands");
            if (weq != null) weaponBonus = Math.max(0, weq.statBoost) * 6;
        } catch (Exception ignored) {}
        int lvl = current_player.getPlayerLevel();
        int dmg;
        switch (current_player.getPlayerJob()) {
            case "WARRIOR" -> dmg = current_player.getStrength() * 30 + lvl * 8 + equipBoost * 8 + weaponBonus + r.nextInt(30);
            case "MAGE" -> dmg = current_player.getInsight() * 30 + lvl * 6 + Math.max(0, equipBoost) * 6 + weaponBonus + r.nextInt(40);
            case "THIEF" -> dmg = current_player.getAgility() * 26 + lvl * 6 + Math.max(0, equipBoost) * 6 + weaponBonus + r.nextInt(30);
            case "SWORDSMAN" -> dmg = current_player.getStrength() * 34 + lvl * 8 + equipBoost * 8 + weaponBonus + r.nextInt(35);
            default -> dmg = current_player.getStrength() * 16 + lvl * 4 + r.nextInt(18) + weaponBonus;
        }

        // apply damage to target monster
        Monster target = floorMonsters[monster_to_atk];
        Item dropped = target.takeDamage(dmg);
        // if monster died, award XP to the attacker
        if (target.getRemainingHealth() <= 0) {
            int xp = target.getXpReward();
            current_player.gainExperience(xp);
        }
        if (dropped != null) {
            // auto-pickup into the attacker's inventory
            current_player.addItem(dropped);
            System.out.println("Item acquired: " + dropped.itemName + " (" + dropped.itemType + ") added to your inventory.");
        }

    }

    /**
     * non-static method to perform player actions based on the job they awakened with
     * @param current_player current player
     * @param playerResponse player's response
     * @param floorMonsters array of monsters on a floor
     */
    public void performAction(Player current_player, int playerResponse, Floor floor, Player[] players) {
        int act_choice;
        int monster_to_atk;

        // Helper to choose a skill index for the current job (encapsulates prompt + input)
        // Returns a valid index within the job's skill map
        java.util.function.IntUnaryOperator chooseSkillForJob = (ignored) -> {
            int choice = 0;
            switch (this.getPlayerJob()) {
                case "WARRIOR" -> printSkillOptions(warrior_act);
                case "MAGE" -> printSkillOptions(mage_act);
                case "THIEF" -> printSkillOptions(thief_act);
                case "SWORDSMAN" -> printSkillOptions(swordsman_act);
                default -> System.out.println("No skills available for this job.");
            }
            try { choice = input.nextInt(); } catch (Exception e) { input.nextLine(); choice = 0; }
            return Math.max(0, choice);
        };

        // Actions that consume the player's turn will set this to true.
        boolean turnConsumed = false;

        while (!turnConsumed) {
            // If playerResponse is informational (status, inventory, pause, inspect, save/load), show info and re-prompt
            if (playerResponse == 1) {
                // status
                this.fullPlayerStatus();
                System.out.println("(Status viewed — this does not consume your turn.) Choose an action:");
                Player.showGlobalActions();
                try { playerResponse = input.nextInt(); } catch (Exception e) { input.nextLine(); playerResponse = 0; }
                continue;
            } else if (playerResponse == 2) {
                // pause screen (informational)
                System.out.println("PLAYER HAS PAUSED THE GAME!!!");
                Player.showPauseScreen(current_player);
                System.out.println("(Pause viewed — this does not consume your turn.) Choose an action:");
                Player.showGlobalActions();
                try { playerResponse = input.nextInt(); } catch (Exception e) { input.nextLine(); playerResponse = 0; }
                continue;
            } else if (playerResponse == 3) {
                // Show player inventory (informational)
                this.showInventory();
                System.out.println("(Inventory viewed — this does not consume your turn.) Choose an action:");
                Player.showGlobalActions();
                try { playerResponse = input.nextInt(); } catch (Exception e) { input.nextLine(); playerResponse = 0; }
                continue;
            } else if (playerResponse == 8) {
                // Inspect item (informational)
                System.out.println("Inspect which source? 0 = floor, 1 = inventory");
                int src;
                try { src = input.nextInt(); } catch (Exception e) { input.nextLine(); src = -1; }
                if (src == 0) {
                    java.util.List<Item> floorItems = floor.getItems();
                    if (floorItems == null || floorItems.isEmpty()) {
                        System.out.println("No items on floor to inspect.");
                    } else {
                        floor.displayItems();
                        System.out.println("Enter index to inspect or -1 to cancel:");
                        int idx = input.nextInt();
                        if (idx >= 0 && idx < floorItems.size()) {
                            Item it = floorItems.get(idx);
                            inspectItemDetails(it);
                        } else {
                            System.out.println("Cancelled or invalid index.");
                        }
                    }
                } else if (src == 1) {
                    if (this.inventory.isEmpty()) {
                        System.out.println("Inventory empty.");
                    } else {
                        this.showInventory();
                        System.out.println("Enter inventory index to inspect or -1 to cancel:");
                        int idx = input.nextInt();
                        if (idx >= 0 && idx < this.inventory.size()) {
                            Item it = this.inventory.get(idx);
                            inspectItemDetails(it);
                        } else {
                            System.out.println("Cancelled or invalid index.");
                        }
                    }
                } else {
                    System.out.println("Invalid source.");
                }
                System.out.println("(Inspect viewed — this does not consume your turn.) Choose an action:");
                Player.showGlobalActions();
                try { playerResponse = input.nextInt(); } catch (Exception e) { input.nextLine(); playerResponse = 0; }
                continue;
            } else if (playerResponse == 9) {
                // save game (informational-ish)
                System.out.println("Saving game...");
                Main.saveGame(players, floor);
                System.out.println("(Save completed — this does not consume your turn.) Choose an action:");
                Player.showGlobalActions();
                try { playerResponse = input.nextInt(); } catch (Exception e) { input.nextLine(); playerResponse = 0; }
                continue;
            } else if (playerResponse == 10) {
                // load game (informational-ish) — cannot safely replace the current floor here
                System.out.println("Load attempted from in-battle action is not performed. Use the main menu to load a save.");
                System.out.println("(Load ignored — this does not consume your turn.) Choose an action:");
                Player.showGlobalActions();
                try { playerResponse = input.nextInt(); } catch (Exception e) { input.nextLine(); playerResponse = 0; }
                continue;
            }

            // From here on, actions consume the player's turn. Handle them and break the loop.
            if (playerResponse == 0) {

                switch (this.getPlayerJob()) {
                    case "WARRIOR" -> {
                        act_choice = chooseSkillForJob.applyAsInt(0);
                        System.out.println("Which monster do you want to attack");
                        Monster[] floorMonsters = floor.getMonsters();
                        for (int j = 0; j < floorMonsters.length; j++) {
                            System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                        }
                        monster_to_atk = input.nextInt();
                        Player.attack(current_player, act_choice, monster_to_atk, floor);
                    }
                    case "MAGE" -> {
                        act_choice = chooseSkillForJob.applyAsInt(0);
                        System.out.println("Which monster do you want to attack");
                        Monster[] floorMonsters = floor.getMonsters();
                        for (int j = 0; j < floorMonsters.length; j++) {
                            System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                        }
                        monster_to_atk = input.nextInt();
                        Player.attack(current_player, act_choice, monster_to_atk, floor);
                    }
                    case "THIEF" -> {
                        act_choice = chooseSkillForJob.applyAsInt(0);
                        System.out.println("Which monster do you want to attack");
                        Monster[] floorMonsters = floor.getMonsters();
                        for (int j = 0; j < floorMonsters.length; j++) {
                            System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                        }
                        monster_to_atk = input.nextInt();
                        Player.attack(current_player, act_choice, monster_to_atk, floor);
                    }
                    case "SWORDSMAN" -> {
                        act_choice = chooseSkillForJob.applyAsInt(0);
                        System.out.println("Which monster do you want to attack");
                        Monster[] floorMonsters = floor.getMonsters();
                        for (int j = 0; j < floorMonsters.length; j++) {
                            System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                        }
                        monster_to_atk = input.nextInt();
                        Player.attack(current_player, act_choice, monster_to_atk, floor);
                    }
                }

                turnConsumed = true;

            } else if (playerResponse == 6) {
                // Choose which skill to use
                act_choice = 0;
                System.out.println("Choose a skill to use:");
                switch (this.getPlayerJob()) {
                    case "WARRIOR" -> printSkillOptions(warrior_act);
                    case "MAGE" -> printSkillOptions(mage_act);
                    case "THIEF" -> printSkillOptions(thief_act);
                    case "SWORDSMAN" -> printSkillOptions(swordsman_act);
                    default -> System.out.println("No skills available for this job.");
                }
                try { act_choice = input.nextInt(); } catch (Exception e) { input.nextLine(); act_choice = 0; }

                // Use job-specific skill
                switch (this.getPlayerJob()) {
                    case "WARRIOR" -> {
                        Skill skill = getSkillByIndex(warrior_act, act_choice);
                        if (skill == null) { System.out.println("Invalid skill choice."); break; }
                        int cost = skill.cost;
                        if (this.getManaBar() < cost) { System.out.println("Not enough MP to use " + getSkillNameByIndex(warrior_act, act_choice)); break; }
                        this.setManaBar(this.getManaBar() - cost);
                        Monster[] mons = floor.getMonsters();
                        System.out.println(this.getPlayerName() + " uses " + getSkillNameByIndex(warrior_act, act_choice) + "! Hitting all monsters.");
                        for (int mi = 0; mi < mons.length; mi++) {
                            if (mons[mi] == null || mons[mi].getRemainingHealth() <= 0) continue;
                            int dmg = this.getStrength() * 60 + new java.util.Random().nextInt(101) + this.getTotalEquipBoost() * 8 + this.getPlayerLevel() * 10;
                            // include weapon-specific bonus if present
                            try { Equipment w = this.equipped.get("hands"); if (w != null) dmg += Math.max(0, w.statBoost) * 6; } catch (Exception ignored) {}
                            Item drop = mons[mi].takeDamage(dmg);
                            if (drop != null) { this.addItem(drop); System.out.println("Looted: " + drop.itemName); }
                        }
                    }
                    case "MAGE" -> {
                        Skill skill = getSkillByIndex(mage_act, act_choice);
                        if (skill == null) { System.out.println("Invalid skill choice."); break; }
                        int cost = skill.cost;
                        if (this.getManaBar() < cost) { System.out.println("Not enough MP to cast " + getSkillNameByIndex(mage_act, act_choice)); break; }
                        this.setManaBar(this.getManaBar() - cost);
                        int heal = this.getInsight() * 8 + new java.util.Random().nextInt(21);
                        System.out.println(this.getPlayerName() + " casts " + getSkillNameByIndex(mage_act, act_choice) + " for " + heal + " HP.");
                        if (players != null) {
                            for (Player pp : players) {
                                if (pp.getHealthBar() > 0) pp.setHealthBar(pp.getHealthBar() + heal);
                            }
                        } else {
                            this.setHealthBar(this.getHealthBar() + heal);
                        }
                    }
                    case "THIEF" -> {
                        Skill skill = getSkillByIndex(thief_act, act_choice);
                        if (skill == null) { System.out.println("Invalid skill choice."); break; }
                        int cost = skill.cost;
                        if (this.getManaBar() < cost) { System.out.println("Not enough MP to use " + getSkillNameByIndex(thief_act, act_choice)); break; }
                        Monster[] mons = floor.getMonsters();
                        int target = -1;
                        for (int i = 0; i < mons.length; i++) if (mons[i] != null && mons[i].getRemainingHealth() > 0) { target = i; break; }
                        if (target == -1) { System.out.println("No valid monster to backstab."); break; }
                        this.setManaBar(this.getManaBar() - cost);
                        int dmg = this.getAgility() * 48 + new java.util.Random().nextInt(41) + this.getTotalEquipBoost() * 6 + this.getPlayerLevel() * 8;
                        System.out.println(this.getPlayerName() + " performs " + getSkillNameByIndex(thief_act, act_choice) + " on monster index " + target + " for " + dmg + " damage.");
                        Item drop = mons[target].takeDamage(dmg);
                        if (drop != null) { this.addItem(drop); System.out.println("Backstab loot: " + drop.itemName); }
                    }
                    case "SWORDSMAN" -> {
                        Skill skill = getSkillByIndex(swordsman_act, act_choice);
                        if (skill == null) { System.out.println("Invalid skill choice."); break; }
                        int cost = skill.cost;
                        if (this.getManaBar() < cost) { System.out.println("Not enough MP to use " + getSkillNameByIndex(swordsman_act, act_choice)); break; }
                        Monster[] mons = floor.getMonsters();
                        int target = -1;
                        for (int i = 0; i < mons.length; i++) if (mons[i] != null && mons[i].getRemainingHealth() > 0) { target = i; break; }
                        if (target == -1) { System.out.println("No valid monster to strike."); break; }
                        this.setManaBar(this.getManaBar() - cost);
                        int dmg = this.getStrength() * 66 + new java.util.Random().nextInt(61) + this.getTotalEquipBoost() * 8 + this.getPlayerLevel() * 10;
                        System.out.println(this.getPlayerName() + " uses " + getSkillNameByIndex(swordsman_act, act_choice) + " on monster index " + target + " for " + dmg + " damage.");
                        Item drop = mons[target].takeDamage(dmg);
                        if (drop != null) { this.addItem(drop); System.out.println("Looted: " + drop.itemName); }
                    }
                    default -> System.out.println("No skill defined for this job.");
                }

                turnConsumed = true;

            } else if (playerResponse == 4) {
                // Equip gear from inventory (this consumes a turn)
                List<Integer> equipmentIndices = new ArrayList<>();
                for (int i = 0; i < inventory.size(); i++) {
                    Item it = inventory.get(i);
                    if (it instanceof Equipment) equipmentIndices.add(i);
                }
                if (equipmentIndices.isEmpty()) {
                    System.out.println("No equipment in inventory to equip.");
                } else {
                    System.out.println("Choose equipment index to equip:");
                    for (int idx : equipmentIndices) {
                        Equipment eq = (Equipment) inventory.get(idx);
                        System.out.println(idx + " => " + eq.itemName + " (slot: " + eq.bodyPOS + ") - boost: " + eq.statBoost);
                    }
                    int choice = input.nextInt();
                    if (choice < 0 || choice >= inventory.size()) {
                        System.out.println("Invalid choice.");
                    } else {
                        Item chosen = inventory.get(choice);
                        if (chosen instanceof Equipment) {
                            this.equipFromInventory(choice);
                        } else {
                            System.out.println("Selected item is not equipment.");
                        }
                    }
                }
                turnConsumed = true;

            } else if (playerResponse == 5) {
                // Drink potion / use consumable from player inventory (consumes turn)
                List<Integer> consumableIndices = new ArrayList<>();
                for (int i = 0; i < inventory.size(); i++) {
                    Item it = inventory.get(i);
                    if (it instanceof Consumable || "Consumable".equals(it.itemType)) {
                        consumableIndices.add(i);
                    }
                }
                if (consumableIndices.isEmpty()) {
                    System.out.println("You have no consumables in your inventory.");
                } else {
                    System.out.println("Choose a consumable to use (index):");
                    for (int idx : consumableIndices) {
                        Item it = inventory.get(idx);
                        System.out.println(idx + " => " + it.itemName + " (" + it.itemType + ") - boost: " + it.statBoost);
                    }
                    int choice = input.nextInt();
                    if (choice < 0 || choice >= inventory.size()) {
                        System.out.println("Invalid choice.");
                    } else {
                        Item chosen = inventory.get(choice);
                        if (chosen instanceof Consumable) {
                            ((Consumable) chosen).use(current_player);
                            inventory.remove(choice);
                        } else if ("Consumable".equals(chosen.itemType)) {
                            Consumable c = new Consumable(chosen.itemName, chosen.itemType, floor.getFloorNumber());
                            c.use(current_player);
                            inventory.remove(choice);
                        } else {
                            System.out.println("Selected item is not consumable.");
                        }
                    }
                }
                turnConsumed = true;

            } else if (playerResponse == 7) {
                // Pickup items from the floor into player inventory (consumes turn)
                java.util.List<Item> floorItems = floor.getItems();
                if (floorItems == null || floorItems.isEmpty()) {
                    System.out.println("There are no items on the floor to pick up.");
                } else {
                    System.out.println("Items on floor:");
                    for (int i = 0; i < floorItems.size(); i++) {
                        Item it = floorItems.get(i);
                        if (it instanceof Equipment) {
                            Equipment eq = (Equipment) it;
                            System.out.println(i + " => " + it.itemName + " (" + it.itemType + ") - slot: " + eq.bodyPOS + " - rank: " + it.itemRank + " - computed boost: " + eq.computeStatDelta());
                        } else {
                            System.out.println(i + " => " + it.itemName + " (" + it.itemType + ") - rank: " + it.itemRank + " boost: " + it.statBoost);
                        }
                    }
                    System.out.println("Enter the index of the item to pick up, or -1 to cancel:");
                    int choice = input.nextInt();
                    if (choice >= 0 && choice < floorItems.size()) {
                        Item picked = floorItems.remove(choice);
                        this.addItem(picked);
                        System.out.println("Picked up: " + picked.itemName + " added to inventory.");
                    } else {
                        System.out.println("Pickup cancelled or invalid index.");
                    }
                }
                turnConsumed = true;

            } else {
                // Unknown action index: treat as informational and re-prompt
                System.out.println("Unknown action. Choose an action:");
                Player.showGlobalActions();
                try { playerResponse = input.nextInt(); } catch (Exception e) { input.nextLine(); playerResponse = 0; }
            }
        }

    }

}