package com.gamedev.towerdefense;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.BudgetManager;
import com.gamedev.towerdefense.model.CurvedPath;
import com.gamedev.towerdefense.model.Enemy;
import com.gamedev.towerdefense.model.EnemyFactory;
import com.gamedev.towerdefense.model.GameState;
import com.gamedev.towerdefense.model.MoneyCoin;
import com.gamedev.towerdefense.model.NearestEnemyStrategy;
import com.gamedev.towerdefense.model.Path;
import com.gamedev.towerdefense.model.Position;
import com.gamedev.towerdefense.model.Projectile;
import com.gamedev.towerdefense.model.StrongestEnemyStrategy;
import com.gamedev.towerdefense.model.TargetingStrategy;
import com.gamedev.towerdefense.model.Tower;
import com.gamedev.towerdefense.model.WaveManager;
import com.gamedev.towerdefense.model.WeakestEnemyStrategy;
import com.gamedev.towerdefense.util.AnimationManager;

public class TowerDefenseGame extends ApplicationAdapter {

    // Constants
    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;
    public static final float UI_MARGIN = 10f;
    public static final float TOWER_LIST_START_Y = WORLD_HEIGHT - 100f;
    public static final float TOWER_LIST_SPACING = 20f;
    public static final float DEFAULT_PROJECTILE_SPEED = 300f;
    public static final float DEFAULT_COIN_SPEED = 200f;

    // Enemy animation defaults
    public static final int ENEMY_FRAME_WIDTH = 32;
    public static final int ENEMY_FRAME_HEIGHT = 32;
    public static final int ENEMY_ANIM_COLS = 4;
    public static final int ENEMY_ANIM_ROWS = 4;
    public static final float ENEMY_FRAME_DURATION = 0.2f;

    // Rendering components
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Texture backgroundTexture;
    private Texture beetlTexture;
    private AnimationManager enemyAnimation;
    private Texture coinTexture;

    private Rectangle damageTextBounds;
    private Rectangle rangeTextBounds;
    private Rectangle cooldownTextBounds;
    private Rectangle strategyTextBounds;

    // Game configuration and managers
    private GameConfig gameConfig;
    private BudgetManager budgetManager;
    private WaveManager waveManager;
    private final EnemyFactory enemyFactory = new EnemyFactory();

    // Game state
    private int lives;
    private GameState gameState = GameState.PLAYING;
    private Path path;

    // Game entities
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<MoneyCoin> moneyCoins = new ArrayList<>();

    // Tower selection
    private GameConfig.TowerTypeConfig selectedTowerType;
    private Tower selectedTower;
    private GameRenderer renderer;

    @Override
    public void create() {
        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();

            try {
                backgroundTexture = new Texture(Gdx.files.internal("assets/images/grass_template2.jpg"));
            } catch (GdxRuntimeException e) {
                System.err.println("Failed to load background texture: " + e.getMessage());
                backgroundTexture = null;
            }

            try {
                beetlTexture = new Texture(Gdx.files.internal("assets/images/BeetleMove.png"));
            } catch (GdxRuntimeException e) {
                System.err.println("Failed to load enemy texture: " + e.getMessage());
                beetlTexture = null;
            }

            if (beetlTexture != null) {
                try {
                    int frameWidth = ENEMY_FRAME_WIDTH;
                    int frameHeight = ENEMY_FRAME_HEIGHT;
                    int cols = ENEMY_ANIM_COLS;
                    int rows = ENEMY_ANIM_ROWS;
                    float frameDuration = ENEMY_FRAME_DURATION;
                    enemyAnimation = new AnimationManager(beetlTexture, frameWidth, frameHeight, cols, rows,
                            frameDuration);
                } catch (GdxRuntimeException e) {
                    System.err.println("Failed to create enemy animation: " + e.getMessage());
                    enemyAnimation = null;
                }
            }

            try {
                coinTexture = new Texture(Gdx.files.internal("assets/images/coin.png"));
            } catch (GdxRuntimeException e) {
                System.err.println("Failed to load coin texture: " + e.getMessage());
                coinTexture = null;
            }

            camera = new OrthographicCamera();
            camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
            viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

            font = new BitmapFont();
            font.getData().setScale(1f, 1f);

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

            budgetManager = new BudgetManager(gameConfig.getInitialBudget());
            lives = gameConfig.getInitialLives();

            try {
                List<Position> waypoints = gameConfig.getPathWaypoints();
                if (waypoints == null || waypoints.isEmpty()) {
                    throw new RuntimeException("Path waypoints are missing or empty");
                }
                path = new CurvedPath(waypoints);
            } catch (RuntimeException e) {
                System.err.println("Failed to setup path: " + e.getMessage());
                throw new RuntimeException("Cannot start game without path", e);
            }

            try {
                if (gameConfig.getWaves() != null && !gameConfig.getWaves().isEmpty()) {
                    waveManager = new WaveManager(gameConfig.getWaves());
                } else {
                    waveManager = new WaveManager(null);
                }
            } catch (RuntimeException e) {
                System.err.println("Failed to setup wave manager: " + e.getMessage());
                waveManager = new WaveManager(null);
            }

            try {
                if (gameConfig.getInitialEnemies() != null) {
                    for (GameConfig.EnemyConfig enemyConfig : gameConfig.getInitialEnemies()) {
                        int reward = enemyConfig.getReward();
                        if (reward == 0) {
                            reward = 10;
                        }
                        // Using EnemyFactory pattern for enemy creation
                        enemies.add(enemyFactory.createCustomEnemy(
                            path, enemyConfig.getHealth(), enemyConfig.getSpeed(), 0, reward));
                    }
                }
            } catch (RuntimeException e) {
                System.err.println("Failed to setup initial enemies: " + e.getMessage());
            }

            try {
                if (gameConfig.getTowerTypes() != null && !gameConfig.getTowerTypes().isEmpty()) {
                    selectedTowerType = gameConfig.getTowerTypes().get(0);
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("No tower types available: " + e.getMessage());
                selectedTowerType = null;
            } catch (RuntimeException e) {
                System.err.println("Failed to setup tower selection: " + e.getMessage());
                selectedTowerType = null;
            }
            renderer = new GameRenderer(this);
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
        if (backgroundTexture != null) {
            float worldWidth = gameConfig != null && gameConfig.getWorldWidth() > 0
                    ? gameConfig.getWorldWidth()
                    : WORLD_WIDTH;
            float worldHeight = gameConfig != null && gameConfig.getWorldHeight() > 0
                    ? gameConfig.getWorldHeight()
                    : WORLD_HEIGHT;
            batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        }
        batch.end();

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            updateGame(deltaTime);
        }

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
            if (backgroundTexture != null) {
                backgroundTexture.dispose();
            }
        } catch (Exception e) {
            System.err.println("Error disposing background texture: " + e.getMessage());
        }

        try {
            if (beetlTexture != null) {
                beetlTexture.dispose();
            }
        } catch (Exception e) {
            System.err.println("Error disposing enemy texture: " + e.getMessage());
        }

        try {
            if (font != null) {
                font.dispose();
            }
        } catch (Exception e) {
            System.err.println("Error disposing font: " + e.getMessage());
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
        return font;
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
        return enemyAnimation;
    }

    public com.badlogic.gdx.graphics.Texture getCoinTexture() {
        return coinTexture;
    }

    public com.badlogic.gdx.graphics.Texture getBackgroundTexture() {
        return backgroundTexture;
    }

    public java.util.List<Enemy> getEnemies() {
        return enemies;
    }

    public java.util.List<Tower> getTowers() {
        return towers;
    }

    public java.util.List<Projectile> getProjectiles() {
        return projectiles;
    }

    public java.util.List<MoneyCoin> getMoneyCoins() {
        return moneyCoins;
    }

    public GameConfig.TowerTypeConfig getSelectedTowerType() {
        return selectedTowerType;
    }

    public Tower getSelectedTower() {
        return selectedTower;
    }

    public BudgetManager getBudgetManager() {
        return budgetManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public Path getPath() {
        return path;
    }

    public int getLives() {
        return lives;
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

    public com.gamedev.towerdefense.model.GameState getGameState() {
        return gameState;
    }

    private void updateGame(float deltaTime) {
        if (gameState == GameState.PAUSED) {
            handleInput();
            checkGameState();
            return;
        }
        if (waveManager != null) {
            waveManager.update(deltaTime, enemies, path);
        }

        handleInput();
        updateEnemies(deltaTime);
        checkGameState();
        updateMoneyCoins(deltaTime);
        updateTowers(deltaTime);
        updateProjectiles(deltaTime);
    }

    private void updateEnemies(float deltaTime) {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            enemy.update(deltaTime);
            if (!enemy.isAlive()) {
                Position enemyPos = enemy.getPosition();
                Position budgetTextPos = new Position(UI_MARGIN, WORLD_HEIGHT - UI_MARGIN);
                float coinSpeed = gameConfig.getMoneyCoinSpeed() > 0 ? gameConfig.getMoneyCoinSpeed() : DEFAULT_COIN_SPEED;
                MoneyCoin coin = new MoneyCoin(enemyPos, budgetTextPos, coinSpeed, enemy.getReward());
                moneyCoins.add(coin);
                it.remove();
            } else if (enemy.hasReachedEnd()) {
                lives--;
                it.remove();
            }
        }
    }

    private void updateTowers(float deltaTime) {
        for (Tower tower : towers) {
            tower.update(deltaTime, enemies, projectiles);
        }
    }

    private void updateProjectiles(float deltaTime) {
        Iterator<Projectile> projectileIt = projectiles.iterator();
        while (projectileIt.hasNext()) {
            Projectile projectile = projectileIt.next();
            projectile.update(deltaTime);
            if (projectile.hasHit()) {
                projectileIt.remove();
            }
        }
    }

    private void updateMoneyCoins(float deltaTime) {
        Iterator<MoneyCoin> coinIt = moneyCoins.iterator();
        while (coinIt.hasNext()) {
            MoneyCoin coin = coinIt.next();
            coin.update(deltaTime);
            if (coin.hasReachedTarget()) {
                budgetManager.earn(coin.getReward());
                coinIt.remove();
            }
        }
    }

    private void checkGameState() {
        if (gameState != GameState.PLAYING) {
            return;
        }

        if (lives <= 0) {
            gameState = GameState.LOST;
            return;
        }

        if (waveManager != null && waveManager.areAllWavesComplete() && enemies.isEmpty()) {
            gameState = GameState.WON;
        }

    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
                return;
            }
            gameState = GameState.PAUSED;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.T) && selectedTower != null) {
            cycleTargetingStrategy(selectedTower);
        }

        int towerKey = getTowerKeyPressed();

        switch (towerKey) {
            case 1 ->
                toggleTowerSelection(0);
            case 2 ->
                toggleTowerSelection(1);
            case 3 ->
                toggleTowerSelection(2);
        }

        handleTowerPlacement();
        Tower clickedTower = handleTowerClick();
        if (clickedTower != null) {
            selectedTower = clickedTower;
        } else if (handleTowerDescriptionClick()) {
        } else if (Gdx.input.justTouched()) {
            selectedTower = null;
        }
    }

    private void cycleTargetingStrategy(Tower tower) {
        TargetingStrategy current = tower.getTargetingStrategy();
        TargetingStrategy next;
        
        if (current instanceof NearestEnemyStrategy) {
            next = new StrongestEnemyStrategy();
        } else if (current instanceof StrongestEnemyStrategy) {
            next = new WeakestEnemyStrategy();
        } else {
            next = new NearestEnemyStrategy();
        }
        
        tower.setTargetingStrategy(next);
    }

    private boolean handleTowerDescriptionClick() {
        if (!Gdx.input.justTouched()) {
            return false;
        }
        if (selectedTower == null) {
            return false;
        }

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        Vector2 worldCoords = new Vector2(screenX, screenY);
        viewport.unproject(worldCoords);

        GameConfig.UpgradeConfig upgrades = gameConfig != null ? gameConfig.getUpgrades() : null;
        if (upgrades == null) {
            return false;
        }

        if (damageTextBounds.contains(worldCoords.x, worldCoords.y)) {
            int damageCost = upgrades.getDamageCost();
            int damageAmount = upgrades.getDamageAmount();
            if (budgetManager.canAfford(damageCost)) {
                budgetManager.spend(damageCost);
                selectedTower.increaseDamage(damageAmount);
            }
            return true;
        }
        if (rangeTextBounds.contains(worldCoords.x, worldCoords.y)) {
            int rangeCost = upgrades.getRangeCost();
            int rangeAmount = upgrades.getRangeAmount();
            if (budgetManager.canAfford(rangeCost)) {
                budgetManager.spend(rangeCost);
                selectedTower.increaseRange(rangeAmount);
            }
            return true;
        }
        if (cooldownTextBounds.contains(worldCoords.x, worldCoords.y)) {
            int cooldownCost = upgrades.getCooldownCost();
            float cooldownAmount = upgrades.getCooldownAmount();
            if (budgetManager.canAfford(cooldownCost)) {
                budgetManager.spend(cooldownCost);
                selectedTower.decreaseAttackCooldown(cooldownAmount);
            }
            return true;
        }
        // Strategy toggle - click to cycle through targeting strategies
        if (strategyTextBounds.contains(worldCoords.x, worldCoords.y)) {
            cycleTargetingStrategy(selectedTower);
            return true;
        }
        return false;
    }

    private int getTowerKeyPressed() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            return 1;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            return 2;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            return 3;
        }
        return 0;
    }

    private void toggleTowerSelection(int towerIndex) {
        try {
            if (gameConfig == null || gameConfig.getTowerTypes() == null) {
                return;
            }

            if (towerIndex < 0 || towerIndex >= gameConfig.getTowerTypes().size()) {
                return;
            }

            GameConfig.TowerTypeConfig towerType = gameConfig.getTowerTypes().get(towerIndex);

            if (selectedTowerType == towerType) {
                selectedTowerType = null;
            } else {
                selectedTowerType = towerType;
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Invalid tower index: " + towerIndex + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error toggling tower selection: " + e.getMessage());
        }
    }

    private Tower handleTowerClick() {
        if (!Gdx.input.justTouched()) {
            return null;
        }
        if (selectedTowerType != null) {
            return null;
        }

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        Vector2 worldCoords = new Vector2(screenX, screenY);
        viewport.unproject(worldCoords);

        for (Tower tower : towers) {
            if (worldCoords.x + 20 > tower.getPosition().getX()
                    && worldCoords.x - 20 < tower.getPosition().getX()) {
                if (worldCoords.y + 20 > tower.getPosition().getY()
                        && worldCoords.y - 20 < tower.getPosition().getY()) {
                    return tower;
                }
            }
        }
        return null;
    }

    private void handleTowerPlacement() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        Vector2 worldCoords = new Vector2(screenX, screenY);
        viewport.unproject(worldCoords);

        float worldX = worldCoords.x;
        float worldY = worldCoords.y;

        if (selectedTowerType == null || !budgetManager.canAfford(selectedTowerType.getCost())) {
            return;
        }

        if (!isValidTowerPlacement(worldX, worldY, selectedTowerType.getRange())) {
            return;
        }

        Position towerPos = new Position(worldX, worldY);
        float projectileSpeed = selectedTowerType.getProjectileSpeed() > 0
                ? selectedTowerType.getProjectileSpeed()
                : (gameConfig.getProjectileSpeed() > 0 ? gameConfig.getProjectileSpeed() : DEFAULT_PROJECTILE_SPEED);

        Tower newTower = new Tower(
                selectedTowerType.getCost(),
                selectedTowerType.getRange(),
                selectedTowerType.getDamage(),
                selectedTowerType.getAttackCooldown(),
                projectileSpeed,
                towerPos,
                selectedTowerType.getId());

        towers.add(newTower);
        budgetManager.spend(selectedTowerType.getCost());
        selectedTowerType = null;
    }

    public boolean isValidTowerPlacement(float x, float y, int newTowerRange) {
        Position pos = new Position(x, y);

        int minSpacing = gameConfig.getTowerPlacement() != null ? gameConfig.getTowerPlacement().getMinTowerSpacing()
                : 40;
        int minPathDistance = gameConfig.getTowerPlacement() != null
                ? gameConfig.getTowerPlacement().getMinDistanceFromPath()
                : 30;

        for (Tower tower : towers) {
            float distance = Position.distance(pos, tower.getPosition());

            if (distance < minSpacing) {
                return false;
            }

            if (distance < tower.getRange()) {
                return false;
            }

            if (distance < newTowerRange) {
                return false;
            }
        }

        try {
            List<Position> waypoints = path.getWaypoints();
            if (waypoints == null || waypoints.size() < 2) {
                return false;
            }

            for (int i = 0; i < waypoints.size() - 1; i++) {
                Position start = waypoints.get(i);
                Position end = waypoints.get(i + 1);

                float distToSegment = distanceToLineSegment(pos, start, end);
                if (distToSegment < minPathDistance) {
                    return false;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Index out of bounds when checking tower placement: " + e.getMessage());
            return false;
        } catch (RuntimeException e) {
            System.err.println("Error checking tower placement: " + e.getMessage());
            return false;
        }

        return true;
    }

    private float distanceToLineSegment(Position point, Position lineStart, Position lineEnd) {
        if (point == null || lineStart == null || lineEnd == null) {
            return Float.MAX_VALUE;
        }

        try {
            float A = point.getX() - lineStart.getX();
            float B = point.getY() - lineStart.getY();
            float C = lineEnd.getX() - lineStart.getX();
            float D = lineEnd.getY() - lineStart.getY();

            float dot = A * C + B * D;
            float lenSq = C * C + D * D;
            float param = -1;

            if (lenSq != 0) {
                param = dot / lenSq;
            }

            float xx, yy;

            if (param < 0) {
                xx = lineStart.getX();
                yy = lineStart.getY();
            } else if (param > 1) {
                xx = lineEnd.getX();
                yy = lineEnd.getY();
            } else {
                xx = lineStart.getX() + param * C;
                yy = lineStart.getY() + param * D;
            }

            float dx = point.getX() - xx;
            float dy = point.getY() - yy;
            return (float) Math.sqrt(dx * dx + dy * dy);
        } catch (ArithmeticException e) {
            System.err.println("Arithmetic error in distance calculation: " + e.getMessage());
            return Float.MAX_VALUE;
        } catch (RuntimeException e) {
            System.err.println("Error calculating distance to line segment: " + e.getMessage());
            return Float.MAX_VALUE;
        }
    }

}
