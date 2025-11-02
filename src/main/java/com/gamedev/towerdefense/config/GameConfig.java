package com.gamedev.towerdefense.config;

import com.gamedev.towerdefense.model.Position;
import com.google.gson.Gson;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;

public class GameConfig {
    private int initialBudget;
    private int initialLives;
    private PathConfig path;
    private List<EnemyConfig> initialEnemies;
    private List<WaveConfig> waves;
    private List<TowerTypeConfig> towerTypes;
    private int worldWidth;
    private int worldHeight;
    private float projectileSpeed;
    private float moneyCoinSpeed;
    private TowerPlacementConfig towerPlacement;
    private VisualConfig visual;

    public static GameConfig load(String filename) {
        try {
            FileHandle file = Gdx.files.internal(filename);
            if (!file.exists()) {
                throw new RuntimeException("Config file not found: " + filename);
            }
            
            String json = file.readString();
            if (json == null || json.isEmpty()) {
                throw new RuntimeException("Config file is empty: " + filename);
            }
            
            Gson gson = new Gson();
            GameConfig config = gson.fromJson(json, GameConfig.class);
            
            if (config == null) {
                throw new RuntimeException("Failed to parse config file: " + filename);
            }
            
            return config;
        } catch (RuntimeException e) {
            System.err.println("Error loading game config: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error loading game config: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load game configuration", e);
        }
    }

    public int getInitialBudget() {
        return initialBudget;
    }

    public int getInitialLives() {
        return initialLives;
    }

    public PathConfig getPath() {
        return path;
    }

    public List<EnemyConfig> getInitialEnemies() {
        return initialEnemies;
    }

    public List<WaveConfig> getWaves() {
        return waves;
    }

    public List<TowerTypeConfig> getTowerTypes() {
        return towerTypes;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public float getMoneyCoinSpeed() {
        return moneyCoinSpeed;
    }

    public TowerPlacementConfig getTowerPlacement() {
        return towerPlacement;
    }

    public VisualConfig getVisual() {
        return visual;
    }

    public List<Position> getPathWaypoints() {
        List<Position> waypoints = new ArrayList<>();
        if (path != null && path.getWaypoints() != null) {
            for (WaypointConfig waypoint : path.getWaypoints()) {
                waypoints.add(new Position(waypoint.getX(), waypoint.getY()));
            }
        }
        return waypoints;
    }

    public static class PathConfig {
        private List<WaypointConfig> waypoints;

        public List<WaypointConfig> getWaypoints() {
            return waypoints;
        }
    }

    public static class WaypointConfig {
        private float x;
        private float y;

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    public static class EnemyConfig {
        private int health;
        private float speed;
        private float spawnTime;
        private int reward;

        public int getHealth() {
            return health;
        }

        public float getSpeed() {
            return speed;
        }

        public float getSpawnTime() {
            return spawnTime;
        }

        public int getReward() {
            return reward;
        }
    }

    public static class WaveConfig {
        private int waveNumber;
        private float startTime;
        private List<WaveEnemyConfig> enemies;

        public int getWaveNumber() {
            return waveNumber;
        }

        public float getStartTime() {
            return startTime;
        }

        public List<WaveEnemyConfig> getEnemies() {
            return enemies;
        }
    }

    public static class WaveEnemyConfig {
        private int health;
        private float speed;
        private float spawnDelay;
        private int reward;

        public int getHealth() {
            return health;
        }

        public float getSpeed() {
            return speed;
        }

        public float getSpawnDelay() {
            return spawnDelay;
        }

        public int getReward() {
            return reward;
        }
    }

    public static class TowerTypeConfig {
        private int id;
        private String name;
        private int cost;
        private int range;
        private int damage;
        private float attackCooldown;
        private float projectileSpeed;
        private ColorConfig color;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public int getRange() {
            return range;
        }

        public int getDamage() {
            return damage;
        }

        public float getAttackCooldown() {
            return attackCooldown;
        }

        public float getProjectileSpeed() {
            return projectileSpeed;
        }

        public ColorConfig getColor() {
            return color;
        }
    }

    public static class TowerPlacementConfig {
        private int minTowerSpacing;
        private int minDistanceFromPath;

        public int getMinTowerSpacing() {
            return minTowerSpacing;
        }

        public int getMinDistanceFromPath() {
            return minDistanceFromPath;
        }
    }

    public static class VisualConfig {
        private ColorConfig backgroundColor;
        private float rangeCircleOpacity;

        public ColorConfig getBackgroundColor() {
            return backgroundColor;
        }

        public float getRangeCircleOpacity() {
            return rangeCircleOpacity;
        }
    }

    public static class ColorConfig {
        private float r;
        private float g;
        private float b;
        private float a;

        public float getR() {
            return r;
        }

        public float getG() {
            return g;
        }

        public float getB() {
            return b;
        }

        public float getA() {
            return a;
        }
    }
}
