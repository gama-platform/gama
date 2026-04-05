/**
 * 
 */
package gama.api.types.topology;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.ISpatialGraph;
import gama.api.types.misc.IContainer;

/**
 * The Interface ITopologyFactory.
 * 
 * Factory interface for creating {@link ITopology} instances. This interface defines the contract for implementations
 * that create various types of topologies used in GAMA simulations, including continuous, grid, and graph-based
 * topologies.
 * 
 * <p>
 * Implementations of this interface are responsible for:
 * <ul>
 * <li>Creating continuous topologies from shapes</li>
 * <li>Creating grid topologies from species</li>
 * <li>Creating graph-based topologies</li>
 * <li>Converting objects to topologies with type casting</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A topology in GAMA defines the spatial structure of an environment and provides methods for spatial operations like
 * distance calculations, path finding, and neighborhood queries.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see ITopology
 * @see GamaTopologyFactory
 */
public interface ITopologyFactory {

	/**
	 * Creates a topology from a shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the shape defining the topology boundaries
	 * @return the created topology
	 */
	ITopology createFrom(IScope scope, IShape obj);

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
	ITopology createFrom(IScope scope, IContainer<?, IShape> obj) throws GamaRuntimeException;

	/**
	 * Creates a topology from an arbitrary object with type casting.
	 *
	 * @param scope
	 *            the execution scope
	 * @param obj
	 *            the object to convert to a topology
	 * @param copy
	 *            whether to create a copy of the object
	 * @return the created topology
	 * @throws GamaRuntimeException
	 *             if casting or creation fails
	 */
	ITopology createFrom(IScope scope, Object obj, boolean copy) throws GamaRuntimeException;

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
	ITopology createContinuous(IScope scope, IShape host);

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
	ITopology createGrid(IScope scope, ISpecies species, IAgent host);

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
	ITopology createGraph(IScope scope, IShape host, ISpatialGraph graph);

}