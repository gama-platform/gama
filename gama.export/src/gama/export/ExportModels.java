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
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.URI;

import gama.api.compilation.GamlCompilationError;
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
		return discover(roots, null);
	}

	/**
	 * Discovers the export content. Statically referenced data files that cannot be found are collected and submitted
	 * to {@code proceedWithMissing}, which decides whether the export can go on without them (they may only be used in
	 * branches that are never executed).
	 *
	 * @param roots
	 *            the models to export
	 * @param proceedWithMissing
	 *            receives the descriptions of the missing data files and returns whether to proceed anyway; if
	 *            null, missing data files make the discovery fail
	 * @return the export models
	 * @throws IOException
	 *             if a model cannot be compiled or one of its imports cannot be found
	 * @throws OperationCanceledException
	 *             if {@code proceedWithMissing} refuses to proceed
	 */
	public static ExportModels discover(final List<IFile> roots, final Predicate<List<String>> proceedWithMissing)
			throws IOException {
		final List<String> missingFiles = new ArrayList<>();
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
			final List<GamlCompilationError> compilationErrors = new ArrayList<>();
			final var model = GamlModelBuilder.getInstance()
					.compile(URI.createPlatformResourceURI(file.getFullPath().toString(), true), compilationErrors);
			if (model == null) throw compilationFailure(file, compilationErrors);
			final Path parent = file.getLocation().toFile().toPath().getParent();
			try {
				final var underlyingModel = model.getDescription().getUnderlyingElement();
				final Set<Path> outputOnly = ModelDataPaths.outputOnly(underlyingModel, parent);
				final Set<Path> staticInputs = ModelDataPaths.staticInputs(underlyingModel, parent);
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

				final GamlFileInfo info = new GamlFileInfo(file);
				Set<String> files = dataFiles.get(file.getProject());
				if (files == null) {
					files = new HashSet<>();
					dataFiles.put(file.getProject(), files);
				}
				for (final String use : info.getUses()) {
					final Path path = parent.resolve(use.replace('\\', '/')).toAbsolutePath().normalize();
					if (outputOnly.contains(path)) continue;
					if (!java.nio.file.Files.exists(path)) {
						// Paths built by expressions cannot be checked statically
						if (!staticInputs.contains(path)) continue;
						final int line = ModelDataPaths.sourceLine(underlyingModel, parent, path);
						missingFiles.add("Unable to find model data file " + path + ", required by model "
								+ file.getFullPath() + (line > 0 ? " at line " + line : ""));
						continue;
					}
					files.add(path.toString());
				}
				for (final String imported : info.getImports()) {
					final IPath path = imported.startsWith("/resource/")
							? org.eclipse.core.runtime.Path.fromPortableString(
									URI.createPlatformResourceURI(imported.substring(9), false).toPlatformString(true))
							: file.getFullPath().removeLastSegments(1).append(URI.decode(imported));
					final IFile importedFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					if (!importedFile.exists()) {
						final int line = ModelDataPaths.importLine(underlyingModel, imported);
						throw new IOException("Unable to find imported model " + path + ", required by model "
								+ file.getFullPath() + (line > 0 ? " at line " + line : ""));
					}
					pending.add(importedFile);
				}
			} finally {
				model.dispose();
			}
		}
		if (experiments.isEmpty()) throw new IOException("No experiments were found in the selected models");
		if (!missingFiles.isEmpty()) {
			if (proceedWithMissing == null) throw new IOException(String.join("\n", missingFiles));
			if (!proceedWithMissing.test(List.copyOf(missingFiles))) throw new OperationCanceledException();
		}
		return new ExportModels(List.copyOf(roots), List.copyOf(experiments), List.copyOf(projects), plugins,
				dataFiles);
	}

	private static IOException compilationFailure(final IFile file, final List<GamlCompilationError> errors) {
		final List<GamlCompilationError> failures = errors.stream()
				.filter(error -> error.errorType() == GamlCompilationError.Type.Error).toList();
		if (failures.isEmpty())
			return new IOException("Cannot export " + file.getFullPath() + ": please correct its compilation errors first");
		final boolean unresolved = failures.stream().anyMatch(error -> error.message().contains("cannot be resolved")
				|| error.message().startsWith("Unknown ") || error.message().startsWith("Cannot find reference"));
		final String details = failures.stream().map(error -> {
			String source = error.uri() == null ? file.getFullPath().toString()
					: error.uri().toPlatformString(true);
			if (source == null || source.isBlank()) source = file.getFullPath().toString();
			final int line = error.location() == null ? 0 : error.location().line();
			String message = error.message();
			if (message.startsWith("Unknown ")) message = "Unable to find " + message.substring("Unknown ".length());
			else if (message.startsWith("Cannot find reference "))
				message = "Unable to find reference " + message.substring("Cannot find reference ".length());
			else if (message.startsWith("The ") && message.contains(" cannot be resolved")) {
				final int end = message.indexOf(" cannot be resolved");
				message = "Unable to find " + message.substring("The ".length(), end)
						+ " (not defined or accessible)";
			}
			return source + (line > 0 ? ":" + line : "") + ": " + message;
		}).collect(java.util.stream.Collectors.joining("\n"));
		return new IOException((unresolved ? "Unable to find required model references" : "Cannot export " + file.getFullPath())
				+ ":\n" + details);
	}

	public String defaultTitle() {
		return roots.size() == 1 ? roots.getFirst().getName().replaceFirst("(?i)\\.gaml$", "")
				: roots.getFirst().getProject().getName();
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
