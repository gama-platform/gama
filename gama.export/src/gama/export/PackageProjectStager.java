package gama.export;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

/** Copies author resources (including materialized Eclipse links) into a portable, metadata-free package workspace. */
public final class PackageProjectStager {

	public record Source(Path file, String path, Charset charset) {
		public Source {
			SimulationPackage.validatePath("workspace/" + path);
			file = file.toAbsolutePath().normalize();
		}
	}

	private static final Set<String> SHAPE_COMPANIONS = Set.of(".shx", ".dbf", ".prj", ".sbn", ".sbx", ".cpg",
			".qix", ".fix", ".shp.xml");

	private PackageProjectStager() {}

	/** The target must be an empty staging directory, never an author's project or workspace. */
	public static void stage(final Path target, final List<Source> sources, final Map<String, Set<Path>> data,
			final BooleanSupplier canceled) throws IOException {
		Files.createDirectories(target);
		try (var children = Files.list(target)) {
			if (children.findAny().isPresent()) throw new IOException("Package staging requires an empty directory");
		}
		final var targets = new LinkedHashMap<Path, Source>();
		final var relocated = new LinkedHashMap<Path, Path>();
		for (final Source source : sources) {
			checkCanceled(canceled);
			final Path destination = Path.of("workspace").resolve(source.path());
			if (Files.isSymbolicLink(source.file()) || !Files.isRegularFile(source.file(), LinkOption.NOFOLLOW_LINKS))
				throw new IOException("Missing or symbolic project resource: " + source.file());
			if (targets.putIfAbsent(destination, source) != null)
				throw new IOException("Conflicting project resource: " + source.path());
			relocated.putIfAbsent(source.file(), destination);
		}
		int index = 0;
		for (final var project : data.entrySet()) {
			SimulationPackage.validatePath("workspace/" + project.getKey());
			for (final Path input : project.getValue().stream().sorted().toList()) {
				checkCanceled(canceled);
				final Path source = input.toAbsolutePath().normalize();
				if (relocated.containsKey(source)) continue;
				if (Files.isSymbolicLink(source) || !Files.exists(source, LinkOption.NOFOLLOW_LINKS))
					throw new IOException("Missing or symbolic model data: " + source);
				final Path parent = Path.of("workspace", project.getKey(), "_package_data", Integer.toString(index++));
				if (sources.stream().anyMatch(file -> Path.of("workspace").resolve(file.path()).startsWith(parent.getParent())))
					throw new IOException("The project already uses the reserved _package_data directory: " + project.getKey());
				final Path destination = parent.resolve(source.getFileName());
				if (Files.isDirectory(source, LinkOption.NOFOLLOW_LINKS)) {
					relocated.put(source, destination);
					try (var paths = Files.walk(source)) {
						for (final Path child : paths.sorted().toList()) {
							checkCanceled(canceled);
							if (Files.isSymbolicLink(child)) throw new IOException("Model data contains a symbolic link: " + child);
							if (Files.isDirectory(child)) {
								try (var children = Files.list(child)) {
									if (children.findAny().isEmpty())
										throw new IOException("Empty model data directories cannot be packaged: " + child);
								}
							}
							if (!Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS))
								addData(child, destination.resolve(source.relativize(child)), targets, relocated);
						}
					}
				} else {
					addData(source, destination, targets, relocated);
					final String filename = source.getFileName().toString();
					if (filename.toLowerCase(Locale.ROOT).endsWith(".shp")) {
						final String stem = filename.substring(0, filename.length() - 4);
						try (var companions = Files.list(source.getParent())) {
							for (final Path companion : companions.sorted().toList()) {
								final String name = companion.getFileName().toString();
								if (name.length() > stem.length() && name.regionMatches(true, 0, stem, 0, stem.length())
										&& SHAPE_COMPANIONS.contains(name.substring(stem.length()).toLowerCase(Locale.ROOT))) {
									addData(companion, parent.resolve(name), targets, relocated);
								}
							}
						}
					}
				}
			}
		}
		final var portable = new LinkedHashMap<String, String>();
		final var preferencePaths = new java.util.HashSet<Path>();
		for (final Path path : targets.keySet()) {
			SimulationPackage.checkName(path.toString().replace('\\', '/'), portable);
			if (isModel(path)) preferencePaths.add(path.subpath(0, 2).resolve(".settings/org.eclipse.core.resources.prefs"));
		}
		for (final Path path : preferencePaths) {
			if (!targets.containsKey(path)) SimulationPackage.checkName(path.toString().replace('\\', '/'), portable);
		}
		for (final var entry : targets.entrySet()) {
			checkCanceled(canceled);
			final Path path = entry.getKey();
			final Source source = entry.getValue();
			final Path output = target.resolve(path);
			Files.createDirectories(output.getParent());
			if (path.getNameCount() == 3 && path.getFileName().toString().equals(".project")) {
				Files.writeString(output, projectDescription(Files.readString(source.file()), path.getName(1).toString()));
			} else if (isModel(path)) {
				final String text = Files.readString(source.file(), source.charset());
				Files.writeString(output, GamlExportPaths.rewrite(text, source.file(), path, relocated));
			} else {
				copy(source.file(), output, canceled);
			}
		}
		final var encodings = new LinkedHashMap<Path, java.util.Properties>();
		for (final Path path : targets.keySet()) {
			if (!isModel(path)) continue;
			checkCanceled(canceled);
			final Path preferences = target.resolve(path.subpath(0, 2)).resolve(".settings/org.eclipse.core.resources.prefs");
			var properties = encodings.get(preferences);
			if (properties == null) {
				properties = new java.util.Properties();
				if (Files.exists(preferences)) {
					try (var input = Files.newInputStream(preferences)) { properties.load(input); }
				}
				properties.setProperty("eclipse.preferences.version", "1");
				encodings.put(preferences, properties);
			}
			properties.setProperty("encoding/" + path.subpath(2, path.getNameCount()).toString().replace('\\', '/'), "UTF-8");
		}
		for (final var entry : encodings.entrySet()) {
			Files.createDirectories(entry.getKey().getParent());
			try (var output = Files.newOutputStream(entry.getKey())) { entry.getValue().store(output, null); }
		}
	}

	static void copy(final Path source, final Path target, final BooleanSupplier canceled) throws IOException {
		try (var input = Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS);
				var output = Files.newOutputStream(target, java.nio.file.StandardOpenOption.CREATE_NEW)) {
			final byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) != -1) {
				checkCanceled(canceled);
				output.write(buffer, 0, read);
			}
		}
	}

	private static void addData(final Path source, final Path target, final Map<Path, Source> targets,
			final Map<Path, Path> relocated) throws IOException {
		if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS))
			throw new IOException("Model data is not a regular file: " + source);
		final String path = target.subpath(1, target.getNameCount()).toString().replace('\\', '/');
		final var entry = new Source(source, path, StandardCharsets.UTF_8);
		if (targets.putIfAbsent(target, entry) != null) throw new IOException("Conflicting model data: " + target);
		relocated.putIfAbsent(source, target);
	}

	private static boolean isModel(final Path path) {
		final String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return name.endsWith(".gaml") || name.endsWith(".experiment");
	}

	static String projectDescription(final String xml, final String name) throws IOException {
		try {
			final var factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			final var document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
			if (!document.getDocumentElement().getNodeName().equals("projectDescription"))
				throw new IOException("Invalid project description for " + name);
			for (final String tag : List.of("linkedResources", "variableList", "filteredResources")) {
				final var nodes = document.getElementsByTagName(tag);
				while (nodes.getLength() > 0) nodes.item(0).getParentNode().removeChild(nodes.item(0));
			}
			int names = 0;
			final var children = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				final var child = children.item(i);
				if (!child.getNodeName().equals("name")) continue;
				if (!child.getTextContent().equals(name))
					throw new IOException("Project description name must match its workspace directory: " + name);
				names++;
			}
			if (names != 1) throw new IOException("Missing or repeated project name: " + name);
			final var transformers = TransformerFactory.newInstance();
			transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			transformers.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			final var writer = new StringWriter();
			transformers.newTransformer().transform(new DOMSource(document), new StreamResult(writer));
			return writer.toString();
		} catch (final javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException
				| javax.xml.transform.TransformerException | IllegalArgumentException e) {
			throw new IOException("Unable to materialize project description: " + name, e);
		}
	}

	private static void checkCanceled(final BooleanSupplier canceled) throws IOException {
		if (canceled.getAsBoolean()) throw new java.io.InterruptedIOException("Model export canceled");
	}
}
