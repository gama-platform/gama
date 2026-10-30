package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/** Tracks files produced since startup or the last completed save. */
public final class SimulationArtifacts {

	private Map<Path, String> baseline = Map.of();

	public void initialize(final Path workspace) throws IOException {
		baseline = snapshot(workspace, new org.eclipse.core.runtime.NullProgressMonitor());
	}

	private Map<Path, String> snapshot(final Path workspace, final IProgressMonitor monitor) throws IOException {
		final Map<Path, String> files = new HashMap<>();
		Files.walkFileTree(workspace, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				final String name = directory.getFileName().toString();
				return !directory.equals(workspace) && (name.equals(".metadata") || name.equals(".settings"))
						? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				final String name = file.getFileName().toString();
				if (!attributes.isRegularFile() || name.equals(".project") || name.equals(".classpath"))
					return FileVisitResult.CONTINUE;
				try {
					final MessageDigest digest = MessageDigest.getInstance("SHA-256");
					try (var input = new DigestInputStream(Files.newInputStream(file), digest)) {
						final byte[] buffer = new byte[8192];
						while (input.read(buffer) != -1) {
							if (monitor.isCanceled()) throw new OperationCanceledException();
						}
					}
					files.put(workspace.relativize(file), HexFormat.of().formatHex(digest.digest()));
				} catch (final NoSuchAlgorithmException e) {
					throw new IllegalStateException("SHA-256 is unavailable", e);
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return files;
	}

	public boolean hasChanges(final Path workspace) throws IOException {
		return snapshot(workspace, new org.eclipse.core.runtime.NullProgressMonitor()).entrySet().stream()
				.anyMatch(entry -> !entry.getValue().equals(baseline.get(entry.getKey())));
	}

	public boolean save(final Path workspace, final Path destination, final IProgressMonitor monitor) throws IOException {
		final Path source = workspace.toRealPath();
		final Path target = destination.toAbsolutePath().normalize();
		if (target.startsWith(source)) throw new IOException("The destination must be outside the simulation workspace");
		Files.createDirectories(target);
		if (target.toRealPath().startsWith(source))
			throw new IOException("The destination must be outside the simulation workspace");
		boolean saved = false;
		monitor.beginTask("Saving simulation artifacts", IProgressMonitor.UNKNOWN);
		try {
			final var current = snapshot(source, monitor);
			for (final var entry : current.entrySet()) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				if (entry.getValue().equals(baseline.get(entry.getKey()))) continue;
				final Path output = target.resolve(entry.getKey());
				Files.createDirectories(output.getParent());
				Files.copy(source.resolve(entry.getKey()), output, StandardCopyOption.REPLACE_EXISTING);
				monitor.worked(1);
				saved = true;
			}
			baseline = current;
		} finally {
			monitor.done();
		}
		return saved;
	}
}
