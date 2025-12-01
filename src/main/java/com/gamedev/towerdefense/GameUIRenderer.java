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
            drawStatsTextEntries(selectedTower, panelX, panelY, panelHeight);

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

        drawDamageEntry(selectedTower, panelX, startY, damageCost);
        startY -= lineSpacing;
        drawRangeEntry(selectedTower, panelX, startY, rangeCost);
        startY -= lineSpacing;
        drawCooldownEntry(selectedTower, panelX, startY, cooldownCost);

        startY -= lineSpacing;
        game.getFont().setColor(1f, 1f, 1f, 1f);
        game.getFont().draw(game.getBatch(), "ID: " + selectedTower.getTowerId(), panelX + 10, startY);

        // batch begin/end handled by caller (GameRenderer.withBatch)
    }

    private void drawDamageEntry(Tower selectedTower, float panelX, float y, int damageCost) {
        String damageText = "Damage: " + selectedTower.getDamage();
        boolean canAffordDamage = game.getBudgetManager().canAfford(damageCost);
        String damageCostText = " [$" + damageCost + "]" + (canAffordDamage ? " +" : "");

        game.getFont().setColor(1f, 1f, 1f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout damageLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), damageText);
        float damageX = panelX + 10;
        game.getFont().draw(game.getBatch(), damageText, damageX, y);

        game.getFont().setColor(canAffordDamage ? 0.4f : 1f, canAffordDamage ? 1f : 0.4f, 0.4f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout damageCostLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), damageCostText);
        game.getFont().draw(game.getBatch(), damageCostText, damageX + damageLayout.width, y);

        game.getDamageTextBounds().width = damageLayout.width + damageCostLayout.width;
        game.getDamageTextBounds().height = Math.max(damageLayout.height, damageCostLayout.height);
        game.getDamageTextBounds().x = damageX;
        game.getDamageTextBounds().y = y - game.getDamageTextBounds().height;
    }

    private void drawRangeEntry(Tower selectedTower, float panelX, float y, int rangeCost) {
        String rangeText = "Range: " + selectedTower.getRange();
        boolean canAffordRange = game.getBudgetManager().canAfford(rangeCost);
        String rangeCostText = " [$" + rangeCost + "]" + (canAffordRange ? " +" : "");

        game.getFont().setColor(1f, 1f, 1f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout rangeLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), rangeText);
        float rangeX = panelX + 10;
        game.getFont().draw(game.getBatch(), rangeText, rangeX, y);

        game.getFont().setColor(canAffordRange ? 0.4f : 1f, canAffordRange ? 1f : 0.4f, 0.4f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout rangeCostLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), rangeCostText);
        game.getFont().draw(game.getBatch(), rangeCostText, rangeX + rangeLayout.width, y);

        game.getRangeTextBounds().width = rangeLayout.width + rangeCostLayout.width;
        game.getRangeTextBounds().height = Math.max(rangeLayout.height, rangeCostLayout.height);
        game.getRangeTextBounds().x = rangeX;
        game.getRangeTextBounds().y = y - game.getRangeTextBounds().height;
    }

    private void drawCooldownEntry(Tower selectedTower, float panelX, float y, int cooldownCost) {
        String cooldownText = "Cooldown: " + String.format("%.2f", selectedTower.getBaseAttackCooldown()) + "s";
        boolean canAffordCooldown = game.getBudgetManager().canAfford(cooldownCost);
        String cooldownCostText = " [$" + cooldownCost + "]" + (canAffordCooldown ? " +" : "");

        game.getFont().setColor(1f, 1f, 1f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout cooldownLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), cooldownText);
        float cooldownX = panelX + 10;
        game.getFont().draw(game.getBatch(), cooldownText, cooldownX, y);

        game.getFont().setColor(canAffordCooldown ? 0.4f : 1f, canAffordCooldown ? 1f : 0.4f, 0.4f, 1f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout cooldownCostLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), cooldownCostText);
        game.getFont().draw(game.getBatch(), cooldownCostText, cooldownX + cooldownLayout.width, y);

        game.getCooldownTextBounds().width = cooldownLayout.width + cooldownCostLayout.width;
        game.getCooldownTextBounds().height = Math.max(cooldownLayout.height, cooldownCostLayout.height);
        game.getCooldownTextBounds().x = cooldownX;
        game.getCooldownTextBounds().y = y - game.getCooldownTextBounds().height;
    }
}
