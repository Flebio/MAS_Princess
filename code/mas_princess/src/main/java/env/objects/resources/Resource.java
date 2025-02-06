package env.objects.resources;

import env.utils.*;

/**
 * The base class for all resources on the map (e.g., princesses). Defines common properties and
 * behaviors shared by all resources.
 */
public abstract class Resource {
    private final String name;
    private boolean team;
    private Pose pose;
    private boolean carried = false;

    /**
     * Constructs a new Resource with the specified name, team, and pose.
     *
     * @param name the name of the resource.
     * @param team {@code true} if the resource belongs to the red team, {@code false} for blue.
     * @param pose the initial position and orientation of the resource.
     */
    public Resource(String name, boolean team, Pose pose) {
        this.name = name;
        this.team = team;
        this.pose = pose;
    }

    /**
     * Returns the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the team this resource belongs to.
     *
     * @return {@code true} if the resource belongs to the red team, {@code false} for blue.
     */
    public boolean getTeam() {
        return team;
    }

    /**
     * Returns the pose (position and orientation) of the resource.
     *
     * @return the pose of the resource.
     */
    public Pose getPose() {
        return pose;
    }

    /**
     * Sets the pose (position and orientation) of the resource.
     *
     * @param pose the new pose for the resource.
     */
    public void setPose(Pose pose) {
        this.pose = pose;
    }

    /**
     * Checks if the resource is currently being carried by an agent.
     *
     * @return {@code true} if the resource is carried, {@code false} otherwise.
     */
    public boolean isCarried() {
        return carried;
    }

    /**
     * Marks the resource as picked up (carried).
     *
     * @throws IllegalStateException if the resource is already carried.
     */
    public void pickedUp() {
        if (!carried) {
            carried = true;
        } else {
            throw new IllegalStateException("Resource is already being carried.");
        }
    }

    /**
     * Marks the resource as dropped (no longer carried).
     */
    public void dropped() {
        carried = false;
    }

    /**
     * Returns a string representation of the resource.
     *
     * @return a string representation of the resource, including its name and pose.
     */
    @Override
    public String toString() {
        return String.format("Resource[Name: %s, Pose: %s]",
                name, pose);
    }
}