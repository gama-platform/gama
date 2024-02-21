/*******************************************************************************************************
 *
 * AOptimizationAlgorithm.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.optimization;

import static gama.gaml.operators.Cast.asFloat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.batch.IExploration;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.GAMA.InScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.AbstractGamlAdditions;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.IType;

/**
 * The Class AOptimizationAlgorithm.
 */
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
public abstract class AOptimizationAlgorithm extends Symbol implements IExploration {

	/** The Constant C_MEAN. */
	public final static short C_MAX = 0, C_MIN = 1, C_MEAN = 2;

	/** The Constant COMBINATIONS. */
	public final static String[] COMBINATIONS = { "maximum", "minimum", "average" };
	static {
		AbstractGamlAdditions._constants(COMBINATIONS);
	}

	/** The tested solutions. */
	protected HashMap<ParametersSet, Double> testedSolutions;

	/** The fitness expression. */
	protected IExpression fitnessExpression;

	/** The is maximize. */
	protected boolean isMaximize;

	/** The current experiment. */
	// private BatchAgent currentExperiment;

	/** The best solution. */
	protected ParametersSet bestSolution = null;

	/** The best fitness. */
	protected Double bestFitness = null;

	/** The combination. */
	protected short combination;

	/**
	 * Find best solution.
	 *
	 * @param scope
	 *            the scope
	 * @return the parameters set
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected abstract ParametersSet findBestSolution(IScope scope) throws GamaRuntimeException;

	@Override
	public void initializeFor(final IScope scope, final BatchAgent agent) throws GamaRuntimeException {
		// setCurrentExperiment(agent);
	}

	/**
	 * Initialize tested solutions.
	 */
	protected void initializeTestedSolutions() {
		testedSolutions = new HashMap<>();
	}

	/**
	 * Inits the params.
	 */
	protected void initParams() {
		GAMA.run(new InScope.Void() {

			@Override
			public void process(final IScope scope) {
				initParams(scope);
			}
		});
	}

	/**
	 * Inits the params.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void initParams(final IScope scope) {}

	/**
	 * Instantiates a new a optimization algorithm.
	 *
	 * @param desc
	 *            the desc
	 */
	public AOptimizationAlgorithm(final IDescription desc) {
		super(desc);
		initializeTestedSolutions();
		fitnessExpression = getFacet(IKeyword.MAXIMIZE, IKeyword.MINIMIZE);
		isMaximize = hasFacet(IKeyword.MAXIMIZE);
		final String ag = getLiteral(IKeyword.AGGREGATION);
		combination = IKeyword.MAX.equals(ag) ? C_MAX : IKeyword.MIN.equals(ag) ? C_MIN : C_MEAN;
		bestFitness = isMaximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
	}

	@Override
	public void run(final IScope scope) {
		try {
			findBestSolution(scope);
		} catch (final GamaRuntimeException e) {
			GAMA.reportError(scope, e, false);
		}
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {}

	@Override
	public void addParametersTo(final List<IParameter.Batch> params, final BatchAgent agent) {

		params.add(new ParameterAdapter("Parameter space", BatchAgent.CALIBRATION_EXPERIMENT, "", IType.STRING) {

			@Override
			public String value() {
				BatchAgent batch = getCurrentExperiment();
				if (batch == null) return "-";
				final Map<String, IParameter.Batch> explorable = batch.getSpecies().getExplorableParameters();
				if (explorable.isEmpty()) return "1";
				String result = "";
				int dim = 1;
				for (final Map.Entry<String, IParameter.Batch> entry : explorable.entrySet()) {
					result += entry.getKey() + " (";
					final int entryDim = getExplorationDimension(entry.getValue());
					dim = dim * entryDim;
					result += String.valueOf(entryDim) + ") * ";
				}
				result = result.substring(0, result.length() - 2);
				result += " = " + dim;
				return result;
			}

			int getExplorationDimension(final IParameter.Batch p) {
				BatchAgent batch = getCurrentExperiment();
				IScope scope = batch == null ? GAMA.getRuntimeScope() : batch.getScope();

				// AD TODO Issue a warning in the compilation if a batch experiment tries to explore non-int or
				// non-float values
				if (p.getAmongValue(scope) != null) return p.getAmongValue(scope).size();
				return (int) ((asFloat(scope, p.getMaxValue(scope)) - asFloat(scope, p.getMinValue(scope)))
						/ asFloat(scope, p.getStepValue(scope))) + 1;
			}

		});

		params.add(
				new ParameterAdapter("Last parameter set tested", BatchAgent.CALIBRATION_EXPERIMENT, "", IType.STRING) {

					@Override
					public String value() {
						BatchAgent batch = getCurrentExperiment();
						if (batch == null || batch.getLatestSolution() == null) return "-";
						return batch.getLatestSolution().toString();
					}

				});

		params.add(new ParameterAdapter("Calibration method", BatchAgent.CALIBRATION_EXPERIMENT, IType.STRING) {

			@Override
			public Object value() {
				@SuppressWarnings ("rawtypes") final List<Class> classes = Arrays.asList(CLASSES);
				final String methodName = IKeyword.METHODS[classes.indexOf(AOptimizationAlgorithm.this.getClass())];
				final String fit = fitnessExpression == null ? "" : "fitness = "
						+ (isMaximize ? " maximize " : " minimize ") + fitnessExpression.serializeToGaml(false);
				final String sim = fitnessExpression == null ? ""
						: (combination == C_MAX ? " max " : combination == C_MIN ? " min " : " average ") + "of "
								+ agent.getSeeds().length + " simulations";
				return "Method " + methodName + " | " + fit + " | " + "compute the" + sim + " for each solution";
			}

		});

		params.add(
				new ParameterAdapter("Best parameter set found", BatchAgent.CALIBRATION_EXPERIMENT, "", IType.STRING) {

					@Override
					public String value() {
						final ParametersSet solutions = bestSolution;
						if (solutions == null) return "";
						return solutions.toString();
					}

				});

		params.add(new ParameterAdapter("Best fitness", BatchAgent.CALIBRATION_EXPERIMENT, "", IType.STRING) {

			@Override
			public String value() {
				final Double best = bestFitness;
				if (best == null) return "-";
				return best.toString();
			}

		});

	}

	@Override
	public boolean isFitnessBased() { return true; }

	@Override
	public IExpression getOutputs() { return getFitnessExpression(); }

	// ------------
	// OPTIMIZATION

	/**
	 * Return the best fitness of the experiment
	 *
	 * @return Double
	 */
	public Double getBestFitness() { return bestFitness; }

	/**
	 * Return the expression that characterizes the fitness computation
	 *
	 * @return IExpression
	 */
	public IExpression getFitnessExpression() { return fitnessExpression; }

	/**
	 * Return the set of parameter @ParametersSet attached to the best fitness
	 *
	 * @return ParametersSet
	 */
	public ParametersSet getBestSolution() { return bestSolution; }

	/**
	 * If the fitness should maximize (or minimize) the corresponding value
	 *
	 * @return boolean
	 */
	public boolean getIsMaximize() { return this.isMaximize; }

	/**
	 * Returns the way to combine replication fitness (either min, max or mean)
	 *
	 * @return short
	 */
	public short getCombination() { return combination; }

	/**
	 * Checks if is maximize.
	 *
	 * @return true, if is maximize
	 */
	public boolean isMaximize() { return isMaximize; }

	/**
	 * Gets the combination name.
	 *
	 * @return the combination name
	 */
	public String getCombinationName() { return COMBINATIONS[combination]; }

	/**
	 * Sets the best solution.
	 *
	 * @param bestSolution
	 *            the new best solution
	 */
	protected void setBestSolution(final ParametersSet bestSolution) {
		this.bestSolution = new ParametersSet(bestSolution);
	}

	/**
	 * Sets the best fitness.
	 *
	 * @param bestFitness
	 *            the new best fitness
	 */
	protected void setBestFitness(final Double bestFitness) { this.bestFitness = bestFitness; }

	/**
	 * Update best fitness.
	 *
	 * @param solution
	 *            the solution
	 * @param fitness
	 *            the fitness
	 */
	public void updateBestFitness(final ParametersSet solution, final Double fitness) {
		if (fitness == null) return;
		Double best = getBestFitness();
		if (bestSolution == null || (isMaximize() ? fitness > best : fitness < best)) {
			setBestFitness(fitness);
			setBestSolution(solution);
		}
	}

	/**
	 * Gets the current experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the current experiment
	 * @date 1 sept. 2023
	 */
	protected BatchAgent getCurrentExperiment() {
		IExperimentPlan plan = GAMA.getExperiment();
		if (plan == null) return null; // can happen when closing
		IExperimentAgent agent = plan.getAgent();
		if (agent instanceof BatchAgent batch) return batch;
		return null;
	}

	/**
	 * Gets the first fitness.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param results
	 *            the results
	 * @return the first fitness
	 * @date 1 sept. 2023
	 */
	protected Double getFirstFitness(final Map<String, List<Object>> results) {
		List<Object> objects = results.get(IKeyword.FITNESS);
		if (objects == null || objects.isEmpty()) return 0d;
		Object o = objects.get(0);
		return o instanceof Double d ? d : 0d;
	}

	// protected void setCurrentExperiment(BatchAgent currentExperiment) {
	// this.currentExperiment = currentExperiment;
	// }
}
