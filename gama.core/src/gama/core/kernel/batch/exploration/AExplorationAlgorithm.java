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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.util.FileUtils;
import gama.core.kernel.batch.IExploration;
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
import gama.core.util.IMap;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;
import gama.gaml.types.GamaDateType;
import gama.gaml.types.GamaFloatType;
import gama.gaml.types.GamaPointType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
	
	/** The default step factor. */
	private final int __DEFAULT_STEP_FACTOR = 10;
	
	public static final String CSV_SEP = ",";
	
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
	 * Construct the experimental plan based on the given the proper modeler input: from sampling methods, from a file or explicit list of points
	 *  
	 * @param parameters
	 * @param scope
	 * @return List of {@link ParametersSet}
	 */
	public List<ParametersSet> getExperimentPlan(List<Batch> parameters, IScope scope) {
		String method = hasFacet(Exploration.METHODS) ? 
				Cast.asString(scope, getFacet(Exploration.METHODS).value(scope)) 
				: (hasFacet(IKeyword.FROM) ? Exploration.FROM_FILE 
						: hasFacet(IKeyword.WITH) ? Exploration.FROM_LIST 
								: "");
		
		return switch (method) {
			case IKeyword.MORRIS: yield MorrisSampling.makeMorrisSamplingOnly(hasFacet(MorrisExploration.NB_LEVELS) ?
						Cast.asInt(scope, getFacet(MorrisExploration.NB_LEVELS)) : Morris.DEFAULT_LEVELS, sample_size, parameters, scope);
			case IKeyword.LHS: yield LatinhypercubeSampling.latinHypercubeSamples(sample_size, parameters, scope.getRandom().getGenerator(), scope);
			case IKeyword.ORTHOGONAL: yield OrthogonalSampling.orthogonalSamples(sample_size, 
					hasFacet(Exploration.ITERATIONS) ? Cast.asInt(scope, getFacet(Exploration.ITERATIONS).value(scope)) : OrthogonalSampling.DEFAULT_ITERATION, 
							parameters, scope.getRandom().getGenerator(), scope);
			case IKeyword.SALTELLI: yield SaltelliSampling.makeSaltelliSampling(scope, sample_size, parameters);
			case IKeyword.UNIFORM: yield RandomSampling.uniformSampling(scope, sample_size, parameters);
			case IKeyword.FACTORIAL: yield hasFacet(Exploration.SAMPLE_FACTORIAL) ? 
							RandomSampling.factorialUniformSampling(scope, getFactorial(scope, parameters), parameters)
							: RandomSampling.factorialUniformSampling(scope, sample_size, parameters);
			case Exploration.FROM_LIST: yield buildParameterFromMap(scope, new ArrayList<>(), 0);
			case Exploration.FROM_FILE: yield buildParametersFromCSV(scope, Cast.asString(scope, getFacet(IKeyword.FROM).value(scope)), new ArrayList<>());
			default: yield buildParameterSets(scope, new ArrayList<>(), 0);
		};
		
	}
	
	/**
	 * Gives the factorial plan based on SAMPLE_FACTORIAL facets of experiment
	 * @return
	 */
	public int[] getFactorial(IScope scope, List<Batch> parameters) {
		Object o = getFacet(Exploration.SAMPLE_FACTORIAL).value(scope);
		int[] r = new int[parameters.size()];
		Arrays.fill(r, Integer.parseInt(o.toString()));
		return r;
	}
	
	/**
	 * Main method to build the set of points to visit during the exploration of a model
	 * 
	 * @param scope
	 * @param sets
	 * @param index
	 * @return
	 */
	public List<ParametersSet> buildParameterSets(IScope scope, List<ParametersSet> sets, int index) {
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
	
	/**
	 * 
	 * Save the raw results of simulations with targeted outputs specified in the facet {@value gama.core.common.interfaces.IKeyword#BATCH_VAR_OUTPUTS} and
	 * file destination in the facet {@value gama.core.common.interfaces.IKeyword#BATCH_OUTPUT}
	 * 
	 * WARNING : file are erased if same path is passed
	 * 
	 * @param scope
	 * @param results
	 */
	public void saveRawResults(final IScope scope, IMap<ParametersSet, Map<String, List<Object>>> results) {
		String path_to = Cast.asString(scope, outputFilePath.value(scope));
		final File fo = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
		final File parento = fo.getParentFile();
		if (!parento.exists()) { parento.mkdirs(); }
		if (fo.exists()) { fo.delete(); }
		try (FileWriter fw = new FileWriter(fo, StandardCharsets.UTF_8, false)) {
			fw.write(buildSimulationCsv(results, scope));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + fo.toString() + " cannot be found to save "+currentExperiment.getName()+" experiment results", scope);
		}
	}
	
	// ############################################################
	// Private ways to read file or manual input experimental plans

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
		if (psexp.getDenotedType() != Types.LIST) throw GamaRuntimeException.error(
				"You cannot use " + IKeyword.WITH + " facet without input a list of maps as parameters inputs", scope);
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
			try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8); BufferedReader br = new BufferedReader(fr)) {
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
	
	/**
	 * Rebuild simulations ouptuts to be written in a file
	 * 
	 * @param Outputs
	 * @param scope
	 * @return
	 */
	private String buildSimulationCsv(final IMap<ParametersSet, Map<String, List<Object>>> results, IScope scope) {
		StringBuilder sb = new StringBuilder();
		
		@SuppressWarnings("unchecked")
		List<String> outputs = Cast.asList(scope, getFacet(IKeyword.BATCH_VAR_OUTPUTS).value(scope));
		List<String> inputs = results.getKeys().anyValue(scope).getKeys();
		// Write the header
		sb.append(String.join(CSV_SEP, inputs));
		sb.append(CSV_SEP);
		sb.append(String.join(CSV_SEP, outputs));
		
		// Find results and append to global string
		for (ParametersSet ps : results.keySet()) {
			Map<String, List<Object>> res = results.get(ps);
			int nbr = res.values().stream().findAny().get().size();
			if (!res.values().stream().allMatch(r -> r.size()==nbr)) { 
				GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning("Not all sample of stochastic analysis have the same number of replicates", scope), false); 
			}
			else {
				// Swipe over the replication of each parameter sets, writing a line for each
				for (int r = 0; r < nbr; r++) {
					sb.append(Strings.LN);
					sb.append(inputs.stream().map(i -> ps.get(i).toString()).collect(Collectors.joining(CSV_SEP)));
					for (String output : res.keySet()) { sb.append(CSV_SEP).append(res.get(output).get(r)); }
				}				
			}
		}

		return sb.toString();
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
				} else if (maxValue - minValue > __DEFAULT_STEP_FACTOR) {
					stepValue = (maxValue - minValue) / (double)__DEFAULT_STEP_FACTOR;
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
					stepFloatValue = Cast.asFloat(scope, var.getStepValue(scope))-1;
				} else {
					stepFloatValue = (maxFloatValue - minFloatValue) / (__DEFAULT_STEP_FACTOR-1);
				}
				
				// Do we need to account for min > max ???
				
				while (minFloatValue <= maxFloatValue) {
					minFloatValue += stepFloatValue;
					res.add(minFloatValue);
				}
				// Do we need to control for errors ????
				// Do we have to use Math.ulp() ???
//				if (Math.abs(Cast.asFloat(scope, res.get(res.size()-1)) - maxFloatValue) < stepFloatValue) {
//					res.remove(res.size()-1);
//					res.add(maxFloatValue);
//				}
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
					floatcrement = (maxVarValue - varValue) / __DEFAULT_STEP_FACTOR;
				}
				
				double v = floatcrement >= 0 ? varValue : maxVarValue;
				
				while (varValue <= maxVarValue) {

					if (var.getType().id() == IType.INT) {
						res.add((int) v);
					} else if (var.getType().id() == IType.FLOAT) {
						res.add(v);
					}
					v += floatcrement;
				}
		}
		return res;
	}
	
}
