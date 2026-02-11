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

import gama.annotations.support.ISymbolKind;
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

/**
 * The Class GamaAgentType.
 */

/**
 * The Class GamaAgentType.
 */
public class GamaAgentType<T extends IAgent> extends GamaType<T> {

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
	@SuppressWarnings ("unchecked")
	public GamaAgentType(final ITypesManager typesManager, final ISpeciesDescription species, final int id) {
		this(typesManager, species, species.getName(), (Class<T>) species.getJavaBase(), id);
	}

	/**
	 * Instantiates a new gama agent type.
	 *
	 * @param typesManager
	 *            the types manager
	 * @param species
	 *            the species.
	 * @param name
	 *            the name
	 * @param support
	 *            the support
	 * @param id
	 *            the id
	 */
	public GamaAgentType(final ITypesManager typesManager, final ISpeciesDescription species, final String name,
			final Class<T> support, final int id) {
		super(typesManager);
		this.species = species;
		this.id = id;
		this.name = name;
		this.varKind = ISymbolKind.Variable.REGULAR;
		this.support = support;
	}

	@Override
	protected void init() {}

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
	public String getDefiningPlugin() { return getSpecies().getDefiningPlugin(); }

	@SuppressWarnings ("unchecked")
	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null || scope == null || scope.getModel() == null) return null;
		final ISpecies species = param instanceof ISpecies sp ? sp : scope.getModel().getSpecies(getName());
		if (species == null) return (T) Types.AGENT.cast(scope, obj, param, copy);
		if (obj instanceof IAgent ia) return ia.isInstanceOf(species, false) ? (T) ia : null;
		final IAgent agent = scope.getAgent();
		if (agent != null) {
			if (obj instanceof Integer i) return (T) agent.getPopulationFor(species).getAgent(i);
			if (obj instanceof IPoint p) return (T) agent.getPopulationFor(species).getAgent(scope, p);
		}
		return null;
	}

	@Override
	public T getDefault() { return null; }

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
				new GamlRegularDocumentation("Represents instances of species " + getSpecies().getName());
		getSpecies().documentAttributes(result);
		return result;
	}

	@Override
	public IType<String> getKeyType() { return Types.STRING; }

	@Override
	public boolean isFixedLength() { return false; }

	@Override
	public boolean isDrawable() { return true; }

}
