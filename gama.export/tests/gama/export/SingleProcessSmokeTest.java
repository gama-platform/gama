package gama.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Exercises the actual native executable, not an already prepared player subprocess. */
public final class SingleProcessSmokeTest {

	public static void main(final String[] args) throws Exception {
		final Path scratch = Files.createTempDirectory(Path.of(args[2]), "single-process-smoke-");
		System.setProperty("gama.smoke.prepareOnly", "true");
		if (Boolean.getBoolean("gama.smoke.home")) System.setProperty("gama.smoke.export", "true");
		SimulationPackageSmokeTest.main(new String[] { args[0], args[1], scratch.toString(), args[3] });
		final Path fixture;
		try (var children = Files.list(scratch)) { fixture = children.findFirst().orElseThrow(); }
		final Path overlay = fixture.resolve("installation");
		final Path archive = fixture.resolve("A playable model \u00e9.gamx");
		Files.move(fixture.resolve("smoke.gamx"), archive);
		final boolean invalid = Boolean.getBoolean("gama.smoke.invalid");
		if (invalid) Files.writeString(archive, "Not a ZIP archive");
		final boolean normal = Boolean.getBoolean("gama.smoke.normal");
		if (normal) {
			installProbe(overlay.resolve("gama.ui.export.jar"), "gama/ui/export/launcher/NormalStartupProbe.class",
					xml -> xml.replace("</plugin>", """
							<extension point="org.eclipse.ui.startup">
							<startup class="gama.ui.export.launcher.NormalStartupProbe"/>
							</extension></plugin>
							"""));
			installProbe(overlay.resolve("gama.ui.application.jar"), "gama/ui/application/NativeModelerProbe.class",
					xml -> xml.replace("class=\"gama.ui.application.Application\"",
							"class=\"gama.ui.application.NativeModelerProbe\""));
		}
		final Path app = scratch.resolve("Gama.app");
		final Path eclipse = Files.createDirectories(app.resolve("Contents/Eclipse"));
		final Path plugins = Files.createDirectories(eclipse.resolve("plugins"));
		final Path executable = Files.createDirectories(app.resolve("Contents/MacOS")).resolve("Gama");
		Files.copy(Path.of(args[1]), executable);
		executable.toFile().setExecutable(true);
		final Path source = Path.of(args[0]);
		Files.copy(source.resolve("Gama.ini"), eclipse.resolve("Gama.ini"));
		final Path plist = Path.of(args[3]).resolve("gama.product/extraresources/Info.plist");
		if (Files.exists(plist)) Files.copy(plist, app.resolve("Contents/Info.plist"));
		try (var entries = Files.list(source.resolve("plugins"))) {
			for (final Path entry : entries.toList()) Files.createSymbolicLink(plugins.resolve(entry.getFileName()), entry);
		}
		final Path configuration = Files.createDirectories(eclipse.resolve("configuration"));
		Files.copy(overlay.resolve("configuration/config.ini"), configuration.resolve("config.ini"));
		final String inventory = "org.eclipse.equinox.simpleconfigurator/bundles.info";
		Files.createDirectories(configuration.resolve(inventory).getParent());
		Files.copy(overlay.resolve("configuration/" + inventory), configuration.resolve(inventory));
		for (final String line : Files.readAllLines(configuration.resolve(inventory))) {
			if (line.startsWith("#") || line.isBlank()) continue;
			final String[] parts = line.split(",");
			if (!List.of("gama.api", "gama.export", "gama.ui.export", "gama.ui.application", "gama.ui.shared",
					"gama.ui.navigator").contains(parts[0])) continue;
			final Path existing = plugins.resolve(parts[0] + "_" + parts[1] + ".jar");
			Files.deleteIfExists(existing);
			Files.createSymbolicLink(existing, Path.of(java.net.URI.create(parts[2])));
		}
		final var installer = Class.forName("LauncherInstaller").getDeclaredMethod("main", String[].class);
		installer.invoke(null, (Object) new String[] { Path.of(args[3]).resolve("gama.product").toString(), scratch.toString() });
		final Path home = Files.createDirectory(scratch.resolve("home"));
		Files.writeString(eclipse.resolve("Gama.ini"), Files.readString(eclipse.resolve("Gama.ini"))
				.replace("@noDefault", scratch.resolve("modeler-workspace").toString())
				.replace("-vmargs", "-vm\n" + Path.of(System.getProperty("java.home")).resolve("lib/server/libjvm.dylib") + "\n-vmargs")
				+ "\n-Dgama.launcher.debug=true\n-Dgama.launcher.suppressErrors=true\n-Dgama.smoke.cold=true\n-Dgama.smoke.home=" + Boolean.getBoolean("gama.smoke.home")
				+ "\n-Dgama.smoke.normal.marker=" + scratch.resolve("modeler.ready")
				+ "\n-Duser.home=" + home + "\n");
		final var command = new ArrayList<>(List.of(executable.toString(), "--launcher.suppressErrors", "--launcher.appendVmargs",
				"-consoleLog", "-vm", Path.of(System.getProperty("java.home")).resolve("lib/server/libjvm.dylib").toString(),
				archive.toString(), "-vmargs", "-Duser.home=" + home));
		final Path log = scratch.resolve("launch.log");
		final boolean prepared = Boolean.getBoolean("gama.smoke.prepared");
		if (prepared) {
			command.clear();
			command.addAll(SimulationPackageLauncher.prepare(archive,
					home.resolve(".gama/package-sessions"), eclipse, executable, "0.0.0-SNAPSHOT").command());
		}
		if (normal) command.remove(archive.toString());
		final ProcessHandle process;
		if (Boolean.getBoolean("gama.smoke.finder")) {
			// A unique test bundle identity avoids forwarding to a user's running GAMA.
			final Path info = app.resolve("Contents/Info.plist");
			Files.writeString(info, Files.readString(info).replaceFirst(
					"(?s)(<key>CFBundleIdentifier</key>\\s*<string>)[^<]+", "$1org.gama.smoke." + java.util.UUID.randomUUID()));
			final var openCommand = new ArrayList<>(List.of("open", "-n", "-a", app.toString(),
					"--stdout", log.toString(), "--stderr", log.toString()));
			if (!normal) openCommand.add(archive.toString());
			final Process open = new ProcessBuilder(openCommand).start();
			if (!open.waitFor(30, TimeUnit.SECONDS) || open.exitValue() != 0)
				throw new AssertionError("Launch Services could not open the test application");
			ProcessHandle launched = null;
			for (int i = 0; i < 30 && launched == null; i++) {
				launched = ProcessHandle.allProcesses().filter(handle ->
						handle.info().command().map(value -> value.equals(executable.toString())).orElse(false))
						.findFirst().orElse(null);
				if (launched == null) Thread.sleep(1000);
			}
			if (launched == null) throw new AssertionError("Native application did not start");
			process = launched;
		} else {
			process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start().toHandle();
		}
		System.out.println("Single-process native smoke: " + scratch + " PID " + process.pid());
		try {
			if (invalid) {
				process.onExit().get(30, TimeUnit.SECONDS);
				final String output = Files.readString(log);
				if (!output.contains("Unable to start GAMA") || output.contains("Building metamodel"))
					throw new AssertionError("Invalid archive did not fail before GAMA initialization; see " + log);
				System.out.println("Invalid playable model rejected before framework/GAMA startup without a second process");
				return;
			}
			if (normal) {
				for (int i = 0; i < 120 && !Files.exists(scratch.resolve("modeler.ready")); i++) {
					if (!process.isAlive()) throw new AssertionError("Modeler exited; see " + log);
					Thread.sleep(1000);
				}
				if (!Files.exists(scratch.resolve("modeler.ready")))
					throw new AssertionError("Modeler did not start; see " + log);
				if (Files.exists(home.resolve(".gama/package-sessions")))
					throw new AssertionError("Ordinary startup prepared a playable model");
				System.out.println("Normal startup retained the modeler in the original native process");
				if (Boolean.getBoolean("gama.smoke.live")) {
					final Process open = new ProcessBuilder("open", "-a", app.toString(), archive.toString()).start();
					if (!open.waitFor(30, TimeUnit.SECONDS) || open.exitValue() != 0)
						throw new AssertionError("Unable to deliver a playable model to the running modeler");
					Path session = null;
					final Path sessions = home.resolve(".gama/package-sessions");
					for (int i = 0; i < 120; i++) {
						if (Files.exists(sessions)) {
							try (var children = Files.list(sessions)) { session = children.findFirst().orElse(null); }
						}
						if (session != null && Files.exists(session.resolve("player.ready"))) break;
						Thread.sleep(1000);
					}
					if (session == null) throw new AssertionError("Running modeler did not open the playable model; see " + log);
					final var metadata = new java.util.Properties();
					try (var input = Files.newInputStream(session.resolve("player-session.properties"))) { metadata.load(input); }
					final var player = ProcessHandle.of(Long.parseLong(metadata.getProperty("pid"))).orElseThrow();
					if (player.pid() == process.pid() || !player.info().command().orElse("").equals(executable.toString()))
						throw new AssertionError("Expected an isolated player from this test installation");
					try {
						if (!process.isAlive() || !Files.readString(session.resolve("bootstrap.ready")).contains("in-process=false")
								|| !Files.exists(session.resolve("plugin.ready")) || !Files.exists(session.resolve("splash.ready")))
							throw new AssertionError("Live modeler/player isolation or playable model presentation failed");
						System.out.println("Running modeler remained active; one isolated player opened with its playable model splash");
					} finally {
						player.destroy();
						try { player.onExit().get(15, TimeUnit.SECONDS); }
						catch (java.util.concurrent.TimeoutException timeout) {
							player.destroyForcibly();
							player.onExit().get(15, TimeUnit.SECONDS);
						}
					}
				}
				return;
			}
			Path session = null;
			final Path sessions = home.resolve(".gama/package-sessions");
			for (int i = 0; i < 120; i++) {
				if (Files.isDirectory(sessions)) {
					try (var children = Files.list(sessions)) { session = children.findFirst().orElse(null); }
				}
				if (session != null && Files.exists(session.resolve("player.ready"))
						&& (Boolean.getBoolean("gama.smoke.home") || Files.exists(session.resolve("workspace/Example/simulation.ready"))))
					break;
				if (!process.isAlive()) throw new AssertionError(Files.readString(log));
				Thread.sleep(1000);
			}
			if (session == null || !Files.exists(session.resolve("player.ready")))
				throw new AssertionError("Player did not start: " + Files.readString(log));
			final String bootstrap = Files.readString(session.resolve("bootstrap.ready"));
			if (!bootstrap.contains("pid=" + process.pid() + "\n") || !bootstrap.contains("in-process=" + !prepared))
				throw new AssertionError("Cold open started another process: " + bootstrap);
			if (process.descendants().anyMatch(ProcessHandle::isAlive))
				throw new AssertionError("The player launched a child process");
			if (!Files.exists(session.resolve("plugin.ready")) || !Files.exists(session.resolve("splash.ready")))
				throw new AssertionError("Missing embedded plugin or personalized splash");
			if (Boolean.getBoolean("gama.smoke.home")) {
				if (Files.exists(session.resolve("workspace/Example/simulation.ready")))
					throw new AssertionError("Home-first playable model auto-ran its experiment");
				for (int i = 0; i < 30 && !Files.exists(session.resolve("home-ui.ready")); i++) Thread.sleep(1000);
				if (!Files.exists(session.resolve("home-ui.ready")))
					throw new AssertionError("Actual Home perspective, Home view and hidden editor were not verified");
			} else if (!Files.exists(session.resolve("workspace/Example/simulation.ready"))) {
				throw new AssertionError("Auto-run experiment did not initialize");
			}
			System.out.println("Original native PID reached the player with embedded plugins, splash and "
					+ (Boolean.getBoolean("gama.smoke.home") ? "Home-first" : "auto-run") + " startup");
		} finally {
			process.destroy();
			try {
				process.onExit().get(15, TimeUnit.SECONDS);
			} catch (java.util.concurrent.TimeoutException timeout) {
				process.destroyForcibly();
				process.onExit().get(15, TimeUnit.SECONDS);
			}

		}
	}

	private static void installProbe(final Path bundle, final String probe,
			final java.util.function.UnaryOperator<String> patch) throws Exception {
		final var entries = new java.util.LinkedHashMap<String, byte[]>();
		final java.util.jar.Manifest manifest;
		try (var jar = new java.util.jar.JarFile(bundle.toFile())) {
			manifest = jar.getManifest();
			final var iterator = jar.entries();
			while (iterator.hasMoreElements()) {
				final var entry = iterator.nextElement();
				if (entry.isDirectory() || entry.getName().equals("META-INF/MANIFEST.MF")) continue;
				try (var input = jar.getInputStream(entry)) { entries.put(entry.getName(), input.readAllBytes()); }
			}
		}
		try (var input = SingleProcessSmokeTest.class.getClassLoader().getResourceAsStream(probe)) {
			if (input == null) throw new IllegalStateException("Compile " + probe + " first");
			entries.put(probe, input.readAllBytes());
		}
		final String xml = new String(entries.get("plugin.xml"), java.nio.charset.StandardCharsets.UTF_8);
		entries.put("plugin.xml", patch.apply(xml).getBytes(java.nio.charset.StandardCharsets.UTF_8));
		try (var jar = new java.util.jar.JarOutputStream(Files.newOutputStream(bundle), manifest)) {
			for (final var entry : entries.entrySet()) {
				jar.putNextEntry(new java.util.jar.JarEntry(entry.getKey()));
				jar.write(entry.getValue());
				jar.closeEntry();
			}
		}
	}
}
