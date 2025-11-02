package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectileTest {

    private Path createTestPath(float x, float y) {
        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(x, y));
        waypoints.add(new Position(x + 100, y));
        return new LinearPath(waypoints);
    }

    @Test
    public void testConstructor() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(60, 60);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 300f, 50);

        Position projPos = projectile.getPosition();
        assertEquals(50, projPos.getX(), 0.001f);
        assertEquals(50, projPos.getY(), 0.001f);
        assertFalse(projectile.hasHit());
    }

    @Test
    public void testUpdate_MovesTowardTarget() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(100, 50);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        Position initialPos = projectile.getPosition();
        assertEquals(50, initialPos.getX(), 0.001f);
        assertEquals(50, initialPos.getY(), 0.001f);

        projectile.update(0.5f);

        Position newPos = projectile.getPosition();
        assertNotEquals(50, newPos.getX(), 0.001f);
        assertFalse(projectile.hasHit());
    }

    @Test
    public void testUpdate_HitsTargetWhenClose() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(52, 50);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        int initialHealth = enemy.getHealth();

        projectile.update(0.1f);

        assertTrue(projectile.hasHit());
        assertEquals(initialHealth - 50, enemy.getHealth());
    }

    @Test
    public void testUpdate_DealsDamageOnHit() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(53, 50);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 75);

        int initialHealth = enemy.getHealth();
        projectile.update(0.1f);

        assertEquals(initialHealth - 75, enemy.getHealth());
    }

    @Test
    public void testUpdate_MarksHitWhenTargetDead() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(60, 50);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);
        enemy.takeDamage(100);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        assertFalse(projectile.hasHit());
        projectile.update(0.1f);
        assertTrue(projectile.hasHit());
    }

    @Test
    public void testUpdate_DoesNotUpdateAfterHit() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(52, 50);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        projectile.update(0.1f);
        assertTrue(projectile.hasHit());

        Position hitPos = projectile.getPosition();
        projectile.update(0.5f);

        Position afterHitPos = projectile.getPosition();
        assertEquals(hitPos.getX(), afterHitPos.getX(), 0.001f);
        assertEquals(hitPos.getY(), afterHitPos.getY(), 0.001f);
    }

    @Test
    public void testUpdate_TracksMovingTarget() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(60, 50);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        float initialDistance = Position.distance(projectile.getPosition(), enemy.getPosition());

        enemy.update(0.1f);
        projectile.update(0.1f);

        float newDistance = Position.distance(projectile.getPosition(), enemy.getPosition());
        assertTrue(newDistance < initialDistance);
    }

    @Test
    public void testGetPosition() {
        Position startPos = new Position(75, 75);
        Path path = createTestPath(100, 100);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        Position pos = projectile.getPosition();
        assertEquals(75, pos.getX(), 0.001f);
        assertEquals(75, pos.getY(), 0.001f);
    }

    @Test
    public void testHasHit_InitiallyFalse() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(100, 100);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        assertFalse(projectile.hasHit());
    }

    @Test
    public void testUpdate_HitsTargetAtExactDistance() {
        Position startPos = new Position(50, 50);
        Path path = createTestPath(55, 50);
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);

        Projectile projectile = new Projectile(startPos, enemy, 100f, 50);

        assertFalse(projectile.hasHit());

        while (!projectile.hasHit()) {
            projectile.update(0.1f);
        }

        assertTrue(projectile.hasHit());
    }
}
