package com.gamedev.towerdefense.model;

import java.util.List;


public interface TargetingStrategy {
    Enemy selectTarget(Position towerPosition, int range, List<Enemy> enemies);
}
