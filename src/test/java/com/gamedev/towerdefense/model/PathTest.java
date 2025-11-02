package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathTest {

    @Test
    public void testConstructorWithList() {
        List<Position> positions = new ArrayList<>();
        positions.add(new Position(0, 0));
        positions.add(new Position(100, 100));

        Path path = new Path(positions);

        assertEquals(2, path.getWaypointCount());
    }

    @Test
    public void testConstructorWithArray() {
        Position[] positions = {
                new Position(0, 0),
                new Position(100, 0),
                new Position(100, 100)
        };

        Path path = new Path(positions);

        assertEquals(3, path.getWaypointCount());
    }

    @Test
    public void testGetWaypoints() {
        Position pos1 = new Position(0, 0);
        Position pos2 = new Position(100, 100);
        List<Position> positions = Arrays.asList(pos1, pos2);

        Path path = new Path(positions);
        List<Position> waypoints = path.getWaypoints();

        assertEquals(2, waypoints.size());
        assertEquals(pos1.getX(), waypoints.get(0).getX());
        assertEquals(pos2.getY(), waypoints.get(1).getY());

        // Verify it returns a copy (modifying returned list shouldn't affect original)
        waypoints.clear();
        assertEquals(2, path.getWaypointCount());
    }

    @Test
    public void testGetPoint() {
        Position pos1 = new Position(10, 20);
        Position pos2 = new Position(30, 40);
        Path path = new Path(Arrays.asList(pos1, pos2));

        Position retrieved = path.getPoint(0);
        assertEquals(10, retrieved.getX());
        assertEquals(20, retrieved.getY());

        retrieved = path.getPoint(1);
        assertEquals(30, retrieved.getX());
        assertEquals(40, retrieved.getY());
    }

    @Test
    public void testGetWaypointCount() {
        Path path1 = new Path(Arrays.asList(new Position(0, 0)));
        assertEquals(1, path1.getWaypointCount());

        Path path2 = new Path(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0),
                new Position(100, 100),
                new Position(0, 100)));
        assertEquals(4, path2.getWaypointCount());
    }

    @Test
    public void testGetPathLength_SinglePoint() {
        Path path = new Path(Arrays.asList(new Position(0, 0)));
        assertEquals(0f, path.getPathLength(), 0.001f);
    }

    @Test
    public void testGetPathLength_TwoPoints() {
        // Distance from (0,0) to (100, 0) = 100
        Path path = new Path(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0)));
        assertEquals(100f, path.getPathLength(), 0.001f);
    }

    @Test
    public void testGetPathLength_MultiplePoints() {
        // (0,0) to (100, 0) = 100
        // (100, 0) to (100, 100) = 100
        // Total = 200
        Path path = new Path(Arrays.asList(
                new Position(0, 0),
                new Position(100, 0),
                new Position(100, 100)));
        assertEquals(200f, path.getPathLength(), 0.001f);
    }

    @Test
    public void testGetPathLength_Diagonal() {
        // Distance from (0,0) to (3,4) = 5 (3-4-5 triangle)
        Path path = new Path(Arrays.asList(
                new Position(0, 0),
                new Position(3, 4)));
        assertEquals(5f, path.getPathLength(), 0.001f);
    }

    @Test
    public void testGetPathLength_EmptyPath() {
        Path path = new Path(new ArrayList<>());
        assertEquals(0f, path.getPathLength(), 0.001f);
    }
}
