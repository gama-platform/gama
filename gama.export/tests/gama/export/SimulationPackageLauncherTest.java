package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/** JDK-only provisioning checks; never starts the fixture's launcher. */
public final class SimulationPackageLauncherTest {

	@FunctionalInterface
	private interface CheckedRunnable { void run() throws Exception; }

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static void rejects(final CheckedRunnable runnable) throws Exception {
		try {
			runnable.run();
			throw new AssertionError("Invalid provisioning must fail");
		} catch (final IOException expected) {
			check(expected.getMessage() != null, "Explain provisioning failures");
		}
	}

	private static void plugin(final Path jar, final String id, final String version, final String header,
			final String entry) throws IOException {
		final var manifest = new Manifest();
		final var attributes = manifest.getMainAttributes();
		attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		attributes.putValue("Bundle-ManifestVersion", "2");
		attributes.putValue("Bundle-SymbolicName", id);
		attributes.putValue("Bundle-Version", version);
		if (header != null) attributes.putValue(header, "unsupported");
		try (var output = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
			if (entry != null) {
				output.putNextEntry(new JarEntry(entry));
				output.write(new byte[] { 1, 2, 3 });
				output.closeEntry();
			}
		}
	}

	private static SimulationPackageManifest manifest(final String id, final String version, final String artifact) {
		return new SimulationPackageManifest("example.package", "1", "test",
				List.of(new SimulationPackageManifest.Experiment("Example/model.gaml", "Run")),
				List.of(new SimulationPackageManifest.BundleRequirement(id, version, artifact)));
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Files.createTempDirectory("gama-package-launch-test-");
		try {
			final Path install = Files.createDirectories(root.resolve("installation with spaces,comma")).toRealPath();
			final Path inventory = install.resolve("configuration/org.eclipse.equinox.simpleconfigurator/bundles.info");
			Files.createDirectories(inventory.getParent());
			final var lines = new StringBuilder("#version=1\n");
			for (final String id : List.of("gama.export", "gama.ui.export", "gama.core", "org.eclipse.osgi",
					"org.eclipse.equinox.common", "org.eclipse.equinox.simpleconfigurator",
					"org.eclipse.osgi.compatibility.state", "org.eclipse.equinox.p2.reconciler.dropins")) {
				final Path file = Files.createDirectories(install.resolve("plugins")).resolve(id + ".jar");
				Files.write(file, new byte[0]);
				lines.append(id).append(",1.0.0,plugins/").append(file.getFileName()).append(",4,true\n");
			}
			Files.writeString(inventory, lines);
			Files.write(install.resolve("plugins/second-version.jar"), new byte[0]);
			Files.writeString(inventory, "gama.core,2.0.0,plugins/second-version.jar,4,false\n",
					java.nio.file.StandardOpenOption.APPEND);
			Files.writeString(install.resolve("configuration/config.ini"),
					"eclipse.product=gama.ui.application.product\nosgi.sharedConfiguration.area=private-author-path\n"
							+ "eclipse.p2.data.area=private-author-path\neclipse.p2.profile=DefaultProfile\n"
							+ "osgi.framework.extensions=untrusted-relative-path\n");
			final byte[] originalInventory = Files.readAllBytes(inventory);
			final byte[] originalConfig = Files.readAllBytes(install.resolve("configuration/config.ini"));
			final Path source = Files.createDirectories(root.resolve("source"));
			Files.createDirectories(source.resolve("workspace/Example"));
			Files.writeString(source.resolve("workspace/Example/.project"), "<projectDescription><name>Example</name></projectDescription>");
			Files.writeString(source.resolve("workspace/Example/model.gaml"), "model example\nexperiment Run type: gui {}");
			Files.createDirectories(source.resolve("configuration"));
			Files.writeString(source.resolve(LauncherConfiguration.FILE), LauncherConfiguration.defaults().serialize());
			Files.createDirectories(source.resolve("plugins"));
			final Path plugin = source.resolve("plugins/example.jar");
			plugin(plugin, "example.extension", "1.2.3", null, null);
			final Path archive = root.resolve("example.gamx");
			SimulationPackage.write(source, archive, manifest("example.extension", "1.2.3", "plugins/example.jar"));
			final Path sessions = Files.createDirectories(root.resolve("sessions"));
			final Path launcher = Path.of(System.getProperty("java.home"), "bin", "java");
			final var prepared = SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test");
			check(SimulationPackage.Extracted.open(prepared.content()).manifest()
					.equals(manifest("example.extension", "1.2.3", "plugins/example.jar")), "Persist package metadata across processes");
			check(prepared.workspace().startsWith(prepared.content()), "Workspace is private");
			check(prepared.configuration().startsWith(prepared.content()), "Configuration is private");
			final String info = Files.readString(prepared.configuration().resolve("org.eclipse.equinox.simpleconfigurator/bundles.info"));
			check(info.contains("example.extension,1.2.3," + prepared.content().resolve("plugins/example.jar").toUri()),
					"Provision the package bundle from its private location");
			check(info.contains("gama.core,1.0.0," + install.resolve("plugins/gama.core.jar").toUri().toASCIIString().replace(",", "%2C")),
					"Installed bundle URLs must be absolute and escaped");
			check(info.contains("gama.core,2.0.0," + install.resolve("plugins/second-version.jar").toUri().toASCIIString().replace(",", "%2C")),
					"Preserve multiple bundle versions from the host target platform");
			check(info.contains("org.eclipse.equinox.p2.reconciler.dropins,1.0.0,"
					+ install.resolve("plugins/org.eclipse.equinox.p2.reconciler.dropins.jar").toUri().toASCIIString().replace(",", "%2C") + ",4,false"),
					"Do not start p2 dropins reconciliation");
			final var config = new Properties();
			try (var reader = Files.newBufferedReader(prepared.configuration().resolve("config.ini"))) { config.load(reader); }
			check(!config.containsKey("osgi.sharedConfiguration.area") && !config.containsKey("eclipse.p2.profile"),
					"Do not inherit shared runtime state");
			check(config.getProperty("eclipse.p2.data.area").startsWith(prepared.configuration().toUri().toString()),
					"p2 data is isolated");
			check(config.getProperty("eclipse.application").equals(SimulationPackageLauncher.APPLICATION), "Use pre-core bootstrap");
			check(config.getProperty("osgi.bundles").contains("reference:file:"), "Explicit initial framework bundles");
			check(prepared.command().getFirst().equals(launcher.toRealPath().toString())
					&& prepared.command().contains("--launcher.appendVmargs")
					&& !prepared.command().contains("--launcher.openFile"),
					"Invoke the executable directly with installed JVM flags, not open-document forwarding");
			check(prepared.command().contains("-Duse_global_preference_store=false")
					&& prepared.command().contains("-Dexported_runner=true"), "Player flags are process-local");
			check(prepared.command().contains("-Dpref_enable_server=false"),
					"Package players must not compete with the modeler's default WebSocket port");
			check(java.util.Arrays.equals(originalInventory, Files.readAllBytes(inventory))
					&& java.util.Arrays.equals(originalConfig, Files.readAllBytes(install.resolve("configuration/config.ini"))),
					"Host configuration must not change");
			final long before;
			try (var paths = Files.list(sessions)) { before = paths.count(); }
			for (final String header : List.of("Fragment-Host", "Bundle-NativeCode")) {
				plugin(plugin, "example.extension", "1.2.3", header, null);
				SimulationPackage.write(source, archive, manifest("example.extension", "1.2.3", "plugins/example.jar"));
				rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			}
			for (final String entry : List.of("lib/example.dylib", "lib/Example.DLL", "nested.jar", "lib/example.so")) {
				plugin(plugin, "example.extension", "1.2.3", null, entry);
				SimulationPackage.write(source, archive, manifest("example.extension", "1.2.3", "plugins/example.jar"));
				rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			}
			plugin(plugin, "example.extension", "1.2.3", null, "plugin.xml");
			SimulationPackage.write(source, archive, manifest("example.extension", "1.2.3", "plugins/example.jar"));
			rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			plugin(plugin, "wrong.identity", "1.2.3", null, null);
			SimulationPackage.write(source, archive, manifest("example.extension", "1.2.3", "plugins/example.jar"));
			rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			plugin(plugin, "gama.core", "1.0.0", null, null);
			SimulationPackage.write(source, archive, manifest("gama.core", "1.0.0", "plugins/example.jar"));
			rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			plugin(plugin, "org.eclipse.unknown", "1.0.0", null, null);
			SimulationPackage.write(source, archive, manifest("org.eclipse.unknown", "1.0.0", "plugins/example.jar"));
			rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			Files.delete(plugin);
			SimulationPackage.write(source, archive, manifest("gama.core", "1.0.0", ""));
			final var hostOnly = SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test");
			check(Files.isRegularFile(hostOnly.configuration().resolve("config.ini")), "Accept the exact host version even when another version exists");
			SimulationPackage.write(source, archive, manifest("gama.core", "1.5.0", ""));
			final var newerHost = SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test");
			check(Files.isRegularFile(newerHost.configuration().resolve("config.ini")), "Accept a newer host bundle without requiring an exact match");
			SimulationPackage.write(source, archive, manifest("gama.core", "9.0.0", ""));
			rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			SimulationPackage.write(source, archive, manifest("missing.extension", "1.0.0", ""));
			rejects(() -> SimulationPackageLauncher.prepare(archive, sessions, install, launcher, "test"));
			try (var paths = Files.list(sessions)) { check(paths.count() == before + 2, "Failed preparation cleans up only its own session"); }
		} finally {
			try (var paths = Files.walk(root)) {
				for (final var path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
			}
		}
		System.out.println("Package player provisioning checks passed");
	}
}
