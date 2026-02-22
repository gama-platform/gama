/*******************************************************************************************************
 *
 * AmorphousTopology.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.topology;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.topology.ISpatialIndex;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.GamaPathFactory;
import gama.api.types.graph.IPath;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.interfaces.IAgentFilter;

/**
 * The Class AmorphousTopology.
 * 
 * Represents an expandable, boundless topology that grows dynamically to accommodate agents at any location. Unlike
 * bounded topologies with fixed environments, an amorphous topology has no predefined limits and expands its
 * environment to include any agent added to it.
 * 
 * <p>
 * Key characteristics:
 * <ul>
 * <li>No fixed boundaries - the environment expands to contain all agents</li>
 * <li>No spatial index - spatial queries return empty results</li>
 * <li>Direct Euclidean distance calculations</li>
 * <li>Simple straight-line paths between locations</li>
 * <li>All locations are considered valid</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This topology is useful for:
 * <ul>
 * <li>Simulations where the environment size is unknown or unbounded</li>
 * <li>Abstract models that don't require spatial structure</li>
 * <li>Situations where agents can exist anywhere in infinite space</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Note: Most spatial query methods (like finding neighbors or nearby agents) return empty results, as this topology
 * does not maintain agent positions in a spatial index.
 * </p>
 *
 * @author drogoul
 * @since 2 décembre 2011
 */
public class AmorphousTopology implements ITopology {

	/** The expandable environment shape that grows to contain all agents. */
	IShape expandableEnvironment = GamaShapeFactory.buildPoint(GamaPointFactory.create());

	/**
	 * @see gama.api.types.misc.interfaces.IValue#stringValue()
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return "Expandable topology";
	}

	@Override
	public IType<?> getGamlType() { return Types.TOPOLOGY; }

	/**
	 * @see gama.api.types.misc.interfaces.IValue#toGaml()
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "topology({0,0})";
	}

	@Override
	public ITopology copy(final IScope scope) throws GamaRuntimeException {
		return new AmorphousTopology();
	}

	@Override
	public void initialize(final IScope scope, final IPopulation<? extends IAgent> pop) throws GamaRuntimeException {}

	@Override
	public void updateAgent(final IEnvelope previous, final IAgent agent) {
		Geometry geom = GeometryUtils.robustUnion(expandableEnvironment.getGeometry().getInnerGeometry(),
				agent.getGeometry().getInnerGeometry());
		expandableEnvironment.setGeometry(GamaShapeFactory.createFrom(geom.getEnvelope()));
	}

	@Override
	public void removeAgent(final IAgent agent) {}

	@Override
	public IList<IAgent> getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter,
			final int number) {
		return GamaListFactory.getEmptyList();
	}

	@Override
	public IAgent getAgentClosestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		return null;
	}

	@Override
	public IAgent getAgentFarthestTo(final IScope scope, final IShape source, final IAgentFilter filter) {
		return null;
	}

	@Override
	public Set<IAgent> getNeighborsOf(final IScope scope, final IShape source, final Double distance,
			final IAgentFilter filter) throws GamaRuntimeException {
		return Collections.EMPTY_SET;
	}

	@Override
	public Set<IAgent> getAgentsIn(final IScope scope, final IShape source, final IAgentFilter f,
			final SpatialRelation relation) {
		return Collections.EMPTY_SET;
	}

	/**
	 * Computes the Euclidean distance between two shapes.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the source shape
	 * @param target
	 *            the target shape
	 * @return the Euclidean distance between the shapes
	 */
	@Override
	public Double distanceBetween(final IScope scope, final IShape source, final IShape target) {
		return source.euclidianDistanceTo(target);
	}

	/**
	 * Computes the Euclidean distance between two points.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the source point
	 * @param target
	 *            the target point
	 * @return the Euclidean distance between the points
	 */
	@Override
	public Double distanceBetween(final IScope scope, final IPoint source, final IPoint target) {
		return source.euclidianDistanceTo(target);
	}

	/**
	 * Creates a straight-line path between two shapes.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting shape
	 * @param target
	 *            the destination shape
	 * @return a path containing the source and target shapes
	 * @throws GamaRuntimeException
	 *             if path creation fails
	 */
	@Override
	public IPath pathBetween(final IScope scope, final IShape source, final IShape target) throws GamaRuntimeException {
		// return new GamaPath(this, GamaList.with(source, target));
		return GamaPathFactory.createFrom(scope, this, GamaListFactory.wrap(Types.GEOMETRY, source, target), 0.0);
	}

	/**
	 * Creates a straight-line path between two points.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point
	 * @param target
	 *            the destination point
	 * @return a path containing the source and target points
	 * @throws GamaRuntimeException
	 *             if path creation fails
	 */
	@Override
	public IPath pathBetween(final IScope scope, final IPoint source, final IPoint target) throws GamaRuntimeException {
		return GamaPathFactory.createFrom(scope, this, GamaListFactory.create(scope, Types.POINT, source, target), 0.0);
	}

	/**
	 * Computes a destination point from a source, direction, and distance in 2D.
	 * 
	 * Since this is an unbounded topology, the destination is always valid and nullIfOutside is ignored.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point
	 * @param direction
	 *            the heading angle in radians (not degrees, despite the interface description)
	 * @param distance
	 *            the distance to travel
	 * @param nullIfOutside
	 *            ignored (always returns a valid point)
	 * @return the destination point
	 */
	@Override
	public IPoint getDestination(final IScope scope, final IPoint source, final double direction, final double distance,
			final boolean nullIfOutside) {
		double d = Math.toDegrees(direction);
		final double cos = distance * Math.cos(d);
		final double sin = distance * Math.sin(d);
		return GamaPointFactory.create(source.getX() + cos, source.getY() + sin);

	}

	/**
	 * Computes a destination point from a source, heading, pitch, and distance in 3D.
	 * 
	 * Since this is an unbounded topology, the destination is always valid and nullIfOutside is ignored.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point
	 * @param heading
	 *            the horizontal angle in radians (not degrees)
	 * @param pitch
	 *            the vertical angle in radians (not degrees)
	 * @param distance
	 *            the distance to travel in 3D space
	 * @param nullIfOutside
	 *            ignored (always returns a valid point)
	 * @return the 3D destination point
	 */
	@Override
	public IPoint getDestination3D(final IScope scope, final IPoint source, final double heading, final double pitch,
			final double distance, final boolean nullIfOutside) {
		double p = Math.toDegrees(pitch);
		double h = Math.toDegrees(heading);
		final double x = distance * Math.cos(p) * Math.cos(h);
		final double y = distance * Math.cos(p) * Math.sin(h);
		final double z = distance * Math.sin(p);
		return GamaPointFactory.create(source.getX() + x, source.getY() + y, source.getZ() + z);
	}

	/**
	 * Returns a random location within [0,1] x [0,1] bounds.
	 *
	 * @param scope
	 *            the execution scope (provides the random generator)
	 * @return a random point with coordinates between 0 and 1
	 */
	@Override
	public IPoint getRandomLocation(final IScope scope) {
		return GamaPointFactory.create(scope.getRandom().next(), scope.getRandom().next());
	}

	/**
	 * Returns the discrete places that compose this topology.
	 * 
	 * For amorphous topologies, this returns a list containing only the expandable environment.
	 *
	 * @return a list containing the environment shape
	 */
	@Override
	public IContainer<?, IShape> getPlaces() {
		final IList<IShape> result = GamaListFactory.create(Types.GEOMETRY);
		result.add(expandableEnvironment);
		return result;
	}

	/**
	 * Returns the environment shape of this topology.
	 *
	 * @return the expandable environment shape
	 */
	@Override
	public IShape getEnvironment() { return expandableEnvironment; }

	/**
	 * Normalizes a location within this topology.
	 * 
	 * Since this is an unbounded topology, all points are valid and returned as-is.
	 *
	 * @param scope
	 *            the execution scope
	 * @param p
	 *            the point to normalize
	 * @param nullIfOutside
	 *            ignored (all points are considered inside)
	 * @return the same point
	 */
	@Override
	public IPoint normalizeLocation(final IScope scope, final IPoint p, final boolean nullIfOutside) {
		return p;
	}

	// @Override
	// public void shapeChanged(final IPopulation pop) {}

	/**
	 * Returns the width of the environment.
	 *
	 * @return the width of the environment's bounding box
	 */
	@Override
	public double getWidth() { return expandableEnvironment.getEnvelope().getWidth(); }

	/**
	 * Returns the height of the environment.
	 *
	 * @return the height of the environment's bounding box
	 */
	@Override
	public double getHeight() { return expandableEnvironment.getEnvelope().getHeight(); }

	/**
	 * Disposes of this topology and releases resources.
	 * 
	 * For amorphous topologies, this method does nothing as there are no resources to release.
	 */
	@Override
	public void dispose() {}

	/**
	 * Tests whether a point is valid in this topology.
	 * 
	 * Since this is an unbounded topology, all points are valid.
	 *
	 * @param scope
	 *            the execution scope
	 * @param p
	 *            the point to test
	 * @return always true
	 */
	@Override
	public boolean isValidLocation(final IScope scope, final IPoint p) {
		return true;
	}

	/**
	 * Tests whether a geometry is valid in this topology.
	 * 
	 * Since this is an unbounded topology, all geometries are valid.
	 *
	 * @param scope
	 *            the execution scope
	 * @param g
	 *            the geometry to test
	 * @return always true
	 */
	@Override
	public boolean isValidGeometry(final IScope scope, final IShape g) {
		return true;
	}

	/**
	 * Computes the direction in degrees from one shape to another.
	 * 
	 * The angle is measured using arctangent of the coordinate differences.
	 *
	 * @param scope
	 *            the execution scope
	 * @param g1
	 *            the source shape
	 * @param g2
	 *            the target shape
	 * @return the direction in degrees
	 */
	@Override
	public Double directionInDegreesTo(final IScope scope, final IShape g1, final IShape g2) {
		final IPoint source = g1.getLocation();
		final IPoint target = g2.getLocation();
		final double x2 = /* translateX(source.x, target.x); */target.getX();
		final double y2 = /* translateY(source.y, target.y); */target.getY();
		final double dx = x2 - source.getX();
		final double dy = y2 - source.getY();
		final double result = Math.atan2(dy, dx);
		return Math.toDegrees(result);
	}

	/**
	 * Returns the list of toroidal geometry copies.
	 * 
	 * Since this is not a toroidal topology, this always returns an empty list.
	 *
	 * @param geom
	 *            the geometry
	 * @return an empty list
	 */
	@Override
	public List<Geometry> listToroidalGeometries(final Geometry geom) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Tests whether this is a toroidal topology.
	 *
	 * @return always false
	 */
	@Override
	public boolean isTorus() { return false; }

	/**
	 * Tests whether this is a continuous topology.
	 *
	 * @return always true
	 */
	@Override
	public boolean isContinuous() { return true; }

	/**
	 * Returns the spatial index for this topology.
	 * 
	 * Amorphous topologies don't use a spatial index.
	 *
	 * @return the null spatial index
	 */
	@Override
	public ISpatialIndex getSpatialIndex() { return ISpatialIndex.NULL_INDEX; }

	/**
	 * Finds K shortest paths between two shapes.
	 * 
	 * For amorphous topologies, only one straight-line path exists, so this returns a list with a single path.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting shape
	 * @param target
	 *            the destination shape
	 * @param k
	 *            the number of paths requested (ignored, always returns 1 path)
	 * @return a list containing a single straight-line path
	 */
	@Override
	public IList<IPath> kPathsBetween(final IScope scope, final IShape source, final IShape target, final int k) {
		final IList<IPath> paths = GamaListFactory.create(Types.PATH);
		paths.add(pathBetween(scope, source, target));
		return paths;
	}

	/**
	 * Finds K shortest paths between two points.
	 * 
	 * For amorphous topologies, only one straight-line path exists, so this returns a list with a single path.
	 *
	 * @param scope
	 *            the execution scope
	 * @param source
	 *            the starting point
	 * @param target
	 *            the destination point
	 * @param k
	 *            the number of paths requested (ignored, always returns 1 path)
	 * @return a list containing a single straight-line path
	 */
	@Override
	public IList<IPath> kPathsBetween(final IScope scope, final IPoint source, final IPoint target, final int k) {
		final IList<IPath> paths = GamaListFactory.create(Types.PATH);
		paths.add(pathBetween(scope, source, target));
		return paths;
	}

	/**
	 * Sets the root topology.
	 * 
	 * For amorphous topologies, this method does nothing as they don't use hierarchical topologies.
	 *
	 * @param scope
	 *            the execution scope
	 * @param rt
	 *            the root topology (ignored)
	 */
	@Override
	public void setRoot(final IScope scope, final ITopology rt) {}
}
