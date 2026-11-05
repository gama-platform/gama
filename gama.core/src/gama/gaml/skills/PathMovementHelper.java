/*******************************************************************************************************
 *
 * PathMovementHelper.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
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
import java.util.List;

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import gama.api.GAMA;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
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
import gama.api.utils.interfaces.IAgentFilter;
import gama.core.topology.filter.In;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.core.util.path.GamaSpatialPath;
import gama.gaml.operators.spatial.SpatialPunctal;
import gama.gaml.operators.spatial.SpatialRelations;

/**
 * PathMovementHelper - Utility class for handling agent movement along paths and graphs. Extracted from MovingSkill to
 * improve modularity and maintainability.
 *
 * @author drogoul
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class PathMovementHelper {

	/**
	 * Result of path initialization containing current position and indices.
	 */
	public static class PathInitResult {

		/** The index. */
		public int index;

		/** The index segment. */
		public int indexSegment;

		/** The end index segment. */
		public int endIndexSegment;

		/** The current location. */
		public IPoint currentLocation;

		/** The false target. */
		public IPoint falseTarget;

		/** The reverse. */
		public int reverse; // Used for graph movement

		/**
		 * Instantiates a new path init result.
		 *
		 * @param index
		 *            the index
		 * @param indexSegment
		 *            the index segment
		 * @param endIndexSegment
		 *            the end index segment
		 * @param currentLocation
		 *            the current location
		 * @param falseTarget
		 *            the false target
		 */
		public PathInitResult(final int index, final int indexSegment, final int endIndexSegment, final IPoint currentLocation,
				final IPoint falseTarget) {
			this.index = index;
			this.indexSegment = indexSegment;
			this.endIndexSegment = endIndexSegment;
			this.currentLocation = currentLocation;
			this.falseTarget = falseTarget;
		}

		/**
		 * Instantiates a new path init result.
		 *
		 * @param index
		 *            the index
		 * @param indexSegment
		 *            the index segment
		 * @param reverse
		 *            the reverse
		 */
		public PathInitResult(final int index, final int indexSegment, final int reverse) {
			this.index = index;
			this.indexSegment = indexSegment;
			this.reverse = reverse;
		}
	}

	/**
	 * Result of movement along a path.
	 */
	public static class MovementResult {

		/** The final location. */
		public IPoint finalLocation;

		/** The final index. */
		public int finalIndex;

		/** The final index segment. */
		public int finalIndexSegment;

		/** The final reverse. */
		public int finalReverse;

		/** The travelled distance. */
		public double travelledDistance;

		/** The computed heading. */
		public double computedHeading;

		/** The path followed. */
		public IPath pathFollowed; // Only set when return_path is true

		public IShape finalEdge;

		/**
		 * Instantiates a new movement result.
		 *
		 * @param finalLocation
		 *            the final location
		 * @param finalIndex
		 *            the final index
		 * @param finalIndexSegment
		 *            the final index segment
		 * @param travelledDistance
		 *            the travelled distance
		 * @param computedHeading
		 *            the computed heading
		 */
		public MovementResult(final IPoint finalLocation, final int finalIndex, final int finalIndexSegment, final double travelledDistance,
				final double computedHeading) {
			this.finalLocation = finalLocation;
			this.finalIndex = finalIndex;
			this.finalIndexSegment = finalIndexSegment;
			this.travelledDistance = travelledDistance;
			this.computedHeading = computedHeading;
		}
	}

	/**
	 * Initializes movement along a path (3D variant).
	 *
	 * @param agent
	 *            the agent moving
	 * @param path
	 *            the path to follow
	 * @param cl
	 *            the current location
	 * @return initialization values (index, indexSegment, endIndexSegment, currentLocation, falseTarget)
	 */
	public static IList initMoveAlongPath3D(final IAgent agent, final IPath path, final IPoint cl) {
		return initializationValues(initializePath3D(agent, path, cl));
	}

	private static IList initializationValues(final PathInitResult result) {
		return result == null ? null : GamaListFactory.createWithoutCasting(Types.NO_TYPE, result.index,
				result.indexSegment, result.endIndexSegment, result.currentLocation, result.falseTarget);
	}

	private static PathInitResult initializePath3D(final IAgent agent, final IPath path, final IPoint cl) {
		IPoint currentLocation = cl.copy(GAMA.getRuntimeScope());

		Integer index = 0;
		Integer indexSegment = 1;
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
		final GamaSpatialPath.MovementTarget target = ((GamaSpatialPath) path).getMovementTarget();
		return new PathInitResult(index, indexSegment, target.segmentIndex(), currentLocation, target.point());
	}

	/**
	 * Initializes movement along a path (standard variant). Finds the closest point on the path to the agent's current
	 * location and determines the segment indices for starting the movement.
	 *
	 * @param agent
	 *            the agent moving
	 * @param path
	 *            the path to follow
	 * @param cl
	 *            the current location
	 * @return initialization values (index, indexSegment, endIndexSegment, currentLocation, falseTarget)
	 */
	public static IList initMoveAlongPath(final IAgent agent, final IPath path, final IPoint cl) {
		return initializationValues(initializePath(agent, path, cl));
	}

	private static PathInitResult initializePath(final IAgent agent, final IPath path, final IPoint cl) {
		IPoint currentLocation = cl;
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
			final GamaSpatialPath.MovementTarget target = ((GamaSpatialPath) path).getMovementTarget();
			falseTarget = target.point();
			endIndexSegment = target.segmentIndex();
		}
		return new PathInitResult(index, indexSegment, endIndexSegment, currentLocation, falseTarget);
	}

	/**
	 * Initializes movement along a graph. Determines the starting edge and segment for an agent moving on a spatial
	 * graph.
	 *
	 * @param scope
	 *            the execution scope
	 * @param agent
	 *            the agent moving
	 * @param graph
	 *            the spatial graph
	 * @param currentLoc
	 *            the current location
	 * @return initialization values (index, indexSegment, reverse)
	 */
	public static IList initMoveAlongPath(final IScope scope, final IAgent agent, final GamaSpatialGraph graph,
			final IPoint currentLoc) {
		final PathInitResult result = initializeGraph(scope, agent, graph, currentLoc);
		return result == null ? null : GamaListFactory.createWithoutCasting(Types.NO_TYPE, result.index,
				result.indexSegment, result.reverse);
	}

	@SuppressWarnings ("null")
	private static PathInitResult initializeGraph(final IScope scope, final IAgent agent, final GamaSpatialGraph graph,
			final IPoint currentLoc) {
		IPoint currentLocation = currentLoc;
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
						final java.util.Collection<IAgent> ags = scope.getSimulation().getTopology()
								.getNeighborsOf(scope, currentLocation, dist, filter);
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
					index = graph.getEdgeIndex(line);
				} else {
					double distanceS = Double.MAX_VALUE;
					int i = 0;
					for (final IShape candidate : edges) {
						final double distS = candidate.euclidianDistanceTo(currentLocation);
						if (distS < distanceS) {
							distanceS = distS;
							index = i;
						}
						i++;
					}
					line = (IShape) graph.getEdgeAt(index);
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
									points[i].toCoordinate(), points[i + 1].toCoordinate());
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

		return new PathInitResult(index, indexSegment, reverse);
	}

	/**
	 * Computes the weight for movement along an edge. Weight is calculated as edge weight divided by edge perimeter.
	 *
	 * @param graph
	 *            the graph containing the edge
	 * @param path
	 *            the path being followed
	 * @param line
	 *            the edge shape
	 * @return the weight value for movement calculation
	 */
	public static double computeWeight(final IGraph graph, final IPath path, final IShape line) {
		if (graph == null || path == null || line == null) return 1.0;
		final IShape realShape = path.getRealObject(line);
		if (realShape == null || realShape.getGeometry() == null) return 1;
		final double perimeter = realShape.getGeometry().getPerimeter();
		return perimeter == 0 ? 1 : graph.getEdgeWeight(realShape) / perimeter;
	}

	private static CoordinateSequence coordinatesOf(final IShape shape) {
		final Geometry geometry = shape.getInnerGeometry();
		return geometry instanceof LineString line ? line.getCoordinateSequence()
				: new CoordinateArraySequence(geometry.getCoordinates());
	}

	private static IPoint interpolate(final IPoint from, final IPoint to, final double ratio) {
		return GamaPointFactory.create(from.getX() + (to.getX() - from.getX()) * ratio,
				from.getY() + (to.getY() - from.getY()) * ratio,
				from.getZ() + (to.getZ() - from.getZ()) * ratio);
	}

	/**
	 * Moves an agent to the next location along a graph, handling random edge selection. This method handles movement
	 * on graphs where the agent can wander randomly.
	 *
	 * @param scope
	 *            the execution scope
	 * @param agent
	 *            the agent to move
	 * @param graph
	 *            the spatial graph to move on
	 * @param distance
	 *            the maximum distance to travel
	 * @param probaEdge
	 *            optional probability map for edge selection
	 * @return movement result with final position and statistics
	 */
	public static MovementResult moveAlongGraph(final IScope scope, final IAgent agent, final GamaSpatialGraph graph,
			final double distance, final IMap probaEdge) {
		IPoint currentLocation = agent.getLocation().copy(scope);
		final PathInitResult indexVals = initializeGraph(scope, agent, graph, currentLocation);
		if (indexVals == null) return null;

		int index = indexVals.index;
		int indexSegment = indexVals.indexSegment;
		int inverse = indexVals.reverse;
		IShape edge = (IShape) graph.getEdgeAt(index);
		double remainingDistance = distance;
		double travelledDist = 0.0;
		IPoint headingFrom = null, headingTo = null;

		while (true) {
			final CoordinateSequence coords = coordinatesOf(edge);
			final boolean reversed = !graph.isDirected() && inverse == 1;

			final double perimeter = edge.getGeometry() == null ? 0 : edge.getGeometry().getPerimeter();
			final double weight = perimeter == 0 ? 1 : graph.getEdgeWeight(edge) / perimeter;
			for (int j = indexSegment; j < coords.size(); j++) {
				final IPoint pt = GamaPointFactory.create(coords.getCoordinate(reversed ? coords.size() - 1 - j : j));
				final double dis = pt.distance3D(currentLocation);
				final double dist = weight * dis;
				headingFrom = currentLocation;
				headingTo = pt;

				if (remainingDistance < dist) {
					final double ratio = remainingDistance / dist;
					travelledDist += dis * ratio;
					currentLocation = interpolate(currentLocation, pt, ratio);
					remainingDistance = 0;
					break;
				}
				if (remainingDistance <= dist) {
					currentLocation = pt;
					travelledDist += dis;
					remainingDistance = 0;
					if (indexSegment < coords.size() - 1) {
						indexSegment++;
					} else {
						indexSegment = 1;
					}
					break;
				}
				currentLocation = pt;
				travelledDist += dis;

				remainingDistance = remainingDistance - dist;
				indexSegment++;
				if (j == coords.size() - 1) {
					IShape node = (IShape) graph.getEdgeTarget(edge);
					if (!graph.isDirected() && !node.getLocation().equals(currentLocation)) {
						node = (IShape) graph.getEdgeSource(edge);
					}
					final List<IShape> nextRoads = new ArrayList<IShape>(
							graph.isDirected() ? graph.outgoingEdgesOf(node) : graph.edgesOf(node));
					if (nextRoads.isEmpty()) {
						remainingDistance = 0;
						break;
					}
					if (nextRoads.size() == 1) {
						edge = nextRoads.get(0);
					} else if (probaEdge == null || probaEdge.isEmpty()) {
						edge = nextRoads.get(scope.getRandom().between(0, nextRoads.size() - 1));
					} else {
						final IList<Double> distribution = GamaListFactory.create(Types.FLOAT);
						for (final IShape r : nextRoads) {
							final Double val = (Double) probaEdge.get(r);
							distribution.add(val == null ? 0.0 : val);
						}
						edge = nextRoads.get(scope.getRandom().choiceIn(distribution));
					}
					index = graph.getEdgeIndex(edge);
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
			if (remainingDistance == 0) { break; }
			indexSegment = 1;
		}

		final double computedHeading = headingFrom == null ? 0 : SpatialRelations.towards(scope, headingFrom, headingTo);
		MovementResult result =
				new MovementResult(currentLocation, index, indexSegment, travelledDist, computedHeading);
		result.finalReverse = inverse;
		result.finalEdge = edge;
		return result;
	}

	/**
	 * Moves an agent along a path, optionally returning the path followed. This unified method handles both simple
	 * movement and path tracking.
	 *
	 * @param scope
	 *            the execution scope
	 * @param agent
	 *            the agent to move
	 * @param path
	 *            the path to follow
	 * @param distance
	 *            the maximum distance to travel
	 * @param weights
	 *            optional weights for edges
	 * @param returnPath
	 *            whether to build and return the followed path
	 * @return movement result with optional path followed
	 */
	public static MovementResult moveAlongPath(final IScope scope, final IAgent agent, final IPath path,
			final double distance, final IMap weights, final boolean returnPath) {
		final IPoint startLocation = returnPath ? agent.getLocation().copy(scope) : null;
		IPoint currentLocation = agent.getLocation().copy(scope);

		final PathInitResult indexVals = ((GamaSpatialPath) path).isThreeD() ? initializePath3D(agent, path, currentLocation)
				: initializePath(agent, path, currentLocation);
		if (indexVals == null) return null;

		final IList<IShape> segments = returnPath ? GamaListFactory.create(Types.GEOMETRY) : null;
		final IMap agents = returnPath ? GamaMapFactory.createUnordered() : null;

		int index = indexVals.index;
		int indexSegment = indexVals.indexSegment;
		final int endIndexSegment = indexVals.endIndexSegment;
		currentLocation = indexVals.currentLocation;
		final IPoint falseTarget = indexVals.falseTarget;
		final IList<IShape> edges = path.getEdgeGeometry();
		final int nb = edges.size();
		double remainingDistance = distance;
		double travelledDist = 0.0;
		IPoint headingFrom = null, headingTo = null;
		final GamaSpatialGraph graph = (GamaSpatialGraph) path.getGraph();

		for (int i = index; i < nb; i++) {
			final IShape line = edges.get(i);
			final CoordinateSequence coords = coordinatesOf(line);

			final IShape realShape = path.getRealObject(line);
			final IShape realGeometry = realShape == null ? null : realShape.getGeometry();
			final double perimeter = realGeometry == null ? 0 : realGeometry.getPerimeter();
			final Double edgeWeight = weights == null || realShape == null ? null : (Double) weights.get(realShape);
			final double weight = perimeter == 0 ? 1 : edgeWeight != null ? edgeWeight / perimeter
					: graph == null ? 1 : graph.getEdgeWeight(realShape) / perimeter;

			for (int j = indexSegment; j < coords.size(); j++) {
				IPoint pt = null;
				if (i == nb - 1 && j == endIndexSegment) {
					pt = falseTarget;
				} else {
					pt = GamaPointFactory.create(coords.getCoordinate(j));
				}
				final double dis = pt.distance3D(currentLocation);
				final double dist = weight * dis;
				headingFrom = currentLocation;
				headingTo = pt;

				if (remainingDistance < dist) {
					if (returnPath) {
						final IPoint pto = currentLocation.copy(scope);
						final double ratio = remainingDistance / dist;
						travelledDist += dis * ratio;
						currentLocation = interpolate(currentLocation, pt, ratio);
						remainingDistance = 0;

						final IShape gl = GamaShapeFactory.buildLine(pto, currentLocation);
						final IShape sh = path.getRealObject(line);
						if (sh != null) {
							final IAgent a = sh.getAgent();
							if (a != null) { agents.put(gl, a); }
						}
						segments.add(gl);
					} else {
						final double ratio = remainingDistance / dist;
						travelledDist += dis * ratio;
						currentLocation = interpolate(currentLocation, pt, ratio);
						remainingDistance = 0;
					}
					break;
				}
				if (remainingDistance <= dist) {
					travelledDist += dis;
					if (returnPath) {
						final IShape gl = GamaShapeFactory.buildLine(currentLocation, pt);
						if (path.getRealObject(line) != null) {
							final IAgent a = path.getRealObject(line).getAgent();
							if (a != null) { agents.put(gl, a); }
						}
						segments.add(gl);
					}
					currentLocation = pt;
					remainingDistance = 0;
					if (indexSegment < coords.size() - 1) {
						indexSegment++;
					} else {
						if (index < nb - 1) { index++; }
						indexSegment = 1;
					}
					break;
				}
				travelledDist += dis;
				if (returnPath) {
					final IShape gl = GamaShapeFactory.buildLine(currentLocation, pt);
					final IShape sh = path.getRealObject(line);
					if (sh != null) {
						final IAgent a = sh.getAgent();
						if (a != null) { agents.put(gl, a); }
					}
					segments.add(gl);
				}
				currentLocation = pt;
				remainingDistance = remainingDistance - dist;
				if (i == nb - 1 && j == endIndexSegment) { break; }
				indexSegment++;
			}
			if (remainingDistance == 0) { break; }
			indexSegment = 1;
			if (index < nb - 1) { index++; }
		}

		if (currentLocation.equals(falseTarget)) {
			currentLocation = GamaPointFactory.castToPoint(scope, path.getEndVertex(), returnPath);
			index++;
		}

		final double computedHeading = headingFrom == null ? 0 : SpatialRelations.towards(scope, headingFrom, headingTo);
		MovementResult result =
				new MovementResult(currentLocation, index, indexSegment, travelledDist, computedHeading);

		if (returnPath && segments != null && !segments.isEmpty()) {
			final IPath followedPath = GamaPathFactory.createFrom(scope, path.getTopology(scope), startLocation,
					currentLocation, segments, false);
			followedPath.setRealObjects(agents);
			result.pathFollowed = followedPath;
		}

		return result;
	}
}
