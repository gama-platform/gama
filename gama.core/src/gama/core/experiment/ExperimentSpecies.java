/*******************************************************************************************************
 *
 * ExperimentSpecies.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.validation.BatchValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.IParameter;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.agent.IPopulation.Experiment;
import gama.api.kernel.simulation.DefaultExperimentController;
import gama.api.kernel.simulation.HeadlessExperimentController;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.IExperimentController;
import gama.api.kernel.simulation.IExploration;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.GamlSpecies;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.ExecutionScope;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.topology.AmorphousTopology;
import gama.api.ui.IExperimentDisplayable;
import gama.api.ui.IOutputManager;
import gama.api.ui.IStatusMessage;
import gama.api.utils.interfaces.ICategory;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.experiment.batch.exploration.Exploration;
import gama.core.experiment.parameters.ExperimentParameter;
import gama.core.experiment.parameters.TextStatement;
import gama.core.outputs.ExperimentOutputManager;
import gama.core.outputs.SimulationOutputManager;
import gama.core.population.GamaPopulation;
import gama.gaml.statements.LayoutStatement;

/**
 * Written by drogoul Modified on 28 mai 2011 Apr. 2013: Important modifications to enable running true experiment
 * agents
 *
 *
 * Dec 2015: ExperimentPlans now manage their own controller. They are entirely responsible for its life-cycle
 * (creation, disposal)
 *
 * @todo Description
 *
 */

/**
 * The Class ExperimentSpecies.
 */
@symbol (
		name = { IKeyword.EXPERIMENT },
		kind = ISymbolKind.EXPERIMENT,
		with_sequence = true,
		concept = { IConcept.EXPERIMENT })
@doc ("""
		Declaration of a particular type of agent that can manage simulations. If the experiment directly imports a model using the 'model:' facet, this facet *must* be the first one after the name of the experiment. \
		Any experiment attached to a model is a species (introduced by the keyword 'experiment' which directly or indirectly inherits from an abstract species called 'experiment' itself. This abstract species (sub-species of 'agent') defines several attributes and actions that can then be used in any experiment. \
		Experiments also define several attributes, which, in addition to the attributes inherited from agent, form the minimal set of knowledge any experiment will have access to.""")

@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.LABEL,
				optional = false,
				doc = @doc ("identifier of the experiment")),
				@facet (
						name = IKeyword.TITLE,
						type = IType.STRING,
						optional = false,
						doc = @doc (""),
						internal = true),
				@facet (
						name = IKeyword.BENCHMARK,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("If true, make GAMA record the number of invocations and running time of the statements and operators of the simulations launched in this experiment. The results are automatically saved in a csv file in a folder called 'benchmarks' when the experiment is closed"),
						internal = false),
				@facet (
						name = IKeyword.PARENT,
						type = IType.ID,
						optional = true,
						doc = @doc ("the parent experiment (in case of inheritance between experiments)")),
				@facet (
						name = IKeyword.SKILLS,
						type = IType.LIST,
						optional = true,
						doc = @doc ("the skills attached to the experiment"),
						internal = true),
				@facet (
						name = IKeyword.CONTROL,
						type = IType.ID,
						optional = true,
						doc = @doc ("the control architecture used for defining the behavior of the experiment"),
						internal = true),
				@facet (
						name = IKeyword.KEEP_SEED,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Allows to keep the same seed between simulations. Mainly useful for batch experiments")),
				@facet (
						name = IKeyword.KEEP_SIMULATIONS,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("In the case of a batch experiment, specifies whether or not the simulations should be kept in memory for further analysis or immediately discarded with only their fitness kept in memory")),
				@facet (
						name = IKeyword.REPEAT,
						type = IType.INT,
						optional = true,
						doc = @doc ("In the case of a batch experiment, expresses hom many times the simulations must be repeated")),
				@facet (
						name = IKeyword.UNTIL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("In the case of a batch experiment, an expression that will be evaluated to know when a simulation should be terminated")),
				@facet (
						name = IKeyword.PARALLEL,
						type = { IType.BOOL, IType.INT },
						optional = true,
						doc = @doc ("When set to true, use multiple threads to run its simulations. Setting it to n will set the numbers of threads to use")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.LABEL,
						optional = false,
						doc = @doc ("The type of the experiment: `gui`, `batch`, `test`, etc.")),
				@facet (
						name = IKeyword.VIRTUAL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Whether the experiment is virtual (cannot be instantiated, but only used as a parent, false by default)")),
				@facet (
						name = IKeyword.SCHEDULES,
						type = IType.CONTAINER,
						of = IType.AGENT,
						optional = true,
						internal = true,
						doc = @doc ("A container of agents (a species, a dynamic list, or a combination of species and containers) , which represents which agents will be actually scheduled when the population is scheduled for execution. For instance, 'species a schedules: (10 among a)' will result in a population that schedules only 10 of its own agents every cycle. 'species b schedules: []' will prevent the agents of 'b' to be scheduled. Note that the scope of agents covered here can be larger than the population, which allows to build complex scheduling controls; for instance, defining 'global schedules: [] {...} species b schedules: []; species c schedules: b + world; ' allows to simulate a model where the agents of b are scheduled first, followed by the world, without even having to create an instance of c.")),
				@facet (
						name = IKeyword.RECORD,
						type = { IType.BOOL },
						optional = true,
						doc = @doc ("Cannot be used in batch experiments. Whether the simulations run by this experiment are recorded so that they be run backward. Boolean expression expected, which will be evaluated by simulations at each cycle, so that the recording can occur based on specific conditions (for instance 'every(10#cycles)'). A value of 'true' will record each step.")),
				@facet (
						name = IKeyword.AUTORUN,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Whether this experiment should be run automatically when launched (false by default)")) },
		omissible = IKeyword.NAME)
@inside (
		kinds = { ISymbolKind.MODEL })
@validator (BatchValidator.class)
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class ExperimentSpecies extends GamlSpecies implements IExperimentSpecies {

	/** The controller. */
	protected IExperimentController controller;
	// An original copy of the simualtion outputs (which will be eventually
	/** The original simulation outputs. */
	// duplicated in all the simulations)
	protected SimulationOutputManager originalSimulationOutputs;

	/** The experiment outputs. */
	protected ExperimentOutputManager experimentOutputs;

	/** The parameters. */
	// private IItemList parametersEditors;
	protected final Map<String, IParameter> parameters = GamaMapFactory.create();

	/** The texts. */
	// protected final List<TextStatement> texts = GamaListFactory.create();

	/** The explorable parameters. */
	protected final Map<String, IParameter.Batch> explorableParameters = GamaMapFactory.create();

	/** The agent. */
	protected IExperimentAgent agent;

	/** The my scope. */
	protected final Scope myScope = new Scope("in ExperimentSpecies");

	/** The model. */
	protected IModelSpecies model;

	/** The exploration. */
	protected IExploration exploration;

	/** The log. */
	// private FileOutput log;

	/** The is headless. */
	private boolean isHeadless;

	/** The keep seed. */
	private final boolean keepSeed;

	/** The keep simulations. */
	private final boolean keepSimulations;

	/** The experiment type. */
	private final String experimentType;

	/** The autorun. */
	private final boolean autorun;

	/** The benchmarkable. */
	private final boolean benchmarkable;

	/** The should record. */
	private final IExpression shouldRecord;

	/** The sync. */

	/** The displayables. */
	private final List<IExperimentDisplayable> displayables = new ArrayList();

	/** The stop condition. */
	private IExpression stopCondition;

	/**
	 * The Class ExperimentPopulation.
	 */
	public class ExperimentPopulation extends GamaPopulation<IExperimentAgent> implements IPopulation.Experiment {

		/**
		 * Instantiates a new experiment population.
		 *
		 * @param expr
		 *            the expr
		 */
		public ExperimentPopulation(final ISpecies expr) {
			super(null, expr);
		}

		/**
		 * To array.
		 *
		 * @return the population as a new array
		 */
		@SuppressWarnings ("unchecked")
		@Override
		public IExperimentAgent[] toArray() {
			return super.toArray(new IExperimentAgent[0]);
		}

		/**
		 * Creates the agents.
		 *
		 * @param scope
		 *            the scope
		 * @param number
		 *            the number
		 * @param initialValues
		 *            the initial values
		 * @param isRestored
		 *            the is restored
		 * @param toBeScheduled
		 *            the to be scheduled
		 * @param sequence
		 *            the sequence
		 * @return the i list
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		@SuppressWarnings ("null")
		@Override
		public IList<IExperimentAgent> createAgents(final IScope scope, final int number,
				final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
				final boolean toBeScheduled, final IStatement sequence) throws GamaRuntimeException {
			final boolean empty = initialValues == null || initialValues.isEmpty();
			Map<String, Object> inits = Collections.EMPTY_MAP;
			for (int i = 0; i < number; i++) {
				agent = IExperimentDescription.createExperimentAgent(getExperimentType(), this, currentAgentIndex++);
				add(agent);
				scope.push(agent);
				if (!empty) { inits = initialValues.get(i); }
				// List of the names of initialized variables. Those that do not
				// belong to the experiment will be passed
				// to the simulation (see #3198)
				final List<String> names = new ArrayList(inits.keySet());
				for (final IVariable var : orderedVars) {
					String s = var.getName();
					final Object initGet = empty/*
												 * || !allowVarInitToBeOverridenByExternalInit(var)
												 */ ? null : inits.get(s);
					var.initializeWith(scope, agent, initGet);
					names.remove(s);
				}
				// Trick to initialize the simulation variables (and not only
				// the experiment's variables)
				// See discussion in #3198
				for (final String s : names) {
					final Object initGet = empty/*
												 * || !allowVarInitToBeOverridenByExternalInit(var)
												 */ ? null : inits.get(s);
					agent.getScope().setAgentVarValue(agent, s, initGet);
				}
				if (sequence != null && !sequence.isEmpty()) { scope.execute(sequence, agent, null); }
				scope.pop(agent);
			}
			return this;
		}

		@Override
		protected boolean stepAgents(final IScope scope) {
			return scope.step(agent).passed();
		}

		@Override
		public IExperimentAgent getAgent(final IScope scope, final IPoint value) {
			return agent;
		}

		@Override
		public void computeTopology(final IScope scope) throws GamaRuntimeException {
			topology = new AmorphousTopology();
		}

	}

	@Override
	public boolean isHeadless() { return GAMA.isInHeadLessMode() || isHeadless; }

	@Override
	public void setHeadless(final boolean headless) { isHeadless = headless; }

	@Override
	public IExperimentAgent getAgent() { return agent; }

	/**
	 * Instantiates a new experiment plan.
	 *
	 * @param description
	 *            the description
	 */
	public ExperimentSpecies(final IDescription description) {
		super(description);
		setName(description.getName());
		experimentType = description.getLitteral(IKeyword.TYPE);
		// final String type = description.getFacets().getLabel(IKeyword.TYPE);
		if (IKeyword.BATCH.equals(experimentType) || IKeyword.TEST.equals(experimentType)) {
			exploration = new Exploration(null);
		}

		// else if (IKeyword.HEADLESS_UI.equals(experimentType)) {
		// setHeadless(true); }
		final IExpression expr = getFacet(IKeyword.KEEP_SEED);
		if (expr != null && expr.isConst()) {
			keepSeed = Cast.asBool(myScope, expr.value(myScope));
		} else {
			keepSeed = false;
		}
		final IExpression ksExpr = getFacet(IKeyword.KEEP_SIMULATIONS);
		if (ksExpr != null && ksExpr.isConst() && IKeyword.BATCH.equals(experimentType)) {
			keepSimulations = Cast.asBool(myScope, ksExpr.value(myScope));
		} else {
			keepSimulations = true;
		}
		final IExpression ar = getFacet(IKeyword.AUTORUN);
		if (ar == null) {
			autorun = GamaPreferences.Runtime.CORE_AUTO_RUN.getValue();
		} else {
			autorun = Cast.asBool(myScope, ar.value(myScope));
		}
		final IExpression bm = getFacet(IKeyword.BENCHMARK);
		benchmarkable = bm != null && Cast.asBool(myScope, bm.value(myScope));
		shouldRecord = getFacet(IKeyword.RECORD);
		stopCondition = getFacet(IKeyword.UNTIL);
	}

	/**
	 * Gets the stop condition.
	 *
	 * @return the stop condition
	 */
	@Override
	public IExpression getStopCondition() { return stopCondition; }

	@Override
	public void setStopCondition(final IExpression expression) { stopCondition = expression; }

	@Override
	public boolean isAutorun() { return autorun; }

	@Override
	public boolean keepsSeed() {
		return keepSeed;
	}

	@Override
	public boolean keepsSimulations() {
		return keepSimulations;
	}

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		GAMA.getPlatformAgent().restorePrefs();
		// DEBUG.LOG("ExperimentSpecies.dipose BEGIN");
		// Dec 2015 Addition
		if (controller != null) { controller.dispose(); }
		if (agent != null) {
			agent.dispose();
			agent = null;
		}
		if (originalSimulationOutputs != null) {
			originalSimulationOutputs.dispose();
			originalSimulationOutputs = null;
		}
		if (experimentOutputs != null) {
			experimentOutputs.dispose();
			experimentOutputs = null;
		}
		parameters.clear();
		displayables.clear();
		// myScope.getGui().getStatus().neutralStatus("No simulation running");
		GAMA.releaseScope(myScope);
		// FIXME Should be put somewhere around here, but probably not here
		// exactly.
		// ProjectionFactory.reset();

		super.dispose();
		// DEBUG.LOG("ExperimentSpecies.dipose END");
		// Addition 2021

	}

	/**
	 * Creates the agent.
	 *
	 * @param seed
	 *            the seed
	 */
	public void createAgent(final Double seed) {
		final ExperimentPopulation pop = new ExperimentPopulation(this);
		final IScope scope = getExperimentScope();
		pop.initializeFor(scope);
		final List<Map<String, Object>> params =
				seed == null ? Collections.EMPTY_LIST : Collections.singletonList(new HashMap<String, Object>() {
					{
						put(IKeyword.SEED, seed);
					}
				});
		agent = pop.createAgents(scope, 1, params, false, true).get(0);
		addDefaultParameters();
	}

	@Override
	public IModelSpecies getModel() { return model; }

	@Override
	public void setModel(final IModelSpecies model) {
		this.model = model;
		if (!isBatch()) {
			// We look first in the experiment itself
			for (final IVariable v : getVars()) {
				if (v.isParameter()) {
					final ExperimentParameter p = new ExperimentParameter(myScope, v);
					final String parameterName = "(Experiment) " + p.getName();
					final boolean already = parameters.containsKey(parameterName);
					if (!already) {
						parameters.put(parameterName, p);
						displayables.add(p);
					}
				}
			}

			for (final IVariable v : model.getVars()) {
				if (v.isParameter()) {
					final IParameter p = new ExperimentParameter(myScope, v);
					final String parameterName = p.getName();
					final boolean already = parameters.containsKey(parameterName);
					if (!already) {
						parameters.put(parameterName, p);
						displayables.add(p);
					}
				}

			}
		}
	}

	/**
	 * Adds the default parameters.
	 */
	protected void addDefaultParameters() {
		for (final IParameter.Batch p : agent.getDefaultParameters()) { addParameter(p); }
	}

	@Override
	public final IOutputManager getExperimentOutputs() { return experimentOutputs; }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		super.setChildren(children);

		LayoutStatement layout = null;
		for (final ISymbol s : children) {
			switch (s) {
				case null:
					continue;
				case ICategory c:
					displayables.add(c);
					break;
				case TextStatement t:
					displayables.add(t);
					break;
				case LayoutStatement ls:
					layout = ls;
					break;
				case IExploration ie:
					exploration = ie;
					break;
				case SimulationOutputManager som: {
					if (originalSimulationOutputs != null) {
						originalSimulationOutputs.setChildren(som);
					} else {
						originalSimulationOutputs = som;
					}
				}
					break;
				case IParameter.Batch pb: {
					if (isBatch() && pb.canBeExplored()) {
						pb.setEditable(false);
						addExplorableParameter(pb);
						displayables.add(pb);
						continue;
					}
					final String parameterName = pb.getName();
					final boolean already = parameters.containsKey(parameterName);
					if (!already) {
						displayables.add(pb);
						parameters.put(parameterName, pb);
					}
				}
					break;
				case ExperimentOutputManager eom:
					if (experimentOutputs != null) {
						experimentOutputs.setChildren(eom);
					} else {
						experimentOutputs = eom;
					}
					break;
				default:
					continue;
			}

		}
		if (originalSimulationOutputs == null) { originalSimulationOutputs = SimulationOutputManager.createEmpty(); }
		if (experimentOutputs == null) { experimentOutputs = ExperimentOutputManager.createEmpty(); }
		if (experimentOutputs.getLayout() == null) {
			if (layout != null) {
				experimentOutputs.setLayout(layout);
			} else if (originalSimulationOutputs.getLayout() != null) {
				experimentOutputs.setLayout(originalSimulationOutputs.getLayout());
			}
		}
		displayables.addAll(getUserCommands());
	}

	/**
	 * Open.
	 *
	 * @param seed
	 *            the seed
	 */
	@Override
	public synchronized void open(final Double seed) {

		createAgent(seed);

		// We add the agent as soon as possible so as to make it possible to
		// evaluate variables in the opening of the
		// experiment
		// Make sure that the attributes in experiment are initialized (see #3842)
		agent.getParameterValues().forEach((n, v) -> { if (hasVar(n)) { agent.setDirectVarValue(myScope, n, v); } });
		myScope.push(agent);
		prepareGui();
		IScope scope = agent.getScope();
		agent.schedule(scope);

		// showParameters();

		if (isBatch()) {
			myScope.getGui().getStatus().informStatus(" Batch ready. Click run to begin.",
					IStatusMessage.SIMULATION_ICON);
			GAMA.updateExperimentState(this);
		}

	}

	/**
	 * Prepare gui.
	 */
	void prepareGui() {
		if (isTest()) return;
		final ExperimentOutputManager manager = (ExperimentOutputManager) agent.getOutputManager();
		Symbol layout = manager.getLayout() == null ? manager : manager.getLayout();
		final Boolean keepTabs = layout.getFacetValue(myScope, "tabs", true);
		final Boolean keepToolbars = layout.getFacetValue(myScope, "toolbars", null);
		final Boolean showConsoles = layout.getFacetValue(myScope, "consoles", null);
		final Boolean showNavigator = layout.getFacetValue(myScope, "navigator", false);
		final Boolean showControls = layout.getFacetValue(myScope, "controls", null);
		final Boolean showParameters = layout.getFacetValue(myScope, "parameters", null);
		final Boolean keepTray = layout.getFacetValue(myScope, "tray", false);
		final Boolean showEditors = layout.hasFacet("editors") ? layout.getFacetValue(myScope, "editors", false)
				: !GamaPreferences.Modeling.EDITOR_PERSPECTIVE_HIDE.getValue();
		Supplier<IColor> color = () -> layout.getFacetValue(myScope, "background", null);
		myScope.getGui().arrangeExperimentViews(myScope, this, keepTabs, keepToolbars, showConsoles, showParameters,
				showNavigator, showControls, keepTray, color, showEditors);
	}

	@Override
	public synchronized void open() {
		Double seed = null;
		if (isHeadless()) {
			try {
				seed = this.getAgent().getSeed();
			} catch (Exception e) { // Catch no seed
				seed = null;
			}
		}
		open(seed);
	}

	// Horrible workaround for #334
	/** The reloading. */
	// ----------------------------------------------------------------------------------
	private volatile boolean reloading;

	@Override
	public void reload() {
		try {

			reloading = true;
			agent.dispose();
			agent = null;
		} finally {
			reloading = false;
		}
		open();
	}

	@Override
	public boolean isReloading() { return reloading; }
	// ----------------------------------------------------------------------------------

	@Override
	public boolean hasParametersOrUserCommands() {
		return !displayables.isEmpty() || GamaPreferences.Runtime.CORE_MONITOR_PARAMETERS.getValue()
				&& (experimentOutputs.hasMonitors() || originalSimulationOutputs.hasMonitors());
	}

	// @Override
	@Override
	public boolean isBatch() { return exploration != null; }

	@Override
	public boolean isTest() { return IKeyword.TEST.equals(getExperimentType()); }

	@Override
	public boolean isMemorize() { return getDescription().hasFacet(IKeyword.RECORD); }

	@Override
	public IScope getExperimentScope() { return myScope; }

	/**
	 * Sets the parameter value.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param val
	 *            the val
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	// @Override
	@Override
	public void setParameterValue(final IScope scope, final String name, final Object val) throws GamaRuntimeException {
		checkGetParameter(name).setValue(scope, val);
	}

	/**
	 * Sets the parameter value by title.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param val
	 *            the val
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public void setParameterValueByTitle(final IScope scope, final String name, final Object val)
			throws GamaRuntimeException {
		checkGetParameterByTitle(name).setValue(scope, val);
	}

	/**
	 * Gets the parameter value.
	 *
	 * @param parameterName
	 *            the parameter name
	 * @return the parameter value
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	// @Override
	public Object getParameterValue(final String parameterName) throws GamaRuntimeException {
		return checkGetParameter(parameterName).value(myScope);
		// VERIFY THE USAGE OF SCOPE HERE
	}

	@Override
	public boolean hasParameter(final String parameterName) {
		return getParameter(parameterName) != null;
	}

	/**
	 * Gets the parameter by title.
	 *
	 * @param title
	 *            the title
	 * @return the parameter by title
	 */
	@Override
	public IParameter.Batch getParameterByTitle(final String title) {
		for (final IParameter p : parameters.values()) {
			if (p.getTitle().equals(title) && p instanceof IParameter.Batch) return (IParameter.Batch) p;
		}
		return null;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param parameterName
	 *            the parameter name
	 * @return the parameter
	 */
	public IParameter.Batch getParameter(final String parameterName) {
		final IParameter p = parameters.get(parameterName);
		if (p instanceof IParameter.Batch) return (IParameter.Batch) p;
		return null;
	}

	/**
	 * Adds the parameter.
	 *
	 * @param p
	 *            the p
	 */
	public void addParameter(final IParameter p) {
		final String parameterName = p.getName();
		final IParameter already = parameters.get(parameterName);
		if (already != null) { p.setValue(myScope, already.getInitialValue(myScope)); }
		parameters.put(parameterName, p);
		displayables.add(p);
	}

	/**
	 * Check get parameter by title.
	 *
	 * @param parameterName
	 *            the parameter name
	 * @return the i parameter. batch
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected IParameter.Batch checkGetParameterByTitle(final String parameterName) throws GamaRuntimeException {
		final IParameter.Batch v = getParameterByTitle(parameterName);
		if (v == null) throw GamaRuntimeException
				.error("No parameter named " + parameterName + " in experiment " + getName(), getExperimentScope());
		return v;
	}

	/**
	 * Check get parameter.
	 *
	 * @param parameterName
	 *            the parameter name
	 * @return the i parameter. batch
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected IParameter.Batch checkGetParameter(final String parameterName) throws GamaRuntimeException {
		final IParameter.Batch v = getParameter(parameterName);
		if (v == null) throw GamaRuntimeException
				.error("No parameter named " + parameterName + " in experiment " + getName(), getExperimentScope());
		return v;
	}

	@Override
	public Map<String, IParameter> getParameters() { return parameters; }

	// @Override
	// public List<TextStatement> getTexts() { return texts; }

	@Override
	public ISimulationAgent getCurrentSimulation() {
		if (agent == null) return null;
		return agent.getSimulation();
	}

	/**
	 * A short-circuited scope that represents the scope of the experiment plan, before any agent is defined. If a
	 * simulation is available, it refers to it and gains access to its global scope. If not, it throws the appropriate
	 * runtime exceptions when a feature dependent on the existence of a simulation is accessed
	 *
	 * @author Alexis Drogoul
	 * @since November 2011
	 */
	private class Scope extends ExecutionScope {

		/**
		 * Instantiates a new scope.
		 *
		 * @param additionalName
		 *            the additional name
		 */
		public Scope(final String additionalName) {
			super(null, additionalName);
		}

		/**
		 * Sets the global var value.
		 *
		 * @param name
		 *            the name
		 * @param v
		 *            the v
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		@Override
		public void setGlobalVarValue(final String name, final Object v) throws GamaRuntimeException {
			if (hasParameter(name)) { setParameterValue(this, name, v); }
			// final SimulationAgent a = getCurrentSimulation();
			// if (a != null) { a.setDirectVarValue(this, name, v); }
		}

		/**
		 * Gets the global var value.
		 *
		 * @param varName
		 *            the var name
		 * @return the global var value
		 * @throws GamaRuntimeException
		 *             the gama runtime exception
		 */
		@Override
		public Object getGlobalVarValue(final String varName) throws GamaRuntimeException {
			if (hasParameter(varName)) return getParameterValue(varName);
			// final SimulationAgent a = getCurrentSimulation();
			// if (a != null) return a.getDirectVarValue(this, varName);
			return null;
		}

	}

	@Override
	public IExploration getExplorationAlgorithm() { return exploration; }

	/**
	 * Gets the log.
	 *
	 * @return the log
	 */
	// @Override
	// public FileOutput getLog() { return log; }

	/**
	 * Adds the explorable parameter.
	 *
	 * @param p
	 *            the p
	 */
	public void addExplorableParameter(final IParameter.Batch p) {
		p.setCategory(EXPLORABLE_CATEGORY_NAME);
		p.setUnitLabel(null);
		explorableParameters.put(p.getName(), p);
	}

	@Override
	public Map<String, IParameter.Batch> getExplorableParameters() { return explorableParameters; }

	/**
	 * Method getController()
	 *
	 * @see gama.api.kernel.species.IExperimentSpecies#getController()
	 */
	@Override
	public IExperimentController getController() {
		if (controller == null) {
			controller = isHeadless ? new HeadlessExperimentController(this) : new DefaultExperimentController(this);
		}
		return controller;
	}

	/**
	 * Method setController()
	 *
	 * @see gama.api.kernel.species.IExperimentSpecies#setController()
	 */
	@Override
	public void setController(final IExperimentController ec) {
		if (controller != null && controller.equals(ec)) {
			controller.close();
			controller.dispose();
		}
		controller = ec;
	}

	/**
	 * Method refreshAllOutputs()
	 *
	 * @see gama.api.kernel.species.IExperimentSpecies#refreshAllOutputs()
	 */
	@Override
	public void refreshAllOutputs() {
		for (final IOutputManager manager : getActiveOutputManagers()) { manager.forceUpdateOutputs(); }
	}

	@Override
	public void pauseAllOutputs() {
		for (final IOutputManager manager : getActiveOutputManagers()) { manager.pause(); }
	}

	@Override
	public void resumeAllOutputs() {
		for (final IOutputManager manager : getActiveOutputManagers()) { manager.resume(); }
	}

	@Override
	public void closeAllOutputs() {
		for (final IOutputManager manager : getActiveOutputManagers()) { manager.close(); }
	}

	/**
	 * Same as the previous one, but forces the outputs to do one step of computation (if some values have changed)
	 */
	@Override
	public void recomputeAndRefreshAllOutputs() {
		for (final IOutputManager manager : getActiveOutputManagers()) { manager.step(getExperimentScope()); }
	}

	/**
	 * Method getOriginalSimulationOutputs()
	 *
	 * @see gama.api.kernel.species.IExperimentSpecies#getOriginalSimulationOutputs()
	 */
	@Override
	public IOutputManager getOriginalSimulationOutputs() { return originalSimulationOutputs; }

	@Override
	public String getExperimentType() { return experimentType; }

	/**
	 * Returns the output managers that are currently active. If no agent is defined, then an empty iterable is returned
	 *
	 * @return
	 */

	@Override
	public Iterable<IOutputManager> getActiveOutputManagers() {
		if (agent == null) return Collections.EMPTY_LIST;
		return Iterables.concat(agent.getAllSimulationOutputs(), Collections.singletonList(experimentOutputs));

	}

	@Override
	public IExperimentDescription getDescription() { return (IExperimentDescription) super.getDescription(); }

	@Override
	public boolean shouldBeBenchmarked() {
		return benchmarkable;
	}

	@Override
	public List<IExperimentDisplayable> getDisplayables() { return displayables; }

	@Override
	public void setConcurrency(final IExpression exp) { concurrency = exp; }

	/**
	 * Should record.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the i expression
	 * @date 2 sept. 2023
	 */
	@Override
	public IExpression shouldRecord() {
		return shouldRecord;
	}

	/**
	 * Sets the parameter values. If other parameters outside of that list exist, they won't be reinitialized.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param p
	 *            the new parameter values
	 * @date 15 oct. 2023
	 */
	@Override
	public void setParameterValues(final IList p) {
		if (p != null) {
			var scope = getExperimentScope();
			for (var param : p.listValue(null, Types.MAP, false)) {
				@SuppressWarnings ("unchecked") IMap<String, Object> m = (IMap<String, Object>) param;
				String type = m.get(IKeyword.TYPE) != null ? m.get(IKeyword.TYPE).toString() : "";
				Object v = m.get("value");
				if ("int".equals(type)) { v = Integer.valueOf("" + m.get("value")); }
				if ("float".equals(type)) { v = Double.valueOf("" + m.get("value")); }

				final IParameter.Batch b = getParameterByTitle(m.get("name").toString());
				if (b != null) {
					setParameterValueByTitle(scope, m.get("name").toString(), v);
				} else if (getParameter(m.get("name").toString()) != null) {
					setParameterValue(scope, m.get("name").toString(), v);
				}
			}
		}
	}

	@Override
	public void refreshAllParameters() {
		GAMA.getGui().updateParameters(true);
	}

	@Override
	public void setStopCondition(final String cond) {
		IExpression exp = GAML.getExpressionFactory().getFalse();
		if (cond != null && !cond.isBlank()) {
			setStopCondition(GAML.getExpressionFactory().createExpr(cond, getModel().getDescription()));
		}
		if (exp.getGamlType() != Types.BOOL) throw GamaRuntimeException
				.error("The until condition of the experiment should be a boolean", GAMA.getRuntimeScope());
	}

	@Override
	public Experiment createPopulation(final IScope scope) {
		return new ExperimentPopulation(this);
	}

}
