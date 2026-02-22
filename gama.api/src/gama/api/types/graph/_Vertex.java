/*******************************************************************************************************
 *
 * _Vertex.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.util.ArrayUnenforcedSet;

/**
 * Internal representation of a vertex (node) in a GAMA graph.
 * 
 * <p>
 * This class maintains the internal structure of a graph vertex, including:
 * <ul>
 * <li>Sets of incoming and outgoing edges for efficient traversal</li>
 * <li>A count of connected edges</li>
 * <li>An optional index for algorithms that require vertex ordering</li>
 * <li>A weight value inherited from {@link GraphObject}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The vertex tracks both inbound and outbound edges separately, which is particularly
 * useful for directed graphs. For undirected graphs, edges may appear in both sets.
 * </p>
 * 
 * <p>
 * This is an internal implementation class and should not be directly instantiated
 * by client code. Use {@link IGraph} methods to add vertices to a graph.
 * </p>
 * 
 * @param <E> the type of edges in the graph
 * @param <V> the type of vertices in the graph
 * 
 * @see _Edge
 * @see GraphObject
 * @see IGraph
 * @author drogoul
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class _Vertex<E, V> extends GraphObject<IGraph<E, V>, E, V> {

	/** The set of incoming edges to this vertex. */
	Set<E> inEdges = new ArrayUnenforcedSet(1);

	/** The set of outgoing edges from this vertex. */
	Set<E> outEdges = new ArrayUnenforcedSet(1);

	/** The total count of edges (both incoming and outgoing) connected to this vertex. */
	int edgesCount = 0;

	/** The index of this vertex (used by some algorithms, -1 if not set). */
	int index = -1;

	/**
	 * Creates a new vertex belonging to the specified graph.
	 * 
	 * <p>
	 * The vertex is initialized with the graph's default vertex weight.
	 * </p>
	 *
	 * @param gamaGraph the graph to which this vertex belongs
	 */
	public _Vertex(final IGraph<E, V> gamaGraph) {
		super(gamaGraph, IGraph.DEFAULT_VERTEX_WEIGHT);
	}

	@Override
	public double getWeight() { return weight; }

	/**
	 * Adds an outgoing edge from this vertex.
	 * 
	 * <p>
	 * This method is called internally when an edge with this vertex as its source
	 * is added to the graph. The edge count is incremented.
	 * </p>
	 *
	 * @param e the edge to add
	 */
	public void addOutEdge(final Object e) {
		outEdges.add((E) e);
		edgesCount++;
	}

	/**
	 * Removes an incoming edge to this vertex.
	 * 
	 * <p>
	 * This method is called internally when an edge with this vertex as its target
	 * is removed from the graph. The edge count is decremented.
	 * </p>
	 *
	 * @param e the edge to remove
	 */
	public void removeInEdge(final Object e) {
		inEdges.remove(e);
		edgesCount--;
	}

	/**
	 * Removes an outgoing edge from this vertex.
	 * 
	 * <p>
	 * This method is called internally when an edge with this vertex as its source
	 * is removed from the graph. The edge count is decremented.
	 * </p>
	 *
	 * @param e the edge to remove
	 */
	public void removeOutEdge(final Object e) {
		outEdges.remove(e);
		edgesCount--;
	}

	/**
	 * Adds an incoming edge to this vertex.
	 * 
	 * <p>
	 * This method is called internally when an edge with this vertex as its target
	 * is added to the graph. The edge count is incremented.
	 * </p>
	 *
	 * @param e the edge to add
	 */
	public void addInEdge(final Object e) {
		inEdges.add((E) e);
		edgesCount++;
	}

	/**
	 * Finds the first outgoing edge from this vertex to a specified target vertex.
	 * 
	 * <p>
	 * This method searches through all outgoing edges to find one that connects
	 * to the specified target vertex. If multiple edges exist to the same target,
	 * only the first one found is returned.
	 * </p>
	 *
	 * @param v2 the target vertex
	 * @return the edge connecting this vertex to v2, or null if no such edge exists
	 */
	public Object edgeTo(final Object v2) {
		for (final Object e : outEdges) {
			final _Edge<V, E> edge = (_Edge<V, E>) graph.getEdgeMap().get(e);
			if (edge != null && edge.getTarget().equals(v2)) return e;
		}
		return null;
	}

	/**
	 * Finds all outgoing edges from this vertex to a specified target vertex.
	 * 
	 * <p>
	 * This method is useful for multigraphs where multiple edges can exist
	 * between the same pair of vertices.
	 * </p>
	 *
	 * @param v2 the target vertex
	 * @return a set of all edges connecting this vertex to v2 (empty if none exist)
	 */
	public Set edgesTo(final Object v2) {
		Set result = new LinkedHashSet<>();
		for (final Object e : outEdges) {
			final _Edge<V, E> edge = (_Edge<V, E>) graph.getEdgeMap().get(e);
			if (edge.getTarget().equals(v2)) { result.add(e); }
		}
		return result;
	}

	/**
	 * Gets all edges connected to this vertex (both incoming and outgoing).
	 * 
	 * <p>
	 * The returned set contains the union of incoming and outgoing edges.
	 * For undirected graphs, this represents all incident edges.
	 * </p>
	 *
	 * @return a set containing all edges connected to this vertex
	 */
	public Set getEdges() {
		final Set result = new LinkedHashSet<>(inEdges);
		result.addAll(outEdges);
		return result;
	}

	/**
	 * Gets the total number of edges connected to this vertex.
	 * 
	 * <p>
	 * This count includes both incoming and outgoing edges. For undirected graphs,
	 * the degree of the vertex equals this count.
	 * </p>
	 *
	 * @return the number of edges connected to this vertex
	 */
	public int getEdgesCount() { return edgesCount; }

	/**
	 * Sets the index of this vertex.
	 * 
	 * <p>
	 * The index is used by certain graph algorithms that require vertices to be
	 * numbered. It has no semantic meaning otherwise.
	 * </p>
	 *
	 * @param i the index value to set
	 */
	public void setIndex(final int i) { index = i; }

	/**
	 * Gets the index of this vertex.
	 * 
	 * <p>
	 * Returns -1 if no index has been set.
	 * </p>
	 *
	 * @return the index of this vertex, or -1 if not set
	 */
	public int getIndex() { return index; }

	@Override
	public boolean isNode() { return true; }

	/**
	 * Gets the set of incoming edges to this vertex.
	 * 
	 * <p>
	 * For directed graphs, these are edges where this vertex is the target.
	 * </p>
	 *
	 * @return the set of incoming edges
	 */
	public Set getInEdges() { return inEdges; }

	/**
	 * Gets the set of outgoing edges from this vertex.
	 * 
	 * <p>
	 * For directed graphs, these are edges where this vertex is the source.
	 * </p>
	 *
	 * @return the set of outgoing edges
	 */
	public Set getOutEdges() { return outEdges; }

}