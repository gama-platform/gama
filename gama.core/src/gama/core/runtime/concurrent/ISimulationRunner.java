/*******************************************************************************************************
 *
 * ISimulationRunner.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.concurrent;

import java.util.Set;

import gama.core.kernel.simulation.SimulationAgent;

/**
 * The Interface ISimulationRunner.
 */
public interface ISimulationRunner {

	/**
	 * Removes the.
	 *
	 * @param agent
	 *            the agent
	 */
	void remove(SimulationAgent agent);

	/**
	 * Adds the.
	 *
	 * @param agent
	 *            the agent
	 */
	void add(SimulationAgent agent);

	/**
	 * Step.
	 */
	void step();

	/**
	 * Dispose.
	 */
	void dispose();

	/**
	 * Gets the active stepables.
	 *
	 * @return the active stepables
	 */
	Set<SimulationAgent> getStepable();

	/**
	 * Gets the active threads.
	 *
	 * @return the active threads
	 */
	int getActiveThreads();

	/**
	 * Checks for simulations.
	 *
	 * @return true, if successful
	 */
	boolean hasSimulations();

}