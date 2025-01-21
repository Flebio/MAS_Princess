package env.utils;

import java.util.Random;

/**
 * Relative 8-valued direction
 */
public enum Direction {
    FORWARD,
    FORWARD_RIGHT,
    RIGHT,
    BACKWARD_RIGHT,
    BACKWARD,
    BACKWARD_LEFT,
    LEFT,
    FORWARD_LEFT;

    private static final Random RAND = new Random();

    public static Direction random() {
        return values()[RAND.nextInt(4) * 2];
    }

}
