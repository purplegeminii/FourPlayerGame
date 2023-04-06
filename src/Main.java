/**
 * @author Delali Nsiah-Asare
 * @version 1.0.0
 */

import java.util.Scanner;
import java.util.Random;
public class Main {

    /**
     * Main method to play the game
     * @param args array args of class String
     */
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        Random rand = new Random();

        Player p1 = new Player();
        Player p2 = new Player();
        Player p3 = new Player();
        Player p4 = new Player();
        Player[] players = {p1, p2, p3, p4};

        int player_to_atk;
        int monster_dealing_dmg;


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
        Player.beginnerSummary(players);



        // Floor 1
        System.out.println("All players arrived on the first floor of the tower");
        System.out.println("Lurking monsters have noticed your presence");
        Monster m1 = new Monster(1);
        Monster m2 = new Monster(1);
        Monster m3 = new Monster(1);
        Monster m4 = new Monster(1);
        Monster[] floor1monsters = {m1, m2, m3, m4};
        player_to_atk = rand.nextInt(4);
        monster_dealing_dmg = rand.nextInt(4);
        Monster.attack(player_to_atk, monster_dealing_dmg, players, floor1monsters);

        while (!(Monster.allMonstersDead(floor1monsters))) {

            for (Player player : players) {
                System.out.println("\n");
                System.out.println(player.getPlayerName() + ", what action do you want to perform?");
                Player.showGlobalActions();
                System.out.println("Note that choosing \"status\" would mean giving up your turn");
                int player_response = input.nextInt();
                player.performAction(player, player_response, floor1monsters);
                player_to_atk = rand.nextInt(4);
                monster_dealing_dmg = rand.nextInt(4);
                Monster.attack(player_to_atk, monster_dealing_dmg, players, floor1monsters);
                Player.gameStatus(players);
            }

        }



    }
}