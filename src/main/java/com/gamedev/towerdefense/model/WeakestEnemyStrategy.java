package com.gamedev.towerdefense.model;

import java.util.List;

public class WeakestEnemyStrategy implements TargetingStrategy {
    
    @Override
    public Enemy selectTarget(Position towerPosition, int range, List<Enemy> enemies) {
        Enemy weakestEnemy = null;
        int lowestHealth = Integer.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            
            float distance = Position.distance(towerPosition, enemy.getPosition());
            if (distance <= range && enemy.getHealth() < lowestHealth) {
                weakestEnemy = enemy;
                lowestHealth = enemy.getHealth();
            }
        }
        
        return weakestEnemy;
    }
}
