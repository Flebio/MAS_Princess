package env.objects.structures;

import env.utils.*;

// Base Structure class
public abstract class MapStructure {
    private final String name;
    private final boolean breakable;
    private final boolean repairable;
    private Boolean walkable;
    private final int max_hp;
    final Boolean team;
    private int hp;
    private Pose pose;


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

    public String getName() { return name; }

    public boolean isBreakable() {
        return breakable;
    }

    public boolean isRepairable() {
        return repairable;
    }
    public boolean isWalkable() {
        return walkable;
    }
    public void setWalkable(Boolean walkable) { this.walkable = walkable; }

    public int getMaxHp() {
        return max_hp;
    }
    public Boolean getTeam() {
        return this.team;
    }
    public int getHp() {
        return hp;
    }
    public void setHp(int hp) {
        this.hp = hp;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    @Override
    public String toString() {
        return String.format("Structure[Name: %s, Max HP: %d]",
                this.getClass().getSimpleName(), max_hp);
    }
}