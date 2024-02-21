/*******************************************************************************************************
 *
 * Selector.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * The Interface Selector.
 */
@FunctionalInterface
public interface Selector extends SelectionListener {

	/**
	 * Widget default selected.
	 *
	 * @param e the e
	 */
	@Override
	default void widgetDefaultSelected(final SelectionEvent e) {
		widgetSelected(e);
	}
}
