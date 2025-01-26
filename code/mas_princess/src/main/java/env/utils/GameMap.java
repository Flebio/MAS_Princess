package env.utils;

import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;
import jason.asSyntax.Literal;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
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
        addTrees();
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

//    private void createBattlefield() {
//        int crossableY1 = (this.getHeight() / 3) - 1;
//        int crossableY2 = crossableY1 - 1;
//
//        for (int x = 0; x < this.getWidth(); x++) {
//            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.OUT_OF_MAP) {
//                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD, x, crossableY1);
//            }
//            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.OUT_OF_MAP) {
//                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD, x , crossableY2);
//            }
//        }
//
//        for (int x = 0; x < this.getWidth(); x++) {
//            for (int y = 0; y < this.getHeight(); y++) {
//                if (map[x][y] == null) {
//                    map[x][y] = new Cell(Zone.BATTLEFIELD, x, y);
//                }
//            }
//        }
//    }
    private void createBattlefield() {
        int crossableY1 = (this.getHeight() / 3) - 1;
        int crossableY2 = crossableY1 - 1;
        int middleX = this.getWidth() / 2;

        boolean flagPlaced = false;

        for (int x = 0; x < this.getWidth(); x++) {
            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD, x, crossableY1);
            }
            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD, x, crossableY2);
            }
        }

        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD, x, y);
                }
            }
            // Place WarFlags where the battlefield crosses the river
            if (!flagPlaced && x == middleX) {
                if (map[x][crossableY1] != null) {
                    map[x][crossableY1].setStructure(new WarFlag());
                }
                if (map[x][crossableY2] != null) {
                    map[x][crossableY2].setStructure(new WarFlag());
                }
                flagPlaced = true;
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
                        Gate gate = new Gate(100, false);
                        map[x][y].setStructure(gate);
                        map[x][y + 1].setStructure(gate);
                    } else if (y != this.getHeight() / 2) {
                        map[x][y].setStructure(new Wall(false));
                    }
                }
            }
        }

        for (int x = this.getWidth() - baseWidth - 1; x < this.getWidth(); x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate = new Gate(100, true);
                        map[x][y].setStructure(gate);
                        map[x][y + 1].setStructure(gate);
                    } else if (y != this.getHeight() / 2) {
                        map[x][y].setStructure(new Wall(true));
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
    // Helper method to validate if a cell can have a tree
    private boolean isValidTreeCell(int x, int y) {
        return map[x][y] != null
                && map[x][y].getStructure() == null
                && map[x][y].getZoneType() == Zone.BATTLEFIELD;
    }

    private void addTrees() {
        // Number of trees to spawn (e.g., ~5% of the map area)
        int treeCount = (this.width * this.height) / 15;

        // Define spawn area dimensions
        int northSpawnAreaWidth = this.getWidth() / 3; // Width for each northern area (smaller)
        int southSpawnAreaWidth = this.getWidth() / 2; // Width for each southern area (larger)
        int spawnAreaHeight = this.getHeight() / 4; // Height for each area (same for all)

        // Define bounds for the four areas (northern and southern)
        int northStartY = 0; // Northern areas start at the top row
        int southStartY = this.getHeight() - spawnAreaHeight; // Southern areas start at the bottom row
        int area1StartX = 0; // Left northern area
        int area2StartX = this.getWidth() - northSpawnAreaWidth; // Right northern area
        int area3StartX = 0; // Left southern area
        int area4StartX = this.getWidth() - southSpawnAreaWidth; // Right southern area

        // Define a list of all spawnable cells
        List<Cell> spawnableCells = new ArrayList<>();

        // Add northern areas to spawnable cells (top border included)
        for (int x = area1StartX; x < area1StartX + northSpawnAreaWidth; x++) {
            for (int y = northStartY; y < northStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnableCells.add(map[x][y]);
                }
            }
        }
        for (int x = area2StartX; x < area2StartX + northSpawnAreaWidth; x++) {
            for (int y = northStartY; y < northStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnableCells.add(map[x][y]);
                }
            }
        }

        // Add southern areas to spawnable cells (bottom border included)
        for (int x = area3StartX; x < area3StartX + southSpawnAreaWidth; x++) {
            for (int y = southStartY; y < southStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnableCells.add(map[x][y]);
                }
            }
        }
        for (int x = area4StartX; x < area4StartX + southSpawnAreaWidth; x++) {
            for (int y = southStartY; y < southStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnableCells.add(map[x][y]);
                }
            }
        }

        // Randomly place trees in the spawnable cells
        for (int i = 0; i < treeCount && !spawnableCells.isEmpty(); i++) {
            int index = RAND.nextInt(spawnableCells.size());
            Cell selectedCell = spawnableCells.remove(index);
            selectedCell.setStructure(new Tree(50, 20)); // Create a tree with example health and resource values
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
     * @param zoneType       Optional zone type to filter by (can be null).
     * @param structureClass Optional class type of structure to filter by (can be null).
     * @param structurePredicate Optional predicate to filter structures by specific fields (can be null).
     * @param resourceClass  Optional class type of resource to filter by (can be null).
     * @param resourcePredicate Optional predicate to filter resources by specific fields (can be null).
     * @param agentClass     Optional class type of agent to filter by (can be null).
     * @param agentPredicate Optional predicate to filter agents by specific fields (can be null).
     * @param includeMatching If true, returns cells matching the criteria; if false, returns the complement.
     * @return List of cells filtered by the specified criteria in raster order.
     */
    public List<Cell> getAllCells(
            Zone zoneType,
            Class<? extends MapStructure> structureClass, Predicate<MapStructure> structurePredicate,
            Class<? extends Resource> resourceClass, Predicate<Resource> resourcePredicate,
            Class<? extends Agent> agentClass, Predicate<Agent> agentPredicate,
            boolean includeMatching) {

        mapLock.readLock().lock();
        try {
            return Arrays.stream(map)
                    .flatMap(Arrays::stream)
                    .filter(cell -> {
                        boolean matches = true;

                        if (zoneType != null) {
                            matches &= zoneType.equals(cell.getZoneType());
                        }
                        if (structureClass != null) {
                            matches &= structureClass.isInstance(cell.getStructure()) &&
                                    (structurePredicate == null || structurePredicate.test(cell.getStructure()));
                        }

                        if (resourceClass != null) {
                            matches &= resourceClass.isInstance(cell.getResource()) &&
                                    (resourcePredicate == null || resourcePredicate.test(cell.getResource()));
                        }
                        if (agentClass != null && cell.getAgent() != null) {
                            matches &= agentClass.isInstance(cell.getAgent()) &&
                                    (agentPredicate == null || agentPredicate.test(cell.getAgent()));
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
        // Get all unoccupied cells in the team's base zone with their positions
        List<Cell> availableCells = getAllCells(
                (agent.getTeam() == true) ? Zone.RBASE : Zone.BBASE,
                null, null,
                null, null,
                null, null,
                true
        );

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

            synchronized (currentCell) {
                synchronized (targetCell) {
                    if (targetCell.isOccupied(agent)) {
                        return false; // Target cell is occupied
                    }

                    // Update agent position
                    this.setAgentPose(agent, newPosition, newOrientation);

                    currentCell.setAgent(null);
                    targetCell.setAgent(agent);

//                    if ((targetCell.getZoneType() == Zone.BBASE && agent.getTeam() == false) || (targetCell.getZoneType() == Zone.RBASE && agent.getTeam() == true)) {
//                        agent.setState("spawn");
//                    } else if ((targetCell.getZoneType() == Zone.BBASE && agent.getTeam() == true) || (targetCell.getZoneType() == Zone.RBASE && agent.getTeam() == false)) {
//                        agent.setState("enemy_base");
//                    } else if ((targetCell.getZoneType() == Zone.BATTLEFIELD)) {
//                        agent.setState("battlefield");
//                    }

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

    private Pair<String, Vector2D> findClosestStructure(Agent agent, Class<? extends MapStructure> structureClass, Predicate<MapStructure> filter, String stateName) {
        List<Cell> structures = getAllCells(
                null,
                structureClass, filter,
                null, null, // No resource filtering
                null, null, // No agent filtering
                true
        );

        Vector2D agentPosition = agent.getPose().getPosition();
        Vector2D closestPosition = null;
        double minDistance = Double.MAX_VALUE;

        for (Cell cell : structures) {
            double distance = calculateDistance(agentPosition, cell.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closestPosition = cell.getPosition();
            }
        }

        return closestPosition != null ? new Pair<>(stateName, closestPosition) : null;
    }

    private double calculateDistance(Vector2D pos1, Vector2D pos2) {
        return Math.sqrt(
                Math.pow(pos1.getX() - pos2.getX(), 2) + Math.pow(pos1.getY() - pos2.getY(), 2)
        );
    }

    public Pair<String, Vector2D> getClosestObjectiveSoldier(Agent agent) {
        switch (agent.getState()) {
            case "spawn":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "ally_gate");

            case "ally_gate":
                return findClosestStructure(agent, WarFlag.class, null, "war_flag");

            case "war_flag":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() != agent.getTeam(), "enemy_gate");

            default:
                return null;
        }
    }

    public Pair<String, Vector2D> getClosestObjective(Agent agent) {
        if (agent instanceof Soldier) {
            return getClosestObjectiveSoldier(agent);
//        } else if (agent instanceof Gatherer) {
//            return getClosestObjectiveGatherer(agent);
        }
        return null; // Return null if the agent type is not recognized
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
    public boolean areAgentsNeighbours(Agent agent, Agent neighbour, int range) {
        if (!containsAgent(agent) || !containsAgent(neighbour)) {
            return false;
        }

        BiFunction<Vector2D, Vector2D, Boolean> neighbourhoodFunction = (a, b) -> {
            Vector2D distanceVector = b.minus(a);
            return Math.abs(distanceVector.getX()) <= range
                    && Math.abs(distanceVector.getY()) <= range;
        };

        Vector2D agentPosition = this.getAgentPosition(agent);
        Vector2D neighbourPosition = this.getAgentPosition(neighbour);
        return neighbourhoodFunction.apply(agentPosition, neighbourPosition);
    }

    public Set<Agent> getAgentNeighbours(Agent agent, int range) {
        return this.getAllAgents().stream()
                .filter(it -> !it.equals(agent))
                .filter(other -> this.areAgentsNeighbours(agent, other, range))
                .collect(Collectors.toSet());
    }
}
