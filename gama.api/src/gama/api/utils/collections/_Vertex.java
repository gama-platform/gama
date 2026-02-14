/*******************************************************************************************************
 *
 * _Vertex.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.collections;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jgrapht.util.ArrayUnenforcedSet;

import gama.api.data.objects.IGraph;

/**
 * The Class _Vertex.
 *
 * @param <E>
 *            the element type
 * @param <V>
 *            the value type
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class _Vertex<E, V> extends GraphObject<IGraph<E, V>, E, V> {

	/** The in edges. */
	Set<E> inEdges = new ArrayUnenforcedSet(1);

	/** The out edges. */
	Set<E> outEdges = new ArrayUnenforcedSet(1);

	/** The edges count. */
	// Double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;
	int edgesCount = 0;

	/** The index. */
	int index = -1;

	/**
	 * The graph to which this vertex belongs
	 */
	// protected final GamaGraph<?, V> graph;

	/**
	 * @param gamaGraph
	 */
	public _Vertex(final IGraph<E, V> gamaGraph) {
		super(gamaGraph, IGraph.DEFAULT_VERTEX_WEIGHT);
	}

	@Override
	public double getWeight() { return weight; }

	/**
	 * Adds the out edge.
	 *
	 * @param e
	 *            the e
	 */
	public void addOutEdge(final Object e) {
		outEdges.add((E) e);
		edgesCount++;
	}

	/**
	 * Removes the in edge.
	 *
	 * @param e
	 *            the e
	 */
	public void removeInEdge(final Object e) {
		inEdges.remove(e);
		edgesCount--;
	}

	/**
	 * Removes the out edge.
	 *
	 * @param e
	 *            the e
	 */
	public void removeOutEdge(final Object e) {
		outEdges.remove(e);
		edgesCount--;
	}

	/**
	 * Adds the in edge.
	 *
	 * @param e
	 *            the e
	 */
	public void addInEdge(final Object e) {
		inEdges.add((E) e);
		edgesCount++;
	}

	/**
	 * Edge to.
	 *
	 * @param v2
	 *            the v 2
	 * @return the object
	 */
	public Object edgeTo(final Object v2) {
		for (final Object e : outEdges) {
			final _Edge<V, E> edge = (_Edge<V, E>) graph.getEdgeMap().get(e);
			if (edge != null && edge.getTarget().equals(v2)) return e;
		}
		return null;
	}

	/**
	 * Edges to.
	 *
	 * @param v2
	 *            the v 2
	 * @return the sets the
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
	 * Gets the edges.
	 *
	 * @return the edges
	 */
	public Set getEdges() {
		final Set result = new LinkedHashSet<>(inEdges);
		result.addAll(outEdges);
		return result;
	}

	/**
	 * @return
	 */
	public int getEdgesCount() { return edgesCount; }

	/**
	 * @param i
	 */
	public void setIndex(final int i) { index = i; }

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public int getIndex() { return index; }

	@Override
	public boolean isNode() { return true; }

	/**
	 * Gets the in edges.
	 *
	 * @return the in edges
	 */
	public Set getInEdges() { return inEdges; }

	/**
	 * Gets the out edges.
	 *
	 * @return the out edges
	 */
	public Set getOutEdges() { return outEdges; }

}