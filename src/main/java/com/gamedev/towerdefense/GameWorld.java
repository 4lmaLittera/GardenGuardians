package com.gamedev.towerdefense;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.BudgetManager;
import com.gamedev.towerdefense.model.CurvedPath;
import com.gamedev.towerdefense.model.Enemy;
import com.gamedev.towerdefense.model.EnemyFactory;
import com.gamedev.towerdefense.model.GameState;
import com.gamedev.towerdefense.model.MoneyCoin;
import com.gamedev.towerdefense.model.Path;
import com.gamedev.towerdefense.model.Position;
import com.gamedev.towerdefense.model.Projectile;
import com.gamedev.towerdefense.model.Tower;
import com.gamedev.towerdefense.model.WaveManager;

public class GameWorld {
    public static final float UI_MARGIN = 10f;
    public static final float DEFAULT_COIN_SPEED = 200f;

    private final GameConfig gameConfig;
    private final BudgetManager budgetManager;
    private final WaveManager waveManager;
    private final EnemyFactory enemyFactory = new EnemyFactory();

    private int lives;
    private GameState gameState = GameState.PLAYING;
    private final Path path;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<MoneyCoin> moneyCoins = new ArrayList<>();

    private GameConfig.TowerTypeConfig selectedTowerType;
    private Tower selectedTower;

    public GameWorld(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
        try {
            List<Position> waypoints = gameConfig.getPathWaypoints();
            if (waypoints == null || waypoints.isEmpty()) {
                throw new RuntimeException("Path waypoints are missing or empty");
            }
            this.path = new CurvedPath(waypoints);
        } catch (RuntimeException e) {
            System.err.println("Failed to setup path: " + e.getMessage());
            throw new RuntimeException("Cannot start game without path", e);
        }

        this.budgetManager = new BudgetManager(gameConfig.getInitialBudget());
        this.lives = gameConfig.getInitialLives();

        try {
            if (gameConfig.getWaves() != null && !gameConfig.getWaves().isEmpty()) {
                this.waveManager = new WaveManager(gameConfig.getWaves());
            } else {
                this.waveManager = new WaveManager(null);
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to setup wave manager: " + e.getMessage());
            throw new RuntimeException("Cannot start game without wave manager", e);
        }
        initializeEnemies();
        initializeTowerSelection();
    }

    private void initializeEnemies() {
        try {
            if (gameConfig.getInitialEnemies() != null) {
                for (GameConfig.EnemyConfig enemyConfig : gameConfig.getInitialEnemies()) {
                    int reward = enemyConfig.getReward();
                    if (reward == 0) {
                        reward = 10;
                    }
                    enemies.add(enemyFactory.createCustomEnemy(
                            path, enemyConfig.getHealth(), enemyConfig.getSpeed(), 0, reward));
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to setup initial enemies: " + e.getMessage());
        }
    }

    private void initializeTowerSelection() {
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
    }

    public void update(float deltaTime) {
        if (gameState == GameState.PAUSED) {
            checkGameState();
            return;
        }
        
        if (waveManager != null) {
            waveManager.update(deltaTime, enemies, path);
        }

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
                // Note: WORLD_HEIGHT access needs to be resolved. Passing simplified coordinate or calculating elsewhere.
                // For now, assuming standard height or retrieving from config if stored there, 
                // but simpler to use a fixed position for coin target relative to screen.
                float worldHeight = gameConfig.getWorldHeight() > 0 ? gameConfig.getWorldHeight() : 720;
                Position budgetTextPos = new Position(UI_MARGIN, worldHeight - UI_MARGIN);
                
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
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public BudgetManager getBudgetManager() {
        return budgetManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public int getLives() {
        return lives;
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Path getPath() {
        return path;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<MoneyCoin> getMoneyCoins() {
        return moneyCoins;
    }

    public GameConfig.TowerTypeConfig getSelectedTowerType() {
        return selectedTowerType;
    }

    public void setSelectedTowerType(GameConfig.TowerTypeConfig selectedTowerType) {
        this.selectedTowerType = selectedTowerType;
    }

    public Tower getSelectedTower() {
        return selectedTower;
    }

    public void setSelectedTower(Tower selectedTower) {
        this.selectedTower = selectedTower;
    }
}
