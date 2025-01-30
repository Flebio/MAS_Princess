package env.utils;

import env.BlackForestView;
import env.MapView;
import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GameMap {
    private final int width;
    private final int height;
    private final AtomicInteger woodAmountBlue = new AtomicInteger(0);
    private final AtomicInteger woodAmountRed = new AtomicInteger(0);
    private final Cell[][] map;
    private final Map<String, Agent> agentsList = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, MapStructure> structuresList = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Resource> resourcesList = Collections.synchronizedMap(new HashMap<>());
    private final ReadWriteLock mapLock = new ReentrantReadWriteLock();
    private static final Random RAND = new Random();

    private MapView view; // Keep reference to the existing view


    public GameMap(int width, int height, MapView view) {
        this.width = Objects.requireNonNull(width);
        this.height = Objects.requireNonNull(height);
        this.map = new Cell[width][height];
        this.view = view;
        createZones();
        addStructures();
        addResources();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public AtomicInteger getWoodAmountRed() {
        return this.woodAmountRed;
    }

    public AtomicInteger getWoodAmountBlue() {
        return this.woodAmountBlue;
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

    public void addResources() {
        spawnPrincess(false);
        spawnPrincess(true);
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
            for (int y = 0; y < this.getHeight(); y++) {
                map[x][y] = new Cell(Zone.OUT_OF_MAP, x, y);
            }
        }

        //createDiagonal(centerX, centerY, -1);
        //createDiagonal(centerX, centerY, 1);
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
        int crossableY1 = (this.getHeight() / 3);
        int crossableY2 = crossableY1 - 1;
        int crossableY3 = crossableY1 - 2;
        int middleX = this.getWidth() / 2;

        boolean flagPlaced = false;

        for (int x = 0; x < this.getWidth(); x++) {
            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD, x, crossableY1);
            }
            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD, x, crossableY2);
            }
            if (map[x][crossableY3] != null && map[x][crossableY3].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY3] = new Cell(Zone.BATTLEFIELD, x, crossableY2);
            }
        }

        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD, x, y);
                }
            }

            // Place Emptys where the battlefield crosses the river
            if (!flagPlaced && x == middleX) {
                if (map[x][crossableY1] != null) {
                    Empty empty1 = new Empty(new Pose(new Vector2D(x, crossableY1), Orientation.SOUTH));
                    map[x][crossableY1].setStructure(empty1);
                    structuresList.put("empty_" + x + "_" + crossableY1, empty1);
                }
                if (map[x][crossableY2] != null) {
                    Empty empty2 = new Empty(new Pose(new Vector2D(x, crossableY2), Orientation.SOUTH));
                    map[x][crossableY2].setStructure(empty2);
                    structuresList.put("empty_" + x + "_" + crossableY2, empty2);
                }
                if (map[x][crossableY3] != null) {
                    Empty empty3 = new Empty(new Pose(new Vector2D(x, crossableY3), Orientation.SOUTH));
                    map[x][crossableY3].setStructure(empty3);
                    structuresList.put("empty_" + x + "_" + crossableY3, empty3);
                }
                flagPlaced = true;
            }

        }
    }

    private void addWallsAndGates() {
        int baseWidth = this.getWidth() / 12;
        int baseHeight = this.getHeight() / 4;
        int blueWallsIdx = 1;
        int redWallsIdx = 1;

        for (int x = 0; x < baseWidth + 1; x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate1 = new Gate("gate_b1", 100, false, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        Gate gate2 = new Gate("gate_b2", 100, false, new Pose(new Vector2D(x, y + 1), Orientation.SOUTH));

                        map[x][y].setStructure(gate1);
                        map[x][y + 1].setStructure(gate2);

                        structuresList.put(gate1.getName(), gate1);
                        structuresList.put(gate2.getName(), gate2);
                    } else if (y != this.getHeight() / 2) {
                        Wall wall = new Wall("wall_b" + blueWallsIdx, false, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        map[x][y].setStructure(wall);
                        structuresList.put(wall.getName(), wall);
                        blueWallsIdx++;
                    }
                }
            }
        }

        for (int x = this.getWidth() - baseWidth - 1; x < this.getWidth(); x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate1 = new Gate("gate_r1", 100, true, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        Gate gate2 = new Gate("gate_r2", 100, true, new Pose(new Vector2D(x, y + 1), Orientation.SOUTH));

                        map[x][y].setStructure(gate1);
                        map[x][y + 1].setStructure(gate2);

                        structuresList.put(gate1.getName(), gate1);
                        structuresList.put(gate2.getName(), gate2);
                    } else if (y != this.getHeight() / 2) {
                        Wall wall = new Wall("wall_r" + redWallsIdx, true, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        map[x][y].setStructure(wall);
                        structuresList.put(wall.getName(), wall);
                        redWallsIdx++;
                    }
                }
            }
        }
    }

    private void addBridge() {
        int bridgeY = (2 * this.getHeight()) / 3 + 1;
        int bridgeXStart = this.getWidth() / 2 - 1;
        int bridgeXEnd = this.getWidth() / 2 + 1;

        for (int x = bridgeXStart; x <= bridgeXEnd; x++) {
            for (int y = bridgeY; y < bridgeY + 2; y++) {
                Bridge bridge = new Bridge(0.1, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                map[x][y].setStructure(bridge);
                map[x][y].setZoneType(Zone.BATTLEFIELD);
                structuresList.put("bridge_" + x + "_" + y, bridge);
            }
        }
    }

    private boolean isValidTreeCell(int x, int y) {
        Cell cell = this.getCellByPosition(x, y);
        return cell != null
                && cell.getStructure() == null
                && cell.getZoneType() == Zone.BATTLEFIELD;
    }

    private void addTrees() {
        // Number of trees to spawn
        int treeCount = (this.width * this.height) / 33;

        // Define spawn area dimensions
        int northSpawnAreaWidth = this.getWidth() / 2 - 1;
        int southSpawnAreaWidth = this.getWidth() / 2 - 1;
        int spawnAreaHeight = this.getHeight() / 4 - 1;

        // Define bounds for the four areas (northern and southern)
        int northStartY = 0;
        int southStartY = this.getHeight() - spawnAreaHeight;
        int area1StartX = 0;
        int area2StartX = this.getWidth() - northSpawnAreaWidth;
        int area3StartX = 0;
        int area4StartX = this.getWidth() - southSpawnAreaWidth;

        // Define a list of all spawnable cells
        List<Cell> spawnableCells = new ArrayList<>();

        // Add northern areas to spawnable cells
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

        // Add southern areas to spawnable cells
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
        for (int i = 1; i <= treeCount && !spawnableCells.isEmpty(); i++) {
            int index = RAND.nextInt(spawnableCells.size());
            Cell selectedCell = spawnableCells.remove(index);
            Tree tree = new Tree("tree_" + i, 50, 20, new Pose(new Vector2D(selectedCell.getX(), selectedCell.getY()), Orientation.SOUTH));
            selectedCell.setStructure(tree);
            structuresList.put(tree.getName(), tree);
        }
    }

    public void spawnPrincess(boolean team) {
        Zone opponentBaseZone = team ? Zone.BBASE : Zone.RBASE;

        // Get all cells in the opponent's base zone
        List<Cell> allCellsInBaseZone = getAllCells(
                opponentBaseZone,
                null, null,
                null, null,
                null, null,
                true
        );

        if (allCellsInBaseZone.isEmpty()) {
            throw new IllegalStateException("No available cells in the opponent's base zone to spawn the Princess.");
        }

        // Determine the target column for spawning
        int targetColumn = team ? 0 : this.getWidth() - 1; // First column for BBASE, last column for RBASE

        // Filter cells to only those in the designated column
        List<Cell> availableCells = allCellsInBaseZone.stream()
                .filter(cell -> cell.getX() == targetColumn)
                .toList();

        if (availableCells.isEmpty()) {
            throw new IllegalStateException("No available cells in the designated column to spawn the Princess.");
        }

        // Randomly select a cell and spawn the princess
        Cell randomCell = availableCells.get(RAND.nextInt(availableCells.size()));

        String name = team ? "princess_r" : "princess_b";

        Princess princess = new Princess(
                name,
                team,
                new Pose(new Vector2D(randomCell.getX(), randomCell.getY()), Orientation.SOUTH)
        );

        randomCell.setResource(princess);
        resourcesList.put(princess.getName(), princess);
        System.out.println("Princess spawned at: " + randomCell.getX() + ", " + randomCell.getY());
    }


    public void printAgentList(Logger logger) {
        mapLock.readLock().lock();
        try {
            logger.info("Current agent list:");
            for (Map.Entry<String, Agent> entry : agentsList.entrySet()) {
                logger.info("Agent Name: " + entry.getKey() + ", Details: " + entry.getValue().getPose());
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
     * @param zoneType           Optional zone type to filter by (can be null).
     * @param structureClass     Optional class type of structure to filter by (can be null).
     * @param structurePredicate Optional predicate to filter structures by specific fields (can be null).
     * @param resourceClass      Optional class type of resource to filter by (can be null).
     * @param resourcePredicate  Optional predicate to filter resources by specific fields (can be null).
     * @param agentClass         Optional class type of agent to filter by (can be null).
     * @param agentPredicate     Optional predicate to filter agents by specific fields (can be null).
     * @param includeMatching    If true, returns cells matching the criteria; if false, returns the complement.
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
            mapLock.writeLock().lock();
            try {
                Cell newCell = this.getCellByPosition(x, y);
                if (!newCell.isOccupied(agent, null)) {
                    newCell.setAgent(agent);
                }
            } finally {
                mapLock.writeLock().unlock();
            }

//            this.getCellByPosition(x, y).setAgent(agent);
            agent.setPose(new Pose(Vector2D.of(x, y), orientation));
            this.agentsList.put(agent.getName(), agent);

            return true;
        }
    }

    private boolean setAgentPose(Agent agent, Vector2D afterStep, Orientation newOrientation) {
        return this.setAgentPose(agent, afterStep.getX(), afterStep.getY(), newOrientation);
    }

    public boolean spawnAgent(Agent agent) {
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
        while (randomCellPosition.isOccupied(agent, null)) {
            randomCellPosition = availableCells.get(RAND.nextInt(availableCells.size()));
        }
        Orientation randomOrientation = Orientation.random();

        return setAgentPose(agent, randomCellPosition.getX(), randomCellPosition.getY(), randomOrientation);
    }

    public boolean respawnAgent(Agent agent) {

        // Synchronize access to agentsList
//        synchronized (agentsList) {
//            // Remove the agent from the agentsList
//            agentsList.remove(agent.getName());
//        }

        mapLock.writeLock().lock();
        try {
            Cell agentCell = this.getCellByPosition(agent.getPose().getPosition());
            if (agentCell != null) {
                if (agentCell.getAgent().getCarriedItem() != null) {
                    agentCell.getAgent().stopCarrying(agentCell.getAgent().getCarriedItem());
                }
                //view.triggerDeathView(agentCell.getPosition());
                agentCell.clearAgent();



            }
        } finally {
            mapLock.writeLock().unlock();
        }

        agent.setHp(agent.getMaxHp());
        agent.setState("spawn");
        spawnAgent(agent);
        return true;
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

            if (targetCell.getZoneType() == Zone.OUT_OF_MAP) {
                return false;
            }

            synchronized (currentCell) {
                synchronized (targetCell) {
                    if (targetCell.isOccupied(agent, null)) {
                        return false; // Target cell is occupied
                    }

                    // Update agent position
                    this.setAgentPose(agent, newPosition, newOrientation);

                    currentCell.setAgent(null);
                    targetCell.setAgent(agent);

                    if (agent.getCarriedItem() != null) {
                        agent.getCarriedItem().setPose(new Pose(agent.getPose().getPosition(), Orientation.SOUTH));
                        this.resourcesList.put(agent.getCarriedItem().getName(), agent.getCarriedItem());
                        currentCell.setResource(null);
                        targetCell.setResource(agent.getCarriedItem());
                    }

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

    public boolean attackAgent(Agent attacking_agent, Agent target) {
        synchronized (target) {
            view.triggerAttackView(attacking_agent.getPose().getPosition());
            view.triggerDamageView(target.getPose().getPosition());
            int newHp = target.getHp() - attacking_agent.getAttackPower();
            target.setHp(newHp);
        }
        return true;
    }

    public boolean attackGate(Agent attacking_agent, Gate target) {
        target.takeDamage(attacking_agent.getAttackPower());
        view.triggerAttackView(attacking_agent.getPose().getPosition());
        view.triggerDamageView(target.getPose().getPosition());

        return true;
    }

    public boolean attackTree(Agent attacking_agent, Tree target) {
        synchronized (target) {
            target.takeDamage(attacking_agent.getAttackPower());
            view.triggerAttackView(attacking_agent.getPose().getPosition());
            view.triggerDamageView(target.getPose().getPosition());
        }
        return true;
    }

    private Pair<String, Vector2D> findClosestResource(Agent agent, Class<? extends Resource> resourceClass, Predicate<Resource> filter, String stateName) {
        List<Cell> resources = getAllCells(
                null,
                null, null,
                resourceClass, filter, // No resource filtering
                null, null, // No agent filtering
                true
        );


        Vector2D agentPosition = agent.getPose().getPosition();
        Vector2D closestPosition = null;
        double minDistance = Double.MAX_VALUE;

        for (Cell cell : resources) {
            double distance = calculateDistance(agentPosition, cell.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closestPosition = cell.getPosition();
            }
        }

        return closestPosition != null ? new Pair<>(stateName, closestPosition) : null;
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
//
//        // If the agent is carrying a princess, return its current position
//        if (agent.getState() != "ally_princess_reached" && agent.getCarriedItem() instanceof Princess) {
//            return new Pair("ally_princess_reached", agent.getPose().getPosition());
//        }

        // Retrieve princesses safely
        Optional<Princess> princessB = this.getPrincessByName("princess_b");
        Optional<Princess> princessR = this.getPrincessByName("princess_r");

        if (princessB.isPresent() && princessR.isPresent() && !(agent.getCarriedItem() instanceof Princess)) {
            boolean isBlueCarried = princessB.get().isCarried();
            boolean isRedCarried = princessR.get().isCarried();
            boolean isTeamBlue = !agent.getTeam(); // Assuming true = blue, false = red
            boolean isTeamRed = agent.getTeam(); // Assuming true = blue, false = red

            if ((isBlueCarried && isTeamBlue) || (isRedCarried && isTeamRed)) {
                Pair <String, Vector2D> test = findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(),
                        "follow_ally_princess");

                //System.out.println("[" + agent.getName() + "] is following ally princess carried at position " + test.getSecond() + ".");
                // **************CANCELLARE POIIIIIIIIIIIIIIIIIIIIIIIII*****************************
                if (agent.getCarriedItem() != null) {
                    System.out.println("IO (" + agent.getName() + ") sto portando: " + agent.getCarriedItem());
                } else {
                    System.out.println("[" + agent.getName() + "] is following ally princess carried at position " + test.getSecond() + ".");
                }
                return test;
            } else if ((isBlueCarried && isTeamRed) || (isRedCarried && isTeamBlue)) {
                Pair <String, Vector2D> test = findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() != agent.getTeam(),
                        "follow_enemy_princess");
                //System.out.println("[" + agent.getName() + "] is following enemy princess carried at position " + test.getSecond() + ".");
                return test;
            }
        }

        switch (agent.getState()) {
            // SPAWN -> CHOOSE PATH
            case "spawn":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "choose_path");

            // CHOOSE PATH -> TOWARDS LAND/BRIDGE
            case "choose_path":
                if (RAND.nextDouble() < agent.getLandProbability()) {
                    return new Pair("towards_land_passage", agent.getPose().getPosition());
                } else {
                    return new Pair("towards_bridge", agent.getPose().getPosition());
                }

            // TOWARDS LAND -> LAND REACHED
            case "towards_land_passage":
                return findClosestStructure(agent, Empty.class, null, "land_passage_reached");

            // TOWARDS BRIDGE -> BRIDGE REACHED
            case "towards_bridge":
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached");

            // LAND/BRIDGE REACHED -> ENEMY GATE REACHED
            case "land_passage_reached", "bridge_reached":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() != agent.getTeam(), "enemy_gate_reached");

            // ENEMY GATE REACHED -> ALLY PRINCESS REACHED
            case "enemy_gate_reached":
                if (agent.getCarriedItem() instanceof Princess) {
                    return new Pair("ally_princess_reached", agent.getPose().getPosition());
                }
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(), "ally_princess_reached");

            // ALLY PRINCESS REACHED -> ENEMY GATE REACHED BACK
            case "ally_princess_reached":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() != agent.getTeam(), "enemy_gate_reached_back");

            // ENEMY GATE REACHED BACK -> CHOOSE PATH BACK
            case "enemy_gate_reached_back":
                return new Pair("choose_path_back", agent.getPose().getPosition());

            // CHOOSE PATH BACK -> TOWARDS LAND/BRIDGE BACK
            case "choose_path_back":
                if (RAND.nextDouble() < .5) {
                    return new Pair("towards_land_passage_back", agent.getPose().getPosition());
                } else {
                    return new Pair("towards_bridge_back", agent.getPose().getPosition());
                }

            // TOWARDS LAND BACK -> LAND_PASSAGE_REACHED_BACK
            case "towards_land_passage_back":
                return findClosestStructure(agent, Empty.class, null, "land_passage_reached_back");

            // TOWARDS BRIDGE BACK -> BRIDGE REACHED BACK
            case "towards_bridge_back":
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached_back");

            // BRIDGE REACHED BACK -> GAME_WIN
            case "land_passage_reached_back", "bridge_reached_back":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "game_win");

            default:
                System.out.println("HO FALLITO PORCO DIO");
                return null;
            }
    }

    public Pair<String, Vector2D> getClosestObjectiveGatherer(Agent agent) {
        switch (agent.getState()) {
            case "spawn":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "gather_wood");

            case "gather_wood":
                return findClosestStructure(agent, Tree.class, null, "tree_reached");

            case "tree_reached":
                synchronized (this.getWoodAmountBlue()) {
                    synchronized (this.getWoodAmountRed()) {
                        if (!agent.getTeam() && this.getWoodAmountBlue().get() >= 5) { // If agent is blue and there is enough wood
                            return new Pair("choose_path", new Vector2D(agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY()));
                        } else if (agent.getTeam() && this.getWoodAmountRed().get() >= 5) { // Else if agent is red and there is enough wood
                            return new Pair("choose_path", new Vector2D(agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY()));
                        } else {
                            return new Pair("gather_wood", new Vector2D(agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY()));
                        }
                    }
                }

            case "choose_path":
                if (RAND.nextDouble() < agent.getLandProbability()) {
                    return new Pair("towards_land_passage", new Vector2D(agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY()));
                } else {
                    return new Pair("towards_bridge", new Vector2D(agent.getPose().getPosition().getX(), agent.getPose().getPosition().getY()));
                }

            case "land_passage_reached", "bridge_reached":
                return findClosestStructure(agent, Wall.class,
                        wall -> ((Wall) wall).getTeam() != agent.getTeam(), "enemy_wall_reached");

            /* CONTINUARE PIANO GATHERER; FARE ANCHE SU ASL CHECK WALL CHECK . MANCANO LE AZIONI BUILD LADDER E REPAIR GATE.*/

            default:
                return null;
        }
    }

    public Pair<String, Vector2D> getClosestObjective(Agent agent) {
        if (!(agent instanceof Gatherer)) {
            return getClosestObjectiveSoldier(agent);
        } else if (agent instanceof Gatherer) {
            return getClosestObjectiveGatherer(agent);
        }
        return null; // Return null if the agent type is not recognized
    }

    public boolean containsAgent(Agent agent) {
        synchronized (this.agentsList) {
            return this.agentsList.containsKey(agent.getName());
        }
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

    public Set<Agent> getAllAgents() {
        synchronized (this.agentsList) {
            return this.agentsList.values()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    public boolean containsStructure(MapStructure structure) {
        synchronized (this.structuresList) {
            return this.structuresList.containsKey(structure.getName());
        }
    }

    private boolean isStructureInRange(Agent agent, MapStructure structure, int range) {
        if (!containsAgent(agent) || !containsStructure(structure)) {
            return false;
        }
        BiFunction<Vector2D, Vector2D, Boolean> neighbourhoodFunction = (a, b) -> {
            Vector2D distanceVector = b.minus(a);
            return Math.abs(distanceVector.getX()) <= range
                    && Math.abs(distanceVector.getY()) <= range;
        };

        Vector2D agentPosition = this.getAgentPosition(agent);
        Vector2D neighbourPosition = structure.getPose().getPosition();
        return neighbourhoodFunction.apply(agentPosition, neighbourPosition);
    }

    public Optional<Gate> getGateByName(String gName) {
        synchronized (this.structuresList) {
            return this.getAllStructures(Gate.class).stream()
                    .filter(entry -> gName.equals(entry.getName()))
                    .map(gate -> (Gate) gate) // Cast to Gate
                    .findFirst();
        }
    }

    public Optional<Tree> getTreeByName(String tName) {
        synchronized (this.structuresList) {
            return this.getAllStructures(Tree.class).stream()
                    .filter(entry -> tName.equals(entry.getName()))
                    .map(tree -> (Tree) tree) // Cast to Tree
                    .findFirst();
        }
    }

    public Set<Gate> getEnemyGateNeighbours(Agent agent, int range) {
        //System.out.println(this.getAllStructures(Gate.class));
        return this.getAllStructures(Gate.class).stream()
                .filter(gate -> !gate.getTeam().equals(agent.getTeam())) // Only enemy gates
                .filter(gate -> this.isStructureInRange(agent, gate, range)) // Within range
                .map(gate -> (Gate) gate) // Cast to Gate
                .collect(Collectors.toSet());
    }

    public Set<Tree> getTreeNeighbours(Agent agent, int range) {
        return this.getAllStructures(Tree.class).stream() // Use Tree.class instead of Tree
                .filter(tree -> this.isStructureInRange(agent, tree, range))
                .map(tree -> (Tree) tree) // Cast to Tree
                .collect(Collectors.toSet());
    }

    public Set<MapStructure> getAllStructures(Class<? extends MapStructure> structureClass) {
        synchronized (this.structuresList) {
            //System.out.println(this.structuresList.values());
            return this.structuresList.values()
                    .stream()
                    .filter(structure -> structureClass.isInstance(structure))
                    .collect(Collectors.toSet());
        }
    }

    public Set<Resource> getAllResources(Class<? extends Resource> resourceClass) {
        synchronized (this.resourcesList) {
            //System.out.println(this.resourcesList.values());
            return this.resourcesList.values()
                    .stream()
                    .filter(resource -> resourceClass.isInstance(resource))
                    .collect(Collectors.toSet());
        }
    }

    public Optional<Princess> getPrincessByName(String pName) {
        synchronized (this.resourcesList) {
            return this.getAllResources(Princess.class).stream()
                    .filter(entry -> pName.equals(entry.getName()))
                    .map(princess -> (Princess) princess) // Cast to Princess
                    .findFirst();
        }
    }

    public Set<Princess> getAllyPrincessNeighbours(Agent agent, int range) {

        return this.getAllResources(Princess.class).stream()
                .filter(princess -> princess.getTeam() == agent.getTeam())
                .filter(princess -> this.isResourceInRange(agent, princess, range)) // Within range
                .map(princess -> (Princess) princess)
                .collect(Collectors.toSet());
    }

    private boolean isResourceInRange(Agent agent, Resource resource, int range) {
        if (!containsAgent(agent) || !containsResource(resource) || resource.isCarried()) {
            return false;
        }
        BiFunction<Vector2D, Vector2D, Boolean> neighbourhoodFunction = (a, b) -> {
            Vector2D distanceVector = b.minus(a);
            return Math.abs(distanceVector.getX()) <= range
                    && Math.abs(distanceVector.getY()) <= range;
        };

        Vector2D agentPosition = this.getAgentPosition(agent);
        Vector2D neighbourPosition = resource.getPose().getPosition();
        return neighbourhoodFunction.apply(agentPosition, neighbourPosition);
    }

    public boolean containsResource(Resource resource) {
        synchronized (this.resourcesList) {
            return this.resourcesList.containsKey(resource.getName());
        }
    }

    public boolean pickUpPrincess(Agent agent, Princess target) {
        mapLock.writeLock().lock();
        try {
            Vector2D p_pos = target.getPose().getPosition();
            agent.startCarrying(target);
            this.getCellByPosition(p_pos.getX(), p_pos.getY()).clearResource();
            this.getCellByPosition(agent.getPose().getPosition()).setResource(target);
            return true;
        } finally {
            mapLock.writeLock().unlock();
        }
    }

    public void setView(MapView view) {
        this.view = view;
    }
}
