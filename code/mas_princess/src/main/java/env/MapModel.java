package env;

import env.objects.resources.Princess;
import env.utils.*;
import env.agents.*;
import env.objects.structures.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MapModel {
    // Environment Dimensions
    int getWidth();
    int getHeight();
    long getFPS();
    void setFPS(long fps);
    String printMap(Logger logger);
    String printAgentList(Logger logger);

    // Agent Management
    boolean containsAgent(Agent agent);
    Set<Agent> getAllAgents();
    Vector2D getAgentPosition(Agent agent);
    Orientation getAgentDirection(Agent agent);
    void setAgentPosition(Agent agent, Vector2D position);
    void setAgentDirection(Agent agent, Orientation orientation);
    boolean setAgentPose(Agent agent, int x, int y, Orientation orientation);
    default boolean setAgentPose(Agent agent, Vector2D position, Orientation orientation) {
        return setAgentPose(agent, position.getX(), position.getY(), orientation);
    }

    boolean spawnAgent(Agent agent);
    boolean attackAgent(Agent attacking_agent, Agent target);

    boolean moveAgent(Agent agent, int stepSize, Direction direction);
    Optional<Agent> getAgentByPosition(Vector2D position);
    public Optional<Agent> getAgentByName(String agName);
    public Optional<Gate> getGateByName(String gName);
    public Optional<Tree> getTreeByName(String tName);

    public  boolean respawnAgent(Agent agent);
    // Map
    Cell getCellByPosition(Vector2D position);
    boolean isPositionInside(int x, int y);
    default boolean isPositionInside(Vector2D position) {
        return isPositionInside(position.getX(), position.getY());
    }
    default boolean isPositionOutside(int x, int y) {
        return !isPositionInside(x, y);
    }
    default boolean isPositionOutside(Vector2D position) {
        return isPositionOutside(position.getX(), position.getY());
    }
    Pair<String, Vector2D> getClosestObjective(Agent agent);
    Set<Agent> getAgentNeighbours(Agent agent, int range);
    Set<Gate> getEnemyGateNeighbours(Agent agent, int range);
    Set<Tree> getTreeNeighbours(Agent agent, int range);
    default Map<Direction, Vector2D> getAgentSurroundingPositions(Agent agent) {
        Vector2D pos = this.getAgentPosition(agent);
        Orientation dir = this.getAgentDirection(agent);
        return Stream.of(Direction.values()).collect(Collectors.toMap(
                k -> k,
                v -> pos.afterStep(1, dir.rotate(v))
        ));
        // Maybe here we can do something for the range
    }
    boolean attackGate(Agent attacking_agent, Gate target);
    boolean attackTree(Agent attacking_agent, Tree target);

    Set<Princess> getAllyPrincessNeighbours(Agent agent, int range);
    Optional<Princess> getPrincessByName(String pName);

    boolean pickUpPrincess(Agent agent, Princess target);

}