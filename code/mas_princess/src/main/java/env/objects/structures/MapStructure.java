package env.objects.structures;

import env.utils.*;

// Base Structure class
public abstract class MapStructure {
    private final String name;
    private final boolean breakable;
    private final boolean repairable;
    private final Boolean walkable;
    private final int width;
    private final int height;
    private final int max_hp;
    final Boolean team;
    private int hp;
    private Pose pose;


    public MapStructure(String name, boolean breakable, boolean repairable, Boolean walkable, int width, int height, int max_hp, Boolean team, Pose pose) {
        this.name = name;
        this.breakable = breakable;
        this.repairable = repairable;
        this.walkable = walkable;
        this.width = width;
        this.height = height;
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxHP() {
        return max_hp;
    }
    public Boolean getTeam() {
        return this.team;
    }
    public int getCurrentHP() {
        return hp;
    }

    public boolean isDestroyed() {
        return hp == 0;
    }

    public void takeDamage(int damage) {
        if (breakable && hp > 0) {
            hp = Math.max(hp - damage, 0);
        }
    }

    public void repair(int amount) {
        if (repairable && hp < max_hp) {
            hp = Math.min(hp + amount, max_hp);
        }
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    @Override
    public String toString() {
        return String.format("Structure[Name: %s, Max HP: %d, Width: %d, Height: %d]",
                this.getClass().getSimpleName(), max_hp, width, height);
    }
}