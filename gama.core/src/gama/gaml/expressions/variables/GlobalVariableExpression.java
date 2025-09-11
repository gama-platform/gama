/*******************************************************************************************************
 *
 * GlobalVariableExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.variables;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.ICollector;
import gama.gaml.compilation.GAML;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IVarDescriptionUser;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IVarExpression;
import gama.gaml.types.IType;

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
		final VariableDescription v = ((SpeciesDescription) world).getAttribute(n);
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
		VariableDescription vd = getDefinitionDescription().getSpeciesContext().getAttribute(name);
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
		if (scope.hasAccessToGlobalVar(name) && !this.getDefinitionDescription().getModelDescription().isMicroModel()) return scope.getGlobalVarValue(name);
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
			VariableDescription vd = desc.getSpeciesContext().getAttribute(getName());
			isParameter = vd != null && vd.isParameter();
		} else {
			isParameter = false;
		}
		return "global " + (isParameter ? "parameter" : isNotModifiable ? "constant" : "attribute") + " " + getName()
				+ " of type " + getGamlType().getName();
	}

	@Override
	public Doc getDocumentation() {
		final IDescription desc = getDefinitionDescription();
		if (desc == null) return new ConstantDoc("Type " + type.getTitle());
		Doc doc = new RegularDoc(new StringBuilder());
		final VariableDescription var = desc.getSpeciesContext().getAttribute(name);
		doc.append("Type ").append(type.getTitle()).append("<br/>");
		String builtInDoc = null;
		if (var != null) { builtInDoc = var.getBuiltInDoc(); }
		if (builtInDoc != null) { doc.append(builtInDoc).append("<br/>"); }
		doc.append(desc.isBuiltIn() ? "Built in " : builtInDoc == null ? "Defined in " : "Redefined in ")
				.append(desc.getTitle());
		return doc;
	}

	@Override
	public void collectUsedVarsOf(final SpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<VariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		final SpeciesDescription sd = this.getDefinitionDescription().getSpeciesContext();
		if (species.equals(sd)) { result.add(sd.getAttribute(getName())); }
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
