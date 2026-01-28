// java
package com.csc205.project1;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Immutable 3D line value object.
 *
 * Design patterns demonstrated:
 * - Immutability: fields are final and no mutators; operations return new Point3D instances when needed.
 * - Factory Method: static constructors (`of`, `fromPointAndDirection`) centralize validation and creation.
 * - Encapsulation & Abstraction: geometry operations hide implementation details (vector math) behind clear method contracts.
 *
 * These choices show foundational principles for data structures and algorithms: stable, thread-safe value
 * representations (immutability), reduced surface area for bugs (encapsulation), and predictable runtime
 * characteristics for common geometric operations (most methods are O(1) arithmetic operations).
 */
public final class Line3D {
    private static final Logger LOGGER = Logger.getLogger(Line3D.class.getName());

    private final Point3D p1;
    private final Point3D p2;

    private static final double PARALLEL_EPS = 1e-12;

    private Line3D(Point3D p1, Point3D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Create a new Line3D from two distinct points.
     *
     * This factory method validates inputs are non-null and logs if the defining points are identical
     * (a degenerate line). Lines are infinite and defined by the two supplied points.
     *
     * @param p1 first point on the line
     * @param p2 second point on the line
     * @return new immutable Line3D
     * @throws IllegalArgumentException if either point is null
     */
    public static Line3D of(Point3D p1, Point3D p2) {
        if (p1 == null || p2 == null) {
            LOGGER.severe("of called with null point(s).");
            throw new IllegalArgumentException("points must not be null");
        }
        if (p1.equals(p2)) {
            LOGGER.warning("Creating Line3D with identical points; line is degenerate (no direction).");
        } else {
            LOGGER.info(String.format("Creating Line3D through %s and %s", p1, p2));
        }
        return new Line3D(p1, p2);
    }

    /**
     * Create a Line3D from a point and a direction vector (direction must be non-zero).
     *
     * The resulting line passes through `point` and follows `direction` as a second defining point:
     * secondPoint = point + direction.
     *
     * @param point anchor point on the line
     * @param direction direction vector (treated as point delta)
     * @return new immutable Line3D
     * @throws IllegalArgumentException if inputs are null or direction is zero-length
     */
    public static Line3D fromPointAndDirection(Point3D point, Point3D direction) {
        if (point == null || direction == null) {
            LOGGER.severe("fromPointAndDirection called with null argument(s).");
            throw new IllegalArgumentException("point and direction must not be null");
        }
        double dirMag = direction.magnitude();
        if (dirMag == 0.0) {
            LOGGER.severe("fromPointAndDirection called with zero-length direction.");
            throw new IllegalArgumentException("direction must be non-zero");
        }
        Point3D second = point.add(direction);
        LOGGER.info(String.format("Creating Line3D from point %s with direction %s", point, direction));
        return new Line3D(point, second);
    }

    /**
     * Get the first defining point.
     *
     * @return p1
     */
    public Point3D getP1() {
        return p1;
    }

    /**
     * Get the second defining point.
     *
     * @return p2
     */
    public Point3D getP2() {
        return p2;
    }

    /**
     * Return the Euclidean length of the segment between the two defining points.
     *
     * Note: this is the finite segment length between `p1` and `p2`. For an infinite line this
     * value is the distance between the defining points only and may be zero for degenerate lines.
     *
     * @return non-negative length
     */
    public double length() {
        double len = p1.distanceTo(p2);
        LOGGER.info(String.format("Segment length of %s - %s = %f", p1, p2, len));
        return len;
    }

    /**
     * Return the direction vector (unit-length) from `p1` to `p2`.
     *
     * This method normalizes the vector (p2 - p1). If the defining points are identical an
     * IllegalStateException is thrown because the direction is undefined.
     *
     * @return unit-length direction vector as Point3D
     * @throws IllegalStateException if line is degenerate (zero direction)
     */
    public Point3D direction() {
        Point3D delta = p2.subtract(p1);
        try {
            Point3D dir = delta.normalize();
            LOGGER.info(String.format("Direction of line (%s -> %s) = %s", p1, p2, dir));
            return dir;
        } catch (IllegalStateException ex) {
            LOGGER.severe("direction() called on degenerate line with identical defining points.");
            throw ex;
        }
    }

    /**
     * Return the point on the (infinite) line at parameter t:
     * pointAt(t) = p1 + t * (p2 - p1).
     *
     * For the finite segment between p1 and p2, t in [0,1] interpolates between endpoints.
     *
     * @param t parameter (may be any finite number)
     * @return Point3D at parameter t
     * @throws IllegalArgumentException if t is not finite
     */
    public Point3D pointAt(double t) {
        if (!Double.isFinite(t)) {
            LOGGER.severe("pointAt called with non-finite parameter.");
            throw new IllegalArgumentException("t must be finite");
        }
        Point3D delta = p2.subtract(p1);
        Point3D result = p1.add(delta.scale(t));
        LOGGER.info(String.format("pointAt(%f) on line %s-%s -> %s", t, p1, p2, result));
        return result;
    }

    /**
     * Return the shortest distance from this infinite line to a point.
     *
     * Uses the cross-product magnitude formula:
     * distance = |(p2 - p1) x (p1 - point)| / |p2 - p1|
     *
     * @param point target point
     * @return non-negative shortest distance
     * @throws IllegalArgumentException if point is null
     * @throws IllegalStateException if the line is degenerate (zero direction)
     */
    public double distanceToPoint(Point3D point) {
        if (point == null) {
            LOGGER.severe("distanceToPoint called with null argument.");
            throw new IllegalArgumentException("point must not be null");
        }
        Point3D d = p2.subtract(p1);
        double dMag = d.magnitude();
        if (dMag == 0.0) {
            LOGGER.severe("distanceToPoint called on degenerate line (zero direction).");
            throw new IllegalStateException("line has zero direction");
        }
        Point3D r = p1.subtract(point);
        Point3D cross = d.cross(r);
        double dist = cross.magnitude() / dMag;
        LOGGER.info(String.format("Distance from line %s-%s to point %s = %f", p1, p2, point, dist));
        return dist;
    }

    /**
     * Determine whether this line is parallel to another line.
     *
     * Uses the magnitude of the cross product of direction vectors to check parallelism with
     * a small epsilon threshold to account for floating point inaccuracies.
     *
     * @param other other line
     * @return true if parallel (or one is degenerate), false otherwise
     * @throws IllegalArgumentException if other is null
     */
    public boolean isParallel(Line3D other) {
        if (other == null) {
            LOGGER.severe("isParallel called with null argument.");
            throw new IllegalArgumentException("other must not be null");
        }
        Point3D d1 = this.p2.subtract(this.p1);
        Point3D d2 = other.p2.subtract(other.p1);
        double crossMag = d1.cross(d2).magnitude();
        boolean parallel = crossMag <= PARALLEL_EPS;
        LOGGER.info(String.format("isParallel(%s-%s, %s-%s) = %b (crossMag=%e)", p1, p2, other.p1, other.p2, parallel, crossMag));
        return parallel;
    }

    /**
     * Compute the pair of closest points (one on each infinite line) between this line and another.
     *
     * This method solves the standard two-variable minimization for parameters s and t of the form:
     * P(s) = p1 + s*d1, Q(t) = other.p1 + t*d2; returns {P(s), Q(t)}.
     *
     * For nearly parallel lines a fallback projection strategy is used to produce a valid closest pair.
     *
     * @param other other line
     * @return an array of two Point3D: [closestOnThis, closestOnOther]
     * @throws IllegalArgumentException if other is null
     * @throws IllegalStateException if either line is degenerate (zero direction)
     */
    public Point3D[] closestPointsWith(Line3D other) {
        if (other == null) {
            LOGGER.severe("closestPointsWith called with null argument.");
            throw new IllegalArgumentException("other must not be null");
        }

        Point3D d1 = this.p2.subtract(this.p1);
        Point3D d2 = other.p2.subtract(other.p1);
        double a = d1.dot(d1);
        double b = d1.dot(d2);
        double c = d2.dot(d2);
        Point3D r = this.p1.subtract(other.p1);
        double d = d1.dot(r);
        double e = d2.dot(r);

        if (a == 0.0 || c == 0.0) {
            LOGGER.severe("closestPointsWith called on degenerate line(s).");
            throw new IllegalStateException("one of the lines has zero direction");
        }

        double denom = a * c - b * b;

        if (Math.abs(denom) <= PARALLEL_EPS) {
            // Lines are nearly parallel: use a stable fallback by projecting p1 onto other and then projecting back.
            LOGGER.warning("Lines are nearly parallel in closestPointsWith; using fallback projection strategy.");
            double t = d2.dot(this.p1.subtract(other.p1)) / c;
            Point3D closestOnOther = other.p1.add(d2.scale(t));
            double s = d1.dot(closestOnOther.subtract(this.p1)) / a;
            Point3D closestOnThis = this.p1.add(d1.scale(s));
            LOGGER.info(String.format("Closest (parallel fallback) points: %s (this), %s (other)", closestOnThis, closestOnOther));
            return new Point3D[] { closestOnThis, closestOnOther };
        }

        double s = (b * e - c * d) / denom;
        double t = (a * e - b * d) / denom;

        Point3D closestOnThis = this.p1.add(d1.scale(s));
        Point3D closestOnOther = other.p1.add(d2.scale(t));
        LOGGER.info(String.format("Closest points computed: %s (this), %s (other), params s=%f t=%f", closestOnThis, closestOnOther, s, t));
        return new Point3D[] { closestOnThis, closestOnOther };
    }

    /**
     * Compute the shortest distance between this infinite line and another infinite line.
     *
     * Internally uses `closestPointsWith` to compute the closest pair and then returns their separation.
     *
     * @param other other line
     * @return non-negative shortest distance between the two infinite lines
     * @throws IllegalArgumentException if other is null
     * @throws IllegalStateException if either line is degenerate
     */
    public double shortestDistanceTo(Line3D other) {
        Point3D[] pts = closestPointsWith(other);
        double dist = pts[0].distanceTo(pts[1]);
        LOGGER.info(String.format("Shortest distance between lines %s-%s and %s-%s = %f", p1, p2, other.p1, other.p2, dist));
        return dist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line3D)) return false;
        Line3D other = (Line3D) o;
        return Objects.equals(p1, other.p1) && Objects.equals(p2, other.p2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p1, p2);
    }

    @Override
    public String toString() {
        return String.format("Line3D[%s -> %s]", p1, p2);
    }
}
