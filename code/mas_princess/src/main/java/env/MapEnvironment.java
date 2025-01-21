package env;

import env.agents.*;

import env.utils.AgentClass;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;

import java.util.Collection;

public interface MapEnvironment {

    void init(String[] args);

//    void notifyModelChangedToView();

    Agent initializeAgentIfNeeded(String agentName);

    Collection<Literal> getPercepts(String agentName);

    boolean executeAction(String ag, Structure action);

    Collection<Literal> surroundingPercepts(Agent agent);

    Collection<Literal> neighboursPercepts(Agent agent);

}
