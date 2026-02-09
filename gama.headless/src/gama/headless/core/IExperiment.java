/*******************************************************************************************************
 *
 * IExperiment.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.core;

import gama.api.data.objects.IList;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.headless.server.GamaServerExperimentJob;

/**
 * The Interface IExperiment.
 */
public interface IExperiment {

	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	IModelSpecies getModel();

	/**
	 * Gets the experiment plan.
	 *
	 * @return the experiment plan
	 */
	IExperimentSpecies getExperimentPlan();

	/**
	 * Gets the simulation.
	 *
	 * @return the simulation
	 */
	ISimulationAgent getSimulation();

	/**
	 * Setup.
	 *
	 * @param experimentName
	 *            the experiment name
	 * @param seed
	 *            the seed
	 */
	void setup(final String experimentName, final double seed);

	/**
	 * Setup.
	 *
	 * @param experimentName
	 *            the experiment name
	 * @param seed
	 *            the seed
	 * @param manualExperimentJob
	 */
	void setup(final String experimentName, final double seed, final IList params,
			GamaServerExperimentJob manualExperimentJob);

	/**
	 * Step.
	 *
	 * @return the long
	 */
	long step();

	/**
	 * BackStep.
	 *
	 * @return the long
	 */
	long backStep();

	/**
	 * Checks if is interrupted.
	 *
	 * @return true, if is interrupted
	 */
	boolean isInterrupted();

	/**
	 * Sets the parameter.
	 *
	 * @param parameterName
	 *            the parameter name
	 * @param value
	 *            the value
	 */
	void setParameter(final String parameterName, final Object value);

	/**
	 * Gets the output.
	 *
	 * @param parameterName
	 *            the parameter name
	 * @return the output
	 */
	Object getOutput(final String parameterName);

	/**
	 * Gets the variable output.
	 *
	 * @param parameterName
	 *            the parameter name
	 * @return the variable output
	 */
	Object getVariableOutput(final String parameterName);

	/**
	 * Compile expression.
	 *
	 * @param expression
	 *            the expression
	 * @return the i expression
	 */
	IExpression compileExpression(final String expression);

	/**
	 * Evaluate expression.
	 *
	 * @param exp
	 *            the exp
	 * @return the object
	 */
	Object evaluateExpression(IExpression exp);

	/**
	 * Evaluate expression.
	 *
	 * @param exp
	 *            the exp
	 * @return the object
	 */
	Object evaluateExpression(String exp);

	/**
	 * Dispose.
	 */
	void dispose();

}
