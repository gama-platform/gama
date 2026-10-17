package gaml.compiler.ui.editor;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

public final class GamlEditorContextMenuTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) throws Exception {
		Display display = new Display();
		Shell shell = new Shell(display);
		MenuManager manager = new MenuManager();
		var externalActions = ExternalActionManager.getInstance();
		var previousCallback = externalActions.getCallback();
		try {
			for (String path : args) {
				for (int zoom : new int[] { 100, 200 }) {
					try (var input = java.nio.file.Files.newInputStream(java.nio.file.Path.of(path))) {
						var image = org.eclipse.swt.internal.NativeImageLoader.load(
								new org.eclipse.swt.internal.DPIUtil.ElementAtZoom<>(input, 100),
								new org.eclipse.swt.graphics.ImageLoader(), zoom).getFirst().element();
						check(image.width == 25 * zoom / 100 && image.height == image.width,
								"Preferences SVG renders at standard and high DPI");
					}
				}
			}
			// The workbench can hide actions bound to inactive commands; the standalone default does not.
			externalActions.setCallback(new ExternalActionManager.ICallback() {
				@Override
				public boolean isActive(final String commandId) { return commandId == null; }
				@Override
				public void addPropertyChangeListener(final String id, final org.eclipse.jface.util.IPropertyChangeListener listener) {}
				@Override
				public void removePropertyChangeListener(final String id, final org.eclipse.jface.util.IPropertyChangeListener listener) {}
				@Override
				public Integer getAccelerator(final String id) { return null; }
				@Override
				public String getAcceleratorText(final String id) { return null; }
				@Override
				public boolean isAcceleratorInUse(final int accelerator) { return false; }
			});
			AtomicInteger searches = new AtomicInteger();
			AtomicInteger preferenceOpens = new AtomicInteger();
			Action paste = new Action("Paste") {};
			paste.setId("paste");
			Action findReferences = new Action("Find References") {};
			findReferences.setId("org.eclipse.xtext.ui.editor.FindReferences");
			ImageDescriptor icon = ImageDescriptor.getMissingImageDescriptor();
			manager.setRemoveAllWhenShown(true);
			manager.addMenuListener(menu -> {
				menu.add(new Action("Edit...") {});
				menu.add(paste);
				menu.add(new Action("Delete") {});
				GamlEditorContextMenu.addEditorActions(menu, searches::incrementAndGet,
						preferenceOpens::incrementAndGet, icon, icon);
			});
			// Simulate contributions arriving after the editor's own menu listener.
			manager.addMenuListener(menu -> {
				menu.add(new MenuManager("Localized run", "org.eclipse.debug.ui.contextualLaunch.run.submenu"));
				menu.add(new MenuManager("De&bug As"));
				menu.add(new MenuManager("&Source"));
				menu.add(new MenuManager("Refactorings"));
				var wrapped = new SubContributionItem(new MenuManager("Code &Actions\tshortcut"));
				wrapped.setVisible(true);
				menu.add(wrapped);
				menu.add(new Action("Rename") {});
				menu.add(new Action("Toggle Comments") {});
				MenuManager references = new MenuManager("References", "references");
				references.add(new Action("Reference entry") {});
				menu.add(references);
				menu.add(findReferences);
				if (searches.get() == 0) {
					Action legacyPreferences = new Action("&Preferences...") {};
					legacyPreferences.setId("__PREFS__.ContextAction");
					legacyPreferences.setActionDefinitionId("inactive.preferences.command");
					menu.add(legacyPreferences);
				}
			});
			var popup = manager.createContextMenu(shell);
			popup.addListener(SWT.Show, event -> {
				GamlEditorContextMenu.finish(manager, icon);
				GamlEditorContextMenu.update(manager);
			});
			for (int opening = 0; opening < 2; opening++) {
				popup.notifyListeners(SWT.Show, new Event());
				GamlEditorContextMenu.finish(manager, icon);
				GamlEditorContextMenu.update(manager);
				var items = manager.getItems();
				check(items.length == 12, "Only requested menus removed; grouping is idempotent");
				check(items[1].getId().equals("paste") && items[2].isSeparator(), "Reference group follows Paste");
				check(items[3].getId().equals(findReferences.getId()), "Find References first in group");
				check(items[4].getId().equals(GamlEditorContextMenu.SEARCH_REFERENCE), "Search in reference second in group");
				check(items[5].isSeparator(), "Reference group ends with separator");
				check(items[items.length - 1].getId().equals(GamlEditorContextMenu.PREFERENCES), "Preferences last");
				var preferences = ((ActionContributionItem) items[items.length - 1]).getAction();
				check(preferences.getImageDescriptor() == icon, "Preferences has requested icon");
				check(manager.find("references") != null, "Reference menu preserved");
				check(Arrays.stream(popup.getItems()).noneMatch(item -> item.getText().contains("Code")),
						"Filtered contributions absent from native menu");
				check(Arrays.stream(popup.getItems()).noneMatch(item -> item.getText().startsWith("Define ")
						|| item.getText().startsWith("Extract ")), "No creation actions in popup");
				check(popup.getItem(popup.getItemCount() - 1).getImage() != null, "Native Preferences item has icon");
				check(Arrays.stream(popup.getItems()).anyMatch(item -> item.getText().equals("Search in GAML Reference")),
						"Native reference search visible with inactive workbench commands");
				check(popup.getItem(popup.getItemCount() - 1).getText().equals("Preferences..."),
						"Native Preferences visible without inherited preferences contribution");
				check(Arrays.equals(Arrays.stream(popup.getItems()).map(item -> item.getText()).toArray(String[]::new),
						new String[] { "Edit...", "Paste", "", "References in other models", "Search in GAML Reference", "",
								"Delete", "Rename", "Toggle Comments", "References", "", "Preferences..." }),
						"Every native entry is present in the exact requested order");
				popup.getItem(4).notifyListeners(SWT.Selection, new Event());
				popup.getItem(popup.getItemCount() - 1).notifyListeners(SWT.Selection, new Event());
				check(searches.get() == opening + 1 && preferenceOpens.get() == opening + 1,
						"Production actions retain their callbacks");
			}
			System.out.println("GAML context menu regression: 30 checks passed");
		} finally {
			manager.dispose();
			externalActions.setCallback(previousCallback);
			shell.dispose();
			display.dispose();
		}
	}
}
