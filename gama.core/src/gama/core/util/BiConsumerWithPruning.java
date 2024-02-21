/*******************************************************************************************************
 *
 * BiConsumerWithPruning.java, in gama.annotations, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util;

/**
 * The Interface BiConsumerWithPruning.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface BiConsumerWithPruning<K, V> {
	
	/**
	 * Process.
	 *
	 * @param k the k
	 * @param v the v
	 * @return true, if successful
	 */
	boolean process(K k, V v);
}