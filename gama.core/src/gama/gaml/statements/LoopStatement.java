/*******************************************************************************************************
 *
 * LoopStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2.0.0).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama2 for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
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

/**
 * The Class LoopStatement.
 */

/**
 * The Class LoopStatement.
 */

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
		value = "Allows the agent to perform the same set of statements either a fixed number of times, or while a condition is true, or by progressing in a collection of elements or along an interval of numbers. Be aware that there are no prevention of infinite loops. As a consequence, open loops should be used with caution, as one agent may block the execution of the whole model.",
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
						value = "The basic syntax for repeating a set of statements while an index iterates over a range of values with a fixed step of 1 is:",
						examples = { @example (
								value = "loop a_temp_var from: int_expression_1 to: int_expression_2 {",
								isExecutable = false),
								@example (
										value = "     // [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "The incrementation step of the index can also be chosen:",
						examples = { @example (
								value = "loop a_temp_var from: int_expression_1 to: int_expression_2 step: int_expression3 {",
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
										value = "loop i from: 10 to: 30 step: 10 {sumFor <- sumFor + i;}",
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
						value = "The second (quite common) case of the loop syntax allows one to use an interval of integers or floats. The from and to facets take an int or float expression as arguments, with the first (resp. the last) specifying the beginning (resp. end) of the inclusive interval (i.e. [to, from]). If the step is not defined, it is assumed to be equal to 1 or -1, depending on the direction of the range. If it is defined, its sign will be respected, so that a positive step will never allow the loop to enter a loop from i to j where i is greater than j",
						examples = { @example (
								value = "list the_list <-list (species_of (self));"),
								@example (
										value = "loop i from: 0 to: length (the_list) - 1 {"),
								@example (
										value = "     ask the_list at i {"),
								@example (
										value = "        // ..."),
								@example (
										value = "     }"),
								@example (
										value = "} // every  agent of the list is asked to do something") }) })
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
				description.error("'loop' is missing the 'from:' facet", IGamlIssue.MISSING_FACET,
						description.getUnderlyingElement(), FROM, "0");
			} else {
				description.error("Missing the definition of the kind of loop to perform (times, over, while, from/to)",
						IGamlIssue.MISSING_FACET);
			}
		}

		/**
		 * Process from to.
		 *
		 * @param description
		 *            the description
		 * @param to
		 *            the to
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
				description.error("'loop' is missing the 'to:' facet", IGamlIssue.MISSING_FACET,
						description.getUnderlyingElement(), TO, "0");
			}
		}

		/**
		 * Process from to.
		 *
		 * @param description
		 *            the description
		 * @param from
		 *            the from
		 * @param to
		 *            the to
		 * @param name
		 *            the name
		 */
		private void processCond(final IDescription description, final IExpressionDescription from,
				final IExpressionDescription to, final IExpressionDescription name) {
			if (from != null) {
				description.error("'while' and 'from' are not compatible", IGamlIssue.CONFLICTING_FACETS, WHILE, FROM);
			}
			if (to != null) {
				description.error("'while' and 'to' are not compatible", IGamlIssue.CONFLICTING_FACETS, WHILE, TO);
			}
			if (name != null) { description.error("No variable should be declared", IGamlIssue.UNUSED, WHILE, NAME); }
		}

		/**
		 * Process over.
		 *
		 * @param description
		 *            the description
		 * @param from
		 *            the from
		 * @param to
		 *            the to
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
				description.error("'over' and 'from' are not compatible", IGamlIssue.CONFLICTING_FACETS, OVER, FROM);
			} else if (to != null) {
				description.error("'over' and 'to' are not compatible", IGamlIssue.CONFLICTING_FACETS, OVER, TO);
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
		 * @param from
		 *            the from
		 * @param to
		 *            the to
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
				description.error("'times' and 'from' are not compatible", IGamlIssue.CONFLICTING_FACETS, TIMES, FROM);
			} else if (to != null) {
				description.error("'times' and 'to' are not compatible", IGamlIssue.CONFLICTING_FACETS, TIMES, TO);
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
	// private final Object[] result = new Object[1];

	/**
	 * Instantiates a new loop statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public LoopStatement(final IDescription desc) {
		super(desc);
		final boolean isWhile = getFacet(IKeyword.WHILE) != null;
		final boolean isList = getFacet(IKeyword.OVER) != null;
		final boolean isBounded = getFacet(IKeyword.FROM) != null && getFacet(IKeyword.TO) != null;
		varName = desc.getName();
		executer = isWhile ? new While() : isList ? new Over() : isBounded ? new Bounded() : new Times();
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
	 * @param theVar
	 *            the var
	 * @param result
	 *            the result
	 * @return true, if successful
	 */
	protected FlowStatus loopBody(final IScope scope, final Object theVar, final Object[] result) {
		scope.push(this);
		// We set it explicitely to the newly created scope
		if (varName != null) { scope.setVarValue(varName, theVar, true); }
		result[0] = super.privateExecuteIn(scope);
		scope.pop(this);
		// return !scope.interrupted();
		return scope.getAndClearContinueStatus();
	}

	/**
	 * The Interface LoopExecuter.
	 */
	interface LoopExecuter {

		/**
		 * Run in.
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
	class Bounded implements LoopExecuter {

		/** The from. */
		private final IExpression from = getFacet(IKeyword.FROM);

		/** The to. */
		private final IExpression to = getFacet(IKeyword.TO);

		/** The step. */
		private final IExpression step = getFacet(IKeyword.STEP);

		/** The constant step. */
		private Number constantFrom;

		/** The constant to. */
		private Number constantTo;

		/** The constant step. */
		private Number constantStep;

		/** The step defined. */
		private final boolean stepDefined;

		/** The is int. */
		private final boolean isInt;

		/**
		 * Instantiates a new bounded.
		 *
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		Bounded() throws GamaRuntimeException {
			final IScope scope = null;
			// final IScope scope = GAMA.obtainNewScope();
			isInt = from.getGamlType() == Types.INT && to.getGamlType() == Types.INT
					&& (step == null || step.getGamlType() == Types.INT);
			if (from.isConst()) { constantFrom = getFromExp(scope, from); }
			if (to.isConst()) { constantTo = getFromExp(scope, to); }
			if (step == null) {
				stepDefined = false;
				constantStep = 1;
			} else if (step.isConst()) {
				stepDefined = true;
				constantStep = getFromExp(scope, step);
			} else {
				stepDefined = true;
			}
		}

		/**
		 * Gets the from exp.
		 *
		 * @param scope
		 *            the scope
		 * @param exp
		 *            the exp
		 * @return the from exp
		 */
		Number getFromExp(final IScope scope, final IExpression exp) {
			return isInt ? Cast.asInt(scope, exp.value(scope)) : Cast.asFloat(scope, exp.value(scope));
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			final Number f = constantFrom == null ? getFromExp(scope, from) : constantFrom;
			final Number t = constantTo == null ? getFromExp(scope, to) : constantTo;
			Number s = constantStep == null ? getFromExp(scope, step) : constantStep;
			boolean shouldBreak = false;
			if (f.equals(t)) {
				loopBody(scope, f, result);
			} else if (f.doubleValue() - t.doubleValue() > 0) {
				if (s.doubleValue() > 0) {
					if (stepDefined) return null;
					if (s instanceof Integer) {
						s = -s.intValue();
					} else {
						s = -s.doubleValue();
					}
				}
				if (isInt) {
					for (int i = f.intValue(), n = t.intValue(); i >= n && !shouldBreak; i += s.intValue()) {
						FlowStatus status = loopBody(scope, i, result);
						switch (status) {
							case CONTINUE:
								continue;
							case BREAK, RETURN, DIE, DISPOSE:
								shouldBreak = true;
								break;
							default:
						}
					}
				} else {
					for (double i = f.doubleValue(), n = t.doubleValue(); i >= n && !shouldBreak; i +=
							s.doubleValue()) {
						FlowStatus status = loopBody(scope, i, result);
						switch (status) {
							case CONTINUE:
								continue;
							case BREAK, RETURN, DIE, DISPOSE:
								shouldBreak = true;
								break;
							default:
						}
					}
				}
			} else if (isInt) {
				for (int i = f.intValue(), n = t.intValue(); i <= n && !shouldBreak; i += s.intValue()) {
					FlowStatus status = loopBody(scope, i, result);
					switch (status) {
						case CONTINUE:
							continue;
						case BREAK, RETURN, DIE, DISPOSE:
							shouldBreak = true;
							break;
						default:
					}
				}
			} else {
				for (double i = f.doubleValue(), n = t.doubleValue(); i <= n && !shouldBreak; i += s.doubleValue()) {
					FlowStatus status = loopBody(scope, i, result);
					switch (status) {
						case CONTINUE:
							continue;
						case BREAK, RETURN, DIE, DISPOSE:
							shouldBreak = true;
							break;
						default:
					}
				}
			}
			return result[0];
		}
	}

	/**
	 * The Class Over.
	 */
	class Over implements LoopExecuter {

		/** The over. */
		private final IExpression overExpression = getFacet(IKeyword.OVER);

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			final Object obj = overExpression.value(scope);
			final Iterable list = !(obj instanceof IContainer c) ? Cast.asList(scope, obj) : c.iterable(scope);
			boolean shouldBreak = false;

			for (final Object each : list) {
				switch (loopBody(scope, each, result)) {
					case CONTINUE:
						continue;
					case BREAK, RETURN, DIE, DISPOSE:
						shouldBreak = true;
						break;
					default:
				}
				if (shouldBreak) { break; }
			}
			return result[0];
		}
	}

	/**
	 * The Class Times.
	 */
	class Times implements LoopExecuter {

		/** The times. */
		private final IExpression timesExpression = getFacet(IKeyword.TIMES);

		/** The constant times. */
		private Integer constantTimes;

		/**
		 * Instantiates a new times.
		 *
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		Times() throws GamaRuntimeException {
			if (timesExpression.isConst()) {
				constantTimes = Types.INT.cast(null, timesExpression.getConstValue(), null, false);
			}
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			final int max = constantTimes == null ? Cast.asInt(scope, timesExpression.value(scope)) : constantTimes;
			boolean shouldBreak = false;
			for (int i = 0; i < max && !shouldBreak; i++) {
				switch (loopBody(scope, null, result)) {
					case CONTINUE:
						continue;
					case BREAK, RETURN, DIE, DISPOSE:
						shouldBreak = true;
						break;
					default:
				}
			}
			return result[0];
		}

	}

	/**
	 * The Class While.
	 */
	class While implements LoopExecuter {

		/** The cond. */
		private final IExpression cond = getFacet(IKeyword.WHILE);

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object[] result = new Object[1];
			boolean shouldBreak = false;
			while (Boolean.TRUE.equals(Cast.asBool(scope, cond.value(scope))) && !shouldBreak) {
				switch (loopBody(scope, null, result)) {
					case CONTINUE:
						continue;
					case BREAK, RETURN, DIE, DISPOSE:
						shouldBreak = true;
						break;
					default:
				}
			}
			return result[0];
		}
	}

}
