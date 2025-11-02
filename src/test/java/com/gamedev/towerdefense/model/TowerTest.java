package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class TowerTest {

    @Test
    public void testConstructor() {
        Position position = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, position, "Test Tower");

        assertEquals(30, tower.getCost());
        assertEquals(100, tower.getRange());
        assertEquals(50, tower.getDamage());
        assertEquals(0.5f, tower.getAttackCooldowns());
        assertEquals(position, tower.getPosition());
        assertEquals("Test Tower", tower.getTowerName());
    }

    @Test
    public void testGetNearestEnemy_NoEnemies() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");
        List<Enemy> enemies = new ArrayList<>();

        assertNull(tower.getNearestEnemy(enemies));
    }

    @Test
    public void testGetNearestEnemy_EnemyInRange() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(60, 50));
        waypoints.add(new Position(100, 50));
        Path path = new Path(waypoints);

        List<Enemy> enemies = new ArrayList<>();
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);
        enemies.add(enemy);

        Enemy nearest = tower.getNearestEnemy(enemies);
        assertNotNull(nearest);
        assertEquals(enemy, nearest);
    }

    @Test
    public void testGetNearestEnemy_EnemyOutOfRange() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(200, 200));
        waypoints.add(new Position(250, 250));
        Path path = new Path(waypoints);

        List<Enemy> enemies = new ArrayList<>();
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);
        enemies.add(enemy);

        assertNull(tower.getNearestEnemy(enemies));
    }

    @Test
    public void testGetNearestEnemy_IgnoresDeadEnemies() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(60, 50));
        waypoints.add(new Position(100, 50));
        Path path = new Path(waypoints);

        List<Enemy> enemies = new ArrayList<>();
        Enemy deadEnemy = new Enemy(path, 100, 10.0f, 0, 10);
        deadEnemy.takeDamage(100);
        enemies.add(deadEnemy);

        assertNull(tower.getNearestEnemy(enemies));
    }

    @Test
    public void testGetNearestEnemy_FindsNearestWhenMultiple() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 200, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Enemy> enemies = new ArrayList<>();

        List<Position> farWaypoints = new ArrayList<>();
        farWaypoints.add(new Position(100, 50));
        farWaypoints.add(new Position(150, 50));
        Path farPath = new Path(farWaypoints);
        Enemy farEnemy = new Enemy(farPath, 100, 10.0f, 0, 10);
        enemies.add(farEnemy);

        List<Position> nearWaypoints = new ArrayList<>();
        nearWaypoints.add(new Position(55, 50));
        nearWaypoints.add(new Position(60, 50));
        Path nearPath = new Path(nearWaypoints);
        Enemy nearEnemy = new Enemy(nearPath, 100, 10.0f, 0, 10);
        enemies.add(nearEnemy);

        List<Position> middleWaypoints = new ArrayList<>();
        middleWaypoints.add(new Position(70, 50));
        middleWaypoints.add(new Position(80, 50));
        Path middlePath = new Path(middleWaypoints);
        Enemy middleEnemy = new Enemy(middlePath, 100, 10.0f, 0, 10);
        enemies.add(middleEnemy);

        Enemy nearest = tower.getNearestEnemy(enemies);
        assertNotNull(nearest);
        assertEquals(nearEnemy, nearest);
    }

    @Test
    public void testUpdate_CreatesProjectile() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(60, 50));
        waypoints.add(new Position(100, 50));
        Path path = new Path(waypoints);

        List<Enemy> enemies = new ArrayList<>();
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);
        enemies.add(enemy);

        List<Projectile> projectiles = new ArrayList<>();

        tower.update(0.6f, enemies, projectiles);

        assertEquals(1, projectiles.size());
        assertNotNull(projectiles.get(0));
    }

    @Test
    public void testUpdate_RespectsCooldown() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(60, 50));
        waypoints.add(new Position(100, 50));
        Path path = new Path(waypoints);

        List<Enemy> enemies = new ArrayList<>();
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);
        enemies.add(enemy);

        List<Projectile> projectiles = new ArrayList<>();

        tower.update(0.6f, enemies, projectiles);
        assertEquals(1, projectiles.size());

        projectiles.clear();
        tower.update(0.3f, enemies, projectiles);
        assertEquals(0, projectiles.size());

        tower.update(0.6f, enemies, projectiles);
        assertEquals(1, projectiles.size());
    }

    @Test
    public void testUpdate_NoProjectileWhenNoEnemies() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");
        List<Enemy> enemies = new ArrayList<>();
        List<Projectile> projectiles = new ArrayList<>();

        tower.update(1.0f, enemies, projectiles);
        assertEquals(0, projectiles.size());
    }

    @Test
    public void testUpdate_NoProjectileWhenEnemiesOutOfRange() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(200, 200));
        waypoints.add(new Position(250, 250));
        Path path = new Path(waypoints);

        List<Enemy> enemies = new ArrayList<>();
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);
        enemies.add(enemy);

        List<Projectile> projectiles = new ArrayList<>();
        tower.update(1.0f, enemies, projectiles);

        assertEquals(0, projectiles.size());
    }

    @Test
    public void testUpdate_ProjectileHasCorrectProperties() {
        Position towerPos = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, towerPos, "Test Tower");

        List<Position> waypoints = new ArrayList<>();
        waypoints.add(new Position(60, 50));
        waypoints.add(new Position(100, 50));
        Path path = new Path(waypoints);

        List<Enemy> enemies = new ArrayList<>();
        Enemy enemy = new Enemy(path, 100, 10.0f, 0, 10);
        enemies.add(enemy);

        List<Projectile> projectiles = new ArrayList<>();
        tower.update(0.6f, enemies, projectiles);

        assertEquals(1, projectiles.size());
        Projectile projectile = projectiles.get(0);

        Position projPos = projectile.getPosition();
        assertEquals(towerPos.getX(), projPos.getX(), 0.001f);
        assertEquals(towerPos.getY(), projPos.getY(), 0.001f);
        assertFalse(projectile.hasHit());
    }

    @Test
    public void testGetTowerName() {
        Position position = new Position(50, 50);
        Tower tower = new Tower(30, 100, 50, 0.5f, 300f, position, "Basic Bitch");

        assertEquals("Basic Bitch", tower.getTowerName());
    }

    @Test
    public void testGetTowerName_DifferentNames() {
        Position position = new Position(50, 50);
        Tower tower1 = new Tower(30, 100, 50, 0.5f, 300f, position, "Basic Bitch");
        Tower tower2 = new Tower(50, 50, 80, 1.5f, 300f, position, "Gorlock The Destroyer");

        assertEquals("Basic Bitch", tower1.getTowerName());
        assertEquals("Gorlock The Destroyer", tower2.getTowerName());
    }
}
