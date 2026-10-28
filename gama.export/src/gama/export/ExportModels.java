/*******************************************************************************************************
 *
 * ExportModels.java, in gama.export, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.export;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;

import gama.api.constants.GamlFileExtension;
import gama.api.utils.GamlProperties;
import gaml.compiler.resource.GamlFileInfo;
import gaml.compiler.validation.GamlModelBuilder;

/** Discovers the experiments, projects, plugins and data required by an export. */
public record ExportModels(List<IFile> roots, List<Experiment> experiments, List<IProject> projects,
		Set<String> plugins, Map<IProject, Set<String>> dataFiles) {

	/**
	 * The Record Experiment.
	 *
	 * @param name
	 *            the name
	 * @param modelPath
	 *            the model path
	 */
	public record Experiment(String name, String modelPath) {

		/**
		 * Label.
		 *
		 * @param multipleModels
		 *            the multiple models
		 * @return the string
		 */
		public String label(final boolean multipleModels) {
			return multipleModels ? name + "    from model    " + modelPath : name;
		}

		/**
		 * Launcher selection.
		 *
		 * @param multipleModels
		 *            the multiple models
		 * @return the string
		 */
		public String launcherSelection(final boolean multipleModels) {
			return multipleModels ? name + "@" + modelPath : name;
		}
	}

	/**
	 * Model files.
	 *
	 * @param container
	 *            the container
	 * @return the list
	 * @throws CoreException
	 *             the core exception
	 */
	public static List<IFile> modelFiles(final IContainer container) throws CoreException {
		final List<IFile> result = new ArrayList<>();
		for (final IResource resource : container.members()) {
			if (resource instanceof IContainer child) {
				result.addAll(modelFiles(child));
			} else if (resource instanceof IFile file
					&& (GamlFileExtension.isGaml(file.getName()) || GamlFileExtension.isExperiment(file.getName()))) {
				result.add(file);
			}
		}
		return result;
	}

	/**
	 * Discover.
	 *
	 * @param roots
	 *            the roots
	 * @return the export models
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static ExportModels discover(final List<IFile> roots) throws IOException {
		if (roots.isEmpty()) throw new IOException("No GAML models were found in the selection");
		final List<Experiment> experiments = new ArrayList<>();
		final Set<IProject> projects = new LinkedHashSet<>();
		final Set<String> plugins = new HashSet<>();
		final Map<IProject, Set<String>> dataFiles = new HashMap<>();
		final Set<IPath> visited = new HashSet<>();
		final List<IFile> pending = new ArrayList<>(roots);
		for (int index = 0; index < pending.size(); index++) {
			final IFile file = pending.get(index);
			if (!visited.add(file.getFullPath())) { continue; }
			final var model = GamlModelBuilder.getInstance()
					.compile(URI.createPlatformResourceURI(file.getFullPath().toString(), true), null);
			if (model == null) throw new IOException(
					"Cannot export " + file.getFullPath() + ": please correct its compilation errors first");
			try {
				projects.add(file.getProject());
				final GamlProperties properties = new GamlProperties();
				model.getDescription().collectMetaInformation(properties);
				final Set<String> required = properties.get(GamlProperties.PLUGINS);
				if (required != null) { plugins.addAll(required); }
				if (roots.contains(file)) {
					for (final var experiment : model.getExperiments()) {
						if (!experiment.getDescription().isAbstract())
							experiments.add(new Experiment(experiment.getName(), file.getFullPath().toString()));
					}
				}
			} finally {
				model.dispose();
			}
			final GamlFileInfo info = new GamlFileInfo(file);
			final Path parent = file.getLocation().toFile().toPath().getParent();
			final Set<String> files = dataFiles.computeIfAbsent(file.getProject(), key -> new HashSet<>());
			for (final String use : info.getUses()) { files.add(parent.resolve(use).normalize().toString()); }
			for (final String imported : info.getImports()) {
				final IPath path = imported.startsWith("/resource/")
						? org.eclipse.core.runtime.Path.fromPortableString(
								URI.createPlatformResourceURI(imported.substring(9), false).toPlatformString(true))
						: file.getFullPath().removeLastSegments(1).append(URI.decode(imported));
				pending.add(ResourcesPlugin.getWorkspace().getRoot().getFile(path));
			}
		}
		if (experiments.isEmpty()) throw new IOException("No experiments were found in the selected models");
		return new ExportModels(List.copyOf(roots), List.copyOf(experiments), List.copyOf(projects), plugins,
				dataFiles);
	}

	public String defaultTitle() {
		return roots.size() == 1 ? roots.getFirst().getName() : roots.getFirst().getProject().getName();
	}

	public String defaultDescription() throws IOException, CoreException {
		if (roots.size() != 1) return "";
		final IFile file = roots.getFirst();
		try (var input = file.getContents()) {
			return leadingDescription(new String(input.readAllBytes(), Charset.forName(file.getCharset())));
		}
	}

	/** Description text from leading model comments, excluding conventional header metadata. */
	public static String leadingDescription(final String source) {
		final var comments = Pattern.compile("\\G\\s*(?://([^\\r\\n]*)|/\\*(.*?)\\*/)", Pattern.DOTALL)
				.matcher(source.startsWith("\uFEFF") ? source.substring(1) : source);
		final List<String> lines = new ArrayList<>();
		while (comments.find()) {
			final String body = comments.group(1) == null ? comments.group(2) : comments.group(1);
			for (final String raw : body.split("\\R", -1)) {
				final String line = raw.strip().replaceFirst("^\\*\\s?", "");
				if (line.matches("(?i)^(name|author|tags):.*")) continue;
				lines.add(line.replaceFirst("(?i)^description:\\s*", ""));
			}
		}
		return String.join("\n", lines).strip();
	}
}
