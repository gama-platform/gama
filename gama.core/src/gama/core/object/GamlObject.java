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
import gama.api.gaml.symbols.IVariable;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.runtime.scope.IScope;
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
	IMap<String, Object> attributes;

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
			if (!this.attributes.containsKey(v.getName())) {
				// No initialization provided
				this.attributes.put(v.getName(), v.getInitialValue(scope));
				// FIXME TODO In this configuraiton, the object is **not** passed in the scope !!!!
			}
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
	public IObject copy(final IScope scope) throws GamaRuntimeException {
		return new GamlObject(scope, species, attributes.copy(scope));
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
		return attributes == null ? createIfNeeded ? attributes = GamaMapFactory.create() : GamaMapFactory.EMPTY
				: attributes;
	}

	/**
	 * Gets the.
	 *
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public Object get(final IScope scope, final String index) throws GamaRuntimeException {
		return getAttributes(false).get(index);
	}

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	@Override
	public IClass getSpecies() { return species; }

	/**
	 * Gets the direct var value.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @return the direct var value
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public Object getDirectVarValue(final IScope scope, final String s) throws GamaRuntimeException {
		IVariable variable = getSpecies().getVar(s);
		if (variable == null)
			throw GamaRuntimeException.error("Variable " + s + " not found in class " + getSpecies().getName(), scope);
		Object result = getAttributes(false).get(s);
		if (result == null) { result = variable.getType().getDefault(); }
		return result;
	}

	/**
	 * Sets the direct var value.
	 *
	 * @param scope
	 *            the scope
	 * @param s
	 *            the s
	 * @param v
	 *            the v
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public void setDirectVarValue(final IScope scope, final String s, final Object value) throws GamaRuntimeException {
		IVariable v = getSpecies().getVar(s);
		if (v == null)
			throw GamaRuntimeException.error("Variable " + s + " not found in class " + getSpecies().getName(), scope);
		if (v.isNotModifiable()) throw GamaRuntimeException
				.error("Variable " + s + " is not modifiable in class " + getSpecies().getName(), scope);

		getAttributes(true).put(s, value);
	}

	/**
	 * Checks if is instance of.
	 *
	 * @param s
	 *            the s
	 * @param direct
	 *            the direct
	 * @return true, if is instance of
	 */
	@Override
	public boolean isInstanceOf(final IClass s, final boolean direct) {
		if (IKeyword.OBJECT.equals(s.getName())) return true;
		final IClass species = getSpecies();
		if (species == s) return true;
		if (!direct) return species.extendsSpecies(s);
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
		for (String key : getAttributes(false).keySet()) { atts.add(key, json.valueOf(getAttributes(false).get(key))); }
		obj.add("attributes", atts);
		return obj;
	}

}