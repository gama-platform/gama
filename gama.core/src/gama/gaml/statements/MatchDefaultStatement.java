/*******************************************************************************************************
 *
 * MatchDefaultStatement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.doc;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * The Class MatchDefaultStatement.
 */
@symbol (
		name = { IKeyword.DEFAULT },
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		unique_in_context = true,
		concept = { IConcept.CONDITION })
@inside (
		symbols = IKeyword.SWITCH)
@doc (
		value = "Used in a switch match structure, the block prefixed by default is executed only if no other block has matched (otherwise it is not).",
		see = { "switch", "match" })
public class MatchDefaultStatement extends MatchStatement {

	/**
	 * Instantiates a new match default statement.
	 *
	 * @param desc the desc
	 */
	public MatchDefaultStatement(final IDescription desc) {
		super(desc);
	}

	@Override
	public boolean matches(final IScope scope, final Object switchValue) throws GamaRuntimeException {
		return false;
	}

}