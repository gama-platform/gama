/*******************************************************************************************************
 *
 * GlobalVariableExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.expressions.IVarExpression.Agent;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.collections.ICollector;
import gama.api.utils.prefs.GamaPreferences;

/**
 * The Class GlobalVariableExpression.
 */
public class GlobalVariableExpression extends VariableExpression implements IVarExpression.Agent {

	/**
	 * Creates the.
	 *
	 * @param n
	 *            the n
	 * @param type
	 *            the type
	 * @param notModifiable
	 *            the not modifiable
	 * @param world
	 *            the world
	 * @return the i expression
	 */
	public static IExpression create(final String n, final IType<?> type, final boolean notModifiable,
			final IDescription world) {
		final IVariableDescription v = ((ISpeciesDescription) world).getAttribute(n);
		final IExpression exp = v.getFacetExpr(IKeyword.INIT);
		if (exp != null && notModifiable && !v.isFunction()) {
			// AD Addition of a test on whether the variable is a function or not
			if (GamaPreferences.Experimental.CONSTANT_OPTIMIZATION.getValue() && exp.isConst())
				return GAML.getExpressionFactory().createConst(exp.getConstValue(), type, n);
		}
		return new GlobalVariableExpression(n, type, notModifiable, world);
	}

	/**
	 * Instantiates a new global variable expression.
	 *
	 * @param n
	 *            the n
	 * @param type
	 *            the type
	 * @param notModifiable
	 *            the not modifiable
	 * @param world
	 *            the world
	 */
	protected GlobalVariableExpression(final String n, final IType<?> type, final boolean notModifiable,
			final IDescription world) {
		super(n, type, notModifiable, world);
	}

	@Override
	public boolean isConst() {
		// Allow global variables to report that they are constant if they are noted so (except if they are containers).
		if (type.isContainer()) return false;
		IVariableDescription vd = getDefinitionDescription().getSpeciesContext().getAttribute(name);
		if (vd == null || vd.isFunction()) return false;
		return isNotModifiable;
	}

	@Override
	public IExpression getOwner() {
		return this.getDefinitionDescription().getModelDescription().getVarExpr(IKeyword.WORLD_AGENT_NAME, false);
	}

	@Override
	public Object _value(final IScope scope) throws GamaRuntimeException {
		final String name = getName();
		// We first try in the 'normal' scope (so that regular global vars are still accessed by agents of micro-models,
		// see #2238)
		if (scope.hasAccessToGlobalVar(name) && !this.getDefinitionDescription().getModelDescription().isMicroModel())
			return scope.getGlobalVarValue(name);
		final IAgent microAgent = scope.getAgent();
		if (microAgent != null) {
			final IScope agentScope = microAgent.getScope();
			if (agentScope != null) {
				final ITopLevelAgent root = agentScope.getRoot();
				if (root != null) {
					final IScope globalScope = root.getScope();
					if (globalScope != null) return globalScope.getGlobalVarValue(getName());
				}
			}
		}

		return null;
	}

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {
		if (isNotModifiable) return;
		if (scope.hasAccessToGlobalVar(name)) {
			scope.setGlobalVarValue(name, v);
		} else {
			final IAgent sc = scope.getAgent();
			if (sc != null) { sc.getScope().getRoot().getScope().setGlobalVarValue(name, v); }
		}
	}

	@Override
	public String getTitle() {
		final IDescription desc = getDefinitionDescription();
		boolean isParameter;
		if (desc != null) {
			IVariableDescription vd = desc.getSpeciesContext().getAttribute(getName());
			isParameter = vd != null && vd.isParameter();
		} else {
			isParameter = false;
		}
		return "global " + (isParameter ? "parameter" : isNotModifiable ? "constant" : "attribute") + " " + getName()
				+ " of type " + getGamlType().getName();
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		final IDescription desc = getDefinitionDescription();
		if (desc == null) return new GamlConstantDocumentation("Type " + type.getTitle());
		IGamlDocumentation doc = new GamlRegularDocumentation(new StringBuilder());
		final IVariableDescription var = desc.getSpeciesContext().getAttribute(name);
		doc.append("Type ").append(type.getTitle()).append("<br/>");
		String builtInDoc = null;
		if (var != null) { builtInDoc = var.getBuiltInDoc(); }
		if (builtInDoc != null) { doc.append(builtInDoc).append("<br/>"); }
		doc.append(desc.isBuiltIn() ? "Built in " : builtInDoc == null ? "Defined in " : "Redefined in ")
				.append(desc.getTitle());
		return doc;
	}

	@Override
	public void collectUsedVarsOf(final ISpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		final ISpeciesDescription sd = this.getDefinitionDescription().getSpeciesContext();
		if (species.equals(sd)) { result.add(sd.getAttribute(getName())); }
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
