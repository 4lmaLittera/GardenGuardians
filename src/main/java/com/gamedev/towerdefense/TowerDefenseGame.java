package com.gamedev.towerdefense;



import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gamedev.towerdefense.config.GameConfig;


public class TowerDefenseGame extends ApplicationAdapter {

    // Constants
    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;
    public static final float UI_MARGIN = 10f;
    public static final float TOWER_LIST_START_Y = WORLD_HEIGHT - 100f;
    public static final float TOWER_LIST_SPACING = 20f;
    public static final float DEFAULT_PROJECTILE_SPEED = 300f;
    public static final float DEFAULT_COIN_SPEED = 200f;

    // Rendering components
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private GameAssetLoader assetLoader;

    private Rectangle damageTextBounds;
    private Rectangle rangeTextBounds;
    private Rectangle cooldownTextBounds;
    private Rectangle strategyTextBounds;

    // Game configuration and managers
    private GameConfig gameConfig;
    // Game World
    private GameWorld gameWorld;

    private GameRenderer renderer;
    private GameInputHandler inputHandler;

    @Override
    public void create() {
        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();

            assetLoader = new GameAssetLoader();
            assetLoader.loadAll();

            camera = new OrthographicCamera();
            camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
            viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

            try {
                gameConfig = GameConfig.load("game-config.json");
                if (gameConfig == null) {
                    throw new RuntimeException("Game config is null");
                }
            } catch (RuntimeException e) {
                System.err.println("Failed to load game configuration: " + e.getMessage());
                throw new RuntimeException("Cannot start game without configuration", e);
            }

            try {
                if (gameConfig.getWorldWidth() > 0 && gameConfig.getWorldHeight() > 0) {
                    camera.setToOrtho(false, gameConfig.getWorldWidth(), gameConfig.getWorldHeight());
                    viewport = new FitViewport(gameConfig.getWorldWidth(), gameConfig.getWorldHeight(), camera);
                }
            } catch (RuntimeException e) {
                System.err.println("Failed to setup camera: " + e.getMessage());
            }

            gameWorld = new GameWorld(gameConfig);
            renderer = new GameRenderer(this);
            inputHandler = new GameInputHandler(this);
        } catch (RuntimeException e) {
            System.err.println("Critical error during game initialization: " + e.getMessage());
            throw e;
        }

        damageTextBounds = new Rectangle();
        rangeTextBounds = new Rectangle();
        cooldownTextBounds = new Rectangle();
        strategyTextBounds = new Rectangle();
    }

    @Override
    public void render() {
        if (gameConfig.getVisual() != null && gameConfig.getVisual().getBackgroundColor() != null) {
            GameConfig.ColorConfig bgColor = gameConfig.getVisual().getBackgroundColor();
            Gdx.gl.glClearColor(bgColor.getR(), bgColor.getG(), bgColor.getB(), bgColor.getA());
        } else {
            Gdx.gl.glClearColor(0.1f, 0.15f, 0.2f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (assetLoader.getBackgroundTexture() != null) {
            float worldWidth = gameConfig != null && gameConfig.getWorldWidth() > 0
                    ? gameConfig.getWorldWidth()
                    : WORLD_WIDTH;
            float worldHeight = gameConfig != null && gameConfig.getWorldHeight() > 0
                    ? gameConfig.getWorldHeight()
                    : WORLD_HEIGHT;
            batch.draw(assetLoader.getBackgroundTexture(), 0, 0, worldWidth, worldHeight);
        }
        batch.end();

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (gameWorld != null) {
            gameWorld.update(deltaTime);
        }

        inputHandler.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        if (renderer != null) {
            renderer.renderAll();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        try {
            if (batch != null) {
                batch.dispose();
            }
        } catch (Exception e) {
            System.err.println("Error disposing SpriteBatch: " + e.getMessage());
        }

        try {
            if (shapeRenderer != null) {
                shapeRenderer.dispose();
            }
        } catch (Exception e) {
            System.err.println("Error disposing ShapeRenderer: " + e.getMessage());
        }

        try {
            if (assetLoader != null) {
                assetLoader.dispose();
            }
        } catch (Exception e) {
            System.err.println("Error disposing assets: " + e.getMessage());
        }
    }

    // --- Accessors used by GameRenderer ---
    public com.badlogic.gdx.graphics.g2d.SpriteBatch getBatch() {
        return batch;
    }

    public com.badlogic.gdx.graphics.glutils.ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public com.badlogic.gdx.graphics.g2d.BitmapFont getFont() {
        return assetLoader.getFont();
    }

    public com.badlogic.gdx.graphics.OrthographicCamera getCamera() {
        return camera;
    }

    public com.badlogic.gdx.utils.viewport.Viewport getViewport() {
        return viewport;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public com.gamedev.towerdefense.util.AnimationManager getEnemyAnimation() {
        return assetLoader.getEnemyAnimation();
    }

    public com.badlogic.gdx.graphics.Texture getCoinTexture() {
        return assetLoader.getCoinTexture();
    }

    public com.badlogic.gdx.graphics.Texture getBackgroundTexture() {
        return assetLoader.getBackgroundTexture();
    }
    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public com.badlogic.gdx.math.Rectangle getDamageTextBounds() {
        return damageTextBounds;
    }

    public com.badlogic.gdx.math.Rectangle getRangeTextBounds() {
        return rangeTextBounds;
    }

    public com.badlogic.gdx.math.Rectangle getCooldownTextBounds() {
        return cooldownTextBounds;
    }

    public com.badlogic.gdx.math.Rectangle getStrategyTextBounds() {
        return strategyTextBounds;
    }
}
