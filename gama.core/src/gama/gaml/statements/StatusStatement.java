/*******************************************************************************************************
 *
 * StatusStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
import gama.api.gaml.statements.AbstractStatement;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@symbol (
		name = IKeyword.STATUS,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.DISPLAY, IConcept.TEXT })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets (
		value = { @facet (
				name = IKeyword.COLOR,
				type = IType.COLOR,
				optional = true,
				doc = @doc ("The color used for displaying the background of the status message")),
				@facet (
						name = IKeyword.MESSAGE,
						type = IType.NONE,
						optional = false,
						doc = @doc ("Allows to display a necessarily short message in the status box in the upper left corner. No formatting characters (carriage returns, tabs, or Unicode characters) should be used, but a background color can be specified. The message will remain in place until it is replaced by another one or by nil, in which case the standard status (number of cycles) will be displayed again")) },
		omissible = IKeyword.MESSAGE)
@doc (
		value = "The statement makes the agent output an arbitrary message in the status box.",
		usages = { @usage (
				value = "Outputting a message",
				examples = { @example ("status (\"This is my status \" + self) color: #yellow;") }) })
public class StatusStatement extends AbstractStatement {

	@Override
	public String getTrace(final IScope scope) {
		// We dont trace status statements
		return "";
	}

	/** The message. */
	final IExpression message;

	/** The color. */
	final IExpression color;

	/**
	 * Instantiates a new status statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public StatusStatement(final IDescription desc) {
		super(desc);
		message = getFacet(IKeyword.MESSAGE);
		color = getFacet(IKeyword.COLOR);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = scope.getAgent();
		final String mes = null;
		if (agent != null && !agent.dead()) {
			final Object o = message.value(scope);
			final String msg = o == null ? null : Cast.asString(scope, o);
			scope.getGui().getStatus().setStatus(msg, null,
					color == null ? null : GamaColorFactory.castToColor(scope, color.value(scope)));
		}
		return mes;
	}
}
