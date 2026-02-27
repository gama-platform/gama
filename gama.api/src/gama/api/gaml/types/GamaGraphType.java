/*******************************************************************************************************
 *
 * GamaGraphType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.graph.GamaGraphFactory;
import gama.api.types.graph.IGraph;

/**
 * Type representing graphs in GAML - specialized containers composed of vertices (nodes) and edges.
 * <p>
 * Graphs are fundamental data structures for modeling networks, relationships, and connectivity in GAML. They support
 * both directed and undirected graphs, weighted edges, and various graph algorithms for pathfinding, shortest paths,
 * and network analysis.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Vertices and edges as first-class elements</li>
 * <li>Support for directed and undirected graphs</li>
 * <li>Edge weights for pathfinding algorithms</li>
 * <li>Graph topology for spatial operations</li>
 * <li>Integration with agent-based models (agents as vertices)</li>
 * <li>Drawable for visualization</li>
 * </ul>
 * 
 * <h2>Type Parameters:</h2>
 * <p>
 * Graphs have two type parameters:
 * <ul>
 * <li>Vertex type - the type of nodes in the graph</li>
 * <li>Edge type - the type of edges connecting vertices</li>
 * </ul>
 * </p>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>
 * {@code
 * // Create a graph from a list of vertices and edges
 * graph my_graph <- graph([node1, node2, node3]);
 * 
 * // Create a road network from geometries
 * graph road_network <- as_edge_graph(road_shapefile);
 * 
 * // Add edges with weights
 * my_graph <- my_graph add_edge (node1::node2, 5.0);
 * 
 * // Find shortest path
 * path shortest <- path_between(my_graph, node1, node3);
 * 
 * // Directed graph
 * graph directed_net <- directed(graph([node1, node2]));
 * 
 * // Get neighbors
 * list<agent> neighbors <- my_graph neighbors_of(current_node);
 * }
 * </pre>
 * 
 * @author GAMA Development Team
 * @see GamaContainerType
 * @see IGraph
 * @see gama.api.types.graph.GamaPathFactory
 * @since GAMA 1.0
 */
@type (
		name = IKeyword.GRAPH,
		id = IType.GRAPH,
		wraps = { IGraph.class },
		kind = ISymbolKind.CONTAINER,
		concept = { IConcept.TYPE, IConcept.GRAPH },
		doc = @doc ("Special type of container composed of edges and vertices"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaGraphType extends GamaContainerType<IGraph> {

	/**
	 * Constructs a new graph type.
	 * 
	 * @param typesManager
	 *            the types manager responsible for type resolution and management
	 */
	public GamaGraphType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a graph.
	 * <p>
	 * This method supports casting from various source types:
	 * <ul>
	 * <li>Matrix - creates a grid graph from matrix structure</li>
	 * <li>List of geometries - creates an edge graph from spatial features</li>
	 * <li>Container of agents - creates a graph with agents as vertices</li>
	 * <li>Existing graph - returns a copy if requested</li>
	 * </ul>
	 * </p>
	 * 
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a graph
	 * @param param
	 *            optional parameter for graph configuration
	 * @param keyType
	 *            the type of vertices (nodes) in the graph
	 * @param contentsType
	 *            the type of edges in the graph
	 * @param copy
	 *            whether to create a copy if obj is already a graph
	 * @return the graph representation of the object
	 * @throws GamaRuntimeException
	 *             if the casting operation fails
	 */
	@Override
	public IGraph cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return GamaGraphFactory.castToGraph(scope, obj, param, copy);
	}

	/**
	 * Returns the number of type parameters for graphs.
	 * <p>
	 * Graphs are parametric types with two parameters:
	 * <ol>
	 * <li>Vertex (node) type</li>
	 * <li>Edge type</li>
	 * </ol>
	 * </p>
	 * 
	 * @return 2, as graphs have vertex and edge type parameters
	 */
	@Override
	public int getNumberOfParameters() { return 2; }

	/**
	 * Indicates whether graphs can be cast to constant values.
	 * <p>
	 * Graphs cannot be constant because their structure (vertices and edges) can change during simulation.
	 * </p>
	 * 
	 * @return false, graphs cannot be constant
	 */
	@Override
	public boolean canCastToConst() {
		return false;
	}

	/**
	 * Indicates whether graphs can be drawn/visualized.
	 * <p>
	 * Graphs are drawable and can be visualized with vertices as points and edges as lines.
	 * </p>
	 * 
	 * @return true, graphs can be displayed
	 */
	@Override
	public boolean isDrawable() { return true; }

}
