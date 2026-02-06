package com.csc205.project1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Point3DTest {

    private static final double EPS = 1e-9;

    @Test
    void distanceToSamePointIsZero() {
        Point3D p = Point3D.of(1.0, 2.0, 3.0);
        assertEquals(0.0, p.distanceTo(p), EPS, "Distance from a point to itself should be zero");
    }

    @Test
    void distanceIsSymmetric() {
        Point3D a = Point3D.of(0.0, 0.0, 0.0);
        Point3D b = Point3D.of(1.0, 2.0, 2.0);
        assertEquals(a.distanceTo(b), b.distanceTo(a), EPS, "Distance should be symmetric");
    }

    @Test
    void distanceKnownValue() {
        Point3D a = Point3D.of(0.0, 0.0, 0.0);
        Point3D b = Point3D.of(1.0, 1.0, 1.0);
        assertEquals(Math.sqrt(3.0), a.distanceTo(b), EPS, "Distance from (0,0,0) to (1,1,1) should be sqrt(3)");
    }
}
