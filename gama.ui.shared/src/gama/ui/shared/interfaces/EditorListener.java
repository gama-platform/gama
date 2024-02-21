/*******************************************************************************************************
 *
 * EditorListener.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.interfaces;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 * Written by drogoul Modified on 27 mai 2011
 *
 * @todo Description
 *
 */
public interface EditorListener<T> {

	/**
	 * Value modified.
	 *
	 * @param val
	 *            the val
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	void valueModified(T val) throws GamaRuntimeException;

	/**
	 * The Interface Command.
	 */
	public interface Command extends EditorListener<Object>, SelectionListener {

		@Override
		default void valueModified(final Object o) {
			this.widgetSelected(null);
		}

		@Override
		default void widgetDefaultSelected(final SelectionEvent o) {
			this.widgetSelected(null);
		}

	}

	public interface Static extends EditorListener<Object>, SelectionListener {
		@Override
		default void valueModified(final Object o) {}

		@Override
		default void widgetDefaultSelected(final SelectionEvent o) {}

	}

}
