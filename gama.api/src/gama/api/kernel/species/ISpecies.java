/*******************************************************************************************************
 *
 * ISpecies.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.species;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.ITypeProvider;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.agent.IPopulationSet;
import gama.api.kernel.object.IClass;
import gama.api.kernel.skill.IArchitecture;
import gama.api.kernel.skill.ISkill;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.ui.IExperimentDisplayable;

/**
 * The Interface ISpecies.
 *
 * <p>
 * Represents a species definition in GAMA - the fundamental organizing concept for agents. A species is like a class in
 * object-oriented programming: it defines the structure (attributes), behaviors (actions, reflexes), appearance
 * (aspects), and control logic (architecture) for a category of agents. Each agent belongs to exactly one species.
 * </p>
 *
 * <h3>Core Concept</h3>
 * <p>
 * In GAMA, a species is:
 * </p>
 * <ul>
 * <li><b>A Template:</b> Defines what attributes and behaviors agents of this type have</li>
 * <li><b>A Population Container:</b> Groups all agents of the same type</li>
 * <li><b>A Namespace:</b> Scopes variables, actions, and aspects</li>
 * <li><b>A Type:</b> Used for type checking and declarations</li>
 * <li><b>An Inheritance Hierarchy:</b> Can extend other species</li>
 * </ul>
 *
 * <h3>Species Components</h3>
 * <table border="1">
 * <tr>
 * <th>Component</th>
 * <th>Purpose</th>
 * <th>Example</th>
 * </tr>
 * <tr>
 * <td>Attributes (Variables)</td>
 * <td>Agent state</td>
 * <td>int age, float energy</td>
 * </tr>
 * <tr>
 * <td>Actions</td>
 * <td>Callable behaviors</td>
 * <td>action eat, action move_to</td>
 * </tr>
 * <tr>
 * <td>Reflexes</td>
 * <td>Automatic behaviors</td>
 * <td>reflex update_energy</td>
 * </tr>
 * <tr>
 * <td>Aspects</td>
 * <td>Visual representation</td>
 * <td>aspect default, aspect 3d</td>
 * </tr>
 * <tr>
 * <td>Architecture</td>
 * <td>Behavior control</td>
 * <td>control: fsm</td>
 * </tr>
 * <tr>
 * <td>Skills</td>
 * <td>Reusable capabilities</td>
 * <td>skills: [moving]</td>
 * </tr>
 * </table>
 *
 * <h3>Usage in GAML</h3>
 *
 * <h4>1. Basic Species Definition</h4>
 *
 * <pre>
 * <code>
 * species person {
 *     // Attributes
 *     int age <- rnd(80);
 *     float energy <- 100.0;
 *
 *     // Reflex (automatic behavior)
 *     reflex age_and_tire {
 *         age <- age + 1;
 *         energy <- energy - 0.1;
 *     }
 *
 *     // Action (callable behavior)
 *     action eat(float amount) {
 *         energy <- min(100.0, energy + amount);
 *     }
 *
 *     // Aspect (visualization)
 *     aspect default {
 *         draw circle(1) color: #blue;
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h4>2. Species Inheritance</h4>
 *
 * <pre>
 * <code>
 * species animal {
 *     float energy <- 100.0;
 *
 *     action move {
 *         energy <- energy - 1.0;
 *     }
 * }
 *
 * species predator parent: animal {
 *     // Inherits 'energy' and 'move'
 *     float hunt_skill <- rnd(1.0);
 *
 *     action hunt(animal prey) {
 *         // Predator-specific behavior
 *     }
 * }
 *
 * species prey parent: animal {
 *     // Also inherits from animal
 *     float flee_speed <- rnd(10.0);
 * }
 * </code>
 * </pre>
 *
 * <h4>3. Multi-Level Species (Micro-Species)</h4>
 *
 * <pre>
 * <code>
 * species city {
 *     // Micro-species defined inside city
 *     species building {
 *         int floors <- rnd(1, 20);
 *
 *         // Nested micro-species
 *         species room {
 *             string type <- one_of(["office", "apartment"]);
 *         }
 *     }
 *
 *     init {
 *         create building number: 50 {
 *             create room number: floors * 4;
 *         }
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h4>4. Species with Skills and Architecture</h4>
 *
 * <pre>
 * <code>
 * species robot skills: [moving] control: fsm {
 *     // Skills provide attributes and actions
 *     float speed <- 5.0;  // From 'moving' skill
 *
 *     // FSM architecture uses states instead of reflexes
 *     state searching initial: true {
 *         transition to: moving when: target != nil;
 *     }
 *
 *     state moving {
 *         do goto target: target;  // Action from 'moving' skill
 *         transition to: working when: location = target;
 *     }
 *
 *     state working {
 *         do work;
 *         transition to: searching when: task_done;
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h4>5. Grid Species</h4>
 *
 * <pre>
 * <code>
 * grid cell width: 100 height: 100 {
 *     // Special grid species
 *     rgb color <- #white;
 *     float value <- 0.0;
 *
 *     reflex diffuse {
 *         value <- mean(neighbors collect each.value);
 *     }
 *
 *     aspect default {
 *         draw shape color: rgb(255 * value, 0, 0);
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h3>Java Usage</h3>
 *
 * <pre>
 * <code>
 * ISpecies species = ...;
 *
 * // Basic information
 * String name = species.getName();
 * ISpecies parent = species.getParentSpecies();
 * boolean isGrid = species.isGrid();
 *
 * // Hierarchy
 * List&lt;ISpecies&gt; hierarchy = species.getSelfWithParents();
 * IList&lt;ISpecies&gt; subSpecies = species.getSubSpecies(scope);
 * boolean extends = species.extendsSpecies(otherSpecies);
 *
 * // Micro-species
 * IList&lt;ISpecies&gt; microSpecies = species.getMicroSpecies();
 * ISpecies microSpec = species.getMicroSpecies("building");
 * boolean hasMicro = species.hasMicroSpecies();
 *
 * // Attributes (variables)
 * IVariable var = species.getVar("age");
 * Collection&lt;String&gt; varNames = species.getVarNames();
 * IList&lt;String&gt; attributes = species.getAttributeNames(scope);
 * boolean hasVar = species.hasVar("energy");
 *
 * // Actions
 * IStatement.WithArgs action = species.getAction("eat");
 * IList&lt;String&gt; actionNames = species.getActionNames(scope);
 * Collection&lt;? extends IStatement&gt; actions = species.getActions();
 *
 * // Aspects
 * IExecutable aspect = species.getAspect("default");
 * IList&lt;String&gt; aspectNames = species.getAspectNames();
 * boolean hasAspect = species.hasAspect("3d");
 *
 * // Architecture and skills
 * IArchitecture arch = species.getArchitecture();
 * String archName = species.getArchitectureName();
 *
 * // Population
 * IPopulation&lt;? extends IAgent&gt; pop = species.getPopulation(scope);
 *
 * // Scheduling
 * IExpression frequency = species.getFrequency();
 * IExpression schedule = species.getSchedule();
 * IExpression concurrency = species.getConcurrency();
 * </code>
 * </pre>
 *
 * <h3>Species Hierarchy</h3>
 * <ul>
 * <li><b>agent:</b> The root species (all species inherit from agent)</li>
 * <li><b>experiment:</b> Special species for experiments</li>
 * <li><b>model:</b> The global/world species</li>
 * <li><b>User-defined:</b> All species defined in GAML models</li>
 * </ul>
 *
 * <h3>Special Species Types</h3>
 * <ul>
 * <li><b>Grid Species:</b> Declared with 'grid', agents arranged in 2D matrix</li>
 * <li><b>Graph Species:</b> Agents that can be nodes/edges in graphs</li>
 * <li><b>Mirror Species:</b> Mirror external data sources</li>
 * <li><b>Micro-Species:</b> Nested species that exist within macro-agents</li>
 * </ul>
 *
 * <h3>Introspection</h3>
 * <p>
 * Species are themselves accessible as GAML values:
 * </p>
 *
 * <pre>
 * <code>
 * global {
 *     init {
 *         // Access species as a value
 *         species person_spec <- person;
 *
 *         // Get species information
 *         write "Person has " + length(person_spec.attributes) + " attributes";
 *         write "Actions: " + person_spec.actions;
 *         write "Parent: " + person_spec.parent;
 *     }
 * }
 * </code>
 * </pre>
 *
 * <h3>Implementation Notes</h3>
 * <ul>
 * <li>Species implements ISymbol (has a description and belongs to a model)</li>
 * <li>Species implements IContainer.Addressable (agents indexed by integer)</li>
 * <li>Species implements IPopulationSet (collection of agents)</li>
 * <li>Each species has exactly one parent (except 'agent' which is root)</li>
 * <li>Species can have multiple sub-species (children)</li>
 * <li>Attributes, actions, and aspects are inherited from parent</li>
 * <li>Architecture is not inherited (must be explicitly set)</li>
 * </ul>
 *
 * @see IAgent
 * @see IPopulation
 * @see IArchitecture
 * @see ISkill
 * @see IVariable
 * @author drogoul
 * @since GAMA 1.0
 * @date 25 avr. 2010
 */
@vars ({ @variable (
		name = ISpecies.ACTIONS,
		type = IType.LIST,
		of = IType.STRING,
		doc = @doc ("A list of the names of the actions defined in this species")),
		@variable (
				name = ISpecies.ASPECTS,
				type = IType.LIST,
				of = IType.STRING,
				doc = @doc ("A list of the names of the aspects defined in this species")),
		@variable (
				name = IKeyword.ATTRIBUTES,
				type = IType.LIST,
				of = IType.STRING,
				doc = @doc ("A list of the names of the attributes of this species")),
		@variable (
				name = IKeyword.PARENT,
				type = IType.SPECIES,
				doc = @doc ("The parent (if any) of this species")),
		@variable (
				name = IKeyword.NAME,
				type = IType.STRING,
				doc = @doc ("The name of the species")),
		@variable (
				name = ISpecies.SUBSPECIES,
				type = IType.LIST,
				of = IType.SPECIES,
				doc = @doc ("A list of the names of subspecies of this species")),
		@variable (
				name = ISpecies.MICROSPECIES,
				type = IType.LIST,
				of = IType.STRING,
				doc = @doc ("A list of the names of the micro-species declared inside this species")),
		@variable (
				name = ISpecies.POPULATION,
				type = IType.LIST,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = @doc ("The population that corresponds to this species in an instance of its host")) })
public interface ISpecies
		extends IClass, IContainer.Addressable<Integer, IAgent, Integer, IAgent>, IPopulationSet<IAgent> {

	/** The step action name. */
	String stepActionName = "_step_";

	/** The init action name. */
	String initActionName = "_init_";

	/** The population. */
	String POPULATION = "population";

	/** The subspecies. */
	String SUBSPECIES = "subspecies";

	/** The microspecies. */
	String MICROSPECIES = "microspecies";

	/** The actions. */
	String ACTIONS = "actions";

	/** The aspects. */
	String ASPECTS = "aspects";

	/**
	 * Gets the frequency.
	 *
	 * @return the frequency
	 */
	IExpression getFrequency();

	/**
	 * Gets the schedule.
	 *
	 * @return the schedule
	 */
	IExpression getSchedule();

	/**
	 * Gets the concurrency.
	 *
	 * @return the concurrency
	 */
	IExpression getConcurrency();

	/**
	 * Checks if is grid.
	 *
	 * @return true, if is grid
	 */
	boolean isGrid();

	/**
	 * Checks if is graph.
	 *
	 * @return true, if is graph
	 */
	boolean isGraph();

	/**
	 * Return all the direct subspecies of this species, properly typed for GAMA
	 *
	 * @return
	 */

	@Override
	IList<ISpecies> getSubSpecies(IScope scope);

	/**
	 * Gets the sub species names.
	 *
	 * @param scope
	 *            the scope
	 * @return the sub species names
	 */
	@Override
	@SuppressWarnings ("unchecked")
	@getter (SUBSPECIES)
	@doc ("Returns all the direct subspecies names of this species")
	IList<String> getSubSpeciesNames(final IScope scope);

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	@getter (IKeyword.NAME)
	String getName();

	/**
	 * Returns all the micro-species. Micro-species includes: 1. the "direct" micro-species; 2. the micro-species of the
	 * parent-species.
	 *
	 * @return
	 */
	IList<ISpecies> getMicroSpecies();

	/**
	 * Returns a micro-species with the specified name or null otherwise.
	 *
	 * @param microSpeciesName
	 * @return a species or null
	 */
	ISpecies getMicroSpecies(String microSpeciesName);

	/**
	 * Verifies if this species has micro-species or not.
	 *
	 * @return true if this species has micro-species false otherwise
	 */
	boolean hasMicroSpecies();

	/**
	 * Verifies of the specified species is a micro-species of this species of not.
	 *
	 * @param species
	 * @return
	 */
	boolean containMicroSpecies(ISpecies species);

	/**
	 * Returns the parent species.
	 *
	 * @return
	 */
	@Override
	@getter (IKeyword.PARENT)
	@doc ("Returns the direct parent of the species. Experiments, models and species with no explicit parents will return nil")
	ISpecies getParentSpecies();

	/**
	 * Verifies that if this species is the peer species of other species.
	 *
	 * @param other
	 * @return
	 */
	boolean isPeer(ISpecies other);

	/**
	 * Gets the self with parents.
	 *
	 * @return the self with parents
	 */
	@Override
	List<ISpecies> getSelfWithParents();

	/**
	 * Gets the user commands.
	 *
	 * @return the user commands
	 */
	Collection<? extends IExperimentDisplayable> getUserCommands();

	/**
	 * Gets the statement.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @param name
	 *            the name
	 * @return the statement
	 */
	// Huynh Quang Nghi 29/01/13
	<T extends IStatement> T getStatement(Class<T> clazz, String name);

	/**
	 * Gets the action.
	 *
	 * @param name
	 *            the name
	 * @return the action
	 */
	@Override
	IStatement.WithArgs getAction(final String name);

	/**
	 * Gets the action names.
	 *
	 * @param scope
	 *            the scope
	 * @return the action names
	 */
	@Override
	@getter (ACTIONS)
	@doc ("retuns the list of actions defined in this species (incl. the ones inherited from its parent)")
	IList<String> getActionNames(final IScope scope);

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	@Override
	Collection<IStatement.Action> getActions();

	/**
	 * Gets the aspect.
	 *
	 * @param n
	 *            the n
	 * @return the aspect
	 */
	IExecutable getAspect(final String n);

	/**
	 * Gets the aspects.
	 *
	 * @return the aspects
	 */
	Collection<? extends IExecutable> getAspects();

	/**
	 * Gets the aspect names.
	 *
	 * @return the aspect names
	 */
	@getter (ASPECTS)
	@doc ("retuns the list of aspects defined in this species")
	IList<String> getAspectNames();

	/**
	 * Gets the architecture.
	 *
	 * @return the architecture
	 */
	IArchitecture getArchitecture();

	/**
	 * Gets the architecture name.
	 *
	 * @return the architecture name
	 */
	String getArchitectureName();

	/**
	 * Gets the macro species.
	 *
	 * @return the macro species
	 */
	ISpecies getMacroSpecies();

	/**
	 * Gets the parent name.
	 *
	 * @return the parent name
	 */
	@Override
	String getParentName();

	/**
	 * Gets the var.
	 *
	 * @param n
	 *            the n
	 * @return the var
	 */
	@Override
	IVariable getVar(final String n);

	/**
	 * Gets the var names.
	 *
	 * @return the var names
	 */
	@Override
	Collection<String> getVarNames();

	/**
	 * Similar to getVarNames(), but returns a correctly initialized IList of attribute names
	 *
	 * @param scope
	 * @return the list of all the attributes defined in this species
	 */
	@Override
	@getter (IKeyword.ATTRIBUTES)
	@doc ("retuns the list of attributes defined in this species (incl. the ones inherited from its parent)")
	IList<String> getAttributeNames(final IScope scope);

	/**
	 * Gets the vars.
	 *
	 * @return the vars
	 */
	@Override
	Collection<IVariable> getVars();

	/**
	 * Checks for aspect.
	 *
	 * @param n
	 *            the n
	 * @return true, if successful
	 */
	boolean hasAspect(final String n);

	/**
	 * Checks for var.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	@Override
	boolean hasVar(final String name);

	/**
	 * Sets the macro species.
	 *
	 * @param macroSpecies
	 *            the new macro species
	 */
	@Override
	void setMacroSpecies(final ISpecies macroSpecies);

	/**
	 * Checks if is mirror.
	 *
	 * @return true, if is mirror
	 */
	boolean isMirror();

	/**
	 * Implements skill.
	 *
	 * @param skill
	 *            the skill
	 * @return the boolean
	 */
	Boolean implementsSkill(String skill);

	/**
	 * Gets the micro species names.
	 *
	 * @return the micro species names
	 */
	@getter (MICROSPECIES)
	@doc ("Returns all the direct microspecies names of this species")
	Collection<String> getMicroSpeciesNames();

	/**
	 * Returns the population of agents that belong to this species and that are hosted in the same host
	 *
	 * @param scope
	 * @return
	 *
	 */
	@Override
	@getter (POPULATION)
	@doc ("Returns the population of agents that belong to this species")
	IPopulation<? extends IAgent> getPopulation(IScope scope);

	/**
	 * Adds the temporary action.
	 *
	 * @param a
	 *            the a
	 */
	void addTemporaryAction(IStatement a);

	/**
	 * Gets the behaviors.
	 *
	 * @return the behaviors
	 */
	Collection<IStatement> getBehaviors();

	/**
	 * Removes the temporary action.
	 */
	void removeTemporaryAction();

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Override
	ISpeciesDescription getDescription();

	/**
	 * @param skillClass
	 * @return
	 */
	ISkill getSkillInstanceFor(Class skillClass);

	/**
	 * Sets the enclosing.
	 *
	 * @param enclosing
	 *            the new enclosing
	 */
	@Override
	default void setEnclosing(final ISymbol enclosing) {
		if (isBuiltIn()) return;
		if (enclosing instanceof ISpecies s) { setMacroSpecies(s); }
	}

	/**
	 * Checks if is built in.
	 *
	 * @return true, if is built in
	 */
	default boolean isBuiltIn() { return getDescription() != null && getDescription().isBuiltIn(); }

	/**
	 * Creates the instance.
	 *
	 * @param scope
	 *            the scope
	 * @param args
	 *            the args
	 * @return the i agent
	 */
	@Override
	default IAgent createInstance(final IScope scope, final Map<String, Object> args) {
		return getPopulation(scope).createOneAgent(scope, args);
	}

	/**
	 * Checks if is class.
	 *
	 * @return true, if is class
	 */
	@Override
	default boolean isClass() { return false; }

	/**
	 * Checks if is species.
	 *
	 * @return true, if is species
	 */
	@Override
	default boolean isSpecies() { return true; }

}