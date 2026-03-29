/*******************************************************************************************************
 *
 * ActionCallOperator.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import static gama.api.exceptions.GamaRuntimeException.error;
import static gama.api.exceptions.GamaRuntimeException.warning;

import gama.api.GAMA;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.statements.IStatement.WithArgs;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.api.utils.collections.ICollector;

/**
 * ActionCallOperator – an operator expression that wraps a call to a GAML action or primitive.
 *
 * <p>
 * Instances are created during compilation and reused across simulation steps and threads, so all mutable state must be
 * thread-safe.
 * </p>
 *
 * @author drogoul 4 sept. 07
 */
public class ActionCallOperator implements IOperator {

	/** The arguments passed to the action call. May be {@code null} when the action takes no arguments. */
	final Arguments parameters;

	/**
	 * The target expression evaluating to the agent on which the action is called. {@code null} means the current agent
	 * ({@code self}).
	 */
	final IExpression target;

	/** The compile-time description of the action being called. Never {@code null}. */
	final IActionDescription action;

	/** The is super invocation. */
	final boolean isSuperInvocation;

	/**
	 * Creates an {@link ActionCallOperator} for an action called from within a given compilation context (used for
	 * {@code self.action()} and {@code super.action()} calls as well as standalone {@code do} statements).
	 *
	 * @param callerContext
	 *            the compile-time description that contains the call; used to determine the species type when
	 *            {@code target} is {@code null}
	 * @param action
	 *            the compile-time description of the action being called; must not be {@code null}
	 * @param target
	 *            expression evaluating to the receiver agent at runtime; {@code null} means "current agent"
	 * @param args
	 *            compiled arguments; may be {@code null} when the action accepts no arguments
	 * @param superInvocation
	 *            {@code true} when this is an {@code invoke} / {@code super.action()} call so the parent species is
	 *            used as the lookup class
	 */
	public ActionCallOperator(final IActionDescription action, final IExpression target, final Arguments args,
			final boolean superInvocation) {
		this.target = target;
		this.isSuperInvocation = superInvocation;
		this.action = action;
		parameters = args;
	}

	@Override
	public String getName() { return action.getName(); }

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		if (scope == null) return null;

		IObject object = getTarget(scope);
		if (object == null) return null;

		// When a target is present, resolve the action from the actual runtime species/class of the target
		// object so that an overriding action defined in a child species or subclass is correctly dispatched
		// even when the variable holding the object was declared with a parent type (polymorphism).

		IClass context = getClass(scope, object);
		if (context == null) return null;
		IStatement.WithArgs executer = getExecuter(scope, context);
		if (executer == null) return null;

		boolean useTargetScope = scope.getRoot() instanceof IExperimentAgent && object instanceof ISimulationAgent;
		return scope.execute(executer, object, useTargetScope, getRuntimeArgs(scope)).getValue();

	}

	/**
	 * @param scope
	 * @param context
	 * @return
	 */
	private WithArgs getExecuter(final IScope scope, final IClass context) {
		WithArgs executer = context.getAction(getName());
		// the executer is not available. Can happen in rare cases (like the one in Issue #3493).
		if (executer == null) {
			GAMA.reportError(scope,
					warning(getName() + " is not available in the context of " + scope.getAgent(), scope), false);
		}
		return executer;
	}

	/**
	 * @param scope
	 * @param object
	 * @return
	 */
	private IClass getClass(final IScope scope, final IObject object) {
		IClass context = object.getSpecies();
		if (context == null) {
			GAMA.reportError(scope, error("No species/class available to execute " + getName(), scope), false);
			return null;
		}
		if (isSuperInvocation) { context = context.getParent(); }
		return context;
	}

	/**
	 * @param scope
	 * @return
	 */
	private IObject getTarget(final IScope scope) {
		Object val = target.value(scope);
		if (val == null) {
			GAMA.reportError(scope, error("Agent or object is nil.", scope), false);
			return null;
		}
		if (!(val instanceof IObject object)) {
			GAMA.reportError(scope, error("Agent or object is invalid.", scope), true);
			return null;
		}
		return object;
	}

	/**
	 * Returns a scope-resolved copy of the arguments for this call.
	 *
	 * <p>
	 * A fresh copy is produced on every call because argument expressions may contain dynamic references (e.g. local
	 * variables) that must be re-evaluated against the current scope. See issues #2943 and #2922, as well as the
	 * multiple-parallel-simulations case.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @return resolved {@link Arguments}, or {@code null} when this action accepts no arguments
	 */
	public Arguments getRuntimeArgs(final IScope scope) {
		if (parameters == null) return null;
		return parameters.resolveAgainst(scope);
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public String getTitle() {
		final StringBuilder sb = new StringBuilder(50);
		sb.append("action ").append(getName()).append(" defined in species ");
		sb.append(target.getGamlType().getSpeciesName());
		sb.append(" returns ").append(getGamlType().getName());
		return sb.toString();
	}

	@Override
	public IGamlDocumentation getDocumentation() { return action.getDocumentation(); }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		AbstractExpression.parenthesize(sb, target);
		sb.append(".");
		sb.append(literalValue()).append("(");
		argsToGaml(sb, includingBuiltIn);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Serialises all arguments as a comma-separated string of {@code name:value} pairs (or bare values for positional
	 * arguments) and appends the result to {@code sb}.
	 *
	 * <p>
	 * The method is {@code public} so that {@link gama.gaml.statements.DoStatement.DoSerializer} can delegate to it
	 * when serialising a {@code do} statement without the enclosing target prefix.
	 * </p>
	 *
	 * @param sb
	 *            the buffer to append to
	 * @param includingBuiltIn
	 *            whether to include built-in symbol names in the serialised output
	 * @return the same {@code sb} for chaining (empty string when there are no parameters)
	 */
	public String argsToGaml(final StringBuilder sb, final boolean includingBuiltIn) {
		if (parameters == null || parameters.isEmpty()) return "";
		parameters.forEachFacet((name, expr) -> {
			if (StringUtils.isGamaNumber(name)) {
				sb.append(expr.serializeToGaml(false));
			} else {
				sb.append(name).append(":").append(expr.serializeToGaml(includingBuiltIn));
			}
			sb.append(", ");
			return true;
		});
		if (sb.length() > 0) { sb.setLength(sb.length() - 2); }
		return sb.toString();
	}

	/**
	 * Collect used vars of.
	 *
	 * @param species
	 *            the species
	 * @param alreadyProcessed
	 *            the already processed
	 * @param result
	 *            the result
	 */
	@Override
	public void collectUsedVarsOf(final ITypeDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		if (parameters != null) {
			parameters.forEachFacet((name, exp) -> {
				final IExpression expression = exp.getExpression();
				if (expression != null) { expression.collectUsedVarsOf(species, alreadyProcessed, result); }
				return true;

			});
		}
		// See https://github.com/COMOKIT/COMOKIT-Model/issues/21 . An action used in the initialization section may not
		// be correctly analyzed for dependencies.
		action.collectUsedVarsOf(species, alreadyProcessed, result);
	}

	@Override
	public void setName(final String newName) {}

	@Override
	public IType<?> getGamlType() { return action.getGamlType(); }

	@Override
	public void dispose() {
		if (parameters != null) { parameters.dispose(); }
	}

	@Override
	public String literalValue() {
		return action.getName();
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return new ActionCallOperator(action, target == null ? null : target.resolveAgainst(scope),
				parameters == null ? null : parameters.resolveAgainst(scope), isSuperInvocation);
	}

	@Override
	public boolean shouldBeParenthesized() {
		return true;
	}

	@Override
	public void visitSuboperators(final IOperatorVisitor visitor) {
		if (parameters != null) {
			parameters.forEachFacet((name, exp) -> {
				final IExpression expr = exp.getExpression();
				if (expr instanceof IOperator) { visitor.visit((IOperator) expr); }
				return true;
			});
		}

	}

	@Override
	public IExpression arg(final int i) {
		if (i < 0 || i > parameters.size()) return null;
		return parameters.getExpr(i);
	}

	@Override
	public IArtefact getPrototype() { return null; }

}
