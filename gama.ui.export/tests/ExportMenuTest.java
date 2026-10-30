import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

/** Standalone menu registration checks; run with the repository root as the first argument. */
public final class ExportMenuTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static boolean matches(final Document document, final String expression) throws Exception {
		return (Boolean) XPathFactory.newInstance().newXPath().evaluate(expression, document, XPathConstants.BOOLEAN);
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Path.of(args.length == 0 ? "." : args[0]);
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final var document = factory.newDocumentBuilder().parse(root.resolve("gama.ui.export/plugin.xml").toFile());
		final String label = "Export as playable model...";
		check(!matches(document, "//*[@commandId='gama.ui.export.sessions' or @id='gama.ui.export.sessions']"),
				"Do not expose a playable model history or session management command");
		for (String command : new String[] { "gama.ui.export.model", "gama.ui.export.selectedModel" }) {
			check(matches(document, "//command[@id='" + command + "' and @name='Export as playable model']"),
					"Both export commands must use the playable model name");
			check(matches(document, "//handler[@commandId='" + command
					+ "' and @class='gama.ui.export.commands.ExportSimulation']"),
					"Both commands must open the existing export dialog");
		}
		check(matches(document, "//menuContribution[@locationURI='menu:file?after=additions']"
				+ "/command[@commandId='gama.ui.export.model' and @label='" + label + "']"),
				"Rename the File menu entry");
		check(matches(document, "//menuContribution[@locationURI='popup:navigator.menu?endof=additions']"
				+ "/command[@commandId='gama.ui.export.selectedModel' and @label='" + label + "']"),
				"Add the model entry directly to the navigator context menu");
		check(matches(document, "//handler[@commandId='gama.ui.export.selectedModel']"
				+ "/enabledWhen/reference[@definitionId='gama.ui.export.isModelSelection']"),
				"Navigator enablement must depend on the selection, not the active editor");
		final String selection = "//definition[@id='gama.ui.export.isModelSelection']/and/with[@variable='selection']";
		check(matches(document, selection + "/count[@value='1']"), "Require a single selection");
		check(matches(document, selection + "/iterate[@ifEmpty='false']/or"
				+ "[count(instanceof)=2]"
				+ "[instanceof/@value='gama.ui.navigator.view.contents.WrappedGamaFile']"
				+ "[instanceof/@value='gama.ui.navigator.view.contents.WrappedExperimentContent']"),
				"Only model files and experiment nodes enable model export");
		check(matches(document, "//handler[@commandId='gama.ui.navigator.commands.exportProjectAsSimulation']"),
				"Retain the existing project/folder export");
		check(matches(document, "//command[@id='gama.ui.navigator.commands.exportProjectAsSimulation'"
				+ " and @name='Export as playable model']"),
				"Use playable model terminology for project export");
		check(matches(document, "//menuContribution[@locationURI='popup:menu.gama.export?after=additions']"
				+ "/command[@commandId='gama.ui.navigator.commands.exportProjectAsSimulation'"
				+ " and @label='As playable model...']"),
				"Use playable model terminology in the navigator export submenu");

		final var shared = factory.newDocumentBuilder().parse(root.resolve("gama.ui.shared/plugin.xml").toFile());
		for (String command : new String[] { "gama.ui.install.graphical", "gama.ui.install.copilot", "gama.ui.install" }) {
			check(matches(shared, "//menuContribution[@locationURI='menu:support?after=group.about.ext']"
					+ "/command[@commandId='" + command + "']/visibleWhen[@checkEnabled='false']"
					+ "/not/systemTest[@property='exported_runner' and @value='true']"),
					"Hide Support installation entries in player mode: " + command);
		}

		for (String bundle : new String[] { "gama.ui.editor", "gama.ui.navigator" }) {
			try (var input = Files.newInputStream(root.resolve(bundle + "/META-INF/MANIFEST.MF"))) {
				final var attributes = new Manifest(input).getMainAttributes();
				for (String header : new String[] { "Require-Bundle", "Import-Package" }) {
					final String dependencies = attributes.getValue(header);
					check(dependencies == null || !dependencies.contains("gama.export")
							&& !dependencies.contains("gama.ui.export"),
							"The editor and navigator must not depend on export bundles");
				}
			}
		}
		System.out.println("Export menu registration checks passed");
	}
}
