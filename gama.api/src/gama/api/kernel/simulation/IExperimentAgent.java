/*******************************************************************************************************
 *
 * IExperimentAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import java.util.Collection;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.agent.IPopulationFactory;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.ui.IExperimentDisplayable;
import gama.api.ui.IOutputManager;

/**
 * The Interface IExperimentAgent.
 */

public interface IExperimentAgent extends ITopLevelAgent {

	/** The Constant MODEL_PATH. */
	String MODEL_PATH = "model_path";

	/** The Constant PROJECT_PATH. */
	String PROJECT_PATH = "project_path";

	/** The Constant MINIMUM_CYCLE_DURATION. */
	String MINIMUM_CYCLE_DURATION = "minimum_cycle_duration";

	/** The Constant MAXIMUM_CYCLE_DURATION. */
	String MAXIMUM_CYCLE_DURATION = "maximum_cycle_duration";

	/**
	 * The Interface Batch.
	 */

	interface Batch extends IExperimentAgent {

		/**
		 * @return
		 */
		List<IParameter.Batch> getParametersToExplore();

		/**
		 * @return
		 */
		Double[] getSeeds();

	}

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	@Override
	IExperimentSpecies getSpecies();

	/**
	 * Gets the working path.
	 *
	 * @return the working path
	 */
	@getter (
			value = MODEL_PATH,
			initializer = true)
	String getWorkingPath();

	/**
	 * Sets the working path.
	 *
	 * @param p
	 *            the new working path
	 */
	@setter (MODEL_PATH)
	void setWorkingPath(final String p);

	/**
	 * Gets the working paths.
	 *
	 * @return the working paths
	 */
	List<String> getWorkingPaths();

	/**
	 * Gets the parameters.
	 *
	 * @param scope
	 *            the scope
	 * @return the parameters
	 */
	@getter (IKeyword.PARAMETERS)
	@doc ("returns the map of parameters defined in this experiment")
	IMap<String, Object> getParameters(final IScope scope);

	/**
	 * Gets the minimum duration.
	 *
	 * @return the minimum duration
	 */
	@getter (
			value = MINIMUM_CYCLE_DURATION,
			initializer = true)
	Double getMinimumDuration();

	/**
	 * Sets the minimum duration.
	 *
	 * @param d
	 *            the new minimum duration
	 */
	@setter (MINIMUM_CYCLE_DURATION)
	void setMinimumDuration(Double d);

	/**
	 * Close simulations.
	 */
	void closeSimulations(boolean andLeaveExperimentPerspective);

	/**
	 * Close simulation.
	 *
	 * @param simulationAgent
	 *            the simulation agent
	 */
	void closeSimulation(ISimulationAgent simulationAgent);

	/**
	 * Gets the simulation population.
	 *
	 * @return the simulation population
	 */
	IPopulation.Simulation getSimulationPopulation();

	/**
	 * Can step back.
	 *
	 * @return true, if successful
	 */
	boolean canStepBack();

	/**
	 * Checks if is headless.
	 *
	 * @return true, if is headless
	 */
	boolean isHeadless();

	/**
	 * Returns the population factory of this type of experiment -- default is a DefaultPopulationFactory
	 *
	 * @return the population factory
	 */
	IPopulationFactory getPopulationFactory();

	/**
	 * Sets the maximum duration.
	 *
	 * @param d
	 *            the new maximum duration
	 */
	@setter (MAXIMUM_CYCLE_DURATION)
	void setMaximumDuration(Double d);

	/**
	 * Gets the maximum duration.
	 *
	 * @return the maximum duration
	 */
	@getter (
			value = MAXIMUM_CYCLE_DURATION,
			initializer = true)
	Double getMaximumDuration();

	/**
	 * Checks if is record.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is record
	 * @date 9 août 2023
	 */
	default boolean isRecord() { return getSpecies().isMemorize(); }

	/**
	 * Sets the current simulation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param simulationAgent
	 *            the new current simulation
	 * @date 11 août 2023
	 */
	void setCurrentSimulation(final ISimulationAgent simulationAgent);

	/**
	 * Checks for parameters or user commands.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if successful
	 * @date 14 août 2023
	 */
	default boolean hasParametersOrUserCommands() {
		return getSpecies().hasParametersOrUserCommands();
	}

	/**
	 * Gets the displayables.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the displayables
	 * @date 14 août 2023
	 */
	default List<IExperimentDisplayable> getDisplayables() { return getSpecies().getDisplayables(); }

	/**
	 * Gets the user commands.
	 *
	 * @return the user commands
	 */
	default Collection<? extends IExperimentDisplayable> getUserCommands() { return getSpecies().getUserCommands(); }

	/**
	 * Gets the stop condition.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the stop condition
	 * @date 15 oct. 2023
	 */
	IExpression getStopCondition();

	/**
	 * @param scope
	 */
	boolean backward(IScope scope);

	/**
	 * @param parametersSet
	 * @param b
	 * @return
	 */
	ISimulationAgent createSimulation(IMap<String, Object> parametersSet, boolean b);

	/**
	 * @return
	 */
	@getter (PROJECT_PATH)
	String getProjectPath();

	/**
	 * Gets the cycle.
	 *
	 * @param scope
	 *            the scope
	 * @return the cycle
	 */
	@getter (IKeyword.CYCLE)
	Integer getCycle(final IScope scope);

	/**
	 * Gets the seed.
	 *
	 * @return the seed
	 */
	@getter (
			value = IKeyword.SEED,
			initializer = true)
	Double getSeed();

	/**
	 * Sets the seed.
	 *
	 * @param s
	 *            the new seed
	 */
	@setter (IKeyword.SEED)
	void setSeed(final Double s);

	/**
	 * @param d
	 */
	void setMinimumDurationExternal(Double d);

	/**
	 * Gets the usage.
	 *
	 * @return the usage
	 */
	@getter (
			value = ISimulationAgent.USAGE,
			initializer = false)
	Integer getUsage();

	/**
	 * Sets the usage.
	 *
	 * @param s
	 *            the new usage
	 */
	@setter (ISimulationAgent.USAGE)
	void setUsage(final Integer s);

	/**
	 * Gets the rng.
	 *
	 * @return the rng
	 */
	@getter (
			value = IKeyword.RNG,
			initializer = true)
	String getRng();

	/**
	 * Sets the rng.
	 *
	 * @param newRng
	 *            the new rng
	 */
	@setter (IKeyword.RNG)
	void setRng(final String newRng);

	/**
	 * Gets the simulations.
	 *
	 * @return the simulations
	 */
	@getter (IKeyword.SIMULATIONS)
	IList<? extends IAgent> getSimulations();

	/**
	 * Sets the simulations.
	 *
	 * @param simulations
	 *            the new simulations
	 */
	@setter (IKeyword.SIMULATIONS)
	default void setSimulations(final IList<IAgent> simulations) {
		// Forbidden
	}

	/**
	 * Gets the simulation.
	 *
	 * @return the simulation
	 */
	@Override
	@getter (IKeyword.SIMULATION)
	ISimulationAgent getSimulation();

	/**
	 * Sets the simulation.
	 *
	 * @param sim
	 *            the new simulation
	 */
	@setter (IKeyword.SIMULATION)
	void setSimulation(final ISimulationAgent sim);

	/**
	 * Update displays.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	Object updateDisplays(final IScope scope);

	/**
	 * Update parameters.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	Object updateParameters(final IScope scope);

	/**
	 * Compact memory.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */

	Object compactMemory(final IScope scope);

	/**
	 * @return
	 */
	IMap<String, Object> getParameterValues();

	/**
	 * @return
	 */
	Iterable<? extends IParameter.Batch> getDefaultParameters();

	/**
	 * @return
	 */
	Iterable<IOutputManager> getAllSimulationOutputs();

	/**
	 * @return
	 */
	boolean isScheduled();

	/**
	 * @param scope
	 */
	default Object _init_(final IScope scope) {
		return null;
	}

}
