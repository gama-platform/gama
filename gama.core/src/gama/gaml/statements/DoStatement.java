/*******************************************************************************************************
 *
 * DoStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.Set;

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
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.PrimitiveDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.descriptions.SymbolSerializer.StatementSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Strings;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.DoStatement.DoSerializer;
import gama.gaml.statements.DoStatement.DoValidator;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 7 févr. 2010
 *
 * @todo Description
 *
 */
@symbol (
		name = { IKeyword.DO, IKeyword.INVOKE },
		kind = ISymbolKind.SINGLE_STATEMENT,
		with_sequence = true,
		with_scope = false,
		concept = { IConcept.ACTION },
		with_args = true)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER },
		symbols = IKeyword.CHART)
@facets (
		value = { @facet (
				name = IKeyword.ACTION,
				type = IType.ID,
				optional = false,
				doc = @doc ("the name of an action or a primitive")),
				@facet (
						name = IKeyword.WITH,
						type = IType.MAP,
						of = IType.NONE,
						index = IType.STRING,
						optional = true,
						doc = @doc (
								value = "a map expression containing the parameters of the action")),
				@facet (
						name = IKeyword.INTERNAL_FUNCTION,
						type = IType.NONE,
						optional = true,
						internal = true),
		},
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
@validator (DoValidator.class)
@serializer (DoSerializer.class)
public class DoStatement extends AbstractStatementSequence implements IStatement.WithArgs {

	/**
	 * The Class DoSerializer.
	 */
	public static class DoSerializer extends StatementSerializer {

		@Override
		protected void serializeArg(final IDescription desc, final IDescription arg, final StringBuilder sb,
				final boolean includingBuiltIn) {
			final String name = arg.getName();
			final IExpressionDescription value = arg.getFacet(VALUE);
			if (Strings.isGamaNumber(name)) {
				sb.append(value.serializeToGaml(includingBuiltIn));
			} else {
				sb.append(name).append(":").append(value.serializeToGaml(includingBuiltIn));
			}

		}

		@Override
		protected String serializeFacetValue(final SymbolDescription s, final String key,
				final boolean includingBuiltIn) {
			if (!DO_FACETS.contains(key)) return null;
			return super.serializeFacetValue(s, key, includingBuiltIn);
		}

	}

	/**
	 * The Class DoValidator.
	 */
	public static class DoValidator implements IDescriptionValidator<StatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final StatementDescription desc) {
			final String action = desc.getLitteral(ACTION);

//			if ("update_outputs".equals(action)) {
//				DEBUG.LOG("Validating Do " + desc + " in thread " + Thread.currentThread().getName());
//				TypeDescription tt = (TypeDescription) desc.getDescriptionDeclaringAction(action, false);
//				if (tt == null) {
//
//					desc.getDescriptionDeclaringAction(action, false);
//				}
//
//			}
			final boolean isSuperInvocation = desc.isSuperInvocation();
			TypeDescription sd = desc.getSpeciesContext();
			if (!sd.hasAction(action, isSuperInvocation)) {
				desc.error("Action " + action + " does not exist in " + sd.getName(), IGamlIssue.UNKNOWN_ACTION, ACTION,
						action, sd.getName());
			}
			if (isSuperInvocation) { sd = sd.getParent(); }
			final ActionDescription ad = sd.getAction(action);
			if (ad instanceof PrimitiveDescription pd) {
				final String dep = pd.getDeprecated();
				if (dep != null) {
					desc.warning("Action " + action + " is deprecated: " + dep, IGamlIssue.DEPRECATED, ACTION);
				}
			}
		}

	}

	/** The args. */
	Arguments args;

	/** The target species. */
	final String targetSpecies;

	/** The function. */
	final IExpression function;

	/** The Constant DO_FACETS. */
	public static final Set<String> DO_FACETS = DescriptionFactory.getAllowedFacetsFor(IKeyword.DO, IKeyword.INVOKE);

	/**
	 * Instantiates a new do statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public DoStatement(final IDescription desc) {
		super(desc);

		if (((StatementDescription) desc).isSuperInvocation()) {
			final SpeciesDescription s = desc.getSpeciesContext().getParent();
			targetSpecies = s.getName();
		} else {
			targetSpecies = null;
		}
		function = getFacet(IKeyword.INTERNAL_FUNCTION);

		setName(getLiteral(IKeyword.ACTION));
	}

	@Override
	public void enterScope(final IScope scope) {
		super.enterScope(scope);
	}

	@Override
	public void setFormalArgs(final Arguments args) { this.args = args; }

	/**
	 * Gets the runtime args.
	 *
	 * @param scope
	 *            the scope
	 * @return the runtime args
	 */
	public Arguments getRuntimeArgs(final IScope scope) {
		if (args == null) return null;
		// Dynamic arguments necessary (see #2943, #2922, plus issue with multiple parallel simulations)
		return args.resolveAgainst(scope);
	}

	/**
	 * Returns the species on which to find the action. If a species target (desc) exists, then it is a super invocation
	 * and we have to find the corresponding action. Otherwise, we return the species of the agent
	 */
	private ISpecies getContext(final IScope scope) {
		// if ((targetSpecies != null) && "model".equals(targetSpecies)) {
		//
		// DEBUG.OUT("Model found to execute " + name);
		//
		// }

		return targetSpecies != null ? scope.getModel().getSpecies(targetSpecies) : scope.getAgent().getSpecies();
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final ISpecies species = getContext(scope);
		if (species == null)
			throw GamaRuntimeException.error("Impossible to find a species to execute " + getName(), scope);
		final IStatement.WithArgs executer = species.getAction(name);
		Object result = null;
		if (executer != null) {
			final ExecutionResult er = scope.execute(executer, getRuntimeArgs(scope));
			result = er.getValue();
		} else if (function != null) {
			result = function.value(scope);
		} else
			throw GamaRuntimeException.error("Impossible to find action " + getName() + " in " + species.getName(),
					scope);
		return result;
	}

	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {}

	@Override
	public void dispose() {
		if (args != null) { args.dispose(); }
		args = null;
		super.dispose();
	}

}
