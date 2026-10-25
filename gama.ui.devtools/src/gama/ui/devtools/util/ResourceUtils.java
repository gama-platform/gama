/*******************************************************************************************************
 *
 * ResourceUtils.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import gama.ui.devtools.GamaDevToolsActivator;

/**
 * Small helpers to read and write workspace text files.
 */
public final class ResourceUtils {

	private ResourceUtils() {}

	/**
	 * Reads a workspace file as UTF-8, or returns {@code null} if it does not exist.
	 */
	public static String read(final IFile file) throws CoreException {
		if (file == null || !file.exists()) return null;
		try (InputStream in = file.getContents(true)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new CoreException(Status.error("Cannot read " + file.getFullPath(), e));
		}
	}

	/**
	 * Reads a project-relative file, or returns {@code null} if it does not exist.
	 */
	public static String read(final IProject project, final String path) throws CoreException {
		return read(project.getFile(path));
	}

	/**
	 * Creates or replaces a file (creating the parent folders if needed).
	 */
	public static IFile write(final IContainer container, final String path, final String content,
			final IProgressMonitor monitor) throws CoreException {
		IFile file = container.getFile(org.eclipse.core.runtime.IPath.fromPortableString(path));
		createParents(file, monitor);
		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		if (file.exists()) {
			file.setContents(new ByteArrayInputStream(bytes), IResource.FORCE | IResource.KEEP_HISTORY, monitor);
		} else {
			file.create(new ByteArrayInputStream(bytes), IResource.FORCE, monitor);
		}
		return file;
	}

	/**
	 * Creates a folder (and its parents) if it does not exist.
	 */
	public static IFolder createFolder(final IContainer container, final String path, final IProgressMonitor monitor)
			throws CoreException {
		IFolder folder = container.getFolder(org.eclipse.core.runtime.IPath.fromPortableString(path));
		createRecursively(folder, monitor);
		return folder;
	}

	private static void createParents(final IResource resource, final IProgressMonitor monitor)
			throws CoreException {
		if (resource.getParent() instanceof IFolder f) { createRecursively(f, monitor); }
	}

	private static void createRecursively(final IFolder folder, final IProgressMonitor monitor) throws CoreException {
		if (folder.exists()) return;
		createParents(folder, monitor);
		folder.create(IResource.FORCE, true, monitor);
	}

	/**
	 * Builds an error status for this plugin.
	 */
	public static IStatus error(final String message, final Throwable t) {
		return new Status(IStatus.ERROR, GamaDevToolsActivator.PLUGIN_ID, message, t);
	}

	/**
	 * Logs an exception in the error log.
	 */
	public static void log(final Throwable t) {
		IStatus status = t instanceof CoreException ce ? ce.getStatus() : error(t.getMessage(), t);
		org.eclipse.core.runtime.ILog.of(ResourceUtils.class).log(status);
	}

}
