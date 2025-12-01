package com.gamedev.towerdefense.model;

import java.util.ArrayList;
import java.util.List;

import com.gamedev.towerdefense.config.GameConfig;

public class WaveManager {

    private List<GameConfig.WaveConfig> waves;
    private List<PendingEnemy> pendingEnemies;
    private float gameTime;
    private int currentWaveIndex;
    private boolean allWavesComplete;

    private static class PendingEnemy {

        float spawnTime;
        GameConfig.WaveEnemyConfig enemyConfig;

        PendingEnemy(float spawnTime, GameConfig.WaveEnemyConfig enemyConfig) {
            this.spawnTime = spawnTime;
            this.enemyConfig = enemyConfig;
        }
    }

    public WaveManager(List<GameConfig.WaveConfig> waves) {
        this.waves = waves != null ? waves : new ArrayList<>();
        this.pendingEnemies = new ArrayList<>();
        this.gameTime = 0f;
        this.currentWaveIndex = 0;
        this.allWavesComplete = false;
        prepareWaveEnemies();
    }

    private void prepareWaveEnemies() {
        pendingEnemies.clear();
        for (GameConfig.WaveConfig wave : waves) {
            float waveStartTime = wave.getStartTime();
            if (wave.getEnemies() != null) {
                for (GameConfig.WaveEnemyConfig enemyConfig : wave.getEnemies()) {
                    float spawnTime = waveStartTime + enemyConfig.getSpawnDelay();
                    pendingEnemies.add(new PendingEnemy(spawnTime, enemyConfig));
                }
            }
        }
        pendingEnemies.sort((a, b) -> Float.compare(a.spawnTime, b.spawnTime));
    }

    public void update(float deltaTime, List<Enemy> enemies, Path path) {
        gameTime += deltaTime;

        if (allWavesComplete) {
            return;
        }

        while (currentWaveIndex < pendingEnemies.size()) {
            PendingEnemy pending = pendingEnemies.get(currentWaveIndex);

            if (gameTime >= pending.spawnTime) {
                GameConfig.WaveEnemyConfig enemyConfig = pending.enemyConfig;
                int reward = enemyConfig.getReward();
                if (reward == 0) {
                    reward = 10;
                }
                Enemy enemy = new Enemy(path, enemyConfig.getHealth(),
                        enemyConfig.getSpeed(), 0, reward);
                enemies.add(enemy);
                currentWaveIndex++;
            } else {
                break;
            }
        }

        if (currentWaveIndex >= pendingEnemies.size()) {
            allWavesComplete = true;
        }
    }

    public float getGameTime() {
        return gameTime;
    }

    public int getCurrentWaveNumber() {
        if (waves.isEmpty()) {
            return 0;
        }

        for (int i = waves.size() - 1; i >= 0; i--) {
            if (gameTime >= waves.get(i).getStartTime()) {
                return waves.get(i).getWaveNumber();
            }
        }
        return 1;
    }

    public int getTotalWaves() {
        return waves.size();
    }

    public boolean areAllWavesComplete() {
        return allWavesComplete;
    }
}
