/*******************************************************************************************************
 *
 * NodeToAdd.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

/**
 * Descriptor for a node (vertex) to be added to a graph.
 * 
 * <p>
 * This class encapsulates the information needed to add a vertex to a graph, including
 * the vertex object itself and an optional weight. It is used as an intermediate
 * representation in graph construction operations.
 * </p>
 * 
 * <p>
 * NodeToAdd supports both weighted and unweighted vertices. If no weight is specified,
 * the graph's default vertex weight will be used.
 * </p>
 * 
 * @see GraphObjectToAdd
 * @see EdgeToAdd
 * @see IGraph
 * @author drogoul
 */
public class NodeToAdd implements GraphObjectToAdd {

	/** The vertex object to be added to the graph. */
	public Object object;

	/** The weight associated with this vertex (optional, may be null). */
	public Double weight;

	/**
	 * Instantiates a new node descriptor with both object and weight.
	 *
	 * @param object the vertex object to add
	 * @param weight the weight of the vertex (may be null)
	 */
	public NodeToAdd(final Object object, final Double weight) {
		this.object = object;
		this.weight = weight;
	}

	/**
	 * Instantiates a new node descriptor with only the vertex object.
	 * 
	 * <p>
	 * The vertex will use the graph's default weight.
	 * </p>
	 *
	 * @param o the vertex object to add
	 */
	public NodeToAdd(final Object o) {
		object = o;
	}

	@Override
	public Object getObject() { return object; }

}