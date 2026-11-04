package gama.ui.export.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.handlers.HandlerUtil;

import gama.ui.navigator.wizards.ImportProjectWizard;

public final class ChooseProjectImportHandler extends AbstractHandler {

	public static final String COMMAND = "gama.ui.export.chooseProjectImport";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final var selection = HandlerUtil.getCurrentSelection(event);
		final IImportWizard wizard = new ImportProjectWizard();
		wizard.init(window.getWorkbench(), selection instanceof IStructuredSelection structured ? structured
				: StructuredSelection.EMPTY);
		new WizardDialog(window.getShell(), wizard).open();
		return null;
	}
}
