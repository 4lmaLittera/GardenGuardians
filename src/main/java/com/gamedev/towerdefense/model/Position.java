package com.gamedev.towerdefense.model;

public class Position {
    private float x;
    private float y;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public static float distance(Position a, Position b) {
        float dx = b.getX() - a.getX();
        float dy = b.getY() - a.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
