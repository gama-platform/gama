/*******************************************************************************************************
 *
 * GamaAgentType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * The type used to represent an agent of a species. Should be used by the species for all the operations relative to
 * casting, etc.
 *
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
public class GamaAgentType extends GamaType<IAgent> {

	/** The species. */
	ISpeciesDescription species;

	/**
	 * Instantiates a new gama agent type.
	 *
	 * @param species
	 *            the species
	 * @param name
	 *            the name
	 * @param speciesId
	 *            the species id
	 * @param base
	 *            the base
	 */
	public GamaAgentType(final ISpeciesDescription species, final String name, final int speciesId,
			final Class<IAgent> base) {
		this.species = species;
		this.name = name;
		id = speciesId;
		support = base;
		// supports = new Class[] { base };
		if (species != null) { setDefiningPlugin(species.getDefiningPlugin()); }
	}

	@Override
	public boolean isAssignableFrom(final IType<?> t) {
		final boolean assignable = super.isAssignableFrom(t);
		// Hack to circumvent issue #1999. Should be better handled by
		// letting type managers of comodels inherit from the type managers
		// of imported models.
		if (!assignable && t.isAgentType() && t.getSpecies() == getSpecies()) return true;
		return assignable;
	}

	@Override
	public String getDefiningPlugin() { return species.getDefiningPlugin(); }

	@Override
	public IAgent cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null || scope == null || scope.getModel() == null) return null;
		ISpecies species = (ISpecies) param;
		if (species == null) { species = scope.getModel().getSpecies(this.species.getName()); }
		if (species == null) return (IAgent) Types.AGENT.cast(scope, obj, param, copy);
		if (obj instanceof IAgent) return ((IAgent) obj).isInstanceOf(species, false) ? (IAgent) obj : null;
		if (obj instanceof Integer) return scope.getAgent().getPopulationFor(species).getAgent((Integer) obj);
		if (obj instanceof IPoint) {
			IAgent agent = scope.getAgent();
			if (agent != null) return agent.getPopulationFor(species).getAgent(scope, (IPoint) obj);
		}
		return null;
	}

	@Override
	public IAgent getDefault() { return null; }

	@Override
	public boolean isAgentType() { return true; }

	@Override
	public String getSpeciesName() { return name; }

	@Override
	public ISpeciesDescription getSpecies() { return species; }

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public boolean canBeTypeOf(final IScope scope, final Object obj) {
		final boolean b = super.canBeTypeOf(scope, obj);
		if (b) return true;
		if (obj instanceof IAgent) {
			final ISpecies s = scope.getModel().getSpecies(getSpeciesName());
			return ((IAgent) obj).isInstanceOf(s, false);
		}
		return false;
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		IGamlDocumentation result =
				new GamlRegularDocumentation("Represents instances of species " + species.getName());
		species.documentAttributes(result);
		return result;
	}

	@Override
	public IType<String> getKeyType() { return Types.STRING; }

	@Override
	public boolean isFixedLength() { return false; }

	@Override
	public boolean isDrawable() { return true; }

}
