package env.objects.structures;

import env.utils.*;

/**
 * Represents a wall structure on the map. Walls are not walkable, unbreakable, and cannot be repaired.
 */
public class Wall extends MapStructure {
    /**
     * Constructs a new Wall with the specified name, team, and pose.
     *
     * @param name  the name of the wall.
     * @param team  the team the wall belongs to (can be {@code null} for neutral walls).
     * @param pose the initial position and orientation of the wall.
     */
    public Wall(String name, Boolean team, Pose pose) {
        super(name, false, false, false, 10, team, pose); // Walls are unbreakable, not repairable, and have no life points
    }
}