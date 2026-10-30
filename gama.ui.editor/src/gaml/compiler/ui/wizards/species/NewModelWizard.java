package gaml.compiler.ui.wizards.species;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;
import javax.lang.model.SourceVersion;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.osgi.framework.Bundle;
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
		setWindowTitle("New GAML file");
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new ModelPage();
		addPage(page);
	}

	@Override
	public boolean performCancel() {
		if (page != null && page.form != null) page.form.clearResult();
		return true;
	}

	@Override
	public boolean performFinish() {
		if (page.isExperimentSelected()) return createExperiment();
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

	private boolean createExperiment() {
		if (!page.experimentPage.isValid()) return false;
		ExperimentPage experimentPage = page.experimentPage;
		ModelLocation location = experimentPage.location();
		String content;
		try {
			content = experimentContent(location, experimentPage.modelPath(), experimentPage.experimentName(),
					experimentPage.author(), experimentPage.experimentType());
		} catch (IOException error) {
			experimentPage.setErrorMessage(error.getMessage());
			return false;
		}
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
			experimentPage.setErrorMessage(error.getTargetException().getMessage());
			return false;
		} catch (InterruptedException error) {
			return false;
		}
		ResourceManager.getInstance().reveal(created[0]);
		GAMA.getGui().getModelsManager().editModel(created[0]);
		return true;
	}

	private static String experimentContent(final ModelLocation location, final String modelPath, final String title,
			final String author, final String type) throws IOException {
		Bundle navigator = Platform.getBundle("gama.ui.navigator");
		if (navigator == null) throw new IOException("The GAMA navigator templates are unavailable.");
		var templateUrl = navigator.getEntry("/templates/experiment.template.resource");
		if (templateUrl == null) throw new IOException("The experiment template is unavailable.");
		String template;
		try (InputStream input = templateUrl.openStream(); Scanner scanner = new Scanner(input, StandardCharsets.UTF_8)) {
			template = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
		}
		IResource model = modelPath.isBlank() ? null : root().findMember(new Path(modelPath));
		String relativeModel = model instanceof IFile
				? "'" + model.getFullPath().makeRelativeTo(new Path(location.container())) + "'" : "";
		String modelFacet = relativeModel.isEmpty() ? "" : "model:" + relativeModel;
		return template.replace("$TITLE$", title).replace("$AUTHOR$", author).replace("$DESC$", "")
				.replace("model:$MODEL$", modelFacet).replace("$MODEL$", relativeModel)
				.replace("$TYPE$", type.toLowerCase(Locale.ROOT));
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

	private String initialFileName(final String container, final String stem) {
		IResource resource = container.isBlank() ? null : root().findMember(new Path(container));
		for (int i = 0;; i++) {
			String name = stem + (i == 0 ? "" : String.valueOf(i)) + ".gaml";
			if (!(resource instanceof IContainer folder) || !folder.getFile(new Path(name)).exists()) return name;
		}
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
		private ExperimentPage experimentPage;
		private boolean modelComplete;
		private String modelError;
		private TabFolder tabs;

		ModelPage() {
			super("model");
			setTitle("New GAML file");
			setDescription("Create a model or a standalone experiment.");
			setPageComplete(false);
		}

		@Override
		public void createControl(final Composite parent) {
			tabs = new TabFolder(parent, SWT.TOP);
			tabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			TabItem modelTab = new TabItem(tabs, SWT.NONE);
			modelTab.setText("Model");
			Composite modelControl = new Composite(tabs, SWT.NONE);
			GridLayout layout = new GridLayout(1, false);
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			modelControl.setLayout(layout);
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
			form.setCreationMode(true);
			form.createForm(modelControl, new SpeciesWizard.FormHost() {
				@Override
				public void setTitle(final String title) { ModelPage.this.setTitle(title); }

				@Override
				public void setMessage(final String message) {
					ModelPage.this.setDescription("Model — " + message);
				}

				@Override
				public void setErrorMessage(final String error) {
					modelError = error;
					refreshCompleteness();
				}

				@Override
				public void setComplete(final boolean complete) {
					modelComplete = complete;
					refreshCompleteness();
				}

				@Override
				public void relayout() {
					var shell = getShell();
					var size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
					shell.setSize(Math.max(size.x, shell.getSize().x), size.y);
				}
			}).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			modelTab.setControl(modelControl);

			TabItem experimentTab = new TabItem(tabs, SWT.NONE);
			experimentTab.setText("Standalone experiment");
			experimentPage = new ExperimentPage(this);
			experimentTab.setControl(experimentPage.createControl(tabs));
			tabs.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent event) { refreshCompleteness(); }
			});
			tabs.setSelection(modelTab);
			setControl(tabs);
			refreshCompleteness();
		}

		private boolean isExperimentSelected() {
			return tabs != null && tabs.getSelectionIndex() == 1;
		}

		private void refreshCompleteness() {
			boolean experimentSelected = isExperimentSelected();
			setErrorMessage(experimentSelected
					? experimentPage == null ? null : experimentPage.errorMessage()
					: modelError);
			setPageComplete(experimentSelected
					? experimentPage != null && experimentPage.isValid() : modelComplete);
		}
	}

	private final class ExperimentPage {

			private final ModelPage owner;
			private Text containerText;
			private Text fileText;
			private Text authorText;
			private Text nameText;
			private Text modelText;
			private String type = "GUI";
			private String errorMessage;

			ExperimentPage(final ModelPage owner) { this.owner = owner; }

			private Composite createControl(final Composite parent) {
				Composite control = new Composite(parent, SWT.NONE);
				GridLayout layout = new GridLayout(3, false);
				layout.marginWidth = 0;
				control.setLayout(layout);
				String container = initialContainer();
				addLabel(control, "Container:");
				containerText = addText(control, container, 1);
				Button browseContainer = new Button(control, SWT.PUSH);
				browseContainer.setText("Browse...");
				browseContainer.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent event) {
						var dialog = new ContainerSelectionDialog(getShell(), root(), false, "Select a project or a folder");
						if (dialog.open() == Window.OK && dialog.getResult().length == 1)
							containerText.setText(dialog.getResult()[0].toString());
					}
				});
				addLabel(control, "File name:");
				fileText = addText(control, initialFileName(container, "New Experiment"), 2);
				addLabel(control, "Experiment name:");
				nameText = addText(control, "New Experiment", 2);
				fileText.addModifyListener(event -> {
					String fileName = fileText.getText();
					int extension = fileName.lastIndexOf(".gaml");
					if (extension > 0) nameText.setText(fileName.substring(0, extension).replaceAll("[^\\p{Alnum}]", ""));
				});
				addLabel(control, "Author:");
				authorText = addText(control, AbstractNewModelWizardPage.getComputerFullName(), 2);
				addLabel(control, "Model to import:");
				modelText = addText(control, "", 1);
				modelText.setEditable(false);
				Button browseModel = new Button(control, SWT.PUSH);
				browseModel.setText("Browse...");
				browseModel.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent event) {
						FilteredResourcesSelectionDialog dialog =
								new FilteredResourcesSelectionDialog(getShell(), false, root(), IResource.FILE);
						dialog.setInitialPattern("*.gaml");
						dialog.setTitle("Choose a GAML model");
						if (dialog.open() == Window.OK && dialog.getResult().length == 1)
							modelText.setText(((IResource) dialog.getResult()[0]).getFullPath().toString());
					}
				});
				addLabel(control, "Type:");
				Composite types = new Composite(control, SWT.NONE);
				types.setLayout(new GridLayout(2, false));
				types.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
				Button gui = new Button(types, SWT.RADIO);
				gui.setText("GUI");
				gui.setSelection(true);
				Button headless = new Button(types, SWT.RADIO);
				headless.setText("Headless");
				gui.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent event) {
						if (gui.getSelection()) type = "GUI";
					}
				});
				headless.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent event) {
						if (headless.getSelection()) type = "Headless";
					}
				});
				validate();
				return control;
			}

			private void addLabel(final Composite parent, final String text) {
				Label label = new Label(parent, SWT.RIGHT);
				label.setText(text);
				label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			}

			private Text addText(final Composite parent, final String value, final int span) {
				Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
				text.setText(value == null ? "" : value);
				text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, span, 1));
				text.addModifyListener(event -> validate());
				return text;
			}

			private void validate() {
				if (containerText == null) return;
				String container = containerText.getText().trim();
				IPath path = new Path(container);
				if (container.isEmpty() || !path.isValidPath(container) || path.segmentCount() == 0) {
					setErrorMessage("Enter a valid workspace folder path.");
				} else {
					IProject project = root().getProject(path.segment(0));
					IResource existing = root().findMember(path);
					if (!project.exists() || !project.isOpen()) {
						setErrorMessage("Select an open project or a folder in an open project.");
					} else if (existing != null && !(existing instanceof IContainer)) {
						setErrorMessage(container + " is not a folder.");
					} else if (fileText.getText().isBlank()
							|| !new Path(fileText.getText()).isValidSegment(fileText.getText())
							|| !fileText.getText().endsWith(".gaml")) {
						setErrorMessage("Enter a valid file name ending in .gaml.");
					} else if (root().getFile(path.append(fileText.getText())).exists()) {
						setErrorMessage("A file with the same name already exists.");
					} else if (nameText.getText().isBlank() || !SourceVersion.isName(nameText.getText())) {
						setErrorMessage("Enter a valid experiment name.");
					} else if (authorText.getText().isBlank()) {
						setErrorMessage("Enter an author name.");
					} else if (!modelText.getText().isBlank()
							&& !(root().findMember(new Path(modelText.getText())) instanceof IFile)) {
						setErrorMessage("The selected model file no longer exists.");
					} else {
						setErrorMessage(null);
					}
				}
				owner.refreshCompleteness();
			}

			private boolean isValid() { return errorMessage == null; }
			private String errorMessage() { return errorMessage; }

			private void setErrorMessage(final String message) { errorMessage = message; }

			private ModelLocation location() { return new ModelLocation(containerText.getText().trim(), fileText.getText()); }
			private String experimentName() { return nameText.getText().trim(); }
			private String author() { return authorText.getText().trim(); }
			private String modelPath() { return modelText.getText().trim(); }
			private String experimentType() { return type; }
	}
}
