/*******************************************************************************************************
 *
 * WorkspaceProjects.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.packaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Registers project directories in the headless workspace, in place, so that the exporters can work on them. The
 * registrations created here are removed on {@link #close()}; the project contents are never deleted or copied.
 */
final class WorkspaceProjects implements AutoCloseable {

	/** The projects registered by this instance. */
	private final List<IProject> created = new ArrayList<>();

	/** The directory of the project containing a model file: its closest ancestor with a .project file. */
	static Path projectDirectoryOf(final Path model) throws IOException {
		for (Path directory = model.getParent(); directory != null; directory = directory.getParent()) {
			if (Files.isRegularFile(directory.resolve(".project"))) return directory;
		}
		throw new IOException("The model is not in a GAMA project (no .project file in its directories): " + model);
	}

	/**
	 * Registers the project of a directory, or returns the project already registered at this location.
	 *
	 * @throws IOException
	 *             if the directory is not a project, or if another project with the same name is registered
	 */
	IProject register(final Path directory) throws IOException, CoreException {
		final Path location = directory.toAbsolutePath().normalize();
		final Path file = location.resolve(".project");
		if (!Files.isRegularFile(file))
			throw new IOException("Not a GAMA project (no .project file): " + location);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.loadProjectDescription(IPath.fromOSString(file.toString()));
		final IProject project = workspace.getRoot().getProject(description.getName());
		if (project.exists()) {
			if (project.getLocation() == null
					|| !Files.isSameFile(project.getLocation().toFile().toPath(), location))
				throw new IOException("Another project named " + project.getName()
						+ " is already registered in the workspace, at " + project.getLocation());
			if (!project.isOpen()) { project.open(null); }
		} else {
			final IPath path = IPath.fromOSString(location.toString());
			description.setLocation(workspace.getRoot().getLocation().append(project.getName()).equals(path) ? null
					: path);
			final var status = workspace.validateProjectLocation(project, description.getLocation());
			if (!status.isOK())
				throw new IOException("Cannot use the project at " + location + ": " + status.getMessage());
			project.create(description, null);
			created.add(project);
			project.open(null);
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		return project;
	}

	/** Removes the registrations made by this instance, keeping the project contents. */
	@Override
	public void close() {
		for (final IProject project : created.reversed()) {
			try {
				project.delete(false /* never delete the contents */, true, null);
			} catch (final CoreException e) {
				System.err.println("Unable to unregister project " + project.getName() + ": " + e.getMessage());
			}
		}
		created.clear();
	}
}
