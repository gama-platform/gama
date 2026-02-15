/*******************************************************************************************************
 *
 * GamaMatrixType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMatrixFactory;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.GamaPointFactory;

/**
 * The Class GamaMatrixType.
 */
@type (
		name = IKeyword.MATRIX,
		id = IType.MATRIX,
		wraps = { IMatrix.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.MATRIX },
		doc = @doc ("Matrices are 2-dimensional containers that can contain any type of date (not only floats or integers). They can be accessed with a point index or by rows / columns"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMatrixType extends GamaContainerType<IMatrix> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaMatrixType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	public IMatrix cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return GamaMatrixFactory.createFrom(scope, obj, param, contentsType, copy);
	}

	@Override
	public IType getKeyType() { return Types.POINT; }

	@Override
	public boolean isFixedLength() { return true; }

	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		final IType cType = itemType.getContentType();
		if (itemType.id() == IType.LIST && cType.id() == IType.LIST) {
			// cf. issue #3792 -- the computation of type is now taken in charge by ListExpression itself
			// if (exp instanceof ListExpression) {
			// final IExpression[] array = ((ListExpression) exp).getElements();
			// if (array.length == 0) return Types.NO_TYPE;
			// return array[0].getGamlType().getContentType();
			// }
			if (!(exp instanceof IExpression.Map me)) return cType.getContentType();
			final IExpression[] array = me.getValues();
			if (array.length == 0) return Types.NO_TYPE;
			return array[0].getGamlType().getContentType();
		}
		if (Types.CONTAINER.isAssignableFrom(itemType)) return itemType.getContentType();
		return itemType;
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public IMatrix deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		IType requested = (IType) map2.remove("requested_type");
		IList contents = (IList) map2.get("contents");
		Integer x = (Integer) map2.get("cols");
		Integer y = (Integer) map2.get("rows");
		IPoint size = GamaPointFactory.create(x, y);
		return GamaMatrixFactory.createFrom(scope, contents, requested.getContentType(), size);
	}

}
