package env.utils;

import env.MapView;
import env.agents.*;
import env.objects.structures.*;
import env.objects.resources.*;
import env.ConfigWindow;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameMap {
    private final int width;
    private final int height;
    private Boolean win = null;
    private final AtomicInteger woodAmountBlue = new AtomicInteger(0);
    private final AtomicInteger woodAmountRed = new AtomicInteger(0);
    private final int enoughWoodAmount = 5;
    private final Cell[][] map;
    private final Map<String, Agent> agentsList = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, MapStructure> structuresList = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Resource> resourcesList = Collections.synchronizedMap(new HashMap<>());
    private Vector2D bluePrincessSpawnPoint = null;
    private Vector2D redPrincessSpawnPoint = null;


    private final ReadWriteLock mapLock = new ReentrantReadWriteLock();
    private static final Random RAND = new Random();

    private MapView view; // Keep reference to the existing view
    private final int baseWidth;
    private final int baseHeight;

    public GameMap(int width, int height, MapView view) {
        this.width = Objects.requireNonNull(width);
        this.height = Objects.requireNonNull(height);
        this.map = new Cell[width][height];
        this.view = view;
        this.baseWidth = this.getWidth() / 6;
        this.baseHeight = this.getHeight() / 4;
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

    public synchronized AtomicInteger getWoodAmountRed() {
        return this.woodAmountRed;
    }

    public synchronized AtomicInteger getWoodAmountBlue() {
        return this.woodAmountBlue;
    }

    public synchronized boolean isEnoughWoodRed() { return (this.getWoodAmountRed().get() >= this.enoughWoodAmount); }

    public synchronized boolean isEnoughWoodBlue() { return (this.getWoodAmountBlue().get() >= this.enoughWoodAmount); }

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
        // Set base zones
        for (int x = 0; x < baseWidth; x++) {
            for (int y = baseHeight; y < this.getHeight() - baseHeight; y++) {
                map[x][y] = new Cell(Zone.BBASE, x, y);
                map[this.getWidth() - x - 1][this.getHeight() - y - 1] = new Cell(Zone.RBASE, this.getWidth() - x - 1, this.getHeight() - y - 1);
            }
        }

        // Set OUT_OF_MAP zones above and below the bases

        for (int x = 0; x < baseWidth + 1; x++) {
            for (int y = 0; y < baseHeight - 1; y++) {
                map[x][y] = new Cell(Zone.OUT_OF_MAP, x, y);
                map[x][this.getHeight() - y - 1] = new Cell(Zone.OUT_OF_MAP, x, this.getHeight() - y - 1);

                int mirroredX = this.getWidth() - x - 1;
                map[mirroredX][y] = new Cell(Zone.OUT_OF_MAP, mirroredX, y);
                map[mirroredX][this.getHeight() - y - 1] = new Cell(Zone.OUT_OF_MAP, mirroredX, this.getHeight() - y - 1);
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
            if (x < baseWidth || x >= this.getWidth() - baseWidth) {
                continue;
            }

            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD, x, crossableY1);
            }
            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD, x, crossableY2);
            }
            if (map[x][crossableY3] != null && map[x][crossableY3].getZoneType() == Zone.OUT_OF_MAP) {
                map[x][crossableY3] = new Cell(Zone.BATTLEFIELD, x, crossableY3);
            }
        }

        for (int x = 0; x < this.getWidth(); x++) {
            for (int y = 0; y < this.getHeight(); y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD, x, y);
                }
            }


            if (!flagPlaced && x == middleX) {
                if (map[x][crossableY1] != null) {
                    Empty empty1 = new Empty(new Pose(new Vector2D(x, crossableY1), Orientation.SOUTH), "half");
                    map[x][crossableY1].setStructure(empty1);
                    structuresList.put("empty_" + x + "_" + crossableY1, empty1);
                }
                if (map[x][crossableY2] != null) {
                    Empty empty2 = new Empty(new Pose(new Vector2D(x, crossableY2), Orientation.SOUTH), "half");
                    map[x][crossableY2].setStructure(empty2);
                    structuresList.put("empty_" + x + "_" + crossableY2, empty2);
                }
                if (map[x][crossableY3] != null) {
                    Empty empty3 = new Empty(new Pose(new Vector2D(x, crossableY3), Orientation.SOUTH), "half");
                    map[x][crossableY3].setStructure(empty3);
                    structuresList.put("empty_" + x + "_" + crossableY3, empty3);
                }
                flagPlaced = true;
            }
        }
    }

    private void addWallsAndGates() {
        int blueWallsIdx = 1;
        int redWallsIdx = 1;

        for (int x = 0; x < baseWidth + 1; x++) {
            for (int y = baseHeight - 1; y < this.getHeight() - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == this.getHeight() / 2 - 1) {
                        Gate gate1 = new Gate("gate_b1", 50, false, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        Gate gate2 = new Gate("gate_b2", 50, false, new Pose(new Vector2D(x, y + 1), Orientation.SOUTH));

                        map[x][y].setStructure(gate1);
                        map[x][y + 1].setStructure(gate2);
                        map[x][y].setZoneType(Zone.BBASE);
                        map[x][y + 1].setZoneType(Zone.BBASE);

                        structuresList.put(gate1.getName(), gate1);
                        structuresList.put(gate2.getName(), gate2);

                        Empty empty1 = new Empty(new Pose(new Vector2D(x + 2, y), Orientation.SOUTH), "base_b");
                        Empty empty2 = new Empty(new Pose(new Vector2D(x + 2, y + 1), Orientation.SOUTH), "base_b");
                        map[x+2][y].setStructure(empty1);
                        map[x+2][y + 2].setStructure(empty2);
                        structuresList.put("empty_" + (x + 2) + "_" + y, empty1);
                        structuresList.put("empty_" + (x + 2) + "_" + (y + 1), empty2);
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
                        Gate gate1 = new Gate("gate_r1", 50, true, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                        Gate gate2 = new Gate("gate_r2", 50, true, new Pose(new Vector2D(x, y + 1), Orientation.SOUTH));

                        map[x][y].setStructure(gate1);
                        map[x][y + 1].setStructure(gate2);

                        map[x][y].setZoneType(Zone.RBASE);
                        map[x][y + 1].setZoneType(Zone.RBASE);

                        structuresList.put(gate1.getName(), gate1);
                        structuresList.put(gate2.getName(), gate2);

                        Empty empty1 = new Empty(new Pose(new Vector2D(x - 2, y), Orientation.SOUTH), "base_r");
                        Empty empty2 = new Empty(new Pose(new Vector2D(x - 2, y + 1), Orientation.SOUTH), "base_r");
                        map[x-2][y].setStructure(empty1);
                        map[x-2][y + 2].setStructure(empty2);
                        structuresList.put("empty_" + (x - 2) + "_" + y, empty1);
                        structuresList.put("empty_" + (x - 2) + "_" + (y + 1), empty2);
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
                Bridge bridge = new Bridge(10, new Pose(new Vector2D(x, y), Orientation.SOUTH));
                map[x][y].setStructure(bridge);
                map[x][y].setZoneType(Zone.BATTLEFIELD);
                structuresList.put("bridge_" + x + "_" + y, bridge);
            }
        }
    }

//    private boolean isValidTreeCell(Cell cell, int baseWidth) {
//        return cell != null
//                && cell.getStructure() == null
//                && cell.getZoneType() != Zone.BBASE
//                && cell.getZoneType() != Zone.RBASE
//                && cell.getZoneType() != Zone.OUT_OF_MAP
//                && cell.getZoneType() == Zone.BATTLEFIELD;
//                && cell.getX() >  baseWidth
//                && cell.getX() <  this.getWidth() - baseWidth;
//    }

//    private void addTrees() {
//        int baseWidth = this.getWidth() / 12;
//        int treeCount = (this.width * this.height) / 33;
//
//        List<Cell> spawnableCells = new ArrayList<>();
//        for (int x = 0; x < this.getWidth(); x++) {
//            for (int y = 0; y < this.getHeight(); y++) {
//                Cell cell = this.getCellByPosition(x, y);
//                if (this.isValidTreeCell(cell, baseWidth)) {
//                    spawnableCells.add(cell);
//                }
//
//            }
//        }
//
//        for (int i = 1; i <= treeCount && !spawnableCells.isEmpty(); i++) {
//            int index = RAND.nextInt(spawnableCells.size());
//            Cell selectedCell = spawnableCells.remove(index);
//            Tree tree = new Tree("tree_" + i, 50, 30000, new Pose(new Vector2D(selectedCell.getX(), selectedCell.getY()), Orientation.SOUTH));
//            selectedCell.setStructure(tree);
//            structuresList.put(tree.getName(), tree);
//        }
//
//
//    }

    private boolean isValidTreeCell(int x, int y) {
        Cell cell = this.getCellByPosition(x, y);
        return cell != null
                && cell.getStructure() == null
                && cell.getZoneType() != Zone.BBASE
                && cell.getZoneType() != Zone.RBASE
                && cell.getZoneType() != Zone.OUT_OF_MAP
                && cell.getZoneType() == Zone.BATTLEFIELD;
    }

    private void removeAdjacentCells(List<Vector2D> positions, Vector2D treePos) {
        int[][] neighbors = {
                {-1, -1}, {0, -1}, {1, -1},  // Top-left, Top, Top-right
                {-1,  0},         {1,  0},  // Left,        Right
                {-1,  1}, {0,  1}, {1,  1}   // Bottom-left, Bottom, Bottom-right
        };

        positions.remove(treePos);  // Remove the tree position itself

        for (int[] offset : neighbors) {
            Vector2D neighborPos = Vector2D.of(treePos.getX() + offset[0], treePos.getY() + offset[1]);
            positions.remove(neighborPos); // Remove adjacent positions
        }
    }


    private void addTrees() {
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
        List<Vector2D> spawnablePositions = new ArrayList<>();

        // Add northern areas to spawnable cells
        for (int x = area1StartX; x < area1StartX + northSpawnAreaWidth; x++) {
            for (int y = northStartY; y < northStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }
        for (int x = area2StartX; x < area2StartX + northSpawnAreaWidth; x++) {
            for (int y = northStartY; y < northStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }

        // Add southern areas to spawnable cells
        for (int x = area3StartX; x < area3StartX + southSpawnAreaWidth; x++) {
            for (int y = southStartY; y < southStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }
        for (int x = area4StartX; x < area4StartX + southSpawnAreaWidth; x++) {
            for (int y = southStartY; y < southStartY + spawnAreaHeight; y++) {
                if (isValidTreeCell(x, y)) {
                    spawnablePositions.add(Vector2D.of(x, y));
                }
            }
        }

        // Randomly place trees in the spawnable cells
        Random random = new Random();
        for (int i = 1; i <= treeCount && !spawnablePositions.isEmpty(); i++) {
            int index = random.nextInt(spawnablePositions.size());
            Vector2D selectedPos = spawnablePositions.remove(index);  // Remove chosen cell from list
            Cell selectedCell = map[selectedPos.getX()][selectedPos.getY()];

            // Place tree
            Tree tree = new Tree("tree_" + i, 50, 30000, new Pose(selectedPos, Orientation.SOUTH));
            selectedCell.setStructure(tree);
            structuresList.put(tree.getName(), tree);

            // **NEW: Remove adjacent cells from spawnable list**
            removeAdjacentCells(spawnablePositions, selectedPos);
        }
    }


    public void spawnPrincess(boolean team) {
        synchronized (this.map) {
            synchronized (this.structuresList) {
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
            Vector2D princessSpawnPoint = randomCell.getPosition();
            String name = team ? "princess_r" : "princess_b";
            if (name == "princess_r") {
                Empty empty_pr = new Empty(new Pose(princessSpawnPoint, Orientation.SOUTH), "empty_pr");
                this.getCellByPosition(princessSpawnPoint).setStructure(empty_pr);
                structuresList.put("empty_pr", empty_pr);

                redPrincessSpawnPoint = princessSpawnPoint;
            } else if (name == "princess_b" ) {
                Empty empty_pb = new Empty(new Pose(princessSpawnPoint, Orientation.SOUTH), "empty_pb");
                this.getCellByPosition(princessSpawnPoint).setStructure(empty_pb);
                structuresList.put("empty_pb", empty_pb);

                bluePrincessSpawnPoint = princessSpawnPoint;

            }
            Princess princess = new Princess(
                    name,
                    team,
                    new Pose(princessSpawnPoint, Orientation.SOUTH)
            );

            randomCell.setResource(princess);
            resourcesList.put(princess.getName(), princess);
            System.out.println("Princess spawned at: " + randomCell.getX() + ", " + randomCell.getY());
            }
        }
    }

    public void printAgentList(Logger logger) {
        mapLock.readLock().lock();
        try {
            logger.info("Current agent list:");
            for (Map.Entry<String, Agent> entry : agentsList.entrySet()) {
                logger.info("Agent Name: " + entry.getKey() + ", Details: " + entry.getValue().getCarriedItem());
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

    public synchronized Cell getCellByPosition(int x, int y) {
        mapLock.readLock().lock();
        try {
            synchronized (this.map) {
                if (!this.isPositionInside(x, y)) {
                    return null;
                }
                return map[x][y];
            }
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public synchronized Cell getCellByPosition(Vector2D position) {
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
    public synchronized List<Cell> getAllCells(
            Zone zoneType,
            Class<? extends MapStructure> structureClass, Predicate<MapStructure> structurePredicate,
            Class<? extends Resource> resourceClass, Predicate<Resource> resourcePredicate,
            Class<? extends Agent> agentClass, Predicate<Agent> agentPredicate,
            boolean includeMatching) {

        mapLock.readLock().lock();
        try {
            synchronized (this.map) {
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
            }
        } finally {
            mapLock.readLock().unlock();
        }
    }

    // AGENTS MANAGEMENT
    public synchronized void setAgentPosition(Agent agent, Vector2D position) {
//        synchronized (this.agentsList) {
            Pose currentPose = this.agentsList.get(agent.getName()).getPose();
            agent.setPose(new Pose(position, currentPose.getOrientation()));
            this.agentsList.put(agent.getName(), agent);
//        }
    }

    public synchronized void setAgentDirection(Agent agent, Orientation orientation) {
//        synchronized (this.agentsList) {
            Pose currentPose = this.agentsList.get(agent.getName()).getPose();
            agent.setPose(new Pose(currentPose.getPosition(), orientation));
            this.agentsList.put(agent.getName(), agent);
//        }
    }

    public synchronized boolean setAgentPose(Agent agent, int x, int y, Orientation orientation) {
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
                return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
            }
            mapLock.writeLock().lock();
            try {
                synchronized (this.map) {
                    Cell newCell = this.getCellByPosition(x, y);
                    if (!newCell.isOccupied(agent, null)) {
                        newCell.setAgent(agent);
                    }
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

    private synchronized boolean setAgentPose(Agent agent, Vector2D afterStep, Orientation newOrientation) {
        return this.setAgentPose(agent, afterStep.getX(), afterStep.getY(), newOrientation);
    }

    private synchronized  Cell getRandomBaseCell(Agent agent) {
        // Get all unoccupied cells in the team's base zone with their positions
        List<Cell> availableCells = getAllCells(
                (agent.getTeam() == true) ? Zone.RBASE : Zone.BBASE,
                null, null,
                null, null,
                null, null,
                true
        );

        if (availableCells.isEmpty()) {
            return null; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA // No available cells in the base
        }

        // Select a random cell from the available cells
        Cell randomCell = availableCells.get(RAND.nextInt(availableCells.size()));
        while (randomCell.isOccupied(agent, null)) {
            randomCell = availableCells.get(RAND.nextInt(availableCells.size()));
        }

        return randomCell;
    }

//    public synchronized boolean removeAgent(Agent agent) {
//        synchronized (this.map) {
//            synchronized (this.agentsList) {
//                synchronized (this.resourcesList) {
//                    // If the agent's HP is 0 or less, perform respawn logic
//                    Cell agentCell = this.getCellByPosition(agent.getPose().getPosition());
//                    if (agentCell != null) {
//                        if (agentCell.getAgent() != null && agentCell.getAgent().getCarriedItem() != null) {
//                            agentCell.getAgent().stopCarrying(agentCell.getAgent().getCarriedItem());
//                        }
//                        agentCell.clearAgent();
//                        this.agentsList.remove(agent.getName());
//                        agent.setState("dead");
//
//                        return true;
//                    }
//                    return false;
//                    }
//            }
//        }
//    }
    public synchronized boolean resetAgentHp (Agent agent) {
        synchronized (this.agentsList) {
            agent.setHp(agent.getMaxHp());
            return true;
        }
    }

    public synchronized boolean spawnAgent(Agent agent) {
        mapLock.writeLock().lock();
        boolean result;
        try {
            synchronized (this.map) {
                synchronized (this.agentsList) {

                    if (agent.getHp() <= 0) {
                        // If the agent's HP is 0 or less, perform respawn logic
                        Cell agentCell = this.getCellByPosition(agent.getPose().getPosition());
                        if (agentCell != null) {
                            if (agentCell.getAgent() != null && agentCell.getAgent().getCarriedItem() != null) {
                                synchronized (this.resourcesList) {

                                    agentCell.setResource(agentCell.getAgent().getCarriedItem());
                                    agentCell.getAgent().stopCarrying(agentCell.getAgent().getCarriedItem());

                                    this.resourcesList.put(agentCell.getResource().getName(), agentCell.getResource());
                                }
                            }
                            agentCell.clearAgent();
                        }
                    }

//                agent.setHp(agent.getMaxHp());
                agent.setState("spawn");
                Cell randomCell = getRandomBaseCell(agent);

                if (randomCell != null) {
                    result = setAgentPose(agent, randomCell.getX(), randomCell.getY(), Orientation.random());
                } else {
                    result = false;
                }

            }
        }
        } finally {
            mapLock.writeLock().unlock();
        }
        return result;
    }

    public synchronized boolean moveAgent(Agent agent, Vector2D newPosition, Orientation newOrientation) {
        mapLock.writeLock().lock();
        try {
            synchronized (this.map) {
                synchronized (this.resourcesList) {
                    synchronized (this.structuresList) {
                        synchronized (this.agentsList)  {

                        if (!isPositionInside(newPosition.getX(), newPosition.getY())) {
                            return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
                        }

                        Pose currentPose = agentsList.get(agent.getName()).getPose();

                        Cell currentCell = this.getCellByPosition(currentPose.getPosition().getX(), currentPose.getPosition().getY());
                        Cell targetCell = this.getCellByPosition(newPosition.getX(), newPosition.getY());

                        if (targetCell.getZoneType() == Zone.OUT_OF_MAP) {
                            return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
                        }

    //                synchronized (currentCell) {
    //                    synchronized (targetCell) {
                        if (targetCell.isOccupied(agent, null)) {
                            return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA // Target cell is occupied
                        }
                            // **NEW: Check if target cell has a bridge and apply death probability**
//                            if (targetCell.getStructure() instanceof Bridge bridge) {
//                                double slipProbability = bridge.getSlipProbability();
//                                if (new Random().nextDouble() < slipProbability) {
//                                    System.out.println(agent.getName() + " slipped and died on the bridge at (" + newPosition.getX() + "," + newPosition.getY() + ")!");
//                                    agent.setHp(0);
//                                    spawnAgent(agent); // Ensure this method exists in Agent
//                                    return false; // Agent dies, movement is canceled
//                                }
//                            }

                        // Update agent position
                        this.setAgentPose(agent, newPosition, newOrientation);

                        currentCell.setAgent(null);
                        targetCell.setAgent(agent);

                        if (agent.getCarriedItem() != null) {
                            synchronized (this.resourcesList) {

                                agent.getCarriedItem().setPose(new Pose(agent.getPose().getPosition(), Orientation.SOUTH));
                                currentCell.setResource(null);
                                targetCell.setResource(agent.getCarriedItem());

                                this.resourcesList.put(agent.getCarriedItem().getName(), agent.getCarriedItem());

                            }
                        }

                        return true;
    //                    }
    //                }
                        }
                    }
                }
            }

        } finally {
            mapLock.writeLock().unlock();
        }
    }

    public synchronized boolean moveAgent(Agent agent, Vector2D newPosition, Direction direction) {
        Pose currentPose = this.agentsList.get(agent.getName()).getPose();
        Orientation newOrientation = currentPose.getOrientation().rotate(direction);

        // Delegate to the main method
        return moveAgent(agent, newPosition, newOrientation);
    }

    public synchronized boolean moveAgent(Agent agent, int stepSize, Direction direction) {
        Pose currentPose = this.agentsList.get(agent.getName()).getPose();
        Orientation newOrientation = currentPose.getOrientation().rotate(direction);

        // Delegate to the main method
        return moveAgent(agent, currentPose.getPosition().afterStep(stepSize, newOrientation), newOrientation);
    }

    public synchronized boolean attackAgent(Agent attacking_agent, Agent target, boolean crit) {
        synchronized (this.agentsList) {
            if (target.getHp() > 0) {
                int originalAttackPower = attacking_agent.getAttackPower();

                if (crit) {
                    attacking_agent.setAttackPower(originalAttackPower * 5);
                }

                view.triggerAttackView(attacking_agent.getPose().getPosition());
                view.triggerDamageView(target.getPose().getPosition());
                int newHp = target.getHp() - attacking_agent.getAttackPower();
                target.setHp(newHp);
                if (crit) {
                    attacking_agent.setAttackPower(originalAttackPower);
                }
                return true;

            } else {
                return false; }//TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
            }
        }

    public synchronized boolean healAgent(Agent healing_agent, Agent target) {
        synchronized (this.agentsList) {
            if ((target.getHp()) > 0 && (target.getHp() < target.getMaxHp())) {
                view.triggerAttackView(healing_agent.getPose().getPosition());
                view.triggerHealView(target.getPose().getPosition());

                int newHp = 0;
                if ((healing_agent instanceof Priest priest)) {
                    newHp = target.getHp() + priest.getHealPower();
                }

                target.setHp(newHp);
                return true;
            } else {
                return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
            }
        }
    }

    public synchronized boolean attackGate(Agent attacking_agent, Gate target) {
        synchronized (this.structuresList) {
            if (target.getHp() > 0) {
                view.triggerAttackView(attacking_agent.getPose().getPosition());
                view.triggerDamageView(target.getPose().getPosition());
                target.takeDamage(attacking_agent.getAttackPower());
                return true;
            } else {
                return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
            }
        }
    }

    public synchronized boolean repairGate(Agent repairing_agent, Gate target) {
        synchronized (this.structuresList) {
            boolean isBlueTeam = !repairing_agent.getTeam();
            boolean isRedTeam = repairing_agent.getTeam();

            if (target.isDestroyed()) {
                if (isBlueTeam) {
                    synchronized (this.getWoodAmountBlue()) {
                        if (this.getWoodAmountBlue().get() >= this.enoughWoodAmount) {
                            target.repair();
                            this.woodAmountBlue.addAndGet(-this.enoughWoodAmount);
                            return true;
                        }
                    }
                } else if (isRedTeam) {
                    synchronized (this.getWoodAmountRed()) {
                        if (this.getWoodAmountRed().get() >= this.enoughWoodAmount) {
                            target.repair();
                            this.woodAmountRed.addAndGet(-this.enoughWoodAmount);
                            return true;
                        }
                    }
                }
            }

            return false; // TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
        }
    }


    public synchronized void addWood(Agent agent) {
        synchronized (this.woodAmountBlue){
            synchronized (this.woodAmountRed){
                if (!agent.getTeam()) {
                    woodAmountBlue.incrementAndGet();
                } else if (agent.getTeam()) {
                    woodAmountRed.incrementAndGet();
                }
            }
        }
    }
    public synchronized boolean attackTree(Agent attacking_agent, Tree target) {
        synchronized (this.structuresList) {
            if (target.getHp() > 0) {
                view.triggerAttackView(attacking_agent.getPose().getPosition());
                view.triggerDamageView(target.getPose().getPosition());
                target.takeDamage(attacking_agent.getAttackPower());
                if (target.getHp() == 0) {
                    addWood(attacking_agent);
                }

                return true;
            } else {
                return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
            }
        }
    }

    private synchronized Pair<String, Vector2D> findClosestResource(Agent agent, Class<? extends Resource> resourceClass, Predicate<Resource> filter, String stateName) {
        synchronized (this.map) {
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
    }

    private synchronized Pair<String, Vector2D> findClosestStructure(Agent agent, Class<? extends MapStructure> structureClass, Predicate<MapStructure> filter, String stateName) {
        synchronized (this.map) {
            Predicate<MapStructure> combinedFilter = structure -> {
                if (structure instanceof Tree tree) {
                    return !tree.isDestroyed() && (filter == null || filter.test(structure));
                }
                return filter == null || filter.test(structure);
            };

            List<Cell> structures = getAllCells(
                    null,
                    structureClass, combinedFilter,
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
    }

    private double calculateDistance(Vector2D pos1, Vector2D pos2) {
        return Math.sqrt(
                Math.pow(pos1.getX() - pos2.getX(), 2) + Math.pow(pos1.getY() - pos2.getY(), 2)
        );
    }

    private synchronized Pair<String, Vector2D> fallbackPlanGeneral(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();

        agent.setState("fallback_general");

        boolean isTeamBlue = !agent.getTeam(); // True if the agent belongs to the blue team
        boolean isTeamRed = agent.getTeam(); // True if the agent belongs to the red team

        boolean isInAllyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.BBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.RBASE);
        // The agent is in the ally base if it's a blue agent in the blue base or a red agent in the red base

        boolean isInEnemyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.RBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.BBASE);
        // The agent is in the enemy base if it's a blue agent in the red base or a red agent in the blue base

        boolean isOnRightSide = agent_position.getX() > this.getWidth() / 2;
        // The agent is on the right side of the map

        boolean isOnLeftSide = agent_position.getX() <= this.getWidth() / 2;
        // The agent is on the left side of the map


        boolean isOnLowerSide = agent_position.getY() > this.getHeight() / 2;
        // The agent is on the right side of the map

        boolean isOnUpperSide = agent_position.getY() <= this.getHeight() / 2;
        // The agent is on the left side of the map


        if (isInAllyBase) {
            return new Pair<>("spawn", agent_position);
        } else if ((isOnRightSide & isTeamBlue) || (isOnLeftSide & isTeamRed)) {
            if (isOnUpperSide) {
                return new Pair<>("land_passage_reached", agent_position);
            } else if(isOnLowerSide) {
                return new Pair<>("bridge_reached", agent_position);
            }
        } else if ((isOnLeftSide & isTeamBlue) || (isOnRightSide & isTeamRed)) {
            if (isOnUpperSide) {
                return new Pair<>("towards_land_passage", agent_position);
            } else if(isOnLowerSide) {
                return new Pair<>("towards_bridge", agent_position);
            }
        } else if (isInEnemyBase) {
            return new Pair<>("enemy_gate_reached", agent_position);
        }

        return null;
    }

    private synchronized Pair<String, Vector2D> fallbackPlanRescueAllyPrincess(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();
        Cell agent_cell = getCellByPosition(agent_position);

        agent.setState("fallback_ally_princess");

        boolean isTeamBlue = !agent.getTeam(); // True if the agent belongs to the blue team
        boolean isTeamRed = agent.getTeam(); // True if the agent belongs to the red team

        boolean isInAllyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.BBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.RBASE);
        boolean isInEnemyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.RBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.BBASE);
        // The agent is in the enemy base if it's a blue agent in the red base or a red agent in the blue base

        boolean isOnBridge = (agent_cell.getStructure() != null && agent_cell.getStructure() instanceof Bridge);

        boolean isOnRightSide = agent_position.getX() > this.getWidth() / 2;
        // The agent is on the right side of the map

        boolean isOnLeftSide = agent_position.getX() < this.getWidth() / 2;
        // The agent is on the left side of the map

        boolean isOnTheMiddle = agent_position.getX() == this.getWidth() / 2;

        boolean isOnLowerSide = agent_position.getY() > this.getHeight() / 2;
        // The agent is on the right side of the map

        boolean isOnUpperSide = agent_position.getY() <= this.getHeight() / 2;
        // The agent is on the left side of the map

        if (isInAllyBase) {
            this.win = agent.getTeam(); // false blue team, true red team
            if (this.win != null) {
                endGame(this.win);
            }
            return new Pair<>("game_win", agent_position);
        } else if (isInEnemyBase) {
            if (isTeamBlue) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_r"), "choose_path_back");
            } else if (isTeamRed) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_b"), "choose_path_back");
            }
        } else if (isOnTheMiddle || isOnBridge) {
            return findClosestStructure(agent, Gate.class,
                    gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
        } else if (((isOnRightSide & isTeamBlue) || (isOnLeftSide & isTeamRed))) {

            if (isOnUpperSide) {
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached_back");
            } else if (isOnLowerSide) {
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached_back");
            }

        } else if ((isOnLeftSide & isTeamBlue) || (isOnRightSide & isTeamRed)) {
            return findClosestStructure(agent, Gate.class,
                    gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
        }

        return null;
    }

    private synchronized Pair<String, Vector2D> fallbackPlanCaptureEnemyPrincess(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();
        Cell agent_cell = getCellByPosition(agent_position);

        agent.setState("fallback_enemy_princess");

        boolean isTeamBlue = !agent.getTeam(); // True if the agent belongs to the blue team
        boolean isTeamRed = agent.getTeam(); // True if the agent belongs to the red team

        boolean isInAllyBase = (isTeamBlue && getCellByPosition(agent_position).getZoneType() == Zone.BBASE) ||
                (isTeamRed && getCellByPosition(agent_position).getZoneType() == Zone.RBASE);
        // The agent is in the enemy base if it's a blue agent in the red base or a red agent in the blue base

        boolean isOnBridge = (agent_cell.getStructure() != null && agent_cell.getStructure() instanceof Bridge);

        boolean isOnRightSide = agent_position.getX() > this.getWidth() / 2;
        // The agent is on the right side of the map

        boolean isOnLeftSide = agent_position.getX() <= this.getWidth() / 2;
        // The agent is on the left side of the map

        boolean isOnTheMiddle = agent_position.getX() == this.getWidth() / 2;

        boolean isOnLowerSide = agent_position.getY() > this.getHeight() / 2;
        // The agent is on the right side of the map

        boolean isOnUpperSide = agent_position.getY() <= this.getHeight() / 2;
        // The agent is on the left side of the map

        if (isInAllyBase) {
            if (isTeamBlue) {
                Pair<String, Vector2D> result = findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "empty_pr", "spawn");

                if (agent_position.equals(redPrincessSpawnPoint)) {
                    System.out.println("\nFuori\n");
                    if (agent.getCarriedItem() != null) {
                        System.out.println("\nDentro\n");
                        agent_cell.setResource(agent.getCarriedItem());
                        agent.stopCarrying(agent.getCarriedItem());

                        synchronized (this.resourcesList) {
                            this.resourcesList.put(agent_cell.getResource().getName(), agent_cell.getResource());
                        }
                        synchronized (this.agentsList) {
                            this.agentsList.put(agent.getName(), agent);
                        }
                    }
                }
                return result;
            } else if (isTeamRed) {
                Pair<String, Vector2D> result = findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "empty_pb", "spawn");

                if (agent_position.equals(bluePrincessSpawnPoint)) {
                    System.out.println("\nFuori\n");
                    if (agent.getCarriedItem() != null) {
                        System.out.println("\nDentro\n");
                        agent_cell.setResource(agent.getCarriedItem());
                        agent.stopCarrying(agent.getCarriedItem());

                        synchronized (this.resourcesList) {
                            this.resourcesList.put(agent_cell.getResource().getName(), agent_cell.getResource());
                        }
                        synchronized (this.agentsList) {
                            this.agentsList.put(agent.getName(), agent);
                        }
                    }
                }
                return result;
            }
        } else if (isOnTheMiddle || isOnBridge) {
            return findClosestStructure(agent, Gate.class,
                    gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
        } else if ((isOnRightSide & isTeamBlue) || (isOnLeftSide & isTeamRed)) {

            if (isOnUpperSide) {
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached_back");
            } else if (isOnLowerSide) {
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached_back");
            }

        } else if ((isOnLeftSide & isTeamBlue) || (isOnRightSide & isTeamRed)) {
            if (isTeamBlue) {
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
            } else if (isTeamRed) {
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam().equals(agent.getTeam()), "back_to_base");
            }
        }

        return null;
    }


    // SE ANCORA NON VA AGGIUNGERE DA QUI I SYNCRO CORRETTI
    private synchronized Pair<String, Vector2D> handlePrincessScenarios(Agent agent) {
        Vector2D agent_position = agent.getPose().getPosition();

        Optional<Princess> princessB = this.getPrincessByName("princess_b");
        Optional<Princess> princessR = this.getPrincessByName("princess_r");

        boolean isOnRightSidePB = agent_position.getX() > princessB.get().getPose().getPosition().getX();
        boolean isOnLeftSidePB = agent_position.getX() <= princessB.get().getPose().getPosition().getX();

        boolean isOnRightSidePR = agent_position.getX() > princessR.get().getPose().getPosition().getX();
        boolean isOnLeftSidePR = agent_position.getX() <= princessR.get().getPose().getPosition().getX();

        // The agent is on the left side wrt the princess
        if (princessB.isPresent() && princessR.isPresent()) {
            boolean isBlueCarried = princessB.get().isCarried();
            boolean isRedCarried = princessR.get().isCarried();

            boolean isInRedBase = getCellByPosition(agent_position).getZoneType() == Zone.RBASE;
            boolean isInBlueBase = getCellByPosition(agent_position).getZoneType() == Zone.BBASE;

            boolean isBlueOutsideBlueBase = this.getCellByPosition(princessB.get().getPose().getPosition()).getZoneType() != Zone.BBASE;
            boolean isRedOutsideRedBase = this.getCellByPosition(princessR.get().getPose().getPosition()).getZoneType() != Zone.RBASE;

            boolean isBlueOutsideRedBase = this.getCellByPosition(princessB.get().getPose().getPosition()).getZoneType() != Zone.RBASE;
            boolean isRedOutsideBlueBase = this.getCellByPosition(princessR.get().getPose().getPosition()).getZoneType() != Zone.BBASE;

            boolean isTeamBlue = !agent.getTeam();
            boolean isTeamRed = agent.getTeam();

            // AGENT CARRING PRINCESS
            // Agent is carrying ally princess
            if (agent.getCarriedItem() instanceof Princess && agent.getCarriedItem().getTeam() == agent.getTeam()) {
                return this.fallbackPlanRescueAllyPrincess(agent);
            }
            // Agent is carrying enemy princess
            if (agent.getCarriedItem() instanceof Princess && agent.getCarriedItem().getTeam() != agent.getTeam()) {
                return this.fallbackPlanCaptureEnemyPrincess(agent);
            }


            // CAPTURE ENEMY PRINCESS
            if ((isTeamRed && isBlueOutsideRedBase) || (isTeamBlue && isRedOutsideBlueBase)) {
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() != agent.getTeam(),
                        "capture_enemy_princess");
            }

            // RESCUE ALLY PRINCESS
            // Ally princess is being carried by teammate and the agent is in enemies base
            // We avoid to check if agent is in enemy base and enemy princess as well because it would mean the game is over
            if ((isBlueCarried && isTeamBlue && isInRedBase)) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_r"), "rescue_ally_princess");
            } else if ((isRedCarried && isTeamRed && isInBlueBase)) {
                return findClosestStructure(agent, Empty.class,
                        empty -> ((Empty) empty).getType().equals("base_b"), "rescue_ally_princess");
            }

            // Ally princess is outside enemy base, either carried or dropped, so stay behind the princess
            if ((isTeamBlue && isOnRightSidePB && isBlueOutsideRedBase) || (isTeamRed && isOnLeftSidePR && isRedOutsideBlueBase)) {
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(),
                        "rescue_ally_princess");
            }

        }

        return null;
    }

    private synchronized Pair<String, Vector2D> handleGatesScenarios(Agent agent) {
        Optional<Gate> gate_b1 = this.getGateByName("gate_b1");
        Optional<Gate> gate_b2 = this.getGateByName("gate_b2");
        Optional<Gate> gate_r1 = this.getGateByName("gate_r1");
        Optional<Gate> gate_r2 = this.getGateByName("gate_r2");

        if (gate_b1.isPresent() && gate_b2.isPresent() && gate_r1.isPresent() && gate_r2.isPresent()
                && !(agent.getCarriedItem() instanceof Princess)) {

            boolean isGateB1Destroyed = gate_b1.get().isDestroyed();
            boolean isGateB2Destroyed = gate_b2.get().isDestroyed();
            boolean isGateR1Destroyed = gate_r1.get().isDestroyed();
            boolean isGateR2Destroyed = gate_r2.get().isDestroyed();
            boolean isTeamBlue = !agent.getTeam();
            boolean isTeamRed = agent.getTeam();

            if (isTeamBlue) {
                synchronized (this.getWoodAmountBlue()) {
                    if (this.getWoodAmountBlue().get() >= this.enoughWoodAmount) {
                        if (isGateB1Destroyed) {
                            Vector2D gate_b1_pos = gate_b1.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_b1_pos.getX() + 1, gate_b1_pos.getY() - 1));
                        } else if (isGateB2Destroyed) {
                            Vector2D gate_b2_pos = gate_b2.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_b2_pos.getX() + 1, gate_b2_pos.getY() + 1));
                        }
                    }
                }
            }

            if (isTeamRed) {
                synchronized (this.getWoodAmountRed()) {
                    if (this.getWoodAmountRed().get() >= this.enoughWoodAmount) {
                        if (isGateR1Destroyed) {
                            Vector2D gate_r1_pos = gate_r1.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_r1_pos.getX() - 1, gate_r1_pos.getY() - 1));
                        } else if (isGateR2Destroyed) {
                            Vector2D gate_r2_pos = gate_r2.get().getPose().getPosition();
                            return new Pair<>("repair_destroyed_gate", new Vector2D(gate_r2_pos.getX() - 1, gate_r2_pos.getY() + 1));
                        }
                    }
                }
            }
        }

        return null;
    }


    public synchronized Pair<String, Vector2D> getClosestObjectiveSoldier(Agent agent) {

        if (this.win != null) {
            if (agent.getTeam() == this.win) {
                return new Pair<>("my_team_won", agent.getPose().getPosition());
            } else {
                return new Pair<>("my_team_lost", agent.getPose().getPosition());
            }
        }

        Pair<String, Vector2D> princessScenario = handlePrincessScenarios(agent);
        if (princessScenario != null) {
            return princessScenario;
        }

        switch (agent.getState()) {
            case "spawn":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "exit_from_ally_base");

            case "exit_from_ally_base":
                if (!agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_b"), "choose_path");
                } else if (agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_r"), "choose_path");
                }

            case "choose_path":
                if (RAND.nextDouble() < agent.getLandProbability()) {
                    return new Pair("towards_land_passage", agent.getPose().getPosition());
                } else {
                    return new Pair("towards_bridge", agent.getPose().getPosition());
                }

            case "towards_land_passage":
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached");

            case "towards_bridge":
                Cell cell = this.getCellByPosition(agent.getPose().getPosition());
                boolean isOnBridge = cell.getStructure() != null && (cell.getStructure() instanceof Bridge);

                if (isOnBridge) {
                    return new Pair<>("bridge_reached", agent.getPose().getPosition());
                }
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached");

            case "land_passage_reached", "bridge_reached":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() != agent.getTeam(), "enemy_gate_reached");

            case "enemy_gate_reached":
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(), "ally_princess_reached");

            case "rescue_ally_princess":
                if (this.win == null) {
                    //I was an agent following my princess while being rescued and the princess fell of one of my teammates

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                }

            case "capture_enemy_princess":
                if (this.win == null) {
                    //I was an agent following enemy princess while being rescued and the princess fell of one of my enemies

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following enemy princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                }

            default:
                return null;
//                return this.fallbackPlan(agent);

        }
    }

    public synchronized Pair<String, Vector2D> getClosestObjectiveGatherer(Agent agent) {

        if (this.win != null) {
            if (agent.getTeam() == this.win) {
                return new Pair<>("my_team_won", agent.getPose().getPosition());
            } else {
                return new Pair<>("my_team_lost", agent.getPose().getPosition());
            }
        }

        Pair<String, Vector2D> princessScenario = handlePrincessScenarios(agent);
        if (princessScenario != null) {
            return princessScenario;
        }

        Pair<String, Vector2D> gatesScenario = handleGatesScenarios(agent);
        if (gatesScenario != null) {
            return gatesScenario;
        }

        switch (agent.getState()) {
            case "spawn", "repairing_gate":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() == agent.getTeam(), "exit_from_ally_base");

            case "exit_from_ally_base":
                if (!agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_b"), "gather_wood");
                } else if (agent.getTeam()) {
                    return findClosestStructure(agent, Empty.class,
                            empty -> ((Empty) empty).getType().equals("base_r"), "gather_wood");
                }

            case "gather_wood":

                if (agent.getTeam()) {
                    synchronized (this.getWoodAmountBlue()) {
                            if (this.getWoodAmountRed().get() >= (this.enoughWoodAmount * 2)) { // Else if agent is red and there is enough wood
                                return new Pair("choose_path", agent.getPose().getPosition());
                            }
                    }
                }

                if (!agent.getTeam()) {
                    synchronized (this.getWoodAmountRed()) { // Maybe check if the princess is carried?
                        if (!agent.getTeam() && this.getWoodAmountBlue().get() >= (this.enoughWoodAmount * 2)) { // If agent is blue and there is enough wood
                            return new Pair("choose_path", agent.getPose().getPosition());
                        }
                    }
                }

                return findClosestStructure(agent, Tree.class, null, "tree_reached");

            case "tree_reached":
                return new Pair("gather_wood", agent.getPose().getPosition());

            case "repair_destroyed_gate":
                return new Pair("gather_wood", agent.getPose().getPosition());

            case "choose_path":
                if (RAND.nextDouble() < agent.getLandProbability()) {
                    return new Pair("towards_land_passage", agent.getPose().getPosition());
                } else {
                    return new Pair("towards_bridge", agent.getPose().getPosition());
                }

            case "towards_land_passage":
                return findClosestStructure(agent, Empty.class, empty -> ((Empty) empty).getType() == "half", "land_passage_reached");

            case "towards_bridge":
                Cell cell = this.getCellByPosition(agent.getPose().getPosition());
                boolean isOnBridge = cell.getStructure() != null && (cell.getStructure() instanceof Bridge);

                if (isOnBridge) {
                    return new Pair<>("bridge_reached", agent.getPose().getPosition());
                }
                return findClosestStructure(agent, Bridge.class, null, "bridge_reached");

            case "land_passage_reached", "bridge_reached":
                return findClosestStructure(agent, Gate.class,
                        gate -> ((Gate) gate).getTeam() != agent.getTeam(), "enemy_gate_reached");

            case "enemy_gate_reached":
                return findClosestResource(agent, Princess.class,
                        princess -> ((Princess) princess).getTeam() == agent.getTeam(), "ally_princess_reached");

            case "rescue_ally_princess":
                if (this.win == null) {
                    //I was an agent following my princess while being rescued and the princess fell of one of my teammates

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                }

            case "capture_enemy_princess":
                if (this.win == null) {
                    //I was an agent following enemy princess while being rescued and the princess fell of one of my enemies

                    return fallbackPlanGeneral(agent);

                } else if ((!this.win && agent.getTeam()) || (this.win && !agent.getTeam())) {
                    //I was an agent following enemy princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_lost", agent.getPose().getPosition());
                } else if ((!this.win && !agent.getTeam()) || (this.win && agent.getTeam())) {
                    //I was an agent following my princess while being rescued and the princess arrived at its base

                    // L'agente va eliminato / stoppato / fermato / cancellato
                    return new Pair<>("my_team_won", agent.getPose().getPosition());
                }

            default:
                return null;
//                return this.fallbackPlan(agent);
            }
    }

    public synchronized Pair<String, Vector2D> getClosestObjective(Agent agent) {
//        synchronized (this.map) {
//            synchronized (this.agentsList) {
//                synchronized (this.structuresList) {
//                    synchronized (this.resourcesList) {
//                        synchronized (this.win) {
                            if (!(agent instanceof Gatherer)) {
                                return getClosestObjectiveSoldier(agent);
                            } else if (agent instanceof Gatherer) {
                                return getClosestObjectiveGatherer(agent);
                            }
//                        }
//                    }
//                }
//            }
//        }
        return null; // Return null if the agent type is not recognized
    }

    public synchronized boolean containsAgent(Agent agent) {
        synchronized (this.agentsList) {
            return this.agentsList.containsKey(agent.getName());
        }
    }

    public void ensureAgentExists(Agent agent) {
        if (!containsAgent(agent)) {
            throw new IllegalArgumentException("No such agent: " + agent.getName());
        }
    }

    public synchronized Vector2D getAgentPosition(Agent agent) {
        synchronized (this.agentsList) {
            this.ensureAgentExists(agent);
            return this.agentsList.get(agent.getName()).getPose().getPosition();
        }
    }

    public synchronized Orientation getAgentDirection(Agent agent) {
        synchronized (this.agentsList) {
            this.ensureAgentExists(agent);
            return this.agentsList.get(agent.getName()).getPose().getOrientation();
        }
    }

    public synchronized Optional<Agent> getAgentByPosition(Vector2D position) {
        synchronized (this.agentsList) {
            return this.agentsList.values().stream()
                    .filter(agent -> agent.getPose().getPosition().equals(position))
                    .findFirst();
        }
    }

    public synchronized Optional<Agent> getAgentByName(String agName) {
        synchronized (this.agentsList) {
            return this.agentsList.values().stream()
                    .filter(entry -> agName.equals(entry.getName()))
                    .findFirst();
        }
    }

    public boolean areAgentsNeighbours(Agent agent, Agent neighbour, int range) {
        if (!containsAgent(agent) || !containsAgent(neighbour)) {
            return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
        }

        if ((agent instanceof Priest) & agent.getTeam() == neighbour.getTeam() & neighbour.getHp() == neighbour.getMaxHp()) {
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
                .filter(it -> it.getHp() > 0)
                .filter(it -> !it.equals(agent))
                .filter(other -> this.areAgentsNeighbours(agent, other, range))
                .collect(Collectors.toSet());
    }

    public synchronized Set<Agent> getAllAgents() {
        synchronized (this.agentsList) {
            return this.agentsList.values()
                    .stream()
                    .collect(Collectors.toSet());
        }
    }

    public synchronized boolean containsStructure(MapStructure structure) {
        synchronized (this.structuresList) {
            return this.structuresList.containsKey(structure.getName());
        }
    }

    private boolean isStructureInRange(Agent agent, MapStructure structure, int range) {
        if (!containsAgent(agent) || !containsStructure(structure)) {
            return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
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

    public synchronized Optional<Gate> getGateByName(String gName) {
        synchronized (this.structuresList) {
            return this.getAllStructures(Gate.class).stream()
                    .filter(entry -> gName.equals(entry.getName()))
                    .map(gate -> (Gate) gate) // Cast to Gate
                    .findFirst();
        }
    }

    public synchronized Optional<Tree> getTreeByName(String tName) {
        synchronized (this.structuresList) {
            return this.getAllStructures(Tree.class).stream()
                    .filter(entry -> tName.equals(entry.getName()))
                    .map(tree -> (Tree) tree) // Cast to Tree
                    .findFirst();
        }
    }

    public Set<Gate> getGateNeighbours(Agent agent, String team, int range) {
        boolean isBlueTeam = !agent.getTeam(); // True if the agent is on the blue team
        boolean isRedTeam = agent.getTeam();   // True if the agent is on the red team
        boolean isEnemyTeam = team.equals("enemy"); // True if searching for enemy gates
        boolean isAllyTeam = team.equals("ally");   // True if searching for ally gates
        boolean hasEnoughWoodBlue = this.isEnoughWoodBlue(); // Blue team has enough wood
        boolean hasEnoughWoodRed = this.isEnoughWoodRed();   // Red team has enough wood
        boolean hasEnoughWood = (isBlueTeam && hasEnoughWoodBlue) || (isRedTeam && hasEnoughWoodRed); // Current team has enough wood

        if (isEnemyTeam) {
            return this.getAllStructures(Gate.class).stream()
                    .filter(gate -> !gate.getTeam().equals(agent.getTeam())) // Only enemy gates
                    .filter(gate -> this.isStructureInRange(agent, gate, range)) // Within range
                    .map(gate -> (Gate) gate) // Cast to Gate
                    .collect(Collectors.toSet());
        } else if (isAllyTeam && hasEnoughWood) {
            return this.getAllStructures(Gate.class).stream()
                    .filter(gate -> gate.getTeam().equals(agent.getTeam())) // Only ally gates
//                    .filter(gate -> this.getCellByPosition(gate.getPose().getPosition()).isOccupied(agent, null))
                    .filter(gate -> this.isStructureInRange(agent, gate, range)) // Within range
                    .map(gate -> (Gate) gate) // Cast to Gate
                    .collect(Collectors.toSet());
        }

        return this.getAllStructures(Gate.class).stream()
                .filter(gate -> this.isStructureInRange(agent, gate, range)) // Within range
                .map(gate -> (Gate) gate) // Cast to Gate
                .collect(Collectors.toSet());
    }

    public Set<Tree> getTreeNeighbours(Agent agent, int range) {
        boolean isBlueTeam = !agent.getTeam(); // True if the agent is on the blue team
        boolean isRedTeam = agent.getTeam();   // True if the agent is on the red team

        // Gate destruction status
        boolean isBlueGateDestroyed = this.getGateByName("gate_b1").get().isDestroyed() ||
                this.getGateByName("gate_b2").get().isDestroyed();
        boolean isRedGateDestroyed = this.getGateByName("gate_r1").get().isDestroyed() ||
                this.getGateByName("gate_r2").get().isDestroyed();
        boolean isAnyGateDestroyed = isBlueGateDestroyed || isRedGateDestroyed;

        // Princess carrying status
        boolean isBluePrincessCarried = this.getPrincessByName("princess_b").get().isCarried();
        boolean isRedPrincessCarried = this.getPrincessByName("princess_r").get().isCarried();
        boolean isAnyPrincessCarried = isBluePrincessCarried || isRedPrincessCarried;

        // Wood availability
        boolean hasEnoughWoodBlue = this.isEnoughWoodBlue();
        boolean hasEnoughWoodRed = this.isEnoughWoodRed();
        boolean hasDoubleWoodBlue = this.getWoodAmountBlue().get() >= 2 * this.enoughWoodAmount;
        boolean hasDoubleWoodRed = this.getWoodAmountRed().get() >= 2 * this.enoughWoodAmount;

        // Conditions to avoid trees
        boolean shouldAvoidTreesForGateRepair = (isBlueTeam && hasEnoughWoodBlue && isBlueGateDestroyed) ||
                (isRedTeam && hasEnoughWoodRed && isRedGateDestroyed);
        boolean shouldAvoidTreesForPrincess = isAnyPrincessCarried;
        boolean shouldAvoidTreesForExcessWood = (isBlueTeam && hasDoubleWoodBlue) || (isRedTeam && hasDoubleWoodRed);

        if (shouldAvoidTreesForGateRepair || shouldAvoidTreesForPrincess || shouldAvoidTreesForExcessWood) {
            return Collections.emptySet();
        }

        return this.getAllStructures(Tree.class).stream()
                .filter(tree -> this.isStructureInRange(agent, tree, range))
                .map(tree -> (Tree) tree) // Cast to Tree
                .collect(Collectors.toSet());
    }

    public Map<Direction, Vector2D> getAgentSurroundingPositions(Agent agent) {
        Vector2D pos = this.getAgentPosition(agent);
        Orientation dir = this.getAgentDirection(agent);
        return Stream.of(Direction.values()).collect(Collectors.toMap(
                k -> k,
                v -> pos.afterStep(1, dir.rotate(v))
        ));
    }

    public synchronized Set<MapStructure> getAllStructures(Class<? extends MapStructure> structureClass) {
        synchronized (this.structuresList) {
            //System.out.println(this.structuresList.values());
            return this.structuresList.values()
                    .stream()
                    .filter(structure -> structureClass.isInstance(structure))
                    .collect(Collectors.toSet());
        }
    }

    public synchronized Set<Resource> getAllResources(Class<? extends Resource> resourceClass) {
        synchronized (this.resourcesList) {
            //System.out.println(this.resourcesList.values());
            return this.resourcesList.values()
                    .stream()
                    .filter(resource -> resourceClass.isInstance(resource))
                    .collect(Collectors.toSet());
        }
    }

    public synchronized Optional<Princess> getPrincessByName(String pName) {
        synchronized (this.resourcesList) {
            return this.getAllResources(Princess.class).stream()
                    .filter(entry -> pName.equals(entry.getName()))
                    .map(princess -> (Princess) princess) // Cast to Princess
                    .findFirst();
        }
    }

    public Set<Princess> getPrincessNeighbours(Agent agent, String team, int range) {
        boolean isEnemyTeam = team.equals("enemy"); // True if searching for enemy princess
        boolean isAllyTeam = team.equals("ally");   // True if searching for ally princess

        if (isAllyTeam) {
            return this.getAllResources(Princess.class).stream()
                    .filter(princess -> princess.getTeam() == agent.getTeam())
                    .filter(princess -> this.isResourceInRange(agent, princess, range)) // Within range
                    .map(princess -> (Princess) princess)
                    .collect(Collectors.toSet());
        } else if (isEnemyTeam) {
            return this.getAllResources(Princess.class).stream()
                    .filter(princess -> princess.getTeam() != agent.getTeam())
                    .filter(princess -> this.isResourceInRange(agent, princess, range)) // Within range
                    .map(princess -> (Princess) princess)
                    .collect(Collectors.toSet());
        }

        return this.getAllResources(Princess.class).stream()
                .filter(princess -> this.isResourceInRange(agent, princess, range)) // Within range
                .map(princess -> (Princess) princess)
                .collect(Collectors.toSet());

    }

    private boolean isResourceInRange(Agent agent, Resource resource, int range) {
        if (!containsAgent(agent) || !containsResource(resource) || resource.isCarried()) {
            return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
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

    public synchronized boolean containsResource(Resource resource) {
        synchronized (this.resourcesList) {
            return this.resourcesList.containsKey(resource.getName());
        }
    }

    public synchronized boolean pickUpPrincess(Agent agent, Princess target) {
        synchronized (this.map) {
            synchronized (agentsList) {
                synchronized (resourcesList) {
                    mapLock.writeLock().lock();
                    try {
                        if (!target.isCarried()) {
                            Vector2D p_pos = target.getPose().getPosition();
                            agent.startCarrying(target);
                            target.setPose(agent.getPose());
                            System.out.println("PRINCESS PRIMA" + p_pos);
                            this.getCellByPosition(p_pos).clearResource();
                            System.out.println("AGENT PRIMA" + agent.getPose().getPosition());
                            this.getCellByPosition(agent.getPose().getPosition()).setResource(target);

                            this.agentsList.put(agent.getName(), agent);
                            this.resourcesList.put(target.getName(), target);
                            System.out.println(this.resourcesList);
                            return true;
                        } else {
                            return false; //TUTTI QUESTI RETURN FALSE CHE STO AGGIUNGENDO PORTANO A STAMPARE "CONDITION FAILED" RICORDA
                        }
                    } finally {
                        mapLock.writeLock().unlock();
                    }
                }
            }
        }
    }

    private void endGame(boolean redTeamWins) {
        String winningTeam = redTeamWins ? "Red Team" : "Blue Team";
        System.out.println("Game Over! " + winningTeam + " wins!");

        // Show result popup after the game ends
        SwingUtilities.invokeLater(() -> ConfigWindow.showGameResult(winningTeam));
    }


    public void setView(MapView view) {
        this.view = view;
    }
}
