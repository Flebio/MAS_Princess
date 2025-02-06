package env;

import env.objects.resources.Princess;
import env.utils.*;
import env.agents.*;
import env.objects.structures.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The MapModel interface defines the behavior of the game map model.
 * Implementing classes should manage the state of the map, including updating and rendering
 * the map's elements based on agent interactions and environment changes.
 * <p>
 * The model should be able to update the map's state, as well as provide necessary information
 * for rendering or simulating the game world.
 */
public interface MapModel {
    int getWidth();
    int getHeight();
    long getFPS();

    // Agent management and acts
    Set<Agent> getAllAgents();
    boolean spawnAgent(Agent agent);
    boolean resetAgent(Agent agent);
    boolean attackAgent(Agent attacking_agent, Agent target, boolean crit);
    boolean healAgent(Agent attacking_agent, Agent target);
    boolean moveAgent(Agent agent, int stepSize, Direction direction);
    boolean attackGate(Agent attacking_agent, Gate target);
    boolean repairGate(Agent attacking_agent, Gate target);
    boolean attackTree(Agent attacking_agent, Tree target);
    boolean pickUpPrincess(Agent agent, Princess target);
    Optional<Agent> getAgentByName(String agName);
    Set<Agent> getAgentNeighbours(Agent agent, int range);
    Set<Gate> getGateNeighbours(Agent agent, String team, int range);
    Set<Tree> getTreeNeighbours(Agent agent, int range);
    Set<Princess> getPrincessNeighbours(Agent agent, String team, int range);
    Map<Direction, Vector2D> getAgentSurroundingPositions(Agent agent);
    Pair<String, Vector2D> getClosestObjective(Agent agent);


    // Artifacts management
    AtomicInteger getWoodAmountBlue();
    AtomicInteger getWoodAmountRed();
    Optional<Gate> getGateByName(String gName);
    Optional<Tree> getTreeByName(String tName);
    Optional<Princess> getPrincessByName(String pName);

    // Map
    Cell getCellByPosition(Vector2D position);
    void setView(MapView view);

}