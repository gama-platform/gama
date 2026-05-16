/*******************************************************************************************************
 *
 * GamaMap.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.map;

import static gama.api.types.map.GamaMapFactory.createWithoutCasting;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.GamaPairList;
import gama.api.types.list.IList;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IRuntimeContainer;
import gama.api.types.pair.GamaPairFactory;
import gama.api.types.pair.IPair;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.ConsumerWithPruning;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.json.IJsonValue;
import gama.dev.FLAGS;

/**
 * The primary concrete implementation of {@link IMap} for the GAMA platform.
 *
 * <p>
 * {@code GamaMap} extends {@link LinkedHashMap} to provide a type-safe, ordered, GAML-integrated map implementation. It
 * tracks both key and value types through an {@link IContainerType} and ensures proper type handling for all
 * operations.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>LinkedHashMap-based</b>: Preserves insertion order for keys, values, and pairs</li>
 * <li><b>Dual Type Tracking</b>: Maintains both key type and value type via {@link IContainerType}</li>
 * <li><b>Ordered Iteration</b>: {@link #isOrdered()} returns {@code true} - consistent iteration order</li>
 * <li><b>GAML Integration</b>: Full support for GAML operators and pseudo-variables</li>
 * <li><b>Custom Equality</b>: Uses {@link GamaMapFactory#equals} for GAMA-aware equality checks</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * <b>Do not instantiate directly</b>. Use {@link GamaMapFactory} instead:
 * </p>
 *
 * <pre>
 * // Create an empty map
 * IMap&lt;String, Integer&gt; scores = GamaMapFactory.create(Types.STRING, Types.INT);
 *
 * // Create from key-value pairs
 * IMap&lt;String, Double&gt; prices =
 * 		GamaMapFactory.create(scope, Types.STRING, Types.FLOAT, "apple", 1.5, "banana", 0.8, "orange", 2.0);
 *
 * // Create with initial capacity
 * IMap&lt;Integer, String&gt; lookup = GamaMapFactory.create(Types.INT, Types.STRING, 1000);
 * </pre>
 *
 * <h2>Ordered Behavior</h2>
 * <p>
 * Inherits insertion-order preservation from {@link LinkedHashMap}:
 * </p>
 *
 * <pre>
 * IMap&lt;String, Integer&gt; map = GamaMapFactory.create(Types.STRING, Types.INT);
 * map.put("first", 1);
 * map.put("second", 2);
 * map.put("third", 3);
 *
 * // Iteration order matches insertion order
 * IList&lt;String&gt; keys = map.getKeys();
 * // keys = ["first", "second", "third"]
 *
 * IList&lt;Integer&gt; values = map.getValues();
 * // values = [1, 2, 3]
 * </pre>
 *
 * <h2>Type Handling</h2>
 * <p>
 * The map maintains an {@link IContainerType} with both key and value types. Type conversions use
 * {@link #buildIndex(IScope, Object)} for keys and {@link #buildValue(IScope, Object)} for values:
 * </p>
 *
 * <pre>
 * IMap&lt;Integer, String&gt; map = GamaMapFactory.create(Types.INT, Types.STRING);
 *
 * // Automatic type casting (when FLAGS.CAST_CONTAINER_CONTENTS is enabled)
 * map.setValueAtIndex(scope, "42", 100); // "42" casted to Integer, 100 to String
 * // Result: map contains {42 -&gt; "100"}
 * </pre>
 *
 * <h2>GAML Pseudo-Variables</h2>
 * <p>
 * Supports the standard map pseudo-variables:
 * </p>
 * <ul>
 * <li><b>keys</b>: {@link #getKeys()} - Returns {@link IList} of keys in insertion order</li>
 * <li><b>values</b>: {@link #getValues()} - Returns {@link IList} of values in insertion order</li>
 * <li><b>pairs</b>: {@link #getPairs()} - Returns {@link IPairList} of Map.Entry objects</li>
 * </ul>
 *
 * <h2>Performance Characteristics</h2>
 * <p>
 * Inherits performance from {@link LinkedHashMap}:
 * </p>
 * <ul>
 * <li><b>get/put/remove</b>: O(1) average case</li>
 * <li><b>Iteration</b>: O(n) - faster than HashMap due to maintained order</li>
 * <li><b>containsKey/containsValue</b>: O(1) for keys, O(n) for values</li>
 * <li><b>Memory overhead</b>: Slightly higher than HashMap (maintains doubly-linked list)</li>
 * </ul>
 *
 * <h2>Specialized Iteration</h2>
 * <p>
 * Provides pruning-capable iteration methods for efficiency:
 * </p>
 *
 * <pre>
 * // Iterate over pairs with early termination
 * map.forEachPair((key, value) -&gt; {
 * 	if (someCondition(key, value)) return false; // Stop iteration
 * 	process(key, value);
 * 	return true; // Continue iteration
 * });
 *
 * // Iterate over values only
 * map.forEachValue(value -&gt; {
 * 	processValue(value);
 * 	return true; // Continue
 * });
 * </pre>
 *
 * <h2>Equality and Hashing</h2>
 * <p>
 * Overrides {@link #equals(Object)} to use {@link GamaMapFactory#equals}, which compares:
 * </p>
 * <ul>
 * <li>Map sizes</li>
 * <li>All key-value pairs (must match exactly)</li>
 * <li>Does NOT compare key/value types</li>
 * <li>Does NOT require same iteration order (just same mappings)</li>
 * </ul>
 *
 * <h2>Null Handling</h2>
 * <p>
 * Following {@link LinkedHashMap} behavior:
 * </p>
 * <ul>
 * <li><b>Null keys</b>: Not supported (will throw NullPointerException)</li>
 * <li><b>Null values</b>: Supported</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Like LinkedHashMap, {@code GamaMap} is <b>not thread-safe</b>. For concurrent access:
 * </p>
 * <ul>
 * <li>Use {@link GamaMapFactory#synchronizedMap(IMap)} for synchronized wrapper</li>
 * <li>Use {@link GamaMapFactory#concurrentMap()} for concurrent map</li>
 * <li>Provide external synchronization</li>
 * </ul>
 *
 * <h2>JSON Serialization</h2>
 * <p>
 * Implements {@link #serializeToJson(IJson)} for JSON export:
 * </p>
 *
 * <pre>
 * IMap&lt;String, Integer&gt; map = ...;
 * IJsonValue json = map.serializeToJson(jsonContext);
 * // Results in: {"key1": value1, "key2": value2, ...}
 * </pre>
 *
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li>Protected constructor ensures creation only through {@link GamaMapFactory}</li>
 * <li>Mutable type field allows efficient type changes in some scenarios</li>
 * <li>Constants KEYS, VALUES, PAIRS match pseudo-variable names</li>
 * <li>Implements {@link #buildIndexes} for bulk key conversion</li>
 * </ul>
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 *
 * @see GamaMapFactory for creation methods
 * @see IMap for the interface contract
 * @see LinkedHashMap for underlying implementation details
 *
 * @author drogoul
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMap<K, V> extends LinkedHashMap<K, V> implements IMap<K, V> {

	/** The Constant KEYS. */
	public static final String KEYS = "keys";

	/** The Constant VALUES. */
	public static final String VALUES = "values";

	/** The Constant PAIRS. */
	public static final String PAIRS = "pairs";

	/** The type. */
	IContainerType type;

	/**
	 * Instantiates a new gama map.
	 *
	 * @param capacity
	 *            the capacity
	 * @param key
	 *            the key
	 * @param content
	 *            the content
	 */
	protected GamaMap(final int capacity, final IType key, final IType content) {
		super(capacity);
		type = Types.MAP.of(key, content);
	}

	@Override
	public IContainerType getGamlType() { return type; }

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof IMap)) return false;
		return GamaMapFactory.equals(this, (IMap) o);
	}

	/**
	 * Builds the indexes.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @return the i container
	 */
	protected IContainer<?, K> buildIndexes(final IScope scope, final IContainer value) {
		final IList<K> result = GamaListFactory.create(getGamlType().getContentType());
		for (final Object o : value.iterable(scope)) { result.add(buildIndex(scope, o)); }
		return result;
	}

	@Override
	public boolean isOrdered() { return true; }

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@Override
	public String stringValue(final IScope scope) {
		return serializeToGaml(false);
	}

	/**
	 * Method add()
	 *
	 * @see gama.api.types.misc.IContainer#add(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void addValue(final IScope scope, final V v) {
		if (v instanceof IPair) {
			setValueAtIndex(scope, (K) ((IPair) v).key(), (V) ((IPair) v).value());
		} else {
			setValueAtIndex(scope, v, v);
		}
	}

	/**
	 * Method add()
	 *
	 * @see gama.api.types.misc.IContainer#add(gama.api.runtime.scope.IScope, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final V value) {
		// Cf. discussion on mailing-list about making "add" a synonym of "put"
		// for maps
		// if ( !containsKey(index) ) {
		setValueAtIndex(scope, index, value);
		// }
	}

	/**
	 * Method addAll()
	 *
	 * @see gama.api.types.misc.IContainer#addAll(gama.api.runtime.scope.IScope, gama.api.types.misc.IContainer)
	 */
	// AD July 2020: Addition of the index (see #2985)
	@Override
	public void addValues(final IScope scope, final Object index, final IRuntimeContainer/* <?, IPair<K, V>> */ values) {
		// If an index is specified, we add only the last object
		if (index != null) {
			final Iterable list = values.iterable(scope);
			setValueAtIndex(scope, index, (V) Iterables.getLast(list));
		} else {
			for (final Object o : values.iterable(scope)) { addValue(scope, (V) o); }
		}
	}

	/**
	 * Method removeAt()
	 *
	 * @see gama.api.types.misc.IContainer#removeAt(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void removeIndex(final IScope scope, final Object index) {
		remove(index);
	}

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public boolean containsKey(final IScope scope, final Object o) throws GamaRuntimeException {
		return containsKey(o);
	}

	/**
	 * Length.
	 *
	 * @param scope
	 *            the scope
	 * @return the int
	 */
	@Override
	public int length(final IScope scope) {
		return size();
	}

	/**
	 * Checks if is empty.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is empty
	 */
	@Override
	public boolean isEmpty(final IScope scope) {
		return length(scope) == 0;
	}

	/**
	 * Any value.
	 *
	 * @param scope
	 *            the scope
	 * @return the v
	 */
	@Override
	public V anyValue(final IScope scope) {
		final int size = length(scope);
		if (size == 0) return null;
		final K key = scope.getRandom().oneOf(keySet());
		return get(key);
	}

	/**
	 * First value.
	 *
	 * @param scope
	 *            the scope
	 * @return the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public V firstValue(final IScope scope) throws GamaRuntimeException {
		if (length(scope) == 0) return null;
		return Iterators.get(values().iterator(), 0);
	}

	/**
	 * Last value.
	 *
	 * @param scope
	 *            the scope
	 * @return the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public V lastValue(final IScope scope) throws GamaRuntimeException {
		if (length(scope) == 0) return null;
		return Iterators.getLast(values().iterator());
	}

	/**
	 * Value at.
	 *
	 * @param index
	 *            the index
	 * @return the v
	 */
	@Override
	public V valueAt(final int index) {
		if (size() == 0) return null;
		return Iterators.get(values().iterator(), index);
	}

	/**
	 * Method removeAll()
	 *
	 * @see gama.api.types.misc.IContainer#removeAll(gama.api.runtime.scope.IScope, gama.api.types.misc.IContainer)
	 */
	@Override
	public void removeValues(final IScope scope, final IRuntimeContainer<?, ?> values) {
		// we suppose we have pairs
		for (final Object o : values.iterable(scope)) { removeValue(scope, o); }
	}

	/**
	 * Method checkBounds()
	 *
	 * @see gama.core.util.IContainer#checkBounds(java.lang.Object, boolean)
	 */
	// @Override
	// default boolean checkBounds(final IScope scope, final Object index, final boolean forAdding) {
	// return true;
	// }

	/**
	 * Method removeIndexes()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#removeIndexes(gama.api.runtime.scope.IScope,
	 *      gama.api.types.misc.IContainer)
	 */
	@Override
	public void removeIndexes(final IScope scope, final IRuntimeContainer<?, ?> index) {
		for (final Object key : index.iterable(scope)) { remove(key); }
	}

	/**
	 * Method buildValue()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildValue(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	@Override
	public V buildValue(final IScope scope, final Object object) {
		// If we pass a pair to this method, but the content type is not a pair,
		// then it is is interpreted as a key + a value by addValue()
		if (!FLAGS.CAST_CONTAINER_CONTENTS
				|| object instanceof IPair && !getGamlType().getContentType().isTranslatableInto(Types.PAIR))
			return (V) object;
		return (V) getGamlType().getContentType().cast(scope, object, null, false);
	}

	/**
	 * Method buildIndex()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildIndex(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	@Override
	public K buildIndex(final IScope scope, final Object object) {
		return FLAGS.CAST_CONTAINER_CONTENTS ? (K) getGamlType().getKeyType().cast(scope, object, null, false)
				: (K) object;
	}

	/**
	 * Iterable.
	 *
	 * @param scope
	 *            the scope
	 * @return the java.lang. iterable
	 */
	@Override
	public java.lang.Iterable<V> iterable(final IScope scope) {
		return values();
	}

	/**
	 * Gets the from indices list.
	 *
	 * @param scope
	 *            the scope
	 * @param indices
	 *            the indices
	 * @return the from indices list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public V getFromIndicesList(final IScope scope, final IList<K> indices) throws GamaRuntimeException {
		if (indices == null || indices.isEmpty()) return null;
		return get(scope, indices.get(0));
		// We do not consider the case where multiple indices are used. Maybe
		// could be used in the
		// future to return a list of values ?
	}

	/**
	 * Contains.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	public boolean contains(final IScope scope, final Object o) {
		// AD: see Issue 918 and #2772
		return /* containsKey(o) || */containsValue(o);
	}

	/**
	 * Returns the list of values by default (NOT the list of pairs) Method listValue()
	 *
	 * @see gama.api.types.misc.IContainer#listValue(gama.api.runtime.scope.IScope)
	 */
	@Override
	public IList<V> listValue(final IScope scope, final IType contentsType, final boolean copy) {
		IType myContentsType = getGamlType().getContentType();
		if (GamaType.requiresCasting(contentsType, myContentsType))
			return GamaListFactory.create(scope, contentsType, values());
		if (!copy) return GamaListFactory.wrap(contentsType, values());
		return GamaListFactory.createWithoutCasting(contentsType, values());
	}

	/**
	 * Matrix value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IMatrix<?> matrixValue(final IScope scope, final IType<?> contentsType, final boolean copy)
			throws GamaRuntimeException {
		// No attempt to coerce the contentsType, as both keys and values should
		// be in the same matrix
		final IMatrix<?> matrix = GamaMatrixFactory.create(2, size(), contentsType);
		int i = 0;
		for (final Map.Entry entry : entrySet()) {
			matrix.set(scope, 0, i, GamaType.toType(scope, entry.getKey(), contentsType, false));
			matrix.set(scope, 1, i, GamaType.toType(scope, entry.getValue(), contentsType, false));
			i++;
		}
		return matrix;
	}

	/**
	 * Matrix value.
	 *
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param preferredSize
	 *            the preferred size
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentsType, final IPoint preferredSize,
			final boolean copy) throws GamaRuntimeException {
		return matrixValue(scope, contentsType, copy);
	}

	/**
	 * Serialize.
	 *
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "map(" + getPairs().serializeToGaml(includingBuiltIn) + ")";
	}

	/**
	 * Map value.
	 *
	 * @param scope
	 *            the scope
	 * @param keyType
	 *            the key type
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i map
	 */
	@Override
	public IMap mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy) {
		IType myKeyType = getGamlType().getKeyType();
		IType myContentsType = getGamlType().getContentType();
		final boolean coerceKey = GamaType.requiresCasting(keyType, myKeyType);
		final boolean coerceValue = GamaType.requiresCasting(contentsType, myContentsType);
		if (coerceKey || coerceValue) {
			final IMap result = GamaMapFactory.create(keyType, contentsType, size());
			for (final Map.Entry<K, V> entry : entrySet()) {
				result.put(coerceKey ? result.buildIndex(scope, entry.getKey()) : entry.getKey(),
						coerceValue ? result.buildValue(scope, entry.getValue()) : entry.getValue());
			}
			return result;
		}
		if (copy) return copy(scope);
		if (!keyType.equals(myKeyType) || !contentsType.equals(myContentsType))
			return GamaMapFactory.wrap(keyType, contentsType, this);
		return this;

	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 */
	@Override
	public IMap<K, V> copy(final IScope scope) {
		return createWithoutCasting((IType<K>) getGamlType().getKeyType(), (IType<V>) getGamlType().getContentType(),
				this, isOrdered());
	}

	/**
	 * Method put()
	 *
	 * @see gama.api.types.misc.IContainer#put(gama.api.runtime.scope.IScope, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setValueAtIndex(final IScope scope, final Object index, final V value) {
		final K key = buildIndex(scope, index);
		final V val = buildValue(scope, value);
		put(key, val);
	}

	/**
	 * Reverse.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 */

	@Override
	public GamaMap reverse(final IScope scope) {
		final GamaMap map = new GamaMap(size(), getGamlType().getContentType(), getGamlType().getKeyType());
		forEach((k, v) -> map.put(v, k));
		return map;
	}

	/**
	 * Method removeAll()
	 *
	 * @see gama.api.types.misc.IContainer#removeAll(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {
		values().removeIf(v -> Objects.equal(value, v));
	}

	/**
	 * Method remove()
	 *
	 * @see gama.api.types.misc.IContainer#remove(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void removeValue(final IScope scope, final Object value) {
		// Dont know what to do... Removing the first pair with value = value ?
		final Collection<V> values = values();
		for (V v : values) {
			if (Objects.equal(v, value)) {
				values.remove(v);
				return;
			}
		}
	}

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	@Override
	public IList<K> getKeys() {
		// See issue #2792. key can be used to modify the map...
		return GamaListFactory.<K> createWithoutCasting(getGamlType().getKeyType(), keySet());
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	@Override
	public IList<V> getValues() {
		return GamaListFactory.<V> wrap((IType<V>) getGamlType().getContentType(), values());
	}

	/**
	 * Gets the.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @return the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public V get(final IScope scope, final K index) throws GamaRuntimeException {
		return get(index);
	}

	/**
	 * Method setAll()
	 *
	 * @see gama.api.types.misc.IContainer#setAll(gama.api.runtime.scope.IScope, java.lang.Object)
	 */
	@Override
	public void setAllValues(final IScope scope, final V value) {
		replaceAll((k, v) -> value);
	}

	/**
	 * Gets the pairs.
	 *
	 * @return the pairs
	 */
	@Override
	public IPairList getPairs() {
		// FIXME: in the future, this method will be directly operating upon the
		// entry set (so as to avoir duplications). See IPair
		final GamaPairList pairs = new GamaPairList(this);
		forEach((key, value) -> pairs.add(
				GamaPairFactory.createWith(key, value, getGamlType().getKeyType(), getGamlType().getContentType())));
		return pairs;
	}

	/**
	 * For each pair.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean forEachPair(final BiConsumerWithPruning<K, V> visitor) {
		for (Entry<K, V> entry : entrySet()) { if (!visitor.process(entry.getKey(), entry.getValue())) return false; }
		return true;
	}

	/**
	 * For each value.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean forEachValue(final ConsumerWithPruning<? super V> visitor) {
		for (Entry<K, V> entry : entrySet()) { if (!visitor.process(entry.getValue())) return false; }
		return true;
	}

	/**
	 * For each key.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	@Override
	public boolean forEachKey(final ConsumerWithPruning<K> visitor) {
		for (Entry<K, V> entry : entrySet()) { if (!visitor.process(entry.getKey())) return false; }
		return true;
	}

	/**
	 * To json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 28 oct. 2023
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		IJsonObject result = json.object();
		for (java.util.Map.Entry<K, V> entry : this.entrySet()) {
			result.add(entry.getKey().toString(), json.valueOf(entry.getValue()));
		}
		return result;
	}

	/**
	 * Compute runtime type.
	 *
	 * @param scope
	 *            the scope
	 * @return the i type
	 */
	@Override
	public IType<?> computeRuntimeType(final IScope scope) {
		return Types.MAP.of(getKeys().computeRuntimeType(scope).getContentType(),
				getValues().computeRuntimeType(scope).getContentType());
	}

}
