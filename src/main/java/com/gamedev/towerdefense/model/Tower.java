package com.gamedev.towerdefense.model;

import java.util.List;

public class Tower {
    private int cost;
    private int range;
    private int damage;
    private float attackCooldown;
    private float baseAttackCooldown;
    private float projectileSpeed;
    private Position position;

    private int towerId;

    public Tower(int cost, int range, int damage, float attackCooldown, float projectileSpeed,
            Position position, int towerId) {
        this.cost = cost;
        this.range = range;
        this.damage = damage;
        this.baseAttackCooldown = attackCooldown;
        this.attackCooldown = attackCooldown;
        this.projectileSpeed = projectileSpeed;
        this.position = position;
        this.towerId = towerId;
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

    public float getBaseAttackCooldown() {
        return baseAttackCooldown;
    }

    public int getTowerId() {
        return towerId;
    }

    public void increaseDamage(int damage) {
        this.damage += damage;
    }
    public void increaseRange(int range) {
        this.range += range;
    }
    public void decreaseAttackCooldown(float cooldownDecrease) {
        this.baseAttackCooldown -= cooldownDecrease;
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
            attackCooldown = baseAttackCooldown;

        }
    }
}
