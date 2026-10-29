package gama.export;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** Uses build-time product metadata, not the author's potentially extended installation. */
final class StandardDistribution {

	@FunctionalInterface
	interface Resources { InputStream open(String path) throws IOException; }

	private StandardDistribution() {}

	static Set<String> bundles() throws IOException {
		return bundles(path -> StandardDistribution.class.getResourceAsStream("/standard-distribution/" + path));
	}

	static Set<String> bundles(final Resources resources) throws IOException {
		final Element product = read(resources, "gama.product/gama.product");
		final var result = new HashSet<String>();
		final var visited = new HashSet<String>();
		final var features = product.getElementsByTagName("feature");
		if (features.getLength() == 0) throw new IOException("Standard GAMA product has no feature inventory");
		for (int i = 0; i < features.getLength(); i++)
			feature(resources, ((Element) features.item(i)).getAttribute("id"), result, visited);
		return Set.copyOf(result);
	}

	private static void feature(final Resources resources, final String id, final Set<String> bundles,
			final Set<String> visited) throws IOException {
		if (!id.matches("[A-Za-z0-9_.-]+")) throw new IOException("Invalid standard GAMA feature identity: " + id);
		if (!visited.add(id)) return;
		final Element feature = read(resources, id + "/feature.xml");
		if (!id.equals(feature.getAttribute("id"))) throw new IOException("Standard GAMA feature identity mismatch: " + id);
		final var plugins = feature.getElementsByTagName("plugin");
		for (int i = 0; i < plugins.getLength(); i++) {
			final String plugin = ((Element) plugins.item(i)).getAttribute("id");
			if (plugin.isBlank()) throw new IOException("Missing plugin identity in standard GAMA feature: " + id);
			bundles.add(plugin);
		}
		final var includes = feature.getElementsByTagName("includes");
		for (int i = 0; i < includes.getLength(); i++)
			feature(resources, ((Element) includes.item(i)).getAttribute("id"), bundles, visited);
		final var imports = feature.getElementsByTagName("import");
		for (int i = 0; i < imports.getLength(); i++) {
			final var dependency = (Element) imports.item(i);
			if (dependency.hasAttribute("feature")) feature(resources, dependency.getAttribute("feature"), bundles, visited);
			if (dependency.hasAttribute("plugin")) bundles.add(dependency.getAttribute("plugin"));
		}
	}

	private static Element read(final Resources resources, final String path) throws IOException {
		try (var input = resources.open(path)) {
			if (input == null) throw new IOException("Missing standard GAMA distribution metadata: " + path
					+ ". Build gama.export before exporting models.");
			final var factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
			return factory.newDocumentBuilder().parse(input).getDocumentElement();
		} catch (final ParserConfigurationException | SAXException e) {
			throw new IOException("Invalid standard GAMA distribution metadata: " + path, e);
		}
	}
}
