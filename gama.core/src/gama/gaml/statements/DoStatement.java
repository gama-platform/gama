/*******************************************************************************************************
 *
 * DoStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

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
import gama.api.compilation.IInternalFacets;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.gaml.statements.DoStatement.DoSerializer;

/**
 * Implements the {@code do} / {@code invoke} / {@code .} statement, which allows an agent to execute an action or
 * primitive at runtime.
 *
 * <h2>Preferred syntax</h2>
 * <p>
 * The recommended way to call an action is the <em>dot-notation</em>:
 * {@code target.action_name(arg1: val1, arg2: val2)} (or simply {@code target.action_name()} when no arguments are
 * needed). When an agent calls one of its own actions it can use the {@code do} statement with the functional form:
 * {@code do action_name(arg1: val1, arg2: val2);}. Both forms produce exactly the same compiled representation.
 * </p>
 *
 * <h2>Execution model</h2>
 * <p>
 * All syntactic forms are normalised at parse time (see
 * {@link gaml.compiler.parsing.GamlSyntacticConverter#processDo}) and compiled at validation time (see
 * {@link gaml.compiler.descriptions.DoDescription#validate()}) into a single
 * {@link gaml.compiler.expressions.ActionCallOperator} expression stored in the
 * {@link IInternalFacets#INTERNAL_FUNCTION} facet. At runtime, {@link #privateExecuteIn(IScope)} simply evaluates that
 * expression via {@code function.value(scope)}, so all lookup, dispatch and argument-passing logic lives in
 * {@link gaml.compiler.expressions.ActionCallOperator}.
 * </p>
 *
 * <h2>Thread safety</h2>
 * <p>
 * All fields of this class are either {@code final} or encapsulated inside an
 * {@link gaml.compiler.expressions.ActionCallOperator} that is itself thread-safe. No additional synchronisation is
 * needed here.
 * </p>
 *
 * <p>
 * Originally written by drogoul, modified on 7 févr. 2010.
 * </p>
 */
@symbol (
		name = { IKeyword.DO, IKeyword.INVOKE, IKeyword._DOT },
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = true,
		with_scope = false,
		concept = { IConcept.ACTION },
		with_args = false)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER },
		symbols = IKeyword.CHART)
@facets (
		value = { @facet (
				name = IInternalFacets.INTERNAL_FUNCTION,
				type = IType.NONE,
				optional = true,
				internal = true),
				@facet (
						name = IInternalFacets.INTERNAL_TARGET,
						type = IType.NONE,
						optional = true,
						internal = true),
				@facet (
						name = IInternalFacets.INTERNAL_NAME,
						type = IType.LABEL,
						optional = true,
						internal = true), },
		omissible = IKeyword.ACTION)
@doc (
		value = "Executes an action or primitive belonging to the calling agent or to another agent. "
				+ "The functional form `do action_name(arg: value, ...)` is mandatory for the `do` statement; "
				+ "the dot-notation `target.action_name(arg: value, ...)` is the preferred form when calling actions on other agents or when the result must be captured, and will progressively become the norm. "
				+ "For built-in primitives see the BuiltIn and Skills pages; for custom actions see the Species page.",
		usages = { @usage (
				value = "Calling an action with no arguments — `do` form (self) and dot-notation (any target):",
				examples = @example (
						value = """
								do my_action();                         // calls my_action on self, result discarded
								some_agent.my_action();                 // calls my_action on another agent
								int result <- some_agent.my_action();   // captures the returned value""",
						isExecutable = false)),
				@usage (
						value = "Passing arguments — arguments are always passed in the functional form `(arg: value, ...)`:",
						examples = @example (
								value = """
										do my_action(arg1: expression1, arg2: expression2);
										int result <- some_agent.my_action(arg1: expression1, arg2: expression2);""",
								isExecutable = false)),
				@usage (
						value = "Tolerated (but not preferred) facet-based form — arguments can still be provided as inline facets on the `do` statement:",
						examples = @example (
								value = "do my_action arg1: expression1 arg2: expression2;",
								isExecutable = false)),
				@usage (
						value = "Deprecated forms — no longer accepted, listed here for migration purposes only:",
						examples = @example (
								value = """
										do action: my_action;                                    // explicit 'action:' facet
										do my_action returns: result;                            // 'returns:' no longer exists
										do my_action with: [arg1::expression1];                  // 'with:' no longer works
										do my_action { arg arg1 value: expression1; }            // 'arg' sub-statements removed
										let result <- my_action(self, [arg1::expression1]);      // 'let' no longer exists
										type result <- my_action(self, []);                      // old operator call form""",
								isExecutable = false)) })
@serializer (DoSerializer.class)
public class DoStatement extends AbstractStatementSequence {

	/**
	 * Serialiser for {@code do} / {@code invoke} statements.
	 *
	 * <p>
	 * Serialises argument names and values from child arg descriptions. Only the facets listed in
	 * {@link ArtefactRegistry#DO_FACETS} are serialised; internal housekeeping facets (e.g.
	 * {@link IInternalFacets#INTERNAL_FUNCTION}) are excluded.
	 * </p>
	 */
	public static class DoSerializer extends StatementSerializer {

		/**
		 * Serialises a single argument facet.
		 *
		 * <p>
		 * Positional arguments (whose name is a number) are serialised as bare values; named arguments are serialised
		 * as {@code name:value} pairs.
		 * </p>
		 *
		 * @param desc
		 *            the enclosing statement description
		 * @param arg
		 *            the argument description
		 * @param sb
		 *            the buffer to append to
		 * @param includingBuiltIn
		 *            whether to include built-in symbols
		 */
		@Override
		protected void serializeArg(final IDescription desc, final IDescription arg, final StringBuilder sb,
				final boolean includingBuiltIn) {
			final String name = arg.getName();
			final IExpressionDescription value = arg.getFacet(VALUE);
			if (StringUtils.isGamaNumber(name)) {
				sb.append(value.serializeToGaml(includingBuiltIn));
			} else {
				sb.append(name).append(":").append(value.serializeToGaml(includingBuiltIn));
			}
		}

		/**
		 * Serialises the arguments of the action call from the compiled
		 * {@link gaml.compiler.expressions.ActionCallOperator} stored in the
		 * {@link IInternalFacets#INTERNAL_FUNCTION} facet.
		 *
		 * <p>
		 * Falls back to the super-class implementation (which uses {@code passedArgs} or formal arg children) only when
		 * no compiled expression is available (e.g. during partial compilation).
		 * </p>
		 *
		 * @param s
		 *            the statement description
		 * @param sb
		 *            the buffer to append to
		 * @param includingBuiltIn
		 *            whether to include built-in symbols
		 */
		@Override
		protected void serializeArgs(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
			final IExpressionDescription functionFacet = s.getFacet(IInternalFacets.INTERNAL_FUNCTION);
			if (functionFacet != null) {
				final IExpression compiled = functionFacet.getExpression();
				if (compiled instanceof gaml.compiler.expressions.ActionCallOperator aco) {
					final StringBuilder argsBuf = new StringBuilder();
					aco.argsToGaml(argsBuf, includingBuiltIn);
					if (argsBuf.length() > 0) { sb.append("(").append(argsBuf).append(")"); }
					return;
				}
			}
			// Fallback: no compiled expression available
			super.serializeArgs(s, sb, includingBuiltIn);
		}

		/**
		 * Serialises a single facet value, returning {@code null} for internal/housekeeping facets that should not
		 * appear in the serialised output.
		 *
		 * @param s
		 *            the enclosing statement description
		 * @param key
		 *            the facet name
		 * @param includingBuiltIn
		 *            whether to include built-in symbols
		 * @return the serialised facet value, or {@code null} to skip this facet
		 */
		@Override
		public String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
			return null;
		}
	}

	/**
	 * The compiled action-call expression. After validation this is always an
	 * {@link gaml.compiler.expressions.ActionCallOperator} that encapsulates the target agent expression, the
	 * action description, and the compiled arguments. {@link #privateExecuteIn(IScope)} simply evaluates this
	 * expression.
	 *
	 * <p>
	 * Immutable after construction (set in the constructor from the description's {@code INTERNAL_FUNCTION} facet).
	 * </p>
	 */
	final IExpression function;

	/**
	 * Constructs a {@code DoStatement} from its compile-time description.
	 *
	 * <p>
	 * The compiled {@link gaml.compiler.expressions.ActionCallOperator} produced during validation is retrieved
	 * from the {@link IInternalFacets#INTERNAL_FUNCTION} facet and stored as {@link #function}.
	 * </p>
	 *
	 * @param desc
	 *            the compile-time description; must not be {@code null}
	 */
	public DoStatement(final IDescription desc) {
		super(desc);
		function = getFacet(IInternalFacets.INTERNAL_FUNCTION);
		setName(function != null ? function.getName() : getLiteral(IInternalFacets.INTERNAL_NAME));
	}

	// -------------------------------------------------------------------------
	// Execution
	// -------------------------------------------------------------------------

	/**
	 * Executes the action call by evaluating the compiled {@link gaml.compiler.expressions.ActionCallOperator}.
	 *
	 * <p>
	 * The operator resolves the target agent, locates the action in the appropriate species or class, and passes the
	 * compiled arguments — all in a single {@code value(scope)} call. No additional species lookup or argument
	 * resolution is required here.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @return the value returned by the action, or {@code null} when the action returns nothing or {@link #function} is
	 *         {@code null}
	 * @throws GamaRuntimeException
	 *             if the action throws a runtime error
	 */
	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		if (function == null) return null;
		return function.value(scope);
	}

	/**
	 * Disposes of this statement by delegating to the superclass. The {@link #function} expression is managed
	 * externally (as part of the species description) and is not disposed here.
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

}