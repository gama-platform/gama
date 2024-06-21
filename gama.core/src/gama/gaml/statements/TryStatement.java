/*******************************************************************************************************
 *
 * TryStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import com.google.common.collect.Iterables;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer.StatementSerializer;
import gama.gaml.operators.Strings;
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
		protected void serializeChildren(final SymbolDescription desc, final StringBuilder sb,
				final boolean includingBuiltIn) {
			sb.append(' ').append('{').append(Strings.LN);
			final String[] catchString = { null };
			desc.visitChildren(s -> {
				if (IKeyword.CATCH.equals(s.getKeyword())) {
					catchString[0] = s.serializeToGaml(false) + Strings.LN;
				} else {
					serializeChild(s, sb, includingBuiltIn);
				}
				return true;
			});

			sb.append('}');
			if (catchString[0] != null) {
				sb.append(catchString[0]);
			} else {
				sb.append(Strings.LN);
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
			if (!(e instanceof GamaRuntimeException)){
				scope.setCurrentError(GamaRuntimeException.create(e, scope));
			}
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