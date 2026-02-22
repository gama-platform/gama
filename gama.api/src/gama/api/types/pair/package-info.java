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
 * Provides interfaces and factory classes for creating and managing pair (tuple) data structures in GAMA.
 * 
 * <p>
 * This package contains the core abstractions for pairs, which are two-element containers consisting of a key and a
 * value. Pairs are used throughout GAMA to represent simple associations, coordinates, key-value relationships, and
 * other two-element data structures.
 * </p>
 * 
 * <h2>Main Components</h2>
 * <ul>
 * <li>{@link gama.api.types.pair.IPair} - The core interface representing a pair of values</li>
 * <li>{@link gama.api.types.pair.IPairFactory} - Factory interface for creating pair instances</li>
 * <li>{@link gama.api.types.pair.GamaPairFactory} - Static factory providing convenient methods for pair creation</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>
 * Pairs can be created using the {@link gama.api.types.pair.GamaPairFactory}:
 * </p>
 * 
 * <pre>
 * // Create a simple pair
 * IPair&lt;String, Integer&gt; pair = GamaPairFactory.createWith("key", 42);
 * 
 * // Create a typed pair
 * IPair pair = GamaPairFactory.createWith("name", "value", Types.STRING, Types.STRING);
 * 
 * // Convert an object to a pair
 * IPair pair = GamaPairFactory.castToPair(scope, someObject);
 * </pre>
 * 
 * <h2>Key Features</h2>
 * <ul>
 * <li>Type-safe pair creation with generic support</li>
 * <li>Multiple access patterns (first/last, key/value, Map.Entry interface)</li>
 * <li>Integration with GAMA's type system through IContainer</li>
 * <li>Conversion from various object types (lists, maps, points, etc.)</li>
 * <li>Support for null values and default pairs</li>
 * </ul>
 * 
 * @since GAMA 1.0
 * @author drogoul
 */
package gama.api.types.pair;
