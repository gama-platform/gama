/*******************************************************************************************************
 *
 * SobolExploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration.sobol;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import gama.core.common.util.FileUtils;
import gama.core.kernel.batch.exploration.AExplorationAlgorithm;
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.kernel.experiment.ParameterAdapter;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.operators.Cast;
import gama.gaml.types.GamaDateType;
import gama.gaml.types.IType;

/**
 *
 *
 * @author kevinchapuis
 *
 */
@symbol (
		name = IKeyword.SOBOL,
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
						name = SobolExploration.SAMPLE_SIZE,
						type = IType.ID,
						optional = false,
						doc = @doc ("The size of the sample for the sobol sequence")),
				@facet (
						name = IKeyword.BATCH_VAR_OUTPUTS,
						type = IType.LIST,
						of = IType.STRING,
						optional = false,
						doc = @doc ("The list of output variables to analyse through sobol indexes")),
				@facet (
						name = IKeyword.BATCH_OUTPUT,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path to the file where the automatic batch report will be written")),
				@facet (
						name = IKeyword.BATCH_REPORT,
						type = IType.STRING,
						optional = false,
						doc = @doc ("The path to the file where the Sobol report will be written")),
				@facet (
						name = IKeyword.PATH,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path to the saltelli sample csv file. If the file doesn't exist automatic Saltelli sampling will be performed and saved in the corresponding location")) },
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm runs a Sobol exploration - it has been built upon the moea framework at https://github.com/MOEAFramework/MOEAFramework - disabled the repeat facet of the experiment",
		usages = { @usage (
				value = "For example: ",
				examples = { @example (
						value = "method sobol sample_size:100 outputs:['my_var'] report:'../path/to/report/file.txt'; ",
						isExecutable = false) }) })
public class SobolExploration extends AExplorationAlgorithm {

	/** The Constant SAMPLE_SIZE. */
	protected static final String SAMPLE_SIZE = "sample";

	/** map containing the output of sobol methode */
	private Sobol sobol_analysis;

	/** The parameters. */
	private List<Batch> parameters;

	/** The outputs */
	private IList<String> outputs;

	/** The current parameters space. */
	/* The parameter space defined by the Sobol sequence (Satteli sampling method) */
	private List<ParametersSet> solutions;

	/** The res outputs. */
	/* All the outputs for each simulation */
	protected IMap<ParametersSet, Map<String, List<Object>>> res_outputs;

	/** The sample. */
	private int _sample;

	/**
	 * Instantiates a new sobol exploration.
	 *
	 * @param desc
	 *            the desc
	 */
	public SobolExploration(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	@SuppressWarnings ("unchecked")
	@Override
	public void explore(final IScope scope) {
		List<ParametersSet> solutions =
				this.solutions == null ? buildParameterSets(scope, new ArrayList<>(), 0) : this.solutions;
		if (solutions.size() != _sample) throw GamaRuntimeException
				.error("Saltelli sample should be " + _sample + " but is " + solutions.size(), scope);

		/* Disable repetitions / repeat argument */
		currentExperiment.setSeeds(new Double[1]);
		// TODO : why doesnt it take into account the value of 'keep_simulations:' ?
		currentExperiment.setKeepSimulations(false);
		if (GamaExecutorService.shouldRunAllSimulationsInParallel(currentExperiment)) {
			res_outputs = currentExperiment.launchSimulationsWithSolution(solutions);
		} else {
			res_outputs = GamaMapFactory.create();
			for (ParametersSet sol : solutions) {
				res_outputs.put(sol, currentExperiment.launchSimulationsWithSolution(sol));
			}
		}

		Map<String, List<Object>> rebuilt_output = rebuildOutput(res_outputs);

		sobol_analysis.setOutputs(rebuilt_output);
		sobol_analysis.evaluate();

		/* Save the simulation values in the provided .csv file (input and corresponding output) */
		if (hasFacet(IKeyword.BATCH_OUTPUT)) {
			String path_to = Cast.asString(scope, outputFilePath.value(scope));
			final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
			final File parent = f.getParentFile();
			if (!parent.exists()) { parent.mkdirs(); }
			if (f.exists()) { f.delete(); }
			sobol_analysis.saveSimulation(f);
		}

		/* Save the Sobol analysis report in a .txt file */
		String path_to = Cast.asString(scope, getFacet(IKeyword.BATCH_REPORT).value(scope));
		final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
		final File parent = f.getParentFile();
		if (!parent.exists()) { parent.mkdirs(); }
		if (f.exists()) { f.delete(); }
		sobol_analysis.saveResult(f);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public List<ParametersSet> buildParameterSets(final IScope scope, final List<ParametersSet> sets, final int index) {
		int sample = Cast.asInt(scope, getFacet(SAMPLE_SIZE).value(scope));
		// Do not trust getExplorableParameter of the BatchAgent
		// Needs a step to explore a parameter, also for any sampling methods only min/max is required
		List<Batch> params = new ArrayList<>(currentExperiment.getParametersToExplore());
		parameters = parameters == null ? params : parameters;
		/* times 2 the number of parameters for the bootstraping (Saltelli 2002) and +2 because of sample A & B */
		_sample = sample * (2 * parameters.size() + 2);

		// Retrieve the list of parameters from the batch experiment
		LinkedHashMap<String, List<Object>> problem = new LinkedHashMap<>();
		for (int j = 0; j < parameters.size(); j++) {
			List<Object> var_info = new ArrayList<>();

			switch (parameters.get(j).getType().id()) {
				case IType.INT, IType.FLOAT:
					var_info.add(parameters.get(j).getMinValue(scope));
					var_info.add(parameters.get(j).getMaxValue(scope));
					break;
				case IType.BOOL:
					var_info.add(false);
					var_info.add(true);
					break;
				case IType.DATE:
					var_info.add(GamaDateType.staticCast(scope, parameters.get(j).getMinValue(scope), null, false));
					var_info.add(GamaDateType.staticCast(scope, parameters.get(j).getMaxValue(scope), null, false));
					break;
				case IType.POINT:
					var_info.add(Cast.asPoint(scope, parameters.get(j).getMinValue(scope)));
					var_info.add(Cast.asPoint(scope, parameters.get(j).getMaxValue(scope)));
					break;
				case IType.STRING:
					if (parameters.get(j).getAmongValue(scope).isEmpty()) throw GamaRuntimeException
							.error("Trying to force a string variable in sampling without among facets", scope);
					var_info.addAll(parameters.get(j).getAmongValue(scope));
					break;
				default:
					throw GamaRuntimeException.error("Trying to add a variable of unknown type "
							+ parameters.get(j).getType().id() + " to a parameter set", scope);
			}

			problem.put(parameters.get(j).getName(), var_info);
		}

		// Get the output variables of the sobol batch experiment
		outputs = Cast.asList(scope, getFacet(IKeyword.BATCH_VAR_OUTPUTS).value(scope));

		// Build a sobol object
		sobol_analysis = new Sobol(problem, outputs, sample, scope);

		// Path to saltelli sample
		if (hasFacet(IKeyword.PATH)) {
			String path = Cast.asString(scope, getFacet(IKeyword.PATH).value(scope));
			final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path, false));

			// Use the saltelli sequence provided...
			if (f.exists()) {
				// DEBUG.OUT("Sample used : " + path);
				sobol_analysis.setSaltelliSamplingFromCsv(f);
			}
			// ... or build the saltelli sequence automatically and save it into a file
			else {
				// DEBUG.OUT("Automatic sampling used");
				sobol_analysis.setRandomSaltelliSampling();
				sobol_analysis.saveSaltelliSample(f);
			}
		} else {
			// No path provided use random saltelli sampling and nothing is saved
			sobol_analysis.setRandomSaltelliSampling();
		}

		// Add the points to explore to the solutions set
		Map<String, List<Object>> sample2 = sobol_analysis.getParametersValues();

		for (int i = 0; i < _sample; i++) {
			ParametersSet origi = new ParametersSet();

			for (Batch param : parameters) { origi.put(param.getName(), sample2.get(param.getName()).get(i)); }

			sets.add(origi);
		}

		this.solutions = sets;

		return sets;
	}

	@Override
	public void addParametersTo(final List<Batch> exp, final BatchAgent agent) {
		super.addParametersTo(exp, agent);

		exp.add(new ParameterAdapter("Saltelli sample", IKeyword.SOBOL, IType.STRING) {
			@Override
			public Object value() {
				return _sample;
			}
		});

	}

	/**
	 * Convert the output of Gaml so it can be read by the Sobol class
	 *
	 * @param res_outputs
	 *            : output of simulation
	 * @return A map with <br>
	 *         - K the name of the output <br>
	 *         - V the value of the output
	 */
	private Map<String, List<Object>> rebuildOutput(final IMap<ParametersSet, Map<String, List<Object>>> res_outputs) {
		Map<String, List<Object>> rebuilt_output = new HashMap<>();
		for (String output : outputs) { rebuilt_output.put(output, new ArrayList<>()); }

		for (ParametersSet sol : solutions) {
			for (String output : outputs) {
				try {
					rebuilt_output.get(output).add(res_outputs.get(sol).get(output).get(0));
				} catch (NullPointerException e) {
					return rebuilt_output;
				}
			}
		}
		return rebuilt_output;
	}
}
