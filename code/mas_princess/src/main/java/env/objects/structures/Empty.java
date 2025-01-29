package env.objects.structures;

import env.utils.*;

// Empty structure, used for movement
public class Empty extends MapStructure {
    public Empty(Pose pose) {
        super("empty", false, false, true,1, 1, 0, null, pose);
    }
}