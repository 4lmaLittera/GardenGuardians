package com.gamedev.towerdefense;

import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    private static final float WAYPOINT_RADIUS = 10f;
    private static final float TOWER_SIZE = 20f;
    private static final float TOWER_HALF = TOWER_SIZE / 2f;
    private static final float ENEMY_DRAW_SIZE = 64f;
    private static final float ENEMY_HALF = ENEMY_DRAW_SIZE / 2f;
    private static final float PROJECTILE_RADIUS = 5f;
    private static final int CURVED_PATH_SAMPLES = 100;
    static final float STATS_PANEL_WIDTH = 230f;
    static final float STATS_PANEL_HEIGHT = 200f;
    static final float STATS_PANEL_Y = 100f;
    private static final float COIN_SIZE = 32f;
    static final float COIN_HALF = COIN_SIZE / 2f;
    static final float BUDGET_LIVES_LINE_GAP = 20f;
    static final float WAVE_INFO_OFFSET_Y = 40f;

    public GameRenderer(TowerDefenseGame game) {
        this.game = game;
        this.uiRenderer = new GameUIRenderer(game);
    }

    private final GameUIRenderer uiRenderer;

    private void withShapeRenderer(ShapeType type, Runnable r) {
        try {
            game.getShapeRenderer().begin(type);
            r.run();
        } catch (Exception e) {
            System.err.println("Error in shape renderer: " + e.getMessage());
        } finally {
            try {
                game.getShapeRenderer().end();
            } catch (Exception ignored) {
            }
        }
    }

    private void withBatch(Consumer<SpriteBatch> c) {
        if (game.getBatch() == null) {
            return;
        }
        try {
            game.getBatch().setProjectionMatrix(game.getCamera().combined);
            game.getBatch().begin();
            c.accept(game.getBatch());
        } catch (Exception e) {
            System.err.println("Error in batch rendering: " + e.getMessage());
        } finally {
            try {
                game.getBatch().end();
            } catch (Exception ignored) {
            }
        }
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
        uiRenderer.render();
    }

    private void drawPath(Path path) {
        if (path == null) {
            return;
        }
        game.getShapeRenderer().setColor(1, 1, 1, 1);
        if (path instanceof CurvedPath) {
            withShapeRenderer(ShapeType.Line, () -> {
                Position prev = path.getPositionAt(0f);
                for (int i = 1; i <= CURVED_PATH_SAMPLES; i++) {
                    float t = (float) i / CURVED_PATH_SAMPLES;
                    Position current = path.getPositionAt(t);
                    if (prev != null && current != null) {
                        game.getShapeRenderer().line(prev.getX(), prev.getY(), current.getX(), current.getY());
                    }
                    prev = current;
                }
            });
        } else {
            List<Position> waypoints = path.getWaypoints();
            if (waypoints == null || waypoints.size() < 2) {
                return;
            }
            withShapeRenderer(ShapeType.Line, () -> {
                for (int i = 0; i < waypoints.size() - 1; i++) {
                    Position start = waypoints.get(i);
                    Position end = waypoints.get(i + 1);
                    if (start != null && end != null) {
                        game.getShapeRenderer().line(start.getX(), start.getY(), end.getX(), end.getY());
                    }
                }
            });
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

        withShapeRenderer(ShapeType.Filled, () -> {
            Position startPos = waypoints.get(0);
            if (startPos != null) {
                game.getShapeRenderer().setColor(0, 1, 0, 1);
                game.getShapeRenderer().circle(startPos.getX(), startPos.getY(), WAYPOINT_RADIUS);
            }
            if (waypoints.size() > 1) {
                Position endPos = waypoints.get(waypoints.size() - 1);
                if (endPos != null) {
                    game.getShapeRenderer().setColor(1, 0, 0, 1);
                    game.getShapeRenderer().circle(endPos.getX(), endPos.getY(), WAYPOINT_RADIUS);
                }
            }
        });
    }

    private void drawEnemies() {
        if (game.getEnemyAnimation() == null) {
            return;
        }
        withBatch(batch -> {
            for (Enemy enemy : game.getEnemies()) {
                if (!enemy.isAlive()) {
                    continue;
                }
                Position enemyPos = enemy.getPosition();
                int directionRow = enemy.getDirectionRow();
                TextureRegion currentFrame = game.getEnemyAnimation().getFrame(enemy.getAnimationTime(), directionRow);
                batch.draw(currentFrame, enemyPos.getX() - ENEMY_HALF, enemyPos.getY() - ENEMY_HALF, ENEMY_DRAW_SIZE,
                        ENEMY_DRAW_SIZE);
            }
        });
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

        withShapeRenderer(ShapeType.Line, () -> {
            game.getShapeRenderer().setColor(0.5f, 0.5f, 0.5f, rangeOpacity);
            for (Tower tower : game.getTowers()) {
                Position towerPos = tower.getPosition();
                game.getShapeRenderer().circle(towerPos.getX(), towerPos.getY(), tower.getRange());
            }
        });

        withShapeRenderer(ShapeType.Filled, () -> {
            for (Tower tower : game.getTowers()) {
                Color towerColor = getTowerColor(tower.getTowerId());
                game.getShapeRenderer().setColor(towerColor.r, towerColor.g, towerColor.b, towerColor.a);
                Position towerPos = tower.getPosition();
                game.getShapeRenderer().rect(towerPos.getX() - TOWER_HALF, towerPos.getY() - TOWER_HALF, TOWER_SIZE,
                        TOWER_SIZE);
            }
        });
    }

    private void drawProjectiles() {
        withShapeRenderer(ShapeType.Filled, () -> {
            game.getShapeRenderer().setColor(1f, 1f, 0f, 1f);
            for (Projectile projectile : game.getProjectiles()) {
                Position projPos = projectile.getPosition();
                game.getShapeRenderer().circle(projPos.getX(), projPos.getY(), PROJECTILE_RADIUS);
            }
        });
    }

    private void drawMoneyCoins() {
        withBatch(batch -> {
            if (game.getCoinTexture() != null) {
                for (MoneyCoin coin : game.getMoneyCoins()) {
                    Position coinPos = coin.getPosition();
                    batch.draw(game.getCoinTexture(), coinPos.getX() - COIN_HALF, coinPos.getY() - COIN_HALF,
                            COIN_SIZE, COIN_SIZE);
                }
            }
        });
    }

    private void drawTowerPreview() {
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

        withShapeRenderer(ShapeType.Line, () -> {
            float rangeOpacity = 0.5f;
            if (isValidPlacement) {
                game.getShapeRenderer().setColor(towerColor.r, towerColor.g, towerColor.b, rangeOpacity);
            } else {
                game.getShapeRenderer().setColor(1f, 0f, 0f, rangeOpacity);
            }
            game.getShapeRenderer().circle(worldX, worldY, game.getSelectedTowerType().getRange());
        });

        withShapeRenderer(ShapeType.Filled, () -> {
            float previewOpacity = 0.6f;
            if (isValidPlacement) {
                game.getShapeRenderer().setColor(towerColor.r, towerColor.g, towerColor.b, previewOpacity);
            } else {
                game.getShapeRenderer().setColor(1f, 0f, 0f, previewOpacity);
            }
            game.getShapeRenderer().rect(worldX - TOWER_HALF, worldY - TOWER_HALF, TOWER_SIZE, TOWER_SIZE);
        });
    }

}
