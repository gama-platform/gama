/*******************************************************************************************************
 *
 * IGraphFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;

/**
 * Interface for graph factory implementations.
 * 
 * <p>
 * This interface defines the contract for creating {@link IGraph} instances from various
 * data sources. Implementations provide the actual graph construction logic and are
 * registered with {@link GamaGraphFactory} to make them available throughout GAMA.
 * </p>
 * 
 * <h2>Graph Creation Strategies</h2>
 * <p>
 * The factory supports multiple creation strategies:
 * </p>
 * 
 * <h3>From Lists</h3>
 * <ul>
 * <li>Create graphs from lists of vertices or edges</li>
 * <li>Automatically infer structure based on element types</li>
 * <li>Support for directed/undirected and spatial/non-spatial graphs</li>
 * <li>Type constraints can be specified for vertices and edges</li>
 * </ul>
 * 
 * <h3>From Maps</h3>
 * <ul>
 * <li>Create graphs from adjacency representations (vertex → neighbors)</li>
 * <li>Keys are source vertices, values are target vertices or edges</li>
 * <li>Natural representation for network data</li>
 * </ul>
 * 
 * <h3>From Generic Objects</h3>
 * <ul>
 * <li>Flexible creation from various source types (files, other graphs, etc.)</li>
 * <li>Support for copying or referencing existing graphs</li>
 * <li>Additional parameters can guide the creation process</li>
 * </ul>
 * 
 * <h3>Graph Transformations</h3>
 * <ul>
 * <li>Convert directed graphs to undirected and vice versa</li>
 * <li>Can create views or copies depending on implementation</li>
 * </ul>
 * 
 * <h2>Implementation Notes</h2>
 * <p>
 * Implementations should:
 * <ul>
 * <li>Handle null and empty inputs gracefully</li>
 * <li>Validate type constraints when specified</li>
 * <li>Support both spatial and non-spatial graph creation</li>
 * <li>Provide efficient graph structures appropriate for GAMA's use cases</li>
 * </ul>
 * </p>
 * 
 * @see GamaGraphFactory
 * @see IGraph
 * @author drogoul
 */
public interface IGraphFactory {

	/**
	 * Creates an undirected graph view or copy of a source graph.
	 *
	 * @param source
	 *            the source graph
	 * @return an undirected graph
	 */
	IGraph asUndirectedGraph(final IGraph source);

	/**
	 * Creates a directed graph view or copy of a source graph.
	 *
	 * @param source
	 *            the source graph
	 * @return a directed graph
	 */
	IGraph asDirectedGraph(final IGraph source);

	/**
	 * Creates a graph from a list of edges or vertices.
	 *
	 * @param scope
	 *            the scope
	 * @param edgesOrVertices
	 *            list of objects to be interpreted as edges or vertices
	 * @param byEdge
	 *            if true, the list contains edges; otherwise, vertices
	 * @param directed
	 *            whether the created graph should be directed
	 * @param spatial
	 *            whether the graph should be spatial
	 * @param nodeType
	 *            the type constraint for nodes
	 * @param edgeType
	 *            the type constraint for edges
	 * @return the created graph
	 */
	IGraph createFromList(final IScope scope, final IList edgesOrVertices, final boolean byEdge, final boolean directed, final boolean spatial, final IType nodeType, final IType edgeType);

	/**
	 * Creates a graph from a list, guessing the structure (edges or vertices).
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the list of objects
	 * @param spatial
	 *            whether the graph is spatial
	 * @return the created graph
	 */
	IGraph createFromList(final IScope scope, final IList obj, final boolean spatial);

	/**
	 * Creates a graph from a map where keys are source nodes and values are target nodes (or edges).
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the map defining connections
	 * @param spatial
	 *            whether the graph is spatial
	 * @return the created graph
	 */
	IGraph createFromMap(final IScope scope, final IMap<?, ?> obj, final boolean spatial);

	/**
	 * General purpose creation/casting method to create a graph from an arbitrary object.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the source object (could be another graph, list, map, file...)
	 * @param param
	 *            additional parameters for creation (e.g. node type)
	 * @param copy
	 *            whether to copy the data or reference it
	 * @return the created graph
	 */
	IGraph createFrom(final IScope scope, final Object obj, final Object param, final boolean copy);

}