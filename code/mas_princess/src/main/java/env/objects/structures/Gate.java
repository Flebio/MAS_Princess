package env.objects.structures;

import env.utils.*;

// Gate structure
public class Gate extends MapStructure {
    public Gate(String name, int maxLife, Boolean team, Pose pose) {
        super(name, true, true, true,1, 2, maxLife, team, pose); // Gates are 1 cell wide, 2 cells tall, breakable, and repairable
    }

    @Override
    public void takeDamage(int damage) {
        // Gates only take damage from the opposing team
        super.takeDamage(damage);
    }
}
