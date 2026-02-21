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
 * The Interface IPopulationFactory.
 * 
 * <p>
 * A factory interface for creating different types of agent populations in GAMA. This factory provides methods to
 * create regular populations, grid populations, and experiment populations based on species specifications.
 * </p>
 * 
 * <h3>Purpose</h3>
 * <p>
 * The factory pattern is used to:
 * </p>
 * <ul>
 * <li>Abstract the creation logic for different population types</li>
 * <li>Allow custom population implementations to be plugged in</li>
 * <li>Centralize population creation and configuration</li>
 * <li>Support different population strategies (regular, grid, experiment)</li>
 * </ul>
 * 
 * <h3>Population Types</h3>
 * <ul>
 * <li><b>Regular Population:</b> Standard collection of agents with no special structure</li>
 * <li><b>Grid Population:</b> Specialized population for grid-based agents with spatial indexing</li>
 * <li><b>Experiment Population:</b> Population managing experiment instances</li>
 * </ul>
 * 
 * <h3>Java Usage</h3>
 * 
 * <pre>
 * <code>
 * // Get the factory
 * IPopulationFactory factory = ...; // obtained from platform
 * 
 * // Create a regular population
 * IPopulation&lt;IAgent&gt; agentPop = factory.createRegularPopulation(scope, host, species);
 * 
 * // Create a grid population
 * IPopulation&lt;IGridAgent&gt; gridPop = factory.createGridPopulation(scope, host, gridSpecies);
 * 
 * // Create an experiment population
 * IPopulation.Experiment expPop = factory.createExperimentPopulation(scope, experimentSpecies);
 * </code>
 * </pre>
 * 
 * <h3>Implementation Notes</h3>
 * <p>
 * The default createPopulation() method automatically delegates to the appropriate specialized method based on whether
 * the species is a grid species or not. Custom implementations can override this behavior.
 * </p>
 * 
 * @see IPopulation
 * @see ISpecies
 * @see IMacroAgent
 * @author drogoul
 * @since GAMA 1.0
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
