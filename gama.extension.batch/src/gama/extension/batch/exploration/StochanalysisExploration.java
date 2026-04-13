/*******************************************************************************************************
 *
 * StochanalysisExploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.batch.exploration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import gama.extension.stats.Stochanalysis;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IParameter.Batch;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExploration;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.files.FileUtils;
import gama.core.experiment.parameters.ParametersSet;

/**
 * The Class StochanalysisExploration.
 */
@symbol (
		name = { IExploration.STO },
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
						optional = true,
						doc = @doc ("The SAMPLING method to build parameters sets. Available methods are: "
								+ IKeyword.LHS + ", " + IKeyword.ORTHOGONAL + ", " + IKeyword.FACTORIAL + ", "
								+ IKeyword.UNIFORM + ", " + IKeyword.SALTELLI + ", " + IExploration.MORRIS)),
				@facet (
						name = IKeyword.BATCH_VAR_OUTPUTS,
						type = IType.LIST,
						optional = false,
						doc = @doc ("The list of output variables to analyse")),
				@facet (
						name = IKeyword.BATCH_RAW_RESULTS,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path to the file where the raw results will be written")),
				@facet (
						name = Exploration.SAMPLE_SIZE,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of sample required , 10 by default")),
				@facet (
						name = IKeyword.BATCH_REPORT,
						type = IType.STRING,
						optional = false,
						doc = @doc ("The path to the file where the Stochasticity Analysis report will be written"))

		},
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm runs an exploration with a given SAMPLING to compute a Stochasticity Analysis",
		usages = { @usage (
				value = "For example: ",
				examples = { @example (
						value = "method stochanalyse SAMPLING:'latinhypercube' outputs:['my_var'] replicat:10 report:'../path/to/report/file.txt'; ",
						isExecutable = false) }) })
public class StochanalysisExploration extends AExplorationAlgorithm {

	/** Theoretical inputs */
	private List<Batch> parameters;
	/** Theoretical outputs */
	private IList<String> outputs;
	/** Actual input / output map */
	protected IMap<ParametersSet, Map<String, List<Object>>> res_outputs;

	/**
	 * Instantiates a new stochanalysis exploration.
	 *
	 * @param desc
	 *            the desc
	 */
	public StochanalysisExploration(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	@SuppressWarnings ("unchecked")
	@Override
	public void explore(final IScope scope) throws GamaRuntimeException {

		parameters = parameters == null ? new ArrayList<>(currentExperiment.getParametersToExplore()) : parameters;

		if (hasFacet(IExploration.SAMPLE_SIZE)) {
			this.sample_size = Cast.asInt(scope, getFacet(IExploration.SAMPLE_SIZE).value(scope));
		}

		List<ParametersSet> sets = getExperimentPlan(parameters, scope);

		res_outputs = currentExperiment.runSimulationsAndReturnResults(sets);

		outputs = getLitteralOutputs();
		Map<String, Map<ParametersSet, Map<String, List<Double>>>> MapOutput = new LinkedHashMap<>();
		for (String out : outputs) {

			IMap<ParametersSet, IList<Object>> sp = GamaMapFactory.create();
			for (ParametersSet ps : res_outputs.keySet()) { 
				sp.put(ps, res_outputs.get(ps).get(out).stream()
						.mapToDouble(d -> Cast.asFloat(scope, d)).boxed().collect(GamaListFactory.toGamaList())); 
			}

			Map<ParametersSet, Map<String, List<Double>>> res_val = GamaMapFactory.create();
			for (String m : Stochanalysis.SA) {
				IMap<ParametersSet, IList<Double>> stoch = Stochanalysis.stochasticityAnalysis(sp, m, scope);
				for (ParametersSet p : sp.keySet()) {
					if (!res_val.containsKey(p)) {
						res_val.put(p, Stochanalysis.SA.stream()
								.collect(Collectors.toMap(Function.identity(), i -> new ArrayList<Double>())));
					}
					res_val.get(p).put(m, stoch.get(p));
				}

			}
			MapOutput.put(out, res_val);
		}

		// Build report
		String path = Cast.asString(scope, getFacet(IKeyword.BATCH_REPORT).value(scope));
		final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path, false));
		final File parent = f.getParentFile();
		if (!parent.exists()) { parent.mkdirs(); }
		if (f.exists()) { f.delete(); }
		Stochanalysis.writeAndTellReport(f, MapOutput, sample_size, currentExperiment.getSeeds().length, scope);

		/* Save the simulation values in the provided .csv file (input and corresponding output) */
		if (hasFacet(IKeyword.BATCH_VAR_OUTPUTS) && hasFacet(IKeyword.BATCH_RAW_RESULTS)) {
			saveRawResults(scope, res_outputs);
		}

		/** If any of the two facet is missing pop up a warning */
		if (!(hasFacet(IKeyword.BATCH_VAR_OUTPUTS) && hasFacet(IKeyword.BATCH_RAW_RESULTS))) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning(
					"Facet " + (hasFacet(IKeyword.BATCH_VAR_OUTPUTS) ? IKeyword.BATCH_RAW_RESULTS
							: IKeyword.BATCH_VAR_OUTPUTS) + " is missing - corresponding results won't be saved",
					scope), false);
		}
	}

	@Override
	public void addParametersTo(final List<Batch> exp, final IExperimentAgent.Batch agent) {
		super.addParametersTo(exp, agent);
	}

}
