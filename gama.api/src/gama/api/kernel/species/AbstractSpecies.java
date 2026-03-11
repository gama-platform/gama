/*******************************************************************************************************
 *
 * AbstractSpecies.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
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

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.statements.IStatement.WithArgs;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGraphAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.skill.IArchitecture;
import gama.api.kernel.skill.ISkill;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.ui.IExperimentDisplayable;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

/**
 * Abstract base class for all species implementations in GAMA.
 * 
 * <p>
 * A species in GAML represents a type of agent. This abstract class provides the core implementation for managing
 * species properties including variables, actions, behaviors, aspects, micro-species, and the control architecture.
 * Species can be organized hierarchically through inheritance (parent/child) and composition (macro/micro).
 * </p>
 * 
 * <h2>Key Responsibilities</h2>
 * <ul>
 * <li><b>Variables:</b> Manages attributes that define the state of agents of this species</li>
 * <li><b>Actions:</b> Executable operations that can be invoked on agents</li>
 * <li><b>Behaviors:</b> Scheduled statements (reflexes, init, etc.) executed by agents</li>
 * <li><b>Aspects:</b> Graphical representations for agent visualization</li>
 * <li><b>Micro-species:</b> Species contained within this species' agents</li>
 * <li><b>Architecture:</b> Control structure governing agent behavior (FSM, BDI, etc.)</li>
 * </ul>
 * 
 * <h2>Species Hierarchy</h2>
 * <p>
 * Species can be organized in two types of hierarchies:
 * </p>
 * <ul>
 * <li><b>Inheritance (parent/child):</b> A species can extend another species, inheriting its attributes and
 * behaviors</li>
 * <li><b>Composition (macro/micro):</b> A species can contain other species (micro-species), creating nested
 * agent populations</li>
 * </ul>
 * 
 * <h2>Grid and Graph Specializations</h2>
 * <p>
 * The class handles two special types of species:
 * </p>
 * <ul>
 * <li><b>Grid species:</b> Agents organized in a spatial grid topology</li>
 * <li><b>Graph species:</b> Agents representing nodes in a graph structure</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <p>
 * In GAML, species are defined declaratively:
 * </p>
 * 
 * <pre>
 * {@code
 * species predator skills: [moving] {
 *     float energy <- 100.0;
 *     
 *     reflex hunt when: energy > 50 {
 *         // behavior code
 *     }
 *     
 *     action eat (prey target) {
 *         energy <- energy + target.energy;
 *     }
 *     
 *     aspect default {
 *         draw circle(2) color: #red;
 *     }
 * }
 * }
 * </pre>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see ISpecies
 * @see Symbol
 * @see IPopulation
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public abstract class AbstractSpecies extends Symbol implements ISpecies {

	static {
		DEBUG.OFF();
	}

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
	 * Constructs a new species from its description.
	 * 
	 * <p>
	 * This constructor initializes the species by:
	 * </p>
	 * <ul>
	 * <li>Setting the species name from the description</li>
	 * <li>Determining if the species is a grid or graph based on the keyword and Java base class</li>
	 * <li>Creating and initializing the control architecture instance</li>
	 * </ul>
	 * 
	 * @param description
	 *            the species description containing all metadata from GAML compilation
	 */
	public AbstractSpecies(final IDescription description) {
		super(description);
		setName(description.getName());
		isGrid = IKeyword.GRID.equals(getKeyword());
		isGraph = IGraphAgent.class.isAssignableFrom(((ISpeciesDescription) description).getJavaBase());
		control = getDescription().getControl().createArchitectureInstance();
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
	 * Temporary actions are dynamically added actions that can be removed later. They are typically used for
	 * on-the-fly action creation during runtime, such as when evaluating expressions that require temporary
	 * executable code.
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
	public boolean extendsSpecies(final ISpecies s) {
		final ISpecies parent = getParentSpecies();
		if (parent == null) return false;
		if (parent == s) return true;
		return parent.extendsSpecies(s);
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
	public Collection<? extends IStatement> getActions() { return actions.values(); }

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
	 * Skills provide additional capabilities to agents. This method retrieves the singleton instance of a skill that
	 * is either the control architecture or declared in the species (or its parents). The search proceeds as follows:
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

}