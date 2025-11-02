package com.gamedev.towerdefense.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Path {
    protected List<Position> positions;

    protected Path(List<Position> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("Path must have at least one waypoint");
        }
        this.positions = new ArrayList<>(positions);
    }

    protected Path(Position[] positions) {
        if (positions == null || positions.length == 0) {
            throw new IllegalArgumentException("Path must have at least one waypoint");
        }
        this.positions = Arrays.asList(positions);
    }

    public abstract Position getPositionAt(float t);

    public abstract float getPathLength();

    public List<Position> getWaypoints() {
        return new ArrayList<>(positions);
    }

    public Position getPoint(int index) {
        if (index < 0 || index >= positions.size()) {
            throw new IndexOutOfBoundsException("Waypoint index out of bounds: " + index);
        }
        return positions.get(index);
    }

    public int getWaypointCount() {
        return this.positions.size();
    }
}
