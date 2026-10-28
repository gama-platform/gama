package gama.ui.devtools.install;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Filesystem and project-set parsing, independent of Eclipse. */
public final class InstallModel {

	public static final String COMMUNITY_SET = "==================== COMMUNITY";
	public static final String COMMUNITY_BRANCH = "GAMA_2026-06";
	public static final String PROJECT_SETS = "Project sets for Gama.psf";

	public enum Kind {
		CORE("core plugins", "https://github.com/gama-platform/gama.git", "main", "gama"),
		GRAPHICAL("graphical editor", "https://github.com/gama-platform-plugins/gama.graphical.modeling.git",
				COMMUNITY_BRANCH, "gama.graphical.modeling"),
		COMMUNITY("community plugins", "", COMMUNITY_BRANCH, "");

		public final String label, url, branch, folder;

		Kind(final String label, final String url, final String branch, final String folder) {
			this.label = label;
			this.url = url;
			this.branch = branch;
			this.folder = folder;
		}
	}

	public record Repository(String name, String url, String branch) {}

	public record Project(String name, Path directory, Repository repository, boolean recommended) {}

	public record RemoteProject(Repository repository, String path) {
		@Override
		public String toString() {
			return repository.name() + " / " + (path.isEmpty() ? "(root project)" : path);
		}
	}

	private InstallModel() {}

	public static Document xml(final InputStream input) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		return factory.newDocumentBuilder().parse(input);
	}

	public static List<Project> projects(final Path root, final Repository repository, final Kind kind)
			throws Exception {
		Set<Path> recommended = new LinkedHashSet<>();
		Path pom = root.resolve("gama.parent/pom.xml");
		if (kind == Kind.CORE && Files.isRegularFile(pom)) {
			try (InputStream input = Files.newInputStream(pom)) {
				var modules = xml(input).getElementsByTagName("module");
				for (int i = 0; i < modules.getLength(); i++) {
					recommended.add(pom.getParent().resolve(modules.item(i).getTextContent().strip()).normalize());
				}
			}
			recommended.add(pom.getParent());
		}
		List<Project> projects = new ArrayList<>();
		List<Path> descriptions = new ArrayList<>();
		// Community repositories sometimes keep projects below a plugins/ directory.
		Files.walkFileTree(root, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) {
				if (directory.equals(root)) return FileVisitResult.CONTINUE;
				if (Set.of(".git", ".metadata", "target", "bin").contains(directory.getFileName().toString())
						|| kind == Kind.CORE && root.relativize(directory).getNameCount() > 1) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
				if (file.getFileName().toString().equals(".project")) descriptions.add(file);
				return FileVisitResult.CONTINUE;
			}
		});
		for (Path description : descriptions) {
			Path directory = description.getParent();
			try (InputStream input = Files.newInputStream(description)) {
				Element document = xml(input).getDocumentElement();
				String name = document.getElementsByTagName("name").item(0).getTextContent().strip();
				projects.add(new Project(name, directory, repository,
						kind == Kind.GRAPHICAL || recommended.contains(directory.normalize())));
			}
		}
		projects.sort(Comparator.comparing(Project::name));
		return projects;
	}

	public static Map<String, List<String>> workingSets(final InputStream input) throws Exception {
		Map<String, List<String>> result = new LinkedHashMap<>();
		var sets = xml(input).getElementsByTagName("workingSets");
		for (int i = 0; i < sets.getLength(); i++) {
			Element set = (Element) sets.item(i);
			List<String> projects = new ArrayList<>();
			var items = set.getElementsByTagName("item");
			for (int j = 0; j < items.getLength(); j++) {
				Element item = (Element) items.item(j);
				String path = item.getAttribute("path");
				String java = item.getAttribute("elementID");
				if (path.startsWith("/")) projects.add(path.substring(1).split("/")[0]);
				else if (java.startsWith("=")) projects.add(java.substring(1).split("[/<>]")[0]);
			}
			result.put(set.getAttribute("name"), projects);
		}
		result.putIfAbsent(COMMUNITY_SET, List.of());
		return result;
	}
}
