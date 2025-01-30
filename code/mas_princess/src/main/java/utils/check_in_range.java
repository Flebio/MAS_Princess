package utils;

import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

/**
 * Identifies the enemy with the lowest HP and sets it as the target.
 */
public class check_in_range extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // Retrieve the agent and its belief base
        Agent currentAgent = ts.getAg();
        BeliefBase bb = currentAgent.getBB();

        // Variables to track the entity with the lowest HP
        String targetedEntity = null;
        int lowestHp = Integer.MAX_VALUE;

        String percept = String.valueOf(args[0]);
        // Iterate over beliefs directly
        for (Literal belief : bb) {
            if (belief.getFunctor().equals(percept) && belief.getArity() == 2) {
                String entityName = belief.getTerm(0).toString(); // Extract entity name
                String entityHpStr = belief.getTerm(1).toString(); // Extract HP as a string

                try {
                    int enemyHp = Integer.parseInt(entityHpStr); // Safely parse HP

                    // Ignore enemies with HP <= 0
                    if (percept == "enemy_in_range") {
                        if (enemyHp < lowestHp) {
                            lowestHp = enemyHp;
                            targetedEntity = entityName;
                        }
                    } else if (enemyHp < lowestHp && enemyHp > 0) {
                        lowestHp = enemyHp;
                        targetedEntity = entityName;
                    }

                } catch (NumberFormatException e) {
                    // Log invalid HP values and skip this enemy
                    System.err.println("Invalid HP value for enemy '" + entityName + "': " + entityHpStr);
                }
            } else if (belief.getFunctor().equals(percept) && belief.getArity() == 1) {
                targetedEntity = belief.getTerm(0).toString(); // Extract entity name
            }
        }

        // Remove any existing "target" belief
        Literal oldTarget = currentAgent.findBel(Literal.parseLiteral("target(_)"), un);
        if (oldTarget != null) {
            currentAgent.delBel(oldTarget);
        }

        // If an entity is found, set it as the target
        if (targetedEntity != null) {
            currentAgent.addBel(Literal.parseLiteral("target(" + targetedEntity + ")"));
            //System.out.println("New target set: " + targetedEntity );
        }

        // Always return true as per requirements
        return true;
    }
}
