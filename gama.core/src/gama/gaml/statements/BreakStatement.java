/*******************************************************************************************************
 *
 * BreakStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
import gama.gaml.statements.BreakStatement.BreakSerializer;
import gama.gaml.statements.BreakStatement.BreakValidator;

/**
 * The class BreakCommand.
 *
 * @author drogoul
 * @since 22 avr. 2012
 *
 */
@symbol (
		name = IKeyword.BREAK,
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = false,
		concept = { IConcept.LOOP })
@inside (
		kinds = ISymbolKind.SEQUENCE_STATEMENT)
@doc (
		value = "`" + IKeyword.BREAK + "` allows to interrupt the current sequence of statements.")
@validator (BreakValidator.class)
@serializer (BreakSerializer.class)
public class BreakStatement extends AbstractStatement {

	/**
	 * The Class BreakSerializer.
	 */
	public static class BreakSerializer extends SymbolSerializer<StatementDescription> {

		@Override
		protected void serialize(final SymbolDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			sb.append(BREAK).append(";");
		}
	}

	/**
	 * The Class BreakValidator.
	 */
	public static class BreakValidator implements IDescriptionValidator<StatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final StatementDescription description) {
			IDescription superDesc = description.getEnclosingDescription();
			while (superDesc instanceof StatementWithChildrenDescription) {
				if (((StatementWithChildrenDescription) superDesc).isBreakable()) return;
				superDesc = superDesc.getEnclosingDescription();
			}
			description.error("'break' must be used in the context of " + SymbolProto.BREAKABLE_STATEMENTS,
					IGamlIssue.WRONG_CONTEXT);
		}
	}

	/**
	 * @param desc
	 */
	public BreakStatement(final IDescription desc) {
		super(desc);
	}

	/**
	 * @see gama.gaml.commands.AbstractCommand#privateExecuteIn(gama.core.runtime.IScope)
	 */
	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.setBreakStatus();
		return null; // How to return the last object ??
	}

}
