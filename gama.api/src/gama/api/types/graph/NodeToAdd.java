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
 * This class encapsulates the information needed to add a vertex to a graph, including the vertex object itself and an
 * optional weight. It is used as an intermediate representation in graph construction operations.
 * </p>
 *
 * <p>
 * NodeToAdd supports both weighted and unweighted vertices. If no weight is specified, the graph's default vertex
 * weight will be used.
 * </p>
 *
 * @see GraphObjectToAdd
 * @see EdgeToAdd
 * @see IGraph
 * @author drogoul
 */
public record NodeToAdd(Object object, Double weight) implements GraphObjectToAdd {

}