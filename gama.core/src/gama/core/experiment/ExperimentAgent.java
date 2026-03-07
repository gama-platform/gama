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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
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
import gama.api.kernel.simulation.IClock;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentRecorder;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.ExecutionScope;
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
		doc = @doc ("The species of agents that represent experiments"))
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

	/** The recorder. */
	protected IExperimentRecorder recorder;

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

	/** The scheduled. */
	// protected SimulationPopulation populationOfSimulations;
	private Boolean scheduled = false;

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
		if (getSpecies().isMemorize()) { recorder = GAMA.getExperimentRecorder(); }
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
	public Object _init_(final IScope scope) {
		if (scope.interrupted()) return null;
		if (automaticallyCreateFirstSimulation()) { createSimulation(ParametersSet.EMPTY, scheduled); }
		// We execute any behavior defined in GAML.
		super._init_(scope);
		tryToRecordSimulations();
		return this;
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
		executer.executeBeginActions();
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
					IScope scope = sim.getScope();
					if (!scope.interrupted() && Cast.asBool(scope, shouldRecord.value(scope))) { recorder.record(sim); }
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
	public void schedule(final IScope scope) {
		scheduled = true;
		// The experiment agent is scheduled in the global scheduler
		getSpecies().getController().schedule(this);
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
		microPopulations = new IPopulation[] { pop };
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
		p = new ExperimentParameter(getScope(), getSpecies().getVar(IKeyword.SEED), "Default random seed", cat,
				"(current seed)", null, true) {

			@Override
			protected Object getValue(final IScope scope) {
				return getSeed();
			}
		};
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

	public String getProjectPath() { return getModel().getProjectPath() + "/"; }

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
	 * Update displays.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@Override
	@action (
			name = "update_outputs",
			doc = { @doc ("Forces all outputs to refresh, optionally recomputing their values") },
			args = { @arg (
					name = "recompute",
					type = IType.BOOL,
					doc = { @doc ("Whether or not to force the outputs to make a computation step") }) })

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
			doc = { @doc ("Forces the parameters view, in GUI, to refresh, picking up any updated value of the parameters") })

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
			doc = { @doc ("Forces a 'garbage collect' of the unused objects in GAMA") })
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
		if (main.getMicroModel(micro.getAlias()) == null) return sim.getPopulationFor(species.getName());
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
		final boolean result = true;
		try {
			GAMA.runAndUpdateAll(() -> {
				for (ISimulationAgent sim : getSimulationPopulation()) {
					if (recorder.canStepBack(sim)) {
						recorder.restore(sim);
						if (!((ExperimentSpecies) this.getSpecies()).keepsSeed()) {
							sim.setRandomGenerator(
									new RandomUtils(random.next(), sim.getRandomGenerator().getRngName()));
						}
					}

				}
			});
		} finally {
			informStatus();
			GAMA.updateExperimentState(getSpecies());
		}
		return result;
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
		public void setAgentVarValue(final IAgent a, final String name, final Object value) {
			if (a == ExperimentAgent.this) {
				setGlobalVarValue(name, value);
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
		public Object getAgentVarValue(final IAgent a, final String varName) {
			if (a == ExperimentAgent.this) return getGlobalVarValue(varName);
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
	@Override
	public void executeAction(final IExecutable executable) {
		executer.executeOneAction(executable);

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
	public IExpression getStopCondition() { return stopCondition; }

}