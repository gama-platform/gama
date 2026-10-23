package gama.ui.navigator.view.contents;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import gama.api.GAMA;
import gama.api.runtime.IWorkspaceManager;

/** Standalone regression checks for group deletion, including closed projects and symbolic links. */
public final class UserGroupsTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static IProject project(final Path location, final AtomicBoolean registered) {
		return proxy(IProject.class, (p, method, args) -> switch (method.getName()) {
			case "getLocation" -> IPath.fromOSString(location.toString());
			case "delete" -> {
				check(!(boolean) args[0], "Unregister without following project locations or linked directories");
				check((boolean) args[1], "Force removal of closed or out-of-sync projects");
				registered.set(false);
				yield null;
			}
			default -> throw new AssertionError("Unexpected project call: " + method);
		});
	}

	private static void rejects(final String path) throws Exception {
		try {
			UserGroups.deleteGroup(new org.eclipse.core.runtime.Path(path), new NullProgressMonitor());
			throw new AssertionError("Deletion must reject: " + path);
		} catch (final IOException expected) {
			check(!expected.getMessage().isBlank(), "Rejected deletion explains the error");
		}
	}

	public static void main(final String[] args) throws Exception {
		final Path fixture = Files.createTempDirectory("gama-user-groups-");
		final Path workspace = Files.createDirectory(fixture.resolve("workspace"));
		final List<IProject> projects = new ArrayList<>();
		final IWorkspace eclipseWorkspace = proxy(IWorkspace.class, (p, method, arguments) -> {
			if ("validateName".equals(method.getName())) return Status.OK_STATUS;
			throw new AssertionError("Unexpected workspace call: " + method);
		});
		final IWorkspaceRoot root = proxy(IWorkspaceRoot.class, (p, method, arguments) -> switch (method.getName()) {
			case "getLocation" -> IPath.fromOSString(workspace.toString());
			case "getProjects" -> projects.toArray(IProject[]::new);
			default -> throw new AssertionError("Unexpected root call: " + method);
		});
		final IWorkspaceManager previous = GAMA.getWorkspaceManager();
		GAMA.setWorkspaceManager(proxy(IWorkspaceManager.class, (p, method, arguments) -> switch (method.getName()) {
			case "getRoot" -> root;
			case "getWorkspace" -> eclipseWorkspace;
			default -> throw new AssertionError("Unexpected manager call: " + method);
		}));
		try {
			final Path group = Files.createDirectories(workspace.resolve("group/nested"));
			final Path first = Files.createDirectory(workspace.resolve("group/first"));
			Files.writeString(first.resolve(".project"), "project");
			Files.writeString(first.resolve("model.gaml"), "model first");
			final Path second = Files.createDirectory(group.resolve("second"));
			Files.writeString(second.resolve(".project"), "project");
			Files.writeString(workspace.resolve("group/.hidden"), "hidden file");
			final Path outside = Files.createDirectory(fixture.resolve("outside"));
			final Path keep = Files.writeString(outside.resolve("keep.txt"), "keep");
			final Path link = Files.createSymbolicLink(group.resolve("link"), outside);
			final AtomicBoolean firstRegistered = new AtomicBoolean(true);
			final AtomicBoolean secondRegistered = new AtomicBoolean(true);
			final AtomicBoolean linkedRegistered = new AtomicBoolean(true);
			final AtomicBoolean unrelatedRegistered = new AtomicBoolean(true);
			projects.add(project(first, firstRegistered));
			projects.add(project(second, secondRegistered));
			projects.add(project(link, linkedRegistered));
			projects.add(project(workspace.resolve("group-other/project"), unrelatedRegistered));

			UserGroups.deleteGroup(new org.eclipse.core.runtime.Path("group"), new NullProgressMonitor());
			check(!Files.exists(workspace.resolve("group")), "Delete projects, subgroups, hidden and ordinary files");
			check(!firstRegistered.get() && !secondRegistered.get() && !linkedRegistered.get(),
					"Unregister every contained project, even when not present in a cached group");
			check(unrelatedRegistered.get(), "Do not remove similarly named sibling groups");
			check(Files.readString(keep).equals("keep"), "Do not follow symbolic links outside the group");

			projects.clear();
			final AtomicBoolean missingRegistered = new AtomicBoolean(true);
			projects.add(project(workspace.resolve("missing/project"), missingRegistered));
			UserGroups.deleteGroup(new org.eclipse.core.runtime.Path("missing"), new NullProgressMonitor());
			check(!missingRegistered.get(), "Remove stale registrations when the group was deleted externally");
			projects.clear();

			Files.createDirectories(workspace.resolve("empty/subgroup"));
			UserGroups.deleteGroup(new org.eclipse.core.runtime.Path("empty"), new NullProgressMonitor());
			check(!Files.exists(workspace.resolve("empty")), "Delete empty groups and subgroups");
			rejects("");
			rejects("../outside");
			rejects("/absolute");
			rejects("Tests");
			Files.createSymbolicLink(workspace.resolve("external"), outside);
			rejects("external");
			Files.createSymbolicLink(workspace.resolve("root-link"), workspace);
			rejects("root-link");
			check(Files.exists(keep) && Files.isDirectory(workspace), "Rejected deletions preserve their targets");
			System.out.println("UserGroupsTest passed");
		} finally {
			GAMA.setWorkspaceManager(previous);
			try (var paths = Files.walk(fixture)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
			}
		}
	}
}
