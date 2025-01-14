package com.mas_princess.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mas_princess.env.*;
import com.mas_princess.env.structures.*;
import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MapRenderer {

    private final GameWorld gameWorld;
    private final Map<Zone, Texture> zoneTextures;
    private final Map<Class<? extends Structure>, Texture> structureTextures;
    private final OrthographicCamera camera; // Add camera
    private float cellSize = 20f; // Size of each cell in pixels

    public MapRenderer(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        this.zoneTextures = new HashMap<>();
        this.structureTextures = new HashMap<>();

        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, gameWorld.getGridWidth() * cellSize, gameWorld.getGridHeight() * cellSize);

        // Load zone textures
        zoneTextures.put(Zone.BASE1, new Texture("base1.png"));
        zoneTextures.put(Zone.BASE2, new Texture("base2.png"));
        zoneTextures.put(Zone.BATTLEFIELD, new Texture("battlefield.png"));
        zoneTextures.put(Zone.RIVER, new Texture("river.png"));

        // Load structure textures
        structureTextures.put(Gate.class, new Texture("gate.png"));
        structureTextures.put(Wall.class, new Texture("wall.png"));
        structureTextures.put(Bridge.class, new Texture("bridge.png"));
    }

    public void render(SpriteBatch batch) {
        // Update the camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        for (int y = 0; y < gameWorld.getGridHeight(); y++) {
            for (int x = 0; x < gameWorld.getGridWidth(); x++) {
                Cell cell = gameWorld.getMap()[x][y];
                if (cell != null) {
                    renderCell(cell, x, y, batch);
                }
            }
        }
        batch.end();
    }

    private void renderCell(Cell cell, int x, int y, SpriteBatch batch) {
        // Invert Y-axis
        float worldX = x * cellSize;
        float worldY = (gameWorld.getGridHeight() - y - 1) * cellSize;

        // Render zone texture
        Texture zoneTexture = zoneTextures.get(cell.getZoneType());
        if (zoneTexture != null) {
            batch.draw(zoneTexture, worldX, worldY, cellSize, cellSize);
        }

        // Render structure texture (if present)
        Structure structure = cell.getStructure();
        if (structure != null) {
            Texture structureTexture = structureTextures.get(structure.getClass());
            if (structureTexture != null) {
                batch.draw(structureTexture, worldX, worldY, cellSize, cellSize);
            }
        }
    }

    public void dispose() {
        // Dispose zone textures
        for (Texture texture : zoneTextures.values()) {
            texture.dispose();
        }

        // Dispose structure textures
        for (Texture texture : structureTextures.values()) {
            texture.dispose();
        }
    }
}
