/**
 * @author Delali Nsiah-Asare
 * @version 1.0.0
 */

import java.util.Random;
import java.util.Scanner;
public class Player {
    Scanner input = new Scanner(System.in);
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
    private static final String[] jobs = {"WARRIOR", "MAGE", "THIEF", "SWORDSMAN"};
    private String playerJob;
    private static final String[] global_actions = {
            "attack", "status", "pause", "inventory", "equip gear", "drink potion", "use skill"
    };
    private static final String[] warrior_act = {"punch", "bull charge"};
    private static final String[] mage_act = {"elemental blast", "team heal"};
    private static final String[] thief_act = {"back stab", "mana poison"};
    private static final String[] swordsman_act = {"slash", "critical seal"};

    Random rand = new Random();

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
        int vit = this.getVitality() * 8;
        int str = this.getStrength() * 6;
        this.healthBar = (vit + str)*10;
    }

    public void setHealthBar(int newHP) {
        this.healthBar = newHP;
    }

    public int getHealthBar() {
        return this.healthBar;
    }

    public void setManaBar() {
        int ins = this.getInsight() * 3;
        int tal = this.getTalent() * 2;
        this.manaBar = (ins + tal)*5;
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
        System.out.println(
                        "\n"+
                        "player1: " + players[0].getPlayerName() + " awakened as a " + players[0].getPlayerJob() + "\n" +
                        "player2: " + players[1].getPlayerName() + " awakened as a " + players[1].getPlayerJob() + "\n" +
                        "player3: " + players[2].getPlayerName() + " awakened as a " + players[2].getPlayerJob() + "\n" +
                        "player4: " + players[3].getPlayerName() + " awakened as a " + players[3].getPlayerJob() + "\n"
        );
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
                         + "________________________________________"
                );
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
        if (
                players[0].getHealthBar() == 0 &&
                players[1].getHealthBar() == 0 &&
                players[2].getHealthBar() == 0 &&
                players[3].getHealthBar() == 0
        ) {
            System.out.println("Every player has died... GAME OVER !!!");
        }
    }

    public static void showPauseScreen(Player current_player) {
        Scanner keyboard = new Scanner(System.in);
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

    /**
     * static method performs the attack actions of a player
     * @param current_player current player
     * @param act_choice player's choice of action
     * @param monster_to_atk index of monster to attack
     * @param floorMonsters array of monsters on a floor
     */
    public static void attack(Player current_player, int act_choice, int monster_to_atk, Monster[] floorMonsters) {
        System.out.println(current_player.getPlayerName()+" is attacking "+floorMonsters[monster_to_atk].getMonsterName());

        switch (current_player.getPlayerJob()) {
            case "WARRIOR" -> {
                if (act_choice == 0) {
                    System.out.println(current_player.getPlayerName() + " has used " + warrior_act[act_choice]);
                } else if (act_choice == 1) {
                    System.out.println(current_player.getPlayerName() + " has used " + warrior_act[act_choice]);
                }
            }
            case "MAGE" -> {
                if (act_choice == 0) {
                    System.out.println(current_player.getPlayerName() + " has used " + mage_act[act_choice]);
                } else if (act_choice == 1) {
                    System.out.println(current_player.getPlayerName() + " has used " + mage_act[act_choice]);
                }
            }
            case "THIEF" -> {
                if (act_choice == 0) {
                    System.out.println(current_player.getPlayerName() + " has used " + thief_act[act_choice]);
                } else if (act_choice == 1) {
                    System.out.println(current_player.getPlayerName() + " has used " + thief_act[act_choice]);
                }
            }
            case "SWORDSMAN" -> {
                if (act_choice == 0) {
                    System.out.println(current_player.getPlayerName() + " has used " + swordsman_act[act_choice]);
                } else if (act_choice == 1) {
                    System.out.println(current_player.getPlayerName() + " has used " + swordsman_act[act_choice]);
                }
            }
        }

    }

    /**
     * non-static method to perform player actions based on the job they awakened with
     * @param current_player current player
     * @param playerResponse player's response
     * @param floorMonsters array of monsters on a floor
     */
    public void performAction(Player current_player, int playerResponse, Monster[] floorMonsters) {
        int act_choice;
        int monster_to_atk;

        if (playerResponse == 0) {

            switch (this.getPlayerJob()) {
                case "WARRIOR" -> {
                    System.out.println("\nChoose from this list of actions: ");
                    for (int i = 0; i < warrior_act.length; i++) {
                        System.out.println(i + " => " + warrior_act[i]);
                    }
                    act_choice = input.nextInt();
                    System.out.println("Which monster do you want to attack");
                    for (int j = 0; j < floorMonsters.length; j++) {
                        System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                    }
                    monster_to_atk = input.nextInt();
                    Player.attack(current_player, act_choice, monster_to_atk, floorMonsters);
                }
                case "MAGE" -> {
                    System.out.println("\nChoose from this list of actions: ");
                    for (int i = 0; i < mage_act.length; i++) {
                        System.out.println(i + " => " + mage_act[i]);
                    }
                    act_choice = input.nextInt();
                    System.out.println("Which monster do you want to attack");
                    for (int j = 0; j < floorMonsters.length; j++) {
                        System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                    }
                    monster_to_atk = input.nextInt();
                    Player.attack(current_player, act_choice, monster_to_atk, floorMonsters);
                }
                case "THIEF" -> {
                    System.out.println("\nChoose from this list of actions: ");
                    for (int i = 0; i < thief_act.length; i++) {
                        System.out.println(i + " => " + thief_act[i]);
                    }
                    act_choice = input.nextInt();
                    System.out.println("Which monster do you want to attack");
                    for (int j = 0; j < floorMonsters.length; j++) {
                        System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                    }
                    monster_to_atk = input.nextInt();
                    Player.attack(current_player, act_choice, monster_to_atk, floorMonsters);
                }
                case "SWORDSMAN" -> {
                    System.out.println("\nChoose from this list of actions: ");
                    for (int i = 0; i < swordsman_act.length; i++) {
                        System.out.println(i + " => " + swordsman_act[i]);
                    }
                    act_choice = input.nextInt();
                    System.out.println("Which monster do you want to attack");
                    for (int j = 0; j < floorMonsters.length; j++) {
                        System.out.println(j + " => " + floorMonsters[j].displayMonsterName());
                    }
                    monster_to_atk = input.nextInt();
                    Player.attack(current_player, act_choice, monster_to_atk, floorMonsters);
                }
            }

        } else if (playerResponse == 1) {
            this.shortPlayerStatus();

        } else if (playerResponse == 2) {
            System.out.println("PLAYER HAS PAUSED THE GAME!!!");
            Player.showPauseScreen(current_player);

        } else if (playerResponse == 3) {
            System.out.println("Player inventory");

        } else if (playerResponse == 4) {
            System.out.println("Equip gear");

        } else if (playerResponse == 5) {
            System.out.println("Drink potion");

        } else if (playerResponse == 6) {
            System.out.println("Activate skill");
        }

    }

}