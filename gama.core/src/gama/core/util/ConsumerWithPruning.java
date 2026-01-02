/*******************************************************************************************************
 *
 * ConsumerWithPruning.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import gama.annotations.precompiler.OkForAPI;

/**
 * The Interface ConsumerWithPruning.
 *
 * @param <T>
 *            the generic type
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface ConsumerWithPruning<T> {

	/**
	 * Process.
	 *
	 * @param t
	 *            the t
	 * @return true, if successful
	 */
	boolean process(T t);

}
