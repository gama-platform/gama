/*******************************************************************************************************
 *
 * IGamlAdditions.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;



/**
 * The Interface IGamlAdditions. Holds a number of default utility methods that allow to write compact declarations of
 * operators, actions, skills, etc. in the GamlAdditions files. Also provides the unique method to redefine
 * ({@link #initialize()}.
 */

public interface IGamlAdditions {

	/**
	 * Initialize.
	 *
	 * @throws SecurityException
	 *             the security exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	void initialize() throws SecurityException, NoSuchMethodException;

}
