/*******************************************************************************************************
 *
 * BenchmarkStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

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
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.StringUtils;
import gama.api.utils.files.BufferingUtils;
import gama.api.utils.files.BufferingUtils.BufferingStrategies;
import gama.api.utils.prefs.GamaPreferences;

/**
 * Measures execution durations for a block of statements.
 *
 * @author drogoul
 * @since 23 févr. 2014
 *
 */
@symbol (
		name = "benchmark",
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.TEST })
@facets (
		value = { @facet (
				name = IKeyword.COLOR,
				type = IType.COLOR,
				optional = true,
				doc = @doc ("The color of the console summary. Ignored when console: false. If omitted, the simulation's default color is used")),
				@facet (
						name = IKeyword.END,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The string appended to the summary. By default, a platform-specific newline is appended")),
				@facet (
						name = IKeyword.BUFFERING,
						type = { IType.STRING },
						optional = true,
						doc = @doc (
								value = "Specifies the console summary's buffering strategy; ignored when `console: false`. Accepted values are `"
										+ BufferingUtils.PER_CYCLE_BUFFERING + "` and `"
										+ BufferingUtils.PER_SIMULATION_BUFFERING + "`, `" + BufferingUtils.NO_BUFFERING
										+ "`. " + "In the case of `" + BufferingUtils.PER_CYCLE_BUFFERING + "` or `"
										+ BufferingUtils.PER_SIMULATION_BUFFERING
										+ "`, all the write operations in the simulation which used these values would be "
										+ "executed all at once at the end of the cycle or simulation while keeping the initial order. In case of '"
										+ BufferingUtils.PER_AGENT
										+ "' all operations will be released when the agent is killed (or the simulation ends). Those strategies can be used to optimise a "
										+ "simulation's execution time on models that extensively write in files. "
										+ "The `" + BufferingUtils.NO_BUFFERING
										+ "` (which is the system's default) will directly write into the file.")),
				@facet (
						name = IKeyword.MESSAGE,
						type = IType.NONE,
						optional = true,
						doc = @doc ("A message to display alongside the results. Should concisely describe the contents of the benchmark")),
				@facet (
						name = IKeyword.REPEAT,
						type = IType.INT,
						optional = true,
						doc = @doc ("The number of executions of the block (default: 1). Must be strictly positive; zero or a negative value raises a runtime error. Each execution is timed separately")),
				@facet (
						name = IKeyword.RETURNS,
						declares_new_temp = true,
						type = IType.LIST,
						of = IType.FLOAT,
						optional = true,
						doc = @doc ("Declares a new temporary list<float> variable containing one duration in milliseconds per execution, in execution order. It is available after the benchmark in the enclosing scope; do not predeclare it. The list has repeat elements (one by default), and its entries can be indexed directly without a cast")),
				@facet (
						name = "console",
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Whether to display the minimum, maximum, and average durations in the console (default: true). Setting this to false does not disable timing or collection through returns:, and does not suppress output written by statements inside the block")) },
		omissible = IKeyword.MESSAGE)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc (
		value = "Measures the elapsed execution time of a block in milliseconds. By default the block runs once and a summary is written to the console. "
				+ "Use `repeat:` for multiple executions, `returns:` to declare a temporary `list<float>` of individual durations, and `console: false` to disable the summary. "
				+ "Repeated executions use the same model state: side effects are not reset between runs. This statement measures only its block; the experiment facet `benchmark: true` enables experiment-wide profiling instead.",
		usages = { @usage (
				value = "Default console summary for a single execution:",
				examples = @example (
						value = "benchmark \"List construction\" { list<int> values <- [1, 2, 3]; }",
						isExecutable = false)),
				@usage (
						value = "Capture five durations without a console summary. The declared variable is a list<float>, including when repeat is omitted:",
						examples = @example (
								value = """
										benchmark "List construction" repeat: 5 returns: durations console: false {
										    list<int> values <- [1, 2, 3];
										}
										float first_duration <- durations[0];
										write "Average duration: " + mean(durations) + " ms";""",
								isExecutable = false)) })
public class BenchmarkStatement extends AbstractStatementSequence {

	/** The message. */
	final IExpression repeat, message;

	/** The optional console output and results variable. */
	final IExpression console;
	final String results;

	/** The color. */
	final IExpression color;

	/** The buffering strategy. */
	final IExpression bufferingStrategy;

	/** The end. */
	final IExpression end;

	/**
	 * @param desc
	 */
	public BenchmarkStatement(final IDescription desc) {
		super(desc);
		repeat = getFacet(IKeyword.REPEAT);
		message = getFacet(IKeyword.MESSAGE);
		console = getFacet("console");
		results = getLiteral(IKeyword.RETURNS);
		color = getFacet(IKeyword.COLOR);
		bufferingStrategy = getFacet(IKeyword.BUFFERING);
		end = getFacet(IKeyword.END);
	}

	@Override
	public void enterScope(final IScope scope) {
		if (results != null) { scope.addVarWithValue(results, GamaListFactory.create(Types.FLOAT)); }
		super.enterScope(scope);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final int repeatTimes = repeat == null ? 1 : Cast.asInt(scope, repeat.value(scope));
		if (repeatTimes <= 0) {
			throw GamaRuntimeException.error("The repeat facet of a benchmark statement must be positive", scope);
		}
		final IList<Double> durations = GamaListFactory.create(Types.FLOAT, repeatTimes);
		if (results != null) { scope.setVarValue(results, durations); }
		final boolean printSummary = console == null || Cast.asBool(scope, console.value(scope));
		IColor rgb = null;
		BufferingStrategies strategy = null;
		if (printSummary) {
			if (color != null) { rgb = (IColor) color.value(scope); }
			strategy = BufferingUtils.stringToBufferingStrategies(scope,
					GamaPreferences.Experimental.DEFAULT_WRITE_BUFFERING_STRATEGY.value(scope));
			if (bufferingStrategy != null) {
				strategy = BufferingUtils.stringToBufferingStrategies(scope,
						Cast.asString(scope, bufferingStrategy.value(scope)));
			}
		}

		double min = Long.MAX_VALUE;
		int timeOfMin = 0;
		double max = Long.MIN_VALUE;
		int timeOfMax = 0;
		double total = 0;

		for (int i = 0; i < repeatTimes; i++) {
			final long begin = System.nanoTime();
			super.privateExecuteIn(scope);
			final long end = System.nanoTime();
			final double duration = (end - begin) / 1000000d;
			durations.add(duration);
			if (min > duration) {
				min = duration;
				timeOfMin = i;
			}
			if (max < duration) {
				max = duration;
				timeOfMax = i;
			}
			total += duration;
		}

		var messageToSend = new StringBuilder();
		messageToSend.append(message == null ? "Execution time " : Cast.asString(scope, message.value(scope)));
		messageToSend.append(" (over ").append(repeatTimes).append(" iteration(s)): min = ").append(min)
				.append(" ms (iteration #").append(timeOfMin).append(") | max = ").append(max)
				.append(" ms (iteration #").append(timeOfMax).append(") | average = ").append(total / repeatTimes)
				.append("ms");
		if (end != null) {
			messageToSend.append(Cast.asString(scope, end));
		} else {
			messageToSend.append(StringUtils.LN);
		}
		if (printSummary) { BufferingUtils.getInstance().askWriteConsole(scope, messageToSend, rgb, strategy); }
		return messageToSend.toString();
	}

}
