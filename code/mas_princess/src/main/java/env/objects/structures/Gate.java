package env.objects.structures;

import env.utils.*;

/**
 * Represents a gate structure on the map. Gates are walkable, breakable, and can be repaired.
 */
public class Gate extends BreakableStructure {

    /**
     * Constructs a new Gate with the specified name, healt points quantity, team, and pose.
     *
     * @param name  the name of the gate.
     * @param maxLife  the healt points of the gate.
     * @param team  the team the gate belongs to.
     * @param pose the initial position and orientation of the gate.
     */
    public Gate(String name, int maxLife, Boolean team, Pose pose) {
        super(name, true, true,  maxLife, team, pose);
    }
}
