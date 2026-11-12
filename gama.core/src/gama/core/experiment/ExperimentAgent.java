/*******************************************************************************************************
 *
 * ExperimentAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.experiment;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Iterables;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.experiment;
import gama.annotations.species;
import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.constants.Generators;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.agent.IPopulationFactory;
import gama.api.kernel.object.IObject;
import gama.api.kernel.simulation.IClock;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentController;
import gama.api.kernel.simulation.IExperimentController.ExperimentCommandTypes;
import gama.api.kernel.simulation.ISimulationRecorder;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.ExecutionScope;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.ui.IGui;
import gama.api.ui.IOutputManager;
import gama.api.utils.prefs.GamaPreferences;
import gama.api.utils.random.IRandom;
import gama.api.utils.random.RandomUtils;
import gama.api.utils.server.GamaWebSocketServer;
import gama.api.utils.server.IServerConfiguration;
import gama.core.agent.GamlAgent;
import gama.core.experiment.parameters.ExperimentParameter;
import gama.core.experiment.parameters.ParametersSet;
import gama.core.experiment.tools.ActionExecuter;
import gama.core.experiment.tools.ExperimentClock;
import gama.core.outputs.ExperimentOutputManager;
import gama.core.population.GamaPopulation;
import gama.core.population.InternalPopulationFactory;
import gama.core.simulation.SimulationPopulation;
import gama.dev.DEBUG;

/**
 *
 * The class ExperimentAgent. Represents the support for the different experiment species
 *
 * @author drogoul
 * @since 13 mai 2013
 *
 */
@species (
		name = IKeyword.EXPERIMENT,
		doc = @doc ("Agents that manage experiments and their simulations. Use ask experiment { ... } from a model to call play, pause, step_once, step_back, synchronize, desynchronize or reload. Read experiment.status for the same execution status as the server. update_outputs, update_parameters and compact_memory provide maintenance controls; die closes the experiment, not GAMA. Execution commands target this experiment's controller, whereas display synchronization is currently platform-wide."))
@experiment (
		value = IKeyword.GUI_)
@doc ("GUI experiments are experiments used to visualise and interact with simulations through the regular user interface of GAMA. They can declare, parameters, used to populate the Parameters view, outputs like displays or monitors")

public class ExperimentAgent extends GamlAgent implements IExperimentAgent {

	static {
		DEBUG.OFF();
	}

	/** The stop condition. */
	protected final IExpression stopCondition;

	/** The own scope. */
	private final IScope ownScope;

	/** The executer. */
	protected final ActionExecuter executer;

	/** Held while the experiment steps, so that actions requested from other threads do not run meanwhile. */
	private final ReentrantLock stepLock = new ReentrantLock();

	/** The recorder. */
	protected ISimulationRecorder recorder;

	/** The extra parameters map. */
	final IMap<String, Object> extraParametersMap = GamaMapFactory.createOrdered();

	/** The random. */
	protected IRandom random;

	/** The current minimum duration. */
	protected Double currentMinimumDuration = 0d;

	/** The current maximum duration. */
	protected Double currentMaximumDuration = 1d;

	/** The own clock. */
	final protected ExperimentClock ownClock;

	/** The warnings as errors. */
	// Removed while working on #3641 : is redundant with gama.prefs_errors_warnings_errors
	// protected boolean warningsAsErrors = GamaPreferences.Runtime.CORE_WARNINGS_AS_ERRORS.getValue();

	/** The own model path. */
	protected String ownModelPath;

	/**
	 * Memoizes absolute file-path resolutions that do not depend on file existence, shared across this experiment's
	 * simulations. See {@link gama.api.kernel.simulation.IExperimentAgent#getResolvedFilePathCache()}.
	 */
	private final Map<String, String> resolvedFilePathCache = new ConcurrentHashMap<>();

	/** The scheduled. */
	// protected SimulationPopulation populationOfSimulations;
	protected Boolean scheduled = false;

	/** The is on user hold. */
	private volatile boolean isOnUserHold = false;

	/** The default population factory for this kind of experiment. */
	private IPopulationFactory populationFactory;

	/**
	 * Instantiates a new experiment agent.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public ExperimentAgent(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
		super.setGeometry(GamaShapeFactory.buildPoint(GamaPointFactory.create(-1, -1)));
		ownScope = new ExperimentAgentScope();
		ownClock = new ExperimentClock(ownScope);
		executer = new ActionExecuter(ownScope);
		populationFactory = initializePopulationFactory();
		if (getSpecies().isMemorize()) { recorder = GAMA.createExperimentRecorder(); }
		// Should not perform a whole reset as it shuts down UI outputs in comodels (see #2813)
		if (s.getSpecies().getDescription().belongsToAMicroModel()) {
			initialize();
		} else {
			reset();
		}
		IExpression condition = getSpecies().getStopCondition();
		stopCondition = condition == null ? defaultStopCondition() : condition;
	}

	/**
	 * Default stop condition.
	 *
	 * @return the i expression
	 */
	protected IExpression defaultStopCondition() {
		return GAML.getExpressionFactory().getFalse();
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		// We initialize the population that will host the simulation
		createSimulationPopulation();
		// We initialize a new random number generator
		if (random == null) {
			random = new RandomUtils();
		} else {
			random = new RandomUtils(getDefinedSeed(), getDefinedRng());
		}
	}

	@Override
	public final IPopulationFactory getPopulationFactory() { return populationFactory; }

	/**
	 * Initialize population factory. This method should be redefined in experiments that need to return a different
	 * population factory.
	 *
	 * @return the population factory
	 */
	protected IPopulationFactory initializePopulationFactory() {
		return new InternalPopulationFactory();
	}

	/**
	 * Sets the default population factory for this kind of experiment.
	 *
	 * @param factory
	 *            the new default population factory for this kind of experiment
	 */
	public final void setPopulationFactory(final IPopulationFactory factory) { populationFactory = factory; }

	/**
	 * Gets the clock.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the clock
	 * @date 24 sept. 2023
	 */
	@Override
	public IClock getClock() { return ownClock; }

	/**
	 * Reset.
	 */
	public void reset() {
		ownClock.reset();
		// We close any simulation that might be running
		closeSimulations(false);
		initialize();
	}

	/**
	 * Gets the defined rng.
	 *
	 * @return the defined rng
	 */
	public String getDefinedRng() {
		if (GamaPreferences.Runtime.CORE_RND_EDITABLE.getValue())
			return (String) ((ExperimentSpecies) getSpecies()).parameters.get(IKeyword.RNG).value(ownScope);
		return GamaPreferences.External.CORE_RNG.getValue();
	}

	/**
	 * Gets the defined seed.
	 *
	 * @return the defined seed
	 */
	public Double getDefinedSeed() {
		if (GamaPreferences.Runtime.CORE_RND_EDITABLE.getValue()) {
			final IParameter.Batch p =
					(IParameter.Batch) ((ExperimentSpecies) getSpecies()).parameters.get(IKeyword.SEED);
			return p.isDefined() ? (Double) p.value(ownScope) : null;
		}
		return GamaPreferences.External.CORE_SEED_DEFINED.getValue() ? GamaPreferences.External.CORE_SEED.getValue()
				: null;
	}

	@Override
	public void closeSimulations(final boolean andLeaveExperimentPerspective) {
		// We unschedule the simulation if any
		executer.executeDisposeActions();
		if (getSimulationPopulation() != null) { getSimulationPopulation().dispose(); }
		if (!getSpecies().isBatch()) {
			ownScope.getGui().setSelectedAgent(null);
			ownScope.getGui().setHighlightedAgent(null);
			// AD: Fix for issue #1342 -- verify that it does not break
			// something else in the dynamics of closing/opening
			ownScope.getGui().closeDialogs(ownScope);
			ownScope.getGui().closeSimulationViews(ownScope, andLeaveExperimentPerspective, true);
		}
	}

	/**
	 * Method primDie()
	 *
	 * @see msi.gama.metamodel.agent.MinimalAgent#primDie(msi.gama.runtime.IScope)
	 */
	@Override
	@action (
			name = "die",
			doc = @doc (
					value = "Closes this experiment and disposes its simulations, outputs and controller. Does not quit GAMA; use ask gama { do die; } to quit the platform.",
					returns = "nil",
					examples = @example (
							value = "ask experiment { do die; }",
							isExecutable = false)))
	public Object primDie(final IScope scope) throws GamaRuntimeException {
		if (dying || dead) return null;
		GAMA.closeExperiment(getSpecies());
		GAMA.getGui().closeSimulationViews(scope, true, false);
		return null;
	}

	/**
	 * Dispose.
	 */
	@Override
	public void dispose() {
		if (dying || dead) return;
		dying = true;
		getSpecies().getArchitecture().abort(ownScope);
		closeSimulations(!getSpecies().isReloading());
		GAMA.releaseScope(ownScope);
		super.dispose();
	}

	/**
	 * Redefinition of the callback method
	 *
	 * @see msi.gama.metamodel.agent.GamlAgent#_init_(msi.gama.runtime.IScope)
	 */
	@Override
	public void _init_(final IScope scope) {
		if (scope.interrupted()) return;
		if (automaticallyCreateFirstSimulation()) {
			createSimulation(ParametersSet.EMPTY, shouldScheduleSimulations());
		}
		// We execute any behavior defined in GAML.
		super._init_(scope);
		tryToRecordSimulations();
	}

	/**
	 * Should schedule first simulation.
	 *
	 * @return true, if successful
	 */
	public boolean shouldScheduleSimulations() {
		return scheduled;
	}

	/**
	 * Pre step.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	@Override
	protected boolean preStep(final IScope scope) {
		ownClock.beginCycle();
		return super.preStep(scope);
	}

	/**
	 * Post step.
	 *
	 * @param scope
	 *            the scope
	 */
	@Override
	protected void postStep(final IScope scope) {
		// super.postStep(scope);
		executer.executeEndActions();
		executer.executeOneShotActions();
		// Save simulation state in the history
		tryToRecordSimulations();
		final IOutputManager outputs = getOutputManager();
		if (outputs != null) { outputs.step(scope); }
		ownClock.step();
		informStatus();
		GAMA.updateExperimentState(getSpecies());
	}

	/**
	 * Try to record simulations.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 2 sept. 2023
	 */
	private void tryToRecordSimulations() {
		if (isRecord()) {
			SimulationPopulation sp = getSimulationPopulation();
			IExpression shouldRecord = getSpecies().shouldRecord();
			if (sp != null && shouldRecord != null) {
				List<ISimulationAgent> sims = new ArrayList<>(sp);
				for (ISimulationAgent sim : sims) {
					sim.runInOwnThread(() -> {
						IScope scope = sim.getScope();
						if (!scope.interrupted() && Cast.asBool(scope, shouldRecord.value(scope))) {
							recorder.record(sim);
						}
					});
				}
			}
		}
	}

	/**
	 * Automatically create first simulation.
	 *
	 * @return true, if successful
	 */
	protected boolean automaticallyCreateFirstSimulation() {
		return true;
	}

	/**
	 * Inits the.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	@Override
	public boolean init(final IScope scope) {
		showParameters(scope);
		GAMA.changeCurrentTopLevelAgent(this, true);
		GAMA.desynchronizeFrontmostExperiment();
		scope.getGui().clearErrors(scope);
		super.init(scope);
		final IOutputManager outputs = getOutputManager();
		if (outputs != null) { outputs.init(scope); }

		// scope.getGui().getStatus().informStatus(scope, "Experiment ready");
		GAMA.updateExperimentState(getSpecies());
		return true;
	}

	/**
	 * Show parameters.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @date 6 sept. 2023
	 */
	private void showParameters(final IScope scope) {
		final ExperimentOutputManager manager = (ExperimentOutputManager) getOutputManager();
		ISymbol layout = manager.getLayout() == null ? manager : manager.getLayout();
		final Boolean showParameters = layout.getFacetValue(scope, "parameters", null);
		if (showParameters != null && !showParameters) {
			scope.getGui().hideParameters();
		} else {
			scope.getGui().updateParameters(false);
		}
	}

	/**
	 * Creates the simulation.
	 *
	 * @param parameters
	 *            the parameters
	 * @param scheduleIt
	 *            the schedule it
	 * @return the simulation agent
	 */
	@Override
	public ISimulationAgent createSimulation(final IMap<String, Object> parameters, final boolean scheduleIt) {
		final SimulationPopulation pop = getSimulationPopulation();
		if (pop == null) return null;
		final ParametersSet ps = getParameterValues();
		if (parameters != null) { ps.putAll(parameters); }
		final IList<Map<String, Object>> list = GamaListFactory.create(Types.MAP);
		list.add(ps);
		final IList<? extends ISimulationAgent> c = pop.createAgents(ownScope, 1, list, false, scheduleIt);
		return c.get(0);
	}

	/**
	 * Gets the parameter values.
	 *
	 * @return the parameter values
	 */
	@Override
	public ParametersSet getParameterValues() {
		final Map<String, IParameter> parameters = getSpecies().getParameters();
		final ParametersSet ps = new ParametersSet(ownScope, parameters, false);
		ps.putAll(extraParametersMap);
		return ps;
	}

	/**
	 * Gets the random generator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the random generator
	 * @date 24 sept. 2023
	 */
	@Override
	public IRandom getRandomGenerator() { return random; }

	/**
	 * Schedule.
	 *
	 * @param scope
	 *            the scope
	 */
	@Override
	public IExecutionResult schedule(final IScope scope) {
		scheduled = true;
		// The experiment agent is scheduled in the global scheduler
		return getSpecies().getController().schedule(this);
	}

	/**
	 * Building the simulation agent and its population
	 */

	@SuppressWarnings ("unchecked")
	protected void createSimulationPopulation() {
		final IModelSpecies model = getModel();
		SimulationPopulation pop = (SimulationPopulation) this.getMicroPopulation(model);
		if (pop == null) {
			pop = new SimulationPopulation(this, model);
			setAttribute(model.getName(), pop);
			pop.initializeFor(ownScope);
		}
		microPopulations.set(new IPopulation[] { pop });
	}

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	@Override
	public IScope getScope() { return ownScope; }

	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	@Override
	public IModelSpecies getModel() { return getSpecies().getModel(); }

	/**
	 * Gets the experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the experiment
	 * @date 24 sept. 2023
	 */
	@Override
	public IExperimentAgent getExperiment() { return this; }

	@Override
	public IExperimentSpecies getSpecies() { return (IExperimentSpecies) super.getSpecies(); }

	/**
	 * Sets the location.
	 *
	 * @param p
	 *            the p
	 * @return the gama point
	 */
	@Override
	public IPoint setLocation(final IPoint p) {
		return p;
	}

	/**
	 * Sets the geometry.
	 *
	 * @param newGlobalGeometry
	 *            the new geometry
	 */
	@Override
	public void setGeometry(final IShape newGlobalGeometry) {}

	/**
	 * GAML global variables
	 *
	 */

	@Override
	public Iterable<? extends IParameter.Batch> getDefaultParameters() {
		if (!getSpecies().isHeadless() && !GamaPreferences.Runtime.CORE_RND_EDITABLE.getValue())
			return new ArrayList<>();
		final List<ExperimentParameter> params = new ArrayList<>();
		final String cat = getExperimentParametersCategory();
		ExperimentParameter p = new ExperimentParameter(getScope(), getSpecies().getVar(IKeyword.RNG),
				"Random number generator", cat, Generators.names(), false);

		params.add(p);
		// Reload reapplies the stored parameter value before creating the simulation.
		p = new ExperimentParameter(getScope(), getSpecies().getVar(IKeyword.SEED), "Default random seed", cat,
				"(current seed)", null, true);
		p.setDefined(GamaPreferences.External.CORE_SEED_DEFINED.getValue());
		params.add(p);
		return params;
	}

	/**
	 * Gets the experiment parameters category.
	 *
	 * @return the experiment parameters category
	 */
	protected String getExperimentParametersCategory() { return IExperimentSpecies.SYSTEM_CATEGORY_PREFIX; }

	@Override

	public Double getMinimumDuration() { return currentMinimumDuration; }

	/**
	 * Gets the maximum duration.
	 *
	 * @return the maximum duration
	 */
	@Override
	public Double getMaximumDuration() { return currentMaximumDuration; }

	@Override
	public void setMinimumDuration(final Double d) {
		// d is in seconds, but the slider expects milliseconds
		// DEBUG.LOG("Minimum duration set to " + d);
		if (d > currentMaximumDuration) { currentMaximumDuration = d; }
		setMinimumDurationExternal(d);
		ownScope.getGui().updateSpeedDisplay(ownScope, currentMinimumDuration * 1000, currentMaximumDuration * 1000,
				false);
	}

	/**
	 * Sets the maximum duration.
	 *
	 * @param d
	 *            the new maximum duration
	 */
	@Override
	public void setMaximumDuration(Double d) {
		// d is in seconds, but the slider expects milleseconds
		// DEBUG.LOG("Maximum duration set to " + d);
		if (d <= 0) { d = 1d; }
		if (d < currentMinimumDuration) { d = currentMinimumDuration; }
		currentMaximumDuration = d;
		ownScope.getGui().updateSpeedDisplay(ownScope, currentMinimumDuration * 1000, currentMaximumDuration * 1000,
				false);
	}

	/**
	 * Called normally from UI directly. Does not notify the GUI.
	 *
	 * @param d
	 */
	@Override
	public void setMinimumDurationExternal(final Double d) { currentMinimumDuration = d; }

	@Override
	public String getWorkingPath() {
		if (ownModelPath == null) { ownModelPath = getModel().getWorkingPath() + "/"; }
		return ownModelPath;
	}

	@Override
	public List<String> getWorkingPaths() {
		final List<String> result = new ArrayList<>();
		result.add(getWorkingPath());
		result.addAll(getModel().getImportedPaths());
		return result;
	}

	@Override
	public Map<String, String> getResolvedFilePathCache() { return resolvedFilePathCache; }

	/**
	 * Sets the working path.
	 *
	 * @param p
	 *            the new working path
	 */
	@Override
	public void setWorkingPath(final String p) {
		if (p.endsWith("/")) {
			ownModelPath = p;
		} else {
			ownModelPath = p + "/";
		}
	}

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	@Override
	public IMap<String, Object> getParameters(final IScope scope) {
		return getParameterValues();
	}

	/**
	 * Gets the project path.
	 *
	 * @return the project path
	 */
	@Override

	public String getProjectPath() { return getModel().getProjectPath() + FileSystems.getDefault().getSeparator(); }

	/**
	 * Gets the cycle.
	 *
	 * @param scope
	 *            the scope
	 * @return the cycle
	 */
	@Override
	public Integer getCycle(final IScope scope) {
		if (ownClock != null) return ownClock.getCycle();
		return 0;
	}

	/**
	 * Resolves this agent's controller, never the frontmost experiment's controller.
	 */
	private IExperimentController interactiveController(final IScope scope) {
		final IExperimentController controller = getSpecies().getController();
		if (dead || dying || controller == null || controller.isDisposing())
			throw GamaRuntimeException.error("This experiment has no active controller", scope);
		if (!controller.supportsInteractiveControl()) throw GamaRuntimeException
				.error("This experiment's controller does not support interactive execution commands", scope);
		return controller;
	}

	private void checkControlRequest(final IScope scope, final boolean accepted, final String command) {
		if (!accepted) throw GamaRuntimeException.error(
				"Unable to " + command + " the experiment: its controller refused the request or its command queue is full",
				scope);
	}

	private Object requestControl(final IScope scope, final ExperimentCommandTypes command) {
		final IExperimentController controller = interactiveController(scope);
		// Submit destructive or stepping commands only after the requesting experiment cycle has finished.
		executeAction(actionScope -> {
			final boolean accepted = switch (command) {
				case _STEP -> controller.processStep(1, false);
				case _BACK -> controller.processBack(1, false);
				case _RELOAD -> controller.processReload(false);
				default -> throw new IllegalArgumentException("Unsupported experiment command: " + command);
			};
			checkControlRequest(actionScope, accepted, command.name().substring(1).toLowerCase(java.util.Locale.ROOT));
			return null;
		});
		return this;
	}

	@Override
	@action (
			name = "play",
			doc = @doc (
					value = "Requests continuous execution of this experiment and its scheduled simulations, like the UI Play control or server play command. Does not toggle pause. Calling it when already running leaves it running.",
					returns = "This experiment, once the request is accepted; completion is asynchronous. A missing, disposed, unsupported or full controller raises a runtime error.",
					comment = "Works with interactive GUI and server controllers. Non-interactive command-line/headless controllers are unsupported. An ended server execution may require a new load.",
					examples = @example (
							value = "ask experiment { do play; }",
							isExecutable = false),
					see = { "pause", "step_once", "status" }))
	public Object play(final IScope scope) {
		checkControlRequest(scope, interactiveController(scope).processStart(false), "play");
		return this;
	}

	@Override
	@action (
			name = "pause",
			doc = @doc (
					value = "Pauses this experiment's scheduler and therefore all its scheduled simulations, like UI Pause or server pause. A cycle already executing is allowed to finish; this does not pause only one simulation. Calling it when paused leaves it paused.",
					returns = "This experiment once the pause request is accepted. A missing, disposed, unsupported or full controller raises a runtime error.",
					comment = "Normally updates the scheduler state immediately. From a display thread the request is queued to avoid blocking rendering. Use play to continue; model reflexes cannot resume themselves once execution is paused.",
					examples = @example (
							value = "ask experiment { do pause; }",
							isExecutable = false),
					see = { "play", "status" }))
	public Object pause(final IScope scope) {
		checkControlRequest(scope, interactiveController(scope).processPause(true), "pause");
		return this;
	}

	@action (
			name = "step_once",
			doc = @doc (
					value = "Requests one experiment step, then leaves its controller paused, like UI Step or server step with nb_steps: 1. Steps the scheduled simulations, not just the current simulation.",
					returns = "This experiment; the request is submitted after the current experiment cycle, if any, and completes asynchronously.",
					comment = "A cycle already in progress finishes first. Do not call this every cycle unless deliberately requesting continued stepping. In a batch experiment one experiment step can run an entire exploration. Requires an interactive controller; rejected requests raise runtime errors.",
					examples = @example (
							value = "ask experiment { do step_once; }",
							isExecutable = false),
					see = { "play", "pause", "step_back" }))
	public Object stepOnce(final IScope scope) {
		return requestControl(scope, ExperimentCommandTypes._STEP);
	}

	@action (
			name = "step_back",
			doc = @doc (
					value = "Requests restoration of the preceding recorded simulation states and leaves the controller paused, like UI Back or server stepBack with nb_steps: 1. Applies to this experiment's simulations that have recorded history.",
					returns = "This experiment; submission is deferred until the current experiment cycle ends and restoration is asynchronous.",
					comment = "Requires experiment recording (record: true), an available recorder and an interactive controller. Without a recorder a runtime error is raised. No earlier state means there is nothing to restore. Restores simulation state, not external files, network effects or the experiment clock. Random-seed behavior follows the experiment's keep_seed setting.",
					examples = @example (
							value = "ask experiment { do step_back; }",
							isExecutable = false),
					see = { "step_once", "pause", "reload" }))
	public Object stepBack(final IScope scope) {
		if (recorder == null) throw GamaRuntimeException.error(
				"step_back requires experiment recording (record: true) and an available simulation recorder", scope);
		return requestControl(scope, ExperimentCommandTypes._BACK);
	}

	@action (
			name = "reload",
			doc = @doc (
					value = "Requests reinitialization of this experiment, like UI Reload or server reload. Disposes the current experiment agent and simulations and creates fresh ones using the current experiment definition and parameter values; init runs again.",
					returns = "The old experiment agent; submission is deferred until the current cycle ends and reload is asynchronous. Do not retain this reference: reacquire experiment and simulation after reload.",
					comment = "This is not a source recompilation, does not accept new parameters and does not undo external side effects. Running/paused behavior follows the controller: GUI reload normally resumes a previously running experiment and honors autorun. Server behavior follows server reload. Requires an interactive controller; rejected requests raise runtime errors.",
					examples = @example (
							value = "ask experiment { do reload; }",
							isExecutable = false),
					see = { "play", "pause", "die" }))
	public Object reload(final IScope scope) {
		return requestControl(scope, ExperimentCommandTypes._RELOAD);
	}

	@action (
			name = "synchronize",
			doc = @doc (
					value = "Enables waiting for display rendering before simulation execution advances, like the UI Synchronize control. Does not play or pause the experiment.",
					returns = "This experiment.",
					comment = "Currently changes a platform-wide flag shared by all experiments. It does not synchronize simulations with each other, wall-clock time, or server clients. Without displays there is no rendering to wait for.",
					examples = @example (
							value = "ask experiment { do synchronize; }",
							isExecutable = false),
					see = { "desynchronize", "is_synchronized" }))
	public Object synchronize(final IScope scope) {
		GAMA.synchronizeFrontmostExperiment();
		return this;
	}

	@action (
			name = "desynchronize",
			doc = @doc (
					value = "Disables waiting for display rendering, like the UI Synchronize control when switched off. Execution and rendering may advance at different rates; does not play or pause the experiment.",
					returns = "This experiment.",
					comment = "Currently changes the same platform-wide flag for every experiment. Some rendered frames may be skipped as simulations advance.",
					examples = @example (
							value = "ask experiment { do desynchronize; }",
							isExecutable = false),
					see = { "synchronize", "is_synchronized" }))
	public Object desynchronize(final IScope scope) {
		GAMA.desynchronizeFrontmostExperiment();
		return this;
	}

	/**
	 * Update displays.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@Override
	@action (
			name = "update_outputs",
			doc = @doc (
					value = "Refreshes this experiment's outputs and its simulations' outputs, even when paused. With recompute: true runs an output-manager step: refreshable outputs recompute and refresh according to their refresh conditions, and autosave is evaluated. Does not advance simulation agents or their clocks.",
					returns = "This experiment.",
					examples = @example (
							value = "ask experiment { do update_outputs recompute: false; }",
							isExecutable = false)),
			args = { @arg (
					name = "recompute",
					type = IType.BOOL,
					doc = @doc ("Required. true runs an output-manager step, respecting output refresh conditions; false only refreshes presentation. Recomputing can have side effects in output expressions and trigger autosave.")) })

	public Object updateDisplays(final IScope scope) {
		final Boolean force = scope.getBoolArg("recompute");
		if (force) {
			getSpecies().recomputeAndRefreshAllOutputs();
		} else {
			getSpecies().refreshAllOutputs();
		}
		return this;
	}

	/**
	 * Update parameters.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@Override
	@action (
			name = "update_parameters",
			doc = @doc (
					value = "Refreshes the GUI Parameters view from current parameter values. Does not change parameter values, reload simulations or advance execution. Has no visible effect without a GUI Parameters view.",
					comment = "The Parameters view is a frontmost-experiment GUI facility: this is not an independent parameter view for a background or server experiment.",
					returns = "This experiment.",
					examples = @example (
							value = "ask experiment { do update_parameters; }",
							isExecutable = false)))

	public Object updateParameters(final IScope scope) {
		getSpecies().refreshAllParameters();
		return this;
	}

	/**
	 * Compact memory.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@Override
	@action (
			name = "compact_memory",
			doc = @doc (
					value = "Requests JVM garbage collection of unreachable objects across GAMA, not just this experiment. The JVM may defer or ignore the request; reachable agents, recorded history and outputs are not removed.",
					returns = "This experiment.",
					comment = "May cause a platform-wide execution pause. Prefer normal memory management rather than calling it every cycle.",
					examples = @example (
							value = "ask experiment { do compact_memory; }",
							isExecutable = false)))
	public Object compactMemory(final IScope scope) {
		System.gc();
		return this;

	}

	/**
	 * Gets the seed.
	 *
	 * @return the seed
	 */
	@Override
	public Double getSeed() {
		final Double seed = random.getSeed();
		// DEBUG.LOG("experiment agent get seed: " + seed);
		return seed == null ? 0d : seed;
	}

	/**
	 * Sets the seed.
	 *
	 * @param s
	 *            the new seed
	 */
	@Override
	public void setSeed(final Double s) {
		// DEBUG.LOG("experiment agent set seed: " + s);
		Double seed;
		if (s == null || s.doubleValue() == 0d) {
			seed = null;
		} else {
			seed = s;
		}
		getRandomGenerator().setSeed(seed, true);
	}

	/**
	 * Gets the usage.
	 *
	 * @return the usage
	 */
	@Override
	public Integer getUsage() {
		final Integer usage = random.getUsage();
		return usage == null ? 0 : usage;
	}

	/**
	 * Sets the usage.
	 *
	 * @param s
	 *            the new usage
	 */
	@Override
	public void setUsage(final Integer s) {
		Integer usage = s;
		if (s == null) { usage = 0; }
		getRandomGenerator().setUsage(usage);
	}

	/**
	 * Gets the rng.
	 *
	 * @return the rng
	 */
	@Override
	public String getRng() { return getRandomGenerator().getRngName(); }

	/**
	 * Sets the rng.
	 *
	 * @param newRng
	 *            the new rng
	 */
	@Override
	public void setRng(final String newRng) {
		getRandomGenerator().setGenerator(newRng, true);
	}

	@Override
	public SimulationPopulation getSimulationPopulation() {
		// Lazy initialization of the population, in case
		// createSimulationPopulation();
		return (SimulationPopulation) getMicroPopulation(getModel());
	}

	/**
	 * Gets the simulations.
	 *
	 * @return the simulations
	 */
	@Override
	public IList<? extends IAgent> getSimulations() { return getSimulationPopulation().copy(ownScope); }

	/**
	 * Gets the simulation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the simulation
	 * @date 24 sept. 2023
	 */
	@Override
	public ISimulationAgent getSimulation() {
		if (getSimulationPopulation() != null) return getSimulationPopulation().getCurrentSimulation();
		return null;
	}

	/**
	 * Sets the simulation.
	 *
	 * @param sim
	 *            the new simulation
	 */
	@Override
	public void setSimulation(final ISimulationAgent sim) {
		getSimulationPopulation().setCurrentSimulation(sim);
	}

	/**
	 * Checks if is on user hold.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is on user hold
	 * @date 24 sept. 2023
	 */
	@Override
	public boolean isOnUserHold() { return isOnUserHold; }

	/**
	 * Sets the on user hold.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param state
	 *            the new on user hold
	 * @date 24 sept. 2023
	 */
	@Override
	public void setOnUserHold(final boolean state) { isOnUserHold = state; }

	/**
	 * Gets the population for.
	 *
	 * @param species
	 *            the species
	 * @return the population for
	 */
	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final ISpecies species) {
		if (species == getModel()) return getSimulationPopulation();
		final ISimulationAgent sim = getSimulation();
		if (sim == null) return createEmptyPopulation(species);
		// Issue #3983
		final IModelDescription micro = species.getDescription().getModelDescription();
		final IModelDescription main = this.getModel().getDescription();
		if (main.getMicroModel(micro.getMicroAlias()) == null) return sim.getPopulationFor(species.getName());
		return sim.getPopulationFor(species);
	}

	/**
	 * @param species
	 * @return
	 */
	private IPopulation<? extends IAgent> createEmptyPopulation(final ISpecies species) {
		return new GamaPopulation<>(null, species);
	}

	/**
	 * Inform status.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 24 sept. 2023
	 */
	protected void informStatus() {
		if (isHeadless()) return;
		ownScope.getGui().getStatus().updateExperimentStatus();
	}

	/**
	 * Backward.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	@Override
	public boolean backward(final IScope scope) {
		// No recorder when the experiment is not declared with `record:` or no implementation is available.
		if (recorder == null) return false;
		final boolean[] restored = { false };
		try {
			GAMA.runAndUpdateAll(() -> {
				for (ISimulationAgent sim : getSimulationPopulation()) {
					if (recorder.canStepBack(sim)) {
						// restore() returns false when there is nothing left to restore.
						if (!sim.computeInOwnThread(() -> recorder.restore(sim))) { continue; }
						restored[0] = true;
						if (!((ExperimentSpecies) this.getSpecies()).keepsSeed()) {
							final double seed = random.next();
							sim.runInOwnThread(() -> sim.setRandomGenerator(
									new RandomUtils(seed, sim.getRandomGenerator().getRngName())));
						}
					}

				}
			});
		} finally {
			informStatus();
			GAMA.updateExperimentState(getSpecies());
		}
		return restored[0];
	}

	/**
	 *
	 * The class ExperimentAgentScope. A "pass through" class used when the simulation is not yet computed and when an
	 * experiment tries to have access to simulation attributes (which is the case in most of the models). It returns
	 * and sets the values of parameters when they are defined in the experiment, and also allows extra parameters to be
	 * set. TODO Allow this class to read the init values of global variables that are not defined as parameters.
	 *
	 * @author drogoul
	 * @since 22 avr. 2013
	 *
	 */
	public class ExperimentAgentScope extends ExecutionScope {

		/** The server config. */
		IServerConfiguration serverConfig = GamaWebSocketServer.NULL;

		/**
		 * Gets the server configuration.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the server configuration
		 * @date 3 nov. 2023
		 */
		@Override
		public IServerConfiguration getServerConfiguration() { return serverConfig; }

		/**
		 * Sets the server configuration.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @date 3 nov. 2023
		 */
		@Override
		public void setServerConfiguration(final IServerConfiguration config) { serverConfig = config; }

		/**
		 * Method getRandom()
		 *
		 * @see msi.gama.runtime.IScope#getRandom()
		 */
		@Override
		public IRandom getRandom() { return ExperimentAgent.this.random; }

		/**
		 * Instantiates a new experiment agent scope.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @date 13 août 2023
		 */
		public ExperimentAgentScope() {
			super(ExperimentAgent.this);
		}

		/**
		 * Instantiates a new experiment agent scope.
		 *
		 * @param name
		 *            the name
		 */
		public ExperimentAgentScope(final String name) {
			super(ExperimentAgent.this, name);
		}

		/**
		 * Copy.
		 *
		 * @param additionalName
		 *            the additional name
		 * @return the i scope
		 */
		@Override
		public IScope copy(final String additionalName) {
			ExperimentAgentScope copy = new ExperimentAgentScope(additionalName);
			copy.push(getCurrentSymbol());
			return copy;
		}

		@Override
		public IScope detachedCopy(final String additionalName) {
			// Copies never share the frames of this scope
			return copy(additionalName);
		}

		/**
		 * Gets the experiment.
		 *
		 * @return the experiment
		 */
		@Override
		public IExperimentAgent getExperiment() { return ExperimentAgent.this; }

		/**
		 * Gets the global var value.
		 *
		 * @param varName
		 *            the var name
		 * @return the global var value
		 */
		@Override
		public Object getGlobalVarValue(final String varName) {
			if (getAgent() != ExperimentAgent.this) return super.getGlobalVarValue(varName);

			// First case: we have the variable inside the experiment.
			if (ExperimentAgent.this.hasAttribute(varName) || getSpecies().hasVar(varName))
				return super.getGlobalVarValue(varName);
			// Second case: the simulation is not null, so it should handle it
			final ISimulationAgent sim = getSimulation();
			if (sim != null && !sim.dead()) return sim.getScope().getGlobalVarValue(varName);
			// Third case, the simulation is null but the model defines this variable (see #2044). We then grab its
			// initial value if possible
			// However, if the experiment is defined with keep_simulations: false, we should not give access to the
			// value, as no simulations can be made available (see #2727)
			if (this.getModel().getSpecies().hasVar(varName)) {
				final IVariable var = getModel().getSpecies().getVar(varName);
				boolean variableIsNotConst = !var.isNotModifiable();
				boolean simulationsAreNotKept = !getExperiment().getSpecies().keepsSimulations();
				// Addition of a condition for #287, in order to not trigger the error when we compute parameters
				boolean isInParametersUpdate = getCurrentSymbol() instanceof ExperimentParameter;

				if (variableIsNotConst && simulationsAreNotKept && !isInParametersUpdate)
					throw GamaRuntimeException.error("This experiment does not keep its simulations. " + varName
							+ " cannot be retrieved in this context", this);
				return var.getInitialValue(this);
			}
			// Fourth case: this is a parameter, so we get it from the species
			if (getSpecies().hasParameter(varName)) return getSpecies().getExperimentScope().getGlobalVarValue(varName);
			// Fifth case: it is an extra parameter
			return extraParametersMap.get(varName);
		}

		/**
		 * Checks for access to global var.
		 *
		 * @param varName
		 *            the var name
		 * @return true, if successful
		 */
		@Override
		public boolean hasAccessToGlobalVar(final String varName) {
			if (getAgent() != ExperimentAgent.this) return super.hasAccessToGlobalVar(varName);
			if (ExperimentAgent.this.hasAttribute(varName) || getSpecies().hasVar(varName)
					|| this.getModel().getSpecies().hasVar(varName) || getSpecies().hasParameter(varName))
				return true;
			return extraParametersMap.containsKey(varName);
		}

		/**
		 * Sets the global var value.
		 *
		 * @param name
		 *            the name
		 * @param v
		 *            the v
		 */
		@Override
		public void setGlobalVarValue(final String name, final Object v) {
			if (getAgent() != ExperimentAgent.this) {
				super.setGlobalVarValue(name, v);
				return;
			}
			if (getSpecies().hasVar(name)) {
				super.setGlobalVarValue(name, v);
				return;
			}
			final ISimulationAgent sim = getSimulation();
			if (sim != null && !sim.dead() && sim.getSpecies().hasVar(name)) {
				sim.getScope().setGlobalVarValue(name, v);
			} else {
				extraParametersMap.put(name, v);
			}
		}

		/**
		 * Sets the agent var value.
		 *
		 * @param a
		 *            the a
		 * @param name
		 *            the name
		 * @param value
		 *            the value
		 */
		@Override
		public void setAgentVarValue(final IObject a, final String name, final Object value) {
			if (a == ExperimentAgent.this) {
				final boolean pushed = push(a);
				try {
					setGlobalVarValue(name, value);
				} finally {
					if (pushed) { pop(a); }
				}
			} else {
				super.setAgentVarValue(a, name, value);
			}
		}

		/**
		 * Gets the agent var value.
		 *
		 * @param a
		 *            the a
		 * @param varName
		 *            the var name
		 * @return the agent var value
		 */
		@Override
		public Object getAgentVarValue(final IObject a, final String varName) {
			if (a == ExperimentAgent.this) {
				final boolean pushed = push(a);
				try {
					return getGlobalVarValue(varName);
				} finally {
					if (pushed) { pop(a); }
				}
			}
			return super.getAgentVarValue(a, varName);
		}

		/**
		 * Gets the gui.
		 *
		 * @return the gui
		 */
		@Override
		public IGui getGui() {
			if (getSpecies().isHeadless()) return GAMA.getHeadlessGui();
			return GAMA.getRegularGui();
		}

	}

	/**
	 * @return
	 */
	@Override
	public Iterable<IOutputManager> getAllSimulationOutputs() {
		final SimulationPopulation pop = getSimulationPopulation();
		if (pop != null)
			return Iterables.filter(Iterables.concat(Iterables.transform(pop, ISimulationAgent::getOutputManager),
					Collections.singletonList(getOutputManager())), each -> each != null);
		return Collections.emptyList();
	}

	/**
	 * @return
	 */
	@Override
	public boolean isScheduled() { return scheduled; }

	/**
	 * Method closeSimulation()
	 *
	 * @see msi.gama.kernel.experiment.IExperimentAgent#closeSimulation(msi.gama.kernel.simulation.SimulationAgent)
	 */
	@Override
	public void closeSimulation(final ISimulationAgent simulationAgent) {
		simulationAgent.dispose();
	}

	/**
	 * Gets the color.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the color
	 * @date 24 sept. 2023
	 */
	@Override
	public IColor getColor() { return GamaColorFactory.createWithRGBA(30, 30, 30, 255); }

	/**
	 * Method getOutputManager()
	 *
	 * @see msi.gama.kernel.experiment.ITopLevelAgent#getOutputManager()
	 */
	@Override
	public IOutputManager getOutputManager() { return getSpecies().getExperimentOutputs(); }

	/**
	 * Post end action.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executable
	 *            the executable
	 * @date 24 sept. 2023
	 */
	@Override
	public void postEndAction(final IExecutable executable) {
		executer.insertEndAction(executable);

	}

	/**
	 * Post dispose action.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executable
	 *            the executable
	 * @date 24 sept. 2023
	 */
	@Override
	public void postDisposeAction(final IExecutable executable) {
		executer.insertDisposeAction(executable);

	}

	/**
	 * Post one shot action.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executable
	 *            the executable
	 * @date 24 sept. 2023
	 */
	@Override
	public void postOneShotAction(final IExecutable executable) {
		executer.insertOneShotAction(executable);

	}

	/**
	 * Execute action.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param executable
	 *            the executable
	 * @date 24 sept. 2023
	 */
	/**
	 * Executes the action as soon as possible, never while the experiment steps: immediately (in the calling thread) if
	 * the experiment is not stepping, otherwise at the end of its current step. Never waits for the step to finish.
	 */
	@Override
	public void executeAction(final IExecutable executable) {
		// Called from within the step itself: the action is executed at its end, as before
		if (stepLock.isHeldByCurrentThread() || !stepLock.tryLock()) {
			executer.insertOneShotAction(executable);
			return;
		}
		try {
			executer.executeNow(executable);
		} finally {
			stepLock.unlock();
		}
	}

	/**
	 * Steps the experiment, preventing actions from being executed meanwhile (see {@link #executeAction(IExecutable)}).
	 * The actions requested during the step are executed at its end, whatever its outcome.
	 */
	@Override
	public boolean step(final IScope scope) {
		stepLock.lock();
		try {
			return super.step(scope);
		} finally {
			try {
				executer.executeOneShotActions();
			} finally {
				stepLock.unlock();
			}
		}
	}

	@Override
	public boolean canStepBack() {
		if (!isRecord()) return false;
		ISimulationAgent sim = getSimulation();
		if (sim == null) return false;
		return sim.getClock().getCycle() > 0; // see if cycle needs to be taken into account
	}

	@Override
	public void setCurrentSimulation(final ISimulationAgent simulationAgent) {
		SimulationPopulation pop = getSimulationPopulation();
		if (pop != null) { pop.setCurrentSimulation(simulationAgent); }
	}

	@Override
	public boolean isHeadless() { return getSpecies().isHeadless(); }

	/**
	 * Checks if is batch.
	 *
	 * @return true, if is batch
	 */
	public boolean isBatch() { return getSpecies().isBatch(); }

	@Override
	public boolean isRecord() { return recorder != null; }

	/**
	 * Gets the family name.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the family name
	 * @date 24 sept. 2023
	 */
	@Override
	public String getFamilyName() { return IKeyword.EXPERIMENT; }

	/**
	 * Checks if is experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is experiment
	 * @date 10 sept. 2023
	 */
	@Override
	public boolean isExperiment() { return true; }

	@Override
	public IExpression getStopCondition() {
		// Read from the species, which may set it after the agent has been built (gama-server does so when loading).
		final IExpression fromSpecies = getSpecies().getStopCondition();
		return fromSpecies == null ? stopCondition : fromSpecies;
	}

	@Override
	public boolean isGUI() { return true; }

}