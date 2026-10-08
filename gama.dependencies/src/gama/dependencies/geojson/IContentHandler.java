/*******************************************************************************************************
 *
 * IContentHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson;

import org.json.simple.parser.ContentHandler;

/**
 * The Interface IContentHandler.
 *
 * @param <T>
 *            the generic type
 */
public interface IContentHandler<T> extends ContentHandler {

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	T getValue();
}
