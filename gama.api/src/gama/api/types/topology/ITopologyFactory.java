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
 * 
 */
public interface ITopologyFactory {

	/**
	 * From.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i topology
	 */
	ITopology createFrom(IScope scope, IShape obj);

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
	ITopology createFrom(IScope scope, IContainer<?, IShape> obj) throws GamaRuntimeException;

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
	ITopology createFrom(IScope scope, Object obj, boolean copy) throws GamaRuntimeException;

	/**
	 * Creates a new GamaTopology object.
	 *
	 * @param scope
	 *            the scope
	 * @param host
	 *            the host
	 * @return the i topology
	 */
	ITopology createContinuous(IScope scope, IShape host);

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
	ITopology createGrid(IScope scope, ISpecies species, IAgent host);

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
	ITopology createGraph(IScope scope, IShape host, ISpatialGraph graph);

}