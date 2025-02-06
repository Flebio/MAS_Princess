package env.objects.structures;

import env.utils.*;

/**
 * Represents an Empty structure on the map. Empties are walkable, unbreakable, and cannot be repaired.
 * They are fictitious structures used by agents to set their objective and move towards that point.
 */
public class Empty extends MapStructure {
    private String type;

    /**
     * Constructs a new Empty structure with the specified pose and type.
     *
     * @param pose The initial position and orientation of the empty structure.
     * @param type The type of the empty structure (used to distinguish different empty structures).
     */
    public Empty(Pose pose, String type) {
        super("empty", false, false, true, 10, null, pose);
        this.type = type;
    }

    /**
     * Returns the type of this empty structure.  This can be used to distinguish between
     * different empty structures used for various purposes (e.g., "base_b", "half").
     *
     * @return The type of the empty structure.
     */
    public String getType() {
        return this.type;
    }
}