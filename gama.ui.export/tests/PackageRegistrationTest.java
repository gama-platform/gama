import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/** JDK-only checks for package command and pre-core application registration. */
public final class PackageRegistrationTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Path.of(args.length == 0 ? "." : args[0]);
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final var parser = factory.newDocumentBuilder();
		final var xpath = XPathFactory.newInstance().newXPath();
		final var ui = parser.parse(root.resolve("gama.ui.export/plugin.xml").toFile());
		check((Boolean) xpath.evaluate("//editor[@id='gama.ui.export.standaloneModel'"
				+ " and @launcher='gama.ui.export.commands.StandaloneModelLauncher' and @default='true']"
				+ "/contentTypeBinding[@contentTypeId='gama.exported.model.type']", ui, XPathConstants.BOOLEAN),
				"Navigator opens playable models through the player, not a text editor");
		check((Boolean) xpath.evaluate("//page[@id='gama.ui.export.standaloneModel.properties']"
				+ "/enabledWhen/adapt[@type='org.eclipse.core.resources.IFile']/or"
				+ "/test[@value='gamx']", ui, XPathConstants.BOOLEAN),
				"Expose playable model details in file properties");
		check((Boolean) xpath.evaluate("//command[@id='gama.ui.export.openPackage']"
				+ "/commandParameter[@id='gama.ui.export.packagePath' and @optional='true']", ui, XPathConstants.BOOLEAN),
				"OS document routing and the file dialog share a parameterized command");
		check((Boolean) xpath.evaluate("//handler[@commandId='gama.ui.export.openPackage'"
				+ " and @class='gama.ui.export.commands.OpenSimulationPackage']", ui, XPathConstants.BOOLEAN),
				"Register the package-open handler");
		check((Boolean) xpath.evaluate("//menuContribution[@locationURI='menu:file?after=additions']"
				+ "/command[@commandId='gama.ui.export.openPackage' and @label='Open playable model...']",
				ui, XPathConstants.BOOLEAN), "Make package opening available from the File menu");
		for (final String command : java.util.List.of("gama.ui.export.model", "gama.ui.export.selectedModel",
				"gama.ui.navigator.commands.exportProjectAsSimulation")) {
			check((Boolean) xpath.evaluate("//command[@id='" + command + "']", ui, XPathConstants.BOOLEAN),
					"Keep existing export commands");
			check((Boolean) xpath.evaluate("//handler[@commandId='" + command
					+ "' and @class='gama.ui.export.commands.ExportSimulation']", ui, XPathConstants.BOOLEAN),
					"Reuse common model discovery for package export");
		}
		check(!(Boolean) xpath.evaluate("//command[@id='gama.ui.export.package' or @id='gama.ui.export.selectedPackage'"
				+ " or @commandId='gama.ui.export.package' or @commandId='gama.ui.export.selectedPackage']",
				ui, XPathConstants.BOOLEAN), "Do not add duplicate package export commands or menu items");
		final var bootstrap = parser.parse(root.resolve("gama.export/plugin.xml").toFile());
		check((Boolean) xpath.evaluate("//content-type[@id='gama.exported.model.type' and @file-extensions='gamx,GAMX']",
				bootstrap, XPathConstants.BOOLEAN), "Recognize .gamx as a distinct content type");
		check((Boolean) xpath.evaluate("//implementation[@class='gama.export.ExportedModelInfo'"
				+ " and @content_type='gama.exported.model.type']", bootstrap, XPathConstants.BOOLEAN),
				"Contribute safe archive metadata to the navigator");
		check((Boolean) xpath.evaluate("//extension[@id='DocumentApplication']"
				+ "/application/run[@class='gama.export.DocumentApplication']", bootstrap, XPathConstants.BOOLEAN),
				"Register document routing independently of the modeling application");
		final var product = parser.parse(root.resolve("gama.product/gama.product").toFile());
		check((Boolean) xpath.evaluate("/product[@application='gama.export.DocumentApplication']",
				product, XPathConstants.BOOLEAN), "Built products must enter the cold document router");
		final var application = parser.parse(root.resolve("gama.ui.application/plugin.xml").toFile());
		check((Boolean) xpath.evaluate("//product[@application='gama.export.DocumentApplication']",
				application, XPathConstants.BOOLEAN), "Product extension and product build must agree on their entry point");
		check((Boolean) xpath.evaluate("//extension[@id='PackageApplication'"
				+ " and @point='org.eclipse.core.runtime.applications']/application/run[@class='gama.export.PackageApplication']",
				bootstrap, XPathConstants.BOOLEAN), "Register a bootstrap independent of the GUI application activator");
		check(Files.readString(root.resolve("gama.export/build.properties")).contains("plugin.xml"),
				"Ship the bootstrap application extension");
		final String applicationDependencies = Files.readString(root.resolve("gama.ui.application/META-INF/MANIFEST.MF"));
		check(!applicationDependencies.contains("gama.export") && !applicationDependencies.contains("gama.ui.export"),
				"Document routing must not introduce a circular bundle dependency");
		System.out.println("Package command and bootstrap registration checks passed");
	}
}
