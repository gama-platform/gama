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

import java.util.Collections;
import java.util.HashMap;

import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IOperator;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.IType;
import gama.api.kernel.object.IClass;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.api.utils.collections.ICollector;

/**
 * ActionCallOperator. An operator that wraps a primitive or an action.
 *
 * @author drogoul 4 sept. 07
 */

public record ConstructorForClass(IClassDescription target, Arguments parameters) implements IOperator {

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
	public ConstructorForClass(final IDescription callerContext, final IClassDescription target, final Arguments args) {
		this(target, args);
	}

	@Override
	public String getName() { return target.getName(); }

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		IClass clazz = scope.getModel().getClass(getName());
		if (clazz == null) return null;
		return clazz.createInstance(scope, getRuntimeArgs(scope));
	}

	/**
	 * Gets the runtime args.
	 *
	 * @param scope
	 *            the scope
	 * @return the runtime args
	 */
	public java.util.Map<String, Object> getRuntimeArgs(final IScope scope) {
		if (parameters == null) return Collections.emptyMap();
		java.util.Map<String, Object> args = new HashMap<>();
		parameters.forEachArgument((k, v) -> {
			args.put(k, v.getExpression().value(scope));
			return true;
		});
		return args;
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public String getTitle() {
		final StringBuilder sb = new StringBuilder(50);
		sb.append("constructor for class ").append(getName());
		return sb.toString();

	}

	@Override
	public IGamlDocumentation getDocumentation() { return target.getDocumentation(); }

	@Override
	public String getDefiningPlugin() { return target.getDefiningPlugin(); }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder(128).append(getName()).append("(");
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
		IVariableDescription vd = species.getAttribute(getName());
		if (vd != null) { result.add(vd); }
		if (parameters != null) {
			parameters.forEachFacet((name, exp) -> {
				final IExpression expression = exp.getExpression();
				if (expression != null) { expression.collectUsedVarsOf(species, alreadyProcessed, result); }
				return true;
			});
		}
	}

	@Override
	public void setName(final String newName) {}

	@Override
	public IType<?> getGamlType() { return target.getGamlType(); }

	@Override
	public void dispose() {
		if (parameters != null) { parameters.dispose(); }
	}

	@Override
	public String literalValue() {
		return target.getName();
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return new ConstructorForClass(target, parameters == null ? null : parameters.resolveAgainst(scope));
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public void visitSuboperators(final IOperatorVisitor visitor) {
		if (parameters != null) {
			parameters.forEachFacet((name, exp) -> {
				final IExpression expr = exp.getExpression();
				if (expr instanceof IOperator io) { visitor.visit(io); }
				return true;
			});
		}

	}

	// TODO The arguments are not ordered...
	@Override
	public IExpression arg(final int i) {
		if (i < 0 || i > parameters.size()) return null;
		return parameters.getExpr(i);
	}

	@Override
	public IArtefact getPrototype() { return null; }

}
