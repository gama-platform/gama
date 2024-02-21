/*******************************************************************************************************
 *
 * ResetModelingPerspective.java, in gama.ui.shared.experiment, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gama.ui.application.Application;
import gama.ui.shared.dialogs.Messages;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class ResetModelingPerspective.
 */
public class ResetModelingPerspective extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final boolean result = Messages.confirm("Reset modeling perspective",
				"Resetting the modeling perspective will lose memory of the current editors, navigator state and restart GAMA in a pristine state. Do you want to proceed ?");
		if (result) {
			Application.clearWorkspace(true);
			// removeWorkbenchXMI();
			WorkbenchHelper.getWorkbench().restart();
		}
		return null;
	}

	// TODO Propose it anyway ? workbench.xmi might play a role in the wrong color of tabs after a change of themes
	//
	// public static void removeWorkbenchXMI() {
	// final File workspace = new File(Platform.getInstanceLocation().getURL().getFile());
	// DEBUG.OUT("[GAMA] Removing the definition of workbench.xmi from the workspace");
	// File[] files = workspace.listFiles((FileFilter) file -> file.getName().equals(".metadata"));
	// if (files.length == 0) { return; }
	// files = files[0].listFiles((FileFilter) file -> file.getName().equals(".plugins"));
	// if (files.length == 0) { return; }
	// files = files[0].listFiles((FileFilter) file -> file.getName().equals("org.eclipse.e4.workbench"));
	// if (files.length == 0) { return; }
	// files = files[0].listFiles((FileFilter) file -> file.getName().equals("workbench.xmi"));
	// if (files.length == 0) { return; }
	// final File toRemove = files[0];
	// if (toRemove.exists()) {
	// final File renamed = new File(toRemove.getAbsolutePath().replace("workbench.xmi", "corrupted.xmi"));
	// toRemove.renameTo(renamed);
	// }
	// DEBUG.OUT("[GAMA] workbench.xmi removed. Restarting now");
	// return;
	//
	// }

}
