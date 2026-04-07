/*******************************************************************************************************
 *
 * SpeciesConstantExpression.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
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
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;
import gama.api.utils.collections.ICollector;
import gama.dev.DEBUG;

/**
 * The Class SpeciesConstantExpression.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 16 janv. 2024
 */
@SuppressWarnings ({ "rawtypes" })
public class SpeciesConstantExpression extends ConstantExpression implements IExpression.Species {

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
	public SpeciesConstantExpression(final String string, final IType t, final IDescription context) {
		super(string, t);

		origin = context.getModelDescription().getName();
		alias = context.getModelDescription().getMicroAlias();
		belongsToAMicroModel = alias != null && !alias.isEmpty();
		// DEBUG.OUT("Creation of species constant expression " + string + " in context of " + origin + " with alias "
		// + alias);
	}

	@Override
	public Object _value(final IScope scope) {
		final IAgent a = scope.getAgent();
		if (a != null) {
			if (!belongsToAMicroModel) {
				final IPopulation pop = a.getPopulationFor((String) value);
				if (pop != null) return pop.getSpecies();
				return scope.getModel().getSpecies((String) value);
			}
			final IPopulation pop = scope.getRoot().getExternMicroPopulationFor(alias + "." + value);
			if (pop != null) return pop.getSpecies();
			return scope.getModel().getSpecies((String) value, origin);
		}
		return null;
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return (String) value;
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		IGamlDocumentation result = new GamlRegularDocumentation("");
		getGamlType().getContentType().getSpecies().documentThis(result);
		return result;
	}

	/**
	 * Method collectPlugins()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#collectPlugins(java.util.Set)
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		final ITypeDescription sd = getGamlType().getContentType().getSpecies();
		if (sd != null) {
			meta.put(GamlProperties.PLUGINS, sd.getDefiningPlugin());
			if (sd.isBuiltIn()) { meta.put(GamlProperties.SPECIES, (String) value); }
		}
	}

	@Override
	public boolean isContextIndependant() { return false; }

	@Override
	public boolean isAllowedInParameters() { return true; } // verify this

	@Override
	public void collectUsedVarsOf(final ITypeDescription species,
			final ICollector<IVarDescriptionUser> alreadyProcessed, final ICollector<IVariableDescription> result) {
		if (alreadyProcessed.contains(this)) return;
		alreadyProcessed.add(this);
		if (species.hasAttribute(value.toString())) { result.add(species.getAttribute(value.toString())); }
	}

}
