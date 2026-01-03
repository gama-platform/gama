/*******************************************************************************************************
 *
 * IJsonValue.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file.json;

import java.io.IOException;
import java.io.Writer;

import gama.annotations.precompiler.OkForAPI;
import gama.core.runtime.IScope;

/**
 *
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface IJsonValue extends IJsonConstants {

	/**
	 * Checks if is gaml object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is gaml object
	 * @date 4 nov. 2023
	 */
	boolean isGamlObject();

	/**
	 * Checks if is gaml agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is gaml agent
	 * @date 4 nov. 2023
	 */
	boolean isGamlAgent();

	/**
	 * Detects whether this value represents a JSON object. If this is the case, this value is an instance of
	 * {@link JsonObject}.
	 *
	 * @return <code>true</code> if this value is an instance of JsonObject
	 */
	boolean isObject();

	/**
	 * Detects whether this value represents a JSON array. If this is the case, this value is an instance of
	 * {@link JsonArray}.
	 *
	 * @return <code>true</code> if this value is an instance of JsonArray
	 */
	boolean isArray();

	/**
	 * Detects whether this value represents a JSON number.
	 *
	 * @return <code>true</code> if this value represents a JSON number
	 */
	boolean isNumber();

	/**
	 * Detects whether this value represents a JSON string.
	 *
	 * @return <code>true</code> if this value represents a JSON string
	 */
	boolean isString();

	/**
	 * Detects whether this value represents a boolean value.
	 *
	 * @return <code>true</code> if this value represents either the JSON literal <code>true</code> or
	 *         <code>false</code>
	 */
	boolean isBoolean();

	/**
	 * Detects whether this value represents the JSON literal <code>true</code>.
	 *
	 * @return <code>true</code> if this value represents the JSON literal <code>true</code>
	 */
	boolean isTrue();

	/**
	 * Detects whether this value represents the JSON literal <code>false</code>.
	 *
	 * @return <code>true</code> if this value represents the JSON literal <code>false</code>
	 */
	boolean isFalse();

	/**
	 * Detects whether this value represents the JSON literal <code>null</code>.
	 *
	 * @return <code>true</code> if this value represents the JSON literal <code>null</code>
	 */
	boolean isNull();

	/**
	 * Returns this JSON value as {@link JsonObject}, assuming that this value represents a JSON object. If this is not
	 * the case, an exception is thrown.
	 *
	 * @return a JSONObject for this value
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON object
	 */
	JsonObject asObject();

	/**
	 * Returns this JSON value as {@link JsonArray}, assuming that this value represents a JSON array. If this is not
	 * the case, an exception is thrown.
	 *
	 * @return a JSONArray for this value
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON array
	 */
	IJsonArray asArray();

	/**
	 * Returns this JSON value as an <code>int</code> value, assuming that this value represents a JSON number that can
	 * be interpreted as Java <code>int</code>. If this is not the case, an exception is thrown.
	 * <p>
	 * To be interpreted as Java <code>int</code>, the JSON number must neither contain an exponent nor a fraction part.
	 * Moreover, the number must be in the <code>Integer</code> range.
	 * </p>
	 *
	 * @return this value as <code>int</code>
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON number
	 * @throws NumberFormatException
	 *             if this JSON number can not be interpreted as <code>int</code> value
	 */
	int asInt();

	/**
	 * Returns this JSON value as a <code>long</code> value, assuming that this value represents a JSON number that can
	 * be interpreted as Java <code>long</code>. If this is not the case, an exception is thrown.
	 * <p>
	 * To be interpreted as Java <code>long</code>, the JSON number must neither contain an exponent nor a fraction
	 * part. Moreover, the number must be in the <code>Long</code> range.
	 * </p>
	 *
	 * @return this value as <code>long</code>
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON number
	 * @throws NumberFormatException
	 *             if this JSON number can not be interpreted as <code>long</code> value
	 */
	long asLong();

	/**
	 * Returns this JSON value as a <code>float</code> value, assuming that this value represents a JSON number. If this
	 * is not the case, an exception is thrown.
	 * <p>
	 * If the JSON number is out of the <code>Float</code> range, {@link Float#POSITIVE_INFINITY} or
	 * {@link Float#NEGATIVE_INFINITY} is returned.
	 * </p>
	 *
	 * @return this value as <code>float</code>
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON number
	 */
	float asFloat();

	/**
	 * Returns this JSON value as a <code>double</code> value, assuming that this value represents a JSON number. If
	 * this is not the case, an exception is thrown.
	 * <p>
	 * If the JSON number is out of the <code>Double</code> range, {@link Double#POSITIVE_INFINITY} or
	 * {@link Double#NEGATIVE_INFINITY} is returned.
	 * </p>
	 *
	 * @return this value as <code>double</code>
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON number
	 */
	double asDouble();

	/**
	 * Returns this JSON value as String, assuming that this value represents a JSON string. If this is not the case, an
	 * exception is thrown.
	 *
	 * @return the string represented by this value
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON string
	 */
	String asString();

	/**
	 * Returns this JSON value as a <code>boolean</code> value, assuming that this value is either <code>true</code> or
	 * <code>false</code>. If this is not the case, an exception is thrown.
	 *
	 * @return this value as <code>boolean</code>
	 * @throws UnsupportedOperationException
	 *             if this value is neither <code>true</code> or <code>false</code>
	 */
	boolean asBoolean();

	/**
	 * As gaml object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the json gaml object
	 * @date 4 nov. 2023
	 */
	JsonGamlObject asGamlObject();

	/**
	 * As gaml agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the json gaml agent
	 * @date 4 nov. 2023
	 */
	JsonGamlAgent asGamlAgent();

	/**
	 * Writes the JSON representation of this value to the given writer in its minimal form, without any additional
	 * whitespace.
	 * <p>
	 * Writing performance can be improved by using a {@link java.io.BufferedWriter BufferedWriter}.
	 * </p>
	 *
	 * @param writer
	 *            the writer to write this value to
	 * @throws IOException
	 *             if an I/O error occurs in the writer
	 */
	void writeTo(Writer writer) throws IOException;

	/**
	 * Writes the JSON representation of this value to the given writer using the given formatting.
	 * <p>
	 * Writing performance can be improved by using a {@link java.io.BufferedWriter BufferedWriter}.
	 * </p>
	 *
	 * @param writer
	 *            the writer to write this value to
	 * @param config
	 *            a configuration that controls the formatting or <code>null</code> for the minimal form
	 * @throws IOException
	 *             if an I/O error occurs in the writer
	 */
	void writeTo(Writer writer, WriterConfig config) throws IOException;

	/**
	 * Returns the JSON string for this value using the given formatting.
	 *
	 * @param config
	 *            a configuration that controls the formatting or <code>null</code> for the minimal form
	 * @return a JSON string that represents this value
	 */
	String toString(WriterConfig config);

	/**
	 * To gaml value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the object
	 * @date 2 nov. 2023
	 */
	Object toGamlValue(IScope scope);

	/**
	 * @param writer
	 */
	void write(JsonWriter writer) throws IOException;

}