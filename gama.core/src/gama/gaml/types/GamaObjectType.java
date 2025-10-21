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

import gama.core.metamodel.agent.GamlObject;
import gama.core.metamodel.agent.IObject;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IMap;
import gama.gaml.descriptions.ClassDescription;
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
public class GamaObjectType extends GamaInstanceType<IObject> {

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
		super(species, name, speciesId, base);
	}

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
	public boolean isObjectType() { return true; }

	/**
	 * Deserialize from json.
	 *
	 * @param scope
	 *            the scope
	 * @param str
	 *            the str
	 * @return the gaml object
	 */
	@Override
	public GamlObject deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		return new GamlObject(scope, scope.getModel().getClass(name), map2);
	}

	@Override
	public boolean isDrawable() { return false; }

}
