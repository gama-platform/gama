/*******************************************************************************************************
 *
 * _SpatialVertex.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.topology.graph;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.types.geometry.IShape;
import gama.api.types.graph._Vertex;
import gama.api.utils.StringUtils;

/**
 * The Class _SpatialVertex.
 */
public class _SpatialVertex extends _Vertex<IShape, IShape> {

	/**
	 * Instantiates a new spatial vertex.
	 *
	 * @param graph the graph
	 * @param vertex the vertex
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	public _SpatialVertex(final GamaSpatialGraph graph, final Object vertex) throws GamaRuntimeException {
		super(graph);
		if (!(vertex instanceof IShape)) { throw GamaRuntimeException
				.error(StringUtils.toGaml(vertex, false) + " is not a geometry", graph.getScope()); }
	}

}