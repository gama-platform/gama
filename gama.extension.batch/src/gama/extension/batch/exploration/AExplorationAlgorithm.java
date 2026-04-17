/*******************************************************************************************************
 *
 * AExplorationAlgorithm.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.batch.exploration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gama.annotations.inside;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.IParameter.Batch;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.GamaFloatType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExploration;
import gama.api.runtime.scope.IScope;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.StringUtils;
import gama.api.utils.files.FileUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.experiment.ExperimentAgent;
import gama.core.experiment.parameters.ParameterAdapter;
import gama.core.experiment.parameters.ParametersSet;
import gama.extension.batch.BatchAgent;
import gama.extension.batch.optimization.GeneticAlgorithm;
import gama.extension.batch.optimization.HillClimbing;
import gama.extension.batch.optimization.SimulatedAnnealing;
import gama.extension.batch.optimization.Swarm;
import gama.extension.batch.optimization.TabuSearch;
import gama.extension.batch.optimization.TabuSearchReactive;
import gama.extension.stats.sampling.LatinhypercubeSampling;
import gama.extension.stats.sampling.Morris;
import gama.gaml.operators.Containers;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

/**
 * The Class AExplorationAlgorithm.
 */
@inside (
		kinds = { ISymbolKind.EXPERIMENT })
public abstract class AExplorationAlgorithm extends Symbol implements IExploration {

	/** The Constant CLASSES. */
	public static final List<Class<?>> CLASSES =
			Arrays.asList(GeneticAlgorithm.class, SimulatedAnnealing.class, HillClimbing.class, TabuSearch.class,
					TabuSearchReactive.class, Exploration.class, Swarm.class, SobolExploration.class,
					MorrisExploration.class, StochanalysisExploration.class, BetaExploration.class);

	/** The current experiment. */
	protected BatchAgent currentExperiment;

	/** The outputs expression. */
	protected IExpression outputsExpression;

	/** The automatic output batch file */
	protected IExpression outputFilePath;

	/** The sample size. */
	protected int sample_size = 255;

	@Override
	public void initializeFor(final IScope scope, final IExperimentAgent.Batch agent) throws GamaRuntimeException {
		this.currentExperiment = (BatchAgent) agent;
	}

	/**
	 * Instantiates a new a exploration algorithm.
	 *
	 * @param desc
	 *            the desc
	 */
	public AExplorationAlgorithm(final IDescription desc) {
		super(desc);
		if (hasFacet(IKeyword.BATCH_VAR_OUTPUTS)) { outputsExpression = getFacet(IKeyword.BATCH_VAR_OUTPUTS); }
		if (hasFacet(IKeyword.BATCH_RAW_RESULTS)) { 
			outputFilePath = getFacet(IKeyword.BATCH_RAW_RESULTS); 
		}
	}

	/**
	 * Adds the parameters to.
	 *
	 * @param exp
	 *            the exp
	 * @param agent
	 *            the agent
	 */
	@Override
	public void addParametersTo(final List<Batch> exp, final IExperimentAgent.Batch agent) {
		exp.add(new ParameterAdapter("Exploration method", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				final String methodName = IExploration.METHODS[CLASSES.indexOf(AExplorationAlgorithm.this.getClass())];
				return methodName;
			}

		});

		exp.add(new ParameterAdapter("Sampling method", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				if (hasFacet(IKeyword.FROM)) return IExploration.FROM_FILE;
				if (hasFacet(IKeyword.WITH)) return IExploration.FROM_LIST;
				final String methodName = IExploration.METHODS[CLASSES.indexOf(AExplorationAlgorithm.this.getClass())];
				if (!hasFacet(IExploration.SAMPLING)) {
					if (methodName == IExploration.MORRIS) return IExploration.MORRIS;
					if (methodName == SOBOL) return IKeyword.SALTELLI;
					return hasFacet(IExploration.SAMPLE_SIZE) ? IKeyword.UNIFORM : IExploration.DEFAULT_SAMPLING;
				}
				return Cast.asString(agent.getScope(), getFacet(IExploration.SAMPLING).value(agent.getScope()));
			}
		});


		exp.add(new ParameterAdapter("Sampled points", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			long estimatedSamples = -1;
			@Override
			public Object value() {
				if (estimatedSamples < 0) {
					estimatedSamples = estimateSamples(agent);
				}
				return estimatedSamples;
			}
		});

		exp.add(new ParameterAdapter("Simulation runs", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {

			long estimatedSamples = -1;
			@Override
			public Object value() {
				if (estimatedSamples < 0) {
					String xpm = IExploration.METHODS[CLASSES.indexOf(AExplorationAlgorithm.this.getClass())];
					int repeat = (xpm != SOBOL && xpm != MORRIS) ? agent.getSeeds().length : 1;
					estimatedSamples = estimateSamples(agent) * repeat;
				}
				return estimatedSamples;
			}
		});

		if (getOutputs() != null) {
			exp.add(new ParameterAdapter("Outputs of interest", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
				@Override
				public Object value() {
					return getOutputs().literalValue();
				}
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
	 * Return the specific report for this exploration TODO : has been specified for calibration - to be removed or used
	 * consistently across experiment; see {@link ExperimentAgent}
	 */
	public String getReport() { return ""; }

	/**
	 * Gives the list of variables the exploration method is targeting
	 *
	 * @return {@link IExpression}
	 */
	@Override
	public IExpression getOutputs() { return outputsExpression; }

	/**
	 * Return the name of variables / or string representation entered in BATCH_OUTPUT_VAR facet
	 *
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	public IList<String> getLitteralOutputs() {
		return StreamEx.of(((IExpression.List) outputsExpression).getElements()).map(IExpression::getName)
				.toCollection(Containers.listOf(Types.STRING));
	}

	/**
	 * Construct the experimental plan based on the given the proper modeler input: from SAMPLING methods, from a file
	 * or explicit list of points
	 *
	 * @param parameters
	 * @param scope
	 * @return List of {@link ParametersSet}
	 */
	public List<ParametersSet> getExperimentPlan(final List<Batch> parameters, final IScope scope) {
		String method =
				hasFacet(IExploration.SAMPLING) ? Cast.asString(scope, getFacet(IExploration.SAMPLING).value(scope))
						: hasFacet(IKeyword.FROM) ? IExploration.FROM_FILE
						: hasFacet(IKeyword.WITH) ? IExploration.FROM_LIST : "";

		return switch (method) {
			case MORRIS:
				yield MorrisSampling.makeMorrisSamplingOnly(hasFacet(MorrisExploration.NB_LEVELS)
						? Cast.asInt(scope, getFacet(MorrisExploration.NB_LEVELS).value(scope))
						: Morris.DEFAULT_LEVELS, sample_size, parameters, scope);
			case IKeyword.LHS:
				yield LatinhypercubeSampling.latinHypercubeSamples(sample_size, parameters,
						scope.getRandom().getGenerator(), scope,
						hasFacet(IKeyword.LHS_OUTER) ? Cast.asInt(scope, getFacet(IKeyword.LHS_OUTER).value(scope))
								: 50,
						hasFacet(IKeyword.LHS_INNER) ? Cast.asInt(scope, getFacet(IKeyword.LHS_INNER).value(scope))
								: 100);
			case IKeyword.ORTHOGONAL:
				yield OrthogonalSampling.orthogonalSamples(sample_size,
						hasFacet(IExploration.ITERATIONS)
								? Cast.asInt(scope, getFacet(IExploration.ITERATIONS).value(scope))
								: OrthogonalSampling.DEFAULT_ITERATION,
						parameters, scope.getRandom().getGenerator(), scope);
			case IKeyword.SALTELLI:
				yield SaltelliSampling.makeSaltelliSampling(scope, sample_size, parameters);
			case IKeyword.UNIFORM:
				yield RandomSampling.uniformSampling(scope, sample_size, parameters);
			case IKeyword.FACTORIAL:
				yield hasFacet(IExploration.SAMPLE_FACTORIAL)
						? RandomSampling.factorialUniformSampling(scope, getFactorial(scope, parameters), parameters)
						: RandomSampling.factorialUniformSampling(scope, sample_size, parameters);
			case IExploration.FROM_LIST:
				yield buildParameterFromMap(scope);
			case IExploration.FROM_FILE:
				yield buildParametersFromCSV(scope, Cast.asString(scope, getFacet(IKeyword.FROM).value(scope)));
			default:
				yield hasFacet(IExploration.SAMPLE_SIZE) ? 
						RandomSampling.uniformSampling(scope, sample_size, parameters) : 
							buildParameterSets(scope, new ArrayList<>(), 0);
		};

	}

	/**
	 * Gives the factorial plan based on SAMPLE_FACTORIAL facets of experiment
	 *
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	public int[] getFactorial(final IScope scope, final List<Batch> parameters) {

		IList<Integer> fact = GamaListFactory.castToList(scope, getFacet(IExploration.SAMPLE_FACTORIAL).value(scope));
		if (fact.size() < parameters.size()) {
			fact.addAll(Collections.nCopies(parameters.size() - fact.size(), IExploration.DEFAULT_FACTORIAL));
		} else if (fact.size() > parameters.size()) {
			fact = GamaListFactory.castToList(scope, fact.subList(0, parameters.size()));
		}

		return IntStreamEx.of(fact).toArray();
	}

	/**
	 * Main method to build the set of points to visit during the exploration of a model
	 *
	 * @param scope
	 * @param sets
	 * @param index
	 * @return
	 */
	@SuppressWarnings ("rawtypes")
	public List<ParametersSet> buildParameterSets(final IScope scope, final List<ParametersSet> sets, final int index) {
		if (sets == null) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Cannot build a sample with empty parameter set", scope), true);
		}
		final List<Batch> variables = currentExperiment.getParametersToExplore();
		List<ParametersSet> sets2 = new ArrayList<>();
		if (variables.isEmpty()) return sets2;
		if (sets.isEmpty()) { sets.add(new ParametersSet()); }
		final IParameter.Batch var = variables.get(index);
		for (ParametersSet solution : sets) {
			List vals = var.getAmongValue(scope) != null ? var.getAmongValue(scope) : getParameterSwip(scope, var);
			for (final Object val : vals) {
				ParametersSet ps = new ParametersSet(solution);
				ps.put(var.getName(), val);
				sets2.add(ps);
			}
		}
		if (index == variables.size() - 1) return sets2;
		return buildParameterSets(scope, sets2, index + 1);
	}

	/**
	 *
	 * Save the raw results of simulations with targeted outputs specified in the facet
	 * {@value gama.annotations.constants.IKeyword#BATCH_VAR_OUTPUTS} and file destination in the facet
	 * {@value gama.annotations.constants.IKeyword#BATCH_OUTPUT}
	 *
	 * WARNING : file are erased if same path is passed
	 *
	 * @param scope
	 * @param results
	 */
	public void saveRawResults(final IScope scope, final IMap<ParametersSet, Map<String, List<Object>>> results) {
		String path_to = outputFilePath == null
				? FileUtils.constructAbsoluteFilePath(scope, currentExperiment.getName() + "_results.csv", false)
				: Cast.asString(scope, outputFilePath.value(scope));
		final File fo = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
		final File parento = fo.getParentFile();
		if (!parento.exists()) {
			try {
				if (!parento.mkdirs()) {
					GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(new Exception("Unknown reason"), scope), true);
				}
			} catch (Exception e) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error(
						"Cannot create a folder at " + parento.toString() + " because: " + e.getMessage(), scope), true);
			}
		}
		if (fo.exists()) {
			try {
				if (!fo.delete()) {
					GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(new Exception("Unknown reason"), scope), true);
				}
			} catch (Exception e) {
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException
						.error("File " + fo.toString() + " cannot be deleted because: " + e.getMessage(), scope), true);
			}
		}
		try (FileWriter fw = new FileWriter(fo, StandardCharsets.UTF_8, false)) {
			fw.write(buildSimulationCsv(results, scope));
		} catch (Exception e) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("File " + fo.toString() + " cannot be found to save "
					+ currentExperiment.getName() + " experiment results", scope), true);
		}
	}

	// ############################################################
	// Private ways to read file or manual input experimental plans

	/**
	 * Build a parameter set (a sample of the parameter space) based on explicit point given either with a gaml map or
	 * written in a file
	 *
	 * @param scope
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	private List<ParametersSet> buildParameterFromMap(final IScope scope) {
		IExpression psexp = getFacet(IKeyword.WITH);
		if (psexp == null) return new ArrayList<>();
		if (!Types.LIST.isAssignableFrom(psexp.getGamlType())) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("You cannot use " + IKeyword.WITH
					+ " facet without input a list of maps: got " + psexp.getDenotedType(), scope), true);
		}
		Object val = psexp.value(scope);
		if (!(val instanceof List)) return new ArrayList<>();
		return buildParametersSetList(scope, (List<Map<String, Object>>) val);
	}

	/**
	 * Builds the parameters set list.
	 *
	 * @param scope
	 *            the scope
	 * @param parameterSets
	 *            the parameter sets
	 * @return the list
	 */
	private List<ParametersSet> buildParametersSetList(final IScope scope,
			final List<Map<String, Object>> parameterSets) {
		var sets = new ArrayList<ParametersSet>();
		for (Map<String, Object> parameterSet : parameterSets) {
			ParametersSet p = new ParametersSet();
			for (Entry<String, Object> entry : parameterSet.entrySet()) {
				p.put(entry.getKey(), entry.getValue() instanceof IExpression
						? ((IExpression) entry.getValue()).value(scope) : entry.getValue());
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
	 * @return
	 */
	private List<ParametersSet> buildParametersFromCSV(final IScope scope, final String path)
			throws GamaRuntimeException {
		List<Map<String, Object>> parameters = new ArrayList<>();
		try (FileReader fr = new FileReader(new File(FileUtils.constructAbsoluteFilePath(scope, path, false)),
				StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(fr)) {
			String line = " ";
			String[] tempArr;
			List<String> list_name = new ArrayList<>();
			int i = 0;
			while ((line = br.readLine()) != null) {
				tempArr = line.split(GamaPreferences.External.CSV_SEPARATOR.getValue());
				if (i > 0) {
					Map<String, Object> temp_map = new HashMap<>();
					for (int y = 0; y < tempArr.length; y++) { temp_map.put(list_name.get(y), tempArr[y]); }
					parameters.add(temp_map);
				} else {
					for (String tempStr : tempArr) { list_name.add(tempStr); }
				}
				i++;
			}
		} catch (FileNotFoundException nfe) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("CSV file not found: " + path, scope), true);
		} catch (IOException ioe) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Error during the reading of the CSV file", scope), true);
		}

		return buildParametersSetList(scope, parameters);
	}

	/**
	 * Rebuild simulations ouptuts to be written in a file
	 *
	 * @param Outputs
	 * @param scope
	 * @return
	 */
	private String buildSimulationCsv(final IMap<ParametersSet, Map<String, List<Object>>> results,
			final IScope scope) {
		String separator = GamaPreferences.External.CSV_SEPARATOR.getValue();
		StringBuilder sb = new StringBuilder();

		List<String> outputs = getLitteralOutputs();
		List<String> inputs = results.getKeys().anyValue(scope).getKeys();
		// Write the header
		sb.append(String.join(separator, inputs));
		sb.append(separator);
		sb.append(String.join(separator, outputs));

		// Find results and append to global string
		for (var entry : results.entrySet()) {
			Map<String, List<Object>> res = entry.getValue();
			var ps = entry.getKey();
			int nbr = res.values().stream().findAny().get().size();
			if (!res.values().stream().allMatch(r -> r.size() == nbr)) {
				GAMA.reportAndThrowIfNeeded(scope,
						GamaRuntimeException.warning(
								"Not all sample of stochastic analysis have the same number of replicates", scope),
						false);
				continue;
			}

			// Swipe over the replication of each parameter sets, writing a line for each
			for (int r = 0; r < nbr; r++) {
				sb.append(StringUtils.LN);
				sb.append(inputs.stream().map(i -> ps.get(i).toString()).collect(Collectors.joining(separator)));
				for (var entrySet : res.entrySet()) { sb.append(separator).append(entrySet.getValue().get(r)); }
			}
		}

		return sb.toString();
	}

	// ##################### Estimate sample size based on method facets

	/**
	 * Estimate samples.
	 *
	 * @param agent
	 *            the agent
	 * @return the int
	 */
	private long estimateSamples(final IExperimentAgent.Batch agent) {
		String method = IExploration.DEFAULT_SAMPLING;
		if (hasFacet(IExploration.SAMPLING)) {
			method = Cast.asString(agent.getScope(), getFacet(IExploration.SAMPLING).value(agent.getScope()));
		} else {
			String xpm = IExploration.METHODS[CLASSES.indexOf(AExplorationAlgorithm.this.getClass())];
			if (hasFacet(IKeyword.FROM)) {
				method = IExploration.FROM_FILE;
			} else if (hasFacet(IKeyword.WITH)) {
				method = IExploration.FROM_LIST;
			} else if (xpm == MORRIS) {
				method = MORRIS;
			} else if (xpm == SOBOL) { method = IKeyword.SALTELLI; }
		}
		int K = agent.getParametersToExplore().size();
		int N = hasFacet(IExploration.SAMPLE_SIZE)
				? Cast.asInt(agent.getScope(), getFacet(IExploration.SAMPLE_SIZE).value(agent.getScope()))
				: sample_size;
		long res = switch (method) {
			case MORRIS:
				yield N * (K + 1);
			case IKeyword.SALTELLI:
				yield N * (2 * K + 2);
			case IKeyword.LHS, IKeyword.ORTHOGONAL, IKeyword.UNIFORM:
				yield N;
			case IExploration.FROM_LIST:
				yield buildParameterFromMap(agent.getScope()).size();
			case IExploration.FROM_FILE:
				Stream<String> lines;
				String filePath = "";
				var scope = agent.getScope();
				long count = 0;
				try {
					filePath = FileUtils.constructAbsoluteFilePath(scope, getFacetValue(scope, IKeyword.FROM).toString(), false);
					lines = Files.lines(Paths.get(filePath));
					count = lines.count();
				} catch (FileNotFoundException nfe) {
					GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("CSV file not found: '" + filePath +"'", scope), true);
				} catch (IOException ioe) {
					GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.error("Error during the reading of the CSV file", scope), true);
				} catch (Exception ex) {
					GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.create(ex, scope), true);
				}
				yield count;
				

			default:
				yield hasFacet(IExploration.SAMPLE_SIZE) ? N : 
					hasFacet(IExploration.SAMPLE_FACTORIAL) ? IntStreamEx
						.of(getFactorial(agent.getScope(), agent.getParametersToExplore())).reduce(1, (a, b) -> a * b)
						: IntStreamEx
								.of(agent.getParametersToExplore().stream()
										.mapToInt(b -> getParameterSwip(agent.getScope(), b).size()))
								.reduce(1, (a, b) -> a * b);

		};
		if (METHODS[CLASSES.indexOf(AExplorationAlgorithm.this.getClass())] == BETAD
				&& hasFacet(BetaExploration.BOOTSTRAP)) {
			res = N + N * Cast.asInt(agent.getScope(), getFacet(BetaExploration.BOOTSTRAP).value(agent.getScope())) * K;
		}
		return res;
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
		return switch (var.getType().id()) {
			case IType.INT -> getIntParameterSwip(scope, var);
			case IType.FLOAT -> getFloatParameterSwip(scope, var);
			case IType.DATE -> getDateParameterSwip(scope, var);
			case IType.POINT -> getPointParameterSwip(scope, var);
			case IType.BOOL -> Arrays.asList(true, false);
			default -> getDefaultParameterSwip(scope, var);
		};
	}

	/**
	 * Gets the date parameter swip.
	 *
	 * @param scope
	 *            the scope
	 * @param var
	 *            the var
	 * @return the date parameter swip
	 */
	private List<Object> getDateParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();
		IDate dateValue = GamaDateFactory.castToDate(scope, var.getMinValue(scope));
		IDate maxDateValue = GamaDateFactory.castToDate(scope, var.getMaxValue(scope));
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
		return res;
	}

	/**
	 * Gets the point parameter swip.
	 *
	 * @param scope
	 *            the scope
	 * @param var
	 *            the var
	 * @return the point parameter swip
	 */
	private List<Object> getPointParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();
		IPoint pointValue = GamaPointFactory.castToPoint(scope, var.getMinValue(scope));
		IPoint maxPointValue = GamaPointFactory.castToPoint(scope, var.getMaxValue(scope));
		Double stepV = null;

		IPoint increment = GamaPointFactory.create((maxPointValue.getX() - pointValue.getX()) / 10.0,
				(maxPointValue.getY() - pointValue.getY()) / 10.0, (maxPointValue.getZ() - pointValue.getZ()) / 10.0);
		if (var.getStepValue(scope) != null) {
			increment = GamaPointFactory.castToPoint(scope, var.getStepValue(scope), true);

			if (increment == null) {
				double d = GamaFloatType.staticCast(scope, var.getStepValue(scope), null, false);
				stepV = d;
				increment = GamaPointFactory.create(d, d, d);
			} else {
				stepV = (increment.getX() + increment.getY() + increment.getZ()) / 3.0;
			}

		}

		while (pointValue.smallerThanOrEqualTo(maxPointValue)) {
			if (stepV == null || stepV > 0) {
				res.add(pointValue);
				pointValue = pointValue.plus(GamaPointFactory.castToPoint(scope, increment));
			} else {
				res.add(maxPointValue);
				maxPointValue = maxPointValue.plus(GamaPointFactory.castToPoint(scope, increment));
			}
		}
		return res;
	}

	/**
	 * Gets the float parameter swip.
	 *
	 * @param scope
	 *            the scope
	 * @param var
	 *            the var
	 * @return the float parameter swip
	 */
	private List<Object> getFloatParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();

		double minFloatValue = Cast.asFloat(scope, var.getMinValue(scope));
		double maxFloatValue = Cast.asFloat(scope, var.getMaxValue(scope));
		double stepFloatValue = 0.1;
		double df = IExploration.DEFAULT_FACTORIAL - 1;

		if (hasFacet(IExploration.SAMPLE_FACTORIAL)) {
			List<Batch> b = currentExperiment.getParametersToExplore();
			df = getFactorial(scope, b)[b.indexOf(var)];
			stepFloatValue = (maxFloatValue - minFloatValue) / df;
		} else if (var.getStepValue(scope) != null) {
			stepFloatValue = Cast.asFloat(scope, var.getStepValue(scope)); // - 1; AD: Why -1 ??
		} else {
			stepFloatValue = (maxFloatValue - minFloatValue) / df;
		}
		// AD: Addition of tests to avoid infinite loops (cf #
		if (stepFloatValue < 0) {
			stepFloatValue *= -1;
		} else if (stepFloatValue == 0) { stepFloatValue = 0.1; }

		while (minFloatValue <= maxFloatValue) {
			res.add(minFloatValue);
			minFloatValue += stepFloatValue;
		}

		return res;
	}

	/**
	 * Gets the int parameter swip.
	 *
	 * @param scope
	 *            the scope
	 * @param var
	 *            the var
	 * @return the int parameter swip
	 */
	private List<Object> getIntParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();

		int minValue = Cast.asInt(scope, var.getMinValue(scope));
		int maxValue = Cast.asInt(scope, var.getMaxValue(scope));
		double stepValue = 1;
		double df = IExploration.DEFAULT_FACTORIAL - 1;

		if (hasFacet(IExploration.SAMPLE_FACTORIAL)) {
			List<Batch> b = currentExperiment.getParametersToExplore();
			df = getFactorial(scope, b)[b.indexOf(var)];
			if (maxValue - minValue > df) { stepValue = (maxValue - minValue) / df; }
		} else if (var.getStepValue(scope) != null) {
			stepValue = Cast.asInt(scope, var.getStepValue(scope));
		} else if (maxValue - minValue > df) { stepValue = (maxValue - minValue) / df; }

		// This means if we have min=0 max=4 and step=3, we will get [0, 3] in res
		int nbIterNeeded = Math.abs((int) ((maxValue - minValue) / stepValue));
		double start = stepValue >= 0 ? minValue : maxValue;
		for (int i = 0; i <= nbIterNeeded; i++) { res.add(start + (int) (stepValue * i)); }
		return res;
	}

	/**
	 * Gets the default parameter swip.
	 *
	 * @param scope
	 *            the scope
	 * @param var
	 *            the var
	 * @return the default parameter swip
	 */
	private List<Object> getDefaultParameterSwip(final IScope scope, final Batch var) {
		
		if (var.getAmongValue(scope) != null) { return var.getAmongValue(scope); }
		
		List<Object> res = new ArrayList<>();
		double varValue = Cast.asFloat(scope, var.getMinValue(scope));
		double maxVarValue = Cast.asFloat(scope, var.getMaxValue(scope));
		double floatcrement = 1;
		double dfactor = IExploration.DEFAULT_FACTORIAL - 1;

		if (hasFacet(IExploration.SAMPLE_FACTORIAL)) {
			List<Batch> b = currentExperiment.getParametersToExplore();
			dfactor = getFactorial(scope, b)[b.indexOf(var)];
			floatcrement = (maxVarValue - varValue) / dfactor;
		} else if (var.getStepValue(scope) != null) {
			floatcrement = Cast.asFloat(scope, var.getStepValue(scope));
		} else {
			floatcrement = (maxVarValue - varValue) / dfactor;
		}

		double v = floatcrement >= 0 ? varValue : maxVarValue;

		while (v <= maxVarValue) {

			if (var.getType().id() == IType.INT) {
				res.add((int) v);
			} else if (var.getType().id() == IType.FLOAT) { res.add(v); }
			v += floatcrement;
		}
		return res;
	}

}
