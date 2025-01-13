package mas_princess.env;

import java.util.*;
import mas_princess.env.structures.*;

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

}













