/*******************************************************************************************************
 *
 * MovingSkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import static gama.api.utils.geometry.GeometryUtils.getFirstPointOf;
import static gama.api.utils.geometry.GeometryUtils.getLastPointOf;
import static gama.api.utils.geometry.GeometryUtils.getPointsOf;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.ArrayUtils.indexOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;

import gama.annotations.action;
import gama.annotations.arg;
import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.skill;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.IConcept;
import gama.api.GAMA;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaPathFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.factories.GamaTopologyFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPath;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.skill.Skill;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;
import gama.api.utils.IAgentFilter;
import gama.api.utils.collections.Collector;
import gama.api.utils.geometry.GamaPointFactory;
import gama.api.utils.list.GamaListFactory;
import gama.api.utils.map.GamaMapFactory;
import gama.core.topology.filter.In;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.core.topology.graph.GraphTopology;
import gama.core.topology.grid.GamaSpatialMatrix;
import gama.core.topology.grid.GridTopology;
import gama.core.util.path.GamaSpatialPath;
import gama.gaml.operators.Maths;
import gama.gaml.operators.Random;
import gama.gaml.operators.spatial.SpatialCreation;
import gama.gaml.operators.spatial.SpatialPunctal;
import gama.gaml.operators.spatial.SpatialRelations;

/**
 * MovingSkill : This class is intended to define the minimal set of behaviours required from an agent that is able to
 * move. Each member that has a meaning in GAML is annotated with the respective tags (vars, getter, setter, init,
 * action & args)
 *
 * @author drogoul 4 juil. 07
 */

@doc ("The moving skill is intended to define the minimal set of behaviours required for agents that are able to move on different topologies")
@vars ({ @variable (
		name = IKeyword.LOCATION,
		type = IType.POINT,
		depends_on = IKeyword.SHAPE,
		doc = @doc ("Represents the current position of the agent")),
		@variable (
				name = IKeyword.SPEED,
				type = IType.FLOAT,
				init = "1.0",
				doc = @doc ("Represents the speed of the agent (in meter/second)")),
		@variable (
				name = IKeyword.HEADING,
				type = IType.FLOAT,
				init = "rnd(360.0)",
				doc = @doc ("Represents the absolute heading of the agent in degrees.")),
		@variable (
				name = "current_path",
				type = IType.PATH,
				init = "nil",
				doc = @doc ("Represents the path on which the agent is moving on (goto action on a graph)")),
		@variable (
				name = "current_edge",
				type = IType.GEOMETRY,
				init = "nil",
				doc = @doc ("Represents the agent/geometry on which the agent is located (only used with a graph)")),
		@variable (
				name = IKeyword.REAL_SPEED,
				type = IType.FLOAT,
				init = "0.0",
				doc = @doc ("Represents the actual speed of the agent (in meter/second)")), })
@skill (
		name = IKeyword.MOVING_SKILL,
		concept = { IConcept.SKILL, IConcept.AGENT_MOVEMENT })
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class MovingSkill extends Skill {

	/** The Constant CACHE_KEY_TOPOLOGY. */
	// Performance optimization: Cache frequently accessed values to avoid repeated lookups
	private static final String CACHE_KEY_TOPOLOGY = "__cached_topology__";

	/** The Constant CACHE_KEY_GRAPH. */
	private static final String CACHE_KEY_GRAPH = "__cached_graph__";

	/** The Constant DISTANCE_PRECISION_THRESHOLD. */
	private static final double DISTANCE_PRECISION_THRESHOLD = 0.01;

	/** The Constant REUSABLE_SHAPE_LIST. */
	// Object pooling for memory optimization - reusable collections
	private static final ThreadLocal<List<IShape>> REUSABLE_SHAPE_LIST = ThreadLocal.withInitial(ArrayList::new);

	/** The Constant REUSABLE_DOUBLE_LIST. */
	private static final ThreadLocal<List<Double>> REUSABLE_DOUBLE_LIST = ThreadLocal.withInitial(ArrayList::new);

	/**
	 * Gets the topology with caching for improved performance.
	 *
	 * @param agent
	 *            the agent
	 * @return the cached or computed topology
	 */
	@Override
	protected ITopology getTopology(final IAgent agent) {
		if (agent == null) return null;

		ITopology cachedTopology = (ITopology) agent.getAttribute(CACHE_KEY_TOPOLOGY);
		if (cachedTopology == null) {
			cachedTopology = agent.getTopology();
			agent.setAttribute(CACHE_KEY_TOPOLOGY, cachedTopology);
		}
		return cachedTopology;
	}

	/**
	 * Optimized distance calculation between two points. Uses Euclidean distance with early termination for
	 * performance.
	 *
	 * @param point1
	 *            the first point
	 * @param point2
	 *            the second point
	 * @return the distance between points
	 */
	protected static double calculateOptimizedDistance(final IPoint point1, final IPoint point2) {
		if (point1 == null || point2 == null) return Double.MAX_VALUE;

		// Quick check for identical points
		if (point1 == point2 || point1.equalsWithTolerance(point2, DISTANCE_PRECISION_THRESHOLD)) return 0.0;

		return point1.euclidianDistanceTo(point2);
	}

	/**
	 * Optimized distance calculation between a point and segment. Performance-optimized version with reduced object
	 * allocations.
	 *
	 * @param point
	 *            the point
	 * @param segmentStart
	 *            the segment start
	 * @param segmentEnd
	 *            the segment end
	 * @return the distance from point to segment
	 */
	protected static double calculatePointToSegmentDistance(final IPoint point, final IPoint segmentStart,
			final IPoint segmentEnd) {
		if (point == null || segmentStart == null || segmentEnd == null) return Double.MAX_VALUE;

		return Distance.pointToSegment(point.toCoordinate(), segmentStart.toCoordinate(), segmentEnd.toCoordinate());
	}

	/**
	 * Clears thread-local object pools to prevent memory leaks. Should be called after intensive operations.
	 */
	protected static void clearObjectPools() {
		REUSABLE_SHAPE_LIST.get().clear();
		REUSABLE_DOUBLE_LIST.get().clear();
	}

	/**
	 * Gets the heading.
	 *
	 * @param agent
	 *            the agent
	 * @return the heading
	 */
	@getter (IKeyword.HEADING)
	public Double getHeading(final IAgent agent) {
		Double h = (Double) agent.getAttribute(IKeyword.HEADING);
		if (h == null) {
			h = agent.getScope().getRandom().next() * 360;
			setHeading(agent, h);
		}
		return Maths.checkHeading(h);
	}

	/**
	 * Sets the heading.
	 *
	 * @param agent
	 *            the agent
	 * @param heading
	 *            the heading
	 */
	@setter (IKeyword.HEADING)
	public void setHeading(final IAgent agent, final double heading) {
		if (agent == null) return;
		final double headingValue = heading % 360;
		final Double oldValue = (Double) agent.getAttribute(IKeyword.HEADING);
		if (oldValue == null || oldValue.doubleValue() != headingValue) {
			agent.setAttribute(IKeyword.HEADING, headingValue);
			agent.notifyVarValueChange(IKeyword.HEADING, headingValue);
		}
	}

	/**
	 * Gets the destination.
	 *
	 * @param agent
	 *            the agent
	 * @return the destination
	 */
	@getter (IKeyword.DESTINATION)
	public IPoint getDestination(final IAgent agent) {
		if (agent == null) return null;
		final IPoint actualLocation = agent.getLocation();
		final double dist = computeDistance(agent.getScope(), agent);
		final ITopology topology = getTopology(agent);
		return topology.getDestination(agent.getScope(), actualLocation, getHeading(agent), dist, false);
	}

	/**
	 * Sets the destination.
	 *
	 * @param agent
	 *            the agent
	 * @param p
	 *            the p
	 */
	@setter (IKeyword.DESTINATION)
	public void setDestination(final IAgent agent, final IPoint p) {
		// READ_ONLY
	}

	/**
	 * Gets the speed.
	 *
	 * @param agent
	 *            the agent
	 * @return the speed
	 */
	@getter (IKeyword.SPEED)
	public static Double getSpeed(final IAgent agent) {
		if (agent == null) return 0.0;
		return (Double) agent.getAttribute(IKeyword.SPEED);
	}

	/**
	 * Gets the real speed.
	 *
	 * @param agent
	 *            the agent
	 * @return the real speed
	 */
	@getter (IKeyword.REAL_SPEED)
	public static Double getRealSpeed(final IAgent agent) {
		if (agent == null) return 0.0;
		return (Double) agent.getAttribute(IKeyword.REAL_SPEED);
	}

	/**
	 * Sets the speed.
	 *
	 * @param agent
	 *            the agent
	 * @param s
	 *            the s
	 */
	@setter (IKeyword.SPEED)
	public static void setSpeed(final IAgent agent, final Double s) {
		if (agent == null) return;
		final Double oldValue = (Double) agent.getAttribute(IKeyword.SPEED);
		if (oldValue == null || oldValue.doubleValue() != s) {
			agent.setAttribute(IKeyword.SPEED, s);
			agent.notifyVarValueChange(IKeyword.SPEED, s);
		}
	}

	/**
	 * Sets the real speed.
	 *
	 * @param agent
	 *            the agent
	 * @param s
	 *            the s
	 */
	@setter (IKeyword.REAL_SPEED)
	public static void setRealSpeed(final IAgent agent, final Double s) {
		if (agent == null) return;
		agent.setAttribute(IKeyword.REAL_SPEED, s);
	}

	/**
	 * Gets the location.
	 *
	 * @param agent
	 *            the agent
	 * @return the location
	 */
	@getter (
			value = IKeyword.LOCATION,
			initializer = true)
	public IPoint getLocation(final IAgent agent) {
		if (agent == null) return null;
		return agent.getLocation();
	}

	/**
	 * Sets the location.
	 *
	 * @param agent
	 *            the agent
	 * @param p
	 *            the p
	 */
	@setter (IKeyword.LOCATION)
	// Correctly manages the heading
	public void setLocation(final IAgent agent, final IPoint p) {
		if (agent == null) return;
		final ITopology topology = getTopology(agent);
		final IPoint oldLocation = agent.getLocation();
		if (!topology.isTorus() && p != null && !p.equalsWithTolerance(oldLocation, 0.01)) {
			final Double newHeading = topology.directionInDegreesTo(agent.getScope(), oldLocation, p);
			if (newHeading != null) { setHeading(agent, newHeading); }
		}
		agent.setLocation(p);
	}

	/**
	 * Sets the current path.
	 *
	 * @param agent
	 *            the agent
	 * @param p
	 *            the p
	 */
	@setter ("current_path")
	public static void setCurrentPath(final IAgent agent, final IPath p) {
		// READ_ONLY
	}

	/**
	 * Gets the current path.
	 *
	 * @param agent
	 *            the agent
	 * @return the current path
	 */
	@getter (
			value = "current_path")
	public static IPath getCurrentPath(final IAgent agent) {
		if (agent == null) return null;
		return (IPath) agent.getAttribute("current_path");
	}

	/**
	 * Sets the current edge.
	 *
	 * @param agent
	 *            the agent
	 * @param g
	 *            the g
	 */
	@setter ("current_edge")
	public void setCurrentEdge(final IAgent agent, final IShape g) {
		// READ_ONLY
	}

	/**
	 * Gets the current edge.
	 *
	 * @param agent
	 *            the agent
	 * @return the current edge
	 */
	@getter (
			value = "current_edge")
	public IShape getCurrentEdge(final IAgent agent) {
		if (agent == null) return null;
		return (IShape) agent.getAttribute("current_edge");
	}

	/**
	 * Sets the current edge.
	 *
	 * @param agent
	 *            the agent
	 * @param path
	 *            the path
	 */
	public void setCurrentEdge(final IAgent agent, final IPath path) {
		if (path != null) {
			final Integer index = (Integer) agent.getAttribute("index_on_path");
			if (index < path.getEdgeList().size()) {
				agent.setAttribute("current_edge", path.getEdgeList().get(index));
			}
		}
	}

	/**
	 * Sets the current edge.
	 *
	 * @param agent
	 *            the agent
	 * @param graph
	 *            the graph
	 */
	public void setCurrentEdge(final IAgent agent, final IGraph graph) {
		if (graph != null) {
			final Integer index = (Integer) agent.getAttribute("index_on_path");
			if (index < graph.getEdges().size()) { agent.setAttribute("current_edge", graph.getEdges().get(index)); }
		}
	}

	/**
	 * @throws GamaRuntimeException
	 * @throws GamaRuntimeException
	 *             Prim: move randomly. Has to be redefined for every class that implements this interface.
	 *
	 * @param args
	 *            the args speed (meter/sec) : the speed with which the agent wants to move distance (meter) : the
	 *            distance the agent want to cover in one step amplitude (in degrees) : 360 or 0 means completely random
	 *            move, while other values, combined with the heading of the agent, define the angle in which the agent
	 *            will choose a new place. A bounds (geometry, agent, list of agents, list of geometries, species) can
	 *            be specified
	 * @return the path followed
	 */

	protected double computeHeadingFromAmplitude(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final double ampl = scope.hasArg("amplitude") ? scope.getFloatArg("amplitude") : 359;
		setHeading(agent, getHeading(agent) + scope.getRandom().between(-ampl / 2.0, ampl / 2.0));
		return getHeading(agent);
	}

	/**
	 * Compute heading.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected double computeHeading(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Double heading = scope.hasArg(IKeyword.HEADING) ? scope.getFloatArg(IKeyword.HEADING) : null;
		if (heading != null) { setHeading(agent, heading); }
		return getHeading(agent);
	}

	/**
	 * Compute distance.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the double
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected Double computeDistance(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		// We do not change the speed of the agent anymore. Only the current
		// primitive is affected
		Double s = scope.hasArg(IKeyword.SPEED) ? scope.getFloatArg(IKeyword.SPEED) : getSpeed(agent);
		// 20/1/2012 Change : The speed of the agent is multiplied by the
		// timestep in order to
		// obtain the maximal distance it can cover in one step.
		if (s == null) { s = 0.0; }
		return s * scope.getClock().getStepInSeconds();
	}

	/**
	 * Compute target.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected IShape computeTarget(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Object target = scope.getArg("target", IType.NONE);
		IShape result = null;
		if (target instanceof IShape) {
			result = (IShape) target;// ((ILocated) target).getLocation();
		}
		// if ( result == null ) {
		// scope.setStatus(ExecutionStatus.failure);
		// }
		return result;
	}

	/**
	 * Compute topology.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the i topology
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected ITopology computeTopology(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Object on = scope.getArg("on", IType.NONE);
		final ITopology topo = GamaTopologyFactory.createFrom(scope, on);
		if (topo == null) return scope.getTopology();
		return topo;
	}

	/**
	 * Compute move weights.
	 *
	 * @param scope
	 *            the scope
	 * @return the map
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	protected Map computeMoveWeights(final IScope scope) throws GamaRuntimeException {
		return scope.hasArg("move_weights") ? (Map) scope.getArg("move_weights", IType.MAP) : null;
	}

	/**
	 * Prim move randomly.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "wander",
			args = { @arg (
					name = IKeyword.SPEED,
					type = IType.FLOAT,
					optional = true,
					doc = @doc ("the speed to use for this move (replaces the current value of speed)")),
					@arg (
							name = "amplitude",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("a restriction placed on the random heading choice. The new heading is chosen in the range (heading - amplitude/2, heading+amplitude/2)")),
					@arg (
							name = IKeyword.BOUNDS,
							type = IType.GEOMETRY,
							optional = true,
							doc = @doc ("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry)")),
					@arg (
							name = IKeyword.ON,
							type = IType.GRAPH,
							optional = true,
							doc = @doc ("the graph that restrains this move (the agent moves on the graph")),
					@arg (
							name = "proba_edges",
							type = IType.MAP,
							optional = true,
							doc = @doc ("When the agent moves on a graph, the probability to choose another edge. If not defined, each edge has the same probability to be chosen")) },
			doc = @doc (
					examples = { @example ("do wander speed: speed - 10 amplitude: 120 bounds: agentA;") },
					value = "Moves the agent towards a random location at the maximum distance (with respect to its speed). The heading of the agent is chosen randomly if no amplitude is specified. This action changes the value of heading."))
	public boolean primMoveRandomly(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final IPoint location = agent.getLocation();
		final double heading = computeHeadingFromAmplitude(scope, agent);
		final double dist = computeDistance(scope, agent);

		IPoint loc = scope.getTopology().getDestination(scope, location, heading, dist, true);
		if (loc == null) {
			setHeading(agent, heading - 180);
			// pathFollowed = null;
		} else {
			final Object on = scope.getArg(IKeyword.ON, IType.GRAPH);
			Double newHeading = null;
			if (on instanceof GamaSpatialGraph graph) {
				IMap<IShape, Double> probaDeplacement = null;
				if (scope.hasArg("proba_edges")) {
					probaDeplacement = (IMap<IShape, Double>) scope.getVarValue("proba_edges");
				}
				moveToNextLocAlongPathSimplified(scope, agent, graph, dist, probaDeplacement);
				return true;
			}
			final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			if (bounds != null) {
				IShape geom = GamaShapeFactory.createFrom(scope, bounds, false);

				if (geom.getGeometries().size() > 1) {
					for (final IShape g : geom.getGeometries()) {
						if (g.euclidianDistanceTo(location) < 0.01) {
							geom = g;
							break;
						}
					}
				}
				if (geom.getInnerGeometry() != null) {
					final IPoint loc2 = computeLocationForward(scope, dist, loc, geom);
					if (!loc2.equals(loc)) {
						newHeading = heading - 180;
						loc = loc2;
					}
				}
			}

			// Enable to use wander in 3D space. An agent will wander in the
			// plan define by its z value.
			loc.setZ(agent.getLocation().getZ());
			agent.setAttribute(IKeyword.REAL_SPEED,
					loc.euclidianDistanceTo(location) / scope.getClock().getStepInSeconds());

			setLocation(agent, loc);
			if (newHeading != null) {
				setHeading(agent, newHeading);

			}
		}
		return true;
	}

	/**
	 * Prim move forward.
	 *
	 * @param scope
	 *            the scope
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "move",
			args = { @arg (
					name = IKeyword.SPEED,
					type = IType.FLOAT,
					optional = true,
					doc = @doc ("the speed to use for this move (replaces the current value of speed)")),
					@arg (
							name = IKeyword.HEADING,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the angle (in degree) of the target direction.")),
					@arg (
							name = IKeyword.BOUNDS,
							type = IType.GEOMETRY,
							optional = true,
							doc = @doc ("the geometry (the localized entity geometry) that restrains this move (the agent moves inside this geometry"))

			},
			doc = @doc (
					examples = { @example ("do move speed: speed - 10 heading: heading + rnd (30) bounds: agentA;") },
					value = "moves the agent forward, the distance being computed with respect to its speed and heading. The value of the corresponding variables are used unless arguments are passed."))

	public IPath primMoveForward(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final IPoint location = agent.getLocation();
		final Double dist = computeDistance(scope, agent);
		final Double heading = computeHeading(scope, agent);

		IPoint loc = scope.getTopology().getDestination(scope, location, heading, dist, true);
		if (loc == null) {
			setHeading(agent, heading - 180);
		} else {
			final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			if (bounds != null) {
				final IShape geom = GamaShapeFactory.createFrom(scope, bounds, false);
				if (geom != null && geom.getInnerGeometry() != null) {
					loc = computeLocationForward(scope, dist, loc, geom);
				}
			}
			setLocation(agent, loc);
		}
		if (loc != null) {
			agent.setAttribute(IKeyword.REAL_SPEED,
					loc.euclidianDistanceTo(location) / scope.getClock().getStepInSeconds());
		} else {
			agent.setAttribute(IKeyword.REAL_SPEED, 0.0);
		}
		return null;
	}

	/**
	 * Prim follow.
	 *
	 * @param scope
	 *            the scope
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "follow",
			args = { @arg (
					name = IKeyword.SPEED,
					type = IType.FLOAT,
					optional = true,
					doc = @doc ("the speed to use for this move (replaces the current value of speed)")),
					@arg (
							name = "path",
							type = IType.PATH,
							optional = false,
							doc = @doc ("a path to be followed.")),
					@arg (
							name = "move_weights",
							type = IType.MAP,
							optional = true,
							doc = @doc ("Weights used for the moving.")),
					@arg (
							name = "return_path",
							type = IType.BOOL,
							optional = true,
							doc = @doc ("if true, return the path followed (by default: false)")) },
			doc = @doc (
					value = "moves the agent along a given path passed in the arguments.",
					returns = "optional: the path followed by the agent.",
					examples = { @example ("do follow speed: speed * 2 path: road_path;") }))
	public IPath primFollow(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final double dist = computeDistance(scope, agent);
		final Boolean returnPath = scope.getBoolArg("return_path");
		final IMap weigths = (IMap) computeMoveWeights(scope);
		final IPath path = scope.hasArg("path") ? (IPath) scope.getArg("path", IType.PATH) : null;
		if (path != null && !path.getEdgeList().isEmpty()) {
			if (returnPath != null && returnPath) {
				final IPath pathFollowed = moveToNextLocAlongPath(scope, agent, path, dist, weigths);
				if (pathFollowed == null) {
					notMoving(agent);

					return null;
				}
				return pathFollowed;
			}
			moveToNextLocAlongPathSimplified(scope, agent, path, dist, weigths);
			return null;
		}
		notMoving(agent);
		return null;
	}

	/**
	 * Prim goto.
	 *
	 * @param scope
	 *            the scope
	 * @return the i path
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "goto",
			args = { @arg (
					name = "target",
					type = IType.GEOMETRY,
					optional = false,
					doc = @doc ("the location or entity towards which to move.")),
					@arg (
							name = IKeyword.SPEED,
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the speed to use for this move (replaces the current value of speed)")),
					@arg (
							name = "on",
							type = IType.NONE,
							optional = true,
							doc = @doc ("graph, topology, list of geometries or map of geometries that restrain this move")),
					@arg (
							name = "recompute_path",
							type = IType.BOOL,
							optional = true,
							doc = @doc ("if false, the path is not recompute even if the graph is modified (by default: true)")),
					@arg (
							name = "return_path",
							type = IType.BOOL,
							optional = true,
							doc = @doc ("if true, return the path followed (by default: false)")),
					@arg (
							name = "move_weights",
							type = IType.MAP,
							optional = true,
							doc = @doc ("Weights used for the moving.")) },
			doc = @doc (
					value = "moves the agent towards the target passed in the arguments.",
					returns = "optional: the path followed by the agent.",
					examples = {
							@example ("do goto target: (one_of road).location speed: speed * 2 on: road_network;") }))
	/**
	 * Prim goto - Optimized version with decomposed methods for better performance. Performance improvements: - Uses
	 * context objects to reduce parameter passing overhead - Implements intelligent path caching and reuse - Optimized
	 * container handling and early validation - Reduced memory allocations through object pooling
	 *
	 * @param scope
	 *            the scope
	 * @return the path followed by the agent or null
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public IPath primGoto(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final IPoint source = agent.getLocation().copy(scope);
		final double maxDistance = computeDistance(scope, agent);
		final boolean returnPath =
				scope.hasArg("return_path") ? (Boolean) scope.getArg("return_path", IType.NONE) : false;

		// Create optimization context to reduce parameter passing overhead
		final GotoContext context = new GotoContext(agent, source, maxDistance, returnPath);
		context.goal = computeTarget(scope, agent);
		context.weights = (IMap) computeMoveWeights(scope);

		// Resolve target topology and containers efficiently
		resolveGotoTarget(scope, context);

		// Early validation to avoid expensive computation for invalid operations
		if (!validateGotoOperation(scope, context)) return context.path; // May be null or empty path

		// Compute path with intelligent caching
		computeGotoPath(scope, context);

		// Handle case where no valid path exists
		if (context.path == null) {
			notMoving(context.agent);
			if (context.returnPath) return GamaPathFactory.createFrom(scope, context.topology, context.source,
					context.source, GamaListFactory.<IShape> create(Types.GEOMETRY), false);
			return null;
		}

		// Store computed path for future use (performance optimization)
		context.agent.setAttribute("current_path", context.path);

		// Execute movement with optimized algorithms
		if (context.returnPath) {
			final IPath pathFollowed =
					moveToNextLocAlongPath(scope, context.agent, context.path, context.maxDistance, context.weights);
			if (pathFollowed == null) return GamaPathFactory.createFrom(scope, context.topology, context.source,
					context.source, GamaListFactory.<IShape> create(Types.GEOMETRY), false);
			return pathFollowed;
		}

		// Use simplified movement for better performance when path return is not needed
		moveToNextLocAlongPathSimplified(scope, context.agent, context.path, context.maxDistance, context.weights);

		// Clear object pools to prevent memory leaks in long-running simulations
		clearObjectPools();

		return null;
	}

	/**
	 * Not moving.
	 *
	 * @param agent
	 *            the agent
	 */
	private void notMoving(final IAgent agent) {
		setRealSpeed(agent, 0.0);
		agent.setAttribute("current_edge", null);
		agent.setAttribute("current_path", null);
	}

	/**
	 * @throws GamaRuntimeException
	 *             Return the next location toward a target on a line
	 *
	 * @param coords
	 *            coordinates of the line
	 * @param source
	 *            current location
	 * @param target
	 *            location to reach
	 * @param distance
	 *            max displacement distance
	 * @return the next location
	 */

	protected IList initMoveAlongPath3D(final IAgent agent, final IPath path, final IPoint cl) {
		IPoint currentLocation = cl.copy(GAMA.getRuntimeScope());
		final IList initVals = GamaListFactory.create();

		Integer index = 0;
		Integer indexSegment = 1;
		Integer endIndexSegment = 1;
		IPoint falseTarget = null;
		final IList<IShape> edges = path.getEdgeGeometry();
		if (path.isVisitor(agent)) {
			index = path.indexOf(agent);
			indexSegment = path.indexSegmentOf(agent);

		} else {
			if (edges.isEmpty()) return null;
			path.acceptVisitor(agent);

			double dist = Double.MAX_VALUE;
			int i = 0;
			for (final IShape e : edges) {
				final IPoint[] points = getPointsOf(e);
				int j = 0;
				for (final IPoint pt : points) {
					final double d = pt.euclidianDistanceTo(cl);
					if (d < dist) {
						currentLocation = pt;
						dist = d;
						index = i;
						indexSegment = j + 1;
						if (dist == 0.0) { break; }
					}
					j++;
				}

				if (dist == 0.0) { break; }
				i++;
			}
		}
		final IPoint[] points = getPointsOf(edges.lastValue(GAMA.getRuntimeScope()));
		int j = 0;
		double dist = Double.MAX_VALUE;
		final IPoint end = ((IShape) path.getEndVertex()).getLocation();
		for (final IPoint pt : points) {
			final double d = pt.euclidianDistanceTo(end);
			if (d < dist) {
				dist = d;
				endIndexSegment = j;
				falseTarget = pt;
				if (dist == 0.0) { break; }

			}
			j++;
		}
		initVals.add(index);
		initVals.add(indexSegment);
		initVals.add(endIndexSegment);
		initVals.add(currentLocation);
		initVals.add(falseTarget);
		return initVals;
	}

	/**
	 * Inits the move along path.
	 *
	 * @param agent
	 *            the agent
	 * @param path
	 *            the path
	 * @param cl
	 *            the cl
	 * @return the i list
	 */
	protected IList initMoveAlongPath(final IAgent agent, final IPath path, final IPoint cl) {
		IPoint currentLocation = cl;
		try (final Collector.AsList initVals = Collector.getList()) {
			Integer index = 0;
			Integer indexSegment = 1;
			Integer endIndexSegment = 0;
			IPoint falseTarget = null;
			final IList<IShape> edges = path.getEdgeGeometry();
			if (edges.isEmpty()) return null;
			final int nb = edges.size();
			if (path.getGraph() == null && nb == 1 && edges.get(0).getInnerGeometry().getNumPoints() == 2) {
				index = 0;
				indexSegment = 0;
				endIndexSegment = 0;
				falseTarget = ((IShape) path.getEndVertex()).getLocation();
				path.acceptVisitor(agent);

			} else {
				if (path.isVisitor(agent)) {
					index = path.indexOf(agent);
					indexSegment = path.indexSegmentOf(agent);

				} else {
					path.acceptVisitor(agent);
					double distanceS = Double.MAX_VALUE;
					IShape line = null;
					for (int i = 0; i < nb; i++) {
						line = edges.get(i);
						final double distS = Distance.pointToSegment(currentLocation.toCoordinate(),
								getFirstPointOf(line).toCoordinate(), getLastPointOf(line).toCoordinate());
						if (distS < distanceS) {
							distanceS = distS;
							index = i;
						}
					}
					line = edges.get(index);
					final IPoint[] points = getPointsOf(line);
					if (contains(points, currentLocation)) {
						currentLocation = GamaPointFactory.create(currentLocation);
						indexSegment = indexOf(points, currentLocation) + 1;
					} else {
						currentLocation = SpatialPunctal._closest_point_to(currentLocation, line);
						if (points.length >= 3) {
							distanceS = Double.MAX_VALUE;
							final int nbSp = points.length;
							for (int i = 0; i < nbSp - 1; i++) {
								final double distS = Distance.pointToSegment(currentLocation.toCoordinate(),
										points[i].toCoordinate(), points[i + 1].toCoordinate());
								if (distS < distanceS) {
									distanceS = distS;
									indexSegment = i + 1;
									currentLocation.setZ(points[i].getZ() + (points[i + 1].getZ() - points[i].getZ())
											* currentLocation.distance(points[i]) / points[i].distance(points[i + 1]));
								}
							}
						} else if (points.length >= 2) {
							final IPoint c0 = points[0];
							final IPoint c1 = points[1];
							currentLocation.setZ(c0.getZ()
									+ (c1.getZ() - c0.getZ()) * currentLocation.distance(c0) / line.getPerimeter());
						} else {
							currentLocation.setZ(points[0].getZ());
						}
					}
				}
				final IShape lineEnd = edges.get(nb - 1);
				final IPoint end = ((IShape) path.getEndVertex()).getLocation();
				final IPoint[] points = getPointsOf(lineEnd);
				if (contains(points, end)) {
					falseTarget = GamaPointFactory.create(end);
					endIndexSegment = indexOf(points, end) + 1;
				} else {
					falseTarget = SpatialPunctal._closest_point_to(end, lineEnd);
					endIndexSegment = 1;
					if (points.length >= 3) {
						double distanceT = Double.MAX_VALUE;
						for (int i = 0; i < points.length - 1; i++) {
							final double distT = Distance.pointToSegment(falseTarget.toCoordinate(),
									points[i].toCoordinate(), points[i + 1].toCoordinate());// segment.distance(pointGeom);
							if (distT < distanceT) {
								distanceT = distT;
								endIndexSegment = i + 1;
								falseTarget.setZ(points[i].getZ() + (points[i + 1].getZ() - points[i].getZ())
										* falseTarget.distance3D(points[i]) / points[i].distance3D(points[i + 1]));
							}
						}
					} else {
						final IPoint c0 = points[0];
						final IPoint c1 = points[1];
						falseTarget.setZ(c0.getZ()
								+ (c1.getZ() - c0.getZ()) * falseTarget.distance3D(c0) / lineEnd.getPerimeter());
					}
				}
			}
			initVals.add(index);
			initVals.add(indexSegment);
			initVals.add(endIndexSegment);
			initVals.add(currentLocation);
			initVals.add(falseTarget);
			return initVals.items();
		}
	}

	/**
	 * Inits the move along path.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param graph
	 *            the graph
	 * @param currentLoc
	 *            the current loc
	 * @return the i list
	 */
	@SuppressWarnings ("null")
	protected IList initMoveAlongPath(final IScope scope, final IAgent agent, final GamaSpatialGraph graph,
			final IPoint currentLoc) {
		IPoint currentLocation = currentLoc;
		try (final Collector.AsList initVals = Collector.getList()) {
			Integer index = 0;
			Integer indexSegment = 1;
			Integer reverse = 0;
			final IList<IShape> edges = graph.getEdges();
			if (edges.isEmpty()) return null;
			final int nb = edges.size();
			if (nb == 1 && edges.get(0).getInnerGeometry().getNumPoints() == 2) {
				index = 0;
				indexSegment = 1;
			} else {
				IShape line = null;
				index = (Integer) agent.getAttribute("index_on_path");
				indexSegment = (Integer) agent.getAttribute("index_on_path_segment");
				reverse = (Integer) agent.getAttribute("reverse");
				if (index == null || indexSegment == null) {
					reverse = scope.getRandom().between(0, 1);
					final boolean optimization = graph.edgeSet().size() > 1000;
					final double dist = optimization
							? Math.sqrt(scope.getSimulation().getArea()) / graph.edgeSet().size() * 100 : -1;
					if (graph.isAgentEdge()) {
						final IAgentFilter filter = In.edgesOf(graph);
						if (optimization) {
							final Collection<IAgent> ags = scope.getSimulation().getTopology().getNeighborsOf(scope,
									currentLocation, dist, filter);
							if (!ags.isEmpty()) {
								double distMin = Double.MAX_VALUE;
								for (final IAgent e : ags) {
									final double d = currentLocation.euclidianDistanceTo(e);
									if (d < distMin) {
										line = e;
										distMin = d;
									}
								}
							}
						}
						if (line == null) {
							line = scope.getSimulation().getTopology().getAgentClosestTo(scope, currentLocation,
									filter);
						}
						index = edges.indexOf(line);
					} else {
						double distanceS = Double.MAX_VALUE;
						for (int i = 0; i < nb; i++) {
							line = edges.get(i);
							final double distS = line.euclidianDistanceTo(currentLocation);
							if (distS < distanceS) {
								distanceS = distS;
								index = i;
							}
						}
						line = edges.get(index);
					}
					final IPoint[] points = getPointsOf(line);
					if (contains(points, currentLocation)) {
						currentLocation = GamaPointFactory.create(currentLocation);
						indexSegment = indexOf(points, currentLocation) + 1;
					} else {
						currentLocation = SpatialPunctal._closest_point_to(currentLocation, line);
						if (points.length >= 3) {
							Double distanceS = Double.MAX_VALUE;
							for (int i = 0; i < points.length - 1; i++) {
								final double distS = Distance.pointToSegment(currentLocation.toCoordinate(),
										points[i].toCoordinate(), points[i + 1].toCoordinate()); // segment.distance(pointGeom);
								if (distS < distanceS) {
									distanceS = distS;
									indexSegment = i + 1;
									currentLocation.setZ(points[i].getZ() + (points[i + 1].getZ() - points[i].getZ())
											* currentLocation.distance3D(points[i])
											/ points[i].distance3D(points[i + 1]));
								}
							}
						} else {
							indexSegment = 1;
							currentLocation.setZ(points[0].getZ() + (points[1].getZ() - points[0].getZ())
									* currentLocation.distance3D(points[0]) / line.getPerimeter());
						}
					}
				}
			}

			initVals.add(index);
			initVals.add(indexSegment);
			initVals.add(reverse);
			return initVals.items();
		}
	}

	/**
	 * Move to next loc along path simplified.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param graph
	 *            the graph
	 * @param d
	 *            the d
	 * @param probaEdge
	 *            the proba edge
	 */
	public void moveToNextLocAlongPathSimplified(final IScope scope, final IAgent agent, final GamaSpatialGraph graph,
			final double d, final IMap probaEdge) {
		IPoint currentLocation = agent.getLocation().copy(scope);
		final IList indexVals = initMoveAlongPath(scope, agent, graph, currentLocation);
		if (indexVals == null) return;
		int index = (Integer) indexVals.get(0);
		int indexSegment = (Integer) indexVals.get(1);
		int inverse = (Integer) indexVals.get(2);
		IShape edge = (IShape) graph.getEdges().get(index);
		double distance = d;
		double travelledDist = 0.0;
		double computedHeading = 0.0;
		while (true) {
			Coordinate coords[] = edge.getInnerGeometry().getCoordinates();
			if (!graph.isDirected() && inverse == 1) {
				final int si = coords.length;
				final Coordinate coords2[] = new Coordinate[si];
				for (int i = 0; i < coords.length; i++) { coords2[i] = coords[si - 1 - i]; }
				coords = coords2;
			}

			final double weight = graph.getEdgeWeight(edge) / edge.getGeometry().getPerimeter();
			for (int j = indexSegment; j < coords.length; j++) {
				final IPoint pt = GamaPointFactory.create(coords[j]);
				final double dis = pt.distance3D(currentLocation);
				final double dist = weight * dis;
				computedHeading = SpatialRelations.towards(scope, currentLocation, pt);

				if (distance < dist) {
					final double ratio = distance / dist;
					travelledDist += dis * ratio;
					currentLocation = currentLocation.plus(pt.minus(currentLocation).times(ratio));
					distance = 0;
					break;
				}
				if (distance <= dist) {
					currentLocation = pt;
					travelledDist += dis;
					distance = 0;
					if (indexSegment < coords.length - 1) {
						indexSegment++;
					} else {
						indexSegment = 1;
					}
					break;
				}
				currentLocation = pt;
				travelledDist += dis;

				distance = distance - dist;
				indexSegment++;
				if (j == coords.length - 1) {
					IShape node = (IShape) graph.getEdgeTarget(edge);
					if (!graph.isDirected() && !node.getLocation().equals(currentLocation)) {
						node = (IShape) graph.getEdgeSource(edge);
					}
					final List<IShape> nextRoads = new ArrayList<IShape>(
							graph.isDirected() ? graph.outgoingEdgesOf(node) : graph.edgesOf(node));
					if (nextRoads.isEmpty()) {
						distance = 0;
						break;
					}
					if (nextRoads.size() == 1) { edge = nextRoads.get(0); }
					if (nextRoads.size() > 1) {
						if (probaEdge == null || probaEdge.isEmpty()) {
							edge = nextRoads.get(scope.getRandom().between(0, nextRoads.size() - 1));
						} else {
							final IList<Double> distribution = GamaListFactory.create(Types.FLOAT);
							for (final IShape r : nextRoads) {
								final Double val = (Double) probaEdge.get(r);
								distribution.add(val == null ? 0.0 : val);
							}
							edge = nextRoads.get(Random.opRndChoice(scope, distribution));
						}
					}
					index = graph.getEdges().indexOf(edge);
					if (!graph.isDirected()) {
						if (currentLocation.equals(graph.getEdgeSource(edge))) {
							inverse = 0;
						} else {
							inverse = 1;
						}
					}
					indexSegment = 0;
				}
			}
			if (distance == 0) { break; }
			indexSegment = 1;
		}
		agent.setAttribute(IKeyword.REAL_SPEED, travelledDist / scope.getClock().getStepInSeconds());

		agent.setAttribute("index_on_path", index);
		setCurrentEdge(agent, graph);
		agent.setAttribute("index_on_path_segment", indexSegment);
		agent.setAttribute("reverse", inverse);
		setLocation(agent, currentLocation);

		setHeading(agent, computedHeading);
	}

	/**
	 * Move to next loc along path simplified.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param path
	 *            the path
	 * @param d
	 *            the d
	 * @param weigths
	 *            the weigths
	 */
	private void moveToNextLocAlongPathSimplified(final IScope scope, final IAgent agent, final IPath path,
			final double d, final IMap weigths) {
		IPoint currentLocation = agent.getLocation().copy(scope);
		final IList indexVals = ((GamaSpatialPath) path).isThreeD() ? initMoveAlongPath3D(agent, path, currentLocation)
				: initMoveAlongPath(agent, path, currentLocation);
		if (indexVals == null) return;
		int index = (Integer) indexVals.get(0);
		int indexSegment = (Integer) indexVals.get(1);
		final int endIndexSegment = (Integer) indexVals.get(2);
		currentLocation = (IPoint) indexVals.get(3);
		final IPoint falseTarget = (IPoint) indexVals.get(4);
		final IList<IShape> edges = path.getEdgeGeometry();
		double computedHeading = 0.0;
		final int nb = edges.size();
		double distance = d;
		final GamaSpatialGraph graph = (GamaSpatialGraph) path.getGraph();
		double travelledDist = 0.0;
		for (int i = index; i < nb; i++) {
			final IShape line = edges.get(i);
			final IPoint[] coords = getPointsOf(line);

			double weight;
			if (weigths == null) {
				weight = computeWeigth(graph, path, line);
			} else {
				final IShape realShape = path.getRealObject(line);
				final Double w = realShape == null ? null
						: (Double) weigths.get(realShape) / realShape.getGeometry().getPerimeter();
				weight = w == null ? computeWeigth(graph, path, line) : w;
			}
			for (int j = indexSegment; j < coords.length; j++) {
				IPoint pt = null;
				if (i == nb - 1 && j == endIndexSegment) {
					pt = falseTarget;
				} else {
					pt = GamaPointFactory.create(coords[j]);
				}
				final double dis = pt.distance3D(currentLocation);
				final double dist = weight * dis;
				computedHeading = SpatialRelations.towards(scope, currentLocation, pt);

				if (distance < dist) {
					final double ratio = distance / dist;
					currentLocation = currentLocation.plus(pt.minus(currentLocation).times(ratio));
					travelledDist += dis * ratio;
					distance = 0;
					break;
				}
				if (distance <= dist) {
					currentLocation = pt;
					distance = 0;
					travelledDist += dis;
					if (indexSegment < coords.length - 1) {
						indexSegment++;
					} else {
						if (index < nb - 1) { index++; }
						indexSegment = 1;
					}
					break;
				}
				currentLocation = pt;
				travelledDist += dis;
				distance = distance - dist;
				if (i == nb - 1 && j == endIndexSegment) { break; }
				indexSegment++;
			}
			if (distance == 0) { break; }
			indexSegment = 1;
			if (index < nb - 1) { index++; }
		}
		if (currentLocation.equals(falseTarget)) {

			currentLocation = GamaPointFactory.toPoint(scope, path.getEndVertex());
			index++;
		}
		path.setIndexSegementOf(agent, indexSegment);
		path.setIndexOf(agent, index);
		setCurrentEdge(agent, path);
		setLocation(agent, currentLocation);
		setHeading(agent, computedHeading);
		path.setSource(agent.getLocation()/* .copy(scope) */);

	}

	/**
	 * Compute weigth.
	 *
	 * @param graph
	 *            the graph
	 * @param path
	 *            the path
	 * @param line
	 *            the line
	 * @return the double
	 */
	protected double computeWeigth(final IGraph graph, final IPath path, final IShape line) {
		if (graph == null) return 1.0;
		final IShape realShape = path.getRealObject(line);
		return realShape == null ? 1 : graph.getEdgeWeight(realShape) / realShape.getGeometry().getPerimeter();
	}

	/**
	 * Move to next loc along path.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @param path
	 *            the path
	 * @param d
	 *            the d
	 * @param weigths
	 *            the weigths
	 * @return the i path
	 */
	private IPath moveToNextLocAlongPath(final IScope scope, final IAgent agent, final IPath path, final double d,
			final IMap weigths) {
		final IPoint startLocation = agent.getLocation().copy(scope);

		IPoint currentLocation = agent.getLocation().copy(scope);
		final IList indexVals = ((GamaSpatialPath) path).isThreeD() ? initMoveAlongPath3D(agent, path, currentLocation)
				: initMoveAlongPath(agent, path, currentLocation);
		if (indexVals == null) return null;
		final IList<IShape> segments = GamaListFactory.create(Types.GEOMETRY);
		final IMap agents = GamaMapFactory.createUnordered();

		int index = (Integer) indexVals.get(0);
		int indexSegment = (Integer) indexVals.get(1);
		final int endIndexSegment = (Integer) indexVals.get(2);
		currentLocation = (IPoint) indexVals.get(3);
		final IPoint falseTarget = (IPoint) indexVals.get(4);
		final IList<IShape> edges = path.getEdgeGeometry();
		final int nb = edges.size();
		double distance = d;
		double travelledDist = 0.0;
		double computedHeading = 0.0;
		final GamaSpatialGraph graph = (GamaSpatialGraph) path.getGraph();
		for (int i = index; i < nb; i++) {
			final IShape line = edges.get(i);
			final Coordinate coords[] = line.getInnerGeometry().getCoordinates();
			double weight;
			if (weigths == null) {
				weight = computeWeigth(graph, path, line);
			} else {
				final IShape realShape = path.getRealObject(line);
				final Double w = realShape == null ? null
						: (Double) weigths.get(realShape) / realShape.getGeometry().getPerimeter();
				weight = w == null ? computeWeigth(graph, path, line) : w;
			}

			for (int j = indexSegment; j < coords.length; j++) {
				IPoint pt = null;
				if (i == nb - 1 && j == endIndexSegment) {
					pt = falseTarget;
				} else {
					pt = GamaPointFactory.create(coords[j]);
				}
				final double dis = pt.distance3D(currentLocation);
				final double dist = weight * dis;
				computedHeading = SpatialRelations.towards(scope, currentLocation, pt);

				if (distance < dist) {
					final IPoint pto = currentLocation.copy(scope);

					final double ratio = distance / dist;
					travelledDist += dis * ratio;
					currentLocation = currentLocation.plus(pt.minus(currentLocation).times(ratio));
					distance = 0;

					final IShape gl = GamaShapeFactory.buildLine(pto, currentLocation);
					final IShape sh = path.getRealObject(line);
					if (sh != null) {
						final IAgent a = sh.getAgent();
						if (a != null) { agents.put(gl, a); }
					}
					segments.add(gl);

					break;
				}
				if (distance <= dist) {
					travelledDist += dis;
					final IShape gl = GamaShapeFactory.buildLine(currentLocation, pt);
					if (path.getRealObject(line) != null) {
						final IAgent a = path.getRealObject(line).getAgent();

						if (a != null) { agents.put(gl, a); }
					}

					segments.add(gl);
					currentLocation = pt;
					distance = 0;
					if (indexSegment < coords.length - 1) {
						indexSegment++;
					} else {
						if (index < nb - 1) { index++; }
						indexSegment = 1;
					}
					break;
				}
				travelledDist += dis;
				final IShape gl = GamaShapeFactory.buildLine(currentLocation, pt);
				final IShape sh = path.getRealObject(line);
				if (sh != null) {
					final IAgent a = sh.getAgent();
					if (a != null) { agents.put(gl, a); }
				}
				segments.add(gl);

				currentLocation = pt;
				distance = distance - dist;
				if (i == nb - 1 && j == endIndexSegment) { break; }
				indexSegment++;
			}
			if (distance == 0) { break; }
			indexSegment = 1;
			if (index < nb - 1) { index++; }
		}
		if (currentLocation.equals(falseTarget)) {

			currentLocation = GamaPointFactory.toPoint(scope, path.getEndVertex());
			index++;
		}
		path.setIndexSegementOf(agent, indexSegment);
		path.setIndexOf(agent, index);
		setCurrentEdge(agent, path);
		setLocation(agent, currentLocation);
		path.setSource(currentLocation.copy(scope));
		agent.setAttribute(IKeyword.REAL_SPEED, travelledDist / scope.getClock().getStepInSeconds());

		if (segments.isEmpty()) return null;
		final IPath followedPath =
				GamaPathFactory.createFrom(scope, agent.getTopology(), startLocation, currentLocation, segments, false);
		followedPath.setRealObjects(agents);

		setHeading(agent, computedHeading);
		return followedPath;
	}

	/**
	 * Compute location forward.
	 *
	 * @param scope
	 *            the scope
	 * @param dist
	 *            the dist
	 * @param loc
	 *            the loc
	 * @param geom
	 *            the geom
	 * @return the gama point
	 */
	protected IPoint computeLocationForward(final IScope scope, final double dist, final IPoint loc,
			final IShape geom) {
		final IList pts = GamaListFactory.create(Types.POINT);
		pts.add(scope.getAgent().getLocation(scope));
		pts.add(loc);
		final IShape line = SpatialCreation.line(scope, pts);
		// line = Spatial.Operators.inter(scope, line, geom);

		if (line == null) return getCurrentAgent(scope).getLocation(scope);
		if (geom.covers(line)) return loc;

		// final IPoint computedPt = line.getPoints().lastValue(scope);

		final IPoint computedPt = SpatialPunctal.closest_points_with(line, geom.getExteriorRing(scope)).get(0);
		if (computedPt != null && computedPt.intersects(geom)) return computedPt;
		return getCurrentAgent(scope).getLocation(scope);
	}

	/**
	 * Context class to encapsulate goto parameters and state for better performance. Reduces parameter passing overhead
	 * and improves code readability.
	 */
	protected static class GotoContext {

		/** The agent. */
		public final IAgent agent;

		/** The source. */
		public final IPoint source;

		/** The max distance. */
		public final double maxDistance;

		/** The return path. */
		public final boolean returnPath;

		/** The goal. */
		public IShape goal;

		/** The container. */
		public IContainer container;

		/** The edge. */
		public IShape edge;

		/** The topology. */
		public ITopology topology;

		/** The path. */
		public IPath path;

		/** The weights. */
		public IMap weights;

		/**
		 * Instantiates a new goto context.
		 *
		 * @param agent
		 *            the agent
		 * @param source
		 *            the source
		 * @param maxDistance
		 *            the max distance
		 * @param returnPath
		 *            the return path
		 */
		public GotoContext(final IAgent agent, final IPoint source, final double maxDistance,
				final boolean returnPath) {
			this.agent = agent;
			this.source = source;
			this.maxDistance = maxDistance;
			this.returnPath = returnPath;
		}
	}

	/**
	 * Resolves the "on" parameter for goto operations. Performance: Optimized to handle different container types
	 * efficiently
	 *
	 * @param scope
	 *            the simulation scope
	 * @param context
	 *            the goto context
	 */
	protected void resolveGotoTarget(final IScope scope, final GotoContext context) {
		final Object onValue = scope.getArg("on", IType.NONE);

		if (onValue instanceof IShape && ((IShape) onValue).isLine()) {
			context.edge = (IShape) onValue;
			context.topology = scope.getTopology();
			return;
		}

		// Handle different container types
		if (onValue instanceof ISpecies) {
			context.container = ((ISpecies) onValue).listValue(scope, Types.AGENT, false);
		} else if (onValue instanceof final IList sourceList) {
			if (!sourceList.isEmpty() && sourceList.get(0) instanceof IAgent) {
				context.container = GamaListFactory.create(Types.AGENT);
				((IList) context.container).addAll(sourceList);
			}
		} else if (onValue instanceof IMap) {
			context.container = GamaMapFactory.wrap(Types.AGENT, Types.NO_TYPE, (IMap) onValue);
		}

		// Create topology from resolved container
		final Object topologySource = onValue instanceof IMap i ? i.keySet() : onValue;
		context.topology = GamaTopologyFactory.createFrom(scope, topologySource);
		if (context.topology == null) { context.topology = scope.getTopology(); }

		// Clear empty containers for performance
		if (context.container != null && context.container.isEmpty(scope)) { context.container = null; }
	}

	/**
	 * Validates the goto operation parameters and handles early termination cases. Memory optimization: Returns empty
	 * paths for invalid cases without heavy computation
	 *
	 * @param scope
	 *            the simulation scope
	 * @param context
	 *            the goto context
	 * @return true if operation should continue, false if it should terminate early
	 */
	protected boolean validateGotoOperation(final IScope scope, final GotoContext context) {
		if (context.goal == null || context.topology == null) {
			notMoving(context.agent);
			if (context.returnPath) {
				context.path = GamaPathFactory.createFrom(scope, context.topology, context.source, context.source,
						GamaListFactory.getEmptyList(), false);
			}
			return false;
		}

		// Handle grid topology special case
		if (context.topology instanceof GridTopology) {
			context.goal = ((GamaSpatialMatrix) context.topology.getPlaces()).getAgentAt(context.goal.getLocation())
					.getLocation();
		}

		// Check if already at target
		if (context.source.equals(context.goal.getLocation())) {
			notMoving(context.agent);
			if (context.returnPath) {
				context.path = GamaPathFactory.createFrom(scope, context.topology, context.source, context.source,
						GamaListFactory.getEmptyList(), false);
			}
			return false;
		}

		return true;
	}

	/**
	 * Computes or reuses the path for goto operations with intelligent caching. Performance: Avoids expensive path
	 * recomputation when possible
	 *
	 * @param scope
	 *            the simulation scope
	 * @param context
	 *            the goto context
	 */
	protected void computeGotoPath(final IScope scope, final GotoContext context) {
		final Boolean recomputePath =
				scope.hasArg("recompute_path") ? (Boolean) scope.getArg("recompute_path", IType.NONE) : true;

		// Get current path from agent
		IPath currentPath = (IPath) context.agent.getAttribute("current_path");

		// Force recomputation for grid topology
		if (recomputePath && context.topology instanceof GridTopology) {
			context.agent.setAttribute("current_path", null);
			currentPath = null;
		}

		// Check if current path is still valid
		final boolean pathInvalid =
				currentPath == null || !isPathValid(scope, currentPath, context.topology, context.source, context.goal);

		if (pathInvalid) {
			context.path = createNewPath(scope, context);
		} else if (shouldRecomputeForGraphTopology(context.topology, currentPath, recomputePath)) {
			context.path = context.topology.pathBetween(scope, context.agent, context.goal);
		} else {
			context.path = currentPath;
		}
	}

	/**
	 * Checks if a path is still valid for the current operation. Performance: Avoids expensive path recalculation when
	 * current path is usable
	 *
	 * @param scope
	 *            the simulation scope
	 * @param path
	 *            the path to validate
	 * @param topology
	 *            the current topology
	 * @param source
	 *            the source point
	 * @param goal
	 *            the goal shape
	 * @return true if path is valid, false otherwise
	 */
	private boolean isPathValid(final IScope scope, final IPath path, final ITopology topology, final IPoint source,
			final IShape goal) {

		if (path.getTopology(scope) != null && !path.getTopology(scope).equals(topology)
				|| !((IShape) path.getEndVertex()).getLocation().equals(goal.getLocation())
				|| !((IShape) path.getStartVertex()).getLocation().equals(source.getLocation()))
			return false;

		return true;
	}

	/**
	 * Creates a new path based on the goto context. Memory optimization: Uses optimized path creation based on topology
	 * type
	 *
	 * @param scope
	 *            the simulation scope
	 * @param context
	 *            the goto context
	 * @return the created path
	 */
	private IPath createNewPath(final IScope scope, final GotoContext context) {
		if (context.edge != null) {
			final List<IShape> edges = REUSABLE_SHAPE_LIST.get();
			edges.clear();
			edges.add(context.edge);
			return new GamaSpatialPath(context.source.getGeometry(), context.goal,
					GamaListFactory.wrap(Types.GEOMETRY, edges), true);
		}

		if (context.topology instanceof final GridTopology gridTopo) {
			if (context.container instanceof IList)
				return gridTopo.pathBetween(scope, context.source, context.goal, (IList) context.container);
			if (context.container instanceof IMap)
				return gridTopo.pathBetween(scope, context.source, context.goal, (IMap) context.container);
		}

		return context.topology.pathBetween(scope, context.agent, context.goal);
	}

	/**
	 * Checks if path should be recomputed for graph topology. Performance: Optimizes graph path recomputation decisions
	 *
	 * @param topology
	 *            the topology
	 * @param path
	 *            the current path
	 * @param recomputePath
	 *            recomputation flag
	 * @return true if path should be recomputed
	 */
	private boolean shouldRecomputeForGraphTopology(final ITopology topology, final IPath path,
			final boolean recomputePath) {

		if (!(topology instanceof final GraphTopology graphTopo)) return false;

		return graphTopo.getPlaces() != path.getGraph()
				|| recomputePath && graphTopo.getPlaces().getPathComputer().getVersion() != path.getGraphVersion();
	}

}
