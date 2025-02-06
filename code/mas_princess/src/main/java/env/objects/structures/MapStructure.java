package env.objects.structures;

import env.utils.*;

/**
 * The base class for all map structures (e.g., walls, gates, bridges, trees).  Defines common
 * properties and behaviors shared by all structures.
 */
public abstract class MapStructure {
    private final String name;
    private final boolean breakable;
    private final boolean repairable;
    private Boolean walkable;
    private final int max_hp;
    final Boolean team;
    private int hp;
    private Pose pose;

    /**
     * Constructs a new MapStructure with the specified properties.
     *
     * @param name       the name of the structure.
     * @param breakable  {@code true} if the structure can be destroyed, {@code false} otherwise.
     * @param repairable {@code true} if the structure can be repaired, {@code false} otherwise.
     * @param walkable   {@code true} if agents can walk through the structure (when it satisfies some conditions), {@code false} otherwise.
     * @param max_hp     the maximum hit points of the structure.
     * @param team      the team this structure belongs to (can be null for neutral structures).
     * @param pose       the initial position and orientation of the structure.
     */
    public MapStructure(String name, boolean breakable, boolean repairable, Boolean walkable, int max_hp, Boolean team, Pose pose) {
        this.name = name;
        this.breakable = breakable;
        this.repairable = repairable;
        this.walkable = walkable;
        this.max_hp = max_hp;
        this.hp = max_hp;
        this.team = team;
        this.pose = pose;  // Set the initial position and orientation
    }

    /**
     * Returns the name of the structure.
     *
     * @return the name of the structure.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the structure is breakable.
     *
     * @return {@code true} if the structure is breakable, {@code false} otherwise.
     */
    public boolean isBreakable() {
        return breakable;
    }

    /**
     * Checks if the structure is repairable.
     *
     * @return {@code true} if the structure is repairable, {@code false} otherwise.
     */
    public boolean isRepairable() {
        return repairable;
    }

    /**
     * Checks if the structure is walkable (when it satisfies some conditions).
     *
     * @return {@code true} if the structure is walkable, {@code false} otherwise.
     */
    public boolean isWalkable() {
        return walkable;
    }

    /**
     * Sets the walkability of the structure.
     * @param walkable the new walkability value
     */
    public void setWalkable(Boolean walkable) {
        this.walkable = walkable;
    }


    /**
     * Returns the maximum hit points of the structure.
     *
     * @return the maximum hit points.
     */
    public int getMaxHp() {
        return max_hp;
    }

    /**
     * Returns the team this structure belongs to.
     *
     * @return the team (can be {@code null} for neutral structures).
     */
    public Boolean getTeam() {
        return this.team;
    }

    /**
     * Returns the current hit points of the structure.
     *
     * @return the current hit points.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Sets the current hit points of the structure.
     *
     * @param hp the new hit points value.
     */
    public void setHp(int hp) {
        this.hp = hp;
    }

    /**
     * Returns the pose (position and orientation) of the structure.
     *
     * @return the pose of the structure.
     */
    public Pose getPose() {
        return pose;
    }

    /**
     * Sets the pose (position and orientation) of the structure.
     *
     * @param pose the new pose for the structure.
     */
    public void setPose(Pose pose) {
        this.pose = pose;
    }

    /**
     * Returns a string representation of the structure.
     *
     * @return a string representation of the structure, including its name and maximum HP.
     */
    @Override
    public String toString() {
        return String.format("Structure[Name: %s, Max HP: %d]",
                this.getClass().getSimpleName(), max_hp);
    }
}