package com.gamedev.towerdefense.util;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnimationManagerTest {

    @BeforeAll
    public static void initGdx() {
        if (com.badlogic.gdx.Gdx.app == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            new HeadlessApplication(new com.badlogic.gdx.ApplicationAdapter() {
                @Override
                public void create() {}
            }, config);
        }
    }

    @Test
    public void testConstructor_ValidTexture() {
        Texture texture = new Texture(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        Texture texture = new Texture(128, 128, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        Texture texture = new Texture(128, 128, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        Texture texture = new Texture(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        Texture texture = new Texture(128, 256, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        Texture texture = new Texture(128, 128, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        Texture texture = new Texture(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
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
        Texture texture = new Texture(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 2, 2, 0.2f);
            
            assertFalse(manager.isAnimationFinished(0.0f));
        } finally {
            texture.dispose();
        }
    }

    @Test
    public void testGetAnimationDuration() {
        Texture texture = new Texture(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        try {
            AnimationManager manager = new AnimationManager(texture, 32, 32, 2, 2, 0.2f);
            
            float duration = manager.getAnimationDuration();
            assertTrue(duration > 0);
        } finally {
            texture.dispose();
        }
    }
}

