/*******************************************************************************************************
 *
 * GamaContainerType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.expressions.IExpression;

/**
 * Written by drogoul Modified on 11 nov. 2011
 *
 * A generic type for containers. Tentative.
 *
 */
@type (
		name = IKeyword.CONTAINER,
		id = IType.CONTAINER,
		wraps = { IContainer.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER },
		doc = @doc ("Generic super-type of all the container types (list, graph, matrix, etc.)"))
public class GamaContainerType<T extends IContainer<?, ?>> extends GamaType<T> implements IContainerType<T> {

	@doc ("Allows to cast the argument to a container. If the argument is already a container, returns it, otherwise cast it to a list")
	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return cast(scope, obj, param, getKeyType(), getContentType(), copy);
		// return (T) (obj instanceof IContainer ? (IContainer) obj :
		// Types.get(LIST).cast(scope, obj, null,
		// Types.NO_TYPE, Types.NO_TYPE));
	}

	@Override
	public int getNumberOfParameters() { return 1; }

	@SuppressWarnings ("unchecked")
	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) throws GamaRuntimeException {
		// by default
		return (T) (obj instanceof IContainer ? (IContainer<?, ?>) obj
				: (IList<?>) Types.get(LIST).cast(scope, obj, null, copy));
	}

	@Override
	public T getDefault() { return null; }

	@Override
	public IContainerType<T> getGamlType() { return this; }

	@Override
	public boolean isCompoundType() { return true; }

	@Override
	public boolean isFixedLength() { return false; }

	@Override
	public IType<?> contentsTypeIfCasting(final IExpression exp) {
		final IType<?> itemType = exp.getGamlType();
		if (itemType.isContainer() || itemType.isAgentType() || itemType.isCompoundType())
			return itemType.getContentType();
		return itemType;
	}

	@Override
	public IContainerType<?> typeIfCasting(final IExpression exp) {
		return (IContainerType<?>) super.typeIfCasting(exp);
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IContainerType<?> of(final IType<?> sub1) {
		final IType<?> kt = getKeyType();
		IType<?> ct = sub1;
		if (ct == Types.NO_TYPE) {
			if (kt == Types.NO_TYPE) return this;
			ct = getContentType();
		}
		return ParametricType.createParametricType((IContainerType<IContainer<?, ?>>) this, kt, ct);

	}

	@SuppressWarnings ("unchecked")
	@Override
	public IContainerType<?> of(final IType<?> sub1, final IType<?> sub2) {
		IType<?> kt = sub1;
		IType<?> ct = sub2;
		if (ct == Types.NO_TYPE) {
			if (kt == Types.NO_TYPE) return this;
			ct = getContentType();
		}
		if (kt == Types.NO_TYPE) { kt = getKeyType(); }
		return ParametricType.createParametricType((IContainerType<IContainer<?, ?>>) this, kt, ct);

	}

}