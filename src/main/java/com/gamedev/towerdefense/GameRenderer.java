package com.gamedev.towerdefense;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.gamedev.towerdefense.config.GameConfig;
import com.gamedev.towerdefense.model.CurvedPath;
import com.gamedev.towerdefense.model.Enemy;
import com.gamedev.towerdefense.model.MoneyCoin;
import com.gamedev.towerdefense.model.Path;
import com.gamedev.towerdefense.model.Position;
import com.gamedev.towerdefense.model.Projectile;
import com.gamedev.towerdefense.model.Tower;

public class GameRenderer {

    private final TowerDefenseGame game;

    public GameRenderer(TowerDefenseGame game) {
        this.game = game;
    }

    public void renderAll() {
        GameConfig gameConfig = game.getGameConfig();
        // Clear background
        if (gameConfig != null && gameConfig.getVisual() != null && gameConfig.getVisual().getBackgroundColor() != null) {
            GameConfig.ColorConfig bgColor = gameConfig.getVisual().getBackgroundColor();
            Gdx.gl.glClearColor(bgColor.getR(), bgColor.getG(), bgColor.getB(), bgColor.getA());
        } else {
            Gdx.gl.glClearColor(0.1f, 0.15f, 0.2f, 1f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.getCamera().update();

        drawBackground();

        drawSceneElements();
    }

    private void drawBackground() {
        if (game.getBatch() == null) {
            return;
        }
        game.getBatch().setProjectionMatrix(game.getCamera().combined);
        game.getBatch().begin();
        if (game.getBackgroundTexture() != null) {
            float worldWidth = game.getGameConfig() != null && game.getGameConfig().getWorldWidth() > 0
                    ? game.getGameConfig().getWorldWidth()
                    : TowerDefenseGame.WORLD_WIDTH;
            float worldHeight = game.getGameConfig() != null && game.getGameConfig().getWorldHeight() > 0
                    ? game.getGameConfig().getWorldHeight()
                    : TowerDefenseGame.WORLD_HEIGHT;
            game.getBatch().draw(game.getBackgroundTexture(), 0, 0, worldWidth, worldHeight);
        }
        game.getBatch().end();
    }

    private void drawSceneElements() {
        Path path = game.getPath();
        drawPath(path);
        drawWaypoints(path);
        drawEnemies();
        drawTowers();
        drawProjectiles();
        drawMoneyCoins();
        drawTowerPreview();
        drawUI();
        drawTowerStatsPanel();
    }

    private void drawPath(Path path) {
        if (path == null) {
            return;
        }
        try {
            game.getShapeRenderer().begin(ShapeType.Line);
            game.getShapeRenderer().setColor(1, 1, 1, 1);

            if (path instanceof CurvedPath) {
                int samples = 100;
                Position prev = path.getPositionAt(0f);
                for (int i = 1; i <= samples; i++) {
                    float t = (float) i / samples;
                    Position current = path.getPositionAt(t);
                    if (prev != null && current != null) {
                        game.getShapeRenderer().line(prev.getX(), prev.getY(), current.getX(), current.getY());
                    }
                    prev = current;
                }
            } else {
                List<Position> waypoints = path.getWaypoints();
                if (waypoints == null || waypoints.size() < 2) {
                    game.getShapeRenderer().end();
                    return;
                }
                for (int i = 0; i < waypoints.size() - 1; i++) {
                    Position start = waypoints.get(i);
                    Position end = waypoints.get(i + 1);
                    if (start != null && end != null) {
                        game.getShapeRenderer().line(start.getX(), start.getY(), end.getX(), end.getY());
                    }
                }
            }
            game.getShapeRenderer().end();
        } catch (Exception e) {
            System.err.println("Error rendering path: " + e.getMessage());
            try {
                game.getShapeRenderer().end();
            } catch (Exception ignored) {
            }
        }
    }

    private void drawWaypoints(Path path) {
        if (path == null) {
            return;
        }
        List<Position> waypoints = path.getWaypoints();
        if (waypoints == null || waypoints.isEmpty()) {
            return;
        }

        try {
            game.getShapeRenderer().begin(ShapeType.Filled);
            Position startPos = waypoints.get(0);
            if (startPos != null) {
                game.getShapeRenderer().setColor(0, 1, 0, 1);
                game.getShapeRenderer().circle(startPos.getX(), startPos.getY(), 10);
            }
            if (waypoints.size() > 1) {
                Position endPos = waypoints.get(waypoints.size() - 1);
                if (endPos != null) {
                    game.getShapeRenderer().setColor(1, 0, 0, 1);
                    game.getShapeRenderer().circle(endPos.getX(), endPos.getY(), 10);
                }
            }
            game.getShapeRenderer().end();
        } catch (Exception e) {
            System.err.println("Error rendering waypoints: " + e.getMessage());
            try {
                game.getShapeRenderer().end();
            } catch (Exception ignored) {
            }
        }
    }

    private void drawEnemies() {
        if (game.getEnemyAnimation() == null) {
            return;
        }
        game.getBatch().setProjectionMatrix(game.getCamera().combined);
        game.getBatch().begin();
        for (Enemy enemy : game.getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }
            Position enemyPos = enemy.getPosition();
            int directionRow = enemy.getDirectionRow();
            TextureRegion currentFrame = game.getEnemyAnimation().getFrame(enemy.getAnimationTime(), directionRow);
            game.getBatch().draw(currentFrame, enemyPos.getX() - 32, enemyPos.getY() - 32, 64, 64);
        }
        game.getBatch().end();
    }

    private Color getTowerColor(int towerId) {
        GameConfig cfg = game.getGameConfig();
        if (cfg == null || cfg.getTowerTypes() == null) {
            return new Color(0.8f, 0.8f, 0.2f, 1f);
        }
        for (GameConfig.TowerTypeConfig towerType : cfg.getTowerTypes()) {
            if (towerId == towerType.getId()) {
                GameConfig.ColorConfig colorConfig = towerType.getColor();
                if (colorConfig != null) {
                    return new Color(colorConfig.getR(), colorConfig.getG(), colorConfig.getB(), colorConfig.getA());
                }
            }
        }
        return new Color(0.8f, 0.8f, 0.2f, 1f);
    }

    private void drawTowers() {
        float rangeOpacity = game.getGameConfig() != null && game.getGameConfig().getVisual() != null
                ? game.getGameConfig().getVisual().getRangeCircleOpacity() : 0.3f;

        game.getShapeRenderer().begin(ShapeType.Line);
        game.getShapeRenderer().setColor(0.5f, 0.5f, 0.5f, rangeOpacity);
        for (Tower tower : game.getTowers()) {
            Position towerPos = tower.getPosition();
            game.getShapeRenderer().circle(towerPos.getX(), towerPos.getY(), tower.getRange());
        }
        game.getShapeRenderer().end();

        game.getShapeRenderer().begin(ShapeType.Filled);
        for (Tower tower : game.getTowers()) {
            Color towerColor = getTowerColor(tower.getTowerId());
            game.getShapeRenderer().setColor(towerColor.r, towerColor.g, towerColor.b, towerColor.a);
            Position towerPos = tower.getPosition();
            game.getShapeRenderer().rect(towerPos.getX() - 10, towerPos.getY() - 10, 20, 20);
        }
        game.getShapeRenderer().end();
    }

    private void drawProjectiles() {
        game.getShapeRenderer().begin(ShapeType.Filled);
        game.getShapeRenderer().setColor(1f, 1f, 0f, 1f);
        for (Projectile projectile : game.getProjectiles()) {
            Position projPos = projectile.getPosition();
            game.getShapeRenderer().circle(projPos.getX(), projPos.getY(), 5);
        }
        game.getShapeRenderer().end();
    }

    private void drawMoneyCoins() {
        game.getBatch().setProjectionMatrix(game.getCamera().combined);
        game.getBatch().begin();
        if (game.getCoinTexture() != null) {
            for (MoneyCoin coin : game.getMoneyCoins()) {
                Position coinPos = coin.getPosition();
                game.getBatch().draw(game.getCoinTexture(), coinPos.getX() - 16, coinPos.getY() - 16, 32, 32);
            }
        }
        game.getBatch().end();
    }

    private void drawTowerPreview() {
        try {
            if (game.getSelectedTowerType() == null || game.getGameState() != com.gamedev.towerdefense.model.GameState.PLAYING) {
                return;
            }

            if (game.getBudgetManager() == null || !game.getBudgetManager().canAfford(game.getSelectedTowerType().getCost())) {
                return;
            }

            int screenX = com.badlogic.gdx.Gdx.input.getX();
            int screenY = com.badlogic.gdx.Gdx.input.getY();
            com.badlogic.gdx.math.Vector2 worldCoords = new com.badlogic.gdx.math.Vector2(screenX, screenY);
            game.getViewport().unproject(worldCoords);

            float worldX = worldCoords.x;
            float worldY = worldCoords.y;

            boolean isValidPlacement = game.isValidTowerPlacement(worldX, worldY, game.getSelectedTowerType().getRange());

            Color towerColor = getTowerColor(game.getSelectedTowerType().getId());

            try {
                game.getShapeRenderer().begin(ShapeType.Line);
                float rangeOpacity = 0.5f;
                if (isValidPlacement) {
                    game.getShapeRenderer().setColor(towerColor.r, towerColor.g, towerColor.b, rangeOpacity);
                } else {
                    game.getShapeRenderer().setColor(1f, 0f, 0f, rangeOpacity);
                }
                game.getShapeRenderer().circle(worldX, worldY, game.getSelectedTowerType().getRange());
                game.getShapeRenderer().end();
            } catch (Exception e) {
                System.err.println("Error rendering range preview: " + e.getMessage());
                try {
                    game.getShapeRenderer().end();
                } catch (Exception ignored) {
                }
            }

            try {
                game.getShapeRenderer().begin(ShapeType.Filled);
                float previewOpacity = 0.6f;
                if (isValidPlacement) {
                    game.getShapeRenderer().setColor(towerColor.r, towerColor.g, towerColor.b, previewOpacity);
                } else {
                    game.getShapeRenderer().setColor(1f, 0f, 0f, previewOpacity);
                }
                game.getShapeRenderer().rect(worldX - 10, worldY - 10, 20, 20);
                game.getShapeRenderer().end();
            } catch (Exception e) {
                System.err.println("Error rendering tower preview: " + e.getMessage());
                try {
                    game.getShapeRenderer().end();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            System.err.println("Error rendering tower preview: " + e.getMessage());
        }
    }

    private void drawUI() {
        try {
            game.getBatch().setProjectionMatrix(game.getCamera().combined);
            game.getBatch().begin();

            game.getFont().setColor(1f, 1f, 1f, 1f);

            try {
                game.getFont().draw(game.getBatch(), "Budget: " + game.getBudgetManager().getBudget(), 10, TowerDefenseGame.WORLD_HEIGHT - 10);
                game.getFont().draw(game.getBatch(), "Lives: " + game.getLives(), 10, TowerDefenseGame.WORLD_HEIGHT - 50);
            } catch (Exception e) {
                System.err.println("Error rendering budget/lives: " + e.getMessage());
            }

            if (game.getGameConfig() != null && game.getGameConfig().getTowerTypes() != null) {
                try {
                    float startY = TowerDefenseGame.WORLD_HEIGHT - 100;
                    float spacing = 20f;

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

            try {
                if (game.getWaveManager() != null) {
                    int currentWave = game.getWaveManager().getCurrentWaveNumber();
                    int totalWaves = game.getWaveManager().getTotalWaves();
                    if (game.getWaveManager().areAllWavesComplete()) {
                        game.getFont().draw(game.getBatch(), "Wave: " + currentWave + "/" + totalWaves + " (Complete)", 10,
                                TowerDefenseGame.WORLD_HEIGHT - 30);
                    } else {
                        game.getFont().draw(game.getBatch(), "Wave: " + currentWave + "/" + totalWaves, 10, TowerDefenseGame.WORLD_HEIGHT - 30);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error rendering wave info: " + e.getMessage());
            }

            try {
                if (game.getGameState() == com.gamedev.towerdefense.model.GameState.WON) {
                    String winText = "YOU WIN!";
                    float textWidth = game.getFont().getData().getGlyph('A').width * winText.length();
                    game.getFont().draw(game.getBatch(), winText, TowerDefenseGame.WORLD_WIDTH / 2 - textWidth / 2, TowerDefenseGame.WORLD_HEIGHT / 2);
                } else if (game.getGameState() == com.gamedev.towerdefense.model.GameState.LOST) {
                    String loseText = "GAME OVER!";
                    float textWidth = game.getFont().getData().getGlyph('A').width * loseText.length();
                    game.getFont().draw(game.getBatch(), loseText, TowerDefenseGame.WORLD_WIDTH / 2 - textWidth / 2, TowerDefenseGame.WORLD_HEIGHT / 2);
                }
            } catch (Exception e) {
                System.err.println("Error rendering game state message: " + e.getMessage());
            }

            game.getBatch().end();
        } catch (Exception e) {
            System.err.println("Error rendering UI: " + e.getMessage());
            try {
                game.getBatch().end();
            } catch (Exception ignored) {
            }
        }
    }

    private void drawTowerStatsPanel() {
        Tower selectedTower = game.getSelectedTower();
        if (selectedTower == null) {
            return;
        }

        try {
            float panelX = TowerDefenseGame.WORLD_WIDTH - 250;
            float panelY = 100;
            float panelWidth = 230;
            float panelHeight = 200;

            game.getShapeRenderer().setProjectionMatrix(game.getCamera().combined);
            game.getShapeRenderer().begin(ShapeType.Filled);
            game.getShapeRenderer().setColor(0.2f, 0.2f, 0.3f, 0.9f);
            game.getShapeRenderer().rect(panelX, panelY, panelWidth, panelHeight);
            game.getShapeRenderer().end();

            game.getShapeRenderer().begin(ShapeType.Line);
            game.getShapeRenderer().setColor(1f, 1f, 1f, 1f);
            game.getShapeRenderer().rect(panelX, panelY, panelWidth, panelHeight);
            game.getShapeRenderer().end();

            game.getBatch().setProjectionMatrix(game.getCamera().combined);
            game.getBatch().begin();

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
            String damageCostText = " [$" + damageCost + "]" + (canAffordDamage ? " +" : "");
            game.getFont().setColor(1f, 1f, 1f, 1f);
            com.badlogic.gdx.graphics.g2d.GlyphLayout damageLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), damageText);
            float damageX = panelX + 10;
            game.getFont().draw(game.getBatch(), damageText, damageX, startY);

            game.getFont().setColor(canAffordDamage ? 0.4f : 1f, canAffordDamage ? 1f : 0.4f, 0.4f, 1f);
            com.badlogic.gdx.graphics.g2d.GlyphLayout damageCostLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), damageCostText);
            game.getFont().draw(game.getBatch(), damageCostText, damageX + damageLayout.width, startY);

            game.getDamageTextBounds().width = damageLayout.width + damageCostLayout.width;
            game.getDamageTextBounds().height = Math.max(damageLayout.height, damageCostLayout.height);
            game.getDamageTextBounds().x = damageX;
            game.getDamageTextBounds().y = startY - game.getDamageTextBounds().height;

            startY -= lineSpacing;

            String rangeText = "Range: " + selectedTower.getRange();
            boolean canAffordRange = game.getBudgetManager().canAfford(rangeCost);
            String rangeCostText = " [$" + rangeCost + "]" + (canAffordRange ? " +" : "");
            game.getFont().setColor(1f, 1f, 1f, 1f);
            com.badlogic.gdx.graphics.g2d.GlyphLayout rangeLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), rangeText);
            float rangeX = panelX + 10;
            game.getFont().draw(game.getBatch(), rangeText, rangeX, startY);

            game.getFont().setColor(canAffordRange ? 0.4f : 1f, canAffordRange ? 1f : 0.4f, 0.4f, 1f);
            com.badlogic.gdx.graphics.g2d.GlyphLayout rangeCostLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), rangeCostText);
            game.getFont().draw(game.getBatch(), rangeCostText, rangeX + rangeLayout.width, startY);

            game.getRangeTextBounds().width = rangeLayout.width + rangeCostLayout.width;
            game.getRangeTextBounds().height = Math.max(rangeLayout.height, rangeCostLayout.height);
            game.getRangeTextBounds().x = rangeX;
            game.getRangeTextBounds().y = startY - game.getRangeTextBounds().height;

            startY -= lineSpacing;

            String cooldownText = "Cooldown: " + String.format("%.2f", selectedTower.getBaseAttackCooldown()) + "s";
            boolean canAffordCooldown = game.getBudgetManager().canAfford(cooldownCost);
            String cooldownCostText = " [$" + cooldownCost + "]" + (canAffordCooldown ? " +" : "");
            game.getFont().setColor(1f, 1f, 1f, 1f);
            com.badlogic.gdx.graphics.g2d.GlyphLayout cooldownLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), cooldownText);
            float cooldownX = panelX + 10;
            game.getFont().draw(game.getBatch(), cooldownText, cooldownX, startY);

            game.getFont().setColor(canAffordCooldown ? 0.4f : 1f, canAffordCooldown ? 1f : 0.4f, 0.4f, 1f);
            com.badlogic.gdx.graphics.g2d.GlyphLayout cooldownCostLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(game.getFont(), cooldownCostText);
            game.getFont().draw(game.getBatch(), cooldownCostText, cooldownX + cooldownLayout.width, startY);

            game.getCooldownTextBounds().width = cooldownLayout.width + cooldownCostLayout.width;
            game.getCooldownTextBounds().height = Math.max(cooldownLayout.height, cooldownCostLayout.height);
            game.getCooldownTextBounds().x = cooldownX;
            game.getCooldownTextBounds().y = startY - game.getCooldownTextBounds().height;

            startY -= lineSpacing;
            game.getFont().setColor(1f, 1f, 1f, 1f);
            game.getFont().draw(game.getBatch(), "ID: " + selectedTower.getTowerId(), panelX + 10, startY);

            game.getBatch().end();

        } catch (Exception e) {
            System.err.println("Error rendering tower stats panel: " + e.getMessage());
        }
    }
}
