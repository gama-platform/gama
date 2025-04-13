/*******************************************************************************************************
 *
 * EditorToolbar.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.toolbar;

import static gama.ui.shared.utils.WorkbenchHelper.getCommand;
import static gama.ui.shared.utils.WorkbenchHelper.runCommand;
import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY;
import static org.eclipse.ui.IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import gama.core.common.preferences.GamaPreferences;
import gama.core.common.preferences.Pref;
import gama.core.runtime.GAMA;
import gama.core.runtime.PlatformHelper;
import gama.dev.DEBUG;
import gama.gaml.compilation.kernel.GamaBundleLoader;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.bindings.GamaKeyBindings.PluggableBinding;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gama.ui.shared.views.toolbar.Selector;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
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
		DEBUG.ON();
		Command command = WorkbenchHelper.getCommand("org.eclipse.ui.edit.text.zoomOut");
		if (command != null) { command.setHandler(null); }
		command = WorkbenchHelper.getCommand("org.eclipse.ui.edit.text.zoomIn");
		if (command != null) { command.setHandler(null); }
		command = WorkbenchHelper.getCommand("org.eclipse.ui.edit.text.zoomOut");
		if (command != null) { command.setHandler(null); }
		if (PlatformHelper.isMac()) {
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

		GamaKeyBindings.plug(new PluggableBinding(SWT.MOD1, 'g') {

			@Override
			public void run() {
				IEditorPart part = WorkbenchHelper.getActiveEditor();
				if (part instanceof GamlEditor ge) { ge.doSearch(); }
			}
		});

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

	/** The find. */
	EditorSearchControls find;

	/** The editor. */
	final GamlEditor editor;

	/** The mark pref. */
	public Pref<Boolean> markPref;

	/** The searching. */
	volatile boolean searching;

	/** The global previous. */
	final Selector globalPrevious = e -> runCommand(NAVIGATE_BACKWARD_HISTORY);

	/** The global next. */
	final Selector globalNext = e -> runCommand(NAVIGATE_FORWARD_HISTORY);

	/** The search previous. */
	final Selector searchPrevious = e -> find.findPrevious();

	/** The search next. */
	final Selector searchNext = e -> find.findNext();

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
	 * @return the editor search controls
	 */
	public EditorSearchControls fill(final GamaToolbarSimple toolbar) {

		previous = toolbar.button("editor/command.lastedit", null, "Previous edit location", globalPrevious);

		find = new EditorSearchControls(editor).fill(toolbar);
		next = toolbar.button("editor/command.nextedit", null, "Next edit location", globalNext);
		toolbar.button("editor/command.outline", null, "Show outline", e -> { editor.openOutlinePopup(); });
		if (GamaBundleLoader.isDiagramEditorLoaded()) {
			toolbar.button("editor/command.graphical", null, "Switch to diagram", e -> { editor.switchToDiagram(); });
		}
		toolbar.button(IGamaIcons.IMPORTED_IN, "Imported in...", "List the files in which this model is imported",
				e -> {
					final GamaMenu menu = new GamaMenu() {

						@Override
						protected void fillMenu() {
							for (final MenuItem item : mainMenu.getItems()) { item.dispose(); }
							createImportedSubMenu(mainMenu);
						}

					};
					menu.open(toolbar, e, toolbar.getSize().y, 200);
				});

		toolbar.button("editor/local.menu", "Presentation preferences", "Presentation preferences", e -> {

			final GamaMenu menu = new GamaMenu() {

				@Override
				protected void fillMenu() {
					GamaCommand.build("display/zoom.in", "Zoom In ", "Increases the size of the font in this editor",
							event -> {
								editor.zoomIn();
							}).toItem(mainMenu).setAccelerator(SWT.MOD1 | '+');
					GamaCommand.build("display/zoom.fit", "Zoom Reset ",
							"Resets the size of the font to its default in the preferences", e -> {
								editor.zoomFit();
							}).toItem(mainMenu).setAccelerator(SWT.MOD1 | '=');
					GamaCommand.build("display/zoom.out", "Zoom Out ", "Decreases the size of the font in this editor",
							event -> {
								editor.zoomOut();
							}).toItem(mainMenu).setAccelerator(SWT.MOD1 | '-');
					GamaMenu.separate(mainMenu);
					addPresentationItems(mainMenu, -1);
					addManagementItems(mainMenu, -1);
				}

			};
			menu.open(toolbar, e, toolbar.getSize().y, 200);
		});

		hookToCommands(previous, next);
		hookToSearch(previous, next);

		return find;
	}

	/**
	 * Creates the imported sub menu.
	 *
	 * @param parentMenu
	 *            the parent menu
	 * @param editor
	 *            the editor
	 * @return the menu
	 */
	public void createImportedSubMenu(final Menu parentMenu) {
		final Set<URI> importers = new HashSet<>();
		editor.getDocument().readOnly(new IUnitOfWork.Void<XtextResource>() {

			@Override
			public void process(final XtextResource resource) throws Exception {
				importers.addAll(GamlResourceIndexer.directImportersOf(resource.getURI()));
			}
		});
		if (importers.isEmpty()) {
			final MenuItem nothing = new MenuItem(parentMenu, SWT.PUSH);
			nothing.setText("No importers");
			nothing.setEnabled(false);
		} else {
			for (final URI uri : importers) {
				final MenuItem modelItem = new MenuItem(parentMenu, SWT.PUSH);
				modelItem.setText(URI.decode(uri.lastSegment()));
				modelItem.setImage(GamaIcon.named(IGamaIcons.FILE_ICON).image());
				modelItem.setData("uri", uri);
				modelItem.addSelectionListener(UsedInAdapter);
			}
		}
	}

	/**
	 * Gets the importers.
	 *
	 * @param editor
	 *            the editor
	 * @return the importers
	 */
	private Set<URI> getImporters() { return new HashSet<>(); }

	/** The Constant UsedInAdapter. */
	private static final SelectionAdapter UsedInAdapter = new SelectionAdapter() {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final MenuItem mi = (MenuItem) e.widget;
			final URI uri = (URI) mi.getData("uri");
			GAMA.getGui().editModel(uri);
		}
	};

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
			markPref = GamaPreferences.get("pref_editor_mark_occurrences", Boolean.class);
			for (final MenuItem item : menu.getItems()) { item.dispose(); }
			createLineToggle(menu);
			createFoldingToggle(menu);
			createMarkToggle(menu);
			createOverviewToggle(menu);
			createWordWrapToggle(menu);
		});
	}

	/**
	 * Adds the management items.
	 *
	 * @param m
	 *            the m
	 * @param index
	 *            the index
	 */
	public void addManagementItems(final Menu m, final int index) {

		MenuItem menuItem = new MenuItem(m, SWT.CASCADE);
		menuItem.setText("Management");
		menuItem.setImage(GamaIcon.named("views/layout.menu").image());
		Menu menu2 = new Menu(menuItem);
		if (menuItem.getMenu() != null) { menuItem.getMenu().dispose(); }
		menuItem.setMenu(menu2);
		menu2.addListener(SWT.Show, e -> {
			for (final MenuItem item : menu2.getItems()) { item.dispose(); }
			GamaCommand.build("views/layout.vertical", "Split Vertically ", "", event -> {
				WorkbenchHelper.runCommand("org.eclipse.ui.window.splitEditor",
						Map.of("Splitter.isHorizontal", "true"));
			}).toItem(menu2);
			GamaCommand.build("views/layout.horizontal", "Split Horizontally ", "", event -> {
				WorkbenchHelper.runCommand("org.eclipse.ui.window.splitEditor",
						Map.of("Splitter.isHorizontal", "false"));
			}).toItem(menu2);
			GamaCommand.build("views/layout.stack", "Clone ", "", event -> {
				WorkbenchHelper.runCommand("org.eclipse.ui.window.newEditor");
			}).toItem(menu2);
		});
	}

	/**
	 * Hook to search.
	 *
	 * @param lastEdit
	 *            the last edit
	 * @param nextEdit
	 *            the next edit
	 */
	private void hookToSearch(final ToolItem lastEdit, final ToolItem nextEdit) {
		find.getFindControl().addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
				searching = true;
				previous.removeSelectionListener(globalPrevious);
				previous.setToolTipText("Search previous occurence");
				next.removeSelectionListener(globalNext);
				next.setToolTipText("Search next occurence " + GamaKeyBindings.format(SWT.MOD1, 'G'));
				previous.addSelectionListener(searchPrevious);
				next.addSelectionListener(searchNext);
				previous.setEnabled(true);
				next.setEnabled(true);
			}

			@Override
			public void focusLost(final FocusEvent e) {
				searching = false;
				previous.removeSelectionListener(searchPrevious);
				previous.setToolTipText("Previous edit location");
				next.removeSelectionListener(searchNext);
				next.setToolTipText("Next edit location");
				previous.addSelectionListener(globalPrevious);
				next.addSelectionListener(globalNext);
				previous.setEnabled(getCommand(NAVIGATE_BACKWARD_HISTORY).isEnabled());
				next.setEnabled(getCommand(NAVIGATE_FORWARD_HISTORY).isEnabled());
			}
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
				final ICommandListener nextListener = e -> nextEdit.setEnabled(searching || nextCommand.isEnabled());
				nextCommand.addCommandListener(nextListener);
				nextEdit.addDisposeListener(e -> nextCommand.removeCommandListener(nextListener));
			}
			final Command lastCommand = getCommand(NAVIGATE_BACKWARD_HISTORY);
			if (!lastEdit.isDisposed()) {
				final ICommandListener lastListener = e -> lastEdit.setEnabled(searching || lastCommand.isEnabled());
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
		boolean selected = markPref.getValue();
		mark.setText(selected ? " Do not mark symbols occurences" : " Mark occurences of symbols");
		// mark.setSelection(markPref.getValue());
		mark.setImage(GamaIcon.named("editor/toggle.mark").image());

		mark.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				markPref.set(mark.getSelection()).save();
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
