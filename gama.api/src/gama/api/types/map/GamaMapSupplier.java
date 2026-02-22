/*******************************************************************************************************
 *
 * GamaMapSupplier.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.map;

import java.util.function.Supplier;

import gama.api.gaml.types.IType;

/**
 * A {@link Supplier} implementation for lazy creation of {@link IMap} instances.
 * 
 * <p>
 * {@code GamaMapSupplier} implements the Java {@link Supplier} interface to provide on-demand creation of GAMA maps
 * with predefined key and value types. This is particularly useful for:
 * </p>
 * <ul>
 * <li>Stream collectors and functional programming patterns</li>
 * <li>Lazy initialization scenarios</li>
 * <li>Factory method references</li>
 * <li>Frameworks requiring Supplier instances</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li><b>Lazy Creation</b>: Maps are created only when {@link #get()} is called</li>
 * <li><b>Type-Parameterized</b>: Stores key and value types for consistent map creation</li>
 * <li><b>Reusable</b>: Can create multiple maps with the same types</li>
 * <li><b>Lightweight</b>: Only stores two type references</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Usage</h3>
 * 
 * <pre>
 * // Create a supplier for String -> Integer maps
 * Supplier&lt;IMap&gt; mapSupplier = new GamaMapSupplier(Types.STRING, Types.INT);
 * 
 * // Get a new map when needed
 * IMap&lt;String, Integer&gt; map1 = mapSupplier.get();
 * IMap&lt;String, Integer&gt; map2 = mapSupplier.get(); // Different instance
 * 
 * assert map1 != map2; // Each call creates a new map
 * </pre>
 * 
 * <h3>With Stream Collectors</h3>
 * 
 * <pre>
 * // Use as a supplier in custom collectors
 * Collector&lt;Entry&lt;String, Integer&gt;, ?, IMap&lt;String, Integer&gt;&gt; collector = 
 *     Collector.of(
 *         new GamaMapSupplier(Types.STRING, Types.INT),  // Supplier
 *         (map, entry) -&gt; map.put(entry.getKey(), entry.getValue()),  // Accumulator
 *         (map1, map2) -&gt; { map1.putAll(map2); return map1; }  // Combiner
 *     );
 * 
 * IMap&lt;String, Integer&gt; result = stream.collect(collector);
 * </pre>
 * 
 * <h3>Lazy Initialization</h3>
 * 
 * <pre>
 * public class DataProcessor {
 *     private final Supplier&lt;IMap&gt; cacheSupplier;
 *     private IMap cache;
 *     
 *     public DataProcessor() {
 *         // Define supplier at construction
 *         this.cacheSupplier = new GamaMapSupplier(Types.STRING, Types.OBJECT);
 *     }
 *     
 *     public IMap getCache() {
 *         // Create cache only when first accessed
 *         if (cache == null) {
 *             cache = cacheSupplier.get();
 *         }
 *         return cache;
 *     }
 * }
 * </pre>
 * 
 * <h3>Method References</h3>
 * 
 * <pre>
 * // Use supplier as a method reference
 * GamaMapSupplier supplier = new GamaMapSupplier(Types.INT, Types.STRING);
 * List&lt;IMap&gt; maps = Stream.generate(supplier::get)
 *                          .limit(10)
 *                          .collect(Collectors.toList());
 * // Creates 10 independent maps
 * </pre>
 * 
 * <h2>Created Maps</h2>
 * <p>
 * Each call to {@link #get()} creates a new {@link IMap} via {@link GamaMapFactory#create(IType, IType)}:
 * </p>
 * <ul>
 * <li>Uses {@link GamaMap} implementation (ordered, LinkedHashMap-based)</li>
 * <li>Empty map with default initial capacity</li>
 * <li>Parameterized with the supplier's key and value types</li>
 * <li>Independent instance (no sharing between calls)</li>
 * </ul>
 * 
 * <h2>Type Handling</h2>
 * <p>
 * The supplier stores {@link IType} references for consistent type parameterization:
 * </p>
 * 
 * <pre>
 * GamaMapSupplier supplier = new GamaMapSupplier(Types.STRING, Types.FLOAT);
 * 
 * IMap map = supplier.get();
 * assert map.getGamlType().getKeyType() == Types.STRING;
 * assert map.getGamlType().getContentType() == Types.FLOAT;
 * </pre>
 * 
 * <h2>Thread Safety</h2>
 * <p>
 * The supplier itself is thread-safe (immutable after construction). Multiple threads can call {@link #get()}
 * concurrently, and each will receive an independent map instance. However, the created maps are not thread-safe
 * unless wrapped appropriately.
 * </p>
 * 
 * <h2>Performance Characteristics</h2>
 * <ul>
 * <li><b>Construction</b>: O(1) - just stores two references</li>
 * <li><b>get() call</b>: O(1) - delegates to {@link GamaMapFactory#create}</li>
 * <li><b>Memory</b>: Minimal - two IType references only</li>
 * <li><b>No caching</b>: Each call creates a new instance</li>
 * </ul>
 * 
 * <h2>Comparison with Direct Creation</h2>
 * <table border="1">
 * <tr>
 * <th>Aspect</th>
 * <th>GamaMapSupplier</th>
 * <th>Direct GamaMapFactory.create()</th>
 * </tr>
 * <tr>
 * <td>Creation Timing</td>
 * <td>Lazy (on get())</td>
 * <td>Immediate</td>
 * </tr>
 * <tr>
 * <td>Reusability</td>
 * <td>Can create multiple maps</td>
 * <td>Single map created</td>
 * </tr>
 * <tr>
 * <td>Functional API</td>
 * <td>Yes (implements Supplier)</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>Use Case</td>
 * <td>Streams, lazy init, factory refs</td>
 * <td>Direct instantiation</td>
 * </tr>
 * </table>
 * 
 * <h2>Limitations</h2>
 * <ul>
 * <li>Cannot specify initial capacity (uses default)</li>
 * <li>Cannot create with initial data</li>
 * <li>No customization of map implementation</li>
 * <li>No scope parameter (no type casting on creation)</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 * <li>Reuse supplier instances when creating multiple maps of the same type</li>
 * <li>Store as {@code Supplier<IMap>} for interface-based programming</li>
 * <li>Consider caching the result if only one map is needed</li>
 * <li>Use for factory patterns and functional programming idioms</li>
 * </ul>
 * 
 * @see GamaMapFactory#create(IType, IType)
 * @see Supplier
 * @see IMap
 * 
 * @author drogoul
 */
public class GamaMapSupplier implements Supplier<IMap> {

	/** The k. */
	IType k;

	/** The c. */
	IType c;

	/**
	 * Instantiates a new gama map supplier.
	 *
	 * @param key
	 *            the key
	 * @param contents
	 *            the contents
	 * @param internalGamaMapBuilder
	 *            TODO
	 */
	public GamaMapSupplier(final IType key, final IType contents) {
		k = key;
		c = contents;
	}

	@Override
	public IMap get() {
		return GamaMapFactory.create(k, c);
	}
}