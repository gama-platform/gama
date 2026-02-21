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
 * The scope package provides runtime scope management for GAML execution.
 * 
 * <p>This package contains interfaces and implementations for managing execution scopes,
 * which provide the runtime context for evaluating expressions and executing statements.</p>
 * 
 * <h2>Core Interfaces</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.runtime.scope.IScope} - Main scope interface providing runtime context</li>
 *   <li>{@link gama.api.runtime.scope.InScope} - Functional interface for scope-based operations</li>
 * </ul>
 * 
 * <h2>Scope Functionality</h2>
 * 
 * <p>Scopes provide access to:</p>
 * <ul>
 *   <li><strong>Current Agent:</strong> The agent in whose context code is executing</li>
 *   <li><strong>Current Simulation:</strong> The simulation instance</li>
 *   <li><strong>Variables:</strong> Local and global variable values</li>
 *   <li><strong>Random Generator:</strong> Random number generation</li>
 *   <li><strong>Clock:</strong> Simulation time</li>
 *   <li><strong>Error Handling:</strong> Exception and interruption management</li>
 *   <li><strong>Execution Stack:</strong> Call stack for debugging</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * public Object execute(IScope scope) {
 *     // Access current agent
 *     IAgent agent = scope.getAgent();
 *     
 *     // Access simulation
 *     ISimulationAgent simulation = scope.getSimulation();
 *     
 *     // Get variable value
 *     Object value = scope.getVarValue("my_variable");
 *     
 *     // Use random generator
 *     double random = scope.getRandom().next();
 *     
 *     return result;
 * }
 * }</pre>
 * 
 * <h2>Scope Lifecycle</h2>
 * 
 * <p>Scopes are created for each execution context and should be used within their
 * intended context. They are not thread-safe and should not be shared across threads.</p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.runtime.scope.IScope
 * @see gama.api.runtime.IExecutionContext
 */
package gama.api.runtime.scope;
