/*******************************************************************************************************
 *
 * GamaObjectType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IObject;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.ClassDescription;
import gama.gaml.species.IClass;
import gama.gaml.species.ISpecies;

/**
 * The type used to represent an object of a class. Should be used by the class for all the operations relative to
 * casting, etc.
 *
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
public class GamaObjectType extends GamaType<IObject> {

	/** The species. */
	ClassDescription species;

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
	public GamaObjectType(final ClassDescription species, final String name, final int speciesId,
			final Class<IObject> base) {
		this.species = species;
		this.name = name;
		id = speciesId;
		support = base;
		// supports = new Class[] { base };
		if (species != null) { setDefiningPlugin(species.getDefiningPlugin()); }
	}

	// @Override
	// public boolean isAssignableFrom(final IType<?> t) {
	// final boolean assignable = super.isAssignableFrom(t);
	// // Hack to circumvent issue #1999. Should be better handled by
	// // letting type managers of comodels inherit from the type managers
	// // of imported models.
	// if (!assignable && t.isAgentType() && t.getSpecies() == getSpecies()) return true;
	// return assignable;
	// }

	@Override
	public String getDefiningPlugin() { return species.getDefiningPlugin(); }

	@Override
	public IObject cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null || scope == null || scope.getModel() == null) return null;
		IClass species = (IClass) param;
		if (species == null) { species = scope.getModel().getClass(this.species.getName()); }
		if (species == null) return Types.OBJECT.cast(scope, obj, param, copy);
		if (obj instanceof IObject result) return result.isInstanceOf(species, false) ? result : null;
		return null;
	}

	@Override
	public IAgent getDefault() { return null; }

	@Override
	public boolean isObjectType() { return true; }

	@Override
	public String getSpeciesName() { return name; }

	@Override
	public ClassDescription getSpecies() { return species; }

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
	public Doc getDocumentation() {
		Doc result = new RegularDoc("Represents instances of species " + species.getName());
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
