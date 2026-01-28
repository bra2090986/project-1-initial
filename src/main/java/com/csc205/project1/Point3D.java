package com.csc205.project1;

// java
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Immutable 3D point / vector value object.
 *
 * Design patterns demonstrated:
 * - Immutability: all fields are final and no setters; methods return new instances for transformations,
 *   ensuring thread-safety and predictable behavior.
 * - Factory Method: static constructors (of, origin) provide named creation points and encapsulate validation.
 * - Value Object: equals, hashCode and toString are implemented so instances represent equality by value.
 * - Defensive Programming: inputs are validated and methods log warnings/errors when encountering invalid state.
 *
 * This class is suitable for geometry, graphics and algorithmic code that requires stable, copy-safe 3D points.
 */
public final class Point3D {
    private static final Logger LOGGER = Logger.getLogger(Point3D.class.getName());

    private final double x;
    private final double y;
    private final double z;

    private Point3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Create a new Point3D instance from coordinates.
     *
     * This factory method validates that coordinates are finite numbers.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return new immutable Point3D
     * @throws IllegalArgumentException if any coordinate is not a finite number
     */
    public static Point3D of(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            LOGGER.severe("Attempted to create Point3D with non-finite coordinate(s).");
            throw new IllegalArgumentException("Coordinates must be finite numbers");
        }
        LOGGER.info(String.format("Creating Point3D: (%f, %f, %f)", x, y, z));
        return new Point3D(x, y, z);
    }

    /**
     * Return the origin point (0,0,0).
     *
     * @return origin Point3D
     */
    public static Point3D origin() {
        LOGGER.info("Returning origin Point3D (0,0,0).");
        return new Point3D(0.0, 0.0, 0.0);
    }

    /**
     * Get X coordinate.
     *
     * @return x
     */
    public double getX() {
        return x;
    }

    /**
     * Get Y coordinate.
     *
     * @return y
     */
    public double getY() {
        return y;
    }

    /**
     * Get Z coordinate.
     *
     * @return z
     */
    public double getZ() {
        return z;
    }

    /**
     * Compute Euclidean distance to another point.
     *
     * Logs at INFO on normal calls and SEVERE then throws IllegalArgumentException if the argument is null.
     *
     * @param other target point
     * @return Euclidean distance
     * @throws IllegalArgumentException if other is null
     */
    public double distanceTo(Point3D other) {
        if (other == null) {
            LOGGER.severe("distanceTo called with null argument.");
            throw new IllegalArgumentException("other must not be null");
        }
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double dz = other.z - this.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        LOGGER.info(String.format("Computed distance from %s to %s = %f", this, other, dist));
        return dist;
    }

    /**
     * Return the vector magnitude (length) of this point considered as a vector from origin.
     *
     * @return magnitude (>= 0)
     */
    public double magnitude() {
        double mag = Math.sqrt(x * x + y * y + z * z);
        LOGGER.info(String.format("Magnitude of %s = %f", this, mag));
        return mag;
    }

    /**
     * Return a normalized (unit-length) vector version of this point.
     *
     * Logs WARNING if magnitude is very small and SEVERE + IllegalStateException if zero-length.
     *
     * @return new Point3D normalized
     * @throws IllegalStateException if vector length is zero
     */
    public Point3D normalize() {
        double mag = magnitude();
        if (mag == 0.0) {
            LOGGER.severe("normalize called on zero-length vector.");
            throw new IllegalStateException("Cannot normalize a zero-length vector");
        }
        if (mag < 1e-12) {
            LOGGER.warning("Normalizing a very small magnitude vector may be unstable numerically.");
        }
        return scale(1.0 / mag);
    }

    /**
     * Scale this vector by a scalar factor.
     *
     * @param factor scalar multiplier
     * @return new scaled Point3D
     */
    public Point3D scale(double factor) {
        if (!Double.isFinite(factor)) {
            LOGGER.severe("scale called with non-finite factor.");
            throw new IllegalArgumentException("factor must be finite");
        }
        Point3D result = new Point3D(x * factor, y * factor, z * factor);
        LOGGER.info(String.format("Scaled %s by %f -> %s", this, factor, result));
        return result;
    }

    /**
     * Add another vector/point to this one component-wise.
     *
     * @param other other point
     * @return new Point3D sum
     * @throws IllegalArgumentException if other is null
     */
    public Point3D add(Point3D other) {
        if (other == null) {
            LOGGER.severe("add called with null argument.");
            throw new IllegalArgumentException("other must not be null");
        }
        Point3D result = new Point3D(this.x + other.x, this.y + other.y, this.z + other.z);
        LOGGER.info(String.format("Added %s + %s = %s", this, other, result));
        return result;
    }

    /**
     * Subtract another vector/point from this one component-wise.
     *
     * @param other other point
     * @return new Point3D difference
     * @throws IllegalArgumentException if other is null
     */
    public Point3D subtract(Point3D other) {
        if (other == null) {
            LOGGER.severe("subtract called with null argument.");
            throw new IllegalArgumentException("other must not be null");
        }
        Point3D result = new Point3D(this.x - other.x, this.y - other.y, this.z - other.z);
        LOGGER.info(String.format("Subtracted %s - %s = %s", this, other, result));
        return result;
    }

    /**
     * Compute dot product with another vector.
     *
     * @param other other vector
     * @return dot product
     * @throws IllegalArgumentException if other is null
     */
    public double dot(Point3D other) {
        if (other == null) {
            LOGGER.severe("dot called with null argument.");
            throw new IllegalArgumentException("other must not be null");
        }
        double dp = this.x * other.x + this.y * other.y + this.z * other.z;
        LOGGER.info(String.format("Dot(%s, %s) = %f", this, other, dp));
        return dp;
    }

    /**
     * Compute cross product with another vector.
     *
     * @param other other vector
     * @return new Point3D representing cross product
     * @throws IllegalArgumentException if other is null
     */
    public Point3D cross(Point3D other) {
        if (other == null) {
            LOGGER.severe("cross called with null argument.");
            throw new IllegalArgumentException("other must not be null");
        }
        double cx = this.y * other.z - this.z * other.y;
        double cy = this.z * other.x - this.x * other.z;
        double cz = this.x * other.y - this.y * other.x;
        Point3D result = new Point3D(cx, cy, cz);
        LOGGER.info(String.format("Cross(%s, %s) = %s", this, other, result));
        return result;
    }

    /**
     * Rotate this point around the X-axis by the given angle (radians), around origin.
     *
     * Uses right-hand rule; returns a new Point3D.
     *
     * @param radians angle in radians
     * @return rotated Point3D
     */
    public Point3D rotateX(double radians) {
        if (!Double.isFinite(radians)) {
            LOGGER.severe("rotateX called with non-finite radians.");
            throw new IllegalArgumentException("radians must be finite");
        }
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double ny = y * cos - z * sin;
        double nz = y * sin + z * cos;
        Point3D result = new Point3D(x, ny, nz);
        LOGGER.info(String.format("rotateX(%f) of %s -> %s", radians, this, result));
        return result;
    }

    /**
     * Rotate this point around the Y-axis by the given angle (radians), around origin.
     *
     * @param radians angle in radians
     * @return rotated Point3D
     */
    public Point3D rotateY(double radians) {
        if (!Double.isFinite(radians)) {
            LOGGER.severe("rotateY called with non-finite radians.");
            throw new IllegalArgumentException("radians must be finite");
        }
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double nx = x * cos + z * sin;
        double nz = -x * sin + z * cos;
        Point3D result = new Point3D(nx, y, nz);
        LOGGER.info(String.format("rotateY(%f) of %s -> %s", radians, this, result));
        return result;
    }

    /**
     * Rotate this point around the Z-axis by the given angle (radians), around origin.
     *
     * @param radians angle in radians
     * @return rotated Point3D
     */
    public Point3D rotateZ(double radians) {
        if (!Double.isFinite(radians)) {
            LOGGER.severe("rotateZ called with non-finite radians.");
            throw new IllegalArgumentException("radians must be finite");
        }
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double nx = x * cos - y * sin;
        double ny = x * sin + y * cos;
        Point3D result = new Point3D(nx, ny, z);
        LOGGER.info(String.format("rotateZ(%f) of %s -> %s", radians, this, result));
        return result;
    }

    /**
     * Translate (add) a displacement vector to this point.
     *
     * @param dx delta x
     * @param dy delta y
     * @param dz delta z
     * @return new translated Point3D
     */
    public Point3D translate(double dx, double dy, double dz) {
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) {
            LOGGER.severe("translate called with non-finite delta(s).");
            throw new IllegalArgumentException("deltas must be finite");
        }
        Point3D result = new Point3D(x + dx, y + dy, z + dz);
        LOGGER.info(String.format("translate(%f,%f,%f) of %s -> %s", dx, dy, dz, this, result));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point3D)) return false;
        Point3D other = (Point3D) o;
        return Double.compare(other.x, x) == 0
                && Double.compare(other.y, y) == 0
                && Double.compare(other.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Double.valueOf(x), Double.valueOf(y), Double.valueOf(z));
    }

    @Override
    public String toString() {
        return String.format("Point3D(%.6f, %.6f, %.6f)", x, y, z);
    }
}
