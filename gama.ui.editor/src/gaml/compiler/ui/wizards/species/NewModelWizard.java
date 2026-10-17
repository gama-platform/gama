package gaml.compiler.ui.wizards.species;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.xtext.parser.IParser;

import com.google.inject.Inject;

import gama.api.GAMA;
import gama.ui.navigator.view.contents.ResourceManager;
import gama.ui.navigator.wizards.AbstractNewModelWizardPage;

/** New Model File wizard hosting the shared model form; replaces the former template-based page. */
public class NewModelWizard extends Wizard implements INewWizard {

	@Inject private IParser parser;
	private IStructuredSelection selection;
	private ModelPage page;

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
		selection = currentSelection;
		setWindowTitle("New Model");
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new ModelPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		SpeciesWizard form = page.form;
		if (form == null || !form.accept()) return false;
		ModelLocation location = form.getLocation();
		String content = ModelSource.newModel(form.getResult(), form.getDocumentation(), "\n");
		IFile[] created = new IFile[1];
		try {
			getContainer().run(false, false, monitor -> {
				try {
					IContainer folder = createContainer(root(), new Path(location.container()));
					IFile file = folder.getFile(new Path(location.fileName()));
					file.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), true, monitor);
					created[0] = file;
				} catch (CoreException error) {
					throw new InvocationTargetException(error);
				}
			});
		} catch (InvocationTargetException error) {
			page.setErrorMessage(error.getTargetException().getMessage());
			return false;
		} catch (InterruptedException error) {
			return false;
		}
		ResourceManager.getInstance().reveal(created[0]);
		GAMA.getGui().getModelsManager().editModel(created[0]);
		return true;
	}

	private static IWorkspaceRoot root() { return GAMA.getWorkspaceManager().getRoot(); }

	private static IContainer createContainer(final IWorkspaceRoot root, final IPath path) throws CoreException {
		IContainer container = root.getProject(path.segment(0));
		for (int i = 1; i < path.segmentCount(); i++) {
			IFolder folder = container.getFolder(new Path(path.segment(i)));
			if (!folder.exists()) folder.create(true, true, null);
			container = folder;
		}
		return container;
	}

	/** Initial container: the selected folder, or the models folder of a selected project. */
	private String initialContainer() {
		Object element = selection == null || selection.isEmpty() ? null : selection.getFirstElement();
		IResource resource = element == null ? null : ResourceManager.getResource(element);
		if (resource == null) return "";
		if (resource instanceof IProject project) return project.getFolder("models").getFullPath().toString();
		if (!(resource instanceof IContainer)) resource = resource.getParent();
		return resource.getFullPath().toString();
	}

	private static String initialFileName(final String container) {
		IResource resource = container.isBlank() ? null : root().findMember(new Path(container));
		for (int i = 0;; i++) {
			String name = "New Model" + (i == 0 ? "" : String.valueOf(i)) + ".gaml";
			if (!(resource instanceof IContainer folder) || !folder.getFile(new Path(name)).exists()) return name;
		}
	}

	static String locationProblem(final IWorkspaceRoot root, final ModelLocation location) {
		IPath path = new Path(location.container());
		if (!path.isValidPath(location.container()) || path.segmentCount() == 0)
			return "Enter a valid workspace folder path.";
		IProject project = root.getProject(path.segment(0));
		if (!project.exists()) return "Project '" + path.segment(0) + "' does not exist.";
		if (!project.isOpen()) return "Project '" + path.segment(0) + "' is closed.";
		IResource existing = root.findMember(path);
		if (existing != null && !(existing instanceof IContainer)) return location.container() + " is not a folder.";
		if (!new Path(location.fileName()).isValidSegment(location.fileName())) return "Enter a valid file name.";
		if (root.getFile(path.append(location.fileName())).exists()) return "A file with the same name already exists.";
		return null;
	}

	private final class ModelPage extends WizardPage {
		private SpeciesWizard form;

		ModelPage() {
			super("model");
			setTitle("New model");
			setDescription("Define the model and its global attributes.");
			setPageComplete(false);
		}

		@Override
		public void createControl(final Composite parent) {
			Composite control = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout(1, false);
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			control.setLayout(layout);
			String container = initialContainer();
			String author = AbstractNewModelWizardPage.getComputerFullName();
			ModelSource source = ModelSource.creation(parser);
			form = new SpeciesWizard(getShell(), SpeciesDraft.empty(), source::validationError, true, source.context(),
					new ModelLocation(container, initialFileName(container)), true,
					new ModelDocumentation(author == null ? "" : author, "", "")) {
				@Override
				protected String browseContainer(final String current) {
					var dialog = new ContainerSelectionDialog(getShell(), root(), false, "Select a project or a folder");
					if (dialog.open() != Window.OK || dialog.getResult().length != 1) return null;
					return dialog.getResult()[0].toString();
				}

				@Override
				protected String locationError(final ModelLocation proposed) { return locationProblem(root(), proposed); }
			};
			form.createForm(control, new SpeciesWizard.FormHost() {
				@Override
				public void setTitle(final String title) { ModelPage.this.setTitle(title); }

				@Override
				public void setMessage(final String message) { ModelPage.this.setDescription(message); }

				@Override
				public void setErrorMessage(final String error) { ModelPage.this.setErrorMessage(error); }

				@Override
				public void setComplete(final boolean complete) { setPageComplete(complete); }

				@Override
				public void relayout() {
					var shell = getShell();
					var size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
					shell.setSize(Math.max(size.x, shell.getSize().x), size.y);
				}
			}).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			setControl(control);
		}
	}
}
