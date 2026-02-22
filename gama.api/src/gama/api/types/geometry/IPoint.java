/*******************************************************************************************************
 *
 * IPoint.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.geometry;

import org.locationtech.jts.geom.Coordinate;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IIntersectable;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Interface IPoint.
 */
@vars ({ @variable (
		name = IKeyword.X,
		type = IType.FLOAT,
		doc = { @doc ("Returns the x ordinate of this point") }),
		@variable (
				name = IKeyword.Y,
				type = IType.FLOAT,
				doc = { @doc ("Returns the y ordinate of this point") }),
		@variable (
				name = IKeyword.Z,
				type = IType.FLOAT,
				doc = { @doc ("Returns the z ordinate of this point") }) })
/**
 * Interface for 3D point values in GAMA.
 * 
 * <p>An {@code IPoint} represents a position in 3D Cartesian space with x, y, and z coordinates. Points are
 * fundamental building blocks in GAMA for representing locations of agents, vertices of geometries, and vectors
 * for transformations.</p>
 * 
 * <p>This interface extends {@link IShape}, making points a special case of geometric shapes. It also extends
 * {@link Comparable} for ordering points and {@link Cloneable} for creating copies.</p>
 * 
 * <h2>Coordinate System</h2>
 * <p>Points use a right-handed 3D Cartesian coordinate system:</p>
 * <ul>
 *   <li><b>X-axis:</b> Horizontal (typically west-east)</li>
 *   <li><b>Y-axis:</b> Vertical (typically south-north)</li>
 *   <li><b>Z-axis:</b> Depth/elevation (typically down-up)</li>
 * </ul>
 * 
 * <h2>Operations</h2>
 * <p>IPoint supports rich arithmetic and comparison operations:</p>
 * 
 * <h3>Arithmetic Operations</h3>
 * <ul>
 *   <li><b>Addition:</b> {@code p1 + p2}, {@code p + scalar}</li>
 *   <li><b>Subtraction:</b> {@code p1 - p2}, {@code p - scalar}</li>
 *   <li><b>Multiplication:</b> {@code p * scalar}</li>
 *   <li><b>Division:</b> {@code p / scalar}</li>
 * </ul>
 * 
 * <h3>Comparison Operations</h3>
 * <ul>
 *   <li>{@link #smallerThan(IPoint)} - Component-wise less than</li>
 *   <li>{@link #biggerThan(IPoint)} - Component-wise greater than</li>
 *   <li>{@link #smallerThanOrEqualTo(IPoint)} - Component-wise less than or equal</li>
 *   <li>{@link #biggerThanOrEqualTo(IPoint)} - Component-wise greater than or equal</li>
 * </ul>
 * 
 * <h2>Mutability</h2>
 * <p>Most {@code IPoint} implementations (e.g., {@link GamaPoint}) are mutable. Coordinates can be modified
 * using setter methods. For immutable points, use {@link GamaPointFactory.Immutable}.</p>
 * 
 * <p><b>Warning:</b> When using mutable points, be aware that modifying a point affects all references to it.
 * Use {@link #copy(gama.api.runtime.scope.IScope)} to create independent copies when needed.</p>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>GAML Usage</h3>
 * <pre>
 * // Create points
 * point p1 &lt;- {10.0, 20.0};        // 2D point (z=0)
 * point p2 &lt;- {10.0, 20.0, 5.0};   // 3D point
 * 
 * // Access coordinates
 * float x &lt;- p1.x;
 * float y &lt;- p1.y;
 * float z &lt;- p1.z;
 * 
 * // Arithmetic
 * point sum &lt;- p1 + p2;
 * point scaled &lt;- p1 * 2.0;
 * 
 * // Comparison
 * bool is_smaller &lt;- p1 &lt; p2;
 * </pre>
 * 
 * <h3>Java Usage</h3>
 * <pre>
 * // Create points
 * IPoint p1 = GamaPointFactory.create(10.0, 20.0, 5.0);
 * 
 * // Access coordinates
 * double x = p1.getX();
 * double y = p1.getY();
 * double z = p1.getZ();
 * 
 * // Modify (if mutable)
 * p1.setLocation(15.0, 25.0, 10.0);
 * 
 * // Arithmetic
 * IPoint sum = p1.add(p2);  // Modifies p1!
 * IPoint copy = p1.copy(scope);  // Create independent copy
 * </pre>
 * 
 * <h2>Thread Safety</h2>
 * <p>Mutable {@code IPoint} implementations are NOT thread-safe. If a point must be shared across threads,
 * either:</p>
 * <ul>
 *   <li>Use immutable points ({@link GamaPointFactory.Immutable})</li>
 *   <li>Create copies for each thread</li>
 *   <li>Provide external synchronization</li>
 * </ul>
 * 
 * <h2>Tolerance</h2>
 * <p>Equality comparisons use a configurable tolerance value (see {@link GamaPoint#TOLERANCE}) to handle
 * floating-point precision issues. Two points are considered equal if their coordinates differ by less than
 * the tolerance.</p>
 * 
 * <h2>JTS Integration</h2>
 * <p>Points are compatible with JTS {@link Coordinate} objects. The primary implementation {@link GamaPoint}
 * extends {@code Coordinate} directly, allowing seamless integration with JTS geometry operations.</p>
 * 
 * @author drogoul
 * @see GamaPoint
 * @see GamaPointFactory
 * @see IShape
 * @see org.locationtech.jts.geom.Coordinate
 * @since GAMA 1.0
 */

public interface IPoint extends IShape, IIntersectable, Cloneable, Comparable<Coordinate> {

	/**
	 * Tests whether this point is component-wise smaller than another point.
	 * 
	 * <p>Returns {@code true} if and only if {@code this.x < other.x AND this.y < other.y}.
	 * The z-coordinate is not considered in this comparison.</p>
	 * 
	 * <p>This is a strict comparison - if any coordinate is equal or greater, returns false.</p>
	 *
	 * @param other the point to compare against
	 * @return true if this point is strictly smaller in both x and y, false otherwise
	 */
	boolean smallerThan(IPoint other);

	/**
	 * Tests whether this point is component-wise smaller than or equal to another point.
	 * 
	 * <p>Returns {@code true} if and only if {@code this.x <= other.x AND this.y <= other.y}.
	 * The z-coordinate is not considered in this comparison.</p>
	 *
	 * @param other the point to compare against
	 * @return true if this point is smaller or equal in both x and y, false otherwise
	 */
	boolean smallerThanOrEqualTo(IPoint other);

	/**
	 * Tests whether this point is component-wise bigger than another point.
	 * 
	 * <p>Returns {@code true} if and only if {@code this.x > other.x AND this.y > other.y}.
	 * The z-coordinate is not considered in this comparison.</p>
	 * 
	 * <p>This is a strict comparison - if any coordinate is equal or smaller, returns false.</p>
	 *
	 * @param other the point to compare against
	 * @return true if this point is strictly bigger in both x and y, false otherwise
	 */
	boolean biggerThan(IPoint other);

	/**
	 * Tests whether this point is component-wise bigger than or equal to another point.
	 * 
	 * <p>Returns {@code true} if and only if {@code this.x >= other.x AND this.y >= other.y}.
	 * The z-coordinate is not considered in this comparison.</p>
	 *
	 * @param other the point to compare against
	 * @return true if this point is bigger or equal in both x and y, false otherwise
	 */
	boolean biggerThanOrEqualTo(IPoint other);

	/**
	 * Sets the location of this point to the specified coordinates.
	 * 
	 * <p>This method modifies the point in place and returns {@code this} for method chaining.</p>
	 * 
	 * <p><b>Note:</b> For immutable points, this method returns the point unchanged.</p>
	 *
	 * @param x the new x-coordinate
	 * @param y the new y-coordinate
	 * @param z the new z-coordinate
	 * @return this point (modified)
	 */
	IPoint setLocation(double x, double y, double z);

	/**
	 * Sets the location of this point from a JTS Coordinate.
	 * 
	 * <p>Copies the x, y, and z coordinates from the provided coordinate to this point.
	 * Modifies the point in place.</p>
	 * 
	 * <p><b>Note:</b> For immutable points, this method has no effect.</p>
	 *
	 * @param c the coordinate to copy from
	 */
	void setCoordinate(Coordinate c);

	/**
	 * Sets a specific ordinate (coordinate) of this point.
	 * 
	 * <p>The ordinate index follows JTS conventions:</p>
	 * <ul>
	 *   <li>0 or {@link Coordinate#X} - X-coordinate</li>
	 *   <li>1 or {@link Coordinate#Y} - Y-coordinate</li>
	 *   <li>2 or {@link Coordinate#Z} - Z-coordinate</li>
	 * </ul>
	 * 
	 * <p><b>Note:</b> For immutable points, this method has no effect.</p>
	 *
	 * @param i the ordinate index (0=x, 1=y, 2=z)
	 * @param v the new value for the ordinate
	 */
	void setOrdinate(int i, double v);

	/**
	 * Sets the x-coordinate of this point.
	 * 
	 * <p><b>Note:</b> For immutable points, this method has no effect.</p>
	 *
	 * @param xx the new x-coordinate
	 */
	void setX(double xx);

	/**
	 * Sets the y-coordinate of this point.
	 * 
	 * <p><b>Note:</b> For immutable points, this method has no effect.</p>
	 *
	 * @param yy the new y-coordinate
	 */
	void setY(double yy);

	/**
	 * Sets the z-coordinate of this point.
	 * 
	 * <p>If the provided value is {@link Double#NaN}, it is automatically converted to 0.0.</p>
	 * 
	 * <p><b>Note:</b> For immutable points, this method has no effect.</p>
	 *
	 * @param zz the new z-coordinate (NaN is converted to 0.0)
	 */
	void setZ(double zz);

	/**
	 * Returns the x-coordinate of this point.
	 *
	 * @return the x-coordinate as a double
	 */
	@getter (IKeyword.X)
	double getX();

	/**
	 * Returns the y-coordinate of this point.
	 *
	 * @return the y-coordinate as a double
	 */
	@getter (IKeyword.Y)
	double getY();

	/**
	 * Returns the z-coordinate of this point.
	 * 
	 * <p>For 2D points, this typically returns 0.0.</p>
	 *
	 * @return the z-coordinate as a double
	 */
	@getter (IKeyword.Z)
	double getZ();

	/**
	 * Returns whether this shape is a point.
	 * 
	 * <p>For {@code IPoint} instances, this always returns {@code true}.</p>
	 *
	 * @return true
	 */
	@Override
	boolean isPoint();

	/**
	 * Returns whether this shape is a line.
	 * 
	 * <p>For {@code IPoint} instances, this always returns {@code false}.</p>
	 *
	 * @return false
	 */
	@Override
	boolean isLine();

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	String toString();

	/**
	 * Serialize to gaml.
	 *
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	@Override
	String serializeToGaml(boolean includingBuiltIn);

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	@Override
	IPoint getLocation();

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@Override
	String stringValue(IScope scope);

	/**
	 * Adds another point to this point.
	 * 
	 * <p><b>Warning:</b> This method modifies the point in place and returns {@code this}.
	 * It does NOT create a new point. Use {@link #copy(gama.api.runtime.scope.IScope)} first if you need
	 * to preserve the original point.</p>
	 * 
	 * <p>The operation is: {@code this.x += loc.x; this.y += loc.y; this.z += loc.z}</p>
	 *
	 * @param loc the point to add
	 * @return this point (modified)
	 */
	IPoint add(IPoint loc);

	/**
	 * Adds specified offsets to this point's coordinates.
	 * 
	 * <p><b>Warning:</b> This method modifies the point in place and returns {@code this}.
	 * It does NOT create a new point.</p>
	 * 
	 * <p>The operation is: {@code this.x += ax; this.y += ay; this.z += az}</p>
	 *
	 * @param ax the offset to add to x-coordinate
	 * @param ay the offset to add to y-coordinate
	 * @param az the offset to add to z-coordinate
	 * @return this point (modified)
	 */
	IPoint add(double ax, double ay, double az);

	/**
	 * Subtracts another point from this point.
	 * 
	 * <p><b>Warning:</b> This method modifies the point in place and returns {@code this}.
	 * It does NOT create a new point. Use {@link #copy(gama.api.runtime.scope.IScope)} first if you need
	 * to preserve the original point.</p>
	 * 
	 * <p>The operation is: {@code this.x -= loc.x; this.y -= loc.y; this.z -= loc.z}</p>
	 *
	 * @param loc the point to subtract
	 * @return this point (modified)
	 */
	IPoint subtract(IPoint loc);

	/**
	 * Multiplies this point's coordinates by a scalar value.
	 * 
	 * <p><b>Warning:</b> This method modifies the point in place and returns {@code this}.
	 * It does NOT create a new point.</p>
	 * 
	 * <p>The operation is: {@code this.x *= value; this.y *= value; this.z *= value}</p>
	 *
	 * @param value the scalar to multiply by
	 * @return this point (modified)
	 */
	IPoint multiplyBy(double value);

	/**
	 * Divides this point's coordinates by a scalar value.
	 * 
	 * <p><b>Warning:</b> This method modifies the point in place and returns {@code this}.
	 * It does NOT create a new point.</p>
	 * 
	 * <p>The operation is: {@code this.x /= value; this.y /= value; this.z /= value}</p>
	 * 
	 * <p><b>Note:</b> Division by zero will result in infinite or NaN values.</p>
	 *
	 * @param value the scalar to divide by
	 * @return this point (modified)
	 */
	IPoint divideBy(double value);

	/**
	 * Tests equality with tolerance.
	 * 
	 * <p>Returns {@code true} if the Euclidean distance between this point and {@code c} is less than
	 * the specified tolerance. This is useful for comparing points with floating-point coordinates where
	 * exact equality is unreliable.</p>
	 * 
	 * <p>The comparison uses: {@code distance(this, c) < tolerance}</p>
	 *
	 * @param c the point to compare against
	 * @param tolerance the maximum distance for considering points equal
	 * @return true if points are within tolerance, false otherwise
	 * @since GAMA 1.9 (September 17, 2023)
	 * @author Alexis Drogoul
	 */
	boolean equalsWithTolerance(IPoint c, double tolerance);

	/**
	 * Times.
	 *
	 * @param d
	 *            the d
	 * @return the gama point
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = { IConcept.POINT })
	@doc (
			value = "Returns a point with coordinates multiplied by a number.")
	@test ("{2,5} * 4.0 = {8.0,20.0}")
	@test ("{2,5} * 0.0 = {0.0,0.0}")
	IPoint times(double d);

	/**
	 * Times.
	 *
	 * @param i
	 *            the i
	 * @return the i point
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = {})
	@doc (
			value = "Returns a point with coordinates multiplied by a number.",
			usages = @usage (
					value = "if the left-hand operator is a point and the right-hand a number, "
							+ "returns a point with coordinates multiplied by the number",
					examples = { @example (
							value = "{2,5} * 4",
							equals = "{8.0, 20.0}"),
							@example (
									value = "{2, 4} * 2.5",
									equals = "{5.0, 10.0}") }))
	@test ("{2,5} * 4 = {8,20}")
	@test ("{2,5} * 0 = {0,0}")
	default IPoint times(final Integer i) {
		return times(i.doubleValue());
	}

	/**
	 * Divided by.
	 *
	 * @param d
	 *            the d
	 * @return the gama point
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = { IConcept.POINT })
	@doc (
			value = "Returns a point with coordinates divided by the number",
			usages = @usage (
					value = "if the left operand is a point, returns a new point with coordinates divided by the right operand",
					examples = { @example (
							value = "{5, 7.5} / 2.5",
							equals = "{2, 3}"),
							@example (
									value = "{2,5} / 4",
									equals = "{0.5,1.25}") }))
	@test ("{5, 7.5} / 2.5 = {2,3}")
	IPoint dividedBy(double d);

	/**
	 * Divided by.
	 *
	 * @param i
	 *            the i
	 * @return the i point
	 */
	@operator (
			value = IKeyword.DIVIDE,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = {})
	@doc (
			value = "Returns a point with coordinates divided by the number")
	@test ("{2,5} / 4 = {0.5,1.25}")
	@test ("is_error({2,5} / 0)")
	default IPoint dividedBy(final Integer i) {
		return dividedBy(i.doubleValue());
	}

	/**
	 * Minus.
	 *
	 * @param other
	 *            the other
	 * @return the gama point
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = {})
	@doc (
			value = "Returns a point with coordinate resulting from the first operand minus the second operand.",
			usages = @usage (
					value = "if both operands are points, returns their difference (coordinates per coordinates).",
					examples = @example (
							value = "{1, 2} - {4, 5}",
							equals = "{-3.0, -3.0}")))
	IPoint minus(IPoint other);

	/**
	 * Minus.
	 *
	 * @param ax
	 *            the ax
	 * @param ay
	 *            the ay
	 * @param az
	 *            the az
	 * @return the gama point
	 */
	IPoint minus(double ax, double ay, double az);

	/**
	 * Minus.
	 *
	 * @param d
	 *            the d
	 * @return the i point
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = { IConcept.POINT })
	@doc (
			value = "Returns a point with coordinate resulting from the first operand minus the second operand.",
			usages = @usage (
					value = "if left-hand operand is a point and the right-hand a number, returns a new point with each coordinate as the difference of the operand coordinate with this number.",
					examples = { @example (
							value = "{1, 2} - 4.5",
							equals = "{-3.5, -2.5, -4.5}"),
							@example (
									value = "{1, 2} - 4",
									equals = "{-3.0,-2.0,-4.0}") }))
	default IPoint minus(final double d) {
		return minus(d, d, d);
	}

	/**
	 * Minus.
	 *
	 * @param i
	 *            the i
	 * @return the i point
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = {})
	@doc (
			value = "Returns a point with coordinate resulting from the first operand minus the second operand.",
			examples = { @example (
					value = "{2.0,3.0,4.0} - 1",
					equals = "{1.0,2.0,3.0}") })
	@test ("{2.0,3.0,4.0} - 1 = {1.0,2.0,3.0}")
	default IPoint minus(final Integer i) {
		return minus(i.doubleValue());
	}

	/**
	 * Plus.
	 *
	 * @param other
	 *            the other
	 * @return the gama point
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = { IConcept.POINT })
	@doc (
			value = "Returns a point with coordinate summing of the two operands.",
			usages = @usage (
					value = "if both operands are points, returns their sum.",
					examples = @example (
							value = "{1, 2} + {4, 5}",
							equals = "{5.0, 7.0}")))
	@test ("{1, 2} + {4, 5} = {5,7}")
	@test (
			value = "point p <- {1, 2}; p + {0, 0} = p",
			warning = true)
	IPoint plus(IPoint other);

	/**
	 * Plus.
	 *
	 * @param ax
	 *            the ax
	 * @param ay
	 *            the ay
	 * @param az
	 *            the az
	 * @return the gama point
	 */
	IPoint plus(double ax, double ay, double az);

	/**
	 * Plus.
	 *
	 * @param d
	 *            the d
	 * @return the i point
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = {})
	@doc (
			value = "Returns a point with coordinate summing of the two operands.",
			usages = @usage (
					value = "if the left-hand operand is a point and the right-hand a number, returns a new point with each coordinate as the sum of the operand coordinate with this number.",
					examples = { @example (
							value = "{1, 2} + 4.5",
							equals = "{5.5, 6.5,4.5}") }))
	default IPoint plus(final double d) {
		return plus(d, d, d);
	}

	/**
	 * Plus.
	 *
	 * @param i
	 *            the i
	 * @return the i point
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = {})
	@doc (
			value = "Returns a point with coordinate summing of the two operands.",
			examples = { @example (
					value = "{1, 2} + 4",
					equals = "{5.0, 6.0,4.0}") })
	default IPoint plus(final Integer i) {
		return plus(i.doubleValue());
	}

	/**
	 * Norm.
	 *
	 * @return the double
	 */
	@operator (
			value = "norm",
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = { IConcept.POINT })
	@doc (
			value = "the norm of the vector with the coordinates of the point operand.",
			examples = @example (
					value = "norm({3,4})",
					equals = "5.0"))
	@test (
			value = "norm({3,4}) = 5.0",
			name = "Regular")
	@test (
			value = "norm({1,1}) = sqrt(2)",
			name = "Not")
	@test ("norm({0,0}) = 0.0")
	@test ("norm({1,0}) = norm({0,1})")
	double norm();

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	int hashCode();

	/**
	 * Normalized.
	 *
	 * @return the gama point
	 */
	IPoint normalized();

	/**
	 * Normalize.
	 *
	 * @return the gama point
	 */
	IPoint normalize();

	/**
	 * Negated.
	 *
	 * @return the gama point
	 */
	@operator (
			value = IKeyword.MINUS,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = { IConcept.POINT })
	@doc (
			value = "Returns a point with coordinate resulting from the negation of the operand",
			usages = @usage (
					value = "",
					examples = { @example (
							value = "-{3.0,5.0}",
							equals = "{-3.0,-5.0}"),
							@example (
									value = "-{1.0,6.0,7.0}",
									equals = "{-1.0,-6.0,-7.0}") }))
	IPoint negated();

	/**
	 * Negate.
	 */
	void negate();

	/**
	 * Dot product with.
	 *
	 * @param v2
	 *            the v 2
	 */
	@operator (
			value = IKeyword.MULTIPLY,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			concept = {})
	@doc (
			value = "Returns the scalar product of two points (or dot product).",
			usages = @usage (
					value = "if both operands are points, returns their scalar product",
					examples = @example (
							value = "{2,5} * {4.5, 5}",
							equals = "34.0")))
	@test ("{2,5} * {4.5, 5} = 34.0")
	double dotProductWith(IPoint v2);

	/**
	 * Cross product with.
	 *
	 * @param v2
	 *            the other
	 * @return the gama point
	 */
	IPoint crossProductWith(IPoint v2);

	/**
	 * @return the point with y negated (for OpenGL, for example), without side effect on the point.
	 */
	IPoint yNegated();

	/**
	 * Gets the ordinate.
	 *
	 * @param i
	 *            the i
	 * @return the ordinate
	 */
	double getOrdinate(int i);

	/**
	 * Orthogonal.
	 *
	 * @return the gama point
	 */
	IPoint orthogonal();

	/**
	 * With precision.
	 *
	 * @param i
	 *            the i
	 * @return the i point
	 */
	@operator (
			value = "with_precision",
			can_be_const = true,
			concept = { IConcept.POINT })
	@doc (
			value = "Rounds off the ordinates of the left-hand point to the precision given by the value of right-hand operand",
			examples = { @example (
					value = "{12345.78943, 12345.78943, 12345.78943} with_precision 2 ",
					equals = "{12345.79, 12345.79, 12345.79}") },
			see = "round")
	/**
	 * With precision.
	 *
	 * @param i
	 *            the i
	 * @return the gama point
	 */
	IPoint withPrecision(int i);

	/**
	 * Clone.
	 *
	 * @return the i point
	 */
	IPoint clone();

	/**
	 * Rounded.
	 *
	 * @return the gamap point
	 */
	@operator (
			value = "round",
			can_be_const = true,
			concept = { IConcept.POINT })
	@doc (
			value = "Returns the rounded value of the operand.",
			examples = { @example (
					value = "{12345.78943,  12345.78943, 12345.78943} with_precision 2",
					equals = "{12345.79,12345.79,12345.79}") },
			see = "round")
	@test ("{12345.78943,  12345.78943, 12345.78943} with_precision 2 = {12345.79,12345.79,12345.79}")

	IPoint rounded();

	/**
	 * Checks if is null.
	 *
	 * @return true, if is null
	 */
	boolean isNull();

	/**
	 * Serialize to json.
	 *
	 * @param json
	 *            the json
	 * @return the i json value
	 */
	@Override
	IJsonValue serializeToJson(IJson json);

	/**
	 * Translated to.
	 *
	 * @param scope
	 *            the scope
	 * @param absoluteLocation
	 *            the absolute location
	 * @return the i shape
	 */
	@Override
	IShape translatedTo(IScope scope, IPoint absoluteLocation);

	/**
	 * @param fp
	 * @return
	 */
	boolean equals3D(IPoint fp);

	/**
	 * Distance 3 D.
	 *
	 * @param fp
	 *            the fp
	 * @return the double
	 */
	double distance3D(IPoint fp);

	/**
	 * @return
	 */
	Coordinate toCoordinate();

	/**
	 * Dispose.
	 */
	@Override
	default void dispose() {}

	/**
	 *
	 */
	default void negateY() {
		setY(-getY());
	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i point
	 */
	@Override
	IPoint copy(IScope scope);

	/**
	 * Compare to.
	 *
	 * @param p
	 *            the p
	 * @return the i point
	 */
	@Override
	int compareTo(Coordinate p);

	/**
	 * Compare to.
	 *
	 * @param p
	 *            the p
	 * @return the int
	 */
	default int compareTo(final IPoint p) {
		return compareTo(p.toCoordinate());
	}

	/**
	 *
	 * Distance in 2D
	 *
	 * @param pt2
	 * @return
	 */
	double distance(IPoint pt2);

	/**
	 * Distance in 2D.
	 *
	 * @param c
	 *            the c
	 * @return the double
	 */
	double distance(Coordinate c);

	/**
	 * @param current
	 * @param tOLERANCE
	 * @return
	 */
	boolean equals2D(IPoint current, double tolerance);

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	default IType<IPoint> getGamlType() { return Types.POINT; }

}