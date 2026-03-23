/*******************************************************************************************************
 *
 * VertexRelationship.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.graph;

import gama.api.runtime.scope.IScope;

/**
 * Interface for defining relationships between vertices in a graph.
 * 
 * <p>
 * This interface allows for custom definitions of how two vertices are related or equivalent.
 * It is particularly useful for spatial graphs where vertices may have geometric properties
 * and relationships can be based on spatial criteria (e.g., proximity, intersection, containment).
 * </p>
 * 
 * <p>
 * Implementations can define domain-specific relationships such as:
 * <ul>
 * <li>Spatial relationships (touching, overlapping, within distance)</li>
 * <li>Topological relationships (connected, adjacent)</li>
 * <li>Semantic relationships (same type, same category)</li>
 * </ul>
 * </p>
 * 
 * @param <T> the type of vertices being compared
 * 
 * @see ISpatialGraph
 * @author drogoul
 */
public interface VertexRelationship<T> {

	/**
	 * Determines if two vertices are related according to some criterion.
	 * 
	 * <p>
	 * This method defines a general relationship between vertices. The specific
	 * meaning of "related" depends on the implementation. For example, in a spatial
	 * graph, vertices might be related if they are within a certain distance of
	 * each other.
	 * </p>
	 * 
	 * <p>
	 * The relationship does not have to be symmetric: {@code related(scope, p1, p2)}
	 * may differ from {@code related(scope, p2, p1)}.
	 * </p>
	 *
	 * @param scope the execution scope
	 * @param p1 the first vertex
	 * @param p2 the second vertex
	 * @return true if the vertices are related, false otherwise
	 */
	boolean related(IScope scope, T p1, T p2);

	/**
	 * Determines if two vertices are equivalent according to some criterion.
	 * 
	 * <p>
	 * This method defines an equivalence relationship between vertices, which should
	 * typically be symmetric: if {@code equivalent(scope, p1, p2)} is true, then
	 * {@code equivalent(scope, p2, p1)} should also be true.
	 * </p>
	 * 
	 * <p>
	 * Equivalence is often used to determine if two vertices should be considered
	 * the same for certain operations, even if they are not identical objects.
	 * For example, two geometric vertices might be equivalent if they represent
	 * the same location within some tolerance.
	 * </p>
	 *
	 * @param scope the execution scope
	 * @param p1 the first vertex
	 * @param p2 the second vertex
	 * @return true if the vertices are equivalent, false otherwise
	 */
	boolean equivalent(IScope scope, T p1, T p2);

}