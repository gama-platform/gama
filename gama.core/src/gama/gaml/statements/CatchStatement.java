/*******************************************************************************************************
 *
 * CatchStatement.java, in gama.core, is part of the source code of the
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
import gama.api.gaml.statements.AbstractStatementSequence;

/**
 * Written by drogoul Modified on 8 févr. 2010
 * 
 * @todo Description
 * 
 */
@symbol (
		name = IKeyword.CATCH,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.ACTION })
@inside (
		symbols = IKeyword.TRY)
@doc (
		value = "This statement cannot be used alone",
		see = { IKeyword.TRY })
public class CatchStatement extends AbstractStatementSequence {

	/**
	 * Instantiates a new catch statement.
	 *
	 * @param desc the desc
	 */
	public CatchStatement(final IDescription desc) {
		super(desc);
		setName(IKeyword.CATCH);
	}

}
