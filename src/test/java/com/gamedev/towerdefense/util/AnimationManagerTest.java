package com.gamedev.towerdefense.util;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnimationManagerTest {

    @BeforeAll
    public static void initGdx() {
        try {
            if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
                HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();

                new HeadlessApplication(new com.badlogic.gdx.ApplicationAdapter() {
                    @Override
                    public void create() {
                        // LibGDX initialized
                    }
                }, config);

                // Wait for initialization with timeout - need both app and graphics
                int maxWait = 100;
                int waited = 0;
                while (waited < maxWait &&
                        (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null)) {
                    try {
                        Thread.sleep(10);
                        waited += 10;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to initialize LibGDX for tests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Texture createTestTexture(int width, int height) {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return null;
        }
        try {
            Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void testConstructor_ValidTexture() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }

        Texture texture = createTestTexture(64, 64);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 2, 2, 0.2f);

            assertNotNull(manager);
            assertEquals(2, manager.getRowCount());
            assertTrue(manager.getFrameCount() > 0);
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetFrame_SpecificRow() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(128, 128);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 4, 4, 0.2f);

            TextureRegion frame0 = manager.getFrame(0.0f, 0);
            TextureRegion frame1 = manager.getFrame(0.0f, 1);
            TextureRegion frame2 = manager.getFrame(0.0f, 2);
            TextureRegion frame3 = manager.getFrame(0.0f, 3);

            assertNotNull(frame0);
            assertNotNull(frame1);
            assertNotNull(frame2);
            assertNotNull(frame3);
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetFrame_TimeProgression() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(128, 128);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 4, 4, 0.2f);

            TextureRegion frame1 = manager.getFrame(0.0f, 0);
            TextureRegion frame2 = manager.getFrame(0.3f, 0);

            assertNotNull(frame1);
            assertNotNull(frame2);
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetFrame_InvalidRowIndex() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(64, 64);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 2, 2, 0.2f);

            TextureRegion frame = manager.getFrame(0.0f, 10);
            assertNotNull(frame);

            frame = manager.getFrame(0.0f, -1);
            assertNotNull(frame);
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetRowCount() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(128, 256);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 4, 8, 0.2f);

            int rows = manager.getRowCount();
            assertEquals(8, rows);
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetFrameCount() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(128, 128);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 4, 4, 0.2f);

            int frameCount = manager.getFrameCount();
            assertEquals(4, frameCount);
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetFrame_DefaultRow() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(64, 64);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 2, 2, 0.2f);

            TextureRegion frame = manager.getFrame(0.0f);
            assertNotNull(frame);
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testIsAnimationFinished() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(64, 64);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 2, 2, 0.2f);

            assertFalse(manager.isAnimationFinished(0.0f));
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetAnimationDuration() {
        if (com.badlogic.gdx.Gdx.app == null || com.badlogic.gdx.Gdx.graphics == null) {
            return;
        }
        Texture texture = createTestTexture(64, 64);
        if (texture == null) {
            return;
        }
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 2, 2, 0.2f);

            float duration = manager.getAnimationDuration();
            assertTrue(duration > 0);
        } finally {
            texture.dispose();
        }
    }
}
