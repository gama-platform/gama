/*******************************************************************************************************
 *
 * _Edge.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import org.jgrapht.Graph;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;

/**
 * Internal representation of an edge in a GAMA graph.
 * 
 * <p>
 * This class maintains the internal structure of a graph edge, including:
 * <ul>
 * <li>References to source and target vertices</li>
 * <li>A weight value inherited from {@link GraphObject}</li>
 * <li>Bidirectional connections to vertices (updating their in/out edge sets)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Each edge maintains pointers to its source and target vertices and automatically
 * registers itself with those vertices when created. This ensures that the graph
 * structure remains consistent.
 * </p>
 * 
 * <p>
 * This is an internal implementation class and should not be directly instantiated
 * by client code. Use {@link IGraph} methods to add edges to a graph.
 * </p>
 * 
 * @param <V> the type of vertices in the graph
 * @param <E> the type of edges in the graph
 * 
 * @see _Vertex
 * @see GraphObject
 * @see IGraph
 * @author drogoul
 */
public class _Edge<V, E> extends GraphObject<IGraph<V, E>, V, E> {

	/** The source vertex of this edge. */
	private Object source;
	
	/** The target vertex of this edge. */
	private Object target;

	/**
	 * Creates a new edge with default weight.
	 * 
	 * <p>
	 * This constructor initializes the edge with the graph's default edge weight
	 * and registers it with both source and target vertices.
	 * </p>
	 *
	 * @param gamaGraph the graph to which this edge belongs
	 * @param edge the edge object
	 * @param source the source vertex
	 * @param target the target vertex
	 * @throws GamaRuntimeException if the edge cannot be created
	 */
	public _Edge(final IGraph<V, E> gamaGraph, final Object edge, final Object source, final Object target)
			throws GamaRuntimeException {
		this(gamaGraph, edge, source, target, Graph.DEFAULT_EDGE_WEIGHT);
	}

	/**
	 * Creates a new edge with specified weight.
	 * 
	 * <p>
	 * This constructor initializes the edge with the given weight and registers
	 * it with both source and target vertices.
	 * </p>
	 *
	 * @param gamaGraph the graph to which this edge belongs
	 * @param edge the edge object
	 * @param source the source vertex
	 * @param target the target vertex
	 * @param weight the weight of the edge
	 * @throws GamaRuntimeException if the edge cannot be created
	 */
	public _Edge(final IGraph<V, E> gamaGraph, final Object edge, final Object source, final Object target,
			final double weight) throws GamaRuntimeException {
		super(gamaGraph, weight);
		init(graph.getScope(), edge, source, target);
	}

	/**
	 * Initializes the edge by setting source and target vertices.
	 * 
	 * <p>
	 * This method is called during construction to establish the edge's endpoints
	 * and register the edge with the corresponding vertices.
	 * </p>
	 *
	 * @param scope the execution scope
	 * @param edge the edge object
	 * @param source1 the source vertex
	 * @param target1 the target vertex
	 * @throws GamaRuntimeException if initialization fails
	 */
	protected void init(final IScope scope, final Object edge, final Object source1, final Object target1)
			throws GamaRuntimeException {
		buildSource(edge, source1);
		buildTarget(edge, target1);
	}

	/**
	 * Sets the source vertex and registers this edge as an outgoing edge.
	 * 
	 * <p>
	 * This method updates the source vertex's outgoing edge set to include
	 * this edge.
	 * </p>
	 *
	 * @param edge the edge object
	 * @param source1 the source vertex
	 */
	protected void buildSource(final Object edge, final Object source1) {
		this.source = source1;
		graph.getVertex(source1).addOutEdge(edge);
	}

	/**
	 * Sets the target vertex and registers this edge as an incoming edge.
	 * 
	 * <p>
	 * This method updates the target vertex's incoming edge set to include
	 * this edge.
	 * </p>
	 *
	 * @param edge the edge object
	 * @param target1 the target vertex
	 */
	protected void buildTarget(final Object edge, final Object target1) {
		this.target = target1;
		graph.getVertex(target1).addInEdge(edge);
	}

	/**
	 * Removes this edge from its source and target vertices.
	 * 
	 * <p>
	 * This method is called when the edge is being removed from the graph.
	 * It updates both vertices to remove this edge from their respective
	 * incoming and outgoing edge sets.
	 * </p>
	 *
	 * @param edge the edge object to remove
	 */
	public void removeFromVerticesAs(final Object edge) {
		_Vertex<V, E> s = graph.getVertex(source);
		if (s != null) { s.removeOutEdge(edge); }
		_Vertex<V, E> t = graph.getVertex(target);
		if (t != null) { t.removeInEdge(edge); }
	}

	@Override
	public double getWeight() {
		// Note: Could potentially compute weight based on vertex weights
		// Double na = graph.getVertexWeight(source);
		// Double nb = graph.getVertexWeight(target);
		// return weight * (na + nb) / 2;
		return weight;
	}

	/**
	 * Gets the source vertex of this edge.
	 * 
	 * <p>
	 * For directed edges, this is the vertex from which the edge originates.
	 * For undirected edges, the distinction between source and target is arbitrary.
	 * </p>
	 *
	 * @return the source vertex
	 */
	public Object getSource() { return source; }

	/**
	 * Gets the opposite endpoint of this edge given one endpoint.
	 * 
	 * <p>
	 * This utility method returns the other vertex of the edge. If the given
	 * vertex is the source, it returns the target; otherwise, it returns the source.
	 * </p>
	 *
	 * @param extremity one endpoint of the edge
	 * @return the other endpoint of the edge
	 */
	public Object getOther(final Object extremity) {
		return extremity == source ? target : source;
	}

	/**
	 * Gets the target vertex of this edge.
	 * 
	 * <p>
	 * For directed edges, this is the vertex to which the edge points.
	 * For undirected edges, the distinction between source and target is arbitrary.
	 * </p>
	 *
	 * @return the target vertex
	 */
	public Object getTarget() { return target; }

	@Override
	public String toString() {
		return new StringBuffer().append(source).append(" -> ").append(target).toString();
	}

	@Override
	public boolean isEdge() { return true; }
}