/*******************************************************************************************************
 *
 * GamaTopologyFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import gama.api.data.objects.IContainer;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.ISpatialGraph;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public class GamaTopologyFactory implements IFactory<ITopology> {

	/** The Internal factory. */
	private static ITopologyFactory InternalFactory;

	/**
	 * Sets the builder.
	 *
	 * @param factory
	 *            the new builder
	 */
	public static void setBuilder(final ITopologyFactory factory) { InternalFactory = factory; }

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i topology
	 */
	public static ITopology createFrom(final IScope scope, final IShape obj) {
		return createContinuous(scope, obj);
	}

	/**
	 * Creates the.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static ITopology createFrom(final IScope scope, final IContainer<?, IShape> obj)
			throws GamaRuntimeException {
		return InternalFactory.createFrom(scope, obj);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param copy
	 *            the copy
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("rawtypes")
	public static ITopology createFrom(final IScope scope, final Object obj, final boolean copy)
			throws GamaRuntimeException {
		return InternalFactory.createFrom(scope, obj, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param copy
	 *            the copy
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("rawtypes")
	public static ITopology createFrom(final IScope scope, final Object obj) throws GamaRuntimeException {
		return InternalFactory.createFrom(scope, obj, false);
	}

	/**
	 * Creates a new GamaTopology object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the host
	 * @return the i topology
	 */
	public static ITopology createContinuous(final IScope scope, final IShape host) {
		return InternalFactory.createFrom(scope, host);
	}

	/**
	 * Creates a new GamaTopology object.
	 *
	 * @return the i topology
	 */

	/**
	 * Builds the grid topology.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @param host
	 *            the host
	 * @return the i topology
	 */
	public static ITopology createGrid(final IScope scope, final ISpecies species, final IAgent host) {
		return InternalFactory.createGrid(scope, species, host);
	}

	/**
	 * Creates a new GamaTopology object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the host
	 * @param graph
	 *            the graph
	 * @return the i topology
	 */
	public static ITopology createGraph(final IScope scope, final IShape host, final ISpatialGraph graph) {
		return InternalFactory.createGraph(scope, host, graph);
	}

}
