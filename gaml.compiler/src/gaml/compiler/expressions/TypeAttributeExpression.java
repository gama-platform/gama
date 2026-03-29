/*******************************************************************************************************
 *
 * TypeAttributeExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;
import gama.api.utils.collections.ICollector;

/**
 * The Class TypeAttributeExpression.
 */
public class TypeAttributeExpression extends VariableExpression implements IVarExpression.Agent {

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
	public TypeAttributeExpression(final String n, final IType type, final boolean notModifiable,
			final IDescription def) {
		super(n, type, notModifiable, def);
	}

	@Override
	public IExpression getOwner() {
		return new SelfExpression(this.getDefinitionDescription().getTypeContext().getGamlType());
	}

	@Override
	public Object _value(final IScope scope) throws GamaRuntimeException {
		return scope.getAgentVarValue(scope.getAgent(), getName());
	}

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {
		scope.setAgentVarValue(scope.getAgent(), getName(), v);
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		final IDescription desc = getDefinitionDescription();
		if (desc == null) return new GamlConstantDocumentation("Type " + type.getTitle());
		IGamlDocumentation doc = new GamlRegularDocumentation(new StringBuilder());
		final IVariableDescription var = desc.getTypeContext().getAttribute(name);
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
	 * @see gama.api.compilation.descriptions.IGamlDescription#collectPlugins(java.util.Set)
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		if (getDefinitionDescription().isBuiltIn()) { meta.put(GamlProperties.ATTRIBUTES, getName()); }
	}

	@Override
	public void collectUsedVarsOf(final ITypeDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		final ITypeDescription sd = this.getDefinitionDescription().getTypeContext();
		if (species.equals(sd) || species.hasParent(sd)) { result.add(sd.getAttribute(getName())); }
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
