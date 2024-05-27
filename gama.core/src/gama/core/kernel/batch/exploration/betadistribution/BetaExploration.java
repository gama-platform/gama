/*******************************************************************************************************
 *
 * BetaExploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration.betadistribution;

import java.io.File;
import java.io.FileWriter;
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
import gama.core.kernel.batch.exploration.sampling.LatinhypercubeSampling;
import gama.core.kernel.batch.exploration.sampling.MorrisSampling;
import gama.core.kernel.batch.exploration.sampling.OrthogonalSampling;
import gama.core.kernel.batch.exploration.sampling.RandomSampling;
import gama.core.kernel.batch.exploration.sampling.SaltelliSampling;
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
import gama.gaml.operators.Strings;
import gama.gaml.types.IType;

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
						name = Exploration.METHODS,
						type = IType.ID,
						optional = false,
						doc = @doc ("The sampling method to build parameters sets that must be factorial based to some extends - available are saltelli and default uniform")),
				@facet (
						name = IKeyword.BATCH_VAR_OUTPUTS,
						type = IType.LIST,
						of = IType.STRING,
						optional = false,
						doc = @doc ("The list of output variables to analyse")),
				@facet (
						name = Exploration.SAMPLE_SIZE,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of sample required.")),
				@facet (
						name = Exploration.SAMPLE_FACTORIAL,
						type = IType.LIST,
						of = IType.INT,
						optional = true,
						doc = @doc ("The number of automated steps to swip over, when step facet is missing in parameter definition. Default is 9")),
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

		// == Parameters ==

		List<Batch> params = currentExperiment.getParametersToExplore().stream()
				.filter(p -> p.getMinValue(scope) != null && p.getMaxValue(scope) != null).map(p -> p).toList();

		parameters = parameters == null ? params : parameters;
		List<ParametersSet> sets;

		int sample_size = (int) Math.round(Math.pow(params.size(), 2) * 2);
		if (hasFacet(Exploration.SAMPLE_SIZE)) {
			sample_size = Cast.asInt(scope, getFacet(Exploration.SAMPLE_SIZE).value(scope));
		}

		// == Build sample of parameter inputs ==

		String method = Cast.asString(scope, getFacet(Exploration.METHODS).value(scope));
		sets = switch (method) {
			case IKeyword.MORRIS: yield MorrisSampling.makeMorrisSamplingOnly(4, sample_size, params, scope);
			case IKeyword.LHS: yield LatinhypercubeSampling.latinHypercubeSamples(sample_size, params, scope.getRandom().getGenerator(), scope);
			case IKeyword.ORTHOGONAL: yield OrthogonalSampling.orthogonalSamples(sample_size, OrthogonalSampling.DEFAULT_ITERATION, params, scope.getRandom().getGenerator(), scope);
			case IKeyword.SALTELLI: yield SaltelliSampling.makeSaltelliSampling(scope, sample_size, parameters);
			case IKeyword.UNIFORM: yield RandomSampling.uniformSampling(scope, sample_size, parameters);
			default: 
				List<ParametersSet> ps = null;
				if (hasFacet(Exploration.SAMPLE_FACTORIAL)) {
					int[] factors = Cast.asList(scope, getFacet(Exploration.SAMPLE_FACTORIAL).value(scope)).stream()
							.mapToInt(o -> Integer.parseInt(o.toString())).toArray();
					ps = RandomSampling.factorialUniformSampling(scope, factors, params);
				} else {
					ps = RandomSampling.factorialUniformSampling(scope, sample_size, params);
				}
				yield ps;
		};

		// == Launch simulations ==
		currentExperiment.setSeeds(new Double[1]);
		// TODO : why doesn't it take into account the value of 'keep_simulations:' ?
		currentExperiment.setKeepSimulations(false);
		if (GamaExecutorService.shouldRunAllSimulationsInParallel(currentExperiment)) {
			res_outputs = currentExperiment.launchSimulationsWithSolution(sets);
		} else {
			res_outputs = GamaMapFactory.create();
			for (ParametersSet sol : sets) {
				res_outputs.put(sol, currentExperiment.launchSimulationsWithSolution(sol));
			}
		}

		outputs = Cast.asList(scope, getFacet(IKeyword.BATCH_VAR_OUTPUTS).value(scope));

		Map<String, Map<Batch, Double>> res = new HashMap<>();
		for (String out : outputs) {
			IMap<ParametersSet, List<Object>> sp = GamaMapFactory.create();
			for (ParametersSet ps : res_outputs.keySet()) { sp.put(ps, res_outputs.get(ps).get(out)); }
			Betadistribution bs = new Betadistribution(sp, parameters, out);
			res.put(out, bs.evaluate());
		}

		/* Save the simulation values in the provided .csv file (input and corresponding output) */
		if (hasFacet(IKeyword.BATCH_OUTPUT)) {
			 String path_to = Cast.asString(scope, getFacet(IKeyword.BATCH_OUTPUT).value(scope));
			final File fo = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
			final File parento = fo.getParentFile();
			if (!parento.exists()) { parento.mkdirs(); }
			if (fo.exists()) { fo.delete(); }
			saveSimulation(fo, scope);
		}
		
		String path_to = Cast.asString(scope, getFacet(IKeyword.BATCH_REPORT).value(scope));
		final File f = new File(FileUtils.constructAbsoluteFilePath(scope, path_to, false));
		final File parent = f.getParentFile();
		try (FileWriter fw = new FileWriter(f, false)) {
			if (!parent.exists()) { parent.mkdirs(); }
			if (f.exists()) { f.delete(); }
			fw.write(buildReportString(res,FileNameUtils.getExtension(f.getPath())));
		} catch (Exception e) {
			throw GamaRuntimeException.error("File " + f.toString() + " not found", scope);
		}

	}

	@Override
	public List<ParametersSet> buildParameterSets(final IScope scope, final List<ParametersSet> sets, final int index) { return null; }

	@Override
	public void addParametersTo(List<Batch> exp, BatchAgent agent) {
		super.addParametersTo(exp, agent);

		exp.add(new ParameterAdapter("Sampled points", IKeyword.BETAD, IType.STRING) {
				@Override public Object value() { return Cast.asInt(agent.getScope(), 
						getFacet(Exploration.SAMPLE_SIZE).value(agent.getScope())); }
		});

		exp.add(new ParameterAdapter("Sampling method", IKeyword.BETAD, IType.STRING) {
			@Override public Object value() {
				return hasFacet(Exploration.METHODS) ? 
						Cast.asString(agent.getScope(), getFacet(Exploration.METHODS).value(agent.getScope())) : "exhaustive";
			}
		});
		
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
	private void saveSimulation(final File file, final IScope scope) throws GamaRuntimeException {
		try (FileWriter fw = new FileWriter(file, false)) {
			fw.write(this.buildSimulationCsv());
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
	private String buildSimulationCsv() {
		StringBuilder sb = new StringBuilder();
		String sep = ",";
		
		List<String> paramnames = parameters.stream().map(b -> b.getName()).toList();
		
		// Headers
		sb.append(String.join(sep, paramnames));
		sb.append(sep);
		sb.append(String.join(sep, outputs));
		sb.append(Strings.LN);

		// Values
		for (ParametersSet ps : res_outputs.keySet().stream().toList()) {
			for (String sol : paramnames) { sb.append(ps.get(sol)).append(sep); } // inputs values
			for (String output : outputs) { 
				sb.append(res_outputs.get(ps).get(output).get(0)).append(sep); 
			} // outputs values
			sb.deleteCharAt(sb.length() - 1).append(Strings.LN); // new line
		}
		return sb.toString();
	}
	
	/**
	 * Builds the report string.
	 *
	 * @param res
	 *            the res
	 * @return the string
	 */
	public String buildReportString(final Map<String, Map<Batch, Double>> res, final String extension) {
		StringBuilder sb = new StringBuilder();
		String sep = ",";
		
		if ("txt".equalsIgnoreCase(extension)) {
		
			sb.append("BETA b Kuiper based estimator :").append(Strings.LN);
			sb.append("##############################").append(Strings.LN);
			sb.append("inputs" + sep + String.join(sep, outputs)).append(Strings.LN);
			String line = "";
			for (Batch param : parameters) {
				line = param.getName();
				for (String output_name : outputs) { line = line + sep + res.get(output_name).get(param).toString(); }
				sb.append(line).append(Strings.LN);
			}
		} else {
			
			// Build header
			sb.append("output").append(sep);
			sb.append("parameter").append(sep); 
			sb.append("\u03B2").append(Strings.LN);
			
			for (String output_name : outputs) {
				for (Batch param : parameters) {
					// The output & parameter
					sb.append(output_name).append(sep);
					sb.append(param.getName()).append(sep);
					sb.append(res.get(output_name).get(param)).append(Strings.LN);
				}
			}
			
		}
		return sb.toString();
	}

}
