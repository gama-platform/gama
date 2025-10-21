/*******************************************************************************************************
 *
 * ClassConstantExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.types;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.ICollector;
import gama.dev.DEBUG;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IVarDescriptionUser;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.types.IType;

/**
 * The Class SpeciesConstantExpression.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 16 janv. 2024
 */
@SuppressWarnings ({ "rawtypes" })
public class ClassConstantExpression extends TypeConstantExpression {

	static {
		DEBUG.OFF();
	}

	/**
	 * The origin name of the species. Can be in a remote micro-model. Equivalent to the originName present on
	 * SpeciesDescription
	 */
	final String origin;

	/** The alias. */
	final String alias;

	/** The belongs to A micro model. */
	final boolean belongsToAMicroModel;

	/**
	 * Instantiates a new species constant expression.
	 *
	 * @param string
	 *            the string
	 * @param t
	 *            the t
	 */
	public ClassConstantExpression(final String string, final IType t, final IDescription context) {
		super(string, t);
		origin = context.getModelDescription().getName();
		alias = context.getModelDescription().getAlias();
		belongsToAMicroModel = alias != null && !alias.isEmpty();
	}

	@Override
	public Object _value(final IScope scope) {

		final IAgent a = scope.getAgent();
		if (a != null) {
			if (!belongsToAMicroModel) return scope.getModel().getClass((String) value);
			return scope.getModel().getClass((String) value, origin);
		}
		return null;
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return (String) value;
	}

	@Override
	public Doc getDocumentation() {
		Doc result = new RegularDoc("");
		getGamlType().getContentType().getSpecies().documentThis(result);
		return result;
	}

	@Override
	public void collectUsedVarsOf(final SpeciesDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<VariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		if (species.hasAttribute(value.toString())) { result.add(species.getAttribute(value.toString())); }
	}

}
