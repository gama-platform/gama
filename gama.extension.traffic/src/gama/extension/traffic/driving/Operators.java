/*******************************************************************************************************
 *
 * Operators.java, in gaml.extensions.traffic, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.extension.traffic.driving;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ITypeProvider;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.core.metamodel.topology.graph.GamaSpatialGraph;
import gama.core.runtime.IScope;
import gama.core.util.IContainer;
import gama.core.util.graph.IGraph;

/**
 * The Class Operators.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Operators {
	
	/**
	 * Spatial driving from edges.
	 *
	 * @param scope the scope
	 * @param edges the edges
	 * @param nodes the nodes
	 * @return the i graph
	 */
	@operator(
		value = "as_driving_graph",
		content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
		index_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
		concept = { IConcept.GRAPH, IConcept.TRANSPORT }
	)
	@doc(
		value = "creates a graph from the list/map of edges given as operand and connect the node to the edge", 
		examples = {
			@example(
				value = "as_driving_graph(road, node)  --:  build a graph while using the road agents as edges and the node agents as nodes",
				isExecutable = false
			)
		},
		see = { "as_intersection_graph", "as_distance_graph", "as_edge_graph" }
	)
	@no_test
	public static IGraph spatialDrivingFromEdges(final IScope scope, final IContainer edges, final IContainer nodes) {
		return new DrivingGraph(edges, nodes, scope);
	}
}
