/*******************************************************************************************************
 *
 * RestartHandler.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.commands;

import static gama.ui.shared.utils.WorkbenchHelper.getCommand;
import static gama.ui.shared.utils.WorkbenchHelper.runCommand;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.internal.AbstractEnabledHandler;

/**
 * The Class RestartHandler.
 */
public class RestartHandler extends AbstractEnabledHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		return runCommand(getCommand("org.eclipse.ui.file.restartWorkbench"), event);
	}

}
