/*******************************************************************************************************
 *
 * ISpatialGraph.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.metamodel.topology.graph;

import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.metamodel.topology.filter.IAgentFilter;
import gama.core.runtime.IScope;
import gama.core.util.IList;
import gama.core.util.graph.IGraph;

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
	 * @param scope the scope
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
