/*******************************************************************************************************
 *
 * EditorToolbar.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.toolbar;

import static gama.ui.shared.utils.WorkbenchHelper.getCommand;
import static gama.ui.shared.utils.WorkbenchHelper.runCommand;
import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY;
import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import gama.api.additions.GamaBundleLoader;
import gama.api.runtime.SystemInfo;
import gama.api.ui.IGui;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.bindings.GamaKeyBindings.PluggableBinding;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.ui.editor.GamlEditor;

/**
 * The class EditorNavigationControls.
 *
 * @author drogoul
 * @since 11 nov. 2016
 *
 */
public class EditorToolbar {

	static {
		DEBUG.OFF();
		Command command = WorkbenchHelper.getCommand("org.eclipse.ui.edit.text.zoomOut");
		if (command != null) { command.setHandler(null); }
		command = WorkbenchHelper.getCommand("org.eclipse.ui.edit.text.zoomIn");
		if (command != null) { command.setHandler(null); }
		command = WorkbenchHelper.getCommand("org.eclipse.ui.edit.text.zoomOut");
		if (command != null) { command.setHandler(null); }
		if (SystemInfo.isMac()) {
			GamaKeyBindings.plug(new PluggableBinding(SWT.MOD1 | SWT.SHIFT, '=') {
				// +
				@Override
				public void run() {
					IEditorPart part = WorkbenchHelper.getActiveEditor();
					if (part instanceof GamlEditor ge) { ge.zoomIn(); }
				}
			});
		} else {
			GamaKeyBindings.plug(new PluggableBinding(SWT.MOD1, '+') {

				@Override
				public void run() {
					IEditorPart part = WorkbenchHelper.getActiveEditor();
					if (part instanceof GamlEditor ge) { ge.zoomIn(); }
				}
			});
		}

		GamaKeyBindings.plug(new PluggableBinding(SWT.MOD1, '=') {

			@Override
			public void run() {
				IEditorPart part = WorkbenchHelper.getActiveEditor();
				if (part instanceof GamlEditor ge) { ge.zoomFit(); }
			}
		});

		GamaKeyBindings.plug(new PluggableBinding(SWT.MOD1, '-') {

			@Override
			public void run() {
				IEditorPart part = WorkbenchHelper.getActiveEditor();
				if (part instanceof GamlEditor ge) { ge.zoomOut(); }
			}
		});
	}

	/** The previous. */
	ToolItem next, previous; // diagram;

	/** The editor. */
	final GamlEditor editor;

	/** The global previous. */
	final Selector globalPrevious = e -> runCommand(NAVIGATE_BACKWARD_HISTORY);

	/** The global next. */
	final Selector globalNext = e -> runCommand(NAVIGATE_FORWARD_HISTORY);

	/**
	 * Instantiates a new editor toolbar.
	 *
	 * @param editor
	 *            the editor
	 */
	public EditorToolbar(final GamlEditor editor) {
		this.editor = editor;
	}

	/**
	 * Fill.
	 *
	 * @param toolbar
	 *            the toolbar
	 */
	public void fill(final GamaToolbarSimple toolbar) {

		previous = toolbar.button("editor/command.lastedit", null, "Previous edit location", globalPrevious);
		next = toolbar.button("editor/command.nextedit", null, "Next edit location", globalNext);

		toolbar.button("editor/search", null,
				"Find/Replace (" + GamaKeyBindings.format(SWT.MOD1, 'F') + ")", e -> editor.doSearch());

		toolbar.button("editor/local.menu", "Editor options", "Editor options", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					addPresentationItems(mainMenu, -1);
					// addManagementItems(mainMenu, -1);
					separate();
					if (GamaBundleLoader.isDiagramEditorLoaded()) {
						GamaCommand.build("editor/command.graphical", "Show diagram", "Switch to diagram",
								e -> { editor.switchToDiagram(); }).toItem(mainMenu);
					}
					final IViewPart part = WorkbenchHelper.getPage().findView(IGui.OUTLINE_VIEW_ID);
					String title = part == null ? "Show outline" : "Hide outline";
					Selector command = part == null ? e -> showView(IGui.OUTLINE_VIEW_ID)
							: e -> WorkbenchHelper.getPage().hideView(part);
					GamaCommand.build("editor/command.outline", title, null, command).toItem(mainMenu);

					final IViewPart map = WorkbenchHelper.getPage().findView(IGui.MINIMAP_VIEW_ID);
					title = map == null ? "Show minimap" : "Hide minimap";
					command = map == null ? e -> showView(IGui.MINIMAP_VIEW_ID)
							: e -> WorkbenchHelper.getPage().hideView(map);
					GamaCommand.build("editor/command.navigation", title, null, command).toItem(mainMenu);
					separate();
					final Menu edition = sub(mainMenu, "Edition", null, "editor/menu.edition");
					check(edition, "Surround selection using { [ ( \\", GamaPreferences.Modeling.CORE_SURROUND_SELECTED,
							null);
					check(edition, "Close quotes '...' ", GamaPreferences.Modeling.CORE_CLOSE_QUOTE, null);
					check(edition, "Close double quotes \"...\" ", GamaPreferences.Modeling.CORE_CLOSE_DOUBLE, null);
					check(edition, "Close brackets [...] ", GamaPreferences.Modeling.CORE_CLOSE_SQUARE, null);
					check(edition, "Close curly brackets {...} ", GamaPreferences.Modeling.CORE_CLOSE_CURLY, null);
					check(edition, "Close parentheses (...) ", GamaPreferences.Modeling.CORE_CLOSE_PARENTHESES, null);
					separate();
					check("Formatting on save", GamaPreferences.Modeling.EDITOR_CLEAN_UP)
							.setImage(GamaIcon.named("editor/command.format").image());
					separate();
					MenuItem automatic = check(mainMenu, "Automatic validation", editor.isAutomaticValidationEnabled(),
							(Selector) e -> editor.setAutomaticValidation(((MenuItem) e.widget).getSelection()));
					automatic.setEnabled(editor.getResource() instanceof IFile);
					GamaMenu.action(mainMenu, "Validate now", e -> editor.validateNow(), null)
							.setEnabled(!editor.isAutomaticValidationEnabled());
					final Command export = getCommand("gama.ui.export.model");
					if (export != null && export.isDefined()) {
						separate();
						final var parameters = new CommandContributionItemParameter(editor.getSite(), null,
								export.getId(), CommandContributionItem.STYLE_PUSH);
						parameters.label = "Export as playable model...";
						parameters.icon = GamaIcon.named("navigator/menu.export").descriptor();
						final var contribution = new CommandContributionItem(parameters);
						contribution.fill(mainMenu, -1);
						mainMenu.addDisposeListener(event -> contribution.dispose());
					}
				}

			};
			menu.open(toolbar, e, toolbar.getSize().y, 100);
		});

		hookToCommands(previous, next);
	}

	/**
	 * Show view.
	 *
	 * @param id
	 *            the id
	 */
	private void showView(final String id) {
		try {
			WorkbenchHelper.getPage().showView(id);
		} catch (PartInitException e) {}
	}

	/**
	 * Fill.
	 *
	 * @param m
	 *            the m
	 * @param index
	 *            the index
	 */
	public void addPresentationItems(final Menu m, final int index) {

		MenuItem menuItem = new MenuItem(m, SWT.CASCADE);
		menuItem.setText("Presentation");
		menuItem.setImage(GamaIcon.named(IGamaIcons.PRESENTATION_MENU).image());
		final Menu menu = new Menu(menuItem);
		if (menuItem.getMenu() != null) { menuItem.getMenu().dispose(); }
		menuItem.setMenu(menu);
		menu.addListener(SWT.Show, e -> {
			for (final MenuItem item : menu.getItems()) { item.dispose(); }
			GamaCommand.build("display/zoom.in", "Zoom In ", "Increases the size of the font in this editor", event -> {
				editor.zoomIn();
			}).toItem(menu).setAccelerator(SWT.MOD1 | '+');
			GamaCommand.build("display/zoom.fit", "Zoom Reset ",
					"Resets the size of the font to its default in the preferences", event -> {
						editor.zoomFit();
					}).toItem(menu).setAccelerator(SWT.MOD1 | '=');
			GamaCommand
					.build("display/zoom.out", "Zoom Out ", "Decreases the size of the font in this editor", event -> {
						editor.zoomOut();
					}).toItem(menu).setAccelerator(SWT.MOD1 | '-');
			GamaMenu.separate(menu);
			GamaCommand.build("views/layout.vertical", "Split Vertically ", "", event -> {
				WorkbenchHelper.runCommand("org.eclipse.ui.window.splitEditor",
						Map.of("Splitter.isHorizontal", "true"));
			}).toItem(menu);
			GamaCommand.build("views/layout.horizontal", "Split Horizontally ", "", event -> {
				WorkbenchHelper.runCommand("org.eclipse.ui.window.splitEditor",
						Map.of("Splitter.isHorizontal", "false"));
			}).toItem(menu);
			GamaCommand.build("views/layout.stack", "Clone ", "", event -> {
				WorkbenchHelper.runCommand("org.eclipse.ui.window.newEditor");
			}).toItem(menu);
			GamaMenu.separate(menu);
			createLineToggle(menu);
			createFoldingToggle(menu);
			createOverviewToggle(menu);
			createWordWrapToggle(menu);
			GamaMenu.check(menu, "Inline markers", GamaPreferences.Modeling.EDITOR_MINING.getValue(),
					event -> GamaPreferences.Modeling.EDITOR_MINING
							.set(!GamaPreferences.Modeling.EDITOR_MINING.getValue()),
					"editor/toggle.mark").setToolTipText(GamaPreferences.Modeling.EDITOR_MINING.getTitle());
			GamaMenu.separate(menu);
			GamaMenu.check(menu, "Use a menu for experiments", GamaPreferences.Modeling.EDITOR_EXPERIMENT_MENU,
					e1 -> editor.updateToolbar()).setImage(GamaIcon.named(IGamaIcons.OTHER_EXPERIMENTS).image());

		});
	}

	/**
	 * Hook to commands.
	 *
	 * @param lastEdit
	 *            the last edit
	 * @param nextEdit
	 *            the next edit
	 */
	private void hookToCommands(final ToolItem lastEdit, final ToolItem nextEdit) {
		WorkbenchHelper.runInUI("Hooking to commands", 0, m -> {
			final Command nextCommand = getCommand(NAVIGATE_FORWARD_HISTORY);
			if (!nextEdit.isDisposed()) {
				nextEdit.setEnabled(nextCommand.isEnabled());
				final ICommandListener nextListener = e -> nextEdit.setEnabled(nextCommand.isEnabled());
				nextCommand.addCommandListener(nextListener);
				nextEdit.addDisposeListener(e -> nextCommand.removeCommandListener(nextListener));
			}
			final Command lastCommand = getCommand(NAVIGATE_BACKWARD_HISTORY);
			if (!lastEdit.isDisposed()) {
				final ICommandListener lastListener = e -> lastEdit.setEnabled(lastCommand.isEnabled());
				lastEdit.setEnabled(lastCommand.isEnabled());
				lastCommand.addCommandListener(lastListener);
				lastEdit.addDisposeListener(e -> lastCommand.removeCommandListener(lastListener));
			}
			// Attaching dispose listeners to the toolItems so that they remove the
			// command listeners properly

		});

	}

	/**
	 * Creates the mark toggle.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param menu
	 *            the menu
	 * @date 26 juin 2023
	 */
	public void createMarkToggle(final Menu menu) {
		final MenuItem mark = new MenuItem(menu, SWT.PUSH);
		boolean selected = GamaPreferences.Modeling.EDITOR_MARK_OCCURRENCES.getValue();
		mark.setText(selected ? " Do not mark symbols occurences" : " Mark occurences of symbols");
		// mark.setSelection(markPref.getValue());
		mark.setImage(GamaIcon.named("editor/toggle.mark").image());

		mark.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				GamaPreferences.Modeling.EDITOR_MARK_OCCURRENCES.set(mark.getSelection()).save();
			}
		});

	}

	/**
	 * Creates the mark toggle.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param menu
	 *            the menu
	 * @date 26 juin 2023
	 */
	public void createWordWrapToggle(final Menu menu) {
		final MenuItem mark = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isWordWrapEnabled();
		mark.setText(selected ? " Turn Word Wrap off" : " Turn Word Wrap on");
		mark.setImage(GamaIcon.named("editor/word.wrap").image());

		mark.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().setWordWrap(mark.getSelection());
			}
		});

	}

	/**
	 * Creates the overview toggle.
	 *
	 * @param menu
	 *            the menu
	 */
	public void createOverviewToggle(final Menu menu) {
		final MenuItem overview = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isOverviewRulerVisible();
		overview.setText(selected ? " Hide markers overview" : " Show markers overview");
		// overview.setSelection(selected);
		overview.setImage(GamaIcon.named("editor/toggle.overview").image());
		overview.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final boolean shown = getEditor().isOverviewRulerVisible();
				if (shown) {
					getEditor().hideOverviewRuler();
				} else {
					getEditor().showOverviewRuler();
				}
			}
		});

	}

	/**
	 *
	 */
	public void createFoldingToggle(final Menu menu) {
		final MenuItem folding = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isRangeIndicatorEnabled();
		folding.setText(selected ? " Unfold code sections" : " Fold code sections");

		folding.setSelection(selected);
		folding.setImage(GamaIcon.named("editor/toggle.folding").image());
		folding.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().getAction("FoldingToggle").run();
			}
		});

	}

	/**
	 *
	 */
	public void createLineToggle(final Menu menu) {
		final MenuItem line = new MenuItem(menu, SWT.PUSH);
		boolean selected = getEditor().isLineNumberRulerVisible();
		line.setText(selected ? " Hide line numbers" : " Display line numbers");

		// line.setSelection(selected);
		line.setImage(GamaIcon.named("editor/toggle.numbers").image());
		line.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				getEditor().getAction(ITextEditorActionConstants.LINENUMBERS_TOGGLE).run();
			}
		});

	}

	/**
	 * @return
	 */
	private GamlEditor getEditor() { return editor; }

}
