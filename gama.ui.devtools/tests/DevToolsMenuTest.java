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
		final List<String> kinds = new ArrayList<>();
		for (final Element entry : children(menu)) {
			equal(false, entry.hasAttribute("icon"));
			if ("separator".equals(entry.getTagName()) && !"true".equals(entry.getAttribute("visible"))) continue;
			entries.add(switch (entry.getTagName()) {
				case "separator" -> "---";
				case "command" -> label(entry, names);
				default -> entry.getAttribute("label");
			});
			if ("gama.ui.devtools.cleanAndRebuild".equals(entry.getAttribute("commandId"))) {
				equal("all", children(entry).getFirst().getAttribute("value"));
			}
			if (!"menu".equals(entry.getTagName())) continue;
			final String label = entry.getAttribute("label");
			final List<Element> items = children(entry);
			for (Element item : items) equal(false, item.hasAttribute("icon"));
			final List<String> expected = switch (label) {
				case "Run..." -> List.of("Run UI", "Run headless", "Run tests", "---", "Debug UI", "Debug headless",
						"---", "Configure...");
				case "Repair..." -> List.of("Rebuild annotation processor",
						"Regenerate Xtext grammar", "Update file headers", "Generate missing tests");
				case "Search..." -> List.of("Keyword implementations...", "Plugins dependencies...", "Missing annotations...");
				case "Create..." -> List.of("New GAMA Plugin...", "New GAMA Skill...", "New GAMA Operators...",
						"New GAMA Action...", "New GAMA Type...", "New GAML Test Model...");
				case "Install..." -> List.of("Install core plugins...", "Install graphical editor...",
						"Install community plugins...");
				default -> throw new AssertionError("Unexpected submenu " + label);
			};
			equal(expected, items.stream().map(item -> "separator".equals(item.getTagName()) ? "---" : label(item, names)).toList());
			for (Element item : items) {
				if (!"command".equals(item.getTagName())) continue;
				final List<Element> parameters = children(item);
				switch (label) {
					case "Run..." -> {
						equal("gama.ui.devtools.launchGama", item.getAttribute("commandId"));
						final String text = item.getAttribute("label");
						if ("Configure...".equals(text)) {
							equal(1, parameters.size());
							equal("configure", parameters.getFirst().getAttribute("value"));
						} else {
							equal(text.endsWith("UI") ? "GUI" : text.endsWith("headless") ? "HEADLESS" : "TESTS",
									parameters.get(0).getAttribute("value"));
							equal(text.startsWith("Run") ? "run" : "debug", parameters.get(1).getAttribute("value"));
						}
					}
					case "Create..." -> {
						equal("gama.ui.devtools.openNewWizard", item.getAttribute("commandId"));
						equal("gama.ui.devtools.wizardId", parameters.getFirst().getAttribute("name"));
						wizardIds.add(parameters.getFirst().getAttribute("value"));
					}
					case "Install..." -> {
						equal("gama.ui.devtools.install", item.getAttribute("commandId"));
						equal("gama.ui.devtools.installKind", parameters.getFirst().getAttribute("name"));
						kinds.add(parameters.getFirst().getAttribute("value"));
					}
					default -> {}
				}
			}
		}
		equal(List.of("Run...", "---", "Clean all...", "Search...", "Repair...", "Create...", "---", "Install...",
				"Update..."), entries);
		equal(List.of("CORE", "GRAPHICAL", "COMMUNITY"), kinds);
		final var wizards = document.getElementsByTagName("wizard");
		final List<String> registeredWizardIds = new ArrayList<>();
		for (int i = 0; i < wizards.getLength(); i++) {
			registeredWizardIds.add(((Element) wizards.item(i)).getAttribute("id"));
		}
		equal(registeredWizardIds, wizardIds);
		for (String id : names.keySet()) equal(false, id.startsWith("gama.ui.devtools.heading."));

		final var handlers = document.getElementsByTagName("handler");
		boolean updater = false;
		boolean installer = false;
		for (int i = 0; i < handlers.getLength(); i++) {
			final var handler = (Element) handlers.item(i);
			final String commandId = handler.getAttribute("commandId");
			if ("gama.ui.devtools.updateTools".equals(commandId)) {
				equal("gama.ui.devtools.handlers.UpdateToolsHandler", handler.getAttribute("class"));
				updater = true;
			}
			if ("gama.ui.devtools.install".equals(commandId)) {
				equal("gama.ui.devtools.handlers.InstallSourcesHandler", handler.getAttribute("class"));
				installer = true;
			}
		}
		if (!updater) throw new AssertionError("The update command must have a handler");
		if (!installer) throw new AssertionError("The install command must have a handler");
		Element popup = null;
		for (int i = 0; i < menus.getLength(); i++) {
			final var candidate = (Element) menus.item(i);
			if ("gama.ui.devtools.popup".equals(candidate.getAttribute("id"))) popup = candidate;
		}
		if (popup == null) throw new AssertionError("GAMA popup menu is missing");
		equal(List.of("Clean...", "Update headers...", "Report missing annotations..."), children(popup).stream()
				.filter(item -> !"visibleWhen".equals(item.getTagName())).map(item -> label(item, names)).toList());
		equal("gama.ui.devtools.gamaPlugin",
				((Element) popup.getElementsByTagName("test").item(0)).getAttribute("property"));
		System.out.println("Devtools menu checks passed");
	}

	private static String label(final Element item, final java.util.Map<String, String> names) {
		return item.hasAttribute("label") ? item.getAttribute("label") : names.get(item.getAttribute("commandId"));
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
