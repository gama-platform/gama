/*******************************************************************************************************
 *
 * ISpatialGraph.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.topology.ITopology;
import gama.api.utils.interfaces.IAgentFilter;

/**
 * Interface for graphs with spatial properties.
 * 
 * <p>
 * A spatial graph is a specialized type of graph where both vertices and edges are
 * geometric shapes ({@link IShape}). This allows the graph to be embedded in space
 * and enables spatial operations and queries.
 * </p>
 * 
 * <p>
 * Spatial graphs are particularly useful for:
 * <ul>
 * <li>Road networks and transportation systems</li>
 * <li>Spatial movement of agents</li>
 * <li>Network analysis with geographic constraints</li>
 * <li>Topological relationships between spatial entities</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The graph is associated with a {@link ITopology} that defines how distances
 * and spatial relationships are computed. Vertices and edges inherit geometric
 * properties from {@link IShape}, including location, bounds, and geometry.
 * </p>
 * 
 * <h3>Example Usage:</h3>
 * <pre>
 * ISpatialGraph roadNetwork = ...;
 * IShape intersection1 = ...;
 * IShape intersection2 = ...;
 * IPath path = roadNetwork.computeShortestPathBetween(scope, intersection1, intersection2);
 * </pre>
 * 
 * @see IGraph
 * @see IShape
 * @see ITopology
 * @author drogoul
 * @since 3 févr. 2012
 */
public interface ISpatialGraph extends IGraph<IShape, IShape>, IAgentFilter {

	/**
	 * Gets the topology associated with this spatial graph.
	 * 
	 * <p>
	 * The topology defines how spatial distances and relationships are computed.
	 * It determines, for example, whether distances are Euclidean, Manhattan,
	 * or follow the graph structure (graph distance).
	 * </p>
	 *
	 * @param scope the execution scope
	 * @return the topology defining spatial computations for this graph
	 */
	ITopology getTopology(IScope scope);

	/**
	 * Gets the list of vertices (nodes) in this spatial graph.
	 * 
	 * <p>
	 * Each vertex is a geometric shape with spatial properties such as location,
	 * area, and perimeter.
	 * </p>
	 *
	 * @return the list of vertex shapes
	 */
	@Override
	IList<IShape> getVertices();

	/**
	 * Gets the list of edges in this spatial graph.
	 * 
	 * <p>
	 * Each edge is a geometric shape, typically a line or polyline connecting
	 * two vertices. Edges have spatial properties such as length and geometry.
	 * </p>
	 *
	 * @return the list of edge shapes
	 */
	@Override
	IList<IShape> getEdges();

}
