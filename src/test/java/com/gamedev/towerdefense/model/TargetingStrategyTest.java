package com.gamedev.towerdefense.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

class TargetingStrategyTest {

    private Path testPath;
    private Position towerPosition;
    private static final int TOWER_RANGE = 100;

    @BeforeEach
    void setUp() {
        testPath = new LinearPath(Arrays.asList(
            new Position(0, 0),
            new Position(100, 0),
            new Position(200, 0)
        ));
        towerPosition = new Position(50, 0);
    }

    @Test
    void nearestStrategy_selectsClosestEnemy() {
        // Create enemies at different distances
        Enemy nearEnemy = new Enemy(testPath, 100, 50f, 0, 10);  // at start
        Enemy farEnemy = new Enemy(testPath, 100, 50f, 2, 10);   // at end
        
        List<Enemy> enemies = Arrays.asList(farEnemy, nearEnemy);
        
        TargetingStrategy strategy = new NearestEnemyStrategy();
        Enemy target = strategy.selectTarget(towerPosition, TOWER_RANGE, enemies);
        
        assertEquals(nearEnemy, target);
    }

    @Test
    void strongestStrategy_selectsHighestHealthEnemy() {
        Enemy weakEnemy = new Enemy(testPath, 50, 50f, 0, 10);
        Enemy strongEnemy = new Enemy(testPath, 200, 50f, 0, 10);
        
        List<Enemy> enemies = Arrays.asList(weakEnemy, strongEnemy);
        
        TargetingStrategy strategy = new StrongestEnemyStrategy();
        Enemy target = strategy.selectTarget(towerPosition, TOWER_RANGE, enemies);
        
        assertEquals(strongEnemy, target);
    }

    @Test
    void weakestStrategy_selectsLowestHealthEnemy() {
        Enemy weakEnemy = new Enemy(testPath, 50, 50f, 0, 10);
        Enemy strongEnemy = new Enemy(testPath, 200, 50f, 0, 10);
        
        List<Enemy> enemies = Arrays.asList(strongEnemy, weakEnemy);
        
        TargetingStrategy strategy = new WeakestEnemyStrategy();
        Enemy target = strategy.selectTarget(towerPosition, TOWER_RANGE, enemies);
        
        assertEquals(weakEnemy, target);
    }

    @Test
    void strategy_ignoresDeadEnemies() {
        Enemy deadEnemy = new Enemy(testPath, 100, 50f, 0, 10);
        deadEnemy.takeDamage(100);  // Kill the enemy
        
        Enemy aliveEnemy = new Enemy(testPath, 100, 50f, 0, 10);
        
        List<Enemy> enemies = Arrays.asList(deadEnemy, aliveEnemy);
        
        TargetingStrategy strategy = new NearestEnemyStrategy();
        Enemy target = strategy.selectTarget(towerPosition, TOWER_RANGE, enemies);
        
        assertEquals(aliveEnemy, target);
    }

    @Test
    void strategy_returnsNullWhenNoEnemiesInRange() {
        Position farTowerPosition = new Position(500, 0);
        Enemy enemy = new Enemy(testPath, 100, 50f, 0, 10);
        
        List<Enemy> enemies = Arrays.asList(enemy);
        
        TargetingStrategy strategy = new NearestEnemyStrategy();
        Enemy target = strategy.selectTarget(farTowerPosition, TOWER_RANGE, enemies);
        
        assertNull(target);
    }
}
