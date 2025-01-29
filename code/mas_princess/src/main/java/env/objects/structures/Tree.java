package env.objects.structures;

import env.utils.*;

// Tree structure
public class Tree extends BreakableStructure {
    private final int respawnDuration;
    private int remainingRespawnTime;

    public Tree(String name, int maxLife, int respawnDuration, Pose pose) {
        super(name, true, false,  maxLife, null, pose); // Trees are breakable, not repairable
        this.respawnDuration = respawnDuration;
        this.remainingRespawnTime = 0;
    }

    public boolean isActive() {
        return remainingRespawnTime == 0;
    }

    @Override
    public void takeDamage(int damage) {
        if (!isDestroyed()) {
            super.takeDamage(damage);
        } else {
            remainingRespawnTime = respawnDuration;
        }
    }

    public void updateRespawnTimer() {
        if (remainingRespawnTime > 0) {
            remainingRespawnTime--;
             // Trees fully regenerate after respawn
        } else if (remainingRespawnTime == 0) {
            repair(getMaxHp());
        }
    }
}