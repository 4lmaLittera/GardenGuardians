package com.gamedev.towerdefense.model;

public class Projectile extends MovingObject {
    private Enemy target;
    private int damage;

    public Projectile(Position startPos, Enemy target, float speed, int damage) {
        super(startPos, target.getPosition(), speed);
        this.target = target;
        this.damage = damage;
    }

    @Override
    protected void updateTarget() {
        if (target == null || !target.isAlive()) {
            hasReachedTarget = true;
            return;
        }
        targetPosition = target.getPosition();
    }

    @Override
    protected float getHitThreshold() {
        return 5f;
    }

    @Override
    protected void onReachTarget() {
        if (target != null && target.isAlive()) {
            target.takeDamage(damage);
        }
    }

    public boolean hasHit() {
        return hasReachedTarget;
    }
}
