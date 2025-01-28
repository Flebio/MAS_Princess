package env;

import env.utils.*;
import env.agents.*;
import jason.asSyntax.Literal;

import java.util.*;
import java.util.logging.Logger;

public class BlackForestModel implements MapModel {
    private final GameMap gameMap;
    private long fps = 1L;

    public BlackForestModel(int width, int height) {
        this.gameMap = new GameMap(width, height);
    }

    @Override
    public int getWidth() {
        return this.gameMap.getWidth();
    }

    @Override
    public int getHeight() {
        return this.gameMap.getHeight();
    }
    @Override
    public long getFPS() {
        return fps;
    }

    @Override
    public void setFPS(long fps) {
        this.fps = Math.max(1, Math.min(60, fps));
    }

    @Override
    public String printMap(Logger logger) {
        this.gameMap.printMap(logger);
        return null;
    }

    @Override
    public String printAgentList(Logger logger) {
        this.gameMap.printAgentList(logger);
        return null;
    }

    @Override
    public boolean containsAgent(Agent agent) {
        return this.gameMap.containsAgent(agent);
    }

    @Override
    public Set<Agent> getAllAgents() {
        return this.gameMap.getAllAgents();
    }

    @Override
    public Vector2D getAgentPosition(Agent agent) {
        return this.gameMap.getAgentPosition(agent);
    }

    @Override
    public Orientation getAgentDirection(Agent agent) {
        return this.gameMap.getAgentDirection(agent);
    }
    @Override
    public Optional<Agent> getAgentByPosition(Vector2D position) {
        return this.gameMap.getAgentByPosition(position);
    }

    @Override
    public Optional<Agent> getAgentByName(String agName) {
        return this.gameMap.getAgentByName(agName);
    }

    @Override
    public Set<Agent> getAgentNeighbours(Agent agent, int range) {
        return this.gameMap.getAgentNeighbours(agent, range);
    }
    @Override
    public void setAgentPosition(Agent agent, Vector2D position) {
        this.gameMap.setAgentPosition(agent, position);
    }

    @Override
    public void setAgentDirection(Agent agent, Orientation orientation) {
        this.gameMap.setAgentDirection(agent, orientation);
    }

    @Override
    public boolean setAgentPose(Agent agent, int x, int y, Orientation orientation) {
        return this.gameMap.setAgentPose(agent, x, y, orientation);
    }

    @Override
    public boolean spawnAgent(Agent agent) {
        return this.gameMap.spawnAgent(agent);
    }

    @Override
    public boolean attackAgent(Agent attacking_agent, Agent target) {
        return this.gameMap.attackAgent(attacking_agent, target);
    }

    @Override
    public boolean moveAgent(Agent agent, int stepSize, Direction direction) {
        return this.gameMap.moveAgent(agent, stepSize, direction);
    }
    @Override
    public boolean isPositionInside(int x, int y) {
        return this.gameMap.isPositionInside(x, y);
    }
    @Override
    public Cell getCellByPosition(Vector2D position) {
        return this.gameMap.getCellByPosition(position);
    }
    @Override
    public Pair<String, Vector2D> getClosestObjective(Agent agent) {
        return this.gameMap.getClosestObjective(agent);
    }

    @Override
    public  boolean respawnAgent(Agent agent){
        return this.gameMap.respawnAgent(agent);
    }

}
