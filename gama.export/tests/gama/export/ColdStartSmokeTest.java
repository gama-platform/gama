package gama.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.tools.ToolProvider;

/** Native pre-core router checks. Test entry points and error-dialog dismissal live only in scratch bundles. */
public final class ColdStartSmokeTest {

	public static void main(final String[] args) throws Exception {
		if (args.length != 4) throw new IllegalArgumentException("Expected installation, native launcher, scratch parent, source repository");
		final Path root = Files.createTempDirectory(Path.of(args[2]), "cold-router-smoke-");
		final Path install = SimulationPackageSmokeTest.overlay(Path.of(args[0]), Path.of(args[3]), root);
		final Path classes = Files.createDirectory(root.resolve("classes"));
		final Path source = Files.createDirectories(root.resolve("src/gama/export")).resolve("ColdStartProbe.java");
		Files.writeString(source, """
				package gama.export;
				public final class ColdStartProbe implements org.eclipse.equinox.app.IApplication {
					public Object start(org.eclipse.equinox.app.IApplicationContext context) throws Exception {
						var display = new org.eclipse.swt.widgets.Display();
						var root = java.nio.file.Path.of(System.getProperty("gama.router.fixture"));
						boolean normal = Boolean.getBoolean("gama.router.normal");
						var core = org.eclipse.core.runtime.Platform.getBundle("gama.core");
						if (core.getState() == org.osgi.framework.Bundle.ACTIVE) throw new AssertionError("Core started before router");
						Runnable answer = new Runnable() {
							boolean lateSent;
							public void run() {
								if (display.isDisposed()) return;
								for (var shell : display.getShells()) {
									if (shell.getText().equals("Run simulation package?")) {
										throw new AssertionError("Package opening must not ask for confirmation");
									}
									if (!lateSent && Boolean.getBoolean("gama.router.late") && shell.isVisible()) {
										lateSent = true;
											var event = new org.eclipse.swt.widgets.Event();
											event.text = System.getProperty("gama.router.ordinary");
											try {
												var send = org.eclipse.swt.widgets.Display.class.getDeclaredMethod("sendEvent", int.class, org.eclipse.swt.widgets.Event.class);
												send.setAccessible(true);
												send.invoke(display, org.eclipse.swt.SWT.OpenDocument, event);
											} catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
									}
									if (shell.getText().equals("Unable to open exported model")) {
										try { java.nio.file.Files.writeString(root.resolve("error.seen"), "yes"); }
										catch (java.io.IOException e) { throw new IllegalStateException(e); }
										click(shell, "OK");
									}
								}
								display.timerExec(30, this);
							}
							void click(org.eclipse.swt.widgets.Composite owner, String text) {
								for (var child : owner.getChildren()) {
									if (child instanceof org.eclipse.swt.widgets.Button button
											&& button.getText().replace("&", "").equals(text))
										button.notifyListeners(org.eclipse.swt.SWT.Selection, null);
									else if (child instanceof org.eclipse.swt.widgets.Composite nested) click(nested, text);
								}
							}
						};
						display.timerExec(30, answer);
						if (Boolean.getBoolean("gama.router.event")) display.timerExec(30, () -> {
							var event = new org.eclipse.swt.widgets.Event();
							event.text = System.getProperty("gama.router.archive");
							try {
								var send = org.eclipse.swt.widgets.Display.class.getDeclaredMethod("sendEvent", int.class, org.eclipse.swt.widgets.Event.class);
								send.setAccessible(true);
								send.invoke(display, org.eclipse.swt.SWT.OpenDocument, event);
							} catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
						});
						Object result = new DocumentApplication().start(context);
						if (!normal && core.getState() == org.osgi.framework.Bundle.ACTIVE)
							throw new AssertionError("Package routing initialized GAMA core");
						java.nio.file.Files.writeString(root.resolve("router.ready"), "No modeler for package launch");
						return result;
					}
					public void stop() {}
				}
				""");
		final Path normalSource = Files.createDirectories(root.resolve("src/gama/ui/application")).resolve("ColdModelerProbe.java");
		Files.writeString(normalSource, """
				package gama.ui.application;
				public final class ColdModelerProbe implements org.eclipse.equinox.app.IApplication {
					public Object start(org.eclipse.equinox.app.IApplicationContext context) throws Exception {
						var root = java.nio.file.Path.of(System.getProperty("gama.router.fixture"));
						var pending = org.eclipse.swt.widgets.Display.getCurrent().getData("gama.pendingStartupDocuments");
						if (Boolean.getBoolean("gama.router.late") &&
							(!java.util.List.of(System.getProperty("gama.router.ordinary")).equals(pending)
								|| !Boolean.TRUE.equals(org.eclipse.swt.widgets.Display.getCurrent().getData("gama.startupArgumentsConsumed"))))
							throw new AssertionError("Late fallback must forward only unhandled documents and consume package arguments");
						if (Boolean.getBoolean("gama.router.event") && !java.util.List.of(System.getProperty("gama.router.archive")).equals(pending))
							throw new AssertionError("Lost native documents when delegating to the modeler: " + pending);
						java.nio.file.Files.writeString(root.resolve("modeler.ready"), "Normal startup delegated");
						return EXIT_OK;
					}
					public void stop() {}
				}
				""");
		if (ToolProvider.getSystemJavaCompiler().run(null, null, null, "-proc:none", "-classpath",
				System.getProperty("java.class.path"), "-d", classes.toString(), source.toString(), normalSource.toString()) != 0)
			throw new AssertionError("Cold router probe compilation failed");
		patch(install.resolve("gama.export.jar"), classes, "gama/export/ColdStartProbe", xml -> xml.replace("</plugin>", """
				<extension point="org.eclipse.core.runtime.applications" id="ColdStartProbe">
				<application><run class="gama.export.ColdStartProbe"/></application></extension></plugin>
				"""));
		patch(install.resolve("gama.ui.application.jar"), classes, "gama/ui/application/ColdModelerProbe",
				xml -> xml.replace("class=\"gama.ui.application.Application\"", "class=\"gama.ui.application.ColdModelerProbe\""));
		final Path staging = Files.createDirectories(root.resolve("staging/workspace/Example"));
		Files.writeString(staging.resolve(".project"), "<projectDescription><name>Example</name><natures>"
				+ "<nature>gaml.compiler.gamlNature</nature></natures></projectDescription>");
		Files.writeString(staging.resolve("model.gaml"), """
				model cold_router
				global { init { save "ready" to: "simulation.ready" format: "text" rewrite: true buffering: "no_buffering"; } }
				experiment Run type: gui {}
				""");
		Files.createDirectories(root.resolve("staging/configuration"));
		Files.writeString(root.resolve("staging").resolve(LauncherConfiguration.FILE),
				new LauncherConfiguration("Cold route", "", false, "Run@Example/model.gaml", "").serialize());
		final Path archive = root.resolve("model with spaces.gamp");
		SimulationPackage.write(root.resolve("staging"), archive, new SimulationPackageManifest("example.cold", "1",
				"0.0.0-SNAPSHOT", List.of(new SimulationPackageManifest.Experiment("Example/model.gaml", "Run")), List.of()));
		for (final String mode : List.of("argument", "event", "invalid", "normal", "normal-event", "late-document")) {
			final Path fixture = Files.createDirectory(root.resolve(mode));
			final var prepared = SimulationPackageLauncher.prepare(archive, fixture.resolve("configurations"), install,
					Path.of(args[1]), "0.0.0-SNAPSHOT");
			final var command = new ArrayList<>(prepared.command());
			command.set(command.indexOf("-application") + 1, "gama.export.ColdStartProbe");
			command.removeIf(argument -> argument.startsWith("-Dgama.package.") || argument.startsWith("-Dexported_runner="));
			final boolean normal = mode.startsWith("normal");
			final boolean fallback = normal || mode.equals("late-document");
			final Path document = mode.equals("invalid") ? root.resolve("missing.gamp")
					: normal ? root.resolve("ordinary.gaml") : archive;
			if (mode.equals("argument") || mode.equals("invalid") || mode.equals("late-document"))
				command.addAll(command.indexOf("-vmargs"), List.of("--", document.toString()));
			command.addAll(List.of("-Duser.home=" + fixture, "-Dgama.router.fixture=" + fixture,
					"-Dgama.router.archive=" + document, "-Dgama.router.normal=" + fallback,
					"-Dgama.router.event=" + mode.endsWith("event")));
			command.addAll(List.of("-Dgama.router.late=" + mode.equals("late-document"),
					"-Dgama.router.ordinary=" + root.resolve("late ordinary.gaml")));
			final Process process = new ProcessBuilder(command).redirectErrorStream(true)
					.redirectOutput(fixture.resolve("router.log").toFile()).start();
			try {
				for (int i = 0; i < 120 && process.isAlive() && !Files.exists(fixture.resolve("router.ready")); i++)
					Thread.sleep(1000);
				if (fallback && Files.exists(fixture.resolve("router.ready")) && process.isAlive()) {
					// The substituted modeler has no workbench shutdown lifecycle; core background jobs may remain.
					process.destroy();
					if (!process.waitFor(15, TimeUnit.SECONDS)) process.destroyForcibly();
				}
				if (!Files.exists(fixture.resolve("router.ready"))
						|| (!fallback && (!process.waitFor(15, TimeUnit.SECONDS) || process.exitValue() != 0)))
					throw new AssertionError("Router failed in " + mode + ": " + Files.readString(fixture.resolve("router.log")));
				if (Files.exists(fixture.resolve("modeler.ready")) != fallback)
					throw new AssertionError("Wrong modeling fallback in " + mode);
				if (mode.equals("invalid") != Files.exists(fixture.resolve("error.seen")))
					throw new AssertionError("Invalid packages must report failure without starting the modeler");
				final Path sessions = fixture.resolve(".gama/package-sessions");
				if (normal || mode.equals("invalid")) {
					if (Files.exists(sessions)) throw new AssertionError("Ordinary/invalid launches must not create sessions");
				} else {
					try (var children = Files.list(sessions)) {
						final Path session = children.findFirst().orElseThrow();
						for (int i = 0; i < 60 && !Files.exists(session.resolve("workspace/Example/simulation.ready")); i++) Thread.sleep(1000);
						if (!Files.exists(session.resolve("workspace/Example/simulation.ready")))
							throw new AssertionError("Cold router did not launch the experiment: " + Files.readString(session.resolve("player.log")));
						stopPlayer(session);
					}
				}
				System.out.println("Cold router passed: " + mode);
			} finally {
				if (process.isAlive()) { process.destroyForcibly(); process.waitFor(15, TimeUnit.SECONDS); }
			}
		}
		System.out.println("Cold routing passed without opening the modeler for packages; scratch: " + root);
	}

	private static void stopPlayer(final Path content) {
		final String marker = "-Dgama.package.content=" + content;
		for (final var process : ProcessHandle.allProcesses().toList()) {
			if (java.util.Arrays.asList(process.info().arguments().orElse(new String[0])).contains(marker)) {
				process.destroy();
				try { process.onExit().get(15, TimeUnit.SECONDS); }
				catch (final java.util.concurrent.TimeoutException e) { process.destroyForcibly(); }
				catch (final InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
				catch (final java.util.concurrent.ExecutionException e) { throw new IllegalStateException(e); }
			}
		}
	}

	private static void patch(final Path jarPath, final Path classes, final String prefix,
			final java.util.function.UnaryOperator<String> xml) throws Exception {
		final Path temporary = jarPath.resolveSibling(jarPath.getFileName() + ".tmp");
		try (var jar = new JarFile(jarPath.toFile()); var output = new JarOutputStream(Files.newOutputStream(temporary), jar.getManifest())) {
			final var entries = jar.entries();
			while (entries.hasMoreElements()) {
				final var entry = entries.nextElement();
				if (entry.getName().equals("META-INF/MANIFEST.MF")) continue;
				output.putNextEntry(new JarEntry(entry.getName()));
				try (var input = jar.getInputStream(entry)) {
					if (entry.getName().equals("plugin.xml"))
						output.write(xml.apply(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8))
								.getBytes(java.nio.charset.StandardCharsets.UTF_8));
					else input.transferTo(output);
				}
				output.closeEntry();
			}
			try (var paths = Files.walk(classes)) {
				for (final Path path : paths.filter(Files::isRegularFile).sorted(Comparator.naturalOrder()).toList()) {
					final String name = classes.relativize(path).toString();
					if (!name.startsWith(prefix)) continue;
					output.putNextEntry(new JarEntry(name));
					Files.copy(path, output);
					output.closeEntry();
				}
			}
		}
		Files.move(temporary, jarPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}
}
