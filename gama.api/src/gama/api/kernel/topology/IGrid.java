/*******************************************************************************************************
 *
 * IGrid.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Map;
import java.util.Set;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IPath;
import gama.api.types.list.IList;
import gama.api.types.matrix.IMatrix;
import gama.api.types.topology.ITopology;
import gama.api.utils.interfaces.IAgentFilter;
import gama.api.utils.interfaces.IDiffusionTarget;

/**
 * The Interface IGrid.
 * 
 * <p>This interface represents a 2D grid topology in GAMA, combining matrix functionality with spatial indexing
 * and diffusion capabilities. A grid is a discretized spatial structure where space is divided into regular cells,
 * each represented by an agent.</p>
 * 
 * <h3>Key Characteristics:</h3>
 * <ul>
 *   <li><strong>Discrete Topology:</strong> Space is divided into cells (agents) arranged in a regular grid</li>
 *   <li><strong>Grid Types:</strong> Supports square grids and hexagonal grids with different orientations</li>
 *   <li><strong>Pathfinding:</strong> Provides shortest path algorithms (Dijkstra, A*) for grid navigation</li>
 *   <li><strong>Neighborhoods:</strong> Manages neighbor relationships (Von Neumann, Moore, custom)</li>
 *   <li><strong>Diffusion:</strong> Supports value diffusion across grid cells</li>
 *   <li><strong>Torus Option:</strong> Can wrap around edges for toroidal topology</li>
 * </ul>
 * 
 * <h3>Grid Cell Representation:</h3>
 * <p>Each cell in the grid is typically an agent of a specific grid species. Cells can have attributes
 * (e.g., elevation, temperature, land use) and can participate in spatial operations.</p>
 * 
 * <h3>Performance Optimizations:</h3>
 * <p>The grid can be optimized for different use cases:</p>
 * <ul>
 *   <li>Individual shapes per cell vs. shared geometry</li>
 *   <li>Neighbor caching for repeated neighbor queries</li>
 *   <li>Different optimizer strategies for pathfinding</li>
 * </ul>
 * 
 * <h3>Common Use Cases:</h3>
 * <ul>
 *   <li>Cellular automata models</li>
 *   <li>Raster-based environmental models</li>
 *   <li>Agent movement on grid-based terrain</li>
 *   <li>Diffusion processes (heat, chemicals, etc.)</li>
 * </ul>
 *
 * @author Alexis Drogoul
 * @since 13 mai 2013
 * 
 * @see IMatrix
 * @see ISpatialIndex
 * @see IDiffusionTarget
 * @see INeighborhood
 */
public interface IGrid extends IMatrix<IShape>, ISpatialIndex, IDiffusionTarget {

	/**
	 * Gets the list of all agents (cells) in the grid.
	 * 
	 * <p>Returns all grid cells as agents. The order of agents in the list corresponds to
	 * the grid's internal ordering (typically row-major: left-to-right, top-to-bottom).</p>
	 *
	 * @return the list of all agents representing grid cells
	 */
	IList<IAgent> getAgents();

	/**
	 * Checks if this grid uses hexagonal cells.
	 * 
	 * <p>Grids can be composed of either square cells (default) or hexagonal cells.
	 * Hexagonal grids provide more natural neighbor relationships in certain models
	 * (e.g., each cell has 6 equidistant neighbors instead of 4 or 8).</p>
	 *
	 * @return true if the grid uses hexagonal cells, false if it uses square cells
	 */
	Boolean isHexagon();

	/**
	 * Checks if hexagonal grid uses horizontal orientation.
	 * 
	 * <p>For hexagonal grids, this determines the orientation of the hexagons:</p>
	 * <ul>
	 *   <li><strong>Horizontal (true):</strong> Hexagons have flat tops and bottoms (pointy sides)</li>
	 *   <li><strong>Vertical (false):</strong> Hexagons have pointy tops and bottoms (flat sides)</li>
	 * </ul>
	 * 
	 * <p>This affects neighbor calculations and visual representation. Only meaningful
	 * when isHexagon() returns true.</p>
	 *
	 * @return true for horizontal orientation (flat-top), false for vertical (pointy-top)
	 */
	Boolean isHorizontalOrientation();

	/**
	 * Sets the cell species for this grid.
	 * 
	 * <p>Associates a population of agents with this grid structure. The population must contain
	 * agents that represent individual grid cells. This method is typically called during
	 * grid initialization to link the grid topology with its cell agents.</p>
	 * 
	 * <p>Once set, the grid can manage and query these agents as spatial entities.</p>
	 *
	 * @param pop the population of agents representing grid cells
	 */
	void setCellSpecies(final IPopulation<? extends IAgent> pop);

	/**
	 * Gets the agent (grid cell) at the specified coordinate.
	 * 
	 * <p>Retrieves the agent representing the grid cell at the given point location.
	 * The point coordinates are interpreted in the grid's coordinate system.</p>
	 * 
	 * <p>If the point falls outside the grid boundaries, the behavior depends on
	 * whether the grid is toroidal (wraps around) or not (returns null).</p>
	 *
	 * @param c the point coordinate to query
	 * @return the agent at that location, or null if the location is outside the grid
	 */
	IAgent getAgentAt(final IPoint c);

	/**
	 * Computes the shortest path between two shapes on the grid.
	 * 
	 * <p>Uses a pathfinding algorithm (typically A* or Dijkstra) to find the shortest path
	 * from source to target, moving through grid cells. The path respects the grid structure
	 * and can optionally be constrained to move only through specified cells.</p>
	 * 
	 * <p>The algorithm considers:</p>
	 * <ul>
	 *   <li>Grid topology (square vs hexagonal)</li>
	 *   <li>Neighbor relationships</li>
	 *   <li>Optional cell restrictions (via the 'on' parameter)</li>
	 * </ul>
	 *
	 * @param scope the current execution scope
	 * @param source the starting shape/location
	 * @param target the destination shape/location
	 * @param topo the topology to use for distance calculations
	 * @param on optional list of agents to restrict the path to (can be null)
	 * @return the computed spatial path, or null if no path exists
	 * @throws GamaRuntimeException if path computation fails
	 */
	IPath computeShortestPathBetween(final IScope scope, final IShape source, final IShape target, final ITopology topo,
			final IList<IAgent> on) throws GamaRuntimeException;

	/**
	 * Computes the weighted shortest path between two shapes on the grid.
	 * 
	 * <p>Similar to {@link #computeShortestPathBetween}, but uses weighted edges where each cell
	 * has an associated cost/weight. This is useful for modeling terrain difficulty, movement costs,
	 * or other factors that make some paths more expensive than others.</p>
	 * 
	 * <p>The weights are provided as a map where each agent (grid cell) is associated with a
	 * numeric weight value. Higher weights make the cell more costly to traverse.</p>
	 * 
	 * <p>Common applications:</p>
	 * <ul>
	 *   <li>Elevation-based pathfinding (prefer flat terrain)</li>
	 *   <li>Road network traversal (speed limits, traffic)</li>
	 *   <li>Cost-optimal routing</li>
	 * </ul>
	 *
	 * @param scope the current execution scope
	 * @param source the starting shape/location
	 * @param target the destination shape/location
	 * @param topo the topology to use for distance calculations
	 * @param on map of agents to their traversal weights/costs
	 * @return the computed weighted spatial path, or null if no path exists
	 * @throws GamaRuntimeException if path computation fails
	 */
	IPath computeShortestPathBetweenWeighted(final IScope scope, final IShape source, final IShape target,
			final ITopology topo, final Map<IAgent, Object> on) throws GamaRuntimeException;

	// public abstract Iterator<IAgent> getNeighborsOf(final IScope scope, final
	// IPoint shape, final Double
	// distance,
	// IAgentFilter filter);

	/**
	 * Gets the neighbors of a given shape within a specified distance.
	 * 
	 * <p>Returns all grid cells (agents) that are neighbors of the specified shape, within
	 * the given distance radius. The distance is measured in grid cells (e.g., distance=1
	 * returns immediate neighbors, distance=2 returns neighbors of neighbors, etc.).</p>
	 * 
	 * <p>Neighbor relationships depend on the grid type:</p>
	 * <ul>
	 *   <li><strong>Square grid:</strong> Von Neumann (4 neighbors) or Moore (8 neighbors)</li>
	 *   <li><strong>Hexagonal grid:</strong> 6 equidistant neighbors</li>
	 * </ul>
	 * 
	 * <p>The filter parameter allows selective neighbor retrieval based on custom criteria.</p>
	 *
	 * @param scope the current execution scope
	 * @param shape the shape whose neighbors to find
	 * @param distance the maximum distance (in grid cells) to search
	 * @param filter optional filter to select specific neighbors (can be null for all neighbors)
	 * @return set of neighboring agents within the specified distance
	 */
	Set<IAgent> getNeighborsOf(final IScope scope, final IShape shape, final Double distance, IAgentFilter filter);

	/**
	 * Calculates the Manhattan distance between two shapes on the grid.
	 * 
	 * <p>Manhattan distance (also called taxicab distance) is the sum of horizontal and vertical
	 * distances between two cells. For cells at positions (x1, y1) and (x2, y2), the Manhattan
	 * distance is |x2-x1| + |y2-y1|.</p>
	 * 
	 * <p>This metric is particularly useful for grid-based movement where diagonal moves are
	 * not allowed or when estimating the minimum number of grid steps needed.</p>
	 *
	 * @param g1 the first shape
	 * @param g2 the second shape
	 * @return the Manhattan distance as an integer number of grid cells
	 */
	int manhattanDistanceBetween(final IShape g1, final IShape g2);

	/**
	 * Gets the cell/place (shape) at the specified coordinate.
	 * 
	 * <p>Returns the geometric shape representing the grid cell at the given point.
	 * Unlike {@link #getAgentAt}, this returns the shape geometry rather than the agent.</p>
	 *
	 * @param c the point coordinate
	 * @return the shape of the grid cell at that location, or null if outside bounds
	 */
	IShape getPlaceAt(final IPoint c);

	/**
	 * Gets the display data for rendering the grid.
	 * 
	 * <p>Returns an integer array containing data used for optimized grid visualization.
	 * This is typically used by display layers to efficiently render large grids without
	 * querying each cell individually.</p>
	 * 
	 * <p>The exact format depends on the grid implementation and display requirements.</p>
	 *
	 * @return array of display data values
	 */
	int[] getDisplayData();

	/**
	 * Gets the current grid values as a double array.
	 * 
	 * <p>Returns a flat array of double values representing the current state of grid cells.
	 * This is commonly used for:</p>
	 * <ul>
	 *   <li>Diffusion computations</li>
	 *   <li>Grid visualization</li>
	 *   <li>Statistical analysis</li>
	 * </ul>
	 *
	 * @return array of grid values (typically one value per cell)
	 */
	double[] getGridValue();

	/**
	 * Computes and returns grid values by applying an expression to each cell.
	 * 
	 * <p>Evaluates the given expression for each agent (cell) in the grid and returns
	 * the results as a double array. This is useful for extracting attribute values,
	 * computing derived values, or preparing data for visualization or diffusion.</p>
	 * 
	 * <p>For example, extracting elevation values: <code>getGridValueOf(scope, "elevation")</code></p>
	 * 
	 * <p>The expression is evaluated in the context of each cell agent, so it can access
	 * the agent's attributes and perform calculations.</p>
	 *
	 * @param scope the current execution scope
	 * @param expr the expression to evaluate for each cell (must not be null)
	 * @return a double array the size of the grid containing the computed values
	 */
	double[] getGridValueOf(IScope scope, IExpression expr);

	/**
	 * Checks if the grid uses toroidal topology.
	 * 
	 * <p>A toroidal grid wraps around at the edges, so the rightmost column is adjacent
	 * to the leftmost column, and the top row is adjacent to the bottom row. This creates
	 * a continuous surface without boundaries, like a donut.</p>
	 * 
	 * <p>Toroidal grids are useful for avoiding edge effects in cellular automata and
	 * other spatial models.</p>
	 *
	 * @return true if the grid wraps around at edges (toroidal), false for bounded grid
	 */
	boolean isTorus();

	/**
	 * Gets the neighborhood definition for this grid.
	 * 
	 * <p>The neighborhood defines how cells are related to their neighbors. Common types include:</p>
	 * <ul>
	 *   <li><strong>Von Neumann:</strong> 4 neighbors (N, S, E, W)</li>
	 *   <li><strong>Moore:</strong> 8 neighbors (including diagonals)</li>
	 *   <li><strong>Hexagonal:</strong> 6 neighbors (for hex grids)</li>
	 *   <li><strong>Custom:</strong> User-defined neighbor patterns</li>
	 * </ul>
	 *
	 * @return the neighborhood object managing neighbor relationships
	 * @see INeighborhood
	 */
	INeighborhood getNeighborhood();

	/**
	 * Gets the environment frame (bounding shape) of the grid.
	 * 
	 * <p>Returns the geometric shape that defines the overall bounds of the grid.
	 * This is typically a rectangle encompassing all grid cells.</p>
	 *
	 * @return the shape representing the grid's spatial extent
	 */
	IShape getEnvironmentFrame();

	/**
	 * Gets the X coordinate (column index) of a geometry in the grid.
	 * 
	 * <p>Converts a geometry or shape to its grid column index (X coordinate).
	 * For a cell agent, this returns which column it belongs to (0-based).</p>
	 *
	 * @param geometry the geometry to query
	 * @return the column index (X coordinate) in the grid
	 */
	int getX(IShape geometry);

	/**
	 * Gets the Y coordinate (row index) of a geometry in the grid.
	 * 
	 * <p>Converts a geometry or shape to its grid row index (Y coordinate).
	 * For a cell agent, this returns which row it belongs to (0-based).</p>
	 *
	 * @param geometry the geometry to query
	 * @return the row index (Y coordinate) in the grid
	 */
	int getY(IShape geometry);

	/**
	 * Disposes of the grid and releases all associated resources.
	 * 
	 * <p>Cleans up internal data structures, caches, and references. Should be called
	 * when the grid is no longer needed to prevent memory leaks.</p>
	 */
	@Override
	void dispose();

	/**
	 * Checks if the grid uses individual shapes for each cell.
	 * 
	 * <p>Determines whether each grid cell has its own unique geometry object (true)
	 * or if cells share geometry to save memory (false). Using individual shapes
	 * allows cells to be modified independently but uses more memory.</p>
	 *
	 * @return true if each cell has its own shape, false if shapes are shared
	 */
	boolean usesIndiviualShapes();

	/**
	 * Checks if the grid uses a cache for neighbor queries.
	 * 
	 * <p>When enabled, neighbor relationships are pre-computed and cached, which speeds up
	 * repeated neighbor queries at the cost of memory. Useful when neighbors are frequently
	 * accessed (e.g., in cellular automata).</p>
	 *
	 * @return true if neighbors are cached, false if computed on-demand
	 */
	boolean usesNeighborsCache();

	/**
	 * Gets the name of the pathfinding optimizer used by the grid.
	 * 
	 * <p>Returns the strategy used for optimizing shortest path computations.
	 * Common optimizers include "Dijkstra", "A*", "BFS", etc.</p>
	 *
	 * @return the optimizer name as a string
	 */
	String optimizer();

	/**
	 * Gets the species representing grid cells.
	 * 
	 * <p>Returns the species definition for agents that represent individual grid cells.
	 * This species defines the attributes and behaviors available to grid cells.</p>
	 *
	 * @return the cell species
	 */
	ISpecies getCellSpecies();

	/**
	 * Sets the grid values from a double array.
	 * 
	 * <p>Updates the internal grid values with the provided array. This is typically used
	 * after diffusion operations or when loading grid data from external sources.</p>
	 * 
	 * <p>The array must have the same size as the grid (rows × columns).</p>
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param gridValues the new grid values to set
	 * @date 27 août 2023
	 */
	void setGridValues(double[] gridValues);

}
