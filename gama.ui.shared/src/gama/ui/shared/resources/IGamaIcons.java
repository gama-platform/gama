/*******************************************************************************************************
 *
 * IGamaIcons.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.resources;

import gama.ui.application.workbench.ThemeHelper;

/**
 * Class IStrings.
 *
 * @author drogoul
 * @since 13 sept. 2013
 *
 */
public interface IGamaIcons {

	// Constants for producing icons

	/** The light path. */
	String LIGHT_PATH = "light/";

	/** The dark path. */
	String DARK_PATH = "dark/";

	/** The Constant COLORS. */
	String COLOR_PATH = "colors/";

	/** The Constant THEME_PATH. */
	String THEME_PATH = ThemeHelper.isDark() ? DARK_PATH : LIGHT_PATH;

	/** The icons segment. */
	String ICONS_SEGMENT = "/icons/";

	/** The Constant DEFAULT_PATH. */
	String ICONS_PATH = "/icons/" + THEME_PATH;

	/** The Constant GAML_PATH. */
	String GAML_PATH = "gaml/";

	/** The Constant defaultIcon. */
	String MISSING = GAML_PATH + "_unknown";

	/** The Constant PLUGIN_ID. */
	String PLUGIN_ID = "gama.ui.shared";

	/** The Constant DISABLED_SUFFIX. */
	String DISABLED_SUFFIX = "_disabled";

	// Constants corresponding to common icons

	/** The action clear. */
	String ACTION_CLEAR = "viewers/erase.contents";

	/** The action revert. */
	String ACTION_REVERT = "generic/menu.undo";

	/** The add column. */
	String ADD_COLUMN = "viewers/add.column";

	/** The add experiment. */
	String ADD_EXPERIMENT = "overlays/small.exp.plus";

	/** The add row. */
	String ADD_ROW = "viewers/add.row";

	/** The add simulation. */
	String ADD_SIMULATION = "experiment/experiment.simulations.add";

	/** The attributes. */
	String ATTRIBUTES = "gaml/_attributes";

	/** The browser back. */
	String BROWSER_BACK = "editor/command.lastedit";

	/** The browser forward. */
	String BROWSER_FORWARD = "editor/command.nextedit";

	/** The browser home. */
	String BROWSER_HOME = "generic/menu.about";

	/** The build all. */
	String BUILD_ALL = "validation/compile.all";

	/** The button back. */
	String BUTTON_BACK = "overlays/small.exp.back.white";

	/** The button batch. */
	String BUTTON_BATCH = "overlays/small.exp.batch.white";

	/** The button gui. */
	String BUTTON_GUI = "overlays/small.exp.run.white";

	/** The camera empty. */
	String CAMERA_EMPTY = "display/camera.empty";

	/** The camera lock. */
	String CAMERA_LOCK = "display/camera.lock";

	/** The chart parameters. */
	String CHART_PARAMETERS = "layer/chart.parameters";

	/** The paste. */
	String COPY = "generic/menu.copy";

	/** The cut. */
	String CUT = "generic/menu.cut";

	/** The delete. */
	String DELETE = "generic/menu.delete";

	/** The delete column. */
	String DELETE_COLUMN = "viewers/delete.column";

	/** The delete row. */
	String DELETE_ROW = "viewers/delete.row";

	/** The display fullscreen enter. */
	String DISPLAY_FULLSCREEN_ENTER = "display/fullscreen.enter";

	/** The display fullscreen exit. */
	String DISPLAY_FULLSCREEN_EXIT = "display/fullscreen.exit";

	/** The display toolbar camera. */
	String DISPLAY_TOOLBAR_CAMERA = "display/camera.full";

	/** The display toolbar csvexport. */
	String DISPLAY_TOOLBAR_CSVEXPORT = "generic/menu.saveas";

	/** The display toolbar pause. */
	String DISPLAY_TOOLBAR_PAUSE = "display/action.pause";

	/** The display toolbar snapshot. */
	String DISPLAY_TOOLBAR_SNAPSHOT = "display/action.snapshot";

	/** The display toolbar sync. */
	String DISPLAY_TOOLBAR_SYNC = "experiment/experiment.sync";

	/** The display toolbar zoomfit. */
	String DISPLAY_TOOLBAR_ZOOMFIT = "display/zoom.fit";

	/** The display toolbar zoomin. */
	String DISPLAY_TOOLBAR_ZOOMIN = "display/zoom.in";

	// User Panels

	/** The display toolbar zoomout. */
	String DISPLAY_TOOLBAR_ZOOMOUT = "display/zoom.out";

	/** The display update. */
	String DISPLAY_UPDATE = "display/action.update";

	/** The editor link. */
	String EDITOR_LINK = "navigator/editor.link";

	/** The experiment reload. */
	String EXPERIMENT_RELOAD = "experiment/experiment.reload";

	/** The experiment run. */
	String EXPERIMENT_RUN = "experiment/experiment.run";

	/** The experiment step. */
	String EXPERIMENT_STEP = "experiment/experiment.step";

	/** The experiment stop. */
	String EXPERIMENT_STOP = "experiment/experiment.stop";

	/** The file experiment. */
	String FILE_EXPERIMENT = "navigator/files/file.experiment";

	/** The file icon. */
	String FILE_ICON = "navigator/files/file.model";

	/** The file model. */
	String FILE_MODEL = "navigator/files/file.model";

	/** The file refresh. */
	String FILE_REFRESH = "navigator/file.refresh";

	/** The file rename. */
	String FILE_RENAME = "navigator/file.rename";

	/** The file shapesupport. */
	String FILE_SHAPESUPPORT = "navigator/files/file.shapesupport";

	/** The file text. */
	String FILE_TEXT = "navigator/files/file.text";

	/** The folder builtin. */
	String FOLDER_BUILTIN = "navigator/folder.library";

	/** The folder model. */
	String FOLDER_MODEL = "navigator/files/folder.model";

	/** The folder plugin. */
	String FOLDER_PLUGIN = "navigator/folder.plugin";

	/** The folder project. */
	String FOLDER_PROJECT = "navigator/files/folder.project";

	/** The folder project. */
	String CLOSED_PROJECT = "navigator/files/closed.project";

	/** The folder resources. */
	String FOLDER_RESOURCES = "navigator/files/folder.resources";

	// Small Icons

	/** The folder test. */
	String FOLDER_TEST = "navigator/folder.test";

	/** The folder user. */
	String FOLDER_USER = "navigator/folder.user";

	/** The font decrease. */
	String FONT_DECREASE = "display/zoom.out";

	/** The font increase. */
	String FONT_INCREASE = "display/zoom.in";

	/** The import archive. */
	String IMPORT_ARCHIVE = "navigator/import.archive";

	/** The import disk. */
	String IMPORT_DISK = "navigator/import.disk";

	/** The import project. */
	String IMPORT_PROJECT = "navigator/import.project";

	// Navigator

	/** The imported in. */
	String IMPORTED_IN = "editor/imported.in";

	/** The layer agents. */
	String LAYER_AGENTS = "layer/layer.agents";

	/** The layer chart. */
	String LAYER_CHART = "layer/layer.chart";

	/** The layer graphics. */
	String LAYER_GRAPHICS = "layer/layer.graphics";

	/** The layer grid. */
	String LAYER_GRID = "layer/layer.grid";

	/** The layer image. */
	String LAYER_IMAGE = "layer/layer.image";

	/** The layer selection. */
	String LAYER_SELECTION = "layer/layer.selection";

	/** The layer species. */
	String LAYER_SPECIES = "layer/layer.species";

	/** The layer transparency. */
	String LAYER_TRANSPARENCY = "layer/layer.transparency";

	/** The layers menu. */
	String LAYERS_MENU = "layer/layers.menu";

	/** The layout horizontal. */
	String LAYOUT_HORIZONTAL = "views/layout.horizontal";

	/** The lexical sort. */
	String LEXICAL_SORT = "editor/lexical.sort";

	/** The local history. */
	String LOCAL_HISTORY = "navigator/local.history";

	/** The lock population. */
	String LOCK_POPULATION = CAMERA_LOCK;

	/** The marker deleted. */
	String MARKER_DELETED = "markers/marker.deleted";

	/** The marker error. */
	String MARKER_ERROR = "markers/marker.error";

	/** The marker info. */
	String MARKER_INFO = "markers/marker.info";

	/** The marker task. */
	String MARKER_TASK = "markers/marker.task";

	/** The marker warning. */
	String MARKER_WARNING = "markers/marker.warning";

	/** The menu add monitor. */
	String MENU_ADD_MONITOR = "views/open.monitor";

	/** The menu agent. */
	String MENU_AGENT = "agents/agent.submenu";

	/** The button back. */
	String MENU_BACK = "overlays/small.exp.back.green";

	/** The button batch. */
	String MENU_BATCH = "overlays/small.exp.batch.green";

	/** The menu browse. */
	String MENU_BROWSE = "views/open.browser";

	/** The menu focus. */
	String MENU_FOCUS = "agents/action.focus";

	/** The button gui. */
	String MENU_GUI = "overlays/small.exp.run.green";

	/** The menu help. */
	String MENU_HELP = "generic/menu.help";

	/** The menu highlight. */
	String MENU_HIGHLIGHT = "agents/action.highlight";

	/** The menu inspect. */
	String MENU_INSPECT = "views/open.inspector";

	/** The menu kill. */
	String MENU_KILL = "agents/agent.kill";

	/** The menu pause action. */
	String MENU_PAUSE_ACTION = "experiment/experiment.pause";

	/** The menu population. */
	String MENU_POPULATION = "agents/agents.submenu";

	/** The menu run action. */
	String MENU_RUN_ACTION = "agents/agent.actions";

	/** The menu undo. */
	String MENU_UNDO = "generic/menu.undo";

	/** The menu redo. */
	String MENU_REDO = "generic/menu.redo";

	/** The new folder. */
	String NEW_FOLDER = "navigator/new.folder2";

	/** The new model. */
	String NEW_MODEL = "navigator/new.model2";

	/** The new project. */
	String NEW_PROJECT = "navigator/new.project2";

	/** The other experiments. */
	String OTHER_EXPERIMENTS = "editor/other.experiments";

	/** The overlay closed. */
	String OVERLAY_CLOSED = "navigator/overlays/overlay.closed";

	/** The overlay cloud. */
	String OVERLAY_CLOUD = "navigator/overlays/overlay.cloud";

	/** The overlay error. */
	String OVERLAY_ERROR = "navigator/overlays/overlay.error";

	// /** The overlay link broken. */
	// String OVERLAY_LINK_BROKEN = "navigator/overlays/overlay.link.broken";
	//
	// /** The overlay link ok. */
	// String OVERLAY_LINK_OK = "navigator/overlays/overlay.link.ok";

	/** The overlay ok. */
	String OVERLAY_OK = "navigator/overlays/overlay.ok";

	/** The overlay toggle. */
	// String OVERLAY_TOGGLE = "mini/overlay.toggle";

	/** The overlay warning. */
	String OVERLAY_WARNING = "navigator/overlays/overlay.warning";

	/** The panel continue. */
	String PANEL_CONTINUE = "experiment/experiment.continue";

	/** The panel inspect. */
	String PANEL_INSPECT = MENU_INSPECT;

	/** The paste. */
	String PASTE = "generic/menu.paste";

	/** The prefs editor. */
	String PREFS_EDITOR = "prefs/prefs.editor2";

	/** The prefs general. */
	String PREFS_GENERAL = "prefs/prefs.general2";

	/** The prefs libs. */
	String PREFS_LIBS = "prefs/prefs.libraries2";

	/** The prefs runtime. */
	String PREFS_EXPERIMENTAL = "prefs/prefs.experimental2";

	/** The prefs simulation. */
	String PREFS_SIMULATION = "prefs/prefs.simulations2";

	/** The prefs ui. */
	String PREFS_UI = "prefs/prefs.display2";

	/** The presentation menu. */
	String PRESENTATION_MENU = "editor/menu.presentation";

	/** The project close. */
	String PROJECT_CLOSE = "navigator/project.close2";

	/** The project open. */
	String PROJECT_OPEN = "navigator/project.open2";

	/** The reference builtin. */
	String REFERENCE_BUILTIN = "editor/reference.builtin";

	/** The reference colors. */
	String REFERENCE_COLORS = "editor/reference.colors";

	/** The reference operators. */
	String REFERENCE_OPERATORS = "editor/reference.operators";

	/** The reference templates. */
	String REFERENCE_TEMPLATES = "editor/reference.templates";

	/** The save as. */
	String SAVE_AS = "generic/menu.saveas";

	/** The set delimiter. */
	String SET_DELIMITER = "viewers/set.delimiter";

	/** The set header. */
	String SET_HEADER = "viewers/toggle.header";

	/** The small browse. */
	String SMALL_BROWSE = "overlays/small.browse";

	/** The small change. */
	String SMALL_CHANGE = "overlays/small.change";

	/** The small close. */
	String SMALL_CLOSE = "overlays/small.close";

	/** The small collapse. */
	String SMALL_COLLAPSE = "overlays/small.collapse";

	/** The small define. */
	String SMALL_DEFINE = "overlays/small.define";

	/** The small dropdown. */
	String SMALL_DROPDOWN = "overlays/small.dropdown";

	/** The small edit. */
	String SMALL_EDIT = "overlays/small.edit";

	/** The small expand. */
	String SMALL_EXPAND = "overlays/small.expand";

	/** The small hidden. */
	String SMALL_HIDDEN = "overlays/small.hidden";

	/** The small inspect. */
	String SMALL_INSPECT = "overlays/small.inspect";

	/** The small minus. */
	String SMALL_MINUS = "overlays/small.minus";

	/** The small pause. */
	String SMALL_PAUSE = "overlays/small.pause";

	/** The small plus. */
	String SMALL_PLUS = "overlays/small.plus";

	/** The small resume. */
	String SMALL_RESUME = "overlays/small.resume";

	/** The small revert. */
	String SMALL_REVERT = "overlays/small.revert";

	/** The small selectable. */
	String SMALL_SELECTABLE = "overlays/small.selectable";

	/** The small undefine. */
	String SMALL_UNDEFINE = "overlays/small.undefine";

	/** The small unselectable. */
	String SMALL_UNSELECTABLE = "overlays/small.unselectable";

	/** The status clock. */
	String STATUS_CLOCK = "overlays/status.clock";

	/** The test filter. */
	String TEST_FILTER = "validation/test.filter";

	/** The test run. */
	String TEST_RUN = "validation/test.run";

	/** The test sort. */
	String TEST_SORT = "validation/test.sort";

	/** The toggle antialias. */
	String TOGGLE_ANTIALIAS = "display/toggle.antialias";

	/** The toggle info. */
	String TOGGLE_INFOS = "validation/toggle.infos";

	/** The toggle overlay. */
	String TOGGLE_OVERLAY = "display/toggle.overlay";

	/** The toggle warnings. */
	String TOGGLE_WARNINGS = "validation/toggle.warnings";

	// /** The toolbar hide. */
	// String TOOLBAR_HIDE = "mini/toolbar.hide";
	//
	// /** The toolbar show. */
	// String TOOLBAR_SHOW = "mini/toolbar.show";

	/** The tree collapse. */
	String TREE_COLLAPSE = "toolbar/bar.collapse";

	/** The tree expand. */
	String TREE_EXPAND = "toolbar/bar.expand";

	/** The tree sort. */
	String TREE_SORT = "mini/tree.sort";

	/** The view browser. */
	String VIEW_BROWSER = "views/tabs/view.browser";

	/** The view interactive. */
	String VIEW_INTERACTIVE = "views/tabs/view.interactive";

	/** The browse populations. */
	String BROWSE_POPULATIONS = MENU_POPULATION;

}
