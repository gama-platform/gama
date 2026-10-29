package gama.export;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/** Recovery inventory and explicit cleanup for application-owned player directories only. */
public final class SimulationPackageSessions {

	private static final String METADATA = "player-session.properties";
	private static final String LOCK = "player-session.lock";

	public record Session(Path content, String id, String version, Instant created, String source, boolean running) {}

	public static final class Lease implements AutoCloseable {
		private final FileChannel channel;
		private final FileLock lock;

		private Lease(final FileChannel channel, final FileLock lock) { this.channel = channel; this.lock = lock; }

		@Override
		public void close() throws IOException {
			try { lock.release(); } finally { channel.close(); }
		}
	}

	private SimulationPackageSessions() {}

	public static Path directory() { return Path.of(System.getProperty("user.home"), ".gama", "package-sessions"); }

	static void register(final Path content, final Path archive, final SimulationPackageManifest manifest) throws IOException {
		final var properties = new Properties();
		properties.setProperty("format", "1");
		properties.setProperty("id", manifest.id());
		properties.setProperty("version", manifest.version());
		properties.setProperty("created", Instant.now().toString());
		properties.setProperty("source", archive.toAbsolutePath().normalize().toString());
		write(content, properties);
	}

	static void recordProcess(final Path content, final ProcessHandle process) throws IOException {
		final Properties properties = read(content);
		properties.setProperty("pid", Long.toString(process.pid()));
		properties.setProperty("started", process.info().startInstant()
				.orElseThrow(() -> new IOException("Unable to identify the player process start time")).toString());
		write(content, properties);
	}

	public static Lease acquire(final Path content) throws IOException {
		final FileChannel channel = FileChannel.open(content.resolve(LOCK), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
		try {
			final FileLock lock;
			try { lock = channel.tryLock(); }
			catch (final OverlappingFileLockException e) { throw new IOException("This simulation session is in use: " + content, e); }
			if (lock == null) throw new IOException("This simulation session is in use: " + content);
			return new Lease(channel, lock);
		} catch (final IOException | RuntimeException e) {
			try { channel.close(); } catch (final IOException cleanup) { e.addSuppressed(cleanup); }
			throw e;
		}
	}

	public static List<Session> list(final Path directory) throws IOException {
		if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) return List.of();
		final var sessions = new ArrayList<Session>();
		try (var children = Files.list(directory.toRealPath())) {
			for (final Path child : children.sorted().toList()) {
				if (!child.getFileName().toString().startsWith("gama-package-") || Files.isSymbolicLink(child)
						|| !Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) continue;
				if (!Files.exists(child.resolve(METADATA), LinkOption.NOFOLLOW_LINKS)) continue;
				final Properties metadata = read(child);
				sessions.add(new Session(child, required(metadata, "id"), required(metadata, "version"),
						date(metadata, "created"), required(metadata, "source"), isRunning(metadata)));
			}
		}
		return sessions.stream().sorted(Comparator.comparing(Session::created).reversed()).toList();
	}

	/** Deliberately leaves active sessions and unrecognized/legacy directories untouched. */
	public static void delete(final Path directory, final Path session) throws IOException {
		final Path root = directory.toRealPath();
		final Path requested = session.toAbsolutePath().normalize();
		if (requested.getParent() == null) throw new IOException("A filesystem root is not a simulation session");
		final Path content = requested.getParent().toRealPath().resolve(requested.getFileName());
		if (!root.equals(content.getParent()) || Files.isSymbolicLink(content)
				|| !content.getFileName().toString().startsWith("gama-package-"))
			throw new IOException("Only a direct, application-owned package session can be removed");
		read(content);
		try (var lease = acquire(content)) {
			if (isRunning(read(content))) throw new IOException("Close the simulation player before removing its session");
			try (var paths = Files.walk(content)) {
				for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
					if (path.equals(content) || path.equals(content.resolve(LOCK))) continue;
					Files.delete(path);
				}
			}
		}
		Files.delete(content.resolve(LOCK));
		Files.delete(content);
	}

	private static boolean isRunning(final Properties metadata) throws IOException {
		final String value = metadata.getProperty("pid");
		if (value == null) return false;
		try {
			final long pid = Long.parseLong(value);
			if (pid <= 0) throw new NumberFormatException("Invalid process ID");
			final Instant started = date(metadata, "started");
			final var process = ProcessHandle.of(pid);
			if (process.isEmpty() || !process.get().isAlive()) return false;
			return process.get().info().startInstant().map(started::equals).orElse(true);
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid simulation session process identity", e);
		}
	}

	private static Properties read(final Path content) throws IOException {
		final var properties = new Properties();
		try (var input = Files.newInputStream(content.resolve(METADATA), LinkOption.NOFOLLOW_LINKS)) {
			final byte[] bytes = input.readNBytes(65537);
			if (bytes.length > 65536) throw new IOException("Simulation session metadata is too large: " + content);
			properties.load(new java.io.StringReader(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)));
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid simulation session metadata: " + content, e);
		}
		if (!"1".equals(properties.getProperty("format"))) throw new IOException("Unknown simulation session format: " + content);
		required(properties, "id");
		required(properties, "version");
		required(properties, "source");
		date(properties, "created");
		return properties;
	}

	private static String required(final Properties properties, final String key) throws IOException {
		final String value = properties.getProperty(key);
		if (value == null || value.isBlank()) throw new IOException("Missing simulation session metadata: " + key);
		return value;
	}

	private static Instant date(final Properties properties, final String key) throws IOException {
		try { return Instant.parse(required(properties, key)); }
		catch (final java.time.DateTimeException e) { throw new IOException("Invalid simulation session timestamp: " + key, e); }
	}

	private static void write(final Path content, final Properties metadata) throws IOException {
		final Path temporary = Files.createTempFile(content, ".session-", ".tmp");
		try {
			try (var output = Files.newBufferedWriter(temporary)) { metadata.store(output, "GAMA player session"); }
			Files.move(temporary, content.resolve(METADATA), java.nio.file.StandardCopyOption.ATOMIC_MOVE,
					java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} finally {
			Files.deleteIfExists(temporary);
		}
	}
}
