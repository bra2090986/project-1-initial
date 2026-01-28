package com.csc205.project1;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Immutable 3D cube value object.
 *
 * Design patterns demonstrated:
 * - Immutability: all fields are final and no mutators; transformation methods return new Cube3D instances,
 *   ensuring thread-safety and predictable behavior.
 * - Factory Method: static constructors (of, fromVertices) centralize validation and creation logic.
 * - Value Object: equals, hashCode and toString are implemented so instances represent equality by value.
 * - Encapsulation: geometric operations hide vector math and vertex bookkeeping behind clear method contracts.
 *
 * These patterns demonstrate foundational principles for data structures and algorithms: stable value
 * representations (immutability), clear construction contracts (factory methods), and O(1) geometry
 * operations for routine queries (volume, surface area, perimeter).
 */
public final class Cube3D {
    private static final Logger LOGGER = Logger.getLogger(Cube3D.class.getName());

    private final Point3D center;
    private final double side; // positive side length
    private final Point3D[] vertices; // length == 8, ordered by bit pattern: (xBit, yBit, zBit) 0..7

    private static final double EPS = 1e-12;

    private Cube3D(Point3D center, double side, Point3D[] vertices) {
        this.center = center;
        this.side = side;
        this.vertices = vertices;
    }

    /**
     * Create an axis-aligned cube given a center and side length.
     *
     * Validates inputs: center non-null, side positive and finite. Builds 8 vertices arranged such that
     * vertex index i (0..7) corresponds to bits: ((i & 1) ? +x : -x), ((i & 2) ? +y : -y), ((i & 4) ? +z : -z).
     *
     * @param center cube center
     * @param side positive side length
     * @return new immutable Cube3D
     * @throws IllegalArgumentException if inputs are invalid
     */
    public static Cube3D of(Point3D center, double side) {
        if (center == null) {
            LOGGER.severe("of called with null center.");
            throw new IllegalArgumentException("center must not be null");
        }
        if (!Double.isFinite(side) || side <= 0.0) {
            LOGGER.severe("of called with non-positive or non-finite side.");
            throw new IllegalArgumentException("side must be positive and finite");
        }
        double h = side / 2.0;
        Point3D[] verts = new Point3D[8];
        for (int i = 0; i < 8; i++) {
            double vx = center.getX() + (((i & 1) != 0) ? h : -h);
            double vy = center.getY() + (((i & 2) != 0) ? h : -h);
            double vz = center.getZ() + (((i & 4) != 0) ? h : -h);
            verts[i] = Point3D.of(vx, vy, vz);
        }
        LOGGER.info(String.format("Created axis-aligned Cube3D center=%s side=%f", center, side));
        return new Cube3D(center, side, verts);
    }

    /**
     * Create a cube from exactly eight vertices.
     *
     * This factory accepts a vertex array of length 8 and infers the center and side length.
     * Validation checks that vertices are non-null and finite. It is the caller's responsibility
     * to provide vertices that form a cube; this method performs basic validation but does not
     * fully verify perfect cubeness (to avoid heavy numeric tests).
     *
     * @param vertices array of eight Point3D
     * @return new immutable Cube3D
     * @throws IllegalArgumentException if vertices array is invalid
     */
    public static Cube3D fromVertices(Point3D[] vertices) {
        if (vertices == null || vertices.length != 8) {
            LOGGER.severe("fromVertices called with invalid vertex array (must be length 8).");
            throw new IllegalArgumentException("vertices must be an array of eight Point3D");
        }
        Point3D[] copy = Arrays.copyOf(vertices, 8);
        for (int i = 0; i < 8; i++) {
            if (copy[i] == null) {
                LOGGER.severe("fromVertices called with null vertex.");
                throw new IllegalArgumentException("vertices must not contain null");
            }
            // Basic finiteness check using coordinates
            double x = copy[i].getX(), y = copy[i].getY(), z = copy[i].getZ();
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                LOGGER.severe("fromVertices called with non-finite vertex coordinate.");
                throw new IllegalArgumentException("vertex coordinates must be finite");
            }
        }
        // Infer center by averaging vertices (valid for cube)
        double sx = 0, sy = 0, sz = 0;
        for (Point3D p : copy) {
            sx += p.getX(); sy += p.getY(); sz += p.getZ();
        }
        Point3D center = Point3D.of(sx / 8.0, sy / 8.0, sz / 8.0);

        // Infer side as the minimal positive pairwise edge length (heuristic)
        double candidateSide = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                double d = copy[i].distanceTo(copy[j]);
                if (d > EPS && d < candidateSide) candidateSide = d;
            }
        }
        if (!Double.isFinite(candidateSide) || candidateSide <= 0.0) {
            LOGGER.severe("fromVertices could not infer a valid side length from vertices.");
            throw new IllegalArgumentException("could not infer valid side length from vertices");
        }

        // The smallest inter-vertex distance on a cube equals the side length.
        LOGGER.info(String.format("Constructed Cube3D from vertices; inferred center=%s side=%f", center, candidateSide));
        return new Cube3D(center, candidateSide, copy);
    }

    /**
     * Return the cube center.
     *
     * @return center Point3D
     */
    public Point3D getCenter() {
        return center;
    }

    /**
     * Return the cube side length.
     *
     * @return positive side length
     */
    public double getSide() {
        return side;
    }

    /**
     * Return a copy of the eight vertices (defensive copy).
     *
     * Vertices are ordered by bit pattern: index bit0 -> x, bit1 -> y, bit2 -> z.
     *
     * @return Point3D[] length 8
     */
    public Point3D[] getVertices() {
        return Arrays.copyOf(vertices, vertices.length);
    }

    /**
     * Return an array of the 12 edges as Line3D objects.
     *
     * Edges are generated by connecting vertex indices that differ by exactly one bit.
     *
     * @return Line3D[] of length 12
     */
    public Line3D[] getEdges() {
        Line3D[] edges = new Line3D[12];
        int idx = 0;
        for (int i = 0; i < 8; i++) {
            for (int bit = 0; bit < 3; bit++) {
                int j = i ^ (1 << bit);
                if (i < j) { // avoid duplicates
                    try {
                        edges[idx++] = Line3D.of(vertices[i], vertices[j]);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.warning("getEdges encountered invalid vertices when building an edge.");
                        // continue building remaining edges
                    }
                }
            }
        }
        LOGGER.info(String.format("Built %d edges for cube center=%s side=%f", idx, center, side));
        return edges;
    }

    /**
     * Compute total perimeter length (sum of all 12 edge lengths).
     *
     * For a perfect cube this equals 12 * side; computed by summing built edges for robustness.
     *
     * @return non-negative perimeter length
     */
    public double perimeterLength() {
        Line3D[] edges = getEdges();
        double sum = 0.0;
        for (Line3D e : edges) {
            if (e != null) sum += e.length();
        }
        LOGGER.info(String.format("Perimeter length of cube center=%s side=%f -> %f", center, side, sum));
        return sum;
    }

    /**
     * Compute volume of the cube: side^3.
     *
     * @return volume (> 0)
     */
    public double volume() {
        double vol = side * side * side;
        LOGGER.info(String.format("Volume of cube side=%f -> %f", side, vol));
        return vol;
    }

    /**
     * Compute surface area of the cube: 6 * side^2.
     *
     * @return surface area (> 0)
     */
    public double surfaceArea() {
        double area = 6.0 * side * side;
        LOGGER.info(String.format("Surface area of cube side=%f -> %f", side, area));
        return area;
    }

    /**
     * Translate the cube by specified deltas.
     *
     * Returns a new Cube3D whose vertices and center are translated by (dx,dy,dz).
     *
     * @param dx delta x
     * @param dy delta y
     * @param dz delta z
     * @return new translated Cube3D
     * @throws IllegalArgumentException if any delta is non-finite
     */
    public Cube3D translate(double dx, double dy, double dz) {
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) {
            LOGGER.severe("translate called with non-finite delta(s).");
            throw new IllegalArgumentException("deltas must be finite");
        }
        Point3D newCenter = center.translate(dx, dy, dz);
        Point3D[] newVerts = new Point3D[8];
        for (int i = 0; i < 8; i++) {
            newVerts[i] = vertices[i].translate(dx, dy, dz);
        }
        LOGGER.info(String.format("Translated cube center=%s by (%f,%f,%f) -> new center=%s", center, dx, dy, dz, newCenter));
        return new Cube3D(newCenter, side, newVerts);
    }

    /**
     * Rotate the cube around its center about the X-axis by the given radians.
     *
     * Rotation is performed by translating each vertex to origin-relative coordinates
     * (vertex - center), applying Point3D.rotateX, then translating back.
     *
     * @param radians angle in radians (finite)
     * @return new rotated Cube3D
     * @throws IllegalArgumentException if radians is not finite
     */
    public Cube3D rotateX(double radians) {
        if (!Double.isFinite(radians)) {
            LOGGER.severe("rotateX called with non-finite radians.");
            throw new IllegalArgumentException("radians must be finite");
        }
        Point3D[] newVerts = new Point3D[8];
        for (int i = 0; i < 8; i++) {
            Point3D rel = vertices[i].subtract(center);
            Point3D rotatedRel = rel.rotateX(radians);
            newVerts[i] = center.add(rotatedRel);
        }
        LOGGER.info(String.format("Rotated cube center=%s around X by %f radians", center, radians));
        return new Cube3D(center, side, newVerts);
    }

    /**
     * Rotate the cube around its center about the Y-axis by the given radians.
     *
     * @param radians angle in radians (finite)
     * @return new rotated Cube3D
     * @throws IllegalArgumentException if radians is not finite
     */
    public Cube3D rotateY(double radians) {
        if (!Double.isFinite(radians)) {
            LOGGER.severe("rotateY called with non-finite radians.");
            throw new IllegalArgumentException("radians must be finite");
        }
        Point3D[] newVerts = new Point3D[8];
        for (int i = 0; i < 8; i++) {
            Point3D rel = vertices[i].subtract(center);
            Point3D rotatedRel = rel.rotateY(radians);
            newVerts[i] = center.add(rotatedRel);
        }
        LOGGER.info(String.format("Rotated cube center=%s around Y by %f radians", center, radians));
        return new Cube3D(center, side, newVerts);
    }

    /**
     * Rotate the cube around its center about the Z-axis by the given radians.
     *
     * @param radians angle in radians (finite)
     * @return new rotated Cube3D
     * @throws IllegalArgumentException if radians is not finite
     */
    public Cube3D rotateZ(double radians) {
        if (!Double.isFinite(radians)) {
            LOGGER.severe("rotateZ called with non-finite radians.");
            throw new IllegalArgumentException("radians must be finite");
        }
        Point3D[] newVerts = new Point3D[8];
        for (int i = 0; i < 8; i++) {
            Point3D rel = vertices[i].subtract(center);
            Point3D rotatedRel = rel.rotateZ(radians);
            newVerts[i] = center.add(rotatedRel);
        }
        LOGGER.info(String.format("Rotated cube center=%s around Z by %f radians", center, radians));
        return new Cube3D(center, side, newVerts);
    }

    /**
     * Axis-aligned bounding box of the cube (min and max corners).
     *
     * Useful for culling or simple collision checks in graphics pipelines.
     *
     * @return Point3D[] where index 0 = min corner, index 1 = max corner
     */
    public Point3D[] axisAlignedBounds() {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (Point3D p : vertices) {
            minX = Math.min(minX, p.getX()); minY = Math.min(minY, p.getY()); minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX()); maxY = Math.max(maxY, p.getY()); maxZ = Math.max(maxZ, p.getZ());
        }
        Point3D min = Point3D.of(minX, minY, minZ);
        Point3D max = Point3D.of(maxX, maxY, maxZ);
        LOGGER.info(String.format("Computed axis-aligned bounds for cube center=%s -> min=%s max=%s", center, min, max));
        return new Point3D[] { min, max };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cube3D)) return false;
        Cube3D other = (Cube3D) o;
        return Double.compare(other.side, side) == 0
                && Objects.equals(center, other.center)
                && Arrays.equals(vertices, other.vertices);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(center, Double.valueOf(side));
        result = 31 * result + Arrays.hashCode(vertices);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Cube3D[center=%s side=%.6f]", center, side);
    }
}
