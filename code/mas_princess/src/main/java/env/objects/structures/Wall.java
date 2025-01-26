package env.objects.structures;

// Wall structure
public class Wall extends MapStructure {
    public Wall(Boolean team) {
        super(false, false, false,1, 1, 0, team); // Walls are unbreakable, not repairable, and have no life points
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