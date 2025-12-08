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
    int statBoost;
    Random rand = new Random();
    public Item(String name, String type) {
        this(name, type, 1);
    }

    /**
     * Create an item with stats scaled to the floor number.
     * @param name item name
     * @param type item type
     * @param floorNumber floor used to scale item quality
     */
    public Item(String name, String type, int floorNumber) {
        this.itemName = name;
        this.itemType = type;
        // Ensure we use the array length to pick a valid random rank
        this.itemRank = Item.ranks[rand.nextInt(Item.ranks.length)];

        // statBoost depends on rank and floorNumber
        int base;
        switch (this.itemRank) {
            case "black" -> base = 3;
            case "gold" -> base = 6;
            case "purple" -> base = 10;
            default -> base = 1; // white
        }
        // small randomness plus scaling by floor
        this.statBoost = base * floorNumber + rand.nextInt(Math.max(1, base * floorNumber));
    }

    public void setItemRank(String rank) {
        this.itemRank = rank;
    }

    public void setStatBoost(int boost) {
        this.statBoost = boost;
    }

    public void setItemType(String type) {
        this.itemType = type;
    }

}
