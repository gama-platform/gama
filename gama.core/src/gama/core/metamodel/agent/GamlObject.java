/*******************************************************************************************************
 *
 * GamlObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.agent;

import java.util.Collections;
import java.util.Map;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.gaml.species.IClass;

/**
 *
 */
public class GamlObject implements IObject<IClass> {

	/** The attributes. */
	Map<String, Object> attributes;

	/** The species. */
	final IClass species;

	/**
	 * Instantiates a new gaml object.
	 *
	 * @param species
	 *            the species.
	 */
	public GamlObject(final IClass species, final Map<String, Object> attributes) {
		this.species = species;
		if (attributes != null && !attributes.isEmpty()) {
			this.attributes = GamaMapFactory.create();
			this.attributes.putAll(attributes);
		}
	}

	@Override
	public Map<String, Object> getAttributes(final boolean createIfNeeded) {
		return attributes == null ? createIfNeeded ? attributes = GamaMapFactory.create() : Collections.EMPTY_MAP
				: attributes;
	}

	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException {
		if (attributes == null) return null;
		return attributes.get(index);
	}

	@Override
	public Object getFromIndicesList(final IScope scope, final IList<String> indices) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IClass getSpecies() { return species; }

	@Override
	public Object getDirectVarValue(final IScope scope, final String s) throws GamaRuntimeException {
		return getSpecies().getVarValue(scope, s, this);
	}

	@Override
	public void setDirectVarValue(final IScope scope, final String s, final Object v) throws GamaRuntimeException {
		getSpecies().setVarValue(scope, s, v, this);
	}

	@Override
	public boolean isInstanceOf(final IClass s, final boolean direct) {
		if (IKeyword.OBJECT.equals(s.getName())) return true;
		final IClass species = getSpecies();
		if (species == s) return true;
		if (!direct) return species.extendsSpecies(s);
		return false;
	}

}
