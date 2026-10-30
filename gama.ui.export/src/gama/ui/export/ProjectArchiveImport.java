package gama.ui.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import gama.export.ProjectArchive;
import gama.export.ProjectArchiveExporter;
import gama.ui.navigator.view.contents.UserGroups;
import gama.workspace.manager.WorkspaceModelsManager;

/** Imports validated staging copies; replacement retains originals and can roll back failed registration. */
public final class ProjectArchiveImport {

	public record Choice(String source, String name, boolean replace) {}
	private static final class Imported {
		final IProject project;
		final Path target, previous, backup;
		final org.eclipse.core.resources.IProjectDescription old;
		final boolean wasOpen;
		boolean unregistered, moved, created;

		Imported(final IProject project, final Path target, final Path workspace) throws CoreException {
			this.project = project;
			this.target = target;
			old = project.exists() ? project.getDescription() : null;
			wasOpen = project.isOpen();
			previous = old == null ? null : project.getLocation().toFile().toPath();
			backup = old == null ? null : workspace.resolve(".metadata/gama-project-backups")
					.resolve(java.util.UUID.randomUUID().toString()).resolve(previous.getFileName());
		}
	}

	private ProjectArchiveImport() {}

	public static List<Path> run(final ProjectArchive.Extracted archive, final List<Choice> choices,
			final String group, final IProgressMonitor monitor) throws IOException, CoreException {
		final String groupError = UserGroups.validate(group);
		if (groupError != null) throw new IOException(groupError);
		final var root = ResourcesPlugin.getWorkspace().getRoot();
		final Path workspace = root.getLocation().toFile().toPath().toRealPath();
		final var names = new java.util.HashSet<String>();
		final var targets = new LinkedHashMap<Choice, Path>();
		for (final var choice : choices) {
			if (!archive.projects().containsKey(choice.source()) || !names.add(choice.name()))
				throw new IOException("Invalid or repeated project selection");
			final IProject existing = root.getProject(choice.name());
			if (choice.replace() && existing.exists()) {
				if (existing.getPersistentProperty(WorkspaceModelsManager.BUILTIN_PROPERTY) != null)
					throw new IOException("Built-in library projects cannot be replaced; rename the imported project.");
				if (existing.getLocation() == null) throw new IOException("Existing project has no local location");
				final Path location = existing.getLocation().toFile().toPath();
				if (!location.toRealPath().startsWith(workspace) || Files.isSymbolicLink(location))
					throw new IOException("An external project cannot be replaced. Rename and import a workspace copy.");
			} else if (existing.exists()) throw new IOException("Project already exists: " + choice.name());
			final String error = UserGroups.checkProjectName(choice.name(), group, choice.replace() ? existing : null);
			if (error != null) throw new IOException(error);
			final Path target = workspace.resolve(group).resolve(choice.name()).normalize();
			if (!target.startsWith(workspace) || target.equals(workspace)) throw new IOException("Invalid project destination");
			if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
					&& (!existing.exists() || !target.equals(existing.getLocation().toFile().toPath().toAbsolutePath().normalize())))
				throw new IOException("Destination already exists: " + target);
			Path parent = target.getParent();
			while (!Files.exists(parent)) parent = parent.getParent();
			if (!parent.toRealPath().startsWith(workspace))
				throw new IOException("The destination group links outside the workspace");
			targets.put(choice, target);
		}
		// All edits are confined to extraction staging until the entire plan has been checked.
		final Map<String, String> renamed = new LinkedHashMap<>();
		for (final var choice : choices) renamed.put(choice.source(), choice.name());
		for (final var choice : choices) {
			final Path source = archive.projects().get(choice.source());
			rewrite(source, choice.name(), renamed);
		}
		final var imported = new ArrayList<Imported>();
		final var backups = new ArrayList<Path>();
		monitor.beginTask("Importing GAMA projects", choices.size());
		try {
			for (final var entry : targets.entrySet()) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				final Choice choice = entry.getKey();
				final Path target = entry.getValue();
				final IProject project = root.getProject(choice.name());
				final var item = new Imported(project, target, workspace);
				imported.add(item);
				if (item.old != null) {
					Files.createDirectories(item.backup.getParent());
					project.close(monitor);
					project.delete(false, true, monitor);
					item.unregistered = true;
					Files.move(item.previous, item.backup, StandardCopyOption.ATOMIC_MOVE);
					item.moved = true;
					backups.add(item.backup);
				}
				Files.createDirectories(target.getParent());
				Files.createDirectory(target);
				item.created = true;
				copyTree(archive.projects().get(choice.source()), target, monitor);
				final var description = ResourcesPlugin.getWorkspace().loadProjectDescription(
						org.eclipse.core.runtime.Path.fromOSString(target.resolve(".project").toString()));
				description.setName(choice.name());
				description.setLocation(target.equals(workspace.resolve(choice.name())) ? null
						: org.eclipse.core.runtime.Path.fromOSString(target.toString()));
				project.create(description, monitor);
				project.open(monitor);
				WorkspaceModelsManager.instance.setValuesProjectDescription(project, null);
				monitor.worked(1);
			}
			return List.copyOf(backups);
		} catch (final IOException | CoreException | RuntimeException e) {
			for (final var item : imported.reversed()) {
				try {
					if (item.created && item.project.exists()) item.project.delete(false, true, null);
					if (item.created) ProjectArchive.delete(item.target);
					if (item.moved) Files.move(item.backup, item.previous, StandardCopyOption.ATOMIC_MOVE);
					if (item.old != null) {
						if (item.unregistered) item.project.create(item.old, null);
						if (item.wasOpen && !item.project.isOpen()) item.project.open(null);
					}
				} catch (final IOException | CoreException rollback) { e.addSuppressed(rollback); }
			}
			throw e;
		} finally { monitor.done(); }
	}

	private static void copyTree(final Path source, final Path target, final IProgressMonitor monitor) throws IOException {
		try (var paths = Files.walk(source)) {
			for (final Path path : paths.toList()) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				final Path output = target.resolve(source.relativize(path).toString());
				if (Files.isDirectory(path)) Files.createDirectories(output);
				else Files.copy(path, output);
			}
		}
	}

	private static void rewrite(final Path project, final String name, final Map<String, String> renamed) throws IOException {
		final Path file = project.resolve(".project");
		final var document = ProjectArchive.description(Files.newInputStream(file));
		final String originalName = ProjectArchive.projectName(document);
		final var children = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final var child = children.item(i);
			if ("name".equals(child.getNodeName())) child.setTextContent(name);
			if ("projects".equals(child.getNodeName())) {
				final var refs = child.getChildNodes();
				for (int j = 0; j < refs.getLength(); j++)
					if ("project".equals(refs.item(j).getNodeName()))
						refs.item(j).setTextContent(renamed.getOrDefault(refs.item(j).getTextContent(), refs.item(j).getTextContent()));
			}
		}
		ProjectArchiveExporter.writeDescription(document, file);
		// Preserve comments and source encodings when adjusting static cross-project resource paths.
		try (var paths = Files.walk(project)) {
			for (final Path path : paths.filter(Files::isRegularFile).toList()) {
				if (!path.toString().endsWith(".gaml") && !path.toString().endsWith(".experiment")) continue;
				if (renamed.entrySet().stream().allMatch(e -> e.getKey().equals(e.getValue()))) continue;
				final String text = Files.readString(path, modelCharset(project, path));
				final String result = gama.export.GamlExportPaths.renameProjects(text, originalName,
						project.relativize(path), renamed);
				if (!result.equals(text)) Files.writeString(path, result, modelCharset(project, path));
			}
		}
	}

	private static java.nio.charset.Charset modelCharset(final Path project, final Path file) throws IOException {
		final var properties = new java.util.Properties();
		final Path settings = project.resolve(".settings/org.eclipse.core.resources.prefs");
		if (Files.exists(settings)) {
			try (var input = Files.newInputStream(settings)) { properties.load(input); }
		}
		Path relative = project.relativize(file);
		String encoding = null;
		while (relative != null && encoding == null) {
			final String path = relative.toString().replace('\\', '/');
			encoding = properties.getProperty("encoding//" + path, properties.getProperty("encoding/" + path));
			relative = relative.getParent();
		}
		if (encoding == null) encoding = properties.getProperty("encoding/<project>", ResourcesPlugin.getEncoding());
		try { return java.nio.charset.Charset.forName(encoding); }
		catch (final IllegalArgumentException e) { throw new IOException("Unsupported model encoding: " + encoding, e); }
	}
}
