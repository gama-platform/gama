/*******************************************************************************************************
 *
 * GAMA.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import gama.core.common.interfaces.IBenchmarkable;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.ISnapshotMaker;
import gama.core.common.interfaces.ITopLevelAgentChangeListener;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.util.PoolUtils;
import gama.core.common.util.RandomUtils;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.experiment.ExperimentPlan;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.IExperimentController;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.IParameter;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.model.IModel;
import gama.core.kernel.root.PlatformAgent;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.runtime.IExperimentStateListener.State;
import gama.core.runtime.benchmark.Benchmark;
import gama.core.runtime.benchmark.StopWatch;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.runtime.exceptions.GamaRuntimeException.GamaRuntimeFileException;
import gama.dev.DEBUG;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.kernel.GamaBundleLoader;
import gama.gaml.compilation.kernel.GamaMetaModel;

/**
 * Written by drogoul Modified on 23 nov. 2009
 *
 * In GUI Mode, for the moment, only one controller allowed at a time (controllers[0])
 *
 * Description
 */
public class GAMA {

	static {
		DEBUG.OFF();
	}

	/** The Constant VERSION_NUMBER. */
	public static final String VERSION_NUMBER = "0.0.0-SNAPSHOT";

	/** The Constant VERSION. */
	public static final String VERSION = "GAMA " + VERSION_NUMBER;

	/** The agent. */
	private static volatile PlatformAgent agent;

	/** The snapshot agent. */
	private static ISnapshotMaker snapshotAgent;

	/** The benchmark agent. */
	private static Benchmark benchmarkAgent;

	/** The is in headless mode. */
	private static boolean isInHeadlessMode;

	/** The is in headless mode. */
	private static boolean isInServerMode;

	/** The is synchronized. */
	private static volatile boolean isSynchronized;

	/** The regular gui. */
	private static IGui regularGui;

	/** The headless gui. */
	private static volatile IGui headlessGui;

	/** The current top level agent. */
	private static volatile ITopLevelAgent currentTopLevelAgent;

	/** The top level agent listeners. */
	private static List<ITopLevelAgentChangeListener> topLevelAgentListeners = new CopyOnWriteArrayList<>();

	/** The experiment state listeners. */
	private static List<IExperimentStateListener> experimentStateListeners = new CopyOnWriteArrayList<>();

	/** The Constant controllers. */
	// hqnghi: add several controllers to have multi-thread experiments
	private static final List<IExperimentController> controllers = new CopyOnWriteArrayList<>();

	/**
	 * Gets the controllers.
	 *
	 * @return the controllers
	 */
	public static List<IExperimentController> getControllers() { return controllers; }

	/**
	 * Gets the frontmost controller.
	 *
	 * @return the frontmost controller
	 */
	public static IExperimentController getFrontmostController() {
		return controllers.isEmpty() ? null : controllers.get(0);
	}

	/**
	 * New control architecture
	 */

	/**
	 * Create a GUI experiment that replaces the current one (if any)
	 *
	 * @param id
	 * @param model
	 */
	public static void runGuiExperiment(final String id, final IModel model) {
		// DEBUG.OUT("Launching experiment " + id + " of model " + model.getFilePath());
		final IExperimentPlan newExperiment = model.getExperiment(id);
		if (newExperiment == null) // DEBUG.OUT("No experiment " + id + " in model " + model.getFilePath());
			return;
		IExperimentController controller = getFrontmostController();
		if (controller != null) {
			final IExperimentPlan existingExperiment = controller.getExperiment();
			if (existingExperiment != null) {
				controller.processPause(true);
				if (!getGui().confirmClose(existingExperiment)) return;
			}
		}
		controller = newExperiment.getController();
		if (!controllers.isEmpty()) { closeAllExperiments(false, false); }

		if (getGui().openSimulationPerspective(model, id)) {
			controllers.add(controller);
			startBenchmark(newExperiment);
			controller.processOpen(false);
		} else {
			// we are unable to launch the perspective.
			DEBUG.ERR("Unable to launch simulation perspective for experiment " + id + " of model "
					+ model.getFilePath());
		}
	}

	/**
	 * Add an experiment
	 *
	 * @param id
	 * @param model
	 */
	public static synchronized IExperimentPlan addHeadlessExperiment(final IModel model, final String expName,
			final ParametersSet params, final Double seed) {

		final ExperimentPlan currentExperiment = (ExperimentPlan) model.getExperiment(expName);

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
	 * Close experiment.
	 *
	 * @param experiment
	 *            the experiment
	 */
	public static void closeExperiment(final IExperimentPlan experiment) {
		if (experiment == null) return;
		closeController(experiment.getController());
		changeCurrentTopLevelAgent(getPlatformAgent(), false);
	}

	/**
	 * Close all experiments.
	 *
	 * @param andOpenModelingPerspective
	 *            the and open modeling perspective
	 * @param immediately
	 *            the immediately
	 */
	public static void closeAllExperiments(final boolean andOpenModelingPerspective, final boolean immediately) {
		for (final IExperimentController controller : new ArrayList<>(controllers)) { closeController(controller); }
		getGui().closeSimulationViews(null, andOpenModelingPerspective, immediately);
		PoolUtils.writeStats();
		changeCurrentTopLevelAgent(getPlatformAgent(), false);
	}

	/**
	 * Close controller.
	 *
	 * @param controller
	 *            the controller
	 */
	private static void closeController(final IExperimentController controller) {
		if (controller == null) return;
		stopBenchmark(controller.getExperiment());
		desynchronizeFrontmostExperiment();
		controller.close();
		controllers.remove(controller);
	}

	/**
	 *
	 * Access to experiments and their components
	 *
	 */

	public static SimulationAgent getSimulation() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null) return null;
		return controller.getExperiment().getCurrentSimulation();
	}

	/**
	 * Gets the current experiment plan
	 *
	 * @return the experiment
	 */
	public static IExperimentPlan getExperiment() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null) return null;
		return controller.getExperiment();
	}

	/**
	 * Gets the current experiment agent. Safe to use even if the experiment plan is null
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the experiment agent
	 * @date 4 oct. 2023
	 */
	public static IExperimentAgent getExperimentAgent() {
		IExperimentPlan plan = getExperiment();
		if (plan == null) return null;
		return plan.getAgent();
	}

	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	public static IModel getModel() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null)
			return GamaMetaModel.INSTANCE.getAbstractModelSpecies();
		return controller.getExperiment().getModel();
	}

	/**
	 *
	 * Exception and life-cycle related utilities
	 *
	 */

	/**
	 * Report Error: tries to report (on the UI) and returns true if the simulation should continue
	 *
	 * @param scope
	 * @param g
	 * @param shouldStopSimulation
	 * @return
	 */
	public static boolean reportError(final IScope scope, final GamaRuntimeException g,
			final boolean shouldStopSimulation) {
		boolean warning = g.isWarning();
		final boolean shouldStop =
				(warning && GamaPreferences.Runtime.CORE_WARNINGS.getValue() || !warning && shouldStopSimulation)
						&& GamaPreferences.Runtime.CORE_REVEAL_AND_STOP.getValue();

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
	 * Report and throw if needed.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param shouldStopSimulation
	 *            the should stop simulation
	 */
	public static void reportAndThrowIfNeeded(final IScope scope, final GamaRuntimeException g,
			final boolean shouldStopSimulation) {
		// See #3641 -- move this sentence to reportError(): if (g.isReported()) return;
		if (getExperiment() == null && !(g instanceof GamaRuntimeFileException) && scope != null
				&& !scope.reportErrors()) {
			// AD: we still throw exceptions related to files (Issue #1281)
			g.printStackTrace();
			return;
		}

		// DEBUG.LOG("reportAndThrowIfNeeded : " + g.getMessage());
		if (scope != null) {
			if (scope.getAgent() != null) {
				final String name = scope.getAgent().getName();
				if (!g.getAgentsNames().contains(name)) { g.addAgent(name); }
			}
			scope.setCurrentError(g);
			if (scope.isInTryMode()) throw g;
		}
		final boolean shouldStop = !reportError(scope, g, shouldStopSimulation);
		if (shouldStop) {
			if (isInHeadLessMode() && !isInServerMode()) throw g;
			pauseFrontmostExperiment(false);
			throw g;
		}
	}

	/**
	 * Start pause frontmost experiment.
	 */
	public static boolean startPauseFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processStartPause(andWait)) return false;
		}
		return true;
	}

	/**
	 * Step frontmost experiment.
	 */
	public static boolean stepFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processStep(andWait)) return false;
		}
		return true;
	}

	/**
	 * Step back frontmost experiment.
	 */
	public static boolean stepBackFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processBack(andWait)) return false;
		}
		return true;
	}

	/**
	 * Pause frontmost experiment.
	 */
	public static boolean pauseFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processPause(andWait)) return false;
		}
		return true;
	}

	/**
	 * Resume frontmost experiment.
	 */
	public static boolean resumeFrontmostExperiment(final boolean andWait) {
		for (final IExperimentController controller : controllers) {
			if (!controller.processStart(andWait)) return false;
		}
		return true;
	}

	/**
	 * Reload frontmost experiment.
	 */
	public static boolean reloadFrontmostExperiment(final boolean andWait) {
		final IExperimentController controller = getFrontmostController();
		return controller != null && controller.processReload(andWait);
	}

	/**
	 * Start frontmost experiment.
	 */
	public static boolean startFrontmostExperiment(final boolean andWait) {
		final IExperimentController controller = getFrontmostController();
		return controller != null && controller.processStart(andWait);
	}

	/**
	 * Checks if is paused.
	 *
	 * @return true, if is paused
	 */
	public static boolean isPaused() {
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null) return true;
		return controller.isPaused();

	}

	/**
	 *
	 * Scoping utilities
	 *
	 */

	public static void releaseScope(final IScope scope) {
		if (scope != null) { scope.clear(); }
	}

	/**
	 * Copy runtime scope.
	 *
	 * @param additionalName
	 *            the additional name
	 * @return the i scope
	 */
	private static IScope copyRuntimeScope(final String additionalName) {
		// return getCurrentTopLevelAgent().getScope().copy(additionalName);
		final IScope scope = getRuntimeScope();
		if (scope != null) return scope.copy(additionalName);
		return null;
	}

	/**
	 * Gets the runtime scope.
	 *
	 * @return the runtime scope
	 */
	public static IScope getRuntimeScope() {
		// If GAMA has not yet been loaded, we return null
		if (!GamaBundleLoader.LOADED) return null;
		// return getCurrentTopLevelAgent().getScope().copy("(copy)");
		final IExperimentController controller = getFrontmostController();
		if (controller == null || controller.getExperiment() == null) return getPlatformAgent().getScope();
		final ExperimentAgent a = controller.getExperiment().getAgent();
		if (a == null || a.dead()) return controller.getExperiment().getExperimentScope();
		final SimulationAgent s = a.getSimulation();
		if (s == null || s.dead()) return a.getScope();
		return s.getScope();
	}

	/**
	 * Gets the current random.
	 *
	 * @return the current random
	 */
	public static RandomUtils getCurrentRandom() {
		final IScope scope = getRuntimeScope();
		if (scope == null) return new RandomUtils();
		return scope.getRandom();
	}

	/**
	 * The Interface InScope.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public interface InScope<T> {

		/**
		 * The Class Void.
		 */
		public abstract static class Void implements InScope<Object> {

			@Override
			public Object run(final IScope scope) {
				process(scope);
				return null;
			}

			/**
			 * Process.
			 *
			 * @param scope
			 *            the scope
			 */
			public abstract void process(IScope scope);
		}

		/**
		 * Run.
		 *
		 * @param scope
		 *            the scope
		 * @return the t
		 */
		T run(IScope scope);
	}

	/**
	 * Run.
	 *
	 * @param <T>
	 *            the generic type
	 * @param r
	 *            the r
	 * @return the t
	 */
	public static <T> T run(final InScope<T> r) {
		try (IScope scope = copyRuntimeScope(" in temporary scope block")) {
			return r.run(scope);
		}
	}

	/**
	 * Allows to update all outputs after running an experiment
	 *
	 * @param r
	 */
	public static final void runAndUpdateAll(final Runnable r) {
		r.run();
		IExperimentPlan exp = getExperiment();
		if (exp != null) { exp.refreshAllOutputs(); }
	}

	/**
	 * Gets the gui.
	 *
	 * @return the gui
	 */
	public static IGui getGui() {
		// either a headless listener or a fully configured gui
		if (isInHeadlessMode || regularGui == null) return getHeadlessGui();
		return regularGui;
	}

	/**
	 * Gets the headless gui.
	 *
	 * @return the headless gui
	 */
	public static IGui getHeadlessGui() {
		if (headlessGui == null) { headlessGui = new NullGuiHandler(); }
		return headlessGui;
	}

	/**
	 * Gets the regular gui.
	 *
	 * @return the regular gui
	 */
	public static IGui getRegularGui() { return regularGui; }

	/**
	 * @param IGui
	 *            gui
	 */
	public static void setHeadlessGui(final IGui g) { headlessGui = g; }

	/**
	 * Sets the regular gui.
	 *
	 * @param g
	 *            the new regular gui
	 */
	public static void setRegularGui(final IGui g) { regularGui = g; }

	/**
	 * @return
	 */
	public static boolean isInHeadLessMode() { return isInHeadlessMode; }

	/**
	 * Checks if is in server mode.
	 *
	 * @return true, if is in server mode
	 */
	public static boolean isInServerMode() { return isInServerMode; }

	/**
	 *
	 */
	public static void setHeadLessMode(final boolean isServer) {
		isInHeadlessMode = true;
		isInServerMode = isServer;
	}

	/**
	 * Relaunch frontmost experiment.
	 */
	public static void relaunchFrontmostExperiment() {
		// Needs to be done: recompile the model and runs the previous
		// experiment if any

	}

	/**
	 * Register top level agent change listener.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param listener
	 *            the listener
	 * @date 14 août 2023
	 */
	public static void registerTopLevelAgentChangeListener(final ITopLevelAgentChangeListener listener) {
		if (!topLevelAgentListeners.contains(listener)) { topLevelAgentListeners.add(listener); }
	}

	/**
	 * Register top level agent change listener.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param listener
	 *            the listener
	 * @date 14 août 2023
	 */
	public static void removeTopLevelAgentChangeListener(final ITopLevelAgentChangeListener listener) {
		topLevelAgentListeners.remove(listener);
	}

	/**
	 * Access to the one and only 'gama' agent
	 *
	 * @return the platform agent, or creates it if it doesn't exist
	 */
	public static PlatformAgent getPlatformAgent() {
		if (agent == null) { agent = new PlatformAgent(); }
		return agent;
	}

	/**
	 * Gets the current top level agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the current top level agent
	 * @date 14 août 2023
	 */
	public static ITopLevelAgent getCurrentTopLevelAgent() {
		if (currentTopLevelAgent == null || currentTopLevelAgent.dead() || currentTopLevelAgent.getScope().isClosed()) {
			currentTopLevelAgent = computeCurrentTopLevelAgent();
		}
		return currentTopLevelAgent;
	}

	/**
	 * Change current top level agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the agent
	 * @date 14 août 2023
	 */
	public static void changeCurrentTopLevelAgent(final ITopLevelAgent current, final boolean force) {
		if (currentTopLevelAgent == current && !force) return;
		currentTopLevelAgent = current;
		for (ITopLevelAgentChangeListener listener : topLevelAgentListeners) { listener.topLevelAgentChanged(current); }
	}

	/**
	 * Compute current top level agent.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the i top level agent
	 * @date 14 août 2023
	 */
	private static ITopLevelAgent computeCurrentTopLevelAgent() {
		IExperimentPlan plan = getExperiment();
		if (plan == null) return getPlatformAgent();
		IExperimentAgent exp = plan.getAgent();
		if (exp == null || exp.dead()) return getPlatformAgent();
		SimulationAgent sim = exp.getSimulation();
		if (sim == null || sim.dead()) return exp;
		return sim;
	}

	/**
	 *
	 * Benchmarking utilities
	 *
	 */
	public static StopWatch benchmark(final IScope scope, final Object symbol) {
		if (benchmarkAgent == null || symbol == null || scope == null) return StopWatch.NULL;
		if (symbol instanceof IBenchmarkable ib) return benchmarkAgent.record(scope, ib);
		if (symbol instanceof ISymbol is) return benchmarkAgent.record(scope, is.getDescription());
		return StopWatch.NULL;
	}

	/**
	 * Start benchmark.
	 *
	 * @param experiment
	 *            the experiment
	 */
	public static void startBenchmark(final IExperimentPlan experiment) {
		if (experiment.shouldBeBenchmarked()) { benchmarkAgent = new Benchmark(experiment); }
	}

	/**
	 * Stop benchmark.
	 *
	 * @param experiment
	 *            the experiment
	 */
	public static void stopBenchmark(final IExperimentPlan experiment) {
		if (benchmarkAgent != null) { benchmarkAgent.saveAndDispose(experiment); }
		benchmarkAgent = null;
	}

	/**
	 * Toggle sync frontmost experiment.
	 */
	public static void desynchronizeFrontmostExperiment() {
		isSynchronized = false;
	}

	/**
	 * Checks if is synchronized.
	 *
	 * @return true, if is synchronized
	 */
	public static boolean isSynchronized() { return isSynchronized; }

	/**
	 * Synchronize experiment.
	 */
	public static void synchronizeFrontmostExperiment() {
		isSynchronized = true;
	}

	/**
	 * Sets the snapshot maker.
	 *
	 * @param instance
	 *            the new snapshot maker
	 */
	public static void setSnapshotMaker(final ISnapshotMaker instance) {
		if (instance != null) { snapshotAgent = instance; }
	}

	/**
	 * Gets the snapshot maker.
	 *
	 * @return the snapshot maker
	 */
	public static ISnapshotMaker getSnapshotMaker() {
		if (snapshotAgent == null) return IGui.NULL_SNAPSHOT_MAKER;
		return snapshotAgent;
	}

	/**
	 * Adds an IExperimentStateListener
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param listener
	 *            the listener
	 * @date 26 oct. 2023
	 */
	public static void addExperimentStateListener(final IExperimentStateListener listener) {
		if (!experimentStateListeners.contains(listener)) { experimentStateListeners.add(listener); }
	}

	/**
	 * Removes an IExperimentStateListener.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param listener
	 *            the listener
	 * @date 26 oct. 2023
	 */
	public static void removeExperimentStateListener(final IExperimentStateListener listener) {
		experimentStateListeners.remove(listener);
	}

	/**
	 * Gets the experiment state.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param exp
	 *            the exp
	 * @return the experiment state
	 * @date 26 oct. 2023
	 */
	public static State getExperimentState(final IExperimentPlan exp) {
		final IExperimentController controller = exp == null ? GAMA.getFrontmostController() : exp.getController();
		if (controller != null) {
			if (controller.isPaused()) return State.PAUSED;
			return State.RUNNING;
		}
		return State.NONE;
	}

	/**
	 * Update experiment state.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param exp
	 *            the exp
	 * @param state
	 *            the state
	 * @date 26 oct. 2023
	 */
	public static void updateExperimentState(final IExperimentPlan exp, final IExperimentStateListener.State state) {
		for (IExperimentStateListener listener : experimentStateListeners) { listener.updateStateTo(exp, state); }
	}

	/**
	 * Update experiment state.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param exp
	 *            the exp
	 * @date 26 oct. 2023
	 */
	public static void updateExperimentState(final IExperimentPlan exp) {
		updateExperimentState(exp, getExperimentState(exp));
	}

}
