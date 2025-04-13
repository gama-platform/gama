/*******************************************************************************************************
 *
 * IStatusControl.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.common.IStatusMessage;

/**
 * The Interface IStatusControl.
 *
 * @param <Message>
 *            the generic type
 */
public interface IStatusControl {

	/**
	 * Checks if is disposed.
	 *
	 * @return true, if is disposed
	 */
	default boolean isDisposed() { return false; }

	/**
	 * Update with.
	 *
	 * @param m
	 *            the m
	 */
	default void updateWith(final IStatusMessage m) {}

}