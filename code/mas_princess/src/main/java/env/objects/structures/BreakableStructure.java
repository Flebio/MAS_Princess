package env.objects.structures;

import env.utils.*;

// Gate structure
public class BreakableStructure extends MapStructure {

    private boolean broken;
    public BreakableStructure(String name, boolean repairable, boolean walkable, int maxLife, Boolean team, Pose pose) {
        super(name, true, repairable, walkable, maxLife, team, pose); // Gates are 1 cell wide, 2 cells tall, breakable, and repairable
    }

    public boolean isDestroyed() {
        return this.getHp() == 0;
    }

    public void takeDamage(int damage) {
        if (this.isBreakable() && this.getHp() >= 0) {
            this.setHp(Math.max(this.getHp() - damage, 0));
        }
    }

    public void repair(int amount) {
        if (this.isRepairable() && this.getHp() < this.getMaxHp()) {
            this.setHp(Math.min(this.getHp() + amount, this.getMaxHp()));
        }
    }
}
