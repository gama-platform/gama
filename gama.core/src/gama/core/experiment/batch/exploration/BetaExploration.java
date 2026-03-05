/*******************************************************************************************************
 *
 * BetaExploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.experiment.batch.exploration;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter.Batch;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExploration;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.StringUtils;
import gama.api.utils.files.FileUtils;
import gama.core.experiment.parameters.ParametersSet;

/**
 *
 *
 * @author kevinchapuis
 *
 *         Coefficient derived from the work below:
 *
 *         E. Borgonovo, M. Pangallo, J. Rivkin, L. Rizzo, and N. Siggelkow, “Sensitivity analysis of agent-based
 *         models: a new protocol,” Comput. Math. Organ. Theory, vol. 28, no. 1, pp. 52–94, Mar. 2022, doi:
 *         10.1007/s10588-021-09358-5.
 *
 */
@symbol (
		name = IKeyword.BETAD,
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
						name = Exploration.SAMPLING,
						type = IType.ID,
						optional = false,
						doc = @doc ("The sampling method to build parameters sets that must be factorial based to some extends - available are: "
								+ IKeyword.LHS + ", " + IKeyword.ORTHOGONAL + ", " + IKeyword.FACTORIAL + ", "
								+ IKeyword.UNIFORM + ", " + IKeyword.SALTELLI + ", " + IKeyword.MORRIS)),
				@facet (
						name = IKeyword.BATCH_VAR_OUTPUTS,
						type = IType.LIST,
						optional = false,
						doc = @doc ("The list of output variables to analyse")),
				@facet (
						name = Exploration.SAMPLE_SIZE,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of sample required.")),
				@facet (
						name = BetaExploration.BOOTSTRAP,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of time each parameter value is boostraped (or resampled in another context)")),
				@facet (
						name = IKeyword.BATCH_OUTPUT,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path to the file where the automatic batch report will be written")),
				@facet (
						name = IKeyword.BATCH_REPORT,
						type = IType.STRING,
						optional = false,
						doc = @doc ("The path to the file where the Betad report will be written")) },
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm runs an exploration with a given sampling to compute BetadKu - see doi: 10.1007/s10588-021-09358-5",
		usages = { @usage (
				value = "For example: ",
				examples = { @example (
						value = "method sobol sample_size:100 outputs:['my_var'] report:'../path/to/report/file.txt'; ",
						isExecutable = false) }) })
public class BetaExploration extends AExplorationAlgorithm {

	/** The bootstrap. */
	private int bootstrap = Betadistribution.DEFAULT_BOOTSTRAP;

	/** The Constant BOOTSTRAP. */
	public static final String BOOTSTRAP = "bootstrap";

	/** Theoretical inputs */
	private List<Batch> parameters;
	/** Theoretical outputs */
	private IList<String> outputs;
	/** Actual input / output map */
	protected IMap<ParametersSet, Map<String, List<Object>>> res_outputs;

	/**
	 * Instantiates a new beta exploration.
	 *
	 * @param desc
	 *            the desc
	 */
	public BetaExploration(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	@SuppressWarnings ("unchecked")
	@Override
	public void explore(final IScope scope) {

		parameters = parameters == null ? getParams(currentExperiment) : parameters;

		if (hasFacet(IExploration.SAMPLE_SIZE)) {
			sample_size = Cast.asInt(scope, getFacet(IExploration.SAMPLE_SIZE).value(scope));
		}
		if (sample_size < 1) { sample_size = 2; }

		// == Build sample of parameter inputs ==
		List<ParametersSet> sets = getExperimentPlan(parameters, scope);

		// TODO : expend parameter set to include variation over target input,
		// ====> i.e. various parameter combinations for one parameter value
		// to assess how simulation behave when a parameter stay the same, while everything
		// else is moving
		sets = expendExperimentPlan(sets, scope);

		// TODO : why doesn't it take into account the value of 'keep_simulations:' ?
		currentExperiment.setKeepSimulations(false);
		res_outputs = currentExperiment.runSimulationsAndReturnResults(sets);

		outputs = getLitteralOutputs();

		Map<String, Map<Batch, Double>> res = new HashMap<>();
		for (String out : outputs) {
			IMap<ParametersSet, List<Object>> sp = GamaMapFactory.create();
			for (ParametersSet ps : res_outputs.keySet()) { sp.put(ps, res_outputs.get(ps).get(out)); }
			Betadistribution bs = new Betadistribution(sp, parameters);
			res.put(out, bs.evaluate());
		}

		/* Save the simulation values in the provided .csv file (input and corresponding output) */
		if (hasFacet(IKeyword.BATCH_OUTPUT)) { saveRawResults(scope, res_outputs); }

		String path_to = Cast.asString(scope, getFacet(IKeyword.BATCH_REPORT).value(scope));
		final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
		final File parent = f.getParentFile();
		if (!parent.exists()) { parent.mkdirs(); }
		if (f.exists()) { f.delete(); }
		try (FileWriter fw = new FileWriter(f, false)) {
			fw.write(buildReportString(res, FilenameUtils.getExtension(f.getPath())));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + f.toString() + " not found", scope);
		}

	}

	@Override
	public void addParametersTo(final List<Batch> exp, final IExperimentAgent.Batch agent) {
		super.addParametersTo(exp, agent);
	}

	// ================================== //

	/**
	 * Get back the list of 'explorable' parameters (numerical variable with min and max values)
	 *
	 * @param xp
	 * @return
	 */
	private List<Batch> getParams(final IExperimentAgent.Batch xp) {
		return xp.getParametersToExplore().stream().map(p -> p).toList();
	}

	/**
	 * Duplicates values of parameter to put them in various context
	 *
	 * @param sets
	 * @param scope
	 * @return
	 */
	private List<ParametersSet> expendExperimentPlan(final List<ParametersSet> sets, final IScope scope) {

		List<ParametersSet> returnedSet = new ArrayList<>(sets);

		if (hasFacet(BOOTSTRAP)) { bootstrap = Cast.asInt(scope, getFacet(BOOTSTRAP).value(scope)); }

		// For each parameter, duplicates 'fact' times all sampled values
		for (Batch b : parameters) {

			// Target the parameter values and put them in another parameter context
			for (ParametersSet ps : sets) {
				List<ParametersSet> subspace = new ArrayList<>(sets);
				subspace.remove(ps);
				for (int i = 0; i < bootstrap; i++) {
					ParametersSet cross =
							new ParametersSet(subspace.remove((int) scope.getRandom().next() * subspace.size()));
					cross.addValueAtIndex(scope, b, ps.get(b.getName()));
					returnedSet.add(cross);
				}
			}
		}

		return returnedSet;
	}

	// ================================== //

	/**
	 * Builds the report string.
	 *
	 * @param res
	 *            the res
	 * @return the string
	 */
	public String buildReportString(final Map<String, Map<Batch, Double>> res, final String extension) {
		StringBuilder sb = new StringBuilder();

		if ("txt".equalsIgnoreCase(extension)) {

			sb.append("BETA b Kuiper based estimator :").append(StringUtils.LN);
			sb.append("##############################").append(StringUtils.LN);
			sb.append("inputs" + AExplorationAlgorithm.CSV_SEP + String.join(AExplorationAlgorithm.CSV_SEP, outputs))
					.append(StringUtils.LN);
			for (Batch param : parameters) {
				sb.append(param.getName());
				for (String output_name : outputs) {
					sb.append(AExplorationAlgorithm.CSV_SEP).append(res.get(output_name).get(param));
				}
				sb.append(StringUtils.LN);
			}

		} else {

			// Build header
			sb.append("output").append(AExplorationAlgorithm.CSV_SEP);
			sb.append("parameter").append(AExplorationAlgorithm.CSV_SEP);
			sb.append("\u03B2").append(StringUtils.LN);

			for (String output_name : outputs) {
				for (Batch param : parameters) {
					// The output & parameter
					sb.append(output_name).append(AExplorationAlgorithm.CSV_SEP);
					sb.append(param.getName()).append(AExplorationAlgorithm.CSV_SEP);
					sb.append(res.get(output_name).get(param)).append(StringUtils.LN);
				}
			}

		}

		return sb.toString();
	}

}
