/*******************************************************************************************************
 *
 * PedestrianRoadSkill.java, in gaml.extensions.pedestrian, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.pedestrian.skills;

import java.util.Collection;
import java.util.stream.Stream;

import org.locationtech.jts.operation.buffer.BufferParameters;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.graph.IGraph;
import gama.extension.pedestrian.operator.Operators;
import gama.gaml.operators.spatial.SpatialCreation;
import gama.gaml.operators.spatial.SpatialOperators;
import gama.gaml.operators.spatial.SpatialPunctal;
import gama.gaml.operators.spatial.SpatialQueries;
import gama.gaml.operators.spatial.SpatialTransformations;
import gama.gaml.skills.GamlSkill;
import gama.gaml.species.ISpecies;
import gama.gaml.types.GamaIntegerType;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class PedestrianRoadSkill.
 */
@skill (
		name = PedestrianRoadSkill.PEDESTRIAN_ROAD_SKILL,
		concept = { IConcept.TRANSPORT, IConcept.SKILL },
		doc = @doc ("A skill for agents representing pedestrian roads"))
@vars ({ @variable (
		name = PedestrianRoadSkill.AGENTS_ON,
		type = IType.LIST,
		of = IType.AGENT,
		init = "[]",
		doc = @doc ("for each people on the road")),
		@variable (
				name = PedestrianRoadSkill.FREE_SPACE,
				type = IType.GEOMETRY,
				init = "nil",
				doc = @doc ("for each people on the road")),
		@variable (
				name = PedestrianRoadSkill.PEDESTRIAN_ROAD_STATUS,
				type = IType.INT,
				init = "1",
				doc = @doc ("When road status equals 1 it has 2D continuous space property for pedestrian; when equal to 2 is simply a 1D road")),
		@variable (
				name = PedestrianRoadSkill.INTERSECTION_AREAS,
				type = IType.MAP,
				init = "[]",
				doc = @doc ("map of geometries to connect segments linked to this road")),
		@variable (
				name = PedestrianRoadSkill.LINKED_PEDESTRIAN_ROADS,
				type = IType.LIST,
				of = IType.AGENT,
				init = "[]",
				doc = @doc ("the close pedestrian roads")),
		@variable (
				name = PedestrianRoadSkill.EXIT_NODES_HUB,
				type = IType.MAP,
				init = "[]",
				doc = @doc ("The exit hub (several exit connected to each road extremities) that makes it possible to reduce angular distance when travelling to connected pedestrian roads")) })
public class PedestrianRoadSkill extends GamlSkill {

	/** The Constant PEDESTRIAN_ROAD_SKILL. */
	public final static String PEDESTRIAN_ROAD_SKILL = "pedestrian_road";

	/** The Constant LINKED_PEDESTRIAN_ROADS. */
	public final static String LINKED_PEDESTRIAN_ROADS = "linked_pedestrian_roads";

	/** The Constant AGENTS_ON. */
	public final static String AGENTS_ON = "agents_on";

	/** The Constant FREE_SPACE. */
	public final static String FREE_SPACE = "free_space";

	/** The Constant PEDESTRIAN_ROAD_STATUS. */
	public final static String PEDESTRIAN_ROAD_STATUS = "road_status";

	/** The Constant EXIT_NODES_HUB. */
	public final static String EXIT_NODES_HUB = "exit_nodes";

	/** The Constant DISTANCE. */
	public final static String DISTANCE = "distance";

	/** The Constant INTERSECTION_AREAS. */
	public final static String INTERSECTION_AREAS = "intersection_areas";

	/** The Constant SIMPLE_STATUS. */
	public final static int SIMPLE_STATUS = 0; // use simple goto operator on those road

	/** The Constant COMPLEX_STATUS. */
	public final static int COMPLEX_STATUS = 1; // use walk operator

	/**
	 * Gets the agents on.
	 *
	 * @param agent
	 *            the agent
	 * @return the agents on
	 */
	@SuppressWarnings ("unchecked")
	@getter (AGENTS_ON)
	public static IList<IAgent> getAgentsOn(final IAgent agent) {
		return (IList<IAgent>) agent.getAttribute(AGENTS_ON);
	}

	/**
	 * Gets the linked pedestrian roads.
	 *
	 * @param agent
	 *            the agent
	 * @return the linked pedestrian roads
	 */
	@SuppressWarnings ("unchecked")
	@getter (LINKED_PEDESTRIAN_ROADS)
	public static IList<IAgent> getLinkedPedestrianRoads(final IAgent agent) {
		return (IList<IAgent>) agent.getAttribute(LINKED_PEDESTRIAN_ROADS);
	}

	/**
	 * Gets the exit nodes hub.
	 *
	 * @param agent
	 *            the agent
	 * @return the exit nodes hub
	 */
	@SuppressWarnings ("unchecked")
	@getter (EXIT_NODES_HUB)
	public static IMap<GamaPoint, IList<GamaPoint>> getExitNodesHub(final IAgent agent) {
		return (IMap<GamaPoint, IList<GamaPoint>>) agent.getAttribute(EXIT_NODES_HUB);
	}

	/**
	 * Sets the exit nodes hub.
	 *
	 * @param agent
	 *            the agent
	 * @param exitNodesHub
	 *            the exit nodes hub
	 */
	@setter (EXIT_NODES_HUB)
	public static void setExitNodesHub(final IAgent agent, final IMap<GamaPoint, IList<GamaPoint>> exitNodesHub) {
		agent.setAttribute(EXIT_NODES_HUB, exitNodesHub);
	}

	/**
	 * Gets the connected segments intersection.
	 *
	 * @param agent
	 *            the agent
	 * @return the connected segments intersection
	 */
	@SuppressWarnings ("unchecked")
	@getter (INTERSECTION_AREAS)
	public static IMap<IAgent, IShape> getConnectedSegmentsIntersection(final IAgent agent) {
		return (IMap<IAgent, IShape>) agent.getAttribute(INTERSECTION_AREAS);
	}

	/**
	 * Sets the connected segments intersection.
	 *
	 * @param agent
	 *            the agent
	 * @param map
	 *            the map
	 */
	@setter (INTERSECTION_AREAS)
	public static void setConnectedSegmentsIntersection(final IAgent agent, final IMap<IAgent, IShape> map) {
		agent.setAttribute(INTERSECTION_AREAS, map);
	}

	/**
	 * Gets the pedestrian road status.
	 *
	 * @param agent
	 *            the agent
	 * @return the pedestrian road status
	 */
	@getter (PEDESTRIAN_ROAD_STATUS)
	public static int getPedestrianRoadStatus(final IAgent agent) {
		return (int) agent.getAttribute(PEDESTRIAN_ROAD_STATUS);
	}

	/**
	 * Sets the pedestrian road status.
	 *
	 * @param agent
	 *            the agent
	 * @param status
	 *            the status
	 */
	@setter (PEDESTRIAN_ROAD_STATUS)
	public static void setPedestrianRoadStatus(final IAgent agent, final int status) {
		agent.setAttribute(PEDESTRIAN_ROAD_STATUS, status);
	}

	/**
	 * Gets the free space.
	 *
	 * @param agent
	 *            the agent
	 * @return the free space
	 */
	@getter (FREE_SPACE)
	public static IShape getFreeSpace(final IAgent agent) {
		return (IShape) agent.getAttribute(FREE_SPACE);
	}

	/**
	 * Sets the free space.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (FREE_SPACE)
	public static void setFreeSpace(final IAgent agent, final IShape val) {
		agent.setAttribute(FREE_SPACE, val);
	}

	/**
	 * Sets the distance.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (DISTANCE)
	public static void setDistance(final IAgent agent, final Double val) {
		agent.setAttribute(DISTANCE, val);
	}

	/**
	 * Gets the distance.
	 *
	 * @param agent
	 *            the agent
	 * @return the distance
	 */
	@getter (DISTANCE)
	public static Double getDistance(final IAgent agent) {
		return (Double) agent.getAttribute(DISTANCE);
	}

	/**
	 * Gets the close agents.
	 *
	 * @param agent
	 *            the agent
	 * @return the close agents
	 */
	public static IList<IAgent> getCloseAgents(final IAgent agent) {
		IList<IAgent> agents = GamaListFactory.create();
		agents.addAll(getAgentsOn(agent));
		for (IAgent ag : getLinkedPedestrianRoads(agent)) { agents.addAll(getAgentsOn(ag)); }
		return agents;
	}

	/**
	 * Prim initialize.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "initialize",
			args = { @arg (
					name = "distance",
					type = IType.FLOAT,
					optional = true,
					doc = @doc ("the maximal distance to the road")),
					@arg (
							name = "obstacles",
							type = IType.CONTAINER,
							optional = true,
							doc = @doc ("the list of species to consider as obstacles to remove from the free space")),
					@arg (
							name = "distance_extremity",
							type = IType.FLOAT,
							optional = true,
							doc = @doc ("the distance added to the extremimity to connect to other road (in meters)")),
					@arg (
							name = "bounds",
							type = IType.CONTAINER,
							optional = true,
							doc = @doc ("the geometries (the localized entity geometries) that restrains the agent movement (the agent moves inside this geometry")),
					@arg (
							name = "masked_by",
							type = IType.CONTAINER,
							optional = true,
							doc = @doc ("if defined, keep only the part of the geometry that is visible from the location of the road considering the given obstacles")),
					@arg (
							name = "masked_by_precision",
							type = IType.INT,
							optional = true,
							doc = @doc ("if masked_by is defined, number of triangles used to compute the visible geometries (default: 120)")),
					@arg (
							name = "status",
							type = IType.INT,
							optional = true,
							doc = @doc ("the status (int) of the road: 1 (default) for roads where agent move on a continuous 2D space and 0 for 1D roads with queu-in queu-out like movement")), },
			doc = @doc (
					value = "action to initialize the free space of roads",
					examples = { @example ("do initialize distance: 10.0 obstacles: [building];") }))
	@SuppressWarnings ("unchecked")
	public boolean primInitialize(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);

		int status = scope.hasArg("status") ? scope.getIntArg("status")
				: agent.getGeometry().hasAttribute(PEDESTRIAN_ROAD_STATUS) ? GamaIntegerType.staticCast(scope,
						agent.getGeometry().getAttribute(PEDESTRIAN_ROAD_STATUS), null, false)
				: 1;
		setPedestrianRoadStatus(agent, status);
		double distAdd = scope.hasArg("distance_extremity") ? scope.getFloatArg("distance_extremity") : 0.0;
		IShape freeSpace = agent.getGeometry().copy(scope);
		freeSpace = SpatialTransformations.scaled_by(scope, freeSpace,
				(freeSpace.getPerimeter() + distAdd) / freeSpace.getPerimeter());

		if (status == COMPLEX_STATUS) {
			double dist = scope.hasArg("distance") ? scope.getFloatArg("distance") : 0.0;
			setDistance(agent, dist);

			if (dist > 0) {
				freeSpace = SpatialTransformations.enlarged_by(scope, freeSpace, dist,
						BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_FLAT);
			}

			if (scope.hasArg("obstacles")) {
				IContainer obstaclesLC = (IContainer) scope.getArg("obstacles", IType.CONTAINER);
				if (obstaclesLC instanceof ISpecies) {
					IContainer<?, IShape> obstacles = SpatialQueries.overlapping(scope, obstaclesLC, freeSpace);
					IShape obstGeom = SpatialOperators.union(scope, obstacles);
					obstGeom = SpatialTransformations.enlarged_by(scope, obstGeom, dist / 1000.0);
					freeSpace = SpatialOperators.minus(scope, freeSpace, obstGeom);
				} else {
					IList obstaclesL = obstaclesLC.listValue(scope, Types.NO_TYPE, false);
					if (!obstaclesL.isEmpty()) {
						IList<ISpecies> speciesList = obstaclesL.get(0) instanceof ISpecies ? obstaclesL : null;
						if (speciesList != null) {
							for (ISpecies species : speciesList) {
								IContainer<?, IShape> obstacles =
										(IContainer<?, IShape>) SpatialQueries.overlapping(scope, species, freeSpace);
								IShape obstGeom = SpatialOperators.union(scope, obstacles);
								obstGeom = SpatialTransformations.enlarged_by(scope, obstGeom, dist / 1000.0);
								freeSpace = SpatialOperators.minus(scope, freeSpace, obstGeom);
							}
						} else {
							IList<IShape> obstaclesS = GamaListFactory.create();
							for (Object obj : obstaclesL) {
								if (obj instanceof IShape) { obstaclesS.add((IShape) obj); }
							}
							IContainer<?, IShape> obstacles =
									(IContainer<?, IShape>) SpatialQueries.overlapping(scope, obstaclesS, freeSpace);
							IShape obstGeom = SpatialOperators.union(scope, obstacles);
							obstGeom = SpatialTransformations.enlarged_by(scope, obstGeom, dist / 1000.0);
							freeSpace = SpatialOperators.minus(scope, freeSpace, obstGeom);

						}
					}
				}
			}

			IContainer bounds = scope.hasArg("bounds") ? (IContainer) scope.getArg("bounds", IType.CONTAINER) : null;
			if (bounds != null) {
				IShape bds = SpatialOperators.union(scope, bounds);
				IShape g = SpatialOperators.inter(scope, freeSpace, bds);
				if (g != null) { freeSpace = g; }
			}
			if (freeSpace == null) return false;
			if (freeSpace.getGeometries().size() > 1) {
				for (IShape g : freeSpace.getGeometries()) {
					if (agent.intersects(g)) {
						freeSpace = g;
						break;
					}
				}
			}

			if (scope.hasArg("masked_by")) {
				IContainer maskedbyLC = (IContainer) scope.getArg("masked_by", IType.CONTAINER);
				Integer prec = scope.hasArg("masked_by_precision") ? scope.getIntArg("masked_by_precision") : null;
				if (maskedbyLC instanceof ISpecies) {
					freeSpace = SpatialOperators.masked_by(scope, freeSpace, maskedbyLC, prec);
				} else {
					IList maskedbyL = maskedbyLC.listValue(scope, Types.NO_TYPE, false);
					if (!maskedbyL.isEmpty()) {
						IList<IShape> obstacles = GamaListFactory.create();
						IList<ISpecies> speciesList = maskedbyL.get(0) instanceof ISpecies ? maskedbyL : null;
						if (speciesList != null) {
							for (ISpecies species : speciesList) {
								obstacles.addAll((Collection<? extends IShape>) species.getPopulations(scope));
							}
						} else {
							for (Object obj : maskedbyL) { if (obj instanceof IShape) { obstacles.add((IShape) obj); } }
						}
						freeSpace = SpatialOperators.masked_by(scope, freeSpace, obstacles, prec);
					}
				}
			}
		}

		setFreeSpace(agent, freeSpace);
		return true;
	}

	/**
	 * Prim intersection areas.
	 *
	 * @param scope
	 *            the scope
	 */
	@action (
			name = "build_intersection_areas",
			args = { @arg (
					name = PedestrianSkill.PEDESTRIAN_GRAPH,
					type = IType.GRAPH,
					optional = false,
					doc = @doc ("The pedestrian network from which to find connected corridors")) },
			doc = @doc (
					value = "Build intersection areas with connected roads",
					examples = { @example ("do build_intersection_areas pedestrian_graph: pedestrian_network;") }))
	public boolean primIntersectionAreas(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		if (!agent.isInstanceOf(PEDESTRIAN_ROAD_SKILL, true)) throw GamaRuntimeException
				.error("Trying to manipulate agent with " + PEDESTRIAN_ROAD_SKILL + " while being " + agent, scope);
		IShape g = PedestrianRoadSkill.getFreeSpace(agent);
		@SuppressWarnings ("unchecked") final IGraph<IShape, IAgent> graph =
				(IGraph<IShape, IAgent>) scope.getVarValue(PedestrianSkill.PEDESTRIAN_GRAPH);
		IMap<IAgent, IShape> connectedComp = GamaMapFactory.create();
		IShape target = graph.getEdgeTarget(agent);
		IShape source = graph.getEdgeSource(agent);
		IList<IAgent> connectedRoads = getLinkedPedestrianRoads(agent);
		for (Object obj : graph.outgoingEdgesOf(source)) {
			IAgent ag = (IAgent) obj;
			connectedComp.put(ag, SpatialOperators.inter(scope, g, PedestrianRoadSkill.getFreeSpace(ag)));
		}
		for (Object obj : graph.incomingEdgesOf(source)) {
			IAgent ag = (IAgent) obj;
			connectedComp.put(ag, SpatialOperators.inter(scope, g, PedestrianRoadSkill.getFreeSpace(ag)));
		}

		for (Object obj : graph.outgoingEdgesOf(target)) {
			IAgent ag = (IAgent) obj;
			connectedComp.put(ag, SpatialOperators.inter(scope, g, PedestrianRoadSkill.getFreeSpace(ag)));
		}
		for (Object obj : graph.incomingEdgesOf(target)) {
			IAgent ag = (IAgent) obj;
			connectedComp.put(ag, SpatialOperators.inter(scope, g, PedestrianRoadSkill.getFreeSpace(ag)));
		}

		connectedComp.remove(agent);
		for (IAgent ag : agent.getSpecies().getPopulation(scope)) {
			if (ag == agent) { continue; }
			if (getFreeSpace(ag).intersects(g)) { connectedRoads.add(ag); }
		}

		PedestrianRoadSkill.setConnectedSegmentsIntersection(agent, connectedComp);
		return true;
	}

	/**
	 * Prim exit hub escape.
	 *
	 * @param scope
	 *            the scope
	 */
	@action (
			name = "build_exit_hub",
			args = { @arg (
					name = PedestrianSkill.PEDESTRIAN_GRAPH,
					type = IType.GRAPH,
					optional = false,
					doc = @doc ("The pedestrian network from which to find connected corridors")),
					@arg (
							name = "distance_between_targets",
							type = IType.FLOAT,
							optional = false,
							doc = @doc ("min distances between 2 targets")) },
			doc = @doc (
					value = "Add exit hub to pedestrian corridor to reduce angular distance between node of the network",
					examples = {
							@example ("do build_exit_hub pedestrian_graph: pedestrian_network distance_between_targets: 10.0;") }))
	public boolean primExitHubEscape(final IScope scope) {

		// TODO : Exit hub should probably be symmetric ...
		final IAgent agent = getCurrentAgent(scope);
		if (!agent.isInstanceOf(PEDESTRIAN_ROAD_SKILL, true)) throw GamaRuntimeException
				.error("Trying to manipulate agent with " + PEDESTRIAN_ROAD_SKILL + " while being " + agent, scope);

		final Double dist = scope.getFloatArg("distance_between_targets");
		@SuppressWarnings ("unchecked") IMap<GamaPoint, IList<GamaPoint>> exitHub = GamaMapFactory.create();
		IShape bounds = SpatialTransformations.reduced_by(scope, getFreeSpace(agent), dist);
		if (getRoadStatus(scope, agent) == SIMPLE_STATUS) {
			// AD Note: getPoints() can be a very costly operation. It'd be better to call it once.
			for (GamaPoint p : agent.getPoints()) {
				GamaPoint pt = p;
				IList<GamaPoint> ptL = GamaListFactory.create();
				ptL.add(pt);
				exitHub.put(pt, ptL);
			}
		} else {
			for (int i = 0; i < agent.getPoints().size(); i++) {
				GamaPoint pt = agent.getPoints().get(i).copy(scope);
				GamaPoint pp = i == 0 ? agent.getPoints().get(i + 1) : agent.getPoints().get(i - 1);
				exitHub.put(agent.getPoints().get(i), connectedRoads(scope, agent, dist, pt, pp, bounds));
			}
		}
		PedestrianRoadSkill.setExitNodesHub(agent, exitHub);
		return true;
	}

	/**
	 * To quickly access free space within the plugin
	 *
	 * @param scope
	 * @param road
	 * @return
	 */
	public static IShape getFreeSpace(final IScope scope, final IShape road) {
		return (IShape) road.getAttribute(FREE_SPACE);
	}

	/**
	 * To quickly access to road status within the plugin
	 *
	 * @param scope
	 * @param road
	 * @return
	 */
	public static int getRoadStatus(final IScope scope, final IShape road) {
		return (int) road.getAttribute(PEDESTRIAN_ROAD_STATUS);
	}

	/**
	 * To quickly access to exit nodes from the hub. If no exit hub, will only return the exit point of the road
	 *
	 * @param currentRoad
	 * @param target
	 * @return
	 */
	@SuppressWarnings ("unchecked")
	public static IList<GamaPoint> getConnectedOutput(final IScope scope, final IShape currentRoad,
			final GamaPoint target) {
		if (!currentRoad.hasAttribute(EXIT_NODES_HUB)) throw GamaRuntimeException
				.error("Looking for exit hub related to " + currentRoad + " but there is none", scope);
		IMap<GamaPoint, IList<GamaPoint>> exitHub =
				(IMap<GamaPoint, IList<GamaPoint>>) currentRoad.getAttribute(EXIT_NODES_HUB);
		if (exitHub.containsKey(target)) return exitHub.get(target);
		return GamaListFactory.create(Types.POINT, Stream.of(target));
	}

	/**
	 * To register any agent (not necessary pedestrian agent) to be on the pedestrian road segment
	 *
	 * @param scope
	 * @param road
	 * @param pedestrian
	 */
	@SuppressWarnings ("unchecked")
	public static void register(final IScope scope, final IAgent road, final IAgent pedestrian) {
		((IList<IAgent>) road.getAttribute(AGENTS_ON)).add(pedestrian);
		if (!pedestrian.getLocation().intersects(getFreeSpace(road))) {
			pedestrian.setLocation(SpatialPunctal._closest_point_to(pedestrian.getLocation(), getFreeSpace(road)));
		}
		pedestrian.setAttribute("current_edge", road);
	}

	/**
	 * To unregister an agent from the set of agent on the pedestrian road segment
	 *
	 * @param scope
	 * @param road
	 * @param pedestrian
	 */
	@SuppressWarnings ("unchecked")
	public static boolean unregister(final IScope scope, final IAgent road, final IAgent pedestrian) {
		((IList<IAgent>) road.getAttribute(AGENTS_ON)).remove(pedestrian);
		pedestrian.setAttribute("current_edge", null);
		return true;
	}

	/**
	 * Connected roads.
	 *
	 * @param scope
	 *            the scope
	 * @param currentRoad
	 *            the current road
	 * @param dist
	 *            the dist
	 * @param lp
	 *            the lp
	 * @param pp
	 *            the pp
	 * @param bounds
	 *            the bounds
	 * @return the i list
	 */
	/*
	 * Create exit hub for a set of connected out edges
	 */
	@SuppressWarnings ("unchecked")
	private IList<GamaPoint> connectedRoads(final IScope scope, final IAgent currentRoad, final Double dist,
			final GamaPoint lp, final GamaPoint pp, final IShape bounds) {
		IList<GamaPoint> exitConnections = GamaListFactory.create();
		exitConnections.add(lp.copy(scope));
		double distR = getDistance(currentRoad);
		if (distR <= 0 || bounds == null || bounds.getArea() <= 0.001) return exitConnections;

		GamaPoint v = lp.minus(pp);
		GamaPoint n = null;
		if (v.x == 0) {
			n = new GamaPoint(1, 0);
		} else if (v.y == 0) {
			n = new GamaPoint(0, 1);
		} else {
			double nx = -v.y / v.x;
			double norm = Math.sqrt(nx * nx + 1);
			n = new GamaPoint(nx / norm, 1 / norm);
		}
		n = n.multiplyBy(distR);
		IList<IShape> points = GamaListFactory.create();
		points.add(lp.minus(n));
		points.add(lp.add(n));

		IShape hole = SpatialCreation.line(scope, points);
		if (hole == null || hole.getPerimeter() <= dist) return exitConnections;

		IList<GamaPoint> pts = SpatialPunctal.points_on(hole, dist);
		pts.removeIf(p -> p == null || !bounds.intersects(p));
		exitConnections.addAll(pts);

		return exitConnections;
	}

}
