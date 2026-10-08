/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
/**
 * Provides the core geometry types for representing spatial data in GAMA.
 * 
 * <p>This package is the foundation of GAMA's spatial modeling capabilities, containing interfaces and implementations
 * for points, shapes, and geometric operations. It wraps and extends the Java Topology Suite (JTS) library with
 * GAMA-specific functionality including 3D support, type integration, and operator definitions.</p>
 * 
 * <h2>Core Types</h2>
 * 
 * <h3>Points</h3>
 * <ul>
 *   <li>{@link gama.api.types.geometry.IPoint} - Interface for 3D points with coordinates and operations</li>
 *   <li>{@link gama.api.types.geometry.GamaPoint} - Mutable 3D point implementation extending JTS Coordinate</li>
 *   <li>{@link gama.api.types.geometry.GamaPointFactory} - Factory for creating point instances (mutable and immutable)</li>
 * </ul>
 * 
 * <h3>Shapes</h3>
 * <ul>
 *   <li>{@link gama.api.types.geometry.IShape} - Interface for all geometric shapes (points, lines, polygons, etc.)</li>
 *   <li>{@link gama.api.types.geometry.GamaShapeFactory} - Comprehensive factory for creating shapes from various sources</li>
 *   <li>{@link gama.api.types.geometry.IShapeFactory} - Interface defining the shape creation contract</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Working with Points in GAML</h3>
 * <pre>
 * // Create points
 * point p1 &lt;- {10, 20};           // 2D point (z=0)
 * point p2 &lt;- {10, 20, 5};        // 3D point
 * 
 * // Access coordinates
 * float x_coord &lt;- p1.x;
 * float y_coord &lt;- p1.y;
 * float z_coord &lt;- p1.z;
 * 
 * // Arithmetic operations
 * point sum &lt;- p1 + p2;
 * point diff &lt;- p1 - p2;
 * point scaled &lt;- p1 * 2.0;
 * point divided &lt;- p1 / 2.0;
 * 
 * // Comparison operations
 * bool smaller &lt;- p1 &lt; p2;       // component-wise comparison
 * </pre>
 * 
 * <h3>Working with Shapes in GAML</h3>
 * <pre>
 * // Create basic shapes
 * geometry circle &lt;- circle(10);                    // radius 10
 * geometry rect &lt;- rectangle(20, 15);               // width 20, height 15
 * geometry poly &lt;- polygon([{0,0}, {10,0}, {10,10}, {0,10}]);
 * 
 * // Shape properties
 * float area &lt;- circle.area;
 * float perim &lt;- circle.perimeter;
 * point center &lt;- circle.centroid;
 * list&lt;point&gt; vertices &lt;- poly.points;
 * 
 * // Spatial operations
 * geometry intersection &lt;- circle inter rect;
 * geometry union &lt;- circle union rect;
 * float distance &lt;- circle distance_to rect;
 * bool overlaps &lt;- circle overlaps rect;
 * </pre>
 * 
 * <h3>Creating Geometry in Java</h3>
 * <pre>
 * // Create points
 * IPoint point2D = GamaPointFactory.create(10.0, 20.0);
 * IPoint point3D = GamaPointFactory.create(10.0, 20.0, 5.0);
 * IPoint immutablePoint = GamaPointFactory.createImmutable(10.0, 20.0, 5.0);
 * 
 * // Create shapes from JTS geometry
 * Geometry jtsGeom = ...;
 * IShape shape = GamaShapeFactory.createFrom(jtsGeom);
 * 
 * // Create primitive shapes
 * IShape circle = GamaShapeFactory.buildCircle(10.0, centerPoint);
 * IShape square = GamaShapeFactory.buildSquare(20.0, centerPoint);
 * IShape polygon = GamaShapeFactory.buildPolygon(listOfPoints);
 * 
 * // Access shape properties
 * double area = shape.getArea();
 * double perimeter = shape.getPerimeter();
 * IPoint centroid = shape.getCentroid();
 * IEnvelope envelope = shape.getEnvelope();
 * Geometry innerGeometry = shape.getInnerGeometry();
 * </pre>
 * 
 * <h2>Coordinate Systems</h2>
 * <p>All geometry in GAMA uses a 3D Cartesian coordinate system:</p>
 * <ul>
 *   <li><b>X-axis</b> - Horizontal (west-east in default orientation)</li>
 *   <li><b>Y-axis</b> - Vertical (south-north in default orientation)</li>
 *   <li><b>Z-axis</b> - Depth/elevation (down-up)</li>
 * </ul>
 * <p>When working with 2D geometries, the Z-coordinate is typically 0.</p>
 * 
 * <h2>Mutability</h2>
 * <p><b>Points:</b> {@link gama.api.types.geometry.GamaPoint} instances are mutable by default. For immutable points,
 * use {@link gama.api.types.geometry.GamaPointFactory.Immutable} via the factory's {@code createImmutable} methods.</p>
 * 
 * <p><b>Shapes:</b> {@link gama.api.types.geometry.IShape} instances are generally mutable. Modification operations
 * (rotation, scaling, translation) modify the shape in place and return {@code this} for method chaining. Use
 * {@link gama.api.types.geometry.IShape#copy(gama.api.runtime.scope.IScope)} to create independent copies.</p>
 * 
 * <h2>Thread Safety</h2>
 * <p>Neither {@link gama.api.types.geometry.GamaPoint} nor most {@link gama.api.types.geometry.IShape} implementations
 * are thread-safe. If shapes or points must be shared across threads, either:</p>
 * <ul>
 *   <li>Use immutable points ({@link gama.api.types.geometry.GamaPointFactory.Immutable})</li>
 *   <li>Create copies for each thread</li>
 *   <li>Provide external synchronization</li>
 * </ul>
 * 
 * <h2>Tolerance</h2>
 * <p>Geometric comparisons use a configurable tolerance value (default defined in {@code GamaPreferences.Experimental.TOLERANCE_POINTS}).
 * This tolerance is used for:</p>
 * <ul>
 *   <li>Equality comparisons between points</li>
 *   <li>Geometric predicates (contains, intersects, etc.)</li>
 *   <li>Coordinate snapping operations</li>
 * </ul>
 * 
 * <h2>Integration with JTS</h2>
 * <p>GAMA geometry types wrap and extend the Java Topology Suite (JTS):</p>
 * <ul>
 *   <li>{@link gama.api.types.geometry.GamaPoint} extends {@link org.locationtech.jts.geom.Coordinate}</li>
 *   <li>{@link gama.api.types.geometry.IShape} wraps JTS {@link org.locationtech.jts.geom.Geometry}</li>
 *   <li>JTS geometries can be accessed via {@link gama.api.types.geometry.IShape#getInnerGeometry()}</li>
 *   <li>GAMA shapes can be created from JTS geometries via {@link gama.api.types.geometry.GamaShapeFactory#createFrom(org.locationtech.jts.geom.Geometry)}</li>
 * </ul>
 * 
 * <h2>Serialization</h2>
 * <p>Geometry types support multiple serialization formats:</p>
 * <ul>
 *   <li><b>GAML:</b> {@code serializeToGaml(boolean)} - produces valid GAML syntax ({@code {x,y,z}})</li>
 *   <li><b>JSON:</b> {@code serializeToJson(IJson)} - structured JSON representation</li>
 *   <li><b>WKT:</b> Via JTS integration for standard geographic data exchange</li>
 * </ul>
 * 
 * @see gama.api.types.geometry.IPoint
 * @see gama.api.types.geometry.IShape
 * @see gama.api.types.geometry.GamaPointFactory
 * @see gama.api.types.geometry.GamaShapeFactory
 * @see org.locationtech.jts.geom.Geometry
 * @see org.locationtech.jts.geom.Coordinate
 * @since GAMA 1.0
 */
package gama.api.types.geometry;
