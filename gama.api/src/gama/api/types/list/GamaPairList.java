/*******************************************************************************************************
 *
 * GamaPairList.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.list;

import gama.api.gaml.types.Types;
import gama.api.types.map.IMap;
import gama.api.types.pair.IPair;

/**
 * A specialized {@link IList} implementation for storing map entry pairs (key-value pairs).
 *
 * <p>
 * {@code GamaPairList} extends {@link GamaList} to provide a list of {@link Map.Entry} objects, implementing both
 * {@link IList} and {@link IMap.IPairList} interfaces. It is primarily used to represent the "pairs" pseudo-attribute
 * of GAMA maps.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Dual Interface</b>: Implements both {@link IList} and {@link Set} (via {@link IMap.IPairList})</li>
 * <li><b>Entry Storage</b>: Stores {@link Map.Entry} objects preserving key-value relationships</li>
 * <li><b>Type Tracking</b>: Maintains pair type with both key and value types</li>
 * <li><b>Map Integration</b>: Designed specifically for {@link IMap#getPairs()} operation</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * Typically created automatically when accessing the "pairs" attribute of a map:
 * </p>
 *
 * <pre>
 * IMap&lt;String, Integer&gt; map = GamaMapFactory.create(Types.STRING, Types.INT);
 * map.put("a", 1);
 * map.put("b", 2);
 *
 * // Get pairs - returns a GamaPairList
 * IPairList&lt;String, Integer&gt; pairs = map.getPairs();
 *
 * // Each element is a Map.Entry
 * for (Map.Entry&lt;String, Integer&gt; entry : pairs) {
 * 	String key = entry.getKey();
 * 	Integer value = entry.getValue();
 * }
 * </pre>
 *
 * <h2>In GAML</h2>
 * <p>
 * Used when accessing the "pairs" pseudo-variable of maps:
 * </p>
 *
 * <pre>
 * map&lt;string, int&gt; myMap &lt;- ["a"::1, "b"::2, "c"::3];
 * list&lt;pair&gt; pairs &lt;- myMap.pairs;
 *
 * loop p over: pairs {
 *     write "Key: " + p.key + ", Value: " + p.value;
 * }
 * </pre>
 *
 * <h2>Set vs List Behavior</h2>
 * <p>
 * While implementing {@link Set} via {@link IMap.IPairList}, {@code GamaPairList} is backed by an {@link ArrayList} and
 * thus:
 * </p>
 * <ul>
 * <li><b>Allows duplicates</b>: Can contain multiple entries with the same key or value</li>
 * <li><b>Preserves order</b>: Maintains insertion order from the map</li>
 * <li><b>Indexed access</b>: Supports get(index) operations</li>
 * </ul>
 *
 * <h2>Type System</h2>
 * <p>
 * The content type is a PAIR type parameterized with the map's key and value types:
 * </p>
 *
 * <pre>
 * // Map with String keys and Integer values
 * IMap&lt;String, Integer&gt; map = ...;
 * IPairList&lt;String, Integer&gt; pairs = map.getPairs();
 *
 * // Content type is pair&lt;string, int&gt;
 * IType contentType = pairs.getGamlType().getContentType();
 * // contentType equals Types.PAIR.of(Types.STRING, Types.INT)
 * </pre>
 *
 * <h2>Performance Characteristics</h2>
 * <p>
 * Inherits all performance characteristics from {@link GamaList}:
 * </p>
 * <ul>
 * <li><b>Random Access</b>: O(1) - indexed access to pairs</li>
 * <li><b>Iteration</b>: O(n) - linear iteration over all pairs</li>
 * <li><b>Add/Remove</b>: O(1) amortized at end, O(n) at arbitrary position</li>
 * </ul>
 *
 * <h2>Spliterator Override</h2>
 * <p>
 * Overrides {@link #spliterator()} to delegate to {@link IList#spliterator()}, ensuring proper stream support for lists
 * rather than sets.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * Not thread-safe. Concurrent access requires external synchronization.
 * </p>
 *
 * <h2>Implementation Notes</h2>
 * <ul>
 * <li>Pre-sized to match the source map's size for efficiency</li>
 * <li>Type is constructed as {@code Types.PAIR.of(keyType, valueType)}</li>
 * <li>Public constructor allows direct instantiation (unlike other list implementations)</li>
 * </ul>
 *
 * @param <K>
 *            the key type of the map entries
 * @param <V>
 *            the value type of the map entries
 *
 * @see IMap.IPairList
 * @see IMap#getPairs()
 * @see GamaList
 * @see Map.Entry
 *
 * @author drogoul
 */
public class GamaPairList<K, V> extends GamaList<IPair<K, V>> implements IMap.IPairList<K, V> {

	/**
	 * Instantiates a new gama pair list.
	 *
	 * @param map
	 *            the map
	 */
	public GamaPairList(final IMap<K, V> map) {
		super(map.size(), Types.PAIR.of(map.getGamlType().getKeyType(), map.getGamlType().getContentType()));
	}

}