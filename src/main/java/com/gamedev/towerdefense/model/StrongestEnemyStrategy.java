package com.gamedev.towerdefense.model;

import java.util.List;

public class StrongestEnemyStrategy implements TargetingStrategy {
    
    @Override
    public Enemy selectTarget(Position towerPosition, int range, List<Enemy> enemies) {
        Enemy strongestEnemy = null;
        int highestHealth = 0;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            
            float distance = Position.distance(towerPosition, enemy.getPosition());
            if (distance <= range && enemy.getHealth() > highestHealth) {
                strongestEnemy = enemy;
                highestHealth = enemy.getHealth();
            }
        }
        
        return strongestEnemy;
    }
}
