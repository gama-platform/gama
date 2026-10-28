import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

/** Standalone product startup-order checks; run from the repository root. */
public final class ProductStartupTest {

	public static void main(final String[] args) throws Exception {
		final Path root = Path.of(args.length == 0 ? "." : args[0]);
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final var xpath = XPathFactory.newInstance().newXPath();
		for (final String product : new String[] { "gama.product", "gama.headless.product" }) {
			final var document = factory.newDocumentBuilder().parse(root.resolve("gama.product/" + product).toFile());
			final int configuratorLevel = Integer.parseInt(xpath.evaluate(
					"/product/configurations/plugin[@id='org.eclipse.equinox.simpleconfigurator']/@startLevel",
					document));
			for (final String service : new String[] { "org.apache.aries.spifly.dynamic.bundle", "org.apache.felix.scr" }) {
				final String level = xpath.evaluate(
						"/product/configurations/plugin[@id='" + service + "']/@startLevel", document);
				if (!level.isEmpty() && Integer.parseInt(level) <= configuratorLevel)
					throw new AssertionError(product + ": " + service + " must start after bundle configuration");
			}
		}
		System.out.println("Product startup-order checks passed");
	}
}
