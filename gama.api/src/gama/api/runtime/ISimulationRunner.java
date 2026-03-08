/*******************************************************************************************************
 *
 * ISimulationRunner.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import java.util.Set;

import gama.api.kernel.simulation.ISimulationAgent;

/**
 * Manages the concurrent execution of multiple simulation agents within an experiment.
 * 
 * <p>
 * ISimulationRunner coordinates the parallel or sequential execution of simulation agents in experiments that run
 * multiple simulations simultaneously. It handles thread management, synchronization, and lifecycle for simulation
 * threads.
 * </p>
 * 
 * <p>
 * Key responsibilities:
 * </p>
 * <ul>
 * <li>Register and unregister simulation agents for execution</li>
 * <li>Synchronize simulation steps across multiple concurrent simulations</li>
 * <li>Manage dedicated threads for each simulation agent</li>
 * <li>Control concurrency level based on experiment configuration</li>
 * <li>Coordinate step-wise execution across all active simulations</li>
 * </ul>
 * 
 * <p>
 * Usage pattern (typically internal to GAMA):
 * </p>
 * 
 * <pre>
 * ISimulationRunner runner = SimulationRunner.of(population);
 * 
 * // Add simulations
 * runner.add(simulation1);
 * runner.add(simulation2);
 * 
 * // Execute one step for all simulations
 * runner.step(); // Blocks until all simulations complete their step
 * 
 * // Remove a simulation
 * runner.remove(simulation1);
 * 
 * // Clean up
 * runner.dispose();
 * </pre>
 * 
 * @see SimulationRunner
 * @see ISimulationAgent
 * @see GeneralSynchronizer
 */
public interface ISimulationRunner {

	/**
	 * Removes a simulation agent from the runner, stopping its dedicated thread.
	 * 
	 * <p>
	 * This method unregisters the simulation from concurrent execution. The simulation's thread will complete its
	 * current step (if any) and then terminate.
	 * </p>
	 * 
	 * @param agent
	 *            the simulation agent to remove from concurrent execution
	 */
	void remove(ISimulationAgent agent);

	/**
	 * Adds a simulation agent to the runner, creating and starting a dedicated thread for it.
	 * 
	 * <p>
	 * This method registers the simulation for concurrent execution and starts a new thread that will repeatedly
	 * execute the simulation's {@link IStepable#step(gama.api.runtime.scope.IScope)} method until the simulation dies
	 * or is removed.
	 * </p>
	 * 
	 * @param agent
	 *            the simulation agent to add to concurrent execution
	 */
	void add(ISimulationAgent agent);

	/**
	 * Executes one step for all active simulation agents concurrently.
	 * 
	 * <p>
	 * This method signals all simulation threads to execute their next step and blocks until all have completed. It
	 * provides the synchronization necessary for coordinated multi-simulation execution.
	 * </p>
	 */
	void step();

	/**
	 * Disposes of the runner, releasing all resources and terminating all simulation threads.
	 * 
	 * <p>
	 * This method should be called when the experiment completes or is closed. It signals all simulation threads to
	 * terminate and clears internal data structures.
	 * </p>
	 */
	void dispose();

	/**
	 * Returns the set of simulation agents currently managed by this runner.
	 * 
	 * <p>
	 * This provides access to all simulations that are currently registered and may be executing steps. The returned
	 * set reflects the current state and may change as simulations are added or removed.
	 * </p>
	 * 
	 * @return the set of active simulation agents (never null, but may be empty)
	 */
	Set<ISimulationAgent> getStepable();

	/**
	 * Returns the number of simulation threads currently active.
	 * 
	 * <p>
	 * This count represents how many simulations are currently registered with the runner and have active threads.
	 * </p>
	 * 
	 * @return the number of active simulation threads
	 */
	int getActiveThreads();

	/**
	 * Checks whether this runner has any active simulations.
	 * 
	 * @return true if at least one simulation is registered, false otherwise
	 */
	boolean hasSimulations();

}