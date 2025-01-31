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


        String percept = String.valueOf(args[0]);

        // Initialize targetHp based on percept type
        int targetHp = percept.equals("ally_gate_in_range") ? 0 : Integer.MAX_VALUE;

        //        if (currentAgent.toString())
        // Iterate over beliefs directly
        for (Literal belief : bb) {
            if (belief.getFunctor().equals(percept) && belief.getArity() == 2) {
                String entityName = belief.getTerm(0).toString(); // Extract entity name
                String entityHpStr = belief.getTerm(1).toString(); // Extract HP as a string

//                try {
//                    int entityHp = Integer.parseInt(entityHpStr); // Safely parse HP
//
//                    // Ignore enemies with HP <= 0
//                    if (percept.equals("enemy_in_range")) {
//                        if (entityHp < targetHp && entityHp > 0) {
//                            targetHp = entityHp;
//                            targetedEntity = entityName;
//                        }
//                    } else if (percept.equals("ally_gate_in_range")) {
//                        System.out.println(entityHp);
//                        if (entityHp >= targetHp && entityHp < 100) {
//                            targetHp = entityHp;
//                            targetedEntity = entityName;
//                        }
//                    } else if (entityHp < targetHp && entityHp > 0) {
//                        targetHp = entityHp;
//                        targetedEntity = entityName;
//                    }
//
//                } catch (NumberFormatException e) {
//                    // Log invalid HP values and skip this enemy
//                    System.err.println("Invalid HP value for enemy '" + entityName + "': " + entityHpStr);
//                }

                entityHpStr = entityHpStr.trim();
                boolean isNegative = entityHpStr.startsWith("-") && entityHpStr.length() > 1;

                String cleanHpStr = isNegative ? entityHpStr.substring(1) : entityHpStr;
                if (cleanHpStr.matches("\\d+")) {
                    int entityHp = Integer.parseInt(entityHpStr); // Parse normally (handles negatives)

                    if (percept.equals("enemy_in_range")) {
                        if (entityHp < targetHp && entityHp > 0) {
                                targetHp = entityHp;
                                targetedEntity = entityName;
                            }
                    } else if (percept.equals("ally_gate_in_range")) {
                            if (entityHp >= targetHp && entityHp < 100) {
                                targetHp = entityHp;
                                targetedEntity = entityName;
                            }
                    } else if (entityHp < targetHp && entityHp > 0) {
                        targetHp = entityHp;
                        targetedEntity = entityName;
                    }

                } else {
                    System.err.println("Invalid HP value for '" + entityName + "': " + entityHpStr);
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


//package utils;
//
//        import jason.asSemantics.Agent;
//        import jason.asSemantics.DefaultInternalAction;
//        import jason.asSemantics.TransitionSystem;
//        import jason.asSemantics.Unifier;
//        import jason.asSyntax.Literal;
//        import jason.asSyntax.Term;
//        import jason.bb.BeliefBase;
//
///**
// * Identifies the enemy with the lowest HP (or ally gate with the highest HP) and sets it as the target.
// */
//public class check_in_range extends DefaultInternalAction {
//
//    @Override
//    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
//        // Retrieve the agent and its belief base
//        Agent currentAgent = ts.getAg();
//        BeliefBase bb = currentAgent.getBB();
//
//        // Variables to track the targeted entity
//        String targetedEntity = null;
//        int targetHp = 0; // Default HP for ally_gate (maximization) and enemy (minimization)
//
//        String percept = String.valueOf(args[0]);
//
//        // Iterate over beliefs
//        for (Literal belief : bb) {
//            if (belief.getFunctor().equals(percept)) {
//                String entityName = belief.getTerm(0).toString(); // Extract entity name
//
//                if (belief.getArity() == 2) { // If there is an HP term
//                    String entityHpStr = belief.getTerm(1).toString(); // Extract HP as a string
//                    try {
//                        int entityHp = Integer.parseInt(entityHpStr); // Parse HP
//
//                        if (percept.equals("ally_gate_in_range")) {
//                            // Choose the gate with the highest HP
//                            if (entityHp > targetHp && entityHp < 100) {
//                                targetedEntity = entityName;
//                                targetHp = entityHp;
//                            }
//                        } else if (percept.equals("enemy_in_range") || percept.equals("enemy_gate_in_range")) {
//                            // Choose the enemy with the lowest HP (but HP must be > 0)
//                            if (entityHp < targetHp && entityHp > 0) {
//                                targetedEntity = entityName;
//                                targetHp = entityHp;
//                            }
//                        }
//
//                    } catch (NumberFormatException e) {
//                        System.err.println("Invalid HP value for entity '" + entityName + "': " + entityHpStr);
//                    }
//                } else if (belief.getArity() == 1) { // If there's no HP, just set the entity as the target
//                    targetedEntity = entityName;
//                }
//            }
//        }
//
//        // Remove any existing "target" belief
//        Literal oldTarget = currentAgent.findBel(Literal.parseLiteral("target(_)"), un);
//        if (oldTarget != null) {
//            currentAgent.delBel(oldTarget);
//        }
//
//        // If an entity is found, set it as the target
//        if (targetedEntity != null) {
//            currentAgent.addBel(Literal.parseLiteral("target(" + targetedEntity + ")"));
//        }
//
//        return true; // Always return true as per requirements
//    }
//}
//
