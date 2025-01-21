package utils;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.util.Random;

import static utils.Utils.*;

/**
 * Lets an agent draw a random integer in a given range
 * Indicator: <code>rand_int(-X, +Min, +Max)</code>
 */
public class rand_int extends DefaultInternalAction {
    private static final Random RAND = new Random();

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        int min = termToInteger(args[1]);
        int max = termToInteger(args[2]);
        int result = RAND.nextInt(max - min) + min;
        return un.unifies(args[0], numberToTerm(result));
    }
}
