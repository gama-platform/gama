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

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.agent.IPopulationSet;
import gama.api.kernel.skill.IArchitecture;
import gama.api.kernel.skill.ISkill;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IExperimentDisplayable;

/**
 * Written by drogoul Modified on 25 avr. 2010
 *
 * @todo Description
 *
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
		extends ISymbol, IContainer.Addressable<Integer, IAgent, Integer, IAgent>, IPopulationSet<IAgent> {

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
	 * Extends species.
	 *
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	boolean extendsSpecies(final ISpecies s);

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

	IList<ISpecies> getSubSpecies(IScope scope);

	/**
	 * Gets the sub species names.
	 *
	 * @param scope
	 *            the scope
	 * @return the sub species names
	 */
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
	IStatement.WithArgs getAction(final String name);

	/**
	 * Gets the action names.
	 *
	 * @param scope
	 *            the scope
	 * @return the action names
	 */
	@getter (ACTIONS)
	@doc ("retuns the list of actions defined in this species (incl. the ones inherited from its parent)")
	IList<String> getActionNames(final IScope scope);

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	Collection<? extends IStatement> getActions();

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
	String getParentName();

	/**
	 * Gets the var.
	 *
	 * @param n
	 *            the n
	 * @return the var
	 */
	IVariable getVar(final String n);

	/**
	 * Gets the var names.
	 *
	 * @return the var names
	 */
	Collection<String> getVarNames();

	/**
	 * Similar to getVarNames(), but returns a correctly initialized IList of attribute names
	 *
	 * @param scope
	 * @return the list of all the attributes defined in this species
	 */
	@getter (IKeyword.ATTRIBUTES)
	@doc ("retuns the list of attributes defined in this species (incl. the ones inherited from its parent)")
	IList<String> getAttributeNames(final IScope scope);

	/**
	 * Gets the vars.
	 *
	 * @return the vars
	 */
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
	boolean hasVar(final String name);

	/**
	 * Sets the macro species.
	 *
	 * @param macroSpecies
	 *            the new macro species
	 */
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

}