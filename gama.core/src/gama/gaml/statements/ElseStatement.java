/*******************************************************************************************************
 *
 * ElseStatement.java, in gama.core, is part of the source code of the
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
import gama.annotations.constants.IKeyword;
import gama.annotations.support.*;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.statements.AbstractStatementSequence;

/**
 * Written by drogoul Modified on 8 févr. 2010
 * 
 * @todo Description
 * 
 */
@symbol(name = IKeyword.ELSE, kind = ISymbolKind.SEQUENCE_STATEMENT, with_sequence = true, concept = { IConcept.CONDITION })
@inside(symbols = IKeyword.IF)
@doc(value="This statement cannot be used alone",see={IKeyword.IF})
public class ElseStatement extends AbstractStatementSequence {

	/**
	 * Instantiates a new else statement.
	 *
	 * @param desc the desc
	 */
	public ElseStatement(final IDescription desc) {
		super(desc);
		setName(IKeyword.ELSE);
	}

}
