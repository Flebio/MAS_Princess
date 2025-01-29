package env.objects.structures;

import env.utils.*;

// Gate structure
public class Gate extends BreakableStructure {

    public Gate(String name, int maxLife, Boolean team, Pose pose) {
        super(name, true, true,  maxLife, team, pose); // Gates are 1 cell wide, 2 cells tall, breakable, and repairable
    }
}
