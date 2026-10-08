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
 * The types package in gaml provides the type system infrastructure for GAML.
 * 
 * <p>This package contains the core type system interfaces and base implementations that define
 * how types work in GAML, including type hierarchies, casting, and type operations.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.gaml.types.IType} - Base interface for all GAML types</li>
 *   <li>{@link gama.api.gaml.types.IContainerType} - Interface for container types</li>
 *   <li>{@link gama.api.gaml.types.Types} - Registry and factory for types</li>
 * </ul>
 * 
 * <h2>Type Features</h2>
 * 
 * <p>Types provide:</p>
 * <ul>
 *   <li>Type metadata (ID, name, parent type)</li>
 *   <li>Value casting and conversion</li>
 *   <li>Default value provision</li>
 *   <li>Type compatibility checking</li>
 *   <li>Serialization/deserialization</li>
 * </ul>
 * 
 * <h2>Type Hierarchy</h2>
 * 
 * <p>Types are organized in an inheritance hierarchy with dynamic type checking
 * and coercion capabilities.</p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.types.IType
 * @see gama.api.gaml.types.Types
 * @see gama.api.types
 */
package gama.api.gaml.types;
