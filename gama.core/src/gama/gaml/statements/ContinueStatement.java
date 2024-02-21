/*******************************************************************************************************
 *
 * ContinueStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
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
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.StatementWithChildrenDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.interfaces.IGamlIssue;
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
	public static class ContinueSerializer extends SymbolSerializer<StatementDescription> {

		@Override
		protected void serialize(final SymbolDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			sb.append(CONTINUE).append(";");
		}
	}

	/**
	 * The Class BreakValidator.
	 */
	public static class ContinueValidator implements IDescriptionValidator<StatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final StatementDescription description) {
			IDescription superDesc = description.getEnclosingDescription();
			while (superDesc instanceof StatementWithChildrenDescription) {
				if (((StatementWithChildrenDescription) superDesc).isContinuable()) return;
				superDesc = superDesc.getEnclosingDescription();
			}
			description.error("'continue' must be used in the context of " + SymbolProto.CONTINUABLE_STATEMENTS,
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
	 * @see gama.gaml.commands.AbstractCommand#privateExecuteIn(gama.core.runtime.IScope)
	 */
	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.setContinueStatus();
		return null; // How to return the last object ??
	}

}
