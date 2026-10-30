package gama.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** A data-only archive codec. It never installs or executes the plugins it carries. */
public final class SimulationPackage {

	public static final String EXTENSION = ".gamx";
	private static final int MAX_MANIFEST_BYTES = 1024 * 1024;
	public static final Limits DEFAULT_LIMITS = new Limits(10_000, 2L * 1024 * 1024 * 1024, 8L * 1024 * 1024 * 1024);

	public record Limits(int files, long fileBytes, long totalBytes) {
		public Limits {
			if (files < 1 || fileBytes < 1 || totalBytes < 1)
				throw new IllegalArgumentException("Playable model limits must be positive");
		}
	}

	public record Extracted(SimulationPackageManifest manifest, Path content) {
		public Path workspace() { return content.resolve("workspace"); }

		public static Extracted open(final Path content) throws IOException {
			final Path root = content.toRealPath();
			final Path file = root.resolve(SimulationPackageManifest.FILE);
			final Properties properties = new Properties();
			try (var input = Files.newInputStream(file)) {
				final byte[] bytes = input.readNBytes(MAX_MANIFEST_BYTES + 1);
				if (bytes.length > MAX_MANIFEST_BYTES) throw new IOException("Playable model manifest is too large");
				properties.load(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
			} catch (final IllegalArgumentException e) {
				throw new IOException("Invalid extracted playable model manifest", e);
			}
			return new Extracted(SimulationPackageManifest.read(properties), root);
		}
	}

	private record Index(SimulationPackageManifest manifest, Map<String, Long> files) {}

	private SimulationPackage() {}

	/**
	 * Writes staged projects, optional plugin JARs, and launcher assets. Existing output is replaced only after success.
	 * Eclipse workspace metadata, symbolic links, and files outside the package layout are not accepted.
	 */
	public static void write(final Path source, final Path destination, final SimulationPackageManifest manifest)
			throws IOException {
		write(source, destination, manifest, () -> false);
	}

	public static void write(final Path source, final Path destination, final SimulationPackageManifest manifest,
			final java.util.function.BooleanSupplier canceled) throws IOException {
		final Path root = source.toRealPath();
		final Path output = destination.toAbsolutePath().normalize();
		if (output.getParent() == null) throw new IOException("A playable model destination directory is required");
		Files.createDirectories(output.getParent());
		if (output.getParent().toRealPath().resolve(output.getFileName()).startsWith(root))
			throw new IOException("The playable model destination cannot be inside its source");
		final Path staging = Files.createTempFile(output.getParent(), ".gama-package-", ".tmp");
		try {
			final var files = new LinkedHashMap<String, Long>();
			final var names = new LinkedHashMap<String, String>();
			long total = 0;
			try (var zip = new ZipOutputStream(Files.newOutputStream(staging)); var paths = Files.walk(root)) {
				final var iterator = paths.iterator();
				while (iterator.hasNext()) {
					checkCanceled(canceled);
					final Path file = iterator.next();
					if (Files.isSymbolicLink(file)) throw new IOException("Playable model sources cannot contain links: " + file);
					if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) continue;
					if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
						throw new IOException("Not a regular playable model file: " + file);
					final String name = root.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
					checkName(name, names);
					if (files.size() >= DEFAULT_LIMITS.files()) throw new IOException("Too many playable model files");
					zip.putNextEntry(new ZipEntry(name));
					final long size;
					try (var input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
						size = transfer(input, zip, Math.min(DEFAULT_LIMITS.fileBytes(), DEFAULT_LIMITS.totalBytes() - total), canceled);
					}
					zip.closeEntry();
					files.put(name, size);
					total += size;
				}
				validateContents(manifest, files);
				final var properties = manifest.properties();
				final var writer = new StringWriter();
				properties.store(writer, "GAMA playable model");
				final byte[] metadata = writer.toString().getBytes(StandardCharsets.UTF_8);
				if (metadata.length > MAX_MANIFEST_BYTES) throw new IOException("Playable model manifest is too large");
				zip.putNextEntry(new ZipEntry(SimulationPackageManifest.FILE));
				zip.write(metadata);
				zip.closeEntry();
			}
			checkCanceled(canceled);
			Files.move(staging, output, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} finally {
			Files.deleteIfExists(staging);
		}
	}

	/** Inspects structure and ZIP size limits, without extracting content or loading any GAMA classes. */
	public static SimulationPackageManifest inspect(final Path archive) throws IOException {
		try (var zip = new ZipFile(archive.toFile())) {
			return index(zip, DEFAULT_LIMITS).manifest();
		}
	}

	/**
	 * Creates a fresh, private content directory under an application-owned session directory. Every payload file is
	 * subject to extraction limits; no files from an existing session are reused.
	 */
	public static Extracted extract(final Path archive, final Path sessions, final String runtimeVersion,
			final Limits limits) throws IOException {
		return extract(archive, sessions, runtimeVersion, limits, false);
	}

	static Extracted extract(final Path archive, final Path sessions, final String runtimeVersion,
			final Limits limits, final boolean ignoreVersionRequirements) throws IOException {
		try (var zip = new ZipFile(archive.toFile())) {
			final Index index = index(zip, limits);
			if (!ignoreVersionRequirements) index.manifest().requireRuntime(runtimeVersion);
			Files.createDirectories(sessions);
			final Path content = Files.createTempDirectory(sessions.toRealPath(), "gama-package-");
			try {
				long total = 0;
				for (final var entry : index.files().entrySet()) {
					final Path target = content.resolve(entry.getKey());
					Files.createDirectories(target.getParent());
					final long actual;
					try (var input = zip.getInputStream(zip.getEntry(entry.getKey()));
							var output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW,
									StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
						actual = transfer(input, output, Math.min(limits.fileBytes(), limits.totalBytes() - total));
					}
					total += actual;
				}
				try (var input = zip.getInputStream(zip.getEntry(SimulationPackageManifest.FILE))) {
					Files.copy(input, content.resolve(SimulationPackageManifest.FILE));
				}
				return new Extracted(index.manifest(), content);
			} catch (final IOException | RuntimeException e) {
				try {
					deleteContent(content);
				} catch (final IOException cleanup) {
					e.addSuppressed(cleanup);
				}
				throw e;
			}
		}
	}

	public static Extracted extract(final Path archive, final Path sessions, final String runtimeVersion)
			throws IOException {
		return extract(archive, sessions, runtimeVersion, DEFAULT_LIMITS);
	}

	private static Index index(final ZipFile zip, final Limits limits) throws IOException {
		final var metadata = zip.getEntry(SimulationPackageManifest.FILE);
		if (metadata == null || metadata.isDirectory()) throw new IOException("Missing GAMA playable model manifest");
		final Properties properties = new Properties();
		try (var input = zip.getInputStream(metadata)) {
			final byte[] bytes = input.readNBytes(MAX_MANIFEST_BYTES + 1);
			if (bytes.length > MAX_MANIFEST_BYTES) throw new IOException("Playable model manifest is too large");
			properties.load(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid GAMA playable model properties", e);
		}
		final var manifest = SimulationPackageManifest.read(properties);
		final var files = new LinkedHashMap<String, Long>();
		final var names = new LinkedHashMap<String, String>();
		final var entries = zip.entries();
		final var seen = new HashSet<String>();
		int count = 0;
		long total = 0;
		while (entries.hasMoreElements()) {
			final var entry = entries.nextElement();
			final String name = entry.isDirectory()
					? entry.getName().substring(0, entry.getName().length() - 1) : entry.getName();
			if (++count > limits.files() * 2L + 1 || !seen.add(name))
				throw new IOException("Too many or duplicate ZIP entries: " + name);
			if (entry.isDirectory()) {
				try {
					validatePath(name);
				} catch (final IllegalArgumentException e) {
					throw new IOException("Invalid ZIP directory: " + name, e);
				}
				continue;
			}
			if (SimulationPackageManifest.FILE.equals(name)) continue;
			checkName(name, names);
			final long size = entry.getSize();
			if (files.size() >= limits.files() || size < 0 || size > limits.fileBytes()
					|| size > limits.totalBytes() - total)
				throw new IOException("Playable model exceeds size limits: " + name);
			files.put(name, size);
			total += size;
		}
		validateContents(manifest, files);
		return new Index(manifest, files);
	}

	private static void validateContents(final SimulationPackageManifest manifest, final Map<String, Long> files)
			throws IOException {
		if (!files.containsKey(LauncherConfiguration.FILE)) throw new IOException("Missing launcher configuration");
		for (final var experiment : manifest.experiments()) {
			if (!files.containsKey("workspace/" + experiment.model())
					|| !files.containsKey("workspace/" + experiment.model().split("/", 2)[0] + "/.project"))
				throw new IOException("Missing experiment model or project: " + experiment.model());
		}
		final var artifacts = new HashSet<String>();
		for (final var bundle : manifest.bundles()) {
			if (bundle.artifact().isEmpty()) continue;
			artifacts.add(bundle.artifact());
			if (!files.containsKey(bundle.artifact())) throw new IOException("Missing plugin: " + bundle.artifact());
		}
		for (final String name : files.keySet()) {
			if (name.startsWith("plugins/") && !artifacts.contains(name))
				throw new IOException("Undeclared plugin: " + name);
		}
	}

	static void checkName(final String name, final Map<String, String> names) throws IOException {
		checkPortableName(name, names);
		if (!name.startsWith("workspace/") && !name.startsWith("plugins/")
				&& !LauncherConfiguration.FILE.equals(name) && !LauncherConfiguration.IMAGE.equals(name))
			throw new IOException("Unsupported playable model content: " + name);
	}

	static void checkPortableName(final String name, final Map<String, String> names) throws IOException {
		try {
			validatePath(name);
		} catch (final IllegalArgumentException e) {
			throw new IOException(e.getMessage(), e);
		}
		int end = 0;
		do {
			end = name.indexOf('/', end + 1);
			final String prefix = end < 0 ? name : name.substring(0, end);
			final String canonical = Normalizer.normalize(prefix, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
			final String previous = names.putIfAbsent(canonical, prefix);
			if (previous != null && (!previous.equals(prefix) || end < 0))
				throw new IOException("Conflicting playable model paths: " + name);
		} while (end >= 0);
	}

	static void validatePath(final String name) {
		if (name == null || name.isEmpty() || name.startsWith("/") || name.contains("\\"))
			throw new IllegalArgumentException("Invalid playable model path: " + name);
		for (final String component : name.split("/", -1)) {
			if (component.isEmpty() || component.equals(".") || component.equals("..") || component.equalsIgnoreCase(".metadata")
					|| component.endsWith(".") || component.endsWith(" ")
					|| component.chars().anyMatch(c -> c < 32 || "<>:\"|?*".indexOf(c) >= 0)
					|| component.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?"))
				throw new IllegalArgumentException("Non-portable playable model path: " + name);
		}
	}

	private static long transfer(final InputStream input, final OutputStream output, final long limit)
			throws IOException {
		return transfer(input, output, limit, () -> false);
	}

	private static long transfer(final InputStream input, final OutputStream output, final long limit,
			final java.util.function.BooleanSupplier canceled) throws IOException {
		long size = 0;
		final byte[] buffer = new byte[8192];
		int read;
		while ((read = input.read(buffer)) != -1) {
			checkCanceled(canceled);
			if (read > limit - size) throw new IOException("Playable model content exceeds its size limit");
			output.write(buffer, 0, read);
			size += read;
		}
		return size;
	}

	private static void checkCanceled(final java.util.function.BooleanSupplier canceled) throws IOException {
		if (canceled.getAsBoolean()) throw new java.io.InterruptedIOException("Model export canceled");
	}

	private static void deleteContent(final Path content) throws IOException {
		try (var paths = Files.walk(content)) {
			for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) { Files.delete(path); }
		}
	}
}
