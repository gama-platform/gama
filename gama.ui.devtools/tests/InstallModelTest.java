import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import gama.ui.devtools.install.InstallModel;
import gama.ui.devtools.install.InstallModel.Kind;
import gama.ui.devtools.install.InstallModel.Repository;

/** Standalone discovery and bundled project-set regression checks. */
public class InstallModelTest {

	public static void main(final String[] args) throws Exception {
		Path repository = Path.of(args.length == 0 ? "." : args[0]).toAbsolutePath().normalize();
		Repository source = new Repository("gama", Kind.CORE.url, Kind.CORE.branch);
		var actual = InstallModel.projects(repository, source, Kind.CORE);
		check(actual.stream().anyMatch(p -> p.name().equals("gama.core") && p.recommended()), "Core module selected");
		check(actual.stream().anyMatch(p -> p.name().equals("gama.parent") && p.recommended()), "Parent selected");
		check(actual.stream().noneMatch(p -> p.directory().getNameCount() > repository.getNameCount() + 1),
				"Only top-level core projects");
		try (var input = Files.newInputStream(repository.resolve("gama.ui.devtools/utils/" + InstallModel.PROJECT_SETS))) {
			var sets = InstallModel.workingSets(input);
			check(sets.get("==================== CORE").containsAll(List.of("gama.core", "gama.api")),
					"Java project handles parsed");
			check(sets.get("==================== BUILD").contains("gama.parent"), "Resource handles parsed");
			check(sets.containsKey(InstallModel.COMMUNITY_SET), "COMMUNITY set has the same naming format");
			check(sets.keySet().stream().noneMatch(s -> s.endsWith("EXPERIMENTAL")), "No EXPERIMENTAL set");
		}
		Path root = Files.createTempDirectory("gama-install-discovery-");
		try {
			project(root.resolve("plugins/one"), "actual.project.name");
			project(root.resolve("target/generated"), "ignored.target");
			project(root.resolve(".git/hidden"), "ignored.git");
			var community = InstallModel.projects(root, source, Kind.COMMUNITY);
			check(community.size() == 1 && community.getFirst().name().equals("actual.project.name"),
					"Discover nested community projects using .project names");
			check(!community.getFirst().recommended(), "Community choice is explicit");
			check(InstallModel.projects(root, source, Kind.GRAPHICAL).getFirst().recommended(),
					"Graphical projects selected");
			try {
				InstallModel.xml(new ByteArrayInputStream(("""
						<!DOCTYPE x [<!ENTITY file SYSTEM "file:///etc/passwd">]>
						<projectDescription><name>&file;</name></projectDescription>
						""").getBytes(StandardCharsets.UTF_8)));
				throw new AssertionError("External entities accepted");
			} catch (org.xml.sax.SAXParseException expected) {}
		} finally {
			try (var paths = Files.walk(root)) {
				for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
			}
		}
		System.out.println("Installation discovery checks passed");
	}

	private static void project(final Path directory, final String name) throws Exception {
		Files.createDirectories(directory);
		Files.writeString(directory.resolve(".project"),
				"<projectDescription><name>" + name + "</name></projectDescription>");
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}
}
