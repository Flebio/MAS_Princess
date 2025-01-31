package env.objects.structures;

import env.utils.*;

public class Tree extends BreakableStructure {
    private final int respawnDuration;


    public Tree(String name, int maxLife, int respawnDuration, Pose pose) {
        super(name, true, false, maxLife, null, pose); // Trees are breakable, not repairable
        this.respawnDuration = respawnDuration;
    }

    private boolean respawning = false;

    public boolean isRespawning() { return this.respawning; }
    @Override
    public void takeDamage(int damage) {
        if (!isDestroyed()) {
            super.takeDamage(damage);
            if (isDestroyed()) {
                startRespawnTimer();
            }
        }
    }

    private void startRespawnTimer() {
        setWalkable(true);
        respawning = true;
        new Thread(() -> {
            try {
                Thread.sleep(respawnDuration);  // Wait before respawning
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            respawn();
        }).start();
    }

    private void respawn() {
        setHp(getMaxHp());
        setWalkable(false);
        System.out.println(getName() + " has respawned!");
    }
}
