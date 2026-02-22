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
 * Provides type-safe GAML list implementations with seamless integration between Java and GAMA ecosystems.
 *
 * <h2>Package Overview</h2>
 * <p>
 * This package defines the core list abstraction for the GAMA modeling platform, bridging Java's {@link java.util.List}
 * interface with GAMA's type system. It offers efficient implementations, factory methods, and wrapper classes that
 * handle type casting, wrapping, and conversion between various collection types.
 * </p>
 *
 * <h2>Core Components</h2>
 *
 * <h3>1. {@link gama.api.types.list.IList} - The Main List Interface</h3>
 * <p>
 * The central interface that extends both Java's {@link java.util.List} and GAMA's
 * {@link gama.api.types.misc.IContainer.Modifiable} and {@link gama.api.types.misc.IContainer.Addressable}
 * interfaces. It provides:
 * </p>
 * <ul>
 * <li>Standard list operations with GAMA type awareness</li>
 * <li>Conversion to other GAMA types (matrices, maps, etc.)</li>
 * <li>GAML serialization support</li>
 * <li>Type-safe element access and modification</li>
 * </ul>
 *
 * <pre>
 * // Using IList in GAMA models
 * IList&lt;Integer&gt; numbers = GamaListFactory.create(Types.INT);
 * numbers.add(42);
 * numbers.add(100);
 *
 * // Convert to matrix
 * IMatrix&lt;Integer&gt; matrix = numbers.matrixValue(scope, Types.INT, false);
 *
 * // Serialize to GAML
 * String gamlCode = numbers.serializeToGaml(false); // "[42,100]"
 * </pre>
 *
 * <h3>2. {@link gama.api.types.list.GamaList} - Core Implementation</h3>
 * <p>
 * The primary concrete implementation of {@link gama.api.types.list.IList}, extending {@link java.util.ArrayList}
 * with GAMA-specific functionality:
 * </p>
 * <ul>
 * <li>Backed by ArrayList for efficient random access</li>
 * <li>Type-safe with {@link gama.api.gaml.types.IContainerType} tracking</li>
 * <li>Supports type casting during element insertion</li>
 * <li>Implements custom equality based on GAMA semantics</li>
 * </ul>
 *
 * <pre>
 * // Creating a GamaList directly (usually via factory)
 * GamaList&lt;String&gt; names = new GamaList&lt;&gt;(10, Types.STRING);
 * names.add("Alice");
 * names.add("Bob");
 *
 * // Type-aware cloning with casting
 * IList&lt;String&gt; copy = names.listValue(scope, Types.STRING, true);
 * </pre>
 *
 * <h3>3. {@link gama.api.types.list.GamaListFactory} - Factory and Utilities</h3>
 * <p>
 * A comprehensive static factory providing multiple strategies for creating and managing {@link gama.api.types.list.IList}
 * instances. It handles type casting, wrapping, and conversion from various sources.
 * </p>
 *
 * <h4>Factory Method Categories:</h4>
 * <ul>
 * <li><b>Core Creation:</b> Create empty or pre-sized lists
 * <pre>
 * IList&lt;Integer&gt; list = GamaListFactory.create(Types.INT);
 * IList&lt;Double&gt; bigList = GamaListFactory.create(Types.FLOAT, 1000);
 * </pre>
 * </li>
 *
 * <li><b>Scope-Aware Creation:</b> Create lists with automatic type casting
 * <pre>
 * // Heterogeneous inputs are cast to the target type
 * IList&lt;Double&gt; values = GamaListFactory.create(scope, Types.FLOAT, 1, 2.5, "3.14");
 * // Result: [1.0, 2.5, 3.14]
 * </pre>
 * </li>
 *
 * <li><b>Zero-Copy Wrapping:</b> Wrap existing collections without copying
 * <pre>
 * List&lt;String&gt; javaList = Arrays.asList("a", "b", "c");
 * IList&lt;String&gt; wrapped = GamaListFactory.wrap(Types.STRING, javaList);
 * // Changes to wrapped affect javaList and vice versa
 * </pre>
 * </li>
 *
 * <li><b>Without-Casting Methods:</b> Skip type casting for performance
 * <pre>
 * // When types are guaranteed compatible, skip casting overhead
 * IList&lt;String&gt; fast = GamaListFactory.createWithoutCasting(Types.STRING, "x", "y", "z");
 * </pre>
 * </li>
 *
 * <li><b>Stream Integration:</b> Work with Java Streams API
 * <pre>
 * IList&lt;Integer&gt; range = IntStream.range(0, 10)
 *     .boxed()
 *     .collect(GamaListFactory.toGamaList());
 * </pre>
 * </li>
 *
 * <li><b>Conversion Methods:</b> Convert arbitrary objects to lists
 * <pre>
 * // Automatically handles containers, agents, primitives, etc.
 * Object anything = getSomeObject();
 * IList&lt;?&gt; converted = GamaListFactory.toList(scope, anything);
 * </pre>
 * </li>
 * </ul>
 *
 * <h3>4. Wrapper Implementations</h3>
 *
 * <h4>{@link gama.api.types.list.GamaListWrapper} - General List Wrapper</h4>
 * <p>
 * Wraps any Java {@link java.util.List} to provide {@link gama.api.types.list.IList} functionality. Uses Guava's
 * {@link com.google.common.collect.ForwardingList} for delegation.
 * </p>
 * <pre>
 * List&lt;Integer&gt; existingList = new LinkedList&lt;&gt;();
 * existingList.add(1);
 * existingList.add(2);
 *
 * IList&lt;Integer&gt; gamaView = new GamaListWrapper&lt;&gt;(existingList, Types.INT);
 * gamaView.add(3); // Modifies the underlying LinkedList
 * </pre>
 *
 * <h4>{@link gama.api.types.list.GamaListArrayWrapper} - Efficient Array Wrapper</h4>
 * <p>
 * Provides a read-optimized {@link gama.api.types.list.IList} view over a fixed-size array. Implements
 * {@link java.util.RandomAccess} for efficient indexed access. Most modification operations throw
 * {@link UnsupportedOperationException}.
 * </p>
 * <pre>
 * String[] array = {"alpha", "beta", "gamma"};
 * IList&lt;String&gt; arrayView = new GamaListArrayWrapper&lt;&gt;(array, Types.STRING);
 *
 * // Efficient random access
 * String second = arrayView.get(1); // "beta"
 *
 * // Modifications to array reflect in view
 * array[0] = "ALPHA";
 * System.out.println(arrayView.get(0)); // "ALPHA"
 * </pre>
 *
 * <h4>{@link gama.api.types.list.GamaListCollectionWrapper} - Non-List Collection Wrapper</h4>
 * <p>
 * Wraps arbitrary {@link java.util.Collection} instances (Sets, Queues, etc.) that are not Lists. Some operations
 * (especially index-based ones) may be inefficient or have limited functionality.
 * </p>
 * <pre>
 * Set&lt;String&gt; uniqueNames = new HashSet&lt;&gt;(Arrays.asList("Alice", "Bob", "Charlie"));
 * IList&lt;String&gt; listView = new GamaListCollectionWrapper&lt;&gt;(uniqueNames, Types.STRING);
 *
 * // Iteration works, but order may not be preserved (Set semantics)
 * for (String name : listView) {
 *     System.out.println(name);
 * }
 * </pre>
 *
 * <h4>{@link gama.api.types.list.GamaPairList} - Specialized Map Entry List</h4>
 * <p>
 * A specialized {@link gama.api.types.list.GamaList} that holds {@link java.util.Map.Entry} objects representing
 * key-value pairs. Used primarily for map iteration and conversion operations. Implements
 * {@link gama.api.types.map.IMap.IPairList}.
 * </p>
 * <pre>
 * IMap&lt;String, Integer&gt; ages = GamaMapFactory.create();
 * ages.put("Alice", 30);
 * ages.put("Bob", 25);
 *
 * // Get pairs as a list
 * IPairList&lt;String, Integer&gt; pairs = new GamaPairList&lt;&gt;(ages);
 * for (Map.Entry&lt;String, Integer&gt; entry : pairs) {
 *     System.out.println(entry.getKey() + ": " + entry.getValue());
 * }
 * </pre>
 *
 * <h2>Type System Integration</h2>
 * <p>
 * All list implementations maintain GAMA's type system through {@link gama.api.gaml.types.IContainerType}:
 * </p>
 * <ul>
 * <li><b>Content Type Tracking:</b> Each list knows the type of its elements</li>
 * <li><b>Automatic Casting:</b> Elements can be cast to the declared content type when
 * {@code FLAGS.CAST_CONTAINER_CONTENTS} is enabled</li>
 * <li><b>Type Safety:</b> Type mismatches are detected and handled according to GAMA semantics</li>
 * </ul>
 *
 * <pre>
 * // Type is enforced throughout the list lifecycle
 * IList&lt;Integer&gt; numbers = GamaListFactory.create(Types.INT);
 * numbers.getGamlType().getContentType(); // Returns Types.INT
 *
 * // Casting happens automatically in scope-aware operations
 * numbers.setValueAtIndex(scope, 0, "42"); // String "42" cast to Integer 42
 * </pre>
 *
 * <h2>Performance Considerations</h2>
 *
 * <h3>Choosing the Right Method</h3>
 * <ul>
 * <li><b>Wrapping vs. Copying:</b>
 * <ul>
 * <li>{@code wrap()} methods: Zero-copy, bidirectional changes, use when sharing data</li>
 * <li>{@code create()} methods: New independent instance, use for isolation</li>
 * </ul>
 * </li>
 *
 * <li><b>Casting Overhead:</b>
 * <ul>
 * <li>Scope-aware methods: Perform type casting based on {@code FLAGS.CAST_CONTAINER_CONTENTS}</li>
 * <li>{@code createWithoutCasting()}: Skip casting when type safety is guaranteed</li>
 * </ul>
 * </li>
 *
 * <li><b>Parallel Execution:</b>
 * <ul>
 * <li>{@link gama.api.types.list.GamaListFactory#create(gama.api.runtime.scope.IScope, gama.api.gaml.expressions.IExpression, Integer, boolean)}
 * supports parallel filling for large lists created from expressions</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <h2>Common Usage Patterns</h2>
 *
 * <h3>Creating Lists in Java Plugins</h3>
 * <pre>
 * // Empty list
 * IList&lt;IAgent&gt; agents = GamaListFactory.create(Types.AGENT);
 *
 * // From array
 * IList&lt;String&gt; names = GamaListFactory.createWithoutCasting(Types.STRING, 
 *     "Alice", "Bob", "Charlie");
 *
 * // From Java collection
 * List&lt;Double&gt; measurements = Arrays.asList(1.5, 2.3, 4.7);
 * IList&lt;Double&gt; gamaMeasurements = GamaListFactory.wrap(Types.FLOAT, measurements);
 * </pre>
 *
 * <h3>Type Conversion</h3>
 * <pre>
 * // Convert any object to a list
 * Object data = getDataFromSomewhere();
 * IList&lt;?&gt; list = GamaListFactory.toList(scope, data);
 *
 * // List to matrix
 * IList&lt;Integer&gt; numbers = GamaListFactory.create(scope, Types.INT, 1, 2, 3, 4);
 * IMatrix&lt;Integer&gt; matrix = numbers.matrixValue(scope, Types.INT, 
 *     GamaPointFactory.create(2, 2), false);
 * </pre>
 *
 * <h3>Working with Streams</h3>
 * <pre>
 * // Java Stream to IList
 * IList&lt;String&gt; filtered = Stream.of("apple", "banana", "cherry")
 *     .filter(s -&gt; s.startsWith("a"))
 *     .collect(GamaListFactory.toGamaList());
 *
 * // IList to Stream
 * IList&lt;Integer&gt; numbers = GamaListFactory.create(Types.INT);
 * long sum = numbers.stream(scope)
 *     .mapToLong(Integer::longValue)
 *     .sum();
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * List implementations in this package are <b>not synchronized</b>. For concurrent access:
 * </p>
 * <pre>
 * // Option 1: External synchronization
 * IList&lt;String&gt; list = GamaListFactory.create(Types.STRING);
 * synchronized(list) {
 *     list.add("thread-safe");
 * }
 *
 * // Option 2: Use Java's synchronized wrapper (loses some IList features)
 * List&lt;String&gt; syncList = Collections.synchronizedList(list);
 * </pre>
 *
 * <h2>Integration with GAML</h2>
 * <p>
 * In GAML models, lists are created using the bracket syntax or built-in operators:
 * </p>
 * <pre>
 * // GAML code
 * list&lt;int&gt; numbers &lt;- [1, 2, 3, 4, 5];
 * list&lt;string&gt; names &lt;- ["Alice", "Bob"] + ["Charlie"];
 * list&lt;agent&gt; nearby &lt;- agents at_distance 10.0;
 * </pre>
 * <p>
 * Behind the scenes, these operations use {@link gama.api.types.list.GamaListFactory} to create typed
 * {@link gama.api.types.list.IList} instances.
 * </p>
 *
 * <h2>See Also</h2>
 * <ul>
 * <li>{@link gama.api.types.map} - GAMA map types and implementations</li>
 * <li>{@link gama.api.types.matrix} - GAMA matrix types and implementations</li>
 * <li>{@link gama.api.types.misc.IContainer} - Base container interface</li>
 * <li>{@link gama.api.gaml.types.IContainerType} - GAMA container type system</li>
 * <li>{@link gama.api.gaml.types.Types} - GAMA type constants and utilities</li>
 * </ul>
 *
 * @since GAMA 1.0
 * @author drogoul
 */
package gama.api.types.list;
