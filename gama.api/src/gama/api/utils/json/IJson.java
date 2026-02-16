/*******************************************************************************************************
 *
 * IJson.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.function.Supplier;


import gama.api.gaml.types.IType;
import gama.api.kernel.serialization.ISerialisedAgent;

/**
 *
 */


public interface IJson {

	/**
	 * The Interface Labels.
	 */
	interface Labels {

		/** The Constant NAME_GEOMETRY. */
		String NAME_GEOMETRY = "geometry";

		/** The Constant NAME_FEATURES. */
		String NAME_FEATURES = "features";

		/** The Constant NAME_GEOMETRIES. */
		String NAME_GEOMETRIES = "geometries";

		/** The Constant NAME_CRS. */
		String NAME_CRS = "crs";

		/** The Constant NAME_PROPERTIES. */
		String NAME_PROPERTIES = "properties";

		/** The Constant NAME_NAME. */
		String NAME_NAME = "name";

		/** The Constant NAME_TYPE. */
		String NAME_TYPE = "type";

		/** The Constant NAME_POINT. */
		String NAME_POINT = "Point";

		/** The Constant NAME_LINESTRING. */
		String NAME_LINESTRING = "LineString";

		/** The Constant NAME_POLYGON. */
		String NAME_POLYGON = "Polygon";

		/** The Constant NAME_COORDINATES. */
		String NAME_COORDINATES = "coordinates";

		/** The Constant NAME_GEOMETRYCOLLECTION. */
		String NAME_GEOMETRYCOLLECTION = "GeometryCollection";

		/** The Constant NAME_MULTIPOLYGON. */
		String NAME_MULTIPOLYGON = "MultiPolygon";

		/** The Constant NAME_MULTILINESTRING. */
		String NAME_MULTILINESTRING = "MultiLineString";

		/** The Constant NAME_MULTIPOINT. */
		String NAME_MULTIPOINT = "MultiPoint";

		/** The Constant NAME_FEATURE. */
		String NAME_FEATURE = "Feature";

		/** The Constant NAME_FEATURECOLLECTION. */
		String NAME_FEATURECOLLECTION = "FeatureCollection";

		/** The Constant GAML_TYPE_LABEL. */
		String GAML_TYPE_LABEL = "gaml_type";

		/** The Constant GAML_SPECIES_LABEL. */
		String GAML_SPECIES_LABEL = "gaml_species";

		/** The Constant GAMA_OBJECT_LABEL. */
		String CONTENTS_WITH_REFERENCES_LABEL = "gama_contents";

		/** The Constant AGENT_REFERENCE_LABEL. */
		String AGENT_REFERENCE_LABEL = "agent_reference";

		/** The Constant REFERENCE_TABLE_LABEL. */
		String REFERENCE_TABLE_LABEL = "gama_references";

	}

	/**
	 * Serialize.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @return the json value
	 * @date 29 oct. 2023
	 */
	IJsonValue valueOf(Object object);

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
	IJsonObject contents(IJsonValue contents, IJsonObject references);

	/**
	 * Returns a JsonValue instance that represents the given <code>int</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	IJsonValue valueOf(int value);

	/**
	 * Returns a JsonValue instance that represents the given <code>long</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	IJsonValue valueOf(long value);

	/**
	 * Returns a JsonValue instance that represents the given <code>float</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	IJsonValue valueOf(float value);

	/**
	 * Returns a JsonValue instance that represents the given <code>double</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	IJsonValue valueOf(double value);

	/**
	 * Returns a JsonValue instance that represents the given string.
	 *
	 * @param string
	 *            the string to get a JSON representation for
	 * @return a JSON value that represents the given string
	 */
	IJsonValue valueOf(String string);

	/**
	 * Value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return the json value
	 * @date 29 oct. 2023
	 */
	IJsonValue valueOf(Character c);

	/**
	 * Returns a JsonValue instance that represents the given <code>boolean</code> value.
	 *
	 * @param value
	 *            the value to get a JSON representation for
	 * @return a JSON value that represents the given value
	 */
	IJsonValue valueOf(boolean value);

	/**
	 * Creates a new empty JsonArray. This is equivalent to creating a new JsonArray using the constructor.
	 *
	 * @return a new empty JSON array
	 */
	IJsonArray array();

	/**
	 *
	 * @param values
	 *            the values to be included in the new JSON array
	 * @return a new JSON array that contains the given values
	 */
	/**
	 * Creates a new JsonArray that contains the JSON representations of the given <code>double</code> values.
	 *
	 * @param values
	 *            the values to be included in the new JSON array
	 * @return a new JSON array that contains the given values
	 */
	IJsonArray array(double... values);

	/**
	 * Array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param objects
	 *            the objects
	 * @return the json array
	 * @date 29 oct. 2023
	 */
	IJsonArray array(Collection objects);

	/**
	 * Reference.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param ref
	 *            the ref
	 * @return the json object
	 * @date 5 nov. 2023
	 */
	IJsonObject.Reference reference(String ref);

	/**
	 * Creates a new empty JsonObject. This is equivalent to creating a new JsonObject using the constructor.
	 *
	 * @return a new empty JSON object
	 */
	IJsonObject object();

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
	IJsonObject object(String k1, Object v1);

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	IJsonObject object(String k1, Object v1, String k2, Object v2);

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	IJsonObject object(String k1, Object v1, String k2, Object v2, String k3, Object v3);

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	IJsonObject object(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4);

	/**
	 * Creates a new empty JsonGamlObject provided with a gaml type. This is equivalent to creating a new JsonObject
	 * using the constructor and adding a first attribute with the type, but the interest of JsonGamlObjects is that
	 * they deserialized into Gaml objects directly.
	 *
	 * @return a new empty JsonGamlObject with the corresponding type
	 */
	IJsonObject typedObject(IType type);

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
	IJsonObject typedObject(IType type, String k1, Object v1);

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	IJsonObject typedObject(IType type, String k1, Object v1, String k2, Object v2);

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	IJsonObject typedObject(IType type, String k1, Object v1, String k2, Object v2, String k3, Object v3);

	/**
	 * Object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 *
	 * @return the json object
	 * @date 29 oct. 2023
	 */
	IJsonObject typedObject(IType type, String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4,
			Object v4);

	/**
	 * Agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the agent
	 * @date 29 oct. 2023
	 */
	IJsonObject.Agent agent(String species, int index);

	/**
	 * Parses the given input string as JSON. The input must contain a valid JSON value, optionally padded with
	 * whitespace.
	 *
	 * @param s
	 *            the input string, must be valid JSON
	 * @return a value that represents the parsed JSON
	 * @throws ParseException
	 *             if the input is not valid JSON
	 */
	IJsonValue parse(String s);

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
	IJsonValue parse(Reader reader) throws IOException;

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
	void addRef(String key, Supplier<ISerialisedAgent> value);

}