/*******************************************************************************************************
 *
 * IPopulationSet.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import java.util.Collection;

import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IContainer;
import gama.api.utils.interfaces.IAgentFilter;
import one.util.streamex.StreamEx;

/**
 * Class IPopulationSet. An interface common to ISpecies, IPopulation and MetaPopulation
 *
 * @author drogoul
 * @since 9 déc. 2013
 *
 */
public interface IPopulationSet<T extends IAgent> extends IContainer<Integer, T>, IAgentFilter {

	/**
	 * Gets the populations.
	 *
	 * @param scope
	 *            the scope
	 * @return the populations
	 */
	Collection<? extends IPopulation<? extends IAgent>> getPopulations(IScope scope);

	/**
	 * Stream.
	 *
	 * @param scope
	 *            the scope
	 * @return the stream ex
	 */
	@Override
	StreamEx<T> stream(final IScope scope);

}
