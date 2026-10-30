package gama.export;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;

/** Source consistency check; product files are not used by runtime export. */
public final class StandardDistributionTest {

	private static Element read(final Path path) throws Exception {
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		return factory.newDocumentBuilder().parse(path.toFile()).getDocumentElement();
	}

	private static void feature(final Path root, final String id, final Set<String> visited,
			final Set<String> stock) throws Exception {
		if (!visited.add(id)) return;
		final var xml = read(root.resolve(id + "/feature.xml"));
		for (final String tag : new String[] { "plugin", "includes", "import" }) {
			final var entries = xml.getElementsByTagName(tag);
			for (int i = 0; i < entries.getLength(); i++) {
				final var entry = (Element) entries.item(i);
				if (tag.equals("plugin")) stock.add(entry.getAttribute("id"));
				if (tag.equals("includes")) feature(root, entry.getAttribute("id"), visited, stock);
				if (tag.equals("import")) {
					if (entry.hasAttribute("plugin")) stock.add(entry.getAttribute("plugin"));
					if (entry.hasAttribute("feature")) feature(root, entry.getAttribute("feature"), visited, stock);
				}
			}
		}
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Path.of(args.length == 0 ? "." : args[0]);
		final var stock = new HashSet<String>();
		final var visited = new HashSet<String>();
		final var features = read(root.resolve("gama.product/gama.product")).getElementsByTagName("feature");
		for (int i = 0; i < features.getLength(); i++)
			feature(root, ((Element) features.item(i)).getAttribute("id"), visited, stock);
		final var declared = new HashSet<String>();
		try (var projects = Files.list(root)) {
			for (final Path project : projects.filter(Files::isDirectory).toList()) {
				if (!Files.isRegularFile(project.resolve("plugin.xml"))) continue;
				final var entries = read(project.resolve("plugin.xml")).getElementsByTagName("extension");
				for (int i = 0; i < entries.getLength(); i++) {
					final var extension = (Element) entries.item(i);
					if (!extension.getAttribute("point").equals("gama.standard.distribution")) continue;
					declared.add(project.getFileName().toString());
					if (extension.getElementsByTagName("bundle").getLength() != 0)
						throw new AssertionError("Do not duplicate third-party inventory in plugin.xml");
				}
				for (final String feature : new String[] { "gama.feature.dependencies", "gama.feature.ui.dependencies" }) {
					final var plugins = read(root.resolve(feature + "/feature.xml")).getElementsByTagName("plugin");
					for (int i = 0; i < plugins.getLength(); i++)
						declared.add(((Element) plugins.item(i)).getAttribute("id"));
				}
			}
		}
		if (!stock.equals(declared)) {
			final var missing = new HashSet<>(stock);
			missing.removeAll(declared);
			declared.removeAll(stock);
			throw new AssertionError("Standard markers differ from product: missing=" + missing + ", extra=" + declared);
		}
		System.out.println("Standard distribution markers match all " + stock.size() + " stock bundles");
	}
}
