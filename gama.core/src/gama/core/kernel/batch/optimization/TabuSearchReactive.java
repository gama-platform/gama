/*******************************************************************************************************
 *
 * TabuSearchReactive.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.core.kernel.batch.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.batch.StoppingCriterion;
import gama.core.kernel.batch.StoppingCriterionMaxIt;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

/**
 * The Class TabuSearchReactive.
 */
@symbol (
		name = IKeyword.REACTIVE_TABU,
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
				internal = true),
				@facet (
						name = TabuSearchReactive.ITER_MAX,
						type = IType.INT,
						optional = true,
						doc = @doc ("number of iterations. this number corresponds to the number of \"moves\" in the parameter space. For each move, the algorithm will test the whole neighborhood of the current solution, each neighbor corresponding to a particular set of parameters and thus to a run. Thus, there can be several runs per iteration (maximum: 2^(number of parameters)).")),
				@facet (
						name = HillClimbing.INIT_SOL,
						type = IType.MAP,
						optional = true,
						doc = @doc ("init solution: key: name of the variable, value: value of the variable")),
				@facet (
						name = TabuSearchReactive.LIST_SIZE_INIT,
						type = IType.INT,
						optional = true,
						doc = @doc ("initial size of the tabu list")),
				@facet (
						name = TabuSearchReactive.LIST_SIZE_MAX,
						type = IType.INT,
						optional = true,
						doc = @doc ("maximal size of the tabu list")),
				@facet (
						name = TabuSearchReactive.LIST_SIZE_MIN,
						type = IType.INT,
						optional = true,
						doc = @doc ("minimal size of the tabu list")),
				@facet (
						name = TabuSearchReactive.NB_TESTS_MAX,
						type = IType.INT,
						optional = true,
						doc = @doc ("number of movements without collision before shortening the tabu list")),
				@facet (
						name = TabuSearchReactive.CYCLE_SIZE_MAX,
						type = IType.INT,
						optional = true,
						doc = @doc ("minimal size of the considered cycles")),
				@facet (
						name = TabuSearchReactive.CYCLE_SIZE_MIN,
						type = IType.INT,
						optional = true,
						doc = @doc ("maximal size of the considered cycles")),
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
		value = "This algorithm is a simple implementation of the Reactive Tabu Search algorithm ((Battiti et al., 1993)). This Reactive Tabu Search is an enhance version of the Tabu search. It adds two new elements to the classic Tabu Search. The first one concerns the size of the tabu list: in the Reactive Tabu Search, this one is not constant anymore but it dynamically evolves according to the context. Thus, when the exploration process visits too often the same solutions, the tabu list is extended in order to favor the diversification of the search process. On the other hand, when the process has not visited an already known solution for a high number of iterations, the tabu list is shortened in order to favor the intensification of the search process. The second new element concerns the adding of cycle detection capacities. Thus, when a cycle is detected, the process applies random movements in order to break the cycle. See the batch dedicated page.",
		usages = { @usage (
				value = "As other batch methods, the basic syntax of the reactive_tabu statement uses `method reactive_tabu` instead of the expected `reactive_tabu name: id` : ",
				examples = { @example (
						value = "method reactive_tabu [facet: value];",
						isExecutable = false) }),
				@usage (
						value = "For example: ",
						examples = { @example (
								value = "method reactive_tabu iter_max: 50 tabu_list_size_init: 5 tabu_list_size_min: 2 tabu_list_size_max: 10 nb_tests_wthout_col_max: 20 cycle_size_min: 2 cycle_size_max: 20 maximize: food_gathered;",
								isExecutable = false) }) })
public class TabuSearchReactive extends ALocalSearchAlgorithm {

	/** The tabu list size init. */
	int tabuListSizeInit = 5;

	/** The tabu list size max. */
	int tabuListSizeMax = 2;

	/** The tabu list size min. */
	int tabuListSizeMin = 10;

	/** The nb test without collision max. */
	int nbTestWithoutCollisionMax = 20;

	/** The cycle size max. */
	int cycleSizeMax = 20;

	/** The cycle size min. */
	int cycleSizeMin = 2;

	/** The stopping criterion. */
	StoppingCriterion stoppingCriterion = new StoppingCriterionMaxIt(100);

	/** The Constant ITER_MAX. */
	protected static final String ITER_MAX = "iter_max";

	/** The Constant LIST_SIZE_INIT. */
	protected static final String LIST_SIZE_INIT = "tabu_list_size_init";

	/** The Constant LIST_SIZE_MAX. */
	protected static final String LIST_SIZE_MAX = "tabu_list_size_max";

	/** The Constant LIST_SIZE_MIN. */
	protected static final String LIST_SIZE_MIN = "tabu_list_size_min";

	/** The Constant NB_TESTS_MAX. */
	protected static final String NB_TESTS_MAX = "nb_tests_wthout_col_max";

	/** The Constant CYCLE_SIZE_MAX. */
	protected static final String CYCLE_SIZE_MAX = "cycle_size_max";

	/** The Constant CYCLE_SIZE_MIN. */
	protected static final String CYCLE_SIZE_MIN = "cycle_size_min";

	/**
	 * Instantiates a new tabu search reactive.
	 *
	 * @param species
	 *            the species
	 */
	public TabuSearchReactive(final IDescription species) {
		super(species);
		initParams();

	}

	/** The iter max. */
	int iterMax = 100;

	// @Override
	// public void initializeFor(final IScope scope, final BatchAgent agent) throws GamaRuntimeException {
	// super.initializeFor(scope, agent);
	// }

	@Override
	public void initParams(final IScope scope) {
		final IExpression maxIt = getFacet(ITER_MAX);
		if (maxIt != null) {
			iterMax = Cast.asInt(scope, maxIt.value(scope));
			stoppingCriterion = new StoppingCriterionMaxIt(iterMax);
		}
		final IExpression listSizeInit = getFacet(LIST_SIZE_INIT);
		if (listSizeInit != null) { tabuListSizeInit = Cast.asInt(scope, listSizeInit.value(scope)); }
		final IExpression listSizeMax = getFacet(LIST_SIZE_MAX);
		if (listSizeMax != null) { tabuListSizeMax = Cast.asInt(scope, listSizeMax.value(scope)); }
		final IExpression listSizeMin = getFacet(LIST_SIZE_MIN);
		if (listSizeMin != null) { tabuListSizeMin = Cast.asInt(scope, listSizeMin.value(scope)); }
		final IExpression nbTestWtoutColMax = getFacet(NB_TESTS_MAX);
		if (nbTestWtoutColMax != null) {
			nbTestWithoutCollisionMax = Cast.asInt(scope, nbTestWtoutColMax.value(scope));
		}
		final IExpression cycleMax = getFacet(CYCLE_SIZE_MAX);
		if (cycleMax != null) { cycleSizeMax = Cast.asInt(scope, cycleMax.value(scope)); }
		final IExpression cycleMin = getFacet(CYCLE_SIZE_MIN);
		if (cycleMin != null) { cycleSizeMin = Cast.asInt(scope, cycleMin.value(scope)); }
	}

	/**
	 * Keep sol.
	 *
	 * @param neighborSol
	 *            the neighbor sol
	 * @param neighborFitness
	 *            the neighbor fitness
	 * @param bestFitnessAlgo
	 *            the best fitness algo
	 * @return true, if successful
	 */
	public boolean keepSol(final ParametersSet neighborSol, final Double neighborFitness,
			final Double bestFitnessAlgo) {
		if (isMaximize() && neighborFitness > bestFitnessAlgo || !isMaximize() && neighborFitness < bestFitnessAlgo) // setBestFitness(neighborFitness);
			return true;
		return false;
	}

	@Override
	public ParametersSet findBestSolution(final IScope scope) throws GamaRuntimeException {
		initializeTestedSolutions();

		int tabuListSize = tabuListSizeInit;
		ParametersSet bestSolutionAlgo = this.solutionInit;
		BatchAgent batch = getCurrentExperiment();
		if (batch == null) return solutionInit;
		final List<ParametersSet> tabuList = new ArrayList<>();
		tabuList.add(bestSolutionAlgo);
		double currentFitness = getFirstFitness(batch.launchSimulationsWithSingleParametersSet(bestSolutionAlgo));
		testedSolutions.put(bestSolutionAlgo, currentFitness);

		setBestSolution(new ParametersSet(bestSolutionAlgo));
		setBestFitness(currentFitness);

		testedSolutions.put(getBestSolution(), currentFitness);
		int nbIt = 0;
		int nbTestWithoutCollision = 0;
		int currentCycleSize = 0;
		int cycleSize = 0;
		ParametersSet startingCycle = null;
		final Map<String, Object> endingCritParams = new HashMap<>();
		endingCritParams.put("Iteration", Integer.valueOf(nbIt));
		while (!stoppingCriterion.stopSearchProcess(endingCritParams)) {
			nbTestWithoutCollision++;
			final List<ParametersSet> neighbors = neighborhood.neighbor(scope, getBestSolution());
			neighbors.removeAll(tabuList);
			if (neighbors.isEmpty()) { break; }
			double bestFitnessAlgo;

			if (isMaximize()) {
				bestFitnessAlgo = Double.NEGATIVE_INFINITY;
			} else {
				bestFitnessAlgo = Double.POSITIVE_INFINITY;
			}
			ParametersSet bestNeighbor = null;

			if (GamaExecutorService.shouldRunAllSimulationsInParallel(batch)
					&& !batch.getParametersToExplore().isEmpty()) {
				Map<ParametersSet, Double> result = testSolutions(neighbors);
				for (ParametersSet p : result.keySet()) {
					if (keepSol(p, result.get(p), bestFitnessAlgo)) {
						bestNeighbor = p;
						if (testedSolutions.containsKey(p)) {
							nbTestWithoutCollision = 0;
							if (tabuListSize < tabuListSizeMax) { tabuListSize++; }
						}
					}
				}
			} else {
				for (final ParametersSet neighborSol : neighbors) {
					if (neighborSol == null) { continue; }
					if (testedSolutions.containsKey(neighborSol)) {
						nbTestWithoutCollision = 0;
						if (tabuListSize < tabuListSizeMax) { tabuListSize++; }
					}
					Double neighborFitness = testedSolutions.get(neighborSol);
					if (neighborFitness == null || neighborFitness == Double.MAX_VALUE) {
						neighborFitness = getFirstFitness(batch.launchSimulationsWithSingleParametersSet(neighborSol));
					}
					testedSolutions.put(neighborSol, neighborFitness);
					if (keepSol(neighborSol, neighborFitness, bestFitnessAlgo)) { bestNeighbor = neighborSol; }
				}
			}

			if (bestNeighbor == null) { break; }
			if (this.testedSolutions.containsKey(bestNeighbor)) {
				currentCycleSize++;
			} else {
				startingCycle = null;
				currentCycleSize = 0;
				cycleSize = 0;
			}
			if (currentCycleSize == cycleSizeMin) {
				startingCycle = bestNeighbor;
			} else if (currentCycleSize > cycleSizeMin && currentCycleSize <= cycleSizeMax) {
				if (startingCycle != null && !startingCycle.equals(bestNeighbor)) {
					cycleSize++;
				} else {
					final int depl = (int) (1 + scope.getRandom().next() * cycleSize / 2.0);
					for (int i = 0; i < depl; i++) {
						final List<ParametersSet> neighborsAlea = neighborhood.neighbor(scope, bestSolutionAlgo);
						neighborsAlea.removeAll(tabuList);
						if (neighborsAlea.isEmpty()) { break; }
						bestSolutionAlgo = neighborsAlea.get(scope.getRandom().between(0, neighborsAlea.size() - 1));
						if (tabuList.size() == tabuListSize) { tabuList.remove(0); }
						tabuList.add(bestSolutionAlgo);
					}
					currentFitness = getFirstFitness(batch.launchSimulationsWithSingleParametersSet(bestSolutionAlgo));
					testedSolutions.put(bestSolutionAlgo, currentFitness);
					if (nbIt > iterMax) { break; }
				}
			}
			bestSolutionAlgo = bestNeighbor;
			tabuList.add(bestSolutionAlgo);
			if (tabuList.size() > tabuListSize) { tabuList.remove(0); }
			if (nbTestWithoutCollision == nbTestWithoutCollisionMax) {
				nbTestWithoutCollision = 0;
				if (tabuListSize > this.tabuListSizeMin) { tabuListSize--; }
			}

			nbIt++;
			endingCritParams.put("Iteration", Integer.valueOf(nbIt));
		}

		return getBestSolution();
	}

	@Override
	public void addParametersTo(final List<IParameter.Batch> params, final BatchAgent agent) {
		super.addParametersTo(params, agent);
		params.add(new ParameterAdapter("Tabu list initial size", BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return tabuListSizeInit;
			}

		});
		params.add(new ParameterAdapter("Tabu list maximum size", BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return tabuListSizeMax;
			}

		});
		params.add(new ParameterAdapter("Tabu list minimum size", BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return tabuListSizeMin;
			}

		});
		params.add(new ParameterAdapter("Maximum number of tests without collision", BatchAgent.CALIBRATION_EXPERIMENT,
				IType.INT) {

			@Override
			public Object value() {
				return nbTestWithoutCollisionMax;
			}

		});
		params.add(new ParameterAdapter("Maximum cycle size", BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return cycleSizeMax;
			}

		});
		params.add(new ParameterAdapter("Minimum cycle size", BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return cycleSizeMin;
			}

		});
		params.add(
				new ParameterAdapter("Maximum number of iterations", BatchAgent.CALIBRATION_EXPERIMENT, IType.FLOAT) {

					@Override
					public Object value() {
						return iterMax;
					}

				});
	}

}
