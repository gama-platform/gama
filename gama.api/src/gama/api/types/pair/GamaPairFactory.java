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
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.ILink;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.map.IMap;

/**
 * The Class GamaPairFactory.
 *
 * A static factory for creating and managing {@link IPair} instances. This class serves as a frontend for pair
 * creation, delegating to an {@link IPairFactory} implementation. It provides a unified API for creating pairs with
 * specific types or inferred types, and converting objects to pairs.
 *
 * <p>
 * This factory supports multiple creation patterns:
 * <ul>
 * <li>Direct creation from two values: {@link #createWith(Object, Object)}</li>
 * <li>Type-safe creation: {@link #createWith(Object, Object, IType, IType)}</li>
 * <li>Conversion from objects: {@link #castToPair(IScope, Object)}</li>
 * <li>Default/null pairs: {@link #createDefault()}, {@link #createNull()}</li>
 * </ul>
 * </p>
 *
 * <p>
 * The actual implementation is delegated to an {@link IPairFactory} that must be set using
 * {@link #setBuilder(IPairFactory)} before using the factory methods.
 * </p>
 *
 * @author drogoul
 * @since GAMA 1.0
 *
 * @see IPair
 * @see IPairFactory
 */
public class GamaPairFactory {

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
		Object key, value;
		switch (obj) {
			case IPair p -> {
				key = p.key();
				value = p.value();
			}
			case IShape s when s.getInnerGeometry() instanceof ILink isl -> {
				key = isl.getSource();
				value = isl.getTarget();
			}
			case IMap<?, ?> m -> {
				if (m.containsKey("key") && m.containsKey("value")) {
					key = m.get("key");
					value = m.get("value");
				} else {
					key = GamaListFactory.<Object> create(scope, m.getGamlType().getKeyType(), m.keySet());
					value = GamaListFactory.<Object> create(scope, m.getGamlType().getContentType(), m.values());
				}
			}
			case null, default -> {
				// 8/01/14 : Change of behavior: now returns a pair object::object
				key = obj;
				value = obj;
			}
		}
		final IType kt = keyType == null || keyType == Types.NO_TYPE ? GamaType.of(key) : keyType;
		final IType ct = contentsType == null || contentsType == Types.NO_TYPE ? GamaType.of(value) : contentsType;
		return new GamaPair<>(GamaType.toType(scope, key, kt, copy), GamaType.toType(scope, value, ct, copy), kt, ct);

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
		return castToPair(scope, val, Types.NO_TYPE, Types.NO_TYPE, true);
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
		return castToPair(scope, val, Types.NO_TYPE, Types.NO_TYPE, copy);
	}

	/**
	 * Creates a default pair (typically null::null, unless specified otherwise).
	 *
	 * @return the default {@link IPair}.
	 */
	public static IPair createDefault() {
		return new GamaPair<>(null, null, Types.NO_TYPE, Types.NO_TYPE);
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
		return new GamaPair<>(key, value, GamaType.of(key), GamaType.of(value));
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
	public static IPair createWith(final Object v1, final Object v2, final IType keyType, final IType valueType) {
		return new GamaPair<>(v1, v2, keyType, valueType);
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
