/*******************************************************************************************************
 *
 * AgentVariableExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.variables;

import gama.annotations.precompiler.GamlProperties;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.ICollector;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IVarDescriptionUser;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IVarExpression;
import gama.gaml.types.IType;

/**
 * The Class AgentVariableExpression.
 */
public class AgentVariableExpression extends VariableExpression implements IVarExpression.Agent {

	/**
	 * Instantiates a new agent variable expression.
	 *
	 * @param n
	 *            the n
	 * @param type
	 *            the type
	 * @param notModifiable
	 *            the not modifiable
	 * @param def
	 *            the def
	 */
	@SuppressWarnings ("rawtypes")
	public AgentVariableExpression(final String n, final IType type, final boolean notModifiable,
			final IDescription def) {
		super(n, type, notModifiable, def);
	}

	@Override
	public IExpression getOwner() {
		return new SelfExpression(this.getDefinitionDescription().getSpeciesContext().getGamlType());
	}

	@Override
	public Object _value(final IScope scope) throws GamaRuntimeException {
		return scope.getCurrentAgentOrObjectAttributeValue(getName());
	}

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {
		scope.setCurrentAgentOrObjectAttributeValue(getName(), v);
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

	/**
	 * Method collectPlugins()
	 *
	 * @see gama.gaml.interfaces.IGamlDescription#collectPlugins(java.util.Set)
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		if (getDefinitionDescription().isBuiltIn()) { meta.put(GamlProperties.ATTRIBUTES, getName()); }
	}

	@Override
	public void collectUsedVarsOf(final SpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<VariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		final SpeciesDescription sd = this.getDefinitionDescription().getSpeciesContext();
		if (species.equals(sd) || species.hasParent(sd)) { result.add(sd.getAttribute(getName())); }
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
