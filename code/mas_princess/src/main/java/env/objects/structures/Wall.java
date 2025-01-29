package env.objects.structures;

import env.utils.*;

// Wall structure
public class Wall extends MapStructure {
    public Wall(String name, Boolean team, Pose pose) {
        super(name, false, false, false,10, team, pose); // Walls are unbreakable, not repairable, and have no life points
    }
}