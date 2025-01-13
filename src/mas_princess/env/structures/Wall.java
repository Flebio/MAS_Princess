package mas_princess.env.structures;

import mas_princess.env.Structure;

// Wall structure
public class Wall extends Structure {
    public Wall() {
        super(false, false, 1, 1, 0); // Walls are unbreakable, not repairable, and have no life points
    }
    @Override
    public void takeDamage(int damage) {
        // The walls cannot take damage, so this method does nothing
    }

    @Override
    public void repair(int amount) {
        // The walls cannot be repaired, so this method does nothing
    }
}