/*******************************************************************************************************
 *
 * ActionStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

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
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.compilation.validation.ActionValidator;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.ActionStatement.ActionSerializer;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * Implementation of GAML action statements.
 * 
 * <p>
 * Actions are reusable, parameterized procedures that can be defined in species, models, or experiments. They function
 * similarly to methods in object-oriented programming, accepting parameters and optionally returning values.
 * </p>
 * 
 * <h2>Features</h2>
 * <ul>
 * <li><b>Parameters:</b> Actions can accept typed parameters with optional default values</li>
 * <li><b>Return Values:</b> Actions can return values of any type</li>
 * <li><b>Invocation:</b> Actions are called using the 'do' statement</li>
 * <li><b>Inheritance:</b> Actions can be overridden in child species</li>
 * <li><b>Virtual Actions:</b> Abstract actions that must be implemented by child species</li>
 * </ul>
 * 
 * <h2>Declaration Syntax</h2>
 *
 * <pre>
 * {@code
 * // Simple action with no parameters or return value
 * action simple_action {
 *     write "Executing simple action";
 * }
 * 
 * // Action with parameters
 * action move_to(point target, float speed) {
 *     location <- location + (target - location) * speed;
 * }
 * 
 * // Action with return value
 * float distance_to(agent other) {
 *     return self distance_to other;
 * }
 * 
 * // Virtual action (must be implemented by subclasses)
 * int virtual calculate_fitness;
 * }
 * </pre>
 * 
 * <h2>Invocation</h2>
 *
 * <pre>
 * {@code
 * // Call without parameters
 * do simple_action;
 * 
 * // Call with parameters
 * do move_to(target: my_target, speed: 0.5);
 * 
 * // Call with return value
 * float dist <- distance_to(other_agent);
 * }
 * </pre>
 * 
 * <h2>Argument Handling</h2>
 * <p>
 * The action maintains two sets of arguments:
 * </p>
 * <ul>
 * <li><b>Formal arguments:</b> Parameter declarations with types and defaults</li>
 * <li><b>Runtime arguments:</b> Actual values provided during invocation</li>
 * </ul>
 * <p>
 * Runtime arguments are complemented with formal arguments to fill in any missing values from defaults.
 * </p>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see AbstractStatementSequenceWithArgs
 * @see IStatement.Action
 * @see Arguments
 */
@symbol (
		name = IKeyword.ACTION,
		kind = ISymbolKind.ACTION,
		with_sequence = true,
		with_args = true,
		unique_name = true,
		concept = { IConcept.SPECIES, IConcept.ACTION })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL, ISymbolKind.CLASS })
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				doc = @doc ("identifier of the action")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("the action returned type"),
						internal = true),
				@facet (
						name = IKeyword.OF,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("if the action returns a container, the type of its elements"),
						internal = true),
				@facet (
						name = IKeyword.INDEX,
						type = IType.TYPE_ID,
						optional = true,
						doc = @doc ("if the action returns a map, the type of its keys"),
						internal = true),
				@facet (
						name = IKeyword.VIRTUAL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("whether the action is virtual (defined without a set of instructions) (false by default)")), },
		omissible = IKeyword.NAME)
@doc (
		value = "Allows to define in a species, model or experiment a new action that can be called elsewhere.",
		usages = { @usage (
				value = "The simplest syntax to define an action that does not take any parameter and does not return anything is:",
				examples = { @example (
						value = "action simple_action {",
						isExecutable = false),
						@example (
								value = "   // [set of statements]",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "If the action needs some parameters, they can be specified betwee, braquets after the identifier of the action:",
						examples = { @example (
								value = "action action_parameters(int i, string s){",
								isExecutable = false),
								@example (
										value = "   // [set of statements using i and s]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "If the action returns any value, the returned type should be used instead of the \"action\" keyword. A return statement inside the body of the action statement is mandatory.",
						examples = { @example (
								value = "int action_return_val(int i, string s){",
								isExecutable = false),
								@example (
										value = "   // [set of statements using i and s]",
										isExecutable = false),
								@example (
										value = "   return i + i;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "If virtual: is true, then the action is abstract, which means that the action is defined without body. A species containing at least one abstract action is abstract. Agents of this species cannot be created. The common use of an abstract action is to define an action that can be used by all its sub-species, which should redefine all abstract actions and implements its body.",
						examples = { @example (
								value = "species parent_species {",
								isExecutable = false),
								@example (
										value = "   int virtual_action(int i, string s);",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false),
								@example (
										value = "",
										isExecutable = false),
								@example (
										value = "species children parent: parent_species {",
										isExecutable = false),
								@example (
										value = "   int virtual_action(int i, string s) {",
										isExecutable = false),
								@example (
										value = "      return i + i;",
										isExecutable = false),
								@example (
										value = "   }",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) },
		see = { "do" })
@validator (ActionValidator.class)
@serializer (ActionSerializer.class)
@SuppressWarnings ({ "rawtypes" })
public class ActionStatement extends AbstractStatementSequenceWithArgs implements IStatement.Action {

	/**
	 * Custom serializer for action statements.
	 * 
	 * <p>
	 * This serializer generates proper GAML syntax for actions, including the return type as the keyword (instead of
	 * 'action') when a return type is specified.
	 * </p>
	 */
	public static class ActionSerializer extends StatementSerializer {

		@Override
		public String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
			if (TYPE.equals(key)) return null;
			return super.serializeFacetValue(s, key, includingBuiltIn);
		}

		@Override
		protected void serializeArg(final IDescription desc, final IDescription arg, final StringBuilder sb,
				final boolean includingBuiltIn) {
			final String name = arg.getLitteral(NAME);
			final IExpressionDescription type = arg.getFacet(TYPE);
			final IExpressionDescription def = arg.getFacet(DEFAULT);

			sb.append(type == null ? "unknown" : type.serializeToGaml(includingBuiltIn)).append(" ").append(name);
			if (def != null) { sb.append(" <- ").append(def.serializeToGaml(includingBuiltIn)); }
		}

		@Override
		public void serializeKeyword(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
			String type = desc.getGamlType().serializeToGaml(includingBuiltIn);
			if (UNKNOWN.equals(type)) { type = ACTION; }
			sb.append(type).append(" ");
		}

	}

	/** The formal parameter declarations for this action. */
	Arguments formalArgs = new Arguments();

	/**
	 * Constructs a new action statement.
	 * 
	 * <p>
	 * Initializes the action with its description and extracts the action name from the NAME facet.
	 * </p>
	 *
	 * @param desc
	 *            the action description
	 */
	public ActionStatement(final IDescription desc) {
		super(desc);
		if (hasFacet(IKeyword.NAME)) { name = getLiteral(IKeyword.NAME); }

	}

	/**
	 * Exits the action's scope and clears the return status.
	 * 
	 * <p>
	 * Actions always clear the action-halted status to ensure return statements don't affect outer scopes.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 */
	@Override
	public void leaveScope(final IScope scope) {
		// Clears any _action_halted status present
		scope.getAndClearReturnStatus();
		super.leaveScope(scope);
	}

	/**
	 * Sets the runtime argument values, complementing them with formal arguments.
	 * 
	 * <p>
	 * This method fills in any missing runtime arguments with values from the formal argument definitions (including
	 * default values).
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param args
	 *            the runtime arguments
	 */
	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {
		super.setRuntimeArgs(scope, args);
		actualArgs.get().complementWith(formalArgs);
	}

	/**
	 * Sets the formal parameter declarations for this action.
	 * 
	 * <p>
	 * Formal arguments define the parameters this action accepts, including their names, types, and default values.
	 * </p>
	 *
	 * @param args
	 *            the formal argument declarations
	 */
	@Override
	public void setFormalArgs(final Arguments args) {
		formalArgs.putAll(args);
	}

	@Override
	public void dispose() {
		formalArgs.dispose();
		super.dispose();
	}
}
