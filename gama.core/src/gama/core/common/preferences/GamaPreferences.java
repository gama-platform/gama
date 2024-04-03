/*******************************************************************************************************
 *
 * GamaPreferences.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.common.preferences;

import static gama.core.common.preferences.GamaPreferenceStore.getStore;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.geotools.referencing.CRS;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.IPreferenceChangeListener.IPreferenceBeforeChangeListener;
import gama.core.common.preferences.Pref.ValueProvider;
import gama.core.common.util.FileUtils;
import gama.core.common.util.RandomUtils;
import gama.core.common.util.StringUtils;
import gama.core.outputs.layers.properties.ICameraDefinition;
import gama.core.runtime.GAMA;
import gama.core.runtime.PlatformHelper;
import gama.core.util.GamaColor;
import gama.core.util.GamaFont;
import gama.core.util.GamaMapFactory;
import gama.core.util.file.GenericFile;
import gama.core.util.file.IGamaFile;
import gama.core.util.file.csv.AbstractCSVManipulator;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.kernel.GamaMetaModel;
import gama.gaml.operators.Strings;
import gama.gaml.types.IType;
import one.util.streamex.StreamEx;

/**
 * The Class GamaPreferences.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 19 août 2023
 */

@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPreferences {

	/** The Constant BASIC_COLORS. */
	public static final ValueProvider<GamaColor>[] BASIC_COLORS = new ValueProvider[] {
			() -> GamaColor.get(74, 97, 144), () -> GamaColor.get(66, 119, 42), () -> GamaColor.get(83, 95, 107),
			() -> GamaColor.get(195, 98, 43), () -> GamaColor.get(150, 132, 106) };

	/** The Constant DIVERGING_COLORS. */
	public static final ValueProvider<GamaColor>[] QUALITATIVE_COLORS = new ValueProvider[] {
			() -> GamaColor.get(166, 206, 227), () -> GamaColor.get(31, 120, 180), () -> GamaColor.get(178, 223, 138),
			() -> GamaColor.get(51, 160, 44), () -> GamaColor.get(251, 154, 153), () -> GamaColor.get(227, 26, 28),
			() -> GamaColor.get(253, 191, 111), () -> GamaColor.get(255, 127, 0), () -> GamaColor.get(202, 178, 214) };

	/** The Constant DIVERGING_COLORS. */
	public static final ValueProvider<GamaColor>[] DIVERGING_COLORS = new ValueProvider[] {
			() -> GamaColor.get(84, 48, 5), () -> GamaColor.get(140, 81, 10), () -> GamaColor.get(191, 129, 45),
			() -> GamaColor.get(223, 194, 125), () -> GamaColor.get(246, 232, 195), () -> GamaColor.get(245, 245, 245),
			() -> GamaColor.get(199, 234, 229), () -> GamaColor.get(128, 205, 193), () -> GamaColor.get(53, 151, 143),
			() -> GamaColor.get(1, 102, 94), () -> GamaColor.get(0, 60, 48) };

	/**
	 *
	 * Interface tab
	 *
	 */
	public static class Interface {

		/** The Constant NAME. */
		public static final String NAME = "Interface";
		/**
		 * Startup
		 */
		public static final String STARTUP = "Startup";

		/** The Constant CORE_SHOW_PAGE. */
		public static final Pref<Boolean> CORE_SHOW_PAGE =
				create("pref_show_welcome_page", "Display welcome page", true, IType.BOOL, false).in(NAME, STARTUP);

		/** The Constant CORE_REMEMBER_WINDOW. */
		public static final Pref<Boolean> CORE_REMEMBER_WINDOW =
				create("pref_remember_window", "Remember GAMA window size and position", true, IType.BOOL, false)
						.in(NAME, STARTUP).deactivates("pref_show_maximized");

		/** The Constant CORE_SHOW_MAXIMIZED. */
		public static final Pref<Boolean> CORE_SHOW_MAXIMIZED =
				create("pref_show_maximized", "Maximize GAMA window", true, IType.BOOL, false).in(NAME, STARTUP);

		/** The Constant CORE_ASK_REBUILD. */
		public static final Pref<Boolean> CORE_ASK_REBUILD =
				create("pref_ask_rebuild", "Ask before rebuilding a corrupted workspace", true, IType.BOOL, false)
						.in(NAME, STARTUP);

		/** The Constant CORE_ASK_OUTDATED. */
		public static final Pref<Boolean> CORE_ASK_OUTDATED = create("pref_ask_outdated",
				"Ask before using a workspace created by another version", true, IType.BOOL, false).in(NAME, STARTUP);

		/** The Constant CORE_ASK_REBUILD. */
		public static final Pref<Boolean> CORE_STARTUP_MODEL =
				create("pref_startup_model", "Open a model or an experiment at startup", false, IType.BOOL, false)
						.in(NAME, STARTUP).activates("pref_default_model", "pref_default_experiment");

		/** The Constant CORE_DEFAULT_MODEL. */
		public static final Pref<? extends IGamaFile> CORE_DEFAULT_MODEL = create("pref_default_model",
				"Choose the model to open at startup", () -> new GenericFile("Enter path", false), IType.FILE, false)
						.in(NAME, STARTUP).restrictToWorkspace().withExtensions("gaml", "experiment")
						.refreshes("pref_default_experiment").activates("pref_default_experiment");

		/** The Constant CORE_DEFAULT_MODEL. */
		public static final Pref<String> CORE_DEFAULT_EXPERIMENT =
				create("pref_default_experiment", "Choose the experiment to run at startup", "", IType.STRING, false)
						.in(NAME, STARTUP).among(() -> {
							List result = new ArrayList();
							if (CORE_STARTUP_MODEL.getValue()) {
								IGamaFile file = CORE_DEFAULT_MODEL.getValue();
								final URI uriModel = FileUtils.getURI(file.getOriginalPath(), null);
								if (uriModel == null) return result;
								result.addAll(GAML.getInfo(uriModel).getExperiments());
							}
							return result;
						});
		/**
		 * Menus
		 */
		public static final String MENUS = "Menus";

		/** The Constant CORE_MENU_SIZE. */
		public static final Pref<Integer> CORE_MENU_SIZE =
				create("pref_menu_size", "Break down agents in menus every", 50, IType.INT, false).between(10, 1000)
						.in(NAME, MENUS);
		/**
		 * Console
		 */
		public static final String CONSOLE = "Console";

		/** The Constant CORE_CONSOLE_SIZE. */
		public static final Pref<Integer> CORE_CONSOLE_SIZE = create("pref_console_size",
				"Max. number of characters to display (-1 = unlimited)", 20000, IType.INT, true).in(NAME, CONSOLE);

		/** The Constant CORE_CONSOLE_BUFFER. */
		public static final Pref<Integer> CORE_CONSOLE_BUFFER =
				create("pref_console_buffer", "Max. number of characters to keep when paused (-1 = unlimited)", 20000,
						IType.INT, true).in(NAME, CONSOLE);

		/** The Constant CORE_CONSOLE_WRAP. */
		public static final Pref<Boolean> CORE_CONSOLE_WRAP =
				create("pref_console_wrap", "Wrap long lines (can slow down output)", false, IType.BOOL, true).in(NAME,
						CONSOLE);
		/**
		 * Appearance
		 */
		public static final String APPEARANCE = "Appearance";

		/** The Constant NAVIGATOR. */
		public static final String NAVIGATOR = "Navigator";
		/**
		 * Simulation Interface
		 */
		public static final String SIMULATIONS = "Simulations";

		/** The Constant CORE_SIMULATION_NAME. */
		public static final Pref<Boolean> CORE_SIMULATION_NAME = create("pref_append_simulation_name",
				"Append the name of simulations to their outputs", false, IType.BOOL, true).in(NAME, SIMULATIONS);

		/** The Constant SIMULATION_COLORS. */
		// public static final Pref<GamaColor>[] SIMULATION_COLORS = new Pref[5];

		/** The Constant COLORS. */
		private static GamaColor[] SIMULATION_COLORS = null;

		/**
		 * Sets the pivot.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param c
		 *            the new pivot
		 * @date 16 août 2023
		 */
		static void setPivot(final GamaColor c) {
			if (!PIVOT.equals(CORE_SIMULATION_COLOR.getValue())) return;
			SIMULATION_COLORS = new GamaColor[9];
			SIMULATION_COLORS[0] = GamaColor.get(c.darker().darker().darker().darker());
			SIMULATION_COLORS[1] = GamaColor.get(c.darker().darker().darker());
			SIMULATION_COLORS[2] = GamaColor.get(c.darker().darker());
			SIMULATION_COLORS[3] = GamaColor.get(c.darker());
			SIMULATION_COLORS[4] = GamaColor.get(c);
			SIMULATION_COLORS[5] = GamaColor.get(c.brighter());
			SIMULATION_COLORS[6] = GamaColor.get(c.brighter().brighter());
			SIMULATION_COLORS[7] = GamaColor.get(c.brighter().brighter().brighter());
			SIMULATION_COLORS[8] = GamaColor.get(c.brighter().brighter().brighter().brighter());
		}

		/**
		 * Gets the color for simulation.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param index
		 *            the index
		 * @return the color for simulation
		 * @date 16 août 2023
		 */
		public static GamaColor getColorForSimulation(final int index) {
			if (SIMULATION_COLORS == null) { setColorScheme(CORE_SIMULATION_COLOR.getValue()); }
			return SIMULATION_COLORS[index % SIMULATION_COLORS.length];
		}

		/**
		 * Sets the color scheme.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param scheme
		 *            the new color scheme
		 * @date 19 août 2023
		 */
		public static void setColorScheme(final String scheme) {
			if (DIVERGING.equals(scheme)) {
				SIMULATION_COLORS = new GamaColor[DIVERGING_COLORS.length];
				for (int i = 0; i < DIVERGING_COLORS.length; i++) { SIMULATION_COLORS[i] = DIVERGING_COLORS[i].get(); }
			} else if (BASIC.equals(scheme)) {
				SIMULATION_COLORS = new GamaColor[BASIC_COLORS.length];
				for (int i = 0; i < BASIC_COLORS.length; i++) { SIMULATION_COLORS[i] = BASIC_COLORS[i].get(); }
			} else if (QUALITATIVE.equals(scheme)) {
				SIMULATION_COLORS = new GamaColor[QUALITATIVE_COLORS.length];
				for (int i = 0; i < QUALITATIVE_COLORS.length; i++) {
					SIMULATION_COLORS[i] = QUALITATIVE_COLORS[i].get();
				}
			} else {
				setPivot(CORE_PIVOT_COLOR.getValue());
			}
		}

		/** The Constant DIVERGING. */
		static final String DIVERGING = "Diverging (11 colors)";

		/** The Constant BASIC. */
		static final String BASIC = "Basic (5 colors)";

		/** The Constant QUALITATIVE. */
		static final String QUALITATIVE = "Qualitative (9 colors)";

		/** The Constant PIVOT. */
		static final String PIVOT = "Based on pivot color (9 colors)";

		/** The Constant CORE_PIVOT_COLOR. */
		public static final Pref<String> CORE_SIMULATION_COLOR =
				create("pref_simulation_colors", "Default color scheme for simulations in UI", DIVERGING, IType.STRING,
						true).among(BASIC, DIVERGING, QUALITATIVE, PIVOT).in(NAME, SIMULATIONS)
								.onChange(Interface::setColorScheme);

		/** The Constant CORE_PIVOT_COLOR. */
		public static final Pref<GamaColor> CORE_PIVOT_COLOR =
				create("pref_simulation_color", "Pivot color of simulations", GamaColor.get(64, 224, 208), IType.COLOR,
						true).in(NAME, SIMULATIONS).onChange(Interface::setPivot);

	}

	/**
	 *
	 * Modeling tab
	 *
	 */
	public static class Modeling {

		/** The Constant NAME. */
		public static final String NAME = "Editors";

		/** The Constant TEXT. */
		public static final String TEXT = "Edition";
		/**
		 * Options
		 */
		public static final String OPTIONS = "Options";
		/**
		 * Validation
		 */
		public static final Pref<Boolean> WARNINGS_ENABLED =
				create("pref_editor_enable_warnings", "Show warning markers in the editor", true, IType.BOOL, false)
						.in(NAME, OPTIONS);

		/** The Constant INFO_ENABLED. */
		public static final Pref<Boolean> INFO_ENABLED =
				create("pref_editor_enable_infos", "Show information markers in the editor", true, IType.BOOL, false)
						.in(NAME, OPTIONS);

		/** The Constant EDITOR_PERSPECTIVE_SAVE. */
		public static final Pref<Boolean> EDITOR_PERSPECTIVE_SAVE =
				create("pref_editor_perspective_save", "Save all editors when switching perspectives", true, IType.BOOL,
						false).in(Modeling.NAME, Modeling.OPTIONS).activates("pref_editor_ask_save");

		/** The Constant EDITOR_PERSPECTIVE_HIDE. */
		public static final Pref<Boolean> EDITOR_PERSPECTIVE_HIDE = create("pref_editor_perspective_hide",
				"Hide editors when switching to simulation perspectives (can be overriden in the 'layout' statement)",
				true, IType.BOOL, false).in(Modeling.NAME, Modeling.OPTIONS);

		/** The operators menu sort. */
		public static Pref<String> OPERATORS_MENU_SORT =
				create("pref_menu_operators_sort", "Sort operators menu by", "Category", IType.STRING, false)
						.among("Name", "Category").in(Interface.NAME, Interface.MENUS);

		/** The Constant CORE_CLOSE_QUOTE. */
		public static final Pref<Boolean> CORE_CLOSE_QUOTE =
				create("pref_editor_close_quote", "Automatically close single quotes — '..'", true, IType.BOOL, false)
						.in(NAME, TEXT);

		/** The Constant CORE_CLOSE_DOUBLE. */
		public static final Pref<Boolean> CORE_CLOSE_DOUBLE = create("pref_editor_close_double",
				"Automatically close double quotes — \"..\"", true, IType.BOOL, false).in(NAME, TEXT);

		/** The Constant CORE_CLOSE_CURLY. */
		public static final Pref<Boolean> CORE_CLOSE_CURLY =
				create("pref_editor_close_curly", "Automatically close curly brackets — {..}", true, IType.BOOL, false)
						.in(NAME, TEXT);

		/** The Constant CORE_CLOSE_SQUARE. */
		public static final Pref<Boolean> CORE_CLOSE_SQUARE = create("pref_editor_close_square",
				"Automatically close square brackets — [..]", true, IType.BOOL, false).in(NAME, TEXT);

		/** The Constant CORE_CLOSE_PARENTHESES. */
		public static final Pref<Boolean> CORE_CLOSE_PARENTHESES = create("pref_editor_close_parentheses",
				"Automatically close parentheses — (..)", true, IType.BOOL, false).in(NAME, TEXT);

		/** The Constant EDITOR_CLEAN_UP. */
		public static final Pref<Boolean> EDITOR_CLEAN_UP =
				create("pref_editor_save_format", "Apply formatting on save", false, IType.BOOL, false).in(NAME,
						GamaPreferences.Modeling.OPTIONS);

		/** The Constant EDITOR_SAVE. */
		public static final Pref<Boolean> EDITOR_SAVE =
				GamaPreferences
						.create("pref_editor_save_all", "Save all editors before lauching an experiment", true,
								IType.BOOL, false)
						.in(NAME, GamaPreferences.Modeling.OPTIONS).activates("pref_editor_ask_save");

		/** The Constant EDITOR_DRAG_RESOURCES. */
		public static final Pref<Boolean> EDITOR_DRAG_RESOURCES = create("pref_editor_drag_resources",
				"Drag files and resources as references in GAML files", true, IType.BOOL, false).in(NAME, OPTIONS);

		/** The Constant EDITOR_SAVE_ASK. */
		public static final Pref<Boolean> EDITOR_SAVE_ASK =
				create("pref_editor_ask_save", "Ask before saving each file", false, IType.BOOL, false).in(NAME,
						OPTIONS);

		/** The Constant EDITBOX_ENABLED. */
		public static final Pref<Boolean> EDITBOX_ENABLED = GamaPreferences
				.create("pref_editor_editbox_on", "Turn on colorization of code sections", false, IType.BOOL, false)
				.in(NAME, TEXT);

		/** The Constant EDITOR_BASE_FONT. */
		public static final Pref<GamaFont> EDITOR_BASE_FONT = GamaPreferences
				.create("pref_editor_font", "Font of editors", (GamaFont) null, IType.FONT, false).in(NAME, TEXT);

		/** The Constant EDITOR_BACKGROUND_COLOR. */
		public static final Pref<GamaColor> EDITOR_BACKGROUND_COLOR = create("pref_editor_background_color",
				"Background color of editors", (GamaColor) null, IType.COLOR, false).in(NAME, TEXT);

		/** The Constant EDITOR_MARK_OCCURRENCES. */
		public static final Pref<Boolean> EDITOR_MARK_OCCURRENCES = GamaPreferences
				.create("pref_editor_mark_occurrences", "Mark occurrences of symbols", true, IType.BOOL, false)
				.in(NAME, TEXT);

		/** The Constant EDITOR_EXPERIMENT_MENU. */
		public static final Pref<Boolean> EDITOR_EXPERIMENT_MENU =
				GamaPreferences
						.create("pref_editor_experiment_menu",
								"Always display experiments as a menu rather than buttons", false, IType.BOOL, false)
						.deactivates("pref_editor_collapse_buttons").in(NAME, TEXT);

		/** The Constant EDITOR_COLLAPSE_BUTTONS. */
		public static final Pref<Boolean> EDITOR_COLLAPSE_BUTTONS = create("pref_editor_collapse_buttons",
				"Display experiments as a menu when the combined width of the buttons exceed the width of the toolbar",
				PlatformHelper.isLinux(), IType.BOOL, false).in(NAME, TEXT);

		/** The Constant EDITOR_MINING. */
		public static final Pref<Boolean> EDITOR_MINING = create("pref_editor_mining",
				"Inline errors, warnings and information messages", true, IType.BOOL, false).in(NAME, TEXT);

	}

	/**
	 *
	 * Simulations tab
	 *
	 */
	public static class Simulations {

		/** The Constant NAME. */
		public static final String NAME = "Simulations";

	}

	/**
	 *
	 * Runtime tab
	 *
	 */
	public static class Runtime {

		/** The Constant NAME. */
		public static final String NAME = "Execution";

		/**
		 * General
		 */
		/**
		 * Running experiments
		 */
		public static final String EXECUTION = "Experiments";

		/** The Constant CORE_AUTO_RUN. */
		public static final Pref<Boolean> CORE_AUTO_RUN = create("pref_experiment_auto_run",
				"Auto-run experiments when they are launched", false, IType.BOOL, true).in(NAME, EXECUTION);

		/** The Constant CORE_ASK_CLOSING. */
		public static final Pref<Boolean> CORE_ASK_CLOSING =
				create("pref_experiment_ask_closing", "Ask to close the previous experiment when launching a new one",
						true, IType.BOOL, true).in(NAME, EXECUTION);

		/** The Constant CORE_ASK_FULLSCREEN. */
		public static final Pref<Boolean> CORE_ASK_FULLSCREEN =
				create("pref_experiment_ask_fullscreen", "Ask before entering fullscreen mode", false, IType.BOOL, true)
						.in(NAME, EXECUTION).hidden();

		/** The Constant CORE_SLIDER_TYPE. */
		public static final Pref<Boolean> CORE_SLIDER_TYPE = create("pref_experiment_type_slider",
				"Set the step duration slider incrementation to linear. If false set to logarithmic", true, IType.BOOL,
				true).in(NAME, EXECUTION);

		/** The Constant CORE_SYNC. */
		public static final Pref<Boolean> CORE_SYNC =
				create("pref_display_synchronized", "Synchronize outputs with the simulation", false, IType.BOOL, true)
						.in(NAME, EXECUTION);

		/** The Constant PARAMETERS. */
		public static final String PARAMETERS = "Parameters";

		/** The Constant CORE_EXPAND_PARAMS. */
		public static final Pref<Boolean> CORE_EXPAND_PARAMS = create("pref_experiment_expand_params",
				"Automatically expand the parameters categories", false, IType.BOOL, true).in(NAME, PARAMETERS);

		/** The Constant CORE_MONITOR_PARAMETERS. */
		public static final Pref<Boolean> CORE_MONITOR_PARAMETERS =
				create("pref_monitors_in_parameters", "Display monitors in the parameters view", true, IType.BOOL, true)
						.in(NAME, PARAMETERS);

		/** The Constant CORE_RND_EDITABLE. */
		public static final Pref<Boolean> CORE_RND_EDITABLE =
				create("pref_rng_in_parameters", "Include random number generation parameters in the parameters view",
						false, IType.BOOL, true).in(NAME, PARAMETERS);

		/**
		 * Concurrency
		 */
		public static final String CONCURRENCY = "Parallelism";
		/**
		 * Tests
		 */
		public static final String TESTS = "Tests";

		/** The Constant TESTS_SORTED. */
		public static final Pref<Boolean> TESTS_SORTED =
				create("pref_tests_sorted", "Sorts the results of tests by severity", false, IType.BOOL, false)
						.in(NAME, TESTS).withComment(", if true, aborted and failed tests are displayed first");
		// public static final Pref<Boolean> RUN_TESTS =
		// create("pref_run_tests", "Run tests after each update of the platform", false, IType.BOOL, false)
		/** The Constant START_TESTS. */
		// .in(NAME, TESTS).disabled().hidden();
		public static final Pref<Boolean> START_TESTS =
				create("pref_start_tests", "Run tests at each start of the platform", false, IType.BOOL, false).in(NAME,
						TESTS);

		/** The Constant USER_TESTS. */
		public static final Pref<Boolean> USER_TESTS =
				create("pref_user_tests", "Include user-defined tests in the tests suite", false, IType.BOOL, false)
						.in(NAME, TESTS).withComment(", if true, will run user models with 'test' experiments");

		/** The Constant FAILED_TESTS. */
		public static final Pref<Boolean> FAILED_TESTS = create("pref_failed_tests",
				"Only display (in the UI and in headless runs) failed and aborted tests", false, IType.BOOL, true)
						.in(NAME, TESTS).withComment(", if true, only aborted and failed tests are displayed");

		/** The Constant MEMORY. */
		public static final String MEMORY = "Memory";

		/** The Constant CORE_MEMORY_POLLING. */
		public static final Pref<Boolean> CORE_MEMORY_POLLING =
				create("pref_check_memory", "Monitor memory and emit a warning if it is low", true, IType.BOOL, true)
						.in(NAME, MEMORY).activates("pref_memory_threshold", "pref_memory_frequency");

		/** The Constant CORE_MEMORY_PERCENTAGE. */
		public static final Pref<Integer> CORE_MEMORY_PERCENTAGE =
				create("pref_memory_threshold", "Trigger warnings when the percentage of available memory is below", 20,
						IType.INT, true).in(NAME, MEMORY);

		/** The Constant CORE_MEMORY_FREQUENCY. */
		public static final Pref<Integer> CORE_MEMORY_FREQUENCY = create("pref_memory_frequency",
				"Interval (in seconds) at which memory should be monitored", 2, IType.INT, true).in(NAME, MEMORY);

		/** The Constant CORE_MEMORY_ACTION. */
		public static final Pref<Boolean> CORE_MEMORY_ACTION = create("pref_memory_action",
				"If true, when running out of memory, GAMA will try to close the experiment, otherwise it exits", true,
				IType.BOOL, true).in(NAME, MEMORY);
		/**
		 * Errors & warnings
		 */
		public static final String ERRORS = "Runtime errors";

		/** The Constant CORE_SHOW_ERRORS. */
		public static final Pref<Boolean> CORE_SHOW_ERRORS =
				create("pref_errors_display", "Show execution errors", true, IType.BOOL, true).in(NAME, ERRORS)
						.activates("pref_errors_number", "pref_errors_recent_first", "pref_display_show_errors");

		/** The Constant ERRORS_IN_DISPLAYS. */
		public static final Pref<Boolean> ERRORS_IN_DISPLAYS = create("pref_display_show_errors",
				"Show errors thrown in displays and outputs", false, IType.BOOL, true).in(NAME, ERRORS);

		/** The Constant CORE_ERRORS_NUMBER. */
		public static final Pref<Integer> CORE_ERRORS_NUMBER =
				create("pref_errors_number", "Number of errors to display", 10, IType.INT, true).in(NAME, ERRORS)
						.between(1, null);

		/** The Constant CORE_RECENT. */
		public static final Pref<Boolean> CORE_RECENT =
				create("pref_errors_recent_first", "Display most recent first", true, IType.BOOL, true).in(NAME,
						ERRORS);

		/** The Constant CORE_REVEAL_AND_STOP. */
		public static final Pref<Boolean> CORE_REVEAL_AND_STOP =
				create("pref_errors_stop", "Stop simulation at first error", true, IType.BOOL, true).in(NAME, ERRORS);

		/** The Constant CORE_WARNINGS. */
		public static final Pref<Boolean> CORE_WARNINGS =
				create("pref_errors_warnings_errors", "Treat warnings as errors", false, IType.BOOL, true).in(NAME,
						ERRORS);

		/** The Constant CORE_ERRORS_EDITOR_LINK. */
		public static final Pref<Boolean> CORE_ERRORS_EDITOR_LINK = create("pref_errors_in_editor",
				"Automatically open an editor and point at the faulty part of the model if an error or a warning is thrown",
				true, IType.BOOL, true).in(NAME, ERRORS);

		/** The Constant SERVER. */
		public final static String SERVER = "Server mode";

		/** The Constant CORE_SERVER_MODE. */
		public static final Pref<Boolean> CORE_SERVER_MODE =
				create("pref_enable_server", "Enables GAMA Server mode", true, IType.BOOL, true).in(NAME, SERVER)
						.activates("pref_server_port", "pref_server_ping", "pref_server_console");

		/** The Constant CORE_SERVER_PORT. */
		public static final Pref<Integer> CORE_SERVER_PORT =
				create("pref_server_port", "Port to which GAMA Server is listening", 1000, IType.INT, true).in(NAME,
						SERVER);

		/** The Constant CORE_SERVER_PORT. */
		public static final Pref<Integer> CORE_SERVER_PING =
				create("pref_server_ping", "Interval between two pings (-1 to disable)", 10000, IType.INT, true)
						.in(NAME, SERVER);

		/** The Constant CORE_SERVER_CONSOLE. */
		public static final Pref<Boolean> CORE_SERVER_CONSOLE =
				create("pref_server_console", "Send console outputs to clients", true, IType.BOOL, true).in(NAME,
						SERVER);

	}

	/**
	 * The Class Displays.
	 */
	public static class Displays {

		/** The Constant NAME. */
		public static final String NAME = "Displays";

		/** The Constant PRESENTATION. */
		public static final String PRESENTATION = "Presentation and Behavior of Graphical Display Views";
		/**
		 * Presentation
		 */
		public static final List<String> LAYOUTS = Arrays.asList("None", "Stacked", "Split", "Horizontal", "Vertical");

		/** The Constant CORE_DISPLAY_LAYOUT. */
		public static final Pref<String> CORE_DISPLAY_LAYOUT =
				create("pref_display_view_layout", "Default layout of display views", "Split", IType.STRING, true)
						.among(LAYOUTS.toArray(new String[LAYOUTS.size()])).in(NAME, PRESENTATION);

		/** The Constant CORE_DISPLAY_BORDER. */
		public static final Pref<Boolean> CORE_DISPLAY_BORDER =
				create("pref_display_show_border", "Display a border around display views", false, IType.BOOL, true)
						.in(NAME, PRESENTATION);

		/** The Constant CORE_DISPLAY_PERSPECTIVE. */
		public static final Pref<Boolean> CORE_DISPLAY_PERSPECTIVE =
				create("pref_display_continue_drawing", "Continue to draw displays when in Modeling perspective", false,
						IType.BOOL, true).in(NAME, PRESENTATION);

		/** The Constant DISPLAY_FAST_SNAPSHOT. */
		public static final Pref<Boolean> DISPLAY_FAST_SNAPSHOT = create("pref_display_fast_snapshot",
				"Enable fast snapshots (uncomplete when the display is obscured by others but much faster)", false,
				IType.BOOL, true).in(NAME, PRESENTATION);

		/** The Constant CORE_DISPLAY_TOOLBAR. */
		public static final Pref<Boolean> CORE_DISPLAY_TOOLBAR =
				create("pref_display_show_toolbar", "Show the display top toolbar", true, IType.BOOL, true).in(NAME,
						PRESENTATION);

		/** The Constant CORE_OVERLAY. */
		public static final Pref<Boolean> CORE_OVERLAY =
				create("pref_display_show_overlay", "Show the display bottom overlay", false, IType.BOOL, true).in(NAME,
						PRESENTATION);

		/**
		 * Charts
		 */
		public static final String CHARTS = "Charts Preferences";

		/** The Constant CHART_FLAT. */
		public static final Pref<Boolean> CHART_FLAT =
				create("pref_display_flat_charts", "Display 'flat' histograms", true, IType.BOOL, true).in(NAME,
						CHARTS);

		/** The Constant CHART_MEMORIZE. */
		public static final Pref<Boolean> CHART_MEMORIZE = create("pref_display_memorize_charts",
				"Keep values in memory (to save them as CSV)", true, IType.BOOL, true).in(NAME, CHARTS);

		/** The Constant CHART_GRIDLINES. */
		public static final Pref<Boolean> CHART_GRIDLINES =
				create("pref_chart_display_gridlines", "Display grid lines", true, IType.BOOL, true).in(NAME, CHARTS);

		/** The Constant CHART_QUALITY. */
		public static final Pref<Double> CHART_QUALITY = create("pref_chart_quality",
				"Resolution of the charts (from 0, small but fast, to 1, best but resource consuming)", 0.8,
				IType.FLOAT, true).in(NAME, CHARTS).between(0.1, 1.0);

		/**
		 * Drawing methods and defaults
		 */
		public static final String DRAWING = "Default Rendering Properties";

		/** The Constant CORE_DISPLAY. */
		public static final Pref<String> CORE_DISPLAY =
				create("pref_display_default", "Default rendering method", IKeyword._2D, IType.STRING, true)
						.among(IKeyword._2D, IKeyword._3D).in(NAME, DRAWING);

		/** The Constant CORE_ANTIALIAS. */
		public static final Pref<Boolean> CORE_ANTIALIAS =
				create("pref_display_antialias", "Apply antialiasing", false, IType.BOOL, true).in(NAME, DRAWING);

		/** The Constant CORE_BACKGROUND. */
		public static final Pref<GamaColor> CORE_BACKGROUND =
				create("pref_display_background_color", "Default background color ('background' facet of 'display')",
						() -> GamaColor.get("white"), IType.COLOR, true).in(NAME, DRAWING);

		/** The Constant CORE_HIGHLIGHT. */
		public static final Pref<GamaColor> CORE_HIGHLIGHT = create("pref_display_highlight_color",
				"Default highlight color", () -> GamaColor.get(0, 200, 200), IType.COLOR, true).in(NAME, DRAWING);

		/** The Constant CORE_SHAPE. */
		public static final Pref<String> CORE_SHAPE =
				create("pref_display_default_shape", "Defaut shape of agents", "shape", IType.STRING, true)
						.among("shape", "circle", "square", "triangle", "point", "cube", "sphere").in(NAME, DRAWING);

		/** The Constant CORE_SIZE. */
		public static final Pref<Double> CORE_SIZE =
				create("pref_display_default_size", "Default size of agents", 1.0, IType.FLOAT, true)
						.between(0.01, null).in(NAME, DRAWING);

		/** The Constant CORE_COLOR. */
		public static final Pref<GamaColor> CORE_COLOR = create("pref_display_default_color", "Default color of agents",
				() -> GamaColor.get("yellow"), IType.COLOR, true).in(NAME, DRAWING);
		/**
		 * Options
		 */
		public static final String OPTIONS = "Advanced ";

		/** The Constant DISPLAY_NO_ACCELERATION. */
		public static final Pref<Boolean> DISPLAY_NO_ACCELERATION = create("pref_display_no_java2d_acceleration",
				"Disable acceleration for Java2D (necessary on some configurations)", false, IType.BOOL, true)
						.in(NAME, OPTIONS).hidden();
		/**
		 * OPENGL
		 */
		public static final String RENDERING = "OpenGL Rendering Properties";

		/** The Constant DISPLAY_ONLY_VISIBLE. */
		public static final Pref<Boolean> DISPLAY_ONLY_VISIBLE = create("pref_display_visible_agents",
				"Only display visible agents (faster, may create visual oddities)", false, IType.BOOL, true).in(NAME,
						RENDERING);

		/** The Constant CORE_DRAW_ENV. */
		public static final Pref<Boolean> CORE_DRAW_ENV =
				create("pref_display_show_referential", "Draw 3D axes", true, IType.BOOL, true).in(NAME, RENDERING);

		/** The Constant DRAW_ROTATE_HELPER. */
		public static final Pref<Boolean> DRAW_ROTATE_HELPER =
				create("pref_display_show_rotation", "Draw rotation axes", true, IType.BOOL, true).in(NAME, RENDERING);

		/** The Constant CORE_LINE_WIDTH. */
		public static final Pref<Double> CORE_LINE_WIDTH = create("pref_display_line_width",
				"Default line width (facet 'width' of 'draw'). Note that this attribute is not supported by all OpenGL implementations",
				1d, IType.FLOAT, true).in(NAME, RENDERING);

		/** The Constant ONLY_VISIBLE_FACES. */
		public static final Pref<Boolean> ONLY_VISIBLE_FACES = create("pref_display_visible_faces",
				"Draw only the 'external' faces of objects", false, IType.BOOL, true).in(NAME, RENDERING).hidden();

		/** The Constant DISPLAY_SLICE_NUMBER. */
		public static final Pref<Integer> DISPLAY_SLICE_NUMBER =
				create("pref_display_slice_number", "Number of slices of circular geometries", 16, IType.INT, true)
						.in(NAME, RENDERING);

		/** The Constant DISPLAY_SLICE_NUMBER. */
		public static final Pref<Boolean> OPENGL_Z_FIGHTING = create("pref_opengl_z_fighting",
				"Add a small increment to the z ordinate of objects and layers to fight visual artefacts", true,
				IType.BOOL, true).in(NAME, RENDERING).activates("pref_opengl_z_factor");

		/** The Constant OPENGL_Z_FACTOR. */
		public static final Pref<Double> OPENGL_Z_FACTOR =
				create("pref_opengl_z_factor", "Increment factor (from 0, none, to 1, max)", 0.05, IType.FLOAT, true)
						.in(NAME, RENDERING).between(0d, 1d).step(0.001);

		/** The Constant OPENGL_TEXTURE_ORIENTATION. */
		public static final Pref<Boolean> OPENGL_TEXTURE_ORIENTATION = create("pref_texture_orientation",
				"Orient the textures according to the geometry on which they are displayed (may create visual oddities)",
				true, IType.BOOL, true).in(NAME, RENDERING);
		/**
		 * Options
		 */
		public static final Pref<Double> OPENGL_ZOOM =
				create("pref_display_zoom_factor", "Set the zoom factor (0 for slow, 1 fast)", 0.5, IType.FLOAT, true)
						.in(NAME, RENDERING).between(0, 1).step(0.01);

		/** The Constant OPENGL_KEYBOARD. */
		public static final Pref<Double> OPENGL_KEYBOARD = create("pref_display_keyboard_factor",
				"Set the sensitivity of the keyboard movements  (0 for slow, 1 for fast)", 0.5, IType.FLOAT, true)
						.in(NAME, RENDERING).between(0.01, 1.0).step(0.01);

		/** The Constant OPENGL_MOUSE. */
		public static final Pref<Double> OPENGL_MOUSE = create("pref_display_mouse_factor",
				"Set the sensitivity of the mouse/trackpad movements  (0 for slow, 1 fast)", 0.5, IType.FLOAT, true)
						.in(NAME, RENDERING).between(0.01, 1.0).step(0.01);

		/** The Constant OPENGL_CAP_FPS. */
		public static final Pref<Boolean> OPENGL_CAP_FPS =
				create("pref_display_cap_fps", "Limit the number of frames per second", false, IType.BOOL, true)
						.in(NAME, RENDERING).activates("pref_display_max_fps");

		/** The Constant OPENGL_FPS. */
		public static final Pref<Integer> OPENGL_FPS =
				create("pref_display_max_fps", "Max. number of frames per second", 60, IType.INT, true).in(NAME,
						RENDERING);

		/** The Constant DISPLAY_POWER_OF_TWO. */
		// public static final Pref<Boolean> DISPLAY_POWER_OF_TWO = create("pref_display_power_of_2",
		// "Forces textures dimensions to a power of 2 (e.g. 16x16. Necessary on some configurations)", false,
		// IType.BOOL, true).in(NAME, RENDERING).hidden();

		/** The Constant OPENGL_NUM_KEYS_CAM. */
		public static final Pref<Boolean> OPENGL_NUM_KEYS_CAM = create("pref_display_numkeyscam",
				"Use Numeric Keypad (2,4,6,8) for camera interaction", true, IType.BOOL, true).in(NAME, RENDERING);

		/** The Constant OPENGL_DEFAULT_CAM. */
		public static final Pref<String> OPENGL_DEFAULT_CAM =
				create("pref_display_camera", "Default camera to use when none is specified", "From above",
						IType.STRING, true).among(ICameraDefinition.PRESETS).in(NAME, RENDERING);

		/** The Constant OPENGL_USE_IMAGE_CACHE. */
		public static final Pref<Boolean> OPENGL_USE_IMAGE_CACHE = create("pref_display_use_cache",
				"Use GAMA image cache when building textures in OpenGL (potentially faster when running several simulations, but uses more memory)",
				true, IType.BOOL, true).in(NAME, RENDERING);

		/** The Constant OPENGL_DEFAULT_LIGHT_INTENSITY. */
		public static final Pref<Integer> OPENGL_DEFAULT_LIGHT_INTENSITY = create("pref_display_light_intensity",
				"Set the default intensity of the ambient and default lights (from 0, completely dark, to 255, completely light)",
				160, IType.INT, true).in(NAME, RENDERING).between(0, 255);

	}

	/**
	 * The Class External.
	 */
	public static class External {

		/** The Constant NAME. */
		public static final String NAME = "Data and Operators";
		/**
		 * Http connections
		 */
		public static final String HTTP = "Http connections";

		/** The Constant CORE_HTTP_CONNECT_TIMEOUT. */
		public static final Pref<Integer> CORE_HTTP_CONNECT_TIMEOUT =
				create("pref_http_connect_timeout", "Connection timeout (in ms)", 20000, IType.INT, true).in(NAME,
						HTTP);

		/** The Constant CORE_HTTP_READ_TIMEOUT. */
		public static final Pref<Integer> CORE_HTTP_READ_TIMEOUT =
				create("pref_http_read_timeout", "Read timeout (in ms)", 20000, IType.INT, true).in(NAME, HTTP);

		/** The Constant CORE_HTTP_RETRY_NUMBER. */
		public static final Pref<Integer> CORE_HTTP_RETRY_NUMBER = create("pref_http_retry_number",
				"Number of times to retry if connection cannot be established", 3, IType.INT, true).in(NAME, HTTP);

		/** The Constant CORE_HTTP_EMPTY_CACHE. */
		public static final Pref<Boolean> CORE_HTTP_EMPTY_CACHE = create("pref_http_empty_cache",
				"Empty the local cache of files downloaded from the web", true, IType.BOOL, true).in(NAME, HTTP);

		/**
		 * Random numbers
		 */
		public static final String RNG = "Random number generation";

		/** The Constant CORE_RNG. */
		public static final Pref<String> CORE_RNG =
				create("pref_rng_name", "Default random number generator", IKeyword.MERSENNE, IType.STRING, true)
						.among(RandomUtils.Generators.names()).in(NAME, RNG);

		/** The Constant CORE_SEED_DEFINED. */
		public static final Pref<Boolean> CORE_SEED_DEFINED =
				create("pref_rng_define_seed", "Define a default seed", false, IType.BOOL, true)
						.activates("pref_rng_default_seed").in(NAME, RNG);

		/** The Constant CORE_SEED. */
		public static final Pref<Double> CORE_SEED =
				create("pref_rng_default_seed", "Default seed value (0 is undefined)", 1d, IType.FLOAT, true).in(NAME,
						RNG);

		/**
		 * Dates
		 */
		public static final String DATES = "Management of dates";
		/**
		 * Optimizations
		 */
		public static final String OPTIMIZATIONS = "Optimizations";

		/** The Constant AT_DISTANCE_OPTIMIZATION. */
		public static final Pref<Boolean> AT_DISTANCE_OPTIMIZATION =
				create("pref_optimize_at_distance", "Optimize the 'at_distance' operator", true, IType.BOOL, true)
						.in(NAME, OPTIMIZATIONS);

		/** The Constant PATH_COMPUTATION_OPTIMIZATION. */
		public static final Pref<Boolean> PATH_COMPUTATION_OPTIMIZATION = create("pref_optimize_path_computation",
				"Optimize the path computation operators and goto action (but with possible 'jump' issues)", false,
				IType.BOOL, true).in(NAME, OPTIMIZATIONS);

		/** The Constant TOLERANCE_POINTS. */
		public static final Pref<Double> TOLERANCE_POINTS =
				create("pref_point_tolerance", "Tolerance for the comparison of points", 0.0, IType.FLOAT, true)
						.in(NAME, OPTIMIZATIONS);

		/** The Constant SHAPEFILE_IN_MEMORY. */
		public static final Pref<Boolean> SHAPEFILES_IN_MEMORY = create("pref_shapefiles_in_memory",
				"In-memory shapefile mapping (optimizes access to shapefile data in exchange for increased memory usage)",
				true, IType.BOOL, true).in(NAME, OPTIMIZATIONS);

		/**
		 * Paths to libraries
		 */
		public static final String GEOTOOLS =
				"GIS Coordinate Reference Systems (http://spatialreference.org/ref/epsg/ for EPSG codes)";

		/** The Constant LIB_TARGETED. */
		public static final Pref<Boolean> LIB_TARGETED = create("pref_gis_auto_crs",
				"Let GAMA find which CRS to use to project GIS data", true, IType.BOOL, true).in(NAME, GEOTOOLS);

		/** The Constant LIB_PROJECTED. */
		public static final Pref<Boolean> LIB_PROJECTED = create("pref_gis_same_crs",
				"When no .prj file or CRS is supplied, consider GIS data to be already projected in this CRS", true,
				IType.BOOL, true).deactivates("pref_gis_initial_crs").in(NAME, GEOTOOLS);

		/** The Constant LIB_USE_DEFAULT. */
		public static final Pref<Boolean> LIB_USE_DEFAULT =
				create("pref_gis_save_crs", "When no CRS is provided, save the GIS data with the current CRS", true,
						IType.BOOL, true).deactivates("pref_gis_output_crs").in(NAME, GEOTOOLS);

		/** The Constant LIB_TARGET_CRS. */
		public static final Pref<Integer> LIB_TARGET_CRS = create("pref_gis_default_crs",
				"...or use the following EPSG code (the one that will also be used if no projection information is found)",
				32648, IType.INT, true).in(NAME, GEOTOOLS)
						.addChangeListener((IPreferenceBeforeChangeListener<Integer>) newValue -> {
							final var codes = CRS.getSupportedCodes(newValue.toString());
							if (codes.isEmpty()) return false;
							return true;
						});

		/** The Constant LIB_INITIAL_CRS. */
		public static final Pref<Integer> LIB_INITIAL_CRS =
				create("pref_gis_initial_crs", "...or use the following CRS (EPSG code)", 4326, IType.INT, true)
						.in(NAME, GEOTOOLS).addChangeListener((IPreferenceBeforeChangeListener<Integer>) newValue -> {
							final var codes = CRS.getSupportedCodes(newValue.toString());
							if (codes.isEmpty()) return false;
							return true;
						});

		/** The Constant LIB_OUTPUT_CRS. */
		public static final Pref<Integer> LIB_OUTPUT_CRS =
				create("pref_gis_output_crs", "... or use this following CRS (EPSG code)", 4326, IType.INT, true)
						.in(NAME, GEOTOOLS).addChangeListener((IPreferenceBeforeChangeListener<Integer>) newValue -> {
							final var codes = CRS.getSupportedCodes(newValue.toString());
							if (codes.isEmpty()) return false;
							return true;
						});

		/** The Constant CSV_STRING_QUALIFIER. */
		public static final Pref<String> CSV_STRING_QUALIFIER =
				GamaPreferences
						.create("pref_csv_string_qualifier", "Default separator for strings",
								String.valueOf(AbstractCSVManipulator.Letters.QUOTE), IType.STRING, true)
						.in(NAME, "CSV Files");

		/** The Constant CSV_SEPARATOR. */
		public static final Pref<String> CSV_SEPARATOR = GamaPreferences
				.create("pref_csv_separator", "Default separator for fields",
						String.valueOf(AbstractCSVManipulator.Letters.COMMA), IType.STRING, true)
				.in(GamaPreferences.External.NAME, "CSV Files");
	}

	/**
	 * The Class Experimental.
	 */
	public static class Experimental {

		/** The Constant NAME. */
		public static final String NAME = "Experimental";
		/**
		 * Http connections
		 */
		public static final String CATEGORY =
				" These features have not been fully tested. Enable them at your own risks.";

		/** The Constant REQUIRED_PLUGINS. */
		public static final Pref<Boolean> REQUIRED_PLUGINS = create("pref_required_plugins",
				"Automatically add the plugins required to compile and run a model when editing it", false, IType.BOOL,
				false).in(NAME, CATEGORY);

		/** The Constant MISSING_PLUGINS. */
		public static final Pref<Boolean> MISSING_PLUGINS =
				create("pref_missing_plugins", "Verify that the required plugins are present before compiling a model",
						false, IType.BOOL, false).in(NAME, CATEGORY);

		/** The Constant QUADTREE_OPTIMIZATION. */
		public static final Pref<Boolean> QUADTREE_OPTIMIZATION = create("pref_optimize_quadtree",
				"Optimize spatial queries: add agents only when necessary in the quadtree (still experimental)", false,
				IType.BOOL, true).in(NAME, CATEGORY);

		/** The Constant QUADTREE_SYNCHRONIZATION. */
		public static final Pref<Boolean> QUADTREE_SYNCHRONIZATION = create("pref_synchronize_quadtree",
				"Forces the spatial index to synchronize its operations. Useful for interactive models where the users interfere or parallel models with concurrency errors. Note that it may slow down simulations with a lot of mobile agents",
				true, IType.BOOL, true).in(NAME, CATEGORY);

		/** The Constant CONSTANT_OPTIMIZATION. */
		public static final Pref<Boolean> CONSTANT_OPTIMIZATION = create("pref_optimize_constant_expressions",
				"Optimize constant expressions (experimental, performs a rebuild of models)", false, IType.BOOL, true)
						.in(NAME, CATEGORY).onChange(v -> {
							try {
								ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
							} catch (
										/** The e. */
							CoreException e) {}
						});

		/** The Constant USE_POOLING. */
		public static final Pref<Boolean> USE_POOLING =
				create("pref_use_pooling", "Use object pooling to reduce memory usage (still experimental)", false,
						IType.BOOL, true).in(NAME, CATEGORY).hidden();

	}

	/** The prefs. */
	private static Map<String, Pref<? extends Object>> prefs = new LinkedHashMap<>();

	/**
	 * Gets the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param key
	 *            the key
	 * @param clazz
	 *            the clazz
	 * @return the pref
	 */
	public static <T> Pref<T> get(final String key, final Class<T> clazz) {
		return (Pref<T>) prefs.get(key);
	}

	/**
	 * Gets the.
	 *
	 * @param key
	 *            the key
	 * @return the pref
	 */
	public static Pref<?> get(final String key) {
		return prefs.get(key);
	}

	/**
	 * Gets the all.
	 *
	 * @return the all
	 */
	public static Map<String, Pref<?>> getAll() { return prefs; }

	/**
	 * Creates the.
	 *
	 * @param <T>
	 *            the generic type
	 * @param key
	 *            the key
	 * @param title
	 *            the title
	 * @param value
	 *            the value
	 * @param type
	 *            the type
	 * @param inGaml
	 *            the in gaml
	 * @return the pref
	 */
	public static <T> Pref<T> create(final String key, final String title, final T value, final int type,
			final boolean inGaml) {
		final var e = new Pref<T>(key, type, inGaml).named(title).in(Interface.NAME, "").init(value);
		register(e);
		return e;
	}

	/**
	 * Lazy create (tries not to compute immediately the value)
	 *
	 */
	public static <T> Pref<T> create(final String key, final String title, final ValueProvider<T> provider,
			final int type, final boolean inGaml) {
		final var e = new Pref<T>(key, type, inGaml).named(title).in(Interface.NAME, "").init(provider);
		register(e);
		return e;
	}

	/**
	 * Register.
	 *
	 * @param gp
	 *            the gp
	 */
	private static void register(final Pref<?> gp) {
		final var key = gp.key;
		if (key == null) return;
		prefs.put(key, gp);
		getStore().register(gp);
		// Adds the preferences to the platform species if it is already created
		final var spec = GamaMetaModel.getPlatformSpeciesDescription();
		if (spec != null && !spec.hasAttribute(key)) {
			spec.addPref(key, gp);
			// spec.validate();
		}
	}

	/**
	 * Organize prefs.
	 *
	 * @return the map
	 */
	public static Map<String, Map<String, List<Pref<?>>>> organizePrefs() {
		final Map<String, Map<String, List<Pref<?>>>> result = GamaMapFactory.create();
		for (final Pref<?> e : prefs.values()) {
			if (e.isHidden()) { continue; }
			final var tab = e.tab;
			var groups = result.get(tab);
			if (groups == null) {
				groups = GamaMapFactory.create();
				result.put(tab, groups);
			}
			final var group = e.group;
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
	 * Sets the new preferences.
	 *
	 * @param modelValues
	 *            the model values
	 */
	public static void setNewPreferences(final Map<String, Object> modelValues) {
		for (final String name : modelValues.keySet()) {
			final Pref e = prefs.get(name);
			if (e == null) { continue; }
			e.set(modelValues.get(name));
			getStore().write(e);
		}
	}

	/**
	 * Revert to default values.
	 *
	 * @param modelValues
	 *            the model values
	 */
	public static void revertToDefaultValues(final Map<String, Object> modelValues) {
		getStore().clear();
	}

	/**
	 * Apply preferences from.
	 *
	 * @param path
	 *            the path
	 * @param modelValues
	 *            the model values
	 */
	public static void applyPreferencesFrom(final String path, final Map<String, Object> modelValues) {
		// DEBUG.OUT("Apply preferences from " + path);
		getStore().loadFromProperties(path);
		final List<Pref<?>> entries = new ArrayList(prefs.values());
		for (final Pref<?> e : entries) {
			register(e);
			modelValues.put(e.key, e.getValue());
		}
	}

	/**
	 * Save preferences to GAML.
	 *
	 * @param path
	 *            the path
	 */
	public static void savePreferencesToGAML(final String path) {
		try (var os = new FileWriter(path)) {
			final var entries = StreamEx.ofValues(prefs).sortedBy(Pref::getName).toList();

			final var read = new StringBuilder(1000);
			final var write = new StringBuilder(1000);
			for (final Pref<?> e : entries) {
				if (e.isHidden() || !e.inGaml()) { continue; }
				read.append(Strings.TAB).append("//").append(e.getTitle()).append(Strings.LN);
				read.append(Strings.TAB).append("write sample(gama.").append(e.getName()).append(");")
						.append(Strings.LN).append(Strings.LN);
				write.append(Strings.TAB).append("//").append(e.getTitle()).append(Strings.LN);
				write.append(Strings.TAB).append("gama.").append(e.getName()).append(" <- ")
						.append(StringUtils.toGaml(e.getValue(), false)).append(";").append(Strings.LN)
						.append(Strings.LN);
			}
			os.append("// ").append(GAMA.VERSION).append(" Preferences saved on ")
					.append(LocalDateTime.now().toString()).append(Strings.LN).append(Strings.LN);
			os.append("model preferences").append(Strings.LN).append(Strings.LN);
			os.append("experiment 'Display Preferences' type: gui {").append(Strings.LN);
			os.append("init {").append(Strings.LN);
			os.append(read);
			os.append("}").append(Strings.LN);
			os.append("}").append(Strings.LN).append(Strings.LN).append(Strings.LN);
			os.append("experiment 'Set Preferences' type: gui {").append(Strings.LN);
			os.append("init {").append(Strings.LN);
			os.append(write);
			os.append("}").append(Strings.LN);
			os.append("}").append(Strings.LN);
			os.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save preferences to properties.
	 *
	 * @param path
	 *            the path
	 */
	public static void savePreferencesToProperties(final String path) {
		getStore().saveToProperties(path);
	}

	// To force preferences to load

	/** The i. */
	static Interface i_ = new Interface();

	/** The m. */
	static Modeling m_ = new Modeling();

	/** The r. */
	static Runtime r_ = new Runtime();

	/** The s. */
	static Simulations s_ = new Simulations();

	/** The d. */
	static Displays d_ = new Displays();

	/** The ext. */
	static External ext_ = new External();

	/** The exp. */
	static Experimental exp_ = new Experimental();

}
