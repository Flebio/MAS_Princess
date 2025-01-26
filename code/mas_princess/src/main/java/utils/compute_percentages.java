package utils;

import env.utils.Direction;
import env.utils.Orientation;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Computes movement percentages p1 and p2 based on distances to an objective.
 */
public class compute_percentages extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final Map<String, Map<String, String>> absoluteMovementMapping = Map.of(
                "north", Map.of(
                        "left", "left",
                        "right", "right",
                        "up", "forward",
                        "down", "backward"
                ),
                "south", Map.of(
                        "left", "right",
                        "right", "left",
                        "up", "backward",
                        "down", "forward"
                ),
                "east", Map.of(
                        "left", "backward",
                        "right", "forward",
                        "up", "left",
                        "down", "right"
                ),
                "west", Map.of(
                        "left", "forward",
                        "right", "backward",
                        "up", "right",
                        "down", "left"
                )
        );

        Agent currentAgent = ts.getAg();

        // Convert terms to integers
        int K = termToInteger(args[0]); // Agent X-coordinate
        int J = termToInteger(args[1]); // Agent Y-coordinate
        int H = termToInteger(args[2]); // Objective X-coordinate
        int I = termToInteger(args[3]); // Objective Y-coordinate

        // Calculate distances
        double distanceX = Math.abs(K - H);
        double distanceY = Math.abs(J - I);
        double totalDistance = distanceX + distanceY;

        // Prevent division by zero
        if (totalDistance == 0) {
            throw new ArithmeticException("Agent and objective are at the same position. Cannot compute percentages.");
        }

        // Calculate percentages
        double p1 = distanceX / totalDistance; // Percentage for X-axis movement
        double p2 = distanceY / totalDistance; // Percentage for Y-axis movement


        // Remove old beliefs and add beliefs to the agent
        Literal current_p1 = currentAgent.findBel(Literal.parseLiteral("p1(_)"), un);
        Literal current_p2 = currentAgent.findBel(Literal.parseLiteral("p2(_)"), un);
        currentAgent.delBel(current_p1);
        currentAgent.delBel(current_p2);


        // QUELLO CHE DEVO FARE QUI è: SE NON ESISTE NELLA BB FREE(g), DOVE g CORRISPONDE AL MAPPING DATO DALL'ORIENTAMENTO
        // E DALLA DIREZIONE (up, down, left or right) DOVE CI SI VUOLE SPOSTARE, SIGNIFICA CHE LA CASELLA NON è LIBERA.
        // PER TANTO SI METTE QUELLA PROBABILITà A 0. DOPO DI CHE VA MODIFICATO L'AGENTE, PERCHé BISOGNA CONTROLLARE ENTRAMBE
        // LE POSSIBILITà, E MUOVERSI RANDOM SE NESSUNA DELLE DUE è FATTIBILE.
        String current_orientation = currentAgent.findBel(Literal.parseLiteral("orientation(_)"), un).getTerm(0).toString();
        String direction_p1 = String.valueOf(args[4]);
        String direction_p2 = String.valueOf(args[5]);

        String abs_direction_p1 = absoluteMovementMapping.get(current_orientation).get(direction_p1);
        String abs_direction_p2 = absoluteMovementMapping.get(current_orientation).get(direction_p2);

        BeliefBase bb = currentAgent.getBB();

        // If direction 1 is not free, then the agent does not try to move there. A direction is free if in the BB we can find either
        // free(direction), gate(direction) or bridge(direction).
        Boolean test_direction_p1_1 = Optional.ofNullable(bb.toString()).map(s -> s.contains(String.format("free(%s)", abs_direction_p1))).orElse(false);
        Boolean test_direction_p1_g = Optional.ofNullable(bb.toString()).map(s -> s.contains(String.format("gate(%s)", abs_direction_p1))).orElse(false);
        Boolean test_direction_p1_b = Optional.ofNullable(bb.toString()).map(s -> s.contains(String.format("bridge(%s)", abs_direction_p1))).orElse(false);
        if (!test_direction_p1_1 && !test_direction_p1_g && !test_direction_p1_b)  {
            p1 = 0.0;
        }

        // Same reasoning for direction 2.
        Boolean test_direction_p2_1 = Optional.ofNullable(bb.toString()).map(s -> s.contains(String.format("free(%s)", abs_direction_p2))).orElse(false);
        Boolean test_direction_p2_g = Optional.ofNullable(bb.toString()).map(s -> s.contains(String.format("gate(%s)", abs_direction_p2))).orElse(false);
        Boolean test_direction_p2_b = Optional.ofNullable(bb.toString()).map(s -> s.contains(String.format("bridge(%s)", abs_direction_p2))).orElse(false);
        if (!test_direction_p2_1 && !test_direction_p2_g && !test_direction_p2_b)  {
            p2 = 0.0;
        }

        // Log the results
//        System.out.println("ORIENTATION " + current_orientation);
//        System.out.println("DIRECTION P1 " + direction_p1);
//        System.out.println("DIRECTION P2 " + direction_p2);
//        System.out.println("P1 (MOVE THROUGH X-AXIS): " + p1);
//        System.out.println("P2 (MOVE THROUGH Y-AXIS): " + p2);

        currentAgent.addBel(Literal.parseLiteral(String.format(Locale.US, "p1(%.4f)", p1)));
        currentAgent.addBel(Literal.parseLiteral(String.format(Locale.US, "p2(%.4f)", p2)));

        return true;
    }

    /**
     * Converts a Term to an integer.
     * @param term the Term to convert
     * @return the integer value of the term
     */
    private int termToInteger(Term term) {
        return Integer.parseInt(term.toString());
    }
}
