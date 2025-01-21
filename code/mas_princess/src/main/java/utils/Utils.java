package utils;

import env.utils.Direction;
import env.utils.Vector2D;

import jason.NoValueException;
import jason.asSyntax.*;

public class Utils {
    public static Vector2D positionLiteralToVector(Literal literal) {
        if (!literal.getTerm(0).isNumeric() || !literal.getTerm(1).isNumeric()) {
            throw new IllegalArgumentException("Cannot parse as Vector2D: " + literal);
        }
        try {
            return Vector2D.of(
                    termToInteger(literal.getTerm(0)),
                    termToInteger(literal.getTerm(1))
            );
        } catch (NoValueException e) {
            throw new IllegalArgumentException("Cannot parse as Vector2D: " + literal);
        }
    }

    public static Direction termToDirection(Term term) {
        if (!term.isAtom()) {
            throw new IllegalArgumentException("Cannot parse as Direction: " + term);
        }
        return Direction.valueOf(((Atom) term).getFunctor().toUpperCase());
    }

    public static double termToNumber(Term term) throws NoValueException {
        if (!term.isNumeric()) {
            throw new IllegalArgumentException("Cannot parse as number: " + term);
        }
        return ((NumberTerm)term).solve();
    }

    public static int termToInteger(Term term) throws NoValueException {
        return (int) termToNumber(term);
    }

    public static Term numberToTerm(int value) {
        return new NumberTermImpl(value);
    }

    public static Term numberToTerm(double value) {
        return new NumberTermImpl(value);
    }
}
