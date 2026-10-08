/*******************************************************************************************************
 *
 * IfStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import gama.annotations.facet;
import gama.annotations.facets;
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
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.gaml.statements.IfStatement.IfSerializer;

/**
 * IfPrototype.
 *
 * @author drogoul 14 nov. 07
 */
@symbol (
		name = IKeyword.IF,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		concept = { IConcept.CONDITION })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER, ISymbolKind.OUTPUT })
@facets (
		value = { @facet (
				name = IKeyword.CONDITION,
				type = IType.BOOL,
				optional = false,
				doc = @doc ("A boolean expression: the condition that is evaluated.")) },
		omissible = IKeyword.CONDITION)
@doc (
		value = "Allows the agent to execute a sequence of statements if and only if the condition evaluates to true.",
		usages = { @usage (
				value = "The generic syntax is:",
				examples = { @example (
						value = "if bool_expr {",
						isExecutable = false),
						@example (
								value = "    [statements]",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "Optionally, the statements to execute when the condition evaluates to false can be defined in a following statement else. The syntax then becomes:",
						examples = { @example (
								value = "if bool_expr {",
								isExecutable = false),
								@example (
										value = "    [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "else {",
										isExecutable = false),
								@example (
										value = "    [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "string valTrue <- \"\";"),
								@example (
										value = "if true {"),
								@example (
										value = "	valTrue <- \"true\";"),
								@example (
										value = "}"),
								@example (
										value = "else {"),
								@example (
										value = "	valTrue <- \"false\";"),
								@example (
										value = "}"),
								@example (
										var = "valTrue",
										equals = "\"true\""),
								@example (
										value = "string valFalse <- \"\";"),
								@example (
										value = "if false {"),
								@example (
										value = "	valFalse <- \"true\";"),
								@example (
										value = "}"),
								@example (
										value = "else {"),
								@example (
										value = "	valFalse <- \"false\";"),
								@example (
										value = "}"),
								@example (
										var = "valFalse",
										equals = "\"false\"") }),
				@usage (
						value = "ifs and elses can be imbricated as needed. For instance:",
						examples = { @example (
								value = "if bool_expr {",
								isExecutable = false),
								@example (
										value = "    [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "else if bool_expr2 {",
										isExecutable = false),
								@example (
										value = "    [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "else {",
										isExecutable = false),
								@example (
										value = "    [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) })
@serializer (IfSerializer.class)
public class IfStatement extends AbstractStatementSequence {

	/**
	 * The Class IfSerializer.
	 */
	public static class IfSerializer extends StatementSerializer {

		@Override
		public void serializeChildren(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			sb.append(' ').append('{').append(StringUtils.LN);
			final String[] elseString = { null };
			desc.visitChildren(s -> {
				if (IKeyword.ELSE.equals(s.getKeyword())) {
					elseString[0] = s.serializeToGaml(false) + StringUtils.LN;
				} else {
					serializeChild(s, sb, includingBuiltIn);
				}
				return true;
			});

			sb.append('}');
			if (elseString[0] != null) {
				sb.append(elseString[0]);
			} else {
				sb.append(StringUtils.LN);
			}

		}

	}

	/**
	 * The else-branch statement, if any.
	 *
	 * <p><b>Thread-safety:</b> declared {@code volatile} so that the single write performed by
	 * {@link #setChildren(Iterable)} during construction (or the {@code null} written by
	 * {@link #dispose()}) is guaranteed to be visible to all threads that subsequently call
	 * {@link #privateExecuteIn(IScope)}, even when those threads belong to different parallel
	 * simulations sharing this statement instance.</p>
	 */
	public volatile IStatement alt;

	/** The cond. */
	final IExpression cond;

	/**
	 * The Constructor.
	 *
	 * @param sim
	 *            the sim
	 */
	public IfStatement(final IDescription desc) {
		super(desc);
		cond = getFacet(IKeyword.CONDITION);
		if (cond != null) { setName("if " + cond.serializeToGaml(false)); }

	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		for (final ISymbol c : commands) { if (c instanceof ElseStatement) { alt = (IStatement) c; } }
		super.setChildren(Iterables.filter(commands, each -> each != alt));
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final Object condition = cond.value(scope);
		if (!(condition instanceof Boolean))
			throw GamaRuntimeException.error("Impossible to evaluate condition " + cond.serializeToGaml(true), scope);
		return (Boolean) condition ? super.privateExecuteIn(scope) : alt != null ? scope.execute(alt).getValue() : null;
	}

	@Override
	public void dispose() {
		if (alt != null) { alt.dispose(); }
		alt = null;
		super.dispose();
	}
}