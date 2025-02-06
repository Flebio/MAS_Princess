package env;

import env.agents.*;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.util.Collection;

/**
 * The MapEnvironment interface defines the necessary methods for interacting with the map
 * environment in the game. Implementing classes must provide the logic for setting up and
 * managing the environment for the agents to interact with.
 * <p>
 * The environment includes map initialization, agent placement, and environment updates.
 */
public interface MapEnvironment {
    void init(String[] args);

    void notifyModelChangedToView();

    Agent initializeAgentIfNeeded(String agentName);

    Collection<Literal> getPercepts(String agentName);

    Collection<Literal> personalBeliefsPercepts(Agent agent);

    Collection<Literal> surroundingPercepts(Agent agent);

    Collection<Literal> inRangePercepts(Agent agent);

    boolean executeAction(String ag, Structure action);
}
