package env.utils;

import java.util.Objects;
import java.util.Random;

/**
 * Absolute 8-valued orientation
 */
public enum Orientation {
    NORTH(0, -1, "^"),
    NORTH_EAST(1, -1, "/'"),
    EAST(1, 0,">"),
    SOUTH_EAST(1, 1, "\\,"),
    SOUTH(0, 1, "v"),
    SOUTH_WEST(-1, 1, "./"),
    WEST(-1, 0,"<"),
    NORTH_WEST(-1, -1, "'\\");

    private static final Random RAND = new Random();

    public static Orientation random() {
        return values()[RAND.nextInt(4) * 2];
    }

    private final Vector2D vector;
    private final String symbol;

    Orientation(int x, int y, String symbol) {
        this.vector = Vector2D.of(x, y);
        this.symbol = Objects.requireNonNull(symbol);
    }

    public Orientation right90() {
        return right45(2);
    }

    public Orientation right45(int times) {
        return values()[(this.ordinal() + times) % 8];
    }

    public Orientation left90() {
        return left45(2);
    }

    public Orientation left45(int times) {
        return values()[(this.ordinal() + 3 * times) % 8];
    }

    public Orientation back() {
        return right45(4);
    }

    public Orientation rotate(Direction direction) {
        Objects.requireNonNull(direction);
        return right45(direction.ordinal());
    }

    public Vector2D asVector() {
        return vector;
    }

    public String getSymbol() {
        return symbol;
    }
}
