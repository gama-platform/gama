/*******************************************************************************************************
 *
 * ContinueStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.serialization.ISymbolSerializer;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatement;
import gama.api.runtime.scope.IScope;
import gama.gaml.statements.ContinueStatement.ContinueSerializer;
import gama.gaml.statements.ContinueStatement.ContinueValidator;

/**
 * The class ContinueStatement.
 *
 * @author drogoul
 * @since 22 avr. 2012
 *
 */
@symbol (
		name = IKeyword.CONTINUE,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.LOOP })
@inside (
		kinds = ISymbolKind.SEQUENCE_STATEMENT)
@doc (
		value = "`" + IKeyword.CONTINUE
				+ "` allows to skip the remaining statements inside a loop and an ask and directly move to the next element. Inside a switch, it has the same effect as break.")
@validator (ContinueValidator.class)
@serializer (ContinueSerializer.class)
public class ContinueStatement extends AbstractStatement {

	/**
	 * The Class BreakSerializer.
	 */
	public static class ContinueSerializer implements ISymbolSerializer {

		@Override
		public void serialize(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			sb.append(CONTINUE).append(";");
		}
	}

	/**
	 * The Class BreakValidator.
	 */
	public static class ContinueValidator implements IDescriptionValidator<IStatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IStatementDescription description) {
			IDescription superDesc = description.getEnclosingDescription();
			while (superDesc instanceof IStatementDescription isd) {
				if (isd.isContinuable()) return;
				superDesc = superDesc.getEnclosingDescription();
			}
			description.error(
					"'continue' must be used in the context of " + ArtefactProtoRegistry.CONTINUABLE_STATEMENTS,
					IGamlIssue.WRONG_CONTEXT);
		}
	}

	/**
	 * @param desc
	 */
	public ContinueStatement(final IDescription desc) {
		super(desc);
	}

	/**
	 * @see gama.gaml.commands.AbstractCommand#privateExecuteIn(gama.api.runtime.scope.IScope)
	 */
	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.setContinueStatus();
		return null; // How to return the last object ??
	}

}
