/*******************************************************************************************************
 *
 * ICreateDelegate.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.interfaces;

import java.util.List;
import java.util.Map;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.CreateStatement;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IType;

/**
 * Class ICreateDelegate. Allows to create agents from other sources than the ones used in the tradition 'create'
 * statement
 *
 * @author drogoul
 * @since 27 mai 2015
 *
 */
public interface ICreateDelegate {

	/**
	 * Returns whether or not this delegate accepts to create agents from this source.
	 *
	 * @param scope
	 *            TODO
	 * @param source
	 *
	 * @return
	 */

	boolean acceptSource(IScope scope, Object source);

	/**
	 * Fills the list of maps with the initial values read from the source. Returns true if all the inits have been
	 * correctly filled
	 *
	 * @param scope
	 * @param inits
	 * @param max
	 *            can be null (in that case, the maximum number of agents to create is ignored)
	 * @param source
	 * @return
	 */

	boolean createFrom(IScope scope, List<Map<String, Object>> inits, Integer max, Object source, Arguments init,
			CreateStatement statement);

	/**
	 * Returns the type expected in the 'from:' facet of 'create' statement. Should not be null and should be different
	 * from IType.NO_TYPE (in order to be able to check the validity of create statements at compile time)
	 *
	 * @return a GAML type representing the type of the source expected by this ICreateDelegate
	 */
	IType<?> fromFacetType();

	/**
	 * Handles creation. Returns whether or not this delegate handles the complete creation of the agents, in which
	 * #createAgents() will be called on it
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if successful
	 * @date 8 août 2023
	 */
	default boolean handlesCreation() {
		return false;
	}

	/**
	 * Creation of the agents. This method will only be called if {@link #handlesCreation()} returns true. It is
	 * supposed to completely handle the creation of the agents instead of CreateStatement, but it can be used to call
	 * back {@link CreateStatement#createAgents(IScope, IPopulation, List)} and add some important initialisations, etc.
	 *
	 * @param scope
	 *            the scope
	 * @param population
	 *            the population
	 * @param inits
	 *            the inits
	 * @return the i list<? extends I agent>
	 */
	default IList<? extends IAgent> createAgents(final IScope scope, final IPopulation<? extends IAgent> population,
			final List<Map<String, Object>> inits, final CreateStatement statement, final RemoteSequence sequence) {
		return null;
	}

}
