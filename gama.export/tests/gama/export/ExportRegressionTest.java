package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import gama.workspace.manager.WorkspacePaths;

/** Standalone checks; run with the export bundle's runtime classpath. */
public final class ExportRegressionTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Files.createTempDirectory("gama-export-regression-");
		final String previousLocation = System.getProperty("eclipse.home.location");
		try {
			final Path installation = Files.createDirectories(root.resolve("installation with spaces"));
			System.setProperty("eclipse.home.location", installation.toUri().toString());
			check(WorkspacePaths.installationDirectory().equals(installation), "Installation URI must be decoded");
			check(WorkspacePaths.resolveAgainstInstallation("Embedded_Workspace")
					.equals(installation.resolve("Embedded_Workspace").toString()), "Relative workspace resolution");
			check(WorkspacePaths.resolveAgainstInstallation(root.toString()).equals(root.toString()),
					"Absolute workspaces must not be changed");
			check(WorkspacePaths.resolveAgainstInstallation((String) null) == null, "Missing workspace preference");
			check(WorkspacePaths.resolveAgainstInstallation("").isEmpty(), "Empty workspace preference");
			check(LauncherConfiguration.load(installation).equals(LauncherConfiguration.defaults()),
					"Older launchers default to the home page");
			final Path configuration = installation.resolve(LauncherConfiguration.FILE);
			Files.createDirectories(configuration.getParent());
			final var home = new LauncherConfiguration("Model \u00e9", "Description\nSecond line", true, "",
					root.resolve("private-source-image.png").toString());
			Files.writeString(configuration, home.serialize());
			final var loadedHome = LauncherConfiguration.load(installation);
			check(loadedHome.title().equals(home.title()) && loadedHome.description().equals(home.description()),
					"Home text must round-trip in UTF-8");
			check(loadedHome.showHome(), "Home startup must be preserved");
			check(loadedHome.image().equals(LauncherConfiguration.IMAGE), "Image must use its embedded path");
			check(!home.serialize().contains("private-source-image"), "Do not embed the author's image path");
			final var direct = new LauncherConfiguration("Model", "", false, "Run@project/model.gaml", "");
			Files.writeString(configuration, direct.serialize());
			check(LauncherConfiguration.load(installation).equals(direct), "Direct startup must round-trip");

			final Path project = Files.createDirectories(root.resolve("project"));
			final Path baseline = Files.writeString(project.resolve("model.gaml"), "model test");
			Files.setLastModifiedTime(baseline, FileTime.fromMillis(1));
			final var artifacts = new SimulationArtifacts();
			check(!artifacts.hasChanges(project), "Existing model files are not artifacts");
			final Path data = Files.createDirectories(project.resolve("output")).resolve("result.csv");
			Files.writeString(data, "value\n42\n");
			Files.setLastModifiedTime(data, FileTime.fromMillis(System.currentTimeMillis() + 1000));
			check(artifacts.hasChanges(project), "New nested output must be detected");

			final var canceled = new NullProgressMonitor();
			canceled.setCanceled(true);
			try {
				artifacts.save(project, root.resolve("canceled"), canceled);
				throw new AssertionError("Cancellation must be reported");
			} catch (final OperationCanceledException expected) {}
			check(artifacts.hasChanges(project), "Cancellation must not advance the save watermark");
			final Path destination = root.resolve("saved");
			check(artifacts.save(project, destination, new NullProgressMonitor()), "The output should be saved");
			check(Files.readString(destination.resolve("output/result.csv")).equals("value\n42\n"), "Output content");
			check(!Files.exists(destination.resolve("model.gaml")), "Unchanged source must not be copied");
			Files.setLastModifiedTime(data, FileTime.fromMillis(1));
			check(!artifacts.hasChanges(project), "Saved files are not pending");
			try {
				artifacts.save(project, project.resolve("nested-destination"), new NullProgressMonitor());
				throw new AssertionError("Saving inside the source must be rejected");
			} catch (final IOException expected) {}
		} finally {
			if (previousLocation == null) {
				System.clearProperty("eclipse.home.location");
			} else {
				System.setProperty("eclipse.home.location", previousLocation);
			}
			try (var paths = Files.walk(root)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
			}
		}
		System.out.println("Export path, launcher configuration and artifact regression checks passed");
	}
}
