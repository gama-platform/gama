/*******************************************************************************************************
 *
 * IExperiment.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.core;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.model.IModel;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.util.IList;
import gama.gaml.expressions.IExpression;
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
	IModel getModel();

	/**
	 * Gets the experiment plan.
	 *
	 * @return the experiment plan
	 */
	IExperimentPlan getExperimentPlan();

	/**
	 * Gets the simulation.
	 *
	 * @return the simulation
	 */
	SimulationAgent getSimulation();

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
