/*******************************************************************************************************
 *
 * AttributeIO.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

/**
 * Parses and encoded feature attributes.
 *
 * @author Justin Deoliveira, OpenGeo
 */
public interface AttributeIO {

	/**
	 * Parses the.
	 *
	 * @param att
	 *            the att
	 * @param value
	 *            the value
	 * @return the object
	 */
	Object parse(String att, String value);

	/**
	 * Encode.
	 *
	 * @param att
	 *            the att
	 * @param value
	 *            the value
	 * @return the string
	 */
	String encode(String att, Object value);
}
