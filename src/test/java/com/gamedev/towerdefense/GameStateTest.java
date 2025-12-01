package com.gamedev.towerdefense;

import com.gamedev.towerdefense.model.GameState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    @Test
    public void testGameStateEnum_Values() {
        assertNotNull(GameState.PLAYING);
        assertNotNull(GameState.WON);
        assertNotNull(GameState.LOST);
        assertNotNull(GameState.PAUSED);
    }

    @Test
    public void testGameStateEnum_AllValues() {
        GameState[] values = GameState.values();
        assertEquals(4, values.length);

        assertTrue(java.util.Arrays.asList(values).contains(GameState.PLAYING));
        assertTrue(java.util.Arrays.asList(values).contains(GameState.WON));
        assertTrue(java.util.Arrays.asList(values).contains(GameState.LOST));
        assertTrue(java.util.Arrays.asList(values).contains(GameState.PAUSED));
    }
}
