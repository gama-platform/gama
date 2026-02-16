/*******************************************************************************************************
 *
 * GamaPointType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.validation.IOperatorValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.map.IMap;

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

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaPointType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("""
			Transforms the parameter into a point. If it is already a point, returns it. \
			If it is a geometry, returns its location. If it is a list, interprets its elements as float values and use up to the first 3 ones to return a point. \
			If it is a map, tries to find 'x', 'y' and 'z' keys in it. If it is a number, returns a point with the x, y and equal to this value""")
	public IPoint cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return GamaPointFactory.castToPoint(scope, obj, copy);
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
		return GamaPointFactory.createFromXYZMap(scope, map2);
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

}
