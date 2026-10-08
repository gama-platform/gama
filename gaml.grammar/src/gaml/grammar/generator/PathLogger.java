/*******************************************************************************************************
 *
 * PathLogger.java, in gaml.grammar, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.grammar.generator;

import java.io.File;
import java.nio.file.Paths;

import gama.dev.DEBUG;

/**
 * The Class PathLogger.
 */
public class PathLogger {

	/**
	 * Sets the path.
	 *
	 * @param path
	 *            the new path
	 */
	public void setPath(final String path) {
		File file = new File(path);
		DEBUG.LINE();
		DEBUG.TITLE("Generating the GAML ARTIFACTS");
		DEBUG.TITLE("Root: " + Paths.get(file.getAbsolutePath()).toUri().normalize().getRawPath());
		DEBUG.LINE();
	}
}