/*******************************************************************************************************
 *
 * HillClimbing.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.core.experiment.batch.optimization;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExploration;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.core.experiment.batch.BatchAgent;
import gama.core.experiment.batch.StoppingCriterion;
import gama.core.experiment.batch.StoppingCriterionMaxIt;
import gama.core.experiment.parameters.ParameterAdapter;
import gama.core.experiment.parameters.ParametersSet;

/**
 * The Class HillClimbing.
 */
@symbol (
		name = IExploration.HILL_CLIMBING,
		kind = ISymbolKind.BATCH_METHOD,
		with_sequence = false,
		concept = { IConcept.BATCH, IConcept.ALGORITHM })
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				internal = true,
				doc = @doc ("The name of the method. For internal use only")),
				@facet (
						name = HillClimbing.ITER_MAX,
						type = IType.INT,
						optional = true,
						doc = @doc ("number of iterations. this number corresponds to the number of \"moves\" in the parameter space. For each move, the algorithm will test the whole neighborhood of the current solution, each neighbor corresponding to a particular set of parameters and thus to a run. Thus, there can be several runs per iteration (maximum: 2^(number of parameters)).")),
				@facet (
						name = HillClimbing.INIT_SOL,
						type = IType.MAP,
						optional = true,
						doc = @doc ("init solution: key: name of the variable, value: value of the variable")),
				@facet (
						name = IKeyword.MAXIMIZE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the value the algorithm tries to maximize")),
				@facet (
						name = IKeyword.MINIMIZE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the value the algorithm tries to minimize")),
				@facet (
						name = IKeyword.AGGREGATION,
						type = IType.LABEL,
						optional = true,
						values = { IKeyword.MIN, IKeyword.MAX, "avr" },
						doc = @doc ("the agregation method")) },
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm is an implementation of the Hill Climbing algorithm. See the wikipedia article and [batch161 the batch dedicated page].",
		usages = { @usage (
				value = "As other batch methods, the basic syntax of the `hill_climbing` statement uses `method hill_climbing` instead of the expected `hill_climbing name: id` : ",
				examples = { @example (
						value = "method hill_climbing [facet: value];",
						isExecutable = false) }),
				@usage (
						value = "For example: ",
						examples = { @example (
								value = "method hill_climbing iter_max: 50 maximize : food_gathered; ",
								isExecutable = false) }) })
public class HillClimbing extends ALocalSearchAlgorithm {

	/** The Constant ITER_MAX. */
	protected static final String ITER_MAX = "iter_max";

	/** The stopping criterion. */
	StoppingCriterion stoppingCriterion = null;

	/** The max it. */
	int maxIt;

	/**
	 * Instantiates a new hill climbing.
	 *
	 * @param species
	 *            the species
	 */
	public HillClimbing(final IDescription species) {
		super(species);
		initParams();

	}

	/**
	 * Keep sol.
	 *
	 * @param neighborSol
	 *            the neighbor sol
	 * @param neighborFitness
	 *            the neighbor fitness
	 * @return true, if successful
	 */
	public boolean keepSol(final ParametersSet neighborSol, final Double neighborFitness) {
		if (isMaximize() && neighborFitness.doubleValue() > getBestFitness()
				|| !isMaximize() && neighborFitness.doubleValue() < getBestFitness()) {
			setBestFitness(neighborFitness);
			return true;
		}
		return false;
	}

	@Override
	public ParametersSet findBestSolution(final IScope scope) throws GamaRuntimeException {
		setBestSolution(this.solutionInit);
		BatchAgent batch = getCurrentExperiment();
		if (batch == null) return getBestSolution();
		double currentFitness = getFirstFitness(batch.launchSimulationsWithSingleParametersSet(getBestSolution()));
		initializeTestedSolutions();
		testedSolutions.put(getBestSolution(), currentFitness);
		int nbIt = 0;

		final Map<String, Object> endingCritParams = new Hashtable<>();
		endingCritParams.put("Iteration", Integer.valueOf(nbIt));
		while (stoppingCriterion == null || !stoppingCriterion.stopSearchProcess(endingCritParams)) {
			final List<ParametersSet> neighbors = neighborhood.neighbor(scope, getBestSolution());
			if (neighbors.isEmpty()) { break; }
			setBestFitness(currentFitness);
			ParametersSet bestNeighbor = null;
			if (GamaExecutorService.shouldRunAllSimulationsInParallel(batch)
					&& !batch.getParametersToExplore().isEmpty()) {
				Map<ParametersSet, Double> result = testSolutions(neighbors);
				if (result.containsKey(bestSolution)) { bestNeighbor = bestSolution; }
			} else {
				for (final ParametersSet neighborSol : neighbors) {
					if (neighborSol == null) { continue; }
					Double neighborFitness = testedSolutions.get(neighborSol);
					if (neighborFitness == null) {
						neighborFitness = getFirstFitness(batch.launchSimulationsWithSingleParametersSet(neighborSol));
					}
					testedSolutions.put(neighborSol, neighborFitness);
					// TODO: if the goal of this for loop was only to find the best neighbor, we should break here
					// also TODO: comment this code please
					if (neighborSol.equals(bestSolution)) { bestNeighbor = neighborSol; }

				}
			}

			if (bestNeighbor == null) { break; }
			setBestSolution(bestNeighbor);
			currentFitness = getBestFitness();
			nbIt++;
			endingCritParams.put("Iteration", Integer.valueOf(nbIt));
		}
		return getBestSolution();
	}

	@Override
	protected void initParams(final IScope scope) {
		final IExpression maxItExp = getFacet(ITER_MAX);
		if (maxItExp != null) {
			maxIt = Cast.asInt(scope, maxItExp.value(scope));
			stoppingCriterion = new StoppingCriterionMaxIt(maxIt);
		}
	}

	@Override
	public void addParametersTo(final List<IParameter.Batch> params, final IExperimentAgent.Batch agent) {
		super.addParametersTo(params, agent);
		params.add(new ParameterAdapter("Maximum number of iterations", BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return maxIt;
			}

		});
	}

}
