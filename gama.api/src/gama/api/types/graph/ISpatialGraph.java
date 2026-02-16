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
 * The class ISpatialGraph.
 *
 * @author drogoul
 * @since 3 f�vr. 2012
 *
 */
public interface ISpatialGraph extends IGraph<IShape, IShape>, IAgentFilter {

	/**
	 * Gets the topology.
	 *
	 * @param scope
	 *            the scope
	 * @return the topology
	 */
	ITopology getTopology(IScope scope);

	/**
	 * Gets the vertices.
	 *
	 * @return the vertices
	 */
	@Override
	IList<IShape> getVertices();

	/**
	 * Gets the edges.
	 *
	 * @return the edges
	 */
	@Override
	IList<IShape> getEdges();

}
