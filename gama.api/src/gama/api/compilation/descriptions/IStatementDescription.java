/*******************************************************************************************************
 *
 * IStatementDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import gama.api.gaml.symbols.Arguments;

/**
 *
 */
public interface IStatementDescription extends IDescription {

	/**
	 * @return
	 */
	Iterable<IDescription> getFormalArgs();

	/**
	 * Gets the passed args.
	 *
	 * @return the passed args
	 */
	Arguments getPassedArgs();

	/**
	 * @return
	 */
	default boolean isSuperInvocation() { return false; }

	/**
	 * @return
	 */
	default boolean isContinuable() { return false; }

	/**
	 * @return
	 */
	default boolean isBreakable() { return false; }

}
