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
 * The gaml package provides the core abstractions and implementations of the GAML modeling language.
 * 
 * <p>This package contains the fundamental language constructs that make up GAML, including
 * expressions, statements, types, variables, and the main GAML facade.</p>
 * 
 * <h2>Main Entry Point</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.gaml.GAML} - Main facade for GAML language operations and utilities</li>
 * </ul>
 * 
 * <h2>Sub-packages</h2>
 * 
 * <h3>Language Elements:</h3>
 * <ul>
 *   <li>{@link gama.api.gaml.expressions} - Expression system (literals, operators, variables)</li>
 *   <li>{@link gama.api.gaml.statements} - Statement system (loops, conditionals, actions)</li>
 *   <li>{@link gama.api.gaml.symbols} - Symbol system (species, experiments, global)</li>
 *   <li>{@link gama.api.gaml.variables} - Variable system (attributes, parameters)</li>
 * </ul>
 * 
 * <h3>Type System:</h3>
 * <ul>
 *   <li>{@link gama.api.gaml.types} - GAML type system (primitives, containers, spatial types)</li>
 * </ul>
 * 
 * <h3>Species System:</h3>
 * <ul>
 *   <li>{@link gama.api.gaml.species} - Species definitions and management</li>
 * </ul>
 * 
 * <h3>Constants:</h3>
 * <ul>
 *   <li>{@link gama.api.gaml.constants} - GAML constants and predefined values</li>
 * </ul>
 * 
 * <h2>Core Concepts</h2>
 * 
 * <h3>Expressions:</h3>
 * <p>Expressions are evaluable constructs that produce values:</p>
 * <ul>
 *   <li>Literals (numbers, strings, booleans)</li>
 *   <li>Variable references</li>
 *   <li>Operator applications</li>
 *   <li>Function calls</li>
 * </ul>
 * 
 * <h3>Statements:</h3>
 * <p>Statements are executable constructs that perform actions:</p>
 * <ul>
 *   <li>Control structures (if, loop, switch)</li>
 *   <li>Agent creation and manipulation</li>
 *   <li>Communication and interaction</li>
 *   <li>Data manipulation</li>
 * </ul>
 * 
 * <h3>Symbols:</h3>
 * <p>Symbols are structural language elements:</p>
 * <ul>
 *   <li>Model definition</li>
 *   <li>Species definition</li>
 *   <li>Experiment definition</li>
 *   <li>Global section</li>
 * </ul>
 * 
 * <h3>Types:</h3>
 * <p>GAML has a rich type system including:</p>
 * <ul>
 *   <li>Primitives: int, float, bool, string</li>
 *   <li>Containers: list, map, matrix</li>
 *   <li>Spatial: point, geometry, path</li>
 *   <li>Temporal: date</li>
 *   <li>Special: agent, species, file, graph</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Using the GAML Facade:</h3>
 * <pre>{@code
 * // Create an expression
 * IExpression expr = GAML.compileExpression("2 + 2", scope);
 * Object result = expr.evaluate(scope);
 * 
 * // Access type system
 * IType<Integer> intType = GAML.getType("int");
 * }</pre>
 * 
 * <h3>Working with Types:</h3>
 * <pre>{@code
 * IType<?> type = expr.getGamlType();
 * String typeName = type.getName();
 * boolean isNumeric = type.isNumber();
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.GAML
 * @see gama.api.gaml.expressions
 * @see gama.api.gaml.types
 * @see gama.api.gaml.statements
 */
package gama.api.gaml;
