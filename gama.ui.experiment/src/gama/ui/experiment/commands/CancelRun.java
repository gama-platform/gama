/*******************************************************************************************************
 *
 * CancelRun.java, in gama.ui.shared.experiment, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import gama.core.runtime.GAMA;
import gama.ui.shared.bindings.GamaKeyBindings;

/**
 * The Class CancelRun.
 */
public class CancelRun extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		new Thread(() -> GAMA.closeAllExperiments(true, false)).start();

		return null;
	}

	@Override
	public void updateElement(final UIElement element, final Map parameters) {
		element.setTooltip("Closes the current experiment (" + GamaKeyBindings.QUIT_STRING + ")");
		element.setText("Close Experiment (" + GamaKeyBindings.QUIT_STRING + ")");
	}

}
