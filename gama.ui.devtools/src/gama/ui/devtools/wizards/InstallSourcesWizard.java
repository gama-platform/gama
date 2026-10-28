package gama.ui.devtools.wizards;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import gama.ui.devtools.install.CommunityCatalog;
import gama.ui.devtools.install.InstallModel;
import gama.ui.devtools.install.InstallModel.Kind;
import gama.ui.devtools.install.InstallModel.Project;
import gama.ui.devtools.install.InstallModel.RemoteProject;
import gama.ui.devtools.install.InstallModel.Repository;
import gama.ui.devtools.install.RepositoryCheckout;
import gama.ui.devtools.install.WorkspaceSetup;
import gama.ui.devtools.util.Notifier;
import gama.ui.devtools.util.ResourceUtils;

/** Source checkout, project selection and opt-in workspace configuration. */
public class InstallSourcesWizard extends Wizard {

	private final Kind kind;
	private SourcePage source;
	private ProjectsPage projects;
	private OptionsPage options;
	private String prepared;
	private Map<String, List<String>> definitions;

	public InstallSourcesWizard(final Kind kind) {
		this.kind = kind;
		setWindowTitle("Install " + kind.label);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		source = new SourcePage();
		projects = new ProjectsPage();
		options = new OptionsPage();
		addPage(source);
		addPage(projects);
		addPage(options);
		try (var input = WorkspaceSetup.resource(InstallModel.PROJECT_SETS)) {
			definitions = InstallModel.workingSets(input);
		} catch (Exception e) {
			ResourceUtils.log(e);
			source.setErrorMessage("Cannot read bundled working sets: " + e.getMessage());
			source.setPageComplete(false);
		}
	}

	@FunctionalInterface
	private interface Operation {
		void run(IProgressMonitor monitor) throws Exception;
	}

	private boolean run(final WizardPage page, final Operation operation) {
		page.setErrorMessage(null);
		page.setMessage(null);
		try {
			getContainer().run(true, true, monitor -> {
				try {
					operation.run(monitor);
				} catch (OperationCanceledException e) {
					throw new InterruptedException();
				} catch (InterruptedException e) {
					throw e;
				} catch (Exception e) {
					if (monitor.isCanceled()) throw new InterruptedException();
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			});
			return true;
		} catch (InvocationTargetException e) {
			ResourceUtils.log(e.getCause());
			page.setErrorMessage(e.getCause().getMessage()
					+ " Completed checkouts/imports are kept; correct the problem and retry.");
		} catch (InterruptedException e) {
			page.setMessage("Cancelled. Completed checkouts/imports are kept.", WizardPage.WARNING);
		}
		return false;
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		if (page == source) {
			if (definitions == null) return null;
			Path directory = Path.of(source.location.getText().strip()).toAbsolutePath().normalize();
			String branch = source.branch.getText().strip();
			String url = source.url == null ? "" : source.url.getText().strip();
			List<RemoteProject> selected = source.catalog == null ? List.of()
					: Arrays.stream(source.catalog.getCheckedElements()).map(RemoteProject.class::cast).toList();
			String key = directory + "\n" + branch + "\n" + url + "\n"
					+ selected.stream().map(p -> p.repository().toString() + ":" + p.path()).toList();
			if (!key.equals(prepared)) {
				List<Project> available = new ArrayList<>();
				if (!run(source, monitor -> {
					if (kind == Kind.COMMUNITY) {
						Map<Repository, Set<Path>> repositories = new LinkedHashMap<>();
						for (RemoteProject project : selected) {
							repositories.computeIfAbsent(project.repository(), r -> new LinkedHashSet<>())
									.add(directory.resolve(project.repository().name()).resolve(project.path()).normalize());
						}
						SubMonitor sub = SubMonitor.convert(monitor, "Checking out community projects", repositories.size());
						for (var entry : repositories.entrySet()) {
							if (sub.isCanceled()) throw new OperationCanceledException();
							Path checkout = directory.resolve(entry.getKey().name());
							RepositoryCheckout.prepare(entry.getKey(), checkout, sub.split(1));
							for (Project project : InstallModel.projects(checkout, entry.getKey(), kind)) {
								if (entry.getValue().contains(project.directory())) {
									available.add(new Project(project.name(), project.directory(), project.repository(), true));
								}
							}
							for (Path expected : entry.getValue()) {
								if (available.stream().noneMatch(p -> p.directory().equals(expected))) {
									throw new java.io.IOException("The selected project no longer exists: " + expected
											+ ". Reload the GitHub project list.");
								}
							}
						}
					} else {
						Repository repository = new Repository(kind.folder, url, branch);
						RepositoryCheckout.prepare(repository, directory, monitor);
						available.addAll(InstallModel.projects(directory, repository, kind));
					}
				})) return null;
				if (available.isEmpty()) {
					source.setErrorMessage("No Eclipse projects (.project files) found in the selected checkout.");
					return null;
				}
				projects.setProjects(available);
				prepared = key;
			}
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == options && options.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		if (!PlatformUI.getWorkbench().saveAllEditors(true)) return false;
		List<Project> selected = Arrays.stream(projects.viewer.getCheckedElements()).map(Project.class::cast).toList();
		Set<String> sets = new LinkedHashSet<>();
		if (options.sets.getSelection()) {
			for (Object item : options.workingSets.getCheckedElements()) sets.add((String) item);
		}
		boolean eclipse = options.eclipse.getSelection(), cleanup = options.cleanup.getSelection(),
				formatter = options.formatter.getSelection(), closeParent = options.closeParent.getSelection();
		if (!run(options, monitor -> {
			var workspace = ResourcesPlugin.getWorkspace();
			workspace.run(progress -> {
				SubMonitor sub = SubMonitor.convert(progress, "Importing projects and configuring workspace", selected.size() + 2);
				var description = workspace.getDescription();
				boolean autoBuild = description.isAutoBuilding();
				try {
					description.setAutoBuilding(false);
					workspace.setDescription(description);
					for (Project project : selected) {
						if (sub.isCanceled()) throw new OperationCanceledException();
						WorkspaceSetup.importProject(project, sub.split(1));
					}
					if (sub.isCanceled()) throw new OperationCanceledException();
					WorkspaceSetup.preferences(eclipse, cleanup, formatter);
					sub.worked(1);
					var parent = workspace.getRoot().getProject("gama.parent");
					if (closeParent && parent.isOpen()) parent.close(sub.split(1));
				} catch (CoreException | OperationCanceledException e) {
					throw e;
				} catch (Exception e) {
					throw new CoreException(ResourceUtils.error(e.getMessage(), e));
				} finally {
					var restore = workspace.getDescription();
					restore.setAutoBuilding(autoBuild);
					workspace.setDescription(restore);
				}
			}, workspace.getRoot(), 0, monitor);
		})) return false;
		try {
			List<Project> community = kind == Kind.COMMUNITY ? projects.available : List.of();
			var workingSets = WorkspaceSetup.workingSets(definitions, sets, community);
			if (options.sets.getSelection() && options.explorers.getSelection() && !workingSets.isEmpty()) {
				WorkspaceSetup.organizeExplorers(workingSets);
			}
		} catch (Exception e) {
			ResourceUtils.log(e);
			options.setErrorMessage("Projects imported, but working-set setup failed: " + e.getMessage());
			return false;
		}
		new WorkspaceJob("Refresh, clean and build installed GAMA workspace") {
			{
				setRule(ResourcesPlugin.getWorkspace().getRoot());
				setUser(true);
			}

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				try {
					var workspace = ResourcesPlugin.getWorkspace();
					SubMonitor sub = SubMonitor.convert(monitor, getName(), 100);
					workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, sub.split(10));
					workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, sub.split(30));
					workspace.build(IncrementalProjectBuilder.FULL_BUILD, sub.split(60));
					Notifier.show("GAMA source installation finished",
							selected.size() + " project(s) imported. Workspace refreshed and built. "
									+ "Check the Problems view for compilation errors.");
					return Status.OK_STATUS;
				} catch (OperationCanceledException e) {
					return Status.CANCEL_STATUS;
				}
			}
		}.schedule();
		return true;
	}

	private final class SourcePage extends WizardPage {
		Text url, branch, location;
		CheckboxTableViewer catalog;
		Label skipped;

		SourcePage() {
			super("repositories");
			setTitle("Install " + kind.label);
			setDescription("Choose a checkout location and branch. Next clones or fast-forwards clean repositories; "
					+ "existing local changes are never discarded.");
			setPageComplete(false);
		}

		@Override
		public boolean canFlipToNextPage() {
			// JFace probes this while updating buttons; only an explicit Next may check out repositories.
			return isPageComplete();
		}

		@Override
		public void createControl(final Composite parent) {
			Composite body = body(parent, 3);
			if (kind != Kind.COMMUNITY) url = text(body, "Repository URL:", kind.url);
			branch = text(body, "Branch:", kind.branch);
			label(body, kind == Kind.COMMUNITY ? "Checkout parent directory:" : "Repository directory:");
			location = new Text(body, SWT.BORDER);
			location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			location.setText(defaultLocation());
			Button browse = new Button(body, SWT.PUSH);
			browse.setText("Browse...");
			browse.addListener(SWT.Selection, e -> {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setFilterPath(location.getText());
				String path = dialog.open();
				if (path != null) location.setText(path);
			});
			if (kind == Kind.COMMUNITY) {
				note(body, "Projects from https://github.com/gama-platform-plugins.\n"
						+ "Select projects to check out. Only their repositories will be cloned.");
				Button load = check(body, "Load / reload GitHub projects", false, SWT.PUSH);
				load.addListener(SWT.Selection, e -> load());
				catalog = table(body);
				catalog.addCheckStateListener(e -> validate());
				selectButtons(body, catalog, this::validate);
				Text repositoryUrl = text(body, "Selected repository URL:", "");
				catalog.addSelectionChangedListener(e -> {
					Object selected = e.getStructuredSelection().getFirstElement();
					repositoryUrl.setText(selected instanceof RemoteProject project ? project.repository().url() : "");
				});
				Button applyUrl = check(body, "Use this URL for the selected repository", false, SWT.PUSH);
				applyUrl.addListener(SWT.Selection, e -> {
					Object selection = catalog.getStructuredSelection().getFirstElement();
					if (!(selection instanceof RemoteProject project)) return;
					try {
						String newUrl = repositoryUrl.getText().strip();
						var uri = new org.eclipse.jgit.transport.URIish(newUrl);
						if (!"https".equals(uri.getScheme()) || uri.getHost() == null) {
							setErrorMessage("Use an HTTPS Git repository URL.");
							return;
						}
						Set<Object> checked = new LinkedHashSet<>(Arrays.asList(catalog.getCheckedElements()));
						List<RemoteProject> updated = new ArrayList<>(), selectedProjects = new ArrayList<>();
						for (int i = 0; i < catalog.getTable().getItemCount(); i++) {
							RemoteProject old = (RemoteProject) catalog.getElementAt(i);
							RemoteProject replacement = old.repository().equals(project.repository())
									? new RemoteProject(new Repository(old.repository().name(), newUrl, old.repository().branch()),
											old.path()) : old;
							updated.add(replacement);
							if (checked.contains(old)) selectedProjects.add(replacement);
						}
						catalog.setInput(updated);
						catalog.setCheckedElements(selectedProjects.toArray());
						validate();
					} catch (java.net.URISyntaxException exception) {
						setErrorMessage(exception.getMessage());
					}
				});
				skipped = note(body, "");
				branch.addModifyListener(e -> {
					catalog.setInput(List.of());
					skipped.setText("Branch changed: reload the GitHub project list.");
					validate();
				});
			}
			note(body, "Checkout happens on Next and is kept even if you cancel the wizard. "
					+ "Dirty or diverged repositories must be updated manually or installed in another directory.");
			location.addModifyListener(e -> validate());
			branch.addModifyListener(e -> validate());
			if (url != null) url.addModifyListener(e -> validate());
			setControl(body);
			validate();
		}

		private void load() {
			String ref = branch.getText().strip();
			if (ref.isEmpty()) {
				setErrorMessage("Enter a branch first.");
				return;
			}
			CommunityCatalog.Catalog[] result = new CommunityCatalog.Catalog[1];
			if (run(this, monitor -> result[0] = new CommunityCatalog().load(ref, monitor))) {
				prepared = null;
				catalog.setInput(result[0].projects());
				skipped.setText(result[0].unavailable().isEmpty() ? ""
						: "Not offered: " + String.join(", ", result[0].unavailable()));
				skipped.getParent().layout(true, true);
				validate();
			}
		}

		private String defaultLocation() {
			var root = ResourcesPlugin.getWorkspace().getRoot();
			var core = root.getProject("gama.core");
			Path parent = root.getLocation().toFile().toPath().getParent().resolve("git");
			if (core.exists() && core.getLocation() != null) {
				Path gama = core.getLocation().toFile().toPath().getParent();
				if (kind == Kind.CORE) return gama.toString();
				parent = gama.getParent();
			}
			return (kind == Kind.COMMUNITY ? parent : parent.resolve(kind.folder)).toString();
		}

		private void validate() {
			setErrorMessage(null);
			setPageComplete(false);
			if (definitions == null) {
				setErrorMessage("Bundled working sets could not be loaded. See the Error Log.");
				return;
			}
			if (branch.getText().isBlank() || location.getText().isBlank()
					|| url != null && url.getText().isBlank()) return;
			if (!org.eclipse.jgit.lib.Repository.isValidRefName("refs/heads/" + branch.getText().strip())) {
				setErrorMessage("Enter a valid Git branch name.");
				return;
			}
			try {
				Path directory = Path.of(location.getText().strip());
				if (!directory.isAbsolute()) {
					setErrorMessage("Choose an absolute checkout directory.");
					return;
				}
				if (Files.exists(directory) && !Files.isDirectory(directory)) {
					setErrorMessage("The checkout location is not a directory.");
					return;
				}
				if (url != null) {
					var uri = new org.eclipse.jgit.transport.URIish(url.getText().strip());
					if (uri.getHost() == null || !"https".equals(uri.getScheme())) {
						setErrorMessage("Use an HTTPS Git repository URL.");
						return;
					}
				}
			} catch (java.nio.file.InvalidPathException | java.net.URISyntaxException e) {
				setErrorMessage(e.getMessage());
				return;
			}
			setPageComplete(catalog == null || catalog.getCheckedElements().length > 0);
		}
	}

	private final class ProjectsPage extends WizardPage {
		CheckboxTableViewer viewer;
		List<Project> available = List.of();

		ProjectsPage() {
			super("projects");
			setTitle("Import projects");
			setDescription("Choose projects to import. Existing workspace projects cannot be selected. "
					+ "Core Maven modules and graphical editor projects are preselected.");
		}

		@Override
		public void createControl(final Composite parent) {
			Composite body = body(parent, 3);
			viewer = table(body);
			viewer.setLabelProvider(new LabelProvider() {
				@Override public String getText(final Object element) {
					Project project = (Project) element;
					return project.name() + "  -  " + project.repository().name()
							+ (exists(project) ? " (already in workspace)" : "");
				}
			});
			viewer.addCheckStateListener(e -> {
				if (exists((Project) e.getElement())) viewer.setChecked(e.getElement(), false);
				validate();
			});
			selectButtons(body, viewer, () -> {
				for (Project project : available) if (exists(project)) viewer.setChecked(project, false);
				validate();
			});
			note(body, "Projects remain in their Git checkout; they are not copied into the workspace. "
					+ "Selecting no new projects still lets you apply workspace settings.");
			setControl(body);
			populate();
		}

		void setProjects(final List<Project> projects) {
			available = List.copyOf(projects);
			if (viewer != null) populate();
		}

		private boolean exists(final Project project) {
			if (!ResourcesPlugin.getWorkspace().validateName(project.name(), IResource.PROJECT).isOK()) return false;
			return ResourcesPlugin.getWorkspace().getRoot().getProject(project.name()).exists();
		}

		private void populate() {
			viewer.setInput(available);
			viewer.setCheckedElements(available.stream().filter(p -> p.recommended() && !exists(p)).toArray());
			viewer.setGrayedElements(available.stream().filter(this::exists).toArray());
			validate();
		}

		private void validate() {
			Set<String> names = new LinkedHashSet<>();
			setErrorMessage(null);
			for (Object element : viewer.getCheckedElements()) {
				Project project = (Project) element;
				if (!ResourcesPlugin.getWorkspace().validateName(project.name(), IResource.PROJECT).isOK()) {
					setErrorMessage("Invalid Eclipse project name: " + project.name());
					break;
				}
				if (!names.add(project.name())) {
					setErrorMessage("Multiple selected projects are named " + project.name() + ". Select only one.");
					break;
				}
			}
			setPageComplete(getErrorMessage() == null);
		}
	}

	private final class OptionsPage extends WizardPage {
		Button sets, explorers, eclipse, cleanup, formatter, closeParent;
		CheckboxTableViewer workingSets;

		OptionsPage() {
			super("workspace");
			setTitle("Configure the workspace");
			setDescription("Choose optional working sets and preferences, then finish to import, refresh, clean and build.");
		}

		@Override
		public void setVisible(final boolean visible) {
			if (visible && workingSets != null) workingSets.refresh();
			super.setVisible(visible);
		}

		@Override
		public void createControl(final Composite parent) {
			Composite body = body(parent, 3);
			sets = check(body, "Organize projects in GAMA working sets", true, SWT.CHECK);
			workingSets = table(body);
			Map<String, List<String>> availableDefinitions = definitions == null ? Map.of() : definitions;
			workingSets.setLabelProvider(new LabelProvider() {
				@Override public String getText(final Object element) {
					String name = (String) element;
					Set<String> imports = new LinkedHashSet<>();
					for (Object item : projects.viewer.getCheckedElements()) imports.add(((Project) item).name());
					long present = availableDefinitions.get(name).stream()
							.filter(p -> ResourcesPlugin.getWorkspace().getRoot().getProject(p).exists()
									|| imports.contains(p)).count();
					if (InstallModel.COMMUNITY_SET.equals(name) && kind == Kind.COMMUNITY) {
						present = projects.available.stream().filter(p -> projects.exists(p) || imports.contains(p.name())).count();
					}
					return name + " (" + present + " available; missing projects are ignored)";
				}
			});
			workingSets.setInput(new ArrayList<>(availableDefinitions.keySet()));
			workingSets.setCheckedElements(availableDefinitions.keySet().stream().filter(name -> switch (kind) {
				case CORE -> !name.endsWith("COMMUNITY") && !name.endsWith("GRAPHICAL MODELLING");
				case GRAPHICAL -> name.endsWith("GRAPHICAL MODELLING");
				case COMMUNITY -> name.equals(InstallModel.COMMUNITY_SET);
			}).toArray());
			explorers = check(body, "Show working sets as roots in Project Explorer and Package Explorer", true, SWT.CHECK);
			sets.addListener(SWT.Selection, e -> {
				workingSets.getControl().setEnabled(sets.getSelection());
				explorers.setEnabled(sets.getSelection());
			});
			note(body, "Existing working sets and their members are preserved; available projects are added. "
					+ "Missing projects never prevent working-set creation.");
			note(body, "Apply bundled workspace preferences (overwrites matching settings, not unrelated profiles):");
			eclipse = check(body, "Eclipse Preferences for Gama.epf", false, SWT.CHECK);
			cleanup = check(body, "Cleanup rules for Gama.xml", false, SWT.CHECK);
			formatter = check(body, "Code formatter rules for Gama.xml", false, SWT.CHECK);
			note(body, "The Eclipse preference export includes Java compiler, formatter and cleanup settings. "
					+ "Project-specific settings still take precedence. No source files are formatted or cleaned up.");
			closeParent = check(body, "Turn off Maven compilation by closing gama.parent (recommended)", true, SWT.CHECK);
			note(body, "Finish saves editors, imports projects, applies your choices and schedules a refresh, "
					+ "clean build and full build of the whole workspace. Resolve the GAMA target platform "
					+ "and configure the required JDK separately if needed.");
			setControl(body);
		}
	}

	private static Composite body(final Composite parent, final int columns) {
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(columns, false));
		return body;
	}

	private static void label(final Composite parent, final String text) {
		new Label(parent, SWT.NONE).setText(text);
	}

	private static Text text(final Composite parent, final String title, final String value) {
		label(parent, title);
		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		text.setText(value);
		return text;
	}

	private static Label note(final Composite parent, final String text) {
		Label label = new Label(parent, SWT.WRAP);
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1);
		data.widthHint = 650;
		label.setLayoutData(data);
		label.setText(text);
		return label;
	}

	private static Button check(final Composite parent, final String text, final boolean selected, final int style) {
		Button button = new Button(parent, style);
		button.setText(text);
		button.setSelection(selected);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		return button;
	}

	private static CheckboxTableViewer table(final Composite parent) {
		CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		data.heightHint = 200;
		data.widthHint = 650;
		viewer.getControl().setLayoutData(data);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider());
		return viewer;
	}

	private static void selectButtons(final Composite parent, final CheckboxTableViewer viewer, final Runnable changed) {
		Composite buttons = body(parent, 2);
		buttons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		for (boolean selected : new boolean[] { true, false }) {
			Button button = new Button(buttons, SWT.PUSH);
			button.setText(selected ? "Select all" : "Deselect all");
			button.addListener(SWT.Selection, e -> {
				viewer.setAllChecked(selected);
				changed.run();
			});
		}
	}
}
