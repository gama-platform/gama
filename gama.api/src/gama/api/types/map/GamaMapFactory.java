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
package gama.api.types.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import gama.api.GAMA;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;

/**
 * A static factory for creating and managing {@link IMap} instances in the GAMA platform.
 * 
 * <p>
 * {@code GamaMapFactory} serves as the central point for creating all {@link IMap} implementations. It delegates to
 * concrete classes ({@link GamaMap}, {@link GamaMapWrapper}, etc.) and provides various creation strategies including
 * ordered/unordered maps, thread-safe maps, and zero-copy wrapping of existing Java Maps.
 * </p>
 * 
 * <h2>Factory Method Categories</h2>
 * <ul>
 * <li><b>Core creation methods</b> - {@link #create()}, {@link #create(IType, IType)} - Create new empty maps</li>
 * <li><b>Scope-aware methods</b> - {@link #create(IScope, IType, IType, Object...)} - Cast elements according to GAML
 * contracts</li>
 * <li><b>Wrapping methods</b> - {@link #wrap(IType, IType, Map)} - Zero-copy wrappers for existing maps</li>
 * <li><b>Thread-safe methods</b> - {@link #synchronizedMap(IMap)}, {@link #concurrentMap()} - Thread-safe map
 * creation</li>
 * <li><b>Conversion methods</b> - {@link #toMap(IScope, Object)} - Convert arbitrary objects to maps</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Creation</h3>
 * 
 * <pre>
 * // Empty map with default types
 * IMap&lt;?, ?&gt; genericMap = GamaMapFactory.create();
 * 
 * // Empty map with specific types
 * IMap&lt;String, Integer&gt; scores = GamaMapFactory.create(Types.STRING, Types.INT);
 * 
 * // Map with initial capacity
 * IMap&lt;Integer, String&gt; lookup = GamaMapFactory.create(Types.INT, Types.STRING, 1000);
 * </pre>
 * 
 * <h3>Creation from Data</h3>
 * 
 * <pre>
 * // From key-value pairs (requires scope for type casting)
 * IMap&lt;String, Integer&gt; map = GamaMapFactory.create(scope, Types.STRING, Types.INT, 
 *     "alice", 95, "bob", 87, "charlie", 92);
 * 
 * // From another container
 * IList&lt;?&gt; pairs = ...; // List of pairs
 * IMap&lt;?, ?&gt; fromList = GamaMapFactory.create(scope, Types.NO_TYPE, Types.NO_TYPE, pairs);
 * </pre>
 * 
 * <h3>Wrapping Existing Maps</h3>
 * 
 * <pre>
 * // Wrap a Java map (zero-copy, bidirectional)
 * Map&lt;String, Double&gt; javaMap = new LinkedHashMap&lt;&gt;();
 * javaMap.put("pi", 3.14159);
 * IMap&lt;String, Double&gt; gamaMap = GamaMapFactory.wrap(Types.STRING, Types.FLOAT, javaMap);
 * 
 * // Changes are bidirectional
 * javaMap.put("e", 2.71828);
 * assert gamaMap.containsKey("e"); // true
 * </pre>
 * 
 * <h3>Thread-Safe Maps</h3>
 * 
 * <pre>
 * // Synchronized wrapper (for single-threaded writes, multi-threaded reads)
 * IMap&lt;String, Integer&gt; original = GamaMapFactory.create(Types.STRING, Types.INT);
 * IMap&lt;String, Integer&gt; synced = GamaMapFactory.synchronizedMap(original);
 * 
 * // Concurrent map (for multi-threaded reads and writes)
 * IMap&lt;?, ?&gt; concurrent = GamaMapFactory.concurrentMap();
 * 
 * // Synchronized standard Java map
 * Map&lt;String, Integer&gt; syncedJava = GamaMapFactory.synchronizedOrderedMap();
 * </pre>
 * 
 * <h3>Special Cases</h3>
 * 
 * <pre>
 * // Empty immutable map
 * IMap EMPTY = GamaMapFactory.EMPTY;
 * 
 * // Create without type casting (for performance)
 * IMap&lt;String, Integer&gt; fastMap = GamaMapFactory.createWithoutCasting(Types.STRING, Types.INT, existingMap);
 * </pre>
 * 
 * <h2>Type Casting Behavior</h2>
 * <p>
 * Methods accepting an {@link IScope} strictly adhere to GAML container contracts:
 * </p>
 * <ul>
 * <li><b>With scope</b>: Keys and values are cast to declared types when {@code FLAGS.CAST_CONTAINER_CONTENTS} is
 * enabled</li>
 * <li><b>Without scope</b>: No type casting - assumes data is already correctly typed</li>
 * <li><b>createWithoutCasting</b>: Explicitly skips type casting for performance</li>
 * </ul>
 * 
 * <h2>Ordering Guarantees</h2>
 * <ul>
 * <li>{@link GamaMap} - Always ordered (uses LinkedHashMap)</li>
 * <li>{@link GamaMapWrapper} - Preserves wrapped map's ordering behavior</li>
 * <li>{@link #concurrentMap()} - Not ordered (uses ConcurrentHashMap)</li>
 * </ul>
 * 
 * <h2>Equality Testing</h2>
 * <p>
 * Provides {@link #equals(IMap, IMap)} for comparing maps:
 * </p>
 * <ul>
 * <li>Compares sizes first</li>
 * <li>Then compares all key-value pairs</li>
 * <li>Ignores type information and iteration order</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 * <li><b>Wrapping vs. Copying</b>: {@code wrap()} methods are zero-copy but changes affect original; {@code create()}
 * methods always create independent copies</li>
 * <li><b>Casting Overhead</b>: Scope-aware methods have casting overhead; use {@code createWithoutCasting()} when
 * types are guaranteed</li>
 * <li><b>Capacity Hints</b>: Provide initial capacity when size is known to avoid resizing</li>
 * <li><b>Thread Safety</b>: {@code synchronizedMap()} adds synchronization overhead; {@code concurrentMap()} is more
 * efficient for high concurrency</li>
 * </ul>
 * 
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li>All creation methods are static - no factory instance needed</li>
 * <li>DEFAULT_SIZE = 10 for maps created without capacity hints</li>
 * <li>EMPTY map is immutable and shared - do not modify</li>
 * <li>Supports conversion from pairs, lists, and other containers</li>
 * </ul>
 * 
 * @see IMap
 * @see GamaMap
 * @see GamaMapWrapper
 * @see GamaMapSimpleWrapper
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMapFactory {

	/** The Constant DEFAULT_SIZE. */
	private static final int DEFAULT_SIZE = 10;

	/** The Constant EMPTY. */
	public static IMap EMPTY = wrap(Map.of());

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
		return new GamaMapWrapper(wrapped, key, contents, true);
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
		return new GamaMapWrapper(wrapped, Types.NO_TYPE, Types.NO_TYPE, true);
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
		return new GamaMapWrapper(wrapped, key, contents, isOrdered);
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
		final IType key = target.getGamlType().getKeyType();
		final IType contents = target.getGamlType().getContentType();
		final boolean isOrdered = target.isOrdered();
		return wrap(key, contents, isOrdered, Collections.synchronizedMap(target));
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
		Map<K, V> map = create();
		return Collections.synchronizedMap(map);
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
		return wrap(Types.NO_TYPE, Types.NO_TYPE, false, new ConcurrentHashMap<>());
	}

	/**
	 * Creates a default empty map.
	 *
	 * @return a new empty {@link IMap}.
	 */
	public static IMap create() {
		return createOrdered();
	}

	/**
	 * Creates an ordered map (preserves insertion order).
	 *
	 * @return a new ordered {@link IMap}.
	 */
	public static IMap createOrdered() {
		return new GamaMap(DEFAULT_SIZE, Types.NO_TYPE, Types.NO_TYPE);
	}

	/**
	 * Creates an unordered map (typically a HashMap).
	 *
	 * @return a new unordered {@link IMap}.
	 */
	public static IMap createUnordered() {
		final Map map = new HashMap();
		return new GamaMapSimpleWrapper() {

			@Override
			public boolean isOrdered() { return false; }

			@Override
			protected Map delegate() {
				return map;
			}
		};
	}

	/**
	 * Creates a synchronized unordered map.
	 *
	 * @return a new synchronized and unordered {@link IMap}.
	 */
	public static IMap createSynchronizedUnordered() {
		final Map map = Collections.synchronizedMap(new HashMap());
		return new GamaMapSimpleWrapper() {

			@Override
			public boolean isOrdered() { return false; }

			@Override
			protected Map delegate() {
				return map;
			}
		};
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
		return create(key, contents, DEFAULT_SIZE);
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
		return create(key, contents, DEFAULT_SIZE, ordered);
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
		return create(key, contents, size, true);
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
		if (ordered) return new GamaMap<>(size, key, contents);
		return new GamaMapWrapper<>(new HashMap(size), key, contents, false);
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
		return createWithoutCasting(key, contents, map, true);
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
		final IMap<K, V> result = create(key, contents, map.size(), ordered);
		result.putAll(map);
		return result;
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
		if (map == null || map.isEmpty()) return create(key, contents);
		final IMap<K, V> result = create(key, contents, map.size());
		map.forEach((k, v) -> result.setValueAtIndex(scope, k, v));
		return result;
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
		if (map == null || map.isEmpty()) return create(key, contents, ordered);
		final IMap<K, V> result = create(key, contents, map.size(), ordered);
		map.forEach((k, v) -> result.setValueAtIndex(scope, k, v));
		return result;
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
		final IMap<K, V> result = create(key, contents, keys.length(scope));
		for (int i = 0; i < Math.min(keys.length(scope), values.length(scope)); i++) {
			result.put(keys.get(i), values.get(i));
		}
		return result;
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
	public static IMap castToMap(final IScope scope, final Object obj, final IType keyType, final IType contentsType,
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
	private static <V> IMap<String, V> createFromString(final IScope scope, final String s, final IType keyType,
			final IType contentsType) {
		final IMap<String, V> map;
		try {
			Object o = GAMA.getJsonEncoder().parse(s);
			if (o instanceof IMap m) return m;
			map = create();
			map.put(IKeyword.CONTENTS, (V) o);
			return map;
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, scope);
		}

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
	public static IMap castToMap(final IScope scope, final Object obj) {
		return castToMap(scope, obj, Types.NO_TYPE, Types.NO_TYPE, false);
	}
}