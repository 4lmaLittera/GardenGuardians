package com.gamedev.towerdefense.model;

/**
 * Factory Method pattern for creating different types of enemies.
 * Encapsulates enemy creation logic and provides a central place to create enemies.
 */
public class EnemyFactory {
    
    // Enemy type constants
    public static final String TYPE_BASIC = "basic";
    public static final String TYPE_FAST = "fast";
    public static final String TYPE_TANK = "tank";
    public static final String TYPE_BOSS = "boss";
    
    // Default stats for different enemy types
    private static final int BASIC_HEALTH = 100;
    private static final float BASIC_SPEED = 50f;
    private static final int BASIC_REWARD = 10;
    
    private static final int FAST_HEALTH = 50;
    private static final float FAST_SPEED = 100f;
    private static final int FAST_REWARD = 15;
    
    private static final int TANK_HEALTH = 300;
    private static final float TANK_SPEED = 25f;
    private static final int TANK_REWARD = 30;
    
    private static final int BOSS_HEALTH = 1000;
    private static final float BOSS_SPEED = 20f;
    private static final int BOSS_REWARD = 100;

    public Enemy createEnemy(String type, Path path) {
        return createEnemy(type, path, 0);
    }
    
    public Enemy createEnemy(String type, Path path, int startWaypointIndex) {
        switch (type.toLowerCase()) {
            case TYPE_FAST:
                return new Enemy(path, FAST_HEALTH, FAST_SPEED, startWaypointIndex, FAST_REWARD);
            case TYPE_TANK:
                return new Enemy(path, TANK_HEALTH, TANK_SPEED, startWaypointIndex, TANK_REWARD);
            case TYPE_BOSS:
                return new Enemy(path, BOSS_HEALTH, BOSS_SPEED, startWaypointIndex, BOSS_REWARD);
            case TYPE_BASIC:
            default:
                return new Enemy(path, BASIC_HEALTH, BASIC_SPEED, startWaypointIndex, BASIC_REWARD);
        }
    }
    
    public Enemy createCustomEnemy(Path path, int health, float speed, int reward) {
        return createCustomEnemy(path, health, speed, 0, reward);
    }

    public Enemy createCustomEnemy(Path path, int health, float speed, int startWaypointIndex, int reward) {
        return new Enemy(path, health, speed, startWaypointIndex, reward);
    }
    public Enemy[] createWave(int waveNumber, Path path, int count) {
        Enemy[] enemies = new Enemy[count];
        
        for (int i = 0; i < count; i++) {
            String type = determineEnemyTypeForWave(waveNumber, i);
            enemies[i] = createEnemy(type, path);
        }
        
        return enemies;
    }
    
    private String determineEnemyTypeForWave(int waveNumber, int positionInWave) {
        if (waveNumber % 5 == 0 && positionInWave == 0) {
            return TYPE_BOSS;
        }
        
        if (waveNumber >= 3 && positionInWave % 4 == 0) {
            return TYPE_TANK;
        }
        
        if (waveNumber >= 2 && positionInWave % 3 == 0) {
            return TYPE_FAST;
        }
        
        return TYPE_BASIC;
    }
}
