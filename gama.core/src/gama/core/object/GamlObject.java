/*******************************************************************************************************
 *
 * GamlObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.object;

import java.util.Map;
import java.util.Objects;

import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.json.IJsonValue;
import gama.core.util.json.JsonGamlObject;

/**
 *
 */
public class GamlObject implements IObject {

	/** The attributes. */
	final IMap<String, Object> attributes;

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
		this(species);
		initializeAttributes(scope, attributes);
	}

	/**
	 * Allocates an object without initializing its GAML attributes. Used by Java-backed class constructors.
	 */
	protected GamlObject(final IClass species) {
		this.index = System.identityHashCode(this);
		this.hashCode = Objects.hash(species, index);
		this.species = species;
		this.attributes = GamaMapFactory.create();
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
		final IMap<String, Object> values = GamaMapFactory.create();
		values.putAll(attributes);
		for (final var variable : species.getVars()) { values.put(variable.getName(), variable.value(scope, this)); }
		final IObject copy = species.createInstance(scope, values);
		if (copy instanceof GamlObject object) return object;
		throw GamaRuntimeException.error("Cannot copy " + getSpeciesName() + " as a GamlObject", scope);
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
	public IMap<String, Object> getAttributes(final boolean createIfNeeded) {
		return attributes;
	}

	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException {
		return species.hasVar(index) ? getDirectVarValue(scope, index) : attributes.get(index);
	}

	@Override
	public Object getFromIndicesList(final IScope scope, final IList<String> indices) throws GamaRuntimeException {
		return IObject.super.getFromIndicesList(scope, indices);
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
		if (!direct) return species.extendsClassOrSpecies(s);
		return false;
	}

	/**
	 * Serialize to json.
	 *
	 * @param json
	 *            the json
	 * @return the json value
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		JsonGamlObject obj = new JsonGamlObject(species.getName(), json);
		IJsonObject atts = json.object();
		obj.add("attributes", atts);
		for (String key : attributes.keySet()) { atts.add(key, json.valueOf(attributes.get(key))); }
		obj.add("attributes", atts);
		return obj;
	}

}