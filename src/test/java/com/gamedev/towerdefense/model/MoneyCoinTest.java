package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyCoinTest {

    @Test
    public void testConstructor() {
        Position startPos = new Position(50, 50);
        Position targetPos = new Position(100, 100);
        MoneyCoin coin = new MoneyCoin(startPos, targetPos, 100f, 25);

        Position coinPos = coin.getPosition();
        assertEquals(50, coinPos.getX(), 0.001f);
        assertEquals(50, coinPos.getY(), 0.001f);
        assertEquals(25, coin.getReward());
    }

    @Test
    public void testGetReward() {
        Position startPos = new Position(0, 0);
        Position targetPos = new Position(100, 100);
        MoneyCoin coin = new MoneyCoin(startPos, targetPos, 50f, 69);

        assertEquals(69, coin.getReward());
    }

    @Test
    public void testUpdate_MovesTowardTarget() {
        Position startPos = new Position(0, 0);
        Position targetPos = new Position(100, 0);
        MoneyCoin coin = new MoneyCoin(startPos, targetPos, 100f, 10);

        Position initialPos = coin.getPosition();
        assertEquals(0, initialPos.getX(), 0.001f);

        coin.update(0.5f);

        Position newPos = coin.getPosition();
        assertTrue(newPos.getX() > 0);
        assertFalse(coin.hasReachedTarget());
    }

    @Test
    public void testUpdate_ReachesTarget() {
        Position startPos = new Position(0, 0);
        Position targetPos = new Position(100, 0);
        MoneyCoin coin = new MoneyCoin(startPos, targetPos, 200f, 10);

        assertFalse(coin.hasReachedTarget());

        int updates = 0;
        while (!coin.hasReachedTarget() && updates < 100) {
            coin.update(0.1f);
            updates++;
        }

        assertTrue(coin.hasReachedTarget());
    }

    @Test
    public void testUpdate_MovesDiagonal() {
        Position startPos = new Position(0, 0);
        Position targetPos = new Position(100, 100);
        MoneyCoin coin = new MoneyCoin(startPos, targetPos, 100f, 10);

        coin.update(0.5f);

        Position newPos = coin.getPosition();
        assertTrue(newPos.getX() > 0);
        assertTrue(newPos.getY() > 0);
    }

    @Test
    public void testHasReachedTarget_InitiallyFalse() {
        Position startPos = new Position(0, 0);
        Position targetPos = new Position(100, 100);
        MoneyCoin coin = new MoneyCoin(startPos, targetPos, 50f, 10);

        assertFalse(coin.hasReachedTarget());
    }
}
