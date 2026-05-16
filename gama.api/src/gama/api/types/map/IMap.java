/*******************************************************************************************************
 *
 * IMap.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.map;

import java.util.Map;
import java.util.Spliterator;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ITypeProvider;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IRuntimeContainer;
import gama.api.types.pair.IPair;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.ConsumerWithPruning;

/**
 * The main interface for type-safe, key-value maps in the GAMA modeling platform.
 *
 * <p>
 * {@code IMap} extends Java's {@link Map} interface while integrating with GAMA's type system and runtime
 * capabilities.
 * </p>
 *
 * <p>
 * In the GAML type hierarchy, {@code map} is now distinct from {@code container}. At the Java level, {@code IMap}
 * now depends on the extracted runtime contracts in {@link IRuntimeContainer} rather than inheriting the legacy
 * {@link IContainer} capability branch directly.
 * </p>
 *
 * <p>
 * It provides:
 * </p>
 * <ul>
 * <li><b>Dual Type Tracking</b>: Maintains both key and value types via {@link IContainerType}</li>
 * <li><b>Ordered Access</b>: Optionally preserves insertion order (implementation-dependent)</li>
 * <li><b>GAML Integration</b>: Pseudo-variables (keys, values, pairs), operators, and serialization</li>
 * <li><b>Scope-Aware Operations</b>: Operations accept {@link IScope} for proper context</li>
 * <li><b>Specialized Iteration</b>: Pruning-capable forEach methods for efficient traversal</li>
 * </ul>
 *
 * <h2>Core Features</h2>
 *
 * <h3>1. GAML Pseudo-Variables</h3>
 * <p>
 * Maps expose three pseudo-attributes accessible in GAML:
 * </p>
 *
 * <pre>
 * // In GAML
 * map&lt;string, int&gt; myMap &lt;- ["a"::1, "b"::2, "c"::3];
 * list&lt;string&gt; k &lt;- myMap.keys;   // ["a", "b", "c"]
 * list&lt;int&gt; v &lt;- myMap.values;     // [1, 2, 3]
 * list&lt;pair&gt; p &lt;- myMap.pairs;     // [a::1, b::2, c::3]
 * </pre>
 *
 * <pre>
 * // In Java
 * IMap&lt;String, Integer&gt; map = GamaMapFactory.create(Types.STRING, Types.INT);
 * map.put("a", 1);
 *
 * IList&lt;String&gt; keys = map.getKeys(); // List of keys
 * IList&lt;Integer&gt; values = map.getValues(); // List of values
 * IPairList pairs = map.getPairs(); // List of Map.Entry objects
 * </pre>
 *
 * <h3>2. Reverse Operation</h3>
 * <p>
 * The {@code reverse} operator swaps keys and values:
 * </p>
 *
 * <pre>
 * IMap&lt;String, Integer&gt; original = GamaMapFactory.create(Types.STRING, Types.INT);
 * original.put("a", 1);
 * original.put("b", 2);
 *
 * IMap&lt;Integer, String&gt; reversed = original.reverse(scope);
 * // reversed: {1 -&gt; "a", 2 -&gt; "b"}
 * </pre>
 *
 * <h3>3. Type Building</h3>
 * <p>
 * The interface provides methods to convert keys and values according to the map's types:
 * </p>
 * <ul>
 * <li>{@link #buildValue(IScope, Object)} - Casts values to the content type</li>
 * <li>{@link #buildIndex(IScope, Object)} - Casts keys to the key type</li>
 * </ul>
 *
 * <h3>4. Efficient Iteration with Pruning</h3>
 * <p>
 * Specialized forEach methods support early termination:
 * </p>
 *
 * <pre>
 * // Stop iteration when condition is met
 * map.forEachPair((key, value) -&gt; {
 * 	if (value &gt; 100) return false; // Stop iteration
 * 	processEntry(key, value);
 * 	return true; // Continue iteration
 * });
 * </pre>
 *
 * <h2>Ordering</h2>
 * <p>
 * The {@link #isOrdered()} method indicates whether the map preserves insertion order:
 * </p>
 * <ul>
 * <li><b>Ordered</b>: {@link GamaMap} (uses LinkedHashMap) - keys/values/pairs maintain insertion order</li>
 * <li><b>Unordered</b>: Maps wrapping HashMap - iteration order is unpredictable</li>
 * </ul>
 *
 * <pre>
 * IMap&lt;String, Integer&gt; orderedMap = GamaMapFactory.create(Types.STRING, Types.INT);
 * // orderedMap.isOrdered() returns true
 *
 * Map&lt;String, Integer&gt; hashMap = new HashMap&lt;&gt;();
 * IMap&lt;String, Integer&gt; unorderedMap = GamaMapFactory.wrap(Types.STRING, Types.INT, hashMap);
 * // unorderedMap.isOrdered() may return false (implementation-dependent)
 * </pre>
 *
 * <h2>Usage in GAML</h2>
 * <p>
 * Maps are fundamental associative collection types in GAML. They still support many container-like operators, but
 * they no longer belong to the {@code container} inheritance branch of the GAML type hierarchy:
 * </p>
 *
 * <pre>
 * // Creation
 * map&lt;string, int&gt; scores &lt;- ["Alice"::95, "Bob"::87, "Charlie"::92];
 *
 * // Access
 * int aliceScore &lt;- scores["Alice"];
 *
 * // Modification
 * scores["David"] &lt;- 88;
 * remove key: "Bob" from: scores;
 *
 * // Iteration
 * loop key over: scores.keys {
 *     write key + ": " + scores[key];
 * }
 * </pre>
 *
 * <h2>IPairList Interface</h2>
 * <p>
 * Nested interface representing the ordered list view returned by {@link #getPairs()}:
 * </p>
 * <ul>
 * <li>Returned by {@link #getPairs()}</li>
 * <li>Contains {@link IPair} elements exposing both key and value</li>
 * <li>Preserves insertion order when the underlying map is ordered</li>
 * </ul>
 *
 * <h2>Default Implementations</h2>
 * <p>
 * Most implementing classes ({@link GamaMap}, {@link GamaMapWrapper}) provide standard behavior. The interface itself
 * does not provide default implementations for most methods, delegating to concrete classes.
 * </p>
 *
 * <h2>Performance Considerations</h2>
 * <p>
 * Performance depends on the underlying implementation:
 * </p>
 * <ul>
 * <li><b>GamaMap (LinkedHashMap)</b>: O(1) access, ordered iteration</li>
 * <li><b>HashMap-based</b>: O(1) access, unordered iteration (slightly faster)</li>
 * <li><b>TreeMap-based</b>: O(log n) access, sorted iteration</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Like standard Java Maps, {@code IMap} implementations are typically <b>not thread-safe</b>. Use
 * {@link GamaMapFactory#synchronizedMap} for thread-safe access.
 * </p>
 *
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li><b>Null Keys/Values</b>: Support depends on underlying implementation (LinkedHashMap allows null values, not null
 * keys)</li>
 * <li><b>Value at Index</b>: {@link #valueAt(int)} provides indexed access to values (in insertion order for ordered
 * maps)</li>
 * <li><b>Type Casting</b>: Controlled by {@code FLAGS.CAST_CONTAINER_CONTENTS}</li>
 * </ul>
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 *
 * @see GamaMapFactory for creating IMap instances
 * @see GamaMap for the primary implementation
 * @see IRuntimeContainer for the extracted shared Java-level runtime contract
 * @see IContainer for the legacy container API from which maps are now detached
 * @see Map for Java Map interface
 *
 * @author drogoul
 */
@vars ({ @variable (
		name = IMap.KEYS,
		type = IType.LIST,
		of = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
		doc = { @doc ("Returns the list of keys of this map (in their order of insertion)") }),
		@variable (
				name = IMap.VALUES,
				type = IType.LIST,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of values of this map (in their order of insertion)") }),
		@variable (
				name = IMap.PAIRS,
				type = IType.LIST,
				of = ITypeProvider.KEY_AND_CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of pairs (key, value) that compose this map") }) })
@SuppressWarnings ("unchecked")

public interface IMap<K, V> extends Map<K, V>, IRuntimeContainer.Modifiable<K, V, K, V>,
		IRuntimeContainer.Addressable<K, V, K, V> {

	/**
	 * Ordered list view of the pairs contained in a map.
	 *
	 * <p>
	 * This view is primarily used by the {@link #getPairs()} getter and by operators that need stable traversal of map
	 * entries as {@link IPair} values.
	 * </p>
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 */
	interface IPairList<K, V> extends IList<IPair<K, V>> {

		/**
		 * Spliterator.
		 *
		 * @return the spliterator
		 */
		@Override
		default Spliterator<IPair<K, V>> spliterator() {
			return IList.super.spliterator();
		}

	}

	/** Name of the pseudo-variable exposing the list of keys. */
	String KEYS = "keys";

	/** Name of the pseudo-variable exposing the list of values. */
	String VALUES = "values";

	/** Name of the pseudo-variable exposing the list of key/value pairs. */
	String PAIRS = "pairs";

	/**
	 * Returns the value located at the specified iteration position.
	 *
	 * <p>
	 * The index refers to the traversal order of the map values, which is stable only when {@link #isOrdered()} is
	 * {@code true}.
	 * </p>
	 *
	 * @param index
	 *            the zero-based position of the value in iteration order
	 * @return the value at that position
	 */
	V valueAt(final int index);

	/**
	 * Casts or normalizes a value so it matches this map content type.
	 *
	 * <p>
	 * Implementations honor the map type and the current runtime casting policy before the value is inserted or
	 * replaced.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param object
	 *            the candidate value to adapt
	 * @return the value converted to the map content type
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildValue(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	V buildValue(final IScope scope, final Object object);

	/**
	 * Casts or normalizes a key so it matches this map key type.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param object
	 *            the candidate key to adapt
	 * @return the key converted to the map key type
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildIndex(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	K buildIndex(final IScope scope, final Object object);

	/**
	 * Returns whether this map contains the provided value.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param o
	 *            the searched value
	 * @return {@code true} if the value is present in the map
	 * @throws GamaRuntimeException
	 *             if the test cannot be evaluated
	 */
	@operator (
			value = { "contains", "contains_value" },
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.MAP },
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "true if the map contains the right operand as one of its values, false otherwise",
			examples = { @example (
					value = "[1::2, 3::4, 5::6] contains 4",
					equals = "true"),
				@example (
						value = "[1::2, 3::4, 5::6] contains 3",
						equals = "false") },
			see = { "contains_key" })
	@Override
	boolean contains(IScope scope, Object o) throws GamaRuntimeException;

	/**
	 * Returns whether this map contains the provided key.
	 *
	 * @param scope
	 *            the current execution scope
	 * @param o
	 *            the searched key
	 * @return {@code true} if the key is present in the map
	 * @throws GamaRuntimeException
	 *             if the test cannot be evaluated
	 */
	@operator (
			value = { "contains_key" },
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.MAP },
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "true if the map contains the right operand as one of its keys, false otherwise",
			examples = { @example (
					value = "[1::2, 3::4, 5::6] contains_key 3",
					equals = "true"),
				@example (
						value = "[1::2, 3::4, 5::6] contains_key 4",
						equals = "false") },
			see = { "contains" })
	@Override
	boolean containsKey(IScope scope, Object o) throws GamaRuntimeException;

	/**
	 * Returns the first value of this map in iteration order.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the first mapped value, or {@code nil} if the map is empty
	 * @throws GamaRuntimeException
	 *             if the value cannot be retrieved
	 */
	@operator (
			value = "first",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.MAP },
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "the first value of the first pair of the map in iteration order",
			examples = { @example (
					value = "first([1::2, 3::4])",
					equals = "2") },
			see = { "last" })
	@Override
	V firstValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the last value of this map in iteration order.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the last mapped value, or {@code nil} if the map is empty
	 * @throws GamaRuntimeException
	 *             if the value cannot be retrieved
	 */
	@operator (
			value = "last",
			can_be_const = true,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.MAP },
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "the last value of the last pair of the map in iteration order",
			examples = { @example (
					value = "last([1::2, 3::4])",
					equals = "4") },
			see = { "first" })
	@Override
	V lastValue(IScope scope) throws GamaRuntimeException;

	/**
	 * Returns the number of key/value pairs stored in this map.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the map size
	 */
	@operator (
			value = "length",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.MAP },
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "the number of key-value mappings contained in the map",
			examples = { @example (
					value = "length([1::2, 3::4])",
					equals = "2") })
	@Override
	int length(IScope scope);

	/**
	 * Returns whether this map is empty.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return {@code true} if the map contains no pair
	 */
	@operator (
			value = "empty",
			can_be_const = true,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.MAP },
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "true if the map contains no key-value mappings, false otherwise",
			examples = { @example (
					value = "empty([] as_map (each::each))",
					equals = "true",
					test = false) })
	@Override
	boolean isEmpty(IScope scope);

	/**
	 * Reverse.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 */
	@operator (
			value = "reverse",
			can_be_const = true,
			type = IType.MAP,
			content_type = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
			index_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.MAP },
			concept = { IConcept.MAP })
	@doc (
			value = "Specialization of the reverse operator for maps. Reverses keys and values",
			comment = "",
			examples = { @example ("map<int,int> m <- [1::111,2::222, 3::333, 4::444];"), @example (
					value = "reverse(m)",
					equals = "map([111::1,222::2,333::3,444::4])") })

	@test ("map<int,int> m2 <- [1::111,2::222, 3::333, 4::444]; reverse(m2) = map([111::1,222::2,333::3,444::4])")

	@Override
	IMap reverse(final IScope scope);

	/**
	 * Returns one mapped value, typically chosen at random.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return one of the map values, or {@code nil} if the map is empty
	 */
	@operator (
			value = { "one_of", "any" },
			can_be_const = false,
			type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.CONTAINER, IOperatorCategory.MAP },
			concept = { IConcept.CONTAINER, IConcept.MAP })
	@doc (
			value = "one of the values stored in the map, chosen from a random key",
			examples = { @example (
					value = "int x <- one_of([1::2, 3::4]);",
					equals = "2 or 4",
					test = false) },
			see = { "contains" })
	@Override
	V anyValue(IScope scope);

	/**
	 * Returns the keys of this map as a list.
	 *
	 * @return the keys in map iteration order
	 */
	@getter ("keys")
	IList<K> getKeys();

	/**
	 * Returns the values of this map as a list.
	 *
	 * @return the values in map iteration order
	 */
	@getter ("values")
	IList<V> getValues();

	/**
	 * Returns the pairs of this map as an ordered list view.
	 *
	 * @return the map entries exposed as {@link IPair} values
	 */
	@getter (PAIRS)
	IPairList<K, V> getPairs();

	/**
	 * Visits each key/value pair until the visitor requests pruning.
	 *
	 * @param visitor
	 *            the visitor invoked on each pair
	 * @return {@code true} if the whole traversal completed, {@code false} if it was pruned early
	 */
	boolean forEachPair(final BiConsumerWithPruning<K, V> visitor);

	/**
	 * Indicates whether this map preserves a stable iteration order.
	 *
	 * @return {@code true} if iteration order is stable and meaningful
	 */
	boolean isOrdered();

	/**
	 * Visits each value until the visitor requests pruning.
	 *
	 * @param visitor
	 *            the visitor invoked on each value
	 * @return {@code true} if the whole traversal completed, {@code false} if it was pruned early
	 */
	boolean forEachValue(final ConsumerWithPruning<? super V> visitor);

	/**
	 * Visits each key until the visitor requests pruning.
	 *
	 * @param visitor
	 *            the visitor invoked on each key
	 * @return {@code true} if the whole traversal completed, {@code false} if it was pruned early
	 */
	boolean forEachKey(final ConsumerWithPruning<K> visitor);

	/**
	 * Returns a copy of this map.
	 *
	 * @param scope
	 *            the current execution scope
	 * @return a map containing the same entries
	 * @throws GamaRuntimeException
	 *             if the copy cannot be produced
	 */
	@Override
	IMap<K, V> copy(IScope scope) throws GamaRuntimeException;

}