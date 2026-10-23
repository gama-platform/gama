/*******************************************************************************************************
 *
 * SimulationContext.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.runtime;

import java.util.function.Supplier;

import gama.api.kernel.simulation.ISimulationAgent;

/**
 * Binds the simulation on behalf of which the current thread is running, using a {@link ScopedValue}.
 *
 * <p>
 * The simulation is bound for the whole life of the thread owning it (see {@link SimulationRunner}), of the threads
 * created on its behalf (e.g. the virtual threads of the <code>thread</code> skill) and while the worker threads of the
 * parallel execution of agents run tasks for it. Code running in these threads can
 * then retrieve "its" simulation without guessing it from the frontmost experiment (see
 * {@link gama.api.GAMA#getRuntimeScope()}). Other threads (UI, displays, server, experiment...) are not bound.
 * </p>
 *
 * <p>
 * Contrary to a {@link ThreadLocal}, a scoped value cannot be changed once bound, is automatically unbound when the
 * code it is bound for returns, and is cheap to bind in virtual threads.
 * </p>
 */
public final class SimulationContext {

	/** The simulation bound to the current thread. */
	private static final ScopedValue<ISimulationAgent> SIMULATION = ScopedValue.newInstance();

	/**
	 * Instantiates a new simulation context.
	 */
	private SimulationContext() {}

	/**
	 * Returns the simulation on behalf of which the current thread is running.
	 *
	 * @return the simulation, or null if the current thread is not running on behalf of a simulation
	 */
	public static ISimulationAgent current() {
		return SIMULATION.isBound() ? SIMULATION.get() : null;
	}

	/**
	 * Runs the task with the simulation bound to the current thread.
	 *
	 * @param simulation
	 *            the simulation (if null, the task is run without binding anything)
	 * @param task
	 *            the task
	 */
	public static void runFor(final ISimulationAgent simulation, final Runnable task) {
		if (simulation == null) {
			task.run();
		} else {
			ScopedValue.where(SIMULATION, simulation).run(task);
		}
	}

	/**
	 * Computes a result with the simulation bound to the current thread.
	 *
	 * @param <T>
	 *            the type of the result
	 * @param simulation
	 *            the simulation (if null, the result is computed without binding anything)
	 * @param task
	 *            the task
	 * @return the result
	 */
	public static <T> T callFor(final ISimulationAgent simulation, final Supplier<T> task) {
		if (simulation == null) return task.get();
		return ScopedValue.where(SIMULATION, simulation).call(task::get);
	}

}
