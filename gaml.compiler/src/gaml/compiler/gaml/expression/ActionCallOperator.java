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
package gaml.compiler.gaml.expression;

import static gama.api.exceptions.GamaRuntimeException.error;
import static gama.api.exceptions.GamaRuntimeException.warning;

import gama.api.GAMA;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.api.utils.collections.ICollector;

/**
 * ActionCallOperator. An operator that wraps a primitive or an action.
 *
 * @author drogoul 4 sept. 07
 */

public class ActionCallOperator implements IOperator {

	/** The parameters. */
	final Arguments parameters;

	/** The target. */
	final IExpression target;

	/** The action. */
	final IActionDescription action;

	/** The target species. */
	final String targetSpeciesName;

	/** The target species. */
	IClass targetSpecies;

	/** The computed. */
	boolean computed;

	/**
	 * Instantiates a new action call operator.
	 *
	 * @param callerContext
	 *            the caller context
	 * @param action
	 *            the action
	 * @param target
	 *            the target
	 * @param args
	 *            the args
	 * @param superInvocation
	 *            the super invocation
	 */
	public ActionCallOperator(final IDescription callerContext, final IActionDescription action,
			final IExpression target, final Arguments args, final boolean superInvocation) {
		this.target = target;
		ITypeDescription type = target == null ? callerContext.getTypeContext() : target.getGamlType().getSpecies();
		// DEBUG.LOG("action in " + action.getEnclosingDescription() + " - type = " + type);
		this.targetSpeciesName = superInvocation ? type.getParent().getName() : type.getName();
		this.action = action;
		parameters = args;
	}

	/**
	 * Instantiates a new primitive operator.
	 *
	 * @param action
	 *            the action.
	 * @param target
	 *            the target.
	 * @param args
	 *            the args
	 * @param targetSpeciesName
	 *            the target species.
	 */
	public ActionCallOperator(final IActionDescription action, final IExpression target, final Arguments args,
			final String targetSpecies) {
		this.target = target;
		this.targetSpeciesName = targetSpecies;
		this.action = action;
		parameters = args;
	}

	@Override
	public String getName() { return action.getName(); }

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		if (scope == null) return null;
		IObject target;
		if (this.target == null) {
			target = scope.getCurrentObjectOrAgent();
		} else {
			Object val = this.target.value(scope);
			if (!(val instanceof IObject oo)) {
				GAMA.reportError(scope, error("Invalid target : " + val, scope), true);
				return null;
			}
			target = oo;
		}
		if (target == null) {
			GAMA.reportError(scope, error("No agent is available to execute operator " + getName(), scope), false);
			return null;
		}
		final IClass species = getContext(scope);
		if (species == null) {
			GAMA.reportError(scope, error("No species/class is available to execute operator " + getName(), scope),
					false);
			return null;
		}
		final IStatement.WithArgs executer = species.getAction(getName());
		if (executer != null) {
			boolean useTargetScopeForExecution =
					scope.getRoot() instanceof IExperimentAgent && target instanceof ISimulationAgent;
			//
			return scope.execute(executer, target, useTargetScopeForExecution, getRuntimeArgs(scope)).getValue();
		}
		// the executer is not available. Can happen in rare cases (like the one evoked in Issue #3493).
		GAMA.reportError(scope,
				warning("The operator " + getName() + " is not available in the context of " + scope.getAgent(), scope),
				false);
		return null;
	}

	/**
	 * @return
	 */
	private IClass getContext(final IScope scope) {
		if (computed) return targetSpecies;
		computed = true;
		if (targetSpeciesName != null) {
			IModelSpecies model = scope.getModel();
			targetSpecies = model.getClass(targetSpeciesName);
			if (targetSpecies == null) { targetSpecies = model.getSpecies(targetSpeciesName); }
		} else {
			targetSpecies = scope.getCurrentObjectOrAgent().getSpecies();
		}
		return targetSpecies;

	}

	/**
	 * Gets the runtime args.
	 *
	 * @param scope
	 *            the scope
	 * @return the runtime args
	 */
	public Arguments getRuntimeArgs(final IScope scope) {
		if (parameters == null) return null;
		// Dynamic arguments necessary (see #2943, #2922, plus issue with multiple parallel simulations)
		// Copy-paste of DoStatement. Verify that this copy is necessary here.
		return parameters.resolveAgainst(scope);
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public String getTitle() {
		final StringBuilder sb = new StringBuilder(50);
		sb.append("action ").append(getName()).append(" defined in species ")
				.append(target.getGamlType().getSpeciesName()).append(" returns ").append(getGamlType().getName());
		return sb.toString();

	}

	@Override
	public IGamlDocumentation getDocumentation() { return action.getDocumentation(); }

	@Override
	public String getDefiningPlugin() { return action.getDefiningPlugin(); }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		if (target != null) {
			AbstractExpression.parenthesize(sb, target);
			sb.append(".");
		}
		sb.append(literalValue()).append("(");
		argsToGaml(sb, includingBuiltIn);
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Args to gaml.
	 *
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	protected String argsToGaml(final StringBuilder sb, final boolean includingBuiltIn) {
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
				parameters == null ? null : parameters.resolveAgainst(scope), targetSpeciesName);
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

	// TODO The arguments are not ordered...
	@Override
	public IExpression arg(final int i) {
		if (i < 0 || i > parameters.size()) return null;
		return parameters.getExpr(i);
		// return Iterables.get(parameters.values(), i).getExpression();
	}

	@Override
	public IArtefact getPrototype() { return null; }

}
