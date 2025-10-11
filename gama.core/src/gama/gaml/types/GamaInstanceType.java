/*******************************************************************************************************
 *
 * GamaInstanceType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import gama.core.metamodel.agent.IObject;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.species.IClass;

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
public abstract class GamaInstanceType<T extends IObject> extends GamaType<T> {

	/** The species. */
	TypeDescription species;

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
	public GamaInstanceType(final TypeDescription species, final String name, final int speciesId,
			final Class<T> base) {
		this.species = species;
		this.name = name;
		id = speciesId;
		support = base;
		if (species != null) { setDefiningPlugin(species.getDefiningPlugin()); }
	}

	@Override
	public abstract T cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException;

	@Override
	public T getDefault() { return null; }

	@Override
	public abstract boolean isObjectType();

	@Override
	public String getSpeciesName() { return name; }

	@Override
	public TypeDescription getSpecies() { return species; }

	@Override
	public boolean canCastToConst() {
		return false;
	}

	@Override
	public boolean canBeTypeOf(final IScope scope, final Object obj) {
		if (super.canBeTypeOf(scope, obj)) return true;
		if (obj instanceof IObject io) {
			final IClass s = scope.getModel().getClassOrSpecies(getSpeciesName());
			return io.isInstanceOf(s, false);
		}
		return false;
	}

	@Override
	public Doc getDocumentation() {
		Doc result = new RegularDoc("Represents instances of " + species.getName());
		species.documentAttributes(result);
		return result;
	}

	@Override
	public IType<String> getKeyType() { return Types.STRING; }

	@Override
	public boolean isFixedLength() { return false; }

	@Override
	public abstract boolean isDrawable();

}
