/*******************************************************************************************************
 *
 * IGui.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.emf.common.util.URI;

import gama.core.common.interfaces.IDisplayCreator.DisplayDescription;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.parameters.IParameter;
import gama.core.kernel.model.IModel;
import gama.core.kernel.simulation.ISimulationAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.IPoint ;
import gama.core.metamodel.shape.IShape;
import gama.core.outputs.IOutput;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.server.CommandExecutor;
import gama.core.runtime.server.ISocketCommand;
import gama.core.util.GamaFont;
import gama.core.util.IColor;
import gama.core.util.file.IFileMetaDataProvider;
import gama.core.util.file.IGamaFileMetaData;
import gama.core.util.list.IList;
import gama.core.util.map.GamaMapFactory;
import gama.core.util.map.IMap;
import gama.gaml.architecture.user.UserPanelStatement;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.statements.test.CompoundSummary;

/**
 * The interface IGui. Represents objects that act on behalf of a concrete GUI implementation (RCP, Headless, etc.)
 *
 * @author drogoul
 * @since 18 dec. 2011
 *
 */
public interface IGui {

	/** The null metadata provider. */
	IFileMetaDataProvider NULL_METADATA_PROVIDER =
			(element, includeOutdated, immediately) -> new IGamaFileMetaData() {};

	/** The null models manager. */
	IModelsManager NULL_MODELS_MANAGER = new IModelsManager() {};

	/** The null status displayer. */
	IStatusDisplayer NULL_STATUS_DISPLAYER = new IStatusDisplayer() {};

	/** The null dialog factory. */
	IDialogFactory NULL_DIALOG_FACTORY = new IDialogFactory() {};

	/** The null snapshot maker. */
	ISnapshotMaker NULL_SNAPSHOT_MAKER = new ISnapshotMaker() {};

	/** The displays. */
	Map<String, DisplayDescription> DISPLAYS = GamaMapFactory.createOrdered();

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
	default boolean confirmClose(final IExperimentPlan experiment) {
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
	default boolean openSimulationPerspective(final IModel model, final String experimentId) {
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
	default IDisplaySurface createDisplaySurfaceFor(final LayeredDisplayOutput output, final Object... args) {
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
			final List<IParameter> parameters, final GamaFont font, final IColor color, final Boolean showTitle) {
		return GamaMapFactory.create();
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
	default IMap<String, IMap<String, Object>> openWizard(final IScope scope, final String title,
			final ActionDescription finish, final IList<IMap<String, Object>> pages) {
		return GamaMapFactory.create();
	}

	/**
	 * Open user control panel.
	 *
	 * @param scope
	 *            the scope
	 * @param panel
	 *            the panel
	 */
	default void openUserControlPanel(final IScope scope, final UserPanelStatement panel) {}

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
	 * Gets the meta data provider.
	 *
	 * @return the meta data provider
	 */
	default IFileMetaDataProvider getMetaDataProvider() { return NULL_METADATA_PROVIDER; }

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
	default DisplayDescription getDisplayDescriptionFor(final String name) {
		return DISPLAYS.get(name);
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
	default IPoint  getMouseLocationInModel() { return GamaPointFactory.NULL_POINT; }

	/**
	 * Gets the mouse location in display.
	 *
	 * @return the mouse location in model
	 */
	default IPoint  getMouseLocationInDisplay() { return GamaPointFactory.NULL_POINT; }

	/**
	 * Sets the mouse location in model.
	 *
	 * @param modelCoordinates
	 *            the new mouse location in model
	 */
	default void setMouseLocationInModel(final IPoint  modelCoordinates) {}

	/**
	 * Sets the mouse location in model.
	 *
	 * @param modelCoordinates
	 *            the new mouse location in model
	 */
	default void setMouseLocationInDisplay(final IPoint  displayCoordinates) {}

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
	default void arrangeExperimentViews(final IScope myScope, final IExperimentPlan experimentPlan,
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
	default Map<String, ISocketCommand> getServerCommands() { return CommandExecutor.getDefaultCommands(); }

	/**
	 * @param uri
	 */
	default void openFile(final URI uri) {}

}
