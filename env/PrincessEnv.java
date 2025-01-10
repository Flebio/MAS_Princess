package mas_princess.environment;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrincessEnv extends jason.environment.TimeSteppedEnvironment {

    static Logger logger = Logger.getLogger("MASPrincess." + PrincessEnv.class.getName());
    private GameWorld gameWorld;

    public enum Zone {
        BASE,         // Princess is located here
        BATTLEFIELD
    }
    public enum Action {
        MOVE,          // Move to a specific location
        COLLECT,       // Collect resources like wood or metal
        ATTACK,        // Attack enemies
        HEAL,          // Heal teammates
        FEED,          // Feed the princess
        REPAIR_GATE,   // Repair the defensive gate
        BUILD_LADDER   // Build a ladder at a specific location
    }

    @Override
    public void init(String[] args) {
        try {
            logger.info("Initializing MAS Princess Environment...");
            
            // Set the action queue policy
            setOverActionsPolicy(OverActionsPolicy.queue);
            
            // Set step time (default to 1000 ms if not provided)
            int stepTime = args.length > 0 ? Integer.parseInt(args[0]) : 1000;
            super.init(new String[]{String.valueOf(stepTime)});
    
            // Initialize the game world
            gameWorld = new GameWorld();
            gameWorld.initialize();
    
            // Clear existing perceptions and set up initial perceptions
            clearPercepts();
            updateAgentPerceptions();
    
            logger.info("Environment initialized successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during environment initialization.", e);
        }
    }
    


    @Override
    public boolean executeAction(String agentName, jason.asSyntax.Structure action) {
        try {
            // Log action attempt
            logger.info(agentName + " attempting action: " + action);
    
            // Delegate action handling to the game world
            boolean result = gameWorld.handleAction(agentName, action);
            
            // Update perceptions if action succeeds
            if (result) {
                updateAgentPerceptions();
                logger.info(agentName + " successfully executed action: " + action);
            } else {
                logger.warning(agentName + " failed to execute action: " + action);
            }
            
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing action: " + action + " for agent: " + agentName, e);
            return false;
        }
    }
    
    

    private void updateAgentPerceptions() {
        try {
            // Delegate perception updates to the game world
            gameWorld.updateAllAgentPerceptions(this);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating agent perceptions.", e);
        }
    }
    
    

    @Override
    protected void stepFinished(int step, long time, boolean timeout) {
        try {
            // Log the step details
            logger.info("Step " + step + " completed in " + time + "ms. Timeout: " + timeout);
    
            // Check for game over conditions
            if (gameWorld.isGameOver()) {
                logger.info("Game Over! Winning Team: " + gameWorld.getWinningTeam());
                getEnvironmentInfraTier().getRuntimeServices().stopMAS();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during step completion.", e);
        }
    }
    
}
