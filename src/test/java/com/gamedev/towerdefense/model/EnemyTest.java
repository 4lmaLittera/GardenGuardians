package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

public class EnemyTest {

    @Test
    public void testConstructor() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));

        Enemy enemy = new Enemy(path, 100, 50.0f, 0, 10);

        assertEquals(100, enemy.getHealth());
        assertEquals(50.0f, enemy.getSpeed(), 0.001f);
        assertEquals(0, enemy.getPosition().getX(), 0.001f);
        assertEquals(0, enemy.getPosition().getY(), 0.001f);
    }

    @Test
    public void testGetPosition() {
        Path path = new LinearPath(Arrays.asList(new Position(50, 75)));
        Enemy enemy = new Enemy(path, 50, 30.0f, 0, 10);

        Position position = enemy.getPosition();
        assertEquals(50, position.getX(), 0.001f);
        assertEquals(75, position.getY(), 0.001f);
    }

    @Test
    public void testGetHealth() {
        Path path = new LinearPath(Arrays.asList(new Position(0, 0)));
        Enemy enemy = new Enemy(path, 75, 25.0f, 0, 10);

        assertEquals(75, enemy.getHealth());
    }

    @Test
    public void testGetSpeed() {
        Path path = new LinearPath(Arrays.asList(new Position(0, 0)));
        Enemy enemy = new Enemy(path, 50, 40.5f, 0, 10);

        assertEquals(40.5f, enemy.getSpeed(), 0.001f);
    }

    @Test
    public void testIsAlive_Healthy() {
        Path path = new LinearPath(Arrays.asList(new Position(0, 0)));
        Enemy enemy = new Enemy(path, 100, 50.0f, 0, 10);

        assertTrue(enemy.isAlive());
    }

    @Test
    public void testIsAlive_LowHealth() {
        Path path = new LinearPath(Arrays.asList(new Position(0, 0)));
        Enemy enemy = new Enemy(path, 1, 50.0f, 0, 10);

        assertTrue(enemy.isAlive());
    }

    @Test
    public void testIsAlive_Dead() {
        Path path = new LinearPath(Arrays.asList(new Position(0, 0)));
        Enemy enemy = new Enemy(path, 100, 50.0f, 0, 10);
        enemy.takeDamage(100);

        assertFalse(enemy.isAlive());
    }

    @Test
    public void testTakeDamage() {
        Path path = new LinearPath(Arrays.asList(new Position(0, 0)));
        Enemy enemy = new Enemy(path, 100, 50.0f, 0, 10);

        enemy.takeDamage(30);
        assertEquals(70, enemy.getHealth());

        enemy.takeDamage(20);
        assertEquals(50, enemy.getHealth());
    }

    @Test
    public void testTakeDamage_ExceedsHealth() {
        Path path = new LinearPath(Arrays.asList(new Position(0, 0)));
        Enemy enemy = new Enemy(path, 50, 50.0f, 0, 10);

        enemy.takeDamage(75);
        assertEquals(-25, enemy.getHealth());
        assertFalse(enemy.isAlive());
    }

    @Test
    public void testHasReachedEnd_False() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0),
                new Position(100, 100)));
        Enemy enemy = new Enemy(path, 100, 50.0f, 0, 10);

        assertFalse(enemy.hasReachedEnd());
    }

    @Test
    public void testHasReachedEnd_True() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
        Enemy enemy = new Enemy(path, 100, 50.0f, 2, 10); // Start beyond last waypoint

        assertTrue(enemy.hasReachedEnd());
    }

    @Test
    public void testUpdate_Movement() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10); // Speed 100, starting at (0,0)

        // Move for 0.5 seconds (deltaTime = 0.5)
        // Distance moved = 100 * 0.5 = 50 units
        enemy.update(0.5f);

        Position newPos = enemy.getPosition();
        assertEquals(50, newPos.getX(), 0.1f); // Should move 50 units toward (100, 0)
        assertEquals(0, newPos.getY(), 0.1f);
    }

    @Test
    public void testUpdate_ReachWaypoint() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0),
                new Position(100, 100)));
        // Path length = 100 + 100 = 200
        // Speed 100 will travel 100 units in 1 second, reaching first waypoint
        Enemy enemy = new Enemy(path, 100, 100.0f, 0, 10);

        // Move for 1 second, should reach first waypoint (100, 0)
        enemy.update(1.0f);

        Position newPos = enemy.getPosition();
        // With new path parameter approach, should be at or past first waypoint
        assertTrue(newPos.getX() >= 99.9f && newPos.getX() <= 101.0f); // Allow tolerance
        assertEquals(0, newPos.getY(), 0.1f);
        // Should not have reached end yet
        assertFalse(enemy.hasReachedEnd());
    }

    @Test
    public void testUpdate_ReachEnd() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
        Enemy enemy = new Enemy(path, 100, 200.0f, 0, 10);

        // Move past the end
        enemy.update(1.0f); // Reaches first waypoint
        enemy.update(1.0f); // Should advance past last waypoint

        assertTrue(enemy.hasReachedEnd());
    }

    @Test
    public void testUpdate_StopsAtEnd() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
        Enemy enemy = new Enemy(path, 100, 200.0f, 0, 10);

        enemy.update(1.0f); // Reach end
        Position posBefore = enemy.getPosition();

        enemy.update(1.0f); // Try to move after reaching end
        Position posAfter = enemy.getPosition();

        // Position should not change after reaching end
        assertEquals(posBefore.getX(), posAfter.getX(), 0.001f);
        assertEquals(posBefore.getY(), posAfter.getY(), 0.001f);
    }

    @Test
    public void testUpdate_DiagonalMovement() {
        Path path = new LinearPath(Arrays.asList(
                new Position(0, 0),
                new Position(100, 100)));
        Enemy enemy = new Enemy(path, 100, 141.42f, 0, 10); // Speed to cover diagonal distance

        // Move for 1 second toward diagonal target
        enemy.update(1.0f);

        Position newPos = enemy.getPosition();
        // Should be approximately at (100, 100) or very close
        assertTrue(newPos.getX() > 0);
        assertTrue(newPos.getY() > 0);
    }
}
