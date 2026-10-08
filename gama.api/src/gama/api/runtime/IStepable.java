/*******************************************************************************************************
 *
 * IStepable.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * Represents objects that can be stepped through time in GAMA simulations.
 * 
 * <p>
 * IStepable defines the lifecycle interface for objects that participate in the simulation's discrete time steps. This
 * includes agents, simulations, experiments, and other time-aware components that need initialization and repeated
 * stepping.
 * </p>
 * 
 * <p>
 * The typical lifecycle of a stepable object:
 * </p>
 * <ol>
 * <li><b>Initialization:</b> {@link #init(IScope)} is called once to set up the object's initial state</li>
 * <li><b>Stepping:</b> {@link #step(IScope)} is called repeatedly, once per simulation cycle</li>
 * <li><b>Termination:</b> When the object is no longer needed, it's removed from the scheduler</li>
 * </ol>
 * 
 * <p>
 * Common implementations:
 * </p>
 * <ul>
 * <li><b>Agents:</b> Execute their behaviors (reflexes, init, etc.) during each step</li>
 * <li><b>Simulations:</b> Coordinate the stepping of all their agents</li>
 * <li><b>Experiments:</b> Manage simulation lifecycles and data collection</li>
 * <li><b>Schedulers:</b> Control the execution order of multiple stepable objects</li>
 * </ul>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * IStepable agent = ...;
 * IScope scope = ...;
 * 
 * // Initialize the agent
 * if (agent.init(scope)) {
 * 	// Step the agent through time
 * 	while (!agent.dead()) {
 * 		boolean success = agent.step(scope);
 * 		if (!success)
 * 			break; // Handle error
 * 	}
 * }
 * </pre>
 * 
 * @see ISimulationRunner
 * @see GamaExecutorService
 * 
 * @author drogoul
 * @since 13 dec. 2011
 */
public interface IStepable {

	/**
	 * Initializes this stepable object with a valid execution scope.
	 * 
	 * <p>
	 * This method is called once before the first step to set up the stepable's initial state. For agents, this
	 * typically involves:
	 * </p>
	 * <ul>
	 * <li>Initializing attributes to their default or specified values</li>
	 * <li>Executing the GAML 'init' block if present</li>
	 * <li>Setting up spatial location and relationships</li>
	 * <li>Registering with appropriate schedulers or populations</li>
	 * </ul>
	 * 
	 * @param scope
	 *            the scope providing execution context, including access to the simulation, random generators, and
	 *            variables
	 * @return true if initialization completed successfully, false if an error occurred that should prevent further
	 *         execution
	 * @throws GamaRuntimeException
	 *             if a runtime error occurs that should be reported to the user
	 */
	boolean init(IScope scope) throws GamaRuntimeException;

	/**
	 * Executes one time step for this stepable object.
	 * 
	 * <p>
	 * This method is called repeatedly by the scheduler or simulation engine to advance the stepable through
	 * simulation time. The exact behavior depends on the implementation:
	 * </p>
	 * <ul>
	 * <li><b>Agents:</b> Execute their reflexes and other scheduled behaviors</li>
	 * <li><b>Simulations:</b> Step all their agents and update the clock</li>
	 * <li><b>Experiments:</b> Step all simulations and collect results</li>
	 * </ul>
	 * 
	 * <p>
	 * Information about the current cycle, time, and execution context can be retrieved from the scope.
	 * </p>
	 * 
	 * @param scope
	 *            the scope providing execution context for this step
	 * @return true if the step completed successfully, false if an error occurred or execution should stop
	 * @throws GamaRuntimeException
	 *             if a runtime error occurs that should be reported to the user
	 */
	boolean step(IScope scope) throws GamaRuntimeException;

}
