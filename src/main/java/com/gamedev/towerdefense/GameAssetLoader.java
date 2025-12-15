package com.gamedev.towerdefense;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gamedev.towerdefense.util.AnimationManager;

/**
 * Manages loading and disposal of game assets (textures, fonts, animations).
 * Extracted from TowerDefenseGame to follow Single Responsibility Principle.
 */
public class GameAssetLoader implements Disposable {

    // Asset paths
    private static final String BACKGROUND_PATH = "assets/images/grass_template2.jpg";
    private static final String ENEMY_PATH = "assets/images/BeetleMove.png";
    private static final String COIN_PATH = "assets/images/coin.png";

    // Enemy animation defaults
    private static final int ENEMY_FRAME_WIDTH = 32;
    private static final int ENEMY_FRAME_HEIGHT = 32;
    private static final int ENEMY_ANIM_COLS = 4;
    private static final int ENEMY_ANIM_ROWS = 4;
    private static final float ENEMY_FRAME_DURATION = 0.2f;

    // Loaded assets
    private Texture backgroundTexture;
    private Texture enemyTexture;
    private Texture coinTexture;
    private AnimationManager enemyAnimation;
    private BitmapFont font;

    /**
     * Loads all game assets. Should be called during game initialization.
     */
    public void loadAll() {
        loadBackgroundTexture();
        loadEnemyTexture();
        loadCoinTexture();
        loadFont();
    }

    private void loadBackgroundTexture() {
        try {
            backgroundTexture = new Texture(Gdx.files.internal(BACKGROUND_PATH));
        } catch (GdxRuntimeException e) {
            System.err.println("Failed to load background texture: " + e.getMessage());
            backgroundTexture = null;
        }
    }

    private void loadEnemyTexture() {
        try {
            enemyTexture = new Texture(Gdx.files.internal(ENEMY_PATH));
        } catch (GdxRuntimeException e) {
            System.err.println("Failed to load enemy texture: " + e.getMessage());
            enemyTexture = null;
        }

        if (enemyTexture != null) {
            try {
                enemyAnimation = new AnimationManager(
                    enemyTexture,
                    ENEMY_FRAME_WIDTH,
                    ENEMY_FRAME_HEIGHT,
                    ENEMY_ANIM_COLS,
                    ENEMY_ANIM_ROWS,
                    ENEMY_FRAME_DURATION
                );
            } catch (GdxRuntimeException e) {
                System.err.println("Failed to create enemy animation: " + e.getMessage());
                enemyAnimation = null;
            }
        }
    }

    private void loadCoinTexture() {
        try {
            coinTexture = new Texture(Gdx.files.internal(COIN_PATH));
        } catch (GdxRuntimeException e) {
            System.err.println("Failed to load coin texture: " + e.getMessage());
            coinTexture = null;
        }
    }

    private void loadFont() {
        font = new BitmapFont();
        font.getData().setScale(1f, 1f);
    }

    // --- Getters ---

    public Texture getBackgroundTexture() {
        return backgroundTexture;
    }

    public Texture getEnemyTexture() {
        return enemyTexture;
    }

    public Texture getCoinTexture() {
        return coinTexture;
    }

    public AnimationManager getEnemyAnimation() {
        return enemyAnimation;
    }

    public BitmapFont getFont() {
        return font;
    }

    @Override
    public void dispose() {
        if (backgroundTexture != null) {
            try {
                backgroundTexture.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing background texture: " + e.getMessage());
            }
        }

        if (enemyTexture != null) {
            try {
                enemyTexture.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing enemy texture: " + e.getMessage());
            }
        }

        if (coinTexture != null) {
            try {
                coinTexture.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing coin texture: " + e.getMessage());
            }
        }

        if (font != null) {
            try {
                font.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing font: " + e.getMessage());
            }
        }
    }
}
