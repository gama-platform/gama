/*******************************************************************************************************
 *
 * GamaPointFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.geometry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IOperatorCategory;
import gama.api.annotations.validator;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaPointType;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.pair.IPair;

/**
 * A static factory for creating and managing {@link IPoint} instances.
 * 
 * <p>This class provides the primary API for creating 3D points in GAMA, offering multiple creation methods
 * from various sources including coordinates, coordinate sequences, GAML strings, maps, and other types.
 * It handles both mutable and immutable point creation.</p>
 * 
 * <h2>Factory Methods</h2>
 * 
 * <h3>Basic Creation</h3>
 * <ul>
 *   <li>{@link #create()} - Creates a point at origin (0,0,0)</li>
 *   <li>{@link #create(double, double)} - Creates a 2D point (z=0)</li>
 *   <li>{@link #create(double, double, double)} - Creates a 3D point</li>
 *   <li>{@link #create(IPoint)} - Creates a copy of an existing point</li>
 *   <li>{@link #create(Coordinate)} - Creates from JTS Coordinate</li>
 * </ul>
 * 
 * <h3>Immutable Points</h3>
 * <ul>
 *   <li>{@link #createImmutable(double, double, double)} - Creates immutable 3D point</li>
 *   <li>{@link #createImmutable(double, double)} - Creates immutable 2D point</li>
 *   <li>{@link #createImmutable(IPoint)} - Creates immutable copy</li>
 *   <li>{@link #createImmutable(Coordinate)} - Creates immutable from Coordinate</li>
 * </ul>
 * 
 * <h3>Type Conversion</h3>
 * <ul>
 *   <li>{@link #castToPoint(IScope, Object, boolean)} - Converts various types to point</li>
 *   <li>{@link #createFromGamlString(IScope, String)} - Parses GAML point syntax</li>
 *   <li>{@link #createFromXYZMap(IScope, Map)} - Creates from map with x,y,z keys</li>
 * </ul>
 * 
 * <h2>GAML Integration</h2>
 * <p>This class provides the {@code point} operator used in GAML:</p>
 * <pre>
 * point p1 &lt;- point(10, 20);         // 2D point
 * point p2 &lt;- point(10, 20, 5);      // 3D point
 * point p3 &lt;- {10, 20, 5};           // Literal syntax (calls castToPoint)
 * </pre>
 * 
 * <h2>Type Casting</h2>
 * <p>The {@link #castToPoint(IScope, Object, boolean)} method handles conversion from various types:</p>
 * <ul>
 *   <li><b>IPoint:</b> Returns directly or creates copy</li>
 *   <li><b>IShape:</b> Returns the shape's location</li>
 *   <li><b>List:</b> Interprets elements as x, y, z coordinates</li>
 *   <li><b>IColor:</b> Creates point from RGB values</li>
 *   <li><b>Map:</b> Extracts "x", "y", "z" keys</li>
 *   <li><b>String:</b> Parses GAML syntax like "{1.0, 2.0, 3.0}"</li>
 *   <li><b>IPair:</b> Uses first and last as x and y</li>
 *   <li><b>Number:</b> Creates point with same value for x, y, and z</li>
 * </ul>
 * 
 * <h2>Immutable vs Mutable Points</h2>
 * <p>By default, factory methods create mutable {@link GamaPoint} instances. For immutable points
 * (useful for constants, shared references, or thread-safe code), use the {@code createImmutable} methods
 * which return {@link Immutable} instances.</p>
 * 
 * <p>Immutable points ignore all setter operations and return themselves unchanged from modification methods.</p>
 * 
 * <h2>Thread Safety</h2>
 * <p>All factory methods are thread-safe. However, the created mutable points themselves are NOT thread-safe.
 * For concurrent usage, create immutable points or provide external synchronization.</p>
 * 
 * <h2>Example Usage</h2>
 * <pre>
 * // Create mutable points
 * IPoint origin = GamaPointFactory.create();
 * IPoint point2D = GamaPointFactory.create(10.0, 20.0);
 * IPoint point3D = GamaPointFactory.create(10.0, 20.0, 5.0);
 * 
 * // Create immutable points
 * IPoint constPoint = GamaPointFactory.createImmutable(10.0, 20.0, 5.0);
 * 
 * // Type conversion
 * IPoint fromList = GamaPointFactory.castToPoint(scope, Arrays.asList(10, 20, 5));
 * IPoint fromString = GamaPointFactory.createFromGamlString(scope, "{10, 20, 5}");
 * IPoint fromColor = GamaPointFactory.castToPoint(scope, someColor);
 * </pre>
 * 
 * @author drogoul
 * @see IPoint
 * @see GamaPoint
 * @see GamaPointFactory.Immutable
 * @since GAMA 1.0
 */
public class GamaPointFactory {

	/**
	 * Immutable point implementation that ignores all modification attempts.
	 * 
	 * <p>This class extends {@link GamaPoint} but overrides all setter and modification methods to be no-ops,
	 * making instances effectively immutable. Immutable points are useful for:</p>
	 * <ul>
	 *   <li>Constants and default values</li>
	 *   <li>Shared references across multiple objects</li>
	 *   <li>Thread-safe code without synchronization</li>
	 *   <li>Preventing accidental modifications</li>
	 * </ul>
	 * 
	 * <p>All modification methods ({@code setX}, {@code add}, {@code multiply}, etc.) return {@code this}
	 * unchanged, maintaining the method chaining API while preventing state changes.</p>
	 * 
	 * <h3>Usage</h3>
	 * <p>Create immutable points using factory methods:</p>
	 * <pre>
	 * IPoint immutable = GamaPointFactory.createImmutable(10, 20, 5);
	 * immutable.setX(100);  // No effect
	 * immutable.add(offset);  // No effect
	 * </pre>
	 * 
	 * @see GamaPointFactory#createImmutable(double, double, double)
	 */
	public static class Immutable extends GamaPoint {

		/**
		 * Constructs a new immutable point with the specified coordinates.
		 * 
		 * <p>This constructor is public but typically should be accessed via
		 * {@link GamaPointFactory#createImmutable(double, double, double)}.</p>
		 *
		 * @param x the x-coordinate
		 * @param y the y-coordinate
		 * @param z the z-coordinate
		 */
		public Immutable(final double x, final double y, final double z) {
			super(x, y, z);
		}

		/**
		 * Sets the location.
		 *
		 * @param al
		 *            the al
		 * @return the i point
		 */
		@Override
		public Immutable setLocation(final IPoint al) {
			return this;
		}

		@Override
		public Immutable setLocation(final double x, final double y, final double z) {
			return this;
		}

		@Override
		public void setCoordinate(final Coordinate c) {}

		@Override
		public void setOrdinate(final int i, final double v) {}

		@Override
		public void setX(final double xx) {}

		@Override
		public void setY(final double yy) {}

		@Override
		public void setZ(final double zz) {}

		/**
		 * Adds the.
		 *
		 * @param loc
		 *            the loc
		 * @return the i point
		 */
		@Override
		public Immutable add(final IPoint loc) {
			return this;
		}

		@Override
		public Immutable add(final double ax, final double ay, final double az) {
			return this;
		}

		/**
		 * Subtract.
		 *
		 * @param loc
		 *            the loc
		 * @return the i point
		 */
		@Override
		public Immutable subtract(final IPoint loc) {
			return this;
		}

		@Override
		public Immutable multiplyBy(final double value) {
			return this;
		}

		@Override
		public Immutable divideBy(final double value) {
			return this;
		}

		@Override
		public void setGeometry(final IShape g) {}

		@Override
		public void setInnerGeometry(final Geometry point) {

		}

		@Override
		public IPoint normalize() {
			return this;
		}

		@Override
		public void negate() {}

		@Override
		public void setDepth(final double depth) {}

	}

	/** The Constant NULL_POINT. */
	private static IPoint NULL_POINT = createImmutable(0, 0, 0);

	/**
	 * Creates an immutable point with specified coordinates.
	 *
	 * @param x
	 *            the X coordinate.
	 * @param y
	 *            the Y coordinate.
	 * @param z
	 *            the Z coordinate.
	 * @return an immutable {@link IPoint}.
	 */
	public static IPoint createImmutable(final double x, final double y, final double z) {
		return new Immutable(x, y, z);
	}

	/**
	 * Creates an immutable point with specified X and Y coordinates (Z=0).
	 *
	 * @param x
	 *            the X coordinate.
	 * @param y
	 *            the Y coordinate.
	 * @return an immutable {@link IPoint}.
	 */
	public static IPoint createImmutable(final double x, final double y) {
		return new Immutable(x, y, 0);
	}

	/**
	 * Creates an immutable point from another point.
	 *
	 * @param p
	 *            the source point.
	 * @return an immutable {@link IPoint}.
	 */
	public static IPoint createImmutable(final IPoint p) {
		return new Immutable(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Creates an immutable point from a JTS Coordinate.
	 *
	 * @param p
	 *            the source coordinate.
	 * @return an immutable {@link IPoint}.
	 */
	public static IPoint createImmutable(final Coordinate p) {
		return new Immutable(p.x, p.y, p.z);
	}

	/**
	 * Creates a default point (usually 0,0,0).
	 *
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create() {
		return new GamaPoint(0, 0, 0);
	}

	/**
	 * Creates a point with specified X and Y coordinates (Z=0).
	 *
	 * @param x
	 *            the X coordinate.
	 * @param y
	 *            the Y coordinate.
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create(final double x, final double y) {
		return new GamaPoint(x, y, 0);
	}

	/**
	 * Creates a point with specified X, Y, and Z coordinates.
	 *
	 * @param x
	 *            the X coordinate.
	 * @param y
	 *            the Y coordinate.
	 * @param z
	 *            the Z coordinate.
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create(final double x, final double y, final double z) {
		return new GamaPoint(x, y, z);
	}

	/**
	 * Creates a point from another point (copy).
	 *
	 * @param p
	 *            the source point.
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create(final IPoint p) {
		return new GamaPoint(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Creates a point from a JTS Coordinate.
	 *
	 * @param p
	 *            the source coordinate.
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create(final Coordinate p) {
		return new GamaPoint(p.x, p.y, p.z);
	}

	/**
	 * Creates a point from a Map containing "x", "y", "z" keys.
	 *
	 * @param scope
	 *            the execution scope (for casting).
	 * @param m
	 *            the map containing coordinates.
	 * @return the created {@link IPoint}.
	 */
	public static IPoint createFromXYZMap(final IScope scope, final Map m) {
		final double x = Cast.asFloat(scope, m.get("x"));
		final double y = Cast.asFloat(scope, m.get("y"));
		final double z = Cast.asFloat(scope, m.get("z"));
		return new GamaPoint(x, y, z);
	}

	/**
	 * Creates a point from a GAML string representation (e.g., "{1.0, 2.0, 3.0}").
	 *
	 * @param scope
	 *            the execution scope.
	 * @param s
	 *            the string representation.
	 * @return the created {@link IPoint}.
	 */
	public static IPoint createFromGamlString(final IScope scope, final String s) {
		String str = s.trim();
		if (str.startsWith("{") && str.endsWith("}")) {
			str = str.replace("{", "").replace("}", "").trim();
			return castToPoint(scope, Arrays.asList(str.split(",")), false);
		}
		Double v = Cast.asFloat(scope, str);
		if (v != null) return new GamaPoint(v, v, v);
		throw GamaRuntimeException.error("Cannot cast " + s + " into a point", scope);
	}

	/**
	 * Internal helper to create a point from a list.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param l
	 *            the list of coordinates.
	 * @param copy
	 *            whether to copy if the list contains a point.
	 * @return the created {@link IPoint}.
	 */
	private static IPoint createFromList(final IScope scope, final List l, final boolean copy) {
		if (l.size() > 2) return GamaPointFactory.create(Cast.asFloat(scope, l.get(0)), Cast.asFloat(scope, l.get(1)),
				Cast.asFloat(scope, l.get(2)));
		if (l.size() > 1) return GamaPointFactory.create(Cast.asFloat(scope, l.get(0)), Cast.asFloat(scope, l.get(1)));
		if (l.size() > 0) return castToPoint(scope, l.get(0), copy);
		return GamaPointFactory.create(0, 0, 0);
	}

	/**
	 * Retrieves the shared null point instance.
	 *
	 * @return the null point.
	 */
	public static IPoint getNullPoint() { return NULL_POINT; }

	/**
	 * GAML operator to create a point from 3 expressions.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param xExp
	 *            expression for X.
	 * @param yExp
	 *            expression for Y.
	 * @param zExp
	 *            expression for Z.
	 * @return the calculated {@link IPoint}.
	 */
	@operator (
			value = IKeyword.POINT,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			internal = true)
	@validator (GamaPointType.PointValidator.class)
	@no_test
	public static IPoint create(final IScope scope, final IExpression xExp, final IExpression yExp,
			final IExpression zExp) {
		final double x = Cast.asFloat(scope, xExp.value(scope));
		final double y = Cast.asFloat(scope, yExp.value(scope));
		final double z = Cast.asFloat(scope, zExp.value(scope));
		return new GamaPoint(x, y, z);
	}

	/**
	 * GAML operator to create a point from 2 expressions (Z=0).
	 *
	 * @param scope
	 *            the execution scope.
	 * @param xExp
	 *            expression for X.
	 * @param yExp
	 *            expression for Y.
	 * @return the calculated {@link IPoint}.
	 */
	@operator (
			value = IKeyword.POINT,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			internal = true)
	@validator (GamaPointType.PointValidator.class)
	@no_test
	public static IPoint create(final IScope scope, final IExpression xExp, final IExpression yExp) {
		final double x = Cast.asFloat(scope, xExp.value(scope));
		final double y = Cast.asFloat(scope, yExp.value(scope));
		return new GamaPoint(x, y, 0);
	}

	/**
	 * Converts an arbitrary object into a point.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert (point, shape, list, map, string, pair).
	 * @param copy
	 *            whether to copy if object is already a point.
	 * @return the resulting {@link IPoint}.
	 */
	public static IPoint castToPoint(final IScope scope, final Object obj, final boolean copy) {
		return switch (obj) {
			case null -> null;
			case IPoint gp -> copy ? GamaPointFactory.create(gp) : gp;
			case IShape s -> s.getLocation();
			case List l -> createFromList(scope, l, copy);
			case IColor c -> create(c.red(), c.green(), c.blue());
			case Map m -> createFromXYZMap(scope, m);
			case String s -> createFromGamlString(scope, s);
			case IPair p -> create(Cast.asFloat(scope, p.first()), Cast.asFloat(scope, p.last()));
			default -> {
				Double dval = Cast.asFloat(scope, obj);
				yield create(dval, dval, dval);
			}
		};
	}

	/**
	 * Convenience method to convert an object to a point without copying if possible.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert.
	 * @return the resulting {@link IPoint}.
	 */
	public static IPoint castToPoint(final IScope scope, final Object obj) {
		return castToPoint(scope, obj, false);
	}

}