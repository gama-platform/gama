/*******************************************************************************************************
 *
 * Json.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import gama.core.metamodel.agent.SerialisedAgent;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.gaml.interfaces.IJsonable;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * This class serves as the entry point to the minimal-json API. It has been adapted to GAMA by A. Drogoul in 2023 to
 * get rid of the static features, become stateful (i.e. add a serialisation context to keep references, be thread safe
 * and compute statistics), and add some useful features for GAMA (typedObject(...), etc.)
 */
public final class Json implements IJsonConstants {

	/**
	 * Gets a new stateful instance of Json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the new
	 * @date 31 oct. 2023
	 */
	public static Json getNew() { return new Json(); }

	/** The initial. */
	boolean firstPass = true;

	/** The agents. */
	JsonObject agentReferences = new JsonObject(this);

	/**
	 * Serialize.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @return the json value
	 * @date 29 oct. 2023
	 */
	public JsonValue valueOf(final Object object) {
		boolean initial = firstPass;
		firstPass = false;
		JsonValue result = NULL;
		try {
			result = switch (object) {
				case JsonValue jv -> jv;
				case IJsonable j -> j.serializeToJson(this);
				case String s -> valueOf(s);
				case Character c -> valueOf(c);
				case Double d -> valueOf(d.doubleValue());
				case Float f -> valueOf(f.doubleValue());
				case Integer n -> valueOf(n.intValue());
				case Long n -> valueOf((int) n.longValue());
				case Boolean b -> valueOf(b.booleanValue());
				case Collection<?> c -> GamaListFactory.wrap(Types.NO_TYPE, c).serializeToJson(this);
				case Map<?, ?> m -> GamaMapFactory.wrap(m).serializeToJson(this);
				case Exception ex -> object("exception", ex.getClass().getName(), "message", ex.getMessage(), "stack",
						array(Arrays.asList(ex.getStackTrace())));
				case null -> NULL;
				default -> valueOf(object.toString());
			};
		} finally {
			if (initial) {
				if (!agentReferences.isEmpty()) { result = contents(result, agentReferences); }
				firstPass = true; // in case the encoder is reused
			}
		}
		return result;
	}

	/**
	 * Contents.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param contents
	 *            the contents
	 * @param references
	 *            the references
	 * @return the json gama contents object
	 * @date 6 nov. 2023
	 */
	public JsonGamaContentsObject contents(final JsonValue contents, final JsonObject references) {
		return new JsonGamaContentsObject(contents, references, this);
	}

	/**
	 * Returns a JsonValue instance that represents the given <code>int</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	public JsonValue valueOf(final int value) {
		return new JsonInt(Integer.toString(value, 10));
	}

	/**
	 * Returns a JsonValue instance that represents the given <code>long</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	public JsonValue valueOf(final long value) {
		return new JsonInt(Integer.toString((int) value));
	}

	/**
	 * Returns a JsonValue instance that represents the given <code>float</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	public JsonValue valueOf(final float value) {
		// if (Float.isNaN(value)) return NULL;
		return new JsonFloat(value);
	}

	/**
	 * Returns a JsonValue instance that represents the given <code>double</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	public JsonValue valueOf(final double value) {
		// if (Double.isNaN(value)) return NULL;
		return new JsonFloat(value);
	}

	/**
	 * Returns a JsonValue instance that represents the given string.
	 *
	 * @param string
	 *            the string to get a JSON representation for
	 * @return a JSON value that represents the given string
	 */
	public JsonValue valueOf(final String string) {
		return string == null ? NULL : new JsonString(string);
	}

	/**
	 * Value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return the json value
	 * @date 29 oct. 2023
	 */
	public JsonValue valueOf(final Character c) {
		return c == null ? NULL : new JsonString(c.toString());
	}

	/**
	 * Returns a JsonValue instance that represents the given <code>boolean</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	public JsonValue valueOf(final boolean value) {
		return value ? TRUE : FALSE;
	}

	/**
	 * Creates a new empty JsonArray. This is equivalent to creating a new JsonArray using the constructor.
	 *
	 * @return a new empty JSON array
	 */
	public JsonArray array() {
		return new JsonArray(this);
	}

	/**
	 * Creates a new JsonArray that contains the JSON representations of the given <code>int</code> values.
	 *
	 * @param values
	 *            the values to be included in the new JSON array
	 * @return a new JSON array that contains the given values
	 */
	public JsonArray array(final int... values) {
		if (values == null) throw new NullPointerException("values is null");
		JsonArray array = new JsonArray(this);
		for (int value : values) { array.add(value); }
		return array;
	}

	/**
	 * Creates a new JsonArray that contains the JSON representations of the given <code>long</code> values.
	 *
	 * @param values
	 *            the values to be included in the new JSON array
	 * @return a new JSON array that contains the given values
	 */
	public JsonArray array(final long... values) {
		if (values == null) throw new NullPointerException("values is null");
		JsonArray array = new JsonArray(this);
		for (long value : values) { array.add(value); }
		return array;
	}

	/**
	 * Creates a new JsonArray that contains the JSON representations of the given <code>float</code> values.
	 *
	 * @param values
	 *            the values to be included in the new JSON array
	 * @return a new JSON array that contains the given values
	 */
	public JsonArray array(final float... values) {
		if (values == null) throw new NullPointerException("values is null");
		JsonArray array = new JsonArray(this);
		for (float value : values) { array.add(value); }
		return array;
	}

	/**
	 * Creates a new JsonArray that contains the JSON representations of the given <code>double</code> values.
	 *
	 * @param values
	 *            the values to be included in the new JSON array
	 * @return a new JSON array that contains the given values
	 */
	public JsonArray array(final double... values) {
		if (values == null) throw new NullPointerException("values is null");
		JsonArray array = new JsonArray(this);
		for (double value : values) { array.add(value); }
		return array;
	}

	/**
	 * Creates a new JsonArray that contains the JSON representations of the given <code>boolean</code> values.
	 *
	 * @param values
	 *            the values to be included in the new JSON array
	 * @return a new JSON array that contains the given values
	 */
	public JsonArray array(final boolean... values) {
		JsonArray array = new JsonArray(this);
		for (boolean value : values) { array.add(value); }
		return array;
	}

	/**
	 * Creates a new JsonArray that contains the JSON representations of the given strings.
	 *
	 * @param strings
	 *            the strings to be included in the new JSON array
	 * @return a new JSON array that contains the given strings
	 */
	public JsonArray array(final String... strings) {
		JsonArray array = new JsonArray(this);
		for (String value : strings) { array.add(value); }
		return array;
	}

	/**
	 * Array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param objects
	 *            the objects
	 * @return the json array
	 * @date 29 oct. 2023
	 */
	public JsonArray array(final Object[] objects) {
		JsonArray array = new JsonArray(this);
		for (Object value : objects) { array.add(value); }
		return array;
	}

	/**
	 * Array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param objects
	 *            the objects
	 * @return the json array
	 * @date 29 oct. 2023
	 */
	public JsonArray array(final Collection objects) {
		JsonArray array = new JsonArray(this);
		for (Object value : objects) { array.add(value); }
		return array;
	}

	/**
	 * Reference.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param ref
	 *            the ref
	 * @return the json object
	 * @date 5 nov. 2023
	 */
	public JsonReferenceObject reference(final String ref) {
		return new JsonReferenceObject(ref, this);
	}

	/**
	 * Creates a new empty JsonObject. This is equivalent to creating a new JsonObject using the constructor.
	 *
	 * @return a new empty JSON object
	 */
	public JsonObject object() {
		return new JsonObject(this);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param k1
	 *            the k 1
	 * @param v1
	 *            the v 1
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonObject object(final String k1, final Object v1) {
		return (JsonObject) object().add(k1, v1);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonObject object(final String k1, final Object v1, final String k2, final Object v2) {
		return (JsonObject) object(k1, v1).add(k2, v2);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonObject object(final String k1, final Object v1, final String k2, final Object v2, final String k3,
			final Object v3) {
		return (JsonObject) object(k1, v1, k2, v2).add(k3, v3);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonObject object(final String k1, final Object v1, final String k2, final Object v2, final String k3,
			final Object v3, final String k4, final Object v4) {
		return (JsonObject) object(k1, v1, k2, v2, k3, v3).add(k4, v4);
	}

	/**
	 * Creates a new empty JsonGamlObject provided with a gaml type. This is equivalent to creating a new JsonObject
	 * using the constructor and adding a first attribute with the type, but the interest of JsonGamlObjects is that
	 * they deserialized into Gaml objects directly.
	 *
	 * @return a new empty JsonGamlObject with the corresponding type
	 */
	public JsonGamlObject typedObject(final IType type) {
		return new JsonGamlObject(type.getName(), this);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param k1
	 *            the k 1
	 * @param v1
	 *            the v 1
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonGamlObject typedObject(final IType type, final String k1, final Object v1) {
		return (JsonGamlObject) typedObject(type).add(k1, v1);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonGamlObject typedObject(final IType type, final String k1, final Object v1, final String k2,
			final Object v2) {
		return (JsonGamlObject) typedObject(type, k1, v1).add(k2, v2);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonGamlObject typedObject(final IType type, final String k1, final Object v1, final String k2,
			final Object v2, final String k3, final Object v3) {
		return (JsonGamlObject) typedObject(type, k1, v1, k2, v2).add(k3, v3);
	}

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	public JsonGamlObject typedObject(final IType type, final String k1, final Object v1, final String k2,
			final Object v2, final String k3, final Object v3, final String k4, final Object v4) {
		return (JsonGamlObject) typedObject(type, k1, v1, k2, v2, k3, v3).add(k4, v4);
	}

	/**
	 * Agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the agent
	 * @date 29 oct. 2023
	 */
	public JsonGamlAgent agent(final String species, final int index) {
		return new JsonGamlAgent(species, index, this);
	}

	/**
	 * Parses the given input string as JSON. The input must contain a valid JSON value, optionally padded with
	 * whitespace.
	 *
	 * @param string
	 *            the input string, must be valid JSON
	 * @return a value that represents the parsed JSON
	 * @throws ParseException
	 *             if the input is not valid JSON
	 */
	public JsonValue parse(final String string) {
		if (string == null) throw new NullPointerException("string is null");
		JsonGamaHandler handler = new JsonGamaHandler(this);
		new JsonParser(handler).parse(string);
		return handler.getValue();
	}

	/**
	 * Reads the entire input from the given reader and parses it as JSON. The input must contain a valid JSON value,
	 * optionally padded with whitespace.
	 * <p>
	 * Characters are read in chunks into an input buffer. Hence, wrapping a reader in an additional
	 * <code>BufferedReader</code> likely won't improve reading performance.
	 * </p>
	 *
	 * @param reader
	 *            the reader to read the JSON value from
	 * @return a value that represents the parsed JSON
	 * @throws IOException
	 *             if an I/O error occurs in the reader
	 * @throws ParseException
	 *             if the input is not valid JSON
	 */
	public JsonValue parse(final Reader reader) throws IOException {
		if (reader == null) throw new NullPointerException("reader is null");
		JsonGamaHandler handler = new JsonGamaHandler(this);
		new JsonParser(handler).parse(reader);
		return handler.getValue();
	}

	/**
	 * Cut off point zero.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param string
	 *            the string
	 * @return the string
	 * @date 29 oct. 2023
	 */
	private String cutOffPointZero(final String string) {
		if (string.endsWith(".0")) return string.substring(0, string.length() - 2);
		return string;
	}

	/**
	 * Adds the ref.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @date 1 nov. 2023
	 */
	public void addRef(final String key, final Supplier<SerialisedAgent> value) {
		if (agentReferences.contains(key)) return;
		// We first set it to avoid infinite loops
		agentReferences.add(key, (Object) null);
		JsonValue agent = valueOf(value.get());
		// We now replace it with the agent
		agentReferences.set(key, agent);
	}

}
