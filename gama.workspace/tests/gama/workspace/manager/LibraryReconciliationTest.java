package gama.workspace.manager;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.FrameworkWiring;

import gama.api.GAMA;
import gama.api.additions.GamaBundleLoader;
import gama.api.additions.GamaBundleLoader.ModelFolder;
import gama.api.runtime.IWorkspaceManager;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IDialogFactory;
import gama.api.ui.IGui;
import gama.api.ui.NullGuiHandler;

/** Standalone regressions using real project trees and strict Eclipse resource doubles; no SWT display is needed. */
public final class LibraryReconciliationTest {

	private static void check(final boolean value, final String message) {
		if (!value) throw new AssertionError(message);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static IPath path(final Path value) { return IPath.fromOSString(value.toString()); }

	/** Supply only the API bundle lookup needed by GamaBundleLoader's static initializer, without starting GAML. */
	private static final class RuntimeFixture implements AutoCloseable {
		final Object runtime;
		final Map<java.lang.reflect.Field, Object> previous = new LinkedHashMap<>();

		RuntimeFixture() throws Exception {
			final Class<?> platform = Class.forName("org.eclipse.core.internal.runtime.InternalPlatform");
			runtime = platform.getMethod("getDefault").invoke(null);
			final Bundle api = proxy(Bundle.class, (p, method, args) -> switch (method.getName()) {
				case "getSymbolicName" -> "gama.api";
				case "getState" -> Bundle.ACTIVE;
				case "getVersion" -> Version.emptyVersion;
				default -> throw new AssertionError("Unexpected API bundle call: " + method);
			});
			final BundleContext context = proxy(BundleContext.class, (p, method, args) -> {
				if ("getBundle".equals(method.getName())) return api;
				throw new AssertionError("Unexpected runtime context call: " + method);
			});
			final BundleRevision revision = proxy(BundleRevision.class, (p, method, args) -> {
				if ("getBundle".equals(method.getName())) return api;
				throw new AssertionError("Unexpected revision call: " + method);
			});
			final BundleCapability capability = proxy(BundleCapability.class, (p, method, args) -> {
				if ("getRevision".equals(method.getName())) return revision;
				throw new AssertionError("Unexpected capability call: " + method);
			});
			final FrameworkWiring wiring = proxy(FrameworkWiring.class, (p, method, args) -> {
				if ("findProviders".equals(method.getName())) {
					final var requirement = (org.osgi.resource.Requirement) args[0];
					return requirement.getDirectives().get("filter").contains("osgi.identity=gama.api")
							? List.of(capability) : List.of();
				}
				throw new AssertionError("Unexpected wiring call: " + method);
			});
			for (final var entry : Map.of("initialized", true, "context", context, "fwkWiring", wiring).entrySet()) {
				final var field = platform.getDeclaredField(entry.getKey());
				field.setAccessible(true);
				previous.put(field, field.get(runtime));
				field.set(runtime, entry.getValue());
			}
		}

		@Override
		public void close() throws Exception {
			for (final var entry : previous.entrySet()) { entry.getKey().set(runtime, entry.getValue()); }
		}
	}

	private static final class Description {
		String name;
		IPath location;
		String[] natures = { "gama.workspace.GamaNature" };
		String comment = "";
		final IProjectDescription resource = proxy(IProjectDescription.class, (p, method, args) -> switch (method.getName()) {
			case "getName" -> name;
			case "setName" -> { name = (String) args[0]; yield null; }
			case "getLocation" -> location;
			case "setLocation" -> { location = (IPath) args[0]; yield null; }
			case "getNatureIds" -> natures.clone();
			case "setNatureIds" -> { natures = ((String[]) args[0]).clone(); yield null; }
			case "getComment" -> comment;
			case "setComment" -> { comment = (String) args[0]; yield null; }
			default -> throw new AssertionError("Unexpected description call: " + method);
		});

		Description(final String name, final IPath location) { this.name = name; this.location = location; }

		Description(final IProjectDescription other) {
			this(other.getName(), other.getLocation());
			natures = other.getNatureIds();
			comment = other.getComment();
		}
	}

	private static final class Fixture {
		final Path directory;
		final Path workspace;
		final Path models;
		final Map<String, Project> projects = new LinkedHashMap<>();
		final Map<IPath, Description> descriptions = new HashMap<>();
		final Map<QualifiedName, String> rootProperties = new HashMap<>();
		int prompts;
		final IWorkspaceRoot root;
		final IWorkspace eclipse;
		final Bundle bundle;
		final ModelFolder folder;

		Fixture(final Path directory) throws Exception {
			this.directory = directory;
			workspace = Files.createDirectories(directory.resolve("workspace"));
			Files.createDirectory(workspace.resolve(".metadata"));
			models = Files.createDirectories(directory.resolve("new/models/Demos"));
			root = proxy(IWorkspaceRoot.class, (p, method, args) -> switch (method.getName()) {
				case "getProjects" -> projects.values().stream().filter(project -> project.exists)
						.map(project -> project.resource).toArray(IProject[]::new);
				case "getProject" -> project((String) args[0]).resource;
				case "getLocation" -> path(workspace);
				case "getPersistentProperty" -> rootProperties.get(args[0]);
				case "setPersistentProperty" -> {
					if (args[1] == null) { rootProperties.remove(args[0]); }
					else { rootProperties.put((QualifiedName) args[0], (String) args[1]); }
					yield null;
				}
				default -> throw new AssertionError("Unexpected root call: " + method);
			});
			eclipse = proxy(IWorkspace.class, (p, method, args) -> switch (method.getName()) {
				case "loadProjectDescription" -> new Description(descriptions.get(args[0]).resource).resource;
				case "newProjectDescription" -> new Description((String) args[0], null).resource;
				case "run" -> {
					((IWorkspaceRunnable) args[0]).run((IProgressMonitor) args[3]);
					yield null;
				}
				default -> throw new AssertionError("Unexpected workspace call: " + method);
			});
			bundle = proxy(Bundle.class, (p, method, args) -> switch (method.getName()) {
				case "getSymbolicName" -> "gama.library";
				case "getVersion" -> Version.parseVersion("1.0.0");
				case "getEntry" -> models.toUri().toURL();
				default -> throw new AssertionError("Unexpected bundle call: " + method);
			});
			folder = new ModelFolder(bundle, "Demos", "models/Demos");
			GAMA.setWorkspaceManager(proxy(IWorkspaceManager.class, (p, method, args) -> switch (method.getName()) {
				case "getRoot" -> root;
				case "getWorkspace" -> eclipse;
				case "getWorkspaceLocation" -> workspace.toString();
				default -> throw new AssertionError("Unexpected manager call: " + method);
			}));
			final IDialogFactory dialogs = new IDialogFactory() {
				@Override
				public boolean confirm(final IScope scope, final String title, final String message) {
					prompts++;
					throw new AssertionError("Library reconciliation must not prompt to save library projects");
				}
			};
			final IGui gui = new NullGuiHandler() {
				@Override
				public IDialogFactory getDialogFactory() { return dialogs; }
			};
			GAMA.setRegularGui(gui);
			GAMA.setHeadlessGui(gui);
			GamaBundleLoader.getModelFolders().clear();
			ProjectCategory.reset();
		}

		Project project(final String name) {
			return projects.computeIfAbsent(name, key -> new Project(this, key));
		}

		Path tree(final String relative, final String content) throws Exception {
			final Path location = Files.createDirectories(directory.resolve(relative));
			Files.writeString(location.resolve(".project"), "<projectDescription/>");
			Files.writeString(location.resolve("model.gaml"), content);
			descriptions.put(path(location.resolve(".project")), new Description(location.getFileName().toString(), path(location)));
			return location;
		}

		Project old(final String name, final boolean legacy) throws Exception {
			final Path location = tree("old/models/Demos/" + name, "original");
			tree("new/models/Demos/" + name, "current");
			final Project result = project(name);
			result.exists = true;
			result.open = true;
			result.description.location = path(location);
			result.properties.put(WorkspaceModelsManager.BUILTIN_PROPERTY, "old-version");
			if (!legacy) {
				result.properties.put(LibraryProjectSupport.SOURCE, "gama.library:models/Demos/" + name);
				result.properties.put(LibraryProjectSupport.CATEGORY, ProjectCategory.DEMOS.name());
			}
			return result;
		}

		LibraryUpdateReport update(final String... names) throws Exception {
			final Map<File, IPath> found = new HashMap<>();
			for (final String name : names) {
				found.put(models.resolve(name).toFile(), path(models.resolve(name).resolve(".project")));
			}
			final LibraryUpdateReport report = new LibraryUpdateReport();
			final var method = WorkspaceModelsManager.class.getDeclaredMethod("importBuiltInProjects",
					ModelFolder.class, File.class, Map.class, Map.class, java.util.Set.class,
					LibraryUpdateReport.class, IProgressMonitor.class);
			method.setAccessible(true);
			try {
				method.invoke(WorkspaceModelsManager.instance, folder, models.toFile(), found,
						new HashMap<String, Bundle>(), new HashSet<String>(), report, new NullProgressMonitor());
			} catch (final InvocationTargetException e) {
				throw new AssertionError("Library import failed", e.getCause());
			}
			return report;
		}
	}

	private static final class Project {
		final Fixture fixture;
		final String name;
		Description description;
		boolean exists;
		boolean open;
		boolean failCreation;
		int creations;
		int deletions;
		final Map<QualifiedName, String> properties = new HashMap<>();
		final IProject resource;

		Project(final Fixture fixture, final String name) {
			this.fixture = fixture;
			this.name = name;
			description = new Description(name, path(fixture.workspace.resolve(name)));
			resource = proxy(IProject.class, (p, method, args) -> switch (method.getName()) {
				case "getName" -> name;
				case "exists" -> exists;
				case "isOpen" -> open;
				case "getLocation" -> description.location;
				case "getDescription" -> {
					check(open, "Eclipse disallows reading descriptions of closed projects: " + name);
					yield new Description(description.resource).resource;
				}
				case "setDescription" -> {
					check(open, "Eclipse disallows setting descriptions of closed projects: " + name);
					description = new Description((IProjectDescription) args[0]);
					yield null;
				}
				case "getPersistentProperty" -> {
					check(open, "Eclipse disallows reading persistent properties of closed projects: " + name);
					yield properties.get(args[0]);
				}
				case "getPersistentProperties" -> {
					check(open, "Eclipse disallows reading persistent properties of closed projects: " + name);
					yield new HashMap<>(properties);
				}
				case "setPersistentProperty" -> {
					check(open, "Eclipse disallows setting persistent properties of closed projects: " + name);
					if (args[1] == null) { properties.remove(args[0]); }
					else { properties.put((QualifiedName) args[0], (String) args[1]); }
					yield null;
				}
				case "open" -> { open = true; yield null; }
				case "close" -> { open = false; yield null; }
				case "refreshLocal" -> null;
				case "delete" -> {
					check(!(boolean) args[0], "Relinking must never delete project contents");
					exists = false;
					open = false;
					properties.clear();
					deletions++;
					yield null;
				}
				case "create" -> {
					if (failCreation) {
						failCreation = false;
						throw new CoreException(Status.error("Simulated registration failure"));
					}
					description = new Description((IProjectDescription) args[0]);
					exists = true;
					creations++;
					yield null;
				}
				case "copy" -> throw new AssertionError("Library projects must never be copied into User");
				default -> throw new AssertionError("Unexpected project call: " + method);
			});
		}
	}

	public static void main(final String[] args) throws Exception {
		final Path directory = Files.createTempDirectory("gama-library-repair-");
		final RuntimeFixture runtime = new RuntimeFixture();
		final IWorkspaceManager previousManager = GAMA.getWorkspaceManager();
		final IGui previousGui = GAMA.getRegularGui();
		final IGui previousHeadless = GAMA.getHeadlessGui();
		final List<ModelFolder> previousFolders = new ArrayList<>(GamaBundleLoader.getModelFolders());
		try {
			Fixture f = new Fixture(directory.resolve("pristine"));
			Project old = f.old("Ants", false);
			check(ProjectCategory.of(old.resource) == ProjectCategory.DEMOS, "Stale links retain their category");
			LibraryUpdateReport report = f.update("Ants");
			check(!report.needsAttention(), "Pristine relinking succeeds without conflicts");
			check(old.description.location.equals(path(f.models.resolve("Ants"))), "Relink an accessible old installation");
			check(Files.readString(f.directory.resolve("old/models/Demos/Ants/model.gaml")).equals("original"),
					"Leave files in the old installation intact");
			check(f.prompts == 0 && old.creations == 1, "Pristine relocation needs no copy or prompt");
			f.update("Ants");
			check(old.creations == 1 && old.deletions == 1, "Repeated updates are idempotent");

			f = new Fixture(directory.resolve("edits"));
			old = f.old("Ants", false);
			Files.writeString(old.description.location.toFile().toPath().resolve("model.gaml"), "local edits");
			report = f.update("Ants");
			check(old.description.location.equals(path(f.models.resolve("Ants"))),
					"The current library version is authoritative even when old contents were modified");
			check(f.prompts == 0 && !report.needsAttention(), "Changed library projects are silently relinked");
			check(f.projects.size() == 1, "Changed library projects do not create user copies");
			f.update("Ants");
			check(f.projects.size() == 1, "Repeated updates do not duplicate library projects");

			f = new Fixture(directory.resolve("legacy"));
			f.old("Ants", true);
			f.old("Traffic", true);
			report = f.update("Ants", "Traffic");
			check(f.prompts == 0 && f.projects.size() == 2, "Legacy projects are silently relinked without user copies");

			f = new Fixture(directory.resolve("closed-legacy"));
			old = f.old("Ants", true);
			old.open = false;
			check(ProjectCategory.of(old.resource) == ProjectCategory.USER,
					"A legacy closed project without root provenance does not read inaccessible project properties");
			report = f.update("Ants");
			check(!report.needsAttention() && old.deletions == 1 && old.open,
					"Reconciliation migrates and relinks closed legacy library projects");
			old.open = false;
			check(ProjectCategory.of(old.resource) == ProjectCategory.DEMOS,
					"Workspace-owned provenance keeps a closed project in the right category");
			check(!old.open, "Classifying a closed project does not open it");
			old.description.location = path(f.directory.resolve("another-installation"));
			check(ProjectCategory.of(old.resource) == ProjectCategory.USER,
					"Root provenance is not reused for a different location");

			f = new Fixture(directory.resolve("collision"));
			old = f.old("Ants", true);
			LibraryProjectSupport.forget(old.resource);
			old.description.comment = "gama.library";
			report = f.update("Ants");
			check(old.deletions == 0 && f.project("Ants (library)").exists,
					"Names and copied .project comments never authorize replacement of a user project");
			f.update("Ants");
			check(!f.project("Ants (library 2)").exists, "Stable provenance reuses a collision-renamed project");
			check(report.needsAttention(), "Explain why the project has a library suffix");

			f = new Fixture(directory.resolve("missing-user"));
			old = f.old("Ants", true);
			LibraryProjectSupport.forget(old.resource);
			old.description.location = path(f.directory.resolve("missing-user-project"));
			f.update("Ants");
			check(old.deletions == 0, "A missing user location is not proof of library ownership");

			f = new Fixture(directory.resolve("missing-library"));
			old = f.old("Ants", true);
			old.description.location = path(f.directory.resolve("removed-installation"));
			f.update("Ants");
			check(old.deletions == 1 && f.prompts == 0, "Missing owned library links can be restored without a copy");

			f = new Fixture(directory.resolve("rollback"));
			old = f.old("Ants", false);
			final IPath previousLocation = old.description.location;
			final Map<QualifiedName, String> previousProperties = new HashMap<>(old.properties);
			old.failCreation = true;
			report = f.update("Ants");
			check(report.needsAttention() && old.exists && old.description.location.equals(previousLocation),
					"A failed replacement restores the original registration");
			check(old.properties.equals(previousProperties), "Rollback restores provenance");

			f = new Fixture(directory.resolve("closed-user"));
			old = f.old("Ants", true);
			LibraryProjectSupport.forget(old.resource);
			old.open = false;
			f.update("Ants");
			check(!old.open && old.deletions == 0, "A colliding closed user project remains closed and unchanged");

			if (GamaBundleLoader.CORE_MODELS == null) {
				report = WorkspaceModelsManager.instance.loadModelsLibrary(new NullProgressMonitor());
				check(report.needsAttention() && report.getSummary().contains("could not be discovered"),
						"Missing discovery reports a failure rather than an empty success");
			}
			report = new LibraryUpdateReport();
			report.error("Explicit failure without an exception", null);
			report.changed("Relinked old location to new location");
			report.save();
			check(Files.readString(f.workspace.resolve(".metadata/gama-library-repair.log"))
					.contains("Explicit failure"), "Persist the full repair report");
			System.out.println("LibraryReconciliationTest passed");
		} finally {
			GAMA.setWorkspaceManager(previousManager);
			GAMA.setRegularGui(previousGui);
			GAMA.setHeadlessGui(previousHeadless);
			GamaBundleLoader.getModelFolders().clear();
			GamaBundleLoader.getModelFolders().addAll(previousFolders);
			ProjectCategory.reset();
			runtime.close();
			try (var paths = Files.walk(directory)) {
				for (final Path file : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(file); }
			}
		}
	}
}
