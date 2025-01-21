package utils;

import env.utils.Direction;
import env.utils.Vector2D;

import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import static utils.Utils.termToDirection;
import static utils.Utils.positionLiteralToVector;
import static utils.Facing.fromLiteral;

/**
 * Lets an agent update its own believed pose.
 * Indicator: <code>utils.update_pose(+Direction)</code>
 * This action assumes the calling agent has just performed a step in <code>Direction</code>.
 * It also assumes that agent has two believes in its belief base: (i) <code>position(X, Y)</code>
 * and (ii) (i) <code>facing(Dir)</code> (where <code>Dir</code>).
 * If all such assumptions are satisfied, the both the <code>position(X, Y)</code> and <code>facing(Dir)</code>
 * are updated accordingly.
 */
public class update_pose extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Agent currentAgent = ts.getAg();
        Literal currentPosition = currentAgent.findBel(Literal.parseLiteral("position(X, Y)"), un);
        Literal currentFacing = currentAgent.findBel(Literal.parseLiteral("facing(Dir)"), un);
        Term nextDirection = args[0];
        currentAgent.delBel(currentFacing);
        currentAgent.delBel(currentPosition);

        Vector2D position = positionLiteralToVector(currentPosition);
        Facing facing = fromLiteral(currentFacing);
        Direction dir = termToDirection(nextDirection);

        Facing nextFacing = facing.rotate(dir);
        Vector2D nextPosition = position.plus(nextFacing.asVector());

        currentAgent.addBel(Literal.parseLiteral("position" + nextPosition));
        currentAgent.addBel(Literal.parseLiteral("facing(" + nextFacing.toString().toLowerCase() + ")"));
        return true;
    }
}
