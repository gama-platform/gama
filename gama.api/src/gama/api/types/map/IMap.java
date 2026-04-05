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
import gama.api.types.pair.IPair;
import gama.api.utils.interfaces.BiConsumerWithPruning;
import gama.api.utils.interfaces.ConsumerWithPruning;

/**
 * The main interface for type-safe, key-value maps in the GAMA modeling platform.
 *
 * <p>
 * {@code IMap} extends Java's {@link Map} interface while integrating with GAMA's type system and runtime capabilities.
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
 * Maps are fundamental container types in GAML:
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
 * Nested interface that combines {@link Set} and {@link IList} for entry pairs:
 * </p>
 * <ul>
 * <li>Returned by {@link #getPairs()}</li>
 * <li>Implements both Set&lt;Map.Entry&gt; and IList&lt;Map.Entry&gt;</li>
 * <li>Preserves order (in ordered maps)</li>
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
 * @see IContainer for parent container interfaces
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

public interface IMap<K, V> extends Map<K, V>, IContainer.Modifiable<K, V, K, V>, IContainer.Addressable<K, V, K, V> {

	/**
	 * The Interface IPairList.
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

	/** The keys. */
	String KEYS = "keys";

	/** The values. */
	String VALUES = "values";

	/** The pairs. */
	String PAIRS = "pairs";

	/**
	 *
	 *
	 * /** Value at.
	 *
	 * @param index
	 *            the index
	 * @return the v
	 */
	V valueAt(final int index);

	/**
	 * Method buildValue()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildValue(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	V buildValue(final IScope scope, final Object object);

	/**
	 * Method buildIndex()
	 *
	 * @see gama.api.types.misc.IContainer.ToSet#buildIndex(gama.api.runtime.scope.IScope, java.lang.Object,
	 *      gama.gaml.types.IContainerType)
	 */
	K buildIndex(final IScope scope, final Object object);

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
			category = { IOperatorCategory.CONTAINER },
			concept = { IConcept.CONTAINER })
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
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	@getter ("keys")
	IList<K> getKeys();

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	@getter ("values")
	IList<V> getValues();

	/**
	 * Gets the pairs.
	 *
	 * @return the pairs
	 */
	@getter (PAIRS)
	IPairList getPairs();

	/**
	 * For each pair.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean forEachPair(final BiConsumerWithPruning<K, V> visitor);

	/**
	 * Checks if is ordered.
	 *
	 * @return true, if is ordered
	 */
	boolean isOrdered();

	/**
	 * For each value.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean forEachValue(final ConsumerWithPruning<? super V> visitor);

	/**
	 * For each key.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean forEachKey(final ConsumerWithPruning<K> visitor);

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	IMap<K, V> copy(IScope scope) throws GamaRuntimeException;

}