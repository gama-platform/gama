package gama.ui.devtools.install;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.preferences.cleanup.CleanUpProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.FormatterProfileStore;
import org.eclipse.jdt.internal.ui.preferences.formatter.IProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.Profile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileStore;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileVersioner;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.xml.sax.InputSource;

import gama.ui.devtools.GamaDevToolsActivator;
import gama.ui.devtools.install.InstallModel.Project;

/** Eclipse adapters for import, additive working sets and bundled preference profiles. */
@SuppressWarnings("restriction")
public final class WorkspaceSetup {

	private WorkspaceSetup() {}

	public static InputStream resource(final String name) throws IOException {
		var entry = Platform.getBundle(GamaDevToolsActivator.PLUGIN_ID).getEntry("utils/" + name);
		if (entry == null) throw new IOException("Missing bundled workspace settings: " + name);
		return entry.openStream();
	}

	public static void importProject(final Project source, final IProgressMonitor monitor) throws Exception {
		var workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.loadProjectDescription(
				org.eclipse.core.runtime.IPath.fromOSString(source.directory().resolve(".project").toString()));
		IProject project = workspace.getRoot().getProject(description.getName());
		if (project.exists()) {
			if (project.getLocation() == null
					|| !project.getLocation().toFile().toPath().toRealPath().equals(source.directory().toRealPath())) {
				throw new IOException("Project " + project.getName() + " already exists at another location.");
			}
		} else {
			description.setLocation(org.eclipse.core.runtime.IPath.fromOSString(source.directory().toString()));
			project.create(description, monitor);
			project.open(monitor);
		}
		try (var repository = new org.eclipse.jgit.storage.file.FileRepositoryBuilder()
				.findGitDir(source.directory().toFile()).setMustExist(true).build()) {
			new org.eclipse.egit.core.op.ConnectProviderOperation(project, repository.getDirectory()).execute(monitor);
		}
	}

	public static List<IWorkingSet> workingSets(final Map<String, List<String>> definitions,
			final Set<String> selected, final List<Project> communityProjects) throws Exception {
		var manager = PlatformUI.getWorkbench().getWorkingSetManager();
		var root = ResourcesPlugin.getWorkspace().getRoot();
		List<IWorkingSet> result = new ArrayList<>();
		for (String name : selected) {
			Set<IAdaptable> elements = new LinkedHashSet<>();
			IWorkingSet workingSet = manager.getWorkingSet(name);
			if (workingSet != null) elements.addAll(Arrays.asList(workingSet.getElements()));
			Set<String> projects = new LinkedHashSet<>(definitions.getOrDefault(name, List.of()));
			if (InstallModel.COMMUNITY_SET.equals(name)) {
				for (Project project : communityProjects) projects.add(project.name());
			}
			for (String id : projects) {
				IProject project = root.getProject(id);
				if (!project.exists()) continue;
				boolean present = elements.stream().anyMatch(e -> project.equals(e.getAdapter(IProject.class)));
				if (!present) elements.add(project.isOpen() && project.hasNature(JavaCore.NATURE_ID)
						? JavaCore.create(project) : project);
			}
			if (workingSet == null) {
				workingSet = manager.createWorkingSet(name, elements.toArray(IAdaptable[]::new));
				workingSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
				manager.addWorkingSet(workingSet);
			} else {
				workingSet.setElements(elements.toArray(IAdaptable[]::new));
			}
			result.add(workingSet);
		}
		return result;
	}

	public static void organizeExplorers(final List<IWorkingSet> sets) throws Exception {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		var projectExplorer = (ProjectExplorer) page.showView(ProjectExplorer.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
		var manager = PlatformUI.getWorkbench().getWorkingSetManager();
		Set<IWorkingSet> active = new LinkedHashSet<>();
		Object input = projectExplorer.getCommonViewer().getInput();
		if (input instanceof IAggregateWorkingSet aggregate) active.addAll(Arrays.asList(aggregate.getComponents()));
		else if (input instanceof IWorkingSet workingSet) active.add(workingSet);
		active.addAll(sets);
		projectExplorer.setRootMode(ProjectExplorer.WORKING_SETS);
		projectExplorer.getNavigatorContentService().findStateModel(
				"org.eclipse.ui.navigator.resources.workingSets")
				.setBooleanProperty("org.eclipse.ui.navigator.resources.workingSets.showTopLevelWorkingSets", true);
		String aggregateName = "GAMA installation working sets";
		IWorkingSet saved = manager.getWorkingSet(aggregateName);
		if (saved instanceof IAggregateWorkingSet existing) {
			active.addAll(Arrays.asList(existing.getComponents()));
			manager.removeWorkingSet(existing);
		} else if (saved != null) {
			throw new IOException("A non-aggregate working set is already named " + aggregateName
					+ ". Rename it before organizing the explorers.");
		}
		IWorkingSet aggregate = manager.createAggregateWorkingSet(
				aggregateName, aggregateName, active.toArray(IWorkingSet[]::new));
		manager.addWorkingSet(aggregate);
		projectExplorer.getCommonViewer().setInput(aggregate);
		// Configure the native action group as well as the viewer so its selection survives an IDE restart.
		var state = XMLMemento.createWriteRoot("navigator");
		var actions = projectExplorer.getNavigatorActionService();
		actions.saveState(state);
		state.putString("currentWorkingSetName", aggregateName);
		state.putInteger("org.eclipse.ui.navigator.resources.workingSets.showTopLevelWorkingSets", 1);
		actions.restoreState(state);
		projectExplorer.getCommonViewer().refresh();
		projectExplorer.updateTitle();

		var packageExplorer = (PackageExplorerPart) page.showView(
				"org.eclipse.jdt.ui.PackageExplorer", null, IWorkbenchPage.VIEW_VISIBLE);
		var viewer = packageExplorer.getTreeViewer();
		Object previousInput = viewer.getInput();
		viewer.getControl().setRedraw(false);
		try {
			// Detach the root while configuring the model, avoiding JDT's extra modal working-set chooser.
			viewer.setInput(null);
			packageExplorer.rootModeChanged(PackageExplorerPart.WORKING_SETS_AS_ROOTS);
			var model = packageExplorer.getWorkingSetModel();
			Set<IWorkingSet> all = new LinkedHashSet<>(Arrays.asList(model.getAllWorkingSets()));
			Set<IWorkingSet> visible = new LinkedHashSet<>(Arrays.asList(model.getActiveWorkingSets()));
			all.addAll(sets);
			visible.addAll(sets);
			model.setWorkingSets(all.toArray(IWorkingSet[]::new), true, visible.toArray(IWorkingSet[]::new));
			model.configured();
			viewer.setInput(model);
		} finally {
			if (viewer.getInput() == null) viewer.setInput(previousInput);
			viewer.getControl().setRedraw(true);
		}
	}

	public static void preferences(final boolean eclipse, final boolean cleanup, final boolean formatter)
			throws Exception {
		if (eclipse) {
			IPreferencesService service = Platform.getPreferencesService();
			ProfileStore formatterStore = new FormatterProfileStore(new ProfileVersioner());
			ProfileStore cleanupStore = new ProfileStore(CleanUpConstants.CLEANUP_PROFILES, new CleanUpProfileVersioner());
			List<Profile> oldFormatter = formatterStore.readProfiles(InstanceScope.INSTANCE);
			List<Profile> oldCleanup = cleanupStore.readProfiles(InstanceScope.INSTANCE);
			try (InputStream input = resource("Eclipse Preferences for Gama.epf")) {
				var preferences = service.readPreferences(input);
				// Copy instance entries additively; a full EPF import may delete unrelated settings or rewrite values.
				preferences.accept(node -> {
					if (node.absolutePath().startsWith("/" + InstanceScope.SCOPE + "/")) {
						var destination = service.getRootNode().node(node.absolutePath());
						for (String key : node.keys()) destination.put(key, node.get(key, ""));
					}
					return true;
				});
				service.getRootNode().node("/" + InstanceScope.SCOPE).flush();
			}
			preserveProfiles(formatterStore, oldFormatter);
			preserveProfiles(cleanupStore, oldCleanup);
			InstanceScope.INSTANCE.getNode("org.eclipse.jdt.ui").flush();
		}

		if (cleanup) profile("Cleanup rules for Gama.xml", false);
		if (formatter) profile("Code formatter rules for Gama.xml", true);
	}

	private static void preserveProfiles(final ProfileStore store, final List<Profile> previous) throws Exception {
		if (previous == null || previous.isEmpty()) return;
		List<Profile> current = store.readProfiles(InstanceScope.INSTANCE);
		List<Profile> merged = current == null ? new ArrayList<>() : new ArrayList<>(current);
		for (Profile profile : previous) {
			if (merged.stream().noneMatch(p -> p.getID().equals(profile.getID()))) merged.add(profile);
		}
		store.writeProfiles(merged, InstanceScope.INSTANCE);
	}

	private static void profile(final String file, final boolean formatter) throws Exception {
		IProfileVersioner versioner = formatter ? new ProfileVersioner() : new CleanUpProfileVersioner();
		ProfileStore store = formatter ? new FormatterProfileStore(versioner)
				: new ProfileStore(CleanUpConstants.CLEANUP_PROFILES, versioner);
		List<Profile> existing = store.readProfiles(InstanceScope.INSTANCE);
		List<Profile> profiles = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
		List<Profile> imported;
		try (InputStream input = resource(file)) {
			imported = ProfileStore.readProfilesFromStream(new InputSource(input));
		}
		if (imported == null || imported.isEmpty()) throw new IOException("No profiles in " + file);
		var ui = InstanceScope.INSTANCE.getNode("org.eclipse.jdt.ui");
		var options = InstanceScope.INSTANCE.getNode(formatter ? JavaCore.PLUGIN_ID : "org.eclipse.jdt.ui");
		for (Profile profile : imported) {
			if (!(profile instanceof CustomProfile custom)) throw new IOException("Not a custom profile: " + file);
			if (custom.getVersion() > versioner.getCurrentVersion()) {
				throw new IOException(file + " requires a newer Eclipse JDT. Update Eclipse before applying it.");
			}
			versioner.update(custom);
			profiles.removeIf(p -> p.getID().equals(profile.getID()));
			profiles.add(profile);
		}
		store.writeProfiles(profiles, InstanceScope.INSTANCE);
		// The formatter export also contains Java Conventions; activate the first (GAMA) profile, not the last.
		for (var setting : imported.getFirst().getSettings().entrySet()) options.put(setting.getKey(), setting.getValue());
		ui.put(formatter ? PreferenceConstants.FORMATTER_PROFILE : "cleanup_profile", imported.getFirst().getID());
		options.putInt(formatter ? "org.eclipse.jdt.core.formatter.settings.version" : "cleanup_settings_version",
				versioner.getCurrentVersion());
		options.flush();
		ui.flush();
	}
}
