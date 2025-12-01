package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {

    @Test
    public void testConstructor() {
        Position pos = new Position(10.5f, 20.3f);
        assertEquals(10.5f, pos.getX());
        assertEquals(20.3f, pos.getY());
    }

    @Test
    public void testConstructor_NegativeValues() {
        Position pos = new Position(-10.0f, -20.0f);
        assertEquals(-10.0f, pos.getX());
        assertEquals(-20.0f, pos.getY());
    }

    @Test
    public void testConstructor_ZeroValues() {
        Position pos = new Position(0.0f, 0.0f);
        assertEquals(0.0f, pos.getX());
        assertEquals(0.0f, pos.getY());
    }

    @Test
    public void testGetX() {
        Position pos = new Position(5.0f, 10.0f);
        assertEquals(5.0f, pos.getX());
    }

    @Test
    public void testGetY() {
        Position pos = new Position(5.0f, 10.0f);
        assertEquals(10.0f, pos.getY());
    }

    @Test
    public void testDistance_Horizontal() {
        Position a = new Position(0.0f, 0.0f);
        Position b = new Position(10.0f, 0.0f);
        float distance = Position.distance(a, b);
        assertEquals(10.0f, distance, 0.001f);
    }

    @Test
    public void testDistance_Vertical() {
        Position a = new Position(0.0f, 0.0f);
        Position b = new Position(0.0f, 15.0f);
        float distance = Position.distance(a, b);
        assertEquals(15.0f, distance, 0.001f);
    }

    @Test
    public void testDistance_Diagonal() {
        Position a = new Position(0.0f, 0.0f);
        Position b = new Position(3.0f, 4.0f);
        float distance = Position.distance(a, b);
        assertEquals(5.0f, distance, 0.001f); // 3-4-5 triangle
    }

    @Test
    public void testDistance_SamePoint() {
        Position a = new Position(5.0f, 10.0f);
        Position b = new Position(5.0f, 10.0f);
        float distance = Position.distance(a, b);
        assertEquals(0.0f, distance, 0.001f);
    }

    @Test
    public void testDistance_NegativeCoordinates() {
        Position a = new Position(-5.0f, -10.0f);
        Position b = new Position(-2.0f, -6.0f);
        float distance = Position.distance(a, b);
        float expected = (float) Math.sqrt(9 + 16);
        assertEquals(expected, distance, 0.001f);
    }

    @Test
    public void testDistance_LargeValues() {
        Position a = new Position(1000.0f, 2000.0f);
        Position b = new Position(2000.0f, 4000.0f);
        float distance = Position.distance(a, b);
        float expected = (float) Math.sqrt(1000000 + 4000000);
        assertEquals(expected, distance, 0.1f);
    }

    @Test
    public void testDistance_Reversed() {
        Position a = new Position(0.0f, 0.0f);
        Position b = new Position(10.0f, 0.0f);
        float distance1 = Position.distance(a, b);
        float distance2 = Position.distance(b, a);
        assertEquals(distance1, distance2, 0.001f);
    }

    @Test
    public void testDistance_FloatPrecision() {
        Position a = new Position(0.1f, 0.2f);
        Position b = new Position(0.3f, 0.4f);
        float distance = Position.distance(a, b);
        float expected = (float) Math.sqrt(0.04 + 0.04);
        assertEquals(expected, distance, 0.001f);
    }
}
