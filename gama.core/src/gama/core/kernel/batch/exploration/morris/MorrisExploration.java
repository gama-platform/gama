/*******************************************************************************************************
 *
 * MorrisExploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration.morris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import gama.core.common.util.FileUtils;
import gama.core.kernel.batch.exploration.AExplorationAlgorithm;
import gama.core.kernel.batch.exploration.sampling.MorrisSampling;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;
import gama.gaml.types.IType;

/**
 *
 * @author tomroy
 *
 */
@symbol (
		name = IKeyword.MORRIS,
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
						name = MorrisExploration.SAMPLE_SIZE,
						type = IType.ID,
						optional = false,
						doc = @doc ("The size of the sample for Morris samples")),
				@facet (
						name = MorrisExploration.NB_LEVELS,
						type = IType.ID,
						optional = false,
						doc = @doc ("Number of level for the Morris method, can't be 1")),
				@facet (
						name = IKeyword.BATCH_VAR_OUTPUTS,
						type = IType.LIST,
						of = IType.STRING,
						optional = false,
						doc = @doc ("The list of output variables to analyze through morris method")),
				@facet (
						name = IKeyword.BATCH_REPORT,
						type = IType.STRING,
						optional = false,
						doc = @doc ("The path to the file where the Morris report will be written")),
				@facet (
						name = MorrisExploration.PARAMETER_CSV_PATH,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path of morris sample .csv file. If don't use, automatic morris sampling will be perform and saved in the corresponding file")),
				@facet (
						name = IKeyword.BATCH_OUTPUT,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path to the file where the automatic batch report will be written")) },
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm runs a Morris exploration - it has been built upon the SILAB librairy - disabled the repeat facet of the experiment",
		usages = { @usage (
				value = "For example: ",
				examples = { @example (
						value = "method morris sample_size:100 nb_levels:4 outputs:['my_var'] report:'../path/to/report.txt;",
						isExecutable = false) }) })

public class MorrisExploration extends AExplorationAlgorithm {
	/** The Constant SAMPLE_SIZE */
	protected static final String SAMPLE_SIZE = "sample";

	/** The Constant NB_LEVELS */
	protected static final String NB_LEVELS = "levels";

	/** The Constant PARAMETER_CSV_PATH. */
	protected static final String PARAMETER_CSV_PATH = "csv";

	/** The parameters */
	protected List<Batch> parameters;

	/** The outputs */
	protected IList<String> outputs;

	/** The current parameters space. */
	/* The parameter space defined by the Morris sampling method */
	protected List<ParametersSet> solutions;

	/** The res outputs. */
	/* All the outputs for each simulation */
	protected IMap<ParametersSet, Map<String, List<Object>>> res_outputs;

	/** The Parameters names. */
	protected List<String> ParametersNames;

	/** The sample. */
	private int sample;

	/** The nb levels. */
	private int nb_levels;

	/** The My samples. */
	private List<Map<String, Object>> MySamples;

	/**
	 * Instantiates a new morris exploration.
	 *
	 * @param desc
	 *            the desc
	 */
	public MorrisExploration(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	@SuppressWarnings ("unchecked")
	@Override
	public void explore(final IScope scope) {
		this.sample = Cast.asInt(scope, getFacet(SAMPLE_SIZE).value(scope));
		this.nb_levels = Cast.asInt(scope, getFacet(NB_LEVELS).value(scope));
		if (hasFacet(PARAMETER_CSV_PATH)) {
			IExpression path_facet = getFacet(PARAMETER_CSV_PATH);
			String path = Cast.asString(scope, path_facet.value(scope));
			String new_path = scope.getExperiment().getWorkingPath() + "/" + path;
			List<ParametersSet> solutions = this.solutions == null
					? buildParameterSetsFromCSV(scope, new_path, new ArrayList<>()) : this.solutions;
			this.solutions = solutions;
		} else {
			List<ParametersSet> solutions = buildParameterSets(scope, new ArrayList<>(), 0);
			this.solutions = solutions;
		}
		/* Disable repetitions / repeat argument */
		currentExperiment.setSeeds(new Double[1]);
		// TODO : why doesnt it take into account the value of 'keep_simulations:' ? because, by design, there is to many simulation to keep in memory... 
		currentExperiment.setKeepSimulations(false);
		if (GamaExecutorService.shouldRunAllSimulationsInParallel(currentExperiment)) {
			res_outputs = currentExperiment.launchSimulationsWithSolution(solutions);
		} else {
			res_outputs = GamaMapFactory.create();
			for (ParametersSet sol : solutions) {
				res_outputs.put(sol, currentExperiment.launchSimulationsWithSolution(sol));
			}
		}
		
		// The output of simulations
		Map<String, List<Double>> rebuilt_output = rebuildOutput(scope, res_outputs);
		
		// TODO : verify if Morris sampling can lead to several identical points in the parameter space
		int outsize = 0;
		for (Map<String, List<Object>> m : res_outputs.values()) {
			outsize += m.values().stream().findFirst().get().size();
		}
		
		// Prevent OutOfBounds when experiment ends before morris exploration is completed
		if (outsize == MySamples.size() && rebuilt_output.values().stream().findAny().get().size() == MySamples.size()) {
		
			List<String> output_names = rebuilt_output.keySet().stream().toList();
			
			String path_to = Cast.asString(scope, getFacet(IKeyword.BATCH_REPORT).value(scope));
			final File fm = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
			final File parentm = fm.getParentFile();
			if (!parentm.exists()) { parentm.mkdirs(); }
			if (fm.exists()) { fm.delete(); }
			
			for (int i = 0; i < rebuilt_output.size(); i++) {
				String tmp_name = output_names.get(i);
				List<Map<String, Double>> morris_coefficient =
						Morris.MorrisAggregation(nb_levels, rebuilt_output.get(tmp_name), MySamples);
				Morris.WriteAndTellResult(tmp_name, fm.getAbsolutePath(), scope, morris_coefficient);
			}
			/* Save the simulation values in the provided .csv file (input and corresponding output) */
			if (hasFacet(IKeyword.BATCH_OUTPUT)) {
				path_to = Cast.asString(scope, getFacet(IKeyword.BATCH_OUTPUT).value(scope));
				final File fo = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
				final File parento = fo.getParentFile();
				if (!parento.exists()) { parento.mkdirs(); }
				if (fo.exists()) { fo.delete(); }
				saveSimulation(rebuilt_output, fo, scope);
			}
			
		}
	}

	/**
	 * Here we create samples for simulations with MorrisSampling Class
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public List<ParametersSet> buildParameterSets(final IScope scope, final List<ParametersSet> sets, final int index) {
		List<Batch> params = new ArrayList<>(currentExperiment.getParametersToExplore());
		parameters = parameters == null ? params : parameters;
		List<String> names = new ArrayList<>();
		for (int i = 0; i < parameters.size(); i++) { names.add(parameters.get(i).getName()); }
		this.ParametersNames = names;
		outputs = Cast.asList(scope, getFacet(IKeyword.BATCH_VAR_OUTPUTS).value(scope));
		List<Object> morris_samplings = MorrisSampling.MakeMorrisSampling(nb_levels, this.sample, parameters, scope);
		this.MySamples = Cast.asList(scope, morris_samplings.get(0));
		return Cast.asList(scope, morris_samplings.get(1));
	}
	
	@Override
	public void addParametersTo(List<Batch> exp, BatchAgent agent) {
		super.addParametersTo(exp, agent);

		int s = Cast.asInt(agent.getScope(), getFacet(SAMPLE_SIZE).value(agent.getScope()));
		int l = Cast.asInt(agent.getScope(), getFacet(NB_LEVELS).value(agent.getScope()));
		
		exp.add(new ParameterAdapter("Morris level", IKeyword.MORRIS, IType.STRING) {
				@Override public Object value() { return l; }
		});
		
		exp.add(new ParameterAdapter("Morris sample", IKeyword.MORRIS, IType.STRING) {
			@Override public Object value() { return solutions==null?s*l:(solutions.size()==0?sample:solutions.size()); }
		});
		
	}
	
	// ************************* END OF SUPER CLASS CONTRACT ************************* //

	/**
	 * Convert the output of Gaml so it can be read by the Sobol class
	 *
	 * @param res_outputs
	 *            : output of simulation
	 * @return A map with <br>
	 *         K the name of the output <br>
	 *         V the value of the output
	 */
	private Map<String, List<Double>> rebuildOutput(final IScope scope,
			final IMap<ParametersSet, Map<String, List<Object>>> res_outputs) {
		Map<String, List<Double>> rebuilt_output = new HashMap<>();
		for (String output : outputs) { rebuilt_output.put(output, new ArrayList<>()); }
		for (ParametersSet sol : solutions) {
			for (String output : outputs) {
				try {
					rebuilt_output.get(output).add(Cast.asFloat(scope, res_outputs.get(sol).get(output).get(0)));
				} catch (NullPointerException e) {
					return rebuilt_output;
				}
			}
		}
		return rebuilt_output;
	}

	/**
	 * Save simulation.
	 *
	 * @param rebuilt_output
	 *            the rebuilt output
	 * @param file
	 *            the file
	 * @param scope
	 *            the scope
	 */
	private void saveSimulation(final Map<String, List<Double>> rebuilt_output, final File file, final IScope scope) {
		try (FileWriter fw = new FileWriter(file, false)) {
			fw.write(this.buildSimulationCsv(rebuilt_output));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + file.toString() + " not found", scope);
		}
	}

	/**
	 * Builds the simulation csv.
	 *
	 * @param rebuilt_output
	 *            the rebuilt output
	 * @return the string
	 */
	private String buildSimulationCsv(final Map<String, List<Double>> rebuilt_output) {
		StringBuilder sb = new StringBuilder();
		String sep = ",";
		// Headers
		for (String sol : ParametersNames) { sb.append(sol).append(sep); }
		for (String output : outputs) { sb.append(output).append(sep); }

		sb.deleteCharAt(sb.length() - 1).append(Strings.LN); // new line

		// Values
		for (ParametersSet ps : res_outputs.keySet().stream().toList()) {
			for (String sol : ParametersNames) {
				sb.append(ps.get(sol)).append(sep); // inputs values
			}
			for (String output : outputs) {
				sb.append(res_outputs.get(ps).get(output)).append(sep); // outputs values
			}
			sb.deleteCharAt(sb.length() - 1).append(Strings.LN); // new line
		}
		return sb.toString();
	}

	/**
	 * Builds the parameter sets from CSV.
	 *
	 * @param scope
	 *            the scope
	 * @param path
	 *            the path
	 * @param sets
	 *            the sets
	 * @return the list
	 */
	public List<ParametersSet> buildParameterSetsFromCSV(final IScope scope, final String path,
			final List<ParametersSet> sets) {
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
		} catch (IOException ioe) {
			throw GamaRuntimeException.error("File " + path + " not found", scope);
		}
		MySamples = parameters;
		// morris_analysis.ParametersNames=parameters.get(0).keySet().stream().toList();
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

}
