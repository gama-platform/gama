/*******************************************************************************************************
 *
 * ISpecies.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.species;

import java.util.Collection;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.IPopulationSet;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import gama.gaml.architecture.IArchitecture;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.statements.ActionStatement;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.IStatement;
import gama.gaml.statements.UserCommandStatement;
import gama.gaml.types.IType;

/**
 * The {@code ISpecies} interface represents a species in the GAMA modeling platform.
 * It defines the structure and behavior of species, including their aspects, micro-species,
 * population management, and associated actions.
 *
 * Written by drogoul Modified on 25 avr. 2010
 *
 * @todo Description
 *
 */
@vars ({ @variable (
		name = ISpecies.ASPECTS,
		type = IType.LIST,
		of = IType.STRING,
		doc = @doc ("A list of the names of the aspects defined in this species")),
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
public interface ISpecies extends IClass, IPopulationSet<IAgent> {

	/** The step action name. */
	String stepActionName = "_step_";

	/** The init action name. */
	String initActionName = "_init_";

	/** The population. */
	String POPULATION = "population";

	/** The microspecies. */
	String MICROSPECIES = "microspecies";

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
	 * Gets the user commands.
	 *
	 * @return the user commands
	 */
	Collection<UserCommandStatement> getUserCommands();

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
	 * Checks for aspect.
	 *
	 * @param n
	 *            the n
	 * @return true, if successful
	 */
	boolean hasAspect(final String n);

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
	void addTemporaryAction(ActionStatement a);

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
	SpeciesDescription getDescription();

}
