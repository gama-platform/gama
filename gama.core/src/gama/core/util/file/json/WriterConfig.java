/*******************************************************************************************************
 *
 * WriterConfig.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file.json;

import java.io.Writer;

/**
 * Controls the formatting of the JSON output. Use one of the available constants.
 */
public abstract class WriterConfig {

	/**
	 * Write JSON in its minimal form, without any additional whitespace. This is the default.
	 */
	public static final WriterConfig MINIMAL = new WriterConfig() {
		@Override
		JsonWriter createWriter(final Writer writer) {
			return new JsonWriter(writer);
		}
	};

	/**
	 * Write JSON in pretty-print, with each value on a separate line and an indentation of two spaces.
	 */
	public static final WriterConfig PRETTY_PRINT = PrettyPrint.indentWithSpaces(2);

	/**
	 * Creates the writer.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param writer
	 *            the writer
	 * @return the json writer
	 * @date 29 oct. 2023
	 */
	abstract JsonWriter createWriter(Writer writer);

}
