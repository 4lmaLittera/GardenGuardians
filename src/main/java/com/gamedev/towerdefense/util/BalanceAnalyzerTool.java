package com.gamedev.towerdefense.util;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.Path;
import com.gamedev.towerdefense.model.LinearPath;
import com.gamedev.towerdefense.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Standalone Balance Analyzer Tool
 * 
 * Analyzes game balance metrics including:
 * - Tower efficiency (DPS, cost efficiency, time-to-kill)
 * - Wave difficulty progression
 * - Economy balance
 * 
 * Usage:
 * ./gradlew balance
 */
public class BalanceAnalyzerTool {

    public static void main(String[] args) {
        // Create minimal LibGDX application just to access file system
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1, 1);
        config.setIdleFPS(1);

        new Lwjgl3Application(new BalanceAnalyzerApp(), config);
    }

    private static class BalanceAnalyzerApp implements com.badlogic.gdx.ApplicationListener {
        @Override
        public void create() {
            try {
                GameConfig gameConfig = GameConfig.load("game-config.json");
                BalanceReport report = analyze(gameConfig);
                report.printReport();
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Error analyzing balance: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }

        @Override
        public void resize(int width, int height) {
        }

        @Override
        public void render() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void resume() {
        }

        @Override
        public void dispose() {
        }
    }

    public static class TowerAnalysis {
        public final String name;
        public final int id;
        public final int cost;
        public final int damage;
        public final float attackCooldown;
        public final float dps;
        public final float dpsPerCost;
        public final int shotsToKill100HP;
        public final int shotsToKill150HP;
        public final int shotsToKill200HP;
        public final float timeToKill100HP;
        public final float timeToKill150HP;
        public final float timeToKill200HP;
        public final int range;
        public final float efficiencyScore;

        public TowerAnalysis(GameConfig.TowerTypeConfig tower) {
            this.name = tower.getName();
            this.id = tower.getId();
            this.cost = tower.getCost();
            this.damage = tower.getDamage();
            this.attackCooldown = tower.getAttackCooldown();
            this.range = tower.getRange();

            this.dps = attackCooldown > 0 ? damage / attackCooldown : Float.MAX_VALUE;
            this.dpsPerCost = cost > 0 ? dps / cost : 0;

            this.shotsToKill100HP = (int) Math.ceil(100.0 / damage);
            this.shotsToKill150HP = (int) Math.ceil(150.0 / damage);
            this.shotsToKill200HP = (int) Math.ceil(200.0 / damage);

            this.timeToKill100HP = shotsToKill100HP * attackCooldown;
            this.timeToKill150HP = shotsToKill150HP * attackCooldown;
            this.timeToKill200HP = shotsToKill200HP * attackCooldown;

            // Efficiency score: DPS/cost * range/100 (higher is better)
            this.efficiencyScore = dpsPerCost * (range / 100f);
        }
    }

    public static class WaveAnalysis {
        public final int waveNumber;
        public final int enemyCount;
        public final int totalHealth;
        public final float avgHealth;
        public final float avgSpeed;
        public final int totalReward;
        public final float pathTime; // Time to traverse path
        public final float difficultyScore; // Total HP / (average speed)

        public WaveAnalysis(GameConfig.WaveConfig wave, Path path) {
            this.waveNumber = wave.getWaveNumber();
            this.enemyCount = wave.getEnemies() != null ? wave.getEnemies().size() : 0;

            int totalHP = 0;
            float totalSpeed = 0;
            int totalRewards = 0;

            if (wave.getEnemies() != null) {
                for (GameConfig.WaveEnemyConfig enemy : wave.getEnemies()) {
                    totalHP += enemy.getHealth();
                    totalSpeed += enemy.getSpeed();
                    totalRewards += enemy.getReward();
                }
            }

            this.totalHealth = totalHP;
            this.avgHealth = enemyCount > 0 ? totalHP / (float) enemyCount : 0;
            this.avgSpeed = enemyCount > 0 ? totalSpeed / (float) enemyCount : 0;
            this.totalReward = totalRewards;

            float pathLength = path != null ? path.getPathLength() : 0;
            this.pathTime = avgSpeed > 0 && pathLength > 0 ? pathLength / avgSpeed : 0;
            this.difficultyScore = avgSpeed > 0 ? totalHP / avgSpeed : 0;
        }
    }

    public static class BalanceReport {
        public final List<TowerAnalysis> towers;
        public final List<WaveAnalysis> waves;
        public final float pathLength;
        public final int initialBudget;
        public final int initialLives;

        public BalanceReport(GameConfig config, Path path) {
            this.towers = new ArrayList<>();
            this.waves = new ArrayList<>();

            if (config.getTowerTypes() != null) {
                for (GameConfig.TowerTypeConfig tower : config.getTowerTypes()) {
                    towers.add(new TowerAnalysis(tower));
                }
            }

            if (config.getWaves() != null) {
                for (GameConfig.WaveConfig wave : config.getWaves()) {
                    waves.add(new WaveAnalysis(wave, path));
                }
            }

            this.pathLength = path != null ? path.getPathLength() : 0;
            this.initialBudget = config.getInitialBudget();
            this.initialLives = config.getInitialLives();
        }

        public void printReport() {
            System.out.println("=== TOWER DEFENSE BALANCE ANALYSIS ===\n");

            System.out.println("=== GAME SETUP ===");
            System.out.printf("Initial Budget: %d\n", initialBudget);
            System.out.printf("Initial Lives: %d\n", initialLives);
            System.out.printf("Path Length: %.2f units\n\n", pathLength);

            System.out.println("=== TOWER ANALYSIS ===");
            for (TowerAnalysis tower : towers) {
                System.out.printf("\n%s (ID: %d) - Cost: %d\n", tower.name, tower.id, tower.cost);
                System.out.printf("  Damage: %d | Cooldown: %.2fs | Range: %d\n",
                        tower.damage, tower.attackCooldown, tower.range);
                System.out.printf("  DPS: %.2f | DPS/Cost: %.3f | Efficiency Score: %.3f\n",
                        tower.dps, tower.dpsPerCost, tower.efficiencyScore);
                System.out.printf("  Time to Kill: %.2fs (100HP) | %.2fs (150HP) | %.2fs (200HP)\n",
                        tower.timeToKill100HP, tower.timeToKill150HP, tower.timeToKill200HP);
            }

            System.out.println("\n=== WAVE ANALYSIS ===");
            for (WaveAnalysis wave : waves) {
                System.out.printf("\nWave %d\n", wave.waveNumber);
                System.out.printf("  Enemies: %d | Total HP: %d | Avg HP: %.1f | Avg Speed: %.1f\n",
                        wave.enemyCount, wave.totalHealth, wave.avgHealth, wave.avgSpeed);
                System.out.printf("  Total Reward: %d | Path Time: %.2fs | Difficulty Score: %.2f\n",
                        wave.totalReward, wave.pathTime, wave.difficultyScore);
            }

            System.out.println("\n=== BALANCE RECOMMENDATIONS ===");
            printRecommendations();
        }

        private void printRecommendations() {
            if (towers.size() < 2) {
                System.out.println("Need at least 2 towers for balance comparison.");
                return;
            }

            // Find best and worst efficiency
            TowerAnalysis bestEfficiency = towers.get(0);
            TowerAnalysis worstEfficiency = towers.get(0);

            for (TowerAnalysis tower : towers) {
                if (tower.efficiencyScore > bestEfficiency.efficiencyScore) {
                    bestEfficiency = tower;
                }
                if (tower.efficiencyScore < worstEfficiency.efficiencyScore) {
                    worstEfficiency = tower;
                }
            }

            float efficiencyRatio = bestEfficiency.efficiencyScore / worstEfficiency.efficiencyScore;
            if (efficiencyRatio > 2.0f) {
                System.out.printf("⚠️  Large efficiency gap: %s is %.1fx more efficient than %s\n",
                        bestEfficiency.name, efficiencyRatio, worstEfficiency.name);
                System.out.printf("   Consider: Reducing %s cost or increasing %s efficiency\n",
                        worstEfficiency.name, worstEfficiency.name);
            }

            // Check wave progression
            if (waves.size() >= 2) {
                float difficultyIncrease = waves.get(waves.size() - 1).difficultyScore / waves.get(0).difficultyScore;
                System.out.printf("\n📈 Wave difficulty increases by %.2fx from wave 1 to wave %d\n",
                        difficultyIncrease, waves.size());

                if (difficultyIncrease < 1.5f) {
                    System.out.println("   Consider: Gradually increasing difficulty more");
                } else if (difficultyIncrease > 5.0f) {
                    System.out.println("   ⚠️  Difficulty spike may be too steep");
                }
            }

            // Economy check
            if (waves.size() > 0) {
                int totalWaveReward = 0;
                for (WaveAnalysis wave : waves) {
                    totalWaveReward += wave.totalReward;
                }
                int affordableTowers = (initialBudget + totalWaveReward) / towers.get(0).cost;
                System.out.printf("\n💰 Economy: Starting budget + wave rewards = %d total\n",
                        initialBudget + totalWaveReward);
                System.out.printf("   Can afford approximately %d %s towers\n",
                        affordableTowers, towers.get(0).name);

                if (affordableTowers < 3) {
                    System.out.println("   ⚠️  Consider: Increasing rewards or reducing tower costs");
                }
            }
        }
    }

    public static BalanceReport analyze(GameConfig config) {
        List<Position> waypoints = config.getPathWaypoints();
        Path path = new LinearPath(waypoints);
        return new BalanceReport(config, path);
    }
}
