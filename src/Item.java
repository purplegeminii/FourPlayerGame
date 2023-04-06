/**
 * @author Delali Nsiah-Asare
 * @version 1.0.0
 */

import java.util.Random;
public class Item {
    String itemName;
    String itemType;
    static String[] ranks = {"white", "black", "gold", "purple"};
    String itemRank;
    Random rand = new Random();
    public Item(String name, String type) {
        this.itemName = name;
        this.itemType = type;
        this.itemRank = Item.ranks[rand.nextInt(5)];
    }

}
