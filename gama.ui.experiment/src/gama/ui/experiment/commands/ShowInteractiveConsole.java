/*******************************************************************************************************
 *
 * ShowInteractiveConsole.java, in gama.ui.shared.experiment, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import static gama.core.common.interfaces.IGui.INTERACTIVE_CONSOLE_VIEW_ID;
import static org.eclipse.ui.IWorkbenchPage.VIEW_VISIBLE;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gama.core.runtime.GAMA;

/**
 * The Class ShowInteractiveConsole.
 */
public class ShowInteractiveConsole extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		return GAMA.getGui().showView(null, INTERACTIVE_CONSOLE_VIEW_ID, null, VIEW_VISIBLE);
	}

}
