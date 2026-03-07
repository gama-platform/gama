/*******************************************************************************************************
 *
 * GamlSpecies.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.species;

import java.util.Collection;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.GamlSpecies.SpeciesValidator;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;
import one.util.streamex.StreamEx;

/**
 * Concrete implementation of a GAML species with full metadata and validation.
 * 
 * <p>
 * This class represents a standard species definition in GAML, providing the complete implementation for agent type
 * specifications including scheduling, concurrency, mirroring, and specialized topologies (grid, graph). It extends
 * {@link AbstractSpecies} with GAML-specific features and annotations.
 * </p>
 * 
 * <h2>Core Features</h2>
 * <ul>
 * <li><b>Scheduling:</b> Control when and how agents are executed (frequency, custom schedules)</li>
 * <li><b>Concurrency:</b> Enable parallel execution of agents for performance</li>
 * <li><b>Mirroring:</b> Automatically synchronize with another species' population</li>
 * <li><b>Grid Topology:</b> Spatial lattice organization with neighbor relationships</li>
 * <li><b>Graph Topology:</b> Network structure with edges between agents</li>
 * <li><b>Skills:</b> Reusable behavior modules (moving, communication, etc.)</li>
 * <li><b>Control Architecture:</b> Behavior execution patterns (reflex, FSM, BDI, etc.)</li>
 * </ul>
 * 
 * <h2>Species Types</h2>
 * 
 * <h3>Regular Species</h3>
 * <p>
 * Standard agent populations with custom behaviors:
 * </p>
 * <pre>
 * {@code
 * species animal skills: [moving] {
 *     float energy <- 100.0;
 *     
 *     reflex move {
 *         do wander;
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>Grid Species</h3>
 * <p>
 * Spatially organized agents in a regular lattice:
 * </p>
 * <pre>
 * {@code
 * grid cell width: 50 height: 50 neighbors: 8 {
 *     rgb color <- #white;
 *     
 *     reflex update {
 *         color <- mean(neighbors collect each.color);
 *     }
 * }
 * }
 * </pre>
 * 
 * <h3>Mirror Species</h3>
 * <p>
 * Species that automatically tracks another species' population:
 * </p>
 * <pre>
 * {@code
 * species node_agent mirrors: list(agent) {
 *     // Each node automatically has a 'target' attribute
 *     // pointing to the mirrored agent
 *     aspect default {
 *         draw circle(target.size);
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>Scheduling Control</h2>
 * <p>
 * Species can control execution frequency and which agents are scheduled:
 * </p>
 * <pre>
 * {@code
 * species predator frequency: 2 schedules: shuffle(predator) {
 *     // Only scheduled every 2 cycles, in random order
 * }
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see AbstractSpecies
 * @see ISpecies
 * @see GamlModelSpecies
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
public class GamlSpecies extends AbstractSpecies {

	/**
	 * Validator for species descriptions.
	 * 
	 * <p>
	 * This validator ensures the consistency and correctness of species definitions, particularly for grid species. It
	 * validates:
	 * </p>
	 * <ul>
	 * <li>Dimension facets (width, height, cell_width, cell_height) are properly paired and non-conflicting</li>
	 * <li>Grid-specific facets are only used on grid species</li>
	 * <li>File loading facets don't conflict with dimension facets</li>
	 * <li>Frequency settings don't make variables/behaviors unreachable</li>
	 * <li>Torus topology is only defined on global species</li>
	 * <li>Species names don't conflict with existing unary operators</li>
	 * </ul>
	 */
	public static class SpeciesValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Validates the species description for correctness and consistency.
		 *
		 * @param desc
		 *            the species description to validate
		 */
		@Override
		public void validate(final IDescription desc) {

			final ISpeciesDescription sd = (ISpeciesDescription) desc;

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
		 * Processes and validates the neighbors facet for grid species.
		 * 
		 * <p>
		 * The neighbors facet specifies the neighborhood type for grid cells (typically 4, 6, or 8).
		 * </p>
		 *
		 * @param sd
		 *            the species description
		 * @return the neighbors expression, or null if not specified
		 */
		private IExpression processNeighbors(final ISpeciesDescription sd) {

			final IExpression neighbours = sd.getFacetExpr(IKeyword.NEIGHBORS);
			if (neighbours != null) { sd.setFacet(NEIGHBORS, neighbours); }
			return neighbours;
		}

		/**
		 * Validates that the torus facet is only defined on global species.
		 * 
		 * <p>
		 * Torus topology creates a wrapped world where agents crossing one edge appear on the opposite edge. This
		 * should only be defined at the model level.
		 * </p>
		 *
		 * @param desc
		 *            the species description
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
		 * Validates file loading facets for grid species.
		 * 
		 * <p>
		 * Grid species can load initial values from image or raster files. This method ensures:
		 * </p>
		 * <ul>
		 * <li>The 'file' and 'files' facets are not used simultaneously</li>
		 * <li>File facets don't conflict with explicit dimension facets</li>
		 * </ul>
		 *
		 * @param sd
		 *            the species description
		 * @param width
		 *            the width expression
		 * @param height
		 *            the height expression
		 * @param cellWidth
		 *            the cell width expression
		 * @param cellHeight
		 *            the cell height expression
		 */
		private void verifyFiles(final ISpeciesDescription sd, final IExpression width, final IExpression height,
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
		 * Validates the frequency facet and warns about unreachable code.
		 * 
		 * <p>
		 * If a species has a frequency of 0 (never scheduled), any variables with update expressions or behaviors will
		 * never be executed. This method warns the modeler about such dead code.
		 * </p>
		 *
		 * @param sd
		 *            the species description
		 */
		private void verifyFrequency(final ISpeciesDescription sd) {
			final IExpression freq = sd.getFacetExpr(FREQUENCY);
			if (freq != null && freq.isConst() && Integer.valueOf(0).equals(freq.getConstValue())) {
				for (final IVariableDescription vd : sd.getAttributes()) {
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

	/** Expression defining the level of concurrency for agent execution (boolean or integer threshold). */
	protected IExpression concurrency;

	/** Expression defining which agents should be scheduled for execution. */
	private final IExpression schedule;

	/** Expression defining how often this species should be scheduled (in cycles). */
	private final IExpression frequency;

	/**
	 * Constructs a new GAML species from its description.
	 * 
	 * <p>
	 * This constructor initializes the species with scheduling and concurrency controls. For mirror species, it
	 * automatically creates a default schedule expression that filters out agents whose mirrored targets are dead.
	 * </p>
	 *
	 * @param desc
	 *            the species description
	 */
	public GamlSpecies(final IDescription desc) {
		super(desc);
		concurrency = this.getFacet(IKeyword.PARALLEL);
		if (isMirror() && !hasFacet(IKeyword.SCHEDULES)) {
			// See Issue #2731 -- mirror species have a default scheduling rule
			schedule = GAML.getExpressionFactory().createExpr(scope -> {
				final IList<IAgent> agents = GamaListFactory.create();
				for (final IAgent agent : getPopulation(scope)) {
					final Object obj = agent.getDirectVarValue(scope, IKeyword.TARGET);
					if (obj instanceof IAgent target && !target.dead()) { agents.add(agent); }

				}
				return agents;
			}, Types.LIST.of(Types.AGENT));
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
	 * @see gama.api.utils.interfaces.IAgentFilter#getSpecies()
	 */
	@Override
	public ISpecies getSpecies() { return this; }

	/**
	 * Method getAgents()
	 *
	 * @see gama.api.utils.interfaces.IAgentFilter#getAgents()
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
	 * @see gama.api.utils.interfaces.IAgentFilter#accept(gama.api.runtime.scope.IScope, gama.api.types.geometry.IShape,
	 *      gama.api.types.geometry.IShape)
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
	 * @see gama.api.utils.interfaces.IAgentFilter#filter(gama.api.runtime.scope.IScope, gama.api.types.geometry.IShape,
	 *      java.util.Collection)
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		if (pop != null) { pop.filter(scope, source, results); }
	}

	/**
	 * Method getType()
	 *
	 * @see gama.api.types.misc.IContainer#getGamlType()
	 */
	@Override
	public IContainerType<?> getGamlType() {
		return (IContainerType<?>) getDescription().getSpeciesExpr().getGamlType();
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
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), IKeyword.NAME, getName());
	}

}
