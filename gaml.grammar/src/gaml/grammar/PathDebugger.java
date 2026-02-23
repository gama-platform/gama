/*******************************************************************************************************
 *
 * PathDebugger.java, in gaml.grammar, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.grammar;

import java.io.File;

/**
 * The Class PathDebugger.
 */
public class PathDebugger {

	/**
	 * Sets the path.
	 *
	 * @param path
	 *            the new path
	 */
	public void setPath(final String path) {
		File file = new File(path);
		System.out.println("### MWE2 DEBUG ###");
		System.out.println("Relative path: " + path);
		System.out.println("Absolute path: " + file.getAbsolutePath());
		System.out.println("Exists: " + file.exists());
		System.out.println("##################");
	}
}