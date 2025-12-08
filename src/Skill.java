/**
 * Simple Skill metadata holder used by Player skills
 */
public class Skill {
    public final int cost; // MP cost
    public final String description;
    public final String effectId;

    public Skill(int cost, String description, String effectId) {
        this.cost = cost;
        this.description = description;
        this.effectId = effectId;
    }
}
