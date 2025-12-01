package com.gamedev.towerdefense;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.BudgetManager;
import com.gamedev.towerdefense.model.Enemy;
import com.gamedev.towerdefense.model.GameState;
import com.gamedev.towerdefense.model.MoneyCoin;
import com.gamedev.towerdefense.model.Path;
import com.gamedev.towerdefense.model.CurvedPath;
import com.gamedev.towerdefense.model.Position;
import com.gamedev.towerdefense.model.Projectile;
import com.gamedev.towerdefense.model.Tower;
import com.gamedev.towerdefense.model.WaveManager;
import com.gamedev.towerdefense.util.AnimationManager;

public class TowerDefenseGame extends ApplicationAdapter {

    // Constants
    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;

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

    // Game configuration and managers
    private GameConfig gameConfig;
    private BudgetManager budgetManager;
    private WaveManager waveManager;

    // Game state
    private int lives;
    private GameState gameState = GameState.PLAYING;
    private Path path;

    // Game entities
    private List<Enemy> enemies = new ArrayList<>();
    private List<Tower> towers = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<MoneyCoin> moneyCoins = new ArrayList<>();

    // Tower selection
    private GameConfig.TowerTypeConfig selectedTowerType;
    private Tower selectedTower;

    @Override
    public void create() {
        try {
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();

            try {
                backgroundTexture = new Texture(Gdx.files.internal("assets/images/grass_template2.jpg"));
            } catch (Exception e) {
                System.err.println("Failed to load background texture: " + e.getMessage());
                backgroundTexture = null;
            }

            try {
                beetlTexture = new Texture(Gdx.files.internal("assets/images/BeetleMove.png"));
            } catch (Exception e) {
                System.err.println("Failed to load enemy texture: " + e.getMessage());
                beetlTexture = null;
            }

            if (beetlTexture != null) {
                try {
                    int frameWidth = 32;
                    int frameHeight = 32;
                    int cols = 4;
                    int rows = 4;
                    float frameDuration = 0.2f;
                    enemyAnimation = new AnimationManager(beetlTexture, frameWidth, frameHeight, cols, rows,
                            frameDuration);
                } catch (Exception e) {
                    System.err.println("Failed to create enemy animation: " + e.getMessage());
                    enemyAnimation = null;
                }
            }

            try {
                coinTexture = new Texture(Gdx.files.internal("assets/images/coin.png"));
            } catch (Exception e) {
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
            } catch (Exception e) {
                System.err.println("Failed to load game configuration: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Cannot start game without configuration", e);
            }

            try {
                if (gameConfig.getWorldWidth() > 0 && gameConfig.getWorldHeight() > 0) {
                    camera.setToOrtho(false, gameConfig.getWorldWidth(), gameConfig.getWorldHeight());
                    viewport = new FitViewport(gameConfig.getWorldWidth(), gameConfig.getWorldHeight(), camera);
                }
            } catch (Exception e) {
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
            } catch (Exception e) {
                System.err.println("Failed to setup path: " + e.getMessage());
                throw new RuntimeException("Cannot start game without path", e);
            }

            try {
                if (gameConfig.getWaves() != null && !gameConfig.getWaves().isEmpty()) {
                    waveManager = new WaveManager(gameConfig.getWaves());
                } else {
                    waveManager = new WaveManager(null);
                }
            } catch (Exception e) {
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
                        enemies.add(new Enemy(path, enemyConfig.getHealth(), enemyConfig.getSpeed(), 0, reward));
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to setup initial enemies: " + e.getMessage());
            }

            try {
                if (gameConfig.getTowerTypes() != null && !gameConfig.getTowerTypes().isEmpty()) {
                    selectedTowerType = gameConfig.getTowerTypes().get(0);
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("No tower types available: " + e.getMessage());
                selectedTowerType = null;
            } catch (Exception e) {
                System.err.println("Failed to setup tower selection: " + e.getMessage());
                selectedTowerType = null;
            }
        } catch (Exception e) {
            System.err.println("Critical error during game initialization: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        damageTextBounds = new Rectangle();
        rangeTextBounds = new Rectangle();
        cooldownTextBounds = new Rectangle();
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

        renderPath();
        renderWaypoints();
        renderEnemies();
        renderTowers();
        renderProjectiles();
        renderMoneyCoins();
        renderTowerPreview();

        renderUI();
        renderTowerStatsPanel();
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
                Position budgetTextPos = new Position(10, WORLD_HEIGHT - 10);
                float coinSpeed = gameConfig.getMoneyCoinSpeed() > 0 ? gameConfig.getMoneyCoinSpeed() : 200f;
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

        int towerKey = getTowerKeyPressed();

        switch (towerKey) {
            case 1:
                toggleTowerSelection(0);
                break;
            case 2:
                toggleTowerSelection(1);
                break;
            case 3:
                toggleTowerSelection(2);
                break;
            default:
                break;
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
                : (gameConfig.getProjectileSpeed() > 0 ? gameConfig.getProjectileSpeed() : 300f);

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

    private boolean isValidTowerPlacement(float x, float y, int newTowerRange) {
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
        } catch (Exception e) {
            System.err.println("Error checking tower placement: " + e.getMessage());
            return false;
        }

        if (x < 0 || x > WORLD_WIDTH || y < 0 || y > WORLD_HEIGHT) {
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
        } catch (Exception e) {
            System.err.println("Error calculating distance to line segment: " + e.getMessage());
            return Float.MAX_VALUE;
        }
    }

    private Color getTowerColor(int towerId) {
        if (gameConfig.getTowerTypes() == null) {
            return new Color(0.8f, 0.8f, 0.2f, 1f);
        }

        for (GameConfig.TowerTypeConfig towerType : gameConfig.getTowerTypes()) {
            if (towerId == towerType.getId()) {
                GameConfig.ColorConfig colorConfig = towerType.getColor();
                if (colorConfig != null) {
                    return new Color(colorConfig.getR(), colorConfig.getG(), colorConfig.getB(), colorConfig.getA());
                }
            }
        }

        return new Color(0.8f, 0.8f, 0.2f, 1f);
    }

    private void renderPath() {
        try {
            if (path == null) {
                return;
            }

            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(1, 1, 1, 1);

            if (path instanceof CurvedPath) {
                int samples = 100;
                Position prev = path.getPositionAt(0f);

                for (int i = 1; i <= samples; i++) {
                    try {
                        float t = (float) i / samples;
                        Position current = path.getPositionAt(t);
                        if (prev != null && current != null) {
                            shapeRenderer.line(prev.getX(), prev.getY(), current.getX(), current.getY());
                        }
                        prev = current;
                    } catch (Exception e) {
                        System.err.println("Error sampling curve point: " + e.getMessage());
                        break;
                    }
                }
            } else {
                List<Position> waypoints = path.getWaypoints();

                if (waypoints == null || waypoints.size() < 2) {
                    shapeRenderer.end();
                    return;
                }

                for (int i = 0; i < waypoints.size() - 1; i++) {
                    try {
                        Position start = waypoints.get(i);
                        Position end = waypoints.get(i + 1);
                        if (start != null && end != null) {
                            shapeRenderer.line(start.getX(), start.getY(), end.getX(), end.getY());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.err.println("Index out of bounds when rendering path: " + e.getMessage());
                        break;
                    }
                }
            }
            shapeRenderer.end();
        } catch (Exception e) {
            System.err.println("Error rendering path: " + e.getMessage());
            try {
                shapeRenderer.end();
            } catch (Exception ignored) {
            }
        }
    }

    private void renderWaypoints() {
        try {
            if (path == null) {
                return;
            }

            List<Position> waypoints = path.getWaypoints();
            if (waypoints == null || waypoints.isEmpty()) {
                return;
            }

            shapeRenderer.begin(ShapeType.Filled);

            try {
                Position startPos = waypoints.get(0);
                if (startPos != null) {
                    shapeRenderer.setColor(0, 1, 0, 1);
                    shapeRenderer.circle(startPos.getX(), startPos.getY(), 10);
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Index out of bounds when rendering start waypoint: " + e.getMessage());
            }

            try {
                if (waypoints.size() > 1) {
                    Position endPos = waypoints.get(waypoints.size() - 1);
                    if (endPos != null) {
                        shapeRenderer.setColor(1, 0, 0, 1);
                        shapeRenderer.circle(endPos.getX(), endPos.getY(), 10);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Index out of bounds when rendering end waypoint: " + e.getMessage());
            }

            shapeRenderer.end();
        } catch (Exception e) {
            System.err.println("Error rendering waypoints: " + e.getMessage());
            try {
                shapeRenderer.end();
            } catch (Exception ignored) {
            }
        }
    }

    private void renderEnemies() {
        if (enemyAnimation == null) {
            return;
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            Position enemyPos = enemy.getPosition();
            int directionRow = enemy.getDirectionRow();
            TextureRegion currentFrame = enemyAnimation.getFrame(enemy.getAnimationTime(), directionRow);
            batch.draw(currentFrame, enemyPos.getX() - 32, enemyPos.getY() - 32, 64, 64);
        }
        batch.end();
    }

    private void renderTowers() {
        float rangeOpacity = gameConfig.getVisual() != null ? gameConfig.getVisual().getRangeCircleOpacity() : 0.3f;

        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, rangeOpacity);
        for (Tower tower : towers) {
            Position towerPos = tower.getPosition();
            shapeRenderer.circle(towerPos.getX(), towerPos.getY(), tower.getRange());
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeType.Filled);
        for (Tower tower : towers) {
            Color towerColor = getTowerColor(tower.getTowerId());
            shapeRenderer.setColor(towerColor.r, towerColor.g, towerColor.b, towerColor.a);
            Position towerPos = tower.getPosition();
            shapeRenderer.rect(towerPos.getX() - 10, towerPos.getY() - 10, 20, 20);
        }
        shapeRenderer.end();
    }

    private void renderProjectiles() {
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        for (Projectile projectile : projectiles) {
            Position projPos = projectile.getPosition();
            shapeRenderer.circle(projPos.getX(), projPos.getY(), 5);
        }
        shapeRenderer.end();
    }

    private void renderMoneyCoins() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (MoneyCoin coin : moneyCoins) {
            Position coinPos = coin.getPosition();
            batch.draw(coinTexture, coinPos.getX() - 16, coinPos.getY() - 16, 32, 32);
        }
        batch.end();
    }

    private void renderTowerPreview() {
        try {
            if (selectedTowerType == null || gameState != GameState.PLAYING) {
                return;
            }

            if (budgetManager == null || !budgetManager.canAfford(selectedTowerType.getCost())) {
                return;
            }

            try {
                int screenX = Gdx.input.getX();
                int screenY = Gdx.input.getY();
                Vector2 worldCoords = new Vector2(screenX, screenY);
                viewport.unproject(worldCoords);

                float worldX = worldCoords.x;
                float worldY = worldCoords.y;

                boolean isValidPlacement = isValidTowerPlacement(worldX, worldY, selectedTowerType.getRange());

                Color towerColor = getTowerColor(selectedTowerType.getId());

                try {
                    shapeRenderer.begin(ShapeType.Line);
                    float rangeOpacity = 0.5f;
                    if (isValidPlacement) {
                        shapeRenderer.setColor(towerColor.r, towerColor.g, towerColor.b, rangeOpacity);
                    } else {
                        shapeRenderer.setColor(1f, 0f, 0f, rangeOpacity);
                    }
                    shapeRenderer.circle(worldX, worldY, selectedTowerType.getRange());
                    shapeRenderer.end();
                } catch (Exception e) {
                    System.err.println("Error rendering range preview: " + e.getMessage());
                    try {
                        shapeRenderer.end();
                    } catch (Exception ignored) {
                    }
                }

                try {
                    shapeRenderer.begin(ShapeType.Filled);
                    float previewOpacity = 0.6f;
                    if (isValidPlacement) {
                        shapeRenderer.setColor(towerColor.r, towerColor.g, towerColor.b, previewOpacity);
                    } else {
                        shapeRenderer.setColor(1f, 0f, 0f, previewOpacity);
                    }
                    shapeRenderer.rect(worldX - 10, worldY - 10, 20, 20);
                    shapeRenderer.end();
                } catch (Exception e) {
                    System.err.println("Error rendering tower preview: " + e.getMessage());
                    try {
                        shapeRenderer.end();
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting cursor position: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error rendering tower preview: " + e.getMessage());
        }
    }

    private void renderUI() {
        try {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            font.setColor(1f, 1f, 1f, 1f);

            try {
                font.draw(batch, "Budget: " + budgetManager.getBudget(), 10, WORLD_HEIGHT - 10);
                font.draw(batch, "Lives: " + lives, 10, WORLD_HEIGHT - 50);
            } catch (Exception e) {
                System.err.println("Error rendering budget/lives: " + e.getMessage());
            }

            if (gameConfig != null && gameConfig.getTowerTypes() != null) {
                try {
                    float startY = WORLD_HEIGHT - 100;
                    float spacing = 20f;

                    for (int i = 0; i < gameConfig.getTowerTypes().size(); i++) {
                        try {
                            GameConfig.TowerTypeConfig towerType = gameConfig.getTowerTypes().get(i);
                            if (towerType == null) {
                                continue;
                            }

                            String towerText = (i + 1) + ". " + towerType.getName() + " - $" + towerType.getCost();

                            if (selectedTowerType == towerType) {
                                font.setColor(1f, 1f, 0f, 1f);
                            } else {
                                font.setColor(1f, 1f, 1f, 1f);
                            }

                            font.draw(batch, towerText, 10, startY - (i * spacing));
                        } catch (IndexOutOfBoundsException e) {
                            System.err.println("Index out of bounds when rendering tower UI: " + e.getMessage());
                            break;
                        } catch (Exception e) {
                            System.err.println("Error rendering tower " + i + ": " + e.getMessage());
                        }
                    }
                    font.setColor(1f, 1f, 1f, 1f);
                } catch (Exception e) {
                    System.err.println("Error rendering tower selection UI: " + e.getMessage());
                }
            }

            try {
                if (waveManager != null) {
                    int currentWave = waveManager.getCurrentWaveNumber();
                    int totalWaves = waveManager.getTotalWaves();
                    if (waveManager.areAllWavesComplete()) {
                        font.draw(batch, "Wave: " + currentWave + "/" + totalWaves + " (Complete)", 10,
                                WORLD_HEIGHT - 30);
                    } else {
                        font.draw(batch, "Wave: " + currentWave + "/" + totalWaves, 10, WORLD_HEIGHT - 30);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error rendering wave info: " + e.getMessage());
            }

            try {
                if (gameState == GameState.WON) {
                    String winText = "YOU WIN!";
                    float textWidth = font.getData().getGlyph('A').width * winText.length();
                    font.draw(batch, winText, WORLD_WIDTH / 2 - textWidth / 2, WORLD_HEIGHT / 2);
                } else if (gameState == GameState.LOST) {
                    String loseText = "GAME OVER!";
                    float textWidth = font.getData().getGlyph('A').width * loseText.length();
                    font.draw(batch, loseText, WORLD_WIDTH / 2 - textWidth / 2, WORLD_HEIGHT / 2);
                }
            } catch (Exception e) {
                System.err.println("Error rendering game state message: " + e.getMessage());
            }

            batch.end();
        } catch (Exception e) {
            System.err.println("Error rendering UI: " + e.getMessage());
            try {
                batch.end();
            } catch (Exception ignored) {
            }
        }
    }

    private void renderTowerStatsPanel() {
        if (selectedTower == null) {
            return;
        }

        try {
            float panelX = WORLD_WIDTH - 250;
            float panelY = 100;
            float panelWidth = 230;
            float panelHeight = 200;

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 0.9f);
            shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeType.Line);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.rect(panelX, panelY, panelWidth, panelHeight);
            shapeRenderer.end();

            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            float startY = panelY + panelHeight - 20;
            float lineSpacing = 25f;

            font.setColor(1f, 1f, 0f, 1f);
            font.draw(batch, "Tower Stats", panelX + 10, startY);

            font.setColor(1f, 1f, 1f, 1f);

            startY -= lineSpacing;

            GameConfig.UpgradeConfig upgrades = gameConfig != null ? gameConfig.getUpgrades() : null;
            int damageCost = upgrades != null ? upgrades.getDamageCost() : 0;
            int rangeCost = upgrades != null ? upgrades.getRangeCost() : 0;
            int cooldownCost = upgrades != null ? upgrades.getCooldownCost() : 0;

            String damageText = "Damage: " + selectedTower.getDamage();
            boolean canAffordDamage = budgetManager.canAfford(damageCost);
            String damageCostText = " [$" + damageCost + "]" + (canAffordDamage ? " +" : "");
            font.setColor(1f, 1f, 1f, 1f);
            GlyphLayout damageLayout = new GlyphLayout(font, damageText);
            float damageX = panelX + 10;
            font.draw(batch, damageText, damageX, startY);

            font.setColor(canAffordDamage ? 0.4f : 1f, canAffordDamage ? 1f : 0.4f, 0.4f, 1f);
            GlyphLayout damageCostLayout = new GlyphLayout(font, damageCostText);
            font.draw(batch, damageCostText, damageX + damageLayout.width, startY);

            damageTextBounds.width = damageLayout.width + damageCostLayout.width;
            damageTextBounds.height = Math.max(damageLayout.height, damageCostLayout.height);
            damageTextBounds.x = damageX;
            damageTextBounds.y = startY - damageTextBounds.height;

            startY -= lineSpacing;

            String rangeText = "Range: " + selectedTower.getRange();
            boolean canAffordRange = budgetManager.canAfford(rangeCost);
            String rangeCostText = " [$" + rangeCost + "]" + (canAffordRange ? " +" : "");
            font.setColor(1f, 1f, 1f, 1f);
            GlyphLayout rangeLayout = new GlyphLayout(font, rangeText);
            float rangeX = panelX + 10;
            font.draw(batch, rangeText, rangeX, startY);

            font.setColor(canAffordRange ? 0.4f : 1f, canAffordRange ? 1f : 0.4f, 0.4f, 1f);
            GlyphLayout rangeCostLayout = new GlyphLayout(font, rangeCostText);
            font.draw(batch, rangeCostText, rangeX + rangeLayout.width, startY);

            rangeTextBounds.width = rangeLayout.width + rangeCostLayout.width;
            rangeTextBounds.height = Math.max(rangeLayout.height, rangeCostLayout.height);
            rangeTextBounds.x = rangeX;
            rangeTextBounds.y = startY - rangeTextBounds.height;

            startY -= lineSpacing;

            String cooldownText = "Cooldown: " + String.format("%.2f", selectedTower.getBaseAttackCooldown()) + "s";
            boolean canAffordCooldown = budgetManager.canAfford(cooldownCost);
            String cooldownCostText = " [$" + cooldownCost + "]" + (canAffordCooldown ? " +" : "");
            font.setColor(1f, 1f, 1f, 1f);
            GlyphLayout cooldownLayout = new GlyphLayout(font, cooldownText);
            float cooldownX = panelX + 10;
            font.draw(batch, cooldownText, cooldownX, startY);

            font.setColor(canAffordCooldown ? 0.4f : 1f, canAffordCooldown ? 1f : 0.4f, 0.4f, 1f);
            GlyphLayout cooldownCostLayout = new GlyphLayout(font, cooldownCostText);
            font.draw(batch, cooldownCostText, cooldownX + cooldownLayout.width, startY);

            cooldownTextBounds.width = cooldownLayout.width + cooldownCostLayout.width;
            cooldownTextBounds.height = Math.max(cooldownLayout.height, cooldownCostLayout.height);
            cooldownTextBounds.x = cooldownX;
            cooldownTextBounds.y = startY - cooldownTextBounds.height;

            startY -= lineSpacing;
            font.setColor(1f, 1f, 1f, 1f);
            font.draw(batch, "ID: " + selectedTower.getTowerId(), panelX + 10, startY);

            font.setColor(1f, 1f, 1f, 1f);
            batch.end();

        } catch (Exception e) {
            System.err.println("Error rendering tower stats panel: " + e.getMessage());
        }
    }
}
