package com.gamedev.towerdefense.model;

public class Enemy {

    private Position position;
    private int health;
    private float speed;
    private Path path;
    private float pathProgress;
    private int reward;
    private float animationTime;
    private int directionRow;

    public Enemy(Path path, int health, float speed, int currentWaypointIndex, int reward) {
        this.path = path;
        this.health = health;
        this.speed = speed;
        this.reward = reward;
        this.animationTime = 0f;
        this.directionRow = 0;

        if (currentWaypointIndex <= 0) {
            this.pathProgress = 0f;
        } else if (currentWaypointIndex >= path.getWaypointCount()) {
            this.pathProgress = 1f;
        } else {
            this.pathProgress = (float) currentWaypointIndex / Math.max(1, path.getWaypointCount() - 1);
        }

        Position startPos = path.getPositionAt(this.pathProgress);
        this.position = new Position(startPos.getX(), startPos.getY());
    }

    public int getReward() {
        return reward;
    }

    public Position getPosition() {
        return position;
    }

    public int getHealth() {
        return health;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int amount) {
        this.health -= amount;
    }

    public boolean hasReachedEnd() {
        return pathProgress >= 1.0f;
    }

    public void update(float deltaTime) {
        animationTime += deltaTime;

        if (hasReachedEnd()) {
            return;
        }

        float pathLength = path.getPathLength();
        if (pathLength <= 0f) {
            return;
        }

        float moveDistance = speed * deltaTime;
        float progressDelta = moveDistance / pathLength;

        pathProgress = Math.min(1.0f, pathProgress + progressDelta);

        Position newPosition = path.getPositionAt(pathProgress);
        this.position = new Position(newPosition.getX(), newPosition.getY());

        float lookAheadT = Math.min(1.0f, pathProgress + 0.01f);
        Position lookAheadPos = path.getPositionAt(lookAheadT);
        float dx = lookAheadPos.getX() - position.getX();
        float dy = lookAheadPos.getY() - position.getY();

        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) {
            angle += 360;
        }
        directionRow = getDirectionRow(angle);
    }

    private int getDirectionRow(float angle) {
        if (angle >= 225 && angle < 315) {
            return 0;
        } else if (angle >= 135 && angle < 225) {
            return 1;
        } else if (angle >= 315 || angle < 45) {
            return 2;
        } else {
            return 3;
        }
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public int getDirectionRow() {
        return directionRow;
    }
}
