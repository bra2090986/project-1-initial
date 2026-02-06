# Reflection Log

This document captures reflections on the development of 3D geometric classes in Java, focusing on design patterns, principles, and lessons learned.

Point3D Class:
- Design Patterns:
  - Encapsulation by keeping its fields private and providing public getter and setter methods
  - Single Responsibility Principle by focusing solely on representing a point in 3D space and related operations
  - Use of Java's logging framework to provide insights into method execution and potential issues
- Why Immutability:
  - Thread safety, instances are safe to share across threads without synchronization
  - Simplicity, no need to worry about the internal state changing unexpectedly
  - Easier to test
- Mathematical correctness:
  - Distance calculation using the Euclidean formula
  - Rotation methods based on standard rotation matrices for 3D space
- Logging strategy:
  - We may see a lot of unnecessary logs as INFO is used throughout the methods

Line3D Class:
- Composition pattern:
  - Stores two Point3D instances and delegates all math to Point3D methods 
- Complexity of shortest distance between lines method:
  - Involves vector mathematics and handling edge cases such as parallel lines
- Mathematical correctness:
  - Length calculation using the distance between two points
  - Shortest distance method based on vector projections and cross products
- Defensive Programming:
  - Handling cases where lines are parallel or coincident in the shortest distance method

Cube3D Class:
- Multiple design patterns:
  - Composition by using Point3D and Line3D instances to represent the cube's vertices and edges 
  - Single Responsibility Principle by focusing on cube-specific operations like rotation, translation, perimeter, and volume
- Cube validation algorithm:
  - Calculates center by averaging vertices, checks distances from center to vertices, and ensures all edges are of equal length
- Edge generation logic:
  - Connects vertices in a specific order to form the 12 edges of the cube
- Transformation methods:
  - Translation, rotation, and scaling methods that modify the cube's vertices based on geometric transformations
