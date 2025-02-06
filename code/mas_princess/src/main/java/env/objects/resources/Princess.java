package env.objects.resources;

import env.utils.Pose;

/**
 * Represents a princess resource on the map.  Extends the {@code Resource} class.
 */
public class Princess extends Resource {

    /**
     * Constructs a new Princess with the specified name, team, and pose.
     *
     * @param name the name of the princess.
     * @param team {@code true} if the princess belongs to the red team, {@code false} for blue.
     * @param pose the initial position and orientation of the princess.
     */
    public Princess(String name, boolean team, Pose pose) {
        super(name, team, pose);
    }
}