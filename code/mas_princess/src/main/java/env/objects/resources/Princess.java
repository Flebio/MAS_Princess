package env.objects.resources;

import env.utils.Pose;

public class Princess extends Resource {

    private boolean carried; // Whether the princess is currently being carried

    public Princess(String name, boolean team, boolean carried, Pose pose) {
        super(name, team, pose);
        this.carried = carried;
    }

    public boolean isCarried() { return carried; }


    public void pickUp() {
        if (!carried) {
            carried = true;
        }
    }

    public void drop() {
         carried = false;
    }

}
