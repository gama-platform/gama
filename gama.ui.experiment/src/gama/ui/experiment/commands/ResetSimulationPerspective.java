/*******************************************************************************************************
 *
 * ResetSimulationPerspective.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchPage;

import gama.api.GAMA;

/**
 * The Class ResetSimulationPerspective.
 */
public class ResetSimulationPerspective extends AbstractHandler { // NO_UCD (unused code)

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			final WorkbenchPage page = (WorkbenchPage) activeWorkbenchWindow.getActivePage();
			if (page != null) {
				final IPerspectiveDescriptor descriptor = page.getPerspective();
				if (descriptor != null) {
					if (!GAMA.getGui().getDialogFactory().question("Reset experiment perspective",
							"Resetting the perspective will reload the current experiment. Do you want to proceed ?"))
						return null;
					page.resetPerspective();
					GAMA.reloadFrontmostExperiment(false);
				}
			}
		}

		return null;

	}

}
