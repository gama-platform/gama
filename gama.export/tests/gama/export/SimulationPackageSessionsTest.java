package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** JDK-only session recovery and live-player cleanup guards. */
public final class SimulationPackageSessionsTest {

	@FunctionalInterface
	private interface CheckedRunnable { void run() throws Exception; }

	private static void rejects(final CheckedRunnable runnable) throws Exception {
		try { runnable.run(); throw new AssertionError("Session cleanup must be refused"); }
		catch (final IOException expected) {
			if (expected.getMessage() == null) throw new AssertionError("Explain cleanup failures");
		}
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Files.createTempDirectory("gama-session-inventory-");
		try {
			final Path content = Files.createDirectory(root.resolve("gama-package-test"));
			final var manifest = new SimulationPackageManifest("example.test", "1.0", "test",
					List.of(new SimulationPackageManifest.Experiment("Example/model.gaml", "Run")), List.of());
			SimulationPackageSessions.register(content, root.resolve("source.gamp"), manifest);
			Files.createDirectories(content.resolve("workspace/Example"));
			Files.writeString(content.resolve("workspace/Example/results.csv"), "results");
			Files.createDirectory(root.resolve("unrelated"));
			Files.createDirectory(root.resolve("gama-package-legacy"));
			final var entries = SimulationPackageSessions.list(root);
			if (entries.size() != 1 || !entries.getFirst().id().equals("example.test") || entries.getFirst().running())
				throw new AssertionError("List only registered sessions and preserve legacy directories");
			try (var lease = SimulationPackageSessions.acquire(content)) {
				rejects(() -> SimulationPackageSessions.delete(root, content));
			}
			SimulationPackageSessions.recordProcess(content, ProcessHandle.current());
			if (!SimulationPackageSessions.list(root).getFirst().running()) throw new AssertionError("Recognize live process identity");
			rejects(() -> SimulationPackageSessions.delete(root, content));
			rejects(() -> SimulationPackageSessions.delete(root, root));
			rejects(() -> SimulationPackageSessions.delete(root, root.resolve("unrelated")));
			rejects(() -> SimulationPackageSessions.delete(root, root.resolve("gama-package-legacy")));
			if (Files.exists(root.resolve("gama-package-legacy/player-session.lock")))
				throw new AssertionError("Unregistered directories must remain untouched");
			final Path alias = root.resolve("gama-package-alias");
			Files.createSymbolicLink(alias, content);
			rejects(() -> SimulationPackageSessions.delete(root, alias));
			if (SimulationPackageSessions.list(root).size() != 1) throw new AssertionError("Ignore symlinked session roots");
			Files.delete(alias);
			if (!Files.exists(content.resolve("workspace/Example/results.csv"))) throw new AssertionError("Refused cleanup changed results");
			SimulationPackageSessions.register(content, root.resolve("source.gamp"), manifest);
			final Path outside = Files.writeString(root.resolve("outside.csv"), "preserve");
			Files.createSymbolicLink(content.resolve("external.csv"), outside);
			SimulationPackageSessions.delete(root, content);
			if (Files.exists(content) || !Files.readString(outside).equals("preserve"))
				throw new AssertionError("Delete only this session, without following symbolic links");
			System.out.println("Session inventory, live-process guards and safe cleanup checks passed");
		} finally {
			try (var paths = Files.walk(root)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
			}
		}
	}
}
