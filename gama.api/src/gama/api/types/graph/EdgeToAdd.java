/*******************************************************************************************************
 *
 * EdgeToAdd.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

/**
 * Descriptor for an edge to be added to a graph.
 *
 * <p>
 * This class encapsulates all the information needed to add an edge to a graph, including its source and target
 * vertices, the edge object itself, and an optional weight. It is used as an intermediate representation in graph
 * construction operations.
 * </p>
 *
 * <p>
 * EdgeToAdd supports both weighted and unweighted edges. If no weight is specified, the graph's default edge weight
 * will be used.
 * </p>
 *
 * @see GraphObjectToAdd
 * @see NodeToAdd
 * @see IGraph
 * @author drogoul
 */
public class EdgeToAdd implements GraphObjectToAdd {

	/** The source vertex of the edge. */
	public final Object source;

	/** The target vertex of the edge. */
	public final Object target;

	/** The edge object that will be added to the graph. */
	public Object object;

	/** The weight of the edge (optional, may be null). */
	public Double weight;

	/**
	 * Instantiates a new edge descriptor with full specification.
	 *
	 * @param source
	 *            the source vertex of the edge
	 * @param target
	 *            the target vertex of the edge
	 * @param object
	 *            the edge object to add
	 * @param weight
	 *            the weight of the edge (may be null)
	 */
	public EdgeToAdd(final Object source, final Object target, final Object object, final Double weight) {
		this.object = object;
		this.weight = weight;
		this.source = source;
		this.target = target;
	}

	/**
	 * Gets the edge object that will be added to the graph.
	 *
	 * @return the edge object that will be added to the graph
	 */
	@Override
	public Object object() {
		return object;
	}
}