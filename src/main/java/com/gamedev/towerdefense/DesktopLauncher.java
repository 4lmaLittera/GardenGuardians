package com.gamedev.towerdefense;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class DesktopLauncher {

    private DesktopLauncher() {
        // Utility class
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Garden Defense");
        config.setWindowedMode(TowerDefenseGame.WORLD_WIDTH, TowerDefenseGame.WORLD_HEIGHT);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new TowerDefenseGame(), config);
    }
}
