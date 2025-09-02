/*******************************************************************************************************
 *
 * GamlSpecies.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.species;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ISkill;
import gama.core.kernel.model.GamlModelSpecies;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.core.util.graph.AbstractGraphNodeAgent;
import gama.core.util.matrix.IMatrix;
import gama.gaml.architecture.IArchitecture;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.descriptions.VariableDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.species.GamlSpecies.SpeciesValidator;
import gama.gaml.statements.ActionStatement;
import gama.gaml.statements.AspectStatement;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.IStatement;
import gama.gaml.statements.UserCommandStatement;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import one.util.streamex.StreamEx;

/**
 * The Class GamlSpecies. A species specified by GAML attributes
 *
 * @author drogoul
 */
@symbol (
		name = { IKeyword.SPECIES, IKeyword.GLOBAL, IKeyword.GRID },
		kind = ISymbolKind.SPECIES,
		with_sequence = true,
		concept = { IConcept.SPECIES })
@inside (
		kinds = { ISymbolKind.MODEL, ISymbolKind.ENVIRONMENT, ISymbolKind.SPECIES })
@facets (
		value = { @facet (
				name = IKeyword.PARALLEL,
				type = { IType.BOOL, IType.INT },
				optional = true,
				doc = @doc ("(experimental) setting this facet to 'true' will allow this species to use concurrency when scheduling its agents; setting it to an integer will set the threshold under which they will be run sequentially (the default is initially 20, but can be fixed in the preferences). This facet has a default set in the preferences (Under Performances > Concurrency)")),
				@facet (
						name = IKeyword.WIDTH,
						type = IType.INT,
						optional = true,
						doc = @doc ("(grid only), the width of the grid (in terms of agent number)")),
				@facet (
						name = IKeyword.HEIGHT,
						type = IType.INT,
						optional = true,
						doc = @doc ("(grid only),  the height of the grid (in terms of agent number)")),
				@facet (
						name = IKeyword.CELL_WIDTH,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("(grid only), the width of the cells of the grid")),
				@facet (
						name = IKeyword.CELL_HEIGHT,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("(grid only), the height of the cells of the grid")),
				@facet (
						name = IKeyword.NEIGHBORS,
						type = IType.INT,
						optional = true,
						doc = @doc ("(grid only), the chosen neighborhood (4, 6 or 8)")),
				@facet (
						name = "horizontal_orientation",
						type = IType.BOOL,
						optional = true,
						doc = { @doc (
								value = "(hexagonal grid only),(true by default). Allows use a hexagonal grid with a horizontal or vertical orientation. ") }),
				@facet (
						name = "use_individual_shapes",
						type = IType.BOOL,
						optional = true,
						doc = { @doc (
								value = "(grid only),(true by default). Allows to specify whether or not the agents of the grid will have distinct geometries. If set to false, they will all have simpler proxy geometries",
								see = "use_regular_agents",
								comment = "This facet, when set to true, allows to save memory by generating only one reference geometry and proxy geometries for the agents") }),
				@facet (
						name = "use_regular_agents",
						type = IType.BOOL,
						optional = true,
						doc = { @doc (
								value = "(grid only),(true by default). Allows to specify if the agents of the grid are regular agents (like those of any other species) or minimal ones (which can't have sub-populations, can't inherit from a regular species, etc.)") }),
				@facet (
						name = "optimizer",
						type = IType.STRING,
						optional = true,
						doc = { @doc (
								value = "(grid only),(\"A*\" by default). Allows to specify the algorithm for the shortest path computation (\"BF\", \"Dijkstra\", \"A*\" or \"JPS*\"") }),
				@facet (
						name = "use_neighbors_cache",
						type = IType.BOOL,
						optional = true,
						doc = { @doc (
								value = "(grid only),(true by default). Allows to turn on or off the use of the neighbors cache used for grids. Note that if a diffusion of variable occurs, GAMA will emit a warning and automatically switch to a caching version") }),
				@facet (
						name = IKeyword.FILE,
						type = IType.FILE,
						optional = true,
						doc = @doc ("(grid only), a bitmap file that will be loaded at runtime so that the value of each pixel can be assigned to the attribute 'grid_value'")),
				@facet (
						name = IKeyword.FILES,
						type = IType.LIST,
						of = IType.FILE,
						optional = true,
						doc = @doc ("(grid only), a list of bitmap file that will be loaded at runtime so that the value of each pixel of each file can be assigned to the attribute 'bands'")),
				@facet (
						name = IKeyword.TORUS,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("is the topology toric (defaut: false). Needs to be defined on the global species.")),
				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = false,
						doc = @doc ("the identifier of the species")),
				@facet (
						name = IKeyword.PARENT,
						type = IType.SPECIES,
						optional = true,
						doc = @doc ("the parent class (inheritance)")),
				@facet (
						name = IKeyword.EDGE_SPECIES,
						type = IType.SPECIES,
						optional = true,
						doc = @doc ("In the case of a species defining a graph topology for its instances (nodes of the graph), specifies the species to use for representing the edges")),
				@facet (
						name = IKeyword.SKILLS,
						type = IType.LIST,
						of = IType.SKILL,
						optional = true,
						doc = @doc ("The list of skills that will be made available to the instances of this species. Each new skill provides attributes and actions that will be added to the ones defined in this species")),
				@facet (
						name = IKeyword.MIRRORS,
						type = { IType.LIST, IType.SPECIES },
						of = IType.AGENT,
						optional = true,
						doc = @doc ("The species this species is mirroring. The population of this current species will be dependent of that of the species mirrored (i.e. agents creation and death are entirely taken in charge by GAMA with respect to the demographics of the species mirrored). In addition, this species is provided with an attribute called 'target', which allows each agent to know which agent of the mirrored species it is representing.")),
				@facet (
						name = IKeyword.CONTROL,
						type = IType.SKILL,
						optional = true,
						doc = @doc ("defines the architecture of the species (e.g. fsm...)")),
				@facet (
						name = "compile",
						type = IType.BOOL,
						optional = true,
						doc = @doc (""),
						internal = true),
				@facet (
						name = IKeyword.FREQUENCY,
						type = IType.INT,
						optional = true,
						doc = @doc (
								value = "The execution frequency of the species (default value: 1). For instance, if frequency is set to 10, the population of agents will be executed only every 10 cycles.",
								see = { "schedules" })),
				@facet (
						name = IKeyword.SCHEDULES,
						type = IType.CONTAINER,
						of = IType.AGENT,
						optional = true,
						doc = @doc ("A container of agents (a species, a dynamic list, or a combination of species and containers) , which represents which agents will be actually scheduled when the population is scheduled for execution. Note that the world (or the simulation) is *always* scheduled first, so there is no need to explicitly mention it. Doing so would result in a runtime error. For instance, 'species a schedules: (10 among a)' will result in a population that schedules only 10 of its own agents every cycle. 'species b schedules: []' will prevent the agents of 'b' to be scheduled. Note that the scope of agents covered here can be larger than the population, which allows to build complex scheduling controls; for instance, defining 'global schedules: [] {...} species b schedules: []; species c schedules: b; ' allows to simulate a model where only the world and the agents of b are scheduled, without even having to create an instance of c.")),
				@facet (
						name = IKeyword.TOPOLOGY,
						type = IType.TOPOLOGY,
						optional = true,
						doc = @doc ("The topology of the population of agents defined by this species. In case of nested species, it can for example be the shape of the macro-agent. In case of grid or graph species, the topology is automatically computed and cannot be redefined")),
				@facet (
						name = IKeyword.VIRTUAL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("whether the species is virtual (cannot be instantiated, but only used as a parent) (false by default)")) },
		omissible = IKeyword.NAME)
@doc (
		value = "The species statement allows modelers to define new species in the model. `" + IKeyword.GLOBAL
				+ "` and `" + IKeyword.GRID + "` are speciel cases of species: `" + IKeyword.GLOBAL
				+ "` being the definition of the global agent (which has automatically one instance, world) and `"
				+ IKeyword.GRID + "` being a species with a grid topology.",
		usages = { @usage (
				value = "Here is an example of a species definition with a FSM architecture and the additional skill moving:",
				examples = { @example (
						value = "species ant skills: [moving] control: fsm { }",
						isExecutable = false) }),
				@usage (
						value = "In the case of a species aiming at mirroring another one:",
						examples = { @example (
								value = "species node_agent mirrors: list(bug) parent: graph_node edge_species: edge_agent { }",
								isExecutable = false) }),
				@usage (
						value = "The definition of the single grid of a model will automatically create gridwidth x gridheight agents:",
						examples = { @example (
								value = "grid ant_grid width: gridwidth height: gridheight file: grid_file neighbors: 8 use_regular_agents: false { }",
								isExecutable = false) }),
				@usage (
						value = "Using a file to initialize the grid can replace width/height facets:",
						examples = { @example (
								value = "grid ant_grid file: grid_file neighbors: 8 use_regular_agents: false { }",
								isExecutable = false) }) })
@validator (SpeciesValidator.class)
public class GamlSpecies extends GamlClass implements ISpecies {

	/**
	 * The Class SpeciesValidator.
	 */
	public static class SpeciesValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription desc) {

			final SpeciesDescription sd = (SpeciesDescription) desc;

			final IExpression neighbours = processNeighbors(sd);
			// Issue 1311
			final IExpression cellWidth = sd.getFacetExpr(CELL_WIDTH);
			final IExpression cellHeight = sd.getFacetExpr(CELL_HEIGHT);
			if (cellWidth != null == (cellHeight == null)) {
				sd.error("'cell_width' and 'cell_height' must be defined together", IGamlIssue.CONFLICTING_FACETS,
						cellWidth == null ? CELL_HEIGHT : CELL_WIDTH);
				return;
			}
			final IExpression width = sd.getFacetExpr(WIDTH);
			final IExpression height = sd.getFacetExpr(HEIGHT);
			if (cellWidth != null && width != null) {
				sd.error("'cell_width' and 'width' cannot be defined at the same time", IGamlIssue.CONFLICTING_FACETS,
						WIDTH);
				return;
			}
			if (cellHeight != null && height != null) {
				sd.error("'cell_width' and 'width' cannot be defined at the same time", IGamlIssue.CONFLICTING_FACETS,
						HEIGHT);
				return;
			}

			if (cellHeight != null || cellWidth != null || width != null || height != null || neighbours != null) {
				if (!IKeyword.GRID.equals(sd.getKeyword())) {
					sd.warning("Facets related to dimensions and neighboring can only be defined in 'grids' definition",
							IGamlIssue.CONFLICTING_FACETS);
				}
			}
			verifyFiles(sd, width, height, cellWidth, cellHeight);
			// Issue 1138
			verifyFrequency(sd);
			verifyTorus(sd);

			final String name = sd.getName();
			if (GAML.isUnaryOperator(name)) {
				sd.error("The name '" + name + "' cannot be used for naming this " + sd.getKeyword()
						+ ", as the derived casting operator (" + name
						+ "(...)) would conflict with an existing unary operator");
			}

		}

		/**
		 * Process neighbors.
		 *
		 * @param sd
		 *            the sd
		 * @return the i expression
		 */
		private IExpression processNeighbors(final SpeciesDescription sd) {

			final IExpression neighbours = sd.getFacetExpr(IKeyword.NEIGHBORS);
			if (neighbours != null) { sd.setFacet(NEIGHBORS, neighbours); }
			return neighbours;
		}

		/**
		 * Verify torus.
		 *
		 * @param desc
		 *            the desc
		 */
		private void verifyTorus(final IDescription desc) {
			// If torus is declared on a species other than "global", emit a
			// warning
			final IExpression torus = desc.getFacetExpr(TORUS);
			if (torus != null
					&& (IKeyword.SPECIES.equals(desc.getKeyword()) || IKeyword.GRID.equals(desc.getKeyword()))) {
				desc.warning("The 'torus' facet can only be specified for the model topology (i.e. in 'global')",
						IGamlIssue.WRONG_CONTEXT, TORUS);
			}
		}

		/**
		 * Verify files.
		 *
		 * @param desc
		 *            the desc
		 * @param width
		 *            the width
		 * @param height
		 *            the height
		 * @param sd
		 *            the sd
		 * @param cellWidth
		 *            the cell width
		 * @param cellHeight
		 *            the cell height
		 */
		private void verifyFiles(final SpeciesDescription sd, final IExpression width, final IExpression height,
				final IExpression cellWidth, final IExpression cellHeight) {
			final IExpression file = sd.getFacetExpr(FILE);
			final IExpression files = sd.getFacetExpr(FILES);
			if (file != null && files != null) {
				sd.error(
						"The use of the 'files' facet prohibits the use of the 'files' facet: if several files have to be loaded in the grid, use the 'files' facet, otherwise use the 'file' facet",
						IGamlIssue.CONFLICTING_FACETS, FILE);
			}
			if ((file != null || files != null)
					&& (height != null || width != null || cellWidth != null || cellHeight != null)) {
				sd.error(
						"The use of the 'file' and 'files' facets prohibit the use of dimension facets ('width', 'height', 'cell_width', 'cell_height')",
						IGamlIssue.CONFLICTING_FACETS, FILE);
			}
		}

		/**
		 * Verify frequency.
		 *
		 * @param desc
		 *            the desc
		 * @param sd
		 *            the sd
		 */
		private void verifyFrequency(final SpeciesDescription sd) {
			final IExpression freq = sd.getFacetExpr(FREQUENCY);
			if (freq != null && freq.isConst() && Integer.valueOf(0).equals(freq.getConstValue())) {
				for (final VariableDescription vd : sd.getAttributes()) {
					if (vd.getFacet(UPDATE, VALUE) != null) {
						vd.warning(vd.getName() + " will never be updated because " + sd.getName()
								+ " has a scheduling frequency of 0", IGamlIssue.WRONG_CONTEXT);
					}
				}
				for (final IDescription bd : sd.getBehaviors()) {
					bd.warning(bd.getName() + " will never be run because " + sd.getName()
							+ " has a scheduling frequency of 0", IGamlIssue.WRONG_CONTEXT);

				}
			}
		}
	}

	/** The concurrency. */
	protected IExpression concurrency;

	/** The schedule. */
	private final IExpression schedule;

	/** The frequency. */
	private final IExpression frequency;

	/** Indicates whether this species is a grid. */
	protected final boolean isGrid;

	/** Indicates whether this species is a graph. */
	protected final boolean isGraph;

	/** A map of micro-species associated with this species. */
	protected final Map<String, ISpecies> microSpecies = GamaMapFactory.createOrdered();

	/** The aspects. */
	private final Map<String, AspectStatement> aspects = GamaMapFactory.createOrdered();

	/** The user commands. */
	private final Map<String, UserCommandStatement> userCommands = GamaMapFactory.createOrdered();

	/** The behaviors. */
	private final List<IStatement> behaviors = new ArrayList<>();

	/** The control architecture of this species. */
	final IArchitecture control;

	/**
	 * Instantiates a new gaml species.
	 *
	 * @param desc
	 *            the desc
	 */
	public GamlSpecies(final IDescription desc) {
		super(desc);
		isGrid = IKeyword.GRID.equals(getKeyword());
		isGraph = AbstractGraphNodeAgent.class.isAssignableFrom(((SpeciesDescription) description).getJavaBase());
		control = (IArchitecture) getDescription().getControl().createInstance();
		concurrency = this.getFacet(IKeyword.PARALLEL);
		if (isMirror() && !hasFacet(IKeyword.SCHEDULES)) {
			// See Issue #2731 -- mirror species have a default scheduling rule
			schedule = scope -> {
				final IList<IAgent> agents = GamaListFactory.create();
				for (final IAgent agent : getPopulation(scope)) {
					final Object obj = agent.getDirectVarValue(scope, IKeyword.TARGET);
					if (obj instanceof IAgent target && !target.dead()) { agents.add(agent); }

				}
				return agents;
			};
		} else {
			schedule = this.getFacet(IKeyword.SCHEDULES);
		}
		frequency = this.getFacet(IKeyword.FREQUENCY);
	}

	@Override
	public String getArchitectureName() { return getLiteral(IKeyword.CONTROL); }

	@Override
	public IExpression getFrequency() { return frequency; }

	@Override
	public IExpression getSchedule() { return schedule; }

	@Override
	public IExpression getConcurrency() { return concurrency; }

	/**
	 * Method getSpecies()
	 *
	 * @see gama.core.metamodel.topology.filter.IAgentFilter#getSpecies()
	 */
	@Override
	public ISpecies getSpecies() { return this; }

	/**
	 * Method getAgents()
	 *
	 * @see gama.core.metamodel.topology.filter.IAgentFilter#getAgents()
	 */
	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		return this;
	}

	@Override
	public boolean hasAgentList() {
		return true;
	}

	/**
	 * Method accept()
	 *
	 * @see gama.core.metamodel.topology.filter.IAgentFilter#accept(gama.core.runtime.IScope,
	 *      gama.core.metamodel.shape.IShape, gama.core.metamodel.shape.IShape)
	 */
	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? false : pop.accept(scope, source, a);
	}

	@Override
	public boolean containsKey(final IScope scope, final Object o) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? false : pop.containsKey(scope, o);
	}

	@Override
	public StreamEx<IAgent> stream(final IScope scope) {
		final IPopulation<IAgent> pop = getPopulation(scope);
		return pop == null ? StreamEx.empty() : pop.stream(scope);
	}

	/**
	 * Method filter()
	 *
	 * @see gama.core.metamodel.topology.filter.IAgentFilter#filter(gama.core.runtime.IScope,
	 *      gama.core.metamodel.shape.IShape, java.util.Collection)
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		if (pop != null) { pop.filter(scope, source, results); }
	}

	/**
	 * Belongs to A micro model.
	 *
	 * @return true, if successful
	 */
	public boolean belongsToAMicroModel() {
		return getDescription().belongsToAMicroModel();
	}

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), IKeyword.NAME, getName());
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	@Override
	public java.lang.Iterable<? extends IAgent> iterable(final IScope scope) {
		return getPopulation(scope).iterable(scope);
	}

	@Override
	public void addTemporaryAction(final ActionStatement action) {
		actions.put(action.getName(), action);
	}

	@Override
	public void removeTemporaryAction() {
		actions.remove(IExpressionFactory.TEMPORARY_ACTION_NAME);
		getDescription().removeAction(IExpressionFactory.TEMPORARY_ACTION_NAME);
	}

	/**
	 * Retrieves the population of agents for this species in the given scope.
	 *
	 * @param scope
	 *            the current simulation scope
	 * @return the population of agents
	 */
	@Override
	public IPopulation<IAgent> getPopulation(final IScope scope) {
		final IAgent a = scope.getAgent();
		IPopulation result = null;
		if (a != null) {
			// AD 19/09/13 Patch to allow experiments to gain access to the
			// simulation populations
			result = a.getPopulationFor(this);
		}
		return result;
	}

	/**
	 * Retrieves the list of micro-species associated with this species.
	 *
	 * @return a list of micro-species
	 */
	@Override
	public IList<IAgent> listValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		// return getPopulation(scope).listValue(scope, contentsType);
		// hqnghi 16/04/14
		IPopulation pop = getPopulation(scope);
		if (pop == null) { pop = scope.getSimulation().getPopulationFor(contentsType.getName()); }
		// AD 20/01/16 : Explicitly passes true in order to obtain a copy of the
		// population
		return pop.listValue(scope, contentsType, true);
		// end-hqnghi
	}

	@Override
	public String stringValue(final IScope scope) {
		return name;
	}

	@Override
	public IMap<?, ?> mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		final IList<IAgent> agents = listValue(scope, contentsType, false);
		// Default behavior : Returns a map containing the names of agents as
		// keys and the agents themselves as values
		final IMap result = GamaMapFactory.create(Types.STRING, scope.getType(getName()));
		for (final IAgent agent : agents.iterable(scope)) { result.put(agent.getName(), agent); }
		return result;
	}

	@Override
	public boolean isGrid() { return isGrid; }

	@Override
	public boolean isGraph() { return isGraph; }

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public ISpecies copy(final IScope scope) {
		return this;
		// Species are immutable
	}

	@Override
	public IList<ISpecies> getMicroSpecies() {
		final IList<ISpecies> retVal = GamaListFactory.create(Types.SPECIES);
		retVal.addAll(microSpecies.values());
		final ISpecies parentSpecies = this.getParentSpecies();
		if (parentSpecies != null) { retVal.addAll(parentSpecies.getMicroSpecies()); }
		return retVal;
	}

	@Override
	public IList<ISpecies> getSubSpecies(final IScope scope) {
		final IList<ISpecies> subspecies = GamaListFactory.create(Types.SPECIES);
		final GamlModelSpecies model = (GamlModelSpecies) scope.getModel().getSpecies();
		for (final ISpecies s : model.getAllSpecies().values()) {
			if (s.getParentSpecies() == this) { subspecies.add(s); }
		}
		return subspecies;
	}

	@Override
	public Collection<String> getMicroSpeciesNames() { return microSpecies.keySet(); }

	/**
	 * Returns a micro-species with the specified name or null otherwise.
	 *
	 * @param microSpeciesName
	 * @return a species or null
	 */
	@Override
	public ISpecies getMicroSpecies(final String microSpeciesName) {
		final ISpecies retVal = microSpecies.get(microSpeciesName);
		if (retVal != null) return retVal;
		final ISpecies parentSpecies = this.getParentSpecies();
		if (parentSpecies != null) return parentSpecies.getMicroSpecies(microSpeciesName);
		return null;
	}

	@Override
	public boolean containMicroSpecies(final ISpecies species) {
		final ISpecies parentSpecies = this.getParentSpecies();
		return microSpecies.containsValue(species)
				|| (parentSpecies != null ? parentSpecies.containMicroSpecies(species) : false);
	}

	@Override
	public boolean hasMicroSpecies() {
		final ISpecies parentSpecies = this.getParentSpecies();
		return !microSpecies.isEmpty() || (parentSpecies != null ? parentSpecies.hasMicroSpecies() : false);
	}

	@Override
	public SpeciesDescription getDescription() { return (SpeciesDescription) description; }

	@Override
	public boolean isPeer(final ISpecies other) {
		return other != null && other.getMacroSpecies().equals(this.getMacroSpecies());
	}

	/**
	 * Retrieves the parent species of this species.
	 *
	 * @return the parent species, or {@code null} if none exists
	 */
	@Override
	public ISpecies getParentSpecies() {
		if (parentClass == null) {
			final TypeDescription parentSpecDesc = getDescription().getParent();
			// Takes care of invalid species (see Issue 711)
			if (parentSpecDesc == null || parentSpecDesc == getDescription()) return null;
			ISpecies currentMacroSpec = this.getMacroSpecies();
			while (currentMacroSpec != null && parentClass == null) {
				parentClass = currentMacroSpec.getMicroSpecies(parentSpecDesc.getName());
				currentMacroSpec = currentMacroSpec.getMacroSpecies();
			}
		}
		return (ISpecies) parentClass;
	}

	@Override
	public IArchitecture getArchitecture() { return control; }

	@Override
	public Collection<UserCommandStatement> getUserCommands() { return userCommands.values(); }

	@Override
	public Collection<? extends IExecutable> getAspects() { return aspects.values(); }

	@Override
	public Collection<IStatement> getBehaviors() { return behaviors; }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		super.setChildren(children);
		// First we verify the control architecture
		if (control == null)
			throw GamaRuntimeException.error("The control of species " + description.getName() + " cannot be computed",
					GAMA.getRuntimeScope());
		// Then we classify the children in their categories
		for (final ISymbol s : children) {
			if (s instanceof ISpecies oneMicroSpecies) {
				oneMicroSpecies.setMacroSpecies(this);
				microSpecies.put(oneMicroSpecies.getName(), oneMicroSpecies);
			} else if (s instanceof AspectStatement) {
				aspects.put(s.getName(), (AspectStatement) s);
			} else if (s instanceof UserCommandStatement) {
				userCommands.put(s.getName(), (UserCommandStatement) s);
			} else if (s instanceof IStatement) {
				behaviors.add((IStatement) s); // reflexes, states or tasks
			}
		}
		control.setChildren(behaviors);
		behaviors.forEach(b -> b.setEnclosing(this));
		control.verifyBehaviors(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		for (final AspectStatement ac : aspects.values()) { ac.dispose(); }
		aspects.clear();
		for (final IStatement c : behaviors) { c.dispose(); }
		behaviors.clear();

		// TODO dispose micro_species first???
		microSpecies.clear();
	}

	// TODO review this
	// this is the "original" macro-species???

	@Override
	public void setMacroSpecies(final ISpecies macroSpecies) { this.macroSpecies = macroSpecies; }

	/*
	 * Equation (Huynh Quang Nghi)
	 */

	@Override
	public <T extends IStatement> T getStatement(final Class<T> clazz, final String valueOfFacetName) {
		for (final IStatement s : behaviors) {
			final boolean instance = clazz.isAssignableFrom(s.getClass());
			if (instance) {
				if (valueOfFacetName == null) return (T) s;
				final String t = s.getDescription().getName();
				if (t != null) {
					final boolean named = t.equals(valueOfFacetName);
					if (named) return (T) s;
				}
			}
		}
		return null;
	}

	@Override
	public Boolean implementsSkill(final String skill) {
		return getDescription().implementsSkill(skill);
	}

	@Override
	public IAgent get(final IScope scope, final Integer index) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.get(scope, index);
	}

	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? false : pop.contains(scope, o);
	}

	@Override
	public IAgent firstValue(final IScope scope) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.firstValue(scope);
	}

	@Override
	public IAgent lastValue(final IScope scope) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.lastValue(scope);
	}

	@Override
	public int length(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? 0 : pop.length(scope);
	}

	@Override
	public boolean isEmpty(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? true : pop.isEmpty(scope);
	}

	@Override
	public IContainer<Integer, ? extends IAgent> reverse(final IScope scope) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.reverse(scope);
	}

	@Override
	public IAgent anyValue(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.anyValue(scope);
	}

	@Override
	public IMatrix<? extends IAgent> matrixValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.matrixValue(scope, contentsType, copy);
	}

	@Override
	public IMatrix<? extends IAgent> matrixValue(final IScope scope, final IType contentsType,
			final GamaPoint preferredSize, final boolean copy) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.matrixValue(scope, contentsType, preferredSize, copy);
	}

	@Override
	public IAgent getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.getFromIndicesList(scope, indices);
	}

	@Override
	public boolean isMirror() { return getDescription().isMirror(); }

	@Override
	public Collection<? extends IPopulation<? extends IAgent>> getPopulations(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? Collections.EMPTY_LIST : Collections.singleton(pop);
	}

	/**
	 * Gets the skill instance for.
	 *
	 * @param skillClass
	 *            the skill class
	 * @return the skill instance for
	 */
	public ISkill getSkillInstanceFor(final Class skillClass) {
		if (skillClass == null) return null;
		if (skillClass.isAssignableFrom(control.getClass())) return control;
		return getSkillInstanceFor(getDescription(), skillClass);
	}

	/**
	 * Gets the skill instance for.
	 *
	 * @param sd
	 *            the sd
	 * @param skillClass
	 *            the skill class
	 * @return the skill instance for
	 */
	private ISkill getSkillInstanceFor(final SpeciesDescription sd, final Class skillClass) {
		for (final SkillDescription sk : sd.getSkills()) {
			if (skillClass.isAssignableFrom(sk.getJavaBase())) return sk.getInstance();
		}
		if (sd.getParent() != null && sd.getParent() != sd) return getSkillInstanceFor(sd.getParent(), skillClass);
		return null;
	}

	@Override
	public boolean hasAspect(final String n) {
		return aspects.containsKey(n);
	}

	@Override
	public IExecutable getAspect(final String n) {
		return aspects.get(n);
	}

	@Override
	public IList<String> getAspectNames() { return GamaListFactory.wrap(Types.STRING, aspects.keySet()); }

	@Override
	public IContainerType<?> getGamlType() {
		return (IContainerType<?>) getDescription().getConstantExpr().getGamlType();
	}

}
