/*******************************************************************************************************
 *
 * DefaultPopulationFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.population;

import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.grid.GamaSpatialMatrix;
import gama.core.metamodel.topology.grid.GridPopulation;
import gama.core.metamodel.topology.grid.IGridAgent;
import gama.core.runtime.IScope;
import gama.gaml.species.ISpecies;

/**
 * A factory for creating DefaultPopulation objects.
 */
public class DefaultPopulationFactory implements IPopulationFactory {

	@Override
	public <E extends IAgent> IPopulation<E> createRegularPopulation(final IScope scope, final IMacroAgent host,
			final ISpecies species) {
		return new GamaPopulation<>(host, species);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IPopulation<IGridAgent> createGridPopulation(final IScope scope, final IMacroAgent host,
			final ISpecies species) {
		final ITopology t = GridPopulation.buildGridTopology(scope, species, host);
		final GamaSpatialMatrix m = (GamaSpatialMatrix) t.getPlaces();
		return new GridPopulation(m, t, host, species);
	}

}
