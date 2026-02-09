/*******************************************************************************************************
 *
 * Different.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.topology.filter;

import java.util.Collection;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IShape;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IAgentFilter;

/**
 * The Class Different.
 */
public class Different implements IAgentFilter {

	/** The Constant instance. */
	private static final Different instance = new Different();

	/**
	 * With.
	 *
	 * @return the different
	 */
	public static Different with() {
		return instance;
	}

	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		return a.getGeometry() != source.getGeometry();
	}

	/**
	 * @see gama.api.utils.IAgentFilter#getShapes()
	 */
	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		return GamaListFactory.getEmptyList();
	}

	@Override
	public ISpecies getSpecies() {
		return null;
	}

	@Override
	public IPopulation<? extends IAgent> getPopulation(final IScope scope) {
		return null;
	}

	@Override
	public boolean hasAgentList() {
		return false;
	}

	/**
	 * Method filter()
	 *
	 * @see gama.api.utils.IAgentFilter#filter(java.util.Collection)
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> internal_results) {
		internal_results.remove(source);
	}

}