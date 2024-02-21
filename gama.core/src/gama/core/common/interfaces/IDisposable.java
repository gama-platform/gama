/*******************************************************************************************************
 *
 * IDisposable.java, in gama.annotations, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.common.interfaces;

/**
 * Class IDisposable.
 * 
 * @author drogoul
 * @since 13 avr. 2014
 * 
 */
public interface IDisposable {

	/**
	 * Dispose.
	 */
	default void dispose() {}

}
