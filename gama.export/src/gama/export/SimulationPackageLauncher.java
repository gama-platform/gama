package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

/** Prepares an isolated Equinox runtime, either before startup or for an additional native player process. */
public final class SimulationPackageLauncher {

	public static final String CONTENT_PROPERTY = "gama.package.content";
	public static final String APPLICATION = "gama.export.PackageApplication";
	private static final String INVENTORY = "org.eclipse.equinox.simpleconfigurator/bundles.info";

	public record Prepared(Path content, Path configuration, Path workspace, List<String> command) {
		public Prepared { command = List.copyOf(command); }

		public Process start() throws IOException {
			try (var lease = SimulationPackageSessions.acquire(content)) {
				final var arguments = new ArrayList<>(command);
				arguments.add("-Dgama.launcher.suppressErrors=true");
				final Process process = new ProcessBuilder(arguments).directory(content.toFile()).redirectErrorStream(true)
						.redirectOutput(content.resolve("player.log").toFile()).start();
				try {
					SimulationPackageSessions.recordProcess(content, process.toHandle());
					return process;
				} catch (final IOException e) {
					process.destroy();
					throw e;
				}
			} catch (final IOException e) {
				throw new IOException("Unable to start the player. The prepared session is retained at " + content, e);
			}
		}
	}

	private record Bundle(String id, String version, Path file, String level, boolean start) {
		String line() {
			return String.join(",", id, version, location(file), level, Boolean.toString(start));
		}
	}

	private SimulationPackageLauncher() {}

	/** Successful sessions, including their logs and generated results, are retained for recovery. */
	public static Prepared prepare(final Path archive, final Path sessions, final Path installation,
			final Path launcher, final String runtimeVersion) throws IOException {
		final Path install = installation.toRealPath();
		final Path executable = launcher.toRealPath();
		if (!Files.isExecutable(executable)) throw new IOException("The GAMA launcher is not executable: " + executable);
		final var bundles = inventory(install);
		bundle(bundles, "gama.export");
		bundle(bundles, "gama.ui.export");
		final var extracted = SimulationPackage.extract(archive, sessions, runtimeVersion);
		final Path content = extracted.content();
		try {
			SimulationLaunchContext.fromPackage(extracted);
			for (final var required : extracted.manifest().bundles()) {
				final var existing = bundles.values().stream().filter(bundle -> bundle.id().equals(required.symbolicName())).toList();
				if (!existing.isEmpty()) {
					if (existing.stream().noneMatch(bundle -> required.accepts(bundle.version())))
						throw new IOException("Bundle " + required.symbolicName() + " requires " + required.version() + " or newer"
								+ "; the host supplies " + existing.stream().map(Bundle::version).toList());
					if (!required.artifact().isEmpty())
						throw new IOException("A playable model cannot replace an installed bundle: " + required.symbolicName());
				} else {
					if (required.artifact().isEmpty()) throw new IOException("Missing host bundle: " + required.symbolicName());
					final Path file = content.resolve(required.artifact());
					validatePlugin(file, required);
					bundles.put(required.symbolicName() + ":" + required.version(),
							new Bundle(required.symbolicName(), required.version(), file, "4", false));
				}
			}
			final Path configuration = Files.createDirectory(content.resolve("runtime-configuration"));
			final Path info = configuration.resolve(INVENTORY);
			Files.createDirectories(info.getParent());
			Files.writeString(info, "#version=1\n" + String.join("\n", bundles.values().stream().map(Bundle::line).toList()) + "\n");
			final Properties properties = new Properties();
			try (var reader = Files.newBufferedReader(install.resolve("configuration/config.ini"))) { properties.load(reader); }
			properties.remove("osgi.sharedConfiguration.area");
			properties.remove("osgi.configuration.area");
			properties.remove("osgi.instance.area");
			properties.setProperty("osgi.user.area", configuration.resolve("user").toUri().toASCIIString());
			properties.remove("osgi.framework.extensions");
			properties.remove("eclipse.p2.profile");
			properties.setProperty("eclipse.p2.data.area", configuration.resolve("p2").toUri().toASCIIString());
			properties.setProperty("eclipse.application", APPLICATION);
			properties.setProperty("osgi.install.area", install.toUri().toASCIIString());
			properties.setProperty("org.eclipse.equinox.simpleconfigurator.configUrl", info.toUri().toASCIIString());
			properties.setProperty("org.eclipse.equinox.simpleconfigurator.exclusiveInstallation", "true");
			properties.setProperty("osgi.framework", location(bundle(bundles, "org.eclipse.osgi").file()));
			final var common = bundle(bundles, "org.eclipse.equinox.common");
			final var configurator = bundle(bundles, "org.eclipse.equinox.simpleconfigurator");
			properties.setProperty("osgi.bundles", "reference:" + location(common.file())
					+ "@1:start,reference:" + location(configurator.file()) + "@1:start");
			final var compatibility = bundles.values().stream()
					.filter(bundle -> bundle.id().equals("org.eclipse.osgi.compatibility.state")).findFirst().orElse(null);
			if (compatibility != null)
				properties.setProperty("osgi.framework.extensions", "reference:" + location(compatibility.file()));
			try (var writer = Files.newBufferedWriter(configuration.resolve("config.ini"))) {
				properties.store(writer, "Private GAMA playable model configuration");
			}
			final var command = new ArrayList<>(List.of(executable.toString(),
					"--launcher.appendVmargs", "--launcher.suppressErrors", "-consoleLog",
					"-install", install.toString(), "-configuration", configuration.toString(),
					"-data", extracted.workspace().toString(), "-application", APPLICATION, "-clearPersistedState",
					"-vmargs", "-D" + CONTENT_PROPERTY + "=" + content, "-Dexported_runner=true",
					"-Dgama.package.runtime=" + runtimeVersion,
					"-Duse_global_preference_store=false", "-Dpref_startup_model=true",
					"-Dpref_enable_server=false",
					"-Dpref_errors_in_editor=false", "-Dorg.eclipse.equinox.p2.reconciler.dropins.directory="
							+ configuration.resolve("disabled-dropins")));
			SimulationPackageSessions.register(content, archive, extracted.manifest());
			return new Prepared(content, configuration, extracted.workspace(), command);
		} catch (final IOException | RuntimeException e) {
			try (var paths = Files.walk(content)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
			} catch (final IOException cleanup) {
				e.addSuppressed(cleanup);
			}
			throw e;
		}
	}

	private static Bundle bundle(final Map<String, Bundle> bundles, final String id) throws IOException {
		final var bundle = bundles.values().stream().filter(candidate -> candidate.id().equals(id)).findFirst().orElse(null);
		if (bundle == null) throw new IOException("The host is missing " + id);
		return bundle;
	}

	private static String location(final Path path) {
		return path.toUri().toASCIIString().replace(",", "%2C");
	}

	private static Map<String, Bundle> inventory(final Path install) throws IOException {
		final var result = new LinkedHashMap<String, Bundle>();
		for (final String line : Files.readAllLines(install.resolve("configuration").resolve(INVENTORY))) {
			if (line.isBlank() || line.startsWith("#")) continue;
			final String[] parts = line.split(",", -1);
			if (parts.length != 5) throw new IOException("Invalid installed bundle inventory line: " + line);
			final Path path;
			try {
				final var uri = java.net.URI.create(parts[2]);
				path = (uri.isAbsolute() ? Path.of(uri) : install.resolve(parts[2])).toRealPath();
				Integer.parseInt(parts[3]);
			} catch (final IllegalArgumentException e) {
				throw new IOException("Invalid installed bundle location or start level: " + line, e);
			}
			if (!parts[4].equals("true") && !parts[4].equals("false"))
				throw new IOException("Invalid installed bundle start flag: " + line);
			// Package sessions use an explicit inventory, never p2/dropins discovery.
			final boolean start = Boolean.parseBoolean(parts[4])
					&& !parts[0].equals("org.eclipse.equinox.p2.reconciler.dropins");
			if (result.put(parts[0] + ":" + parts[1], new Bundle(parts[0], parts[1], path, parts[3], start)) != null)
				throw new IOException("Duplicate installed bundle identity: " + parts[0] + " " + parts[1]);
		}
		return result;
	}

	static void validatePlugin(final Path file, final SimulationPackageManifest.BundleRequirement required)
			throws IOException {
		if (required.symbolicName().startsWith("org.eclipse.") || required.symbolicName().startsWith("org.osgi.")
				|| required.symbolicName().startsWith("gaml.") || required.symbolicName().equals("gama.api")
				|| required.symbolicName().equals("gama.core") || required.symbolicName().equals("gama.dependencies")
				|| required.symbolicName().equals("gama.workspace") || required.symbolicName().equals("gama.dev")
				|| required.symbolicName().equals("gama.export") || required.symbolicName().equals("gama.ui.application")
				|| required.symbolicName().equals("gama.ui.export"))
			throw new IOException("Playable models cannot supply platform bundles: " + required.symbolicName());
		try (var jar = new JarFile(file.toFile())) {
			final var manifest = jar.getManifest();
			if (manifest == null) throw new IOException("Missing plugin manifest: " + file.getFileName());
			final var attributes = manifest.getMainAttributes();
			final String id = attributes.getValue("Bundle-SymbolicName");
			if (id == null || !required.symbolicName().equals(id.split(";", 2)[0].trim())
					|| !required.version().equals(attributes.getValue("Bundle-Version")))
				throw new IOException("Plugin identity does not match playable model metadata: " + file.getFileName());
			if (jar.getJarEntry("plugin.xml") != null && !id.matches(".*;\\s*singleton\\s*:=\\s*\"?true\"?(?:\\s*;.*|\\s*)"))
				throw new IOException("Plugins contributing Eclipse extensions must be singleton bundles: " + required.symbolicName());
			if (attributes.getValue("Fragment-Host") != null || attributes.getValue("Bundle-NativeCode") != null)
				throw new IOException("Playable model fragments and native plugins are not supported yet: " + required.symbolicName());
			final var entries = jar.entries();
			while (entries.hasMoreElements()) {
				final String name = entries.nextElement().getName().toLowerCase(java.util.Locale.ROOT);
				if (name.endsWith(".dylib") || name.endsWith(".jnilib") || name.endsWith(".so") || name.endsWith(".dll")
						|| name.endsWith(".exe") || name.endsWith(".jar"))
					throw new IOException("Native code and nested JARs are not supported in playable model plugins: " + required.symbolicName());
			}
		}
	}
}
