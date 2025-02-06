package env;

import env.utils.*;
import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The `BlackForestModel` class represents the underlying data and logic for the Black Forest simulation.
 * It manages the state of the environment, including agents, resources, and game mechanics.
 *
 * As part of the Model-View-Controller (MVC) pattern, this class serves as the **Model**,
 * encapsulating the core simulation logic and maintaining the game state.
 * It interacts with `BlackForestGameMap` for spatial and environmental data,
 * while notifying the **View** (`BlackForestView`) about updates.
 */
public class BlackForestModel implements MapModel {

    private final BlackForestGameMap gameMap;
    private long fps = 4L;

    public BlackForestModel(int width, int height, MapView view) {
        this.gameMap = new BlackForestGameMap(width, height, view);
    }

    /**
     * @see BlackForestGameMap#getWidth()
     */
    @Override
    public int getWidth() {
        return this.gameMap.getWidth();
    }
    /**
     * @see BlackForestGameMap#getHeight()
     */
    @Override
    public int getHeight() {
        return this.gameMap.getHeight();
    }
    /**
     * Retrieves the game fps.
     *
     * @return The fps of the game.
     */
    @Override
    public long getFPS() {
        return fps;
    }

    // Agent management and acts
    /**
     * @see BlackForestGameMap#getAllAgents()
     */
    @Override
    public Set<Agent> getAllAgents() {
        return this.gameMap.getAllAgents();
    }
    /**
     * @see BlackForestGameMap#spawnAgent(Agent)
     */
    @Override
    public boolean spawnAgent(Agent agent) {
        return this.gameMap.spawnAgent(agent);
    }
    /**
     * @see BlackForestGameMap#resetAgent(Agent)
     */
    @Override
    public boolean resetAgent(Agent agent) { return this.gameMap.resetAgent(agent); }
    /**
     * @see BlackForestGameMap#attackAgent(Agent, Agent, boolean)
     */
    @Override
    public boolean attackAgent(Agent attacking_agent, Agent target, boolean crit) {
        return this.gameMap.attackAgent(attacking_agent, target, crit);
    }
    /**
     * @see BlackForestGameMap#healAgent(Agent, Agent)
     */
    @Override
    public boolean healAgent(Agent healing_agent, Agent target) {
        return this.gameMap.healAgent(healing_agent, target);
    }
    /**
     * @see BlackForestGameMap#moveAgent(Agent, int, Direction)
     */
    @Override
    public boolean moveAgent(Agent agent, int stepSize, Direction direction) {
        return this.gameMap.moveAgent(agent, stepSize, direction);
    }
    /**
     * @see BlackForestGameMap#attackGate(Agent, Gate)
     */
    @Override
    public boolean attackGate(Agent attacking_agent, Gate target) {
        return this.gameMap.attackGate(attacking_agent, target);
    }
    /**
     * @see BlackForestGameMap#repairGate(Agent, Gate)
     */
    @Override
    public boolean repairGate(Agent attacking_agent, Gate target) {
        return this.gameMap.repairGate(attacking_agent, target);
    }
    /**
     * @see BlackForestGameMap#attackTree(Agent, Tree)
     */
    @Override
    public boolean attackTree(Agent attacking_agent, Tree target) {
        return this.gameMap.attackTree(attacking_agent, target);
    }
    /**
     * @see BlackForestGameMap#pickUpPrincess(Agent, Princess)
     */
    @Override
    public boolean pickUpPrincess(Agent agent, Princess target) {return this.gameMap.pickUpPrincess(agent, target);}
    /**
     * @see BlackForestGameMap#getAgentByName(String)
     */
    @Override
    public Optional<Agent> getAgentByName(String agName) {
        return this.gameMap.getAgentByName(agName);
    }
    /**
     * @see BlackForestGameMap#getAgentNeighbours(Agent, int)
     */
    @Override
    public Set<Agent> getAgentNeighbours(Agent agent, int range) {
        return this.gameMap.getAgentNeighbours(agent, range);
    }
    /**
     * @see BlackForestGameMap#getGateNeighbours(Agent, String, int)
     */
    @Override
    public Set<Gate> getGateNeighbours(Agent agent, String team, int range) {
        return this.gameMap.getGateNeighbours(agent, team, range);
    }
    /**
     * @see BlackForestGameMap#getTreeNeighbours(Agent, int)
     */
    @Override
    public Set<Tree> getTreeNeighbours(Agent agent, int range) {
        return this.gameMap.getTreeNeighbours(agent, range);
    }
    /**
     * @see BlackForestGameMap#getPrincessNeighbours(Agent, String, int)
     */
    @Override
    public Set<Princess> getPrincessNeighbours(Agent agent, String team, int range) {
        return this.gameMap.getPrincessNeighbours(agent, team, range);
    }
    /**
     * @see BlackForestGameMap#getAgentSurroundingPositions(Agent)
     */
    @Override
    public Map<Direction, Vector2D> getAgentSurroundingPositions(Agent agent) {
        return this.gameMap.getAgentSurroundingPositions(agent);
    }
    /**
     * @see BlackForestGameMap#getClosestObjective(Agent)
     */
    @Override
    public Pair<String, Vector2D> getClosestObjective(Agent agent) {
        return this.gameMap.getClosestObjective(agent);
    }

    // Artifacts management
    /**
     * @see BlackForestGameMap#getWoodAmountRed()
     */
    @Override
    public AtomicInteger getWoodAmountRed() {
        return gameMap.getWoodAmountRed();
    }
    /**
     * @see BlackForestGameMap#getWoodAmountBlue()
     */
    @Override
    public AtomicInteger getWoodAmountBlue() {
        return gameMap.getWoodAmountBlue();
    }
    /**
     * @see BlackForestGameMap#getGateByName(String)
     */
    @Override
    public Optional<Gate> getGateByName(String gName) { return this.gameMap.getGateByName(gName); }
    /**
     * @see BlackForestGameMap#getTreeByName(String)
     */
    @Override
    public Optional<Tree> getTreeByName(String tName) {
        return this.gameMap.getTreeByName(tName);
    }
    /**
     * @see BlackForestGameMap#getPrincessByName(String)
     */
    @Override
    public Optional<Princess> getPrincessByName(String pName)
    { return this.gameMap.getPrincessByName(pName); }

    // Map
    /**
     * @see BlackForestGameMap#getCellByPosition(Vector2D)
     */
    @Override
    public Cell getCellByPosition(Vector2D position) {
        return this.gameMap.getCellByPosition(position);
    }
    /**
     * @see BlackForestGameMap#setView(MapView)
     */
    @Override
    public void setView(MapView view) {
        this.gameMap.setView(view);
    }

}
