/*******************************************************************************************************
 *
 * SimulationRunner.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import static gama.api.runtime.GamaExecutorService.EXCEPTION_HANDLER;
import static gama.api.runtime.GamaExecutorService.getParallelism;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.GamaExecutorService.Caller;
import gama.dev.DEBUG;

/**
 * Default implementation of {@link ISimulationRunner} for managing concurrent simulation execution.
 * 
 * <p>
 * SimulationRunner coordinates the parallel execution of multiple simulation agents within a GAMA experiment. It uses
 * a thread-per-simulation model where each simulation runs in its own dedicated thread, synchronized by semaphores to
 * ensure coordinated stepping.
 * </p>
 * 
 * <p>
 * Architecture:
 * </p>
 * <ul>
 * <li>Each simulation agent gets its own dedicated thread that loops until the simulation dies</li>
 * <li>Two {@link GeneralSynchronizer} instances coordinate execution:
 * <ul>
 * <li><b>simulationsSemaphore:</b> Controls when simulations can execute their step</li>
 * <li><b>experimentSemaphore:</b> Signals the experiment when all simulations have completed their step</li>
 * </ul>
 * </li>
 * <li>The experiment thread calls {@link #step()}, which releases all simulation threads and waits for them to
 * complete</li>
 * <li>Concurrency level is determined by experiment configuration and preferences</li>
 * </ul>
 * 
 * <p>
 * Execution flow:
 * </p>
 * 
 * <pre>
 * 1. Experiment calls step()
 * 2. step() releases N permits to simulationsSemaphore (N = number of simulations)
 * 3. Each simulation thread acquires a permit and executes its step
 * 4. Each simulation releases a permit to experimentSemaphore when done
 * 5. step() acquires N permits from experimentSemaphore (blocks until all done)
 * 6. step() returns to experiment, all simulations synchronized
 * </pre>
 * 
 * <p>
 * Usage example (typically internal):
 * </p>
 * 
 * <pre>
 * IPopulation simPop = experiment.getSimulationPopulation();
 * SimulationRunner runner = SimulationRunner.of(simPop);
 * 
 * // Add simulations
 * runner.add(simulation1);
 * runner.add(simulation2);
 * 
 * // Run synchronized steps
 * while (!stopped) {
 * 	runner.step(); // All simulations execute one step
 * }
 * 
 * runner.dispose();
 * </pre>
 * 
 * @see ISimulationRunner
 * @see GeneralSynchronizer
 * @see ISimulationAgent
 */
public class SimulationRunner implements ISimulationRunner {

	static {
		DEBUG.OFF();
	}

	/** Maps each simulation agent to its dedicated execution thread. */
	final Map<ISimulationAgent, Thread> runnables;

	/** Synchronizer controlling when simulations can execute their step. */
	final GeneralSynchronizer simulationsSemaphore = GeneralSynchronizer.withInitialPermits(0);

	/** Synchronizer signaling to the experiment when simulations complete their step. */
	final GeneralSynchronizer experimentSemaphore = GeneralSynchronizer.withInitialPermits(0);
	
	/** The concurrency level (number of simulations that can run concurrently). */
	final int concurrency;

	/** Flag indicating if the runner has been shut down. */
	volatile boolean shutdown = false;

	/**
	 * O(1) count of currently active simulations. Maintained in sync with {@link #runnables} via
	 * {@link #add(ISimulationAgent)} and {@link #remove(ISimulationAgent)}. Replaces
	 * {@code ConcurrentHashMap.size()} (which is O(n)) on the hot {@link #step()} / {@link #hasSimulations()} path.
	 */
	private volatile int activeCount = 0;

	/**
	 * Creates a SimulationRunner configured for the given simulation population.
	 * 
	 * <p>
	 * The concurrency level is determined based on:
	 * </p>
	 * <ul>
	 * <li>For headless non-batch experiments: concurrency = 1 (sequential)</li>
	 * <li>Otherwise: determined by experiment's concurrency expression and preferences</li>
	 * </ul>
	 * 
	 * @param pop
	 *            the simulation population whose simulations will be managed
	 * @return a new SimulationRunner configured appropriately for the experiment type
	 */
	public static SimulationRunner of(final IPopulation pop) {
		int concurrency = 0;
		final IExperimentSpecies plan = (IExperimentSpecies) pop.getHost().getSpecies();
		if (plan.isHeadless() && !plan.isBatch()) {
			concurrency = 1;
		} else {
			concurrency = getParallelism(pop.getHost().getScope(), plan.getConcurrency(), Caller.SIMULATION);
		}
		return new SimulationRunner(concurrency < 0 ? 1 : concurrency);
	}

	/**
	 * Constructs a new SimulationRunner with the specified concurrency level.
	 * 
	 * @param concurrency
	 *            the maximum number of simulations that can execute concurrently (typically 1 for sequential or number
	 *            of CPU cores for parallel)
	 */
	private SimulationRunner(final int concurrency) {
		this.concurrency = concurrency;
		// ConcurrentHashMap ensures that add(), remove(), step(), dispose(), and
		// size() queries from different threads do not race on the underlying structure.
		runnables = new ConcurrentHashMap<>();
	}

	/**
	 * Removes a simulation agent from the runner.
	 *
	 * <p>
	 * The simulation's thread will complete its current step and then terminate. The simulation is removed from the
	 * active set.
	 * </p>
	 *
	 * @param agent
	 *            the simulation agent to remove
	 */
	@Override
	public void remove(final ISimulationAgent agent) {
		if (runnables.remove(agent) != null) { activeCount--; }
	}

	/**
	 * Adds a simulation agent to the runner and starts its dedicated execution thread.
	 *
	 * <p>
	 * Creates a new thread for the simulation that will:
	 * </p>
	 * <ol>
	 * <li>Wait for a permit from simulationsSemaphore</li>
	 * <li>Execute the simulation's step</li>
	 * <li>Release a permit to experimentSemaphore</li>
	 * <li>Repeat until the simulation dies or the runner shuts down</li>
	 * </ol>
	 *
	 * @param agent
	 *            the simulation agent to add
	 */
	@Override
	public void add(final ISimulationAgent agent) {
		Thread t = new Thread("Thread of " + agent.getName()) {
			@Override
			public void run() {
				while (!shutdown && !agent.dead()) {
					// DEBUG.OUT("Waiting for " + agent);
					simulationsSemaphore.acquire();
					// Recheck after waking up: dispose() may have released the permit just to
					// unblock this thread for shutdown — we must not step a dead/cleared runner.
					if (shutdown || agent.dead()) { break; }
					try {
						agent.step();
						experimentSemaphore.release();
					} catch (Throwable tg) {
						EXCEPTION_HANDLER.uncaughtException(Thread.currentThread(), tg);
					}
				}
			}
		};
		t.start();
		runnables.put(agent, t);
		activeCount++;
	}

	/**
	 * Executes one synchronized step for all active simulations.
	 *
	 * <p>
	 * This method:
	 * </p>
	 * <ol>
	 * <li>Releases permits to allow all simulation threads to execute</li>
	 * <li>Blocks waiting for all simulations to complete their step</li>
	 * <li>Returns when all simulations have finished stepping</li>
	 * </ol>
	 * <p>
	 * This provides the synchronization needed for coordinated multi-simulation execution where all simulations
	 * advance together through simulation time.
	 * </p>
	 */
	@Override
	public void step() {
		// DEBUG.OUT("Releasing to all simulations");
		int nb = activeCount;
		simulationsSemaphore.release(nb);
		experimentSemaphore.acquire(nb);
	}

	/**
	 * Disposes of the runner, shutting down all simulation threads.
	 *
	 * <p>
	 * Sets the shutdown flag, clears the simulation map, and releases semaphores to allow any waiting threads to
	 * terminate gracefully.
	 * </p>
	 */
	@Override
	public void dispose() {
		shutdown = true;
		int nb = activeCount;
		activeCount = 0;
		runnables.clear();
		// DEBUG.OUT("Disposing simulation runner and releasing " + nb + " threads");
		experimentSemaphore.release(nb);
		simulationsSemaphore.release(nb);
	}

	/**
	 * Returns the set of simulation agents currently managed by this runner.
	 * 
	 * @return the set of active simulation agents
	 */
	@Override
	public Set<ISimulationAgent> getStepable() { return runnables.keySet(); }

	/**
	 * Returns the number of active simulation threads.
	 *
	 * <p>
	 * Uses a dedicated {@code volatile int} counter maintained by {@link #add(ISimulationAgent)} and
	 * {@link #remove(ISimulationAgent)} rather than calling {@code ConcurrentHashMap.size()} (which is O(n)) on
	 * every simulation cycle.
	 * </p>
	 *
	 * @return the count of simulations currently registered
	 */
	@Override
	public int getActiveThreads() { return activeCount; }

	/**
	 * Checks whether this runner has any active simulations.
	 *
	 * @return true if at least one simulation is registered, false otherwise
	 */
	@Override
	public boolean hasSimulations() {
		return activeCount > 0;
	}

}
