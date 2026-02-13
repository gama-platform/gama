/*******************************************************************************************************
 *
 * GAMA.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;

import gama.api.data.json.IJson;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.exceptions.GamaRuntimeFileException;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.ISymbol;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentController;
import gama.api.kernel.simulation.IExperimentRecorder;
import gama.api.kernel.simulation.IExperimentStateListener;
import gama.api.kernel.simulation.IExperimentStateListener.State;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.IWorkspaceManager;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.InScope;
import gama.api.ui.IGui;
import gama.api.ui.NullGuiHandler;
import gama.api.ui.layers.ISnapshotMaker;
import gama.api.utils.ITopLevelAgentChangeListener;
import gama.api.utils.PoolUtils;
import gama.api.utils.benchmark.Benchmark;
import gama.api.utils.benchmark.IBenchmarkable;
import gama.api.utils.benchmark.StopWatch;
import gama.api.utils.files.IFileMetadataProvider;
import gama.api.utils.files.IGamaFileMetaData;
import gama.api.utils.prefs.ConfigurationPreferenceStore;
import gama.api.utils.prefs.GamaPreferenceStore;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.prefs.IGamaPreferenceStore;
import gama.api.utils.prefs.JREPreferenceStore;
import gama.api.utils.random.IRandom;
import gama.api.utils.server.IGamaServer;
import gama.dev.DEBUG;
import gama.dev.FLAGS;

/**
 * The GAMA class serves as the main entry point and facade for the GAMA modeling and simulation platform. It provides
 * centralized access to all core platform services including experiment management, runtime state, GUI operations, and
 * platform configuration.
 *
 * <p>
 * This class acts as a singleton registry for platform-wide services and maintains the global state of the GAMA
 * platform. It coordinates between different subsystems such as:
 * </p>
 * <ul>
 * <li>Experiment lifecycle management (creation, execution, termination)</li>
 * <li>Runtime scope and agent management</li>
 * <li>GUI and headless mode operations</li>
 * <li>Platform services (preferences, workspace, random generators)</li>
 * <li>Error handling and reporting</li>
 * <li>Benchmarking and performance monitoring</li>
 * </ul>
 *
 * <p>
 * The class is organized into several functional areas:
 * </p>
 * <ul>
 * <li><strong>Platform Services:</strong> Core services initialization and access</li>
 * <li><strong>Experiment Management:</strong> Creating, running, and controlling experiments</li>
 * <li><strong>Runtime Access:</strong> Accessing current runtime state and scopes</li>
 * <li><strong>GUI Operations:</strong> Interface between platform and user interface</li>
 * <li><strong>Error Handling:</strong> Centralized error reporting and management</li>
 * <li><strong>Lifecycle Control:</strong> Experiment state transitions and event handling</li>
 * <li><strong>Benchmarking:</strong> Performance monitoring and analysis</li>
 * <li><strong>Agent Management:</strong> Top-level agent tracking and notifications</li>
 * </ul>
 *
 * @author Alexis Drogoul
 * @author The GAMA Team
 * @since GAMA 1.0
 * @version 2025-03
 */
public class GAMA {

	// ==================================================================================
	// STATIC INITIALIZATION
	// ==================================================================================

	static {
		DEBUG.OFF();
	}

	// ==================================================================================
	// PLATFORM GLOBAL VARIABLES
	// Global variables that are initialized at startup by various components and
	// available during runtime
	// ==================================================================================

	/** The recorder. */
	private volatile static Class<? extends IExperimentRecorder> __RECORDER__;

	/** Platform-wide random number generator */
	private volatile static IRandom __RANDOM__;

	/** The platform agent instance */
	private volatile static ITopLevelAgent.Platform __AGENT__;

	/** The platform preference store */
	public volatile static IGamaPreferenceStore __STORE__;

	/** The workspace manager instance */
	public volatile static IWorkspaceManager __WORKSPACE__;

	/** The snapshot maker for creating UI snapshots */
	private volatile static ISnapshotMaker __SNAPSHOT__;

	/** The file metadata provider. Default is null operation */
	public volatile static IFileMetadataProvider __METADATA__ = (e, o, i) -> new IGamaFileMetaData() {};

	/** The JSON encoder/decoder service */
	public volatile static IJson __JSON__;

	/** The GAMA server instance for headless/server mode operations */
	private volatile static IGamaServer __SERVER__;

	// ==================================================================================
	// PLATFORM SERVICES - INITIALIZATION METHODS
	// Methods for initializing platform-wide services during startup
	// ==================================================================================

	/**
	 * Sets the JSON encoder service for the platform.
	 *
	 * @param json
	 *            the JSON encoder/decoder implementation
	 */
	public static void setJsonEncoder(final IJson json) { __JSON__ = json; }

	/**
	 * Sets the file metadata provider for the platform.
	 *
	 * @param metadata
	 *            the file metadata provider implementation
	 */
	public static void setMetadataProvider(final IFileMetadataProvider metadata) { __METADATA__ = metadata; }

	/**
	 * Sets the recorder class.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param clazz
	 *            the new recorder class
	 * @date 2 sept. 2023
	 */
	public static void setRecorderClass(final Class<? extends IExperimentRecorder> clazz) { __RECORDER__ = clazz; }

	/**
	 * Sets the platform-wide random number generator.
	 *
	 * @param r
	 *            the random number generator instance
	 */
	public static void setRandomGenerator(final IRandom r) { __RANDOM__ = r; }

	/**
	 * Sets the workspace manager for the platform.
	 *
	 * @param r
	 *            the workspace manager instance
	 */
	public static void setWorkspaceManager(final IWorkspaceManager r) { __WORKSPACE__ = r; }

	/**
	 * Sets the GAMA server instance. Stops any existing server before setting the new one.
	 *
	 * @param server
	 *            the new server instance
	 */
	public static void setServer(final IGamaServer server) {
		if (__SERVER__ != null) {
			try {
				__SERVER__.stop();
				__SERVER__ = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		__SERVER__ = server;
	}

	/**
	 * Sets the snapshot maker for creating UI snapshots.
	 *
	 * @param instance
	 *            the snapshot maker instance
	 */
	public static void setSnapshotMaker(final ISnapshotMaker instance) {
		if (instance != null) { __SNAPSHOT__ = instance; }
	}

	// ==================================================================================
	// PLATFORM SERVICES - ACCESS METHODS
	// Methods for accessing platform-wide services
	// ==================================================================================

	/**
	 * Gets the snapshot maker for creating UI snapshots.
	 *
	 * @return the snapshot maker instance
	 */
	public static ISnapshotMaker getSnapshotMaker() {
		if (__SNAPSHOT__ == null) return IGui.NULL_SNAPSHOT_MAKER;
		return __SNAPSHOT__;
	}

	/**
	 * Gets the platform agent instance.
	 *
	 * @return the platform agent
	 */
	public static ITopLevelAgent.Platform getPlatformAgent() {
		if (__AGENT__ == null) { __AGENT__ = new PlatformAgent(); }
		return __AGENT__;
	}

	/**
	 * Gets the workspace manager instance.
	 *
	 * @return the workspace manager
	 */
	public static IWorkspaceManager getWorkspaceManager() { return __WORKSPACE__; }

	/**
	 * Gets the platform preference store.
	 *
	 * @return the preference store
	 */
	public static IGamaPreferenceStore getPreferenceStore() {
		if (__STORE__ == null) {
			__STORE__ = FLAGS.USE_GLOBAL_PREFERENCE_STORE
					? new JREPreferenceStore(Preferences.userRoot().node(GamaPreferenceStore.NODE_NAME))
					: new ConfigurationPreferenceStore(
							ConfigurationScope.INSTANCE.getNode(GamaPreferenceStore.NODE_NAME));
		}
		return __STORE__;
	}

	/**
	 * Gets the GAMA server instance.
	 *
	 * @return the server instance
	 */
	public static IGamaServer getServer() { return __SERVER__; }

	/**
	 * Gets the file metadata provider.
	 *
	 * @return the file metadata provider
	 */
	public static IFileMetadataProvider getMetadataProvider() { return __METADATA__; }

	/**
	 * Gets the JSON encoder/decoder service.
	 *
	 * @return the JSON service
	 */
	public static IJson getJsonEncoder() { return __JSON__; }

	/**
	 * Creates the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the i simulation recorder
	 * @date 2 sept. 2023
	 */
	public static IExperimentRecorder getExperimentRecorder() {
		try {
			if (__RECORDER__ != null) return __RECORDER__.getConstructor().newInstance();
		} catch (Exception e) {}
		return null;
	}

	// ==================================================================================
	// RUNTIME STATE VARIABLES
	// Variables that track the current runtime state of the platform
	// ==================================================================================

	/** Flag indicating whether the platform is in headless mode */
	private static boolean isInHeadlessMode;

	/** Flag indicating whether the platform is in server mode */
	private static boolean isInServerMode;

	/** Flag indicating whether experiments are synchronized */
	private static volatile boolean isSynchronized;

	/** The current top level agent */
	private static volatile ITopLevelAgent currentTopLevelAgent;

	// ==================================================================================
	// GUI MANAGEMENT
	// Variables and methods for managing GUI interfaces
	// ==================================================================================

	/** The regular GUI instance for normal mode */
	private static IGui regularGui;

	/** The headless GUI instance for headless mode */
	private static volatile IGui headlessGui;

	// ==================================================================================
	// EXPERIMENT CONTROLLERS
	// Variables and methods for managing experiment controllers
	// ==================================================================================

	/** List of experiment controllers (supports multi-threaded experiments) */
	private static final List<IExperimentController> controllers = new CopyOnWriteArrayList<>();

	/**
	 * Gets all experiment controllers.
	 *
	 * @return the list of experiment controllers
	 */
	public static List<IExperimentController> getControllers() { return controllers; }

	/**
	 * Gets the frontmost (primary) experiment controller.
	 *
	 * @return the frontmost controller, or null if no controllers exist
	 */
	public static IExperimentController getFrontmostController() {
		return controllers.isEmpty() ? null : controllers.get(0);
	}

	// ==================================================================================
	// EVENT LISTENERS
	// Variables and methods for managing event listeners
	// ==================================================================================

	/** Listeners for top-level agent changes */
	private static List<ITopLevelAgentChangeListener> topLevelAgentListeners = new CopyOnWriteArrayList<>();

	/** Listeners for experiment state changes */
	private static List<IExperimentStateListener> experimentStateListeners = new CopyOnWriteArrayList<>();

	// ==================================================================================
	// BENCHMARKING
	// Variables and methods for performance monitoring
	// ==================================================================================

	/** The benchmark agent for performance monitoring */
	private static Benchmark benchmarkAgent;

	// ==================================================================================
	// EXPERIMENT MANAGEMENT
	// Methods for creating, running, and controlling experiments
	// ==================================================================================

	/**
	 * Creates and runs a GUI experiment that replaces the current one (if any). This method handles the full lifecycle
	 * of launching a new experiment in GUI mode.
	 *
	 * @param id
	 *            the experiment ID to run
	 * @param model
	 *            the model containing the experiment
	 */
	public static void runGuiExperiment(final String id, final IModelSpecies model) {
		final IExperimentSpecies newExperiment = model.getExperiment(id);
		if (newExperiment == null) return;

		IExperimentController controller = getFrontmostController();
		if (controller != null) {
			final IExperimentSpecies existingExperiment = controller.getExperiment();
			if (existingExperiment != null) {
				controller.processPause(true);
				if (!getGui().confirmClose(existingExperiment)) return;
			}
		}

		controller = newExperiment.getController();
		if (!controllers.isEmpty()) { closeAllExperiments(false, false); }

		if (newExperiment.isTest()) {
			controllers.add(controller);
			newExperiment.open();
			final IExperimentAgent agent = newExperiment.getAgent();
			agent.step(agent.getScope());
			GAMA.closeExperiment(newExperiment);
		} else if (getGui().openSimulationPerspective(model, id)) {
			controllers.add(controller);
			startBenchmark(newExperiment);
			controller.processOpen(false);
		} else {
			DEBUG.ERR("Unable to launch simulation perspective for experiment " + id + " of model "
					+ model.getFilePath());
		}
	}

	/**
	 * Adds a headless experiment with the specified parameters. This method creates and configures an experiment for
	 * headless execution.
	 *
	 * @param model
	 *            the model containing the experiment
	 * @param expName
	 *            the experiment name
	 * @param params
	 *            the experiment parameters
	 * @param seed
	 *            the random seed (can be null)
	 * @return the created experiment species
	 * @throws GamaRuntimeException
	 *             if the experiment doesn't exist
	 */
	public static synchronized IExperimentSpecies addHeadlessExperiment(final IModelSpecies model, final String expName,
			final IMap<String, Object> params, final Double seed) {
		final IExperimentSpecies currentExperiment = model.getExperiment(expName);
		if (currentExperiment == null) throw GamaRuntimeException
				.error("Experiment " + expName + " does not exist. Please check its name.", getRuntimeScope());

		currentExperiment.setHeadless(true);
		for (final Map.Entry<String, Object> entry : params.entrySet()) {
			final IParameter.Batch v = currentExperiment.getParameterByTitle(entry.getKey());
			if (v != null) {
				currentExperiment.setParameterValueByTitle(currentExperiment.getExperimentScope(), entry.getKey(),
						entry.getValue());
			} else {
				currentExperiment.setParameterValue(currentExperiment.getExperimentScope(), entry.getKey(),
						entry.getValue());
			}
		}

		currentExperiment.open(seed);
		controllers.add(currentExperiment.getController());
		return currentExperiment;
	}

	/**
	 * Closes the specified experiment and performs cleanup.
	 *
	 * @param experiment
	 *            the experiment to close
	 */
	public static void closeExperiment(final IExperimentSpecies experiment) {
		if (experiment == null) return;
		closeController(experiment.getController());
		changeCurrentTopLevelAgent(getPlatformAgent(), false);
	}

	/**
	 * Closes all active experiments and optionally opens the modeling perspective.
	 *
	 * @param andOpenModelingPerspective
	 *            whether to open the modeling perspective after closing
	 * @param immediately
	 *            whether to close immediately without confirmation
	 */
	public static void closeAllExperiments(final boolean andOpenModelingPerspective, final boolean immediately) {
		for (final IExperimentController controller : new ArrayList<>(controllers)) { closeController(controller); }
		getGui().closeSimulationViews(null, andOpenModelingPerspective, immediately);
		PoolUtils.writeStats();
		changeCurrentTopLevelAgent(getPlatformAgent(), false);
	}

	/**
	 * Closes a specific experiment controller and performs cleanup.
	 *
	 * @param controller
	 *            the controller to close
	 */
	private static void closeController(final IExperimentController controller) {
		if (controller == null) return;
		stopBenchmark(controller.getExperiment());
		desynchronizeFrontmostExperiment();
		controller.close();
		controllers.remove(controller);
	}

	/**
	 * Reloads the frontmost experiment.
	 *
	 * @param andWait
	 *            whether to wait for completion
	 * @return true if the reload was successful
	 */
	public static boolean reloadFrontmostExperiment(final boolean andWait) {
		final IExperimentController controller = getFrontmostController();
		return controller != null && controller.processReload(andWait);
	}

	/**
	 * Placeholder method for relaunching the frontmost experiment. TODO: Implementation needed to recompile the model
	 * and run the previous experiment
	 */
	public static void relaunchFrontmostExperiment() {
		// Needs to be done: recompile the model and runs the previous experiment if any
	}

	// ==================================================================================
	// RUNTIME ACCESS - EXPERIMENT AND SIMULATION
	// Methods for accessing current experiments and their components
	// ==================================================================================

	/**
	 * Gets the current simulation agent.
	 *
	 * @return the current simulation agent, or null if no experiment is running
	 */
	public static ISimulationAgent getSimulation() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null) return null;
		return controller.getExperiment().getCurrentSimulation();
	}

	/**
	 * Gets the current experiment plan.
	 *
	 * @return the experiment, or null if no experiment is active
	 */
	public static IExperimentSpecies getExperiment() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null) return null;
		return controller.getExperiment();
	}

	/**
	 * Gets the current experiment agent. Safe to use even if the experiment plan is null.
	 *
	 * @return the experiment agent, or null if no experiment is active
	 */
	public static IExperimentAgent getExperimentAgent() {
		IExperimentSpecies plan = getExperiment();
		if (plan == null) return null;
		return plan.getAgent();
	}

	/**
	 * Gets the current model species.
	 *
	 * @return the model, or null if no experiment is active
	 */
	public static IModelSpecies getModel() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null) return null;
		return controller.getExperiment().getModel();
	}

	// ==================================================================================
	// ERROR HANDLING AND REPORTING
	// Methods for centralized error handling and reporting
	// ==================================================================================

	/**
	 * Reports an error to the UI and determines whether the simulation should continue. This method handles both
	 * warnings and errors according to user preferences.
	 *
	 * @param scope
	 *            the current scope
	 * @param g
	 *            the runtime exception
	 * @param shouldStopSimulation
	 *            whether errors should stop the simulation
	 * @return true if the simulation should continue, false otherwise
	 */
	public static boolean reportError(final IScope scope, final GamaRuntimeException g,
			final boolean shouldStopSimulation) {
		boolean warning = g.isWarning();
		final boolean shouldStop =
				(warning && GamaPreferences.Runtime.CORE_WARNINGS_AS_ERRORS.getValue() || !warning && shouldStopSimulation)
						&& GamaPreferences.Runtime.CORE_STOP_AT_FIRST_ERROR.getValue();
		if (g.isReported()) return !shouldStop;
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null || controller.isDisposing()
				|| controller.getExperiment().getAgent() == null)
			return false;
		// DEBUG.LOG("report error : " + g.getMessage());
		// Returns whether or not to continue
		if (!(g instanceof GamaRuntimeFileException) && scope != null && !scope.reportErrors()) {
			// AD: we still throw exceptions related to files (Issue #1281)
			g.printStackTrace();
			return true;
		}
		if (scope != null && scope.getGui() != null) { scope.getGui().runtimeError(scope, g); }
		g.setReported();

		return !shouldStop;
	}

	/**
	 * Reports an error and throws it if necessary according to error handling configuration. This method handles the
	 * complete lifecycle of error reporting and decides whether to continue execution or throw the exception.
	 *
	 * @param scope
	 *            the current scope
	 * @param g
	 *            the runtime exception
	 * @param shouldStopSimulation
	 *            whether errors should stop the simulation
	 * @throws GamaRuntimeException
	 *             if the error requires stopping execution
	 */
	public static void reportAndThrowIfNeeded(final IScope scope, final GamaRuntimeException g,
			final boolean shouldStopSimulation) {
		// See #3641 -- move this sentence to reportError(): if (g.isReported())
		// return;

		if (scope == null
				|| getExperiment() == null && !(g instanceof GamaRuntimeFileException) && !scope.reportErrors()) {
			// AD: we still throw exceptions related to files (Issue #1281)
			// AD: also takes into account errors reported to GraphicsScope
			g.printStackTrace();
			return;
		}

		if (scope.getAgent() != null) {
			final String name = scope.getAgent().getName();
			if (!g.getAgentsNames().contains(name)) { g.addAgent(name); }
		}
		scope.setCurrentError(g);
		if (scope.isInTryMode()) throw g;

		// DEBUG.LOG("reportAndThrowIfNeeded : " + g.getMessage());
		final boolean shouldStop = !reportError(scope, g, shouldStopSimulation);
		if (shouldStop) {
			if (isInHeadLessMode() && !isInServerMode()) throw g;
			pauseFrontmostExperiment(false);
			throw g;
		}
	}

	// ==================================================================================
	// EXPERIMENT LIFECYCLE CONTROL
	// Methods for controlling experiment execution state (start, pause, step, etc.)
	// ==================================================================================

	/**
	 * Toggles start/pause state of all frontmost experiments.
	 *
	 * @param andWait
	 *            whether to wait for completion
	 * @return true if the operation was successful for all controllers
	 */
	public static boolean startPauseFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processStartPause(andWait)) return false;
		}
		return true;
	}

	/**
	 * Steps forward all frontmost experiments by one cycle.
	 *
	 * @param andWait
	 *            whether to wait for completion
	 * @return true if the operation was successful for all controllers
	 */
	public static boolean stepFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processStep(andWait)) return false;
		}
		return true;
	}

	/**
	 * Steps backward all frontmost experiments by one cycle.
	 *
	 * @param andWait
	 *            whether to wait for completion
	 * @return true if the operation was successful for all controllers
	 */
	public static boolean stepBackFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processBack(andWait)) return false;
		}
		return true;
	}

	/**
	 * Pauses all frontmost experiments.
	 *
	 * @param andWait
	 *            whether to wait for completion
	 * @return true if the operation was successful for all controllers
	 */
	public static boolean pauseFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processPause(andWait)) return false;
		}
		return true;
	}

	/**
	 * Resumes all frontmost experiments.
	 *
	 * @param andWait
	 *            whether to wait for completion
	 * @return true if the operation was successful for all controllers
	 */
	public static boolean resumeFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processStart(andWait)) return false;
		}
		return true;
	}

	/**
	 * Starts the frontmost experiment.
	 *
	 * @param andWait
	 *            whether to wait for completion
	 * @return true if the operation was successful
	 */
	public static boolean startFrontmostExperiment(final boolean andWait) {
		final IExperimentController controller = getFrontmostController();
		return controller != null && controller.processStart(andWait);
	}

	/**
	 * Checks if the frontmost experiment is paused.
	 *
	 * @return true if paused or no experiment is active
	 */
	public static boolean isPaused() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null) return true;
		return controller.isPaused();
	}

	// ==================================================================================
	// SCOPE AND RANDOM UTILITIES
	// Methods for managing scopes, random number generation, and related operations
	// ==================================================================================

	/**
	 * Releases and clears the given scope.
	 *
	 * @param scope
	 *            the scope to release
	 */
	public static void releaseScope(final IScope scope) {
		if (scope != null) { scope.clear(); }
	}

	/**
	 * Creates a copy of the runtime scope with an additional name identifier.
	 *
	 * @param additionalName
	 *            the additional name for the copied scope
	 * @return a copy of the current runtime scope, or null if none exists
	 */
	private static IScope copyRuntimeScope(final String additionalName) {
		// return getCurrentTopLevelAgent().getScope().copy(additionalName);
		final IScope scope = getRuntimeScope();
		if (scope != null) return scope.copy(additionalName);
		return null;
	}

	/**
	 * Gets the current runtime scope. Returns the most appropriate scope based on current context: simulation scope >
	 * experiment scope > platform scope.
	 *
	 * @return the current runtime scope, or null if platform is not loaded
	 */
	public static IScope getRuntimeScope() {
		// If GAMA has not yet been loaded, we return null
		if (__AGENT__ == null) return null;
		// return getCurrentTopLevelAgent().getScope().copy("(copy)");
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null) return getPlatformAgent().getScope();
		final IExperimentAgent a = controller.getExperiment().getAgent();
		if (a == null || a.dead()) return controller.getExperiment().getExperimentScope();
		final ISimulationAgent s = a.getSimulation();
		if (s == null || s.dead()) return a.getScope();
		return s.getScope();
	}

	/**
	 * Gets the current random number generator. Uses experiment-specific random if available, otherwise falls back to
	 * platform random.
	 *
	 * @return the current random generator
	 */
	public static IRandom getCurrentRandom() {
		if (getExperiment() != null) return getRandom(getRuntimeScope());
		return __RANDOM__;
	}

	/**
	 * Gets the random number generator from the given scope, or platform default if scope is null.
	 *
	 * @param scope
	 *            the scope to get random from
	 * @return the random generator
	 */
	public static IRandom getRandom(final IScope scope) {
		if (scope == null) return __RANDOM__;
		return scope.getRandom();
	}

	/**
	 * Executes a function within a temporary scope and ensures proper cleanup. This is the recommended way to execute
	 * scope-dependent operations safely.
	 *
	 * @param <T>
	 *            the return type of the function
	 * @param r
	 *            the function to run within the scope
	 * @return the result of the function
	 */
	public static <T> T run(final InScope<T> r) {
		try (IScope scope = copyRuntimeScope(" in temporary scope block")) {
			return r.run(scope);
		}
	}

	/**
	 * Runs the given operation and refreshes all experiment outputs afterwards. Useful for operations that modify the
	 * experiment state and need to update displays.
	 *
	 * @param r
	 *            the operation to run
	 */
	public static final void runAndUpdateAll(final Runnable r) {
		r.run();
		IExperimentSpecies exp = getExperiment();
		if (exp != null) { exp.refreshAllOutputs(); }
	}

	// ==================================================================================
	// GUI MANAGEMENT - ACCESS AND CONTROL
	// Methods for managing GUI interfaces and headless mode
	// ==================================================================================

	/**
	 * Gets the appropriate GUI instance based on current mode. Returns headless GUI if in headless mode or regular GUI
	 * is not available.
	 *
	 * @return the GUI instance (headless or regular)
	 */
	public static IGui getGui() {
		// either a headless listener or a fully configured gui
		if (isInHeadlessMode || regularGui == null) return getHeadlessGui();
		return regularGui;
	}

	/**
	 * Gets the headless GUI instance, creating it if necessary.
	 *
	 * @return the headless GUI instance
	 */
	public static IGui getHeadlessGui() {
		if (headlessGui == null) { headlessGui = new NullGuiHandler(); }
		return headlessGui;
	}

	/**
	 * Gets the regular (non-headless) GUI instance.
	 *
	 * @return the regular GUI instance
	 */
	public static IGui getRegularGui() { return regularGui; }

	/**
	 * Sets the headless GUI instance.
	 *
	 * @param g
	 *            the new headless GUI instance
	 */
	public static void setHeadlessGui(final IGui g) { headlessGui = g; }

	/**
	 * Sets the regular (non-headless) GUI instance.
	 *
	 * @param g
	 *            the new regular GUI instance
	 */
	public static void setRegularGui(final IGui g) { regularGui = g; }

	/**
	 * Checks if the platform is running in headless mode.
	 *
	 * @return true if in headless mode
	 */
	public static boolean isInHeadLessMode() { return isInHeadlessMode; }

	/**
	 * Checks if the platform is running in server mode.
	 *
	 * @return true if in server mode
	 */
	public static boolean isInServerMode() { return isInServerMode; }

	/**
	 * Sets the platform to headless mode, optionally enabling server mode.
	 *
	 * @param isServer
	 *            true if server mode should be enabled
	 */
	public static void setHeadLessMode(final boolean isServer) {
		isInHeadlessMode = true;
		isInServerMode = isServer;
	}

	// ==================================================================================
	// TOP-LEVEL AGENT MANAGEMENT
	// Methods for managing the current top-level agent and related listeners
	// ==================================================================================

	/**
	 * Registers a listener for top-level agent changes.
	 *
	 * @param listener
	 *            the listener to register
	 */
	public static void registerTopLevelAgentChangeListener(final ITopLevelAgentChangeListener listener) {
		if (!topLevelAgentListeners.contains(listener)) { topLevelAgentListeners.add(listener); }
	}

	/**
	 * Removes a listener for top-level agent changes.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public static void removeTopLevelAgentChangeListener(final ITopLevelAgentChangeListener listener) {
		topLevelAgentListeners.remove(listener);
	}

	/**
	 * Gets the current top-level agent. Automatically computes the most appropriate agent based on current execution
	 * context (simulation > experiment > platform).
	 *
	 * @return the current top-level agent
	 */
	public static ITopLevelAgent getCurrentTopLevelAgent() {
		if (currentTopLevelAgent == null || currentTopLevelAgent.dead() || currentTopLevelAgent.getScope().isClosed()) {
			currentTopLevelAgent = computeCurrentTopLevelAgent();
		}
		return currentTopLevelAgent;
	}

	/**
	 * Changes the current top-level agent and notifies listeners.
	 *
	 * @param current
	 *            the new current top-level agent
	 * @param force
	 *            whether to force the change even if the agent hasn't changed
	 */
	public static void changeCurrentTopLevelAgent(final ITopLevelAgent current, final boolean force) {
		if (currentTopLevelAgent == current && !force) return;
		currentTopLevelAgent = current;
		for (ITopLevelAgentChangeListener listener : topLevelAgentListeners) { listener.topLevelAgentChanged(current); }
	}

	/**
	 * Computes the most appropriate current top-level agent based on execution context.
	 *
	 * @return the computed top-level agent
	 */
	private static ITopLevelAgent computeCurrentTopLevelAgent() {
		IExperimentSpecies plan = getExperiment();
		if (plan == null) return getPlatformAgent();
		IExperimentAgent exp = plan.getAgent();
		if (exp == null || exp.dead()) return getPlatformAgent();
		ISimulationAgent sim = exp.getSimulation();
		if (sim == null || sim.dead()) return exp;
		return sim;
	}

	// ==================================================================================
	// BENCHMARKING UTILITIES
	// Methods for performance monitoring and benchmark management
	// ==================================================================================

	/**
	 * Creates a benchmark stopwatch for the given scope and symbol. Returns a NULL stopwatch if benchmarking is not
	 * active or scope is null.
	 *
	 * @param scope
	 *            the current scope
	 * @param symbol
	 *            the symbol to benchmark (can be IBenchmarkable or ISymbol)
	 * @return a stopwatch for timing the operation
	 */
	public static StopWatch benchmark(final IScope scope, final Object symbol) {
		if (benchmarkAgent == null || scope == null) return StopWatch.NULL;
		return switch (symbol) {
			case IBenchmarkable ib -> benchmarkAgent.record(scope, ib);
			case ISymbol is -> benchmarkAgent.record(scope, is.getDescription());
			default -> StopWatch.NULL;
		};
	}

	/**
	 * Starts benchmarking for the given experiment if configured to be benchmarked.
	 *
	 * @param experiment
	 *            the experiment to start benchmarking for
	 */
	public static void startBenchmark(final IExperimentSpecies experiment) {
		if (experiment.shouldBeBenchmarked()) { benchmarkAgent = new Benchmark(experiment); }
	}

	/**
	 * Stops benchmarking for the given experiment and saves results.
	 *
	 * @param experiment
	 *            the experiment to stop benchmarking for
	 */
	public static void stopBenchmark(final IExperimentSpecies experiment) {
		if (benchmarkAgent != null) { benchmarkAgent.saveAndDispose(experiment); }
		benchmarkAgent = null;
	}

	// ==================================================================================
	// EXPERIMENT SYNCHRONIZATION
	// Methods for managing experiment synchronization state
	// ==================================================================================

	/**
	 * Desynchronizes the frontmost experiment.
	 */
	public static void desynchronizeFrontmostExperiment() {
		isSynchronized = false;
	}

	/**
	 * Checks if experiments are synchronized.
	 *
	 * @return true if synchronized
	 */
	public static boolean isSynchronized() { return isSynchronized; }

	/**
	 * Synchronizes the frontmost experiment.
	 */
	public static void synchronizeFrontmostExperiment() {
		isSynchronized = true;
	}

	// ==================================================================================
	// EXPERIMENT STATE LISTENING
	// Methods for managing experiment state change listeners and notifications
	// ==================================================================================

	/**
	 * Adds an experiment state listener.
	 *
	 * @param listener
	 *            the listener to add
	 */
	public static void addExperimentStateListener(final IExperimentStateListener listener) {
		if (!experimentStateListeners.contains(listener)) { experimentStateListeners.add(listener); }
	}

	/**
	 * Removes an experiment state listener.
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public static void removeExperimentStateListener(final IExperimentStateListener listener) {
		experimentStateListeners.remove(listener);
	}

	/**
	 * Gets the current state of the given experiment.
	 *
	 * @param exp
	 *            the experiment (null means frontmost experiment)
	 * @return the experiment state
	 */
	public static State getExperimentState(final IExperimentSpecies exp) {
		final IExperimentController controller = exp == null ? GAMA.getFrontmostController() : exp.getController();
		if (controller != null) {
			if (controller.isPaused()) return State.PAUSED;
			return State.RUNNING;
		}
		return State.NONE;
	}

	/**
	 * Updates the experiment state and notifies all listeners.
	 *
	 * @param exp
	 *            the experiment
	 * @param state
	 *            the new state
	 */
	public static void updateExperimentState(final IExperimentSpecies exp, final IExperimentStateListener.State state) {
		for (IExperimentStateListener listener : experimentStateListeners) { listener.updateStateTo(exp, state); }
		getGui().getStatus().updateExperimentStatus();
	}

	/**
	 * Updates the experiment state based on current controller state and notifies listeners.
	 *
	 * @param exp
	 *            the experiment
	 */
	public static void updateExperimentState(final IExperimentSpecies exp) {
		updateExperimentState(exp, getExperimentState(exp));
	}

}
