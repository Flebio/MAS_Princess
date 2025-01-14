package com.mas_princess;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mas_princess.env.maps.BlackForest;
import com.mas_princess.render.MapRenderer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. 
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
*/



public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private BlackForest blackForest;
    private MapRenderer mapRenderer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        blackForest = new BlackForest(60, 20); // Adjust grid size as needed
        blackForest.initialize();
        mapRenderer = new MapRenderer(blackForest);
    }

    @Override
    public void render() {
        mapRenderer.render(batch);
    }

    @Override
    public void dispose() {
        batch.dispose();
        mapRenderer.dispose();
    }
    
}