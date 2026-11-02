package gama.ui.navigator.commands;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.wizards.datatransfer.FileSystemImportWizard;

import gama.ui.navigator.wizards.ZipImportWizard;
import gama.ui.navigator.view.contents.TopLevelFolder;
import gama.ui.navigator.view.contents.UserGroupFolder;
import gama.ui.navigator.view.contents.WrappedProject;

public final class ChooseFileImportHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final String[] choices = { "Cancel", "Archive", "Disk" };
		final int choice = new MessageDialog(window.getShell(), "Import Individual File", null,
				"Choose individual files from disk or a ZIP archive",
				MessageDialog.QUESTION, choices, 2).open();
		if (choice == 0 || choice == -1) return null;

		final var selection = HandlerUtil.getCurrentSelection(event);
		final Object selected = selection instanceof IStructuredSelection structured ? structured.getFirstElement() : null;
		final IContainer destination = destination(window.getShell(), selected);
		if (destination == null) return null;
		final IImportWizard wizard = choice == 1 ? new ZipImportWizard() : new FileSystemImportWizard();
		wizard.init(window.getWorkbench(), new StructuredSelection(destination));
		new WizardDialog(window.getShell(), wizard).open();
		return null;
	}

	private static IContainer destination(final Shell shell, final Object selected) {
		final IResource resource = Adapters.adapt(selected, IResource.class);
		final IContainer container = resource instanceof IFile file ? file.getParent()
				: resource instanceof IContainer c && !(c instanceof org.eclipse.core.resources.IWorkspaceRoot) ? c : null;
		if (container != null && container.isAccessible()) return container;

		final IProject[] projects;
		if (selected instanceof TopLevelFolder category) {
			projects = Arrays.stream(category.getProjects()).map(WrappedProject::getResource).toArray(IProject[]::new);
		} else if (selected instanceof UserGroupFolder group) {
			projects = group.getAllProjects().stream().map(WrappedProject::getResource).toArray(IProject[]::new);
		} else {
			projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		}
		final IProject[] available = Arrays.stream(projects).filter(IProject::isAccessible)
				.sorted(Comparator.comparing(IProject::getName, String.CASE_INSENSITIVE_ORDER)).toArray(IProject[]::new);
		if (available.length == 0) {
			MessageDialog.openInformation(shell, "Import Individual File",
					"No open projects are available in this destination. Create or open a project before importing files.");
			return null;
		}
		final var dialog = new ElementListSelectionDialog(shell, new LabelProvider() {
			@Override
			public String getText(final Object element) {
				return ((IProject) element).getName();
			}
		});
		dialog.setTitle("Import Destination");
		dialog.setMessage("Choose the project to import files into:");
		dialog.setMultipleSelection(false);
		dialog.setElements(available);
		return dialog.open() == Window.OK ? (IProject) dialog.getFirstResult() : null;
	}
}
