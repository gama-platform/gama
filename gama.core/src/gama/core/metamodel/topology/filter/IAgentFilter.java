/*******************************************************************************************************
 *
 * IAgentFilter.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.metamodel.topology.filter;

import java.util.Collection;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.IContainer;
import gama.gaml.species.ISpecies;

/**
 * The Interface IAgentFilter.
 */
public interface IAgentFilter {

	/**
	 * Checks for agent list.
	 *
	 * @return true, if successful
	 */
	boolean hasAgentList();

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	ISpecies getSpecies();

	/**
	 * Gets the population.
	 *
	 * @param scope the scope
	 * @return the population
	 */
	IPopulation<? extends IAgent> getPopulation(IScope scope);

	/**
	 * Gets the agents.
	 *
	 * @param scope the scope
	 * @return the agents
	 */
	IContainer<?, ? extends IAgent> getAgents(IScope scope);

	/**
	 * Accept.
	 *
	 * @param scope the scope
	 * @param source the source
	 * @param a the a
	 * @return true, if successful
	 */
	boolean accept(IScope scope, IShape source, IShape a);

	/**
	 * Filter.
	 *
	 * @param scope the scope
	 * @param source the source
	 * @param results the results
	 */
	void filter(IScope scope, IShape source, Collection<? extends IShape> results);

}