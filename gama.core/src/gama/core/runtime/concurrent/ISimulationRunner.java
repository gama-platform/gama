/*******************************************************************************************************
 *
 * ISimulationRunner.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.concurrent;

import java.util.Set;

import gama.annotations.precompiler.OkForAPI;
import gama.core.kernel.simulation.ISimulationAgent;

/**
 * The Interface ISimulationRunner.
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface ISimulationRunner {

	/**
	 * Removes the.
	 *
	 * @param agent
	 *            the agent
	 */
	void remove(ISimulationAgent agent);

	/**
	 * Adds the.
	 *
	 * @param agent
	 *            the agent
	 */
	void add(ISimulationAgent agent);

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
	Set<ISimulationAgent> getStepable();

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