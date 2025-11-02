package com.gamedev.towerdefense.model;

import com.gamedev.towerdefense.config.GameConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaveManagerTest {

    private GameConfig.WaveConfig createWaveConfig(int waveNumber, float startTime,
            List<GameConfig.WaveEnemyConfig> enemies) {
        GameConfig.WaveConfig wave = new GameConfig.WaveConfig();
        try {
            Field waveNumberField = GameConfig.WaveConfig.class.getDeclaredField("waveNumber");
            waveNumberField.setAccessible(true);
            waveNumberField.set(wave, waveNumber);

            Field startTimeField = GameConfig.WaveConfig.class.getDeclaredField("startTime");
            startTimeField.setAccessible(true);
            startTimeField.set(wave, startTime);

            Field enemiesField = GameConfig.WaveConfig.class.getDeclaredField("enemies");
            enemiesField.setAccessible(true);
            enemiesField.set(wave, enemies);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return wave;
    }

    private GameConfig.WaveEnemyConfig createEnemyConfig(int health, float speed, float spawnDelay, int reward) {
        GameConfig.WaveEnemyConfig enemy = new GameConfig.WaveEnemyConfig();
        try {
            Field healthField = GameConfig.WaveEnemyConfig.class.getDeclaredField("health");
            healthField.setAccessible(true);
            healthField.set(enemy, health);

            Field speedField = GameConfig.WaveEnemyConfig.class.getDeclaredField("speed");
            speedField.setAccessible(true);
            speedField.set(enemy, speed);

            Field spawnDelayField = GameConfig.WaveEnemyConfig.class.getDeclaredField("spawnDelay");
            spawnDelayField.setAccessible(true);
            spawnDelayField.set(enemy, spawnDelay);

            Field rewardField = GameConfig.WaveEnemyConfig.class.getDeclaredField("reward");
            rewardField.setAccessible(true);
            rewardField.set(enemy, reward);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return enemy;
    }

    @Test
    public void testConstructor_EmptyWaves() {
        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        WaveManager waveManager = new WaveManager(waves);

        assertEquals(0, waveManager.getTotalWaves());
        assertEquals(0, waveManager.getCurrentWaveNumber());
        assertFalse(waveManager.areAllWavesComplete());
        assertEquals(0f, waveManager.getGameTime(), 0.001f);
    }

    @Test
    public void testConstructor_WithWaves() {
        List<GameConfig.WaveEnemyConfig> enemies = new ArrayList<>();
        enemies.add(createEnemyConfig(100, 50f, 0f, 10));

        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, enemies));

        WaveManager waveManager = new WaveManager(waves);

        assertEquals(1, waveManager.getTotalWaves());
        assertEquals(1, waveManager.getCurrentWaveNumber());
        assertFalse(waveManager.areAllWavesComplete());
    }

    @Test
    public void testGetTotalWaves() {
        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, new ArrayList<>()));
        waves.add(createWaveConfig(2, 10f, new ArrayList<>()));
        waves.add(createWaveConfig(3, 20f, new ArrayList<>()));

        WaveManager waveManager = new WaveManager(waves);

        assertEquals(3, waveManager.getTotalWaves());
    }

    @Test
    public void testGetCurrentWaveNumber_BeforeFirstWave() {
        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 5f, new ArrayList<>()));

        WaveManager waveManager = new WaveManager(waves);

        assertEquals(1, waveManager.getCurrentWaveNumber());
    }

    @Test
    public void testGetCurrentWaveNumber_DuringWave() {
        List<GameConfig.WaveEnemyConfig> enemies = new ArrayList<>();
        enemies.add(createEnemyConfig(100, 50f, 0f, 10));

        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, enemies));
        waves.add(createWaveConfig(2, 10f, new ArrayList<>()));

        WaveManager waveManager = new WaveManager(waves);
        List<Enemy> enemyList = new ArrayList<>();
        Path path = new LinearPath(Arrays.asList(new Position(0, 0), new Position(100, 0)));

        waveManager.update(5f, enemyList, path);

        assertEquals(1, waveManager.getCurrentWaveNumber());
    }

    @Test
    public void testUpdate_SpawnsEnemies() {
        List<GameConfig.WaveEnemyConfig> enemies = new ArrayList<>();
        enemies.add(createEnemyConfig(100, 50f, 0f, 10));
        enemies.add(createEnemyConfig(150, 60f, 1f, 15));

        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, enemies));

        WaveManager waveManager = new WaveManager(waves);
        List<Enemy> enemyList = new ArrayList<>();
        Path path = new LinearPath(Arrays.asList(new Position(0, 0), new Position(100, 0)));

        assertEquals(0, enemyList.size());

        waveManager.update(0.5f, enemyList, path);
        assertEquals(1, enemyList.size());

        waveManager.update(0.6f, enemyList, path);
        assertEquals(2, enemyList.size());
    }

    @Test
    public void testUpdate_RespectsSpawnDelay() {
        List<GameConfig.WaveEnemyConfig> enemies = new ArrayList<>();
        enemies.add(createEnemyConfig(100, 50f, 0f, 10));
        enemies.add(createEnemyConfig(150, 60f, 2f, 15));

        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, enemies));

        WaveManager waveManager = new WaveManager(waves);
        List<Enemy> enemyList = new ArrayList<>();
        Path path = new LinearPath(Arrays.asList(new Position(0, 0), new Position(100, 0)));

        waveManager.update(0.5f, enemyList, path);
        assertEquals(1, enemyList.size());

        waveManager.update(1f, enemyList, path);
        assertEquals(1, enemyList.size());

        waveManager.update(0.6f, enemyList, path);
        assertEquals(2, enemyList.size());
    }

    @Test
    public void testUpdate_MultipleWaves() {
        List<GameConfig.WaveEnemyConfig> wave1Enemies = new ArrayList<>();
        wave1Enemies.add(createEnemyConfig(100, 50f, 0f, 10));

        List<GameConfig.WaveEnemyConfig> wave2Enemies = new ArrayList<>();
        wave2Enemies.add(createEnemyConfig(150, 60f, 0f, 15));

        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, wave1Enemies));
        waves.add(createWaveConfig(2, 5f, wave2Enemies));

        WaveManager waveManager = new WaveManager(waves);
        List<Enemy> enemyList = new ArrayList<>();
        Path path = new LinearPath(Arrays.asList(new Position(0, 0), new Position(100, 0)));

        waveManager.update(1f, enemyList, path);
        assertEquals(1, enemyList.size());
        assertEquals(1, waveManager.getCurrentWaveNumber());

        waveManager.update(5f, enemyList, path);
        assertEquals(2, enemyList.size());
        assertEquals(2, waveManager.getCurrentWaveNumber());
    }

    @Test
    public void testUpdate_AllWavesComplete() {
        List<GameConfig.WaveEnemyConfig> enemies = new ArrayList<>();
        enemies.add(createEnemyConfig(100, 50f, 0f, 10));

        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, enemies));

        WaveManager waveManager = new WaveManager(waves);
        List<Enemy> enemyList = new ArrayList<>();
        Path path = new LinearPath(Arrays.asList(new Position(0, 0), new Position(100, 0)));

        assertFalse(waveManager.areAllWavesComplete());

        waveManager.update(1f, enemyList, path);
        assertTrue(waveManager.areAllWavesComplete());

        int initialSize = enemyList.size();
        waveManager.update(1f, enemyList, path);
        assertEquals(initialSize, enemyList.size());
    }

    @Test
    public void testUpdate_EnemyRewardDefaultsTo10() {
        List<GameConfig.WaveEnemyConfig> enemies = new ArrayList<>();
        GameConfig.WaveEnemyConfig enemy = createEnemyConfig(100, 50f, 0f, 0);
        enemies.add(enemy);

        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        waves.add(createWaveConfig(1, 0f, enemies));

        WaveManager waveManager = new WaveManager(waves);
        List<Enemy> enemyList = new ArrayList<>();
        Path path = new LinearPath(Arrays.asList(new Position(0, 0), new Position(100, 0)));

        waveManager.update(1f, enemyList, path);

        assertEquals(1, enemyList.size());
        assertEquals(10, enemyList.get(0).getReward());
    }

    @Test
    public void testGetCurrentWaveNumber_NoWaves() {
        List<GameConfig.WaveConfig> waves = new ArrayList<>();
        WaveManager waveManager = new WaveManager(waves);

        assertEquals(0, waveManager.getCurrentWaveNumber());
    }
}
