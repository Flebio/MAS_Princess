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
 * Identifies the enemy with the lowest HP and sets it as the target.
 */
public class check_enemy_in_range extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // Retrieve the agent and its belief base
        Agent currentAgent = ts.getAg();
        BeliefBase bb = currentAgent.getBB();

        // Convert the belief base to a string
        String bbString = bb.toString();

        // Regex pattern to extract enemy in range
        Pattern pattern = Pattern.compile("enemy_in_range\\(([^,]+),([^)]+)\\)");
        Matcher matcher = pattern.matcher(bbString);

        // Variables to track the entity with the lowest HP
        String lowestHpEnemy = null;
        int lowestHp = Integer.MAX_VALUE;

        // Find the enemy with the lowest HP
        while (matcher.find()) {
            String enemyName = matcher.group(1).trim(); // Extract enemy name
            String enemyHpStr = matcher.group(2).trim(); // Extract HP as a string

            try {
                int enemyHp = Integer.parseInt(enemyHpStr); // Safely parse HP

                // Ignore enemies with HP <= 0
                if (enemyHp > 0 && enemyHp < lowestHp) {
                    lowestHp = enemyHp;
                    lowestHpEnemy = enemyName;
                }
            } catch (NumberFormatException e) {
                // Log invalid HP values and skip this enemy
                System.err.println("Invalid HP value for enemy '" + enemyName + "': " + enemyHpStr);
            }
        }

        // Remove any existing "target" belief
        Literal oldTarget = currentAgent.findBel(Literal.parseLiteral("target(_)"), un);
        if (oldTarget != null) {
            currentAgent.delBel(oldTarget);
        }
        // If an enemy is found, set it as the target
        if (lowestHpEnemy != null) {
            // Add the new target belief
            currentAgent.addBel(Literal.parseLiteral("target(" + lowestHpEnemy + ")"));

            // Log the target for debugging
            //System.out.println("New target set: " + lowestHpEnemy + " with HP: " + lowestHp);
        }

        // Always return true as per requirements
        return true;
    }
}
