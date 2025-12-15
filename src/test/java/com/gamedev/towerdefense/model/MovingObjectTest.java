package com.gamedev.towerdefense.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MovingObjectTest {

    private static class TestMovingObject extends MovingObject {
        private boolean updateTargetCalled = false;
        private boolean onReachTargetCalled = false;

        public TestMovingObject(Position startPos, Position targetPos, float speed) {
            super(startPos, targetPos, speed);
        }

        @Override
        protected void updateTarget() {
            updateTargetCalled = true;
        }

        @Override
        protected float getHitThreshold() {
            return 2.0f; // Smaller threshold for easier testing
        }

        @Override
        protected void onReachTarget() {
            onReachTargetCalled = true;
        }

        public boolean wasUpdateTargetCalled() {
            return updateTargetCalled;
        }

        public boolean wasOnReachTargetCalled() {
            return onReachTargetCalled;
        }
    }

    @Test
    public void testConstructor() {
        Position start = new Position(0, 0);
        Position target = new Position(100, 0);
        TestMovingObject obj = new TestMovingObject(start, target, 50.0f);

        assertNotNull(obj.getPosition());
        assertEquals(0.0f, obj.getPosition().getX(), 0.001f);
        assertEquals(0.0f, obj.getPosition().getY(), 0.001f);
        assertFalse(obj.hasReachedTarget());
    }

    @Test
    public void testUpdate_MovesTowardTarget() {
        Position start = new Position(0, 0);
        Position target = new Position(100, 0);
        TestMovingObject obj = new TestMovingObject(start, target, 50.0f);

        obj.update(1.0f);

        Position newPos = obj.getPosition();
        assertTrue(newPos.getX() > 0);
        assertEquals(0.0f, newPos.getY(), 0.001f);
        assertTrue(obj.wasUpdateTargetCalled());
    }

    @Test
    public void testUpdate_ReachesTarget() {
        Position start = new Position(0, 0);
        Position target = new Position(10, 0);
        TestMovingObject obj = new TestMovingObject(start, target, 50.0f);

        // Update until target is reached (should happen in 1-2 updates)
        int maxUpdates = 10;
        for (int i = 0; i < maxUpdates && !obj.hasReachedTarget(); i++) {
            obj.update(0.2f); // 0.2s * 50 speed = 10 units per update
        }

        assertTrue(obj.hasReachedTarget(), "Object should reach target within " + maxUpdates + " updates");
        assertTrue(obj.wasOnReachTargetCalled(), "onReachTarget should be called");
    }

    @Test
    public void testUpdate_DoesNotMoveAfterReachingTarget() {
        Position start = new Position(0, 0);
        Position target = new Position(10, 0);
        TestMovingObject obj = new TestMovingObject(start, target, 100.0f);

        obj.update(1.0f);
        Position pos1 = obj.getPosition();

        obj.update(1.0f);
        Position pos2 = obj.getPosition();

        assertEquals(pos1.getX(), pos2.getX(), 0.001f);
        assertTrue(obj.hasReachedTarget());
    }

    @Test
    public void testUpdate_MovesPartialDistance() {
        Position start = new Position(0, 0);
        Position target = new Position(100, 0);
        TestMovingObject obj = new TestMovingObject(start, target, 30.0f);

        obj.update(1.0f);

        Position newPos = obj.getPosition();
        assertEquals(30.0f, newPos.getX(), 0.1f);
        assertFalse(obj.hasReachedTarget());
    }

    @Test
    public void testUpdate_DiagonalMovement() {
        Position start = new Position(0, 0);
        Position target = new Position(100, 100);
        TestMovingObject obj = new TestMovingObject(start, target, 141.42f);

        obj.update(1.0f);

        Position newPos = obj.getPosition();
        assertTrue(newPos.getX() > 0);
        assertTrue(newPos.getY() > 0);
    }

    @Test
    public void testGetPosition() {
        Position start = new Position(5, 10);
        Position target = new Position(100, 200);
        TestMovingObject obj = new TestMovingObject(start, target, 50.0f);

        Position pos = obj.getPosition();
        assertNotNull(pos);
        assertEquals(5.0f, pos.getX(), 0.001f);
        assertEquals(10.0f, pos.getY(), 0.001f);
    }

    @Test
    public void testHasReachedTarget_InitiallyFalse() {
        Position start = new Position(0, 0);
        Position target = new Position(100, 0);
        TestMovingObject obj = new TestMovingObject(start, target, 50.0f);

        assertFalse(obj.hasReachedTarget());
    }
}
