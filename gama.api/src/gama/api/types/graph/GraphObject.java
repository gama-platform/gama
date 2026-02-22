/*******************************************************************************************************
 *
 * GraphObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

/**
 * Abstract base class for graph objects (vertices and edges).
 * 
 * <p>
 * This class provides common functionality for both vertices ({@link _Vertex}) and edges ({@link _Edge}),
 * including weight management and a reference to the containing graph. It serves as the foundation
 * for GAMA's internal graph element representations.
 * </p>
 * 
 * <p>
 * All graph objects maintain:
 * <ul>
 * <li>A reference to their containing graph</li>
 * <li>A weight value (used for weighted graph algorithms)</li>
 * </ul>
 * </p>
 * 
 * @param <T> the type of the containing graph
 * @param <V> the type of vertices in the graph
 * @param <E> the type of edges in the graph
 * 
 * @see _Vertex
 * @see _Edge
 * @see IGraph
 * @author drogoul
 * @since 12 janv. 2014
 */
public abstract class GraphObject<T extends IGraph<V, E>, V, E> {

	/** The graph to which this object belongs. */
	protected final T graph;

	/** The weight of this graph object (vertex or edge). */
	protected double weight = DefaultDirectedWeightedGraph.DEFAULT_EDGE_WEIGHT;

	/**
	 * Instantiates a new graph object.
	 * 
	 * <p>
	 * This constructor is called by subclasses to initialize the graph reference
	 * and weight value.
	 * </p>
	 *
	 * @param g the graph containing this object
	 * @param w the initial weight for this object
	 */
	GraphObject(final T g, final double w) {
		graph = g;
		weight = w;
	}

	/**
	 * Sets the weight of this graph object.
	 * 
	 * <p>
	 * The weight is used by various graph algorithms such as shortest path
	 * computation. The interpretation of the weight depends on the algorithm
	 * being used.
	 * </p>
	 *
	 * @param w the new weight value
	 */
	public void setWeight(final double w) { weight = w; }

	/**
	 * Gets the weight of this graph object.
	 * 
	 * <p>
	 * Subclasses must implement this method to provide the actual weight value,
	 * which may be stored internally or computed from the graph structure.
	 * </p>
	 *
	 * @return the current weight of this object
	 */
	public abstract double getWeight();

	/**
	 * Checks if this graph object is a vertex (node).
	 * 
	 * <p>
	 * This method is overridden by {@link _Vertex} to return true.
	 * </p>
	 *
	 * @return true if this is a vertex, false otherwise
	 */
	public boolean isNode() { return false; }

	/**
	 * Checks if this graph object is an edge.
	 * 
	 * <p>
	 * This method is overridden by {@link _Edge} to return true.
	 * </p>
	 *
	 * @return true if this is an edge, false otherwise
	 */
	public boolean isEdge() { return false; }
}
