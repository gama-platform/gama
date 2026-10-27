import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Standalone regression test: run with the repository root as the first argument. */
public class DevToolsMenuTest {

	public static void main(final String[] args) throws Exception {
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final var document = factory.newDocumentBuilder().parse(
				Path.of(args.length == 0 ? "." : args[0], "gama.ui.devtools", "plugin.xml").toFile());
		final var commands = document.getElementsByTagName("command");
		final var names = new java.util.HashMap<String, String>();
		for (int i = 0; i < commands.getLength(); i++) {
			final var command = (Element) commands.item(i);
			if (command.hasAttribute("id")) names.put(command.getAttribute("id"), command.getAttribute("name"));
		}
		final var menus = document.getElementsByTagName("menu");
		Element menu = null;
		for (int i = 0; i < menus.getLength(); i++) {
			final var candidate = (Element) menus.item(i);
			if ("gama.ui.devtools.menu".equals(candidate.getAttribute("id"))) menu = candidate;
		}
		if (menu == null) throw new AssertionError("GAMA menu is missing");
		final List<String> entries = new ArrayList<>();
		final List<String> wizardIds = new ArrayList<>();
		for (final Element entry : children(menu)) {
			if ("separator".equals(entry.getTagName()) && !"true".equals(entry.getAttribute("visible"))) continue;
			entries.add(switch (entry.getTagName()) {
				case "separator" -> "---";
				case "command" -> entry.hasAttribute("label") ? entry.getAttribute("label")
						: names.get(entry.getAttribute("commandId"));
				default -> entry.getAttribute("label");
			});
			if ("gama.ui.devtools.cleanAndRebuild".equals(entry.getAttribute("commandId"))) {
				final String scope = entry.getAttribute("label").contains("selected") ? "selection" : "all";
				equal(scope, children(entry).getFirst().getAttribute("value"));
			}
			if ("gama.ui.devtools.openNewWizard".equals(entry.getAttribute("commandId"))) {
				final Element parameter = children(entry).getFirst();
				equal("gama.ui.devtools.wizardId", parameter.getAttribute("name"));
				wizardIds.add(parameter.getAttribute("value"));
			}
			if ("menu".equals(entry.getTagName())) {
				final String mode = "Run...".equals(entry.getAttribute("label")) ? "run" : "debug";
				final List<Element> launches = children(entry).stream()
						.filter(child -> "command".equals(child.getTagName())).toList();
				equal("run".equals(mode) ? 4 : 3, launches.size());
				for (int i = 0; i < 3; i++) {
					final Element launch = launches.get(i);
					equal("gama.ui.devtools.launchGama", launch.getAttribute("commandId"));
					equal(List.of("GAMA GUI", "GAMA Headless...", "GAMA Tests").get(i),
							launch.getAttribute("label"));
					equal(List.of("GUI", "HEADLESS", "TESTS").get(i),
							children(launch).get(0).getAttribute("value"));
					equal(mode, children(launch).get(1).getAttribute("value"));
				}
				if ("run".equals(mode)) {
					equal("Configure Launches...", launches.get(3).getAttribute("label"));
					equal("configure", children(launches.get(3)).getFirst().getAttribute("value"));
				}
			}
		}
		equal(List.of("Run GAMA", "Run...", "Debug...", "---", "Build GAMA",
				"Clean and build all plugins", "Clean and build selected plugins", "---", "Repair",
				"Repair plugins metadata", "Rebuild annotation processor", "Regenerate Xtext grammar",
				"Update file headers", "Generate missing tests", "---", "Search", "Keyword implementations...",
				"Plugins dependencies...", "Missing annotations...", "---", "Create", "New GAMA Plugin...",
				"New GAMA Skill...", "New GAMA Operators...", "New GAMA Action...", "New GAMA Type...",
				"New GAML Test Model...", "---", "Update GAMA tools"), entries);
		final var wizards = document.getElementsByTagName("wizard");
		final List<String> registeredWizardIds = new ArrayList<>();
		for (int i = 0; i < wizards.getLength(); i++) {
			registeredWizardIds.add(((Element) wizards.item(i)).getAttribute("id"));
		}
		equal(registeredWizardIds, wizardIds);

		final var handlers = document.getElementsByTagName("handler");
		boolean updater = false;
		for (int i = 0; i < handlers.getLength(); i++) {
			final var handler = (Element) handlers.item(i);
			final String commandId = handler.getAttribute("commandId");
			if (commandId.startsWith("gama.ui.devtools.heading.")) {
				throw new AssertionError("Section headings must have no handler to remain disabled");
			}
			if ("gama.ui.devtools.updateTools".equals(commandId)) {
				equal("gama.ui.devtools.handlers.UpdateToolsHandler", handler.getAttribute("class"));
				updater = true;
			}
		}
		if (!updater) throw new AssertionError("The update command must have a handler");
		System.out.println("Devtools menu checks passed");
	}

	private static List<Element> children(final Element element) {
		final List<Element> result = new ArrayList<>();
		for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node instanceof Element child) result.add(child);
		}
		return result;
	}

	private static void equal(final Object expected, final Object actual) {
		if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
	}
}
