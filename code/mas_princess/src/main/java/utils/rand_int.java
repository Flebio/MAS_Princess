package utils;

import jason.NoValueException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Random;

/**
 * Lets an agent draw a random integer in a given range
 * Indicator: <code>rand_int(-X, +Min, +Max)</code>
 */
public class rand_int extends DefaultInternalAction {
    private static final Random RAND = new Random();

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

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        int min = termToInteger(args[1]);
        int max = termToInteger(args[2]);
        int result = RAND.nextInt(max - min) + min;
        return un.unifies(args[0], numberToTerm(result));
    }
}
