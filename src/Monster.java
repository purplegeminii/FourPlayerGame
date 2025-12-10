/**
 * @author Delali Nsiah-Asare
 * @verison 1.0.0
 */

import java.util.*;
public class Monster {
    Random rand = new Random();

    private String monsterName;
    private int floorNumber;
    private final String[] types = {"Slime", "Goblin", "Skeleton", "Orc", "Troll", "Wraith"};
    private final String[] ranks = {"normal", "general", "lord"};
    private String monsterRank;
    private int monsterHP;
    private int maxHP;
    private int monsterMP;
    private int damage;
    private final int DIFFICULTY_SCALE_DIV = 4;  // bigger means weaker monsters
    

    public Monster(int diff) {
        // record floor/difficulty
        this.floorNumber = diff;

        // Choose a monster type. Allow more variety at higher floors (diff).
        int availableTypes = Math.min(types.length, Math.max(1, diff + 1));
        this.monsterName = types[rand.nextInt(availableTypes)];
        this.monsterRank = this.getMonsterRank();

        // Base ranges
        int baseMinDamage = 20;
        int baseMaxDamage = 60;
        int baseMinHP = 100;
        int baseMaxHP = 1000;
        int baseMinMP = 10;
        int baseMaxMP = 200;

        // Scale ranges by difficulty (diff >= 1). Higher floors = tougher monsters.
        // Reduce difficulty scaling: divide floor-based scaling by 2 to make monsters weaker
        int minDamage = Math.max(1, (baseMinDamage * diff) / DIFFICULTY_SCALE_DIV);
        int maxDamage = Math.max(minDamage, (baseMaxDamage * diff) / DIFFICULTY_SCALE_DIV);
        this.damage = rand.nextInt(Math.max(1, maxDamage - minDamage + 1)) + minDamage;

        int minHP = Math.max(1, (baseMinHP * diff) / DIFFICULTY_SCALE_DIV);
        int maxHP = Math.max(minHP, (baseMaxHP * diff) / DIFFICULTY_SCALE_DIV);
        this.monsterHP = rand.nextInt(Math.max(1, maxHP - minHP + 1)) + minHP;
        // record initial HP as max HP for UI/proportion purposes
        this.maxHP = this.monsterHP;

        int minMP = Math.max(0, (baseMinMP * diff) / DIFFICULTY_SCALE_DIV);
        int maxMP = Math.max(minMP, (baseMaxMP * diff) / DIFFICULTY_SCALE_DIV);
        this.monsterMP = rand.nextInt(Math.max(1, maxMP - minMP + 1)) + minMP;
    }

    public int getDamage() {
        return this.damage;
    }

    public String getMonsterName() {
        return this.monsterRank +" "+ this.monsterName;
    }

    public String getMonsterRank() {
        int n = rand.nextInt(ranks.length);
        return ranks[n];
    }

    public void monsterStatus() {
        System.out.println(
            "Monster Name: "+this.monsterName+"\n"+
            "Rank: "+this.getMonsterRank()+"\n"+
            "HP: "+this.monsterHP+"\n"+
            "MP: "+this.monsterMP
        );
    }

    public String displayMonsterName() {
        return this.monsterRank + " " + this.monsterName;
    }

    public int getRemainingHealth() {
        return this.monsterHP;
    }

    // Return the monster's initial / maximum health (set when the monster was created)
    public int getMaxHealth() {
        return Math.max(1, this.maxHP);
    }

    // XP reward for defeating this monster
    public int getXpReward() {
        int base = Math.max(1, this.floorNumber * 10);
        int extra = rand.nextInt(Math.max(1, this.floorNumber * 5));
        int total = base + extra;
        // Increase reward for tougher ranks
        if (this.monsterRank != null) {
            if (this.monsterRank.contains("general")) {
                total += Math.max(1, this.floorNumber * 10);
            } else if (this.monsterRank.contains("lord")) {
                total += Math.max(1, this.floorNumber * 25);
            }
        }
        return total;
    }

    /**
     * Apply damage to this monster. If the monster dies, return a dropped Item (or null).
     * @param damageAmount damage dealt
     * @return Item dropped or null
     */
    public Item takeDamage(int damageAmount) {
        this.monsterHP -= damageAmount;
        System.out.println(this.getMonsterName() + " took " + damageAmount + " damage. Remaining HP: " + Math.max(0, this.monsterHP));
        if (this.monsterHP <= 0) {
            System.out.println(this.getMonsterName() + " has been defeated!");
            // drop logic: chance-based
            int chance = rand.nextInt(100);
            if (chance < 60) { // 60% consumable
                String[] result = new String[] {"Health", "Mana"};
                String chosen = result[rand.nextInt(result.length)];
                return new Consumable(String.format("%s Potion", chosen), "Consumable", this.floorNumber);
            } else if (chance < 90) { // 30% equipment
                String[] eqNames = {"Bronze Sword", "Iron Shield", "Mystic Robe", "Swift Boots"};
                String name = eqNames[rand.nextInt(eqNames.length)];
                // choose slot using the data-driven mapping: first matching substring key
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
                return new Equipment(name, pos, this.floorNumber);
            } else {
                return null; // no drop
            }
        }
        return null;
    }

    public static void attack(int player_to_atk, int monster_dealing_dmg, Player[] players, Monster[] floorMonsters) {
        // Find the monster that is dealing damage (index: monster_dealing_dmg)
        if (monster_dealing_dmg < 0 || monster_dealing_dmg >= floorMonsters.length) return;
        Monster current_mob = floorMonsters[monster_dealing_dmg];
        if (current_mob == null) return;

        // Validate target player index
        if (player_to_atk < 0 || player_to_atk >= players.length) {
            // invalid player index; default to first alive player
            int fallback = -1;
            for (int pi = 0; pi < players.length; pi++) {
                if (players[pi].getHealthBar() > 0) { fallback = pi; break; }
            }
            if (fallback == -1) return; // no alive players
            player_to_atk = fallback;
        }

        Player current_player = players[player_to_atk];
        int dmg_received = current_mob.getDamage();
        int new_playerHP = current_player.getHealthBar() - dmg_received;
        current_player.setHealthBar(new_playerHP);
        System.out.println(current_mob.getMonsterName() + " is attacking " + current_player.getPlayerName());
        System.out.println(current_player.getPlayerName() + " has lost " + dmg_received + " HP");
        
    }

    /**
     * Static method that checks if all the monsters are dead in a particular floor
     * @param floorMonsters
     * @return boolean
     */
    public static boolean allMonstersDead(Monster[] floorMonsters) {
        int yes = 0;
        for (Monster floorMonster : floorMonsters)
            if (floorMonster.monsterHP <= 0) {
                yes++;
            }
        return yes == floorMonsters.length;
    }

}