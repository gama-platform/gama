/*******************************************************************************************************
 *
 * GamaKeyAndMouseDispatcher.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.bindings;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import gama.dev.DEBUG;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class GamaKeyAndMouseDispatcher.
 */
public class GamaKeyAndMouseDispatcher implements Listener {

	static {
		DEBUG.OFF();
	}

	@Override
	public void handleEvent(final Event event) {
		DEBUG.OUT("Event  " + event);
		if (event.widget instanceof IDelegateEventsToParent) {
			((IDelegateEventsToParent) event.widget).getParent().notifyListeners(event.type, event);
		}
	}

	/**
	 * Install.
	 */
	public static void install() {
		WorkbenchHelper.run(() -> {
			Display d = WorkbenchHelper.getDisplay();
			GamaKeyAndMouseDispatcher listener = new GamaKeyAndMouseDispatcher();
			d.addFilter(SWT.KeyDown, listener);
			d.addFilter(SWT.MouseDoubleClick, listener);
			d.addFilter(SWT.MouseDown, listener);
			d.addFilter(SWT.MouseEnter, listener);
			d.addFilter(SWT.MouseExit, listener);
			d.addFilter(SWT.MouseHover, listener);
			d.addFilter(SWT.MouseMove, listener);
			d.addFilter(SWT.MouseUp, listener);
			d.addFilter(SWT.MouseVerticalWheel, listener);
			d.addFilter(SWT.Gesture, listener);
			d.addFilter(SWT.KeyUp, listener);
		});

	}

}
