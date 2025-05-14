/*******************************************************************************************************
 *
 * WriteStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.util.StringUtils;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.BufferingController;
import gama.core.runtime.concurrent.BufferingController.BufferingStrategies;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;
import gama.gaml.statements.WriteStatement.WriteValidator;
import gama.gaml.types.IType;

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
				optional = false,
				doc = @doc ("the message to display. Modelers can add some formatting characters to the message (carriage returns, tabs, or Unicode characters), which will be used accordingly in the console.")), },
		
		omissible = IKeyword.MESSAGE)
@doc (
		value = "The statement makes the agent output an arbitrary message in the console.",
		usages = { @usage (
				value = "Outputting a message",
				examples = { @example ("write \"This is a message from \" + self;") }) })
@validator(WriteValidator.class)
public class WriteStatement extends AbstractStatement {

	
	public static class WriteValidator implements IDescriptionValidator<StatementDescription> {

		@Override
		public void validate(StatementDescription description) {
			final StatementDescription desc = description;
			final IExpression bufferingStrategy = desc.getFacetExpr(IKeyword.BUFFERING);
			

			if (bufferingStrategy != null && ! BufferingController.BUFFERING_STRATEGIES.contains(bufferingStrategy.literalValue())) {
				desc.error("The value for buffering must be '" + BufferingController.NO_BUFFERING + "', '" 
							+ BufferingController.PER_CYCLE_BUFFERING + "', '" 
							+ BufferingController.PER_AGENT + "'" 
							+ "' or '" + BufferingController.PER_SIMULATION_BUFFERING +"'.", 
						IGamlIssue.WRONG_TYPE);
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
	
	final IExpression bufferingStrategy;

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
			GamaColor rgb = null;
			if (color != null) { 
				rgb = (GamaColor) color.value(scope); 
			}
			BufferingStrategies strategy = BufferingController.stringToBufferingStrategies(scope, (String)GamaPreferences.get(GamaPreferences.PREF_WRITE_BUFFERING_STRATEGY).value(scope));
			if (bufferingStrategy != null) { 
				strategy = BufferingController.stringToBufferingStrategies(scope, Cast.asString(scope,bufferingStrategy.value(scope)));
			}
			
			var messageToSend = new StringBuilder(mes);
			if (end != null) {
				messageToSend.append(Cast.asString(scope, end));
			}
			else {
				messageToSend.append(Strings.LN);
			}
			
			// DEBUG.OUT(
			// "" + getName() + " asking to write and passing " + scope.getRoot() + " as the corresponding agent");
			GAMA.getBufferingController().askWriteConsole(scope, messageToSend, rgb, strategy);
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
