package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/** Tracks files produced since startup or the last completed save. */
public final class SimulationArtifacts {

	private long lastSaved = Instant.now().toEpochMilli();

	public boolean hasChanges(final Path project) throws IOException {
		try (Stream<Path> paths = Files.walk(project)) {
			for (final Path path : (Iterable<Path>) paths::iterator) {
				if (Files.isRegularFile(path) && Files.getLastModifiedTime(path).toMillis() > lastSaved) return true;
			}
		}
		return false;
	}

	public boolean save(final Path project, final Path destination, final IProgressMonitor monitor) throws IOException {
		final Path source = project.toRealPath();
		final Path target = destination.toAbsolutePath().normalize();
		if (target.startsWith(source)) throw new IOException("The destination must be outside the simulation project");
		Files.createDirectories(target);
		if (target.toRealPath().startsWith(source))
			throw new IOException("The destination must be outside the simulation project");
		final long cutoff = Instant.now().toEpochMilli();
		boolean saved = false;
		monitor.beginTask("Saving simulation artifacts", IProgressMonitor.UNKNOWN);
		try (Stream<Path> paths = Files.walk(source)) {
			for (final Path path : (Iterable<Path>) paths::iterator) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				if (!Files.isRegularFile(path) || Files.getLastModifiedTime(path).toMillis() <= lastSaved) continue;
				final Path output = target.resolve(source.relativize(path));
				Files.createDirectories(output.getParent());
				Files.copy(path, output, StandardCopyOption.REPLACE_EXISTING);
				monitor.worked(1);
				saved = true;
			}
		} finally {
			monitor.done();
		}
		lastSaved = cutoff;
		return saved;
	}
}
