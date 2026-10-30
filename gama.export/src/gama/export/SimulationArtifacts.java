package gama.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/** Tracks files produced since startup or the last completed save. */
public final class SimulationArtifacts {

	private Map<Path, String> baseline = Map.of();
	private Map<Path, String> original = Map.of();
	private Path originals;
	private Path workspaceRoot;

	public void initialize(final Path workspace) throws IOException {
		if (workspaceRoot != null) throw new IllegalStateException("Simulation artifacts are already initialized");
		final Path source = workspace.toRealPath();
		final var initial = snapshot(source, new org.eclipse.core.runtime.NullProgressMonitor());
		final Path backup = Files.createTempDirectory("gama-player-inputs-");
		try {
			for (final Path relative : initial.keySet()) {
				final Path target = backup.resolve(relative);
				Files.createDirectories(target.getParent());
				Files.copy(source.resolve(relative), target);
			}
		} catch (final IOException | RuntimeException e) {
			try { deleteBackup(backup); } catch (final IOException cleanup) { e.addSuppressed(cleanup); }
			throw e;
		}
		workspaceRoot = source;
		originals = backup;
		original = baseline = initial;
	}

	/** Discards generated files and restores inputs from before the player started. */
	public void discard(final Path workspace) throws IOException {
		if (workspaceRoot == null || !workspace.toRealPath().equals(workspaceRoot) || originals == null)
			throw new IOException("No original inputs are available for this simulation workspace");
		final var current = snapshot(workspaceRoot, new org.eclipse.core.runtime.NullProgressMonitor());
		final var paths = new java.util.HashSet<>(current.keySet());
		paths.addAll(original.keySet());
		for (final Path relative : paths) {
			if (java.util.Objects.equals(current.get(relative), original.get(relative))) continue;
			final Path target = workspaceRoot.resolve(relative);
			for (Path parent = target.getParent(); !parent.equals(workspaceRoot); parent = parent.getParent()) {
				if (Files.exists(parent, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(parent))
					throw new IOException("Cannot discard data through a symbolic link: " + parent);
			}
			if (original.containsKey(relative)) {
				Files.createDirectories(target.getParent());
				Files.copy(originals.resolve(relative), target, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.delete(target);
			}
		}
		baseline = original;
	}

	public void releaseOriginals() throws IOException {
		if (originals == null) return;
		deleteBackup(originals);
		originals = null;
	}

	private static void deleteBackup(final Path backup) throws IOException {
		Files.walkFileTree(backup, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path directory, final IOException error) throws IOException {
				if (error != null) throw error;
				Files.delete(directory);
				return FileVisitResult.CONTINUE;
			}
		});
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
		return !snapshot(workspace, new org.eclipse.core.runtime.NullProgressMonitor()).equals(baseline);
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

	public void saveZip(final Path workspace, final Path destination, final IProgressMonitor monitor) throws IOException {
		final Path source = workspace.toRealPath();
		final Path target = destination.toAbsolutePath().normalize();
		if (target.getParent() == null) throw new IOException("Choose a destination file");
		final Path parent = target.getParent().toRealPath();
		if (parent.startsWith(source) || Files.exists(target) && target.toRealPath().startsWith(source))
			throw new IOException("The destination must be outside the simulation workspace");
		final Path staging = Files.createTempFile(parent, ".gama-data-", ".zip");
		monitor.beginTask("Saving experiment data", IProgressMonitor.UNKNOWN);
		try {
			final var current = snapshot(source, monitor);
			try (var zip = new ZipOutputStream(Files.newOutputStream(staging))) {
				final byte[] buffer = new byte[8192];
				for (final var entry : current.entrySet()) {
					if (monitor.isCanceled()) throw new OperationCanceledException();
					if (entry.getValue().equals(baseline.get(entry.getKey()))) continue;
					zip.putNextEntry(new ZipEntry(entry.getKey().toString().replace('\\', '/')));
					try (var input = Files.newInputStream(source.resolve(entry.getKey()))) {
						int read;
						while ((read = input.read(buffer)) != -1) {
							if (monitor.isCanceled()) throw new OperationCanceledException();
							zip.write(buffer, 0, read);
						}
					}
					zip.closeEntry();
					monitor.worked(1);
				}
			}
			if (monitor.isCanceled()) throw new OperationCanceledException();
			Files.move(staging, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			baseline = current;
		} finally {
			try {
				Files.deleteIfExists(staging);
			} finally {
				monitor.done();
			}
		}
	}
}
