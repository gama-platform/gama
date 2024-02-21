/*******************************************************************************************************
 *
 * ConsumerWithPruning.java, in gama.annotations, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util;

/**
 * The Interface ConsumerWithPruning.
 *
 * @param <T> the generic type
 */
public interface ConsumerWithPruning<T> {

	/**
	 * Process.
	 *
	 * @param t the t
	 * @return true, if successful
	 */
	boolean process(T t);

}
