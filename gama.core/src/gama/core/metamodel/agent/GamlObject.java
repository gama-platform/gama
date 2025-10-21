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

import java.util.Map;
import java.util.Objects;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonGamlObject;
import gama.core.util.file.json.JsonObject;
import gama.core.util.file.json.JsonValue;
import gama.gaml.species.IClass;

/**
 *
 */
public class GamlObject implements IObject<IClass> {

	/** The attributes. */
	final Map<String, Object> attributes;

	/** The species. */
	final IClass species;

	/** The hash code. */
	public final int hashCode;

	/** The index. */
	protected final int index;

	/**
	 * Instantiates a new gaml object.
	 *
	 * @param species
	 *            the species.
	 */
	public GamlObject(final IScope scope, final IClass species, final Map<String, Object> attributes) {
		this.index = System.identityHashCode(this);
		this.hashCode = Objects.hash(species, index);
		this.species = species;
		this.attributes = GamaMapFactory.create();
		if (attributes != null && !attributes.isEmpty()) { this.attributes.putAll(attributes); }
		// We initialize the attributes using either the provided value, the init facet, or the default value of the
		// type
		species.getVars().forEach(v -> {
			v.initializeWith(scope, this, attributes == null ? null : attributes.get(v.getName()));
		});
	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the gaml object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public GamlObject copy(final IScope scope) throws GamaRuntimeException {
		return new GamlObject(scope, species, attributes);
	}

	/**
	 * Serialize to gaml.
	 *
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder(30);
		sb.append(getSpeciesName()).append('(').append(index).append(')');
		return sb.toString();
	}

	@Override
	public String toString() {
		return serializeToGaml(true);
	}

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return serializeToGaml(true);
	}

	@Override
	public Map<String, Object> getAttributes(final boolean createIfNeeded) {
		return attributes;
	}

	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException {
		return attributes.get(index);
	}

	@Override
	public Object getFromIndicesList(final IScope scope, final IList<String> indices) throws GamaRuntimeException {
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

	@Override
	public JsonValue serializeToJson(final Json json) {
		JsonGamlObject obj = new JsonGamlObject(species.getName(), json);
		JsonObject atts = json.object();
		obj.add("attributes", atts);
		for (String key : attributes.keySet()) { atts.add(key, json.valueOf(attributes.get(key))); }
		obj.add("attributes", atts);
		return obj;
	}

}
