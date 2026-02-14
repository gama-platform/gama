/*******************************************************************************************************
 *
 * IGui.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;

import gama.api.GAMA;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.compilation.IModelsManager;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IFont;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.IParameter;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.ui.displays.IDisplayCreator;
import gama.api.ui.displays.IDisplaySurface;
import gama.api.ui.layers.ISnapshotMaker;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.tests.CompoundSummary;

/**
 * The interface IGui. Represents objects that act on behalf of a concrete GUI implementation (RCP, Headless, etc.)
 *
 * @author drogoul
 * @since 18 dec. 2011
 *
 */
public interface IGui {

	/** The null models manager. */
	IModelsManager NULL_MODELS_MANAGER = new IModelsManager() {};

	/** The null status displayer. */
	IStatusDisplayer NULL_STATUS_DISPLAYER = new IStatusDisplayer() {};

	/** The null progress indicator. */
	IProgressIndicator NULL_PROGRESS_INDICATOR = new IProgressIndicator() {};

	/** The null dialog factory. */
	IDialogFactory NULL_DIALOG_FACTORY = new IDialogFactory() {};

	/** The null snapshot maker. */
	ISnapshotMaker NULL_SNAPSHOT_MAKER = new ISnapshotMaker() {};

	/** The monitor view id. */
	String MONITOR_VIEW_ID = "gama.ui.application.view.MonitorView";

	/** The interactive console view id. */
	String INTERACTIVE_CONSOLE_VIEW_ID = "gama.ui.application.view.InteractiveConsoleView";

	/** The agent view id. */
	String AGENT_VIEW_ID = "gama.ui.application.view.AgentInspectView";

	/** The table view id. */
	String TABLE_VIEW_ID = "gama.ui.application.view.TableAgentInspectView";

	/** The layer view id. */
	String LAYER_VIEW_ID = "gama.ui.application.view.LayeredDisplayView";

	/** The gl layer view id. */
	String GL_LAYER_VIEW_ID = "gama.ui.application.view.OpenGLDisplayView";

	/** The gl layer view id2. */
	String GL_LAYER_VIEW_ID2 = "gama.ui.application.view.OpenGLDisplayView2";

	/** The gl layer view id3. */
	String GL_LAYER_VIEW_ID3 = "gama.ui.application.view.WebDisplayView";

	/** The error view id. */
	String ERROR_VIEW_ID = "gama.ui.application.view.ErrorView";

	/** The test view id. */
	String TEST_VIEW_ID = "gama.ui.application.view.TestView";

	/** The parameter view id. */
	String PARAMETER_VIEW_ID = "gama.ui.application.view.ParameterView";

	/** The navigator view id. */
	String NAVIGATOR_VIEW_ID = "gama.ui.application.view.GamaNavigator";

	/** The navigator lightweight decorator id. */
	String NAVIGATOR_LIGHTWEIGHT_DECORATOR_ID = "gama.ui.application.decorator";

	/** The console view id. */
	String CONSOLE_VIEW_ID = "gama.ui.application.view.ConsoleView";

	/** The user control view id. */
	String USER_CONTROL_VIEW_ID = "gama.ui.application.view.userControlView";

	/** The outline view id. */
	String OUTLINE_VIEW_ID = "gama.ui.application.view.outline";

	/** The validation view id. */
	String VALIDATION_VIEW_ID = "gama.ui.application.view.problems";

	/** The template view id. */
	String TEMPLATE_VIEW_ID = "gama.ui.application.view.templates";

	/** The minimap view id. */
	String MINIMAP_VIEW_ID = "gama.ui.application.view.minimap";

	/** The perspective modeling id. */
	String PERSPECTIVE_MODELING_ID = "gama.ui.application.perspectives.ModelingPerspective";

	/** The browser view id. */
	String BROWSER_VIEW_ID = "gama.ui.application.browser";

	/**
	 * Gets the status.
	 *
	 * @param scope
	 *            the scope
	 * @return the status
	 */
	default IStatusDisplayer getStatus() { return NULL_STATUS_DISPLAYER; }

	/**
	 * Gets the progress indicator.
	 *
	 * @return the progress indicator
	 */
	default IProgressIndicator getProgressIndicator(final IScope scope, final String taskName) {
		return NULL_PROGRESS_INDICATOR;
	}

	/**
	 * Gets the snapshot maker for this UI. Returns the instance that should have been set in GAMA (by default, unless
	 * set, NULL_SNAPSHOT_MAKER)
	 *
	 * @return the snapshot maker
	 */

	default ISnapshotMaker getSnapshotMaker() { return GAMA.getSnapshotMaker(); }

	/**
	 * Gets the console.
	 *
	 * @return the console
	 */
	IConsoleListener getConsole();

	/**
	 * Gets the dialog factory.
	 *
	 * @return the dialog factory
	 */
	default IDialogFactory getDialogFactory() { return NULL_DIALOG_FACTORY; }

	/**
	 * Show view.
	 *
	 * @param scope
	 *            the scope
	 * @param viewId
	 *            the view id
	 * @param name
	 *            the name
	 * @param code
	 *            the code
	 * @return the i gama view
	 */
	default IGamaView showView(final IScope scope, final String viewId, final String name, final int code) {
		return null;
	}

	/**
	 * Clear errors.
	 *
	 * @param scope
	 *            the scope
	 */
	default void clearErrors(final IScope scope) {}

	/**
	 * Runtime error.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 */
	default void runtimeError(final IScope scope, final GamaRuntimeException g) {}

	/**
	 * Confirm close.
	 *
	 * @param experiment
	 *            the experiment
	 * @return true, if successful
	 */
	default boolean confirmClose(final IExperimentSpecies experiment) {
		return true;
	}

	/**
	 * Copy to clipboard.
	 *
	 * @param text
	 *            the text
	 * @return true, if successful
	 */
	default boolean copyToClipboard(final String text) {
		return false;
	}

	/**
	 * Copy text from clipboard.
	 *
	 * @return the string
	 */
	default String copyTextFromClipboard() {
		return null;
	}

	/**
	 * Open simulation perspective.
	 *
	 * @param model
	 *            the model
	 * @param experimentId
	 *            the experiment id
	 * @return true, if successful
	 */
	default boolean openSimulationPerspective(final IModelSpecies model, final String experimentId) {
		return true;
	}

	/**
	 * Gets the frontmost display surface.
	 *
	 * @return the frontmost display surface
	 */
	default IDisplaySurface getFrontmostDisplaySurface() { return null; }

	/**
	 * Creates the display surface for.
	 *
	 * @param output
	 *            the output
	 * @param args
	 *            the args
	 * @return the i display surface
	 */
	default IDisplaySurface createDisplaySurfaceFor(final IOutput.Display output, final Object uiComponent) {
		return null;
	}

	/**
	 * Open user input dialog.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param parameters
	 *            the parameters
	 * @param font
	 *            the font
	 * @param color
	 *            the color
	 * @return the map
	 */
	default Map<String, Object> openUserInputDialog(final IScope scope, final String title,
			final List<IParameter> parameters, final IFont font, final IColor color, final Boolean showTitle) {
		return Collections.EMPTY_MAP;
	}

	/**
	 * Open wizard.
	 *
	 * @param scope
	 *            the scope
	 * @param title
	 *            the title
	 * @param finish
	 *            the finish
	 * @param pages
	 *            the pages
	 * @return the i map
	 */
	IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final IActionDescription finish, final IList<IMap<String, Object>> pages);

	/**
	 * Open user control panel.
	 *
	 * @param scope
	 *            the scope
	 * @param panel
	 *            the panel
	 */
	default void openUserControlPanel(final IScope scope, final IStatement panel) {}

	/**
	 * Close dialogs.
	 *
	 * @param scope
	 *            the scope
	 */
	default void closeDialogs(final IScope scope) {}

	/**
	 * Gets the highlighted agent.
	 *
	 * @return the highlighted agent
	 */
	default IAgent getHighlightedAgent() { return null; }

	/**
	 * Sets the highlighted agent.
	 *
	 * @param a
	 *            the new highlighted agent
	 */
	default void setHighlightedAgent(final IAgent a) {}

	/**
	 * Sets the selected agent.
	 *
	 * @param a
	 *            the new selected agent
	 */
	default void setSelectedAgent(final IAgent a) {}

	/**
	 * Clean after experiment.
	 */
	default void cleanAfterExperiment() {}

	/**
	 * Update speed display.
	 *
	 * @param scope
	 *            the scope
	 * @param d
	 *            the d
	 * @param notify
	 *            the notify
	 */
	default void updateSpeedDisplay(final IScope scope, final Double minimumCycleDuration,
			final Double maximumCycleDuration, final boolean notify) {}

	/**
	 * Gets the models manager.
	 *
	 * @return the models manager
	 */
	default IModelsManager getModelsManager() { return NULL_MODELS_MANAGER; }

	/**
	 * Close simulation views.
	 *
	 * @param scope
	 *            the scope
	 * @param andOpenModelingPerspective
	 *            the and open modeling perspective
	 * @param immediately
	 *            the immediately
	 */
	default void closeSimulationViews(final IScope scope, final boolean andOpenModelingPerspective,
			final boolean immediately) {}

	/**
	 * Gets the display description for.
	 *
	 * @param name
	 *            the name
	 * @return the display description for
	 */
	default IDisplayCreator getDisplayDescriptionFor(final String name) {
		return GamaAdditionRegistry.getDisplay(name);
	}

	/**
	 * Update view title.
	 *
	 * @param output
	 *            the output
	 * @param agent
	 *            the agent
	 */
	default void updateViewTitle(final IOutput output, final ISimulationAgent agent) {}

	/**
	 * Open welcome page.
	 *
	 * @param b
	 *            the b
	 */
	default void openWebDocumentationPage() {}

	/**
	 * Run.
	 *
	 * @param taskName
	 *            the task name
	 * @param opener
	 *            the opener
	 * @param asynchronous
	 *            the asynchronous
	 */
	default void run(final String taskName, final Runnable opener, final boolean asynchronous) {
		opener.run();
	}

	/**
	 * Sets the focus on.
	 *
	 * @param o
	 *            the new focus on
	 */
	default void setFocusOn(final IShape o) {}

	/**
	 * Apply layout.
	 *
	 * @param scope
	 *            the scope
	 * @param layout
	 *            the layout
	 */
	default void applyLayout(final IScope scope, final Object layout) {}

	/**
	 * Gets the mouse location in model.
	 *
	 * @return the mouse location in model
	 */
	IPoint getMouseLocationInModel();

	/**
	 * Gets the mouse location in display.
	 *
	 * @return the mouse location in model
	 */
	IPoint getMouseLocationInDisplay();

	/**
	 * Sets the mouse location in model.
	 *
	 * @param modelCoordinates
	 *            the new mouse location in model
	 */
	default void setMouseLocationInModel(final IPoint modelCoordinates) {}

	/**
	 * Sets the mouse location in model.
	 *
	 * @param modelCoordinates
	 *            the new mouse location in model
	 */
	default void setMouseLocationInDisplay(final IPoint displayCoordinates) {}

	/**
	 * Exit.
	 */
	default void exit() {
		System.exit(0);
	}

	// Tests

	/**
	 * Open test view.
	 *
	 * @param scope
	 *            the scope
	 * @param remainOpen
	 *            the remain open
	 * @return the i gama view. test
	 */
	default IGamaView.Test openTestView(final IScope scope, final boolean remainOpen) {
		return null;
	}

	/**
	 * @param scope
	 */
	default void displayTestsProgress(final IScope scope, final int number, final int total) {}

	/**
	 * Display tests results.
	 *
	 * @param scope
	 *            the scope
	 * @param summary
	 *            the summary
	 */
	default void displayTestsResults(final IScope scope, final CompoundSummary<?, ?> summary) {}

	/**
	 * End test display.
	 */
	default void endTestDisplay() {}

	/**
	 * Refresh navigator.
	 */
	default void refreshNavigator() {}

	/**
	 * Checks if is in display thread.
	 *
	 * @return true, if is in display thread
	 */
	default boolean isInDisplayThread() { return false; }

	/**
	 * Checks if is hi DPI.
	 *
	 * @return true, if is hi DPI
	 */
	default boolean isHiDPI() { return false; }

	/**
	 * Regular update for the monitors
	 *
	 * @param scope
	 */
	default void updateParameterView(final IScope scope) {}

	/**
	 * Arrange experiment views.
	 *
	 * @param myScope
	 *            the my scope
	 * @param experimentPlan
	 *            the experiment plan
	 * @param keepTabs
	 *            the keep tabs
	 * @param keepToolbars
	 *            the keep toolbars
	 * @param showParameters
	 *            the show parameters
	 * @param showConsoles
	 *            the show consoles
	 * @param showNavigator
	 *            the show navigator
	 * @param showControls
	 *            the show controls
	 * @param keepTray
	 *            the keep tray
	 * @param color
	 *            the color
	 * @param showEditors
	 *            the show editors
	 */
	default void arrangeExperimentViews(final IScope myScope, final IExperimentSpecies experimentPlan,
			final Boolean keepTabs, final Boolean keepToolbars, final Boolean showConsoles,
			final Boolean showParameters, final Boolean showNavigator, final Boolean showControls,
			final Boolean keepTray, final Supplier<IColor> color, final boolean showEditors) {}

	/**
	 * Display errors.
	 *
	 * @param scope
	 *            the scope
	 * @param exceptions
	 *            the exceptions
	 * @param reset
	 *            the reset
	 */
	default void displayErrors(final IScope scope, final List<GamaRuntimeException> exceptions, final boolean reset) {}

	/**
	 * Hide parameters.
	 */
	default void hideParameters() {}

	/**
	 * Update parameters.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 17 août 2023
	 */
	default void updateParameters(final boolean refreshValues) {}

	/**
	 * Gets the server commands.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the server commands
	 * @date 17 oct. 2023
	 */
	Map<String, ISocketCommand> getServerCommands();

	/**
	 * @param uri
	 */
	default void openFile(final URI uri) {}

}
