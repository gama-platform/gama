package gama.export;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Entry point loaded by the native launcher's Java bridge before Equinox exists. */
public final class PackageBootstrap {

	private PackageBootstrap() {}

	/** Uses JDK types only across the isolated bootstrap class-loader boundary. */
	public static String[] prepare(final String archive, final String installation, final String launcher,
			final String runtime) throws Exception {
		final var prepared = SimulationPackageLauncher.prepare(Path.of(archive), SimulationPackageSessions.directory(),
				Path.of(installation), Path.of(launcher), runtime);
		try (var lease = SimulationPackageSessions.acquire(prepared.content())) {
			SimulationPackageSessions.recordProcess(prepared.content(), ProcessHandle.current());
		}
		final var result = new ArrayList<>(prepared.command());
		result.add("-Dgama.package.bootstrap.pid=" + ProcessHandle.current().pid());
		result.addAll(List.of(presentation(prepared.content().toString())));
		return result.toArray(String[]::new);
	}

	public static String[] presentation(final String content) throws Exception {
		final var root = Path.of(content);
		final var context = SimulationLaunchContext.fromPackage(SimulationPackage.Extracted.open(root));
		final var result = new ArrayList<>(List.of("-Dgama.package.title=" + context.configuration().title(),
				"-Dgama.package.description=" + context.configuration().description()));
		if (!context.configuration().image().isBlank())
			result.add("-Dgama.package.splash=" + root.resolve(LauncherConfiguration.IMAGE));
		return result.toArray(String[]::new);
	}
}
