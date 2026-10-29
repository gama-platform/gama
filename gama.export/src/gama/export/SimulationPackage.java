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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** A data-only archive codec. It never installs or executes the plugins it carries. */
public final class SimulationPackage {

	public static final String EXTENSION = ".gamp";
	private static final int MAX_MANIFEST_BYTES = 1024 * 1024;
	public static final Limits DEFAULT_LIMITS = new Limits(10_000, 2L * 1024 * 1024 * 1024, 8L * 1024 * 1024 * 1024);

	public record Limits(int files, long fileBytes, long totalBytes) {
		public Limits {
			if (files < 1 || fileBytes < 1 || totalBytes < 1)
				throw new IllegalArgumentException("Package limits must be positive");
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
				if (bytes.length > MAX_MANIFEST_BYTES) throw new IOException("Package manifest is too large");
				properties.load(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
			} catch (final IllegalArgumentException e) {
				throw new IOException("Invalid extracted package manifest", e);
			}
			return new Extracted(SimulationPackageManifest.read(properties), root);
		}
	}

	private record Fingerprint(long size, String sha256) {}
	private record Index(SimulationPackageManifest manifest, Map<String, Fingerprint> files) {}

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
		if (output.getParent() == null) throw new IOException("A package destination directory is required");
		Files.createDirectories(output.getParent());
		if (output.getParent().toRealPath().resolve(output.getFileName()).startsWith(root))
			throw new IOException("The package destination cannot be inside its source");
		final Path staging = Files.createTempFile(output.getParent(), ".gama-package-", ".tmp");
		try {
			final var files = new LinkedHashMap<String, Fingerprint>();
			final var names = new LinkedHashMap<String, String>();
			long total = 0;
			try (var zip = new ZipOutputStream(Files.newOutputStream(staging)); var paths = Files.walk(root)) {
				final var iterator = paths.iterator();
				while (iterator.hasNext()) {
					checkCanceled(canceled);
					final Path file = iterator.next();
					if (Files.isSymbolicLink(file)) throw new IOException("Package sources cannot contain links: " + file);
					if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) continue;
					if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
						throw new IOException("Not a regular package file: " + file);
					final String name = root.relativize(file).toString().replace(file.getFileSystem().getSeparator(), "/");
					checkName(name, names);
					if (files.size() >= DEFAULT_LIMITS.files()) throw new IOException("Too many package files");
					zip.putNextEntry(new ZipEntry(name));
					final Fingerprint fingerprint;
					try (var input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
						fingerprint = transfer(input, zip, Math.min(DEFAULT_LIMITS.fileBytes(), DEFAULT_LIMITS.totalBytes() - total), canceled);
					}
					zip.closeEntry();
					files.put(name, fingerprint);
					total += fingerprint.size();
				}
				validateContents(manifest, files);
				final var properties = manifest.properties();
				properties.setProperty("files.count", Integer.toString(files.size()));
				int i = 0;
				for (final var entry : files.entrySet()) {
					properties.setProperty("file." + i + ".path", entry.getKey());
					properties.setProperty("file." + i + ".size", Long.toString(entry.getValue().size()));
					properties.setProperty("file." + i++ + ".sha256", entry.getValue().sha256());
				}
				final var writer = new StringWriter();
				properties.store(writer, "GAMA simulation package");
				final byte[] metadata = writer.toString().getBytes(StandardCharsets.UTF_8);
				if (metadata.length > MAX_MANIFEST_BYTES) throw new IOException("Package manifest is too large");
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

	/** Inspects structure and declared sizes, without extracting content or loading any GAMA classes. */
	public static SimulationPackageManifest inspect(final Path archive) throws IOException {
		try (var zip = new ZipFile(archive.toFile())) {
			return index(zip, DEFAULT_LIMITS).manifest();
		}
	}

	/**
	 * Creates a fresh, private content directory under an application-owned session directory. Every payload file is
	 * checked against its declared size and SHA-256; no files from an existing session are reused.
	 */
	public static Extracted extract(final Path archive, final Path sessions, final String runtimeVersion,
			final Limits limits) throws IOException {
		try (var zip = new ZipFile(archive.toFile())) {
			final Index index = index(zip, limits);
			index.manifest().requireRuntime(runtimeVersion);
			Files.createDirectories(sessions);
			final Path content = Files.createTempDirectory(sessions.toRealPath(), "gama-package-");
			try {
				long total = 0;
				for (final var entry : index.files().entrySet()) {
					final Path target = content.resolve(entry.getKey());
					Files.createDirectories(target.getParent());
					final Fingerprint actual;
					try (var input = zip.getInputStream(zip.getEntry(entry.getKey()));
							var output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW,
									StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
						actual = transfer(input, output, Math.min(entry.getValue().size(), limits.totalBytes() - total));
					}
					if (!entry.getValue().equals(actual))
						throw new IOException("Package checksum or size mismatch: " + entry.getKey());
					total += actual.size();
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
		if (metadata == null || metadata.isDirectory()) throw new IOException("Missing GAMA package manifest");
		final Properties properties = new Properties();
		try (var input = zip.getInputStream(metadata)) {
			final byte[] bytes = input.readNBytes(MAX_MANIFEST_BYTES + 1);
			if (bytes.length > MAX_MANIFEST_BYTES) throw new IOException("Package manifest is too large");
			properties.load(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid GAMA package properties", e);
		}
		final var manifest = SimulationPackageManifest.read(properties);
		final var files = new LinkedHashMap<String, Fingerprint>();
		final var names = new LinkedHashMap<String, String>();
		try {
			final int count = Integer.parseInt(SimulationPackageManifest.required(properties, "files.count"));
			if (count < 1 || count > limits.files()) throw new IOException("Invalid package file count");
			long total = 0;
			for (int i = 0; i < count; i++) {
				final String name = SimulationPackageManifest.required(properties, "file." + i + ".path");
				checkName(name, names);
				final long size = Long.parseLong(SimulationPackageManifest.required(properties, "file." + i + ".size"));
				final String hash = SimulationPackageManifest.required(properties, "file." + i + ".sha256");
				if (size < 0 || size > limits.fileBytes() || size > limits.totalBytes() - total
						|| !hash.matches("[0-9a-f]{64}")) throw new IOException("Invalid package fingerprint: " + name);
				files.put(name, new Fingerprint(size, hash));
				total += size;
			}
		} catch (final IllegalArgumentException e) {
			throw new IOException("Invalid package file inventory", e);
		}
		final var entries = zip.entries();
		final var seen = new HashSet<String>();
		int count = 0;
		while (entries.hasMoreElements()) {
			final var entry = entries.nextElement();
			final String name = entry.getName();
			if (++count > limits.files() + 1L || !seen.add(name))
				throw new IOException("Too many or duplicate ZIP entries: " + name);
			if (SimulationPackageManifest.FILE.equals(name)) continue;
			final var expected = files.get(name);
			if (entry.isDirectory() || expected == null || entry.getSize() != expected.size())
				throw new IOException("Undeclared or inconsistent ZIP entry: " + name);
		}
		if (seen.size() != files.size() + 1) throw new IOException("Package payload is incomplete");
		validateContents(manifest, files);
		return new Index(manifest, files);
	}

	private static void validateContents(final SimulationPackageManifest manifest, final Map<String, Fingerprint> files)
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
		try {
			validatePath(name);
		} catch (final IllegalArgumentException e) {
			throw new IOException(e.getMessage(), e);
		}
		if (!name.startsWith("workspace/") && !name.startsWith("plugins/")
				&& !LauncherConfiguration.FILE.equals(name) && !LauncherConfiguration.IMAGE.equals(name))
			throw new IOException("Unsupported package content: " + name);
		int end = 0;
		do {
			end = name.indexOf('/', end + 1);
			final String prefix = end < 0 ? name : name.substring(0, end);
			final String canonical = Normalizer.normalize(prefix, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
			final String previous = names.putIfAbsent(canonical, prefix);
			if (previous != null && (!previous.equals(prefix) || end < 0))
				throw new IOException("Conflicting package paths: " + name);
		} while (end >= 0);
	}

	static void validatePath(final String name) {
		if (name == null || name.isEmpty() || name.startsWith("/") || name.contains("\\"))
			throw new IllegalArgumentException("Invalid package path: " + name);
		for (final String component : name.split("/", -1)) {
			if (component.isEmpty() || component.equals(".") || component.equals("..") || component.equalsIgnoreCase(".metadata")
					|| component.endsWith(".") || component.endsWith(" ")
					|| component.chars().anyMatch(c -> c < 32 || "<>:\"|?*".indexOf(c) >= 0)
					|| component.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?"))
				throw new IllegalArgumentException("Non-portable package path: " + name);
		}
	}

	private static Fingerprint transfer(final InputStream input, final OutputStream output, final long limit)
			throws IOException {
		return transfer(input, output, limit, () -> false);
	}

	private static Fingerprint transfer(final InputStream input, final OutputStream output, final long limit,
			final java.util.function.BooleanSupplier canceled) throws IOException {
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is required by the Java runtime", e);
		}
		long size = 0;
		final byte[] buffer = new byte[8192];
		int read;
		while ((read = input.read(buffer)) != -1) {
			checkCanceled(canceled);
			if (read > limit - size) throw new IOException("Package content exceeds its size limit");
			output.write(buffer, 0, read);
			digest.update(buffer, 0, read);
			size += read;
		}
		return new Fingerprint(size, HexFormat.of().formatHex(digest.digest()));
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
