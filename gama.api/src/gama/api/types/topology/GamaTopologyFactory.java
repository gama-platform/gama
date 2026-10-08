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
package gama.api.types.topology;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.ISpatialGraph;
import gama.api.types.misc.IContainer;

/**
 * The Class GamaTopologyFactory.
 * 
 * A static factory for creating and managing {@link ITopology} instances. This class serves as the main entry point
 * for topology creation in GAMA, delegating to an {@link ITopologyFactory} implementation. It provides a unified API
 * for creating different types of topologies and converting objects to topologies.
 * 
 * <p>
 * This factory supports creating various topology types:
 * <ul>
 * <li>Continuous topologies: defined by a shape, allowing free movement within boundaries</li>
 * <li>Grid topologies: discrete space divided into cells represented by agents</li>
 * <li>Graph topologies: connectivity defined by a spatial graph structure</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The actual implementation is delegated to an {@link ITopologyFactory} that must be set using
 * {@link #setBuilder(ITopologyFactory)} before using the factory methods.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see ITopology
 * @see ITopologyFactory
 */
public class GamaTopologyFactory {

	/** The Internal factory. */
	private static ITopologyFactory InternalFactory;

	/**
	 * Configures the internal factory implementation.
	 *
	 * @param factory
	 *            the {@link ITopologyFactory} to be used as the internal builder
	 */
	public static void setBuilder(final ITopologyFactory factory) { InternalFactory = factory; }

	/**
	 * Creates a topology from a shape. This is an alias for {@link #createContinuous(IScope, IShape)}.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the shape defining the topology boundaries
	 * @return the created continuous topology
	 */
	public static ITopology createFrom(final IScope scope, final IShape obj) {
		return createContinuous(scope, obj);
	}

	/**
	 * Creates a topology from a container of shapes.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the container of shapes
	 * @return the created topology
	 * @throws GamaRuntimeException
	 *             if creation fails
	 */
	public static ITopology createFrom(final IScope scope, final IContainer<?, IShape> obj)
			throws GamaRuntimeException {
		return InternalFactory.createFrom(scope, obj);
	}

	/**
	 * Casts an object to a topology with control over copying.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to convert
	 * @param copy
	 *            whether to create a copy of the object
	 * @return the created topology
	 * @throws GamaRuntimeException
	 *             if casting fails
	 */
	@SuppressWarnings ("rawtypes")
	public static ITopology castToTopology(final IScope scope, final Object obj, final boolean copy)
			throws GamaRuntimeException {
		return InternalFactory.createFrom(scope, obj, copy);
	}

	/**
	 * Casts an object to a topology without copying.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to convert
	 * @return the created topology
	 * @throws GamaRuntimeException
	 *             if casting fails
	 */
	@SuppressWarnings ("rawtypes")
	public static ITopology castToTopology(final IScope scope, final Object obj) throws GamaRuntimeException {
		return InternalFactory.createFrom(scope, obj, false);
	}

	/**
	 * Creates a continuous topology with the specified host shape.
	 * 
	 * A continuous topology allows agents to move freely within the boundaries defined by the host shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @param host
	 *            the shape defining the continuous space boundaries
	 * @return the created continuous topology
	 */
	public static ITopology createContinuous(final IScope scope, final IShape host) {
		return InternalFactory.createFrom(scope, host);
	}

	/**
	 * Creates a grid topology from a species.
	 * 
	 * A grid topology divides the space into discrete cells, where each cell is typically represented by an agent of
	 * the specified species.
	 *
	 * @param scope
	 *            the execution scope
	 * @param species
	 *            the species whose agents form the grid cells
	 * @param host
	 *            the agent hosting this topology
	 * @return the created grid topology
	 */
	public static ITopology createGrid(final IScope scope, final ISpecies species, final IAgent host) {
		return InternalFactory.createGrid(scope, species, host);
	}

	/**
	 * Creates a graph-based topology.
	 * 
	 * A graph topology uses a spatial graph to define connectivity and paths between locations.
	 *
	 * @param scope
	 *            the execution scope
	 * @param host
	 *            the shape defining the topology boundaries
	 * @param graph
	 *            the spatial graph defining the topology structure
	 * @return the created graph topology
	 */
	public static ITopology createGraph(final IScope scope, final IShape host, final ISpatialGraph graph) {
		return InternalFactory.createGraph(scope, host, graph);
	}

}
