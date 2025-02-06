package env.objects.structures;

import env.utils.*;

/**
 * Represents a breakable structure on the map, such as a gate or a wall.  Extends the
 * {@code MapStructure} class and adds functionality for tracking damage and repairs.
 */
public class BreakableStructure extends MapStructure {

    private boolean broken;

    /**
     * Constructs a new BreakableStructure with the specified properties.
     *
     * @param name       the name of the structure.
     * @param repairable {@code true} if the structure can be repaired, {@code false} otherwise.
     * @param walkable   {@code true} if agents can walk through the structure (when it satisfies some conditions), {@code false} otherwise.
     * @param maxLife    the maximum hit points of the structure.
     * @param team      the team this structure belongs to (can be null for neutral structures).
     * @param pose       the initial position and orientation of the structure.
     */
    public BreakableStructure(String name, boolean repairable, boolean walkable, int maxLife, Boolean team, Pose pose) {
        super(name, true, repairable, walkable, maxLife, team, pose); // Gates are 1 cell wide, 2 cells tall, breakable, and repairable
    }

    /**
     * Checks if the structure is destroyed (hit points are zero).
     *
     * @return {@code true} if the structure is destroyed, {@code false} otherwise.
     */
    public boolean isDestroyed() {
        return this.getHp() == 0;
    }

    /**
     * Inflicts damage to the structure.  Reduces the structure's hit points by the specified
     * amount, but does not allow hit points to fall below zero.
     *
     * @param damage the amount of damage to inflict.
     */
    public void takeDamage(int damage) {
        if (this.isBreakable() && this.getHp() >= 0) {
            this.setHp(Math.max(this.getHp() - damage, 0));
        }
    }

    /**
     * Repairs the structure, restoring its hit points to the maximum value.  Only effective if
     * the structure is repairable and currently destroyed.
     */
    public void repair() {
        if (this.isRepairable() && this.isDestroyed()) {
            this.setHp(this.getMaxHp());
        }
    }
}