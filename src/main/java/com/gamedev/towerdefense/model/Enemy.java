package com.gamedev.towerdefense.model;

public class Enemy {
    private Position position;
    private int health;
    private float speed;
    private Path path;
    private int currentWaypointIndex;

    public Enemy(Path path, int health, float speed, int currentWaypointIndex) {
        this.path = path;
        this.health = health;
        this.speed = speed;
        this.currentWaypointIndex = currentWaypointIndex;
        Position firstWaypoint = path.getPoint(0);
        this.position = new Position(firstWaypoint.getX(), firstWaypoint.getY());
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

}
