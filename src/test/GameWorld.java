import java.util.*;

public class GameWorld {

    private final int GRID_WIDTH = 60; // Map width
    private final int GRID_HEIGHT = 20; // Map height
    private Cell[][] map = new Cell[GRID_WIDTH][GRID_HEIGHT]; // 2D grid for the map

    // Constructor
    public GameWorld() {
    }

    // Initialize the game world
    public void initialize() {
        initializeMap();
    }

    private void initializeMap() {
        // Define base zones
        int baseWidth = GRID_WIDTH / 12; // Base width as a fraction of GRID_WIDTH
        int baseHeight = GRID_HEIGHT / 4; // Base height as a fraction of GRID_HEIGHT
    
        for (int x = 0; x < baseWidth; x++) {
            for (int y = baseHeight; y < GRID_HEIGHT - baseHeight; y++) {
                map[x][y] = new Cell(Zone.BASE1);
                map[GRID_WIDTH - x - 1][GRID_HEIGHT - y - 1] = new Cell(Zone.BASE2);
            }
        }
        

        // Define river (Y-shaped with extended diagonals and widened to 3 cells)
        int centerX = GRID_WIDTH / 2; // Center X-coordinate
        int centerY = GRID_HEIGHT / 2; // Center Y-coordinate
    
        // Vertical stem of the Y, broadened to 3 cells wide
        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY; y < GRID_HEIGHT; y++) {
                map[x][y] = new Cell(Zone.RIVER);
            }
        }
    
        // Left diagonal of the Y, extended to the top
        for (int x = centerX - 1; x >= 0; x--) {
            int yStart = centerY - (centerX - x); // Calculate starting Y for this X
            if (yStart < 0) break; // Stop when reaching the top
            for (int offset = -1; offset <= 1; offset++) { // Broaden the diagonal
                if (yStart + offset >= 0 && x + offset >= 0 && x + offset < GRID_WIDTH) {
                    map[x + offset][yStart] = new Cell(Zone.RIVER);
                }
            }
        }
    
        // Right diagonal of the Y, extended to the top
        for (int x = centerX + 1; x < GRID_WIDTH; x++) {
            int yStart = centerY - (x - centerX); // Calculate starting Y for this X
            if (yStart < 0) break; // Stop when reaching the top
            for (int offset = -1; offset <= 1; offset++) { // Broaden the diagonal
                if (yStart + offset >= 0 && x + offset >= 0 && x + offset < GRID_WIDTH) {
                    map[x + offset][yStart] = new Cell(Zone.RIVER);
                }
            }
        }
    
        // Modify river cells to battlefield at 1/3 height
        int crossableY1 = (GRID_HEIGHT / 3) - 1; // First row to modify
        int crossableY2 = crossableY1 - 1;       // Second row to modify
    
        for (int x = 0; x < GRID_WIDTH; x++) {
            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.RIVER) {
                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD);
            }
            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.RIVER) {
                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD);
            }
        }
    
        // Fill remaining cells as battlefield
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD);
                }
            }
        }

    // Add walls and gates around Base1
    for (int x = 0; x < baseWidth + 1; x++) {
        for (int y = baseHeight - 1; y < GRID_HEIGHT - baseHeight + 1; y++) {
            if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                if (y == GRID_HEIGHT / 2 - 1) { // Top cell of the gate
                    Gate gate = new Gate(100, 1); // Create a new Gate instance
                    map[x][y].setStructure(gate); // Assign to top cell
                    map[x][y + 1].setStructure(gate); // Assign the same instance to bottom cell
                } else if (y!=GRID_HEIGHT / 2) {
                    map[x][y].setStructure(new Wall());
                }
            }
        }
    }

    // Add walls and gates around Base2
    for (int x = GRID_WIDTH - baseWidth - 1; x < GRID_WIDTH; x++) {
        for (int y = baseHeight - 1; y < GRID_HEIGHT - baseHeight + 1; y++) {
            if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                if (y == GRID_HEIGHT / 2 - 1) { // Top cell of the gate
                    Gate gate = new Gate(100, 2); // Create a new Gate instance
                    map[x][y].setStructure(gate); // Assign to top cell
                    map[x][y + 1].setStructure(gate); // Assign the same instance to bottom cell
                } else if (y!=GRID_HEIGHT / 2) {
                        map[x][y].setStructure(new Wall());
                }
            }
        }
    }

    // Add a bridge over the river at 2/3 of the map's height
    int bridgeY = (2 * GRID_HEIGHT) / 3 +1; // Y-coordinate for the bridge's top row
    int bridgeXStart = centerX - 1; // Start of the bridge (3 cells wide)
    int bridgeXEnd = centerX + 1;

    Bridge bridge = new Bridge(0.1, 10); // Bridge with 10% break probability and 10 ticks respawn time

    for (int x = bridgeXStart; x <= bridgeXEnd; x++) {
        for (int y = bridgeY; y < bridgeY + 2; y++) { // Bridge is 2 cells tall
            map[x][y].setStructure(bridge);
        }
    }


}



public void printMap() {
    // Debug: Print gate positions
    for (int x = 0; x < GRID_WIDTH; x++) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            if (map[x][y].getStructure() instanceof Gate) {
                System.out.println("Gate at: (" + x + ", " + y + ")");
            }
        }
    }

    // Print map
    for (int y = 0; y < GRID_HEIGHT; y++) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            Cell cell = map[x][y];
            if (cell.getStructure() != null) {
                Structure structure = cell.getStructure();
                if (structure instanceof Gate) {
                    System.out.print("G "); // Print G for gate
                } else if (structure instanceof Wall) {
                    System.out.print("W "); // Print W for wall
                } else if (structure instanceof Bridge) {
                    System.out.print("B "); // Print W for wall
                }
            } else {
                Zone zone = cell.getZoneType();
                char zoneChar = switch (zone) {
                    case BASE1 -> '1';
                    case BASE2 -> '2';
                    case RIVER -> 'R';
                    case BATTLEFIELD -> 'F';
                    default -> ' ';
                };
                System.out.print(zoneChar + " ");
            }
        }
        System.out.println();
    }
}




// Main method
public static void main(String[] args) {
        GameWorld gameWorld = new GameWorld(); // Create a GameWorld instance
        gameWorld.initialize();               // Initialize the game world
        gameWorld.printMap();                 // Print the map layout
    }
}

// Supporting classes

enum Zone {
    BASE1,
    BASE2,
    RIVER,
    BATTLEFIELD
}

// Cell class
class Cell {
    private Zone zoneType;
    private Structure structure; 

    public Cell(Zone zoneType) {
        this.zoneType = zoneType;
        this.structure = null; // No structure by default
    }

    public Zone getZoneType() {
        return zoneType;
    }

    public void setZoneType(Zone zoneType) {
        this.zoneType = zoneType;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }
}

// Base Structure class
abstract class Structure {
    private final boolean breakable;
    private final boolean repairable;
    private final int width;
    private final int height;
    private final int maxLife;
    private int currentLife;

    public Structure(boolean breakable, boolean repairable, int width, int height, int maxLife) {
        this.breakable = breakable;
        this.repairable = repairable;
        this.width = width;
        this.height = height;
        this.maxLife = maxLife;
        this.currentLife = maxLife;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public boolean isRepairable() {
        return repairable;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public int getCurrentLife() {
        return currentLife;
    }

    public boolean isDestroyed() {
        return currentLife == 0;
    }

    public void takeDamage(int damage) {
        if (breakable && currentLife > 0) {
            currentLife = Math.max(currentLife - damage, 0);
        }
    }

    public void repair(int amount) {
        if (repairable && currentLife < maxLife) {
            currentLife = Math.min(currentLife + amount, maxLife);
        }
    }
}

// Gate structure
class Gate extends Structure {
    private final int teamOwnership;

    public Gate(int maxLife, int teamOwnership) {
        super(true, true, 1, 2, maxLife); // Gates are 1 cell wide, 2 cells tall, breakable, and repairable
        this.teamOwnership = teamOwnership;
    }

    public int getTeamOwnership() {
        return teamOwnership;
    }

    @Override
    public void takeDamage(int damage) {
        // Gates only take damage from the opposing team
        super.takeDamage(damage);
    }
}

// Wall structure
class Wall extends Structure {
    public Wall() {
        super(false, false, 1, 1, 0); // Walls are unbreakable, not repairable, and have no life points
    }
    @Override
    public void takeDamage(int damage) {
        // The walls cannot take damage, so this method does nothing
    }

    @Override
    public void repair(int amount) {
        // The walls cannot be repaired, so this method does nothing
    }
}

// Bridge structure
class Bridge extends Structure {
    private final double breakProbability;
    private final int respawnDuration;
    private int remainingRespawnTime;

    public Bridge(double breakProbability, int respawnDuration) {
        super(true, false, 3, 2, 1); // Bridges are breakable, not repairable, 3 cells wide, 2 cells tall, and have 1 life point
        this.breakProbability = breakProbability;
        this.respawnDuration = respawnDuration;
        this.remainingRespawnTime = 0;
    }

    public boolean isBroken() {
        return remainingRespawnTime > 0;
    }

    public void attemptBreak() {
        if (!isBroken() && Math.random() < breakProbability) {
            remainingRespawnTime = respawnDuration;
        }
    }

    public void updateRespawnTimer() {
        if (remainingRespawnTime > 0) {
            remainingRespawnTime--;
        }
    }

    public boolean isActive() {
        return remainingRespawnTime == 0;
    }
}

// Tree structure
class Tree extends Structure {
    private final int respawnDuration;
    private int remainingRespawnTime;

    public Tree(int maxLife, int respawnDuration) {
        super(true, false, 1, 1, maxLife); // Trees are breakable, not repairable, 1x1 cells
        this.respawnDuration = respawnDuration;
        this.remainingRespawnTime = 0;
    }

    public boolean isActive() {
        return remainingRespawnTime == 0;
    }

    @Override
    public void takeDamage(int damage) {
        if (!isDestroyed()) {
            super.takeDamage(damage);
            if (isDestroyed()) {
                remainingRespawnTime = respawnDuration;
            }
        }
    }

    public void updateRespawnTimer() {
        if (remainingRespawnTime > 0) {
            remainingRespawnTime--;
            if (remainingRespawnTime == 0) {
                repair(getMaxLife()); // Trees fully regenerate after respawn
            }
        }
    }
}

// Ladder structure
class Ladder extends Structure {
    public Ladder(int maxLife) {
        super(true, false, 1, 1, maxLife); // Ladders are breakable, not repairable, and 1x1 cells
    }

    public boolean canBePlacedNearWall(Cell[][] map, int x, int y) {
        // Check adjacent cells for a wall
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length) {
                Structure adjacentStructure = map[newX][newY].getStructure();
                if (adjacentStructure instanceof Wall) {
                    return true; // Ladder can be placed next to a wall
                }
            }
        }
        return false;
    }
}

class Princess extends Structure {
    private int weight; // Current weight of the princess
    private final int maxWeight; // Maximum weight the princess can achieve
    private final int minWeight; // Minimum weight before she stops losing weight
    private final int weightDecayRate; // How much weight she loses per timestep if not fed
    private int weightDecayTimer; // Counter for weight decay

    private boolean isCarried; // Whether the princess is currently being carried
    private int carryingTeam; // The team currently carrying the princess (if any)

    public Princess(int initialWeight, int maxWeight, int minWeight, int weightDecayRate) {
        super(false, false, 1, 1, 0); // Princess is not breakable, not repairable, and has no life points
        this.weight = initialWeight;
        this.maxWeight = maxWeight;
        this.minWeight = minWeight;
        this.weightDecayRate = weightDecayRate;
        this.weightDecayTimer = 0;
        this.isCarried = false;
        this.carryingTeam = -1; // No team is carrying her initially
    }

    public int getWeight() {
        return weight;
    }

    public boolean isCarried() {
        return isCarried;
    }

    public int getCarryingTeam() {
        return carryingTeam;
    }

    public void feed(int foodAmount) {
        if (!isCarried) { // Only feed if she's not being carried
            weight = Math.min(weight + foodAmount, maxWeight);
        }
    }

    public void pickUp(int team) {
        if (!isCarried) {
            isCarried = true;
            carryingTeam = team;
        }
    }

    public void drop() {
        isCarried = false;
        carryingTeam = -1;
    }

    public void updateWeightDecay() {
        if (!isCarried) { // Weight decays only if the princess is not carried
            weightDecayTimer++;
            if (weightDecayTimer >= weightDecayRate) {
                weight = Math.max(weight - 1, minWeight);
                weightDecayTimer = 0; // Reset the timer
            }
        }
    }

    @Override
    public void takeDamage(int damage) {
        // The princess cannot take damage, so this method does nothing
    }

    @Override
    public void repair(int amount) {
        // The princess cannot be repaired, so this method does nothing
    }
}

// Resource class
class Resource {
    private final String name;
    private final int quantity;
    private final int x;
    private final int y;

    public Resource(String name, int quantity, int x, int y) {
        this.name = name;
        this.quantity = quantity;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

