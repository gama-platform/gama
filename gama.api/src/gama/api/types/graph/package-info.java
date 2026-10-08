/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * Provides interfaces and classes for graph data structures in the GAMA modeling platform.
 * 
 * <p>
 * This package contains the core graph API used throughout GAMA for representing and manipulating
 * graph structures. It supports both directed and undirected graphs, weighted and unweighted graphs,
 * and spatial graphs where vertices and edges have geometric properties.
 * </p>
 * 
 * <h2>Core Interfaces</h2>
 * <ul>
 * <li>{@link gama.api.types.graph.IGraph} - The main interface for all graph types, extending both
 * GAMA's container interfaces and JGraphT's Graph interface</li>
 * <li>{@link gama.api.types.graph.ISpatialGraph} - Specialized interface for graphs with spatial
 * properties (geometric vertices and edges)</li>
 * <li>{@link gama.api.types.graph.IPath} - Represents a path through a graph as a sequence of
 * vertices and edges</li>
 * </ul>
 * 
 * <h2>Graph Components</h2>
 * <ul>
 * <li>{@link gama.api.types.graph._Vertex} - Internal representation of graph vertices with
 * support for in/out edge tracking</li>
 * <li>{@link gama.api.types.graph._Edge} - Internal representation of graph edges with source,
 * target, and weight properties</li>
 * <li>{@link gama.api.types.graph.GraphObject} - Base class for vertices and edges providing
 * common weight management</li>
 * </ul>
 * 
 * <h2>Factory Classes</h2>
 * <ul>
 * <li>{@link gama.api.types.graph.GamaGraphFactory} - Static factory for creating graph instances
 * from various data sources (lists, maps, etc.)</li>
 * <li>{@link gama.api.types.graph.GamaPathFactory} - Static factory for creating path instances</li>
 * <li>{@link gama.api.types.graph.IGraphFactory} - Interface for graph factory implementations</li>
 * <li>{@link gama.api.types.graph.IPathFactory} - Interface for path factory implementations</li>
 * </ul>
 * 
 * <h2>Path Finding</h2>
 * <ul>
 * <li>{@link gama.api.types.graph.IPathComputer} - Interface for computing shortest paths and
 * k-shortest paths using various algorithms (Dijkstra, A*, Floyd-Warshall, etc.)</li>
 * </ul>
 * 
 * <h2>Event System</h2>
 * <ul>
 * <li>{@link gama.api.types.graph.GraphEvent} - Event record for graph modifications
 * (vertex/edge added, removed, changed)</li>
 * <li>{@link gama.api.types.graph.IGraphEventListener} - Listener interface for graph events</li>
 * <li>{@link gama.api.types.graph.IGraphEventProvider} - Provider interface for objects that
 * emit graph events</li>
 * </ul>
 * 
 * <h2>Helper Classes</h2>
 * <ul>
 * <li>{@link gama.api.types.graph.GraphObjectToAdd} - Marker interface for objects to be added
 * to a graph</li>
 * <li>{@link gama.api.types.graph.EdgeToAdd} - Descriptor for edges to be added with source,
 * target, and weight</li>
 * <li>{@link gama.api.types.graph.NodeToAdd} - Descriptor for nodes to be added with weight</li>
 * <li>{@link gama.api.types.graph.VertexRelationship} - Interface for defining custom vertex
 * relationships (e.g., spatial relationships)</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <p>Creating a graph from a list of edges:</p>
 * <pre>
 * IList edges = ...;
 * IGraph graph = GamaGraphFactory.createFromList(scope, edges, true);
 * </pre>
 * 
 * <p>Finding the shortest path between two vertices:</p>
 * <pre>
 * IPath path = graph.computeShortestPathBetween(scope, source, target);
 * </pre>
 * 
 * <p>Listening to graph events:</p>
 * <pre>
 * graph.addListener(new IGraphEventListener() {
 *     public void receiveEvent(IScope scope, GraphEvent event) {
 *         // Handle graph modification
 *     }
 * });
 * </pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see gama.api.types.graph.IGraph
 * @see gama.api.types.graph.IPath
 * @see gama.api.types.graph.GamaGraphFactory
 */
package gama.api.types.graph;
