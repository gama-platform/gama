package gama.export;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;

public final class ExportedModelInfoTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static IFile file(final Path path) {
		return (IFile) Proxy.newProxyInstance(IFile.class.getClassLoader(), new Class<?>[] { IFile.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "getModificationStamp" -> 42L;
					case "getName" -> path.getFileName().toString();
					case "getLocation" -> org.eclipse.core.runtime.Path.fromOSString(path.toString());
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Files.createTempDirectory("gamx-metadata-test-");
		try {
			final Path source = Files.createDirectories(root.resolve("source"));
			Files.createDirectories(source.resolve("workspace/Example/models"));
			Files.createDirectories(source.resolve("configuration"));
			Files.createDirectories(source.resolve("plugins"));
			Files.writeString(source.resolve("workspace/Example/.project"), "<projectDescription/>");
			Files.writeString(source.resolve("workspace/Example/models/Ants.gaml"), "model ants");
			Files.writeString(source.resolve(LauncherConfiguration.FILE), LauncherConfiguration.defaults().serialize());
			Files.writeString(source.resolve("plugins/example.jar"), "Not executed when reading metadata");
			final var manifest = new SimulationPackageManifest("example.ants", "1.0", "2026.1",
					List.of(new SimulationPackageManifest.Experiment("Example/models/Ants.gaml", "Classic <&>_!_"),
							new SimulationPackageManifest.Experiment("Example/models/Ants.gaml", "Explore")),
					List.of(new SimulationPackageManifest.BundleRequirement("gama.core", "1.0.0", ""),
							new SimulationPackageManifest.BundleRequirement("example.extra", "1.2.3", "plugins/example.jar")));
			final Path archive = root.resolve("Ants.gamx");
			SimulationPackage.write(source, archive, manifest, () -> false);
			final List<Path> before;
			try (var paths = Files.walk(root)) { before = paths.sorted().toList(); }
			final var info = new ExportedModelInfo(file(archive));
			check(info.getSuffix().equals("Example/models/Ants.gaml | 2 experiments"), "Deduplicate model names");
			check(info.getDescription().contains("Classic <&>_!_") && info.getDescription().contains("GAMA 2026.1 or newer")
					&& info.getDescription().contains("example.extra 1.2.3"), "Show experiments, runtime and embedded plugins");
			check(!info.getDescription().contains("gama.core"), "Do not clutter metadata with standard dependencies");
			check(info.getDocumentation().getContents().contains("Classic &lt;&amp;&gt;_!_"), "Escape archive text in HTML");
			final var cached = new ExportedModelInfo(info.toPropertyString());
			check(cached.getModificationStamp() == 42 && cached.getDescription().equals(info.getDescription())
					&& cached.getSuffix().equals(info.getSuffix()), "Lossless metadata cache, including delimiter characters");
			try (var paths = Files.walk(root)) {
				check(before.equals(paths.sorted().toList()), "Reading metadata must not extract files");
			}
			Files.writeString(archive, "Not a ZIP");
			check(new ExportedModelInfo(file(archive)).getSuffix().startsWith("Invalid playable model:"),
					"Corrupt archives must report an explicit failure");
		} finally {
			try (var paths = Files.walk(root)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
			}
		}
		System.out.println("Playable model metadata, safe inspection, error and cache checks passed");
	}
}
