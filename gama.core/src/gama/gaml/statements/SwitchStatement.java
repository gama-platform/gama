/*******************************************************************************************************
 *
 * SwitchStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.ArrayList;
import java.util.List;

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
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.statements.IStatement.Breakable;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.FlowStatus;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.gaml.statements.SwitchStatement.SwitchValidator;

/**
 * IfPrototype.
 *
 * @author drogoul 14 nov. 07
 */
@symbol (
		name = IKeyword.SWITCH,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		concept = { IConcept.CONDITION })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@facets (
		value = { @facet (
				name = IKeyword.VALUE,
				type = IType.NONE,
				optional = false,
				doc = @doc ("an expression")) },
		omissible = IKeyword.VALUE)
@doc (
		value = "The \"switch... match\" statement is a powerful replacement for imbricated \"if ... else ...\" constructs. "
				+ "All the blocks that match are executed in the order they are defined, unless one invokes 'break', in which case "
				+ "the switch statement is exited. The block prefixed by default is executed only if none have matched (otherwise it is not).",
		usages = { @usage (
				value = "The prototypical syntax is as follows:",
				examples = { @example (
						value = "switch an_expression {",
						isExecutable = false),
						@example (
								value = "        match value1 {...}",
								isExecutable = false),
						@example (
								value = "        match_one [value1, value2, value3] {...}",
								isExecutable = false),
						@example (
								value = "        match_between [value1, value2] {...}",
								isExecutable = false),
						@example (
								value = "        default {...}",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "Example:",
						examples = { @example (
								value = "switch 3 {",
								test = false),
								@example (
										value = "   match 1 {write \"Match 1\"; }",
										test = false),
								@example (
										value = "   match 2 {write \"Match 2\"; }",
										test = false),
								@example (
										value = "   match 3 {write \"Match 3\"; }",
										test = false),
								@example (
										value = "   match_one [4,4,6,3,7]  {write \"Match one_of\"; }",
										test = false),
								@example (
										value = "   match_between [2, 4] {write \"Match between\"; }",
										test = false),
								@example (
										value = "   default {write \"Match Default\"; }",
										test = false),
								@example (
										value = "}",
										test = false),
								@example (
										value = "string val1 <- \"\";",
										test = false,
										isTestOnly = true),
								@example (
										value = "switch 1 {",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match 1 {val1 <- val1 + \"1\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match 2 {val1 <- val1 + \"2\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match_one [1,1,6,4,7]  {val1 <- val1 + \"One_of\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match_between [2, 4] {val1 <- val1 + \"Between\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   default {val1 <- val1 + \"Default\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "}",
										test = false,
										isTestOnly = true),
								@example (
										var = "val1",
										equals = "'1One_of'",
										isTestOnly = true),
								@example (
										value = "string val2 <- \"\";",
										test = false,
										isTestOnly = true),
								@example (
										value = "switch 2 {",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match 1 {val2 <- val2 + \"1\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match 2 {val2 <- val2 + \"2\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match_one [1,1,6,4,7]  {val2 <- val2 + \"One_of\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match_between [2, 4] {val2 <- val2 + \"Between\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   default {val2 <- val2 + \"Default\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "}",
										test = false,
										isTestOnly = true),
								@example (
										var = "val2",
										equals = "'2Between'",
										isTestOnly = true),
								@example (
										value = "string val10 <- \"\";",
										test = false,
										isTestOnly = true),
								@example (
										value = "switch 10 {",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match 1 {val10 <- val10 + \"1\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match 2 {val10 <- val10 + \"2\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match_one [1,1,6,4,7]  {val10 <- val10 + \"One_of\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   match_between [2, 4] {val10 <- val10 + \"Between\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "   default {val10 <- val10 + \"Default\"; }",
										test = false,
										isTestOnly = true),
								@example (
										value = "}",
										test = false,
										isTestOnly = true),
								@example (
										var = "val10",
										equals = "'Default'",
										isTestOnly = true) }) },
		see = { IKeyword.MATCH, IKeyword.DEFAULT, IKeyword.IF })
@validator (SwitchValidator.class)
@SuppressWarnings ({ "rawtypes" })
public class SwitchStatement extends AbstractStatementSequence implements Breakable {

	/**
	 * The Class SwitchValidator.
	 */
	public static class SwitchValidator implements IDescriptionValidator {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription desc) {

			// FIXME This assertion only verifies the case of "match" (not
			// match_one or match_between)
			final Iterable<IDescription> matches = desc.getChildrenWithKeyword(MATCH);
			final IExpression switchValue = desc.getFacetExpr(VALUE);
			if (switchValue == null) return;
			final IType switchType = switchValue.getGamlType();
			if (switchType.equals(Types.NO_TYPE)) return;
			for (final IDescription match : matches) {
				final IExpression value = match.getFacetExpr(VALUE);
				if (value == null) { continue; }
				final IType<?> matchType = value.getGamlType();
				// AD : special case introduced for ints and floats (a warning
				// is emitted)
				if (Types.intFloatCase(matchType, switchType)) {
					match.warning(
							"The value " + value.serializeToGaml(false) + " of type " + matchType
									+ " is compared to a value of type " + switchType + ", which will never match ",
							IGamlIssue.SHOULD_CAST, IKeyword.VALUE, switchType.toString());
					continue;
				}

				if (matchType.isTranslatableInto(switchType)) { continue; }
				match.warning(
						"The value " + value.serializeToGaml(false) + " of type " + matchType
								+ " is compared to a value of type " + switchType + ", which will never match ",
						IGamlIssue.SHOULD_CAST, IKeyword.VALUE, switchType.toString());
			}

		}

	}

	/**
	 * The array of {@code match} branches.
	 *
	 * <p><b>Thread-safety:</b> declared {@code volatile} so that the single write performed by
	 * {@link #setChildren(Iterable)} during construction (or the {@code null} written by
	 * {@link #dispose()}) is guaranteed to be visible to all threads that subsequently call
	 * {@link #privateExecuteIn(IScope)}, even when those threads belong to different parallel
	 * simulations sharing this statement instance.</p>
	 */
	public volatile MatchStatement[] matches;

	/**
	 * The {@code default} branch, if any.
	 *
	 * <p><b>Thread-safety:</b> same visibility guarantee as {@link #matches}.</p>
	 */
	public volatile MatchStatement defaultMatch;

	/** The value. */
	final IExpression value;

	/**
	 * The Constructor.
	 *
	 * @param sim
	 *            the sim
	 */
	public SwitchStatement(final IDescription desc) {
		super(desc);
		value = getFacet(IKeyword.VALUE);
		setName("switch" + value.serializeToGaml(false));

	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		final List<MatchStatement> cases = new ArrayList<>();
		for (final ISymbol c : commands) {
			if (c instanceof MatchStatement) {
				if (IKeyword.DEFAULT.equals(((MatchStatement) c).getKeyword())) {
					defaultMatch = (MatchStatement) c;
				} else {
					cases.add((MatchStatement) c);
				}
			}
		}
		matches = cases.toArray(new MatchStatement[cases.size()]);
		super.setChildren(Iterables.filter(commands, each -> each != defaultMatch || !cases.contains(each)));
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		boolean hasMatched = false;
		final Object switchValue = value.value(scope);
		Object lastResult = null;

		// Snapshot the volatile field once into a local variable so that the
		// JIT can keep it in a register and avoid repeated memory-barrier reads
		// on every iteration of the loop.
		final MatchStatement[] localMatches = matches;
		try {
			for (final MatchStatement matche : localMatches) {

				if (matche.matches(scope, switchValue)) {
					final IExecutionResult er = scope.execute(matche);
					if (!er.passed()) return lastResult;
					lastResult = er.getValue();
					hasMatched = true;
				}

				if (scope.getAndClearBreakStatus() == FlowStatus.BREAK || scope.interrupted()) return lastResult;
			}
			if (!hasMatched && defaultMatch != null) return scope.execute(defaultMatch).getValue();
			return lastResult;
		} finally {
			scope.getAndClearBreakStatus();
		}
	}

	@Override
	public void leaveScope(final IScope scope) {
		// Clears any _loop_halted status
		// scope.popLoop();
		super.leaveScope(scope);
	}

	@Override
	public void dispose() {
		if (matches != null) { for (IStatement match : matches) { match.dispose(); } }
		matches = null;
		if (defaultMatch != null) { defaultMatch.dispose(); }
		defaultMatch = null;
		super.dispose();
	}
}