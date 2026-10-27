package gaml.compiler.ui.editor;

import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.menus.CommandContributionItem;

/** Finalizes the text popup after workbench and language-server contributions have been added. */
final class GamlEditorContextMenu {

	static final String SEARCH_REFERENCE = "gaml.searchReference";
	static final String PREFERENCES = "gaml.preferences.action";
	private static final String REFERENCES_START = "gaml.references.start";
	private static final String REFERENCES_END = "gaml.references.end";
	private static final String PREFERENCES_GROUP = "gaml.preferences";

	private static final Set<String> HIDDEN_IDS = Set.of(
			"org.eclipse.debug.ui.contextualLaunch.run.submenu",
			"org.eclipse.debug.ui.contextualLaunch.debug.submenu",
			"org.eclipse.lsp4e.menu.source",
			"org.eclipse.lsp4e.menu.refactorings",
			"org.eclipse.lsp4e.menu.codeActions",
			"org.eclipse.ui.genericeditor.source.menu");
	private static final Set<String> HIDDEN_LABELS =
			Set.of("Run As", "Debug As", "Source", "Refactorings", "Code Actions");

	private GamlEditorContextMenu() {}

	static void update(final MenuManager manager) {
		// JFace cannot refill a moved ActionContributionItem while its previous native widget still exists.
		for (var item : manager.getMenu().getItems()) item.dispose();
		manager.updateAll(true);
	}

	static void addEditorActions(final IMenuManager menu, final Runnable search, final Runnable openPreferences,
			final ImageDescriptor searchIcon, final ImageDescriptor preferencesIcon) {
		Action searchAction = new Action("Search in GAML Reference", searchIcon) {
			@Override
			public void run() { search.run(); }
		};
		searchAction.setId(SEARCH_REFERENCE);
		menu.add(searchAction);
		Action preferencesAction = new Action("Preferences...", preferencesIcon) {
			@Override
			public void run() { openPreferences.run(); }
		};
		preferencesAction.setId(PREFERENCES);
		menu.add(preferencesAction);
	}

	static void finish(final IMenuManager menu, final ImageDescriptor preferencesIcon) {
		IContributionItem paste = null;
		IContributionItem findReferences = null;
		IContributionItem searchReference = null;
		IContributionItem preferences = null;
		for (IContributionItem item : menu.getItems()) {
			IContributionItem contribution = unwrap(item);
			String id = contribution.getId();
			String label = label(contribution);
			if (HIDDEN_IDS.contains(id == null ? "" : id)
					|| contribution instanceof MenuManager submenu
							&& HIDDEN_LABELS.contains(normalizedLabel(submenu.getMenuText()))) {
				menu.remove(item);
			} else if ("paste".equals(id) || "org.eclipse.ui.edit.paste".equals(id) || "Paste".equals(label)) {
				paste = item;
			} else if ("org.eclipse.xtext.ui.editor.FindReferences".equals(id) || "Find References".equals(label)
					|| "References in other models".equals(label)) {
				findReferences = item;
				if (contribution instanceof ActionContributionItem action)
					action.getAction().setText("References in other models");
			} else if (SEARCH_REFERENCE.equals(id)) {
				searchReference = item;
			} else if (PREFERENCES.equals(id) || "__PREFS__.ContextAction".equals(id) || "Preferences".equals(label)
					|| "Preferences...".equals(label) || "Preferences\u2026".equals(label)) {
				menu.remove(item);
				if (preferences == null || PREFERENCES.equals(id)) preferences = item;
				if (contribution instanceof ActionContributionItem action) action.getAction().setImageDescriptor(preferencesIcon);
			}
		}
		menu.remove(REFERENCES_START);
		menu.remove(REFERENCES_END);
		menu.remove(PREFERENCES_GROUP);
		if (findReferences != null) menu.remove(findReferences);
		if (searchReference != null) menu.remove(searchReference);
		if (findReferences != null || searchReference != null) {
			IContributionItem start = new Separator(REFERENCES_START);
			if (paste == null) menu.add(start);
			else {
				// Contribution IDs can be null; rebuilding preserves the original Paste item and its action.
				IContributionItem[] existing = menu.getItems();
				menu.removeAll();
				for (IContributionItem item : existing) {
					menu.add(item);
					if (item == paste) menu.add(start);
				}
			}
			menu.insertAfter(REFERENCES_START, new Separator(REFERENCES_END));
			for (IContributionItem item : new IContributionItem[] { findReferences, searchReference }) {
				if (item == null) continue;
				menu.insertBefore(REFERENCES_END, item);
			}
		}
		if (preferences != null) {
			menu.remove(preferences);
			menu.add(new Separator(PREFERENCES_GROUP));
			menu.add(preferences);
		}
	}

	private static IContributionItem unwrap(final IContributionItem item) {
		IContributionItem result = item;
		while (result instanceof SubContributionItem wrapper) result = wrapper.getInnerItem();
		return result;
	}

	private static String label(final IContributionItem item) {
		if (item instanceof ActionContributionItem action) return normalizedLabel(action.getAction().getText());
		if (item instanceof CommandContributionItem command) {
			if (command.getCommand() != null && "org.eclipse.xtext.ui.editor.FindReferences".equals(command.getCommand().getId()))
				return "Find References";
			return normalizedLabel(command.getData().label);
		}
		return "";
	}

	private static String normalizedLabel(final String label) {
		if (label == null) return "";
		return label.split("\t", 2)[0].replace("&", "").strip();
	}
}
