/*******************************************************************************************************
 *
 * Exploration.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.batch.exploration;

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
import gama.core.kernel.experiment.BatchAgent;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;

/**
 * The Class ExhaustiveSearch.
 */
@symbol (
		name = { IKeyword.EXPLORATION },
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
						type = IType.STRING,
						optional = true,
						doc = @doc ("The name of the sampling method among: " + IKeyword.LHS + ", "
								+ IKeyword.ORTHOGONAL + ", " + IKeyword.FACTORIAL + ", " + IKeyword.UNIFORM + ", "
								+ IKeyword.SALTELLI + ", " + IKeyword.MORRIS)),
				@facet (
						name = IKeyword.FROM,
						type = IType.STRING,
						optional = true,
						doc = @doc ("a path to a file where each lines correspond to one parameter set and each colon a parameter")),
				@facet (
						name = IKeyword.WITH,
						type = IType.LIST,
						of = IType.MAP,
						optional = true,
						doc = @doc ("the list of parameter sets to explore; a parameter set is defined by a map: key: name of the variable, value: expression for the value of the variable")),
				@facet (
						name = IKeyword.BATCH_VAR_OUTPUTS,
						type = IType.LIST,
						of = IType.STRING,
						optional = true,
						doc = @doc ("The list of output variables to track throughout exploration")),
				@facet (
						name = IKeyword.BATCH_OUTPUT,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The path to the file where the automatic batch report will be written")),
				@facet (
						name = Exploration.SAMPLE_SIZE,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of sample required, 132 by default")),
				@facet (
						name = Exploration.SAMPLE_FACTORIAL,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of slice (value) applied to each parameter to build the factorial experimental plan.")),
				@facet (
						name = Exploration.NB_LEVELS,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of levels for morris sampling, 4 by default")),
				@facet (
						name = Exploration.ITERATIONS,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of iteration for orthogonal sampling, 5 by default"))

		},
		omissible = IKeyword.NAME)
@doc (
		value = "This is the standard batch method. The exploration mode is defined by default when there is no method element present in the batch section. It explores all the combination of parameter values in a sequential way. You can also choose a sampling method for the exploration. See [batch161 the batch dedicated page].",
		usages = { @usage (
				value = "As other batch methods, the basic syntax of the exploration statement uses `method exploration` instead of the expected `exploration name: id` : ",
				examples = { @example (
						value = "method exploration;",
						isExecutable = false) }),
				@usage (
						value = "Simplest example: ",
						examples = { @example (
								value = "method exploration;",
								isExecutable = false) }),
				@usage (
						value = "Using sampling facet: ",
						examples = { @example (
								value = "method exploration sampling:latinhypercube sample:100; ",
								isExecutable = false) }),
				@usage (
						value = "Using from facet: ",
						examples = { @example (
								value = "method exploration from:\"../path/to/my/exploration/plan.csv\"; ",
								isExecutable = false) }),
				@usage (
						value = "Using with facet: ",
						examples = { @example (
								value = "method exploration with:[[\"a\"::0.5, \"b\"::10],[\"a\"::0.1, \"b\"::100]]; ",
								isExecutable = false) }) })
public class Exploration extends AExplorationAlgorithm {
	/** The Constant Method */
	public static final String METHODS = "sampling";

	/** The Constant SAMPLE_SIZE */
	public static final String SAMPLE_SIZE = "sample";

	/** The factorial sampling */
	public static final String SAMPLE_FACTORIAL = "factorial";
	public static final int DEFAULT_FACTORIAL = 9;

	/** The Constant NB_LEVELS */
	public static final String NB_LEVELS = "levels";

	/** The Constant ITERATIONS */
	public static final String ITERATIONS = "iterations";

	/** The Constant FROM_FILE. */
	public static final String FROM_FILE = "FROMFILE";

	/** The Constant FROM_LIST. */
	public static final String FROM_LIST = "FROMLIST";

	/** The Constant DEFAULT_SAMPLING */
	public static final String DEFAULT_SAMPLING = "Exhaustive";
	

	/** The parameters. */
	private List<Batch> parameters;

	/**
	 * Instantiates a new exhaustive search.
	 *
	 * @param desc
	 *            the desc
	 */
	public Exploration(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

	@Override
	public void explore(final IScope scope) throws GamaRuntimeException {

		parameters = parameters == null ? currentExperiment.getParametersToExplore() : parameters;

		if (hasFacet(Exploration.SAMPLE_SIZE)) {
			this.sample_size = Cast.asInt(scope, getFacet(SAMPLE_SIZE).value(scope));
		}

		List<ParametersSet> sets = getExperimentPlan(parameters, scope);

		// Because Test in Gama is using batch experiment without any experiment plan !
		// TODO : Should probably do a proper Test experiment
		if (sets.isEmpty()) { sets.add(new ParametersSet()); }

		sample_size = sets.size();

		IMap<ParametersSet, Map<String, List<Object>>> res = currentExperiment.runSimulationsAndReturnResults(sets);

		if (hasFacet(IKeyword.BATCH_VAR_OUTPUTS)) { saveRawResults(scope, res); }

	}

	@Override
	public void addParametersTo(final List<Batch> exp, final BatchAgent agent) {
		super.addParametersTo(exp, agent);
	}

}
