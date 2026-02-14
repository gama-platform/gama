/*******************************************************************************************************
 *
 * IPoint.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.objects;

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
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonValue;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IIntersectable;

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
 * Interface for 3D points.
 */

public interface IPoint extends IShape, IIntersectable, Cloneable, Comparable<Coordinate> {

	/**
	 * Smaller than.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	boolean smallerThan(IPoint other);

	/**
	 * Smaller than or equal to.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	boolean smallerThanOrEqualTo(IPoint other);

	/**
	 * Bigger than.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	boolean biggerThan(IPoint other);

	/**
	 * Bigger than or equal to.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	boolean biggerThanOrEqualTo(IPoint other);

	/**
	 * Sets the location.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the gama point
	 */
	IPoint setLocation(double x, double y, double z);

	/**
	 * Sets the coordinate.
	 *
	 * @param c
	 *            the new coordinate
	 */
	void setCoordinate(Coordinate c);

	/**
	 * Sets the ordinate.
	 *
	 * @param i
	 *            the i
	 * @param v
	 *            the v
	 */
	void setOrdinate(int i, double v);

	/**
	 * Sets the x.
	 *
	 * @param xx
	 *            the new x
	 */
	void setX(double xx);

	/**
	 * Sets the y.
	 *
	 * @param yy
	 *            the new y
	 */
	void setY(double yy);

	/**
	 * Sets the z.
	 *
	 * @param zz
	 *            the new z
	 */
	void setZ(double zz);

	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	@getter (IKeyword.X)
	double getX();

	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	@getter (IKeyword.Y)
	double getY();

	/**
	 * Gets the z.
	 *
	 * @return the z
	 */
	@getter (IKeyword.Z)
	double getZ();

	/**
	 * Checks if is point.
	 *
	 * @return true, if is point
	 */
	@Override
	boolean isPoint();

	/**
	 * Checks if is line.
	 *
	 * @return true, if is line
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
	 * Adds the.
	 *
	 * @param loc
	 *            the loc
	 * @return the gama point
	 */
	IPoint add(IPoint loc);

	/**
	 * Adds the.
	 *
	 * @param ax
	 *            the ax
	 * @param ay
	 *            the ay
	 * @param az
	 *            the az
	 * @return the gama point
	 */
	IPoint add(double ax, double ay, double az);

	/**
	 * Subtract.
	 *
	 * @param loc
	 *            the loc
	 * @return the gama point
	 */
	IPoint subtract(IPoint loc);

	/**
	 * Multiply by.
	 *
	 * @param value
	 *            the value
	 * @return the gama point
	 */
	IPoint multiplyBy(double value);

	/**
	 * Divide by.
	 *
	 * @param value
	 *            the value
	 * @return the gama point
	 */
	IPoint divideBy(double value);

	/**
	 * Equals with tolerance.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @param tolerance
	 *            the tolerance
	 * @return true, if successful
	 * @date 17 sept. 2023
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