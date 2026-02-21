/*******************************************************************************************************
 *
 * ISpatialIndex.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Collection;
import java.util.Collections;

import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.IAgentFilter;

/**
 * The Interface ISpatialIndex.
 * 
 * <p>This interface defines a spatial indexing structure for efficient spatial queries on agents.
 * A spatial index organizes agents based on their location, enabling fast retrieval of agents
 * within a given area or distance, which is crucial for performance in spatial simulations.</p>
 * 
 * <h3>Purpose:</h3>
 * <p>Spatial indexes solve the problem of finding nearby agents efficiently. Without an index,
 * finding neighbors would require checking every agent in the simulation (O(n) complexity).
 * With a spatial index, queries can be answered in O(log n) or better.</p>
 * 
 * <h3>Common Spatial Index Implementations:</h3>
 * <ul>
 *   <li><strong>Quadtree:</strong> Divides 2D space into quadrants recursively</li>
 *   <li><strong>R-tree:</strong> Uses bounding rectangles to group nearby objects</li>
 *   <li><strong>Grid-based:</strong> Divides space into uniform cells</li>
 *   <li><strong>Compound:</strong> Combines multiple indexes for different agent populations</li>
 * </ul>
 * 
 * <h3>Key Operations:</h3>
 * <ul>
 *   <li><strong>Insert:</strong> Add an agent to the index when it's created or moves</li>
 *   <li><strong>Remove:</strong> Remove an agent from the index when it dies or moves</li>
 *   <li><strong>Query:</strong> Find agents within a distance, envelope, or matching criteria</li>
 * </ul>
 * 
 * <h3>Query Types:</h3>
 * <ul>
 *   <li>Distance queries: Find all agents within distance D of a point</li>
 *   <li>Envelope queries: Find all agents intersecting or contained in a rectangle</li>
 *   <li>First-at-distance: Find the closest agent (or N closest agents)</li>
 *   <li>Filtered queries: Apply custom filters to restrict results</li>
 * </ul>
 * 
 * <h3>Performance Considerations:</h3>
 * <p>The effectiveness of a spatial index depends on:</p>
 * <ul>
 *   <li>Agent distribution (clustered vs uniform)</li>
 *   <li>Query patterns (many small queries vs few large queries)</li>
 *   <li>Agent mobility (frequent updates vs static agents)</li>
 *   <li>Population size (larger populations benefit more from indexing)</li>
 * </ul>
 * 
 * <h3>Null Pattern:</h3>
 * <p>The interface provides a NULL_INDEX implementation that performs no operations,
 * useful for populations that don't require spatial indexing.</p>
 * 
 * <h3>Usage Example:</h3>
 * <pre>
 * // Query all agents within 50 units of a location
 * Collection&lt;IAgent&gt; nearby = spatialIndex.allAtDistance(scope, source, 50.0, filter);
 * 
 * // Find the closest agent
 * IAgent closest = spatialIndex.firstAtDistance(scope, source, 100.0, filter);
 * </pre>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * 
 * @see IAgentFilter
 * @see Compound
 */
public interface ISpatialIndex {

	/**
	 * A null spatial index that performs no operations.
	 * 
	 * <p>This is a null object pattern implementation that can be used for populations
	 * that don't require spatial indexing (e.g., very small populations, or non-spatial
	 * agent collections). All query methods return empty results.</p>
	 */
	ISpatialIndex NULL_INDEX = new ISpatialIndex() {};

	/**
	 * Inserts an agent into the spatial index.
	 * 
	 * <p>Adds the agent to the index structure based on its current location. This should be
	 * called when an agent is created or whenever it moves to a new location.</p>
	 * 
	 * <p>The agent's envelope (bounding box) is used to determine its position in the index.
	 * Implementations may use the agent's geometry, location point, or other spatial attributes.</p>
	 * 
	 * <p>Default implementation does nothing (for null index or non-indexed populations).</p>
	 *
	 * @param agent the agent to insert into the index
	 */
	default void insert(final IAgent agent) {}

	/**
	 * Removes an agent from the spatial index.
	 * 
	 * <p>Removes the agent from its previous position in the index. This should be called
	 * before an agent moves to a new location (followed by insert), or when an agent dies.</p>
	 * 
	 * <p>The previous envelope is required because the agent's current envelope may have
	 * already changed, and the index needs to know the old position to remove it correctly.</p>
	 * 
	 * <p>Default implementation does nothing (for null index or non-indexed populations).</p>
	 *
	 * @param previous the agent's previous bounding envelope (before movement or removal)
	 * @param agent the agent to remove from the index
	 */
	default void remove(final IEnvelope previous, final IAgent agent) {}

	/**
	 * Finds the first (closest) agent within a specified distance.
	 * 
	 * <p>Returns the single agent that is closest to the source shape and within the
	 * specified maximum distance. If multiple agents are at the same distance, one is
	 * chosen arbitrarily.</p>
	 * 
	 * <p>The filter can be used to restrict which agents are considered (e.g., only
	 * agents of a certain species, or agents matching specific criteria).</p>
	 * 
	 * <p>This is more efficient than finding all nearby agents and selecting the closest,
	 * especially for large populations.</p>
	 *
	 * @param scope the current execution scope
	 * @param source the source shape from which to measure distance
	 * @param dist the maximum distance to search
	 * @param f the filter to apply to candidate agents (null for no filter)
	 * @return the closest agent within the distance, or null if none found
	 */
	default IAgent firstAtDistance(final IScope scope, final IShape source, final double dist, final IAgentFilter f) {
		return null;
	}

	/**
	 * Finds multiple closest agents within a specified distance.
	 * 
	 * <p>Returns up to 'number' agents that are closest to the source shape and within the
	 * specified maximum distance. Agents already in the 'alreadyChosen' collection are excluded,
	 * which is useful for finding the "next N closest" agents iteratively.</p>
	 * 
	 * <p>Results are not necessarily sorted by distance, though implementations may choose
	 * to do so for convenience.</p>
	 *
	 * @param scope the current execution scope
	 * @param source the source shape from which to measure distance
	 * @param dist the maximum distance to search
	 * @param f the filter to apply to candidate agents (null for no filter)
	 * @param number the maximum number of agents to return
	 * @param alreadyChosen agents to exclude from the results (may be null or empty)
	 * @return collection of up to 'number' closest agents, or empty if none found
	 */
	default Collection<IAgent> firstAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f, final int number, final Collection<IAgent> alreadyChosen) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Finds all agents within or intersecting an envelope.
	 * 
	 * <p>Returns all agents whose bounding boxes intersect (or are contained within, if
	 * contained=true) the specified envelope. This is useful for rectangular area queries.</p>
	 * 
	 * <p>An envelope is a rectangular bounding box defined by min/max X and Y coordinates.
	 * This is one of the most efficient types of spatial queries.</p>
	 *
	 * @param scope the current execution scope
	 * @param source the source shape (may be used for additional filtering)
	 * @param envelope the rectangular area to search
	 * @param f the filter to apply to candidate agents (null for no filter)
	 * @param contained if true, only return agents fully contained in the envelope;
	 *                  if false, return agents that intersect the envelope
	 * @return collection of agents in the envelope, or empty if none found
	 */
	default Collection<IAgent> allInEnvelope(final IScope scope, final IShape source, final IEnvelope envelope,
			final IAgentFilter f, final boolean contained) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Finds all agents within a specified distance.
	 * 
	 * <p>Returns all agents whose distance from the source shape is less than or equal
	 * to the specified distance threshold. This is one of the most common spatial queries.</p>
	 * 
	 * <p>Distance is typically measured as Euclidean distance from the source shape's
	 * boundary to the target agent's boundary. For point agents and point sources,
	 * this is simply the straight-line distance between points.</p>
	 * 
	 * <p>The filter can restrict results to specific agent types or matching custom criteria.</p>
	 *
	 * @param scope the current execution scope
	 * @param source the source shape from which to measure distance
	 * @param dist the maximum distance threshold
	 * @param f the filter to apply to candidate agents (null for no filter)
	 * @return collection of all agents within the distance, or empty if none found
	 */
	default Collection<IAgent> allAtDistance(final IScope scope, final IShape source, final double dist,
			final IAgentFilter f) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Disposes of the spatial index and releases resources.
	 * 
	 * <p>Cleans up internal data structures and frees memory used by the index.
	 * Should be called when the index is no longer needed, typically when a
	 * population or simulation is disposed.</p>
	 * 
	 * <p>Default implementation does nothing.</p>
	 */
	default void dispose() {}

	/**
	 * The Interface Compound.
	 * 
	 * <p>A compound spatial index manages multiple individual spatial indexes, typically one
	 * per species or agent population. This allows efficient querying across all populations
	 * while maintaining separate indexes for each species.</p>
	 * 
	 * <h4>Benefits:</h4>
	 * <ul>
	 *   <li>Species-specific optimizations (different index types per species)</li>
	 *   <li>Efficient cross-species queries</li>
	 *   <li>Selective index updates (only affected species)</li>
	 *   <li>Support for dynamic species addition/removal</li>
	 * </ul>
	 * 
	 * <h4>Use Cases:</h4>
	 * <ul>
	 *   <li>Multi-species simulations with many agents</li>
	 *   <li>Hierarchical agent populations</li>
	 *   <li>Simulations where species have different spatial behaviors</li>
	 * </ul>
	 */
	public interface Compound extends ISpatialIndex {

		/**
		 * Removes a species and its agents from the compound index.
		 * 
		 * <p>Removes the spatial index associated with the given species, effectively
		 * removing all agents of that species from spatial queries. This is called
		 * when a species is removed from the simulation.</p>
		 *
		 * @param species the species whose index should be removed
		 */
		void remove(final ISpecies species);

		/**
		 * Updates the compound index structure.
		 * 
		 * <p>Rebuilds or updates internal index structures to reflect changes in agent
		 * positions. This may be called periodically or after significant changes to
		 * improve query performance.</p>
		 * 
		 * <p>The parallel parameter enables multi-threaded index updates for better
		 * performance with large populations.</p>
		 *
		 * @param scope the current execution scope
		 * @param envelope the bounding envelope for the update
		 * @param parallel true to use parallel processing for the update
		 */
		void update(IScope scope, IEnvelope envelope, boolean parallel);

		/**
		 * Merges another compound spatial index into this one.
		 * 
		 * <p>Combines the species indexes from another compound index into this one.
		 * This is useful when merging populations or combining simulation results.</p>
		 *
		 * @param spatialIndex the compound index to merge into this one
		 */
		void mergeWith(Compound spatialIndex);

	}

}