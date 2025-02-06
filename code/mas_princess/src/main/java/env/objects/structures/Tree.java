package env.objects.structures;

import env.utils.*;

/**
 * Represents a tree structure on the map. Trees are breakable but not repairable, and they
 * respawn after a certain duration when destroyed.
 */
public class Tree extends BreakableStructure {
    private final int respawnDuration;
    private boolean respawning = false;

    /**
     * Constructs a new Tree with the specified name, maximum life points, respawn duration, and pose.
     *
     * @param name            the name of the tree.
     * @param maxLife         the maximum hit points of the tree.
     * @param respawnDuration the duration (in milliseconds) before the tree respawns after being
     *                        destroyed.
     * @param pose            the initial position and orientation of the tree.
     */
    public Tree(String name, int maxLife, int respawnDuration, Pose pose) {
        super(name, true, false, maxLife, null, pose); // Trees are breakable, not repairable
        this.respawnDuration = respawnDuration;
    }

    /**
     * Checks if the tree is currently respawning.
     *
     * @return {@code true} if the tree is respawning, {@code false} otherwise.
     */
    public boolean isRespawning() {
        return this.respawning;
    }

    /**
     * Inflicts damage to the tree. If the damage destroys the tree, a respawn timer is started.
     *
     * @param damage the amount of damage to inflict.
     */
    @Override
    public void takeDamage(int damage) {
        if (!isDestroyed()) {
            super.takeDamage(damage);
            if (isDestroyed()) {
                startRespawnTimer();
            }
        }
    }

    /**
     * Starts the respawn timer for the tree.  After the respawn duration, the tree's hit points
     * are restored to the maximum, and it becomes interactable again.
     */
    private void startRespawnTimer() {
        setWalkable(true); //Make it walkable when destroyed
        respawning = true;
        new Thread(() -> {
            try {
                Thread.sleep(respawnDuration);  // Wait before respawning
            } catch (InterruptedException e) {
                e.printStackTrace(); // Consider proper logging in a real application
            }
            respawn();
        }).start();
    }

    /**
     * Respawns the tree, restoring its hit points to the maximum and resetting its walkability.
     */
    private void respawn() {
        setHp(getMaxHp());
        setWalkable(false); //Make it not walkable when respawned
    }
}