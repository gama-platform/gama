/*******************************************************************************************************
 *
 * DoStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.Set;

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
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.ExecutionResult;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.descriptions.DoDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer.StatementSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.operators.Cast;
import gama.gaml.operators.Strings;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.DoStatement.DoSerializer;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 7 févr. 2010
 *
 * @todo Description
 *
 */

/**
 * The Class DoStatement.
 */
@symbol (
		name = { IKeyword.DO, IKeyword.INVOKE, "." },
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
				name = IKeyword.WITH,
				type = IType.MAP,
				of = IType.NONE,
				index = IType.STRING,
				optional = true,
				doc = @doc (
						value = "a map expression containing the parameters of the action",
						deprecated = "Use the imperative (with facets) or functional (with comma separated values inside parentheses, optionally prefixed by the argument name) form to pass the arguments")),
				@facet (
						name = IKeyword.RETURNS,
						type = IType.NEW_TEMP_ID,
						optional = true,
						doc = @doc ("Specifies the name of the temporary variable that will contain the result.")),
				@facet (
						name = IKeyword.ACTION,
						type = IType.ID,
						optional = false,
						doc = @doc ("the name of an action or a primitive")),
				@facet (
						name = IKeyword.INTERNAL_FUNCTION,
						type = IType.NONE,
						optional = true,
						internal = true),
				@facet (
						name = IKeyword.TARGET,
						type = IType.NONE,
						optional = true), },
		omissible = IKeyword.ACTION)
@doc (
		value = "Allows the agent to execute an action or a primitive (built-in actions available through custom species or skills).",
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

	/** The args. */
	Arguments args;

	/** The target species. */
	String targetSpecies;

	/** The function. */
	final IExpression function, returns, target;

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
		target = getFacet(IKeyword.TARGET);
		targetSpecies = null;
		if (((DoDescription) desc).isSuperInvocation()) {
			final SpeciesDescription s = desc.getSpeciesContext().getParent();
			targetSpecies = s.getName();
		} else if (target != null) {
			IType t = target.getGamlType();
			if (t.isAgentType()) {
				SpeciesDescription sd = t.getDenotedSpecies();
				if (sd != null) { targetSpecies = sd.getName(); }
			}
		}
		function = getFacet(IKeyword.INTERNAL_FUNCTION);
		returns = getFacet(IKeyword.RETURNS);
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
			if (target != null) {
				IAgent agent = Cast.asAgent(scope, target.value(scope));
				if (agent == null)
					throw GamaRuntimeException.error("The target of an action call must be an agent", scope);
				final ExecutionResult er = scope.execute(executer, agent, getRuntimeArgs(scope));
				result = er.getValue();
			} else {
				final ExecutionResult er = scope.execute(executer, getRuntimeArgs(scope));
				result = er.getValue();
			}
		} else if (function != null) {
			result = function.value(scope);
		} else
			throw GamaRuntimeException.error("Impossible to find action " + getName() + " in " + species.getName(),
					scope);
		if (returns != null) {
			String var = returns.literalValue();
			scope.addVarWithValue(var, result);
		}
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
