/*******************************************************************************************************
 *
 * UserGroups.java, in gama.ui.navigator.view, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import gama.api.GAMA;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.workspace.manager.ProjectCategory;
import gama.workspace.manager.WorkspaceModelsManager;

/**
 * Operations on the groups of user projects (plain directories of the workspace containing projects, see issue #678).
 */
public final class UserGroups {

	private UserGroups() {}

	/**
	 * Gets the location of the workspace.
	 *
	 * @return the location of the workspace on disk
	 */
	public static IPath workspaceLocation() {
		return GAMA.getWorkspaceManager().getRoot().getLocation();
	}

	/**
	 * Checks a group path (segments separated by '/'). An empty path means "no group".
	 *
	 * @param groupPath
	 *            the path relative to the workspace
	 * @return an error message, or {@code null} if the path is valid
	 */
	public static String validate(final String groupPath) {
		if (groupPath == null || groupPath.isBlank()) return null;
		final IPath path = new Path(groupPath.trim());
		if (path.isAbsolute() || path.getDevice() != null) return "A group must be a relative path";
		if (path.segmentCount() > 3) return "Groups cannot be nested more than 3 levels deep";
		IPath current = Path.EMPTY;
		for (final String segment : path.segments()) {
			if (segment.isBlank() || segment.startsWith(".") || ".".equals(segment) || "..".equals(segment))
				return "'" + segment + "' is not a valid group name";
			if (current.isEmpty() && ProjectCategory.isReservedName(segment))
				return "'" + segment + "' is the name of a top-level folder and cannot be used for a group";
			if (!GAMA.getWorkspaceManager().getWorkspace().validateName(segment, IResource.FOLDER).isOK())
				return "'" + segment + "' is not a valid folder name";
			current = current.append(segment);
			final File dir = workspaceLocation().append(current).toFile();
			if (dir.isDirectory() && WorkspaceModelsManager.instance.isGamaProject(dir))
				return "'" + current + "' is a project and cannot be used as a group";
		}
		return null;
	}

	/**
	 * Creates the directory of a group.
	 *
	 * @param groupPath
	 *            the validated path relative to the workspace
	 * @return {@code true} if the directory exists afterwards
	 */
	public static boolean create(final String groupPath) {
		if (groupPath == null || groupPath.isBlank()) return true;
		return workspaceLocation().append(groupPath.trim()).toFile().mkdirs()
				|| workspaceLocation().append(groupPath.trim()).toFile().isDirectory();
	}

	/**
	 * Tells why a project name cannot be used in a given group.
	 *
	 * @param name
	 *            the proposed name
	 * @param groupPath
	 *            the path of the group (possibly empty) where the project will be located
	 * @param ignore
	 *            a project that is allowed to already have this name (the one being moved), or {@code null}
	 * @return an error message, or {@code null} if the name is usable
	 */
	public static String checkProjectName(final String name, final String groupPath, final IProject ignore) {
		final String n = name == null ? "" : name.trim();
		if (n.isEmpty()) return "Enter a name";
		if (ProjectCategory.isReservedName(n)) return "'" + n + "' is the name of a top-level folder";
		if (!GAMA.getWorkspaceManager().getWorkspace().validateName(n, IResource.PROJECT).isOK())
			return "'" + n + "' is not a valid project name";
		final IProject existing = GAMA.getWorkspaceManager().getRoot().getProject(n);
		if (existing.exists() && !existing.equals(ignore)) return "A project named '" + n + "' already exists";
		final IPath dir = groupPath == null || groupPath.isBlank() ? workspaceLocation().append(n)
				: workspaceLocation().append(groupPath.trim()).append(n);
		if (dir.toFile().exists() && (ignore == null || !dir.equals(ignore.getLocation())))
			return "A directory named '" + n + "' already exists in this location";
		return null;
	}

	/**
	 * Asks the user for another name for a project whose name cannot be used.
	 *
	 * @param name
	 *            the current name
	 * @param groupPath
	 *            the path of the group where the project will be located
	 * @param ignore
	 *            a project that may keep its name, or {@code null}
	 * @return the new name, or {@code null} if the user cancelled
	 */
	public static String askNewName(final String name, final String groupPath, final IProject ignore) {
		final String reason = checkProjectName(name, groupPath, ignore);
		final org.eclipse.jface.dialogs.InputDialog dialog = new org.eclipse.jface.dialogs.InputDialog(
				WorkbenchHelper.getShell(), "Project name already used",
				(reason == null ? "" : reason + ".\n") + "Please choose another name for '" + name + "':",
				name + "-2", text -> checkProjectName(text, groupPath, ignore));
		return dialog.open() == org.eclipse.jface.window.Window.OK ? dialog.getValue().trim() : null;
	}

	/**
	 * Replaces the name defined in the {@code .project} file of a project directory.
	 *
	 * @param dir
	 *            the project directory
	 * @param name
	 *            the new name
	 * @throws java.io.IOException
	 *             if the file cannot be rewritten
	 */
	public static void rewriteProjectName(final File dir, final String name) throws java.io.IOException {
		final java.nio.file.Path file = dir.toPath().resolve(".project");
		final String content = Files.readString(file, java.nio.charset.StandardCharsets.UTF_8);
		final String escaped = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
		Files.writeString(file, content.replaceFirst("<name>[^<]*</name>",
				java.util.regex.Matcher.quoteReplacement("<name>" + escaped + "</name>")),
				java.nio.charset.StandardCharsets.UTF_8);
	}

	/**
	 * Moves a user project into a group (or out of any group when the path is empty). The contents of the project are
	 * moved on disk.
	 *
	 * @param project
	 *            the project to move
	 * @param groupPath
	 *            the validated path of the target group relative to the workspace, possibly empty
	 * @return {@code false} if the user cancelled the operation
	 * @throws CoreException
	 *             if the move fails
	 */
	public static boolean moveProject(final IProject project, final String groupPath) throws CoreException {
		final boolean ungrouped = groupPath == null || groupPath.isBlank();
		String name = project.getName();
		IPath target = ungrouped ? workspaceLocation().append(name) : workspaceLocation().append(groupPath.trim()).append(name);
		final IPath current = project.getLocation();
		if (current != null && current.equals(target)) return true;
		if (target.toFile().exists()) {
			name = askNewName(name, groupPath, project);
			if (name == null) return false;
			target = ungrouped ? workspaceLocation().append(name) : workspaceLocation().append(groupPath.trim()).append(name);
		}
		if (!ungrouped) { create(groupPath); }
		final IProjectDescription desc = project.getDescription();
		desc.setName(name);
		desc.setLocation(ungrouped && name.equals(project.getName()) ? null : target);
		project.move(desc, IResource.FORCE | IResource.SHALLOW, new NullProgressMonitor());
		ResourceManager.cache.asMap().keySet().removeIf(r -> project.equals(r.getProject()));
		return true;
	}

	/**
	 * Copies a project into a group, asking for the name of the copy.
	 *
	 * @param project
	 *            the project to copy
	 * @param groupPath
	 *            the validated path of the target group, possibly empty
	 * @param shell
	 *            the shell used for dialogs
	 */
	public static void copyProject(final IProject project, final String groupPath,
			final org.eclipse.swt.widgets.Shell shell) {
		final org.eclipse.jface.dialogs.InputDialog dialog = new org.eclipse.jface.dialogs.InputDialog(shell,
				"Copy project", "Name of the copy of '" + project.getName() + "':", project.getName() + "-copy",
				name -> {
					final String n = name.trim();
					if (n.isEmpty()) return "Enter a name";
					if (ProjectCategory.isReservedName(n)) return "'" + n + "' is a reserved name";
					if (!GAMA.getWorkspaceManager().getWorkspace().validateName(n, IResource.PROJECT).isOK())
						return "Invalid project name";
					if (GAMA.getWorkspaceManager().getRoot().getProject(n).exists()) return "This project already exists";
					if (workspaceLocation().append(groupPath).append(n).toFile().exists())
						return "This directory already exists";
					return null;
				});
		if (dialog.open() != org.eclipse.jface.window.Window.OK) return;
		final String name = dialog.getValue().trim();
		try {
			create(groupPath);
			final IProjectDescription desc = project.getDescription();
			desc.setName(name);
			desc.setLocation(workspaceLocation().append(groupPath).append(name));
			project.copy(desc, IResource.FORCE, new NullProgressMonitor());
		} catch (final CoreException e) {
			GAMA.getGui().getDialogFactory().error("The project could not be copied: " + e.getMessage());
		}
	}

	/**
	 * Copies directories (GAMA projects) from the file system into a group and registers them in the workspace.
	 *
	 * @param paths
	 *            the absolute paths of the directories
	 * @param groupPath
	 *            the validated path of the group
	 */
	public static void importDirectories(final String[] paths, final String groupPath) {
		create(groupPath);
		for (final String path : paths) {
			final File src = new File(path);
			if (!WorkspaceModelsManager.instance.isGamaProject(src)) { continue; }
			String name = src.getName();
			if (checkProjectName(name, groupPath, null) != null) {
				name = askNewName(name, groupPath, null);
				if (name == null) { continue; }
			}
			final File target = workspaceLocation().append(groupPath).append(name).toFile();
			try (java.util.stream.Stream<java.nio.file.Path> walk = Files.walk(src.toPath())) {
				for (final java.nio.file.Path p : (Iterable<java.nio.file.Path>) walk::iterator) {
					final java.nio.file.Path dest = target.toPath().resolve(src.toPath().relativize(p).toString());
					if (Files.isDirectory(p)) {
						Files.createDirectories(dest);
					} else {
						Files.copy(p, dest);
					}
				}
				if (!name.equals(src.getName())) { rewriteProjectName(target, name); }
				WorkspaceModelsManager.instance.createOrUpdateProject(name, target);
			} catch (final java.io.IOException e) {
				GAMA.getGui().getDialogFactory().error("Could not copy " + src.getName() + ": " + e.getMessage());
			}
		}
		refreshNavigator();
	}

	/**
	 * Rebuilds the content of the navigator after groups or projects have been created, moved or deleted.
	 */
	public static void refreshNavigator() {
		gama.ui.shared.utils.WorkbenchHelper.runInUI("Refresh groups", 5, m -> {
			final NavigatorRoot root = NavigatorRoot.getInstance();
			for (final TopLevelFolder folder : root.getFolders()) { folder.initializeChildren(); }
			root.getManager().refreshResource(root);
		});
	}

	/**
	 * Moves the projects of a group (and its sub-groups) to a new directory, then removes the old empty directories.
	 *
	 * @param group
	 *            the group
	 * @param newPath
	 *            the validated new path of the group, relative to the workspace
	 * @throws CoreException
	 *             if a move fails
	 */
	public static void renameGroup(final UserGroupFolder group, final String newPath) throws CoreException {
		final IPath old = group.getRelativePath();
		final IPath target = new Path(newPath.trim());
		final List<WrappedProject> projects = group.getAllProjects();
		create(newPath);
		for (final WrappedProject wp : projects) {
			final IProject p = wp.getResource();
			final IPath rel = p.getLocation().removeFirstSegments(workspaceLocation().segmentCount() + old.segmentCount())
					.removeLastSegments(1);
			moveProject(p, target.append(rel).toString());
		}
		deleteIfEmpty(old);
	}

	/**
	 * Deletes the directory of a group if it contains no project (empty directories are removed bottom-up).
	 *
	 * @param group
	 *            the relative path of the group
	 * @return {@code true} if the directory has been deleted
	 */
	public static boolean deleteIfEmpty(final IPath group) {
		final File dir = workspaceLocation().append(group).toFile();
		return deleteEmptyTree(dir);
	}

	private static boolean deleteEmptyTree(final File dir) {
		if (!dir.isDirectory() || WorkspaceModelsManager.instance.isGamaProject(dir)) return false;
		final File[] children = dir.listFiles();
		if (children != null) {
			for (final File f : children) {
				if (f.isDirectory()) { deleteEmptyTree(f); }
			}
		}
		final String[] left = dir.list();
		if (left == null || left.length > 0) return false;
		try {
			Files.delete(dir.toPath());
			return true;
		} catch (final java.io.IOException e) {
			return false;
		}
	}

}
