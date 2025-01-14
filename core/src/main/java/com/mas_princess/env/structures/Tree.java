package com.mas_princess.env.structures;

import com.mas_princess.env.Structure;

// Tree structure
public class Tree extends Structure {
    private final int respawnDuration;
    private int remainingRespawnTime;

    public Tree(int maxLife, int respawnDuration) {
        super(true, false, 1, 1, maxLife); // Trees are breakable, not repairable, 1x1 cells
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
            if (isDestroyed()) {
                remainingRespawnTime = respawnDuration;
            }
        }
    }

    public void updateRespawnTimer() {
        if (remainingRespawnTime > 0) {
            remainingRespawnTime--;
            if (remainingRespawnTime == 0) {
                repair(getMaxLife()); // Trees fully regenerate after respawn
            }
        }
    }
}