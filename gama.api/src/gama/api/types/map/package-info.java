/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * Provides the core map implementation for the GAMA modeling and simulation platform.
 *
 * <h2>Overview</h2>
 *
 * This package contains the complete infrastructure for type-safe, ordered map containers in GAMA, bridging Java's
 * {@link java.util.Map} interface with GAMA's type system and simulation runtime. The package provides support for:
 *
 * <ul>
 * <li>Type-safe maps with tracked key and value types</li>
 * <li>Ordered insertion-preserving maps (default)</li>
 * <li>Thread-safe and concurrent map variants</li>
 * <li>Integration between Java collections and GAMA containers</li>
 * <li>Factory-based creation and wrapping of maps</li>
 * </ul>
 *
 * <h2>Core Components</h2>
 *
 * <h3>{@link gama.api.types.map.IMap}</h3>
 *
 * The main interface for maps in GAMA, extending both {@link java.util.Map} and
 * {@link gama.api.types.misc.IContainer} interfaces. It provides:
 *
 * <ul>
 * <li><b>Type tracking:</b> Maintains GAMA type information for keys and values</li>
 * <li><b>Ordered access:</b> Support for insertion-order preservation</li>
 * <li><b>GAML integration:</b> Variables for keys, values, and pairs accessible from GAML</li>
 * <li><b>Operators:</b> Support for GAML operators like reverse, copy, etc.</li>
 * <li><b>Advanced iteration:</b> Methods for iterating over pairs, keys, and values with pruning support</li>
 * </ul>
 *
 * <p>
 * Example usage in Java:
 * </p>
 *
 * <pre>
 * // Create a typed map
 * IMap&lt;String, Integer&gt; scores = GamaMapFactory.create(Types.STRING, Types.INT);
 * scores.put("Alice", 100);
 * scores.put("Bob", 85);
 *
 * // Access as GAML variables
 * IList&lt;String&gt; keys = scores.getKeys(); // ["Alice", "Bob"]
 * IList&lt;Integer&gt; values = scores.getValues(); // [100, 85]
 *
 * // Iterate with pruning
 * scores.forEachPair((key, value) -&gt; {
 * 	System.out.println(key + ": " + value);
 * 	return true; // continue iteration
 * });
 * </pre>
 *
 * <p>
 * GAML usage example:
 * </p>
 *
 * <pre>
 * map&lt;string, int&gt; scores &lt;- ["Alice"::100, "Bob"::85];
 * list&lt;string&gt; names &lt;- scores.keys;  // ["Alice", "Bob"]
 * list&lt;int&gt; points &lt;- scores.values;    // [100, 85]
 * map&lt;int, string&gt; reversed &lt;- reverse(scores); // [100::"Alice", 85::"Bob"]
 * </pre>
 *
 * <h3>{@link gama.api.types.map.GamaMap}</h3>
 *
 * The primary concrete implementation of {@link gama.api.types.map.IMap}, backed by a
 * {@link java.util.LinkedHashMap} to ensure insertion-order preservation. This class:
 *
 * <ul>
 * <li>Provides efficient ordered map operations</li>
 * <li>Maintains type information for GAML integration</li>
 * <li>Supports all IMap operations including iteration, transformation, and serialization</li>
 * <li>Offers JSON export/import capabilities</li>
 * </ul>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * // Maps preserve insertion order
 * IMap&lt;Integer, String&gt; orderedMap = GamaMapFactory.create(Types.INT, Types.STRING);
 * orderedMap.put(3, "three");
 * orderedMap.put(1, "one");
 * orderedMap.put(2, "two");
 * // Iteration order: 3, 1, 2
 * </pre>
 *
 * <h3>{@link gama.api.types.map.GamaMapFactory}</h3>
 *
 * Factory class providing static methods for creating and managing {@link gama.api.types.map.IMap} instances. It
 * offers flexible creation options:
 *
 * <ul>
 * <li><b>Basic creation:</b> {@code create()}, {@code createOrdered()}, {@code createUnordered()}</li>
 * <li><b>Typed creation:</b> {@code create(IType key, IType contents)}</li>
 * <li><b>Capacity-aware:</b> {@code create(IType key, IType contents, int size)}</li>
 * <li><b>Thread-safe maps:</b> {@code synchronizedMap()}, {@code concurrentMap()}</li>
 * <li><b>Wrapping existing maps:</b> {@code wrap(Map)}, {@code wrap(IType key, IType contents, Map)}</li>
 * </ul>
 *
 * <p>
 * Examples:
 * </p>
 *
 * <pre>
 * // Create an ordered map with default types
 * IMap&lt;Object, Object&gt; map1 = GamaMapFactory.create();
 *
 * // Create a typed map
 * IMap&lt;String, Double&gt; map2 = GamaMapFactory.create(Types.STRING, Types.FLOAT);
 *
 * // Wrap a Java Map
 * Map&lt;Integer, String&gt; javaMap = new HashMap&lt;&gt;();
 * IMap&lt;Integer, String&gt; wrappedMap = GamaMapFactory.wrap(Types.INT, Types.STRING, javaMap);
 *
 * // Create a thread-safe map
 * IMap&lt;String, Object&gt; concurrent = GamaMapFactory.concurrentMap();
 *
 * // Create a synchronized map from existing IMap
 * IMap&lt;?, ?&gt; syncMap = GamaMapFactory.synchronizedMap(map2);
 * </pre>
 *
 * <h3>{@link gama.api.types.map.GamaMapWrapper}</h3>
 *
 * A full-featured wrapper implementation that adapts any Java {@link java.util.Map} into an
 * {@link gama.api.types.map.IMap}. This class:
 *
 * <ul>
 * <li>Delegates all Map operations to the underlying wrapped map</li>
 * <li>Adds GAMA type information and container behavior</li>
 * <li>Supports both ordered and unordered maps</li>
 * <li>Provides full IMap functionality including GAML variable access</li>
 * </ul>
 *
 * <p>
 * Use this when you need to integrate existing Java maps into GAMA's type system:
 * </p>
 *
 * <pre>
 * // Wrap an existing map with type information
 * Map&lt;String, Agent&gt; agentMap = getExternalMap();
 * IMap&lt;String, Agent&gt; gamaMap = new GamaMapWrapper&lt;&gt;(agentMap, Types.STRING, Types.AGENT, true);
 *
 * // The wrapped map is now fully integrated with GAMA
 * IList&lt;Agent&gt; agents = gamaMap.getValues();
 * </pre>
 *
 * <h3>{@link gama.api.types.map.GamaMapSimpleWrapper}</h3>
 *
 * An abstract base class for creating simple map wrappers without full type information. This is useful for
 * specialized map implementations where:
 *
 * <ul>
 * <li>Type information is not available or not needed</li>
 * <li>Custom delegation behavior is required</li>
 * <li>Lightweight wrapping is preferred over full type tracking</li>
 * </ul>
 *
 * <p>
 * Example of extending:
 * </p>
 *
 * <pre>
 * public class CustomMapWrapper&lt;K, V&gt; extends GamaMapSimpleWrapper&lt;K, V&gt; {
 * 	private final Map&lt;K, V&gt; backingMap;
 *
 * 	public CustomMapWrapper(Map&lt;K, V&gt; map) {
 * 		this.backingMap = map;
 * 	}
 *
 * 	&#64;Override
 * 	protected Map&lt;K, V&gt; delegate() {
 * 		return backingMap;
 * 	}
 *
 * 	&#64;Override
 * 	public boolean isOrdered() {
 * 		return false; // or determine based on backingMap type
 * 	}
 * }
 * </pre>
 *
 * <h3>{@link gama.api.types.map.GamaMapSupplier}</h3>
 *
 * A {@link java.util.function.Supplier} implementation for lazy creation of {@link gama.api.types.map.IMap}
 * instances. This is useful when:
 *
 * <ul>
 * <li>Maps need to be created on-demand</li>
 * <li>Working with stream operations requiring suppliers</li>
 * <li>Implementing factory patterns with deferred instantiation</li>
 * </ul>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * // Create a supplier for maps with specific types
 * Supplier&lt;IMap&lt;String, Integer&gt;&gt; mapSupplier = new GamaMapSupplier(Types.STRING, Types.INT);
 *
 * // Get a new map instance whenever needed
 * IMap&lt;String, Integer&gt; map1 = mapSupplier.get();
 * IMap&lt;String, Integer&gt; map2 = mapSupplier.get(); // Different instance
 *
 * // Use with streams
 * Map&lt;String, List&lt;Integer&gt;&gt; grouped = data.stream()
 * 		.collect(Collectors.groupingBy(Item::getCategory, mapSupplier, Collectors.mapping(Item::getValue, toList())));
 * </pre>
 *
 * <h2>Type System Integration</h2>
 *
 * All maps in this package integrate with GAMA's type system through {@link gama.api.gaml.types.IContainerType}:
 *
 * <ul>
 * <li><b>Key type:</b> Retrieved via {@code getGamlType().getKeyType()}</li>
 * <li><b>Content type:</b> Retrieved via {@code getGamlType().getContentType()}</li>
 * <li><b>Type checking:</b> Automatic validation when adding elements</li>
 * <li><b>Type casting:</b> Automatic conversion to target types when needed</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * The package provides multiple options for thread-safe map usage:
 *
 * <ul>
 * <li><b>{@code synchronizedMap(IMap)}:</b> Wraps an IMap with synchronized access</li>
 * <li><b>{@code concurrentMap()}:</b> Creates a ConcurrentHashMap-backed IMap</li>
 * <li><b>{@code synchronizedOrderedMap()}:</b> Creates a synchronized ordered Map (not IMap)</li>
 * </ul>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * // For high-concurrency scenarios
 * IMap&lt;String, Object&gt; concurrent = GamaMapFactory.concurrentMap();
 *
 * // For synchronized access to existing map
 * IMap&lt;String, Integer&gt; original = GamaMapFactory.create(Types.STRING, Types.INT);
 * IMap&lt;String, Integer&gt; synchronized = GamaMapFactory.synchronizedMap(original);
 * </pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 * <li><b>Use GamaMapFactory:</b> Always create maps through the factory rather than direct constructors</li>
 * <li><b>Specify types:</b> Provide explicit type information for better type safety and GAML integration</li>
 * <li><b>Preserve order when needed:</b> Use ordered maps (default) when insertion order matters</li>
 * <li><b>Choose appropriate thread safety:</b> Use concurrent maps for high concurrency, synchronized for simple
 * multi-threading</li>
 * <li><b>Wrap existing maps:</b> Use {@code wrap()} methods to integrate external Java maps</li>
 * </ol>
 *
 * <h2>Integration with GAML</h2>
 *
 * Maps created with this package are fully integrated with the GAML language:
 *
 * <pre>
 * // Declaration
 * map&lt;string, agent&gt; agentsByName;
 *
 * // Initialization
 * agentsByName &lt;- ["agent1"::agent1, "agent2"::agent2];
 *
 * // Access
 * agent a &lt;- agentsByName["agent1"];
 * list&lt;string&gt; names &lt;- agentsByName.keys;
 * list&lt;agent&gt; agents &lt;- agentsByName.values;
 * list&lt;pair&lt;string,agent&gt;&gt; entries &lt;- agentsByName.pairs;
 *
 * // Operators
 * map&lt;agent, string&gt; reversed &lt;- reverse(agentsByName);
 * </pre>
 *
 * @see gama.api.types.map.IMap
 * @see gama.api.types.map.GamaMap
 * @see gama.api.types.map.GamaMapFactory
 * @see gama.api.types.map.GamaMapWrapper
 * @see gama.api.types.map.GamaMapSimpleWrapper
 * @see gama.api.types.map.GamaMapSupplier
 * @see gama.api.types.misc.IContainer
 * @see gama.api.gaml.types.IContainerType
 * @see java.util.Map
 */
package gama.api.types.map;
