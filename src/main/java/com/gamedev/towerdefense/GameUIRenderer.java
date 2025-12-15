package com.gamedev.towerdefense;

import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.Tower;

public class GameUIRenderer {

    private final TowerDefenseGame game;

    public GameUIRenderer(TowerDefenseGame game) {
        this.game = game;
    }

    public void render() {
        drawTowerStatsPanel();
        try {
            game.getBatch().setProjectionMatrix(game.getCamera().combined);
            game.getBatch().begin();

            drawBudgetAndLives();
            drawTowerSelectionList();
            drawWaveInfo();
            drawGameStateMessage();

            Tower selected = game.getSelectedTower();
            if (selected != null) {
                float panelWidth = GameRenderer.STATS_PANEL_WIDTH;
                float panelHeight = GameRenderer.STATS_PANEL_HEIGHT;
                float panelX = TowerDefenseGame.WORLD_WIDTH - panelWidth - TowerDefenseGame.UI_MARGIN;
                float panelY = GameRenderer.STATS_PANEL_Y;
                drawStatsTextEntries(selected, panelX, panelY, panelHeight);
            }

        } catch (Exception e) {
            System.err.println("Error rendering UI batch: " + e.getMessage());
        } finally {
            try {
                game.getBatch().end();
            } catch (Exception ignored) {
            }
        }
    }

    private void drawBudgetAndLives() {
        try {
            game.getFont().setColor(1f, 1f, 1f, 1f);
            float budgetY = TowerDefenseGame.WORLD_HEIGHT - TowerDefenseGame.UI_MARGIN;
            float livesY = budgetY - GameRenderer.BUDGET_LIVES_LINE_GAP;
            game.getFont().draw(game.getBatch(), "Budget: " + game.getBudgetManager().getBudget(),
                    TowerDefenseGame.UI_MARGIN, budgetY);
            game.getFont().draw(game.getBatch(), "Lives: " + game.getLives(), TowerDefenseGame.UI_MARGIN, livesY);
        } catch (Exception e) {
            System.err.println("Error rendering budget/lives: " + e.getMessage());
        }
    }

    private void drawTowerSelectionList() {
        if (game.getGameConfig() == null || game.getGameConfig().getTowerTypes() == null) {
            return;
        }
        try {
            float startY = TowerDefenseGame.TOWER_LIST_START_Y;
            float spacing = TowerDefenseGame.TOWER_LIST_SPACING;

            for (int i = 0; i < game.getGameConfig().getTowerTypes().size(); i++) {
                try {
                    GameConfig.TowerTypeConfig towerType = game.getGameConfig().getTowerTypes().get(i);
                    if (towerType == null) {
                        continue;
                    }

                    String towerText = (i + 1) + ". " + towerType.getName() + " - $" + towerType.getCost();

                    if (game.getSelectedTowerType() == towerType) {
                        game.getFont().setColor(1f, 1f, 0f, 1f);
                    } else {
                        game.getFont().setColor(1f, 1f, 1f, 1f);
                    }

                    game.getFont().draw(game.getBatch(), towerText, 10, startY - (i * spacing));
                } catch (Exception e) {
                    System.err.println("Error rendering tower " + i + ": " + e.getMessage());
                }
            }
            game.getFont().setColor(1f, 1f, 1f, 1f);
        } catch (Exception e) {
            System.err.println("Error rendering tower selection UI: " + e.getMessage());
        }
    }

    private void drawWaveInfo() {
        try {
            if (game.getWaveManager() != null) {
                int currentWave = game.getWaveManager().getCurrentWaveNumber();
                int totalWaves = game.getWaveManager().getTotalWaves();
                float waveY = TowerDefenseGame.WORLD_HEIGHT - TowerDefenseGame.UI_MARGIN - GameRenderer.WAVE_INFO_OFFSET_Y;
                if (game.getWaveManager().areAllWavesComplete()) {
                    game.getFont().draw(game.getBatch(), "Wave: " + currentWave + "/" + totalWaves + " (Complete)",
                            TowerDefenseGame.UI_MARGIN, waveY);
                } else {
                    game.getFont().draw(game.getBatch(), "Wave: " + currentWave + "/" + totalWaves,
                            TowerDefenseGame.UI_MARGIN, waveY);
                }
            }
        } catch (Exception e) {
            System.err.println("Error rendering wave info: " + e.getMessage());
        }
    }

    private void drawGameStateMessage() {
        try {
            if (game.getGameState() == com.gamedev.towerdefense.model.GameState.WON) {
                String winText = "YOU WIN!";
                float textWidth = game.getFont().getData().getGlyph('A').width * winText.length();
                game.getFont().draw(game.getBatch(), winText, TowerDefenseGame.WORLD_WIDTH / 2 - textWidth / 2,
                        TowerDefenseGame.WORLD_HEIGHT / 2);
            } else if (game.getGameState() == com.gamedev.towerdefense.model.GameState.LOST) {
                String loseText = "GAME OVER!";
                float textWidth = game.getFont().getData().getGlyph('A').width * loseText.length();
                game.getFont().draw(game.getBatch(), loseText, TowerDefenseGame.WORLD_WIDTH / 2 - textWidth / 2,
                        TowerDefenseGame.WORLD_HEIGHT / 2);
            }
        } catch (Exception e) {
            System.err.println("Error rendering game state message: " + e.getMessage());
        }
    }

    private void drawTowerStatsPanel() {
        Tower selectedTower = game.getSelectedTower();
        if (selectedTower == null) {
            return;
        }

        try {
            float panelWidth = GameRenderer.STATS_PANEL_WIDTH;
            float panelHeight = GameRenderer.STATS_PANEL_HEIGHT;
            float panelX = TowerDefenseGame.WORLD_WIDTH - panelWidth - TowerDefenseGame.UI_MARGIN;
            float panelY = GameRenderer.STATS_PANEL_Y;

            drawStatsBackground(panelX, panelY, panelWidth, panelHeight);

        } catch (Exception e) {
            System.err.println("Error rendering tower stats panel: " + e.getMessage());
        }
    }

    private void drawStatsBackground(float panelX, float panelY, float panelWidth, float panelHeight) {
        try {
            game.getShapeRenderer().begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
            game.getShapeRenderer().setColor(0.2f, 0.2f, 0.3f, 0.9f);
            game.getShapeRenderer().rect(panelX, panelY, panelWidth, panelHeight);
        } catch (Exception e) {
            System.err.println("Error in shape renderer (filled background): " + e.getMessage());
        } finally {
            try {
                game.getShapeRenderer().end();
            } catch (Exception ignored) {
            }
        }

        try {
            game.getShapeRenderer().begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
            game.getShapeRenderer().setColor(1f, 1f, 1f, 1f);
            game.getShapeRenderer().rect(panelX, panelY, panelWidth, panelHeight);
        } catch (Exception e) {
            System.err.println("Error in shape renderer (outline): " + e.getMessage());
        } finally {
            try {
                game.getShapeRenderer().end();
            } catch (Exception ignored) {
            }
        }
    }

    private void drawStatsTextEntries(Tower selectedTower, float panelX, float panelY,
            float panelHeight) {
        game.getBatch().setProjectionMatrix(game.getCamera().combined);

        float startY = panelY + panelHeight - 20;
        float lineSpacing = 25f;

        game.getFont().setColor(1f, 1f, 0f, 1f);
        game.getFont().draw(game.getBatch(), "Tower Stats", panelX + 10, startY);

        game.getFont().setColor(1f, 1f, 1f, 1f);
        startY -= lineSpacing;

        GameConfig.UpgradeConfig upgrades = game.getGameConfig() != null ? game.getGameConfig().getUpgrades() : null;
        int damageCost = upgrades != null ? upgrades.getDamageCost() : 0;
        int rangeCost = upgrades != null ? upgrades.getRangeCost() : 0;
        int cooldownCost = upgrades != null ? upgrades.getCooldownCost() : 0;

        String damageText = "Damage: " + selectedTower.getDamage();
        boolean canAffordDamage = game.getBudgetManager().canAfford(damageCost);
        drawStatEntry(damageText, " [$" + damageCost + "]" + (canAffordDamage ? " +" : ""),
                panelX + 10, startY, canAffordDamage, game.getDamageTextBounds());
        startY -= lineSpacing;

        String rangeText = "Range: " + selectedTower.getRange();
        boolean canAffordRange = game.getBudgetManager().canAfford(rangeCost);
        drawStatEntry(rangeText, " [$" + rangeCost + "]" + (canAffordRange ? " +" : ""),
                panelX + 10, startY, canAffordRange, game.getRangeTextBounds());
        startY -= lineSpacing;

        String cooldownText = "Cooldown: " + String.format("%.2f", selectedTower.getBaseAttackCooldown()) + "s";
        boolean canAffordCooldown = game.getBudgetManager().canAfford(cooldownCost);
        drawStatEntry(cooldownText, " [$" + cooldownCost + "]" + (canAffordCooldown ? " +" : ""),
                panelX + 10, startY, canAffordCooldown, game.getCooldownTextBounds());

        startY -= lineSpacing;
        game.getFont().setColor(1f, 1f, 1f, 1f);
        game.getFont().draw(game.getBatch(), "ID: " + selectedTower.getTowerId(), panelX + 10, startY);

        // Display targeting strategy with click-to-change
        startY -= lineSpacing;
        String strategyName = getStrategyName(selectedTower.getTargetingStrategy());
        drawStatEntry("Target: " + strategyName, " [click]", panelX + 10, startY, true, game.getStrategyTextBounds());

    }

    private String getStrategyName(com.gamedev.towerdefense.model.TargetingStrategy strategy) {
        if (strategy instanceof com.gamedev.towerdefense.model.NearestEnemyStrategy) {
            return "Nearest";
        } else if (strategy instanceof com.gamedev.towerdefense.model.StrongestEnemyStrategy) {
            return "Strongest";
        } else if (strategy instanceof com.gamedev.towerdefense.model.WeakestEnemyStrategy) {
            return "Weakest";
        }
        return "Unknown";
    }

    private void drawStatEntry(String labelText, String costText, float x, float y, boolean canAfford,
            com.badlogic.gdx.math.Rectangle bounds) {
        game.getFont().setColor(1f, 1f, 1f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout labelLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), labelText);
        game.getFont().draw(game.getBatch(), labelText, x, y);

        game.getFont().setColor(canAfford ? 0.4f : 1f, canAfford ? 1f : 0.4f, 0.4f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout costLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), costText);
        game.getFont().draw(game.getBatch(), costText, x + labelLayout.width, y);

        bounds.width = labelLayout.width + costLayout.width;
        bounds.height = Math.max(labelLayout.height, costLayout.height);
        bounds.x = x;
        bounds.y = y - bounds.height;
    }
}
