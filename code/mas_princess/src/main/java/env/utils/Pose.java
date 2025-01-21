package env.utils;

import java.util.Objects;

public class Pose {
    private final Vector2D position;
    private final Orientation orientation;

    public Pose(Vector2D position, Orientation orientation) {
        this.position = Objects.requireNonNull(position);
        this.orientation = Objects.requireNonNull(orientation);
    }

    public Vector2D getPosition() {
        return position;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pose pose = (Pose) o;
        return position.equals(pose.position) &&
                orientation == pose.orientation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, orientation);
    }

    @Override
    public String toString() {
        return "Pose{" +
                "position=" + position +
                ", orientation=" + orientation +
                '}';
    }
}
