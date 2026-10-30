package gama.export;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import gama.api.additions.GamaBundleLoader;

/** Uses the stock declarations collected at GAMA startup, not the installed plugin inventory. */
final class StandardDistribution {

	private StandardDistribution() {}

	static Set<String> bundles() throws IOException {
		final Set<String> bundles = GamaBundleLoader.getStandardDistributionBundles();
		if (bundles.isEmpty()) throw new IOException("Standard GAMA distribution declarations have not been loaded");
		final var result = new HashSet<>(bundles);
		for (final String feature : new String[] { "gama.feature.dependencies", "gama.feature.ui.dependencies" }) {
			try (var input = feature(feature)) {
				if (input == null) throw new IOException("Missing standard dependency feature: " + feature);
				final var factory = DocumentBuilderFactory.newInstance();
				factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				factory.setXIncludeAware(false);
				factory.setExpandEntityReferences(false);
				final var plugins = factory.newDocumentBuilder().parse(input).getElementsByTagName("plugin");
				for (int i = 0; i < plugins.getLength(); i++) {
					final String id = ((Element) plugins.item(i)).getAttribute("id");
					if (id.isBlank()) throw new IOException("Missing bundle identity in " + feature);
					result.add(id);
				}
			} catch (final ParserConfigurationException | SAXException e) {
				throw new IOException("Invalid standard dependency feature: " + feature, e);
			}
		}
		return Set.copyOf(result);
	}

	private static InputStream feature(final String id) throws IOException {
		final var bundle = FrameworkUtil.getBundle(StandardDistribution.class);
		if (bundle != null) {
			final Path location = FileLocator.getBundleFileLocation(bundle).map(java.io.File::toPath).orElse(null);
			if (location != null && Files.isDirectory(location.resolve("src"))
					&& Files.isRegularFile(location.resolve(".project"))) {
				final Path source = location.getParent().resolve(id + "/feature.xml");
				return Files.newInputStream(source);
			}
		}
		return StandardDistribution.class.getResourceAsStream("/standard-dependencies/" + id + "/feature.xml");
	}
}
