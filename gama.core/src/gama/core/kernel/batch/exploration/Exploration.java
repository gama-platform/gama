/*******************************************************************************************************
 *
 * Exploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.batch.exploration.sampling.LatinhypercubeSampling;
import gama.core.kernel.batch.exploration.sampling.MorrisSampling;
import gama.core.kernel.batch.exploration.sampling.OrthogonalSampling;
import gama.core.kernel.batch.exploration.sampling.RandomSampling;
import gama.core.kernel.batch.exploration.sampling.SaltelliSampling;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaDate;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.GamaDateType;
import gama.gaml.types.GamaFloatType;
import gama.gaml.types.GamaPointType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class ExhaustiveSearch.
 */
@symbol (
		name = { IKeyword.EXPLORATION },
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
						name = Exploration.METHODS,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The name of the sampling method (among saltelli/morris/latinhypercube/orthogonal/uniform/factorial)")),
				@facet (
						name = IKeyword.FROM,
						type = IType.STRING,
						optional = true,
						doc = @doc ("a path to a file where each lines correspond to one parameter set and each colon a parameter")),
				@facet (
						name = IKeyword.WITH,
						type = IType.LIST,
						of = IType.MAP,
						optional = true,
						doc = @doc ("the list of parameter sets to explore; a parameter set is defined by a map: key: name of the variable, value: expression for the value of the variable")),
				@facet (
						name = Exploration.SAMPLE_SIZE,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of sample required, 132 by default")),
				@facet (
						name = Exploration.SAMPLE_FACTORIAL,
						type = IType.LIST,
						of = IType.INT,
						optional = true,
						doc = @doc ("The number of sample required.")),
				@facet (
						name = Exploration.NB_LEVELS,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of levels for morris sampling, 4 by default")),
				@facet (
						name = Exploration.ITERATIONS,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of iteration for orthogonal sampling, 5 by default"))

		},
		omissible = IKeyword.NAME)
@doc (
		value = "This is the standard batch method. The exploration mode is defined by default when there is no method element present in the batch section. It explores all the combination of parameter values in a sequential way. You can also choose a sampling method for the exploration. See [batch161 the batch dedicated page].",
		usages = { @usage (
				value = "As other batch methods, the basic syntax of the exploration statement uses `method exploration` instead of the expected `exploration name: id` : ",
				examples = { @example (
						value = "method exploration;",
						isExecutable = false) }),
				@usage (
						value = "Simplest example: ",
						examples = { @example (
								value = "method exploration;",
								isExecutable = false) }),
				@usage (
						value = "Using sampling facet: ",
						examples = { @example (
								value = "method exploration sampling:latinhypercube sample:100; ",
								isExecutable = false) }),
				@usage (
						value = "Using from facet: ",
						examples = { @example (
								value = "method exploration from:\"../path/to/my/exploration/plan.csv\"; ",
								isExecutable = false) }),
				@usage (
						value = "Using with facet: ",
						examples = { @example (
								value = "method exploration with:[[\"a\"::0.5, \"b\"::10],[\"a\"::0.1, \"b\"::100]]; ",
								isExecutable = false) }) })
public class Exploration extends AExplorationAlgorithm {
	/** The Constant Method */
	public static final String METHODS = "sampling";

	/** The Constant SAMPLE_SIZE */
	public static final String SAMPLE_SIZE = "sample";

	/** The factorial sampling */
	public static final String SAMPLE_FACTORIAL = "factorial";

	/** The Constant NB_LEVELS */
	public static final String NB_LEVELS = "levels";

	/** The Constant ITERATIONS */
	public static final String ITERATIONS = "iterations";

	/** The Constant FROM_FILE. */
	public static final String FROM_FILE = "FROMFILE";

	/** The Constant FROM_LIST. */
	public static final String FROM_LIST = "FROMLIST";

	/** The default step factor. */
	private final int __default_step_factor = 10;

	/** The sample size. */
	private int sample_size = 132;

	/** The nb levels. */
	private int nb_levels = 4;

	/** The iterations. */
	private int iterations = 5;

	/** The parameters. */
	private List<Batch> parameters;

	/**
	 * Instantiates a new exhaustive search.
	 *
	 * @param desc
	 *            the desc
	 */
	public Exploration(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	@Override
	public void explore(final IScope scope) throws GamaRuntimeException {

		List<Batch> params = currentExperiment.getParametersToExplore();

		parameters = parameters == null ? params : parameters;
		List<ParametersSet> sets;

		if (hasFacet(Exploration.SAMPLE_SIZE)) {
			this.sample_size = Cast.asInt(scope, getFacet(SAMPLE_SIZE).value(scope));
		}

		if (hasFacet(Exploration.ITERATIONS)) {
			this.iterations = Cast.asInt(scope, getFacet(SAMPLE_SIZE).value(scope));
		}

		if (hasFacet(Exploration.NB_LEVELS)) { this.nb_levels = Cast.asInt(scope, getFacet(NB_LEVELS).value(scope)); }

		String method = hasFacet(Exploration.METHODS) ? Cast.asString(scope, getFacet(METHODS).value(scope)) : "";

		if (hasFacet(IKeyword.FROM)) {
			method = FROM_FILE;
		} else if (hasFacet(IKeyword.WITH)) { method = FROM_LIST; }

		sets = switch (method) {
			case IKeyword.MORRIS -> MorrisSampling.makeMorrisSamplingOnly(nb_levels, sample_size, parameters, scope);
			case IKeyword.SALTELLI -> SaltelliSampling.makeSaltelliSampling(scope, sample_size, parameters);
			case IKeyword.LHS -> LatinhypercubeSampling.latinHypercubeSamples(sample_size, parameters,
					scope.getRandom().getGenerator(), scope);
			case IKeyword.ORTHOGONAL -> OrthogonalSampling.orthogonalSamples(sample_size, iterations, parameters,
					scope.getRandom().getGenerator(), scope);
			case IKeyword.UNIFORM -> RandomSampling.uniformSampling(scope, sample_size, parameters);
			case IKeyword.FACTORIAL -> {
				List<ParametersSet> ps = null;
				if (hasFacet(Exploration.SAMPLE_FACTORIAL)) {
					@SuppressWarnings ("unchecked") int[] factors =
							Cast.asList(scope, getFacet(Exploration.SAMPLE_FACTORIAL).value(scope)).stream()
									.mapToInt(o -> Integer.parseInt(o.toString())).toArray();
					ps = RandomSampling.factorialUniformSampling(scope, factors, params);
				} else {
					ps = RandomSampling.factorialUniformSampling(scope, sample_size, params);
				}
				yield ps;
			}

			case FROM_LIST -> buildParameterFromMap(scope, new ArrayList<>(), 0);
			case FROM_FILE -> buildParametersFromCSV(scope, Cast.asString(scope, getFacet(IKeyword.FROM).value(scope)),
					new ArrayList<>());
			default -> buildParameterSets(scope, new ArrayList<>(), 0);
		};
		if (sets.isEmpty()) {
			sets.add(new ParametersSet());
		} else if (sample_size == 132) { sample_size = sets.size(); }

		if (GamaExecutorService.shouldRunAllSimulationsInParallel(currentExperiment)) {
			currentExperiment.launchSimulationsWithSolution(sets);
		} else {
			for (ParametersSet sol : sets) { currentExperiment.launchSimulationsWithSolution(sol); }
		}

	}

	@Override
	public List<ParametersSet> buildParameterSets(final IScope scope, final List<ParametersSet> sets, final int index) {

		if (sets == null) throw GamaRuntimeException.error("Cannot build a sample with empty parameter set", scope);
		final List<Batch> variables = currentExperiment.getParametersToExplore();
		List<ParametersSet> sets2 = new ArrayList<>();
		if (variables.isEmpty()) return sets2;
		if (sets.isEmpty()) { sets.add(new ParametersSet()); }
		final IParameter.Batch var = variables.get(index);
		for (ParametersSet solution : sets) {
			@SuppressWarnings ("rawtypes") List vals =
					var.getAmongValue(scope) != null ? var.getAmongValue(scope) : getParameterSwip(scope, var);
			for (final Object val : vals) {
				ParametersSet ps = new ParametersSet(solution);
				ps.put(var.getName(), val);
				sets2.add(ps);
			}
		}
		if (index == variables.size() - 1) return sets2;
		return buildParameterSets(scope, sets2, index + 1);
	}

	@Override
	public void addParametersTo(final List<Batch> exp, final BatchAgent agent) {
		super.addParametersTo(exp, agent);

		exp.add(new ParameterAdapter("Sampled points", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				return sample_size;
			}
		});

		exp.add(new ParameterAdapter("Sampling method", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				if (hasFacet(IKeyword.FROM)) return FROM_FILE;
				if (hasFacet(IKeyword.WITH)) return FROM_LIST;
				return hasFacet(Exploration.METHODS)
						? Cast.asString(agent.getScope(), getFacet(METHODS).value(agent.getScope())) : "exhaustive";
			}
		});

	}

	/**
	 * Build a parameter set (a sample of the parameter space) based on explicit point given either with a gaml map or
	 * written in a file
	 *
	 * @param scope
	 * @param sets
	 * @param index
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	private List<ParametersSet> buildParameterFromMap(final IScope scope, final List<ParametersSet> sets,
			final int index) {
		IExpression psexp = getFacet(IKeyword.WITH);
		if ( !Types.LIST.isAssignableFrom(psexp.getDenotedType())) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error(
					"The facet '" + IKeyword.WITH + "' must be provided with a list of maps", scope), true);
		}
		List<Map<String, Object>> parameterSets = Cast.asList(scope, psexp.value(scope));

		for (Map<String, Object> parameterSet : parameterSets) {
			ParametersSet p = new ParametersSet();
			for (String v : parameterSet.keySet()) {
				Object val = parameterSet.get(v);
				p.put(v, val instanceof IExpression ? ((IExpression) val).value(scope) : val);
			}
			sets.add(p);
		}
		return sets;

	}

	/**
	 * Create a List of Parameters Set with values contains in a CSV file
	 *
	 * @param scope
	 * @param path
	 * @param sets
	 * @return
	 */
	private List<ParametersSet> buildParametersFromCSV(final IScope scope, final String path,
			final List<ParametersSet> sets) throws GamaRuntimeException {
		List<Map<String, Object>> parameters = new ArrayList<>();
		try {
			File file = new File(path);
			try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
				String line = " ";
				String[] tempArr;
				List<String> list_name = new ArrayList<>();
				int i = 0;
				while ((line = br.readLine()) != null) {
					tempArr = line.split(",");
					for (String tempStr : tempArr) { if (i == 0) { list_name.add(tempStr); } }
					if (i > 0) {
						Map<String, Object> temp_map = new HashMap<>();
						for (int y = 0; y < tempArr.length; y++) { temp_map.put(list_name.get(y), tempArr[y]); }
						parameters.add(temp_map);
					}
					i++;
				}
			}
		} catch (FileNotFoundException nfe) {
			throw GamaRuntimeException.error("CSV file not found: " + path, scope);
		} catch (IOException ioe) {
			throw GamaRuntimeException.error("Error during the reading of the CSV file", scope);
		}

		for (Map<String, Object> parameterSet : parameters) {
			ParametersSet p = new ParametersSet();
			for (String v : parameterSet.keySet()) {
				Object val = parameterSet.get(v);
				p.put(v, val instanceof IExpression ? ((IExpression) val).value(scope) : val);
			}
			sets.add(p);
		}

		return sets;
	}

	// ##################### Methods to determine possible values based on exhaustive ######################

	/**
	 * Return all the possible value of a parameter based on
	 *
	 * @param scope
	 * @param var
	 * @return
	 */
	private List<Object> getParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();
		switch (var.getType().id()) {
			case IType.INT:
				int minValue = Cast.asInt(scope, var.getMinValue(scope));
				int maxValue = Cast.asInt(scope, var.getMaxValue(scope));
				double stepValue = 1;
				int nbIterNeeded = 0;
				if (var.getStepValue(scope) != null) {
					stepValue = Cast.asInt(scope, var.getStepValue(scope));
				} else if (maxValue - minValue > __default_step_factor) {
					stepValue = (maxValue - minValue) / (double)__default_step_factor;
				}
				//This means if we have min=0 max=4 and step=3, we will get [0, 3] in res
				nbIterNeeded = Math.abs((int)((maxValue - minValue) / stepValue));
				double start = stepValue >= 0 ? minValue : maxValue;
				for(int i = 0 ; i <= nbIterNeeded ; i++) {
					res.add(start + (int)(stepValue * i));
				}
				break;
			case IType.FLOAT:
				double minFloatValue = Cast.asFloat(scope, var.getMinValue(scope));
				double maxFloatValue = Cast.asFloat(scope, var.getMaxValue(scope));
				double stepFloatValue = 0.1;
				if (var.getStepValue(scope) != null) {
					stepFloatValue = Cast.asFloat(scope, var.getStepValue(scope));
				} else {
					stepFloatValue = (maxFloatValue - minFloatValue) / __default_step_factor;
				}

				while (minFloatValue <= maxFloatValue) {
					if (stepFloatValue >= 0) {
						res.add(minFloatValue);
						minFloatValue = minFloatValue + stepFloatValue;

					} else {
						res.add(maxFloatValue);
						maxFloatValue = maxFloatValue + stepFloatValue;
					}
				}
				break;
			case IType.DATE:
				GamaDate dateValue = GamaDateType.staticCast(scope, var.getMinValue(scope), null, false);
				GamaDate maxDateValue = GamaDateType.staticCast(scope, var.getMaxValue(scope), null, false);
				Double stepVal = Cast.asFloat(scope, var.getStepValue(scope));
				while (dateValue.isSmallerThan(maxDateValue, false)) {
					if (stepVal > 0) {
						res.add(dateValue);
						dateValue = dateValue.plus(stepVal, ChronoUnit.SECONDS);
					} else {
						res.add(maxDateValue);
						maxDateValue = maxDateValue.minus(Math.abs(stepVal.longValue()), ChronoUnit.SECONDS);
					}
				}
				break;
			case IType.POINT:
				GamaPoint pointValue = Cast.asPoint(scope, var.getMinValue(scope));
				GamaPoint maxPointValue = Cast.asPoint(scope, var.getMaxValue(scope));
				GamaPoint increment = null;
				Double stepV = null;

				if (var.getStepValue(scope) != null) {
					increment = GamaPointType.staticCast(scope, var.getStepValue(scope), true);

					if (increment == null) {
						double d = GamaFloatType.staticCast(scope, var.getStepValue(scope), null, false);
						stepV = d;
						increment = new GamaPoint(d, d, d);
					} else {
						stepV = (increment.x + increment.y + increment.z) / 3.0;

					}

				} else {
					increment = new GamaPoint((maxPointValue.x - pointValue.x) / 10.0,
							(maxPointValue.y - pointValue.y) / 10.0, (maxPointValue.z - pointValue.z) / 10.0);

				}
				while (pointValue.smallerThanOrEqualTo(maxPointValue)) {
					if (stepV == null || stepV > 0) {
						res.add(pointValue);
						pointValue = pointValue.plus(Cast.asPoint(scope, increment));
					} else {
						res.add(maxPointValue);
						maxPointValue = maxPointValue.plus(Cast.asPoint(scope, increment));
					}
				}
				break;
			default:
				double varValue = Cast.asFloat(scope, var.getMinValue(scope));
				double maxVarValue = Cast.asFloat(scope, var.getMaxValue(scope));
				double floatcrement = 1;
				if (hasFacet(IKeyword.STEP)) {
					floatcrement = Cast.asFloat(scope, var.getStepValue(scope));
				} else {
					floatcrement = (maxVarValue - varValue) / __default_step_factor;
				}
				while (varValue <= maxVarValue) {

					if (var.getType().id() == IType.INT) {
						if (floatcrement >= 0) {
							res.add((int) varValue);
						} else {
							res.add((int) maxVarValue);
						}

					} else if (var.getType().id() == IType.FLOAT) {
						if (floatcrement >= 0) {
							res.add(varValue);
						} else {
							res.add(maxVarValue);
						}
					} else {
						continue;
					}
					if (floatcrement >= 0) {
						varValue = varValue + floatcrement;
					} else {
						maxVarValue = maxVarValue + floatcrement;
					}
				}
		}
		return res;
	}

}
