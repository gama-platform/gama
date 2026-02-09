/*******************************************************************************************************
 *
 * IPopulationFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.agent;

import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * A factory for creating IPopulation objects.
 */
public interface IPopulationFactory {

	/**
	 * Creates a new IPopulation object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the gaml agent
	 * @param species
	 *            the micro spec
	 * @return the i population<? extends I agent>
	 */
	default <E extends IAgent> IPopulation<E> createPopulation(final IScope scope, final IMacroAgent host,
			final ISpecies species) {
		if (species.isGrid()) return createGridPopulation(scope, host, species);
		return createRegularPopulation(scope, host, species);
	}

	/**
	 * Creates a new IPopulation object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the host
	 * @param species
	 *            the species
	 * @return the i population<? extends I agent>
	 */
	<E extends IAgent> IPopulation<E> createRegularPopulation(IScope scope, IMacroAgent host, ISpecies species);

	/**
	 * Creates a new IPopulation object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the host
	 * @param species
	 *            the species
	 * @return the i population<? extends I agent>
	 */
	<E extends IAgent> IPopulation<E> createGridPopulation(IScope scope, IMacroAgent host, ISpecies species);

	/**
	 * Creates a new IPopulation object.
	 *
	 * @param <E>
	 *            the element type
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @return the i population< e>
	 */
	IPopulation.Experiment createExperimentPopulation(IScope scope, IExperimentSpecies species);
}
