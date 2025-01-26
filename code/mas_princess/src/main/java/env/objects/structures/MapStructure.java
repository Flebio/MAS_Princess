package env.objects.structures;

// Base Structure class
public abstract class MapStructure {
    private final boolean breakable;
    private final boolean repairable;
    private final Boolean walkable;
    private final int width;
    private final int height;
    private final int max_hp;
    final Boolean team;
    private int hp;

    public MapStructure(boolean breakable, boolean repairable, Boolean walkable, int width, int height, int max_hp, Boolean team) {
        this.breakable = breakable;
        this.repairable = repairable;
        this.walkable = walkable;
        this.width = width;
        this.height = height;
        this.max_hp = max_hp;
        this.hp = max_hp;
        this.team = team;
    }

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

    @Override
    public String toString() {
        return String.format("Structure[Name: %s, Max HP: %d, Width: %d, Height: %d]",
                this.getClass().getSimpleName(), max_hp, width, height);
    }
}