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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

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
import gama.api.GAMA;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.statements.IStatement.WithArgs;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGraphAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.object.IClass;
import gama.api.kernel.skill.IArchitecture;
import gama.api.kernel.skill.ISkill;
import gama.api.kernel.species.GamlSpecies.SpeciesValidator;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.ui.IExperimentDisplayable;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;
import one.util.streamex.StreamEx;

/**
 * Concrete implementation of a GAML species with full metadata and validation.
 *
 * <p>
 * This class represents a standard species definition in GAML, providing the complete implementation for agent type
 * specifications including scheduling, concurrency, mirroring, and specialized topologies (grid, graph). It implements
 * {@link ISpecies} with GAML-specific features and annotations.
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
 *
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
 *
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
 *
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
 *
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
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlSpecies extends Symbol implements ISpecies {

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

	/** Indicates whether this species represents a grid (spatial lattice of agents). */
	protected final boolean isGrid;

	/** Indicates whether this species represents a graph (agents are nodes in a graph). */
	protected final boolean isGraph;

	/** Map of micro-species (species that can be hosted by agents of this species), keyed by name. */
	protected final Map<String, ISpecies> microSpecies = GamaMapFactory.createOrdered();

	/** Map of variables (attributes) defined in this species, keyed by name. */
	private final Map<String, IVariable> variables = GamaMapFactory.createOrdered();

	/** Map of aspect definitions for visualizing agents, keyed by name. */
	private final Map<String, IStatement.Aspect> aspects = GamaMapFactory.createOrdered();

	/** Map of actions (methods) that can be invoked on agents, keyed by name. */
	private final Map<String, IStatement.Action> actions = GamaMapFactory.createOrdered();

	/** Map of user commands (UI-accessible actions), keyed by name. */
	private final Map<String, IStatement.UserCommand> userCommands = GamaMapFactory.createOrdered();

	/** List of behaviors (reflexes, init, etc.) that define agent behavior. */
	private final List<IStatement> behaviors = new ArrayList<>();

	/** The macro-species (species that hosts this species as a micro-species). */
	protected ISpecies macroSpecies;

	/** The parent species (species from which this species inherits). */
	protected ISpecies parentSpecies;

	/** The control architecture that governs how agents of this species execute their behaviors. */
	final IArchitecture control;

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
		setName(description.getName());
		isGrid = IKeyword.GRID.equals(getKeyword());
		isGraph = IGraphAgent.class.isAssignableFrom(((ISpeciesDescription) description).getJavaBase());
		control = getDescription().getControl().createArchitectureInstance();
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
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	@Override
	public Collection<IStatement> getBehaviors() { return behaviors; }

	@Override
	public java.lang.Iterable<? extends IAgent> iterable(final IScope scope) {
		return getPopulation(scope).iterable(scope);
	}

	/**
	 * Adds a temporary action to this species.
	 *
	 * <p>
	 * Temporary actions are dynamically added actions that can be removed later. They are typically used for on-the-fly
	 * action creation during runtime, such as when evaluating expressions that require temporary executable code.
	 * </p>
	 *
	 * @param action
	 *            the action to add temporarily
	 * @see #removeTemporaryAction()
	 */
	@Override
	public void addTemporaryAction(final IStatement action) {
		if (action instanceof IStatement.Action as) { actions.put(action.getName(), as); }
	}

	/**
	 * Removes the temporary action previously added.
	 *
	 * <p>
	 * This method removes both the action from the species' action map and from its description, ensuring complete
	 * cleanup.
	 * </p>
	 *
	 * @see #addTemporaryAction(IStatement)
	 */
	@Override
	public void removeTemporaryAction() {
		actions.remove(IExpressionFactory.TEMPORARY_ACTION_NAME);
		getDescription().removeAction(IExpressionFactory.TEMPORARY_ACTION_NAME);
	}

	/**
	 * Gets the population of agents of this species in the given scope.
	 *
	 * <p>
	 * The population is retrieved from the current agent in the scope. If the current agent doesn't directly contain a
	 * population of this species, the method attempts to find it through the agent's hierarchy. This is particularly
	 * useful for experiments accessing simulation populations.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the population of agents, or null if not found
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

	/**
	 * Gets all micro-species defined within this species.
	 *
	 * <p>
	 * Micro-species are species whose agents live inside agents of this species. This method returns all micro-species
	 * including those inherited from parent species.
	 * </p>
	 *
	 * @return a list of all micro-species
	 */
	@Override
	public IList<ISpecies> getMicroSpecies() {
		final IList<ISpecies> retVal = GamaListFactory.create(Types.SPECIES);
		retVal.addAll(microSpecies.values());
		final ISpecies parentSpecies = this.getParentSpecies();
		if (parentSpecies != null) { retVal.addAll(parentSpecies.getMicroSpecies()); }
		return retVal;
	}

	/**
	 * Gets all sub-species (direct children) of this species through inheritance.
	 *
	 * <p>
	 * Unlike micro-species (which are about composition), sub-species are those that directly extend this species
	 * through the {@code parent:} facet. This method traverses the model to find all such species.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return a list of all direct sub-species
	 */
	@Override
	public IList<ISpecies> getSubSpecies(final IScope scope) {
		final IList<ISpecies> subspecies = GamaListFactory.create(Types.SPECIES);
		final IModelSpecies model = scope.getModel();
		for (final ISpecies s : model.getAllSpecies().values()) {
			if (s.getParentSpecies() == this) { subspecies.add(s); }
		}
		return subspecies;
	}

	@Override
	public Collection<String> getMicroSpeciesNames() { return microSpecies.keySet(); }

	/**
	 * Gets a specific micro-species by name.
	 *
	 * <p>
	 * Searches for a micro-species with the given name, first in this species' own micro-species, then recursively in
	 * the parent species' micro-species if not found.
	 * </p>
	 *
	 * @param microSpeciesName
	 *            the name of the micro-species to retrieve
	 * @return the micro-species, or null if not found
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
	public ISpeciesDescription getDescription() { return (ISpeciesDescription) description; }

	@Override
	public boolean isPeer(final ISpecies other) {
		return other != null && Objects.equal(other.getMacroSpecies(), this.getMacroSpecies());
	}

	@Override
	public List<ISpecies> getSelfWithParents() {
		final List<ISpecies> retVal = new ArrayList<>();
		retVal.add(this);
		ISpecies currentParent = this.getParentSpecies();
		while (currentParent != null) {
			retVal.add(currentParent);
			currentParent = currentParent.getParentSpecies();
		}

		return retVal;
	}

	/**
	 * Gets the parent species from which this species inherits.
	 *
	 * <p>
	 * The parent species is resolved lazily on first access. The method searches for the parent species by traversing
	 * the macro-species hierarchy, starting from this species' macro-species and moving upward until the parent is
	 * found or the hierarchy is exhausted.
	 * </p>
	 *
	 * @return the parent species, or null if this species has no parent
	 */
	@Override
	public ISpecies getParentSpecies() {
		if (parentSpecies == null) {
			final ITypeDescription parentSpecDesc = getDescription().getParent();
			// Takes care of invalid species (see Issue 711)
			if (parentSpecDesc == null || parentSpecDesc == getDescription()) return null;
			ISpecies currentMacroSpec = this.getMacroSpecies();
			while (currentMacroSpec != null && parentSpecies == null) {
				parentSpecies = currentMacroSpec.getMicroSpecies(parentSpecDesc.getName());
				currentMacroSpec = currentMacroSpec.getMacroSpecies();
			}
		}
		return parentSpecies;
	}

	@Override
	public <T extends IClass> boolean extendsClassOrSpecies(final T s) {
		final ISpecies parent = getParentSpecies();
		if (parent == null) return false;
		if (parent == s) return true;
		return parent.extendsClassOrSpecies(s);
	}

	@Override
	public String getParentName() { return getDescription().getParentName(); }

	@Override
	public IArchitecture getArchitecture() { return control; }

	@Override
	public IVariable getVar(final String n) {
		return variables.get(n);
	}

	@Override
	public boolean hasVar(final String name) {
		return variables.containsKey(name);
	}

	@Override
	public Collection<String> getVarNames() { return getDescription().getAttributeNames(); }

	@Override
	public Collection<IVariable> getVars() { return variables.values(); }

	@Override
	public Collection<? extends IExperimentDisplayable> getUserCommands() { return userCommands.values(); }

	@Override
	public WithArgs getAction(final String name) {
		return actions.get(name);
	}

	@Override
	public Collection<IStatement.Action> getActions() { return actions.values(); }

	@Override
	public boolean hasAspect(final String n) {
		return aspects.containsKey(n);
	}

	@Override
	public IExecutable getAspect(final String n) {
		return aspects.get(n);
	}

	@Override
	public Collection<? extends IExecutable> getAspects() { return aspects.values(); }

	@Override
	public IList<String> getAspectNames() { return GamaListFactory.wrap(Types.STRING, aspects.keySet()); }

	/**
	 * Organizes and assigns children symbols to this species.
	 *
	 * <p>
	 * This method is called during species initialization to categorize and store all child symbols (variables,
	 * actions, behaviors, aspects, micro-species, etc.) in their respective collections. The process:
	 * </p>
	 * <ol>
	 * <li>Validates the control architecture</li>
	 * <li>Iterates through all child symbols</li>
	 * <li>Classifies each symbol into the appropriate collection (variables, actions, etc.)</li>
	 * <li>Sets this species as the enclosing symbol for each child</li>
	 * <li>Passes behaviors to the control architecture for validation</li>
	 * </ol>
	 *
	 * @param children
	 *            the child symbols to organize
	 * @throws GamaRuntimeException
	 *             if the control architecture cannot be computed
	 */
	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		// First we verify the control architecture
		if (control == null)
			throw GamaRuntimeException.error("The control of species " + description.getName() + " cannot be computed",
					GAMA.getRuntimeScope());
		// Then we classify the children in their categories
		for (final ISymbol s : children) {
			if (s != null) {
				s.setEnclosing(this);
				switch (s) {
					case ISpecies spec -> microSpecies.put(spec.getName(), spec);
					case IVariable v -> variables.put(v.getName(), v);
					case IStatement.Aspect as -> aspects.put(s.getName(), as);
					case IStatement.Action ac -> actions.put(s.getName(), ac);
					case IStatement.UserCommand uc -> userCommands.put(s.getName(), uc);
					case IStatement stat -> behaviors.add(stat);
					default -> {
					}
				}
			}
		}
		control.setChildren(behaviors);
		control.verifyBehaviors(this);
	}

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		super.dispose();
		for (final IVariable v : variables.values()) { v.dispose(); }
		variables.clear();
		for (final IStatement.Aspect ac : aspects.values()) { ac.dispose(); }
		aspects.clear();
		for (final IStatement.Action ac : actions.values()) { ac.dispose(); }
		actions.clear();
		for (final IStatement c : behaviors) { c.dispose(); }
		behaviors.clear();
		macroSpecies = null;
		parentSpecies = null;
		// TODO dispose micro_species first???
		microSpecies.clear();
	}

	// TODO review this
	// this is the "original" macro-species???
	@Override
	public ISpecies getMacroSpecies() { return macroSpecies; }

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

	/*
	 * end-of Equation
	 */

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
			final IPoint preferredSize, final boolean copy) throws GamaRuntimeException {
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
	 * Gets the skill instance for a given skill class.
	 *
	 * <p>
	 * Skills provide additional capabilities to agents. This method retrieves the singleton instance of a skill that is
	 * either the control architecture or declared in the species (or its parents). The search proceeds as follows:
	 * </p>
	 * <ol>
	 * <li>Check if the control architecture is an instance of the requested skill class</li>
	 * <li>Search in this species' declared skills</li>
	 * <li>Recursively search in parent species' skills</li>
	 * </ol>
	 *
	 * @param skillClass
	 *            the class of the skill to retrieve
	 * @return the skill instance, or null if not found
	 */
	@Override
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
	private ISkill getSkillInstanceFor(final ISpeciesDescription sd, final Class skillClass) {
		for (final ISkillDescription sk : sd.getSkills()) {
			if (skillClass.isAssignableFrom(sk.getJavaBase())) return sk.getInstance();
		}
		if (sd.getParent() != null && sd.getParent() != sd) return getSkillInstanceFor(sd.getParent(), skillClass);
		return null;
	}

	@Override
	public IList<String> getSubSpeciesNames(final IScope scope) {
		return StreamEx.of(getSubSpecies(scope)).map(ISpecies::getName)
				.toCollection(GamaListFactory.getSupplier(Types.STRING));
	}

	@Override
	public IList<String> getAttributeNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING, getVarNames());
	}

	@Override
	public IList<String> getActionNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING, StreamEx.of(getActions()).map(IStatement::getName).toList());
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
