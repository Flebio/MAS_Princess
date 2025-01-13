package mas_princess.env.structures;

import mas_princess.env.Structure;

// Bridge structure
public class Bridge extends Structure {
    private final double breakProbability;
    private final int respawnDuration;
    private int remainingRespawnTime;

    public Bridge(double breakProbability, int respawnDuration) {
        super(true, false, 3, 2, 1); // Bridges are breakable, not repairable, 3 cells wide, 2 cells tall, and have 1 life point
        this.breakProbability = breakProbability;
        this.respawnDuration = respawnDuration;
        this.remainingRespawnTime = 0;
    }

    public boolean isBroken() {
        return remainingRespawnTime > 0;
    }

    public void attemptBreak() {
        if (!isBroken() && Math.random() < breakProbability) {
            remainingRespawnTime = respawnDuration;
        }
    }

    public void updateRespawnTimer() {
        if (remainingRespawnTime > 0) {
            remainingRespawnTime--;
        }
    }

    public boolean isActive() {
        return remainingRespawnTime == 0;
    }
}
