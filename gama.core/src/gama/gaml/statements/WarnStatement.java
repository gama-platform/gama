/*******************************************************************************************************
 *
 * WarnStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
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
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatement;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 6 févr. 2010
 *
 * @todo Description
 *
 */

@symbol (
		name = IKeyword.WARN,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.SYSTEM })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets (
		value = { @facet (
				name = IKeyword.MESSAGE,
				type = IType.STRING,
				optional = false,
				doc = @doc ("the message to display as a warning.")) },
		omissible = IKeyword.MESSAGE)
@doc (
		value = "The statement makes the agent output an arbitrary message in the error view as a warning.",
		usages = { @usage (
				value = "Emmitting a warning",
				examples = { @example ("warn \"This is a warning from \" + self;") }) })
public class WarnStatement extends AbstractStatement {

	/** The message. */
	final IExpression message;

	/**
	 * Instantiates a new warn statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public WarnStatement(final IDescription desc) {
		super(desc);
		message = getFacet(IKeyword.MESSAGE);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		String mes = null;
		if (agent != null && !agent.dead()) {
			mes = Cast.asString(scope, message.value(scope));
			GAMA.reportAndThrowIfNeeded(scope, GamaRuntimeException.warning(mes, scope), false);
		}
		return mes;
	}

	// @Override
	// public IType getType() {
	// return Types.STRING;
	// }

}
