/*******************************************************************************************************
 *
 * INeighborhood.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Set;

import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * The Interface INeighborhood.
 * 
 * <p>This interface defines how cells in a grid topology relate to their neighbors. It provides
 * the fundamental structure for neighbor queries and relationships in grid-based spatial models.</p>
 * 
 * <h3>Neighborhood Types:</h3>
 * <ul>
 *   <li><strong>Von Neumann (4-neighborhood):</strong> Only orthogonal neighbors (N, S, E, W)</li>
 *   <li><strong>Moore (8-neighborhood):</strong> Orthogonal and diagonal neighbors</li>
 *   <li><strong>Hexagonal (6-neighborhood):</strong> For hexagonal grids, all 6 equidistant neighbors</li>
 *   <li><strong>Custom:</strong> User-defined neighbor patterns with varying ranges</li>
 * </ul>
 * 
 * <h3>Key Concepts:</h3>
 * <ul>
 *   <li><strong>Place Index:</strong> Each cell has a unique index in the grid (typically row-major order)</li>
 *   <li><strong>Radius/Range:</strong> Distance in grid cells for neighbor searches (1 = immediate, 2 = second-order, etc.)</li>
 *   <li><strong>Neighbor Caching:</strong> Pre-computed neighbor lists for performance optimization</li>
 * </ul>
 * 
 * <h3>Performance Considerations:</h3>
 * <p>Neighborhood calculations can be computationally expensive for large grids or high-radius queries.
 * Implementations may cache neighbor relationships to improve performance when neighbors are
 * frequently accessed (e.g., in cellular automata or diffusion models).</p>
 * 
 * <h3>Common Applications:</h3>
 * <ul>
 *   <li>Cellular automata (Conway's Game of Life, forest fire models, etc.)</li>
 *   <li>Diffusion processes (heat, chemicals, information spread)</li>
 *   <li>Local spatial analysis and aggregation</li>
 *   <li>Agent perception and interaction ranges</li>
 * </ul>
 *
 * @author Alexis Drogoul
 * @since 19 mai 2013
 * 
 * @see IGrid
 */
public interface INeighborhood {

	/**
	 * Gets the neighboring agents within a specified radius.
	 * 
	 * <p>Returns all agents (grid cells) that are neighbors of the cell at the given place index,
	 * within the specified radius distance. The radius is measured in grid cells.</p>
	 * 
	 * <p>For example:</p>
	 * <ul>
	 *   <li>radius = 1: immediate neighbors</li>
	 *   <li>radius = 2: neighbors and neighbors-of-neighbors</li>
	 *   <li>radius = n: all cells within n grid steps</li>
	 * </ul>
	 * 
	 * <p>The actual number of neighbors depends on the neighborhood type (Von Neumann vs Moore)
	 * and whether the grid is toroidal.</p>
	 *
	 * @param scope the current execution scope
	 * @param placeIndex the index of the cell whose neighbors to find
	 * @param radius the maximum distance in grid cells to search
	 * @return set of neighboring agents within the radius
	 */
	public abstract Set<IAgent> getNeighborsIn(IScope scope, final int placeIndex, final int radius);

	/**
	 * Checks if this is a Von Neumann neighborhood.
	 * 
	 * <p>Returns true if this neighborhood uses the Von Neumann pattern (4 orthogonal neighbors),
	 * false if it uses Moore (8 neighbors including diagonals) or another pattern.</p>
	 * 
	 * <p>Von Neumann neighborhoods are useful when diagonal movement or influence is not desired,
	 * such as in certain cellular automata or when modeling processes that spread only along
	 * cardinal directions.</p>
	 *
	 * @return true if Von Neumann (4-neighborhood), false otherwise
	 */
	public abstract boolean isVN();

	/**
	 * Gets the raw neighbor indices including the source cell itself.
	 * 
	 * <p>Returns an array of integer indices representing the neighbors of the cell at placeIndex,
	 * within the specified range. Unlike {@link #getNeighborsIn}, this returns indices rather
	 * than agent objects, and includes the source cell itself.</p>
	 * 
	 * <p>This low-level method is useful for optimized neighbor access and is typically used
	 * internally by grid implementations. The returned indices can be used to directly access
	 * grid cells without object lookups.</p>
	 *
	 * @param scope the current execution scope
	 * @param placeIndex the index of the source cell
	 * @param range the maximum distance in grid cells to search
	 * @return array of neighbor indices (including the source cell)
	 */
	public abstract int[] getRawNeighborsIncluding(IScope scope, int placeIndex, int range);

	/**
	 * Gets the neighbor index at a specific position in the neighbor list.
	 * 
	 * <p>Returns the grid index of the n-th neighbor of the cell at placeIndex.
	 * This provides direct array-based access to neighbor indices.</p>
	 * 
	 * <p>For example, if cell 10 has neighbors [9, 11, 19, 21] (Moore neighborhood),
	 * then neighborsIndexOf(scope, 10, 0) would return 9, neighborsIndexOf(scope, 10, 1)
	 * would return 11, etc.</p>
	 *
	 * @param scope the current execution scope
	 * @param placeIndex the index of the source cell
	 * @param n the position in the neighbor list (0-based)
	 * @return the grid index of the n-th neighbor
	 */
	public abstract int neighborsIndexOf(IScope scope, int placeIndex, int n);

	/**
	 * Clears all cached neighbor data.
	 * 
	 * <p>If the neighborhood implementation uses caching to improve performance, this method
	 * clears all cached neighbor relationships. This may be necessary when the grid structure
	 * changes or to free memory.</p>
	 * 
	 * <p>After calling clear(), subsequent neighbor queries will recompute relationships
	 * as needed.</p>
	 */
	public abstract void clear();

}