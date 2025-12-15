package com.gamedev.towerdefense.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

class EnemyFactoryTest {

    private EnemyFactory factory;
    private Path testPath;

    @BeforeEach
    void setUp() {
        factory = new EnemyFactory();
        testPath = new LinearPath(Arrays.asList(
            new Position(0, 0),
            new Position(100, 0)
        ));
    }

    @Test
    void createEnemy_basic_createsEnemyWithCorrectStats() {
        Enemy enemy = factory.createEnemy(EnemyFactory.TYPE_BASIC, testPath);
        
        assertEquals(100, enemy.getHealth());
        assertEquals(10, enemy.getReward());
        assertTrue(enemy.isAlive());
    }

    @Test
    void createEnemy_fast_createsFasterEnemy() {
        Enemy fastEnemy = factory.createEnemy(EnemyFactory.TYPE_FAST, testPath);
        Enemy basicEnemy = factory.createEnemy(EnemyFactory.TYPE_BASIC, testPath);
        
        assertTrue(fastEnemy.getSpeed() > basicEnemy.getSpeed());
        assertTrue(fastEnemy.getHealth() < basicEnemy.getHealth());
    }

    @Test
    void createEnemy_tank_createsHigherHealthEnemy() {
        Enemy tankEnemy = factory.createEnemy(EnemyFactory.TYPE_TANK, testPath);
        Enemy basicEnemy = factory.createEnemy(EnemyFactory.TYPE_BASIC, testPath);
        
        assertTrue(tankEnemy.getHealth() > basicEnemy.getHealth());
        assertTrue(tankEnemy.getSpeed() < basicEnemy.getSpeed());
    }

    @Test
    void createEnemy_boss_createsStrongestEnemy() {
        Enemy bossEnemy = factory.createEnemy(EnemyFactory.TYPE_BOSS, testPath);
        Enemy tankEnemy = factory.createEnemy(EnemyFactory.TYPE_TANK, testPath);
        
        assertTrue(bossEnemy.getHealth() > tankEnemy.getHealth());
        assertEquals(100, bossEnemy.getReward());
    }

    @Test
    void createCustomEnemy_createsEnemyWithCustomStats() {
        int customHealth = 250;
        float customSpeed = 75f;
        int customReward = 50;
        
        Enemy enemy = factory.createCustomEnemy(testPath, customHealth, customSpeed, customReward);
        
        assertEquals(customHealth, enemy.getHealth());
        assertEquals(customReward, enemy.getReward());
    }

    @Test
    void createWave_createsCorrectNumberOfEnemies() {
        int waveSize = 5;
        Enemy[] wave = factory.createWave(1, testPath, waveSize);
        
        assertEquals(waveSize, wave.length);
        for (Enemy enemy : wave) {
            assertNotNull(enemy);
            assertTrue(enemy.isAlive());
        }
    }
}
