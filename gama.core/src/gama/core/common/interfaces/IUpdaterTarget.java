/*******************************************************************************************************
 *
 * IUpdaterTarget.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

/**
 * The Interface IUpdaterTarget.
 *
 * @param <Message>
 *            the generic type
 */
public interface IUpdaterTarget<Message extends IUpdaterMessage> {

	/**
	 * Checks if is disposed.
	 *
	 * @return true, if is disposed
	 */
	boolean isDisposed();

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	boolean isVisible();

	/**
	 * Checks if is busy.
	 *
	 * @return true, if is busy
	 */
	boolean isBusy();

	/**
	 * Update with.
	 *
	 * @param m
	 *            the m
	 */
	void updateWith(Message m);

	/**
	 * Resume.
	 */
	void resume();

}