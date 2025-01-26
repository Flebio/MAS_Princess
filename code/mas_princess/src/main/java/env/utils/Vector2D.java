package env.utils;

import java.util.Objects;
import java.util.Random;

public final class Vector2D {
    private final int x;
    private final int y;
    private static final Random RAND = new Random();

    public static Vector2D of(int x, int y) {
        return new Vector2D(x, y);
    }

    public static Vector2D random(int maxX, int maxY) {
        return new Vector2D(RAND.nextInt(maxX), RAND.nextInt(maxY));
    }

    Vector2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D that = (Vector2D) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    public Vector2D times(int factor) {
        return new Vector2D(x * factor, y * factor);
    }

    public Vector2D plus(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D minus(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D plus(int x, int y) {
        return new Vector2D(this.x + x, this.y + y);
    }

    public Vector2D minus(int x, int y) {
        return new Vector2D(this.x - x, this.y - y);
    }

    public Vector2D afterStep(int stepSize, Orientation orientation) {
        return this.plus(orientation.asVector().times(stepSize));
    }
}
