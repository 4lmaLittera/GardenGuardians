package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

public class EnemyDirectionTest {

    private Path createSimplePath() {
        return new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
    }

    @Test
    public void testGetAnimationTime_Initial() {
        Path path = createSimplePath();
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        assertEquals(0.0f, enemy.getAnimationTime(), 0.001f);
    }

    @Test
    public void testGetAnimationTime_Increases() {
        Path path = createSimplePath();
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        enemy.update(0.5f);
        assertEquals(0.5f, enemy.getAnimationTime(), 0.001f);

        enemy.update(0.3f);
        assertEquals(0.8f, enemy.getAnimationTime(), 0.001f);
    }

    @Test
    public void testGetDirectionRow_MovingRight() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        enemy.update(0.1f);
        assertEquals(2, enemy.getDirectionRow());
    }

    @Test
    public void testGetDirectionRow_MovingLeft() {
        Path path = new LinearPath(Arrays.asList(
                new Position(100, 0),
                new Position(0, 0)));
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        enemy.update(0.1f);
        assertEquals(1, enemy.getDirectionRow());
    }

    @Test
    public void testGetDirectionRow_MovingUp() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(0, 100)));
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        enemy.update(0.1f);
        assertEquals(3, enemy.getDirectionRow());
    }

    @Test
    public void testGetDirectionRow_MovingDown() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 100),
                new Position(0, 0)));
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        enemy.update(0.1f);
        assertEquals(0, enemy.getDirectionRow());
    }

    @Test
    public void testGetDirectionRow_DiagonalUpRight() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 100)));
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        enemy.update(0.1f);
        int direction = enemy.getDirectionRow();
        assertTrue(direction == 2 || direction == 3);
    }

    @Test
    public void testGetDirectionRow_DiagonalDownLeft() {
        Path path = new LinearPath(Arrays.asList(
                new Position(100, 100),
                new Position(0, 0)));
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        enemy.update(0.1f);
        int direction = enemy.getDirectionRow();
        assertTrue(direction >= 0 && direction <= 3);
    }

    @Test
    public void testGetDirectionRow_InitialState() {
        Path path = createSimplePath();
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        assertEquals(0, enemy.getDirectionRow());
    }

    @Test
    public void testGetDirectionRow_ChangesWithMovement() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0),
                new Position(100, 100)));
        Enemy enemy = new Enemy(path, 100, 200.0f, 0, 10);

        enemy.update(1.0f);
        int dir1 = enemy.getDirectionRow();

        enemy.update(1.0f);
        int dir2 = enemy.getDirectionRow();

        assertTrue(dir1 == 2 || dir2 == 3);
    }

    @Test
    public void testGetDirectionRow_StaysAfterReachingEnd() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
        Enemy enemy = new Enemy(path, 100, 200.0f, 0, 10);

        enemy.update(1.0f);
        int directionBefore = enemy.getDirectionRow();

        enemy.update(1.0f);
        int directionAfter = enemy.getDirectionRow();

        assertEquals(directionBefore, directionAfter);
    }
}
