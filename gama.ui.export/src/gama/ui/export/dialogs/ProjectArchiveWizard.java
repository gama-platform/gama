package gama.ui.export.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import gama.ui.export.commands.ExportProject;

/** Bridges Eclipse's generic Export list to the project export dialog. */
public class ProjectArchiveWizard extends Wizard implements IExportWizard {

	public static final class Export extends ProjectArchiveWizard {
		public Export() {}
	}

	private IWorkbench workbench;
	private IProject selected;
	private Combo projects;

	protected ProjectArchiveWizard() {
		setWindowTitle("Export GAMA project");
	}

	@Override public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		this.workbench = workbench;
		final IResource resource = Adapters.adapt(selection.getFirstElement(), IResource.class);
		if (resource != null) selected = resource.getProject();
	}

	@Override public void addPages() {
		addPage(new WizardPage("projectArchive") {
			@Override public void createControl(final Composite parent) {
				setTitle("Export GAMA project");
				setDescription("Choose the whole project to save. No file selection is needed.");
				final var content = new Composite(parent, SWT.NONE);
				content.setLayout(new GridLayout(2, false));
				projects = new Combo(content, SWT.READ_ONLY);
				for (final var project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
					if (project.isAccessible()) projects.add(project.getName());
				if (selected != null) projects.setText(selected.getName());
				if (projects.getSelectionIndex() < 0 && projects.getItemCount() > 0) projects.select(0);
				setPageComplete(projects.getSelectionIndex() >= 0);
				setControl(content);
			}
		});
	}

	@Override public boolean performFinish() {
		return ExportProject.export(workbench.getActiveWorkbenchWindow(),
				ResourcesPlugin.getWorkspace().getRoot().getProject(projects.getText()));
	}
}
