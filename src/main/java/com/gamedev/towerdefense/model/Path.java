package com.gamedev.towerdefense.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path {
    private List<Position> positions;

    public Path(List<Position> positions) {
        this.positions = positions;
    }

    public Path(Position[] positions) {
        this.positions = Arrays.asList(positions);
    }

    public List<Position> getWaypoints() {
        return new ArrayList<>(positions);
    }

    public Position getPoint(int index) {
        return positions.get(index);
    }

    public float getPathLength() {
        if (positions.size() < 2) {
            return 0f;
        }
        float length = 0;
        for (int i = 0; i < positions.size() - 1; i++) {
            length += Position.distance(positions.get(i), positions.get(i + 1));
        }
        return length;
    }

    public int getWaypointCount() {
        return this.positions.size();
    }

}
