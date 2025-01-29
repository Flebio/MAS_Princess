package utils;

import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.bb.BeliefBase;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identifies the gate with the lowest HP and sets it as the target.
 */
public class check_princess_in_range extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // Retrieve the agent and its belief base
        Agent currentAgent = ts.getAg();
        BeliefBase bb = currentAgent.getBB();

        // Convert the belief base to a string
        String bbString = bb.toString();

        // Regex pattern to extract princess in range
        Pattern pattern = Pattern.compile("princess_in_range\\(([^,]+),([^)]+)\\)");
        Matcher matcher = pattern.matcher(bbString);

        String princess_name = "";
        while (matcher.find()) {
            princess_name = matcher.group(1).trim(); // Extract princess name
        }

        // Remove any existing "target" belief
        Literal oldTarget = currentAgent.findBel(Literal.parseLiteral("target(_)"), un);
        if (oldTarget != null) {
            currentAgent.delBel(oldTarget);
        }
        // If an enemy is found, set it as the target
        if (princess_name != null) {
            // Add the new target belief
            currentAgent.addBel(Literal.parseLiteral("target(" + princess_name + ")"));

            // Log the target for debugging
            //System.out.println("New target set: " + lowestHpEnemy + " with HP: " + lowestHp);
        }

        // Always return true as per requirements
        return true;
    }
}
