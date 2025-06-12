/*******************************************************************************************************
 *
 * IGraph.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.graph;

import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.runtime.IScope;
import gama.core.util.GamaPair;
import gama.core.util.IAddressableContainer;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IModifiableContainer;
import gama.core.util.path.IPath;
import gama.gaml.operators.Graphs;
import gama.gaml.species.ISpecies;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
public interface IGraph<Node, Edge>
		extends IModifiableContainer<Node, Edge, GamaPair<Node, Node>, Graphs.GraphObjectToAdd>,
		IAddressableContainer<Node, Edge, GamaPair<Node, Node>, List<Edge>>, Graph<Node, Edge>, IGraphEventProvider {

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
	 * Internal edge map.
	 *
	 * @return the map
	 */
	Map<Edge, _Edge<Node, Edge>> _internalEdgeMap();

	/**
	 * Internal vertex map.
	 *
	 * @return the map
	 */
	Map<Node, _Vertex<Node, Edge>> _internalVertexMap();

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
	Graphs.GraphObjectToAdd buildValue(IScope scope, Object object);

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
	GamaPair<Node, Node> buildIndex(IScope scope, Object object);

	/**
	 * Builds the indexes.
	 *
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @return the i container
	 */
	IContainer<?, GamaPair<Node, Node>> buildIndexes(IScope scope, IContainer value);

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
	default boolean contains(final IScope scope, final Object o) {
		if (o instanceof GamaPair) return Graphs.containsEdge(scope, this, (GamaPair) o);
		return Graphs.containsEdge(scope, this, o);
	}

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
	default boolean containsKey(final IScope scope, final Object o) {
		return Graphs.containsVertex(scope, this, o);
	}

	/**
	 * Gets the path computer.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the path computer
	 * @date 31 oct. 2023
	 */
	PathComputer getPathComputer();

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

}