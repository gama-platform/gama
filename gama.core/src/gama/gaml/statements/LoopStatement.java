/*******************************************************************************************************
 *
 * LoopStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.EnumSet;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.FlowStatus;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Cast;
import gama.gaml.statements.IStatement.Breakable;
import gama.gaml.statements.LoopStatement.LoopSerializer;
import gama.gaml.statements.LoopStatement.LoopValidator;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

// A group of commands that can be executed repeatedly.

/**
 * The Class LoopStatement.
 */

/**
 * The Class LoopStatement.
 */

/**
 * The Class LoopStatement.
 */
@symbol (
		name = IKeyword.LOOP,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		breakable = true,
		continuable = true,
		concept = { IConcept.LOOP })
@facets (
		value = { @facet (
				name = IKeyword.FROM,
				type = { IType.INT, IType.FLOAT },
				optional = true,
				doc = @doc ("an int or float expression that represents the lower bound of the loop")),
				@facet (
						name = IKeyword.TO,
						type = { IType.INT, IType.FLOAT },
						optional = true,
						doc = @doc ("an int or float expression that represents the higher bound of the loop")),
				@facet (
						name = IKeyword.STEP,
						type = { IType.INT, IType.FLOAT },
						optional = true,
						doc = @doc ("an int or float expression that represents the incrementation of the loop")),
				@facet (
						name = IKeyword.NAME,
						type = IType.NEW_TEMP_ID,
						optional = true,
						doc = @doc ("a temporary variable name")),
				@facet (
						name = IKeyword.OVER,
						type = { IType.CONTAINER, IType.POINT },
						optional = true,
						doc = @doc ("a list, point, matrix or map expression")),
				@facet (
						name = IKeyword.WHILE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("a boolean expression")),
				@facet (
						name = IKeyword.TIMES,
						type = IType.INT,
						optional = true,
						doc = @doc ("an int expression")) },
		omissible = IKeyword.NAME)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc (
		value = "Allows the agent toExpression perform the same set of statements either a fixed number of times, or while a condition is true, or by progressing in a collection of elements or along an interval of numbers. Be aware that there are no prevention of infinite loops. As a consequence, open loops should be used with caution, as one agent may block the execution of the whole model.",
		usages = { @usage (
				value = "The basic syntax for repeating a fixed number of times a set of statements is:",
				examples = { @example (
						value = "loop times: an_int_expression {",
						isExecutable = false),
						@example (
								value = "     // [statements]",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false),
						@example (
								value = "int sumTimes <- 1;",
								isTestOnly = true),
						@example (
								value = "loop times: 3 {sumTimes <- sumTimes + sumTimes;}",
								isTestOnly = true),
						@example (
								var = "sumTimes",
								equals = "8",
								isTestOnly = true) }),
				@usage (
						value = "The basic syntax for repeating a set of statements while a condition holds is:",
						examples = { @example (
								value = "loop while: a_bool_expression {",
								isExecutable = false),
								@example (
										value = "     // [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "int sumWhile <- 1;",
										isTestOnly = true),
								@example (
										value = "loop while: (sumWhile < 5) {sumWhile <- sumWhile + sumWhile;}",
										isTestOnly = true),
								@example (
										var = "sumWhile",
										equals = "8",
										isTestOnly = true) }),
				@usage (
						value = "The basic syntax for repeating a set of statements by progressing over a container of a point is:",
						examples = { @example (
								value = "loop a_temp_var over: a_collection_expression {",
								isExecutable = false),
								@example (
										value = "     // [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "The basic syntax for repeating a set of statements while an index iterates over a range of values with a fixed stepExpression of 1 is:",
						examples = { @example (
								value = "loop a_temp_var fromExpression: int_expression_1 toExpression: int_expression_2 {",
								isExecutable = false),
								@example (
										value = "     // [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "The incrementation stepExpression of the index can also be chosen:",
						examples = { @example (
								value = "loop a_temp_var fromExpression: int_expression_1 toExpression: int_expression_2 stepExpression: int_expression3 {",
								isExecutable = false),
								@example (
										value = "     // [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "int sumFor <- 0;",
										isTestOnly = true),
								@example (
										value = "loop i fromExpression: 10 toExpression: 30 stepExpression: 10 {sumFor <- sumFor + i;}",
										isTestOnly = true),
								@example (
										var = "sumFor",
										equals = "60",
										isTestOnly = true) }),
				@usage (
						value = "In these latter three cases, the name facet designates the name of a temporary variable, whose scope is the loop, and that takes, in turn, the value of each of the element of the list (or each value in the interval). For example, in the first instance of the \"loop over\" syntax :",
						examples = { @example (
								value = "int a <- 0;"),
								@example (
										value = "loop i over: [10, 20, 30] {"),
								@example (
										value = "     a <- a + i;"),
								@example (
										value = "} // a now equals 60"),
								@example (
										var = "a",
										equals = "60",
										isTestOnly = true) }),
				@usage (
						value = "The second (quite common) case of the loop syntax allows one toExpression use an interval of integers or floats. The fromExpression and toExpression facets take an int or float expression as arguments, with the first (resp. the last) specifying the beginning (resp. end) of the inclusive interval (i.e. [toExpression, fromExpression]). If the stepExpression is not defined, it is assumed toExpression be equal toExpression 1 or -1, depending on the direction of the range. If it is defined, its sign will be respected, so that a positive stepExpression will never allow the loop toExpression enter a loop fromExpression i toExpression j where i is greater than j",
						examples = { @example (
								value = "list the_list <-list (species_of (self));"),
								@example (
										value = "loop i fromExpression: 0 toExpression: length (the_list) - 1 {"),
								@example (
										value = "     ask the_list at i {"),
								@example (
										value = "        // ..."),
								@example (
										value = "     }"),
								@example (
										value = "} // every  agent of the list is asked toExpression do something") }) })
@serializer (LoopSerializer.class)
@validator (LoopValidator.class)
@SuppressWarnings ({ "rawtypes" })
public class LoopStatement extends AbstractStatementSequence implements Breakable {

	/**
	 * The Class LoopValidator.
	 */
	public static class LoopValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription description) {
			final IExpressionDescription times = description.getFacet(TIMES);
			final IExpressionDescription over = description.getFacet(OVER);
			final IExpressionDescription from = description.getFacet(FROM);
			final IExpressionDescription to = description.getFacet(TO);
			final IExpressionDescription cond = description.getFacet(WHILE);
			IExpressionDescription name = description.getFacet(NAME);
			if (name != null && name.isConst() && name.toString().startsWith(INTERNAL)) { name = null; }
			// See Issue #3085
			if (name != null) { Assert.nameIsValid(description); }
			if (times != null) {
				processTimes(description, over, from, to, cond, name);
			} else if (over != null) {
				processOver(description, from, to, cond, name);
			} else if (cond != null) {
				processCond(description, from, to, name);
			} else if (from != null) {
				processFromTo(description, to, name);
			} else if (to != null) {
				description.error("'loop' is missing the 'fromExpression:' facet", IGamlIssue.MISSING_FACET,
						description.getUnderlyingElement(), FROM, "0");
			} else {
				description.error(
						"Missing the definition of the kind of loop toExpression perform (times, over, while, fromExpression/toExpression)",
						IGamlIssue.MISSING_FACET);
			}
		}

		/**
		 * Process fromExpression toExpression.
		 *
		 * @param description
		 *            the description
		 * @param toExpression
		 *            the toExpression
		 * @param name
		 *            the name
		 */
		private void processFromTo(final IDescription description, final IExpressionDescription to,
				final IExpressionDescription name) {
			if (name == null) {
				description.error("No variable has been declared", IGamlIssue.MISSING_NAME, NAME);
				return;
			}
			if (to == null) {
				description.error("'loop' is missing the 'toExpression:' facet", IGamlIssue.MISSING_FACET,
						description.getUnderlyingElement(), TO, "0");
			}
		}

		/**
		 * Process fromExpression toExpression.
		 *
		 * @param description
		 *            the description
		 * @param fromExpression
		 *            the fromExpression
		 * @param toExpression
		 *            the toExpression
		 * @param name
		 *            the name
		 */
		private void processCond(final IDescription description, final IExpressionDescription from,
				final IExpressionDescription to, final IExpressionDescription name) {
			if (from != null) {
				description.error("'while' and 'fromExpression' are not compatible", IGamlIssue.CONFLICTING_FACETS,
						WHILE, FROM);
			}
			if (to != null) {
				description.error("'while' and 'toExpression' are not compatible", IGamlIssue.CONFLICTING_FACETS, WHILE,
						TO);
			}
			if (name != null) { description.error("No variable should be declared", IGamlIssue.UNUSED, WHILE, NAME); }
		}

		/**
		 * Process over.
		 *
		 * @param description
		 *            the description
		 * @param fromExpression
		 *            the fromExpression
		 * @param toExpression
		 *            the toExpression
		 * @param cond
		 *            the cond
		 * @param name
		 *            the name
		 */
		private void processOver(final IDescription description, final IExpressionDescription from,
				final IExpressionDescription to, final IExpressionDescription cond, final IExpressionDescription name) {
			if (cond != null) {
				description.error("'over' and 'while' are not compatible", IGamlIssue.CONFLICTING_FACETS, OVER, WHILE);
			} else if (from != null) {
				description.error("'over' and 'fromExpression' are not compatible", IGamlIssue.CONFLICTING_FACETS, OVER,
						FROM);
			} else if (to != null) {
				description.error("'over' and 'toExpression' are not compatible", IGamlIssue.CONFLICTING_FACETS, OVER,
						TO);
			}
			if (name == null) { description.error("No variable has been declared", IGamlIssue.MISSING_NAME, OVER); }
		}

		/**
		 * Process times.
		 *
		 * @param description
		 *            the description
		 * @param over
		 *            the over
		 * @param fromExpression
		 *            the fromExpression
		 * @param toExpression
		 *            the toExpression
		 * @param cond
		 *            the cond
		 * @param name
		 *            the name
		 * @return true, if successful
		 */
		private void processTimes(final IDescription description, final IExpressionDescription over,
				final IExpressionDescription from, final IExpressionDescription to, final IExpressionDescription cond,
				final IExpressionDescription name) {
			if (over != null) {
				description.error("'times' and 'over' are not compatible", IGamlIssue.CONFLICTING_FACETS, TIMES, OVER);
			} else if (cond != null) {
				description.error("'times' and 'while' are not compatible", IGamlIssue.CONFLICTING_FACETS, TIMES,
						WHILE);
			} else if (from != null) {
				description.error("'times' and 'fromExpression' are not compatible", IGamlIssue.CONFLICTING_FACETS,
						TIMES, FROM);
			} else if (to != null) {
				description.error("'times' and 'toExpression' are not compatible", IGamlIssue.CONFLICTING_FACETS, TIMES,
						TO);
			}
			if (name != null) { description.error("No variable should be declared", IGamlIssue.UNUSED, NAME); }
		}

	}

	/**
	 * The Class LoopSerializer.
	 */
	public static class LoopSerializer extends SymbolSerializer<SymbolDescription> {

		@Override
		protected String serializeFacetValue(final SymbolDescription s, final String key,
				final boolean includingBuiltIn) {
			if (NAME.equals(key) && (s.hasFacet(TIMES) || s.hasFacet(WHILE))) return null;
			return super.serializeFacetValue(s, key, includingBuiltIn);
		}

	}

	/** The executer. */
	private final LoopExecuter executer;

	/** The var name. */
	private final String varName;

	/** The status. */
	static final EnumSet<FlowStatus> BREAK_STATUSES =
			EnumSet.of(FlowStatus.BREAK, FlowStatus.RETURN, FlowStatus.DIE, FlowStatus.DISPOSE);

	/**
	 * Instantiates a new loop statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public LoopStatement(final IDescription desc) {
		super(desc);
		IExpression over = getFacet(IKeyword.OVER);
		IExpression times = getFacet(IKeyword.TIMES);
		IExpression cond = getFacet(IKeyword.WHILE);
		final boolean isWhile = cond != null;
		final boolean isList = over != null;
		IExpression from = getFacet(IKeyword.FROM);
		IExpression to = getFacet(IKeyword.TO);
		IExpression step = getFacet(IKeyword.STEP);
		final boolean isBounded = from != null && to != null;
		@SuppressWarnings ("null") boolean isInt = isBounded && from.getGamlType() == Types.INT
				&& to.getGamlType() == Types.INT && (step == null || step.getGamlType() == Types.INT);
		varName = desc.getName();
		executer = isWhile ? new While(cond) : isList ? new Over(over)
				: isBounded ? isInt ? new IntBounded(from, to, step) : new FloatBounded(from, to, step)
				: new Times(times);
	}

	@Override
	public void enterScope(final IScope scope) {
		// 25/02/14: Suppressed because already done in loopBody() :
		// super.enterScope(scope);

		// if (varName != null) { scope.addVarWithValue(varName, null); }
	}

	@Override
	public void leaveScope(final IScope scope) {
		// Should clear any _loop_halted status present
		// if (varName != null) { scope.removeAllVars(); }
		// scope.popLoop();
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		try {
			return executer.runIn(scope);
		} finally {
			scope.getAndClearBreakStatus();
		}
	}

	/**
	 * Loop body.
	 *
	 * @param scope
	 *            the scope
	 * @param currentValue
	 *            the var
	 * @param result
	 *            the result
	 * @return true, if successful
	 */
	protected FlowStatus loopBody(final IScope scope, final Object currentValue, final Object[] result) {
		scope.push(this);
		try {
			// We set it explicitly to the newly created scope
			if (varName != null) { scope.setVarValue(varName, currentValue, true); }
			result[0] = super.privateExecuteIn(scope);
		} finally {
			scope.pop(this);
		}
		return scope.getAndClearContinueStatus();
	}

	/**
	 * The Interface LoopExecuter.
	 */
	interface LoopExecuter {

		/**
		 * Main method for the loop executers.
		 *
		 * @param scope
		 *            the scope
		 * @return the object
		 */
		Object runIn(final IScope scope);
	}

	/**
	 * The Class Bounded.
	 */

	abstract class Bounded<T extends Number> implements LoopExecuter {
		/** The stepExpression. */
		protected final IExpression fromExpression, toExpression, stepExpression;

		/** The stepExpression defined. */
		protected final boolean stepDefined;

		/** The constant step. */
		protected final T constantFrom, constantTo, constantStep;

		/**
		 * Instantiates a new bounded. Initializes the constant values of the fromExpression, toExpression and
		 * stepExpression facets if any.
		 *
		 * @param from
		 *            the from
		 * @param to
		 *            the to
		 * @param step
		 *            the step
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		Bounded(final IExpression from, final IExpression to, final IExpression step) throws GamaRuntimeException {
			this.fromExpression = from;
			this.toExpression = to;
			this.stepExpression = step;
			this.stepDefined = step != null;
			final IScope scope = null;
			if (from.isConst()) {
				constantFrom = value(scope, from);
			} else {
				constantFrom = null;
			}
			if (to.isConst()) {
				constantTo = value(scope, to);
			} else {
				constantTo = null;
			}
			if (step == null) {
				constantStep = defaultStep();
			} else if (step.isConst()) {
				constantStep = value(scope, step);
			} else {
				constantStep = null;
			}
		}

		/**
		 * Returns the value of the expression in the scope.
		 *
		 * @param scope
		 *            the scope
		 * @param exp
		 *            the exp
		 * @return the t
		 */
		abstract T value(final IScope scope, final IExpression exp);

		/**
		 * Default step.
		 *
		 * @return the t
		 */
		abstract T defaultStep();

		/**
		 * Compute from.
		 *
		 * @param scope
		 *            the scope
		 * @return the t
		 */
		T computeFrom(final IScope scope) {
			return constantFrom == null ? value(scope, fromExpression) : constantFrom;
		}

		/**
		 * Compute to.
		 *
		 * @param scope
		 *            the scope
		 * @return the t
		 */
		T computeTo(final IScope scope) {
			return constantTo == null ? value(scope, toExpression) : constantTo;
		}

		/**
		 * Compute step.
		 *
		 * @param scope
		 *            the scope
		 * @return the t
		 */
		T computeStep(final IScope scope) {
			return constantStep == null ? value(scope, stepExpression) : constantStep;
		}

		/**
		 * Step sign.
		 *
		 * @param isReverse
		 *            the is reverse
		 * @return the int
		 */
		int stepSign(final boolean isReverse) {
			return isReverse && !stepDefined ? -1 : 1;
		}

	}

	/**
	 * The Class IntBounded.
	 */
	class IntBounded extends Bounded<Integer> {

		/**
		 * Instantiates a new bounded.
		 *
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		IntBounded(final IExpression from, final IExpression to, final IExpression step) throws GamaRuntimeException {
			super(from, to, step);
		}

		@Override
		protected Integer value(final IScope scope, final IExpression exp) {
			return Cast.asInt(scope, exp.value(scope));
		}

		@Override
		Integer defaultStep() {
			return 1;
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			final int from = computeFrom(scope);
			final int to = computeTo(scope);
			boolean reverse = from > to;
			final int step = computeStep(scope) * stepSign(reverse);
			for (int i = from; reverse ? i >= to : i <= to; i += step) {
				if (BREAK_STATUSES.contains(loopBody(scope, i, result))) { break; }
			}
			return result[0];
		}

	}

	/**
	 * The Class FloatBounded.
	 */
	class FloatBounded extends Bounded<Double> {

		/**
		 * Instantiates a new bounded.
		 *
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		FloatBounded(final IExpression from, final IExpression to, final IExpression step) throws GamaRuntimeException {
			super(from, to, step);
		}

		@Override
		Double defaultStep() {
			return 1d;
		}

		@Override
		protected Double value(final IScope scope, final IExpression exp) {
			return Cast.asFloat(scope, exp.value(scope));
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			final double from = computeFrom(scope);
			final double to = computeTo(scope);
			boolean reverse = from > to;
			final double step = computeStep(scope) * stepSign(reverse);
			for (double i = from; reverse ? i >= to : i <= to; i += step) {
				if (BREAK_STATUSES.contains(loopBody(scope, i, result))) { break; }
			}
			return result[0];
		}

	}

	/**
	 * The Class Over.
	 */
	class Over implements LoopExecuter {

		/** The over expression. */
		private final IExpression overExpression;

		/**
		 * Instantiates a new over.
		 *
		 * @param over
		 *            the over
		 */
		Over(final IExpression over) {
			overExpression = getFacet(IKeyword.OVER);
		}

		/** The over. */

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			final Object obj = overExpression.value(scope);
			final Iterable list = !(obj instanceof IContainer c) ? Cast.asList(scope, obj) : c.iterable(scope);
			for (final Object each : list) { if (BREAK_STATUSES.contains(loopBody(scope, each, result))) { break; } }
			return result[0];
		}
	}

	/**
	 * The Class Times.
	 */
	class Times implements LoopExecuter {

		/** The times. */
		private final IExpression timesExpression;
		/** The constant times. */
		private Integer constantTimes;

		/**
		 * Instantiates a new times.
		 *
		 * @param times
		 *            the times
		 */
		Times(final IExpression times) {
			this.timesExpression = times;
			if (timesExpression.isConst()) {
				constantTimes = Types.INT.cast(null, timesExpression.getConstValue(), null, false);
			}
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			final int max = constantTimes == null ? Cast.asInt(scope, timesExpression.value(scope)) : constantTimes;
			for (int i = 0; i < max; i++) { if (BREAK_STATUSES.contains(loopBody(scope, null, result))) { break; } }
			return result[0];
		}

	}

	/**
	 * The Class While.
	 */
	class While implements LoopExecuter {

		/** The cond. */
		private final IExpression cond;

		/**
		 * Instantiates a new while.
		 *
		 * @param cond
		 *            the cond.
		 */
		While(final IExpression cond) {
			this.cond = cond;
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			while (Cast.asBool(scope, cond.value(scope))) {
				if (BREAK_STATUSES.contains(loopBody(scope, null, result))) { break; }
			}
			return result[0];
		}

	}

}
