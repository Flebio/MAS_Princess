package env;

import env.utils.*;
import env.agents.*;

import java.util.*;

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
    public boolean isPositionInside(int x, int y) {
        return this.gameMap.isPositionInside(x, y);
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
    public String printMap() {
        this.gameMap.printMap();
        return null;
    }

    @Override
    public String printAgentList() {
        this.gameMap.printAgentList();
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
    public Cell getCellByPosition(Vector2D position) {
        return this.gameMap.getCellByPosition(position);
    }

    @Override
    public Optional<Agent> getAgentByName(String agName) {
        return this.gameMap.getAgentByName(agName);
    }

    @Override
    public boolean areAgentsNeighbours(Agent agent, Agent neighbour) {
        return this.gameMap.areAgentsNeighbours(agent, neighbour);
    }

    @Override
    public Set<Agent> getAgentNeighbours(Agent agent) {
        return this.gameMap.getAgentNeighbours(agent);
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
    public boolean moveAgent(Agent agent, int stepSize, Direction direction) {
        return this.gameMap.moveAgent(agent, stepSize, direction);
    }
}
