/*******************************************************************************************************
 *
 * CreateStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.statements;

import static gama.annotations.constants.IKeyword.AS;
import static gama.annotations.constants.IKeyword.CREATE;
import static gama.annotations.constants.IKeyword.FROM;
import static gama.annotations.constants.IKeyword.HEADER;
import static gama.annotations.constants.IKeyword.NUMBER;
import static gama.annotations.constants.IKeyword.RETURNS;
import static gama.annotations.constants.IKeyword.SPECIES;
import static gama.annotations.constants.IKeyword.WITH;
import static gama.annotations.support.ISymbolKind.SEQUENCE_STATEMENT;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
import gama.api.additions.delegates.ICreateDelegate;
import gama.api.additions.registries.GamaAdditionRegistry;
import gama.api.annotations.serializer;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.serialization.StatementSerializer;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.AbstractStatementSequence;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.gaml.statements.CreateStatement.CreateSerializer;
import gama.gaml.statements.CreateStatement.CreateValidator;

/**
 * This command is used to create agents.
 *
 * Considering the invoking agent as the execution context, species of the created agents can be 1. The same species of
 * the invoking agent or any peer species of the invoking agent's species. The newly created agent(s) will take the
 * invoking agent's macro-agent as its/their macro-agent.
 *
 * 2. The direct micro-species of the invoking agent's species. The newly create agent(s) will take the invoking agent
 * as its/their macro-agent.
 *
 * 3. The direct macro-species of the invoking agent's species or any peer species of this direct macro-species. The
 * newly created agent(s) will take the macro-agent of invoking agent's macro-agent as its/their macro-agent.
 *
 * Creation of agents from CSV files: create toto from: "toto.csv" header: true with:[att1::read("NAME"),
 * att2::read("TYPE")]; or, without header: create toto from: "toto.csv"with:[att1::read(0), att2::read(1)]; //with the
 * read(int), the index of the column.
 *
 * <p>
 * <b>Thread-safety:</b> the {@link #init} field, which carries the formal {@link Arguments} set by
 * {@link #setFormalArgs(Arguments)}, is held in an {@link java.util.concurrent.atomic.AtomicReference} so that
 * concurrent reads from {@link #fillWithUserInit} and {@link #privateExecuteIn} always observe a consistent reference,
 * even when multiple parallel simulations share the same statement instance.
 * </p>
 */
@symbol (
		name = CREATE,
		kind = SEQUENCE_STATEMENT,
		with_sequence = true,
		with_args = true,
		breakable = true,
		concept = { IConcept.SPECIES },
		remote_context = true)
@inside (
		kinds = { ISymbolKind.BEHAVIOR, SEQUENCE_STATEMENT })
@facets (
		value = { @facet (
				name = SPECIES,
				type = { IType.SPECIES, IType.AGENT },
				optional = true,
				doc = @doc ("the species of the agents to be created. In the case of simulations, the name 'simulation', which represents the current instance of simulation, can also be used as a proxy to their species")),
				@facet (
						name = RETURNS,
						type = IType.NEW_TEMP_ID,
						optional = true,
						doc = @doc ("a new temporary variable name containing the list of created agents (a list, even if only one agent has been created)")),
				@facet (
						name = FROM,
						type = IType.NONE,
						optional = true,
						doc = @doc ("a geometry, a container of geometries, a string (the path of a file), a file (shapefile, a .csv, a .asc or a OSM file) or a container returned by a request to a database")),
				@facet (
						name = NUMBER,
						type = IType.INT,
						optional = true,
						doc = @doc ("the number of agents to create")),
				@facet (
						name = AS,
						type = { IType.SPECIES },
						optional = true,
						internal = true,
						doc = @doc ("optionally indicates a species into which to cast the created agents.")),
				@facet (
						name = WITH,
						type = { IType.MAP },
						of = IType.NONE,
						index = IType.STRING,
						optional = true,
						doc = @doc ("an expression that evaluates to a map, for each pair the key is a species attribute and the value the assigned value")) },
		omissible = IKeyword.SPECIES)
@doc (
		value = "Allows an agent to create `number` agents of species `species`, to create agents of species `species` from a shapefile or to create agents of species `species` from one or several localized entities (discretization of the localized entity geometries).",
		usages = { @usage (
				value = "Its simple syntax to create `an_int` agents of species `a_species` is:",
				examples = { @example (
						value = "create a_species number: an_int;",
						isExecutable = false),
						@example (
								value = "create species_of(self) number: 5 returns: list5Agents;",
								isTestOnly = false),
				// @example (
				// var = "list5Agents",
				// returnType = "list",
				// value = "5",
				// isExecutable = false)
				}), @usage ("If `number` equals 0 or species is not a species, the statement is ignored."), @usage (
						value = "In GAML modelers can create agents of species `a_species` (with two attributes `type` and `nature` with types corresponding to the types of the shapefile attributes) from a shapefile `the_shapefile` while reading attributes 'TYPE_OCC' and 'NATURE' of the shapefile. One agent will be created by object contained in the shapefile:",
						examples = @example (
								value = "create a_species from: the_shapefile with: [type:: read('TYPE_OCC'), nature::read('NATURE')];",
								isExecutable = false)),
				@usage (
						value = "In order to create agents from a .csv file, facet `header` can be used to specified whether we can use columns header:",
						examples = { @example (
								value = "create toto from: \"toto.csv\" header: true with:[att1::read(\"NAME\"), att2::read(\"TYPE\")];",
								isExecutable = false),
								@example (
										value = "or",
										isExecutable = false),
								@example (
										value = "create toto from: \"toto.csv\" with:[att1::read(0), att2::read(1)]; //with read(int), the index of the column",
										isExecutable = false) }),
				@usage (
						value = "Similarly to the creation from shapefile, modelers can create agents from a set of geometries. In this case, one agent per geometry will be created (with the geometry as shape)",
						examples = { @example (
								value = "create species_of(self) from: [square(4), circle(4)]; 	// 2 agents have been created, with shapes respectively square(4) and circle(4)"),
								@example (
										value = "create species_of(self) from: [square(4), circle(4)] returns: new_agt;",
										isTestOnly = true),
								@example (
										value = "new_agt[0].shape",
										equals = "square(4)",
										returnType = "geometry",
										isTestOnly = true),
								@example (
										value = "new_agt[1].shape",
										equals = "circle(4)",
										returnType = "geometry",
										isTestOnly = true) }),
				@usage (
						value = "Created agents are initialized following the rules of their species. If one wants to refer to them after the statement is executed, the returns keyword has to be defined: the agents created will then be referred to by the temporary variable it declares. For instance, the following statement creates 0 to 4 agents of the same species as the sender, and puts them in the temporary variable children for later use.",
						examples = { @example (
								value = "create species (self) number: rnd (4) returns: children;",
								test = false),
								@example (
										value = "ask children {",
										test = true),
								@example (
										value = "        // ...",
										test = false),
								@example (
										value = "}",
										test = false) }),
				@usage (
						value = "If one wants to specify a special initialization sequence for the agents created, create provides the same possibilities as ask. This extended syntax is:",
						examples = { @example (
								value = "create a_species number: an_int {",
								isExecutable = false),
								@example (
										value = "     [statements]",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "The same rules as in ask apply. The only difference is that, for the agents created, the assignments of variables will bypass the initialization defined in species. For instance:",
						examples = { @example (
								value = "create species(self) number: rnd (4) returns: children {",
								isExecutable = false),
								@example (
										value = "     set location <- myself.location + {rnd (2), rnd (2)}; // tells the children to be initially located close to me",
										isExecutable = false),
								@example (
										value = "     set parent <- myself; // tells the children that their parent is me (provided the variable parent is declared in this species) ",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "Deprecated uses: ",
						examples = { @example (
								value = "// Simple syntax",
								isExecutable = false),
								@example (
										value = "create species: a_species number: an_int;",
										isExecutable = false), }) })
@validator (CreateValidator.class)
@serializer (CreateSerializer.class)
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class CreateStatement extends AbstractStatementSequence implements IStatement.Create {

	/**
	 * The Class CreateValidator.
	 */
	public static class CreateValidator implements IDescriptionValidator<IStatementDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
		 */
		@Override
		public void validate(final IStatementDescription cd) {
			final IExpression speciesExpr = cd.getFacetExpr(SPECIES);
			// If the species cannot be determined, issue an error and leave validation
			if (speciesExpr == null) {
				cd.error("The species to instantiate cannot be determined", UNKNOWN_SPECIES, SPECIES);
				return;
			}

			final ITypeDescription sd = speciesExpr.getGamlType().getDenotedSpecies();
			if (!(sd instanceof ISpeciesDescription spec)) {
				cd.error("The species to instantiate cannot be determined", UNKNOWN_SPECIES, SPECIES);
				return;
			}

			if (speciesExpr instanceof IExpression.Species) {
				final boolean abs = spec.isAbstract();
				final boolean mir = spec.isMirror();
				final boolean gri = spec.isGrid();
				if (abs || mir || gri) {
					final String p = abs ? "abstract" : mir ? "a mirror" : gri ? "a grid" : "";
					cd.error(spec.getName() + " is " + p + " and cannot be instantiated", WRONG_TYPE, SPECIES);
					return;
				}
			} else if (!(sd instanceof IModelDescription)) {
				cd.info("The actual species will be determined at runtime. This can lead to errors if it cannot be instantiated",
						WRONG_TYPE, SPECIES);
			}
			final ITypeDescription callerSpecies = cd.getTypeContext();
			if (sd instanceof IModelDescription && !(callerSpecies instanceof IExperimentDescription)) {
				cd.error("Simulations can only be created within experiments", WRONG_CONTEXT, SPECIES);
				return;
			}

			final ITypeDescription macro = spec.getMacroSpecies();
			if (macro == null && !(sd instanceof IModelDescription)) {
				cd.error("The macro-species of " + speciesExpr + " cannot be determined");
				return;
				// hqnghi special case : create instances of model from
				// model
			}
			if (macro instanceof IModelDescription && callerSpecies instanceof IModelDescription) {

				// end-hqnghi
			} else if (macro instanceof ISpeciesDescription callerSd
					&& callerSpecies instanceof ISpeciesDescription callerSpeciesDesc && callerSpeciesDesc != macro
					&& !callerSpeciesDesc.hasMacroSpecies(callerSd) && !callerSpeciesDesc.hasParent(macro)) {
				cd.error("No instance of " + macro.getName() + " available for creating instances of " + sd.getName());
				return;
			}
			final IExpression exp = cd.getFacetExpr(FROM);
			if (exp != null) {
				final IType type = exp.getGamlType();
				boolean found = false;
				for (final IType delegateType : GamaAdditionRegistry.getCreateDelegateTypes()) {
					found = delegateType.isAssignableFrom(type);
					if (found) { break; }
				}
				if (!found) {
					cd.warning("Facet 'from' expects an expression with one of the following types: "
							+ GamaAdditionRegistry.getCreateDelegateTypes(), WRONG_TYPE, FROM);
				}
			}
			final Arguments facets = cd.getPassedArgs();
			facets.forEachFacet((s, e) -> {
				boolean error = !spec.isExperiment() && !spec.hasAttribute(s);
				if (error) {
					cd.error("Attribute " + s + " is not defined in species " + speciesExpr.getName(), UNKNOWN_VAR);
				}
				return !error;
			});

		}

	}

	/**
	 * The Class CreateSerializer.
	 */
	public static class CreateSerializer extends StatementSerializer {

		@Override
		protected void serializeArgs(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
			final IStatementDescription desc = (IStatementDescription) s;
			final Arguments args = desc.getPassedArgs();
			if (args == null || args.isEmpty()) return;
			sb.append("with: [");
			args.forEachFacet((name, exp) -> {
				sb.append(name).append("::").append(exp.serializeToGaml(false));
				sb.append(", ");
				return true;
			});
			sb.setLength(sb.length() - 2);
			sb.append("]");
		}
	}

	/**
	 * The init. Uses {@link AtomicReference} so that {@link #setFormalArgs(Arguments)} and
	 * {@link #fillWithUserInit(IScope, java.util.Map)} are safe when called from multiple threads simultaneously.
	 */
	private final AtomicReference<Arguments> init = new AtomicReference<>();

	/** The header. */
	private final IExpression from, number, species, header;

	/** The returns. */
	private final String returns;

	/** The sequence. */
	private final RemoteSequence sequence;

	/**
	 * Instantiates a new creates the statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public CreateStatement(final IDescription desc) {
		super(desc);
		returns = getLiteral(RETURNS);
		from = getFacet(FROM);
		number = getFacet(NUMBER);
		species = getFacet(SPECIES);
		header = getFacet(HEADER);
		sequence = new RemoteSequence(description);
		sequence.setName("commands of create ");
		setName("create");
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> com) {
		sequence.setChildren(com);
	}

	@Override
	public void enterScope(final IScope scope) {
		if (returns != null) { scope.addVarWithValue(returns, null); }
		super.enterScope(scope);
	}

	/**
	 * Find population.
	 *
	 * @param scope
	 *            the scope
	 * @return the i population
	 */
	protected IPopulation findPopulation(final IScope scope) {
		final IAgent executor = scope.getAgent();
		if (species == null) return executor.getPopulationFor(description.getTypeContext().getName());
		ISpecies s = Cast.asSpecies(scope, species.value(scope));
		if (s == null) {// A last attempt in order to fix #2466
			final String potentialSpeciesName = species.getDenotedType().getSpeciesName();
			if (potentialSpeciesName != null) { s = scope.getModel().getSpecies(potentialSpeciesName); }
		}
		if (s == null) throw GamaRuntimeException.error("No population of " + species.serializeToGaml(false)
				+ " is accessible in the context of " + executor + ".", scope);
		IPopulation pop = executor.getPopulationFor(s);
		// hqnghi population of micro-model's experiment is not exist, we
		// must create the new one
		if (pop == null && s instanceof IExperimentSpecies ep && executor instanceof IMacroAgent) {
			pop = ep.createPopulation(scope);
			final IScope sc = ep.getExperimentScope();
			pop.initializeFor(sc);
			((IMacroAgent) executor).addExternMicroPopulation(
					s.getDescription().getModelDescription().getAlias() + "." + s.getName(), pop);
		}
		// end-hqnghi
		return pop;
	}

	@Override
	public IList<? extends IAgent> privateExecuteIn(final IScope scope) throws GamaRuntimeException {

		// First, we compute the number of agents to create
		final Integer max = number == null ? null : Cast.asInt(scope, number.value(scope));
		if (from == null && max != null && max <= 0) return GamaListFactory.getEmptyList();

		// Next, we compute the species to instantiate
		final IPopulation pop = findPopulation(scope);
		// A check is made in order to address issues #2621 and #2611
		if (pop == null || pop.getSpecies() == null)
			throw GamaRuntimeException.error("Impossible to determine the species of the agents to create", scope);
		checkPopulationValidity(pop, scope);

		// We grab whatever initial values are defined (from CSV, GIS, or user)
		final List<Map<String, Object>> inits = GamaListFactory.create(Types.MAP, max == null ? 10 : max);
		final Object source = getSource(scope);
		IList<? extends IAgent> agents = null;
		for (final ICreateDelegate delegate : GamaAdditionRegistry.getCreateDelegates()) {
			if (delegate.acceptSource(scope, source)) {
				delegate.createFrom(scope, inits, max, source, init.get(), this);
				if (delegate.handlesCreation()) { agents = delegate.createAgents(scope, pop, inits, this, sequence); }
				break;
			}
		}
		// and we create and return the agent(s)
		if (agents == null) { agents = createAgents(scope, pop, inits); }
		if (returns != null) { scope.setVarValue(returns, agents); }
		return agents;
	}

	/**
	 * A check made in order to address issues #2621 and #2611
	 *
	 * @param pop
	 * @param scope
	 * @throws GamaRuntimeException
	 */
	protected void checkPopulationValidity(final IPopulation pop, final IScope scope) throws GamaRuntimeException {
		if (pop instanceof IPopulation.Simulation && !(scope.getAgent() instanceof IExperimentAgent))
			throw GamaRuntimeException.error("Simulations can only be created within experiments", scope);
		final ISpeciesDescription sd = pop.getSpecies().getDescription();
		final String error =
				sd.isAbstract() ? "abstract" : sd.isMirror() ? "a mirror" : /** see #4 sd.isBuiltIn() ? "built-in" : **/
						sd.isGrid() ? "a grid" : null;
		if (error != null)
			throw GamaRuntimeException.error(sd.getName() + "is " + error + " and cannot be instantiated.", scope);
	}

	/**
	 * Gets the source.
	 *
	 * @param scope
	 *            the scope
	 * @return the source
	 */
	private Object getSource(final IScope scope) {
		Object source = from == null ? null : from.value(scope);
		// if (source instanceof String) {
		// source = Files.from(scope, (String) source);
		// } else
		if (source instanceof IShape) { source = GamaListFactory.wrap(Types.GEOMETRY, (IShape) source); }
		return source;
	}

	/**
	 * Creates the agents.
	 *
	 * @param scope
	 *            the scope
	 * @param population
	 *            the population
	 * @param inits
	 *            the inits
	 * @return the i list<? extends I agent>
	 */
	public IList<? extends IAgent> createAgents(final IScope scope, final IPopulation<? extends IAgent> population,
			final List<Map<String, Object>> inits) {
		if (population == null) return GamaListFactory.getEmptyList();
		// final boolean hasSequence = sequence != null && !sequence.isEmpty();
		boolean shouldBeScheduled = false;
		// If we create simulations within a single experiment, we must schedule
		// them
		if (population.getHost() instanceof IExperimentAgent) {
			final IExperimentAgent exp = (IExperimentAgent) population.getHost();
			if (exp.isScheduled()) { shouldBeScheduled = true; }
		}
		// As we are in the create statement, the agents are not restored
		final IList<? extends IAgent> list =
				population.createAgents(scope, inits.size(), inits, false, shouldBeScheduled, sequence);

		// hqnghi in case of creating experiment of micro-models, we must
		// implicitely initialize it and its simulation output
		if (population instanceof IPopulation.Experiment pop) {
			population.setHost(scope.getExperiment());
			for (final IExperimentAgent a : pop) {
				a._init_(scope);
				final ISimulationAgent sim = a.getSimulation();
				sim.adoptTopologyOf(scope.getSimulation());

				if (!sim.getScheduled()) { sim._init_(sim.getScope()); }
				if (sim.getOutputManager() != null) { sim.getOutputManager().init(sim.getScope()); }
			}
		}
		// end-hqnghi
		return list;
	}

	/**
	 * Fill with user init.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 */
	// TODO Call it before calling the ICreateDelegate createFrom method !
	@Override
	public void fillWithUserInit(final IScope scope, final Map values) {
		final Arguments currentInit = init.get();
		if (currentInit == null) return;
		scope.pushReadAttributes(values);
		try {
			currentInit.forEachArgument((k, v) -> {
				values.put(k, v.getExpression().value(scope));
				return true;
			});
		} finally {
			scope.popReadAttributes();
		}
	}

	@Override
	public void setFormalArgs(final Arguments args) {
		init.set(args);
	}

	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {}

	/**
	 * @return
	 */
	@Override
	public IExpression getHeader() { return header; }

	@Override
	public void dispose() {
		final Arguments currentInit = init.getAndSet(null);
		if (currentInit != null) { currentInit.dispose(); }
		sequence.dispose();
		super.dispose();
	}

}