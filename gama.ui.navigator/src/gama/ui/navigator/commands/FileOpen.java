/*******************************************************************************************************
 *
 * FileOpen.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gama.core.common.GamlFileExtension;
import gama.ui.application.workspace.WorkspaceModelsManager;

/**
 * Opens a file
 */
public class FileOpen extends AbstractHandler { // NO_UCD (unused code)

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.gaml", "*.experiment", "*.*" });
		dialog.setFilterNames(new String[] { "GAML model files", "GAML experiment files", "All Files" });
		final String fileSelected = dialog.open();

		if (fileSelected != null && GamlFileExtension.isAny(fileSelected)) {
			// Perform Action, like open the file.
			WorkspaceModelsManager.instance.openModelPassedAsArgument(fileSelected);
		}
		return null;
	}
}