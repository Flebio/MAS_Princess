package mas_princess.env;

import java.util.*;
import mas_princess.env.structures.*;
import mas_princess.env.resources.*;

public abstract class GameWorld {

    protected final int GRID_WIDTH;  // Map width
    protected final int GRID_HEIGHT; // Map height
    protected Cell[][] map;          // 2D grid for the map

    // Constructor
    public GameWorld(int width, int height) {
        this.GRID_WIDTH = width;
        this.GRID_HEIGHT = height;
        this.map = new Cell[GRID_WIDTH][GRID_HEIGHT];
    }

    // Initialize the game world (template method pattern)
    public void initialize() {
        createZones();
        addStructures();
    }

    // Abstract methods for customization
    protected abstract void createZones();
    protected abstract void addStructures();

    // Utility method to print the map
    public void printMap() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                Cell cell = map[x][y];
                if (cell.getStructure() != null) {
                    System.out.print(getStructureSymbol(cell.getStructure()) + " ");
                } else {
                    System.out.print(getZoneSymbol(cell.getZoneType()) + " ");
                }
            }
            System.out.println();
        }
    }

    private char getZoneSymbol(Zone zone) {
        return switch (zone) {
            case BASE1 -> '1';
            case BASE2 -> '2';
            case RIVER -> 'R';
            case BATTLEFIELD -> 'F';
            default -> ' ';
        };
    }

    private char getStructureSymbol(Structure structure) {
        if (structure instanceof Gate) return 'G';
        if (structure instanceof Wall) return 'W';
        if (structure instanceof Bridge) return 'B';
        return ' ';
    }
}
