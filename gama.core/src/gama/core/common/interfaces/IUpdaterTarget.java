/*******************************************************************************************************
 *
 * IUpdaterTarget.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import gama.core.common.StatusMessage;

/**
 * The Interface IUpdaterTarget.
 *
 * @param <Message>
 *            the generic type
 */
public interface IUpdaterTarget {

	/**
	 * Checks if is disposed.
	 *
	 * @return true, if is disposed
	 */
	default boolean isDisposed() { return false; }

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	default boolean isVisible() { return true; }

	/**
	 * Checks if is busy.
	 *
	 * @return true, if is busy
	 */
	default boolean isBusy() { return false; }

	/**
	 * Update with.
	 *
	 * @param m
	 *            the m
	 */
	default void updateWith(final StatusMessage m) {}

	/**
	 * Resume.
	 */
	default void reset() {}

}