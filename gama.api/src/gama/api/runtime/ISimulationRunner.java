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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
 * <li>Manage the dedicated thread of each simulation agent (one simulation = one thread), which runs its whole
 * lifecycle</li>
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
	 * This method unregisters the simulation. The simulation's thread will complete the tasks already submitted to it
	 * (if any) and then terminate.
	 * </p>
	 * 
	 * @param agent
	 *            the simulation agent to remove from concurrent execution
	 */
	void remove(ISimulationAgent agent);

	/**
	 * Adds a simulation agent to the runner, creating and starting its dedicated thread if it does not exist yet. All
	 * the lifecycle of the simulation (initialization, steps, restoration...) is then executed by this thread, until
	 * the simulation is removed.
	 *
	 * @param agent
	 *            the simulation agent to add
	 * @param scheduled
	 *            whether the simulation takes part in the synchronized steps (see {@link #step()}). Passing false does
	 *            not unschedule an already scheduled simulation (see {@link #unschedule(ISimulationAgent)})
	 */
	void add(ISimulationAgent agent, boolean scheduled);

	/**
	 * Adds a simulation agent to the runner and schedules it.
	 *
	 * @param agent
	 *            the simulation agent to add to concurrent execution
	 * @see #add(ISimulationAgent, boolean)
	 */
	default void add(final ISimulationAgent agent) {
		add(agent, true);
	}

	/**
	 * Stops stepping a simulation (whether synchronously or autonomously), but keeps its thread alive so that the rest
	 * of its lifecycle (e.g. the collection of its results) is still executed by it.
	 *
	 * @param agent
	 *            the simulation agent
	 */
	void unschedule(ISimulationAgent agent);

	/**
	 * Runs a task in the thread of a simulation and waits for its result. The task is run directly if the caller is
	 * already this thread, if the simulation has no thread, or if the caller is the display thread (which must never
	 * wait for a simulation). Exceptions thrown by the task are rethrown to the caller.
	 *
	 * @param <T>
	 *            the type of the result
	 * @param agent
	 *            the simulation agent
	 * @param task
	 *            the task to run
	 * @return the result of the task
	 */
	<T> T runInThreadOf(ISimulationAgent agent, Supplier<T> task);

	/**
	 * Runs a task in the thread of a simulation and waits for its completion.
	 *
	 * @param agent
	 *            the simulation agent
	 * @param task
	 *            the task to run
	 * @see #runInThreadOf(ISimulationAgent, Supplier)
	 */
	default void executeInThreadOf(final ISimulationAgent agent, final Runnable task) {
		runInThreadOf(agent, () -> {
			task.run();
			return null;
		});
	}

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

	/**
	 * Lets an already added simulation run on its own thread, without any synchronization with the other simulations:
	 * it steps as many times as needed until {@code isOver} is true, then {@code onOver} is called (from the
	 * simulation's thread). The thread remains alive (and keeps executing the tasks submitted to it) until the
	 * simulation is removed. The simulation is not taken into account by {@link #step()}
	 * anymore.
	 *
	 * @param agent
	 *            a simulation previously added with {@link #add(ISimulationAgent)}
	 * @param isOver
	 *            evaluated after each step (and in case of an error); true when the simulation must stop
	 * @param isPaused
	 *            evaluated before each step; the simulation waits as long as it is true (unless it is over)
	 * @param onOver
	 *            called once, when the simulation is over, including when it dies (and is thus removed from this
	 *            runner) or is already dead
	 */
	void runAutonomously(ISimulationAgent agent, Predicate<ISimulationAgent> isOver, BooleanSupplier isPaused,
			Consumer<ISimulationAgent> onOver);

}
