package com.gamedev.towerdefense.model;

import java.util.List;

public class LinearPath extends Path {
    private float cachedLength;

    public LinearPath(List<Position> positions) {
        super(positions);
        calculateLength();
    }

    public LinearPath(Position[] positions) {
        super(positions);
        calculateLength();
    }

    private void calculateLength() {
        if (positions.size() < 2) {
            cachedLength = 0f;
            return;
        }
        cachedLength = 0f;
        for (int i = 0; i < positions.size() - 1; i++) {
            cachedLength += Position.distance(positions.get(i), positions.get(i + 1));
        }
    }

    @Override
    public Position getPositionAt(float t) {
        t = Math.max(0f, Math.min(1f, t));

        if (positions.size() == 1) {
            return new Position(positions.get(0).getX(), positions.get(0).getY());
        }

        if (t <= 0f) {
            return new Position(positions.get(0).getX(), positions.get(0).getY());
        }

        if (t >= 1f) {
            Position last = positions.get(positions.size() - 1);
            return new Position(last.getX(), last.getY());
        }

        float totalLength = cachedLength;
        float targetDistance = totalLength * t;

        float accumulatedDistance = 0f;
        for (int i = 0; i < positions.size() - 1; i++) {
            Position p1 = positions.get(i);
            Position p2 = positions.get(i + 1);
            float segmentLength = Position.distance(p1, p2);

            if (accumulatedDistance + segmentLength >= targetDistance) {
                float localT = (targetDistance - accumulatedDistance) / segmentLength;
                float x = p1.getX() + (p2.getX() - p1.getX()) * localT;
                float y = p1.getY() + (p2.getY() - p1.getY()) * localT;
                return new Position(x, y);
            }

            accumulatedDistance += segmentLength;
        }

        Position last = positions.get(positions.size() - 1);
        return new Position(last.getX(), last.getY());
    }

    @Override
    public float getPathLength() {
        return cachedLength;
    }
}
