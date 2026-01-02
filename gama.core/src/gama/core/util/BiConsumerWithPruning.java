/*******************************************************************************************************
 *
 * BiConsumerWithPruning.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import gama.annotations.precompiler.OkForAPI;
import gama.annotations.precompiler.OkForAPI.Location;

/**
 * The Interface BiConsumerWithPruning.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
@OkForAPI (Location.UTILS)
public interface BiConsumerWithPruning<K, V> {

	/**
	 * Process.
	 *
	 * @param k
	 *            the k
	 * @param v
	 *            the v
	 * @return true, if successful
	 */
	boolean process(K k, V v);
}