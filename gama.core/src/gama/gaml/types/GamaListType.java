/*******************************************************************************************************
 *
 * GamaListType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import java.awt.Color;
import java.util.Collection;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaDate;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.expressions.IExpression;

/**
 * The Class GamaListType.
 */
@type (
		name = IKeyword.LIST,
		id = IType.LIST,
		wraps = { IList.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.LIST },
		doc = @doc ("Ordered collection of values or agents"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaListType extends GamaContainerType<IList> {

	@Override
	public IList cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return staticCast(scope, obj, contentsType, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param ct
	 *            the ct
	 * @param copy
	 *            the copy
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IList staticCast(final IScope scope, final Object obj, final IType ct, final boolean copy)
			throws GamaRuntimeException {
		final IType contentsType = ct == null ? Types.NO_TYPE : ct;
		return switch (obj) {
			case null -> GamaListFactory.create(contentsType, 0);
			case GamaDate gd -> gd.listValue(scope, contentsType);
			// Explicitly set copy to true if we deal with a population
			case IPopulation ip -> ip.listValue(scope, contentsType, true);
			case IContainer ic -> ic.listValue(scope, contentsType, copy);
			case Collection coll -> GamaListFactory.create(scope, contentsType, coll);
			case Color c -> GamaListFactory.create(scope, contentsType,
					new int[] { c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() });
			case GamaPoint point -> GamaListFactory.create(scope, contentsType,
					new double[] { point.x, point.y, point.z });
			case String s -> GamaListFactory.create(scope, contentsType, s.toCharArray());
			default -> GamaListFactory.create(scope, contentsType, obj);
		};

	}

	@Override
	public IType getKeyType() { return Types.get(INT); }

	@Override
	public IType contentsTypeIfCasting(final IExpression expr) {
		switch (expr.getGamlType().id()) {
			case COLOR:
			case DATE:
				return Types.get(INT);
			case POINT:
				return Types.get(FLOAT);
		}
		return super.contentsTypeIfCasting(expr);
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}
}
