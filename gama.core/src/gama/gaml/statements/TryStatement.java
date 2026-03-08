/*******************************************************************************************************
 *
 * TryStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import com.google.common.collect.Iterables;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.serializer;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.gaml.statements.TryStatement.IfSerializer;

/**
 * IfPrototype.
 *
 * @author drogoul 14 nov. 07
 */
@symbol (
		name = IKeyword.TRY,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.ACTION })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc (
		value = "Allows the agent to execute a sequence of statements and to catch any runtime error that might happen in a subsequent `catch` block, either to ignore it (not a good idea, usually) or to safely stop the model",
		usages = { @usage (
				value = "The generic syntax is:",
				examples = { @example (
						value = "try {",
						isExecutable = false),
						@example (
								value = "    [statements]",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "Optionally, the statements to execute when a runtime error happens in the block can be defined in a following statement 'catch'. The syntax then becomes:",
						examples = { @example (
								value = "try {",
								isExecutable = false),
								@example (
										value = "    [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "catch {",
										isExecutable = false),
								@example (
										value = "    [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }), })
@serializer (IfSerializer.class)
public class TryStatement extends AbstractStatementSequence {

	/**
	 * The Class IfSerializer.
	 */
	public static class IfSerializer extends StatementSerializer {

		@Override
		public void serializeChildren(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			sb.append(' ').append('{').append(StringUtils.LN);
			final String[] catchString = { null };
			desc.visitChildren(s -> {
				if (IKeyword.CATCH.equals(s.getKeyword())) {
					catchString[0] = s.serializeToGaml(false) + StringUtils.LN;
				} else {
					serializeChild(s, sb, includingBuiltIn);
				}
				return true;
			});

			sb.append('}');
			if (catchString[0] != null) {
				sb.append(catchString[0]);
			} else {
				sb.append(StringUtils.LN);
			}

		}

	}

	/** The catch statement. */
	public IStatement catchStatement;

	/**
	 * The Constructor.
	 *
	 * @param sim
	 *            the sim
	 */
	public TryStatement(final IDescription desc) {
		super(desc);
		setName(IKeyword.CATCH);

	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		for (final ISymbol c : commands) { if (c instanceof CatchStatement) { catchStatement = (IStatement) c; } }
		super.setChildren(Iterables.filter(commands, each -> each != catchStatement));
	}

	@Override
	public Object privateExecuteIn(final IScope scope) {
		Object result = null;
		try {
			scope.enableTryMode();
			result = super.privateExecuteIn(scope);
		} catch (final Exception e) {
			if (!(e instanceof GamaRuntimeException)) { scope.setCurrentError(GamaRuntimeException.create(e, scope)); }
			scope.disableTryMode();
			if (catchStatement != null) return scope.execute(catchStatement).getValue();
		} finally {
			scope.disableTryMode();
		}
		return result;

	}

	@Override
	public void dispose() {
		if (catchStatement != null) { catchStatement.dispose(); }
		catchStatement = null;
		super.dispose();
	}
}