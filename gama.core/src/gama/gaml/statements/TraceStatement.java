/*******************************************************************************************************
 *
 * TraceStatement.java, in gama.core, is part of the source code of the
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
import gama.annotations.support.*;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.runtime.scope.IScope;

/**
 * Class TraceStatement.
 * 
 * @author drogoul
 * @since 23 févr. 2014
 * 
 */
@symbol(name = IKeyword.TRACE, kind = ISymbolKind.SEQUENCE_STATEMENT, with_sequence = true,
concept = { IConcept.DISPLAY })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc(value="All the statements executed in the trace statement are displayed in the console.")
public class TraceStatement extends AbstractStatementSequence {

	/**
	 * @param desc
	 */
	public TraceStatement(final IDescription desc) {
		super(desc);
	}

	@Override
	public void enterScope(final IScope scope) {
		super.enterScope(scope);
		scope.setTrace(true);
	}

	@Override
	public void leaveScope(final IScope scope) {
		scope.setTrace(false);
		super.leaveScope(scope);
	}
}
