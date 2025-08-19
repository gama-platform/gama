/*******************************************************************************************************
 *
 * SimulationAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.simulation;

import static gama.core.runtime.concurrent.GamaExecutorService.getParallelism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.locationtech.jts.geom.Geometry;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.IStatusMessage;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.util.RandomUtils;
import gama.core.kernel.experiment.ActionExecuter;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.IExperimentController;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.agent.ISerialisedAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.ISerialisedPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.continuous.RootTopology;
import gama.core.metamodel.topology.projection.ProjectionFactory;
import gama.core.metamodel.topology.projection.WorldProjection;
import gama.core.outputs.IOutput;
import gama.core.outputs.IOutputManager;
import gama.core.outputs.SimulationOutputManager;
import gama.core.runtime.ExecutionScope;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.concurrent.GamaExecutorService.Caller;
import gama.core.runtime.concurrent.SimulationLocal;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaDate;
import gama.core.util.GamaMapFactory;
import gama.core.util.IReference;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.operators.spatial.SpatialTransformations;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IExecutable;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.IType;

/**
 * Defines an instance of a model (a simulation). Serves as the support for model species (whose metaclass is
 * GamlModelSpecies) Written by drogoul Modified on 1 d�c. 2010, May 2013
 *
 * @todo Description
 *
 */

/**
 * The Class SimulationAgent.
 */
@species (
		name = IKeyword.MODEL,
		internal = true)
@vars ({ @variable (
		name = IKeyword.COLOR,
		type = IType.COLOR,
		doc = @doc (
				value = "The color used to identify this simulation in the UI",
				comment = "Can be set freely by the modeler")),
		@variable (
				name = IKeyword.SEED,
				type = IType.FLOAT,
				doc = @doc (
						value = "The seed of the random number generator. Each time it is set, the random number generator is reinitialized. WARNING: Setting it to zero actually means that you let GAMA choose a random seed.")),
		@variable (
				name = IKeyword.RNG,
				type = IType.STRING,
				doc = @doc (RandomUtils.DOC)),
		@variable (
				name = IKeyword.EXPERIMENT,
				type = ITypeProvider.EXPERIMENT_TYPE,
				doc = { @doc ("Returns the current experiment agent") }),
		@variable (
				name = IKeyword.WORLD_AGENT_NAME,
				type = ITypeProvider.MODEL_TYPE,
				doc = @doc ("Represents the 'world' of the agents, i.e. the instance of the model in which they are instantiated. Equivalent to 'simulation' in experiments")),
		@variable (
				name = IKeyword.STEP,
				type = IType.FLOAT,
				doc = @doc (
						value = "Represents the value of the interval, in model time, between two simulation cycles",
						comment = "If not set, its value is equal to 1.0 and, since the default time unit is the second, to 1 second")),
		@variable (
				name = SimulationAgent.TIME,
				type = IType.FLOAT,
				doc = @doc (
						value = "Represents the total time passed, in model time, since the beginning of the simulation",
						comment = "Equal to cycle * step if the user does not arbitrarily initialize it.")),
		@variable (
				name = IKeyword.CYCLE,
				type = IType.INT,
				doc = @doc ("Returns the current cycle of the simulation")),
		@variable (
				name = SimulationAgent.USAGE,
				type = IType.INT,
				doc = @doc ("Returns the number of times the random number generator of the simulation has been drawn")),
		@variable (
				name = SimulationAgent.PAUSED,
				type = IType.BOOL,
				doc = @doc ("Returns the current pausing state of the simulation")),
		@variable (
				name = SimulationAgent.DURATION,
				type = IType.STRING,
				doc = @doc ("Returns a string containing the duration, in milliseconds, of the previous simulation cycle")),
		@variable (
				name = SimulationAgent.TOTAL_DURATION,
				type = IType.STRING,
				doc = @doc ("Returns a string containing the total duration, in milliseconds, of the simulation since it has been launched ")),
		@variable (
				name = SimulationAgent.AVERAGE_DURATION,
				type = IType.STRING,
				doc = @doc ("Returns a string containing the average duration, in milliseconds, of a simulation cycle.")),
		@variable (
				name = SimulationAgent.CURRENT_DATE,
				depends_on = SimulationAgent.STARTING_DATE,
				type = IType.DATE,
				doc = @doc (
						value = "Returns the current date in the simulation",
						comment = "The return value is a date; the starting_date has to be initialized to use this attribute, which otherwise indicates a pseudo-date")),
		@variable (
				name = SimulationAgent.STARTING_DATE,
				type = IType.DATE,
				doc = @doc (
						value = "Represents the starting date of the simulation",
						comment = "If no starting_date is provided in the model, GAMA initializes it with a zero date: 1st of January, 1970 at 00:00:00")), })
public class SimulationAgent extends GamlAgent implements ITopLevelAgent {

	static {
		// DEBUG.OFF();
	}

	/** The Constant DURATION. */
	public static final String DURATION = "duration";

	/** The Constant TOTAL_DURATION. */
	public static final String TOTAL_DURATION = "total_duration";

	/** The Constant AVERAGE_DURATION. */
	public static final String AVERAGE_DURATION = "average_duration";

	/** The Constant TIME. */
	public static final String TIME = "time";

	/** The Constant CURRENT_DATE. */
	public static final String CURRENT_DATE = "current_date";

	/** The Constant STARTING_DATE. */
	public static final String STARTING_DATE = "starting_date";

	/** The Constant PAUSED. */
	public static final String PAUSED = "paused";

	/** The Constant USAGE. */
	public static final String USAGE = "rng_usage";

	/** The own clock. */
	final SimulationClock ownClock;

	/** The color. */
	GamaColor color;

	/** The own scope. */
	final IScope ownScope = new ExecutionScope(this);

	/** The outputs. */
	private SimulationOutputManager outputs;

	/** The projection factory. */
	final ProjectionFactory projectionFactory;

	/** The scheduled. */
	private Boolean scheduled = false;

	/** The is on user hold. */
	private volatile boolean isOnUserHold;

	/** The random. */
	private RandomUtils random;

	/** The executer. */
	private final ActionExecuter executer;

	/** The topology. */
	private RootTopology topology;

	/** The Simulation local map. */
	private Map simulationLocalMap;

	/**
	 * The external inits and parameters. Holds a memory of the values provided from outside, initially and during
	 * simulation
	 */
	private Map<String, Object> externalInitsAndParameters;

	/**
	 * Instantiates a new simulation agent.
	 *
	 * @param pop
	 *            the pop
	 * @param index
	 *            the index
	 */
	public SimulationAgent(final IPopulation<? extends IAgent> pop, final int index) {
		this((SimulationPopulation) pop, index);
	}

	/**
	 * Sets the external inits.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param inits
	 *            the inits
	 * @date 13 août 2023
	 */
	public void setExternalInits(final Map<String, Object> inits) {
		if (externalInitsAndParameters == null) {
			if (inits == null) {
				externalInitsAndParameters = new HashMap<>();
			} else {
				externalInitsAndParameters = new HashMap<>(inits);
			}
		} else if (inits != null) { externalInitsAndParameters.putAll(inits); }
	}

	/**
	 * Gets the external inits.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the external inits
	 * @date 13 août 2023
	 */
	public Map<String, Object> getExternalInits() {
		if (externalInitsAndParameters == null) return Collections.EMPTY_MAP;
		return externalInitsAndParameters;
	}

	/**
	 * Instantiates a new simulation agent.
	 *
	 * @param pop
	 *            the pop
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public SimulationAgent(final SimulationPopulation pop, final int index) throws GamaRuntimeException {
		super(pop, index);
		ownClock = new SimulationClock(getScope());
		executer = new ActionExecuter(getScope());
		projectionFactory = new ProjectionFactory();
		// Random explicitely created with the seed of the experiment
		random = new RandomUtils(pop.getHost().getSeed(), pop.getHost().getRng());
	}

	/**
	 * Gets the scheduled.
	 *
	 * @return the scheduled
	 */
	public Boolean getScheduled() { return scheduled; }

	@Override
	@getter (IKeyword.EXPERIMENT)
	public IExperimentAgent getExperiment() {
		final IMacroAgent agent = getHost();
		if (agent instanceof IExperimentAgent) return (IExperimentAgent) agent;
		return null;
	}

	@Override
	@getter (IKeyword.WORLD_AGENT_NAME)
	public SimulationAgent getSimulation() { return this; }

	/**
	 * Sets the topology.
	 *
	 * @param topology2
	 *            the new topology
	 */
	public void setTopology(final RootTopology topology2) {
		if (topology != null) { topology.dispose(); }
		topology = topology2;

	}

	/**
	 * Sets the topology.
	 *
	 * @param scope
	 *            the scope
	 * @param shape
	 *            the shape
	 */
	public void setTopology(final IScope scope, final IShape shape) {
		// A topology has already been computed. We update it and updates all
		// the agents present in the spatial index
		final boolean[] parallel = { GamaExecutorService.CONCURRENCY_SPECIES.getValue() };

		if (!parallel[0]) {
			getSpecies().getDescription().visitMicroSpecies(s -> {
				parallel[0] = getParallelism(scope, s.getFacetExpr(IKeyword.PARALLEL), Caller.SPECIES) > 0;
				return !parallel[0];
			});
		}

		if (topology != null) {
			topology.updateEnvironment(scope, shape, parallel[0]);
		} else {
			final IExpression expr = getSpecies().getFacet(IKeyword.TORUS);
			final boolean torus = expr != null && Cast.asBool(scope, expr.value(scope));
			setTopology(new RootTopology(scope, shape, torus, parallel[0]));
		}
	}

	@Override
	public void setName(final String name) {
		super.setName(name);
		final SimulationOutputManager m = getOutputManager();
		if (m != null) { m.updateDisplayOutputsName(this); }
	}

	/**
	 * Sets the scheduled.
	 *
	 * @param scheduled
	 *            the new scheduled
	 */
	public void setScheduled(final Boolean scheduled) { this.scheduled = scheduled; }

	@Override
	@getter (
			value = IKeyword.COLOR,
			initializer = true)
	public GamaColor getColor() {
		if (color == null) { color = GamaPreferences.Interface.getColorForSimulation(getIndex()); }
		return color;
	}

	@Override
	public RootTopology getTopology() { return topology; }

	/**
	 * Sets the color.
	 *
	 * @param color
	 *            the new color
	 */
	@setter (IKeyword.COLOR)
	public void setColor(final GamaColor color) { this.color = color; }

	@Override
	public void schedule(final IScope scope) {
		super.schedule(this.getScope());
	}

	@Override
	protected boolean preStep(final IScope scope) {
		ownClock.beginCycle();
		executer.executeBeginActions();
		return super.preStep(scope);
	}

	@Override
	protected void postStep(final IScope scope) {
		super.postStep(scope);
		executer.executeEndActions();
		executer.executeOneShotActions();
		if (outputs != null) { outputs.step(this.getScope()); }
		ownClock.step();
		GAMA.getBufferingController().flushSaveFilesInCycle(this);
		GAMA.getBufferingController().flushWriteInCycle(this);
	}

	@Override
	public Object _init_(final IScope scope) {
		super._init_(this.getScope());
		initOutputs();
		return this;
	}

	/**
	 * SimulationScope related utilities
	 *
	 */

	@Override
	public IScope getScope() { return ownScope; }

	/**
	 * Gets the projection factory.
	 *
	 * @return the projection factory
	 */
	public ProjectionFactory getProjectionFactory() { return projectionFactory; }

	@Override
	public SimulationClock getClock() { return ownClock; }

	@Override
	public void dispose() {
		GAMA.getGui().getStatus().beginTask("Disposing " + this, IStatusMessage.SIMULATION_ICON);
		if (dead) return;
		executer.executeDisposeActions();
		// hqnghi if simulation comes from an external population, dispose this population first
		// and then its outputs

		if (externMicroPopulations != null) { externMicroPopulations.clear(); }
		GAMA.getGui().getStatus().beginTask("Disposing " + this, IStatusMessage.SIMULATION_ICON);
		if (outputs != null) {
			outputs.dispose();
			outputs = null;
		}
		GAMA.getGui().getStatus().beginTask("Disposing " + this, IStatusMessage.SIMULATION_ICON);
		if (topology != null) {
			if (!isMicroSimulation()) {
				topology.dispose();
				topology = null;
			} else {
				for (final IPopulation<? extends IAgent> pop : getMicroPopulations()) { topology.remove(pop); }
			}
		}
		if (externalInitsAndParameters != null) { externalInitsAndParameters.clear(); }

		// we make sure that all pending write operations are flushed
		GAMA.getBufferingController().flushSaveFilesOfAgent(this);
		GAMA.getBufferingController().flushWriteOfAgent(this);
		GAMA.releaseScope(getScope());
		// scope = null;
		GAMA.getGui().getStatus().beginTask("Disposing " + this, IStatusMessage.SIMULATION_ICON);
		super.dispose();

	}

	/**
	 * Checks if is micro simulation.
	 *
	 * @return true, if is micro simulation
	 */
	public boolean isMicroSimulation() { return getSpecies().getDescription().belongsToAMicroModel(); }

	@Override
	public GamaPoint setLocation(final IScope scope, final GamaPoint p) {
		return p;
	}

	@Override
	public GamaPoint getLocation(final IScope scope) {
		if (geometry == null || geometry.getInnerGeometry() == null) return new GamaPoint(0, 0);
		return super.getLocation(scope);
	}

	@Override
	public void setGeometry(final IScope scope, final IShape g) {
		// FIXME : AD 5/15 Revert the commit by PT:
		// getProjectionFactory().setWorldProjectionEnv(geom.getEnvelope());
		// We systematically translate the geometry to {0,0}
		IShape geom = g;
		if (geom == null) {
			geom = GamaGeometryType.buildBox(100, 100, 100, new GamaPoint(50, 50, 50));
		} else {
			// See Issue #2787, #2795
			final Geometry gg = geom.getInnerGeometry();
			Object savedData = null;
			if (gg != null) { savedData = gg.getUserData(); }
			geom.setInnerGeometry(geom.getEnvelope().toGeometry());
			geom.getInnerGeometry().setUserData(savedData);
		}

		final Envelope3D env = geom.getEnvelope();
		if (getProjectionFactory().getWorld() == null) { projectionFactory.setWorldProjectionEnv(scope, env); }

		((WorldProjection) getProjectionFactory().getWorld()).updateTranslations(env);
		((WorldProjection) getProjectionFactory().getWorld()).updateUnit(getProjectionFactory().getUnitConverter());
		final GamaPoint p = new GamaPoint(-env.getMinX(), -env.getMinY(), -env.getMinZ());
		geometry.setGeometry(SpatialTransformations.translated_by(scope, geom, p));
		if (getProjectionFactory().getUnitConverter() != null) {
			((WorldProjection) getProjectionFactory().getWorld()).convertUnit(geometry.getInnerGeometry());

		}
		setTopology(scope, geometry);

	}

	@Override
	public SimulationPopulation getPopulation() { return (SimulationPopulation) population; }

	@Override
	public IPopulation<? extends IAgent> getPopulationFor(final String speciesName) throws GamaRuntimeException {
		IPopulation<? extends IAgent> pop = super.getPopulationFor(speciesName);
		if (pop != null) return pop;
		final ISpecies microSpec = getSpecies().getMicroSpecies(speciesName);
		if (microSpec == null) return null;
		IScope scope = getScope();
		pop = scope.getPopulationFactory().createPopulation(scope, this, microSpec);
		setAttribute(speciesName, pop);
		pop.initializeFor(scope);
		return pop;
	}

	/**
	 * Gets the cycle.
	 *
	 * @param scope
	 *            the scope
	 * @return the cycle
	 */
	@getter (IKeyword.CYCLE)
	public Integer getCycle(final IScope scope) {
		final SimulationClock clock = getClock();
		if (clock != null) return clock.getCycle();
		return 0;
	}

	/**
	 * Checks if is paused.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if is paused
	 */
	@getter (PAUSED)
	public boolean isPaused(final IScope scope) {
		// The second case is mostly useless for the moment as it corresponds
		// to the global pause of the experiment...
		return getScope().isPaused();
	}

	/**
	 * Sets the paused.
	 *
	 * @param scope
	 *            the scope
	 * @param state
	 *            the state
	 */
	@setter (PAUSED)
	public void setPaused(final IScope scope, final boolean state) {
		// Not used for the moment, but it might allow to set this state
		// explicitly (ie pause a simulation without pausing the experiment)
		// For that, however, we need to check the condition in the step of the
		// simulation and maybe skip it (or put the thread on sleep ?) when its
		// scope is on user hold... The question of what to do with the
		// experiment
		// is also important: should the experiment continue stepping while its
		// simulations are paused ?
		// getScope().setOnUserHold(state);
	}

	@Override
	public boolean isOnUserHold() { return isOnUserHold; }

	@Override
	public void setOnUserHold(final boolean state) { isOnUserHold = state; }

	/**
	 * Gets the time step.
	 *
	 * @param scope
	 *            the scope
	 * @return the time step
	 */
	@getter (
			value = IKeyword.STEP,
			initializer = true)
	public double getTimeStep(final IScope scope) {
		final SimulationClock clock = getClock();
		if (clock != null) return clock.getStepInSeconds();
		return 1d;
	}

	/**
	 * Sets the time step.
	 *
	 * @param scope
	 *            the scope
	 * @param t
	 *            the t
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@setter (IKeyword.STEP)
	public void setTimeStep(final IScope scope, final double t) throws GamaRuntimeException {
		final SimulationClock clock = getClock();
		if (clock != null) {
			clock.setStep(t);

		}
	}

	/**
	 * Gets the time.
	 *
	 * @param scope
	 *            the scope
	 * @return the time
	 */
	@getter (TIME)
	public double getTime(final IScope scope) {
		final SimulationClock clock = getClock();
		if (clock != null) return clock.getTimeElapsedInSeconds();
		return 0d;
	}

	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	@getter (DURATION)
	public String getDuration() { return Long.toString(getClock().getDuration()); }

	/**
	 * Gets the total duration.
	 *
	 * @return the total duration
	 */
	@getter (TOTAL_DURATION)
	public String getTotalDuration() { return Long.toString(getClock().getTotalDuration()); }

	/**
	 * Gets the average duration.
	 *
	 * @return the average duration
	 */
	@getter (AVERAGE_DURATION)
	public String getAverageDuration() { return Double.toString(getClock().getAverageDuration()); }

	/**
	 * Sets the current date.
	 *
	 * @param d
	 *            the new current date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@setter (CURRENT_DATE)
	public void setCurrentDate(final GamaDate d) throws GamaRuntimeException {
		// NOTHING
	}

	/**
	 * Gets the current date.
	 *
	 * @return the current date
	 */
	@getter (CURRENT_DATE)
	public GamaDate getCurrentDate() { return ownClock.getCurrentDate(); }

	/**
	 * Sets the starting date.
	 *
	 * @param d
	 *            the new starting date
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@setter (STARTING_DATE)
	public void setStartingDate(final GamaDate d) throws GamaRuntimeException {
		ownClock.setStartingDate(d);
	}

	/**
	 * Gets the starting date.
	 *
	 * @return the starting date
	 */
	@getter (
			value = STARTING_DATE,
			initializer = true)
	public GamaDate getStartingDate() { return ownClock.getStartingDate(); }

	/**
	 * Pause.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@action (
			name = "pause",
			doc = @doc ("Allows to pause the current simulation **ACTUALLY EXPERIMENT FOR THE MOMENT**. It can be resumed with the manual intervention of the user or the 'resume' action."))

	public Object pause(final IScope scope) {
		// if (!GAMA.isPaused()) { GAMA.pauseFrontmostExperiment(); }
		final IExperimentController controller = scope.getExperiment().getSpecies().getController();
		if (controller != null && !controller.isPaused()) {
			controller.processPause(!GAMA.getGui().isInDisplayThread());
		}
		return null;
	}

	/**
	 * Resume.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@action (
			name = "resume",
			doc = @doc ("Allows to resume the current simulation **ACTUALLY EXPERIMENT FOR THE MOMENT**. It can then be paused with the manual intervention of the user or the 'pause' action."))

	public Object resume(final IScope scope) {
		// if (GAMA.isPaused()) { GAMA.resumeFrontmostExperiment(); }
		final IExperimentController controller = scope.getExperiment().getSpecies().getController();
		if (controller != null && controller.isPaused()) { controller.processStart(false); }
		return null;
	}

	/**
	 * Gets the short user friendly name.
	 *
	 * @return the short user friendly name
	 */
	public String getShortUserFriendlyName() {

		return getName();

	}

	/**
	 * Builds the postfix.
	 *
	 * @return the string
	 */
	public String buildPostfix() {
		final boolean noName = !GamaPreferences.Interface.CORE_SIMULATION_NAME.getValue();
		if (!noName) return " (" + getName() + ")";
		if (getPopulation().size() > 1) return " (S" + getIndex() + ")";
		return "";

	}

	/**
	 * Sets the outputs.
	 *
	 * @param iOutputManager
	 *            the new outputs
	 */
	@SuppressWarnings ("unchecked")
	public void setOutputs(final IOutputManager iOutputManager) {
		if (iOutputManager == null) return;
		// AD : condition removed for Issue #3748
		// hqnghi push outputManager down to Simulation level
		// create a copy of outputs from description
		// if ( /* !scheduled && */ !getExperiment().getSpecies().isBatch()) {
		final IDescription des = ((ISymbol) iOutputManager).getDescription();
		if (des == null) return;
		outputs = (SimulationOutputManager) des.compile();
		final Map<String, IOutput> mm = GamaMapFactory.create();
		outputs.forEach((oName, output) -> {
			String keyName, newOutputName;
			if (!scheduled) {
				keyName = output.getName() + "#" + this.getSpecies().getDescription().getModelDescription().getAlias()
						+ "#" + this.getExperiment().getSpecies().getName() + "#" + this.getExperiment().getIndex();
				newOutputName = keyName;
			} else {
				final String postfix = buildPostfix();
				keyName = oName + postfix;
				newOutputName = output.getName() + postfix;
			}
			mm.put(keyName, output);
			output.setName(newOutputName);
		});
		outputs.clear();
		outputs.putAll(mm);

		// AD : reverted for Issue #3748
		// } else {
		// outputs = (SimulationOutputManager) iOutputManager;
		// }
		// end-hqnghi
	}

	@Override
	public SimulationOutputManager getOutputManager() { return outputs; }

	/**
	 * @param inspectDisplayOutput
	 */
	public void addOutput(final IOutput output) {
		outputs.add(output);
	}

	/**
	 * Gets the usage.
	 *
	 * @return the usage
	 */
	@getter (
			value = SimulationAgent.USAGE,
			initializer = false)
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
	@setter (SimulationAgent.USAGE)
	public void setUsage(final Integer s) {
		Integer usage = s;
		if (s == null) { usage = 0; }
		getRandomGenerator().setUsage(usage);
	}

	/**
	 * Gets the seed.
	 *
	 * @return the seed
	 */
	@getter (
			value = IKeyword.SEED,
			initializer = true)
	public Double getSeed() {
		final Double seed = random.getSeed();
		// DEBUG.LOG("simulation agent get seed: " + seed);
		return seed == null ? 0d : seed;
	}

	/**
	 * Sets the seed.
	 *
	 * @param s
	 *            the new seed
	 */
	@setter (IKeyword.SEED)
	public void setSeed(final Double s) {
		// DEBUG.LOG("simulation agent set seed: " + s);
		Double seed;
		if (s == null || s.doubleValue() == 0d) {
			seed = null;
		} else {
			seed = s;
		}
		getRandomGenerator().setSeed(seed, true);
	}

	/**
	 * Gets the rng.
	 *
	 * @return the rng
	 */
	@getter (
			value = IKeyword.RNG,
			initializer = true)
	public String getRng() { return getRandomGenerator().getRngName(); }

	/**
	 * Sets the rng.
	 *
	 * @param newRng
	 *            the new rng
	 */
	@setter (IKeyword.RNG)
	public void setRng(final String newRng) {

		// rng = newRng;
		// scope.getGui().debug("ExperimentAgent.setRng" + newRng);
		getRandomGenerator().setGenerator(newRng, true);
	}

	// @Override
	@Override
	public RandomUtils getRandomGenerator() { return random; }

	/**
	 * Sets the random generator.
	 *
	 * @param rng
	 *            the new random generator
	 */
	public void setRandomGenerator(final RandomUtils rng) { random = rng; }

	/**
	 * Prepare gui for simulation.
	 *
	 * @param s
	 *            the s
	 */
	// public void prepareGuiForSimulation(final IScope s) {
	// s.getGui().clearErrors(s);
	// }

	/**
	 * Inits the outputs.
	 */
	public void initOutputs() {
		if (outputs != null) { outputs.init(this.getScope()); }
	}

	@Override
	public void postEndAction(final IExecutable executable) {
		executer.insertEndAction(executable);

	}

	@Override
	public void postDisposeAction(final IExecutable executable) {
		executer.insertDisposeAction(executable);

	}

	@Override
	public void postOneShotAction(final IExecutable executable) {
		executer.insertOneShotAction(executable);

	}

	@Override
	public void executeAction(final IExecutable executable) {
		executer.executeOneAction(executable);

	}

	@SuppressWarnings ("unchecked")
	@Override
	public void updateWith(final IScope scope, final ISerialisedAgent sa) {

		Double seedValue = null;
		String rngValue = null;
		Integer usageValue = null;

		// This list is updated during all the updateWith of the simulation.
		// When all the agents will be created (end of this updateWith),
		// all the references will be replaced by the corresponding agent.
		final List<IReference> list_ref = new ArrayList<>();

		// Update Attribute
		final Map<String, Object> attr = sa.attributes();
		for (final String varName : attr.keySet()) {
			final Object attrValue = attr.get(varName);

			final boolean isReference = IReference.isReference(attrValue);
			final boolean isPopulation = attrValue instanceof ISerialisedPopulation;
			if (isPopulation) { continue; }

			// if( attrValue instanceof ReferenceAgent) {
			if (isReference) {
				((IReference) attrValue).setAgentAndAttrName(this, varName);
				if (!list_ref.contains(attrValue)) { list_ref.add((IReference) attrValue); }
			}

			// If attributes are related to the RNG, we keep them to initialise the RNG later, in the proper order.
			switch (varName) {
				case IKeyword.SEED:
					seedValue = (Double) attrValue;
					break;
				case IKeyword.RNG:
					rngValue = (String) attrValue;
					break;
				case SimulationAgent.USAGE:
					usageValue = (Integer) attrValue;
					break;
				default:
					this.setDirectVarValue(scope, varName, attrValue);
					break;
			}

		}

		// Update RNG
		setRandomGenerator(new RandomUtils(seedValue, rngValue));
		setUsage(usageValue);

		// Update Clock
		final Object cycle = sa.getAttributeValue(IKeyword.CYCLE);
		ownClock.setCycleNoCheck((Integer) cycle);

		final Map<String, ISerialisedPopulation> savedAgentInnerPop = sa.innerPopulations();

		if (savedAgentInnerPop != null) {
			for (final String savedAgentMicroPopName : savedAgentInnerPop.keySet()) {
				final IPopulation<? extends IAgent> simuMicroPop = getPopulationFor(savedAgentMicroPopName);

				if (simuMicroPop != null) {
					// Build a map name::innerPopAgentSavedAgt :
					// For each agent from the simulation innerPop, it will be
					// updated from the corresponding agent
					final Map<String, ISerialisedAgent> mapSavedAgtName = GamaMapFactory.createUnordered();
					for (final ISerialisedAgent localSA : savedAgentInnerPop.get(savedAgentMicroPopName).agents()) {
						mapSavedAgtName.put((String) localSA.getAttributeValue("name"), localSA);
					}

					final Map<String, IAgent> mapSimuAgtName = GamaMapFactory.createUnordered();

					for (final IAgent agt : simuMicroPop.toArray()) { mapSimuAgtName.put(agt.getName(), agt); }

					for (final Entry<String, ISerialisedAgent> e : mapSavedAgtName.entrySet()) {
						final IAgent agt = mapSimuAgtName.get(e.getKey());
						if (agt != null) { // the savedAgent is in the
											// simulation, update it, and remove
											// it from the map mapSimuAgtName
							// TODO : implement it for GamlAgent...
							agt.updateWith(scope, e.getValue());
							mapSimuAgtName.remove(e.getKey());
						} else { // the SavedAgent is not in the Simulation,
									// then create it

							simuMicroPop.createAgentAt(scope, e.getValue().getIndex(), e.getValue().attributes(), true,
									true);
						}

						// Find the agt and all the references
						final IAgent currentAgent = agt == null ? simuMicroPop.getAgent(e.getValue().getIndex()) : agt;

						for (final String name : e.getValue().attributes().keySet()) {
							final Object attrValue = e.getValue().getAttributeValue(name);
							final boolean isReference2 = IReference.isReference(attrValue);

							if (isReference2) {
								// if( attrValue instanceof ReferenceAgent) {
								((IReference) attrValue).setAgentAndAttrName(currentAgent, name);
								if (!list_ref.contains(attrValue)) { list_ref.add((IReference) attrValue); }
							}
						}
					}

					// For all remaining agents in the mapSimuAgtName, kill them
					for (final IAgent remainingAgent : mapSimuAgtName.values()) {
						// Kill them all
						remainingAgent.dispose();
					}
				}
			}
		}

		// Update all the references !
		updateReferences(scope, list_ref);
	}

	/**
	 * Update references.
	 *
	 * @param scope
	 *            the scope
	 * @param list_ref
	 *            the list ref
	 * @param sim
	 *            the sim
	 */
	private void updateReferences(final IScope scope, final List<IReference> list_ref) {
		for (final IReference ref : list_ref) { ref.resolveReferences(scope, this); }
	}

	/**
	 * Adopt topology of.
	 *
	 * @param root
	 *            the root
	 */
	public void adoptTopologyOf(final SimulationAgent root) {
		final RootTopology rt = root.getTopology();
		rt.mergeWith(topology);
		setTopology(rt);
		for (final IPopulation<?> p : getMicroPopulations()) { p.getTopology().setRoot(root.getScope(), rt); }
	}

	/**
	 * Gets the Simulation local map.
	 *
	 * @return the Simulation local map
	 */
	@SuppressWarnings ("unchecked")
	public <T> Map<SimulationLocal<T>, T> getSimulationLocalMap() { return simulationLocalMap; }

	/**
	 * Sets the Simulation local map.
	 *
	 * @param map
	 *            the new Simulation local map
	 */
	public <T> void setSimulationLocalMap(final Map<SimulationLocal<T>, T> map) { simulationLocalMap = map; }

	@Override
	public String getFamilyName() { return IKeyword.SIMULATION; }

	@Override
	public boolean isSimulation() { return true; }

}
