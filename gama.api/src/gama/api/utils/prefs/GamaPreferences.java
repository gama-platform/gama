/*******************************************************************************************************
 *
 * GamaPreferences.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

import static gama.api.types.color.GamaColorFactory.LIGHT_GRAY;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.geotools.referencing.CRS;

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ISpeciesDescription.Platform;
import gama.api.constants.Generators;
import gama.api.gaml.GAML;
import gama.api.gaml.types.IType;
import gama.api.kernel.GamaMetaModel;
import gama.api.runtime.SystemInfo;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.file.GenericFile;
import gama.api.types.file.IGamaFile;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.types.map.GamaMapFactory;
import gama.api.ui.layers.ICameraDefinition;
import gama.api.utils.StringUtils;
import gama.api.utils.files.BufferingUtils;
import gama.api.utils.files.FileUtils;
import gama.api.utils.prefs.IPreferenceChangeListener.IPreferenceBeforeChangeListener;

/**
 * {@code GamaPreferences} is the central registry of all GAMA user-configurable preferences. It owns the static
 * factory methods ({@link #create}) used to declare new preferences and the utility methods ({@link #setNewPreferences},
 * {@link #revertToDefaultValues}, {@link #organizePrefs}) used by the UI to display and manipulate them.
 *
 * <p>
 * Preferences are organized into <em>tabs</em> (top-level categories, e.g. {@link Interface}, {@link Runtime},
 * {@link Displays}) and <em>groups</em> within each tab. The canonical display order of tabs is defined by
 * {@link #ORDER_OF_PREFERENCES}.
 * </p>
 *
 * <p>
 * Each preference is represented by a {@link Pref} instance created via one of the {@link #create} factory methods.
 * Upon creation the preference is automatically registered with the global {@link IGamaPreferenceStore} returned by
 * {@link gama.api.GAMA#getPreferenceStore()}, and (if the preference is accessible from GAML) added as a variable to
 * the built-in {@code platform} species.
 * </p>
 *
 * <p>
 * The inner static classes ({@link Interface}, {@link Theme}, {@link Modeling}, {@link Runtime}, {@link Displays},
 * {@link Network}, {@link External}, {@link Experimental}) act as namespaces grouping logically related preferences
 * and their associated tab/group name constants.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 19 août 2023
 */
public class GamaPreferences {

	/**
	 * Triggers the static initialization of all preference sub-namespaces ({@link Interface}, {@link Theme},
	 * {@link Modeling}, {@link Runtime}, {@link Displays}, {@link Network}, {@link External}, {@link Experimental}).
	 * This method must be called once during GAMA startup (before any preference is read) to ensure that every
	 * preference constant has been registered with the global preference store.
	 */
	public static void initialize() {
		inter = new Interface();
		theme = new Theme();
		modeling = new Modeling();
		runtime = new Runtime();
		displays = new Displays();
		network = new Network();
		external = new External();
		exp = new Experimental();
	}

	// To force preferences to load

	/** Holds the singleton instance of the {@link Interface} namespace, forcing its static initializer to run. */
	static Interface inter;

	/** Holds the singleton instance of the {@link Theme} namespace, forcing its static initializer to run. */
	static Theme theme;

	/** Holds the singleton instance of the {@link Modeling} namespace, forcing its static initializer to run. */
	static Modeling modeling;

	/** Holds the singleton instance of the {@link Runtime} namespace, forcing its static initializer to run. */
	static Runtime runtime;

	/** Holds the singleton instance of the {@link Displays} namespace, forcing its static initializer to run. */
	static Displays displays;

	/** Holds the singleton instance of the {@link Network} namespace, forcing its static initializer to run. */
	static Network network;

	/** Holds the singleton instance of the {@link External} namespace, forcing its static initializer to run. */
	static External external;

	/** Holds the singleton instance of the {@link Experimental} namespace, forcing its static initializer to run. */
	static Experimental exp;

	/**
	 * A palette of five semantically neutral "basic" colors used as the default color scheme for distinguishing
	 * multiple simulations in the UI. Each element is a {@link Supplier} so the actual {@link IColor} instance is
	 * lazily created on first access.
	 */
	public static final Supplier<IColor>[] BASIC_COLORS = new Supplier[] { () -> GamaColorFactory.get(74, 97, 144),
			() -> GamaColorFactory.get(66, 119, 42), () -> GamaColorFactory.get(83, 95, 107),
			() -> GamaColorFactory.get(195, 98, 43), () -> GamaColorFactory.get(150, 132, 106) };

	/**
	 * A palette of nine qualitative colors taken from ColorBrewer (Brewer 2003), suitable for distinguishing
	 * categorical data. Each element is a {@link Supplier} so the actual {@link IColor} instance is lazily created on
	 * first access.
	 */
	public static final Supplier<IColor>[] QUALITATIVE_COLORS =
			new Supplier[] { () -> GamaColorFactory.get(166, 206, 227), () -> GamaColorFactory.get(31, 120, 180),
					() -> GamaColorFactory.get(178, 223, 138), () -> GamaColorFactory.get(51, 160, 44),
					() -> GamaColorFactory.get(251, 154, 153), () -> GamaColorFactory.get(227, 26, 28),
					() -> GamaColorFactory.get(253, 191, 111), () -> GamaColorFactory.get(255, 127, 0),
					() -> GamaColorFactory.get(202, 178, 214) };

	/**
	 * A palette of eleven diverging colors taken from ColorBrewer (brown-white-teal), suitable for representing data
	 * that diverges around a central neutral value. Each element is a {@link Supplier} so the actual {@link IColor}
	 * instance is lazily created on first access.
	 */
	public static final Supplier<IColor>[] DIVERGING_COLORS = new Supplier[] { () -> GamaColorFactory.get(84, 48, 5),
			() -> GamaColorFactory.get(140, 81, 10), () -> GamaColorFactory.get(191, 129, 45),
			() -> GamaColorFactory.get(223, 194, 125), () -> GamaColorFactory.get(246, 232, 195),
			() -> GamaColorFactory.get(245, 245, 245), () -> GamaColorFactory.get(199, 234, 229),
			() -> GamaColorFactory.get(128, 205, 193), () -> GamaColorFactory.get(53, 151, 143),
			() -> GamaColorFactory.get(1, 102, 94), () -> GamaColorFactory.get(0, 60, 48) };

	/**
	 * The preference key used to retrieve the default buffering strategy applied to the GAML {@code save} statement.
	 * The actual {@link Pref} is declared in {@link Experimental#DEFAULT_SAVE_BUFFERING_STRATEGY}.
	 */
	public static final String PREF_SAVE_BUFFERING_STRATEGY = "pref_save_buffering_strategy";

	/**
	 * The preference key used to retrieve the default buffering strategy applied to the GAML {@code write} statement.
	 * The actual {@link Pref} is declared in {@link Experimental#DEFAULT_WRITE_BUFFERING_STRATEGY}.
	 */
	public static final String PREF_WRITE_BUFFERING_STRATEGY = "pref_write_buffering_strategy";

	/**
	 * The {@code Theme} class acts as a namespace for preference tab and group name constants related to the visual
	 * theme of the GAMA UI. It does not declare any {@link Pref} constants itself; the actual theme-related
	 * preferences are contributed by other components.
	 */
	public static class Theme {

		/** The display name of the "Theme" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Theme";

		/** The name of the "UI" group within the Theme tab. */
		public static final String UI = "UI";

		/** The name of the "Editor" group within the Theme tab. */
		public static final String EDITOR = "Editor";

	}

	/**
	 * The {@code Network} class groups all GAMA preferences related to networking: HTTP connections, web browser
	 * selection, and GAMA Server mode. It also defines the tab and group name constants used to place these
	 * preferences in the GAMA preferences dialog.
	 */
	public static class Network {

		/** The display name of the "Network" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Network";

		/** The name of the "Server mode" group within the Network tab. */
		public static final String SERVER = "Server mode";

		/** The name of the "Http connections" group within the Network tab. */
		public static final String HTTP = "Http connections";

		/** The name of the "Web access" group within the Network tab. */
		public static final String WEB = "Web access";

		/**
		 * Preference controlling which browser is used to open web links appearing in GAML documentation or text
		 * statements. When {@code true} (labelled "System"), the OS default browser is used; when {@code false}
		 * (labelled "Internal") the embedded SWT browser is used.
		 */
		public static final Pref<Boolean> CORE_EXTERNAL_BROWSER = create("pref_external_browser",
				"Browser to open to display web links (in documentation or text statements)", true, IType.BOOL, true)
						.withLabels("Internal", "System")
						.withColors(() -> GamaColorFactory.get("lightgray"), () -> GamaColorFactory.get("gray"))
						.in(Network.NAME, WEB);

	}

	/**
	 * The {@code Interface} class groups all GAMA preferences that govern the look and behavior of the GAMA UI,
	 * including startup options, menu sizes, console settings, simulation coloring, and the appearance of the
	 * simulation perspective. It also exposes utility methods for managing simulation color schemes.
	 */
	public static class Interface {

		/** The display name of the "Interface" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Interface";

		/** The name of the "Startup" group within the Interface tab. */
		public static final String STARTUP = "Startup";

		/** The Constant CORE_SHOW_PAGE. */
		// public static final Pref<Boolean> CORE_SHOW_PAGE =
		// create("pref_show_welcome_page", "Display welcome page", true, IType.BOOL, false).in(NAME, STARTUP);

		/**
		 * Whether GAMA should restore the main window to its previous size and position at startup. When active it
		 * deactivates {@code pref_show_maximized}.
		 */
		public static final Pref<Boolean> CORE_REMEMBER_WINDOW =
				create("pref_remember_window", "Remember GAMA window size and position", true, IType.BOOL, false)
						.in(NAME, STARTUP).deactivates("pref_show_maximized").withLabels("Yes", "No");

		/** The Constant CORE_SHOW_MAXIMIZED. */
		// public static final Pref<Boolean> CORE_SHOW_MAXIMIZED =
		// create("pref_show_maximized", "Maximize GAMA window", true, IType.BOOL, false).in(NAME, STARTUP)
		// .hidden();

		/** Whether GAMA should ask before automatically rebuilding a corrupted workspace. */
		public static final Pref<Boolean> CORE_ASK_REBUILD =
				create("pref_ask_rebuild", "Ask before rebuilding a corrupted workspace", true, IType.BOOL, false)
						.in(NAME, STARTUP).withLabels("Yes", "No");

		/** Whether GAMA should ask before opening a workspace that was created by a different GAMA version. */
		public static final Pref<Boolean> CORE_ASK_OUTDATED =
				create("pref_ask_outdated", "Ask before using a workspace created by another version", true, IType.BOOL,
						false).in(NAME, STARTUP).withLabels("Yes", "No");

		/** Whether GAMA should automatically open a predefined model at startup. Activates the model/experiment pickers. */
		public static final Pref<Boolean> CORE_STARTUP_MODEL =
				create("pref_startup_model", "Open a model", false, IType.BOOL, false).in(NAME, STARTUP)
						.activates("pref_default_model", "pref_default_experiment").withLabels("Yes", "No");

		/** The file-system path of the model to open automatically at startup (requires {@link #CORE_STARTUP_MODEL}). */
		public static final Pref<? extends IGamaFile> CORE_DEFAULT_MODEL =
				create("pref_default_model", "Model to open", () -> new GenericFile("Enter path", false), IType.FILE,
						false).in(NAME, STARTUP).restrictToWorkspace().withExtensions("gaml", "experiment")
								.refreshes("pref_default_experiment").activates("pref_default_experiment");

		/**
		 * The name of the experiment to launch automatically after opening {@link #CORE_DEFAULT_MODEL} at startup.
		 * The list of available experiments is populated dynamically from the selected model file.
		 */
		public static final Pref<String> CORE_DEFAULT_EXPERIMENT =
				create("pref_default_experiment", "Experiment to run", "", IType.STRING, false).in(NAME, STARTUP)
						.among(() -> {
							List<String> result = new ArrayList<>();
							if (CORE_STARTUP_MODEL.getValue()) {
								IGamaFile file = CORE_DEFAULT_MODEL.getValue();
								final URI uriModel = FileUtils.getURI(file.getOriginalPath(), null);
								if (uriModel == null) return result;
								result.addAll(GAML.getInfo(uriModel).getExperiments());
							}
							return result;
						});

		/** The name of the "Menus" group within the Interface tab. */
		public static final String MENUS = "Menus";

		/**
		 * The threshold at which agent lists in context menus are broken into sub-menus. For example, a value of 50
		 * means that menus showing more than 50 agents will be split into groups of 50.
		 */
		public static final Pref<Integer> CORE_MENU_SIZE =
				create("pref_menu_size", "Break down agents in menus every", 50, IType.INT, false).between(10, 1000)
						.in(NAME, MENUS);
		/** The name of the "Console" group within the Interface tab. */
		public static final String CONSOLE = "Console";

		/**
		 * The maximum number of characters that can be displayed in the GAMA console at any time. A value of
		 * {@code -1} means unlimited.
		 */
		public static final Pref<Integer> CORE_CONSOLE_SIZE = create("pref_console_size",
				"Max. number of characters to display (-1 = unlimited)", 20000, IType.INT, true).in(NAME, CONSOLE);

		/**
		 * The maximum number of characters kept in the console buffer while the simulation is paused. A value of
		 * {@code -1} means unlimited.
		 */
		public static final Pref<Integer> CORE_CONSOLE_BUFFER =
				create("pref_console_buffer", "Max. number of characters to keep when paused (-1 = unlimited)", 20000,
						IType.INT, true).in(NAME, CONSOLE);

		/**
		 * Whether long console lines should be wrapped to fit the console width. Wrapping can slow down output when
		 * large amounts of text are printed.
		 */
		public static final Pref<Boolean> CORE_CONSOLE_WRAP =
				create("pref_console_wrap", "Wrap long lines (can slow down output)", false, IType.BOOL, true)
						.in(NAME, CONSOLE).withLabels("Yes", "No");

		/** The name of the "Appearance" group within the Interface tab. */
		public static final String APPEARANCE = "Appearance";

		/** The name of the "Navigator" group within the Interface tab. */
		public static final String NAVIGATOR = "Navigator";
		/** The name of the "Simulations" group within the Interface tab. */
		public static final String SIMULATIONS = "Simulations";

		/** Whether the name of each simulation should be appended to the title of its output views. */
		public static final Pref<Boolean> CORE_SIMULATION_NAME = create("pref_append_simulation_name",
				"Append the name of simulations to their outputs", false, IType.BOOL, true).in(NAME, SIMULATIONS);

		/**
		 * The resolved simulation color palette. Populated lazily on the first call to
		 * {@link #getColorForSimulation(int)} or whenever the color scheme preference changes. Each element is the
		 * {@link IColor} to use for simulation at the corresponding index.
		 */
		private static IColor[] SIMULATION_COLORS = null;

		/**
		 * Recomputes the {@link #SIMULATION_COLORS} array using the given pivot color. Nine colors are derived by
		 * applying successive {@link IColor#darker()} and {@link IColor#brighter()} transformations, producing a
		 * ramp centered on the pivot. This method is only effective when the current color scheme is
		 * {@link #PIVOT}; if the scheme is something else this method returns immediately.
		 *
		 * @param c
		 *            the pivot (central) color of the generated ramp; must not be {@code null}
		 */
		static void setPivot(final IColor c) {
			if (!PIVOT.equals(CORE_SIMULATION_COLOR.getValue())) return;
			SIMULATION_COLORS = new IColor[9];
			SIMULATION_COLORS[0] = c.darker().darker().darker().darker();
			SIMULATION_COLORS[1] = c.darker().darker().darker();
			SIMULATION_COLORS[2] = c.darker().darker();
			SIMULATION_COLORS[3] = c.darker();
			SIMULATION_COLORS[4] = c;
			SIMULATION_COLORS[5] = c.brighter();
			SIMULATION_COLORS[6] = c.brighter().brighter();
			SIMULATION_COLORS[7] = c.brighter().brighter().brighter();
			SIMULATION_COLORS[8] = c.brighter().brighter().brighter().brighter();
		}

		/**
		 * Returns the UI color associated with the simulation at the given zero-based {@code index}. Colors are
		 * cycled modulo the length of the current palette, so this method never returns {@code null} regardless of
		 * the index value. The color palette is initialized lazily on the first call.
		 *
		 * @param index
		 *            the zero-based index of the simulation whose color is requested
		 * @return the {@link IColor} assigned to that simulation in the active color scheme; never {@code null}
		 */
		public static IColor getColorForSimulation(final int index) {
			if (SIMULATION_COLORS == null) { setColorScheme(CORE_SIMULATION_COLOR.getValue()); }
			return SIMULATION_COLORS[index % SIMULATION_COLORS.length];
		}

		/**
		 * Switches the active simulation color scheme to the named scheme and rebuilds the {@link #SIMULATION_COLORS}
		 * palette. Recognized scheme names are {@link #DIVERGING}, {@link #BASIC}, {@link #QUALITATIVE}, and
		 * {@link #PIVOT}. Any unknown or {@code null} scheme is treated as {@link #PIVOT} and delegates to
		 * {@link #setPivot(IColor)} using the current value of {@link #CORE_PIVOT_COLOR}.
		 *
		 * @param scheme
		 *            the name of the color scheme to activate; may be {@code null}
		 */
		public static void setColorScheme(final String scheme) {
			switch (scheme) {
				case DIVERGING:
					SIMULATION_COLORS = new IColor[DIVERGING_COLORS.length];
					for (int i = 0; i < DIVERGING_COLORS.length; i++) {
						SIMULATION_COLORS[i] = DIVERGING_COLORS[i].get();
					}
					break;
				case BASIC:
					SIMULATION_COLORS = new IColor[BASIC_COLORS.length];
					for (int i = 0; i < BASIC_COLORS.length; i++) { SIMULATION_COLORS[i] = BASIC_COLORS[i].get(); }
					break;
				case QUALITATIVE:
					SIMULATION_COLORS = new IColor[QUALITATIVE_COLORS.length];
					for (int i = 0; i < QUALITATIVE_COLORS.length; i++) {
						SIMULATION_COLORS[i] = QUALITATIVE_COLORS[i].get();
					}
					break;
				case null:
				default:
					setPivot(CORE_PIVOT_COLOR.getValue());
					break;
			}
		}

		/** The display label for the 11-color diverging color scheme in the preferences UI. */
		static final String DIVERGING = "Diverging (11 colors)";

		/** The display label for the 5-color basic color scheme in the preferences UI. */
		static final String BASIC = "Basic (5 colors)";

		/** The display label for the 9-color qualitative color scheme in the preferences UI. */
		static final String QUALITATIVE = "Qualitative (9 colors)";

		/** The display label for the pivot-based 9-color scheme in the preferences UI. */
		static final String PIVOT = "Based on pivot color (9 colors)";

		/**
		 * The default color scheme used for distinguishing simulations in the UI. One of {@link #BASIC},
		 * {@link #DIVERGING}, {@link #QUALITATIVE}, or {@link #PIVOT}. Changing this preference automatically calls
		 * {@link Interface#setColorScheme(String)} to rebuild the color palette.
		 */
		public static final Pref<String> CORE_SIMULATION_COLOR =
				create("pref_simulation_colors", "Default color scheme for simulations in UI", DIVERGING, IType.STRING,
						true).among(BASIC, DIVERGING, QUALITATIVE, PIVOT).in(NAME, SIMULATIONS)
								.onChange(Interface::setColorScheme);

		/**
		 * The pivot (center) color used when the active scheme is {@link #PIVOT}. Changing this preference
		 * automatically calls {@link Interface#setPivot(IColor)} to rebuild the ramp.
		 */
		public static final Pref<IColor> CORE_PIVOT_COLOR =
				create("pref_simulation_color", "Pivot color of simulations", () -> GamaColorFactory.get(64, 224, 208),
						IType.COLOR, true).in(NAME, SIMULATIONS).onChange(Interface::setPivot);

		/**
		 * Whether the console contents should be kept (not cleared) between successive experiments. When
		 * {@code false}, the console is wiped each time a new experiment starts.
		 */
		public static final Pref<Boolean> CORE_CONSOLE_KEEP =
				create("pref_console_keep", "Keep the console contents between experiments", false, IType.BOOL, true)
						.in(NAME, CONSOLE).withLabels("Yes", "No");

	}

	/**
	 * The {@code Modeling} class groups all preferences that control the behavior and appearance of GAML editors in
	 * the GAMA IDE, including auto-closing of brackets and quotes, formatting, font, background color, and the
	 * display of experiment buttons.
	 */
	public static class Modeling {

		/** The display name of the "Editors" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Editors";

		/** The name of the "Edition" group within the Editors tab. */
		public static final String TEXT = "Edition";
		/** The name of the "Options" group within the Editors tab. */
		public static final String OPTIONS = "Options";

		/** Whether warning markers should be displayed in the GAML editor gutter. Hidden from the preferences UI. */
		public static final Pref<Boolean> WARNINGS_ENABLED =
				create("pref_editor_enable_warnings", "Show warning markers in the editor", true, IType.BOOL, false)
						.in(NAME, OPTIONS).hidden();

		/** Whether informational markers should be displayed in the GAML editor gutter. Hidden from the preferences UI. */
		public static final Pref<Boolean> INFO_ENABLED =
				create("pref_editor_enable_infos", "Show information markers in the editor", true, IType.BOOL, false)
						.in(NAME, OPTIONS).hidden();

		/** Whether all open GAML editors should be saved automatically when the user switches perspective. */
		public static final Pref<Boolean> EDITOR_PERSPECTIVE_SAVE = create("pref_editor_perspective_save",
				"Save editors when switching perspectives", true, IType.BOOL, false).in(Modeling.NAME, Modeling.OPTIONS)
						.activates("pref_editor_ask_save").withLabels("Yes", "No");

		/** Whether all open GAML editors should be saved before an experiment is launched. */
		public static final Pref<Boolean> EDITOR_SAVE = GamaPreferences
				.create("pref_editor_save_all", "Save editors before lauching an experiment", true, IType.BOOL, false)
				.in(NAME, GamaPreferences.Modeling.OPTIONS).activates("pref_editor_ask_save").withLabels("Yes", "No");

		/** Whether GAMA should prompt the user before saving each file individually. */
		public static final Pref<Boolean> EDITOR_SAVE_ASK =
				create("pref_editor_ask_save", "Ask before saving each file", false, IType.BOOL, false).in(NAME,
						OPTIONS);

		/** Whether GAML editors should be hidden when the simulation perspective is activated. */
		public static final Pref<Boolean> EDITOR_PERSPECTIVE_HIDE =
				create("pref_editor_perspective_hide", "Hide editors when switching to simulation perspective", true,
						IType.BOOL, false).in(Modeling.NAME, Modeling.OPTIONS).withLabels("Yes", "No");

		/** Whether the operators context menu should be sorted by category or by name. Hidden from the preferences UI. */
		public static final Pref<Boolean> OPERATORS_MENU_SORT =
				create("pref_menu_operators_sort2", "Sort operators menu by", true, IType.STRING, false)
						.withLabels("Category", "Name").in(Interface.NAME, Interface.MENUS).hidden();

		/**
		 * Whether the editor should automatically wrap the selected text with the matching closing character when
		 * {@code {}, [], (), '', "", <>} is pressed while text is selected (see issue #391).
		 */
		public static final Pref<Boolean> CORE_SURROUND_SELECTED = create("pref_editor_surround_selected",
				"Surround selected text with the matching character when { [ ( \" ' < is pressed", true, IType.BOOL,
				false).in(NAME, TEXT).withLabels("Yes", "No");

		/** Whether the editor should automatically insert a closing single quote after an opening one. */
		public static final Pref<Boolean> CORE_CLOSE_QUOTE =
				create("pref_editor_close_quote", "Automatically close single quotes — '..'", true, IType.BOOL, false)
						.in(NAME, TEXT);

		/** Whether the editor should automatically insert a closing double quote after an opening one. */
		public static final Pref<Boolean> CORE_CLOSE_DOUBLE = create("pref_editor_close_double",
				"Automatically close double quotes — \"..\"", true, IType.BOOL, false).in(NAME, TEXT);

		/** Whether the editor should automatically insert a closing curly bracket after an opening one. */
		public static final Pref<Boolean> CORE_CLOSE_CURLY =
				create("pref_editor_close_curly", "Automatically close curly brackets — {..}", true, IType.BOOL, false)
						.in(NAME, TEXT);

		/** Whether the editor should automatically insert a closing square bracket after an opening one. */
		public static final Pref<Boolean> CORE_CLOSE_SQUARE = create("pref_editor_close_square",
				"Automatically close square brackets — [..]", true, IType.BOOL, false).in(NAME, TEXT);

		/** Whether the editor should automatically insert a closing parenthesis after an opening one. */
		public static final Pref<Boolean> CORE_CLOSE_PARENTHESES = create("pref_editor_close_parentheses",
				"Automatically close parentheses — (..)", true, IType.BOOL, false).in(NAME, TEXT);

		/** Whether the GAML formatter should be applied automatically each time a file is saved. */
		public static final Pref<Boolean> EDITOR_CLEAN_UP =
				create("pref_editor_save_format", "Apply formatting on save", false, IType.BOOL, false).in(NAME,
						GamaPreferences.Modeling.OPTIONS);

		/** Whether files and resources dragged into a GAML editor should be inserted as GAML resource references. */
		public static final Pref<Boolean> EDITOR_DRAG_RESOURCES = create("pref_editor_drag_resources",
				"Drag files and resources as references in GAML files", true, IType.BOOL, false).in(NAME, OPTIONS);

		/** The font used in GAML editors. A {@code null} value means the IDE default font is used. */
		public static final Pref<IFont> EDITOR_BASE_FONT = GamaPreferences
				.create("pref_editor_font", "Font of editors", (IFont) null, IType.FONT, false).in(NAME, TEXT);

		/** The background color of GAML editors. A {@code null} value means the IDE default background is used. */
		public static final Pref<IColor> EDITOR_BACKGROUND_COLOR =
				create("pref_editor_background_color", "Background color of editors", (IColor) null, IType.COLOR, false)
						.in(NAME, TEXT);

		/** Whether the editor should highlight all occurrences of the symbol under the cursor. */
		public static final Pref<Boolean> EDITOR_MARK_OCCURRENCES = GamaPreferences
				.create("pref_editor_mark_occurrences", "Mark occurrences of symbols", true, IType.BOOL, false)
				.in(NAME, TEXT);

		/**
		 * Whether experiments defined in a GAML model should appear as a drop-down menu or as individual toolbar
		 * buttons at the top of the editor. When {@code false} (labelled "Buttons"), individual buttons are shown;
		 * when {@code true} (labelled "Menu"), a single menu is used instead.
		 */
		public static final Pref<Boolean> EDITOR_EXPERIMENT_MENU = GamaPreferences
				.create("pref_editor_experiment_menu", "Display experiments as", false, IType.BOOL, false)
				.withLabels("Menu", "Buttons").deactivates("pref_editor_collapse_buttons").in(NAME, TEXT)
				.withColors(() -> GamaColorFactory.get("white"), () -> GamaColorFactory.get("darkgray"));

		/**
		 * Whether experiment buttons that exceed the editor width should collapse into a menu. Defaults to
		 * {@code true} on Linux where button overflow is more common.
		 */
		public static final Pref<Boolean> EDITOR_COLLAPSE_BUTTONS =
				create("pref_editor_collapse_buttons", "Use a menu when the buttons exceed the width of the editor",
						SystemInfo.isLinux(), IType.BOOL, false).in(NAME, TEXT);

		/**
		 * Whether errors, warnings, and informational messages should be displayed inline in the editor body
		 * (as inline annotations) rather than only in the editor gutter.
		 */
		public static final Pref<Boolean> EDITOR_MINING = create("pref_editor_mining",
				"Inline errors, warnings and information messages", true, IType.BOOL, false).in(NAME, TEXT);

	}

	/**
	 * The {@code Runtime} class groups all GAMA preferences related to experiment execution, including auto-run
	 * behavior, synchronization, parameters view, parallelism, memory management, runtime error handling, and GAMA
	 * Server configuration.
	 */
	public static class Runtime {

		/** The display name of the "Execution" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Execution";

		/** The name of the "Experiments" group within the Execution tab. */
		public static final String EXECUTION = "Experiments";

		/** Whether experiments should start running immediately after being launched, without waiting for user input. */
		public static final Pref<Boolean> CORE_AUTO_RUN = create("pref_experiment_auto_run",
				"Auto-run experiments when they are launched", false, IType.BOOL, true).in(NAME, EXECUTION);

		/** The Constant CORE_ASK_CLOSING. */
		public static final Pref<Boolean> CORE_ASK_CLOSING =
				create("pref_experiment_ask_closing", "Ask to close the previous experiment when launching a new one",
						true, IType.BOOL, true).in(NAME, EXECUTION);

		/** The Constant CORE_SLIDER_TYPE. */
		public static final Pref<Boolean> CORE_SLIDER_TYPE =
				create("pref_experiment_type_slider", "Scale of the step duration slider", true, IType.BOOL, true)
						.in(NAME, EXECUTION).withLabels("Linear", "Logarithmic")
						.withColors(() -> GamaColorFactory.get("white"), () -> GamaColorFactory.get("darkgray"));

		/** The Constant CORE_SYNC. */
		public static final Pref<Boolean> CORE_SYNC =
				create("pref_display_synchronized", "Synchronize outputs with the simulation", false, IType.BOOL, true)
						.in(NAME, EXECUTION);

		/** The name of the "Parameters" group within the Execution tab. */
		public static final String PARAMETERS = "Parameters";

		/** Whether all parameter categories in the parameters view should be automatically expanded at startup. */
		public static final Pref<Boolean> CORE_EXPAND_PARAMS = create("pref_experiment_expand_params",
				"Automatically expand the parameters categories", false, IType.BOOL, true).in(NAME, PARAMETERS);

		/** Whether monitor outputs should be displayed in the parameters view rather than in a dedicated monitor view. */
		public static final Pref<Boolean> CORE_MONITOR_PARAMETERS =
				create("pref_monitors_in_parameters", "Display monitors in the parameters view", true, IType.BOOL, true)
						.in(NAME, PARAMETERS);

		/** Whether RNG (random number generation) parameters should appear in the parameters view. */
		public static final Pref<Boolean> CORE_RND_EDITABLE =
				create("pref_rng_in_parameters", "Include random number generation parameters in the parameters view",
						false, IType.BOOL, true).in(NAME, PARAMETERS);

		/** The name of the "Parallelism" group within the Execution tab. */
		public static final String CONCURRENCY = "Parallelism";

		/** The name of the "Tests" group within the Execution tab. */
		public static final String TESTS = "Tests";

		/**
		 * Whether test results should be sorted by severity (aborted and failed tests first) in the tests view.
		 */
		public static final Pref<Boolean> TESTS_SORTED =
				create("pref_tests_sorted", "Sorts the results of tests by severity", false, IType.BOOL, false)
						.in(NAME, TESTS).withComment(", if true, aborted and failed tests are displayed first");

		/** Whether GAMA should automatically run all registered tests each time the platform starts. */
		public static final Pref<Boolean> START_TESTS =
				create("pref_start_tests", "Run tests at each start of the platform", false, IType.BOOL, false).in(NAME,
						TESTS);

		/** Whether user-defined models with {@code test} experiments should be included in the global test suite. */
		public static final Pref<Boolean> USER_TESTS =
				create("pref_user_tests", "Include user-defined tests in the tests suite", false, IType.BOOL, false)
						.in(NAME, TESTS).withComment(", if true, will run user models with 'test' experiments");

		/** Whether only failed and aborted tests should be shown in the tests view, hiding passing tests. */
		public static final Pref<Boolean> FAILED_TESTS =
				create("pref_failed_tests", "Only display failed and aborted tests", false, IType.BOOL, true)
						.in(NAME, TESTS).withComment(", if true, only aborted and failed tests are displayed");

		/** The name of the "Memory" group within the Execution tab. */
		public static final String MEMORY = "Memory";

		/**
		 * Whether GAMA should monitor available memory and emit a warning when it becomes low. Activates the
		 * memory threshold and polling frequency preferences. Hidden from the preferences UI.
		 */
		public static final Pref<Boolean> CORE_MEMORY_POLLING =
				create("pref_check_memory", "Emit a warning when memory is low", true, IType.BOOL, true)
						.in(NAME, MEMORY).activates("pref_memory_threshold", "pref_memory_frequency").hidden();

		/** The percentage of available heap below which a low-memory warning is emitted (0–100). */
		public static final Pref<Integer> CORE_MEMORY_PERCENTAGE =
				create("pref_memory_threshold", "Emit a warning when the percentage of available memory is under", 20,
						IType.INT, true).in(NAME, MEMORY).between(0, 100);

		/** The polling interval in seconds at which the memory level is checked for low-memory warnings. */
		public static final Pref<Integer> CORE_MEMORY_FREQUENCY = create("pref_memory_frequency",
				"Interval (in seconds) at which memory should be monitored", 2, IType.INT, true).in(NAME, MEMORY);

		/**
		 * The action GAMA takes when an experiment runs out of memory: {@code true} (labelled "Close") closes the
		 * experiment, while {@code false} (labelled "Exit") terminates the whole GAMA process.
		 */
		public static final Pref<Boolean> CORE_MEMORY_ACTION =
				create("pref_memory_action", "When running out of memory in an experiment, GAMA should", true,
						IType.BOOL, true).in(NAME, MEMORY).withLabels("Close", "Exit");

		/** The name of the "Runtime errors" group within the Execution tab. */
		public static final String ERRORS = "Runtime errors";

		/**
		 * Whether runtime execution errors should be displayed to the user. When active, also enables the error
		 * count and ordering preferences.
		 */
		public static final Pref<Boolean> CORE_SHOW_ERRORS =
				create("pref_errors_display", "Show execution errors", true, IType.BOOL, true).in(NAME, ERRORS)
						.activates("pref_errors_number", "pref_errors_recent_first", "pref_display_show_errors");

		/** Whether errors thrown inside display and output layers should also be shown to the user. */
		public static final Pref<Boolean> ERRORS_IN_DISPLAYS = create("pref_display_show_errors",
				"Show errors thrown in displays and outputs", false, IType.BOOL, true).in(NAME, ERRORS);

		/** The maximum number of errors to display simultaneously in the error view. */
		public static final Pref<Integer> CORE_ERRORS_NUMBER =
				create("pref_errors_number", "Number of errors to display", 10, IType.INT, true).in(NAME, ERRORS)
						.between(1, null);

		/** Whether the most recently thrown errors should appear first in the error view. Hidden from the preferences UI. */
		public static final Pref<Boolean> CORE_RECENT =
				create("pref_errors_recent_first", "Display most recent first", true, IType.BOOL, true).in(NAME, ERRORS)
						.hidden();

		/** Whether a simulation should be stopped as soon as it throws its first runtime error. */
		public static final Pref<Boolean> CORE_STOP_AT_FIRST_ERROR =
				create("pref_errors_stop", "Stop simulation at first error", true, IType.BOOL, true).in(NAME, ERRORS);

		/** Whether GAML compilation warnings should be treated as hard errors. */
		public static final Pref<Boolean> CORE_WARNINGS_AS_ERRORS =
				create("pref_errors_warnings_errors", "Treat warnings as errors", false, IType.BOOL, true).in(NAME,
						ERRORS);

		/** Whether runtime errors should be highlighted in the GAML editor at the offending source location. */
		public static final Pref<Boolean> CORE_ERRORS_EDITOR_LINK =
				create("pref_errors_in_editor", "Show errors in editors", true, IType.BOOL, true).in(NAME, ERRORS);

		/**
		 * Whether GAMA Server mode should be enabled, allowing remote clients to connect and control GAMA over a
		 * network socket. Activates the port, ping interval, and console output preferences.
		 */
		public static final Pref<Boolean> CORE_SERVER_MODE =
				create("pref_enable_server", "Enable GAMA Server mode", true, IType.BOOL, true)
						.in(Network.NAME, Network.SERVER)
						.activates("pref_server_port", "pref_server_ping", "pref_server_console");

		/** The TCP port on which the GAMA Server listens for incoming client connections. */
		public static final Pref<Integer> CORE_SERVER_PORT =
				create("pref_server_port", "Port to which GAMA Server is listening", 1000, IType.INT, true)
						.in(Network.NAME, Network.SERVER);

		/** The interval in milliseconds between two keep-alive pings from the server to connected clients. Use {@code -1} to disable pinging. */
		public static final Pref<Integer> CORE_SERVER_PING =
				create("pref_server_ping", "Interval between two pings (-1 to disable)", 10000, IType.INT, true)
						.in(Network.NAME, Network.SERVER);

		/** Whether the {@code TCP_NODELAY} socket option should be set, disabling Nagle's algorithm for lower latency. */
		public static final Pref<Boolean> CORE_SERVER_NO_DELAY =
				create("pref_server_no_delay", "Sets the TCP_NODELAY option to true for gama server", false, IType.BOOL,
						true).in(Network.NAME, Network.SERVER);

		/** The Constant CORE_SERVER_CONSOLE. */
		public static final Pref<Boolean> CORE_SERVER_CONSOLE =
				create("pref_server_console", "Send console outputs to clients", true, IType.BOOL, true)
						.in(Network.NAME, Network.SERVER);
	}

	/**
	 * The {@code Displays} class groups all GAMA preferences that control how graphical displays are rendered and
	 * presented, covering layout, background color, agent shapes, OpenGL rendering options, camera settings, and
	 * chart rendering.
	 */
	public static class Displays {

		/** The display name of the "Displays" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Displays";

		/** The name of the group governing general display view presentation and behavior. */
		public static final String PRESENTATION = "Presentation and Behavior of Graphical Display Views";

		/** The ordered list of available display view layout names. */
		public static final List<String> LAYOUTS = Arrays.asList("None", "Stacked", "Split", "Horizontal", "Vertical");

		/** The default layout used to arrange multiple display views when opening an experiment. */
		public static final Pref<String> CORE_DISPLAY_LAYOUT =
				create("pref_display_view_layout", "Default layout of display views", "Split", IType.STRING, true)
						.among(LAYOUTS.toArray(new String[LAYOUTS.size()])).in(NAME, PRESENTATION);

		/** Whether a visible border should be drawn around each display view. Hidden from the preferences UI. */
		public static final Pref<Boolean> CORE_DISPLAY_BORDER =
				create("pref_display_show_border", "Display a border around display views", false, IType.BOOL, true)
						.in(NAME, PRESENTATION).hidden();

		/**
		 * Whether displays should continue to refresh and repaint even while the Modeling perspective is active
		 * (i.e., not the simulation perspective).
		 */
		public static final Pref<Boolean> CORE_DISPLAY_PERSPECTIVE =
				create("pref_display_continue_drawing", "Continue to draw displays when in Modeling perspective", false,
						IType.BOOL, true).in(NAME, PRESENTATION);

		/**
		 * Whether to use a faster but potentially incomplete snapshot mechanism. Fast snapshots may be incorrect if
		 * the display is obscured by other windows.
		 */
		public static final Pref<Boolean> DISPLAY_FAST_SNAPSHOT = create("pref_display_fast_snapshot",
				"Enable fast snapshots (uncomplete when the display is obscured by others but much faster)", false,
				IType.BOOL, true).in(NAME, PRESENTATION);

		/** Whether the toolbar at the top of each display view should be shown. Hidden from the preferences UI. */
		public static final Pref<Boolean> CORE_DISPLAY_TOOLBAR =
				create("pref_display_show_toolbar", "Show the display top toolbar", true, IType.BOOL, true)
						.in(NAME, PRESENTATION).hidden();

		/** Whether the overlay bar at the bottom of each display view should be shown. Hidden from the preferences UI. */
		public static final Pref<Boolean> CORE_OVERLAY =
				create("pref_display_show_overlay", "Show the display bottom overlay", false, IType.BOOL, true)
						.in(NAME, PRESENTATION).hidden();

		/**
		 * Whether chart data points should be kept in memory (enabling CSV export) rather than discarded to reduce
		 * memory usage.
		 */
		public static final Pref<Boolean> CHART_MEMORIZE = create("pref_display_memorize_charts",
				"Keep chart values in memory (to save them as CSV) or not (to lower memory usage)", true, IType.BOOL,
				true).in(Experimental.NAME, Experimental.GRAPHICAL);

		/**
		 * The graphical resolution of charts rendered in OpenGL displays, from {@code 0.1} (fast/low quality) to
		 * {@code 1.0} (slow/best quality).
		 */
		public static final Pref<Double> CHART_QUALITY = create("pref_chart_quality",
				"Graphical resolution of the charts in OpenGL (from 0, small and fast, to 1, best but consuming lots of resources)",
				0.8, IType.FLOAT, true).in(Experimental.NAME, Experimental.GRAPHICAL).between(0.1, 1.0);

		/** The name of the "Default Rendering Properties" group within the Displays tab. */
		public static final String DRAWING = "Default Rendering Properties";

		/**
		 * The default rendering backend for displays. When {@code true} (labelled "2D using Java"), Java2D is used;
		 * when {@code false} (labelled "3D using OpenGL"), OpenGL is used.
		 */
		public static final Pref<Boolean> CORE_DISPLAY =
				create("pref_display_default2", "Default rendering method", true, IType.BOOL, true)
						.withLabels("2D using Java", "3D using OpenGL").in(NAME, DRAWING);

		/** Whether antialiasing should be applied when rendering displays. */
		public static final Pref<Boolean> CORE_ANTIALIAS =
				create("pref_display_antialias", "Apply antialiasing", false, IType.BOOL, true).in(NAME, DRAWING);

		/** The Constant CORE_BACKGROUND. */
		public static final Pref<IColor> CORE_BACKGROUND =
				create("pref_display_background_color", "Default background color ('background' facet of 'display')",
						() -> GamaColorFactory.get("white"), IType.COLOR, true).in(NAME, DRAWING);

		/** The default highlight color used when an agent is highlighted in a display. */
		public static final Pref<IColor> CORE_HIGHLIGHT =
				create("pref_display_highlight_color", "Default highlight color",
						() -> GamaColorFactory.get(0, 200, 200), IType.COLOR, true).in(NAME, DRAWING);

		/** The default shape used to render agents that have no explicit shape definition in their species. */
		public static final Pref<String> CORE_SHAPE =
				create("pref_display_default_shape", "Defaut shape of agents", "shape", IType.STRING, true)
						.among("shape", "circle", "square", "triangle", "point", "cube", "sphere").in(NAME, DRAWING);

		/** The default size (in meters/units) of agents that have no explicit size in their species. */
		public static final Pref<Double> CORE_SIZE =
				create("pref_display_default_size", "Default size of agents", 1.0, IType.FLOAT, true)
						.between(0.01, null).in(NAME, DRAWING);

		/** The default fill color used to render agents that have no explicit color in their species. */
		public static final Pref<IColor> CORE_COLOR = create("pref_display_default_color", "Default color of agents",
				() -> GamaColorFactory.get("yellow"), IType.COLOR, true).in(NAME, DRAWING);

		/**
		 * Whether hardware acceleration for Java2D should be disabled. Useful on configurations where acceleration
		 * causes rendering artifacts or crashes.
		 */
		public static final Pref<Boolean> DISPLAY_NO_ACCELERATION = create("pref_display_no_java2d_acceleration",
				"Disable acceleration for Java2D (necessary on some configurations)", false, IType.BOOL, true)
						.in(Experimental.NAME, Experimental.GRAPHICAL);

		/** The name of the "OpenGL Rendering Properties" group within the Displays tab. */
		public static final String RENDERING = "OpenGL Rendering Properties";

		/**
		 * Whether only agents whose {@code visible} attribute is {@code true} should be sent to the OpenGL
		 * rendering pipeline. Enabling this can improve performance but may create visual oddities.
		 */
		public static final Pref<Boolean> DISPLAY_ONLY_VISIBLE = create("pref_display_visible_agents",
				"Only display visible agents in OpenGL (faster, may create visual oddities)", false, IType.BOOL, true)
						.in(Experimental.NAME, Experimental.GRAPHICAL);

		/** Whether the 3D coordinate axes should be drawn in OpenGL displays. Hidden from the preferences UI. */
		public static final Pref<Boolean> CORE_DRAW_ENV =
				create("pref_display_show_referential", "Draw 3D axes", true, IType.BOOL, true).in(NAME, RENDERING)
						.hidden();

		/** Whether the rotation helper axes should be drawn in OpenGL displays. Hidden from the preferences UI. */
		public static final Pref<Boolean> DRAW_ROTATE_HELPER =
				create("pref_display_show_rotation", "Draw rotation axes", true, IType.BOOL, true).in(NAME, RENDERING)
						.hidden();

		/**
		 * The default line width (in pixels) used by the {@code width} facet of GAML {@code draw} statements.
		 * Note: not all OpenGL implementations support widths other than 1.
		 */
		public static final Pref<Double> CORE_LINE_WIDTH = create("pref_display_line_width",
				"Default line width (facet 'width' of 'draw'). Note that this attribute is not supported by all OpenGL implementations",
				1d, IType.FLOAT, true).in(NAME, RENDERING);

		/**
		 * Whether only the outward-facing faces of 3D objects should be rendered in OpenGL, hiding internal faces
		 * for a potential performance gain.
		 */
		public static final Pref<Boolean> ONLY_VISIBLE_FACES =
				create("pref_display_visible_faces", "Draw only the 'external' faces of objects in OpenGL", false,
						IType.BOOL, true).in(Experimental.NAME, Experimental.GRAPHICAL);

		/**
		 * The number of polygon slices used when approximating circular geometries in OpenGL. Higher values produce
		 * smoother circles but increase GPU load.
		 */
		public static final Pref<Integer> DISPLAY_SLICE_NUMBER = create("pref_display_slice_number",
				"Number of slices of circular geometries in OpenGL (the higher the more resources consuming)", 16,
				IType.INT, true).in(Experimental.NAME, Experimental.GRAPHICAL);

		/**
		 * Whether a small z-increment should be added to objects and layers in OpenGL to prevent z-fighting
		 * (depth-buffer conflicts that produce visual flickering). Activates {@link #OPENGL_Z_FACTOR}.
		 */
		public static final Pref<Boolean> OPENGL_Z_FIGHTING = create("pref_opengl_z_fighting",
				"In OpenGL, add a small increment to the z ordinate of objects and layers to fight visual artefacts",
				true, IType.BOOL, true).in(Experimental.NAME, Experimental.GRAPHICAL).activates("pref_opengl_z_factor");

		/**
		 * The magnitude of the z-increment applied when {@link #OPENGL_Z_FIGHTING} is active. Range: {@code [0, 1]},
		 * step {@code 0.001}.
		 */
		public static final Pref<Double> OPENGL_Z_FACTOR =
				create("pref_opengl_z_factor", "Increment factor (from 0, none, to 1, max)", 0.05, IType.FLOAT, true)
						.in(Experimental.NAME, Experimental.GRAPHICAL).between(0d, 1d).step(0.001);

		/**
		 * Whether textures in OpenGL should be oriented to match the geometry on which they are displayed.
		 * This can produce visual oddities on some geometry types.
		 */
		public static final Pref<Boolean> OPENGL_TEXTURE_ORIENTATION = create("pref_texture_orientation",
				"In OpenGL, orient the textures according to the geometry on which they are displayed (may create visual oddities)",
				true, IType.BOOL, true).in(Experimental.NAME, Experimental.GRAPHICAL);

		/** The zoom speed factor for OpenGL displays, ranging from {@code 0} (slowest) to {@code 1} (fastest). */
		public static final Pref<Double> OPENGL_ZOOM =
				create("pref_display_zoom_factor", "Set the zoom factor (0 for slow, 1 fast)", 0.5, IType.FLOAT, true)
						.in(NAME, RENDERING).between(0, 1).step(0.01);

		/** The keyboard movement sensitivity for OpenGL camera navigation, ranging from {@code 0.01} (slow) to {@code 1} (fast). */
		public static final Pref<Double> OPENGL_KEYBOARD = create("pref_display_keyboard_factor",
				"Set the sensitivity of the keyboard movements  (0 for slow, 1 for fast)", 0.5, IType.FLOAT, true)
						.in(NAME, RENDERING).between(0.01, 1.0).step(0.01);

		/** The mouse/trackpad movement sensitivity for OpenGL camera navigation, ranging from {@code 0.01} (slow) to {@code 1} (fast). */
		public static final Pref<Double> OPENGL_MOUSE = create("pref_display_mouse_factor",
				"Set the sensitivity of the mouse/trackpad movements  (0 for slow, 1 fast)", 0.5, IType.FLOAT, true)
						.in(NAME, RENDERING).between(0.01, 1.0).step(0.01);

		/**
		 * Whether the OpenGL renderer should cap its frame rate to avoid rendering stuttering. Activates
		 * {@link #OPENGL_FPS}.
		 */
		public static final Pref<Boolean> OPENGL_CAP_FPS = create("pref_display_cap_fps",
				"Limit the number of frames per second if you experience stuttering in the rendering", false,
				IType.BOOL, true).in(Experimental.NAME, Experimental.GRAPHICAL).activates("pref_display_max_fps");

		/** The maximum number of frames per second for the OpenGL renderer when {@link #OPENGL_CAP_FPS} is active. */
		public static final Pref<Integer> OPENGL_FPS =
				create("pref_display_max_fps", "Max. number of frames per second", 60, IType.INT, true)
						.in(Experimental.NAME, Experimental.GRAPHICAL);

		/**
		 * Whether the numeric keypad keys (2, 4, 6, 8) should be used for camera interaction in OpenGL displays in
		 * addition to the standard arrow keys.
		 */
		public static final Pref<Boolean> OPENGL_NUM_KEYS_CAM = create("pref_display_numkeyscam",
				"Use Numeric Keypad (2,4,6,8) for camera interaction", true, IType.BOOL, true).in(NAME, RENDERING);

		/**
		 * The preset camera to use in OpenGL displays when no camera is specified by the GAML model. The available
		 * presets are defined in {@link ICameraDefinition#PRESETS}.
		 */
		public static final Pref<String> OPENGL_DEFAULT_CAM =
				create("pref_display_camera", "Default camera to use when none is specified", "From above",
						IType.STRING, true).among(ICameraDefinition.PRESETS).in(NAME, RENDERING);

		/**
		 * Whether the GAMA internal image cache should be used when building OpenGL textures. This can speed up
		 * simulations that reuse the same images across multiple replications but increases memory usage.
		 */
		public static final Pref<Boolean> OPENGL_USE_IMAGE_CACHE = create("pref_display_use_cache",
				"Use GAMA image cache when building textures in OpenGL (potentially faster when running several simulations, but uses more memory)",
				true, IType.BOOL, true).in(Experimental.NAME, Experimental.GRAPHICAL);

		/**
		 * The default intensity of OpenGL light sources, ranging from {@code 0} (completely dark) to {@code 255}
		 * (maximum brightness).
		 */
		public static final Pref<Integer> OPENGL_DEFAULT_LIGHT_INTENSITY = create("pref_display_light_intensity",
				"Set the default intensity of the lights (from 0, dark, to 255, light)", 160, IType.INT, true)
						.in(NAME, RENDERING).between(0, 255);

		/**
		 * The default font used in GAML {@code draw} statements when no explicit font is specified. Defaults to
		 * Helvetica plain 12pt.
		 */
		public static final Pref<IFont> DEFAULT_DISPLAY_FONT = GamaPreferences
				.create("pref_display_default_font", "Default font to use in 'draw'",
						() -> GamaFontFactory.createFont("Helvetica", Font.PLAIN, 12), IType.FONT, true)
				.in(GamaPreferences.Displays.NAME, GamaPreferences.Displays.DRAWING);

	}

	/**
	 * The {@code External} class groups all GAMA preferences related to external data access: HTTP connection
	 * parameters, random number generation, GIS coordinate reference systems, and file format settings (CSV, JSON).
	 */
	public static class External {

		/** The display name of the "Data and Operators" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Data and Operators";

		/** The timeout in milliseconds for establishing an HTTP connection to a remote server. */
		public static final Pref<Integer> CORE_HTTP_CONNECT_TIMEOUT =
				create("pref_http_connect_timeout", "Connection timeout (in ms)", 20000, IType.INT, true)
						.in(Network.NAME, Network.HTTP);

		/** The timeout in milliseconds for reading data from an established HTTP connection. */
		public static final Pref<Integer> CORE_HTTP_READ_TIMEOUT =
				create("pref_http_read_timeout", "Read timeout (in ms)", 20000, IType.INT, true).in(Network.NAME,
						Network.HTTP);

		/** The number of times GAMA should retry a failed HTTP connection attempt before giving up. */
		public static final Pref<Integer> CORE_HTTP_RETRY_NUMBER =
				create("pref_http_retry_number", "Number of times to retry if connection cannot be established", 3,
						IType.INT, true).in(Network.NAME, Network.HTTP);

		/**
		 * Whether the local disk cache of files downloaded from the web should be emptied the next time GAMA
		 * makes an HTTP request.
		 */
		public static final Pref<Boolean> CORE_HTTP_EMPTY_CACHE =
				create("pref_http_empty_cache", "Empty the local cache of files downloaded from the web", true,
						IType.BOOL, true).in(Network.NAME, Network.HTTP);

		/** The name of the "Random number generation" group within the Data and Operators tab. */
		public static final String RNG = "Random number generation";

		/** The default pseudo-random number generator algorithm used by new simulations (e.g. Mersenne Twister). */
		public static final Pref<String> CORE_RNG =
				create("pref_rng_name", "Default random number generator", IKeyword.MERSENNE, IType.STRING, true)
						.among(Generators.names()).in(NAME, RNG);

		/**
		 * Whether a default global seed should be used for the RNG instead of a random one. Activates
		 * {@link #CORE_SEED}.
		 */
		public static final Pref<Boolean> CORE_SEED_DEFINED =
				create("pref_rng_define_seed", "Define a default seed", false, IType.BOOL, true)
						.activates("pref_rng_default_seed").in(NAME, RNG);

		/**
		 * The default seed value for the RNG when {@link #CORE_SEED_DEFINED} is {@code true}. A value of {@code 0}
		 * is treated as "undefined" and causes a random seed to be chosen.
		 */
		public static final Pref<Double> CORE_SEED =
				create("pref_rng_default_seed", "Default seed value (0 is undefined)", 1d, IType.FLOAT, true).in(NAME,
						RNG);

		/** The name of the "Management of dates" group within the Data and Operators tab. */
		public static final String DATES = "Management of dates";

		/**
		 * The name of the GIS / GeoTools CRS (Coordinate Reference Systems) group. The label also contains a
		 * reference URL for EPSG codes.
		 */
		public static final String GEOTOOLS =
				"GIS Coordinate Reference Systems (http://spatialreference.org/ref/epsg/ for EPSG codes)";

		/**
		 * Whether GAMA should automatically detect the appropriate CRS when projecting GIS data, rather than
		 * relying on a user-specified CRS.
		 */
		public static final Pref<Boolean> LIB_TARGETED = create("pref_gis_auto_crs",
				"Let GAMA find which CRS to use to project GIS data", true, IType.BOOL, true).in(NAME, GEOTOOLS);

		/**
		 * Whether GIS data without an associated {@code .prj} file or explicit CRS should be assumed to already be
		 * in the target projected CRS rather than needing reprojection. When active, deactivates
		 * {@code pref_gis_initial_crs}.
		 */
		public static final Pref<Boolean> LIB_PROJECTED = create("pref_gis_same_crs",
				"When no .prj file or CRS is supplied, consider GIS data to be already projected in this CRS", true,
				IType.BOOL, true).deactivates("pref_gis_initial_crs").in(NAME, GEOTOOLS);

		/**
		 * Whether GIS data should be saved using the current simulation CRS when no explicit CRS is provided at
		 * export time. When active, deactivates {@code pref_gis_output_crs}.
		 */
		public static final Pref<Boolean> LIB_USE_DEFAULT =
				create("pref_gis_save_crs", "When no CRS is provided, save the GIS data with the current CRS", true,
						IType.BOOL, true).deactivates("pref_gis_output_crs").in(NAME, GEOTOOLS);

		/**
		 * The EPSG code of the default target CRS used when projecting GIS data. Also used as the fallback CRS when
		 * no projection information is available in the source file. The listener rejects unknown EPSG codes.
		 */
		public static final Pref<Integer> LIB_TARGET_CRS = create("pref_gis_default_crs",
				"...or use the following EPSG code (the one that will also be used if no projection information is found)",
				32648, IType.INT, true).in(NAME, GEOTOOLS)
						.addChangeListener((IPreferenceBeforeChangeListener<Integer>) newValue -> {
							final var codes = CRS.getSupportedCodes(newValue.toString());
							if (codes.isEmpty()) return false;
							return true;
						});

		/**
		 * The EPSG code assumed for the source (input) CRS of GIS data when no {@code .prj} file is found. Used
		 * when {@link #LIB_PROJECTED} is inactive. The listener rejects unknown EPSG codes.
		 */
		public static final Pref<Integer> LIB_INITIAL_CRS =
				create("pref_gis_initial_crs", "...or use the following CRS (EPSG code)", 4326, IType.INT, true)
						.in(NAME, GEOTOOLS).addChangeListener((IPreferenceBeforeChangeListener<Integer>) newValue -> {
							final var codes = CRS.getSupportedCodes(newValue.toString());
							if (codes.isEmpty()) return false;
							return true;
						});

		/**
		 * The EPSG code to use for the output CRS when saving GIS data without an explicit CRS. Used when
		 * {@link #LIB_USE_DEFAULT} is inactive. The listener rejects unknown EPSG codes.
		 */
		public static final Pref<Integer> LIB_OUTPUT_CRS =
				create("pref_gis_output_crs", "... or use this following CRS (EPSG code)", 4326, IType.INT, true)
						.in(NAME, GEOTOOLS).addChangeListener((IPreferenceBeforeChangeListener<Integer>) newValue -> {
							final var codes = CRS.getSupportedCodes(newValue.toString());
							if (codes.isEmpty()) return false;
							return true;
						});

		/** The character used to delimit string values within CSV files (default: {@code "}). */
		public static final Pref<String> CSV_STRING_QUALIFIER =
				GamaPreferences.create("pref_csv_string_qualifier", "Default separator for strings",
						String.valueOf(StringUtils.Letters.QUOTE), IType.STRING, true).in(NAME, "CSV Files");

		/** The Constant CSV_SEPARATOR. */
		public static final Pref<String> CSV_SEPARATOR =
				create("pref_csv_separator", "Default separator for fields", String.valueOf(StringUtils.Letters.COMMA),
						IType.STRING, true).in(GamaPreferences.External.NAME, "CSV Files");

		/**
		 * Whether the special GAML float constant {@code #infinity} should be written and parsed as the JSON string
		 * {@code "Infinity"} ({@code true}) or as the bare JSON literal {@code Infinity} ({@code false}).
		 */
		public static final Pref<Boolean> JSON_INFINITY =
				create("pref_json_infinity_as_string", "Write and parse #infinity as", true, IType.BOOL, true)
						.withLabels("string \"Infinity\"", "literal Infinity")
						.withColors(() -> LIGHT_GRAY, () -> LIGHT_GRAY).in(NAME, "JSON Format");

		/**
		 * Whether the special GAML float constant {@code #nan} should be written and parsed as the JSON string
		 * {@code "NaN"} ({@code true}) or as the bare JSON literal {@code NaN} ({@code false}).
		 */
		public static final Pref<Boolean> JSON_NAN =
				create("pref_json_nan_as_string", "Write and parse #nan as", true, IType.BOOL, true)
						.withLabels("string \"NaN\"", "literal NaN").withColors(() -> LIGHT_GRAY, () -> LIGHT_GRAY)
						.in(NAME, "JSON Format");

		/**
		 * When a JSON integer value overflows the 32-bit int range, whether it should be parsed as a GAML
		 * {@code float} ({@code true}) or as a {@code string} ({@code false}).
		 */
		public static final Pref<Boolean> JSON_INT_OVERFLOW = create("pref_json_int_overflow_as_double",
				"In case of an int overflow, parse the item as a", true, IType.BOOL, true).withLabels("float", "string")
						.withColors(() -> LIGHT_GRAY, () -> LIGHT_GRAY).in(NAME, "JSON Format");
	}

	/**
	 * The {@code Experimental} class groups all GAMA preferences that expose experimental or advanced features:
	 * spatial index optimizations, expression constant folding, path computation shortcuts, tolerance settings,
	 * object pooling, and miscellaneous graphical optimizations. These features are not fully tested and may be
	 * changed or removed in future versions.
	 */
	public static class Experimental {

		/** The display name of the "Advanced" tab shown in the GAMA preferences dialog. */
		public static final String NAME = "Advanced";

		/**
		 * The label for the experimental features group, warning users that features within have not been fully
		 * tested.
		 */
		public static final String CATEGORY =
				" These features have not been fully tested. Enable them at your own risks.";

		/** The label for the safe optimizations group within the Advanced tab. */
		public static final String OPTIMIZATIONS = "These optimizations are considered safe";

		/**
		 * The label for the graphical optimizations group within the Advanced tab, noting that some optimizations
		 * can produce visual oddities.
		 */
		public static final String GRAPHICAL =
				"Various graphics optimizations to speed up displays and/or reduce memory usage. Some can produce visual oddities";

		/**
		 * Whether GAMA should automatically detect and add missing plug-in dependencies when a model is opened
		 * for editing. Hidden from the preferences UI.
		 */
		public static final Pref<Boolean> REQUIRED_PLUGINS = create("pref_required_plugins",
				"Automatically add the plugins required to compile and run a model when editing it", false, IType.BOOL,
				false).in(NAME, CATEGORY).hidden();

		/**
		 * Whether the spatial quadtree index should use a lazy insertion strategy (agents are added only when
		 * spatial queries require it) rather than eager insertion. Still experimental.
		 */
		public static final Pref<Boolean> QUADTREE_OPTIMIZATION = create("pref_optimize_quadtree",
				"Optimize spatial queries: add agents only when necessary in the quadtree (still experimental)", false,
				IType.BOOL, true).in(NAME, CATEGORY);

		/**
		 * Whether operations on the spatial index (quadtree) should be synchronized to prevent concurrency errors.
		 * Recommended for interactive models or parallel models. May slow down simulations with many mobile agents.
		 */
		public static final Pref<Boolean> QUADTREE_SYNCHRONIZATION = create("pref_synchronize_quadtree",
				"Forces the spatial index to synchronize its operations. Useful for interactive models where the users interfere or parallel models with concurrency errors. Note that it may slow down simulations with a lot of mobile agents",
				true, IType.BOOL, true).in(NAME, CATEGORY);

		/**
		 * Whether constant GAML expressions should be evaluated at compile time and replaced by their results
		 * (constant folding). Enabling this triggers a clean rebuild of all models. Hidden from the preferences UI.
		 */
		public static final Pref<Boolean> CONSTANT_OPTIMIZATION = create("pref_optimize_constant_expressions",
				"Optimize constant expressions (experimental, performs a rebuild of models)", false, IType.BOOL, true)
						.in(NAME, CATEGORY).onChange(v -> {
							try {
								GAMA.getWorkspaceManager().getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD,
										null);
							} catch (CoreException e) {}
						}).hidden();

		/**
		 * Whether an optimized but potentially approximate path computation algorithm should be used for the GAML
		 * {@code path_between} operators and {@code goto} action. May cause agents to "jump" over obstacles in
		 * some edge cases.
		 */
		public static final Pref<Boolean> PATH_COMPUTATION_OPTIMIZATION = create("pref_optimize_path_computation",
				"Optimize the path computation operators and goto action (but with possible 'jump' issues)", false,
				IType.BOOL, true).in(NAME, OPTIMIZATIONS);

		/**
		 * The tolerance (epsilon) used when comparing two GAML {@code point} values for equality. A value of
		 * {@code 0.0} means exact comparison.
		 */
		public static final Pref<Double> TOLERANCE_POINTS =
				create("pref_point_tolerance", "Tolerance for the comparison of points", 0.0, IType.FLOAT, true)
						.in(NAME, OPTIMIZATIONS);

		/**
		 * Whether shapefile data should be mapped and cached entirely in memory for faster repeated access.
		 * Disable this if you are working with shapefiles that change on disk during a simulation.
		 */
		public static final Pref<Boolean> SHAPEFILES_IN_MEMORY = create("pref_shapefiles_in_memory",
				"Mapping and caching of shapefiles in memory (optimises access to shapefile data in exchange for increased memory usage). Disable this property if you are dealing with shapefiles that change frequently",
				true, IType.BOOL, true).in(NAME, OPTIMIZATIONS);

		/**
		 * The default I/O buffering strategy applied to the GAML {@code save} statement. Corresponds to the
		 * preference key {@link GamaPreferences#PREF_SAVE_BUFFERING_STRATEGY}.
		 */
		public static final Pref<String> DEFAULT_SAVE_BUFFERING_STRATEGY = create(PREF_SAVE_BUFFERING_STRATEGY,
				"Default buffering strategy for the save statement", BufferingUtils.NO_BUFFERING, IType.STRING, true)
						.among(BufferingUtils.BUFFERING_STRATEGIES.stream().toList()).in(NAME, OPTIMIZATIONS);

		/**
		 * The default I/O buffering strategy applied to the GAML {@code write} statement. Corresponds to the
		 * preference key {@link GamaPreferences#PREF_WRITE_BUFFERING_STRATEGY}.
		 */
		public static final Pref<String> DEFAULT_WRITE_BUFFERING_STRATEGY = create(PREF_WRITE_BUFFERING_STRATEGY,
				"Default buffering strategy for the write statement", BufferingUtils.NO_BUFFERING, IType.STRING, true)
						.among(BufferingUtils.BUFFERING_STRATEGIES.stream().toList()).in(NAME, OPTIMIZATIONS);

		/**
		 * Whether object pooling should be used to reuse short-lived GAML runtime objects and reduce GC pressure.
		 * Still experimental; may cause side effects if object state is not correctly reset.
		 */
		public static final Pref<Boolean> USE_POOLING =
				create("pref_use_pooling", "Use object pooling to reduce memory usage (still experimental)", false,
						IType.BOOL, true).in(NAME, CATEGORY);

	}

	/**
	 * Factory method that creates and registers a new {@link Pref} with an eagerly computed initial value. The
	 * preference is immediately initialized to {@code value}, named {@code title}, and placed in the default
	 * {@link Interface} tab. It is then registered with the global preference store via {@link #register(Pref)}.
	 *
	 * @param <T>
	 *            the type of the preference value
	 * @param key
	 *            the unique preference key; must not be {@code null}
	 * @param title
	 *            the human-readable label shown in the preferences UI
	 * @param value
	 *            the initial (default) value
	 * @param type
	 *            the GAML type identifier (e.g. {@link gama.api.gaml.types.IType#INT})
	 * @param inGaml
	 *            whether this preference should be accessible from GAML as a variable of the {@code platform}
	 *            species
	 * @return the newly created and registered {@link Pref}
	 */
	public static <T> Pref<T> create(final String key, final String title, final T value, final int type,
			final boolean inGaml) {
		final var e = new Pref<T>(key, type, inGaml).named(title).in(Interface.NAME, "").init(value);
		register(e);
		return e;
	}

	/**
	 * Factory method that creates and registers a new {@link Pref} with a lazily evaluated initial value. The
	 * {@code provider} supplier is not invoked until the preference value is first read, avoiding eager
	 * computation of potentially expensive defaults (e.g., font or color objects). The preference is named
	 * {@code title} and placed in the default {@link Interface} tab, then registered with the global preference
	 * store via {@link #register(Pref)}.
	 *
	 * @param <T>
	 *            the type of the preference value
	 * @param key
	 *            the unique preference key; must not be {@code null}
	 * @param title
	 *            the human-readable label shown in the preferences UI
	 * @param provider
	 *            a {@link Supplier} that computes the initial (default) value on demand
	 * @param type
	 *            the GAML type identifier (e.g. {@link gama.api.gaml.types.IType#COLOR})
	 * @param inGaml
	 *            whether this preference should be accessible from GAML as a variable of the {@code platform}
	 *            species
	 * @return the newly created and registered {@link Pref}
	 */
	public static <T> Pref<T> create(final String key, final String title, final Supplier<T> provider, final int type,
			final boolean inGaml) {
		final var e = new Pref<T>(key, type, inGaml).named(title).in(Interface.NAME, "").init(provider);
		register(e);
		return e;
	}

	/**
	 * Registers the given {@link Pref} with the global preference store and, when applicable, exposes it as a
	 * variable of the GAML {@code platform} built-in species. If the preference's key is {@code null} this method
	 * returns silently.
	 *
	 * @param gp
	 *            the preference to register; must not be {@code null}
	 */
	private static void register(final Pref<?> gp) {
		final String key = gp.getKey();
		if (key == null) return;
		GAMA.getPreferenceStore().register(gp);
		// Adds the preferences to the platform species if it is already created
		final ISpeciesDescription.Platform spec = (Platform) GamaMetaModel.getSpeciesDescription(IKeyword.PLATFORM);
		if (spec != null && !spec.hasAttribute(key) && gp.inGaml()) { spec.addPrefAsVariable(gp); }
	}

	/**
	 * Organize prefs.
	 *
	 * @return the map
	 */
	/**
	 * Builds a hierarchical map of all visible registered preferences, organized first by tab name, then by group
	 * name, and finally as an ordered list of {@link Pref} instances within each group. The outer map is pre-seeded
	 * with tabs in {@link #ORDER_OF_PREFERENCES} order; additional tabs encountered during iteration are appended
	 * at the end. Hidden preferences are excluded from the result.
	 *
	 * @return a nested {@code Map<tabName, Map<groupName, List<Pref>>>} suitable for rendering the preferences UI;
	 *         never {@code null}
	 */
	public static Map<String, Map<String, List<Pref<?>>>> organizePrefs() {
		final Map<String, Map<String, List<Pref<?>>>> result = GamaMapFactory.create();
		for (String tab : ORDER_OF_PREFERENCES) { result.put(tab, GamaMapFactory.create()); }
		for (final Pref<?> e : GAMA.getPreferenceStore().getPreferences()) {
			if (e.isHidden()) { continue; }
			final String tab = e.getTab();
			var groups = result.get(tab);
			if (groups == null) {
				groups = GamaMapFactory.create();
				result.put(tab, groups);
			}
			final String group = e.getGroup();
			var in_group = groups.get(group);
			if (in_group == null) {
				in_group = new ArrayList<>();
				groups.put(group, in_group);
			}
			in_group.add(e);
		}
		return result;
	}

	/**
	 * Applies a batch of new preference values and persists them to the backing store. For each entry in
	 * {@code modelValues}, the corresponding registered {@link Pref} (if found) is updated via
	 * {@link Pref#set(Object)} and then written to the store via {@link IGamaPreferenceStore#write(Pref)}.
	 * Entries whose key does not match any registered preference are silently ignored.
	 *
	 * @param modelValues
	 *            a map of preference key to new value; must not be {@code null}
	 */
	public static void setNewPreferences(final Map<String, Object> modelValues) {
		for (final String name : modelValues.keySet()) {
			final Pref<Object> e = GAMA.getPreferenceStore().get(name);
			if (e == null) { continue; }
			e.set(modelValues.get(name));
			GAMA.getPreferenceStore().write(e);
		}
	}

	/**
	 * Resets all preferences to their compiled-in default values by clearing the entire backing store via
	 * {@link IGamaPreferenceStore#clear()}. After this call the preferences will be re-read from their default
	 * value suppliers the next time they are accessed.
	 *
	 * @param modelValues
	 *            currently unused; retained for API compatibility
	 */
	public static void revertToDefaultValues(final Map<String, Object> modelValues) {
		GAMA.getPreferenceStore().clear();
	}

	/**
	 * The canonical display order of preference tabs in the GAMA preferences dialog. Tabs are listed in
	 * the order they should appear from left to right (or top to bottom).
	 */
	public final static List<String> ORDER_OF_PREFERENCES = Arrays.asList(Interface.NAME, Theme.NAME, Modeling.NAME,
			Runtime.NAME, Displays.NAME, Network.NAME, External.NAME, Experimental.NAME);

}
