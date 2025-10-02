/*******************************************************************************************************
 *
 * AExplorationAlgorithm.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.util.FileUtils;
import gama.core.kernel.batch.IExploration;
import gama.core.kernel.batch.exploration.betadistribution.BetaExploration;
import gama.core.kernel.batch.exploration.morris.Morris;
import gama.core.kernel.batch.exploration.morris.MorrisExploration;
import gama.core.kernel.batch.exploration.sampling.LatinhypercubeSampling;
import gama.core.kernel.batch.exploration.sampling.MorrisSampling;
import gama.core.kernel.batch.exploration.sampling.OrthogonalSampling;
import gama.core.kernel.batch.exploration.sampling.RandomSampling;
import gama.core.kernel.batch.exploration.sampling.SaltelliSampling;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaDate;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.data.ListExpression;
import gama.gaml.expressions.data.MapExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Containers;
import gama.gaml.operators.Strings;
import gama.gaml.types.GamaDateType;
import gama.gaml.types.GamaFloatType;
import gama.gaml.types.GamaPointType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

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

	/** The automatic output batch file */
	protected IExpression outputFilePath;

	/** The sample size. */
	protected int sample_size = 132;

	public static final String CSV_SEP = ",";

	@Override
	public void initializeFor(final IScope scope, final BatchAgent agent) throws GamaRuntimeException {
		this.currentExperiment = agent;
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
		if (hasFacet(IKeyword.BATCH_OUTPUT)) { outputFilePath = getFacet(IKeyword.BATCH_OUTPUT); }
	}

	@Override
	public void addParametersTo(final List<Batch> exp, final BatchAgent agent) {
		exp.add(new ParameterAdapter("Exploration method", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				@SuppressWarnings ("rawtypes") final List<Class> classes = Arrays.asList(CLASSES);
				final String methodName = IKeyword.METHODS[classes.indexOf(AExplorationAlgorithm.this.getClass())];
				return methodName;
			}

		});

		exp.add(new ParameterAdapter("Sampling method", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				if (hasFacet(IKeyword.FROM)) return Exploration.FROM_FILE;
				if (hasFacet(IKeyword.WITH)) return Exploration.FROM_LIST;
				final String methodName = IKeyword.METHODS[Arrays.asList(CLASSES).indexOf(AExplorationAlgorithm.this.getClass())];
				if (!hasFacet(Exploration.METHODS)) {
					if (methodName==IKeyword.MORRIS) { return IKeyword.MORRIS; }
					if (methodName==IKeyword.SOBOL) {return IKeyword.SALTELLI; }
					return Exploration.DEFAULT_SAMPLING;
				}
				return hasFacet(Exploration.METHODS)
						? Cast.asString(agent.getScope(), getFacet(Exploration.METHODS).value(agent.getScope())) : 
							Exploration.DEFAULT_SAMPLING;
			}
		});
		
		exp.add(new ParameterAdapter("Sampled points", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() { return estimateSamples(agent); }
		});
		
		exp.add(new ParameterAdapter("Simulation runs", BatchAgent.EXPLORATION_EXPERIMENT, IType.STRING) {
			@Override
			public Object value() {
				int res = estimateSamples(agent);
				final String methodName = IKeyword.METHODS[Arrays.asList(CLASSES).indexOf(AExplorationAlgorithm.this.getClass())];
				if (Arrays.asList(IKeyword.SOBOL,IKeyword.MORRIS,IKeyword.BETAD).contains(methodName)) {
					return res;
				}
				return res * 
					(agent.getSpecies().hasFacet(IKeyword.REPEAT) ? 
						Cast.asInt(agent.getScope(), 
								agent.getSpecies().getFacet(IKeyword.REPEAT).value(agent.getScope())) 
						: 1); }
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
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IList<String> getLitteralOutputs() {;
		return StreamEx.of(((ListExpression) outputsExpression)
					.getElements()).map(IExpression::getName).toCollection(
							Containers.listOf(Types.STRING));
	}

	/**
	 * Construct the experimental plan based on the given the proper modeler input: from sampling methods, from a file
	 * or explicit list of points
	 *
	 * @param parameters
	 * @param scope
	 * @return List of {@link ParametersSet}
	 */
	public List<ParametersSet> getExperimentPlan(final List<Batch> parameters, final IScope scope) {
		String method = hasFacet(Exploration.METHODS) ? Cast.asString(scope, getFacet(Exploration.METHODS).value(scope))
				: hasFacet(IKeyword.FROM) ? Exploration.FROM_FILE : hasFacet(IKeyword.WITH) ? Exploration.FROM_LIST
				: "";

		return switch (method) {
			case IKeyword.MORRIS:
				yield MorrisSampling.makeMorrisSamplingOnly(hasFacet(MorrisExploration.NB_LEVELS)
						? Cast.asInt(scope, getFacet(MorrisExploration.NB_LEVELS)) : Morris.DEFAULT_LEVELS, sample_size,
						parameters, scope);
			case IKeyword.LHS:
				yield LatinhypercubeSampling.latinHypercubeSamples(sample_size, parameters,
						scope.getRandom().getGenerator(), scope);
			case IKeyword.ORTHOGONAL:
				yield OrthogonalSampling.orthogonalSamples(sample_size,
						hasFacet(Exploration.ITERATIONS)
								? Cast.asInt(scope, getFacet(Exploration.ITERATIONS).value(scope))
								: OrthogonalSampling.DEFAULT_ITERATION,
						parameters, scope.getRandom().getGenerator(), scope);
			case IKeyword.SALTELLI:
				yield SaltelliSampling.makeSaltelliSampling(scope, sample_size, parameters);
			case IKeyword.UNIFORM:
				yield RandomSampling.uniformSampling(scope, sample_size, parameters);
			case IKeyword.FACTORIAL:
				yield hasFacet(Exploration.SAMPLE_FACTORIAL)
						? RandomSampling.factorialUniformSampling(scope, getFactorial(scope, parameters), parameters)
						: RandomSampling.factorialUniformSampling(scope, sample_size, parameters);
			case Exploration.FROM_LIST:
				yield buildParameterFromMap(scope);
			case Exploration.FROM_FILE:
				yield buildParametersFromCSV(scope, Cast.asString(scope, getFacet(IKeyword.FROM).value(scope)));
			default:
				yield buildParameterSets(scope, new ArrayList<>(), 0);
		};

	}

	/**
	 * Gives the factorial plan based on SAMPLE_FACTORIAL facets of experiment
	 *
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	public int[] getFactorial(final IScope scope, final List<Batch> parameters) {

		IList<Integer> fact = Cast.asList(scope, getFacet(Exploration.SAMPLE_FACTORIAL).value(scope));
		if (fact.size() < parameters.size()) {
			fact.addAll(Collections.nCopies(parameters.size() - fact.size(), Exploration.DEFAULT_FACTORIAL));
		} else if (fact.size() > parameters.size()) { fact = Cast.asList(scope, fact.subList(0, parameters.size())); }

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
		if (sets == null) throw GamaRuntimeException.error("Cannot build a sample with empty parameter set", scope);
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
	 * {@value gama.core.common.interfaces.IKeyword#BATCH_VAR_OUTPUTS} and file destination in the facet
	 * {@value gama.core.common.interfaces.IKeyword#BATCH_OUTPUT}
	 *
	 * WARNING : file are erased if same path is passed
	 *
	 * @param scope
	 * @param results
	 */
	public void saveRawResults(final IScope scope, final IMap<ParametersSet, Map<String, List<Object>>> results) {
		String path_to = outputFilePath==null ? FileUtils.constructAbsoluteFilePath(scope, currentExperiment.getName()+"_results.csv", false) :
				Cast.asString(scope, outputFilePath.value(scope));
		final File fo = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
		final File parento = fo.getParentFile();
		if (!parento.exists()) {
			try {
				if (!parento.mkdirs()) throw new Exception("Unknown reason");
			} catch (Exception e) {
				throw GamaRuntimeException.error(
						"Cannot create a folder at " + parento.toString() + " because: " + e.getMessage(), scope);
			}
		}
		if (fo.exists()) {
			try {
				if (!fo.delete()) throw new Exception("Unknown reason");
			} catch (Exception e) {
				throw GamaRuntimeException
						.error("File " + fo.toString() + " cannot be deleted because: " + e.getMessage(), scope);
			}
		}
		try (FileWriter fw = new FileWriter(fo, StandardCharsets.UTF_8, false)) {
			fw.write(buildSimulationCsv(results, scope));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + fo.toString() + " cannot be found to save "
					+ currentExperiment.getName() + " experiment results", scope);
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
	private List<ParametersSet> buildParameterFromMap(final IScope scope) {
		IExpression psexp = getFacet(IKeyword.WITH);
		if (psexp.getGamlType().isAssignableFrom(Types.LIST)) throw GamaRuntimeException.error(
				"You cannot use " + IKeyword.WITH + " facet without input a list of maps: got "+psexp.getDenotedType(), scope);
		List<IMap<IExpression, IExpression>> parameterSets = StreamEx.of( ((ListExpression) psexp).getElements() )
				.map(e -> ((MapExpression) e).getElements()).toList();
		List<Map<String, Object>> paramSets = new ArrayList<>();
		for (IMap<IExpression, IExpression> ps : parameterSets) {
			Map<String,Object> lt = new HashMap<>();
			for (var e : ps.entrySet()) { lt.put(e.getKey().getName(), e.getValue()); }
			paramSets.add(lt);
		}
		return buildParametersSetList(scope, paramSets);
	}

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

		try (FileReader fr = new FileReader(
				new File(FileUtils.constructAbsoluteFilePath(scope, path, false)), StandardCharsets.UTF_8);
				BufferedReader br = new BufferedReader(fr)) {
			String line = " ";
			String[] tempArr;
			List<String> list_name = new ArrayList<>();
			int i = 0;
			while ((line = br.readLine()) != null) {
				tempArr = line.split(CSV_SEP);
				for (String tempStr : tempArr) { if (i == 0) { list_name.add(tempStr); } }
				if (i > 0) {
					Map<String, Object> temp_map = new HashMap<>();
					for (int y = 0; y < tempArr.length; y++) { temp_map.put(list_name.get(y), tempArr[y]); }
					parameters.add(temp_map);
				}
				i++;
			}
		} catch (FileNotFoundException nfe) {
			throw GamaRuntimeException.error("CSV file not found: " + path, scope);
		} catch (IOException ioe) {
			throw GamaRuntimeException.error("Error during the reading of the CSV file", scope);
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
		StringBuilder sb = new StringBuilder();

		List<String> outputs = getLitteralOutputs();
		List<String> inputs = results.getKeys().anyValue(scope).getKeys();
		// Write the header
		sb.append(String.join(CSV_SEP, inputs));
		sb.append(CSV_SEP);
		sb.append(String.join(CSV_SEP, outputs));

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
				sb.append(Strings.LN);
				sb.append(inputs.stream().map(i -> ps.get(i).toString()).collect(Collectors.joining(CSV_SEP)));
				for (var entrySet : res.entrySet()) { sb.append(CSV_SEP).append(entrySet.getValue().get(r)); }
			}
		}

		return sb.toString();
	}
	
	// ##################### Estimate sample size based on method facets
	
	private int estimateSamples(final BatchAgent agent) {
		String method = Exploration.DEFAULT_SAMPLING; 
		if (hasFacet(Exploration.METHODS)) {
			method = Cast.asString(agent.getScope(), getFacet(Exploration.METHODS).value(agent.getScope()));
		} else {
			String xpm =IKeyword.METHODS[Arrays.asList(CLASSES).indexOf(AExplorationAlgorithm.this.getClass())];
			if (hasFacet(IKeyword.FROM)) method = Exploration.FROM_FILE;
			else if (hasFacet(IKeyword.WITH)) method = Exploration.FROM_LIST;
			else if (xpm == IKeyword.MORRIS) method = IKeyword.MORRIS;
			else if (xpm == IKeyword.SOBOL) method = IKeyword.SALTELLI;
		}
		int K = agent.getParametersToExplore().size();
		int N = hasFacet(Exploration.SAMPLE_SIZE) ? Cast.asInt(agent.getScope(), 
					getFacet(Exploration.SAMPLE_SIZE).value(agent.getScope())) : sample_size;
		int res = switch (method) {
			case IKeyword.MORRIS:
				yield N * (K+1);
			case IKeyword.SALTELLI:
				yield N * (2*K + 2);
			case IKeyword.LHS, IKeyword.ORTHOGONAL, IKeyword.UNIFORM:
				yield N;
			case Exploration.FROM_LIST:
				yield buildParameterFromMap(agent.getScope()).size();
			case Exploration.FROM_FILE:
				yield buildParametersFromCSV(agent.getScope(), 
						Cast.asString(agent.getScope(), getFacet(IKeyword.FROM).value(agent.getScope()))).size();
			default:
				yield hasFacet(Exploration.SAMPLE_FACTORIAL) ? 
						IntStreamEx.of(getFactorial(agent.getScope(), agent.getParametersToExplore()))
							.reduce(1, (a,b) -> a*b) 
							: (hasFacet(Exploration.SAMPLE_SIZE) ? N 
									: IntStreamEx.of(agent.getParametersToExplore().stream().mapToInt(
											b -> getParameterSwip(agent.getScope(), b).size())).reduce(1, (a,b) -> a*b)); 
			
		};
		if (IKeyword.METHODS[Arrays.asList(CLASSES).indexOf(AExplorationAlgorithm.this.getClass())]==IKeyword.BETAD 
				&& hasFacet(BetaExploration.BOOTSTRAP)) { res = N + N * 
									Cast.asInt(agent.getScope(), getFacet(BetaExploration.BOOTSTRAP).value(agent.getScope()))
									* K;}
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
			case IType.BOOL -> Arrays.asList(true,false);
			default -> getDefaultParameterSwip(scope, var);
		};
	}

	private List<Object> getDateParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();
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
		return res;
	}

	private List<Object> getPointParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();
		GamaPoint pointValue = Cast.asPoint(scope, var.getMinValue(scope));
		GamaPoint maxPointValue = Cast.asPoint(scope, var.getMaxValue(scope));
		Double stepV = null;

		GamaPoint increment = new GamaPoint((maxPointValue.x - pointValue.x) / 10.0,
				(maxPointValue.y - pointValue.y) / 10.0, (maxPointValue.z - pointValue.z) / 10.0);
		if (var.getStepValue(scope) != null) {
			increment = GamaPointType.staticCast(scope, var.getStepValue(scope), true);

			if (increment == null) {
				double d = GamaFloatType.staticCast(scope, var.getStepValue(scope), null, false);
				stepV = d;
				increment = new GamaPoint(d, d, d);
			} else {
				stepV = (increment.x + increment.y + increment.z) / 3.0;
			}

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
		return res;
	}

	private List<Object> getFloatParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();

		double minFloatValue = Cast.asFloat(scope, var.getMinValue(scope));
		double maxFloatValue = Cast.asFloat(scope, var.getMaxValue(scope));
		double stepFloatValue = 0.1;
		double df = Exploration.DEFAULT_FACTORIAL - 1;

		if (hasFacet(Exploration.SAMPLE_FACTORIAL)) {
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

	private List<Object> getIntParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();

		int minValue = Cast.asInt(scope, var.getMinValue(scope));
		int maxValue = Cast.asInt(scope, var.getMaxValue(scope));
		double stepValue = 1;
		double df = Exploration.DEFAULT_FACTORIAL - 1;

		if (hasFacet(Exploration.SAMPLE_FACTORIAL)) {
			List<Batch> b = currentExperiment.getParametersToExplore();
			df = getFactorial(scope, b)[b.indexOf(var)];
			if (maxValue - minValue > df) { stepValue = (maxValue - minValue) / df; }
		} else if (var.getStepValue(scope) != null) {
			stepValue = Cast.asInt(scope, var.getStepValue(scope));
		} else if (maxValue - minValue > df) { stepValue = (maxValue - minValue) / df; }

		int nbIterNeeded = 0;
		// This means if we have min=0 max=4 and step=3, we will get [0, 3] in res
		nbIterNeeded = Math.abs((int) ((maxValue - minValue) / stepValue));
		double start = stepValue >= 0 ? minValue : maxValue;
		for (int i = 0; i <= nbIterNeeded; i++) { res.add(start + (int) (stepValue * i)); }
		return res;
	}

	private List<Object> getDefaultParameterSwip(final IScope scope, final Batch var) {
		List<Object> res = new ArrayList<>();
		double varValue = Cast.asFloat(scope, var.getMinValue(scope));
		double maxVarValue = Cast.asFloat(scope, var.getMaxValue(scope));
		double floatcrement = 1;
		double dfactor = Exploration.DEFAULT_FACTORIAL - 1;

		if (hasFacet(Exploration.SAMPLE_FACTORIAL)) {
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
