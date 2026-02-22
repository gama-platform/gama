/*******************************************************************************************************
 *
 * ITopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.topology;

import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.topology.ISpatialIndex;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IPath;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.misc.IValue;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.IAgentFilter;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Interface ITopology.
 * 
 * Represents the spatial structure of an environment in GAMA simulations. A topology defines how space is organized
 * and provides methods for spatial operations such as distance calculations, path finding, neighbor queries, and
 * location validation.
 * 
 * <p>
 * GAMA supports different types of topologies:
 * <ul>
 * <li><b>Continuous topologies:</b> Allow free movement within boundaries defined by a shape</li>
 * <li><b>Grid topologies:</b> Discrete space divided into cells (typically agents)</li>
 * <li><b>Graph topologies:</b> Connectivity defined by a spatial graph structure</li>
 * <li><b>Toroidal topologies:</b> Wrap-around boundaries for continuous movement</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A topology maintains:
 * <ul>
 * <li>An environment shape that defines its boundaries</li>
 * <li>A spatial index for efficient spatial queries</li>
 * <li>Methods for computing distances, paths, and directions</li>
 * <li>Methods for querying agents by location and spatial relationships</li>
 * </ul>
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 */
@vars ({ @variable (
		name = IKeyword.ENVIRONMENT,
		type = IType.GEOMETRY,
		doc = { @doc ("Returns the environment of this topology, either an agent or a geometry, which defines its boundaries") }),
		@variable (
				name = IKeyword.PLACES,
				type = IType.CONTAINER,
				of = IType.GEOMETRY,
				doc = { @doc ("Returns the list of discrete places that compose this topology (e.g. the list of cells for a grid topology). The continuous topologies will return a singleton list with their environment") }),
		// Could be replaced by "geometries"

})
public interface ITopology extends IValue {

	/**
	 * The Enum SpatialRelation.
	 * 
	 * Defines the types of spatial relationships that can be tested between geometries in a topology. These relations
	 * are used for spatial queries and filtering operations.
	 */
	enum SpatialRelation {

		/** Two geometries overlap (share some but not all area). */
		OVERLAP,

		/** One geometry completely covers another. */
		COVER,

		/** One geometry is completely inside another. */
		INSIDE,

		/** Two geometries touch at their boundaries but do not overlap. */
		TOUCH,

		/** Two geometries cross each other (intersect but neither contains the other). */
		CROSS,

		/** Two geometries partially overlap (share some area). */
		PARTIALLY_OVERLAP
	}

	/**
	 * Returns the spatial index used by this topology for efficient spatial queries.
	 * 
	 * The spatial index maintains agent locations and enables fast proximity searches, neighbor queries, and spatial
	 * filtering.
	 *
	 * @return the spatial index, or {@link ISpatialIndex#NULL_INDEX} if no index is used
	 */
	ISpatialIndex getSpatialIndex();

	/**
	 * Initializes this topology with a population of agents.
	 * 
	 * This method is called when the topology is first associated with a population, allowing it to set up any
	 * necessary data structures or spatial indices.
	 *
	 * @param scope
	 *            the execution scope
	 * @param pop
	 *            the population of agents using this topology
	 * @throws GamaRuntimeException
	 *             if initialization fails
	 */
	void initialize(IScope scope, IPopulation<? extends IAgent> pop) throws GamaRuntimeException;

	/**
	 * Updates the topology when an agent's location or geometry changes.
	 * 
	 * This method updates internal data structures (like spatial indices) to reflect the agent's new position.
	 *
	 * @param previous
	 *            the agent's previous envelope (bounding box), or null if the agent is new
	 * @param agent
	 *            the agent whose location has changed
	 */
	void updateAgent(IEnvelope previous, IAgent agent);

	/**
	 * Removes an agent from this topology's internal data structures.
	 * 
	 * This method is called when an agent dies or is otherwise removed from the simulation.
	 *
	 * @param agent
	 *            the agent to remove
	 */
	void removeAgent(final IAgent agent);

	/**
	 * Returns the list of toroidal copies of a geometry in a toroidal topology.
	 * 
	 * In a toroidal (wrap-around) topology, geometries near edges may have virtual copies on the opposite side. This
	 * method returns all such copies for spatial calculations.
	 *
	 * @param geom
	 *            the geometry to get toroidal copies of
	 * @return the list of toroidal geometry copies, or an empty list if not a toroidal topology
	 */
	List<Geometry> listToroidalGeometries(final Geometry geom);

	/**
	 * Returns the N agents closest to a source shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the reference shape to measure distances from
	 * @param filter
	 *            optional filter to select which agents to consider (can be null for all agents)
	 * @param number
	 *            the maximum number of closest agents to return
	 * @return a collection of up to N agents closest to the source, sorted by distance
	 */
	Collection<IAgent> getAgentClosestTo(IScope scope, final IShape source, IAgentFilter filter, int number);

	/**
	 * Returns the single agent closest to a source shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the reference shape to measure distances from
	 * @param filter
	 *            optional filter to select which agents to consider (can be null for all agents)
	 * @return the closest agent, or null if no agents match the filter
	 */
	IAgent getAgentClosestTo(IScope scope, final IShape source, IAgentFilter filter);

	/**
	 * Returns the agent farthest from a source shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the reference shape to measure distances from
	 * @param filter
	 *            optional filter to select which agents to consider (can be null for all agents)
	 * @return the farthest agent, or null if no agents match the filter
	 */
	IAgent getAgentFarthestTo(IScope scope, final IShape source, IAgentFilter filter);

	/**
	 * Returns all agents within a given distance from a source shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the reference shape to measure distances from
	 * @param distance
	 *            the maximum distance (in topology units)
	 * @param filter
	 *            optional filter to select which agents to consider (can be null for all agents)
	 * @return a collection of agents within the specified distance
	 * @throws GamaRuntimeException
	 *             if the query fails
	 */
	Collection<IAgent> getNeighborsOf(IScope scope, final IShape source, final Double distance, IAgentFilter filter)
			throws GamaRuntimeException;

	/**
	 * Returns all agents that have a specific spatial relationship with a source shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the reference shape to test relationships with
	 * @param f
	 *            optional filter to select which agents to consider (can be null for all agents)
	 * @param relation
	 *            the type of spatial relationship to test (e.g., OVERLAP, INSIDE, TOUCH)
	 * @return a collection of agents that satisfy the spatial relationship
	 */
	Collection<IAgent> getAgentsIn(IScope scope, final IShape source, final IAgentFilter f, SpatialRelation relation);

	/**
	 * Tests whether this topology is toroidal (has wrap-around boundaries).
	 * 
	 * In a toroidal topology, agents reaching one edge automatically appear on the opposite edge, creating a
	 * wrap-around effect.
	 *
	 * @return true if this is a toroidal topology, false otherwise
	 */
	boolean isTorus();

	/**
	 * Tests whether this topology is continuous (allows free movement).
	 * 
	 * Continuous topologies allow agents to occupy any position within the environment, as opposed to discrete
	 * topologies like grids where positions are restricted to cells.
	 *
	 * @return true if this is a continuous topology, false for discrete topologies
	 */
	boolean isContinuous();

	/**
	 * Computes the distance between two shapes in this topology.
	 * 
	 * The distance calculation respects the topology type (e.g., toroidal topologies compute wrap-around distances).
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the source geometry (cannot be null)
	 * @param target
	 *            the target geometry (cannot be null)
	 * @return the distance between the two geometries, or Double.MAX_VALUE if unreachable
	 * @throws GamaRuntimeException
	 *             if distance calculation fails
	 */
	Double distanceBetween(IScope scope, final IShape source, final IShape target);

	/**
	 * Computes the distance between two points in this topology.
	 * 
	 * The distance calculation respects the topology type (e.g., toroidal topologies compute wrap-around distances).
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the source point (cannot be null)
	 * @param target
	 *            the target point (cannot be null)
	 * @return the distance between the two points
	 */
	Double distanceBetween(IScope scope, final IPoint source, final IPoint target);

	/**
	 * Computes a path between two shapes in this topology.
	 * 
	 * The path respects the topology structure (e.g., graph topologies follow edges, continuous topologies use direct
	 * lines).
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting shape
	 * @param target
	 *            the destination shape
	 * @return a path from source to target, or null if no path exists
	 * @throws GamaRuntimeException
	 *             if path computation fails
	 */
	IPath pathBetween(IScope scope, final IShape source, final IShape target) throws GamaRuntimeException;

	/**
	 * Computes a path between two points in this topology.
	 * 
	 * The path respects the topology structure (e.g., graph topologies follow edges, continuous topologies use direct
	 * lines).
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point
	 * @param target
	 *            the destination point
	 * @return a path from source to target, or null if no path exists
	 * @throws GamaRuntimeException
	 *             if path computation fails
	 */
	IPath pathBetween(IScope scope, final IPoint source, final IPoint target) throws GamaRuntimeException;

	/**
	 * Computes the destination point from a source, given a direction, distance, and optional 2D movement.
	 * 
	 * This method calculates where an agent would end up after moving from the source point at a given angle and
	 * distance. The result respects topology boundaries and wrapping rules.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point
	 * @param direction
	 *            the heading angle in degrees (0-360, where 0 is east, 90 is north)
	 * @param distance
	 *            the distance to travel
	 * @param nullIfOutside
	 *            if true, returns null when the destination is outside the topology; if false, clamps or wraps the
	 *            destination
	 * @return the destination point, or null if outside and nullIfOutside is true
	 */
	IPoint getDestination(IScope scope, final IPoint source, final double direction, final double distance,
			boolean nullIfOutside);

	/**
	 * Computes the 3D destination point from a source, given heading, pitch, distance.
	 * 
	 * This method calculates where an agent would end up after moving from the source point with specified heading
	 * (horizontal angle), pitch (vertical angle), and distance in 3D space.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point (can have z coordinate)
	 * @param heading
	 *            the horizontal angle in degrees (0-360, where 0 is east, 90 is north)
	 * @param pitch
	 *            the vertical angle in degrees (-90 to 90, where positive is upward)
	 * @param distance
	 *            the distance to travel in 3D space
	 * @param nullIfOutside
	 *            if true, returns null when the destination is outside the topology; if false, clamps or wraps the
	 *            destination
	 * @return the 3D destination point, or null if outside and nullIfOutside is true
	 */
	IPoint getDestination3D(IScope scope, final IPoint source, final double heading, final double pitch,
			final double distance, boolean nullIfOutside);

	/**
	 * Returns a random location within the boundaries of this topology's environment.
	 * 
	 * The returned point is guaranteed to be a valid location within the topology.
	 *
	 * @param scope
	 *            the execution scope (provides the random generator)
	 * @return a random point within the topology, or null if no random locations are available
	 */
	IPoint getRandomLocation(IScope scope);

	/**
	 * Returns the collection of discrete places (geometries) that compose this topology.
	 * 
	 * For continuous topologies, this returns a singleton list containing the environment shape. For discrete
	 * topologies (like grids), this returns all the cells or places.
	 *
	 * @return a container of geometries representing the places in this topology
	 */
	@getter (IKeyword.PLACES)
	IContainer<?, IShape> getPlaces();

	/**
	 * Returns the environment geometry that defines the boundaries of this topology.
	 * 
	 * The environment is the overall shape that contains all valid locations in the topology.
	 *
	 * @return the environment shape
	 */
	@getter (IKeyword.ENVIRONMENT)
	IShape getEnvironment();

	/**
	 * Normalizes a location to ensure it is valid within this topology.
	 * 
	 * For toroidal topologies, this wraps the point to the opposite side if it's outside. For bounded topologies, it
	 * can either clamp the point to the boundary or return null.
	 *
	 * @param scope
	 *            the execution scope
	 * @param p
	 *            the point to normalize
	 * @param nullIfOutside
	 *            if true, returns null when the point is outside; if false, normalizes it to a valid location
	 * @return a valid point within the topology, or null if outside and nullIfOutside is true
	 */
	IPoint normalizeLocation(IScope scope, final IPoint p, boolean nullIfOutside);

	/**
	 * @throws GamaRuntimeException
	 *             Called by a population to tell this topology that the shape of its host has changed. If the
	 *             environment of the topology depends on the shape of the host, the topology can choose to adapt in
	 *             consequence.
	 *
	 * @param pop
	 *            the population to which this topology is attached.
	 */
	/**
	 * Returns the width of this topology's environment.
	 *
	 * @return the width in topology units
	 */
	double getWidth();

	/**
	 * Returns the height of this topology's environment.
	 *
	 * @return the height in topology units
	 */
	double getHeight();

	/**
	 * Disposes of this topology and releases any resources it holds.
	 * 
	 * This method should be called when the topology is no longer needed to free up spatial indices and other data
	 * structures.
	 */
	void dispose();

	/**
	 * Tests whether a point is a valid location within this topology.
	 * 
	 * A valid location is one that lies within the topology's boundaries and respects any constraints.
	 *
	 * @param scope
	 *            the execution scope
	 * @param p
	 *            the point to test
	 * @return true if the point is valid, false otherwise
	 */
	boolean isValidLocation(IScope scope, IPoint p);

	/**
	 * Tests whether a geometry is valid within this topology.
	 * 
	 * A valid geometry typically lies entirely within the topology's boundaries.
	 *
	 * @param scope
	 *            the execution scope
	 * @param g
	 *            the geometry to test
	 * @return true if the geometry is valid, false otherwise
	 */
	boolean isValidGeometry(IScope scope, IShape g);

	/**
	 * Computes the direction (heading angle) from one shape to another in degrees.
	 * 
	 * The angle is measured clockwise from east (0 degrees), with north at 90 degrees.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the source shape (reference point)
	 * @param target
	 *            the target shape (destination point)
	 * @return the direction in degrees (0-360), or null if either geometry is invalid
	 * @throws GamaRuntimeException
	 *             if direction calculation fails
	 */
	Double directionInDegreesTo(IScope scope, IShape source, IShape target);

	/**
	 * Computes the K shortest paths between two shapes.
	 * 
	 * This is primarily useful for graph topologies where multiple paths may exist.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting shape
	 * @param target
	 *            the destination shape
	 * @param k
	 *            the number of paths to find
	 * @return a list of up to K paths, ordered by length (shortest first)
	 */
	IList<IPath> kPathsBetween(IScope scope, IShape source, IShape target, int k);

	/**
	 * Computes the K shortest paths between two points.
	 * 
	 * This is primarily useful for graph topologies where multiple paths may exist.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point
	 * @param target
	 *            the destination point
	 * @param k
	 *            the number of paths to find
	 * @return a list of up to K paths, ordered by length (shortest first)
	 */
	IList<IPath> kPathsBetween(IScope scope, IPoint source, IPoint target, int k);

	/**
	 * Sets the root topology for this topology.
	 * 
	 * Some topologies are composed of multiple layers or sub-topologies. This method establishes the root topology in
	 * such hierarchies.
	 *
	 * @param scope
	 *            the execution scope
	 * @param rt
	 *            the root topology
	 */
	void setRoot(IScope scope, ITopology rt);

	/**
	 * Returns the integer value representation of this topology.
	 * 
	 * By default, returns the number of places in the topology.
	 *
	 * @param scope
	 *            the execution scope
	 * @return the number of places as an integer
	 */
	@Override
	default int intValue(final IScope scope) {
		return this.getPlaces().intValue(scope);
	}

	/**
	 * Returns the floating-point value representation of this topology.
	 * 
	 * By default, returns the area of the environment shape.
	 *
	 * @param scope
	 *            the execution scope
	 * @return the area of the environment as a double
	 */
	@Override
	default double floatValue(final IScope scope) {
		return this.getEnvironment().floatValue(scope);
	}

	/**
	 * Serializes this topology to JSON format.
	 *
	 * @param json
	 *            the JSON serialization context
	 * @return a JSON representation of this topology
	 */
	@Override
	default IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), IKeyword.ENVIRONMENT, getEnvironment());
	}

	/**
	 * Merges this topology with another topology.
	 * 
	 * The default implementation does nothing. Subclasses can override to provide merge behavior.
	 *
	 * @param topology
	 *            the topology to merge with
	 */
	default void mergeWith(final ITopology topology) {}

}