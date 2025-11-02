package com.gamedev.towerdefense.model;

public class Enemy {
    private Position position;
    private int health;
    private float speed;
    private Path path;
    private int currentWaypointIndex;
    private int reward;
    private float animationTime;
    private int directionRow;

    public Enemy(Path path, int health, float speed, int currentWaypointIndex, int reward) {
        this.path = path;
        this.health = health;
        this.speed = speed;
        this.currentWaypointIndex = currentWaypointIndex;
        this.reward = reward;
        this.animationTime = 0f;
        this.directionRow = 0;
        Position firstWaypoint = path.getPoint(0);
        this.position = new Position(firstWaypoint.getX(), firstWaypoint.getY());
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
        return currentWaypointIndex >= path.getWaypointCount();
    }

    public void update(float deltaTime) {
        animationTime += deltaTime;

        if (hasReachedEnd()) {
            return;
        }

        while (currentWaypointIndex < path.getWaypointCount()) {
            Position targetWaypoint = path.getPoint(currentWaypointIndex);
            float dx = targetWaypoint.getX() - position.getX();
            float dy = targetWaypoint.getY() - position.getY();
            float distanceToTarget = (float) Math.sqrt(dx * dx + dy * dy);

            // If already at this waypoint, advance to next
            if (distanceToTarget < 0.01f) {
                currentWaypointIndex++;
                continue;
            }

            // Calculate direction angle and update direction row
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (angle < 0) {
                angle += 360;
            }
            directionRow = getDirectionRow(angle);

            // Move toward target
            float moveDistance = speed * deltaTime;

            if (moveDistance >= distanceToTarget) {
                position = new Position(targetWaypoint.getX(), targetWaypoint.getY());
                currentWaypointIndex++;
            } else {
                float ratio = moveDistance / distanceToTarget;
                float newX = position.getX() + dx * ratio;
                float newY = position.getY() + dy * ratio;
                position = new Position(newX, newY);
            }
            break; // Done moving for this frame
        }
    }

    private int getDirectionRow(float angle) {
        if (angle >= 225 && angle < 315) {
            return 0; // Down (225-315 degrees)
        } else if (angle >= 135 && angle < 225) {
            return 1; // Left (135-225 degrees)
        } else if (angle >= 315 || angle < 45) {
            return 2; // Right (315-360 and 0-45 degrees)
        } else {
            return 3; // Up (45-135 degrees)
        }
    }

    public float getAnimationTime() {
        return animationTime;
    }

    public int getDirectionRow() {
        return directionRow;
    }
}
