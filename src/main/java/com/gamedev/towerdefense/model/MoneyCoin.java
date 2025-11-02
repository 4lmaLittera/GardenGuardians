package com.gamedev.towerdefense.model;

public class MoneyCoin extends MovingObject {
    private int reward;

    public MoneyCoin(Position startPos, Position targetPos, float speed, int reward) {
        super(startPos, targetPos, speed);
        this.reward = reward;
    }

    @Override
    protected void updateTarget() {
    }

    @Override
    protected float getHitThreshold() {
        return 10f;
    }

    @Override
    protected void onReachTarget() {
    }

    public int getReward() {
        return reward;
    }
}
