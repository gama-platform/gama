package gama.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** JDK-only authoring checks, including Eclipse links already enumerated at their logical paths. */
public final class PackageProjectStagerTest {

	@FunctionalInterface
	private interface CheckedRunnable { void run() throws Exception; }

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static void rejects(final CheckedRunnable runnable) throws Exception {
		try {
			runnable.run();
			throw new AssertionError("Expected an explicit export failure");
		} catch (final IOException expected) {
			check(expected.getMessage() != null, "Explain export failures");
		}
	}

	private static PackageProjectStager.Source source(final Path file, final String path) {
		return new PackageProjectStager.Source(file, path, StandardCharsets.UTF_8);
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Files.createTempDirectory("gama-package-staging-test-");
		try {
			final Path project = Files.createDirectories(root.resolve("author"));
			final Path description = project.resolve(".project");
			final String xml = """
					<projectDescription><name>Example</name><comment>Keep this</comment>
					<buildSpec><buildCommand><name>gaml.compiler.gamlBuilder</name></buildCommand></buildSpec>
					<natures><nature>gaml.compiler.gamlNature</nature></natures>
					<linkedResources><link><name>linked</name><type>1</type><location>/private/source</location></link></linkedResources>
					<variableList><variable><name>PRIVATE</name><value>/private</value></variable></variableList>
					<filteredResources><filter><name>Hidden</name></filter></filteredResources></projectDescription>
					""";
			Files.writeString(description, xml);
			final Path model = project.resolve("model.gaml");
			final Path external = Files.writeString(root.resolve("data \u00e9.csv"), "first,second\n");
			final Path folder = Files.createDirectories(root.resolve("dataset/subdirectory"));
			Files.writeString(folder.resolve("values.csv"), "3,4\n");
			final Path shape = Files.writeString(root.resolve("map.shp"), "shape");
			Files.writeString(root.resolve("map.DBF"), "companion");
			Files.writeString(root.resolve("map.shp.xml"), "metadata");
			final String text = "// \"../data \u00e9.csv\" unchanged\n/* '../map.shp' unchanged */\n"
					+ "model sample\nglobal { string data <- '../data \u00e9.csv';"
					+ " string directory <- '../dataset'; string shape <- '../map.shp';"
					+ " string normal <- 'do not change'; }\nexperiment Run type: gui {}";
			Files.writeString(model, text);
			final Path linked = Files.writeString(root.resolve("linked.gaml"), "model linked\n// caf\u00e9",
					StandardCharsets.ISO_8859_1);
			final var sources = List.of(source(description, "Example/.project"), source(model, "Example/model.gaml"),
					new PackageProjectStager.Source(linked, "Example/links/linked.gaml", StandardCharsets.ISO_8859_1));
			final Path staged = root.resolve("staged");
			PackageProjectStager.stage(staged, sources,
					Map.of("Example", Set.of(external, shape, folder.getParent())), () -> false);
			check(Files.readString(model).equals(text) && Files.readString(description).equals(xml),
					"Never modify the author's resources");
			final String packaged = Files.readString(staged.resolve("workspace/Example/model.gaml"));
			check(packaged.contains("// \"../data \u00e9.csv\" unchanged")
					&& packaged.contains("/* '../map.shp' unchanged */") && packaged.contains("'do not change'"),
					"Do not rewrite comments or unrelated literals");
			check(!packaged.contains("string data <- '../") && !packaged.contains("string directory <- '../")
					&& !packaged.contains("string shape <- '../"), "Relocate all external file and directory references");
			final var references = java.util.regex.Pattern.compile("string (data|directory|shape) <- '([^']+)'").matcher(packaged);
			int referencesChecked = 0;
			while (references.find()) {
				check(Files.exists(staged.resolve("workspace/Example").resolve(references.group(2))),
						"Every rewritten data reference must point to staged content");
				referencesChecked++;
			}
			check(referencesChecked == 3, "Check the file, directory and shapefile paths");
			try (var files = Files.walk(staged)) {
				final var names = files.map(path -> path.getFileName().toString()).toList();
				check(names.containsAll(List.of("data \u00e9.csv", "map.shp", "map.DBF", "map.shp.xml", "values.csv")),
						"Include directory trees, Unicode files and shapefile companions");
			}
			check(Files.readString(staged.resolve("workspace/Example/links/linked.gaml")).contains("caf\u00e9"),
					"Materialize linked models as portable UTF-8 files");
			check(Files.readString(staged.resolve("workspace/Example/.settings/org.eclipse.core.resources.prefs"))
					.contains("encoding/links/linked.gaml=UTF-8"), "Record model encoding independently of author workspace defaults");
			final String materialized = Files.readString(staged.resolve("workspace/Example/.project"));
			check(!materialized.contains("linkedResources") && !materialized.contains("variableList")
					&& !materialized.contains("filteredResources") && materialized.contains("gaml.compiler.gamlBuilder"),
					"Remove author-only metadata but retain builders and natures");
			final Path archive = root.resolve("result.gamx");
			Files.createDirectories(staged.resolve("configuration"));
			Files.writeString(staged.resolve(LauncherConfiguration.FILE), LauncherConfiguration.defaults().serialize());
			final var manifest = new SimulationPackageManifest("example.test", "1.0", "test",
					List.of(new SimulationPackageManifest.Experiment("Example/model.gaml", "Run")), List.of());
			SimulationPackage.write(staged, archive, manifest);
			final var extracted = SimulationPackage.extract(archive, root.resolve("sessions"), "test");
			check(Files.readString(extracted.workspace().resolve("Example/model.gaml")).equals(packaged),
					"Authored payload survives writing and verified extraction");
			final byte[] previous = Files.readAllBytes(archive);
			final var calls = new java.util.concurrent.atomic.AtomicInteger();
			rejects(() -> SimulationPackage.write(staged, archive, manifest, () -> calls.incrementAndGet() > 5));
			check(java.util.Arrays.equals(previous, Files.readAllBytes(archive)), "Canceled exports preserve the old archive");
			try (var files = Files.list(root)) {
				check(files.noneMatch(path -> path.getFileName().toString().startsWith(".gama-package-")),
						"Canceled archive writes remove their temporary output");
			}
			rejects(() -> PackageProjectStager.stage(root.resolve("canceled"), sources, Map.of(), () -> true));
			rejects(() -> PackageProjectStager.stage(staged, sources, Map.of(), () -> false));
			rejects(() -> PackageProjectStager.stage(root.resolve("missing"), sources,
					Map.of("Example", Set.of(root.resolve("missing.csv"))), () -> false));
			final Path empty = Files.createDirectory(root.resolve("empty"));
			rejects(() -> PackageProjectStager.stage(root.resolve("empty-data"), sources,
					Map.of("Example", Set.of(empty)), () -> false));
			final var collision = new ArrayList<>(sources);
			collision.add(source(model, "Example/MODEL.gaml"));
			rejects(() -> PackageProjectStager.stage(root.resolve("collision"), collision, Map.of(), () -> false));
			rejects(() -> PackageProjectStager.stage(root.resolve("ancestor-collision"),
					List.of(source(model, "Example/Models/a.gaml"), source(model, "Example/models/b.gaml")),
					Map.of(), () -> false));
			final Path symlink = Files.createSymbolicLink(root.resolve("symlink.csv"), external);
			rejects(() -> PackageProjectStager.stage(root.resolve("symlink-source"),
					List.of(source(symlink, "Example/data.csv")), Map.of(), () -> false));
			rejects(() -> PackageProjectStager.projectDescription(xml, "Other"));
			rejects(() -> PackageProjectStager.projectDescription("<!DOCTYPE x SYSTEM 'file:///private/none'>" + xml, "Example"));
			final Path original = root.resolve("models/model.gaml");
			final Path target = Path.of("workspace/Example/models/model.gaml");
			final String escaped = "'../a\\'b.csv' \"../a\\\"b.csv\" 'line\\ntext' // '../a\\'b.csv'";
			final String rewritten = GamlExportPaths.rewrite(escaped, original, target,
					Map.of(root.resolve("a'b.csv"), Path.of("workspace/Example/data/a'b.csv"),
							root.resolve("a\"b.csv"), Path.of("workspace/Example/data/a\"b.csv")));
			check(rewritten.equals("'../data/a\\'b.csv' \"../data/a\\\"b.csv\" 'line\\ntext' // '../a\\'b.csv'"),
					"Preserve quotes, escape sequences and comments while rewriting static literals");
			check(GamlExportPaths.rewrite("'..\\\\nested\\\\file.csv'", original, target,
					Map.of(root.resolve("nested/file.csv"), Path.of("workspace/Example/data/file.csv")))
					.equals("'../data/file.csv'"), "Decode escaped Windows separators without confusing them with control escapes");
			System.out.println("Playable model project staging, path rewriting, encoding and cancellation checks passed");
		} finally {
			try (var paths = Files.walk(root)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
			}
		}
	}
}
