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
    }

    @Test
    public void testGameStateEnum_AllValues() {
        GameState[] values = GameState.values();
        assertEquals(3, values.length);
        
        assertTrue(java.util.Arrays.asList(values).contains(GameState.PLAYING));
        assertTrue(java.util.Arrays.asList(values).contains(GameState.WON));
        assertTrue(java.util.Arrays.asList(values).contains(GameState.LOST));
    }
}

