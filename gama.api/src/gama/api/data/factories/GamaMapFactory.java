/*******************************************************************************************************
 *
 * GamaMapFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.util.Map;
import java.util.Objects;

import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * A static factory for creating and managing {@link IMap} instances. This class serves as a frontend for map creation,
 * delegating to an {@link IMapFactory}. It supports creation of ordered/unordered maps, thread-safe maps, and wrapping
 * of existing Java Maps.
 */

@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMapFactory implements IFactory<IMap> {

	/** The Constant EMPTY. */
	public static IMap EMPTY;
	/**
	 * The internal factory used for creating map instances.
	 */
	private static IMapFactory InternalFactory;

	/**
	 * Configures the internal factory implementation.
	 *
	 * @param builder
	 *            the {@link IMapFactory} to be used as the internal builder.
	 */
	public static void setBuilder(final IMapFactory builder) {
		InternalFactory = builder;
		EMPTY = wrap(Map.of());
	}

	/**
	 * Wraps a standard Java {@link Map} into a GAMA {@link IMap}, specifying key and content types.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param key
	 *            the GAMA type of the keys.
	 * @param contents
	 *            the GAMA type of the values.
	 * @param wrapped
	 *            the map to wrap.
	 * @return the {@link IMap} wrapper.
	 */
	public static <K, V> IMap<K, V> wrap(final IType key, final IType contents, final Map<K, V> wrapped) {
		return InternalFactory.wrap(key, contents, wrapped);
	}

	/**
	 * Wraps a standard Java {@link Map} into a GAMA {@link IMap}, inferring types if possible or using defaults.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param wrapped
	 *            the map to wrap.
	 * @return the {@link IMap} wrapper.
	 */
	public static <K, V> IMap<K, V> wrap(final Map<K, V> wrapped) {
		return InternalFactory.wrap(wrapped);
	}

	/**
	 * Wraps a Java {@link Map} into a GAMA {@link IMap} with full configuration.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param key
	 *            the GAMA type of the keys.
	 * @param contents
	 *            the GAMA type of the values.
	 * @param isOrdered
	 *            whether the map preserves insertion order.
	 * @param wrapped
	 *            the map to wrap.
	 * @return the {@link IMap} wrapper.
	 */
	public static <K, V> IMap<K, V> wrap(final IType key, final IType contents, final boolean isOrdered,
			final Map<K, V> wrapped) {
		return InternalFactory.wrap(key, contents, isOrdered, wrapped);
	}

	/**
	 * Returns a synchronized (thread-safe) view of the specified map.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param target
	 *            the map to synchronize.
	 * @return a synchronized {@link IMap}.
	 */
	public static <K, V> IMap<K, V> synchronizedMap(final IMap<K, V> target) {
		return InternalFactory.synchronizedMap(target);
	}

	/**
	 * Creates a new synchronized and ordered standard Java map (not an IMap).
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @return a new synchronized, ordered {@link Map}.
	 */
	public static <K, V> Map<K, V> synchronizedOrderedMap() {
		return InternalFactory.synchronizedOrderedMap();
	}

	/**
	 * Creates a concurrent map suitable for multi-threaded access.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @return a new concurrent {@link IMap}.
	 */
	public static <K, V> IMap<K, V> concurrentMap() {
		return InternalFactory.concurrentMap();
	}

	/**
	 * Creates a default empty map.
	 *
	 * @return a new empty {@link IMap}.
	 */
	public static IMap create() {
		return InternalFactory.create();
	}

	/**
	 * Creates an ordered map (preserves insertion order).
	 *
	 * @return a new ordered {@link IMap}.
	 */
	public static IMap createOrdered() {
		return InternalFactory.createOrdered();
	}

	/**
	 * Creates an unordered map (typically a HashMap).
	 *
	 * @return a new unordered {@link IMap}.
	 */
	public static IMap createUnordered() {
		return InternalFactory.createUnordered();
	}

	/**
	 * Creates a synchronized unordered map.
	 *
	 * @return a new synchronized and unordered {@link IMap}.
	 */
	public static IMap createSynchronizedUnordered() {
		return InternalFactory.createSynchronizedUnordered();
	}

	/**
	 * Creates a map with specific key and content types.
	 *
	 * @param key
	 *            the type of keys.
	 * @param contents
	 *            the type of values.
	 * @return the created {@link IMap}.
	 */
	public static IMap create(final IType key, final IType contents) {
		return InternalFactory.create(key, contents);
	}

	/**
	 * Creates a map with specific types and ordering preference.
	 *
	 * @param key
	 *            the type of keys.
	 * @param contents
	 *            the type of values.
	 * @param ordered
	 *            true for ordered map, false for unordered.
	 * @return the created {@link IMap}.
	 */
	public static IMap create(final IType key, final IType contents, final boolean ordered) {
		return InternalFactory.create(key, contents, ordered);
	}

	/**
	 * Creates a map with specific types and initial capacity.
	 *
	 * @param key
	 *            the type of keys.
	 * @param contents
	 *            the type of values.
	 * @param size
	 *            the initial capacity.
	 * @return the created {@link IMap}.
	 */
	public static IMap create(final IType key, final IType contents, final int size) {
		return InternalFactory.create(key, contents, size);
	}

	/**
	 * Creates a map with specific types, capacity, and ordering.
	 *
	 * @param key
	 *            the type of keys.
	 * @param contents
	 *            the type of values.
	 * @param size
	 *            the initial capacity.
	 * @param ordered
	 *            true for ordered map, false for unordered.
	 * @return the created {@link IMap}.
	 */
	public static IMap create(final IType key, final IType contents, final int size, final boolean ordered) {
		return InternalFactory.create(key, contents, size, ordered);
	}

	/**
	 * Creates a map from an existing Java map without type checking or casting. Warning: This may result in type safety
	 * issues if the map content doesn't match the types.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param key
	 *            the declared key type.
	 * @param contents
	 *            the declared value type.
	 * @param map
	 *            the source map.
	 * @return the created {@link IMap}.
	 */
	public static <K, V> IMap<K, V> createWithoutCasting(final IType<K> key, final IType<V> contents,
			final Map<K, V> map) {
		return InternalFactory.createWithoutCasting(key, contents, map);
	}

	/**
	 * Creates a map from an existing Java map without checking, specifying ordering.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param key
	 *            the declared key type.
	 * @param contents
	 *            the declared value type.
	 * @param map
	 *            the source map.
	 * @param ordered
	 *            whether to respect source order (if applicable) or enforce new one.
	 * @return the created {@link IMap}.
	 */
	public static <K, V> IMap<K, V> createWithoutCasting(final IType<K> key, final IType<V> contents,
			final Map<K, V> map, final boolean ordered) {
		return InternalFactory.createWithoutCasting(key, contents, map, ordered);
	}

	/**
	 * Creates a map by copying and casting elements from another map.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param scope
	 *            the execution scope for casting.
	 * @param key
	 *            the target key type.
	 * @param contents
	 *            the target value type.
	 * @param map
	 *            the source map.
	 * @return the created {@link IMap}.
	 */
	public static <K, V> IMap<K, V> create(final IScope scope, final IType<K> key, final IType<V> contents,
			final Map<K, V> map) {
		return InternalFactory.create(scope, key, contents, map);
	}

	/**
	 * Creates a map by copying and casting elements, specifying ordering.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param scope
	 *            the execution scope.
	 * @param key
	 *            the target key type.
	 * @param contents
	 *            the target value type.
	 * @param map
	 *            the source map.
	 * @param ordered
	 *            whether the resulting map should be ordered.
	 * @return the created {@link IMap}.
	 */
	public static <K, V> IMap<K, V> create(final IScope scope, final IType<K> key, final IType<V> contents,
			final Map<K, V> map, final boolean ordered) {
		return InternalFactory.create(scope, key, contents, map, ordered);
	}

	/**
	 * Creates a map from parallel lists of keys and values.
	 *
	 * @param <K>
	 *            the type of keys.
	 * @param <V>
	 *            the type of values.
	 * @param scope
	 *            the execution scope.
	 * @param key
	 *            the target key type.
	 * @param contents
	 *            the target value type.
	 * @param keys
	 *            the list of keys.
	 * @param values
	 *            the list of values.
	 * @return the created {@link IMap}.
	 */
	public static <K, V> IMap<K, V> create(final IScope scope, final IType<K> key, final IType<V> contents,
			final IList<K> keys, final IList<V> values) {
		return InternalFactory.create(scope, key, contents, keys, values);
	}

	/**
	 * Checks equality between two maps (same size and same content pairs).
	 *
	 * @param one
	 *            the first map.
	 * @param two
	 *            the second map.
	 * @return true if maps are equal, false otherwise.
	 */
	public static boolean equals(final IMap one, final IMap two) {
		if (one.size() != two.size()) return false;
		return one.forEachPair((k1, v1) -> {
			if (!Objects.equals(v1, two.get(k1))) return false;
			return true;
		});
	}

	/**
	 * Converts an arbitrary object into a map.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert (Agent, Container, JSON String, etc.).
	 * @param keyType
	 *            the desired key type.
	 * @param contentsType
	 *            the desired value type.
	 * @param copy
	 *            whether to copy if the object is already a map.
	 * @return the resulting {@link IMap}.
	 */
	public static IMap createFrom(final IScope scope, final Object obj, final IType keyType, final IType contentsType,
			final boolean copy) {
		// TODO Should be removed to privilegiate from_json()
		switch (obj) {
			case IAgent ia -> {
				return ia.getOrCreateAttributes();
			}
			case IContainer ic -> {
				return ic.mapValue(scope, keyType, contentsType, copy);
			}
			case String s -> {
				return createFromString(scope, s, keyType, contentsType);
			}
			case null, default -> {
			}
		}
		final IMap result = create(keyType, contentsType);
		if (obj != null) { result.setValueAtIndex(scope, obj, obj); }
		return result;
	}

	/**
	 * @param scope
	 * @param s
	 * @param keyType
	 * @param contentsType
	 * @return
	 */
	private static IMap createFromString(final IScope scope, final String s, final IType keyType,
			final IType contentsType) {
		return InternalFactory.createFromString(scope, s, keyType, contentsType);
	}

	/**
	 * Convenience method to convert an object to a map using default types.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the object to convert.
	 * @return the resulting {@link IMap}.
	 */
	public static IMap createFrom(final IScope scope, final Object obj) {
		return createFrom(scope, obj, Types.NO_TYPE, Types.NO_TYPE, false);
	}
}