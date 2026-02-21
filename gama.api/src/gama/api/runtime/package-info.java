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
 * The runtime package provides the execution environment and infrastructure for running GAMA simulations.
 * 
 * <p>This package contains the core runtime services that manage simulation execution, including
 * scope management, agent execution, parallel processing, and execution context handling.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Execution Context:</h3>
 * <ul>
 *   <li>{@link gama.api.runtime.IExecutionContext} - Execution context for operations</li>
 *   <li>{@link gama.api.runtime.scope.IScope} - Runtime scope providing access to agents and variables</li>
 * </ul>
 * 
 * <h3>Execution Services:</h3>
 * <ul>
 *   <li>{@link gama.api.runtime.GamaExecutorService} - Thread pool and task execution management</li>
 *   <li>{@link gama.api.runtime.ISimulationRunner} - Interface for running simulations</li>
 *   <li>{@link gama.api.runtime.SimulationRunner} - Standard simulation runner implementation</li>
 * </ul>
 * 
 * <h3>Parallel Execution:</h3>
 * <ul>
 *   <li>{@link gama.api.runtime.ParallelAgentRunner} - Parallel execution of agent operations</li>
 *   <li>{@link gama.api.runtime.ParallelAgentExecuter} - Executor for parallel agent processing</li>
 *   <li>{@link gama.api.runtime.ParallelAgentStepper} - Parallel stepping of agents</li>
 *   <li>{@link gama.api.runtime.AgentSpliterator} - Spliterator for parallel agent iteration</li>
 * </ul>
 * 
 * <h3>Synchronization:</h3>
 * <ul>
 *   <li>{@link gama.api.runtime.GeneralSynchronizer} - Synchronization utilities for concurrent access</li>
 * </ul>
 * 
 * <h3>Support Interfaces:</h3>
 * <ul>
 *   <li>{@link gama.api.runtime.IExecutable} - Interface for executable operations</li>
 *   <li>{@link gama.api.runtime.IStepable} - Interface for entities that can be stepped</li>
 *   <li>{@link gama.api.runtime.IRuntimeExceptionHandler} - Runtime exception handling</li>
 *   <li>{@link gama.api.runtime.IWorkspaceManager} - Workspace management interface</li>
 * </ul>
 * 
 * <h3>System Information:</h3>
 * <ul>
 *   <li>{@link gama.api.runtime.SystemInfo} - System information and resource monitoring</li>
 * </ul>
 * 
 * <h2>Sub-packages</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.runtime.scope} - Scope management and runtime context</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Accessing Runtime Scope:</h3>
 * <pre>{@code
 * IScope scope = GAMA.getRuntimeScope();
 * IAgent agent = scope.getAgent();
 * ISimulationAgent simulation = scope.getSimulation();
 * }</pre>
 * 
 * <h3>Parallel Agent Execution:</h3>
 * <pre>{@code
 * ParallelAgentRunner.execute(scope, agents, agent -> {
 *     agent.performAction(scope);
 * });
 * }</pre>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>The runtime package includes several thread-safe components designed for concurrent
 * execution. However, agent state access should generally occur within the appropriate
 * scope to ensure proper synchronization.</p>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.runtime.scope.IScope
 * @see gama.api.runtime.GamaExecutorService
 * @see gama.api.runtime.IExecutionContext
 */
package gama.api.runtime;
