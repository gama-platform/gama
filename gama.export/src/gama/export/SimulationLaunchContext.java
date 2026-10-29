package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Content locations and startup choices for one player process, independent of the installed application. */
public record SimulationLaunchContext(Path content, Path workspace, LauncherConfiguration configuration,
		List<String> selections, String defaultModel) {

	public SimulationLaunchContext {
		content = content.toAbsolutePath().normalize();
		workspace = workspace.toAbsolutePath().normalize();
		selections = List.copyOf(selections);
		if (configuration == null || selections.isEmpty() || selections.stream().anyMatch(String::isBlank))
			throw new IllegalArgumentException("A simulation launch context requires experiments and presentation settings");
		if (!configuration.startupExperiment().isBlank() && !selections.contains(configuration.startupExperiment()))
			throw new IllegalArgumentException("The configured default experiment is not exported");
	}

	public static SimulationLaunchContext fromPackage(final SimulationPackage.Extracted extracted) throws IOException {
		final Path root = extracted.content().toRealPath();
		final Path settings = root.resolve(LauncherConfiguration.FILE);
		if (!Files.isRegularFile(settings) || Files.size(settings) > 1024 * 1024)
			throw new IOException("Package launcher settings are missing or exceed 1 MiB");
		try {
			final var configuration = LauncherConfiguration.load(root);
			if (!configuration.image().isEmpty() && (!LauncherConfiguration.IMAGE.equals(configuration.image())
					|| !Files.isRegularFile(root.resolve(LauncherConfiguration.IMAGE))))
				throw new IOException("The package home image is missing or has an invalid path");
			return new SimulationLaunchContext(root, extracted.workspace(), configuration,
					extracted.manifest().experiments().stream().map(SimulationPackageManifest.Experiment::selection).toList(), "");
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid package launch configuration: " + e.getMessage(), e);
		}
	}
}
