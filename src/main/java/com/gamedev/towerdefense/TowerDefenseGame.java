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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.BudgetManager;
import com.gamedev.towerdefense.model.Enemy;
import com.gamedev.towerdefense.model.GameState;
import com.gamedev.towerdefense.model.MoneyCoin;
import com.gamedev.towerdefense.model.Path;
import com.gamedev.towerdefense.model.Position;
import com.gamedev.towerdefense.model.Projectile;
import com.gamedev.towerdefense.model.Tower;
import com.gamedev.towerdefense.model.WaveManager;

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

    // Lifecycle methods
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal("assets/images/grass_template2.jpg"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        font = new BitmapFont();
        font.getData().setScale(1f, 1f);

        gameConfig = GameConfig.load("game-config.json");

        if (gameConfig.getWorldWidth() > 0) {
            camera.setToOrtho(false, gameConfig.getWorldWidth(), gameConfig.getWorldHeight());
            viewport = new FitViewport(gameConfig.getWorldWidth(), gameConfig.getWorldHeight(), camera);
        }

        budgetManager = new BudgetManager(gameConfig.getInitialBudget());

        lives = gameConfig.getInitialLives();

        List<Position> waypoints = gameConfig.getPathWaypoints();
        path = new Path(waypoints);

        if (gameConfig.getWaves() != null && !gameConfig.getWaves().isEmpty()) {
            waveManager = new WaveManager(gameConfig.getWaves());
        } else {
            waveManager = new WaveManager(null);
        }

        if (gameConfig.getInitialEnemies() != null) {
            for (GameConfig.EnemyConfig enemyConfig : gameConfig.getInitialEnemies()) {
                int reward = enemyConfig.getReward();
                if (reward == 0) {
                    reward = 10;
                }
                enemies.add(new Enemy(path, enemyConfig.getHealth(), enemyConfig.getSpeed(), 0, reward));
            }
        }

        if (gameConfig.getTowerTypes() != null && !gameConfig.getTowerTypes().isEmpty()) {
            selectedTowerType = gameConfig.getTowerTypes().get(0);
        }
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

        // Draw background image
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

        if (gameState == GameState.PLAYING) {
            updateGame(deltaTime);
        }

        shapeRenderer.setProjectionMatrix(camera.combined);

        renderPath();
        renderWaypoints();
        renderEnemies();
        renderTowers();
        renderProjectiles();
        renderMoneyCoins();

        renderUI();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }

    // Update methods
    private void updateGame(float deltaTime) {
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

    // Input handling
    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            selectedTowerType = gameConfig.getTowerTypes().get(0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            selectedTowerType = gameConfig.getTowerTypes().get(1);
        }

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
                towerPos);

        towers.add(newTower);
        budgetManager.spend(selectedTowerType.getCost());
    }

    // Utility methods
    private boolean isValidTowerPlacement(float x, float y, int newTowerRange) {
        Position pos = new Position(x, y);

        int minSpacing = gameConfig.getTowerPlacement() != null ? gameConfig.getTowerPlacement().getMinTowerSpacing()
                : 40;
        int minPathDistance = gameConfig.getTowerPlacement() != null
                ? gameConfig.getTowerPlacement().getMinDistanceFromPath()
                : 30;

        for (Tower tower : towers) {
            float distance = Position.distance(pos, tower.getPosition());

            // Check minimum spacing between towers
            if (distance < minSpacing) {
                return false;
            }

            // Check if new tower position is within existing tower's range
            if (distance < tower.getRange()) {
                return false;
            }

            // Check if existing tower is within new tower's range
            if (distance < newTowerRange) {
                return false;
            }
        }

        List<Position> waypoints = path.getWaypoints();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Position start = waypoints.get(i);
            Position end = waypoints.get(i + 1);

            float distToSegment = distanceToLineSegment(pos, start, end);
            if (distToSegment < minPathDistance) {
                return false;
            }
        }

        // Check bounds (optional - keep towers on screen)
        if (x < 0 || x > WORLD_WIDTH || y < 0 || y > WORLD_HEIGHT) {
            return false;
        }

        return true;
    }

    private float distanceToLineSegment(Position point, Position lineStart, Position lineEnd) {
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
    }

    // Render methods
    private void renderPath() {
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        List<Position> waypoints = path.getWaypoints();
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Position start = waypoints.get(i);
            Position end = waypoints.get(i + 1);
            shapeRenderer.line(start.getX(), start.getY(), end.getX(), end.getY());
        }
        shapeRenderer.end();
    }

    private void renderWaypoints() {
        List<Position> waypoints = path.getWaypoints();
        if (waypoints.isEmpty()) {
            return;
        }

        shapeRenderer.begin(ShapeType.Filled);

        Position startPos = waypoints.get(0);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(startPos.getX(), startPos.getY(), 10);

        if (waypoints.size() > 1) {
            Position endPos = waypoints.get(waypoints.size() - 1);
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.circle(endPos.getX(), endPos.getY(), 10);
        }

        shapeRenderer.end();
    }

    private void renderEnemies() {
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 1, 1);
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            Position enemyPos = enemy.getPosition();
            shapeRenderer.circle(enemyPos.getX(), enemyPos.getY(), 15);
        }
        shapeRenderer.end();
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
        shapeRenderer.setColor(0.8f, 0.8f, 0.2f, 1f);
        for (Tower tower : towers) {
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
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.84f, 0f, 1f);
        for (MoneyCoin coin : moneyCoins) {
            Position coinPos = coin.getPosition();
            shapeRenderer.circle(coinPos.getX(), coinPos.getY(), 8);
        }
        shapeRenderer.end();
    }

    private void renderUI() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.draw(batch, "Budget: " + budgetManager.getBudget(), 10, WORLD_HEIGHT - 10);
        font.draw(batch, "Lives: " + lives, 10, WORLD_HEIGHT - 50);

        if (gameConfig.getTowerTypes() != null) {
            float startY = WORLD_HEIGHT - 100;
            float spacing = 20f;

            for (int i = 0; i < gameConfig.getTowerTypes().size(); i++) {
                GameConfig.TowerTypeConfig towerType = gameConfig.getTowerTypes().get(i);
                String towerText = (i + 1) + ". " + towerType.getName() + " - $" + towerType.getCost();

                if (selectedTowerType == towerType) {
                    font.setColor(1f, 1f, 0f, 1f);
                } else {
                    font.setColor(1f, 1f, 1f, 1f);
                }

                font.draw(batch, towerText, 10, startY - (i * spacing));
            }
            font.setColor(1f, 1f, 1f, 1f);

        }

        if (waveManager != null) {
            int currentWave = waveManager.getCurrentWaveNumber();
            int totalWaves = waveManager.getTotalWaves();
            if (waveManager.areAllWavesComplete()) {
                font.draw(batch, "Wave: " + currentWave + "/" + totalWaves + " (Complete)", 10, WORLD_HEIGHT - 30);
            } else {
                font.draw(batch, "Wave: " + currentWave + "/" + totalWaves, 10, WORLD_HEIGHT - 30);
            }
        }

        if (gameState == GameState.WON) {
            String winText = "YOU WIN!";
            float textWidth = font.getData().getGlyph('A').width * winText.length();
            font.draw(batch, winText, WORLD_WIDTH / 2 - textWidth / 2, WORLD_HEIGHT / 2);
        } else if (gameState == GameState.LOST) {
            String loseText = "GAME OVER!";
            float textWidth = font.getData().getGlyph('A').width * loseText.length();
            font.draw(batch, loseText, WORLD_WIDTH / 2 - textWidth / 2, WORLD_HEIGHT / 2);
        }

        batch.end();
    }
}
