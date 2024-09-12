/*******************************************************************************************************
 *
 * SimulatedAnnealing.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.optimization;

import java.util.Collections;
import java.util.List;

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
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

/**
 * The Class SimulatedAnnealing.
 */
@symbol (
		name = IKeyword.ANNEALING,
		kind = ISymbolKind.BATCH_METHOD,
		with_sequence = false,
		concept = { IConcept.BATCH })
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
						name = SimulatedAnnealing.TEMP_END,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("final temperature")),
				@facet (
						name = HillClimbing.INIT_SOL,
						type = IType.MAP,
						optional = true,
						doc = @doc ("init solution: key: name of the variable, value: value of the variable")),
				@facet (
						name = SimulatedAnnealing.TEMP_DECREASE,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("temperature decrease coefficient. At each iteration, the current temperature is multiplied by this coefficient.")),
				@facet (
						name = SimulatedAnnealing.TEMP_INIT,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("initial temperature")),
				@facet (
						name = SimulatedAnnealing.NB_ITER,
						type = IType.INT,
						optional = true,
						doc = @doc ("number of iterations per level of temperature")),
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
						values = { IKeyword.MIN, IKeyword.MAX },
						doc = @doc ("the agregation method")) },
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm is an implementation of the Simulated Annealing algorithm. See the wikipedia article and [batch161 the batch dedicated page].",
		usages = { @usage (
				value = "As other batch methods, the basic syntax of the annealing statement uses `method annealing` instead of the expected `annealing name: id` : ",
				examples = { @example (
						value = "method annealing [facet: value];",
						isExecutable = false) }),
				@usage (
						value = "For example: ",
						examples = { @example (
								value = "method annealing temp_init: 100  temp_end: 1 temp_decrease: 0.5 nb_iter_cst_temp: 5 maximize: food_gathered;",
								isExecutable = false) }) })
public class SimulatedAnnealing extends ALocalSearchAlgorithm {

	/** The temperature end. */
	double temperatureEnd = 1;

	/** The temp dim coeff. */
	double tempDimCoeff = 0.5;

	/** The temperature init. */
	double temperatureInit = 100;

	/** The nb iter cst temp. */
	int nbIterCstTemp = 5;

	/** The Constant TEMP_END. */
	protected static final String TEMP_END = "temp_end";

	/** The Constant TEMP_DECREASE. */
	protected static final String TEMP_DECREASE = "temp_decrease";

	/** The Constant TEMP_INIT. */
	protected static final String TEMP_INIT = "temp_init";

	/** The Constant NB_ITER. */
	protected static final String NB_ITER = "nb_iter_cst_temp";

	/**
	 * Instantiates a new simulated annealing.
	 *
	 * @param species
	 *            the species
	 */
	public SimulatedAnnealing(final IDescription species) {
		super(species);
		initParams();
	}

	// @Override
	// public void initializeFor(final IScope scope, final BatchAgent agent) throws GamaRuntimeException {
	// super.initializeFor(scope, agent);
	// }

	// FIXME SimulationScope is normally null at that point. Should be better called from initializeFor()
	@Override
	public void initParams(final IScope scope) {
		final IExpression tempend = getFacet(TEMP_END);
		if (tempend != null) { temperatureEnd = Cast.asFloat(scope, tempend.value(scope)); }
		final IExpression tempdecrease = getFacet(TEMP_DECREASE);
		if (tempdecrease != null) { tempDimCoeff = Cast.asFloat(scope, tempdecrease.value(scope)); }
		final IExpression tempinit = getFacet(TEMP_INIT);
		if (tempinit != null) { temperatureInit = Cast.asFloat(scope, tempinit.value(scope)); }

		final IExpression nbIterCstT = getFacet(NB_ITER);
		if (nbIterCstT != null) { nbIterCstTemp = Cast.asInt(scope, nbIterCstT.value(scope)); }
	}

	@Override
	public ParametersSet findBestSolution(final IScope scope) throws GamaRuntimeException {
		initializeTestedSolutions();
		setBestSolution(new ParametersSet(this.solutionInit));
		BatchAgent batch = getCurrentExperiment();
		if (batch == null) return getBestSolution();
		double currentFitness = getFirstFitness(batch.launchSimulationsWithSingleParametersSet(getBestSolution()));
		ParametersSet bestSolutionAlgo = this.solutionInit;
		testedSolutions.put(getBestSolution(), getBestFitness());
		setBestFitness(currentFitness);
		double temperature = temperatureInit;

		while (temperature > temperatureEnd) {
			final List<ParametersSet> neighbors = neighborhood.neighbor(scope, bestSolutionAlgo);
			if (neighbors.isEmpty()) { break; }
			int iter = 0;
			while (iter < nbIterCstTemp) {
				final ParametersSet neighborSol = neighbors.get(scope.getRandom().between(0, neighbors.size() - 1));
				if (neighborSol == null) {
					neighbors.removeAll(Collections.singleton(null));
					if (neighbors.isEmpty()) { break; }
					continue;
				}
				Double neighborFitness = testedSolutions.get(neighborSol);
				if (neighborFitness == null || neighborFitness == Double.MAX_VALUE) {
					neighborFitness = getFirstFitness(batch.launchSimulationsWithSingleParametersSet(neighborSol));
					testedSolutions.put(neighborSol, neighborFitness);
				}

				if (isMaximize()) {
					if (neighborFitness >= currentFitness || scope.getRandom().next() < Math
							.exp(Math.abs(neighborFitness - currentFitness) / temperature)) {
						bestSolutionAlgo = neighborSol;
						currentFitness = neighborFitness;
					}

				} else if (neighborFitness <= currentFitness || scope.getRandom().next() < Math
						.exp(Math.abs(currentFitness - neighborFitness) / temperature)) {
					bestSolutionAlgo = neighborSol;
					currentFitness = neighborFitness;

				}
				iter++;
			}
			temperature *= tempDimCoeff;
		}

		return getBestSolution();
	}

	@Override
	public void addParametersTo(final List<IParameter.Batch> params, final BatchAgent agent) {
		super.addParametersTo(params, agent);
		params.add(new ParameterAdapter("Final temperature", BatchAgent.CALIBRATION_EXPERIMENT, IType.FLOAT) {

			@Override
			public Object value() {
				return temperatureEnd;
			}

		});
		params.add(new ParameterAdapter("Initial temperature", BatchAgent.CALIBRATION_EXPERIMENT, IType.FLOAT) {

			@Override
			public Object value() {
				return temperatureInit;
			}

		});
		params.add(new ParameterAdapter("Coefficient of diminution", BatchAgent.CALIBRATION_EXPERIMENT, IType.FLOAT) {

			@Override
			public Object value() {
				return tempDimCoeff;
			}

		});
		params.add(new ParameterAdapter("Number of iterations at constant temperature",
				BatchAgent.CALIBRATION_EXPERIMENT, IType.INT) {

			@Override
			public Object value() {
				return nbIterCstTemp;
			}

		});
	}

}
