/*******************************************************************************************************
 *
 * InternalPopulationFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.population;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.agent.IPopulationFactory;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.topology.GamaTopologyFactory;
import gama.api.types.topology.ITopology;
import gama.core.topology.grid.GamaSpatialMatrix;
import gama.core.topology.grid.GridPopulation;

/**
 * A factory for creating Population objects.
 */
public class InternalPopulationFactory implements IPopulationFactory {

	@Override
	public <E extends IAgent> IPopulation<E> createRegularPopulation(final IScope scope, final IMacroAgent host,
			final ISpecies species) {
		return new GamaPopulation<>(host, species);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public IPopulation<IAgent> createGridPopulation(final IScope scope, final IMacroAgent host,
			final ISpecies species) {
		final ITopology t = GamaTopologyFactory.createGrid(scope, species, host);
		final GamaSpatialMatrix m = (GamaSpatialMatrix) t.getPlaces();
		return new GridPopulation(m, t, host, species);
	}

	/**
	 * Creates a new InternalPopulation object.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @return the experiment
	 */
	@Override
	public IPopulation.Experiment createExperimentPopulation(final IScope scope, final IExperimentSpecies species) {
		return species.createPopulation(scope);
	}

}
