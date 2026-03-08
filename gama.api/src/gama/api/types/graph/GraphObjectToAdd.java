/*******************************************************************************************************
 *
 * GraphObjectToAdd.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

/**
 * Marker interface for objects that can be added to a graph.
 * 
 * <p>
 * This interface serves as a placeholder for expressions used to build complex graph items such as
 * edges and nodes. Implementations include {@link EdgeToAdd} and {@link NodeToAdd}, which encapsulate
 * the necessary information for adding new graph elements.
 * </p>
 * 
 * <p>
 * These objects are typically used as intermediates in graph construction operations and are not
 * directly evaluated as expressions. Instead, they provide structured data that graph factories
 * use to create actual vertices and edges.
 * </p>
 * 
 * @see EdgeToAdd
 * @see NodeToAdd
 * @see IGraph
 * @author drogoul
 */
public interface GraphObjectToAdd {

	/**
	 * Gets the underlying object to be added to the graph.
	 * 
	 * <p>
	 * This method returns the actual object that will become a vertex or edge in the graph.
	 * The interpretation of this object depends on the implementing class.
	 * </p>
	 *
	 * @return the object to be added to the graph
	 */
	Object getObject();
}