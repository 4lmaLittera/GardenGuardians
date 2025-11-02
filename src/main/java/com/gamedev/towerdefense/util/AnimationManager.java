package com.gamedev.towerdefense.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

@SuppressWarnings("unchecked")
public class AnimationManager {
    private Animation<TextureRegion>[] animations;

    public AnimationManager(Texture texture, int frameWidth, int frameHeight, int cols, int rows, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(texture, frameWidth, frameHeight);

        int actualRows = tmp.length;
        int actualCols = actualRows > 0 ? tmp[0].length : 0;

        animations = new Animation[actualRows];

        for (int i = 0; i < actualRows; i++) {
            int framesInRow = Math.min(cols, actualCols);
            TextureRegion[] frames = new TextureRegion[framesInRow];
            for (int j = 0; j < framesInRow; j++) {
                frames[j] = tmp[i][j];
            }
            Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
            animations[i] = anim;
        }
    }

    public TextureRegion getFrame(float stateTime) {
        if (animations == null || animations.length == 0) {
            return null;
        }
        return animations[0].getKeyFrame(stateTime);
    }

    public TextureRegion getFrame(float stateTime, int rowIndex) {
        if (animations == null || animations.length == 0) {
            return null;
        }
        int row = Math.max(0, Math.min(rowIndex, animations.length - 1));
        return animations[row].getKeyFrame(stateTime);
    }

    public boolean isAnimationFinished(float stateTime) {
        if (animations == null || animations.length == 0) {
            return true;
        }
        return animations[0].isAnimationFinished(stateTime);
    }

    public float getAnimationDuration() {
        if (animations == null || animations.length == 0) {
            return 0;
        }
        return animations[0].getAnimationDuration();
    }

    public int getFrameCount() {
        if (animations == null || animations.length == 0) {
            return 0;
        }
        return animations[0].getKeyFrames().length;
    }

    public int getRowCount() {
        return animations != null ? animations.length : 0;
    }
}
