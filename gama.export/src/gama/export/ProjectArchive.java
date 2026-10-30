package gama.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

/** Project documents are data until the user explicitly imports projects or installs plugins. */
public final class ProjectArchive {

	public static final String EXTENSION = ".gamp";
	public static final String MANIFEST = "gama-project.properties";
	private static final int MAX_METADATA = 1024 * 1024;
	private static final SimulationPackage.Limits LIMITS = SimulationPackage.DEFAULT_LIMITS;

	public record Info(List<String> projects, String runtime, boolean linksResolved, List<String> links,
			List<SimulationPackageManifest.BundleRequirement> plugins, List<String> warnings, int files, boolean legacy) {
		public Info {
			projects = List.copyOf(projects);
			links = List.copyOf(links);
			plugins = List.copyOf(plugins);
			warnings = List.copyOf(warnings);
		}

		public String description() {
			return "Projects: " + String.join(", ", projects) + "\nFiles: " + files
					+ (runtime.isBlank() ? "" : "\nCreated with GAMA " + runtime)
					+ "\nLinked resources: " + (linksResolved ? "included or none" : "external targets not included")
					+ (links.isEmpty() ? "" : "\nLinks: " + String.join(", ", links))
					+ (plugins.isEmpty() ? "" : "\nRequired plugins:\n" + String.join("\n", plugins.stream()
							.map(p -> p.symbolicName() + " " + p.version()
									+ (p.artifact().isEmpty() ? " (not included)" : " (included)")).toList()))
					+ (warnings.isEmpty() ? "" : "\n\n" + String.join("\n", warnings))
					+ (legacy ? "\nLegacy ZIP: no GAMA dependency manifest." : "");
		}
	}

	public record Extracted(Path directory, Info info, Map<String, Path> projects) implements AutoCloseable {
		@Override public void close() throws IOException { delete(directory); }
	}

	private record Index(Info info, Map<String, Long> files, Map<String, String> projects, List<String> directories) {}

	private ProjectArchive() {}

	public static Info inspect(final Path archive) throws IOException {
		try (var zip = new ZipFile(archive.toFile())) { return index(zip, isProjectArchive(archive)).info(); }
	}

	public static boolean isProjectArchive(final Path path) {
		return path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(EXTENSION);
	}

	public static void write(final Path source, final Path destination, final Info info,
			final BooleanSupplier canceled) throws IOException {
		final Path root = source.toRealPath();
		final Path output = destination.toAbsolutePath().normalize();
		if (!isProjectArchive(output) || output.getParent() == null)
			throw new IOException("Choose a .gamp project archive destination");
		Files.createDirectories(output.getParent());
		if (output.getParent().toRealPath().resolve(output.getFileName()).startsWith(root))
			throw new IOException("The archive cannot be inside its source");
		final Path temporary = Files.createTempFile(output.getParent(), ".gamp-", ".tmp");
		try {
			final var files = new LinkedHashMap<String, Long>();
			final var names = new LinkedHashMap<String, String>();
			long total = 0;
			try (var zip = new ZipOutputStream(Files.newOutputStream(temporary)); var paths = Files.walk(root)) {
				for (final Path path : paths.sorted().toList()) {
					checkCanceled(canceled);
					if (Files.isSymbolicLink(path)) throw new IOException("Symbolic links cannot be archived: " + path);
					if (path.equals(root)) continue;
					final String name = root.relativize(path).toString().replace('\\', '/');
					if (Files.isDirectory(path)) {
						SimulationPackage.validatePath(name);
						zip.putNextEntry(new ZipEntry(name + "/"));
						zip.closeEntry();
						continue;
					}
					SimulationPackage.checkPortableName(name, names);
					if (!name.startsWith("workspace/") && !name.startsWith("plugins/"))
						throw new IOException("Unexpected project payload: " + name);
					if (files.size() >= LIMITS.files()) throw new IOException("Too many archive files");
					zip.putNextEntry(new ZipEntry(name));
					final long size;
					try (var input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
						size = copy(input, zip, Math.min(LIMITS.fileBytes(), LIMITS.totalBytes() - total), canceled);
					}
					zip.closeEntry();
					files.put(name, size);
					total += size;
				}
				final Properties metadata = properties(info);
				final var writer = new StringWriter();
				metadata.store(writer, "GAMA editable project archive");
				final byte[] bytes = writer.toString().getBytes(StandardCharsets.UTF_8);
				if (bytes.length > MAX_METADATA) throw new IOException("Project metadata is too large");
				zip.putNextEntry(new ZipEntry(MANIFEST));
				zip.write(bytes);
				zip.closeEntry();
			}
			try (var zip = new ZipFile(temporary.toFile())) { index(zip, true); }
			checkCanceled(canceled);
			Files.move(temporary, output, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} finally { Files.deleteIfExists(temporary); }
	}

	public static Extracted extract(final Path archive, final BooleanSupplier canceled) throws IOException {
		try (var zip = new ZipFile(archive.toFile())) {
			final Index index = index(zip, isProjectArchive(archive));
			final Path directory = Files.createTempDirectory("gama-project-import-");
			try {
				for (final String path : index.directories()) Files.createDirectories(directory.resolve(path));
				long total = 0;
				for (final var entry : index.files().entrySet()) {
					checkCanceled(canceled);
					final Path target = directory.resolve(entry.getKey());
					Files.createDirectories(target.getParent());
					final long actual;
					try (var input = zip.getInputStream(zip.getEntry(entry.getKey()));
							var output = Files.newOutputStream(target)) {
						actual = copy(input, output, Math.min(LIMITS.fileBytes(), LIMITS.totalBytes() - total), canceled);
					}
					total += actual;
				}
				final var projects = new LinkedHashMap<String, Path>();
				for (final var entry : index.projects().entrySet())
					projects.put(entry.getKey(), directory.resolve(entry.getValue()));
				return new Extracted(directory, index.info(), java.util.Collections.unmodifiableMap(projects));
			} catch (final IOException | RuntimeException e) {
				try { delete(directory); } catch (final IOException cleanup) { e.addSuppressed(cleanup); }
				throw e;
			}
		}
	}

	private static Index index(final ZipFile zip, final boolean required) throws IOException {
		final ZipEntry manifest = zip.getEntry(MANIFEST);
		if (required && manifest == null) throw new IOException("Missing GAMA project manifest");
		final Properties metadata = manifest == null ? null : read(zip.getInputStream(manifest), MAX_METADATA);
		if (metadata != null && (!"1".equals(metadata.getProperty("format.version"))
				|| !"project".equals(metadata.getProperty("archive.kind"))))
			throw new IOException("Unsupported GAMA project archive format");
		final var files = new LinkedHashMap<String, Long>();
		final var names = new LinkedHashMap<String, String>();
		final var seen = new java.util.HashSet<String>();
		final var projectPaths = new LinkedHashMap<String, String>();
		final var links = new ArrayList<String>();
		final var directories = new ArrayList<String>();
		final var entries = zip.entries();
		long total = 0;
		int count = 0;
		while (entries.hasMoreElements()) {
			final var entry = entries.nextElement();
			if (++count > LIMITS.files() * 2L + 1) throw new IOException("Too many ZIP entries");
			String name = entry.getName();
			if (entry.isDirectory()) name = name.substring(0, name.length() - 1);
			if (!seen.add(name)) throw new IOException("Repeated archive path: " + name);
			// Directory entries may follow their children; only files occupy a leaf.
			if (entry.isDirectory()) {
				try { SimulationPackage.validatePath(name); }
				catch (final IllegalArgumentException e) { throw new IOException("Invalid ZIP directory", e); }
				directories.add(name);
				continue;
			}
			SimulationPackage.checkPortableName(name, names);
			if (MANIFEST.equals(name)) continue;
			if (files.size() >= LIMITS.files() || entry.getSize() < 0 || entry.getSize() > LIMITS.fileBytes()
					|| entry.getSize() > LIMITS.totalBytes() - total)
				throw new IOException("Archive exceeds file or size limits: " + name);
			total += entry.getSize();
			files.put(name, entry.getSize());
			if (name.equals(".project") || name.endsWith("/.project")) {
				final var description = description(zip.getInputStream(entry));
				final String project = projectName(description);
				final String parent = name.equals(".project") ? "" : name.substring(0, name.length() - 9);
				if (projectPaths.putIfAbsent(project, parent) != null)
					throw new IOException("Duplicate project name in archive: " + project);
				final var definitions = description.getElementsByTagName("link");
				for (int i = 0; i < definitions.getLength(); i++) {
					final var node = (org.w3c.dom.Element) definitions.item(i);
					final var linkNames = node.getElementsByTagName("name");
					if (linkNames.getLength() != 1) throw new IOException("Invalid linked resource definition");
					final String link = linkNames.item(0).getTextContent();
					try { SimulationPackage.validatePath(link); }
					catch (final IllegalArgumentException e) { throw new IOException("Invalid linked resource path", e); }
					links.add(project + "/" + link);
				}
			}
		}
		validateTree(files.keySet(), directories);
		if (projectPaths.isEmpty()) throw new IOException("No .project description was found in the archive");
		final var plugins = new ArrayList<SimulationPackageManifest.BundleRequirement>();
		final var warnings = new ArrayList<String>();
		String runtime = "";
		boolean resolved = links.isEmpty();
		if (metadata != null) {
			runtime = required(metadata, "gama.version");
			final String linkStatus = required(metadata, "links.resolved");
			if (!"true".equals(linkStatus) && !"false".equals(linkStatus))
				throw new IOException("Invalid linked-resource status");
			resolved = Boolean.parseBoolean(linkStatus);
			final int projects = count(metadata, "projects.count");
			if (projects != projectPaths.size()) throw new IOException("Project inventory mismatch");
			final var declaredProjects = new java.util.HashSet<String>();
			for (int i = 0; i < projects; i++) {
				final String name = required(metadata, "project." + i + ".name");
				if (!declaredProjects.add(name) || !("workspace/" + name).equals(projectPaths.get(name)))
					throw new IOException("Project name/location mismatch: " + name);
			}
			if (resolved && !links.isEmpty()) throw new IOException("Resolved archive still declares external links");
			if (resolved) {
				for (int i = 0, n = count(metadata, "links.count"); i < n; i++)
					links.add(required(metadata, "link." + i));
			}
			for (int i = 0, n = count(metadata, "plugins.count"); i < n; i++) {
				try {
					final var plugin = new SimulationPackageManifest.BundleRequirement(
							required(metadata, "plugin." + i + ".id"), required(metadata, "plugin." + i + ".version"),
							required(metadata, "plugin." + i + ".artifact"));
					if (plugins.stream().anyMatch(p -> p.symbolicName().equals(plugin.symbolicName())))
						throw new IOException("Duplicate required plugin: " + plugin.symbolicName());
					if (!plugin.artifact().isEmpty() && !files.containsKey(plugin.artifact()))
						throw new IOException("Missing plugin payload: " + plugin.artifact());
					plugins.add(plugin);
				} catch (final IllegalArgumentException e) { throw new IOException("Invalid plugin declaration", e); }
			}
			for (int i = 0, n = count(metadata, "warnings.count"); i < n; i++)
				warnings.add(required(metadata, "warning." + i));
			for (final String name : files.keySet()) {
				if (name.startsWith("plugins/")) {
					if (plugins.stream().noneMatch(p -> p.artifact().equals(name)))
						throw new IOException("Undeclared plugin: " + name);
				} else if (projectPaths.values().stream().noneMatch(p -> name.startsWith(p + "/")))
					throw new IOException("Unexpected project archive file: " + name);
			}
		}
		if (!links.isEmpty() && !resolved)
			warnings.add("Linked resources refer to external locations; their targets are not included.");
		final var projectDirectories = new ArrayList<>(projectPaths.values());
		for (int i = 0; i < projectDirectories.size(); i++)
			for (int j = i + 1; j < projectDirectories.size(); j++)
				if (projectDirectories.get(i).isEmpty() || projectDirectories.get(j).isEmpty()
						|| projectDirectories.get(i).startsWith(projectDirectories.get(j) + "/")
						|| projectDirectories.get(j).startsWith(projectDirectories.get(i) + "/"))
					throw new IOException("Nested projects are not supported in a project archive");
		return new Index(new Info(new ArrayList<>(projectPaths.keySet()), runtime, resolved, links, plugins, warnings,
				files.size(), metadata == null), files, projectPaths, directories);
	}

	private static void validateTree(final java.util.Set<String> files, final List<String> directories) throws IOException {
		final var spelling = new java.util.HashMap<String, String>();
		final var kinds = new java.util.HashMap<String, Boolean>();
		final var paths = new ArrayList<>(directories);
		paths.addAll(files);
		for (final String path : paths) {
			final String[] components = path.split("/");
			String prefix = "";
			for (int i = 0; i < components.length; i++) {
				prefix += (i == 0 ? "" : "/") + components[i];
				final String key = java.text.Normalizer.normalize(prefix, java.text.Normalizer.Form.NFC)
						.toLowerCase(java.util.Locale.ROOT);
				final String previous = spelling.putIfAbsent(key, prefix);
				final boolean directory = i < components.length - 1 || !files.contains(path);
				final Boolean kind = kinds.putIfAbsent(key, directory);
				if (previous != null && !previous.equals(prefix) || kind != null && kind != directory)
					throw new IOException("Conflicting ZIP paths: " + path);
			}
		}
	}

	public static org.w3c.dom.Document description(final InputStream input) throws IOException {
		try (input) {
			final var factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			final byte[] bytes = input.readNBytes(MAX_METADATA + 1);
			if (bytes.length > MAX_METADATA) throw new IOException("Project description is too large");
			final var document = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(bytes));
			if (!"projectDescription".equals(document.getDocumentElement().getNodeName()))
				throw new IOException("Invalid project description");
			return document;
		} catch (final javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException e) {
			throw new IOException("Invalid project description", e);
		}
	}

	public static String projectName(final org.w3c.dom.Document document) throws IOException {
		final var names = document.getDocumentElement().getChildNodes();
		String result = null;
		for (int i = 0; i < names.getLength(); i++)
			if ("name".equals(names.item(i).getNodeName())) {
				if (result != null) throw new IOException("Repeated project name");
				result = names.item(i).getTextContent();
			}
		if (result == null || result.isBlank() || result.contains("/") || result.contains("\\"))
			throw new IOException("Invalid project name");
		try { SimulationPackage.validatePath(result); }
		catch (final IllegalArgumentException e) { throw new IOException("Invalid project name", e); }
		return result;
	}

	private static Properties properties(final Info info) {
		final var p = new Properties();
		p.setProperty("format.version", "1");
		p.setProperty("archive.kind", "project");
		p.setProperty("gama.version", info.runtime());
		p.setProperty("links.resolved", Boolean.toString(info.linksResolved()));
		p.setProperty("links.count", Integer.toString(info.links().size()));
		for (int i = 0; i < info.links().size(); i++) p.setProperty("link." + i, info.links().get(i));
		p.setProperty("projects.count", Integer.toString(info.projects().size()));
		for (int i = 0; i < info.projects().size(); i++) p.setProperty("project." + i + ".name", info.projects().get(i));
		p.setProperty("plugins.count", Integer.toString(info.plugins().size()));
		for (int i = 0; i < info.plugins().size(); i++) {
			final var b = info.plugins().get(i);
			p.setProperty("plugin." + i + ".id", b.symbolicName());
			p.setProperty("plugin." + i + ".version", b.version());
			p.setProperty("plugin." + i + ".artifact", b.artifact());
		}
		p.setProperty("warnings.count", Integer.toString(info.warnings().size()));
		for (int i = 0; i < info.warnings().size(); i++) p.setProperty("warning." + i, info.warnings().get(i));
		return p;
	}

	private static Properties read(final InputStream input, final int limit) throws IOException {
		try (input) {
			final byte[] bytes = input.readNBytes(limit + 1);
			if (bytes.length > limit) throw new IOException("Project metadata exceeds its size limit");
			final var result = new Properties();
			result.load(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
			return result;
		} catch (final IllegalArgumentException e) { throw new IOException("Invalid project metadata", e); }
	}

	private static String required(final Properties p, final String key) throws IOException {
		final String value = p.getProperty(key);
		if (value == null) throw new IOException("Missing project metadata: " + key);
		return value;
	}

	private static int count(final Properties p, final String key) throws IOException {
		try {
			final int value = Integer.parseInt(required(p, key));
			if (value < 0 || value > LIMITS.files()) throw new NumberFormatException();
			return value;
		} catch (final NumberFormatException e) { throw new IOException("Invalid project metadata count: " + key, e); }
	}

	private static long copy(final InputStream input, final java.io.OutputStream output, final long limit,
			final BooleanSupplier canceled) throws IOException {
		long size = 0;
		final byte[] buffer = new byte[8192];
		int n;
		while ((n = input.read(buffer)) != -1) {
			checkCanceled(canceled);
			if (n > limit - size) throw new IOException("Project archive exceeds size limits");
			output.write(buffer, 0, n);
			size += n;
		}
		return size;
	}

	public static void delete(final Path ownedDirectory) throws IOException {
		try (var paths = Files.walk(ownedDirectory)) {
			for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
		}
	}

	private static void checkCanceled(final BooleanSupplier canceled) throws IOException {
		if (canceled.getAsBoolean()) throw new java.io.InterruptedIOException("Project archive operation canceled");
	}
}
