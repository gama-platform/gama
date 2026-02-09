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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.GamaExecutorService.Caller;
import gama.dev.DEBUG;

/**
 * The Class SimulationRunner.
 */
public class SimulationRunner implements ISimulationRunner {

	static {
		DEBUG.OFF();
	}

	/** The runnables. */
	final Map<ISimulationAgent, Thread> runnables;

	/** The simulationsSemaphore. */
	final Object lock = new Object();

	/** The simulationsSemaphore. */
	final GeneralSynchronizer simulationsSemaphore = GeneralSynchronizer.withInitialPermits(0);

	/** The experiment semaphore. */
	final GeneralSynchronizer experimentSemaphore = GeneralSynchronizer.withInitialPermits(0);
	/** The concurrency. */
	final int concurrency;

	/** The shutdown. */
	volatile boolean shutdown = false;

	/**
	 * Of.
	 *
	 * @param pop
	 *            the pop
	 * @return the simulation runner
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
	 * Instantiates a new simulation runner.
	 *
	 * @param concurrency
	 *            the concurrency
	 */
	private SimulationRunner(final int concurrency) {
		this.concurrency = concurrency;
		runnables = new LinkedHashMap<>();
	}

	/**
	 * Removes the.
	 *
	 * @param agent
	 *            the agent
	 */
	@Override
	public void remove(final ISimulationAgent agent) {
		runnables.remove(agent);
	}

	/**
	 * Adds the.
	 *
	 * @param agent
	 *            the agent
	 */
	@Override
	public void add(final ISimulationAgent agent) {
		Thread t = new Thread("Thread of " + agent.getName()) {
			@Override
			public void run() {
				while (!shutdown && !agent.dead()) {

					// DEBUG.OUT("Waiting for " + agent);
					simulationsSemaphore.acquire();
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

	}

	/**
	 * Step.
	 */
	@Override
	public void step() {
		// DEBUG.OUT("Releasing to all simulations");
		int nb = getActiveThreads();
		simulationsSemaphore.release(nb);
		experimentSemaphore.acquire(nb);
	}

	/**
	 * Dispose.
	 */
	@Override
	public void dispose() {
		shutdown = true;
		int nb = runnables.size();
		runnables.clear();
		// DEBUG.OUT("Disposing simulation runner and releasing " + nb + " threads");
		experimentSemaphore.release(nb);
		simulationsSemaphore.release(nb);
	}

	/**
	 * Gets the active stepables.
	 *
	 * @return the active stepables
	 */
	@Override
	public Set<ISimulationAgent> getStepable() { return runnables.keySet(); }

	/**
	 * Gets the active threads.
	 *
	 * @return the active threads
	 */
	@Override
	public int getActiveThreads() { return runnables.size(); }

	/**
	 * Checks for simulations.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasSimulations() {
		return runnables.size() > 0;
	}

}
