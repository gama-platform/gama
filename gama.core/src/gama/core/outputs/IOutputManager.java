/*******************************************************************************************************
 *
 * IOutputManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.Collection;
import java.util.Map;

import gama.core.common.interfaces.IStepable;
import gama.core.runtime.IScope;

/**
 *
 * Interface IOutputManager. Output managers are owned by the simulations and experiments and manage, on behalf of them,
 * the various outputs defined in the 'output' section of the experiments
 *
 *
 * @author A. Drogoul
 * @since 14 dec. 2011
 *
 */
public interface IOutputManager extends IStepable, Iterable<IOutput> {

	/**
	 * Adds an output to the outputs managed by this manager. If the output already exists, does nothing.
	 *
	 * @param output
	 *            an instance of IOutput (or IDisplayOutput)
	 */
	void add(IOutput output);

	/**
	 * Removes an output from the outputs managed by this manager. Does nothing if the output does not exist
	 *
	 * @param output
	 *            an instance of IOutput (or IDisplayOutput)
	 */
	void remove(IOutput output);

	/**
	 * Removes all outputs from this IOutputManager
	 */
	void clear();

	/**
	 * Adds all the outputs passed in parameter to this IOutputManager, replacing existing outputs with the same id if
	 * any
	 *
	 * @param outputs
	 *            a map of <String, IOutput> (cannot be null) where the keys represent the output's id. See
	 *            {@link IOutput#getId()}
	 */

	void putAll(Map<String, IOutput> outputs);

	/**
	 * Returns the output with the given id, or null if none can be found
	 *
	 * @param id
	 *            the unique identifier of the output
	 * @return an instance of IOutput or null
	 */
	IOutput getOutputWithId(String id);

	/**
	 * Returns the output with the given original name, or null if none can be found
	 *
	 * @param id
	 *            the original name of the output (i.e. the one provided in the model)
	 * @return an instance of IOutput or null
	 */
	IOutput getOutputWithOriginalName(final String name);

	/**
	 * Returns the map of outputs managed by this IOutputManager. Keys are the identifiers, values the outputs
	 * themselves. Any modification to this map will alter the output manager
	 *
	 * @return the map of <id, output>
	 */
	Map<String, ? extends IOutput> getOutputs();

	/**
	 * Forces all the outputs to update themselves whatever their refreshable state (see {@link IOutput#update()}
	 */
	void forceUpdateOutputs();

	/**
	 * Gets the monitors.
	 *
	 * @return the monitors
	 */
	Collection<MonitorOutput> getMonitors();

	/**
	 * Asks this manager to pause its operations (outputs will not be updated until {@link IOutputManager#resume()} is
	 * called. Does nothing if the manager is already paused.
	 */
	void pause();

	/**
	 * Asks this manager to resume its operations (outputs will be updated until {@link IOutputManager#pause()} or
	 * {IOutputManager{@link #dispose(IScope)} are called. Does nothing if the manager is already active.
	 */
	void resume();

	/**
	 * Asks this manager to close all of its outputs (see {@link IOutput#close()}
	 */
	void close();

	/**
	 * Asks this manager to open a given output (see {@link IOutput#open()}. Does nothing if the output is already open
	 *
	 * @param scope
	 *            the scope in which the output will be opened
	 * @param output
	 *            the output to open
	 * @return true if the output has been opened or was already open, false otherwise (if it cannot be opened)
	 */
	boolean open(IScope scope, IOutput output);

	/**
	 * Checks for monitors.
	 *
	 * @return true, if successful
	 */
	boolean hasMonitors();

}
