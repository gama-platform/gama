/**
 * 
 */
package gama.api.utils.collections;

import gama.api.runtime.scope.IScope;

/**
 * Determines the relationship among two polygons.
 */
public interface VertexRelationship<T> {

	/**
	 * @param scope
	 *            TODO Determines if two vertex geometries are to be treated as related in any way.
	 * @param p1
	 *            a geometrical object
	 * @param p2
	 *            another geometrical object
	 */
	boolean related(IScope scope, T p1, T p2);

	/**
	 * Equivalent.
	 *
	 * @param scope
	 *            the scope
	 * @param p1
	 *            the p 1
	 * @param p2
	 *            the p 2
	 * @return true, if successful
	 */
	boolean equivalent(IScope scope, T p1, T p2);

	// Double distance(T p1, T p2);

}