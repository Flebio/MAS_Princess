package env.objects.resources;

import env.utils.*;

// Resource class
public abstract class Resource {
    private final String name;
    private boolean team;
    private Pose pose;

    public Resource(String name, boolean team, Pose pose) {
        this.name = name;
        this.team = team;
        this.pose = pose;
    }
    public String getName() {
        return name;
    }
    public boolean getTeam() {
        return team;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    @Override
    public String toString() {
        return String.format("Resource[Name: %s, Pose: %s]",
               name, pose);
    }
}

