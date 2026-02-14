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
package gama.api.data.factories;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;

import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IOperatorCategory;
import gama.api.annotations.validator;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IPair;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaPointType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.InternalGamaPointFactory;

/**
 * A static factory for creating and managing {@link IPoint} instances. This class handles the creation of 3D points,
 * both mutable and immutable (though GAMA points are generally treated as values). It supports creation from
 * coordinates, coordinates sequences, GAML strings, and other sources. It delegates to an {@link IPointFactory}.
 */
public class GamaPointFactory implements IFactory<IPoint> {

	/**
	 * The internal factory used for creating point instances.
	 */
	private static IPointFactory InternalFactory = new InternalGamaPointFactory();

	/**
	 * Configures the internal factory and initializes the NULL_POINT constant.
	 *
	 * @param builder
	 *            the {@link IPointFactory} to be used as the internal builder.
	 */
	public static void setBuilder(final IPointFactory builder) { InternalFactory = builder; }

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
		return InternalFactory.createImmutable(x, y, z);
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
		return InternalFactory.createImmutable(x, y, 0);
	}

	/**
	 * Creates an immutable point from another point.
	 *
	 * @param p
	 *            the source point.
	 * @return an immutable {@link IPoint}.
	 */
	public static IPoint createImmutable(final IPoint p) {
		return InternalFactory.createImmutable(p);
	}

	/**
	 * Creates an immutable point from a JTS Coordinate.
	 *
	 * @param p
	 *            the source coordinate.
	 * @return an immutable {@link IPoint}.
	 */
	public static IPoint createImmutable(final Coordinate p) {
		return InternalFactory.createImmutable(p);
	}

	/**
	 * Creates a default point (usually 0,0,0).
	 *
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create() {
		return InternalFactory.create();
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
		return InternalFactory.create(x, y);
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
		return InternalFactory.create(x, y, z);
	}

	/**
	 * Creates a point from another point (copy).
	 *
	 * @param p
	 *            the source point.
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create(final IPoint p) {
		return InternalFactory.create(p);
	}

	/**
	 * Creates a point from a JTS Coordinate.
	 *
	 * @param p
	 *            the source coordinate.
	 * @return a new {@link IPoint}.
	 */
	public static IPoint create(final Coordinate p) {
		return InternalFactory.create(p);
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
		return GamaPointFactory.create(x, y, z);
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
			return toPoint(scope, Arrays.asList(str.split(",")), false);
		}
		Double v = Cast.asFloat(scope, str);
		if (v != null) return GamaPointFactory.create(v, v, v);
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
		if (l.size() > 0) return toPoint(scope, l.get(0), copy);
		return GamaPointFactory.create(0, 0, 0);
	}

	/**
	 * Retrieves the shared null point instance.
	 *
	 * @return the null point.
	 */
	public static IPoint getNullPoint() { return InternalFactory.getNullPoint(); }

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
		return GamaPointFactory.create(x, y, z);
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
		return GamaPointFactory.create(x, y);
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
	public static IPoint toPoint(final IScope scope, final Object obj, final boolean copy) {
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
	public static IPoint toPoint(final IScope scope, final Object obj) {
		return toPoint(scope, obj, false);
	}

}