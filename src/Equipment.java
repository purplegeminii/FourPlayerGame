/**
 * @author Delali Nsiah-Asare
 * @version 1.0.0
 */
import java.util.*;
// import java.io.*;

public class Equipment extends Item{
    String bodyPOS;

    // data-driven equipment->slot mapping loaded from `config/equipment_slots.properties`
    public static final Map<String,String> EQUIP_SLOT_MAP = loadEquipSlotMap();

    // Expose a read-only view if consumers prefer a getter
    public static Map<String,String> getEquipSlotMap() { return EQUIP_SLOT_MAP; }

    public Equipment(String pos) {
        super("Moon_Tear_Set", "Equipment");
        this.bodyPOS = pos;
    }

    /**
     * Create equipment with a custom name and body position.
     * @param name equipment name
     * @param pos body position
     */
    public Equipment(String name, String pos) {
        super(name, "Equipment");
        this.bodyPOS = pos;
    }

    /**
     * Create equipment with a custom name, position and floor-scaling.
     * @param name equipment name
     * @param pos body position
     * @param floorNumber floor to scale stats by
     */
    public Equipment(String name, String pos, int floorNumber) {
        super(name, "Equipment", floorNumber);
        this.bodyPOS = pos;
    }

    private static Map<String,String> loadEquipSlotMap() {
        Map<String,String> map = new LinkedHashMap<>();
        // default fallbacks
        map.put("sword", "hands");
        map.put("shield", "hands");
        map.put("robe", "chest");
        map.put("boots", "feet");
        map.put("helmet", "head");
        map.put("helm", "head");
        map.put("gauntlet", "hands");
        map.put("glove", "hands");
        map.put("ring", "finger");
        map.put("amulet", "neck");

        java.io.File cfg = new java.io.File("config/equipment_slots.properties");
        if (!cfg.exists()) return map;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(cfg))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim().toLowerCase();
                String val = line.substring(eq+1).trim().toLowerCase();
                if (!key.isEmpty() && !val.isEmpty()) map.put(key, val);
            }
        } catch (Exception e) {
            System.out.println("Warning: could not read equipment slot mapping: " + e.getMessage());
        }
        return map;
    }

    /**
     * Compute a stat delta when this equipment is equipped.
     * Different body positions use slightly different formulas, and the item's rank
     * increases the resulting boost.
     * @return integer stat delta to apply to the appropriate player stat
     */
    public int computeStatDelta() {
        int base = Math.max(1, this.statBoost);
        int rankMult;
        switch (this.itemRank) {
            case "black" -> rankMult = 3;
            case "gold" -> rankMult = 5;
            case "purple" -> rankMult = 7;
            default -> rankMult = 2; // white or unknown
        }

        int delta;
        switch (this.bodyPOS) {
            case "head" -> delta = base / 4 + rankMult * 2; // insight-focused
            case "chest" -> delta = base / 3 + rankMult * 3; // vitality-focused
            case "hands" -> delta = base / 2 + rankMult * 4; // strength-focused (weapon becomes significantly stronger)
            case "legs" -> delta = base / 4 + rankMult * 2; // agility-focused
            case "feet" -> delta = base / 6 + rankMult; // small agility/speed boost
            default -> delta = base / 5 + rankMult; // generic
        }

        return Math.max(1, delta);
    }
}
