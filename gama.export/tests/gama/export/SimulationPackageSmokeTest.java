package gama.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.ToolProvider;

/** Opt-in GUI smoke check against a packaged product containing the current application/export bundles. */
public final class SimulationPackageSmokeTest {

	public static void main(final String[] args) throws Exception {
		if (args.length < 3 || args.length > 4)
			throw new IllegalArgumentException("Expected installation, native launcher, scratch parent, and optional source repository");
		final Path root = Files.createTempDirectory(Path.of(args[2]), "package-smoke-");
		final boolean unresolved = Boolean.getBoolean("gama.smoke.unresolved");
		final boolean home = Boolean.getBoolean("gama.smoke.home");
		final boolean authoring = Boolean.getBoolean("gama.smoke.export");
		final Path install = args.length == 4 ? overlay(Path.of(args[0]), Path.of(args[3]), root) : Path.of(args[0]);
		final Path staging = Files.createDirectory(root.resolve("staging"));
		final Path project = Files.createDirectories(staging.resolve("workspace/Example"));
		Files.writeString(project.resolve(".project"), """
				<?xml version="1.0" encoding="UTF-8"?>
				<projectDescription><name>Example</name><comment></comment><projects></projects>
				<buildSpec></buildSpec><natures><nature>gaml.compiler.gamlNature</nature></natures></projectDescription>
				""");
		Files.writeString(project.resolve("model.gaml"), """
				model package_smoke
				global {
					init { save "ready" to: "simulation.ready" format: "text" rewrite: true buffering: "no_buffering"; }
				}
				experiment Run type: gui {}
				""");
		Files.createDirectories(staging.resolve("configuration"));
		final boolean image = Boolean.getBoolean("gama.smoke.image");
		if (image) Files.copy(Path.of(args[3]).resolve("gama.ui.application/splash.png"),
				staging.resolve(LauncherConfiguration.IMAGE));
		Files.writeString(staging.resolve(LauncherConfiguration.FILE),
				new LauncherConfiguration("Package smoke test", "A portable simulation package", home, "Run@Example/model.gaml",
						image ? LauncherConfiguration.IMAGE : "").serialize());
		final Path javaSource = Files.createDirectories(root.resolve("src/gaml/additions/packageprobe")).resolve("GamlAdditions.java");
		Files.writeString(javaSource, """
				package gaml.additions.packageprobe;
				public final class GamlAdditions extends gama.api.additions.AbstractGamlAdditions {
					public static Integer probe(Integer value) { return example.packagehelper.Helper.increment(value); }
					public void initialize() throws NoSuchMethodException {
						_operator(new String[] { "package_probe" }, GamlAdditions.class.getMethod("probe", Integer.class),
							null, new int[0], Integer.class, false, -13, -13, -13, -13,
							(scope, operands) -> probe((Integer) operands[0]), false);
						try {
							var root = java.nio.file.Path.of(System.getProperty("gama.package.content"));
							if (!java.nio.file.Files.isRegularFile(root.resolve("bootstrap.ready")))
								throw new IllegalStateException("GAML initialized before package bootstrap");
							if (!example.packagehelper.Helper.ready()) throw new IllegalStateException("Missing transitive package helper");
							if (Boolean.getBoolean("gama.smoke.cold")) {
								String splash = System.getProperty("org.eclipse.equinox.launcher.splash.location", "");
								String handle = System.getProperty("org.eclipse.equinox.launcher.splash.handle", "0");
								if (!splash.equals(root.resolve("player-splash.png").toString()) || handle.equals("0") || handle.equals("-1"))
									throw new IllegalStateException("The native launcher did not display the package splash: " + splash);
								java.nio.file.Files.writeString(root.resolve("splash.ready"), splash);
							}
							java.nio.file.Files.writeString(root.resolve("plugin.ready"), "Package language contribution loaded");
						} catch (java.io.IOException e) { throw new IllegalStateException(e); }
					}
				}
				""");
		final Path classes = Files.createDirectory(root.resolve("classes"));
		final Path helperSource = Files.createDirectories(root.resolve("src/example/packagehelper")).resolve("Helper.java");
		Files.writeString(helperSource, """
				package example.packagehelper;
				public final class Helper {
					public static boolean ready() { return true; }
					public static Integer increment(Integer value) { return value + 1; }
				}
				""");
		final var compilerArgs = new java.util.ArrayList<>(List.of("-proc:none", "-classpath",
				System.getProperty("java.class.path"), "-d", classes.toString(), javaSource.toString(), helperSource.toString()));
		if (authoring) {
			final Path exportSource = javaSource.getParent().resolve("PackageExportProbe.java");
			Files.writeString(exportSource, """
					package gaml.additions.packageprobe;
					public final class PackageExportProbe implements org.eclipse.ui.IStartup {
						public void earlyStartup() {
							var root = java.nio.file.Path.of(System.getProperty("gama.package.content"));
							if (!java.nio.file.Files.exists(root.resolve("authoring.request"))
									&& !Boolean.getBoolean("gama.smoke.cold")) return;
							org.eclipse.ui.PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
								try {
									var page = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
									if (Boolean.getBoolean("gama.smoke.home")) {
										var splash = org.eclipse.ui.internal.splash.SplashHandlerFactory.findSplashHandlerFor(
											org.eclipse.core.runtime.Platform.getProduct());
										if (splash == null || !splash.getClass().getName().equals("gama.ui.export.launcher.PlayerSplashHandler"))
											throw new AssertionError("The product must select the player-aware splash handler");
										var splashShell = new org.eclipse.swt.widgets.Shell(org.eclipse.swt.widgets.Display.getCurrent());
										try {
											splash.init(splashShell);
											if (splash.getBundleProgressMonitor() != null || splashShell.getChildren().length != 0)
												throw new AssertionError("Player splash must not overlay startup progress or messages");
										} finally { splash.dispose(); splashShell.dispose(); }
										System.setProperty("exported_runner", "false");
										var standardSplash = org.eclipse.ui.internal.splash.SplashHandlerFactory.findSplashHandlerFor(
											org.eclipse.core.runtime.Platform.getProduct());
										var standardShell = new org.eclipse.swt.widgets.Shell(org.eclipse.swt.widgets.Display.getCurrent());
										try {
											standardSplash.init(standardShell);
											if (standardSplash.getBundleProgressMonitor() == null)
												throw new AssertionError("Normal GAMA must retain startup progress");
										} finally {
											standardSplash.dispose(); standardShell.dispose();
											System.setProperty("exported_runner", "true");
										}
										if (page.getPerspective() == null || !page.getPerspective().getId().equals("gama.ui.export.perspectives.Home")
												|| page.findView("gama.ui.export.views.Home") == null || page.isEditorAreaVisible())
											throw new AssertionError("Home-first startup must show the real Home perspective, not the modeling workbench");
										var window = (org.eclipse.ui.internal.WorkbenchWindow) page.getWorkbenchWindow();
										var expectedTitle = "Package smoke test powered by GAMA";
										if (!window.getShell().getText().equals(expectedTitle))
											throw new AssertionError("Unexpected player window title: " + window.getShell().getText());
										if (window.isToolbarVisible() || window.getTopTrim().getChildren().stream()
												.anyMatch(element -> "SearchField".equals(element.getElementId())
													&& (element.isVisible() || element.getWidget() instanceof org.eclipse.swt.widgets.Control control
														&& !control.isDisposed() && control.isVisible())))
											throw new AssertionError("Home must hide global controls: topTrim="
												+ window.getTopTrim().isVisible() + ", coolBar=" + window.getCoolBarVisible());
										var home = page.getPerspective();
										var other = org.eclipse.ui.PlatformUI.getWorkbench().getPerspectiveRegistry()
											.findPerspectiveWithId("gama.ui.application.perspectives.ModelingPerspective");
										if (other == null) throw new AssertionError("Missing modeling perspective for trim transition check");
										try {
											page.setPerspective(other);
											if (!window.getTopTrim().isVisible() || !window.isToolbarVisible())
												throw new AssertionError("Leaving Home must restore the global controls");
										} finally { page.setPerspective(home); }
										if (window.isToolbarVisible() || window.getTopTrim().getChildren().stream()
												.anyMatch(element -> "SearchField".equals(element.getElementId()) && element.isVisible()))
											throw new AssertionError("Returning Home must hide the global controls again");
										java.nio.file.Files.writeString(root.resolve("home-ui.ready"), "Home perspective and view visible");
										return;
									}
									if (java.nio.file.Files.exists(root.resolve("ui-checks.request"))) {
										for (String[] test : java.util.List.of(
												new String[] { "gama.ui.export", "gama.ui.export.dialogs.LauncherHomeTest" },
												new String[] { "gama.ui.shared", "gama.ui.shared.utils.LaunchPresentationTest" })) {
											org.eclipse.core.runtime.Platform.getBundle(test[0]).loadClass(test[1])
												.getMethod("runInWorkbench").invoke(null);
										}
										java.nio.file.Files.writeString(root.resolve("ui-checks.ready"), "UI checks passed");
									}
									var project = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getProject("Example");
									var external = root.resolve("workspace/outside.csv");
									java.nio.file.Files.writeString(external, "first,second\\n1,2\\n");
									var linked = root.resolve("linked.csv");
									java.nio.file.Files.writeString(linked, "first,second\\n3,4\\n");
									project.getFile("linked.csv").createLink(linked.toUri(), 0, null);
									var model = project.getFile("model.gaml");
									String text = "model authored\\nglobal { int plugin_result <- package_probe(1); file linked_data <- file('linked.csv');"
										+ " file external_data <- file('../outside.csv');"
										+ " init { save 'authored' to: 'exported.ready' format: 'text' rewrite: true buffering: 'no_buffering'; } }"
										+ "\\nexperiment Run type: gui { user_command 'Save' {"
										+ " save simulation to: '../result/file.simulation' format: 'json'; } }";
									model.setContents(new java.io.ByteArrayInputStream(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)), true, false, null);
									String description = java.nio.file.Files.readString(project.getLocation().toFile().toPath().resolve(".project"));
									var discovered = gama.export.ExportModels.discover(java.util.List.of(model));
									if (discovered.dataFiles().values().stream().flatMap(java.util.Collection::stream)
											.anyMatch(path -> path.endsWith("file.simulation")))
										throw new AssertionError("A future save destination is not required input data");
									var monitor = new org.eclipse.core.runtime.NullProgressMonitor();
									if (!discovered.plugins().contains("example.packageprobe"))
										throw new AssertionError("Discover extra plugin from a model's actual GAML operator usage");
									var choices = gama.export.SimulationPackageExporter.dependencies(discovered, monitor);
									if (choices.stream().noneMatch(choice -> choice.id().equals("example.packageprobe") && choice.automatic()))
										throw new AssertionError("Discover installed Java plugin for embedding");
									if (choices.stream().noneMatch(choice -> choice.id().equals("example.packagehelper") && choice.automatic()))
										throw new AssertionError("Include Import-Package provider automatically");
									if (choices.stream().noneMatch(choice -> choice.id().equals("gama.api") && choice.standard() && !choice.embeddable() && !choice.reason().isBlank()))
										throw new AssertionError("Discover transitive host-only platform dependencies");
									var output = root.resolve("authored.gamp");
									var config = new gama.export.LauncherConfiguration("Authored package", "Round trip", false, "Run@Example/model.gaml", "");
									var forbidden = project.getLocation().toFile().toPath().resolve("new-folder/forbidden.gamp");
									try {
										gama.export.SimulationPackageExporter.export(discovered, discovered.experiments(), config,
											"example.authored", "1.0", java.util.List.of(), forbidden, new org.eclipse.core.runtime.NullProgressMonitor());
										throw new AssertionError("Export into author resources must fail");
									} catch (java.io.IOException expected) {}
									if (java.nio.file.Files.exists(forbidden.getParent())) throw new AssertionError("Rejected export modified author resources");
									gama.export.SimulationPackageExporter.export(discovered, discovered.experiments(), config,
										"example.authored", "1.0", java.util.List.of(), output, new org.eclipse.core.runtime.NullProgressMonitor());
									var packaged = gama.export.SimulationPackage.inspect(output);
									var standalone = project.getFile("exported.gamp");
									standalone.createLink(output.toUri(), 0, null);
									var metadata = gama.api.GAMA.getMetadataProvider().getMetaData(standalone, false, true);
									if (!(metadata instanceof gama.export.ExportedModelInfo)
											|| !metadata.getSuffix().contains("Example/model.gaml")
											|| !metadata.getDocumentation().getContents().contains("example.packageprobe"))
										throw new AssertionError("Navigator metadata contribution was not loaded: " + metadata);
									var editor = org.eclipse.ui.PlatformUI.getWorkbench().getEditorRegistry()
										.getDefaultEditor("exported.gamp");
									if (editor == null || !editor.getId().equals("gama.ui.export.standaloneModel")
											|| editor.getImageDescriptor() == null)
										throw new AssertionError("Missing standalone player/icon association");
									var documentIcon = editor.getImageDescriptor().createImage(false);
									if (documentIcon == null) throw new AssertionError("Standalone document icon cannot render");
									documentIcon.dispose();
									var propertyPage = (org.eclipse.ui.dialogs.PropertyPage) org.eclipse.core.runtime.Platform
										.getBundle("gama.ui.export").loadClass("gama.ui.export.dialogs.ExportedModelPropertyPage")
										.getConstructor().newInstance();
									var propertyShell = new org.eclipse.swt.widgets.Shell(page.getWorkbenchWindow().getShell());
									try {
										propertyPage.setElement(standalone);
										propertyPage.createControl(propertyShell);
										var controls = new java.util.ArrayDeque<org.eclipse.swt.widgets.Control>();
										controls.add(propertyPage.getControl());
										var propertyText = new StringBuilder();
										while (!controls.isEmpty()) {
											var control = controls.remove();
											if (control instanceof org.eclipse.swt.widgets.Text textControl)
												propertyText.append(textControl.getText());
											if (control instanceof org.eclipse.swt.widgets.Composite composite)
												controls.addAll(java.util.List.of(composite.getChildren()));
										}
										if (!propertyText.toString().contains("Example/model.gaml")
												|| !propertyText.toString().contains("Included plugins:"))
											throw new AssertionError("Standalone model properties are not visible");
									} finally { propertyPage.dispose(); propertyShell.dispose(); }
									standalone.delete(true, null);
									var embedded = packaged.bundles().stream().filter(required -> !required.artifact().isEmpty())
										.map(gama.export.SimulationPackageManifest.BundleRequirement::symbolicName).collect(java.util.stream.Collectors.toSet());
									if (!embedded.equals(java.util.Set.of("example.packageprobe", "example.packagehelper")))
										throw new AssertionError("Embed the complete extra plugin closure, and no standard plugins: " + embedded);
									String inputText = text.replace("int plugin_result", "string required_input <- '../result/file.simulation'; int plugin_result");
									model.setContents(new java.io.ByteArrayInputStream(inputText.getBytes(java.nio.charset.StandardCharsets.UTF_8)), true, false, null);
									try {
										var sharedPath = gama.export.ExportModels.discover(java.util.List.of(model));
										if (sharedPath.dataFiles().values().stream().flatMap(java.util.Collection::stream)
												.noneMatch(path -> path.endsWith("file.simulation")))
											throw new AssertionError("Keep a save destination that is also referenced as input");
										var missingInputArchive = root.resolve("missing-input.gamp");
										try {
											gama.export.SimulationPackageExporter.export(sharedPath, sharedPath.experiments(), config,
												"example.missing", "1.0", java.util.List.of(), missingInputArchive, new org.eclipse.core.runtime.NullProgressMonitor());
											throw new AssertionError("Missing input data must still fail export");
										} catch (java.io.IOException expected) {
											if (!expected.getMessage().contains("file.simulation")) throw expected;
										}
										if (java.nio.file.Files.exists(missingInputArchive)) throw new AssertionError("Do not write an incomplete package");
									} finally {
										model.setContents(new java.io.ByteArrayInputStream(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)), true, false, null);
									}
									var unsupportedJar = root.resolve("unsupported.jar");
									var unsupportedMetadata = new java.util.jar.Manifest();
									unsupportedMetadata.getMainAttributes().putValue("Manifest-Version", "1.0");
									unsupportedMetadata.getMainAttributes().putValue("Bundle-ManifestVersion", "2");
									unsupportedMetadata.getMainAttributes().putValue("Bundle-SymbolicName", "example.unsupported");
									unsupportedMetadata.getMainAttributes().putValue("Bundle-Version", "1.0.0");
									try (var jar = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(unsupportedJar), unsupportedMetadata)) {
										jar.putNextEntry(new java.util.jar.JarEntry("lib/native.so"));
										jar.write(new byte[] { 1, 2, 3 });
										jar.closeEntry();
									}
									var context = org.osgi.framework.FrameworkUtil.getBundle(PackageExportProbe.class).getBundleContext();
									var unsupported = context.installBundle(unsupportedJar.toUri().toString());
									try {
										var framework = context.getBundle(0).adapt(org.osgi.framework.wiring.FrameworkWiring.class);
										if (!framework.resolveBundles(java.util.List.of(unsupported))) throw new AssertionError("Resolve unsupported fixture");
										var unsupportedModels = new gama.export.ExportModels(discovered.roots(), discovered.experiments(),
											discovered.projects(), java.util.Set.of("example.unsupported"), discovered.dataFiles());
										var blocked = root.resolve("blocked.gamp");
										java.nio.file.Files.writeString(blocked, "preserve previous archive");
										try {
											gama.export.SimulationPackageExporter.export(unsupportedModels, discovered.experiments(), config,
												"example.blocked", "1.0", java.util.List.of(), blocked, new org.eclipse.core.runtime.NullProgressMonitor());
											throw new AssertionError("Unsupported extra code must not silently become a host requirement");
										} catch (java.io.IOException expected) {
											if (!expected.getMessage().contains("example.unsupported")) throw expected;
										}
										if (!java.nio.file.Files.readString(blocked).equals("preserve previous archive"))
											throw new AssertionError("Unsupported dependency changed the destination");
									} finally { unsupported.uninstall(); }
									if (!description.equals(java.nio.file.Files.readString(project.getLocation().toFile().toPath().resolve(".project"))))
										throw new AssertionError("Author project metadata changed");
									if (!text.equals(new String(model.getContents().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)))
										throw new AssertionError("Author model changed");
									java.nio.file.Files.writeString(root.resolve("authoring.ready"), output.toString());
								} catch (Throwable failure) {
									try (var output = new java.io.PrintWriter(java.nio.file.Files.newBufferedWriter(root.resolve("authoring.failed")))) {
										failure.printStackTrace(output);
									} catch (java.io.IOException e) { throw new IllegalStateException(e); }
								}
							});
						}
					}
					""");
			compilerArgs.add(exportSource.toString());
		}
		if (ToolProvider.getSystemJavaCompiler().run(null, null, null, compilerArgs.toArray(String[]::new)) != 0)
			throw new AssertionError("Probe compilation failed");
		final var metadata = new Manifest();
		metadata.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		metadata.getMainAttributes().putValue("Bundle-ManifestVersion", "2");
		metadata.getMainAttributes().putValue("Bundle-SymbolicName", "example.packageprobe;singleton:=true");
		metadata.getMainAttributes().putValue("Bundle-ActivationPolicy", "lazy");
		metadata.getMainAttributes().putValue("Bundle-Version", "1.0.0");
		metadata.getMainAttributes().putValue("Import-Package", "example.packagehelper");
		metadata.getMainAttributes().putValue("Require-Bundle", unresolved ? "gama.api,missing.package.dependency"
				: authoring ? "gama.api,gama.export,org.eclipse.ui,org.eclipse.e4.ui.model.workbench,org.eclipse.core.resources,org.eclipse.core.runtime,org.eclipse.osgi,org.eclipse.swt" : "gama.api");
		Files.createDirectories(staging.resolve("plugins"));
		final var helperMetadata = new Manifest();
		helperMetadata.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		helperMetadata.getMainAttributes().putValue("Bundle-ManifestVersion", "2");
		helperMetadata.getMainAttributes().putValue("Bundle-SymbolicName", "example.packagehelper");
		helperMetadata.getMainAttributes().putValue("Bundle-Version", "1.0.0");
		helperMetadata.getMainAttributes().putValue("Export-Package", "example.packagehelper");
		try (var jar = new JarOutputStream(Files.newOutputStream(staging.resolve("plugins/helper.jar")), helperMetadata)) {
			jar.putNextEntry(new JarEntry("example/packagehelper/Helper.class"));
			Files.copy(classes.resolve("example/packagehelper/Helper.class"), jar);
			jar.closeEntry();
		}
		try (var jar = new JarOutputStream(Files.newOutputStream(staging.resolve("plugins/probe.jar")), metadata)) {
			jar.putNextEntry(new JarEntry("plugin.xml"));
			jar.write(("<plugin><extension point=\"gaml.extension\"/>" + (authoring
					? "<extension point=\"org.eclipse.ui.startup\"><startup class=\"gaml.additions.packageprobe.PackageExportProbe\"/></extension>"
					: "") + "</plugin>").getBytes(java.nio.charset.StandardCharsets.UTF_8));
			jar.closeEntry();
			if (authoring) {
				jar.putNextEntry(new JarEntry("gaml/additions/packageprobe/PackageExportProbe.class"));
				Files.copy(classes.resolve("gaml/additions/packageprobe/PackageExportProbe.class"), jar);
				jar.closeEntry();
			}
			jar.putNextEntry(new JarEntry("gaml/additions/packageprobe/GamlAdditions.class"));
			Files.copy(classes.resolve("gaml/additions/packageprobe/GamlAdditions.class"), jar);
			jar.closeEntry();
		}
		final var manifest = new SimulationPackageManifest("example.smoke", "1", "0.0.0-SNAPSHOT",
				List.of(new SimulationPackageManifest.Experiment("Example/model.gaml", "Run")),
				List.of(new SimulationPackageManifest.BundleRequirement("example.packageprobe", "1.0.0", "plugins/probe.jar"),
						new SimulationPackageManifest.BundleRequirement("example.packagehelper", "1.0.0", "plugins/helper.jar"),
						new SimulationPackageManifest.BundleRequirement("gama.api", "0.0.0", "")));
		final Path archive = root.resolve("smoke.gamp");
		SimulationPackage.write(staging, archive, manifest);
		if (Boolean.getBoolean("gama.smoke.prepareOnly")) {
			System.out.println("Prepared smoke archive: " + archive);
			return;
		}
		var prepared = SimulationPackageLauncher.prepare(archive, root.resolve("sessions"), install,
				Path.of(args[1]), "0.0.0-SNAPSHOT");
		if (authoring) Files.writeString(prepared.content().resolve("authoring.request"), "");
		if (home && authoring) {
			final var command = new java.util.ArrayList<>(prepared.command());
			command.add("-Dgama.smoke.home=true");
			prepared = new SimulationPackageLauncher.Prepared(prepared.content(), prepared.configuration(), prepared.workspace(), command);
		}
		if (authoring && Boolean.getBoolean("gama.smoke.ui")) Files.writeString(prepared.content().resolve("ui-checks.request"), "");
		System.out.println("Smoke session: " + prepared.content());
		final var process = prepared.start();
		try {
			try {
				SimulationPackageSessions.delete(root.resolve("sessions"), prepared.content());
				throw new AssertionError("An active simulation session must not be deleted");
			} catch (java.io.IOException expected) {}
			if (unresolved) {
				if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() == 0)
					throw new AssertionError("Unresolved dependencies must abort the player");
				if (Files.exists(prepared.content().resolve("bootstrap.ready"))
						|| Files.exists(prepared.content().resolve("player.ready"))
						|| Files.readString(prepared.content().resolve("player.log")).contains("Building metamodel"))
					throw new AssertionError("Unresolved plugins must fail before GAML and UI startup");
				System.out.println("Unresolved package dependencies rejected before GAML initialization");
				return;
			}
			final Path simulation = prepared.workspace().resolve("Example/simulation.ready");
			for (int i = 0; i < 120; i++) {
				if (Files.exists(home ? prepared.content().resolve("player.ready") : simulation)) break;
				if (process.waitFor(1, TimeUnit.SECONDS)) throw new AssertionError("Player exited: " + process.exitValue());
			}
			for (final Path marker : List.of(prepared.content().resolve("bootstrap.ready"),
					prepared.content().resolve("plugin.ready"), prepared.content().resolve("player.ready"))) {
				if (!Files.isRegularFile(marker)) throw new AssertionError("Missing smoke marker: " + marker);
			}
			if (home && Files.exists(simulation)) throw new AssertionError("Home-first packages must not auto-run");
			if (!home && !Files.exists(simulation)) throw new AssertionError("Default experiment was not initialized");
			System.out.println(home ? "Package bootstrap, language plugin and Home-first startup passed"
					: "Package bootstrap, language plugin, workbench, compilation and experiment init passed");
			if (authoring) {
				if (home) {
					for (int i = 0; i < 60 && !Files.exists(prepared.content().resolve("home-ui.ready")); i++) {
						if (Files.exists(prepared.content().resolve("authoring.failed")))
							throw new AssertionError(Files.readString(prepared.content().resolve("authoring.failed")));
						Thread.sleep(1000);
					}
					if (!Files.exists(prepared.content().resolve("home-ui.ready"))) throw new AssertionError("Home view was not verified");
					System.out.println("Actual Home perspective, visible Home view and hidden editor area verified");
					return;
				}
				for (int i = 0; i < 120; i++) {
					if (Files.exists(prepared.content().resolve("authoring.failed")))
						throw new AssertionError(Files.readString(prepared.content().resolve("authoring.failed")));
					if (Files.exists(prepared.content().resolve("authoring.ready"))) break;
					if (process.waitFor(1, TimeUnit.SECONDS)) throw new AssertionError("Authoring player exited");
				}
				if (!Files.exists(prepared.content().resolve("authoring.ready")))
					throw new AssertionError("Workbench export did not complete: " + Files.readString(prepared.content().resolve("player.log")));
				final Path authored = Path.of(Files.readString(prepared.content().resolve("authoring.ready")));
				if (Boolean.getBoolean("gama.smoke.ui") && !Files.exists(prepared.content().resolve("ui-checks.ready")))
					throw new AssertionError("Workbench UI checks did not complete");
				final var exported = SimulationPackageLauncher.prepare(authored, root.resolve("sessions"), install,
						Path.of(args[1]), "0.0.0-SNAPSHOT");
				final Path ready = exported.workspace().resolve("Example/exported.ready");
				if (Files.exists(ready)) throw new AssertionError("Round-trip marker must not be copied from authoring");
				final String projectXml = Files.readString(exported.workspace().resolve("Example/.project"));
				if (projectXml.contains("linkedResources") || !Files.exists(exported.workspace().resolve("Example/linked.csv")))
					throw new AssertionError("Author links must be materialized");
				final var player = exported.start();
				try {
					for (int i = 0; i < 120 && !Files.exists(ready); i++) {
						if (player.waitFor(1, TimeUnit.SECONDS)) throw new AssertionError("Exported player exited: "
								+ Files.readString(exported.content().resolve("player.log")));
					}
					if (!Files.exists(ready)) throw new AssertionError("Authored package failed to initialize: "
							+ Files.readString(exported.content().resolve("player.log")));
					System.out.println("Workbench export, automatic non-standard plugin closure, unsupported plugin rejection and player round-trip passed");
				} finally {
					player.destroy();
					if (!player.waitFor(15, TimeUnit.SECONDS)) {
						player.destroyForcibly();
						player.waitFor(15, TimeUnit.SECONDS);
					}
				}
			}
		} finally {
			process.destroy();
			if (!process.waitFor(15, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				process.waitFor(15, TimeUnit.SECONDS);
			}
		}
	}

	/** Tests compiled classes without changing any files in the installed application. */
	static Path overlay(final Path installation, final Path repository, final Path root) throws Exception {
		final Path install = installation.toRealPath();
		final Path overlay = Files.createDirectory(root.resolve("installation"));
		final String info = "configuration/org.eclipse.equinox.simpleconfigurator/bundles.info";
		final var inventory = new StringBuilder("#version=1\n");
		for (final String line : Files.readAllLines(install.resolve(info))) {
			if (line.isBlank() || line.startsWith("#")) continue;
			final String[] parts = line.split(",");
			final var uri = java.net.URI.create(parts[2]);
			Path bundle = uri.isAbsolute() ? Path.of(uri) : install.resolve(parts[2]);
			if (List.of("gama.api", "gama.export", "gama.ui.export", "gama.ui.application", "gama.ui.shared",
					"gama.ui.navigator").contains(parts[0])) {
				final Path source = repository.resolve(parts[0]);
				final var contents = new java.util.LinkedHashMap<String, byte[]>();
				final boolean directory = Files.isDirectory(bundle);
				if (directory) {
					try (var paths = Files.walk(bundle)) {
						for (final Path path : paths.filter(Files::isRegularFile).toList()) {
							contents.put(bundle.relativize(path).toString(), Files.readAllBytes(path));
						}
					}
				} else {
					try (var jar = new java.util.jar.JarFile(bundle.toFile())) {
						final var entries = jar.entries();
						while (entries.hasMoreElements()) {
							final var entry = entries.nextElement();
							if (entry.isDirectory() || entry.getName().equals("META-INF/MANIFEST.MF")
									|| entry.getName().matches("META-INF/.*\\.(SF|RSA|DSA)")) continue;
							try (var input = jar.getInputStream(entry)) { contents.put(entry.getName(), input.readAllBytes()); }
						}
					}
				}
				try (var paths = Files.walk(source.resolve("target/classes"))) {
					for (final Path path : paths.filter(Files::isRegularFile).toList()) {
						final String name = source.resolve("target/classes").relativize(path).toString();
						// Keep the installed API/core pair; exercise only the changed extension loader.
						if (parts[0].equals("gama.api") && !name.startsWith("gama/api/additions/GamaBundleLoader"))
							continue;
						contents.put(name, Files.readAllBytes(path));
					}
				}
				contents.remove("META-INF/MANIFEST.MF");
				if (Boolean.getBoolean("gama.smoke.ui")) {
					final String test = parts[0].equals("gama.ui.export") ? "gama/ui/export/dialogs/LauncherHomeTest.class"
							: parts[0].equals("gama.ui.shared") ? "gama/ui/shared/utils/LaunchPresentationTest.class" : null;
					if (test != null) {
						try (var input = SimulationPackageSmokeTest.class.getClassLoader().getResourceAsStream(test)) {
							if (input == null) throw new IllegalStateException("Compile the workbench UI test first: " + test);
							contents.put(test, input.readAllBytes());
						}
					}
				}
				contents.put("plugin.xml", Files.readAllBytes(source.resolve("plugin.xml")));
				final Manifest manifest;
				try (var input = Files.newInputStream(source.resolve("META-INF/MANIFEST.MF"))) { manifest = new Manifest(input); }
				manifest.getMainAttributes().putValue("Bundle-Version", parts[1]);
				bundle = overlay.resolve(parts[0] + (directory ? "" : ".jar"));
				if (directory) {
					Files.createDirectories(bundle.resolve("META-INF"));
					try (var output = Files.newOutputStream(bundle.resolve("META-INF/MANIFEST.MF"))) { manifest.write(output); }
					for (final var entry : contents.entrySet()) {
						Files.createDirectories(bundle.resolve(entry.getKey()).getParent());
						Files.write(bundle.resolve(entry.getKey()), entry.getValue());
					}
				} else {
					try (var jar = new JarOutputStream(Files.newOutputStream(bundle), manifest)) {
						for (final var entry : contents.entrySet()) {
							jar.putNextEntry(new JarEntry(entry.getKey()));
							jar.write(entry.getValue());
							jar.closeEntry();
						}
					}
				}
			}
			parts[2] = bundle.toUri().toASCIIString();
			inventory.append(String.join(",", parts)).append('\n');
		}
		Files.createDirectories(overlay.resolve(info).getParent());
		Files.writeString(overlay.resolve(info), inventory);
		Files.copy(install.resolve("configuration/config.ini"), overlay.resolve("configuration/config.ini"));
		return overlay;
	}
}
