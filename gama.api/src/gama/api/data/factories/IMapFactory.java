/*******************************************************************************************************
 *
 * IMapFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.util.Map;

import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IMapFactory extends IFactory<IMap> {

	/**
	 * Creates a forwarding map that offers an IMap interface to a regular Java Map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param key
	 *            the GAMA type of the keys
	 * @param contents
	 *            the GAMA type of the values
	 * @param wrapped
	 *            the underlying map
	 * @return the IMap wrapper
	 */
	<K, V> IMap<K, V> wrap(IType key, IType contents, Map<K, V> wrapped);

	/**
	 * Wraps a Java Map into an IMap.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param wrapped
	 *            the wrapped map
	 * @return the IMap wrapper
	 * @date 29 oct. 2023
	 */
	<K, V> IMap<K, V> wrap(Map<K, V> wrapped);

	/**
	 * Wraps a Java Map into an IMap, specifying types and ordering.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param key
	 *            the GAMA type of the keys
	 * @param contents
	 *            the GAMA type of the values
	 * @param isOrdered
	 *            whether the map preserves insertion order
	 * @param wrapped
	 *            the underlying map
	 * @return the IMap wrapper
	 */
	<K, V> IMap<K, V> wrap(IType key, IType contents, boolean isOrdered, Map<K, V> wrapped);

	/**
	 * Returns a synchronized (thread-safe) view of the specified map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param target
	 *            the source map
	 * @return a synchronized IMap
	 */
	<K, V> IMap<K, V> synchronizedMap(IMap<K, V> target);

	/**
	 * Returns a new synchronized, ordered Java Map (not necessarily IMap wrapper initially).
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return a synchronized ordered map
	 */
	<K, V> Map<K, V> synchronizedOrderedMap();

	/**
	 * Creates a new concurrent map.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the concurrent IMap
	 */
	<K, V> IMap<K, V> concurrentMap();

	/**
	 * Creates a new empty, unordered IMap.
	 *
	 * @return the new IMap
	 */
	IMap create();

	/**
	 * Creates a new empty, ordered IMap.
	 *
	 * @return the new ordered IMap
	 */
	IMap createOrdered();

	/**
	 * Creates a new empty, unordered IMap (explicitly).
	 *
	 * @return the new unordered IMap
	 */
	IMap createUnordered();

	/**
	 * Creates a new empty, synchronized, unordered IMap.
	 *
	 * @return the new synchronized unordered IMap
	 */
	IMap createSynchronizedUnordered();

	/**
	 * Creates an empty IMap with specific key and content types.
	 *
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @return the created IMap
	 */
	IMap create(IType key, IType contents);

	/**
	 * Creates an empty IMap with specific types and ordering.
	 *
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param ordered
	 *            whether to preserve insertion order
	 * @return the created IMap
	 */
	IMap create(IType key, IType contents, boolean ordered);

	/**
	 * Creates an empty IMap with specific types and initial capacity.
	 *
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param size
	 *            the initial capacity
	 * @return the created IMap
	 */
	IMap create(IType key, IType contents, int size);

	/**
	 * Creates an empty IMap with specific types, capacity and ordering.
	 *
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param size
	 *            the initial capacity
	 * @param ordered
	 *            whether to preserve insertion order
	 * @return the created IMap
	 */
	IMap create(IType key, IType contents, int size, boolean ordered);

	/**
	 * Creates an IMap from an existing Java Map without checking/casting content types.
	 *
	 * @warning This operation can end up putting values of the wrong type into the map if not careful.
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param map
	 *            the source map
	 * @return the created IMap
	 */
	<K, V> IMap<K, V> createWithoutCasting(IType<K> key, IType<V> contents, Map<K, V> map);

	/**
	 * Creates an IMap from an existing Java Map without casting, specifying ordering.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param map
	 *            the source map
	 * @param ordered
	 *            whether the result should be ordered
	 * @return the created IMap
	 */
	<K, V> IMap<K, V> createWithoutCasting(IType<K> key, IType<V> contents, Map<K, V> map, boolean ordered);

	/**
	 * Creates an IMap from a Java Map, performing checks/casts within a scope.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param map
	 *            the source map
	 * @return the created IMap
	 */
	<K, V> IMap<K, V> create(IScope scope, IType<K> key, IType<V> contents, Map<K, V> map);

	/**
	 * Creates an IMap from a Java Map, verifying types and specifying ordering.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param map
	 *            the source map
	 * @param ordered
	 *            whether the map is ordered
	 * @return the created IMap
	 */
	<K, V> IMap<K, V> create(IScope scope, IType<K> key, IType<V> contents, Map<K, V> map, boolean ordered);

	/**
	 * Creates an IMap from lists of keys and values.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param scope
	 *            the scope
	 * @param key
	 *            the key type
	 * @param contents
	 *            the value type
	 * @param keys
	 *            the list of keys
	 * @param values
	 *            the list of values
	 * @return the created IMap
	 */
	<K, V> IMap<K, V> create(IScope scope, IType<K> key, IType<V> contents, IList<K> keys, IList<V> values);

	/**
	 * Checks equality between two IMap objects.
	 *
	 * @param one
	 *            the first map
	 * @param two
	 *            the second map
	 * @return true if equal
	 */
	boolean equals(IMap one, IMap two);

	/**
	 * @param scope
	 * @param s
	 * @param keyType
	 * @param contentsType
	 * @return
	 */
	<V> IMap<String, V> createFromString(IScope scope, String s, IType<String> keyType, IType<V> contentsType);

}