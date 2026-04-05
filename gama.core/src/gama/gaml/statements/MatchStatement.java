/*******************************************************************************************************
 *
 * MatchStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.misc.IContainer;

/**
 * IfPrototype.
 *
 * @author drogoul 14 nov. 07
 */
@symbol (
		name = { IKeyword.MATCH, IKeyword.MATCH_BETWEEN, IKeyword.MATCH_ONE, IKeyword.MATCH_REGEX },
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		concept = { IConcept.CONDITION },
		with_sequence = true)
@inside (
		symbols = IKeyword.SWITCH)
@facets (
		value = { @facet (
				name = IKeyword.VALUE,
				type = IType.NONE,
				optional = true,
				doc = @doc ("The value or values this statement tries to match")) },
		omissible = IKeyword.VALUE)
@doc (
		value = "In a switch...match structure, the value of each match block is compared to the value in the switch. If they match, the embedded statement set is executed. Four kinds of match can be used, equality, containment, betweenness and regex matching",
		usages = { @usage (
				value = IKeyword.MATCH + " block is executed if the switch value is equals to the value of the match:",
				examples = { @example (
						value = "switch 3 {",
						test = false),
						@example (
								value = "   match 1 {write \"Match 1\"; }",
								test = false),
						@example (
								value = "   match 3 {write \"Match 2\"; }",
								test = false),
						@example (
								value = "}",
								test = false) }),
				@usage (
						value = IKeyword.MATCH_BETWEEN
								+ " block is executed if the switch value is in the interval given in value of the "
								+ IKeyword.MATCH_BETWEEN + ":",
						examples = { @example (
								value = "switch 3 {",
								test = false),
								@example (
										value = "   match_between [1,2] {write \"Match OK between [1,2]\"; }",
										test = false),
								@example (
										value = "   match_between [2,5] {write \"Match OK between [2,5]\"; }",
										test = false),
								@example (
										value = "}",
										test = false) }),
				@usage (
						value = IKeyword.MATCH_ONE
								+ " block is executed if the switch value is equals to one of the values of the "
								+ IKeyword.MATCH_ONE + ":",
						examples = { @example (
								value = "switch 3 {",
								test = false),
								@example (
										value = "   match_one [0,1,2] {write \"Match OK with one of [0,1,2]\"; }",
										test = false),
								@example (
										value = "   match_between [2,3,4,5] {write \"Match OK with one of [2,3,4,5]\"; }",
										test = false),
								@example (
										value = "}",
										test = false) }) },
		see = { IKeyword.SWITCH, IKeyword.DEFAULT })
@SuppressWarnings ({ "rawtypes" })
public class MatchStatement extends AbstractStatementSequence {

	/** The value. */
	final IExpression value;

	/** The constant value. */
	Object constantValue;

	/** The executer. */
	final MatchExecuter executer;

	/**
	 * Instantiates a new match statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public MatchStatement(final IDescription desc) {
		super(desc);
		value = getFacet(IKeyword.VALUE);
		final String keyword = desc.getKeyword();
		setName(keyword + " " + (value == null ? "" : value.serializeToGaml(false)));
		executer =
				IKeyword.MATCH.equals(keyword) ? new SimpleMatch() : IKeyword.MATCH_ONE.equals(keyword) ? new MatchOne()
						: IKeyword.MATCH_BETWEEN.equals(keyword) ? new MatchBetween()
						: IKeyword.MATCH_REGEX.equals(keyword) ? new MatchRegex() : null;
		if (executer != null) { executer.acceptValue(); }
	}

	/**
	 * Matches.
	 *
	 * @param scope
	 *            the scope
	 * @param switchValue
	 *            the switch value
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public boolean matches(final IScope scope, final Object switchValue) throws GamaRuntimeException {
		if (executer == null) return false;
		return executer.matches(scope, switchValue);
	}

	/**
	 * The Class MatchExecuter.
	 */
	abstract class MatchExecuter {

		/**
		 * Matches.
		 *
		 * @param scope
		 *            the scope
		 * @param switchValue
		 *            the switch value
		 * @return true, if successful
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		abstract boolean matches(IScope scope, Object switchValue) throws GamaRuntimeException;

		/**
		 * Accept value.
		 */
		void acceptValue() {
			if (value.isConst()) { constantValue = value.getConstValue(); }
		}

		/**
		 * Gets the value.
		 *
		 * @param scope
		 *            the scope
		 * @return the value
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		Object getValue(final IScope scope) throws GamaRuntimeException {
			return constantValue == null ? value.value(scope) : constantValue;
		}
	}

	/**
	 * The Class SimpleMatch.
	 */
	class SimpleMatch extends MatchExecuter {

		@Override
		public boolean matches(final IScope scope, final Object switchValue) throws GamaRuntimeException {
			final Object val = getValue(scope);
			return val == null ? switchValue == null : val.equals(switchValue);
		}

	}

	/**
	 * The Class MatchOne.
	 */
	class MatchOne extends MatchExecuter {

		@Override
		public boolean matches(final IScope scope, final Object switchValue) throws GamaRuntimeException {
			final Object val = getValue(scope);
			if (val instanceof IContainer) return ((IContainer) val).contains(scope, switchValue);
			return GamaListFactory.castToList(scope, val).contains(switchValue);
		}

		@Override
		public void acceptValue() {
			super.acceptValue();
			if (constantValue != null && !(constantValue instanceof IContainer) && !(constantValue instanceof IPoint)) {
				constantValue = Types.LIST.cast(null, constantValue, null, false);
			}
		}
	}

	/**
	 * The Class MatchRegex.
	 *
	 * <p><b>Performance:</b> when the pattern expression is a compile-time constant, the
	 * {@link Pattern} is compiled once inside {@link #acceptValue()} and reused on every call to
	 * {@link #matches(IScope, Object)}, avoiding the cost of {@link Pattern#compile} on every
	 * switch evaluation. When the pattern is dynamic (non-constant) the {@code Pattern} is
	 * compiled on each invocation as before.</p>
	 */
	class MatchRegex extends MatchExecuter {

		/**
		 * The pre-compiled pattern, non-null only when the value expression is a constant.
		 * Reused on every call to {@link #matches(IScope, Object)} to avoid repeated compilation.
		 */
		private Pattern compiledPattern;

		@Override
		public boolean matches(final IScope scope, final Object switchValue) throws GamaRuntimeException {
			if (!(switchValue instanceof String target)) throw GamaRuntimeException.error(
					"Can only match strings against a regular expression. " + switchValue + " is not a string", scope);
			// Fast path: use the pre-compiled pattern for constant patterns.
			if (compiledPattern != null) { return compiledPattern.matcher(target).find(); }
			// Dynamic path: compile on every call.
			final Object val = getValue(scope);
			if (!(val instanceof String pattern)) throw GamaRuntimeException
					.error("Can only match strings against a regular expression. " + val + " is not a string", scope);
			if (pattern.isEmpty()) return false;
			try {
				return Pattern.compile(pattern).matcher(target).find();
			} catch (PatternSyntaxException e) {
				return target.contains(pattern);
			}
		}

		@Override
		public void acceptValue() {
			super.acceptValue();
			if (constantValue != null && !(constantValue instanceof String)) {
				constantValue = Types.STRING.cast(null, constantValue, null, false);
			}
			// Pre-compile the pattern when the value is a constant non-empty string.
			if (constantValue instanceof String pattern && !pattern.isEmpty()) {
				try {
					compiledPattern = Pattern.compile(pattern);
				} catch (PatternSyntaxException e) {
					// Invalid regex at compile time: leave compiledPattern null so that the
					// dynamic path (which falls back to String.contains) is used at runtime.
					compiledPattern = null;
				}
			}
		}
	}

	/**
	 * The Class MatchBetween.
	 */
	class MatchBetween extends MatchExecuter {

		@Override
		public boolean matches(final IScope scope, final Object switchValue) throws GamaRuntimeException {
			if (!(switchValue instanceof Number)) throw GamaRuntimeException
					.error("Can only match if a number is in an interval. " + switchValue + " is not a number", scope);
			Object val = value.value(scope);
			if (!(val instanceof IPoint)) { val = GamaPointFactory.castToPoint(scope, val); }
			final double min = ((IPoint) val).getX();
			final double max = ((IPoint) val).getY();
			final double in = ((Number) switchValue).doubleValue();
			return in >= min && in <= max;
		}

		/**
		 * @see gama.gaml.commands.MatchCommand.MatchExecuter#acceptValue()
		 */
		@Override
		public void acceptValue() {
			super.acceptValue();
			if (constantValue != null && !(constantValue instanceof IPoint)) {
				constantValue = Types.POINT.cast(null, constantValue, null, false);
			}

		}
	}

}