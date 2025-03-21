/*******************************************************************************************************
 *
 * BenchmarkStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.BufferingController;
import gama.core.runtime.concurrent.BufferingController.BufferingStrategies;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;
import gama.gaml.types.IType;

/**
 * Class TraceStatement.
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
		value = { 
				@facet (
						name = IKeyword.COLOR,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("The color with wich the message will be displayed. Note that different simulations will have different (default) colors to use for this purpose if this facet is not specified")),
					@facet (
							name = IKeyword.END,
							type = IType.STRING,
							optional = true,
							doc = @doc ("The string to be appened at the end of the message. By default it's a new line character: '\\n' or '\\r\\n' depending on the operating system." )
							),
					@facet (
							name = IKeyword.BUFFERING,
							type = { IType.STRING},
							optional = true,
							doc = @doc (
									value = "Allows to specify a buffering strategy to write in the console. Accepted values are `" + BufferingController.PER_CYCLE_BUFFERING +"` and `" + BufferingController.PER_SIMULATION_BUFFERING + "`, `" + BufferingController.NO_BUFFERING + "`. "
											+ "In the case of `"+ BufferingController.PER_CYCLE_BUFFERING +"` or `"+ BufferingController.PER_SIMULATION_BUFFERING +"`, all the write operations in the simulation which used these values would be "
											+ "executed all at once at the end of the cycle or simulation while keeping the initial order. In case of '" + BufferingController.PER_AGENT
											+ "' all operations will be released when the agent is killed (or the simulation ends). Those strategies can be used to optimise a "
											+ "simulation's execution time on models that extensively write in files. "
											+ "The `" + BufferingController.NO_BUFFERING + "` (which is the system's default) will directly write into the file.")
							),
				@facet (
				name = IKeyword.MESSAGE,
				type = IType.NONE,
				optional = true,
				doc = @doc ("A message to display alongside the results. Should concisely describe the contents of the benchmark")),
				@facet (
						name = IKeyword.REPEAT,
						type = IType.INT,
						optional = true,
						doc = @doc ("An int expression describing how many executions of the block must be handled. The output in this case will return the min, max and average durations")) 
				},
		omissible = IKeyword.MESSAGE
		)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc (
		value = "Displays in the console the duration in ms of the execution of the statements included in the block. It is possible to indicate, with the 'repeat' facet, how many times the sequence should be run")
public class BenchmarkStatement extends AbstractStatementSequence {

	/** The message. */
	final IExpression repeat, message;

	final IExpression color;
	
	final IExpression bufferingStrategy;

	final IExpression end;
	

	/**
	 * @param desc
	 */
	public BenchmarkStatement(final IDescription desc) {
		super(desc);
		repeat = getFacet(IKeyword.REPEAT);
		message = getFacet(IKeyword.MESSAGE);
		color = getFacet(IKeyword.COLOR);
		bufferingStrategy = getFacet(IKeyword.BUFFERING);
		end = getFacet(IKeyword.END);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final int repeatTimes = repeat == null ? 1 : Cast.asInt(scope, repeat.value(scope));
		GamaColor rgb = null;
		if (color != null) { 
			rgb = (GamaColor) color.value(scope); 
		}
		BufferingStrategies strategy = BufferingController.stringToBufferingStrategies(scope, (String)GamaPreferences.get(GamaPreferences.PREF_WRITE_BUFFERING_STRATEGY).value(scope));
		if (bufferingStrategy != null) { 
			strategy = BufferingController.stringToBufferingStrategies(scope, Cast.asString(scope,bufferingStrategy.value(scope)));
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
		messageToSend.append(" (over ")
						.append(repeatTimes)
						.append(" iteration(s)): min = ")
						.append(min)
						.append(" ms (iteration #")
						.append(timeOfMin)
						.append(") | max = ")
						.append(max)
						.append(" ms (iteration #")
						.append(timeOfMax)
						.append(") | average = ")
						.append(total / repeatTimes)
						.append("ms");
		if (end != null) {
			messageToSend.append(Cast.asString(scope, end));
		}
		else {
			messageToSend.append(Strings.LN);
		}
		GAMA.getBufferingController().askWriteConsole(scope, messageToSend, rgb, strategy);
//		scope.getGui().getConsole().informConsole(result, scope.getRoot(), null);
		return messageToSend.toString();
	}

}
