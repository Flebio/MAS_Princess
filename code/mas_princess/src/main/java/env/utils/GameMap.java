package env.utils;

import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GameMap {
    private final int width;
    private final int height;
    private final Cell[][] map;
    private final Map<String, Agent> agentsList = Collections.synchronizedMap(new HashMap<>());
    private final ReadWriteLock mapLock = new ReentrantReadWriteLock();
    private static final Random RAND = new Random();

    public GameMap(int width, int height) {
        this.width = Objects.requireNonNull(width);
        this.height = Objects.requireNonNull(height);
        this.map = new Cell[width][height];
        createZones();
        addStructures();
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public boolean isPositionInside(int x, int y) {
        return ((x >= 0 && x < this.width && y >= 0 && y < this.height));
    }

    // MAP CONSTRUCTION
    public void createZones() {
        createBaseZones();
        createRiver();
        createBattlefield();
    }

    public void addStructures() {
        addWallsAndGates();
        addBridge();
    }

    private void createBaseZones() {
        int baseWidth = this.getWidth() / 12;
        int baseHeight = this.getHeight() / 4;

        for (int x = 0; x < baseWidth; x++) {
            for (int y = baseHeight; y < this.getHeight() - baseHeight; y++) {
                map[x][y] = new Cell(Zone.BBASE, x, y);
                map[this.getWidth() - x - 1][this.getHeight() - y - 1] = new Cell(Zone.RBASE, this.getWidth() - x - 1, this.getHeight() - y - 1);
            }
        }
    }

    private void createRiver() {
        int centerX = this.getWidth() / 2;
        int centerY = this.getHeight() / 2;

        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY; y < this.getHeight(); y++) {
                map[x][y] = new Cell(Zone.OUT_OF_MAP, x, y);
            }
        }

        createDiagonal(centerX, centerY, -1);
        createDiagonal(centerX, centerY, 1);
    }

    private void createDiagonal(int centerX, int centerY, int direction) {
        for (int x = centerX + direction; x >= 0 && x < this.getWidth(); x += direction) {
            int yStart = centerY - Math.abs(x - centerX);
            if (yStart < 0) break;
            for (int offset = -1; offset <= 1; offset++) {
                if (yStart + offset >= 0 && x + offset >= 0 && x + offset < this.getWidth()) {
                    map[x + offset][yStart] = new Cell(Zone.OUT_OF_MAP, x + offset, yStart);
                }
            }
        }
    }

    private void createBattlefield() {
        int crossableY1 = (this.getHeight() / 3) - 1;
        int crossableY2 = crossableY1 - 1;

        for (int x = 0; x < this.getWidth(); x++) {
            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD, x, crossableY1);
            }
            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD, x , crossableY2);
            }
        }

        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD, x, y);
                }
            }
        }
    }

    private void addWallsAndGates() {
        int baseWidth = this.getWidth() / 12;
        int baseHeight = this.getHeight() / 4;

        for (int x = 0; x < baseWidth + 1; x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate = new Gate(100, 0);
                        map[x][y].setStructure(gate);
                        map[x][y + 1].setStructure(gate);
                    } else if (y != this.getHeight() / 2) {
                        map[x][y].setStructure(new Wall());
                    }
                }
            }
        }

        for (int x = this.getWidth() - baseWidth - 1; x < this.getWidth(); x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate = new Gate(100, 1);
                        map[x][y].setStructure(gate);
                        map[x][y + 1].setStructure(gate);
                    } else if (y != this.getHeight() / 2) {
                        map[x][y].setStructure(new Wall());
                    }
                }
            }
        }
    }
    private void addBridge() {
        int bridgeY = (2 * this.getHeight()) / 3 + 1;
        int bridgeXStart = this.getWidth() / 2 - 1;
        int bridgeXEnd = this.getWidth() / 2 + 1;

        Bridge bridge = new Bridge(0.1, 10);

        for (int x = bridgeXStart; x <= bridgeXEnd; x++) {
            for (int y = bridgeY; y < bridgeY + 2; y++) {
                map[x][y].setStructure(bridge);
            }
        }
    }

    public void printAgentList(Logger logger) {
        mapLock.readLock().lock();
        try {
            logger.info("Current agent list:");
            for (Map.Entry<String, Agent> entry : agentsList.entrySet()) {
                logger.info("Agent Name: " + entry.getKey()+ ", Details: " + entry.getValue().getPose());
            }
            logger.info("\n");
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public void printMap(Logger logger) {
        mapLock.readLock().lock();
        try {
            logger.info("Current map:");
            for (int y = 0; y < this.getHeight(); y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < this.getWidth(); x++) {
                    row.append(this.map[x][y].toString()).append(" ");
                }
                logger.info(row.toString());  // Log the entire row at once
            }
            logger.info("\n");
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public Cell getCellByPosition(int x, int y) {
        mapLock.readLock().lock();
        try {
            if (!this.isPositionInside(x, y)) {
                return null;
            }
            return map[x][y];
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public Cell getCellByPosition(Vector2D position) {
        return this.getCellByPosition(position.getX(), position.getY());
    }

    /**
     * Returns a list of cells based on filtering criteria.
     *
     * @param zoneType        Optional zone type to filter by (can be null).
     * @param structure       Optional structure to filter by (can be null).
     * @param resource        Optional resource to filter by (can be null).
     * @param agent       Optional agent to filter by (can be null).
     * @param includeMatching If true, returns cells matching the criteria; if false, returns the complement.
     * @return List of cells filtered by the specified criteria in raster order.
     */
    public List<Cell> getAllCells(Zone zoneType, MapStructure structure, Resource resource, Agent agent, boolean includeMatching) {
        mapLock.readLock().lock();
        try {
            return Arrays.stream(map)
                    .flatMap(Arrays::stream)
                    .filter(cell -> {
                        boolean matches = true;

                        if (zoneType != null) {
                            matches &= zoneType.equals(cell.getZoneType());
                        }
                        if (structure != null) {
                            matches &= structure.equals(cell.getStructure());
                        }
                        if (resource != null) {
                            matches &= resource.equals(cell.getResource());
                        }
                        if (agent != null) {
                            matches &= agent.getName().equals(cell.getAgent().getName());
                        }

                        return includeMatching == matches;
                    })
                    .collect(Collectors.toList());
        } finally {
            mapLock.readLock().unlock();
        }
    }

    // AGENTS MANAGEMENT
    // NEWLY IMPLEMENTED METHODS
    public void setAgentPosition(Agent agent, Vector2D position) {
        synchronized (this.agentsList) {
            Pose currentPose = this.agentsList.get(agent.getName()).getPose();
            agent.setPose(new Pose(position, currentPose.getOrientation()));
            this.agentsList.put(agent.getName(), agent);
        }
    }
    public void setAgentDirection(Agent agent, Orientation orientation) {
        synchronized (this.agentsList) {
            Pose currentPose = this.agentsList.get(agent.getName()).getPose();
            agent.setPose(new Pose(currentPose.getPosition(), orientation));
            this.agentsList.put(agent.getName(), agent);
        }
    }
    public boolean setAgentPose(Agent agent, int x, int y, Orientation orientation) {
        synchronized (this.agentsList) {

            if (this.containsAgent(agent)) { //UPDATE
                this.setAgentDirection(agent, orientation);
                if (this.isPositionInside(x, y)) {
                    Vector2D position = Vector2D.of(x, y);
                    if (!this.getAgentByPosition(position).isPresent()) {
                        this.setAgentPosition(agent, position);
                        return true;
                    }
                }
                return false;
            }
            this.getCellByPosition(x, y).setAgent(agent);
            agent.setPose(new Pose(Vector2D.of(x,y), orientation));
            this.agentsList.put(agent.getName(), agent);

            return true;
        }
    }
    private boolean setAgentPose(Agent agent, Vector2D afterStep, Orientation newOrientation) {
        return this.setAgentPose(agent, afterStep.getX(), afterStep.getY(), newOrientation);
    }
    public boolean spawnAgent(Agent agent) {
        // BISOGNA UTILIZZARE IL LOCK
        Zone teamBase = (agent.getTeam() == true) ? Zone.RBASE : Zone.BBASE;

        // Get all unoccupied cells in the team's base zone with their positions
        List<Cell> availableCells = getAllCells(teamBase, null, null, null, true);
        if (availableCells.isEmpty()) {
            return false; // No available cells in the base
        }

        // Select a random cell from the available cells
        Cell randomCellPosition = availableCells.get(RAND.nextInt(availableCells.size()));
        Orientation randomOrientation = Orientation.random();

        return setAgentPose(agent, randomCellPosition.getX(), randomCellPosition.getY(), randomOrientation);
    }

    public boolean moveAgent(Agent agent, Vector2D newPosition, Orientation newOrientation) {
        mapLock.writeLock().lock();
        try {
            if (!isPositionInside(newPosition.getX(), newPosition.getY())) {
                return false;
            }

            Pose currentPose = agentsList.get(agent.getName()).getPose();

            Cell currentCell = map[currentPose.getPosition().getX()][currentPose.getPosition().getY()];
            Cell targetCell = map[newPosition.getX()][newPosition.getY()];

            //System.out.println("\n\nCURRENT POSE: " + currentPose + "\n");
            //System.out.println("CURRENT CELL (" + currentCell.getX() + "," + currentCell.getY() + ") -> [Agent: " + currentCell.getAgent() + ", Structure: " + currentCell.getStructure() + ", Resource: " + currentCell.getResource() + "]\n");
            //System.out.println("TARGET CELL (" + targetCell.getX() + "," + targetCell.getY() + ") -> [Agent: " + targetCell.getAgent() + ", Structure: " + targetCell.getStructure() + ", Resource: " + targetCell.getResource() + "]\n");
            //System.out.println("Target cell occupied: " + targetCell.isOccupied() + "\n");
            synchronized (currentCell) {
                synchronized (targetCell) {
                    if (targetCell.isOccupied()) {
                        return false; // Target cell is occupied
                    }

                    // Update agent position
                    this.setAgentPose(agent, newPosition, newOrientation);
                    //System.out.println("NEW POSE: " + agentsList.get(agent.getName()).getPose() + "\n");

                    currentCell.setAgent(null);
                    targetCell.setAgent(agent);
                    //System.out.println("OLD CELL (" + currentCell.getX() + "," + currentCell.getY() + ") -> [Agent: " + currentCell.getAgent() + ", Structure: " + currentCell.getStructure() + ", Resource: " + currentCell.getResource() + "]\n");
                    //System.out.println("NEW CELL (" + targetCell.getX() + "," + targetCell.getY() + ") -> [Agent: " + targetCell.getAgent() + ", Structure: " + targetCell.getStructure() + ", Resource: " + targetCell.getResource() + "]\n\n");
                    return true;
                }
            }

        } finally {
            mapLock.writeLock().unlock();
        }
    }
    public boolean moveAgent(Agent agent, Vector2D newPosition, Direction direction) {
        Pose currentPose = this.agentsList.get(agent.getName()).getPose();
        Orientation newOrientation = currentPose.getOrientation().rotate(direction);

        // Delegate to the main method
        return moveAgent(agent, newPosition, newOrientation);
    }
    public boolean moveAgent(Agent agent, int stepSize, Direction direction) {
        Pose currentPose = this.agentsList.get(agent.getName()).getPose();
        Orientation newOrientation = currentPose.getOrientation().rotate(direction);

        // Delegate to the main method
        return moveAgent(agent, currentPose.getPosition().afterStep(stepSize, newOrientation), newOrientation);
    }

    // Methods that were previously in the environment
    public boolean containsAgent(Agent agent) {
        return this.agentsList.containsKey(agent.getName());
    }
    public Set<Agent> getAllAgents() {
        return this.agentsList.values()
                .stream()
                .collect(Collectors.toSet());
    }

    public void ensureAgentExists(Agent agent) {
        if (!containsAgent(agent)) {
            throw new IllegalArgumentException("No such agent: " + agent.getName());
        }
    }
    public Vector2D getAgentPosition(Agent agent) {
        synchronized (this.agentsList) {
            this.ensureAgentExists(agent);
            return this.agentsList.get(agent.getName()).getPose().getPosition();
        }
    }
    public Orientation getAgentDirection(Agent agent) {
        synchronized (this.agentsList) {
            this.ensureAgentExists(agent);
            return this.agentsList.get(agent.getName()).getPose().getOrientation();
        }
    }
    public Optional<Agent> getAgentByPosition(Vector2D position) {
        synchronized (this.agentsList) {
            return this.agentsList.values().stream()
                    .filter(agent -> agent.getPose().getPosition().equals(position))
                    .findFirst();
        }
    }
    public Optional<Agent> getAgentByName(String agName) {
        synchronized (this.agentsList) {
            return this.agentsList.values().stream()
                    .filter(entry -> agName.equals(entry.getName()))
                    .findFirst();
        }
    }
    public boolean areAgentsNeighbours(Agent agent, Agent neighbour) {
        if (!containsAgent(agent) || !containsAgent(neighbour)) {
            return false;
        }

        BiFunction<Vector2D, Vector2D, Boolean> neighbourhoodFunction = (a, b) -> {
            Vector2D distanceVector = b.minus(a);
            return Math.abs(distanceVector.getX()) <= agent.getAttackRange()
                    && Math.abs(distanceVector.getY()) <= agent.getAttackRange();
        };

        Vector2D agentPosition = this.getAgentPosition(agent);
        Vector2D neighbourPosition = this.getAgentPosition(neighbour);
        return neighbourhoodFunction.apply(agentPosition, neighbourPosition);
    }

    public Set<Agent> getAgentNeighbours(Agent agent) {
        return this.getAllAgents().stream()
                .filter(it -> !it.equals(agent))
                .filter(other -> this.areAgentsNeighbours(agent, other))
                .collect(Collectors.toSet());
    }
}
