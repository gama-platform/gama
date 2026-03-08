/*******************************************************************************************************
 *
 * IGraph.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IContainer.Addressable;
import gama.api.types.misc.IContainer.Modifiable;
import gama.api.types.pair.IPair;

/**
 * The main interface for graph data structures in GAMA.
 * 
 * <p>
 * IGraph defines the complete API for working with graphs in the GAMA modeling platform.
 * It extends both JGraphT's {@link Graph} interface and GAMA's container interfaces
 * ({@link IContainer.Modifiable} and {@link IContainer.Addressable}), providing a rich
 * set of operations for graph manipulation, traversal, and analysis.
 * </p>
 * 
 * <h2>Graph Types</h2>
 * <p>
 * This interface supports various types of graphs:
 * <ul>
 * <li><b>Directed vs. Undirected</b>: Controlled via {@link #isDirected()} and {@link #setDirected(boolean)}</li>
 * <li><b>Weighted vs. Unweighted</b>: Vertices and edges can have associated weights</li>
 * <li><b>Spatial</b>: See {@link ISpatialGraph} for graphs with geometric properties</li>
 * <li><b>Simple vs. Multigraph</b>: Support for multiple edges between vertices</li>
 * </ul>
 * </p>
 * 
 * <h2>Core Operations</h2>
 * <ul>
 * <li><b>Vertex Operations</b>: Add, remove, query vertices and their weights</li>
 * <li><b>Edge Operations</b>: Add, remove, query edges and their weights</li>
 * <li><b>Path Finding</b>: Compute shortest paths using various algorithms (Dijkstra, A*, etc.)</li>
 * <li><b>Graph Analysis</b>: Check connectivity, find cycles, compute spanning trees</li>
 * <li><b>Traversal</b>: Access neighbors, predecessors, successors of vertices</li>
 * </ul>
 * 
 * <h2>Weight Management</h2>
 * <p>
 * Both vertices and edges can have associated weights (doubles). Weights are used by
 * pathfinding algorithms and other graph operations. The default vertex weight is
 * {@link #DEFAULT_VERTEX_WEIGHT}.
 * </p>
 * 
 * <h2>Event Notifications</h2>
 * <p>
 * As a {@link IGraphEventProvider}, graphs notify registered listeners of modifications
 * such as vertex/edge additions, removals, and changes.
 * </p>
 * 
 * <h2>GAML Variables</h2>
 * <p>
 * This interface exposes several attributes accessible from GAML:
 * <ul>
 * <li><code>vertices</code>: List of all vertices</li>
 * <li><code>edges</code>: List of all edges</li>
 * <li><code>spanning_tree</code>: Minimal spanning tree edges</li>
 * <li><code>circuit</code>: Hamiltonian cycle approximation</li>
 * <li><code>connected</code>: Whether the graph is connected</li>
 * </ul>
 * </p>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * // Create a graph from edges
 * IList edges = ...;
 * IGraph graph = GamaGraphFactory.createFromList(scope, edges, true);
 * 
 * // Add vertices and edges
 * graph.addVertex(vertex1);
 * graph.addEdge(vertex1, vertex2, edge12);
 * 
 * // Find shortest path
 * IPath path = graph.computeShortestPathBetween(scope, source, target);
 * 
 * // Check properties
 * boolean isConnected = graph.getConnected();
 * IList spanningTree = graph.getSpanningTree(scope);
 * </pre>
 * 
 * @param <Vertex> the type of vertices in the graph
 * @param <Edge> the type of edges in the graph
 * 
 * @see ISpatialGraph
 * @see IPath
 * @see GamaGraphFactory
 * @see IPathComputer
 * @author drogoul
 * @since 24 nov. 2011
 */
@vars ({ @variable (
		name = "spanning_tree",
		type = IType.LIST,
		of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
		doc = { @doc ("Returns the list of edges that compose the minimal spanning tree of this graph") }),
		@variable (
				name = "circuit",
				type = IType.PATH,
				doc = { @doc ("Returns a polynomial approximation of the Hamiltonian cycle (the optimal tour passing through each vertex) of this graph") }),
		@variable (
				name = "connected",
				type = IType.BOOL,
				doc = { @doc ("Returns whether this graph is connected or not") }),
		@variable (
				name = "edges",
				type = IType.LIST,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of edges of the receiver graph") }),
		@variable (
				name = "vertices",
				type = IType.LIST,
				of = ITypeProvider.KEY_TYPE_AT_INDEX + 1,
				doc = { @doc ("Returns the list of vertices of the receiver graph") }) })
@SuppressWarnings ({ "rawtypes" })
public interface IGraph<Vertex, Edge>
		extends IContainer.Modifiable<Vertex, Edge, IPair<Vertex, Vertex>, GraphObjectToAdd>,
		IContainer.Addressable<Vertex, Edge, IPair<Vertex, Vertex>, List<Edge>>, Graph<Vertex, Edge>,
		IGraphEventProvider {

	/** The default vertex weight. */
	double DEFAULT_VERTEX_WEIGHT = 0.0;

	/**
	 * Gets the vertex weight.
	 *
	 * @param v
	 *            the v
	 * @return the vertex weight
	 */
	double getVertexWeight(final Object v);

	/**
	 * Gets the weight of.
	 *
	 * @param v
	 *            the v
	 * @return the weight of
	 */
	Double getWeightOf(final Object v);

	/**
	 * Sets the vertex weight.
	 *
	 * @param v
	 *            the v
	 * @param weight
	 *            the weight
	 */
	void setVertexWeight(final Object v, final double weight);

	/**
	 * Sets the weights.
	 *
	 * @param weights
	 *            the weights
	 */
	void setWeights(Map<?, Double> weights);

	/**
	 * Gets the edges.
	 *
	 * @return the edges
	 */
	@getter ("edges")
	IList<Edge> getEdges();

	/**
	 * Gets the vertices.
	 *
	 * @return the vertices
	 */
	@getter ("vertices")
	IList<Vertex> getVertices();

	/**
	 * Gets the spanning tree.
	 *
	 * @param scope
	 *            the scope
	 * @return the spanning tree
	 */
	@getter ("spanning_tree")
	IList<Edge> getSpanningTree(IScope scope);

	/**
	 * Gets the circuit.
	 *
	 * @param scope
	 *            the scope
	 * @return the circuit
	 */
	@getter ("circuit")
	IPath<Vertex, Edge, IGraph<Vertex, Edge>> getCircuit(IScope scope);

	/**
	 * Gets the connected.
	 *
	 * @return the connected
	 */
	@getter ("connected")
	Boolean getConnected();

	/**
	 * Checks for cycle.
	 *
	 * @return the boolean
	 */
	@getter ("has_cycle")
	Boolean hasCycle();

	/**
	 * Checks if is directed.
	 *
	 * @return true, if is directed
	 */
	boolean isDirected();

	/**
	 * Sets the directed.
	 *
	 * @param b
	 *            the new directed
	 */
	void setDirected(final boolean b);

	/**
	 * Adds the edge.
	 *
	 * @param p
	 *            the p
	 * @return the object
	 */
	Object addEdge(Object p);

	/**
	 * Compute weight.
	 *
	 * @param gamaPath
	 *            the gama path
	 * @return the double
	 */
	double computeWeight(final IPath<Vertex, Edge, ? extends IGraph<Vertex, Edge>> gamaPath);

	/**
	 * Compute total weight.
	 *
	 * @return the double
	 */
	double computeTotalWeight();

	/**
	 * Builds the value.
	 *
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @return the graphs. graph object to add
	 */
	GraphObjectToAdd buildValue(IScope scope, Object object);

	/**
	 * Builds the values.
	 *
	 * @param scope
	 *            the scope
	 * @param objects
	 *            the objects
	 * @return the i container
	 */
	IContainer buildValues(IScope scope, IContainer objects);

	/**
	 * Builds the index.
	 *
	 * @param scope
	 *            the scope
	 * @param object
	 *            the object
	 * @return the gama pair
	 */
	IPair<Vertex, Vertex> buildIndex(IScope scope, Object object);

	/**
	 * Builds the indexes.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @return the i container
	 */
	IContainer<?, IPair<Vertex, Vertex>> buildIndexes(IScope scope, IContainer value);

	/**
	 * Gets the vertex species.
	 *
	 * @return the vertex species
	 */
	ISpecies getVertexSpecies();

	/**
	 * Gets the edge species.
	 *
	 * @return the edge species
	 */
	ISpecies getEdgeSpecies();

	/**
	 * Contains.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	boolean contains(final IScope scope, final Object o);

	/**
	 * Contains key.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	@Override
	boolean containsKey(final IScope scope, final Object o);

	/**
	 * Gets the path computer.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the path computer
	 * @date 31 oct. 2023
	 */
	IPathComputer getPathComputer();

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	default IType<?> computeRuntimeType(final IScope scope) {
		return Types.GRAPH.of(getVertices().computeRuntimeType(scope).getContentType(),
				getEdges().computeRuntimeType(scope).getContentType());
	}

	/**
	 * @return
	 */
	IScope getScope();

	/**
	 * Gets the edge.
	 *
	 * @param e
	 *            the e
	 * @return the edge
	 */
	_Edge<Vertex, Edge> getEdge(final Object e);

	/**
	 * Gets the vertex.
	 *
	 * @param v
	 *            the v
	 * @return the vertex
	 */
	_Vertex<Vertex, Edge> getVertex(final Object v);

	/**
	 * Gets the vertex map.
	 *
	 * @return the vertex map
	 */
	Map<Vertex, _Vertex<Vertex, Edge>> getVertexMap();

	/**
	 * Gets the edge map.
	 *
	 * @return the edge map
	 */
	Map<Edge, _Edge<Vertex, Edge>> getEdgeMap();

	/**
	 * Disposes the vertex associated with the given node.
	 *
	 * @param node
	 *            the node to dispose
	 */
	void disposeVertex(Vertex node);

	/**
	 * @param vn
	 * @param vc
	 * @return
	 */
	@Override
	Set<Edge> getAllEdges(Vertex vn, Vertex vc);

	/**
	 * @param scope
	 * @param source
	 * @param target
	 * @param p
	 * @return
	 */
	IPath<Vertex, Edge, IGraph<Vertex, Edge>> pathFromEdges(IScope scope, Vertex source, Vertex target, IList<Edge> p);

	/**
	 * @param scope
	 * @return
	 */
	IMatrix toMatrix(IScope scope);

}