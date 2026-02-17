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
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.skill.Skill;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.GamaPathFactory;
import gama.api.types.graph.IGraph;
import gama.api.types.graph.IPath;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.types.misc.IContainer;
import gama.api.types.topology.GamaTopologyFactory;
import gama.api.types.topology.ITopology;
import gama.api.utils.collections.Collector;
import gama.api.utils.interfaces.IAgentFilter;
import gama.core.topology.filter.In;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.core.topology.graph.GraphTopology;
import gama.core.topology.grid.GamaSpatialMatrix;
import gama.core.topology.grid.GridTopology;
import gama.core.util.path.GamaPath;
import gama.core.util.path.GamaSpatialPath;
import gama.gaml.operators.Maths;
import gama.gaml.operators.spatial.SpatialCreation;
import gama.gaml.operators.spatial.SpatialPunctal;
import gama.gaml.operators.spatial.SpatialRelations;

/**
 * MovingSkill - Core skill providing movement capabilities for GAMA agents.
 * 
 * <p>This skill defines the fundamental behaviors required for agent movement across different topologies
 * including continuous space, graphs, and grids. It provides four main movement actions:</p>
 * 
 * <ul>
 *   <li><b>wander</b>: Random movement with optional bounds and amplitude constraints</li>
 *   <li><b>move</b>: Simple forward movement in the current heading direction</li>
 *   <li><b>follow</b>: Movement along a predefined path</li>
 *   <li><b>goto</b>: Goal-directed movement with automatic pathfinding</li>
 * </ul>
 * 
 * <h3>Managed Attributes</h3>
 * <p>The skill maintains several attributes for each agent:</p>
 * <ul>
 *   <li><b>location</b>: Current position (IPoint)</li>
 *   <li><b>speed</b>: Desired speed in meters/second</li>
 *   <li><b>real_speed</b>: Actual speed achieved in last move</li>
 *   <li><b>heading</b>: Direction in degrees (0-360)</li>
 *   <li><b>current_path</b>: Active path being followed</li>
 *   <li><b>current_edge</b>: Current edge on graph/path</li>
 * </ul>
 * 
 * <h3>Architecture</h3>
 * <p>Path movement operations are delegated to {@link PathMovementHelper} for improved modularity.
 * Movement attributes are centralized in the {@link MovementAttributes} inner class.</p>
 * 
 * <h3>Usage Example</h3>
 * <pre>
 * species my_agent skills: [moving] {
 *     reflex move_around {
 *         do wander amplitude: 90.0 speed: 2.0;
 *     }
 *     
 *     reflex go_home when: hungry {
 *         do goto target: home speed: 5.0 on: road_network;
 *     }
 * }
 * </pre>
 *
 * @author drogoul
 * @since GAMA 1.0 (2007)
 * @see PathMovementHelper
 * @see MovingSkill3D
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

	/**
	 * Inner class to centralize movement-related attribute names and management.
	 * This provides a single source of truth for all attribute keys used in movement.
	 */
	protected static class MovementAttributes {
		/** Attribute key for storing the current path */
		public static final String CURRENT_PATH = "current_path";
		
		/** Attribute key for storing the current edge */
		public static final String CURRENT_EDGE = "current_edge";
		
		/** Attribute key for storing the index on the path */
		public static final String INDEX_ON_PATH = "index_on_path";
		
		/** Attribute key for storing the segment index on the path */
		public static final String INDEX_ON_PATH_SEGMENT = "index_on_path_segment";
		
		/** Attribute key for storing the reverse direction flag */
		public static final String REVERSE = "reverse";
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
		return (IPath) agent.getAttribute(MovementAttributes.CURRENT_PATH);
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
		return (IShape) agent.getAttribute(MovementAttributes.CURRENT_EDGE);
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
			final Integer index = (Integer) agent.getAttribute(MovementAttributes.INDEX_ON_PATH);
			if (index < path.getEdgeList().size()) {
				agent.setAttribute(MovementAttributes.CURRENT_EDGE, path.getEdgeList().get(index));
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
			final Integer index = (Integer) agent.getAttribute(MovementAttributes.INDEX_ON_PATH);
			if (index < graph.getEdges().size()) { 
				agent.setAttribute(MovementAttributes.CURRENT_EDGE, graph.getEdges().get(index)); 
			}
		}
	}

	/**
	 * Computes a new heading from the current heading with random amplitude variation.
	 * The new heading is calculated by adding a random value within [-amplitude/2, amplitude/2] 
	 * to the current heading, creating a wandering behavior.
	 * 
	 * @param scope the execution scope
	 * @param agent the agent whose heading is being computed
	 * @return the new heading value in degrees
	 * @throws GamaRuntimeException if computation fails
	 */
	protected double computeHeadingFromAmplitude(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final double ampl = scope.hasArg("amplitude") ? scope.getFloatArg("amplitude") : 359;
		setHeading(agent, getHeading(agent) + scope.getRandom().between(-ampl / 2.0, ampl / 2.0));
		return getHeading(agent);
	}

	/**
	 * Computes the heading for an agent's movement.
	 * If a heading argument is provided in the scope, it updates the agent's heading.
	 * Otherwise, returns the agent's current heading.
	 *
	 * @param scope the execution scope
	 * @param agent the agent whose heading is being computed
	 * @return the heading value in degrees (0-360)
	 * @throws GamaRuntimeException if computation fails
	 */
	protected double computeHeading(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Double heading = scope.hasArg(IKeyword.HEADING) ? scope.getFloatArg(IKeyword.HEADING) : null;
		if (heading != null) { setHeading(agent, heading); }
		return getHeading(agent);
	}

	/**
	 * Computes the maximum distance an agent can travel in the current step.
	 * The distance is calculated by multiplying the agent's speed (from argument or attribute)
	 * by the simulation timestep duration in seconds.
	 * 
	 * Note: The agent's speed attribute is not modified; only the movement primitive is affected.
	 *
	 * @param scope the execution scope
	 * @param agent the agent whose travel distance is being computed
	 * @return the maximum distance the agent can travel in meters
	 * @throws GamaRuntimeException if computation fails
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
	 * Extracts the target shape from the scope arguments.
	 * The target can be any IShape (geometry, agent, location).
	 *
	 * @param scope the execution scope
	 * @param agent the agent for which the target is computed
	 * @return the target shape or null if not found
	 * @throws GamaRuntimeException if target extraction fails
	 */
	protected IShape computeTarget(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Object target = scope.getArg("target", IType.NONE);
		IShape result = null;
		if (target instanceof IShape) {
			result = (IShape) target;
		}
		return result;
	}

	/**
	 * Computes the topology for agent movement.
	 * If an 'on' argument is provided, tries to cast it to a topology.
	 * Otherwise, returns the default topology from the scope.
	 *
	 * @param scope the execution scope
	 * @param agent the agent for which the topology is computed
	 * @return the topology to use for movement
	 * @throws GamaRuntimeException if topology computation fails
	 */
	protected ITopology computeTopology(final IScope scope, final IAgent agent) throws GamaRuntimeException {
		final Object on = scope.getArg("on", IType.NONE);
		final ITopology topo = GamaTopologyFactory.castToTopology(scope, on, false);
		if (topo == null) return scope.getTopology();
		return topo;
	}

	/**
	 * Extracts movement weights from scope arguments.
	 * Weights can be used to modify edge traversal costs in graph-based movement.
	 *
	 * @param scope the execution scope
	 * @return a map of weights or null if not provided
	 * @throws GamaRuntimeException if weight extraction fails
	 */
	protected Map computeMoveWeights(final IScope scope) throws GamaRuntimeException {
		return scope.hasArg("move_weights") ? (Map) scope.getArg("move_weights", IType.MAP) : null;
	}

	/**
	 * Moves the agent randomly within its environment (wander action).
	 * 
	 * <p>The agent moves toward a random heading chosen within an amplitude range around its current heading.
	 * The movement can be constrained by:</p>
	 * <ul>
	 *   <li><b>bounds</b>: A geometry restricting the agent's movement area</li>
	 *   <li><b>on</b>: A graph on which the agent must move</li>
	 * </ul>
	 * 
	 * <p>When moving on a graph, the agent randomly selects edges at intersections, 
	 * optionally weighted by the proba_edges parameter.</p>
	 * 
	 * <p><b>Edge cases:</b></p>
	 * <ul>
	 *   <li>If destination is unreachable, the agent turns 180 degrees</li>
	 *   <li>If bounds constraint is violated, the agent computes a safe location</li>
	 *   <li>For 3D movement, the Z-coordinate is preserved</li>
	 * </ul>
	 *
	 * @param scope the execution scope
	 * @return true if movement was successful
	 * @throws GamaRuntimeException if movement computation fails
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
				PathMovementHelper.MovementResult result = 
						PathMovementHelper.moveAlongGraph(scope, agent, graph, dist, probaDeplacement);
				if (result != null) {
					agent.setAttribute(IKeyword.REAL_SPEED, result.travelledDistance / scope.getClock().getStepInSeconds());
					agent.setAttribute(MovementAttributes.INDEX_ON_PATH, result.finalIndex);
					setCurrentEdge(agent, graph);
					agent.setAttribute(MovementAttributes.INDEX_ON_PATH_SEGMENT, result.finalIndexSegment);
					agent.setAttribute(MovementAttributes.REVERSE, result.finalReverse);
					setLocation(agent, result.finalLocation);
					setHeading(agent, result.computedHeading);
				}
				return true;
			}
			final Object bounds = scope.getArg(IKeyword.BOUNDS, IType.NONE);
			if (bounds != null) {
				IShape geom = GamaShapeFactory.castToShape(scope, bounds, false);

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
	 * Moves the agent forward in its current heading direction (move action).
	 * 
	 * <p>The agent moves straight ahead based on its current heading and speed.
	 * The movement distance is calculated as: speed * timestep_duration</p>
	 * 
	 * <p><b>Parameters:</b></p>
	 * <ul>
	 *   <li><b>speed</b>: Override the agent's speed for this move</li>
	 *   <li><b>heading</b>: Override the agent's heading for this move</li>
	 *   <li><b>bounds</b>: Geometry constraint to keep the agent inside</li>
	 * </ul>
	 * 
	 * <p><b>Edge cases:</b></p>
	 * <ul>
	 *   <li>If destination is unreachable, agent turns 180 degrees</li>
	 *   <li>Updates real_speed with actual distance traveled</li>
	 * </ul>
	 *
	 * @param scope the execution scope
	 * @return the path followed (always null for simple moves)
	 * @throws GamaRuntimeException if movement fails
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
				final IShape geom = GamaShapeFactory.castToShape(scope, bounds, false);
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
	 * Moves the agent along a predefined path (follow action).
	 * 
	 * <p>The agent follows the given path, moving up to the maximum distance determined by
	 * its speed and the timestep. If the path is longer than the distance the agent can cover
	 * in one step, the agent will continue from its current position on the path in the next step.</p>
	 * 
	 * <p><b>Parameters:</b></p>
	 * <ul>
	 *   <li><b>path</b>: Required. The path to follow (IPath)</li>
	 *   <li><b>speed</b>: Override the agent's speed for this move</li>
	 *   <li><b>move_weights</b>: Custom weights for edge traversal (map)</li>
	 *   <li><b>return_path</b>: If true, returns the segment of path actually traveled</li>
	 * </ul>
	 * 
	 * <p><b>Behavior:</b></p>
	 * <ul>
	 *   <li>Updates current_path and current_edge attributes</li>
	 *   <li>Handles 3D paths with Z-coordinate interpolation</li>
	 *   <li>Sets real_speed based on actual distance traveled</li>
	 * </ul>
	 *
	 * @param scope the execution scope
	 * @return the path segment followed if return_path is true, null otherwise
	 * @throws GamaRuntimeException if path following fails
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
		final GamaPath path = scope.hasArg("path") ? (GamaPath) scope.getArg("path", IType.PATH) : null;
		if (path != null && !path.getEdgeList().isEmpty()) {
			PathMovementHelper.MovementResult result = PathMovementHelper.moveAlongPath(
					scope, agent, path, dist, weigths, returnPath != null && returnPath);
			if (result == null) {
				notMoving(agent);
				return null;
			}
			
			// Update agent attributes
			path.setIndexSegementOf(agent, result.finalIndexSegment);
			path.setIndexOf(agent, result.finalIndex);
			agent.setAttribute(IKeyword.REAL_SPEED, result.travelledDistance / scope.getClock().getStepInSeconds());
			setCurrentEdge(agent, path);
			setLocation(agent, result.finalLocation);
			setHeading(agent, result.computedHeading);
			path.setSource(agent.getLocation());
			
			return result.pathFollowed;
		}
		notMoving(agent);
		return null;
	}

	/**
	 * Moves the agent toward a target location (goto action).
	 * 
	 * <p>This is the most versatile movement action, supporting pathfinding on various topologies:</p>
	 * <ul>
	 *   <li><b>Continuous space</b>: Direct movement toward target</li>
	 *   <li><b>Graph topology</b>: Pathfinding using graph algorithms</li>
	 *   <li><b>Grid topology</b>: Grid-based pathfinding (A*, Dijkstra, etc.)</li>
	 * </ul>
	 * 
	 * <p><b>Path caching:</b> The computed path is cached and reused in subsequent steps unless:</p>
	 * <ul>
	 *   <li>The target changes</li>
	 *   <li>recompute_path is true</li>
	 *   <li>The graph is modified (version change detected)</li>
	 * </ul>
	 * 
	 * <p><b>Parameters:</b></p>
	 * <ul>
	 *   <li><b>target</b>: Required. Destination (agent, geometry, or location)</li>
	 *   <li><b>on</b>: Graph, topology, or geometry list constraining movement</li>
	 *   <li><b>speed</b>: Override agent's speed</li>
	 *   <li><b>move_weights</b>: Custom edge weights for pathfinding</li>
	 *   <li><b>recompute_path</b>: Force path recomputation (default: true)</li>
	 *   <li><b>return_path</b>: Return the traveled path segment (default: false)</li>
	 * </ul>
	 * 
	 * <p><b>Special cases:</b></p>
	 * <ul>
	 *   <li>If source equals target, no movement occurs (real_speed = 0)</li>
	 *   <li>If no path exists, agent doesn't move</li>
	 *   <li>Grid topology: target is snapped to nearest grid cell</li>
	 * </ul>
	 *
	 * @param scope the execution scope
	 * @return the path segment followed if return_path is true, null otherwise
	 * @throws GamaRuntimeException if pathfinding or movement fails
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
	public IPath primGoto(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final IPoint source = agent.getLocation().copy(scope);
		final double maxDist = computeDistance(scope, agent);
		IShape goal = computeTarget(scope, agent);
		final Boolean returnPath =
				scope.hasArg("return_path") ? (Boolean) scope.getArg("return_path", IType.NONE) : false;
		IContainer on = null;

		Object onV = scope.getArg("on", IType.NONE);
		Object rt;
		if (onV instanceof IShape && ((IShape) onV).isLine()) {
			rt = onV;
		} else {
			if (onV instanceof ISpecies) {
				on = ((ISpecies) onV).listValue(scope, Types.AGENT, false);
			} else if (onV instanceof final IList ags) {

				on = GamaListFactory.create(Types.AGENT);
				if (!ags.isEmpty() && ags.get(0) instanceof IAgent) {
					((IList) on).addAll(ags);
					onV = ((IAgent) ags.get(0)).getSpecies();
				}
			} else if (onV instanceof IMap) {
				on = GamaMapFactory.wrap(Types.AGENT, Types.NO_TYPE, (IMap) onV);
				onV = ((IAgent) ((IMap) onV).getKeys().get(scope, 0)).getSpecies();
			}
			rt = GamaTopologyFactory.castToTopology(scope, onV instanceof IMap i ? i.keySet() : onV);
		}

		if (on != null && on.isEmpty(scope)) { on = null; }
		final IShape edge = rt instanceof IShape i ? i : null;
		final ITopology topo = rt instanceof ITopology i ? i : scope.getTopology();
		if (goal == null || topo == null) {
			notMoving(agent);
			if (returnPath)
				return GamaPathFactory.createFrom(scope, topo, source, source, GamaListFactory.getEmptyList(), false);
			return null;
		}
		if (topo instanceof GridTopology) {
			// source =
			// ((GamaSpatialMatrix)topo.getPlaces()).getAgentAt(source).getLocation();
			goal = ((GamaSpatialMatrix) topo.getPlaces()).getAgentAt(goal.getLocation()).getLocation();
		}
		if (source.equals(goal.getLocation())) {
			notMoving(agent);
			if (returnPath)
				return GamaPathFactory.createFrom(scope, topo, source, source, GamaListFactory.getEmptyList(), false);

			return null;
		}

		Boolean recomputePath = (Boolean) scope.getArg("recompute_path", IType.NONE);
		if (recomputePath == null) { recomputePath = true; }
		IPath path = (GamaPath) agent.getAttribute(MovementAttributes.CURRENT_PATH);
		if (recomputePath && topo instanceof GridTopology) {
			agent.setAttribute(MovementAttributes.CURRENT_PATH, null);
			path = null;
		}
		if (path == null || path.getTopology(scope) != null && !path.getTopology(scope).equals(topo)
				|| !((IShape) path.getEndVertex()).getLocation().equals(goal.getLocation())
				|| !((IShape) path.getStartVertex()).getLocation().equals(source.getLocation())) {

			if (edge != null) {
				final IList<IShape> edges = GamaListFactory.create(Types.GEOMETRY);
				edges.add(edge);
				path = new GamaSpatialPath(source.getGeometry(), goal, edges, true);
			} else if (topo instanceof GridTopology) {
				if (on instanceof IList) {
					path = ((GridTopology) topo).pathBetween(scope, source, goal, (IList) on);
				} else if (on instanceof IMap) {
					path = ((GridTopology) topo).pathBetween(scope, source, goal, (IMap) on);
				}

			} else {
				path = topo.pathBetween(scope, agent, goal);
			}
		} else if (topo instanceof GraphTopology
				&& (((GraphTopology) topo).getPlaces() != path.getGraph() || recomputePath && ((GraphTopology) topo)
						.getPlaces().getPathComputer().getVersion() != path.getGraphVersion())) {
			path = topo.pathBetween(scope, agent, goal);
		}
		if (path == null) {
			notMoving(agent);
			if (returnPath) return GamaPathFactory.createFrom(scope, topo, source, source,
					GamaListFactory.<IShape> create(Types.GEOMETRY), false);
			return null;
		}

		final IMap weigths = (IMap) computeMoveWeights(scope);
		if (returnPath) {
			PathMovementHelper.MovementResult result = PathMovementHelper.moveAlongPath(
					scope, agent, path, maxDist, weigths, true);
			if (result == null) return GamaPathFactory.createFrom(scope, topo, source, source,
					GamaListFactory.<IShape> create(Types.GEOMETRY), false);
			
			// Update agent attributes
			path.setIndexSegementOf(agent, result.finalIndexSegment);
			path.setIndexOf(agent, result.finalIndex);
			setCurrentEdge(agent, path);
			setLocation(agent, result.finalLocation);
			path.setSource(result.finalLocation.copy(scope));
			agent.setAttribute(IKeyword.REAL_SPEED, result.travelledDistance / scope.getClock().getStepInSeconds());
			setHeading(agent, result.computedHeading);
			
			return result.pathFollowed;
		}
		
		PathMovementHelper.MovementResult result = PathMovementHelper.moveAlongPath(
				scope, agent, path, maxDist, weigths, false);
		if (result != null) {
			// Update agent attributes
			path.setIndexSegementOf(agent, result.finalIndexSegment);
			path.setIndexOf(agent, result.finalIndex);
			agent.setAttribute(IKeyword.REAL_SPEED, result.travelledDistance / scope.getClock().getStepInSeconds());
			setCurrentEdge(agent, path);
			setLocation(agent, result.finalLocation);
			setHeading(agent, result.computedHeading);
			path.setSource(agent.getLocation());
		}
		return null;
	}

	/**
	 * Resets movement-related attributes when agent cannot move.
	 * Sets real_speed to 0 and clears current_edge and current_path.
	 *
	 * @param agent the agent that is not moving
	 */
	private void notMoving(final IAgent agent) {
		setRealSpeed(agent, 0.0);
		agent.setAttribute(MovementAttributes.CURRENT_EDGE, null);
		agent.setAttribute(MovementAttributes.CURRENT_PATH, null);
	}













	/**
	 * Computes a safe forward location when bounds constraint is active.
	 * If the proposed location would violate the bounds geometry, this method
	 * finds the closest valid point on the boundary.
	 * 
	 * <p><b>Algorithm:</b></p>
	 * <ol>
	 *   <li>Creates a line from current location to proposed location</li>
	 *   <li>If line is fully covered by bounds, returns proposed location</li>
	 *   <li>Otherwise, finds closest point on bounds exterior ring</li>
	 *   <li>Returns that point if it intersects bounds, else returns current location</li>
	 * </ol>
	 *
	 * @param scope the execution scope
	 * @param dist the intended travel distance (currently unused)
	 * @param loc the proposed new location
	 * @param geom the bounds geometry constraining movement
	 * @return a safe location within or on the bounds
	 */
	protected IPoint computeLocationForward(final IScope scope, final double dist, final IPoint loc,
			final IShape geom) {
		final IList pts = GamaListFactory.create(Types.POINT);
		pts.add(scope.getAgent().getLocation(scope));
		pts.add(loc);
		final IShape line = SpatialCreation.line(scope, pts);

		if (line == null) return getCurrentAgent(scope).getLocation(scope);
		if (geom.covers(line)) return loc;

		final IPoint computedPt = SpatialPunctal.closest_points_with(line, geom.getExteriorRing(scope)).get(0);
		if (computedPt != null && computedPt.intersects(geom)) return computedPt;
		return getCurrentAgent(scope).getLocation(scope);
	}
}
