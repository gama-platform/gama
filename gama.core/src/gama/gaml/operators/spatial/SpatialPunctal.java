/*******************************************************************************************************
 *
 * SpatialPunctal.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.algorithm.distance.DistanceToPoint;
import org.locationtech.jts.algorithm.distance.PointPairDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.operation.distance.DistanceOp;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.annotations.support.Reason;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.GamaPointFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.gaml.operators.Maths;

/**
 * The Class Punctal.
 */
public class SpatialPunctal {

	/**
	 * Centroid area.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the gama point
	 */
	@operator (
			value = "centroid",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "Centroid (weighted sum of the centroids of a decomposition of the area into triangles) of the operand-geometry. Can be different to the location of the geometry",
			examples = { @example (
					value = "centroid(world)",
					equals = "the centroid of the square, for example : {50.0,50.0}.",
					test = false) },
			see = { "any_location_in", "closest_points_with", "farthest_point_to", "points_at" })
	@test (" centroid(world) = {50.0, 50.0, 0.0} ")
	public static IPoint centroidArea(final IScope scope, final IShape g) {
		if (g == null || g.getInnerGeometry() == null) return null;
		final Centroid cent = new Centroid(g.getInnerGeometry());
		return GamaPointFactory.create(cent.getCentroid());
	}

	/**
	 * Any location in.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the gama point
	 */
	@operator (
			value = { "any_location_in", "any_point_in" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "A point inside (or touching) the operand-geometry.",
			examples = { @example (
					value = "any_location_in(square(5))",
					equals = "a point in the square, for example : {3,4.6}.",
					test = false) },
			see = { "closest_points_with", "farthest_point_to", "points_at" })
	@no_test
	public static IPoint any_location_in(final IScope scope, final IShape g) {
		if (g == null) return null;
		return GeometryUtils.pointInGeom(scope, g.getInnerGeometry());
	}

	/**
	 * Points on.
	 *
	 * @param geom
	 *            the geom
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	@operator (
			value = { "points_on" },
			type = IType.LIST,
			content_type = IType.POINT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "A list of points of the operand-geometry distant from each other to the float right-operand .",
			examples = { @example (
					value = " square(5) points_on(2)",
					equals = "a list of points belonging to the exterior ring of the square distant from each other of 2.",
					test = false) },
			see = { "closest_points_with", "farthest_point_to", "points_at" })
	@test ("line({0,0},{0,10}) points_on 5 = [{0.0,0.0,0.0},{0.0,5.0,0.0},{0.0,10.0,0.0}]")
	public static IList points_on(final IShape geom, final Double distance) {
		final IList<IPoint> locs = GamaListFactory.create(Types.POINT);
		if (geom.getInnerGeometry() instanceof GeometryCollection) {
			for (int i = 0; i < geom.getInnerGeometry().getNumGeometries(); i++) {
				locs.addAll(GeometryUtils.locsOnGeometry(geom.getInnerGeometry().getGeometryN(i), distance));
			}
		} else {
			locs.addAll(GeometryUtils.locsOnGeometry(geom.getInnerGeometry(), distance));
		}
		return locs;
	}

	/**
	 * Points along.
	 *
	 * @param geom
	 *            the geom
	 * @param rates
	 *            the rates
	 * @return the i list
	 */
	@operator (
			value = { "points_along" },
			type = IType.LIST,
			content_type = IType.POINT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "A list of points along the operand-geometry given its location in terms of rate of distance from the starting points of the geometry.",
			examples = { @example (
					value = " line([{10,10},{80,80}]) points_along ([0.3, 0.5, 0.9])",
					equals = "the list of following points: [{31.0,31.0,0.0},{45.0,45.0,0.0},{73.0,73.0,0.0}]",
					test = false) },
			see = { "closest_points_with", "farthest_point_to", "points_at", "points_on" })
	@test ("line({0,0},{0,10}) points_along [0.50, 0.75] = [{0.0,5.0,0.0},{0.0,7.5,0.0}]")
	public static IList points_along(final IShape geom, final IList<Double> rates) {
		final IList<IPoint> locs = GamaListFactory.create(Types.POINT);
		if (geom.getInnerGeometry() instanceof GeometryCollection) {
			for (int i = 0; i < geom.getInnerGeometry().getNumGeometries(); i++) {
				locs.addAll(GeometryUtils.locsAlongGeometry(geom.getInnerGeometry().getGeometryN(i), rates));
			}
		} else {
			locs.addAll(GeometryUtils.locsAlongGeometry(geom.getInnerGeometry(), rates));
		}
		return locs;
	}

	/**
	 * Points at.
	 *
	 * @param scope
	 *            the scope
	 * @param nbLoc
	 *            the nb loc
	 * @param distance
	 *            the distance
	 * @return the i list
	 */
	@operator (
			value = { "points_at" },
			content_type = IType.POINT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "A list of left-operand number of points located at a the right-operand distance to the agent location.",
			examples = { @example (
					value = "3 points_at(20.0)",
					equals = "returns [pt1, pt2, pt3] with pt1, pt2 and pt3 located at a distance of 20.0 to the agent location",
					test = false) },
			see = { "any_location_in", "any_point_in", "closest_points_with", "farthest_point_to" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList<IPoint> points_at(final IScope scope, final Integer nbLoc, final Double distance) {
		if (distance == null || nbLoc == null) // scope.setStatus(ExecutionStatus.failure);
			throw GamaRuntimeException.error("Impossible to compute points_at", scope);
		final IList<IPoint> locations = GamaListFactory.create(Types.POINT);
		final IPoint loc = scope.getAgent().getLocation();
		final double angle1 = scope.getRandom().between(0, 2 * Math.PI);

		for (int i = 0; i < nbLoc; i++) {
			final IPoint p =
					GamaPointFactory.create(loc.getX() + distance * Math.cos(angle1 + (double) i / nbLoc * 2 * Math.PI),
							loc.getY() + distance * Math.sin(angle1 + (double) i / nbLoc * 2 * Math.PI));
			locations.add(p);
		}
		return locations;

	}

	/**
	 * Closest points with.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the i list
	 */
	@operator (
			value = "closest_points_with",
			type = IType.LIST,
			content_type = IType.POINT,
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "A list of two closest points between the two geometries.",
			examples = { @example (
					value = "geom1 closest_points_with(geom2)",
					equals = "[pt1, pt2] with pt1 the closest point of geom1 to geom2 and pt1 the closest point of geom2 to geom1",
					isExecutable = false) },
			see = { "any_location_in", "any_point_in", "farthest_point_to", "points_at" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList<IPoint> closest_points_with(final IShape a, final IShape b) {
		final Coordinate[] coors = DistanceOp.nearestPoints(a.getInnerGeometry(), b.getInnerGeometry());
		return GamaListFactory.wrap(Types.POINT, GamaPointFactory.create(coors[0]), GamaPointFactory.create(coors[1]));
	}

	/**
	 * Farthest point to.
	 *
	 * @param g
	 *            the g
	 * @param p
	 *            the p
	 * @return the gama point
	 */
	@operator (
			value = "farthest_point_to",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "the farthest point of the left-operand to the left-point.",
			examples = { @example (
					value = "geom farthest_point_to(pt)",
					equals = "the farthest point of geom to pt",
					isExecutable = false) },
			see = { "any_location_in", "any_point_in", "closest_points_with", "points_at" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IPoint farthest_point_to(final IShape g, final IPoint p) {
		if (g == null) return p.getLocation();
		if (p == null) return g.getLocation();

		final Coordinate[] cg = g.getInnerGeometry().getCoordinates();
		if (cg.length == 0) return p;
		Coordinate pt_max = cg[0];
		double dist_max = p.distance(pt_max);
		for (int i = 1; i < cg.length; i++) {
			final double dist = p.distance(cg[i]);
			if (dist > dist_max) {
				pt_max = cg[i];
				dist_max = dist;
			}
		}
		return GamaPointFactory.create(pt_max);
	}

	/**
	 * @throws GamaRuntimeException
	 *             determine the closest point of a geometry to another given point.
	 *
	 * @param pt
	 *            a point
	 * @param poly
	 *            a polygon
	 */
	public static IPoint _closest_point_to(final IShape pt, final IShape geom) {
		if (pt == null) return null;
		if (geom == null) return pt.getLocation();
		final Coordinate[] cp = new DistanceOp(geom.getInnerGeometry(), pt.getInnerGeometry()).nearestPoints();
		return GamaPointFactory.create(cp[0]);
	}

	/**
	 * Closest point to.
	 *
	 * @param pt
	 *            the pt
	 * @param geom
	 *            the geom
	 * @return the gama point
	 */
	public static IPoint _closest_point_to(final IPoint pt, final IShape geom) {
		if (pt == null) return null;
		if (geom == null) return pt;
		final PointPairDistance ppd = new PointPairDistance();
		DistanceToPoint.computeDistance(geom.getInnerGeometry(), pt.toCoordinate(), ppd);
		return GamaPointFactory.create(ppd.getCoordinate(0));
	}

	/**
	 * Angle in degrees between.
	 *
	 * @param scope
	 *            the scope
	 * @param p0
	 *            the p 0
	 * @param p1
	 *            the p 1
	 * @param p2
	 *            the p 2
	 * @return the double
	 */
	@operator (
			value = "angle_between",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION, IConcept.POINT })
	@doc (
			value = "the angle between vectors P0P1 and P0P2 (P0, P1, P2 being the three point operands)",
			examples = { @example (
					value = "angle_between({5,5},{10,5},{5,10})",
					equals = "90") })
	public static Double angleInDegreesBetween(final IScope scope, final IPoint p0, final IPoint p1, final IPoint p2) {
		final double Xa = p1.getX() - p0.getX();
		final double Ya = p1.getY() - p0.getY();
		final double Xb = p2.getX() - p0.getX();
		final double Yb = p2.getY() - p0.getY();
		final double Na = Maths.sqrt(scope, Xa * Xa + Ya * Ya);
		final double Nb = Maths.sqrt(scope, Xb * Xb + Yb * Yb);
		final double C = Maths.round((Xa * Xb + Ya * Yb) / (Na * Nb), 10);
		final double S = Xa * Yb - Ya * Xb;
		final double result = S > 0 ? Maths.acos(C) : -1 * Maths.acos(C);
		return Maths.checkHeading(result);
	}

}
