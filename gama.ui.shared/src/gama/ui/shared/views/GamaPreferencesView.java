/*******************************************************************************************************
 *
 * GamaPreferencesView.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;

import gama.api.GAMA;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.Pref;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.controls.ParameterExpandBar;
import gama.ui.shared.controls.ParameterExpandItem;
import gama.ui.shared.interfaces.IParameterEditor;
import gama.ui.shared.parameters.AbstractEditor;
import gama.ui.shared.parameters.EditorFactory;
import gama.ui.shared.parameters.EditorsGroup;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.SyntaxHighlightingPreferences;
import gama.ui.shared.utils.SyntaxHighlightingPreferences.Configuration;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.Selector;

/**
 * Class GamaPreferencesView.
 *
 * @author drogoul
 * @since 31 août 2013
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPreferencesView {

	static {
		DEBUG.OFF();
	}

	/** The dialog location. */
	static final Pref<IPoint> DIALOG_LOCATION =
			GamaPreferences.create("dialog_location", "Location of the preferences dialog on screen",
					GamaPointFactory.create(-1, -1), IType.POINT, false).hidden();

	/** The dialog size. */
	static final Pref<IPoint> DIALOG_SIZE = GamaPreferences.create("dialog_size",
			"Size of the preferences dialog on screen", GamaPointFactory.create(-1, -1), IType.POINT, false).hidden();

	/** The dialog tab. */
	static final Pref<Integer> DIALOG_TAB = GamaPreferences
			.create("dialog_tab", "Tab selected in the preferences dialog", -1, IType.INT, false).hidden();

	/** The prefs images. */
	public static final Map<String, Image> prefs_images = new LinkedHashMap();

	/** The nb divisions. */
	public static final int NB_DIVISIONS = 2;

	/** The instance. */
	static GamaPreferencesView instance;

	/** The restart required. */
	static boolean restartRequired;

	/**
	 * Show.
	 */
	public static void show() {
		if (instance == null || instance.shell == null || instance.shell.isDisposed()) {
			instance = new GamaPreferencesView(WorkbenchHelper.getShell());
		}
		if (!instance.preloaded) { preload(); }
		for (final IParameterEditor ed : instance.editors.values()) { ed.updateWithValueOfParameter(true, false); }
		for (final Pref<?> p : GAMA.getPreferenceStore().getPreferences()) {
			instance.checkActivables(p, Cast.asBool(null, p.getValue()));
		}
		instance.resetSyntaxConfiguration();
		instance.open();
	}

	/**
	 * Preload.
	 */
	public static void preload() {
		DEBUG.TIMER(BANNER_CATEGORY.GAMA, "Preloading preferences view", "completed in", () -> {
			WorkbenchHelper.run(() -> {
				if (instance == null || instance.shell == null || instance.shell.isDisposed()) {
					instance = new GamaPreferencesView(WorkbenchHelper.getShell());
				}
				for (final IParameterEditor ed : instance.editors.values()) {
					ed.updateWithValueOfParameter(true, false);
				}
				instance.preloaded = true;
			});

		});

	}

	static {
		prefs_images.put(GamaPreferences.Interface.NAME, GamaIcon.named(IGamaIcons.PREFS_GENERAL).image());
		prefs_images.put(GamaPreferences.Modeling.NAME, GamaIcon.named(IGamaIcons.PREFS_EDITOR).image());
		prefs_images.put(GamaPreferences.Experimental.NAME, GamaIcon.named(IGamaIcons.PREFS_EXPERIMENTAL).image());
		prefs_images.put(GamaPreferences.Runtime.NAME, GamaIcon.named(IGamaIcons.PREFS_SIMULATION).image());
		prefs_images.put(GamaPreferences.Displays.NAME, GamaIcon.named(IGamaIcons.PREFS_UI).image());
		prefs_images.put(GamaPreferences.External.NAME, GamaIcon.named(IGamaIcons.PREFS_LIBS).image());
		prefs_images.put(GamaPreferences.Theme.NAME, GamaIcon.named(IGamaIcons.PREFS_THEME).image());
		prefs_images.put(GamaPreferences.Network.NAME, GamaIcon.named(IGamaIcons.PREFS_NETWORK).image());
	}

	/** The shell. */
	Shell parentShell, shell;

	/** The tab folder. */
	CTabFolder tabFolder;

	/** The editors. */
	final Map<String, IParameterEditor> editors = new LinkedHashMap();

	/** The model values. */
	final Map<String, Object> modelValues = new LinkedHashMap();

	private final Map<String, Button> syntaxStyleButtons = new LinkedHashMap<>();
	private Combo syntaxConfigurations;

	/**
	 * Instantiates a new gama preferences view.
	 *
	 * @param parent
	 *            the parent
	 */
	private GamaPreferencesView(final Shell parent) {
		parentShell = parent;
		shell = new Shell(parentShell, SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5).spacing(5, 5).applyTo(shell);
		tabFolder = new CTabFolder(shell, SWT.TOP | SWT.NO_TRIM);
		tabFolder.setBorderVisible(true);
		tabFolder.setMRUVisible(true);
		tabFolder.setSimple(false); // rounded tabs
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(tabFolder);
		final var prefs = GamaPreferences.organizePrefs();
		for (final String tabName : prefs.keySet()) {
			final var item = new CTabItem(tabFolder, SWT.NONE);
			item.setText(tabName);
			item.setImage(prefs_images.get(tabName));
			item.setShowClose(false);
			buildContentsFor(item, prefs.get(tabName));
		}
		buildButtons();
		shell.layout();
	}

	/**
	 * Builds the contents for.
	 *
	 * @param tab
	 *            the tab
	 * @param entries
	 *            the entries
	 */
	private void buildContentsFor(final CTabItem tab, final Map<String, List<Pref<?>>> entries) {
		final var viewer = new ParameterExpandBar(tab.getParent(), SWT.V_SCROLL);
		viewer.setBackground(
				!ThemeHelper.isDark() ? IGamaColors.VERY_LIGHT_GRAY.color() : IGamaColors.DARK_GRAY.darker());
		viewer.setSpacing(10);
		tab.setControl(viewer);

		for (final String groupName : entries.keySet()) {
			final var item = new ParameterExpandItem(viewer, entries.get(groupName), SWT.NONE, null);
			item.setText(groupName);
			final var compo = new Composite(viewer, SWT.NONE);
			item.setControl(compo);
			// Build the contents *after* setting the control to the item.
			buildGroupContents(compo, entries.get(groupName));
			item.setHeight(compo.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			item.setExpanded(true);
		}

	}

	/** The activations. */
	final Map<String, Boolean> activations = new HashMap();

	/**
	 * Check activables.
	 *
	 * @param e
	 *            the e
	 * @param value
	 *            the value
	 */
	void checkActivables(final Pref e, final Boolean value) {
		final boolean val = Boolean.TRUE.equals(value);
		for (final String activable : e.getEnablement()) {
			final var ed = editors.get(activable);
			if (ed == null) {
				activations.put(activable, val);
			} else if (WorkbenchHelper.isDisplayThread()) { ed.setActive(val); }
		}
		for (final String deactivable : e.getDisablement()) {
			final var ed = editors.get(deactivable);
			if (ed == null) {
				activations.put(deactivable, !val);
			} else if (WorkbenchHelper.isDisplayThread()) { ed.setActive(!val); }
		}
	}

	/**
	 * Check refreshables.
	 *
	 * @param e
	 *            the e
	 * @param value
	 *            the value
	 */
	void checkRefreshables(final Pref e) {
		if (!WorkbenchHelper.isDisplayThread()) return;
		for (final String activable : e.getRefreshment()) {
			final var ed = editors.get(activable);
			if (ed != null && WorkbenchHelper.isDisplayThread()) { ed.updateWithValueOfParameter(false, false); }
		}
	}

	/**
	 * Builds the group contents.
	 *
	 * @param compo
	 *            the compo
	 * @param list
	 *            the list
	 */
	private void buildGroupContents(final Composite compo, final List<Pref<?>> list) {
		final String group = list.getFirst().getGroup();
		if (SyntaxHighlightingPreferences.CONFIGURATION_GROUP.equals(group)) {
			buildSyntaxConfigurationControls(compo);
			return;
		}
		if (SyntaxHighlightingPreferences.isThemeGroup(group)) {
			buildSyntaxStyleControls(compo, list);
			return;
		}
		GridLayoutFactory.fillDefaults().numColumns(NB_DIVISIONS).spacing(5, 0).equalWidth(true).applyTo(compo);
		final var comps = new EditorsGroup[NB_DIVISIONS];
		for (var i = 0; i < NB_DIVISIONS; i++) { comps[i] = new EditorsGroup(compo, SWT.NONE); }
		var i = 0;
		for (final Pref e : list) {
			modelValues.put(e.getKey(), e.getValue());
			// Initial activations of editors
			checkActivables(e, Cast.asBool(null, e.getValue()));
			e.onChange(value -> {
				if (e.acceptChange(value)) {
					modelValues.put(e.getKey(), value);
					Boolean b = Cast.asBool(GAMA.getPlatformAgent().getScope(), value);
					checkActivables(e, b);
					checkRefreshables(e);
					if (e.isRestartRequired()) { setRestartRequired(); }
				} else {
					GamaPreferencesView.this.showError("" + value + " is not accepted for parameter " + e.getKey());
				}

			});
			final boolean isSubParameter = activations.containsKey(e.getKey());
			final AbstractEditor ed = EditorFactory.create(GAMA.getPlatformAgent().getScope(),
					comps[(int) (i * ((double) NB_DIVISIONS / list.size()))], e, isSubParameter);
			if (e.isDisabled()) {
				ed.setActive(false);
			} else {
				final var m = getMenuFor(e, ed);
				final var l = ed.getLabel();
				l.setMenu(m);
			}
			editors.put(e.getKey(), ed);
			i++;
		}

		// Initial activations of editors
		for (final String s : activations.keySet()) {
			final var ed = editors.get(s);
			if (ed != null) { ed.setActive(activations.get(s)); }
		}
		activations.clear();
		compo.layout();
		compo.pack(true);
	}

	private void buildSyntaxConfigurationControls(final Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(4).margins(5, 5).applyTo(parent);
		new Label(parent, SWT.NONE).setText("Configuration");
		syntaxConfigurations = new Combo(parent, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(syntaxConfigurations);
		syntaxConfigurations.addListener(SWT.Selection, event -> {
			final String name = syntaxConfigurations.getText();
			if (SyntaxHighlightingPreferences.BUILT_INS.contains(name)) {
				SyntaxHighlightingPreferences.stage(SyntaxHighlightingPreferences.builtIn(name), modelValues);
				refreshSyntaxControls();
			}
		});
		final Button load = new Button(parent, SWT.PUSH);
		load.setText("Load...");
		load.addListener(SWT.Selection, event -> {
			final FileDialog dialog = syntaxFileDialog(SWT.OPEN);
			final String file = dialog.open();
			if (file == null) return;
			try {
				final Configuration configuration = SyntaxHighlightingPreferences.load(Path.of(file));
				SyntaxHighlightingPreferences.stage(configuration, modelValues);
				refreshSyntaxControls();
			} catch (final IOException | IllegalArgumentException e) {
				MessageDialog.openError(shell, "Cannot load syntax configuration", e.getMessage());
			}
		});
		final Button save = new Button(parent, SWT.PUSH);
		save.setText("Save as...");
		save.addListener(SWT.Selection, event -> {
			final FileDialog dialog = syntaxFileDialog(SWT.SAVE);
			dialog.setFileName("My syntax.gama-syntax");
			dialog.setOverwrite(true);
			final String file = dialog.open();
			if (file == null) return;
			final Path path = Path.of(file);
			final String name = path.getFileName().toString().replaceFirst("\\.gama-syntax$", "");
			final Configuration configuration =
					new Configuration(name, SyntaxHighlightingPreferences.staged(modelValues).themes());
			try {
				SyntaxHighlightingPreferences.save(path, configuration);
				modelValues.put(SyntaxHighlightingPreferences.CONFIGURATION_KEY, name);
				refreshSyntaxControls();
			} catch (final IOException e) {
				MessageDialog.openError(shell, "Cannot save syntax configuration", e.getMessage());
			}
		});
	}

	private FileDialog syntaxFileDialog(final int style) {
		final FileDialog dialog = new FileDialog(shell, style);
		dialog.setFilterExtensions(new String[] { "*.gama-syntax", "*.properties", "*.*" });
		dialog.setFilterNames(new String[] { "GAMA syntax configuration", "Properties files", "All files" });
		return dialog;
	}

	private void buildSyntaxStyleControls(final Composite parent, final List<Pref<?>> preferences) {
		GridLayoutFactory.fillDefaults().numColumns(NB_DIVISIONS).spacing(5, 0).equalWidth(true).applyTo(parent);
		final EditorsGroup[] columns = new EditorsGroup[NB_DIVISIONS];
		for (int i = 0; i < columns.length; i++) { columns[i] = new EditorsGroup(parent); }
		for (int i = 0; i < preferences.size(); i++) {
			final Pref<?> preference = preferences.get(i);
			final String colorKey = preference.getKey();
			final String theme = colorKey.endsWith("_light") ? "light" : "dark";
			final String id = colorKey.substring("pref_".length(), colorKey.length() - ("_color_" + theme).length());
			final AbstractEditor<?> editor = EditorFactory.create(GAMA.getPlatformAgent().getScope(),
					columns[i * NB_DIVISIONS / preferences.size()], preference, value -> {
						modelValues.put(colorKey, value);
						markSyntaxCustom();
					}, false);
			editors.put(colorKey, editor);
			final Composite row = editor.getEditor().getParent();
			((GridLayout) row.getLayout()).numColumns = 3;
			for (final String property : List.of("bold", "italic")) {
				final String key = SyntaxHighlightingPreferences.key(id, property, theme);
				final Button button = new Button(row, SWT.CHECK);
				button.setText("bold".equals(property) ? "Bold" : "Italic");
				button.addListener(SWT.Selection, event -> {
					modelValues.put(key, button.getSelection());
					markSyntaxCustom();
				});
				syntaxStyleButtons.put(key, button);
			}
			final Menu menu = editor.getLabel().createMenu();
			final MenuItem revert = new MenuItem(menu, SWT.PUSH);
			revert.setText("Restore GAMA 2026 style");
			revert.addListener(SWT.Selection, event -> {
				final var style = SyntaxHighlightingPreferences.builtIn("GAMA 2026").themes().get(theme).get(id);
				modelValues.put(colorKey, style.color());
				modelValues.put(SyntaxHighlightingPreferences.key(id, "bold", theme), style.bold());
				modelValues.put(SyntaxHighlightingPreferences.key(id, "italic", theme), style.italic());
				markSyntaxCustom();
				refreshSyntaxControls();
			});
			editor.getLabel().setMenu(menu);
		}
		parent.layout();
		parent.pack(true);
	}

	private void markSyntaxCustom() {
		final Configuration configuration = SyntaxHighlightingPreferences.staged(modelValues);
		SyntaxHighlightingPreferences.stage(new Configuration("Custom", configuration.themes()), modelValues);
		refreshSyntaxConfigurationName();
	}

	private void resetSyntaxConfiguration() {
		if (syntaxConfigurations == null) return;
		SyntaxHighlightingPreferences.stage(SyntaxHighlightingPreferences.current(), modelValues);
		refreshSyntaxControls();
	}

	private void refreshSyntaxControls() {
		for (final String theme : SyntaxHighlightingPreferences.THEMES) {
			for (final String id : SyntaxHighlightingPreferences.categories().keySet()) {
				final String key = SyntaxHighlightingPreferences.key(id, "color", theme);
				final IParameterEditor editor = editors.get(key);
				if (editor != null) editor.displayValue(modelValues.get(key));
			}
		}
		syntaxStyleButtons.forEach((key, button) -> button.setSelection(Boolean.TRUE.equals(modelValues.get(key))));
		refreshSyntaxConfigurationName();
	}

	private void refreshSyntaxConfigurationName() {
		if (syntaxConfigurations == null) return;
		final Configuration configuration = SyntaxHighlightingPreferences.staged(modelValues);
		String name = configuration.name();
		for (final String builtIn : SyntaxHighlightingPreferences.BUILT_INS) {
			if (configuration.themes().equals(SyntaxHighlightingPreferences.builtIn(builtIn).themes())) {
				name = builtIn;
				break;
			}
			if (builtIn.equals(name)) name = "Custom";
		}
		syntaxConfigurations.setItems(SyntaxHighlightingPreferences.BUILT_INS.toArray(String[]::new));
		if (!SyntaxHighlightingPreferences.BUILT_INS.contains(name)) syntaxConfigurations.add(name);
		syntaxConfigurations.setText(name);
	}

	/**
	 * Gets the menu for.
	 *
	 * @param e
	 *            the e
	 * @param ed
	 *            the ed
	 * @return the menu for
	 */
	private static Menu getMenuFor(final Pref e, final AbstractEditor ed) {
		final var m = ed.getLabel().createMenu();
		final var title = new MenuItem(m, SWT.PUSH);
		title.setEnabled(false);

		if (e.inGaml()) {
			title.setText("Use gama." + e.getKey() + " in GAML");
			@SuppressWarnings ("unused") final var sep = new MenuItem(m, SWT.SEPARATOR);
			final var i = new MenuItem(m, SWT.PUSH);
			i.setText("Copy name to clipboard");
			i.addSelectionListener((Selector) se -> WorkbenchHelper.copy("gama." + e.getKey()));
		} else {
			title.setText("Not assignable from GAML");
			@SuppressWarnings ("unused") final var sep = new MenuItem(m, SWT.SEPARATOR);
		}
		final var i2 = new MenuItem(m, SWT.PUSH);
		i2.setText("Revert to default value");
		i2.addSelectionListener((Selector) se -> {
			e.set(e.getDefaultValue());
			ed.updateWithValueOfParameter(true, false);
		});
		return m;
	}

	/**
	 * @param string
	 */
	protected void showError(final String string) {
		// TODO make it a proper component of the view
		DEBUG.LOG("Error in preferences : " + string);
	}

	/**
	 * Builds the buttons.
	 */
	private void buildButtons() {
		final var doc = new Label(shell, SWT.WRAP);
		GridData docData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		docData.heightHint = 50;
		doc.setLayoutData(docData);
		// doc.setFont(GamaFonts.boldHelpFont);
		doc.setText(
				"Some preferences can also be set in GAML, using 'gama.pref_name <- new_value;'. 'pref_name' is displayed in the contextual menu of each preference");

		final var group1 = new Composite(shell, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.fill = true;
		rowLayout.center = true;
		rowLayout.spacing = 10;
		group1.setLayout(rowLayout);
		final var gridDataGroup1 = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		group1.setLayoutData(gridDataGroup1);

		final var buttonRevert = FlatButton.button(group1, IGamaColors.ERROR, "Revert to defaults");
		buttonRevert.setToolTipText("Restore default values for all preferences");

		final var buttonAdvanced = FlatButton.button(group1, IGamaColors.DARK_ORANGE, "Advanced...");
		buttonAdvanced.setToolTipText("Access to advanced preferences");

		final var buttonImport = FlatButton.button(group1, IGamaColors.DARK_ORANGE, "Import...");
		buttonImport.setToolTipText("Import preferences from a file...");
		buttonImport.setSelectionListener(e -> {
			final var fd = new FileDialog(shell, SWT.OPEN);
			fd.setFilterExtensions("*.prefs");
			final var path = fd.open();
			if (path == null) return;
			GAMA.getPreferenceStore().applyPreferencesFrom(path, modelValues);
			for (final IParameterEditor ed : editors.values()) { ed.updateWithValueOfParameter(true, false); }
			resetSyntaxConfiguration();
		});

		final var buttonExportToGaml = FlatButton.button(group1, IGamaColors.LIGHT_GRAY, "Export to GAML");
		buttonExportToGaml.setToolTipText("Export preferences to a model that can be run to restore or share them...");
		buttonExportToGaml.setSelectionListener(e -> {
			final var fd = new FileDialog(shell, SWT.SAVE);
			fd.setFileName("__PREFS__.gaml");
			fd.setFilterExtensions("*.gaml");
			fd.setOverwrite(false);
			final var path = fd.open();
			if (path == null) return;
			GAMA.getPreferenceStore().saveToGAML(path);
		});

		final var buttonExport = FlatButton.button(group1, IGamaColors.LIGHT_GRAY, "Export to preferences");
		buttonExport
				.setToolTipText("Export preferences in a format suitable to reimport them in another instance of GAMA");
		buttonExport.setSelectionListener(e -> {
			final var fd = new FileDialog(shell, SWT.SAVE);
			fd.setFileName("gama.prefs");
			fd.setFilterExtensions("*.prefs");
			fd.setOverwrite(false);
			final var path = fd.open();
			if (path == null) return;
			GAMA.getPreferenceStore().saveToProperties(path);
		});

		final var group2 = new Composite(shell, SWT.NONE);
		rowLayout = new RowLayout();
		rowLayout.fill = true;
		rowLayout.center = true;
		// rowLayout.justify = true;
		rowLayout.spacing = 10;
		group2.setLayout(rowLayout);
		final var gridDataGroup2 = new GridData(SWT.END, SWT.END, false, false);
		// gridDataGroup2.widthHint = 200;
		group2.setLayoutData(gridDataGroup2);

		final var buttonCancel = FlatButton.button(group2, IGamaColors.DARK_GRAY, "  Cancel  ");
		buttonCancel.setSelectionListener(e -> close());

		final var buttonOK = FlatButton.button(group2, IGamaColors.OK, "   Save   ");
		buttonOK.setSelectionListener(e -> {
			GamaPreferences.setNewPreferences(modelValues);
			if (restartRequired) {
				restartRequired = false;
				final var restart = GAMA.getGui().getDialogFactory().confirm("Restart GAMA",
						"It is advised to restart GAMA after these changes. Restart now ?");
				if (restart) {
					close();
					PlatformUI.getWorkbench().restart(true);
				}
			} else {
				close();
			}
		});

		shell.addListener(SWT.Traverse, event -> {
			switch (event.detail) {
				case SWT.TRAVERSE_RETURN:
					buttonOK.click(event);
			}
		});

		// this.shell.setDefaultButton(buttonOK);

		buttonRevert.setSelectionListener(e -> {
			if (!GAMA.getGui().getDialogFactory().question("Revert to default",
					"Do you want to revert all preferences to their default values ? A restart of the platform will be performed immediately"))
				return;
			GamaPreferences.revertToDefaultValues(modelValues);
			PlatformUI.getWorkbench().restart(true);
		});

		buttonAdvanced.setSelectionListener(e -> {
			close();
			WorkbenchHelper.asyncRun(() -> {
				final PreferenceDialog pd = WorkbenchPreferenceDialog.createDialogOn(parentShell, null);
				pd.open();
				shell.setVisible(true);
			});

		});

		shell.addDisposeListener(e -> { saveDialogProperties(); });
	}

	/**
	 * Close.
	 */
	void close() {
		shell.setVisible(false);
	}

	/**
	 * Save location.
	 */
	private void saveLocation() {
		final var p = shell.getLocation();
		DIALOG_LOCATION.set(GamaPointFactory.create(p.x, p.y)).save();
	}

	/**
	 * Save size.
	 */
	private void saveSize() {
		final var s = shell.getSize();
		DIALOG_SIZE.set(GamaPointFactory.create(s.x, s.y)).save();
	}

	/**
	 * Save tab.
	 */
	private void saveTab() {
		final var index = tabFolder.getSelectionIndex();
		DIALOG_TAB.set(index).save();
	}

	/**
	 * Save dialog properties.
	 */
	private void saveDialogProperties() {
		if (shell.isDisposed()) return;
		saveLocation();
		saveSize();
		saveTab();
	}

	/** The preloaded. */
	boolean preloaded;

	/**
	 * Open.
	 */
	public void open() {
		final var loc = DIALOG_LOCATION.getValue();
		final var size = DIALOG_SIZE.getValue();
		final int tab = DIALOG_TAB.getValue();
		var x = (int) loc.getX();
		var y = (int) loc.getY();
		var width = (int) size.getX();
		var height = (int) size.getY();
		Rectangle savedBounds = new Rectangle(x, y, width, height);
		Rectangle monitorBounds = WorkbenchHelper.getShell().getMonitor().getBounds();
		Rectangle shellBounds = WorkbenchHelper.getShell().getBounds();
		if (!savedBounds.intersects(monitorBounds)) {
			// final Point p = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			x = shellBounds.x + 100;
			y = shellBounds.y + 100;
			width = shellBounds.width - 200;
			height = shellBounds.height - 200;

		}

		tabFolder.setSelection(tab);
		shell.setLocation(x, y);
		shell.setSize(width, height);
		shell.open();

		while (!this.shell.isDisposed() && this.shell.isVisible()) {
			if (!this.shell.getDisplay().readAndDispatch()) { this.shell.getDisplay().sleep(); }
		}

		saveDialogProperties();

	}

	/**
	 * Sets the restart required.
	 */
	public static void setRestartRequired() {
		restartRequired = true;
	}

}
