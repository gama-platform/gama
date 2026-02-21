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
 * The types package provides the GAML type system implementations for all data types available in the language.
 * 
 * <p>This package contains the concrete implementations of GAML's rich type system, including primitive types,
 * container types, spatial types, temporal types, and specialized types for colors, fonts, files, and graphs.</p>
 * 
 * <h2>Type Categories</h2>
 * 
 * <h3>Primitive Types:</h3>
 * <ul>
 *   <li>Integer, Float, Boolean, String types</li>
 *   <li>Handled by {@link gama.api.types.misc} package</li>
 * </ul>
 * 
 * <h3>Container Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.list} - List/sequence types with ordering</li>
 *   <li>{@link gama.api.types.map} - Map/dictionary types for key-value pairs</li>
 *   <li>{@link gama.api.types.matrix} - Matrix types for 2D grid data</li>
 *   <li>{@link gama.api.types.pair} - Pair types for two-element tuples</li>
 * </ul>
 * 
 * <h3>Spatial Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.geometry} - Geometric shapes and spatial operations</li>
 *   <li>{@link gama.api.types.topology} - Spatial topology and neighbor relationships</li>
 * </ul>
 * 
 * <h3>Temporal Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.date} - Date and time types</li>
 * </ul>
 * 
 * <h3>Graph Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.graph} - Graph structures and network types</li>
 * </ul>
 * 
 * <h3>Visual Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.color} - Color representations and manipulations</li>
 *   <li>{@link gama.api.types.font} - Font types for text rendering</li>
 * </ul>
 * 
 * <h3>File Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.file} - File types for various data formats</li>
 * </ul>
 * 
 * <h3>Communication Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.message} - Message types for agent communication</li>
 * </ul>
 * 
 * <h3>Miscellaneous Types:</h3>
 * <ul>
 *   <li>{@link gama.api.types.misc} - Various specialized types and utilities</li>
 * </ul>
 * 
 * <h2>Type System Architecture</h2>
 * 
 * <p>All GAML types implement the {@link gama.api.gaml.types.IType} interface, which provides:</p>
 * <ul>
 *   <li>Type metadata (name, ID, parent type)</li>
 *   <li>Value casting and conversion</li>
 *   <li>Default value provision</li>
 *   <li>Type compatibility checking</li>
 *   <li>Serialization support</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Working with Types:</h3>
 * <pre>{@code
 * // Get a type by name
 * IType<?> listType = Types.get("list");
 * 
 * // Check type compatibility
 * boolean compatible = listType.isAssignableFrom(otherType);
 * 
 * // Cast values
 * Object castedValue = listType.cast(scope, originalValue, null, false);
 * }</pre>
 * 
 * <h3>Container Types:</h3>
 * <pre>{@code
 * // Create a list
 * IList<Integer> list = GamaListFactory.create(Types.INT);
 * list.add(1);
 * list.add(2);
 * 
 * // Create a map
 * IMap<String, Object> map = GamaMapFactory.create();
 * map.put("key", value);
 * }</pre>
 * 
 * <h3>Spatial Types:</h3>
 * <pre>{@code
 * // Create geometry
 * IShape circle = GamaShapeFactory.buildCircle(10.0, new GamaPoint(0, 0));
 * 
 * // Spatial operations
 * double area = circle.getArea();
 * IShape intersection = circle.intersection(otherShape);
 * }</pre>
 * 
 * <h2>Type Registration</h2>
 * 
 * <p>New types can be registered with the platform through the GAML additions mechanism.
 * Custom types should extend appropriate base classes and use the @type annotation.</p>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <p>Type operations are performance-critical. The type system includes:</p>
 * <ul>
 *   <li>Type caching to avoid repeated lookups</li>
 *   <li>Efficient type compatibility checking</li>
 *   <li>Lazy initialization of complex types</li>
 *   <li>Optimized container implementations</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.types.IType
 * @see gama.api.gaml.types.Types
 */
package gama.api.types;
