package com.gamedev.towerdefense.model;

import java.util.ArrayList;
import java.util.List;

public class CurvedPath extends Path {
    private static final float DEFAULT_ALPHA = 0.5f;
    private static final int SAMPLES_PER_SEGMENT = 100;
    private float alpha;
    private List<Float> segmentLengths;
    private List<List<ArcLengthSample>> arcLengthTables;
    private float cachedLength;

    private static class ArcLengthSample {
        float t;
        float distance;

        ArcLengthSample(float t, float distance) {
            this.t = t;
            this.distance = distance;
        }
    }

    public CurvedPath(List<Position> positions) {
        super(positions);
        this.alpha = DEFAULT_ALPHA;
        calculateSegmentLengths();
    }

    public CurvedPath(Position[] positions) {
        super(positions);
        this.alpha = DEFAULT_ALPHA;
        calculateSegmentLengths();
    }

    public CurvedPath(List<Position> positions, float alpha) {
        super(positions);
        this.alpha = alpha;
        calculateSegmentLengths();
    }

    private void calculateSegmentLengths() {
        segmentLengths = new ArrayList<>();
        arcLengthTables = new ArrayList<>();
        cachedLength = 0f;

        if (positions.size() < 2) {
            return;
        }

        for (int i = 0; i < positions.size() - 1; i++) {
            List<ArcLengthSample> arcLengthTable = new ArrayList<>();
            float segmentLength = 0f;
            Position prev = getPointOnCurve(i, 0f);
            arcLengthTable.add(new ArcLengthSample(0f, 0f));

            for (int j = 1; j <= SAMPLES_PER_SEGMENT; j++) {
                float t = (float) j / SAMPLES_PER_SEGMENT;
                Position current = getPointOnCurve(i, t);
                float distance = Position.distance(prev, current);
                segmentLength += distance;
                arcLengthTable.add(new ArcLengthSample(t, segmentLength));
                prev = current;
            }

            segmentLengths.add(segmentLength);
            arcLengthTables.add(arcLengthTable);
            cachedLength += segmentLength;
        }
    }

    private Position getPointOnCurve(int segmentIndex, float t) {
        if (segmentIndex < 0 || segmentIndex >= positions.size() - 1) {
            throw new IndexOutOfBoundsException("Invalid segment index: " + segmentIndex);
        }

        t = Math.max(0f, Math.min(1f, t));

        Position p0, p1, p2, p3;

        if (segmentIndex == 0) {
            p0 = positions.get(0);
        } else {
            p0 = positions.get(segmentIndex - 1);
        }

        p1 = positions.get(segmentIndex);
        p2 = positions.get(segmentIndex + 1);

        if (segmentIndex == positions.size() - 2) {
            p3 = positions.get(positions.size() - 1);
        } else {
            p3 = positions.get(segmentIndex + 2);
        }

        return catmullRomSpline(p0, p1, p2, p3, t);
    }

    private Position catmullRomSpline(Position p0, Position p1, Position p2, Position p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;

        float b0 = -alpha * t3 + 2 * alpha * t2 - alpha * t;
        float b1 = (2 - alpha) * t3 + (alpha - 3) * t2 + 1;
        float b2 = (alpha - 2) * t3 + (3 - 2 * alpha) * t2 + alpha * t;
        float b3 = alpha * t3 - alpha * t2;

        float x = b0 * p0.getX() + b1 * p1.getX() + b2 * p2.getX() + b3 * p3.getX();
        float y = b0 * p0.getY() + b1 * p1.getY() + b2 * p2.getY() + b3 * p3.getY();

        return new Position(x, y);
    }

    private float findTForDistance(int segmentIndex, float targetDistance) {
        List<ArcLengthSample> table = arcLengthTables.get(segmentIndex);
        float segmentLength = segmentLengths.get(segmentIndex);

        if (targetDistance <= 0f) {
            return 0f;
        }
        if (targetDistance >= segmentLength) {
            return 1f;
        }

        for (int i = 0; i < table.size() - 1; i++) {
            ArcLengthSample sample1 = table.get(i);
            ArcLengthSample sample2 = table.get(i + 1);

            if (targetDistance >= sample1.distance && targetDistance <= sample2.distance) {
                float ratio = (targetDistance - sample1.distance) / (sample2.distance - sample1.distance);
                return sample1.t + (sample2.t - sample1.t) * ratio;
            }
        }

        return 1f;
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

        float targetDistance = cachedLength * t;
        float accumulatedDistance = 0f;

        for (int i = 0; i < segmentLengths.size(); i++) {
            float segmentLength = segmentLengths.get(i);

            if (accumulatedDistance + segmentLength >= targetDistance) {
                float localDistance = targetDistance - accumulatedDistance;
                float localT = findTForDistance(i, localDistance);
                return getPointOnCurve(i, localT);
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
