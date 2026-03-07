/*******************************************************************************************************
 *
 * WriteStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
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
import gama.annotations.operator;
import gama.annotations.symbol;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatement;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.utils.StringUtils;
import gama.api.utils.files.BufferingUtils;
import gama.api.utils.files.BufferingUtils.BufferingStrategies;
import gama.api.utils.prefs.GamaPreferences;
import gama.gaml.statements.WriteStatement.WriteValidator;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@symbol (
		name = IKeyword.WRITE,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.SYSTEM })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets (
		value = { @facet (
				name = IKeyword.COLOR,
				type = IType.COLOR,
				optional = true,
				doc = @doc ("The color with wich the message will be displayed. Note that different simulations will have different (default) colors to use for this purpose if this facet is not specified")),
				@facet (
						name = IKeyword.END,
						type = IType.STRING,
						optional = true,
						doc = @doc ("The string to be appened at the end of the message. By default it's a new line character: '\\n' or '\\r\\n' depending on the operating system.")),
				@facet (
						name = IKeyword.BUFFERING,
						type = { IType.STRING },
						optional = true,
						doc = @doc (
								value = "Allows to specify a buffering strategy to write in the console. Accepted values are `"
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
						optional = false,
						doc = @doc ("the message to display. Modelers can add some formatting characters to the message (carriage returns, tabs, or Unicode characters), which will be used accordingly in the console.")), },

		omissible = IKeyword.MESSAGE)
@doc (
		value = "The statement makes the agent output an arbitrary message in the console.",
		usages = { @usage (
				value = "Outputting a message",
				examples = { @example ("write \"This is a message from \" + self;") }) })
@validator (WriteValidator.class)
public class WriteStatement extends AbstractStatement {

	/**
	 * The Class WriteValidator.
	 */
	public static class WriteValidator implements IDescriptionValidator<IStatementDescription> {

		@Override
		public void validate(final IStatementDescription desc) {
			final IExpression bufferingStrategy = desc.getFacetExpr(IKeyword.BUFFERING);

			if (bufferingStrategy != null
					&& !BufferingUtils.BUFFERING_STRATEGIES.contains(bufferingStrategy.literalValue())) {
				desc.error("The value for buffering must be '" + BufferingUtils.NO_BUFFERING + "', '"
						+ BufferingUtils.PER_CYCLE_BUFFERING + "', '" + BufferingUtils.PER_AGENT + "'" + "' or '"
						+ BufferingUtils.PER_SIMULATION_BUFFERING + "'.", IGamlIssue.WRONG_TYPE);
			}
		}

	}

	static {
		// DEBUG.OFF();
	}

	@Override
	public String getTrace(final IScope scope) {
		// We don't trace write statements
		return "";
	}

	/** The message. */
	final IExpression message;

	/** The color. */
	final IExpression color;

	/** The buffering strategy. */
	final IExpression bufferingStrategy;

	/** The end. */
	final IExpression end;

	/**
	 * Instantiates a new write statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public WriteStatement(final IDescription desc) {
		super(desc);
		message = getFacet(IKeyword.MESSAGE);
		color = getFacet(IKeyword.COLOR);
		bufferingStrategy = getFacet(IKeyword.BUFFERING);
		end = getFacet(IKeyword.END);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		String mes = null;
		if (agent != null && !agent.dead()) {
			mes = Cast.asString(scope, message.value(scope));
			if (mes == null) { mes = "nil"; }
			IColor rgb = null;
			if (color != null) { rgb = (IColor) color.value(scope); }
			BufferingStrategies strategy = BufferingUtils.stringToBufferingStrategies(scope,
					GamaPreferences.Experimental.DEFAULT_WRITE_BUFFERING_STRATEGY.value(scope));
			if (bufferingStrategy != null) {
				strategy = BufferingUtils.stringToBufferingStrategies(scope,
						Cast.asString(scope, bufferingStrategy.value(scope)));
			}

			var messageToSend = new StringBuilder(mes);
			if (end != null) {
				messageToSend.append(Cast.asString(scope, end));
			} else {
				messageToSend.append(StringUtils.LN);
			}

			// DEBUG.OUT(
			// "" + getName() + " asking to write and passing " + scope.getRoot() + " as the corresponding agent");
			BufferingUtils.getInstance().askWriteConsole(scope, messageToSend, rgb, strategy);
		}
		return mes;
	}

	/**
	 * Sample.
	 *
	 * @param scope
	 *            the scope
	 * @param expr
	 *            the expr
	 * @return the string
	 */
	@operator (
			value = "sample",
			doc = { @doc ("Returns a string containing the GAML code of the expression passed in parameter, followed by the result of its evaluation") },
			category = { IOperatorCategory.STRING })
	@test ("sample('a' in ['a', 'b']) = \"'a' in (['a','b']) -: true\"")
	public static String sample(final IScope scope, final IExpression expr) {
		return sample(scope, expr == null ? "nil" : expr.serializeToGaml(false), expr);
	}

	/**
	 * Sample.
	 *
	 * @param scope
	 *            the scope
	 * @param text
	 *            the text
	 * @param expr
	 *            the expr
	 * @return the string
	 */
	@operator (
			value = "sample",
			doc = @doc ("Returns a string containing the string passed in parameter, followed by the result of the evaluation of the expression"),
			category = { IOperatorCategory.STRING })
	@test ("sample(\"result: \",'a' in ['a', 'b']) = \"result: -: true\"")
	public static String sample(final IScope scope, final String text, final IExpression expr) {
		return text == null ? ""
				: text.trim() + " -: " + (expr == null ? "nil" : StringUtils.toGaml(expr.value(scope), false));
	}

}
