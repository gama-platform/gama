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
 * The misc package provides fundamental interfaces for GAMA values and containers.
 * 
 * <p>
 * This package contains the core abstractions that form the foundation of GAMA's type system. All GAMA types
 * (primitives, collections, agents, geometries, etc.) implement one or more of these interfaces.
 * </p>
 * 
 * <h2>Main Components</h2>
 * <ul>
 * <li><strong>{@link gama.api.types.misc.IValue}</strong> - The root interface for all GAMA values. Defines the basic
 * contract for objects that can be used in GAML: having a type, being serializable to GAML syntax, being copyable, and
 * providing string/numeric representations.</li>
 * 
 * <li><strong>{@link gama.api.types.misc.IContainer}</strong> - The fundamental interface for all collection-like
 * structures in GAMA. Provides a unified API for lists, maps, matrices, graphs, populations, and files. Supports
 * iteration, containment checking, conversion between container types, and common collection operations.</li>
 * </ul>
 * 
 * <h2>IValue - The Value Interface</h2>
 * <p>
 * {@link gama.api.types.misc.IValue} is the base interface for all objects that can be manipulated in GAML. It
 * requires implementations to:
 * </p>
 * <ul>
 * <li>Provide a GAMA type through {@link gama.api.gaml.types.ITyped#getGamlType()}</li>
 * <li>Be serializable to GAML code via {@link gama.api.utils.interfaces.IGamlable#toGaml(boolean)}</li>
 * <li>Be convertible to JSON via {@link gama.api.utils.json.IJsonable#serializeToJson(gama.api.utils.json.IJson)}</li>
 * <li>Be copyable via {@link #copy(gama.api.runtime.scope.IScope)}</li>
 * <li>Provide string and numeric representations</li>
 * </ul>
 * 
 * <h2>IContainer - The Container Interface</h2>
 * <p>
 * {@link gama.api.types.misc.IContainer} unifies all collection types under a common interface. It provides:
 * </p>
 * <ul>
 * <li><strong>Type safety:</strong> Containers are parameterized with key and value types</li>
 * <li><strong>Conversions:</strong> Any container can be converted to a list, map, or matrix</li>
 * <li><strong>Common operations:</strong> length(), isEmpty(), contains(), first(), last(), reverse()</li>
 * <li><strong>Iteration:</strong> All containers are iterable and can be streamed</li>
 * <li><strong>Modification:</strong> Nested interfaces for addressable and modifiable containers</li>
 * </ul>
 * 
 * <h2>Container Specializations</h2>
 * <p>
 * IContainer defines nested interfaces for specific capabilities:
 * </p>
 * <ul>
 * <li><strong>{@link gama.api.types.misc.IContainer.ToGet}</strong> - For containers supporting element retrieval by
 * key/index</li>
 * <li><strong>{@link gama.api.types.misc.IContainer.ToSet}</strong> - For containers supporting element
 * addition/modification/removal</li>
 * <li><strong>{@link gama.api.types.misc.IContainer.Addressable}</strong> - Combines IContainer with ToGet for
 * readable containers</li>
 * <li><strong>{@link gama.api.types.misc.IContainer.Modifiable}</strong> - Combines IContainer with ToSet for writable
 * containers</li>
 * </ul>
 * 
 * <h2>Container Types</h2>
 * <p>
 * The following GAMA types implement IContainer:
 * </p>
 * <ul>
 * <li><strong>Lists</strong> - Ordered sequences (key type: int, value type: element type)</li>
 * <li><strong>Maps</strong> - Key-value associations (key type: key type, value type: value type)</li>
 * <li><strong>Matrices</strong> - 2D grids (key type: point, value type: element type)</li>
 * <li><strong>Graphs</strong> - Networks of vertices and edges</li>
 * <li><strong>Populations</strong> - Collections of agents (species)</li>
 * <li><strong>Files</strong> - External data sources with container contents</li>
 * <li><strong>Pairs</strong> - Simple key-value pairs</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>
 * // Working with IValue
 * IValue value = ...;
 * IType&lt;?&gt; type = value.getGamlType();
 * String gamlCode = value.toGaml(false);
 * IValue copy = value.copy(scope);
 * 
 * // Working with IContainer
 * IContainer&lt;?, ?&gt; container = ...;
 * int size = container.length(scope);
 * boolean empty = container.isEmpty(scope);
 * Object first = container.firstValue(scope);
 * IList&lt;?&gt; asList = container.listValue(scope, Types.NO_TYPE, false);
 * 
 * // Streaming containers
 * container.stream(scope)
 *          .filter(element -&gt; someCondition(element))
 *          .forEach(element -&gt; process(element));
 * </pre>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 * <li><strong>Uniformity:</strong> All containers share a common interface regardless of implementation</li>
 * <li><strong>Conversions:</strong> Containers can be freely converted between types</li>
 * <li><strong>Immutability options:</strong> Operations like copy() and reverse() return new instances</li>
 * <li><strong>Scope-aware:</strong> All operations take an IScope for accessing simulation context</li>
 * </ul>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
package gama.api.types.misc;
