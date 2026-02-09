/*******************************************************************************************************
 *
 * IExperimentSpecies.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.species;

import java.util.List;
import java.util.Map;

import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.data.objects.IList;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.IParameter.Batch;
import gama.api.kernel.agent.IPopulation.Experiment;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentController;
import gama.api.kernel.simulation.IExploration;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IExperimentDisplayable;
import gama.api.ui.IOutputManager;

/**
 * Written by drogoul Modified on 31 mai 2011
 *
 * @todo Description
 *
 */

public interface IExperimentSpecies extends ISpecies {

	/** The test category name. */
	String TEST_CATEGORY_NAME = "Configuration of tests";

	/** The explorable category name. */
	String EXPLORABLE_CATEGORY_NAME = "Parameters to explore";

	/** The system category prefix. */
	String SYSTEM_CATEGORY_PREFIX = "Random number generation";

	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	IModelSpecies getModel();

	/**
	 * Sets the model.
	 *
	 * @param model
	 *            the new model
	 */
	void setModel(final IModelSpecies model);

	/**
	 * Gets the original simulation outputs.
	 *
	 * @return the original simulation outputs
	 */
	IOutputManager getOriginalSimulationOutputs();

	/**
	 * Refresh all outputs.
	 */
	void refreshAllOutputs();

	/**
	 * Pause all outputs.
	 */
	void pauseAllOutputs();

	/**
	 * Resume all outputs.
	 */
	void resumeAllOutputs();

	/**
	 * Close all outputs.
	 */
	void closeAllOutputs();

	/**
	 * Gets the experiment outputs.
	 *
	 * @return the experiment outputs
	 */
	IOutputManager getExperimentOutputs();

	/**
	 * Checks for parameter.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	boolean hasParameter(String name);

	/**
	 * Gets the agent.
	 *
	 * @return the agent
	 */
	IExperimentAgent getAgent();

	/**
	 * Gets the experiment scope.
	 *
	 * @return the experiment scope
	 */
	IScope getExperimentScope();

	/**
	 * Open.
	 */
	void open();

	/**
	 * Reload.
	 */
	void reload();

	/**
	 * Gets the current simulation.
	 *
	 * @return the current simulation
	 */
	ISimulationAgent getCurrentSimulation();

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	Map<String, IParameter> getParameters();

	/**
	 * Gets the exploration algorithm.
	 *
	 * @return the exploration algorithm
	 */
	IExploration getExplorationAlgorithm();

	/**
	 * Gets the log.
	 *
	 * @return the log
	 */
	// FileOutput getLog();

	/**
	 * Checks if is batch.
	 *
	 * @return true, if is batch
	 */
	boolean isBatch();

	/**
	 * Checks if is memorize.
	 *
	 * @return true, if is memorize
	 */
	boolean isMemorize();

	/**
	 * Gets the explorable parameters.
	 *
	 * @return the explorable parameters
	 */
	Map<String, Batch> getExplorableParameters();

	/**
	 * Gets the controller.
	 *
	 * @return the controller
	 */
	IExperimentController getController();

	/**
	 * Set the controller.
	 *
	 * @return the controller
	 */
	void setController(IExperimentController ec);

	/**
	 * @return
	 */
	boolean isHeadless();

	/**
	 * Sets the headless.
	 *
	 * @param headless
	 *            the new headless
	 */
	void setHeadless(boolean headless);

	/**
	 * Gets the experiment type. TODO : make it coherent with IExperimentStateListener.Type
	 *
	 * @return the experiment type
	 */
	String getExperimentType();

	/**
	 * Keeps seed.
	 *
	 * @return true, if successful
	 */
	boolean keepsSeed();

	/**
	 * Keeps simulations.
	 *
	 * @return true, if successful
	 */
	boolean keepsSimulations();

	/**
	 * Checks for parameters or user commands.
	 *
	 * @return true, if successful
	 */
	boolean hasParametersOrUserCommands();

	/**
	 * Recompute and refresh all outputs.
	 */
	void recomputeAndRefreshAllOutputs();

	/**
	 * Gets the active output managers.
	 *
	 * @return the active output managers
	 */
	Iterable<IOutputManager> getActiveOutputManagers();

	/**
	 * Checks if is autorun.
	 *
	 * @return true, if is autorun
	 */
	boolean isAutorun();

	/**
	 * Checks if is test.
	 *
	 * @return true, if is test
	 */
	boolean isTest();

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Override
	IExperimentDescription getDescription();

	/**
	 * Should be benchmarked.
	 *
	 * @return true, if successful
	 */
	boolean shouldBeBenchmarked();

	/**
	 * Gets the displayables.
	 *
	 * @return the displayables
	 */
	List<IExperimentDisplayable> getDisplayables();

	/**
	 * Sets the concurrency.
	 *
	 * @param exp
	 *            the new concurrency, expected to be an expression returning an integer.
	 */
	void setConcurrency(IExpression exp);

	/**
	 * Should record.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the i expression
	 * @date 2 sept. 2023
	 */
	IExpression shouldRecord();

	/**
	 * Gets the stop condition.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the stop condition
	 * @date 15 oct. 2023
	 */
	IExpression getStopCondition();

	/**
	 * Sets the stop condition.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param expression
	 *            the new stop condition
	 * @date 15 oct. 2023
	 */
	void setStopCondition(IExpression expression);

	/**
	 * Sets the stop condition.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param cond
	 *            the new stop condition
	 * @date 15 oct. 2023
	 */
	void setStopCondition(final String cond);

	/**
	 * Sets the parameter values.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param params
	 *            the new parameter values
	 * @date 15 oct. 2023
	 */
	void setParameterValues(IList params);

	/**
	 *
	 */
	void refreshAllParameters();

	/**
	 * Whether the current experiment is reloading or not (see #344)
	 *
	 * @return
	 */
	boolean isReloading();

	/**
	 * @param key
	 * @return
	 */
	Batch getParameterByTitle(String key);

	/**
	 * @param experimentScope
	 * @param key
	 * @param value
	 */
	void setParameterValueByTitle(IScope experimentScope, String key, Object value);

	/**
	 * @param experimentScope
	 * @param key
	 * @param value
	 */
	void setParameterValue(IScope experimentScope, String key, Object value);

	/**
	 * @param seed
	 */
	void open(Double seed);

	/**
	 * @param scope
	 * @return
	 */
	Experiment createPopulation(IScope scope);

}