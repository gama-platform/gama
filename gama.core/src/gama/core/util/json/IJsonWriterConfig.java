/*******************************************************************************************************
 *
 * IJsonWriterConfig.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import java.io.Writer;

/**
 * Controls the formatting of the JSON output. Use one of the available constants.
 */
public interface IJsonWriterConfig {

	/**
	 * Write JSON in its minimal form, without any additional whitespace. This is the default.
	 */
	IJsonWriterConfig MINIMAL = writer -> new JsonWriter(writer);

	/**
	 * Write JSON in pretty-print, with each value on a separate line and an indentation of two spaces.
	 */
	IJsonWriterConfig PRETTY_PRINT = PrettyPrint.indentWithSpaces(2);

	/**
	 * Creates the writer.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param writer
	 *            the writer
	 * @return the json writer
	 * @date 29 oct. 2023
	 */
	JsonWriter createWriter(Writer writer);

}
