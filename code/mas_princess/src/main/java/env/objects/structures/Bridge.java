package env.objects.structures;

import env.utils.*;

/**
 * Represents a bridge structure on the map. Bridges are walkable, unbreakable, and cannot be repaired.
 * They have a slip probability associated with them, which could case agent death.
 */
public class Bridge extends MapStructure {
    private final int slipProbability;

    /**
     * Constructs a new Bridge with the specified slip probability and pose.
     *
     * @param slipProbability The probability (as a percentage, e.g., 10 for 10%) of an agent slipping
     *                        when moving on the bridge.
     * @param pose            The initial position and orientation of the bridge.
     */
    public Bridge(int slipProbability, Pose pose) {
        super("bridge", false, false, true, 1, null, pose); // Bridges are unbreakable, not repairable, and have 1 life point
        this.slipProbability = slipProbability;
    }

    /**
     * Returns the slip probability of the bridge.
     *
     * @return The slip probability (percentage).
     */
    public int getSlipProbability() {
        return this.slipProbability;
    }
}