package gama.export;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import gama.api.runtime.SystemInfo;

/** Bridges Eclipse resource discovery to the runtime-independent simulation package format. */
public final class SimulationPackageExporter {

	public record Dependency(String id, String version, Path file, boolean embeddable, boolean standard, String reason) {
		public boolean automatic() { return !standard && embeddable; }
	}

	private SimulationPackageExporter() {}

	public static List<Dependency> dependencies(final ExportModels models, final IProgressMonitor monitor) throws IOException {
		final var result = new ArrayList<Dependency>();
		final Set<String> standard = StandardDistribution.bundles();
		final var roots = new ArrayList<Bundle>();
		for (final String name : models.plugins().stream().sorted().toList()) {
			final Bundle bundle = Platform.getBundle(name);
			if (bundle == null) throw new IOException("Required plugin is not installed: " + name);
			roots.add(bundle);
		}
		for (final Bundle bundle : closure(roots, monitor)) result.add(dependency(bundle, standard));
		return result.stream().sorted(Comparator.comparing(Dependency::automatic).reversed().thenComparing(Dependency::id)).toList();
	}

	private static Dependency dependency(final Bundle bundle, final Set<String> standard) {
		final Path file = FileLocator.getBundleFileLocation(bundle).map(java.io.File::toPath).orElse(null);
		if (standard.contains(bundle.getSymbolicName()))
			return new Dependency(bundle.getSymbolicName(), bundle.getVersion().toString(), file, false, true,
					"Included in standard GAMA; supplied by the recipient at this version or newer");
		String reason = "";
		if (file == null || !Files.isRegularFile(file)) {
			reason = "This plugin is not an installed JAR; build and install its plugin JAR before packaging";
		} else {
			try {
				SimulationPackageLauncher.validatePlugin(file, new SimulationPackageManifest.BundleRequirement(
						bundle.getSymbolicName(), bundle.getVersion().toString(), "plugins/dependency.jar"));
			} catch (final IOException e) {
				reason = e.getMessage();
			}
		}
		return new Dependency(bundle.getSymbolicName(), bundle.getVersion().toString(), file,
				reason.isEmpty(), false, reason.isEmpty() ? "Required non-standard plugin; automatically included in the package" : reason);
	}

	public static void export(final ExportModels models, final List<ExportModels.Experiment> experiments,
			final LauncherConfiguration configuration, final String id, final String version, final List<Path> plugins,
			final Path destination, final IProgressMonitor monitor) throws IOException, CoreException {
		monitor.beginTask("Exporting model", IProgressMonitor.UNKNOWN);
		Path staging = null;
		try {
			checkCanceled(monitor);
			final var selected = experiments.stream().map(experiment -> new SimulationPackageManifest.Experiment(
					experiment.modelPath().replaceFirst("^/", ""), experiment.name())).toList();
			if (selected.isEmpty() || !models.experiments().containsAll(experiments))
				throw new IOException("Choose experiments discovered in the selected models");
			final var sources = new ArrayList<PackageProjectStager.Source>();
			final var data = new LinkedHashMap<String, Set<Path>>();
			final Path output = destination.toAbsolutePath().normalize();
			if (output.getParent() == null || !output.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".gamp"))
				throw new IOException("Choose a package destination file with the .gamp extension");
			final Path realOutput = canonicalDestination(output);
			final var installation = Platform.getInstallLocation().getURL();
			if (installation.getProtocol().equals("file")
					&& realOutput.startsWith(Path.of(java.net.URI.create(installation.toExternalForm())).toRealPath()))
				throw new IOException("The package destination must be outside the GAMA installation");
			if (!configuration.image().isBlank() && Files.exists(output)
					&& Files.isSameFile(output, Path.of(configuration.image())))
				throw new IOException("The package destination cannot replace its presentation image");
			for (final var project : models.projects()) {
				checkCanceled(monitor);
				if (project.getLocation() == null) throw new IOException("The project has no local location: " + project.getName());
				if (realOutput.startsWith(project.getLocation().toFile().toPath().toRealPath()))
					throw new IOException("The package destination must be outside the exported projects");
				collect(project, sources, new HashSet<>(), realOutput, monitor);
				final var files = new LinkedHashSet<Path>();
				for (final String path : models.dataFiles().getOrDefault(project, Set.of())) {
					if (path == null || path.isBlank()) throw new IOException("An exported model has an empty data dependency");
					Path dataFile = Path.of(path).toAbsolutePath().normalize();
					final Path projectPath = project.getLocation().toFile().toPath().toAbsolutePath().normalize();
					if (dataFile.startsWith(projectPath)) {
						final var resource = project.findMember(projectPath.relativize(dataFile).toString());
						if (resource != null && resource.getLocation() != null)
							dataFile = resource.getLocation().toFile().toPath().toAbsolutePath().normalize();
					}
					if ((Files.isDirectory(dataFile) && realOutput.startsWith(dataFile.toRealPath()))
							|| (Files.exists(output) && Files.exists(dataFile) && Files.isSameFile(output, dataFile)))
						throw new IOException("The package destination must not replace or be inside model data");
					files.add(dataFile);
				}
				data.put(project.getName(), files);
			}
			for (final var source : sources) {
				if (Files.exists(output) && Files.isSameFile(output, source.file()))
					throw new IOException("The package destination would overwrite a project resource: " + source.file());
			}
			monitor.subTask("Resolving plugin dependencies");
			final Set<String> standard = StandardDistribution.bundles();
			final var embedded = new LinkedHashMap<String, Path>();
			final var roots = new ArrayList<Bundle>();
			for (final String name : models.plugins().stream().sorted().toList()) {
				final var bundle = Platform.getBundle(name);
				if (bundle == null) throw new IOException("Required plugin is not installed: " + name);
				roots.add(bundle);
			}
			for (final Path input : plugins) {
				checkCanceled(monitor);
				final Path file = input.toRealPath();
				final String name;
				final String bundleVersion;
				try (var jar = new JarFile(file.toFile())) {
					final var manifest = jar.getManifest();
					if (manifest == null) throw new IOException("Missing plugin manifest: " + file);
					final String header = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
					if (header == null) throw new IOException("Missing plugin symbolic name: " + file);
					name = header.split(";", 2)[0].trim();
					bundleVersion = manifest.getMainAttributes().getValue("Bundle-Version");
				}
				final var bundle = Platform.getBundle(name);
				if (bundle == null || !bundle.getVersion().toString().equals(bundleVersion)
						|| !Files.isSameFile(file, FileLocator.getBundleFileLocation(bundle)
								.orElseThrow(() -> new IOException("Plugin has no installed file: " + name)).toPath()))
					throw new IOException("Select the installed JAR for plugin " + name + "; arbitrary JARs are not exported");
				if (embedded.putIfAbsent(name, file) != null) throw new IOException("Plugin selected twice: " + name);
				roots.add(bundle);
			}
			final var requirements = new ArrayList<SimulationPackageManifest.BundleRequirement>();
			for (final Bundle bundle : closure(roots, monitor)) {
				checkCanceled(monitor);
				if (standard.contains(bundle.getSymbolicName()) && embedded.containsKey(bundle.getSymbolicName()))
					throw new IOException("Standard GAMA plugins must be supplied by the host: " + bundle.getSymbolicName());
				final Dependency dependency = dependency(bundle, standard);
				if (!dependency.standard() && !dependency.embeddable())
					throw new IOException("Cannot package required non-standard plugin " + dependency.id() + ": " + dependency.reason());
				if (dependency.automatic()) embedded.putIfAbsent(dependency.id(), dependency.file().toRealPath());
				final String artifact = embedded.containsKey(bundle.getSymbolicName())
						? "plugins/" + bundle.getSymbolicName() + "_" + bundle.getVersion() + ".jar" : "";
				final var requirement = new SimulationPackageManifest.BundleRequirement(bundle.getSymbolicName(),
						bundle.getVersion().toString(), artifact);
				if (!artifact.isEmpty()) SimulationPackageLauncher.validatePlugin(embedded.get(bundle.getSymbolicName()), requirement);
				requirements.add(requirement);
			}
			final var manifest = new SimulationPackageManifest(id, version, SystemInfo.VERSION_NUMBER, selected, requirements);
			if (!configuration.startupExperiment().isBlank()
					&& selected.stream().noneMatch(experiment -> experiment.selection().equals(configuration.startupExperiment())))
				throw new IOException("The default experiment is not selected for export");
			staging = Files.createTempDirectory("gama-package-export-");
			monitor.subTask("Copying projects and model data");
			PackageProjectStager.stage(staging, sources, data, monitor::isCanceled);
			Files.createDirectories(staging.resolve("configuration"));
			Files.writeString(staging.resolve(LauncherConfiguration.FILE), configuration.serialize());
			if (!configuration.image().isBlank()) {
				PackageProjectStager.copy(Path.of(configuration.image()), staging.resolve(LauncherConfiguration.IMAGE),
						monitor::isCanceled);
			}
			for (final var requirement : requirements) {
				checkCanceled(monitor);
				if (requirement.artifact().isEmpty()) continue;
				Files.createDirectories(staging.resolve("plugins"));
				PackageProjectStager.copy(embedded.get(requirement.symbolicName()), staging.resolve(requirement.artifact()),
						monitor::isCanceled);
			}
			monitor.subTask("Writing exported model");
			SimulationPackage.write(staging, output, manifest, monitor::isCanceled);
		} catch (final java.io.InterruptedIOException e) {
			throw new OperationCanceledException(e.getMessage());
		} finally {
			try {
				if (staging != null) {
					try (var paths = Files.walk(staging)) {
						for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
					}
				}
			} finally {
				monitor.done();
			}
		}
	}

	private static List<Bundle> closure(final List<Bundle> input, final IProgressMonitor monitor) throws IOException {
		final var pending = new ArrayList<>(input);
		final var result = new ArrayList<Bundle>();
		final var visited = new HashSet<Bundle>();
		final var identities = new HashSet<String>();
		for (int i = 0; i < pending.size(); i++) {
			checkCanceled(monitor);
			final Bundle bundle = pending.get(i);
			if (bundle.getBundleId() == 0 || !visited.add(bundle)) continue;
			if (!identities.add(bundle.getSymbolicName()))
				throw new IOException("Export requires multiple versions of " + bundle.getSymbolicName()
						+ "; this package format currently declares one version per bundle");
			final var wiring = bundle.adapt(BundleWiring.class);
			if (wiring == null) throw new IOException("Plugin is not resolved: " + bundle.getSymbolicName());
			for (final var wire : wiring.getRequiredWires(null)) pending.add(wire.getProviderWiring().getBundle());
			result.add(bundle);
		}
		return result;
	}

	private static void collect(final IContainer container, final List<PackageProjectStager.Source> sources,
			final Set<Path> ancestors, final Path output, final IProgressMonitor monitor) throws IOException, CoreException {
		checkCanceled(monitor);
		final Path location = container.getLocation() == null ? null : container.getLocation().toFile().toPath();
		if (location != null && (!ancestors.add(location.toRealPath()) || Files.isSymbolicLink(location)))
			throw new IOException("Project resources contain a linked directory cycle or symbolic link: " + container.getFullPath());
		if (location != null && output.startsWith(location.toRealPath()))
			throw new IOException("The package destination must be outside project resource folders: " + container.getFullPath());
		try {
			for (final IResource resource : container.members()) {
				checkCanceled(monitor);
				if (resource.getName().equals(".metadata")) continue;
				if (resource instanceof IContainer child) {
					collect(child, sources, ancestors, output, monitor);
				} else if (resource instanceof IFile file) {
					if (file.getLocation() == null) throw new IOException("Cannot export non-local resource: " + file.getFullPath());
					final String name = file.getName().toLowerCase(java.util.Locale.ROOT);
					final Charset charset = name.endsWith(".gaml") || name.endsWith(".experiment")
							? Charset.forName(file.getCharset()) : StandardCharsets.UTF_8;
					sources.add(new PackageProjectStager.Source(file.getLocation().toFile().toPath(),
							file.getFullPath().toPortableString().substring(1), charset));
				}
			}
		} finally {
			if (location != null) ancestors.remove(location.toRealPath());
		}
	}

	private static Path canonicalDestination(final Path output) throws IOException {
		Path existing = output.getParent();
		while (!Files.exists(existing)) existing = existing.getParent();
		return existing.toRealPath().resolve(existing.relativize(output));
	}

	private static void checkCanceled(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) throw new OperationCanceledException();
	}
}
