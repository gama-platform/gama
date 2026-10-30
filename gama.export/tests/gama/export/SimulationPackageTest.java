package gama.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** JDK-only checks; no Equinox, workspace service, GAMA activation, or external test library is needed. */
public final class SimulationPackageTest {

	@FunctionalInterface
	private interface CheckedRunnable {
		void run() throws Exception;
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static void rejects(final CheckedRunnable runnable, final String message) throws Exception {
		try {
			runnable.run();
			throw new AssertionError(message);
		} catch (final IOException expected) {
			check(expected.getMessage() != null, "Rejections must explain the failure");
		}
	}

	private static void invalid(final CheckedRunnable runnable) throws Exception {
		try {
			runnable.run();
			throw new AssertionError("Invalid metadata must be rejected");
		} catch (final IllegalArgumentException expected) {}
	}

	private static SimulationPackageManifest manifest() {
		return new SimulationPackageManifest("org.gama.example", "1.0", "test-runtime",
				List.of(new SimulationPackageManifest.Experiment("Example/models/model.gaml", "Run"),
						new SimulationPackageManifest.Experiment("Example/models/model.gaml", "Explore")),
				List.of(new SimulationPackageManifest.BundleRequirement("gama.core", "1.2.3", ""),
						new SimulationPackageManifest.BundleRequirement("example.extension", "1.0.0", "plugins/extension.jar")));
	}

	private static void rewrite(final Path original, final Path target, final UnaryOperator<String> rename,
			final UnaryOperator<byte[]> metadata, final UnaryOperator<byte[]> payload) throws IOException {
		try (var input = new ZipFile(original.toFile()); var output = new ZipOutputStream(Files.newOutputStream(target))) {
			final var entries = input.entries();
			while (entries.hasMoreElements()) {
				final var entry = entries.nextElement();
				final String name = rename.apply(entry.getName());
				if (name == null) continue;
				final byte[] content;
				try (var stream = input.getInputStream(entry)) {
					content = stream.readAllBytes();
				}
				output.putNextEntry(new ZipEntry(name));
				output.write(SimulationPackageManifest.FILE.equals(entry.getName()) ? metadata.apply(content) : payload.apply(content));
				output.closeEntry();
			}
		}
	}

	private static byte[] changeProperty(final byte[] content, final String key, final String value) {
		try {
			final var properties = new Properties();
			properties.load(new java.io.StringReader(new String(content, StandardCharsets.UTF_8)));
			properties.setProperty(key, value);
			final var writer = new java.io.StringWriter();
			properties.store(writer, "");
			return writer.toString().getBytes(StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new AssertionError(e);
		}
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Files.createTempDirectory("gama-package-test-");
		try {
			final Path source = Files.createDirectories(root.resolve("source with spaces"));
			final Path model = Files.createDirectories(source.resolve("workspace/Example/models")).resolve("model.gaml");
			Files.writeString(model, "model example\nexperiment Run type: gui {}\n");
			Files.writeString(source.resolve("workspace/Example/.project"), "<projectDescription><name>Example</name></projectDescription>");
			Files.createDirectories(source.resolve("configuration"));
			final var home = new LauncherConfiguration("Example \u00e9", "Description\nLine 2", true,
					"Run@Example/models/model.gaml", "original-private-image.png");
			Files.writeString(source.resolve(LauncherConfiguration.FILE), home.serialize());
			Files.write(source.resolve(LauncherConfiguration.IMAGE), new byte[] { 1, 2, 3 });
			Files.createDirectories(source.resolve("plugins"));
			Files.write(source.resolve("plugins/extension.jar"), new byte[] { 4, 5, 6 });
			final Path archive = root.resolve("Example.gamx");
			SimulationPackage.write(source, archive, manifest());
			check(SimulationPackage.inspect(archive).equals(manifest()), "Manifest round trip");
			final Path sessions = Files.createDirectories(root.resolve("sessions"));
			final var extracted = SimulationPackage.extract(archive, sessions, "test-runtime");
			check(Files.readString(extracted.workspace().resolve("Example/models/model.gaml")).equals(Files.readString(model)),
					"Workspace content round trip");
			final var context = SimulationLaunchContext.fromPackage(extracted);
			check(context.configuration().title().equals(home.title()), "UTF-8 home title round trip");
			check(context.configuration().description().equals(home.description()), "Multiline description round trip");
			check(context.selections().equals(List.of("Run@Example/models/model.gaml", "Explore@Example/models/model.gaml")),
					"Explicit model paths for every experiment");
			check(!context.content().equals(source), "Player content must be isolated from author files");
			final var another = SimulationPackage.extract(archive, sessions, "test-runtime");
			check(!extracted.content().equals(another.content()), "Concurrent sessions must not share a workspace");
			Files.writeString(extracted.workspace().resolve("Example/models/model.gaml"), "changed");
			check(Files.readString(another.workspace().resolve("Example/models/model.gaml")).equals(Files.readString(model)),
					"A player cannot alter another session or the source archive");
			final long sessionsBefore = children(sessions);
			rejects(() -> SimulationPackage.extract(archive, sessions, "wrong-runtime"), "Reject incompatible runtimes");
			check(children(sessions) == sessionsBefore, "Incompatibility must not create a session");
			try (var zip = new ZipFile(archive.toFile())) {
				check(zip.size() == 6, "Only manifest, project, model, launcher settings, image and plugin are packaged");
			}

			invalid(() -> new SimulationPackageManifest.Experiment("../outside.gaml", "Run"));
			invalid(() -> new SimulationPackageManifest.Experiment("Example@Other/model.gaml", "Run"));
			invalid(() -> new SimulationPackageManifest.Experiment("Example/model#Other.gaml", "Run"));
			invalid(() -> new SimulationPackageManifest.Experiment("Example/model.gaml", "Run#Other"));
			invalid(() -> new SimulationPackageManifest.BundleRequirement("example", "[1,2)", ""));
			invalid(() -> new SimulationPackageManifest.BundleRequirement("example", "1.0.0", "../plugin.jar"));
			invalid(() -> new SimulationPackageManifest("id", "1", "runtime", List.of(), List.of()));
			invalid(() -> new SimulationPackageManifest("id", "1", "runtime",
					List.of(manifest().experiments().getFirst(), manifest().experiments().getFirst()), List.of()));
			invalid(() -> new SimulationLaunchContext(source, source, LauncherConfiguration.defaults(), List.of(""), ""));
			for (final String path : List.of("../file", "/absolute", "C:/file", "workspace\\file", "workspace/./file",
					"workspace//file", "workspace/.metadata/state", "workspace/CON.txt", "workspace/trailing.",
					"workspace/trailing ", "workspace/control\u0001", "workspace/two\nnew\nlines")) {
				invalid(() -> SimulationPackage.validatePath(path));
			}

			final Path bad = root.resolve("bad.gamx");
			rewrite(archive, bad, UnaryOperator.identity(), bytes -> changeProperty(bytes, "format.version", "99"), UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject future formats");
			rewrite(archive, bad, UnaryOperator.identity(), bytes -> changeProperty(bytes, "file.0.path", "workspace/../escape"),
					UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject traversal in the manifest inventory");
			rewrite(archive, bad, UnaryOperator.identity(), bytes -> {
				byte[] changed = changeProperty(bytes, "file.0.path", "workspace/Example/first");
				return changeProperty(changed, "file.1.path", "workspace/example/second");
			}, UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject differently cased ancestor directories on every OS");
			rewrite(archive, bad, UnaryOperator.identity(), bytes -> {
				byte[] changed = changeProperty(bytes, "file.0.path", "workspace/Example/caf\u00e9");
				return changeProperty(changed, "file.1.path", "workspace/Example/cafe\u0301");
			}, UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject Unicode normalization collisions");
			rewrite(archive, bad, name -> name.equals("plugins/extension.jar") ? "plugins/undeclared.jar" : name,
					UnaryOperator.identity(), UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject undeclared ZIP payload");
			rewrite(archive, bad, name -> name.equals("plugins/extension.jar") ? "../escaped.jar" : name,
					UnaryOperator.identity(), UnaryOperator.identity());
			rejects(() -> SimulationPackage.extract(bad, sessions, "test-runtime"), "Reject traversal");
			check(!Files.exists(root.resolve("escaped.jar")), "No path escapes");
			rewrite(archive, bad, name -> name.equals("plugins/extension.jar") ? null : name, UnaryOperator.identity(), UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject missing payload");
			rewrite(archive, bad, UnaryOperator.identity(), bytes -> changeProperty(bytes, "files.count", "999999999"),
					UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject excessive declared file counts");
			rewrite(archive, bad, UnaryOperator.identity(), bytes -> changeProperty(bytes, "file.0.size", "-1"), UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Reject negative sizes");
			rewrite(archive, bad, UnaryOperator.identity(), bytes -> new byte[1024 * 1024 + 1], UnaryOperator.identity());
			rejects(() -> SimulationPackage.inspect(bad), "Enforce actual decompressed manifest size");
			rewrite(archive, bad, UnaryOperator.identity(), UnaryOperator.identity(), bytes -> {
				final byte[] changed = bytes.clone();
				if (changed.length > 0) changed[0] ^= 1;
				return changed;
			});
			rejects(() -> SimulationPackage.extract(bad, sessions, "test-runtime"), "Reject same-length checksum corruption");
			check(children(sessions) == sessionsBefore, "Failed extraction must remove its own temporary directory");
			rejects(() -> SimulationPackage.extract(archive, sessions, "test-runtime", new SimulationPackage.Limits(4, 1000, 10000)),
					"Enforce file-count threshold");
			final long biggest;
			final long total;
			try (var zip = new ZipFile(archive.toFile())) {
				biggest = zip.stream().filter(entry -> !entry.getName().equals(SimulationPackageManifest.FILE))
						.mapToLong(ZipEntry::getSize).max().orElseThrow();
				total = zip.stream().filter(entry -> !entry.getName().equals(SimulationPackageManifest.FILE))
						.mapToLong(ZipEntry::getSize).sum();
			}
			SimulationPackage.extract(archive, sessions, "test-runtime", new SimulationPackage.Limits(5, biggest, total));
			rejects(() -> SimulationPackage.extract(archive, sessions, "test-runtime", new SimulationPackage.Limits(5, biggest - 1, total)),
					"Enforce per-file byte threshold");
			rejects(() -> SimulationPackage.extract(archive, sessions, "test-runtime", new SimulationPackage.Limits(5, biggest, total - 1)),
					"Enforce total-byte threshold");

			final byte[] previousArchive = Files.readAllBytes(archive);
			final Path link = source.resolve("workspace/Example/link");
			Files.createSymbolicLink(link, model);
			rejects(() -> SimulationPackage.write(source, archive, manifest()), "Reject source links");
			check(java.util.Arrays.equals(previousArchive, Files.readAllBytes(archive)), "Failed export preserves existing output");
			Files.delete(link);
			Files.writeString(source.resolve("unexpected.ini"), "do not execute");
			rejects(() -> SimulationPackage.write(source, archive, manifest()), "Reject arbitrary runtime configuration");
			Files.delete(source.resolve("unexpected.ini"));
			rejects(() -> SimulationPackage.write(source, source.resolve("nested.gamx"), manifest()), "Reject output inside source");
			Files.delete(source.resolve("plugins/extension.jar"));
			rejects(() -> SimulationPackage.write(source, archive, manifest()), "Reject missing declared plugin");
			check(java.util.Arrays.equals(previousArchive, Files.readAllBytes(archive)), "All failed exports preserve output");

			Files.writeString(extracted.content().resolve(LauncherConfiguration.FILE), "image=../../outside.png\n");
			rejects(() -> SimulationLaunchContext.fromPackage(extracted), "Reject external home image paths");
			Files.writeString(extracted.content().resolve(LauncherConfiguration.FILE), "startupExperiment=NotExported\n");
			rejects(() -> SimulationLaunchContext.fromPackage(extracted), "Reject unknown startup experiment");
			Files.writeString(extracted.content().resolve(LauncherConfiguration.FILE), "title=\\uInvalid\n");
			rejects(() -> SimulationLaunchContext.fromPackage(extracted), "Report malformed launcher properties as an I/O failure");
			Files.write(extracted.content().resolve(LauncherConfiguration.FILE), new byte[1024 * 1024 + 1]);
			rejects(() -> SimulationLaunchContext.fromPackage(extracted), "Bound launcher settings before parsing");
		} finally {
			try (var paths = Files.walk(root)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
			}
		}
		System.out.println("Playable model manifest, archive and isolation checks passed");
	}

	private static long children(final Path directory) throws IOException {
		try (var children = Files.list(directory)) { return children.count(); }
	}
}
