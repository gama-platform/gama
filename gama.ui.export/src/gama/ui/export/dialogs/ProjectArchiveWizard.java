package gama.ui.export.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import gama.ui.export.commands.ExportProject;
import gama.ui.export.commands.ImportProject;

/** Bridges Eclipse's generic Import/Export lists to the same simple project dialogs. */
public class ProjectArchiveWizard extends Wizard implements IImportWizard, IExportWizard {

	public static final class Import extends ProjectArchiveWizard {
		public Import() { super(true); }
	}

	public static final class Export extends ProjectArchiveWizard {
		public Export() { super(false); }
	}

	private final boolean importing;
	private IWorkbench workbench;
	private IProject selected;
	private Text archive;
	private Combo projects;

	protected ProjectArchiveWizard(final boolean importing) {
		this.importing = importing;
		setWindowTitle(importing ? "Import GAMA project" : "Export GAMA project");
	}

	@Override public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		this.workbench = workbench;
		final IResource resource = Adapters.adapt(selection.getFirstElement(), IResource.class);
		if (resource != null) selected = resource.getProject();
	}

	@Override public void addPages() {
		addPage(new WizardPage("projectArchive") {
			@Override public void createControl(final Composite parent) {
				setTitle(importing ? "Import GAMA project" : "Export GAMA project");
				setDescription(importing ? "Choose a .gamp or ZIP archive. Import options follow."
						: "Choose the whole project to save. No file selection is needed.");
				final var content = new Composite(parent, SWT.NONE);
				content.setLayout(new GridLayout(2, false));
				if (importing) {
					archive = new Text(content, SWT.BORDER);
					archive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
					final var browse = new Button(content, SWT.PUSH);
					browse.setText("Browse...");
					browse.addListener(SWT.Selection, e -> {
						final var dialog = new FileDialog(getShell(), SWT.OPEN);
						dialog.setFilterExtensions(new String[] { "*.gamp;*.zip", "*.*" });
						final String file = dialog.open();
						if (file != null) archive.setText(file);
					});
					archive.addModifyListener(e -> setPageComplete(!archive.getText().isBlank()));
					setPageComplete(false);
				} else {
					projects = new Combo(content, SWT.READ_ONLY);
					for (final var project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
						if (project.isAccessible()) projects.add(project.getName());
					if (selected != null) projects.setText(selected.getName());
					if (projects.getSelectionIndex() < 0 && projects.getItemCount() > 0) projects.select(0);
					setPageComplete(projects.getSelectionIndex() >= 0);
				}
				setControl(content);
			}
		});
	}

	@Override public boolean performFinish() {
		if (importing) {
			try { return ImportProject.open(java.nio.file.Path.of(archive.getText().trim()), ""); }
			catch (final java.nio.file.InvalidPathException e) {
				((WizardPage) getContainer().getCurrentPage()).setErrorMessage(e.getMessage());
				return false;
			}
		}
		return ExportProject.export(workbench.getActiveWorkbenchWindow(),
				ResourcesPlugin.getWorkspace().getRoot().getProject(projects.getText()));
	}
}
