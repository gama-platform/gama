/*******************************************************************************************************
 *
 * MorrisExploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.FileNameUtils;

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
import gama.core.common.util.FileUtils;
import gama.core.kernel.batch.exploration.AExplorationAlgorithm;
import gama.core.kernel.batch.exploration.Exploration;
import gama.core.kernel.batch.exploration.sampling.MorrisSampling;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
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
						name = MorrisExploration.NB_LEVELS,
						type = IType.ID,
						optional = true,
						doc = @doc ("Number of level each trajectories is made of, can't be 1 / should be even - 4 by default")),
				@facet (
						name = IKeyword.BATCH_VAR_OUTPUTS,
						type = IType.LIST,
						of = IType.STRING,
						optional = false,
						doc = @doc ("The list of output variables to analyze through morris method")),
				@facet (
						name = Exploration.SAMPLE_SIZE,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of trajectories , 10 by default - usually between 5 and 15 but should be relative (positive correlation) to the number of parameters")),
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
						value = "method morris nb_levels:4 outputs:['my_var'] report:'../path/to/report.txt;",
						isExecutable = false) }) })

public class MorrisExploration extends AExplorationAlgorithm {

	/** The Constant NB_LEVELS */
	public static final String NB_LEVELS = "levels";

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

	/** The samples. */
	private List<Map<String, Object>> samples;

	/** Main Morris **/
	private Morris momo;

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

	/** ######################### EVALUATE MORRIS INDEXES ######################### */

	@Override
	public void explore(final IScope scope) {
		this.sample =  hasFacet(Exploration.SAMPLE_SIZE) ? 
				Cast.asInt(scope, getFacet(Exploration.SAMPLE_SIZE).value(scope)) : Morris.DEFAULT_TRAJECTORIES;
		this.nb_levels = hasFacet(NB_LEVELS) ? Cast.asInt(scope, getFacet(NB_LEVELS).value(scope)) : Morris.DEFAULT_LEVELS;
		if (hasFacet(PARAMETER_CSV_PATH)) {
			IExpression path_facet = getFacet(PARAMETER_CSV_PATH);
			String path =
					FileUtils.constructAbsoluteFilePath(scope, Cast.asString(scope, path_facet.value(scope)), false);
			this.solutions =
					this.solutions == null ? buildParameterSetsFromCSV(scope, path, new ArrayList<>()) : this.solutions;
		} else {
			this.solutions = buildParameterSets(scope, new ArrayList<>(), 0);
		}
		/* Disable repetitions / repeat argument */
		currentExperiment.setSeeds(new Double[1]);
		// TODO : why doesn't it take into account the value of 'keep_simulations:' ? because, by design, there is to
		// many simulation to keep in memory...
		currentExperiment.setKeepSimulations(false);
		res_outputs = currentExperiment.runSimulationsAndReturnResults(solutions);

		// The output of simulations
		Map<String, List<Double>> rebuilt_output = rebuildOutput(scope, res_outputs);
		momo.setOutputs(rebuilt_output, scope);

		// TODO : verify if Morris sampling can lead to several identical points in the parameter space
		int outsize = 0;
		for (Map<String, List<Object>> m : res_outputs.values()) {
			outsize += m.values().stream().findFirst().get().size();
		}

		/* Save the simulation values in the provided .csv file (input and corresponding output) */
		if (hasFacet(IKeyword.BATCH_OUTPUT)) { saveRawResults(scope, res_outputs); }

		// Prevent OutOfBounds when experiment ends before morris exploration is completed
		if (outsize == samples.size() && rebuilt_output.values().stream().findAny().get().size() == samples.size()) {

			momo.evaluate();

			String path_to = Cast.asString(scope, getFacet(IKeyword.BATCH_REPORT).value(scope));
			final File fm = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
			final File parentm = fm.getParentFile();
			if (!parentm.exists()) { parentm.mkdirs(); }
			if (fm.exists()) { fm.delete(); }
			saveResults(fm, scope);

		}
	}

	/** ######################### EXPLORATION OVERRIDES ######################### */

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
		outputs = getLitteralOutputs();

		// Puck Fython
		List<Object> morris_samplings = MorrisSampling.makeMorrisSampling(nb_levels, this.sample, parameters, scope);

		// Design sample to be used by Morris
		this.samples = Cast.asList(scope, morris_samplings.get(0));
		momo = new Morris(this.samples, this.nb_levels);

		// Same sample to execute in Gama experiment
		return Cast.asList(scope, morris_samplings.get(1));
	}

	@Override
	public void addParametersTo(final List<Batch> exp, final BatchAgent agent) {
		super.addParametersTo(exp, agent);

		exp.add(new ParameterAdapter("Morris level", IKeyword.MORRIS, IType.STRING) {
			@Override
			public Object value() {
				return Cast.asInt(agent.getScope(), getFacet(NB_LEVELS).value(agent.getScope()));
			}
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
	 * Save the report of the Sobol analysis (sobol indexes) in a .csv file
	 *
	 * @param file
	 *            : .csv file
	 */
	private void saveResults(final File file, final IScope scope) throws GamaRuntimeException {
		try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8, false)) {
			fw.write(momo.buildReportString(FileNameUtils.getExtension(file.getPath())));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + file.toString() + " not found", scope);
		}
	}

	/**
	 * Builds the parameter sets from CSV.
	 *
	 * TODO : create a Morris instance !!!
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
		} catch (IOException ioe) {
			throw GamaRuntimeException.error("File " + path + " not found", scope);
		}
		samples = parameters;
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
