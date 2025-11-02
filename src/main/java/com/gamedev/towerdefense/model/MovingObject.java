package com.gamedev.towerdefense.model;

public abstract class MovingObject {
    protected Position position;
    protected Position targetPosition;
    protected float speed;
    protected boolean hasReachedTarget;

    protected MovingObject(Position startPos, Position targetPos, float speed) {
        this.position = new Position(startPos.getX(), startPos.getY());
        this.targetPosition = new Position(targetPos.getX(), targetPos.getY());
        this.speed = speed;
        this.hasReachedTarget = false;
    }

    public void update(float deltaTime) {
        if (hasReachedTarget) {
            return;
        }

        updateTarget();

        float dx = targetPosition.getX() - position.getX();
        float dy = targetPosition.getY() - position.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < getHitThreshold()) {
            onReachTarget();
            hasReachedTarget = true;
        } else {
            float moveDistance = speed * deltaTime;
            float ratio = Math.min(moveDistance / distance, 1f);
            float newX = position.getX() + dx * ratio;
            float newY = position.getY() + dy * ratio;
            position = new Position(newX, newY);
        }
    }

    protected abstract void updateTarget();

    protected abstract float getHitThreshold();

    protected abstract void onReachTarget();

    public Position getPosition() {
        return position;
    }

    public boolean hasReachedTarget() {
        return hasReachedTarget;
    }
}
