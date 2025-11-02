package com.gamedev.towerdefense.config;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameConfigTest {

    @BeforeAll
    public static void initGdx() {
        if (com.badlogic.gdx.Gdx.app == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            new HeadlessApplication(new com.badlogic.gdx.ApplicationAdapter() {
                @Override
                public void create() {
                }
            }, config);
        }
    }

    @Test
    public void testLoad_ValidConfig() {
        try {
            GameConfig config = GameConfig.load("game-config.json");
            assertNotNull(config);
            assertTrue(config.getInitialBudget() > 0);
            assertTrue(config.getInitialLives() > 0);
        } catch (Exception e) {
            fail("Failed to load valid config: " + e.getMessage());
        }
    }

    @Test
    public void testLoad_InvalidFile() {
        assertThrows(RuntimeException.class, () -> {
            GameConfig.load("nonexistent-config.json");
        });
    }

    @Test
    public void testGetTowerTypes() {
        GameConfig config = GameConfig.load("game-config.json");
        assertNotNull(config.getTowerTypes());
        assertFalse(config.getTowerTypes().isEmpty());
    }

    @Test
    public void testGetWaves() {
        GameConfig config = GameConfig.load("game-config.json");
        assertNotNull(config.getWaves());
        assertFalse(config.getWaves().isEmpty());
    }

    @Test
    public void testGetPathWaypoints() {
        GameConfig config = GameConfig.load("game-config.json");
        assertNotNull(config.getPathWaypoints());
        assertFalse(config.getPathWaypoints().isEmpty());
    }

    @Test
    public void testTowerTypeConfig() {
        GameConfig config = GameConfig.load("game-config.json");
        GameConfig.TowerTypeConfig tower = config.getTowerTypes().get(0);

        assertNotNull(tower);
        assertNotNull(tower.getName());
        assertTrue(tower.getCost() > 0);
        assertTrue(tower.getRange() > 0);
        assertTrue(tower.getDamage() > 0);
        assertTrue(tower.getAttackCooldown() >= 0);
        assertNotNull(tower.getColor());
    }

    @Test
    public void testWaveConfig() {
        GameConfig config = GameConfig.load("game-config.json");
        GameConfig.WaveConfig wave = config.getWaves().get(0);

        assertNotNull(wave);
        assertTrue(wave.getWaveNumber() > 0);
        assertTrue(wave.getStartTime() >= 0);
        assertNotNull(wave.getEnemies());
    }

    @Test
    public void testTowerTypeConfig_Id() {
        GameConfig config = GameConfig.load("game-config.json");
        for (GameConfig.TowerTypeConfig tower : config.getTowerTypes()) {
            assertTrue(tower.getId() > 0);
        }
    }

    @Test
    public void testTowerTypeConfig_Color() {
        GameConfig config = GameConfig.load("game-config.json");
        for (GameConfig.TowerTypeConfig tower : config.getTowerTypes()) {
            GameConfig.ColorConfig color = tower.getColor();
            assertNotNull(color);
            assertTrue(color.getR() >= 0 && color.getR() <= 1);
            assertTrue(color.getG() >= 0 && color.getG() <= 1);
            assertTrue(color.getB() >= 0 && color.getB() <= 1);
            assertTrue(color.getA() >= 0 && color.getA() <= 1);
        }
    }
}
