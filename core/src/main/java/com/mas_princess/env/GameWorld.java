package com.mas_princess.env;

import java.util.*;
import com.mas_princess.env.structures.*;
import com.mas_princess.env.resources.*;

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
        switch (zone) {
            case BASE1:
                return '1';
            case BASE2:
                return '2';
            case RIVER:
                return 'R';
            case BATTLEFIELD:
                return 'F';
            default:
                return ' ';
        }
    }
    
    private char getStructureSymbol(Structure structure) {
        if (structure instanceof Gate) {
            return 'G';
        } else if (structure instanceof Wall) {
            return 'W';
        } else if (structure instanceof Bridge) {
            return 'B';
        } else {
            return ' ';
        }
    }
    
    public int getGridWidth() {
        return GRID_WIDTH;
    }
    
    public int getGridHeight() {
        return GRID_HEIGHT;
    }
    
    public Cell[][] getMap() {
        return map;
    }
    
}
