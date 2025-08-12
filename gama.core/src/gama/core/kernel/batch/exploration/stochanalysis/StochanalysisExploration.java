/*******************************************************************************************************
 *
 * StochanalysisExploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration.stochanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

/**
 * The Class StochanalysisExploration.
 */
@symbol (
		name = { IKeyword.STO },
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
						type = IType.ID,
						optional = true,
						doc = @doc ("The sampling method to build parameters sets. Available methods are: "
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
						doc = @doc ("The number of sample required , 10 by default")),
				@facet (
						name = IKeyword.BATCH_REPORT,
						type = IType.STRING,
						optional = false,
						doc = @doc ("The path to the file where the Sobol report will be written")),
				@facet (
						name = IKeyword.BATCH_OUTPUT,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path to the file where the automatic batch report will be written")),
				@facet (
						name = Exploration.ITERATIONS,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of iteration for orthogonal sampling, 5 by default"))

		},
		omissible = IKeyword.NAME)
@doc (
		value = "This algorithm runs an exploration with a given sampling to compute a Stochasticity Analysis",
		usages = { @usage (
				value = "For example: ",
				examples = { @example (
						value = "method stochanalyse sampling:'latinhypercube' outputs:['my_var'] replicat:10 report:'../path/to/report/file.txt'; ",
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

		if (hasFacet(Exploration.SAMPLE_SIZE)) {
			this.sample_size = Cast.asInt(scope, getFacet(Exploration.SAMPLE_SIZE).value(scope));
		}

		List<ParametersSet> sets = getExperimentPlan(parameters, scope);

		res_outputs = currentExperiment.runSimulationsAndReturnResults(sets);

		outputs = getLitteralOutputs();
		Map<String, Map<ParametersSet, Map<String, List<Double>>>> MapOutput = new LinkedHashMap<>();
		for (String out : outputs) {

			IMap<ParametersSet, List<Object>> sp = GamaMapFactory.create();
			for (ParametersSet ps : res_outputs.keySet()) { sp.put(ps, res_outputs.get(ps).get(out)); }

			Map<ParametersSet, Map<String, List<Double>>> res_val = GamaMapFactory.create();
			for (String m : Stochanalysis.SA) {
				Map<ParametersSet, List<Double>> stoch = Stochanalysis.stochasticityAnalysis(sp, m, scope);
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
		if (hasFacet(IKeyword.BATCH_VAR_OUTPUTS) && hasFacet(IKeyword.BATCH_OUTPUT)) {
			saveRawResults(scope, res_outputs);
		}

		/** If any of the two facet is missing pop up a warning */
		if (hasFacet(IKeyword.BATCH_VAR_OUTPUTS) && hasFacet(IKeyword.BATCH_OUTPUT)) {
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning("Facet "
					+ (hasFacet(IKeyword.BATCH_VAR_OUTPUTS) ? IKeyword.BATCH_OUTPUT : IKeyword.BATCH_VAR_OUTPUTS)
					+ " is missing - corresponding results won't be saved", scope), false);
		}
	}

	@Override
	public void addParametersTo(final List<Batch> exp, final BatchAgent agent) {
		super.addParametersTo(exp, agent);
	}

}
