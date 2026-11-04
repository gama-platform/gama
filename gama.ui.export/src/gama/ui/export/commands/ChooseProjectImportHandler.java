/*******************************************************************************************************
 *
 * ChooseProjectImportHandler.java, in gama.ui.export, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.export.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.export.dialogs.ProjectArchiveWizard;
import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.navigator.wizards.ImportProjectWizard;

/**
 * The Class ChooseProjectImportHandler.
 */
public final class ChooseProjectImportHandler extends AbstractHandler {

	/** The Constant COMMAND. */
	public static final String COMMAND = "gama.ui.export.chooseProjectImport";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final String[] choices = { "Cancel", "Archive", "Folder" };
		final int choice = new MessageDialog(window.getShell(), "Import Project", null,
				"Choose to import a folder (for instance a project from another workspace or a Git repository) or an archive (.zip or the native .gamp format)",
				MessageDialog.QUESTION, choices, 2).open();
		if (choice == 0 || choice == -1) return null;

		final var selection = HandlerUtil.getCurrentSelection(event);
		String group = "";
		if (selection instanceof IStructuredSelection structured && !structured.isEmpty()
				&& structured.getFirstElement() instanceof UserGroupFolder folder) {
			group = folder.getRelativePath().toPortableString();
		}
		final IImportWizard wizard = choice == 2 ? new ImportProjectWizard() : new ProjectArchiveWizard.Import(group);
		wizard.init(window.getWorkbench(),
				selection instanceof IStructuredSelection structured ? structured : StructuredSelection.EMPTY);
		new WizardDialog(window.getShell(), wizard).open();
		return null;
	}
}
