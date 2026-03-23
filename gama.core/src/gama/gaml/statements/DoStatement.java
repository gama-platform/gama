/*******************************************************************************************************
 *
 * DoStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import java.util.concurrent.atomic.AtomicReference;

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
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.annotations.serializer;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.kernel.object.IClass;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.gaml.statements.DoStatement.DoSerializer;

/**
 * Implements the {@code do} / {@code invoke} / {@code .} statement which lets an agent execute an action or
 * primitive at runtime.
 *
 * <p><b>Thread-safety:</b> this class is safe for concurrent use by multiple threads (e.g. parallel
 * simulations). The two pieces of shared mutable state are protected as follows:
 * <ul>
 *   <li>{@link #args} – held in an {@link java.util.concurrent.atomic.AtomicReference} so that
 *       {@link #setFormalArgs(Arguments)} and {@link #getRuntimeArgs(IScope)} never observe a
 *       partially-written reference.</li>
 *   <li>{@link #targetSpecies} – also held in an {@link java.util.concurrent.atomic.AtomicReference}
 *       and lazily initialised via {@link java.util.concurrent.atomic.AtomicReference#compareAndSet}
 *       inside {@link #getContext(IScope)}, ensuring that the resolution is performed exactly once even
 *       when multiple threads race to initialise the field.</li>
 * </ul>
 * </p>
 *
 * <p>Originally written by drogoul, modified on 7 févr. 2010.</p>
 */
@symbol (
		name = { IKeyword.DO, IKeyword.INVOKE, IKeyword._DOT },
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
				internal = true,
				doc = @doc (
						value = "a map expression containing the parameters of the action",
						deprecated = "Use the functional (with comma separated values inside parentheses, optionally prefixed by the argument name) form to pass the arguments")),
				@facet (
						name = IKeyword.RETURNS,
						type = IType.NEW_TEMP_ID,
						optional = true,
						doc = @doc (
								value = "Specifies the name of the temporary variable that will contain the result.",
								deprecated = "Use the functional form to assign the result to a variable")),
				@facet (
						name = IKeyword.ACTION,
						type = IType.ID,
						optional = false,
						doc = @doc ("the name of the action or primitive called")),
				@facet (
						name = IKeyword.SYNTHETIC_DO_TARGET,
						type = IType.NONE,
						optional = true,
						internal = true,
						doc = @doc ("the target agent for the action. Internal use only.")),
				@facet (
						name = IKeyword.INTERNAL_FUNCTION,
						type = IType.NONE,
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
			if (StringUtils.isGamaNumber(name)) {
				sb.append(value.serializeToGaml(includingBuiltIn));
			} else {
				sb.append(name).append(":").append(value.serializeToGaml(includingBuiltIn));
			}

		}

		@Override
		public String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
			if (!ArtefactRegistry.DO_FACETS.contains(key)) return null;
			return super.serializeFacetValue(s, key, includingBuiltIn);
		}

	}

	/**
	 * The args. Uses {@link AtomicReference} so that {@link #setFormalArgs(Arguments)} and
	 * {@link #getRuntimeArgs(IScope)} are safe when called from multiple threads simultaneously.
	 */
	final AtomicReference<Arguments> args = new AtomicReference<>();

	/** The target species name. Immutable after construction. */
	final String targetSpeciesName;

	/**
	 * The resolved target species. Lazily initialised via double-checked locking inside
	 * {@link #getContext(IScope)} so that exactly one initialisation happens even under concurrent
	 * access.
	 */
	final AtomicReference<IClass> targetSpecies = new AtomicReference<>();

	/** The function. */
	final IExpression function, returns;

	/**
	 * Instantiates a new do statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public DoStatement(final IDescription desc) {
		super(desc);
		targetSpeciesName = getLiteral(IKeyword.SYNTHETIC_DO_TARGET_SPECIES);
		function = getFacet(IKeyword.INTERNAL_FUNCTION);
		returns = getFacet(IKeyword.RETURNS);
		setName(getLiteral(IKeyword.ACTION));
	}

	@Override
	public void enterScope(final IScope scope) {
		super.enterScope(scope);
	}

	@Override
	public void setFormalArgs(final Arguments args) { this.args.set(args); }

	/**
	 * Gets the runtime args.
	 *
	 * @param scope
	 *            the scope
	 * @return the runtime args
	 */
	public Arguments getRuntimeArgs(final IScope scope) {
		final Arguments currentArgs = args.get();
		if (currentArgs == null) return null;
		// Dynamic arguments necessary (see #2943, #2922, plus issue with multiple parallel simulations)
		return currentArgs.resolveAgainst(scope);
	}

	/**
	 * Returns the species on which to find the action. If a species exists, we find the corresponding action.
	 * Otherwise, we return the species of the agent.
	 * <p>
	 * Thread-safe: uses {@link AtomicReference#compareAndSet} so that exactly one thread performs the
	 * initialisation even when multiple threads call this method simultaneously on the same instance.
	 * </p>
	 */
	private IClass getContext(final IScope scope) {
		IClass current = targetSpecies.get();
		if (current != null) return current;
		IClass resolved;
		if (targetSpeciesName != null) {
			IModelSpecies model = scope.getModel();
			resolved = model.getClass(targetSpeciesName);
			if (resolved == null) { resolved = model.getSpecies(targetSpeciesName); }
		} else {
			resolved = scope.getAgent().getSpecies();
		}
		// Only store if not already set by another thread; either way, return the winner's value.
		targetSpecies.compareAndSet(null, resolved);
		return targetSpecies.get();
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		final IClass species = getContext(scope);
		if (species == null)
			throw GamaRuntimeException.error("Impossible to find a species or a class to execute " + getName(), scope);
		final IStatement.WithArgs executer = species.getAction(name);
		Object result = null;
		if (executer != null) {
			final IExecutionResult er = scope.execute(executer, getRuntimeArgs(scope));
			result = er.getValue();
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
		final Arguments currentArgs = args.getAndSet(null);
		if (currentArgs != null) { currentArgs.dispose(); }
		super.dispose();
	}

}
