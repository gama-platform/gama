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

public final class ChooseProjectImportHandler extends AbstractHandler {

	public static final String COMMAND = "gama.ui.export.chooseProjectImport";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final String[] choices = { "Cancel", "Archive", "Folder" };
		final int choice = new MessageDialog(window.getShell(), "Import Project", null,
				"Choose a project folder or a GAMA project archive (.zip or .gamp)",
				MessageDialog.QUESTION, choices, 2).open();
		if (choice == 0 || choice == -1) return null;

		final var selection = HandlerUtil.getCurrentSelection(event);
		String group = "";
		if (selection instanceof IStructuredSelection structured && !structured.isEmpty()
				&& structured.getFirstElement() instanceof UserGroupFolder folder)
			group = folder.getRelativePath().toPortableString();
		final IImportWizard wizard = choice == 2 ? new ImportProjectWizard() : new ProjectArchiveWizard.Import(group);
		wizard.init(window.getWorkbench(), selection instanceof IStructuredSelection structured ? structured
				: StructuredSelection.EMPTY);
		new WizardDialog(window.getShell(), wizard).open();
		return null;
	}
}
