package env.objects.structures;

import env.utils.*;

// Empty structure, used for movement
public class Empty extends MapStructure {
    private String type;
    public Empty(Pose pose, String type) {
        super("empty", false, false, true,10, null, pose);
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}