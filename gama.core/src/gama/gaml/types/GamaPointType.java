/*******************************************************************************************************
 *
 * GamaPointType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.IPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaPair;
import gama.core.util.map.IMap;
import gama.gaml.compilation.IOperatorValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@type (
		name = IKeyword.POINT,
		id = IType.POINT,
		wraps = { IPoint.class },
		kind = ISymbolKind.Variable.NUMBER,
		concept = { IConcept.TYPE, IConcept.POINT },
		doc = @doc ("Represent locations in either 2 or 3 dimensions"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPointType extends GamaType<IPoint> {

	@Override
	@doc ("""
			Transforms the parameter into a point. If it is already a point, returns it. \
			If it is a geometry, returns its location. If it is a list, interprets its elements as float values and use up to the first 3 ones to return a point. \
			If it is a map, tries to find 'x', 'y' and 'z' keys in it. If it is a number, returns a point with the x, y and equal to this value""")
	public IPoint cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param copy
	 *            the copy
	 * @return the gama point
	 */
	public static IPoint staticCast(final IScope scope, final Object obj, final boolean copy) {
		return switch (obj) {
			case null -> null;
			case IPoint gp -> copy ? GamaPointFactory.create(gp) : gp;
			case IShape s -> s.getLocation();
			case List l -> castFromList(scope, l, copy);
			case GamaColor c -> GamaPointFactory.create(c.red(), c.green(), c.blue());
			case Map m -> castFromMap(scope, m);
			case String s -> castFromString(scope, s);
			case GamaPair p -> GamaPointFactory.create(Cast.asFloat(scope, p.first()), Cast.asFloat(scope, p.last()));
			default -> {
				Double dval = Cast.asFloat(scope, obj);
				yield GamaPointFactory.create(dval, dval, dval);
			}
		};
	}

	/**
	 * Cast from map.
	 *
	 * @param scope
	 *            the scope
	 * @param m
	 *            the m
	 * @return the gama point
	 */
	private static IPoint castFromMap(final IScope scope, final Map m) {
		final double x = Cast.asFloat(scope, m.get("x"));
		final double y = Cast.asFloat(scope, m.get("y"));
		final double z = Cast.asFloat(scope, m.get("z"));
		return GamaPointFactory.create(x, y, z);
	}

	/**
	 * Cast from string.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @return the gama point
	 */
	private static IPoint castFromString(final IScope scope, final String s) {
		String str = s.trim();
		if (str.startsWith("{") && str.endsWith("}")) {
			str = str.replace("{", "").replace("}", "").trim();
			return staticCast(scope, Arrays.asList(str.split(",")), false);
		}
		Double v = Cast.asFloat(scope, str);
		if (v != null) return GamaPointFactory.create(v, v, v);
		throw GamaRuntimeException.error("Cannot cast " + s + " into a point", scope);
	}

	/**
	 * Cast from list.
	 *
	 * @param scope
	 *            the scope
	 * @param l
	 *            the l
	 * @param copy
	 *            the copy
	 * @return the gama point
	 */
	private static IPoint castFromList(final IScope scope, final List l, final boolean copy) {
		if (l.size() > 2) return GamaPointFactory.create(Cast.asFloat(scope, l.get(0)), Cast.asFloat(scope, l.get(1)),
				Cast.asFloat(scope, l.get(2)));
		if (l.size() > 1) return GamaPointFactory.create(Cast.asFloat(scope, l.get(0)), Cast.asFloat(scope, l.get(1)));
		if (l.size() > 0) return staticCast(scope, l.get(0), copy);
		return GamaPointFactory.create(0, 0, 0);
	}

	@Override
	public IPoint getDefault() { return null; }

	@Override
	public IType getContentType() { return Types.get(FLOAT); }

	@Override
	public IType getKeyType() { return Types.get(INT); }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public boolean isDrawable() { return true; }

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public IPoint deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return GamaPointFactory.create(Cast.asFloat(scope, map2.get("x")), Cast.asFloat(scope, map2.get("y")),
				Cast.asFloat(scope, map2.get("z")));
	}

	/**
	 * The Class PointValidator.
	 */
	public static class PointValidator implements IOperatorValidator {

		@Override
		public boolean validate(final IDescription context, final EObject emfContext, final IExpression... arguments) {
			for (final IExpression expr : arguments) {
				if (!expr.getGamlType().isNumber()) {
					context.error("Points can only be built with int or float coordinates", WRONG_TYPE, emfContext);
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * To point.
	 *
	 * @param scope
	 *            the scope
	 * @param xExp
	 *            the x exp
	 * @param yExp
	 *            the y exp
	 * @return the gama point
	 */
	@operator (
			value = IKeyword.POINT,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			internal = true)
	@validator (PointValidator.class)
	@no_test
	public static IPoint toPoint(final IScope scope, final IExpression xExp, final IExpression yExp) {
		// boolean isGraphics = scope instanceof IGraphicsScope;
		// if (isGraphics) { ((IGraphicsScope) scope).setHorizontalPixelContext(); }
		final double x = Cast.asFloat(scope, xExp.value(scope));
		// if (isGraphics) { ((IGraphicsScope) scope).setVerticalPixelContext(); }
		final double y = Cast.asFloat(scope, yExp.value(scope));
		return GamaPointFactory.create(x, y);
	}

	/**
	 * To point.
	 *
	 * @param scope
	 *            the scope
	 * @param xExp
	 *            the x exp
	 * @param yExp
	 *            the y exp
	 * @param zExp
	 *            the z exp
	 * @return the gama point
	 */
	@operator (
			value = IKeyword.POINT,
			can_be_const = true,
			category = IOperatorCategory.POINT,
			internal = true)
	@validator (PointValidator.class)
	@no_test
	public static IPoint toPoint(final IScope scope, final IExpression xExp, final IExpression yExp,
			final IExpression zExp) {
		// boolean isGraphics = scope instanceof IGraphicsScope;
		// if (isGraphics) { ((IGraphicsScope) scope).setHorizontalPixelContext(); }
		final double x = Cast.asFloat(scope, xExp.value(scope));
		// if (isGraphics) { ((IGraphicsScope) scope).setVerticalPixelContext(); }
		final double y = Cast.asFloat(scope, yExp.value(scope));
		final double z = Cast.asFloat(scope, zExp.value(scope));
		return GamaPointFactory.create(x, y, z);
	}

}
