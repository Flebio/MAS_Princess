package mas_princess.env.maps;

import mas_princess.env.*;
import mas_princess.env.structures.*;

public class BlackForest extends GameWorld {

    // Constructor with configurable size
    public BlackForest(int width, int height) {
        super(width, height); // Pass dimensions to the GameWorld superclass
    }

    @Override
    protected void createZones() {
        createBaseZones();
        createRiver();
        createBattlefield();
    }

    @Override
    protected void addStructures() {
        addWallsAndGates();
        addBridge();
    }

    private void createBaseZones() {
        int baseWidth = GRID_WIDTH / 12;
        int baseHeight = GRID_HEIGHT / 4;

        for (int x = 0; x < baseWidth; x++) {
            for (int y = baseHeight; y < GRID_HEIGHT - baseHeight; y++) {
                map[x][y] = new Cell(Zone.BASE1);
                map[GRID_WIDTH - x - 1][GRID_HEIGHT - y - 1] = new Cell(Zone.BASE2);
            }
        }
    }

    private void createRiver() {
        int centerX = GRID_WIDTH / 2;
        int centerY = GRID_HEIGHT / 2;

        for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int y = centerY; y < GRID_HEIGHT; y++) {
                map[x][y] = new Cell(Zone.RIVER);
            }
        }

        createDiagonal(centerX, centerY, -1);
        createDiagonal(centerX, centerY, 1);
    }

    private void createDiagonal(int centerX, int centerY, int direction) {
        for (int x = centerX + direction; x >= 0 && x < GRID_WIDTH; x += direction) {
            int yStart = centerY - Math.abs(x - centerX);
            if (yStart < 0) break;
            for (int offset = -1; offset <= 1; offset++) {
                if (yStart + offset >= 0 && x + offset >= 0 && x + offset < GRID_WIDTH) {
                    map[x + offset][yStart] = new Cell(Zone.RIVER);
                }
            }
        }
    }

    private void createBattlefield() {
        int crossableY1 = (GRID_HEIGHT / 3) - 1;
        int crossableY2 = crossableY1 - 1;

        for (int x = 0; x < GRID_WIDTH; x++) {
            if (map[x][crossableY1] != null && map[x][crossableY1].getZoneType() == Zone.RIVER) {
                map[x][crossableY1] = new Cell(Zone.BATTLEFIELD);
            }
            if (map[x][crossableY2] != null && map[x][crossableY2].getZoneType() == Zone.RIVER) {
                map[x][crossableY2] = new Cell(Zone.BATTLEFIELD);
            }
        }

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Cell(Zone.BATTLEFIELD);
                }
            }
        }
    }

    private void addWallsAndGates() {
        int baseWidth = GRID_WIDTH / 12;
        int baseHeight = GRID_HEIGHT / 4;

        for (int x = 0; x < baseWidth + 1; x++) {
            for (int y = baseHeight - 1; y < GRID_HEIGHT - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == GRID_HEIGHT / 2 - 1) {
                        Gate gate = new Gate(100, 1);
                        map[x][y].setStructure(gate);
                        map[x][y + 1].setStructure(gate);
                    } else if (y != GRID_HEIGHT / 2) {
                        map[x][y].setStructure(new Wall());
                    }
                }
            }
        }

        for (int x = GRID_WIDTH - baseWidth - 1; x < GRID_WIDTH; x++) {
            for (int y = baseHeight - 1; y < GRID_HEIGHT - baseHeight + 1; y++) {
                if (map[x][y].getZoneType() == Zone.BATTLEFIELD) {
                    if (y == GRID_HEIGHT / 2 - 1) {
                        Gate gate = new Gate(100, 2);
                        map[x][y].setStructure(gate);
                        map[x][y + 1].setStructure(gate);
                    } else if (y != GRID_HEIGHT / 2) {
                        map[x][y].setStructure(new Wall());
                    }
                }
            }
        }
    }

    private void addBridge() {
        int bridgeY = (2 * GRID_HEIGHT) / 3 + 1;
        int bridgeXStart = GRID_WIDTH / 2 - 1;
        int bridgeXEnd = GRID_WIDTH / 2 + 1;

        Bridge bridge = new Bridge(0.1, 10);

        for (int x = bridgeXStart; x <= bridgeXEnd; x++) {
            for (int y = bridgeY; y < bridgeY + 2; y++) {
                map[x][y].setStructure(bridge);
            }
        }
    }
}
