/**
 * Consumable item that can be used by a player to restore HP or MP.
 */
public class Consumable extends Item {
    private String effect; // "health" or "mana"

    public Consumable(String name, String type, int floorNumber) {
        super(name, type, floorNumber);
        String n = name.toLowerCase();
        if (n.contains("mana")) {
            this.effect = "mana";
        } else {
            this.effect = "health";
        }
    }

    /**
     * Use the consumable on the given player.
     * @param p player using the consumable
     */
    public void use(Player p) {
        if (this.effect.equals("health")) {
            int newHP = p.getHealthBar() + this.statBoost;
            p.setHealthBar(newHP);
            System.out.println(p.getPlayerName() + " used " + this.itemName + " and recovered " + this.statBoost + " HP.");
        } else if (this.effect.equals("mana")) {
            int newMP = p.getManaBar() + this.statBoost;
            p.setManaBar(newMP);
            System.out.println(p.getPlayerName() + " used " + this.itemName + " and recovered " + this.statBoost + " MP.");
        } else {
            System.out.println("Nothing happened.");
        }
    }

}
