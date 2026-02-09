/*******************************************************************************************************
 *
 * IGraph.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.objects;

import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ITypeProvider;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.IPathComputer;
import gama.api.runtime.scope.IScope;
import gama.api.utils.collections.GraphObjectToAdd;
import gama.api.utils.collections.IGraphEventProvider;
import gama.api.utils.collections._Edge;
import gama.api.utils.collections._Vertex;

/**
 * Written by drogoul Modified on 24 nov. 2011
 *
 * An interface for the different kinds of graphs encountered in GAML. Vertices are the keys (actually, pairs of nodes),
 * while edges are the values
 *
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
public interface IGraph<Node, Edge> extends IContainer.Modifiable<Node, Edge, IPair<Node, Node>, GraphObjectToAdd>,
		IContainer.Addressable<Node, Edge, IPair<Node, Node>, List<Edge>>, Graph<Node, Edge>, IGraphEventProvider {

	/** The default node weight. */
	double DEFAULT_NODE_WEIGHT = 0.0;

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
	IList<Node> getVertices();

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
	IPath<Node, Edge, IGraph<Node, Edge>> getCircuit(IScope scope);

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
	double computeWeight(final IPath<Node, Edge, ? extends IGraph<Node, Edge>> gamaPath);

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
	IPair<Node, Node> buildIndex(IScope scope, Object object);

	/**
	 * Builds the indexes.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @return the i container
	 */
	IContainer<?, IPair<Node, Node>> buildIndexes(IScope scope, IContainer value);

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
	_Edge<Node, Edge> getEdge(final Object e);

	/**
	 * Gets the vertex.
	 *
	 * @param v
	 *            the v
	 * @return the vertex
	 */
	_Vertex<Node, Edge> getVertex(final Object v);

	/**
	 * Gets the vertex map.
	 *
	 * @return the vertex map
	 */
	Map<Node, _Vertex<Node, Edge>> getVertexMap();

	/**
	 * Gets the edge map.
	 *
	 * @return the edge map
	 */
	Map<Edge, _Edge<Node, Edge>> getEdgeMap();

}