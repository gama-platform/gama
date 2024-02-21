/*******************************************************************************************************
 *
 * In.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.metamodel.topology.filter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Iterables;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.population.IPopulationSet;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.graph.ISpatialGraph;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.types.Types;

/**
 * The Class In.
 */
@SuppressWarnings ({ "rawtypes" })
public abstract class In implements IAgentFilter {

	/**
	 * List.
	 *
	 * @param scope the scope
	 * @param targets the targets
	 * @return the i agent filter
	 */
	public static IAgentFilter list(final IScope scope, final IContainer<?, ? extends IShape> targets) {
		if (targets.isEmpty(scope)) { return null; }
		if (targets instanceof IPopulationSet) { return (IPopulationSet) targets; }
		final ISpecies species = targets.getGamlType().getContentType().isAgentType()
				? Cast.asSpecies(scope, targets.getGamlType().getContentType().getSpeciesName()) : null;
		return new InList(targets.listValue(scope, Types.NO_TYPE, false), species);
	}

	/**
	 * Edges of.
	 *
	 * @param graph the graph
	 * @return the i agent filter
	 */
	public static IAgentFilter edgesOf(final ISpatialGraph graph) {
		return graph;
	}

	/**
	 * The Class InList.
	 */
	private static class InList extends In {

		/** The agents. */
		final Set<IShape> agents;
		
		/** The species. */
		ISpecies species;

		/**
		 * Instantiates a new in list.
		 *
		 * @param list the list
		 * @param species the species
		 */
		InList(final IList<? extends IShape> list, final ISpecies species) {
			agents = new LinkedHashSet<>(list);
			this.species = species;
		}

		@Override
		public boolean accept(final IScope scope, final IShape source, final IShape a) {
			return (source == null || a.getGeometry() != source.getGeometry()) && agents.contains(a);
		}

		@Override
		public boolean hasAgentList() {
			return true;
		}

		@Override
		public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
			return GamaListFactory.createWithoutCasting(Types.AGENT, Iterables.filter(agents, IAgent.class));
		}

		@Override
		public ISpecies getSpecies() {
			return species;
		}

		@Override
		public IPopulation<? extends IAgent> getPopulation(final IScope scope) {
			if (species == null) { return null; }
			return scope.getSimulation().getPopulationFor(species);
		}

		@Override
		public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
			agents.remove(source);
			results.retainAll(agents);
		}

	}

}
