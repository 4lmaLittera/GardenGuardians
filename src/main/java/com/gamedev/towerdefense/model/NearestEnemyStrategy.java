package com.gamedev.towerdefense.model;

import java.util.List;
public class NearestEnemyStrategy implements TargetingStrategy {
    
    @Override
    public Enemy selectTarget(Position towerPosition, int range, List<Enemy> enemies) {
        Enemy nearestEnemy = null;
        float nearestDistance = Float.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            
            float distance = Position.distance(towerPosition, enemy.getPosition());
            if (distance <= range && distance < nearestDistance) {
                nearestEnemy = enemy;
                nearestDistance = distance;
            }
        }
        
        return nearestEnemy;
    }
}
