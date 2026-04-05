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
 * The statements package provides the statement system for GAML.
 * 
 * <p>This package contains interfaces and implementations for GAML statements, which are
 * executable constructs that perform actions and control program flow.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.gaml.statements.IStatement} - Base interface for all statements</li>
 *   <li>{@link gama.api.gaml.statements.IExecutable} - Interface for executable statements</li>
 * </ul>
 * 
 * <h2>Statement Categories</h2>
 * 
 * <h3>Control Structures:</h3>
 * <ul>
 *   <li><strong>if/else:</strong> Conditional execution</li>
 *   <li><strong>loop:</strong> Iteration over collections or ranges</li>
 *   <li><strong>switch/match:</strong> Multi-way branching</li>
 *   <li><strong>return:</strong> Early exit from actions</li>
 * </ul>
 * 
 * <h3>Agent Operations:</h3>
 * <ul>
 *   <li><strong>create:</strong> Agent instantiation</li>
 *   <li><strong>ask:</strong> Executing code in other agents' context</li>
 *   <li><strong>capture/release:</strong> Agent containment management</li>
 *   <li><strong>migrate:</strong> Agent movement between populations</li>
 * </ul>
 * 
 * <h3>Data Operations:</h3>
 * <ul>
 *   <li><strong>save:</strong> Data persistence</li>
 *   <li><strong>write:</strong> Console output</li>
 *   <li><strong>put/add/remove:</strong> Container manipulation</li>
 * </ul>
 * 
 * <h3>Simulation Control:</h3>
 * <ul>
 *   <li><strong>pause:</strong> Simulation suspension</li>
 *   <li><strong>halt:</strong> Simulation termination</li>
 * </ul>
 * 
 * <h2>Statement Execution</h2>
 * 
 * <p>Statements are executed in a runtime scope:</p>
 * <pre>{@code
 * public Object executeOn(IScope scope) throws GamaRuntimeException {
 *     // Statement logic here
 *     return null;
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.gaml.statements.IStatement
 * @see gama.api.runtime.scope.IScope
 */
package gama.api.gaml.statements;
