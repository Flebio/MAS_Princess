package utils;

import env.utils.Direction;
import env.utils.Vector2D;

import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

enum Facing {
    TOP(1, 0),
    RIGHT(0, 1),
    BOTTOM(-1, 0),
    LEFT(0, -1);

    Facing(int x, int y) {
        this.x = x;
        this.y = y;
    }

    static Facing fromLiteral(Literal literal) {
        return fromTerm(literal.getTerm(0));
    }

    static Facing fromTerm(Term term) {
        if (!term.isAtom()) {
            throw new IllegalArgumentException("Cannot parse as Facing: " + term);
        }
        return valueOf(((Atom) term).getFunctor().toUpperCase());
    }

    private final int x, y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Facing rotate(Direction direction) {
        return values()[(this.ordinal() + direction.ordinal() / 2) % 4];
    }

    public Vector2D asVector() {
        return Vector2D.of(x, y);
    }
}
