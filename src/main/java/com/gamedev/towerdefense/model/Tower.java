package com.gamedev.towerdefense.model;

import java.util.List;

public class Tower {
    private int cost;
    private int range;
    private int damage;
    private float attackCooldown;
    private float projectileSpeed;
    private Position position;

    public Tower(int cost, int range, int damage, float attackCooldown, float projectileSpeed, Position position) {
        this.cost = cost;
        this.range = range;
        this.damage = damage;
        this.attackCooldown = attackCooldown;
        this.projectileSpeed = projectileSpeed;
        this.position = position;
    }

    public int getCost() {
        return cost;
    }

    public int getRange() {
        return range;
    }

    public int getDamage() {
        return damage;
    }

    public Position getPosition() {
        return position;
    }

    public float getAttackCooldowns() {
        return attackCooldown;
    }

    public Enemy getNearestEnemy(List<Enemy> enemies) {
        Enemy nearestEnemy = null;
        float nearestDistance = Float.MAX_VALUE;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }

            float distance = Position.distance(this.position, enemy.getPosition());
            if (distance <= range && distance < nearestDistance) {
                nearestEnemy = enemy;
                nearestDistance = distance;
            }
        }
        return nearestEnemy;
    }

    public void update(float deltaTime, List<Enemy> enemies, List<Projectile> projectiles) {
        attackCooldown -= deltaTime;

        Enemy target = getNearestEnemy(enemies);

        if (target != null && attackCooldown <= 0) {
            Projectile bullet = new Projectile(this.position, target, projectileSpeed, damage);
            projectiles.add(bullet);
            attackCooldown = 0.5f;
        }
    }
}
