package gama.ui.navigator.view.dependencies;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;

import gama.dev.DEBUG;
import gaml.compiler.indexer.GamlImportReader;
import gaml.compiler.resource.GamlResourceServices;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.Edge;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.Node;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.Resource;
import gama.ui.navigator.view.dependencies.ModelDependencyGraph.State;

/** Reads reachable imports and optional resource references without semantic compilation or index mutation. */
public final class ModelDependencyService {

	private record Stamp(Path path, BasicFileAttributes attributes) {}

	private ModelDependencyService() {}

	public static boolean isModel(final IFile file) {
		return file != null && ("gaml".equalsIgnoreCase(file.getFileExtension())
				|| "experiment".equalsIgnoreCase(file.getFileExtension()));
	}

	public static ModelDependencyGraph read(final URI input, final IProgressMonitor monitor) {
		return read(input, false, monitor);
	}

	public static ModelDependencyGraph read(final URI input, final boolean includeResources,
			final IProgressMonitor monitor) {
		final URI focus = GamlResourceServices.properlyEncodedURI(input);
		final var nodes = new LinkedHashMap<URI, Node>();
		final var edges = new LinkedHashSet<Edge>();
		final var pending = new ArrayDeque<URI>();
		final var stamps = new LinkedHashMap<URI, Stamp>();
		final var resources = new ArrayList<Resource>();
		pending.add(focus);
		while (!pending.isEmpty()) {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			final URI uri = pending.removeFirst();
			if (nodes.containsKey(uri)) continue;
			monitor.subTask("Reading " + URI.decode(uri.lastSegment()));
			final var diagnostics = new ArrayList<String>();
			State state = State.AVAILABLE;
			try {
				final IFile workspaceFile = workspaceFile(uri);
				final Path disk = diskPath(uri, workspaceFile);
				if (workspaceFile != null && !workspaceFile.isAccessible() || disk == null || !Files.isRegularFile(disk)) {
					throw new IOException("Model file is missing or inaccessible");
				}
				final var before = Files.readAttributes(disk, BasicFileAttributes.class);
				final var encoding = workspaceFile == null ? StandardCharsets.UTF_8
						: java.nio.charset.Charset.forName(workspaceFile.getCharset());
				try (final var reader = new InputStreamReader(Files.newInputStream(disk), encoding)) {
					final var result = GamlImportReader.read(uri, reader, includeResources);
					diagnostics.addAll(result.diagnostics());
					if (!result.diagnostics().isEmpty()) state = State.ERROR;
					for (final var imported : result.imports()) {
						edges.add(new Edge(uri, imported.uri(), imported.alias(), imported.line()));
						pending.addLast(imported.uri());
					}
					final var used = new LinkedHashSet<URI>();
					for (final String path : result.uses()) {
						if (monitor.isCanceled()) throw new OperationCanceledException();
						try {
							final URI target = resolveResource(uri, path);
							if ("gaml".equalsIgnoreCase(target.fileExtension())
									|| "experiment".equalsIgnoreCase(target.fileExtension())) continue;
							if (used.add(target)) resources.add(new Resource(uri, resourceNode(target)));
						} catch (final IllegalArgumentException e) {
							diagnostics.add("Invalid resource path '" + path + "': " + e.getMessage());
						}
					}
				}
				state = withWorkspaceDiagnostics(workspaceFile, state, diagnostics);
				final var after = Files.readAttributes(disk, BasicFileAttributes.class);
				stamps.put(uri, new Stamp(disk, after));
				if (before.size() != after.size() || !before.lastModifiedTime().equals(after.lastModifiedTime())) {
					diagnostics.add("File changed while reading; refresh to obtain a current graph");
				}
				if (!diagnostics.isEmpty() && state == State.AVAILABLE) state = State.WARNING;
			} catch (final IOException | CoreException e) {
				state = State.MISSING;
				diagnostics.add(e.getMessage());
			}
			if (!diagnostics.isEmpty()) DEBUG.ERR("Dependencies for " + uri + ": " + String.join("; ", diagnostics));
			final String name = uri.lastSegment() == null ? uri.toString() : URI.decode(uri.lastSegment());
			final String path = uri.isPlatformResource() ? uri.toPlatformString(true)
					: uri.isFile() ? uri.toFileString() : uri.toString();
			nodes.put(uri, new Node(uri, name, path, state, diagnostics));
			monitor.worked(1);
		}
		// A root can change while its descendants are being read.
		for (final var entry : stamps.entrySet()) {
			if (monitor.isCanceled()) throw new OperationCanceledException();
			final var node = nodes.get(entry.getKey());
			State state = node.state();
			String problem = null;
			try {
				final var after = Files.readAttributes(entry.getValue().path(), BasicFileAttributes.class);
				final var before = entry.getValue().attributes();
				if (!after.isRegularFile()) {
					state = State.MISSING;
					problem = "Model file is missing or inaccessible";
				} else if (before.size() != after.size() || !before.lastModifiedTime().equals(after.lastModifiedTime())) {
					problem = "File changed during dependency discovery; refresh to obtain a current graph";
					if (state == State.AVAILABLE) state = State.WARNING;
				}
			} catch (final IOException e) {
				state = State.MISSING;
				problem = "File became inaccessible during dependency discovery: " + e.getMessage();
			}
			if (problem != null) {
				final var diagnostics = new ArrayList<>(node.diagnostics());
				diagnostics.add(problem);
				nodes.put(node.uri(), new Node(node.uri(), node.name(), node.path(), state, diagnostics));
				DEBUG.ERR("Dependencies for " + node.uri() + ": " + problem);
			}
		}
		return new ModelDependencyGraph(focus, nodes, new ArrayList<>(edges), resources);
	}

	private static URI resolveResource(final URI owner, final String path) {
		final URI reference = URI.createURI(path, true);
		if (!reference.isRelative() && (reference.isFile() || reference.isPlatformResource())) {
			return GamlResourceServices.properlyEncodedURI(reference);
		}
		final var local = new java.io.File(path);
		if (local.isAbsolute()) {
			// Prefer a real absolute file, but also accept workspace-root paths like the navigator.
			if (!Files.exists(local.toPath()) && owner.isPlatformResource()) {
				final var workspacePath = org.eclipse.core.runtime.Path.fromOSString(path);
				if (workspacePath.getDevice() == null && workspacePath.segmentCount() > 1
						&& ResourcesPlugin.getWorkspace().getRoot().getProject(workspacePath.segment(0)).exists()) {
					return URI.createPlatformResourceURI(workspacePath.toPortableString(), true);
				}
			}
			final URI absolute = URI.createFileURI(path);
			final var files = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocationURI(java.net.URI.create(absolute.toString()));
			return files.length == 0 ? absolute : URI.createPlatformResourceURI(files[0].getFullPath().toString(), true);
		}
		if (!reference.isRelative()) return reference;
		return GamlImportReader.resolveImport(owner, path);
	}

	private static Node resourceNode(final URI uri) {
		final var diagnostics = new ArrayList<String>();
		State state = State.AVAILABLE;
		if (uri.isFile() || uri.isPlatformResource()) {
			final IFile file = workspaceFile(uri);
			final Path disk = diskPath(uri, file);
			if (file != null && !file.isAccessible() || disk == null || !Files.isRegularFile(disk)) {
				state = State.MISSING;
				diagnostics.add("Resource is missing or inaccessible (it may be generated at runtime)");
			}
		} else {
			diagnostics.add("Remote resource; availability is not checked");
		}
		final String name = uri.lastSegment() == null ? uri.toString() : URI.decode(uri.lastSegment());
		final String path = uri.isPlatformResource() ? uri.toPlatformString(true)
				: uri.isFile() ? uri.toFileString() : uri.toString();
		return new Node(uri, name, path, state, diagnostics);
	}

	private static State withWorkspaceDiagnostics(final IFile file, final State state, final List<String> diagnostics) {
		if (file == null) return state;
		State result = state;
		try {
			for (final var marker : file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO)) {
				final int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
				if (severity != IMarker.SEVERITY_WARNING && severity != IMarker.SEVERITY_ERROR) continue;
				if (severity == IMarker.SEVERITY_ERROR) result = State.ERROR;
				else if (result == State.AVAILABLE) result = State.WARNING;
				final int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
				final String diagnostic = (line < 1 ? "" : "Line " + line + ": ")
						+ marker.getAttribute(IMarker.MESSAGE, "Workspace validation problem");
				if (!diagnostics.contains(diagnostic)) diagnostics.add(diagnostic);
			}
		} catch (final CoreException e) {
			diagnostics.add("Cannot read workspace diagnostics: " + e.getMessage());
			if (result == State.AVAILABLE) result = State.WARNING;
		}
		return result;
	}

	public static IFile workspaceFile(final URI uri) {
		if (!uri.isPlatformResource()) return null;
		return ResourcesPlugin.getWorkspace().getRoot()
				.getFile(org.eclipse.core.runtime.Path.fromPortableString(uri.toPlatformString(true)));
	}

	private static Path diskPath(final URI uri, final IFile file) {
		if (file != null) return file.getLocation() == null ? null : file.getLocation().toFile().toPath();
		return uri.isFile() ? Path.of(uri.toFileString()) : null;
	}
}
