/**
 * @author Delali Nsiah-Asare
 * @verison 1.0.0
 */

import java.util.Random;
public class Monster {
    Random rand = new Random();

    private String monsterName;
    private final String[] ranks = {"normal", "general", "lord"};
    private String monsterRank;
    private int monsterHP;
    private int monsterMP;
    private int damage;

    public Monster(int diff) {
        if (diff == 1) {
            this.monsterName = "Goblin";
            this.monsterRank = this.getMonsterRank();
            int minDamage = 20;
            int maxDamage = 60;
            this.damage = (int)Math.floor(Math.random() * (maxDamage - minDamage + 1) + minDamage);
            int minHP = 100;
            int maxHP = 1000;
            this.monsterHP = (int)Math.floor(Math.random() * (maxHP - minHP + 1) + minHP);
            int minMP = 10;
            int maxMP = 200;
            this.monsterMP = (int)Math.floor(Math.random() * (maxMP - minMP + 1) + minMP);
        }
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

    public static void attack(int player_to_atk, int monster_dealing_dmg, Player[] players, Monster[] floorMonsters) {

        for (int i=0; i < floorMonsters.length; i++) {
            Monster current_mob;
            Player current_player;
            int dmg_received;
            int new_playerHP;
            if (monster_dealing_dmg == i) {
                if (player_to_atk == 0) {
                    current_mob = floorMonsters[i];
                    dmg_received = current_mob.getDamage();
                    current_player = players[0];
                    new_playerHP = current_player.getHealthBar() - dmg_received;
                    current_player.setHealthBar(new_playerHP);
                    System.out.println(current_mob.getMonsterName()+" is attacking "+current_player.getPlayerName());
                    System.out.println(current_player.getPlayerName() + " has lost " + dmg_received + " HP");
                    break;
                } else if (player_to_atk == 1) {
                    current_mob = floorMonsters[i];
                    dmg_received = current_mob.getDamage();
                    current_player = players[1];
                    new_playerHP = current_player.getHealthBar() - dmg_received;
                    current_player.setHealthBar(new_playerHP);
                    System.out.println(current_mob.getMonsterName()+" is attacking "+current_player.getPlayerName());
                    System.out.println(current_player.getPlayerName() + " has lost " + dmg_received + " HP");
                    break;
                } else if (player_to_atk == 2) {
                    current_mob = floorMonsters[i];
                    dmg_received = current_mob.getDamage();
                    current_player = players[2];
                    new_playerHP = current_player.getHealthBar() - dmg_received;
                    current_player.setHealthBar(new_playerHP);
                    System.out.println(current_mob.getMonsterName()+" is attacking "+current_player.getPlayerName());
                    System.out.println(current_player.getPlayerName() + " has lost " + dmg_received + " HP");
                    break;
                } else if (player_to_atk == 3) {
                    current_mob = floorMonsters[i];
                    dmg_received = current_mob.getDamage();
                    current_player = players[3];
                    new_playerHP = current_player.getHealthBar() - dmg_received;
                    current_player.setHealthBar(new_playerHP);
                    System.out.println(current_mob.getMonsterName()+" is attacking "+current_player.getPlayerName());
                    System.out.println(current_player.getPlayerName() + " has lost " + dmg_received + " HP");
                    break;
                }
            }
        }

    }

    /**
     * Static method that checks if all the monsters are dead in a particular floor
     * @param floorMonsters
     * @return boolean
     */
    public static boolean allMonstersDead(Monster[] floorMonsters) {
        int yes = 0;
        for (Monster floorMonster : floorMonsters)
            if (floorMonster.monsterHP == 0) {
                yes++;
            }
        return yes == floorMonsters.length;
    }

}