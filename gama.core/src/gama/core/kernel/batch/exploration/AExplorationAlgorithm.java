/*******************************************************************************************************
 *
 * AExplorationAlgorithm.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration;

import java.util.Arrays;
import java.util.List;

import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.batch.IExploration;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.IType;

/**
 * The Class AExplorationAlgorithm.
 */
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
public abstract class AExplorationAlgorithm extends Symbol implements IExploration {
	
	/** The current experiment. */
	protected BatchAgent currentExperiment;
	
	/** The outputs expression. */
	protected IExpression outputsExpression;
	
	/** The output variables. */
	protected List<String> outputVariables;
	
	/** The automatic output batch file */
	protected IExpression outputFilePath;
	
	@Override
	public void initializeFor(IScope scope, BatchAgent agent) throws GamaRuntimeException {
		this.currentExperiment = agent;
	}
	
	/**
	 * Instantiates a new a exploration algorithm.
	 *
	 * @param desc the desc
	 */
	public AExplorationAlgorithm(final IDescription desc) { 
		super(desc);
		if (hasFacet(IKeyword.BATCH_VAR_OUTPUTS)) {outputsExpression = getFacet(IKeyword.BATCH_VAR_OUTPUTS);}
		if (hasFacet(IKeyword.BATCH_OUTPUT)) { outputFilePath = getFacet(IKeyword.BATCH_OUTPUT); }
	}

	@Override
	public void addParametersTo(List<Batch> exp, BatchAgent agent) {
		exp.add(new ParameterAdapter("Exploration method", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				@SuppressWarnings ("rawtypes") final List<Class> classes = Arrays.asList(CLASSES);
				final String methodName = IKeyword.METHODS[classes.indexOf(AExplorationAlgorithm.this.getClass())];
				return methodName;
			}

		});
		if (getOutputs()!=null) {
			exp.add(new ParameterAdapter("Outputs of interest", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
				@Override public Object value() { return getOutputs().literalValue(); }
			});
		}
	}
	
	@Override
	public void run(final IScope scope) {
		try {
			explore(scope);
		} catch (final GamaRuntimeException e) {
			GAMA.reportError(scope, e, false);
		}
	}

	@Override
	public boolean isFitnessBased() { return false; }
	
	// MAIN ABSTRACTION
	
	/**
	 * Main method that launch the exploration
	 * 
	 * @param scope
	 */
	public abstract void explore(IScope scope);
	
	/**
	 * Return the specific report for this exploration
	 * TODO : has been specified for calibration - to be removed or used consistently across experiment; see {@link ExperimentAgent}
	 */
	public String getReport() {return "";}
	
	/**
	 * Gives the list of variables the exploration method is targeting
	 * 
	 * @return {@link IExpression}
	 */
	public IExpression getOutputs() {return outputsExpression;}
	
	/**
	 * Main method to build the set of points to visit during the exploration of the model
	 * 
	 * @param scope
	 * @param sets
	 * @param index
	 * @return
	 */
	public abstract List<ParametersSet> buildParameterSets(IScope scope, List<ParametersSet> sets, int index);
	
}
