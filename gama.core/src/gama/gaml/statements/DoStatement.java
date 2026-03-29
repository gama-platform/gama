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
 * Implements the {@code do} / {@code invoke} / {@code .} statement which allows an agent to execute an action or
 * primitive at runtime.
 *
 * <h2>Execution model</h2>
 * <p>
 * All three syntactic forms of action calls are normalised at parse time (see
 * {@link gaml.compiler.gaml.parsing.GamlSyntacticConverter#processDo}) and compiled at validation time (see
 * {@link gaml.compiler.gaml.descriptions.DoDescription#validate()}) into a single
 * {@link gaml.compiler.gaml.expression.ActionCallOperator} expression that is stored in the
 * {@link IInternalFacets#INTERNAL_FUNCTION} facet. At runtime, {@link #privateExecuteIn(IScope)} simply evaluates that
 * expression via {@code function.value(scope)}, so all lookup, dispatch, and argument-passing logic lives in
 * {@link gaml.compiler.gaml.expression.ActionCallOperator}.
 * </p>
 *
 * <h2>Thread safety</h2>
 * <p>
 * All fields of this class are either {@code final} or encapsulated inside an
 * {@link gaml.compiler.gaml.expression.ActionCallOperator} that is itself thread-safe. No additional synchronisation is
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
		value = "Allows the agent to execute an action or a primitive.  For a list of primitives available in every species, see this [BuiltIn161 page]; for the list of primitives defined by the different skills, see this [Skills161 page]. Finally, see this [Species161 page] to know how to declare custom actions.",
		usages = { @usage (
				value = "The simple syntax (when the action does not expect any argument and the result is not to be kept) is:",
				examples = { @example (
						value = "do name_of_action_or_primitive;",
						isExecutable = false) }),
				@usage (
						value = "In case the action expects one or more arguments to be passed, they are defined by using facets (enclosed tags or a map are now deprecated):",
						examples = { @example (
								value = "do name_of_action_or_primitive arg1: expression1 arg2: expression2;",
								isExecutable = false) }),
				@usage (
						value = "In case the result of the action needs to be made available to the agent, the action can be called with the agent calling the action (`self` when the agent itself calls the action) instead of `do`; the result should be assigned to a temporary variable:",
						examples = { @example (
								value = "type_returned_by_action result <- self name_of_action_or_primitive [];",
								isExecutable = false) }),
				@usage (
						value = "In case of an action expecting arguments and returning a value, the following syntax is used:",
						examples = { @example (
								value = "type_returned_by_action result <- self name_of_action_or_primitive [arg1::expression1, arg2::expression2];",
								isExecutable = false) }),
				@usage (
						value = "Deprecated uses: following uses of the `do` statement (still accepted) are now deprecated:",
						examples = { @example (
								value = "// Simple syntax: "),
								@example (
										value = "do action: name_of_action_or_primitive;",
										isExecutable = false),
								@example (""), @example (
										value = "// In case the result of the action needs to be made available to the agent, the `returns` keyword can be defined; the result will then be referred to by the temporary variable declared in this attribute:"),
								@example (
										value = "do name_of_action_or_primitive returns: result;",
										isExecutable = false),
								@example (
										value = "do name_of_action_or_primitive arg1: expression1 arg2: expression2 returns: result;",
										isExecutable = false),
								@example (
										value = "type_returned_by_action result <- name_of_action_or_primitive(self, [arg1::expression1, arg2::expression2]);",
										isExecutable = false),
								@example (""), @example (
										value = "// In case the result of the action needs to be made available to the agent"),
								@example (
										value = "let result <- name_of_action_or_primitive(self, []);",
										isExecutable = false),
								@example (""), @example (
										value = "// In case the action expects one or more arguments to be passed, they can also be defined by using enclosed `arg` statements, or the `with` facet with a map of parameters:"),
								@example (
										value = "do name_of_action_or_primitive with: [arg1::expression1, arg2::expression2];",
										isExecutable = false),
								@example (
										value = "",
										isExecutable = false),
								@example (
										value = "or",
										isExecutable = false),
								@example (
										value = "",
										isExecutable = false),
								@example (
										value = "do name_of_action_or_primitive {",
										isExecutable = false),
								@example (
										value = "     arg arg1 value: expression1;",
										isExecutable = false),
								@example (
										value = "     arg arg2 value: expression2;",
										isExecutable = false),
								@example (
										value = "     ...",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) })
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
		 * {@link gaml.compiler.gaml.expression.ActionCallOperator} stored in the
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
				if (compiled instanceof gaml.compiler.gaml.expression.ActionCallOperator aco) {
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
	 * {@link gaml.compiler.gaml.expression.ActionCallOperator} that encapsulates the target agent expression, the
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
	 * The compiled {@link gaml.compiler.gaml.expression.ActionCallOperator} produced during validation is retrieved
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
	 * Executes the action call by evaluating the compiled {@link gaml.compiler.gaml.expression.ActionCallOperator}.
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