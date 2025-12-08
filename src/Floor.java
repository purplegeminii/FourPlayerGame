/**
 * Floor represents a level in the tower and holds monsters and items.
 * It provides simple helpers to retrieve monsters and check if the floor is cleared.
 * @author Copilot
 */
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Floor {
    private int floorNumber;
    private Monster[] monsters;
    private ArrayList<Item> items;
    private Random rand = new Random();

    /**
     * Construct a Floor with the given number and number of monsters.
     * @param floorNumber the floor index (1-based)
     * @param monsterCount how many monsters to spawn on this floor
     */
    public Floor(int floorNumber, int monsterCount) {
        this(floorNumber, monsterCount, 0);
    }

    /**
     * Construct a Floor with the given number of monsters and items.
     * Monsters' difficulty scales based on the floorNumber.
     * @param floorNumber 1-based floor number
     * @param monsterCount how many monsters to spawn on this floor
     * @param itemCount how many items to spawn on this floor
     */
    public Floor(int floorNumber, int monsterCount, int itemCount) {
        this.floorNumber = floorNumber;
        this.monsters = new Monster[monsterCount];
        for (int i = 0; i < monsterCount; i++) {
            // spawn monsters with difficulty equal to floorNumber
            this.monsters[i] = new Monster(this.floorNumber);
        }

        this.items = new ArrayList<>();
        String[] possibleNames = {"Health Potion", "Mana Potion", "Elixir", "Bronze Sword", "Iron Shield", "Swift Boots", "Mystic Robe"};
        String[] possibleTypes = {"Consumable", "Consumable", "Consumable", "Equipment", "Equipment", "Equipment", "Equipment"};
        String[] poss = {"head", "chest", "legs", "hands", "feet"};
        for (int j = 0; j < itemCount; j++) {
            int idx = rand.nextInt(possibleNames.length);
            String name = possibleNames[idx];
            String type = possibleTypes[idx];
            if (type.equals("Equipment")) {
                String pos = poss[rand.nextInt(poss.length)];
                // use Equipment(name, pos, floorNumber) constructor to set a custom name and scale stats
                this.items.add(new Equipment(name, pos, this.floorNumber));
            } else if (type.equals("Consumable")) {
                // Consumables items will be scaled by floor number — create Consumable instances
                this.items.add(new Consumable(name, type, this.floorNumber));
            } else {
                // generic items will be scaled by floor number — create Consumable instances
                this.items.add(new Item(name, type, this.floorNumber));
            }
        }
    }

    public int getFloorNumber() {
        return this.floorNumber;
    }

    public Monster[] getMonsters() {
        return this.monsters;
    }

    public List<Item> getItems() {
        return this.items;
    }

    public boolean isCleared() {
        return Monster.allMonstersDead(this.monsters);
    }

    public void displayMonsters() {
        System.out.println("Monsters on floor " + this.floorNumber + ":");
        for (int i = 0; i < this.monsters.length; i++) {
            System.out.println(i + " => " + this.monsters[i].displayMonsterName());
        }
    }

    public void displayItems() {
        System.out.println("Items on floor " + this.floorNumber + ":");
        if (this.items == null || this.items.size() == 0) {
            System.out.println("  (no items)");
            return;
        }
        for (int i = 0; i < this.items.size(); i++) {
            Item it = this.items.get(i);
            if (it instanceof Equipment) {
                Equipment eq = (Equipment) it;
                System.out.println(i + " => " + it.itemName + " (" + it.itemType + ") - slot: " + eq.bodyPOS + " - rank: " + it.itemRank + " - computed boost: " + eq.computeStatDelta());
            } else {
                System.out.println(i + " => " + it.itemName + " (" + it.itemType + ") - rank: " + it.itemRank + " boost: " + it.statBoost);
            }
        }
    }

    public void addItem(Item it) {
        this.items.add(it);
    }
}
