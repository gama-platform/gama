/*******************************************************************************************************
 *
 * IExploration.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.kernel.batch;

import java.util.List;

import gama.core.kernel.batch.exploration.Exploration;
import gama.core.kernel.batch.exploration.betadistribution.BetaExploration;
import gama.core.kernel.batch.exploration.morris.MorrisExploration;
import gama.core.kernel.batch.exploration.sobol.SobolExploration;
import gama.core.kernel.batch.exploration.stochanalysis.StochanalysisExploration;
import gama.core.kernel.batch.optimization.HillClimbing;
import gama.core.kernel.batch.optimization.SimulatedAnnealing;
import gama.core.kernel.batch.optimization.Swarm;
import gama.core.kernel.batch.optimization.TabuSearch;
import gama.core.kernel.batch.optimization.TabuSearchReactive;
import gama.core.kernel.batch.optimization.genetic.GeneticAlgorithm;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.ISymbol;
import gama.gaml.expressions.IExpression;

/**
 * The class IExploration.
 *
 * @author drogoul
 * @since 26 d�c. 2011
 *
 */
public interface IExploration extends ISymbol {// , Runnable {

	/** The Constant CLASSES. */
@SuppressWarnings ("rawtypes") public static final Class[] CLASSES =
			{ GeneticAlgorithm.class, SimulatedAnnealing.class, HillClimbing.class, TabuSearch.class,
					TabuSearchReactive.class, Exploration.class, Swarm.class,
					SobolExploration.class,MorrisExploration.class,StochanalysisExploration.class,BetaExploration.class};

	/**
	 * TODO
	 * 
	 * @param scope
	 * @param agent
	 * @throws GamaRuntimeException
	 */
	public abstract void initializeFor(IScope scope, final BatchAgent agent) throws GamaRuntimeException;

	/**
	 * TODO
	 * 
	 * @param exp
	 * @param agent
	 */
	public abstract void addParametersTo(final List<IParameter.Batch> exp, BatchAgent agent);

	/**
	 * TODO
	 * 
	 * @param scope
	 */
	public abstract void run(IScope scope);
	
	/**
	 * If the exploration is based on the optimization of a fitness or not
	 * 
	 * @return {@link Boolean}, true if based on fitness, false otherwise
	 */
	public boolean isFitnessBased();
	
	/**
	 * The expression that represents the requested outputs
	 * 
	 * @return
	 */
	public IExpression getOutputs();

}