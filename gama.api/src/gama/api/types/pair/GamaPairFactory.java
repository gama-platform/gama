/*******************************************************************************************************
 *
 * GamaPairFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.pair;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

/**
 * A static factory for creating and managing {@link IPair} instances. This class serves as a frontend for pair
 * creation, delegating to an {@link IPairFactory}. It allows creating pairs with specific types or inferred types, and
 * converting objects to pairs.
 */
public class GamaPairFactory {

	/**
	 * The internal factory used for creating pair instances.
	 */
	private static IPairFactory InternalFactory;

	/**
	 * Configures the internal factory implementation.
	 *
	 * @param internalGamaPairFactory
	 *            the {@link IPairFactory} to be used as the internal builder.
	 */
	public static void setBuilder(final IPairFactory internalGamaPairFactory) {
		InternalFactory = internalGamaPairFactory;
	}

	/**
	 * Converts an arbitrary object into a pair, with specific key and value types.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert (e.g., list, map, map entry, point).
	 * @param keyType
	 *            the expected type of the key (first element).
	 * @param contentsType
	 *            the expected type of the value (second element).
	 * @param copy
	 *            whether to create a deep copy.
	 * @return the created {@link IPair}.
	 * @throws GamaRuntimeException
	 *             if conversion fails.
	 */
	public static IPair castToPair(final IScope scope, final Object obj, final IType keyType, final IType contentsType,
			final boolean copy) throws GamaRuntimeException {
		return InternalFactory.createFrom(scope, obj, keyType, contentsType, copy);
	}

	/**
	 * Converts an object to a pair, inferring types, creating a copy by default.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param val
	 *            the object to convert.
	 * @return the created {@link IPair}.
	 */
	public static IPair castToPair(final IScope scope, final Object val) {
		return InternalFactory.createFrom(scope, val, Types.NO_TYPE, Types.NO_TYPE, true);
	}

	/**
	 * Converts an object to a pair, inferring types, with control over copying.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param val
	 *            the object to convert.
	 * @param copy
	 *            whether to copy the elements.
	 * @return the created {@link IPair}.
	 */
	public static IPair castToPair(final IScope scope, final Object val, final boolean copy) {
		return InternalFactory.createFrom(scope, val, Types.NO_TYPE, Types.NO_TYPE, copy);
	}

	/**
	 * Creates a default pair (typically null::null, unless specified otherwise).
	 *
	 * @return the default {@link IPair}.
	 */
	public static IPair createDefault() {
		return InternalFactory.createDefault();
	}

	/**
	 * Creates a pair from a key and a value.
	 *
	 * @param key
	 *            the first element (key).
	 * @param value
	 *            the second element (value).
	 * @return the created {@link IPair}.
	 */
	public static IPair createWith(final Object key, final Object value) {
		return InternalFactory.create(key, value);
	}

	/**
	 * Creates a pair from two values, specifying their types.
	 *
	 * @param v1
	 *            the first element (key).
	 * @param v2
	 *            the second element (value).
	 * @param keyType
	 *            the type of the first element.
	 * @param contentsType
	 *            the type of the second element.
	 * @return the created {@link IPair}.
	 */
	public static IPair createWith(final Object v1, final Object v2, final IType keyType, final IType contentsType) {
		return InternalFactory.createFrom(v1, v2, keyType, contentsType);
	}

	/**
	 * Creates a null pair (typically representing absence of value or null::null).
	 *
	 * @return a null pair.
	 */
	public static IPair createNull() {
		return createWith(null, null, Types.NO_TYPE, Types.NO_TYPE);
	}

}
