/*******************************************************************************************************
 *
 * IExploration.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import java.util.List;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.ISymbol;
import gama.api.runtime.scope.IScope;

/**
 * The class IExploration.
 *
 * @author drogoul
 * @since 26 d�c. 2011
 *
 */
public interface IExploration extends ISymbol {// , Runnable {
	/** The Constant Method */
	String METHODS = "sampling";

	/** The Constant SAMPLE_SIZE */
	String SAMPLE_SIZE = "sample";

	/** The factorial sampling */
	String SAMPLE_FACTORIAL = "factorial";

	/** The Constant DEFAULT_FACTORIAL. */
	int DEFAULT_FACTORIAL = 9;

	/** The Constant NB_LEVELS */
	String NB_LEVELS = "levels";

	/** The Constant ITERATIONS */
	String ITERATIONS = "iterations";

	/** The Constant FROM_FILE. */
	String FROM_FILE = "FROMFILE";

	/** The Constant FROM_LIST. */
	String FROM_LIST = "FROMLIST";

	/** The Constant DEFAULT_SAMPLING */
	String DEFAULT_SAMPLING = "Exhaustive";

	/**
	 * Initialize for.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void initializeFor(IScope scope, final IExperimentAgent.Batch agent) throws GamaRuntimeException;

	/**
	 * Adds the parameters to.
	 *
	 * @param exp
	 *            the exp
	 * @param agent
	 *            the agent
	 */
	void addParametersTo(final List<IParameter.Batch> exp, IExperimentAgent.Batch agent);

	/**
	 * Run.
	 *
	 * @param scope
	 *            the scope
	 */
	void run(IScope scope);

	/**
	 * If the exploration is based on the optimization of a fitness or not
	 *
	 * @return {@link Boolean}, true if based on fitness, false otherwise
	 */
	boolean isFitnessBased();

	/**
	 * The expression that represents the requested outputs
	 *
	 * @return
	 */
	IExpression getOutputs();

}