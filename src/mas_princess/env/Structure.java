package mas_princess.env;

// Base Structure class
public abstract class Structure {
    private final boolean breakable;
    private final boolean repairable;
    private final int width;
    private final int height;
    private final int maxLife;
    private int currentLife;

    public Structure(boolean breakable, boolean repairable, int width, int height, int maxLife) {
        this.breakable = breakable;
        this.repairable = repairable;
        this.width = width;
        this.height = height;
        this.maxLife = maxLife;
        this.currentLife = maxLife;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public boolean isRepairable() {
        return repairable;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public int getCurrentLife() {
        return currentLife;
    }

    public boolean isDestroyed() {
        return currentLife == 0;
    }

    public void takeDamage(int damage) {
        if (breakable && currentLife > 0) {
            currentLife = Math.max(currentLife - damage, 0);
        }
    }

    public void repair(int amount) {
        if (repairable && currentLife < maxLife) {
            currentLife = Math.min(currentLife + amount, maxLife);
        }
    }
}