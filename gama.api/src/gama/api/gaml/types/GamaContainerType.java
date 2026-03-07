/*******************************************************************************************************
 *
 * GamaContainerType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;

/**
 * Represents the generic GAML container type.
 * <p>
 * This is the super-type of all container types in GAML (list, graph, matrix, map, etc.). It provides a generic
 * framework for types that hold collections of values with keys/indices. Container types are compound types with
 * variable length and support parameterization by key type and content type.
 * </p>
 * <p>
 * When casting to a container type, if the object is already a container it is returned as-is, otherwise it is cast to
 * a list.
 * </p>
 *
 * @param <T>
 *            the specific container class this type represents
 *
 * @author drogoul
 * @since GAMA 1.0
 * @see IContainer
 * @see IContainerType
 */
@type (
		name = IKeyword.CONTAINER,
		id = IType.CONTAINER,
		wraps = { IContainer.class },
		kind = ISymbolKind.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER },
		doc = @doc ("Generic super-type of all the container types (list, graph, matrix, etc.)"))
public class GamaContainerType<T extends IContainer<?, ?>> extends GamaType<T> implements IContainerType<T> {

	/**
	 * Constructs a new GamaContainerType.
	 *
	 * @param typesManager
	 *            the types manager for type resolution
	 */
	public GamaContainerType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@doc ("Allows to cast the argument to a container. If the argument is already a container, returns it, otherwise cast it to a list")
	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return cast(scope, obj, param, getKeyType(), getContentType(), copy);
		// return (T) (obj instanceof IContainer ? (IContainer) obj :
		// Types.get(LIST).cast(scope, obj, null,
		// Types.NO_TYPE, Types.NO_TYPE));
	}

	/**
	 * Returns the number of type parameters for this container type.
	 * <p>
	 * Container types have one type parameter (the content type).
	 * </p>
	 *
	 * @return 1, indicating one type parameter
	 */
	@Override
	public int getNumberOfParameters() { return 1; }

	/**
	 * Casts an object to this container type with specific key and content types.
	 * <p>
	 * By default, if the object is already a container it is returned as-is, otherwise it is cast to a list.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to cast
	 * @param param
	 *            optional casting parameter
	 * @param keyType
	 *            the key type for the container
	 * @param contentType
	 *            the content type for the container
	 * @param copy
	 *            whether to copy the result
	 * @return the casted container
	 * @throws GamaRuntimeException
	 *             if casting fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) throws GamaRuntimeException {
		// by default
		return (T) (obj instanceof IContainer ? (IContainer<?, ?>) obj
				: (IList<?>) Types.get(LIST).cast(scope, obj, null, copy));
	}

	/**
	 * Returns the default value for this type.
	 *
	 * @return null, as containers have no default value
	 */
	@Override
	public T getDefault() { return null; }

	/**
	 * Gets the GAML type representation of this type.
	 *
	 * @return this container type
	 */
	@Override
	public IContainerType<T> getGamlType() { return this; }

	/**
	 * Indicates whether this is a container type.
	 *
	 * @return true, as this is a container type
	 */
	@Override
	public boolean isContainer() { return true; }

	/**
	 * Indicates whether this is a compound type.
	 *
	 * @return true, as containers have accessible elements
	 */
	@Override
	public boolean isCompoundType() { return true; }

	/**
	 * Indicates whether this type has a fixed length.
	 *
	 * @return false, as container sizes can vary
	 */
	@Override
	public boolean isFixedLength() { return false; }

	/**
	 * Determines the content type if casting an expression to this type.
	 * <p>
	 * If the expression is a container, agent, or compound type, returns its content type. Otherwise, returns the
	 * expression's type itself.
	 * </p>
	 *
	 * @param exp
	 *            the expression to analyze
	 * @return the expected content type after casting
	 */
	@Override
	public IType<?> contentsTypeIfCasting(final IExpression exp) {
		final IType<?> itemType = exp.getGamlType();
		if (itemType.isContainer() || itemType.isAgentType() || itemType.isCompoundType())
			return itemType.getContentType();
		return itemType;
	}

	/**
	 * Determines the container type if casting an expression to this type.
	 *
	 * @param exp
	 *            the expression to analyze
	 * @return the container type after casting
	 */
	@Override
	public IContainerType<?> typeIfCasting(final IExpression exp) {
		return (IContainerType<?>) super.typeIfCasting(exp);
	}

	/**
	 * Indicates whether container values can be cast to constants.
	 *
	 * @return false, as containers generally cannot be compile-time constants
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Creates a parameterized version of this container type with a specified content type.
	 * <p>
	 * The key type is preserved from this type. If the content type is NO_TYPE and the key type is also NO_TYPE,
	 * returns this type unchanged.
	 * </p>
	 *
	 * @param sub1
	 *            the content type parameter
	 * @return a new parameterized container type
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IContainerType<?> of(final IType<?> sub1) {
		final IType<?> kt = getKeyType();
		IType<?> ct = sub1;
		if (ct == Types.NO_TYPE) {
			if (kt == Types.NO_TYPE) return this;
			ct = getContentType();
		}
		return new ParametricType(typesManager, (IContainerType<IContainer<?, ?>>) this, kt, ct);

	}

	/**
	 * Creates a parameterized version of this container type with specified key and content types.
	 * <p>
	 * This method allows full parameterization of both the key and content types. Missing type parameters (NO_TYPE) are
	 * filled in from this type's existing key/content types.
	 * </p>
	 *
	 * @param sub1
	 *            the key type parameter
	 * @param sub2
	 *            the content type parameter
	 * @return a new parameterized container type
	 */
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
		return new ParametricType(typesManager, (IContainerType<IContainer<?, ?>>) this, kt, ct);

	}

}