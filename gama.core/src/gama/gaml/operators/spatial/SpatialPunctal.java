package gama.gaml.operators.spatial;

import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.algorithm.distance.DistanceToPoint;
import org.locationtech.jts.algorithm.distance.PointPairDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.operation.distance.DistanceOp;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.Reason;
import gama.core.common.geometry.GeometryUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.operators.Maths;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.POINT })
	@doc (
			value = "Centroid (weighted sum of the centroids of a decomposition of the area into triangles) of the operand-geometry. Can be different to the location of the geometry",
			examples = { @example (
					value = "centroid(world)",
					equals = "the centroid of the square, for example : {50.0,50.0}.",
					test = false) },
			see = { "any_location_in", "closest_points_with", "farthest_point_to", "points_at" })
	@test (" centroid(world) = {50.0, 50.0, 0.0} ")
	public static GamaPoint centroidArea(final IScope scope, final IShape g) {
		if (g == null || g.getInnerGeometry() == null) return null;
		final Centroid cent = new Centroid(g.getInnerGeometry());
		return new GamaPoint(cent.getCentroid());
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
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.POINT })
	@doc (
			value = "A point inside (or touching) the operand-geometry.",
			examples = { @example (
					value = "any_location_in(square(5))",
					equals = "a point in the square, for example : {3,4.6}.",
					test = false) },
			see = { "closest_points_with", "farthest_point_to", "points_at" })
	@no_test
	public static GamaPoint any_location_in(final IScope scope, final IShape g) {
		return GeometryUtils.pointInGeom(scope, g);
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
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.POINT })
	@doc (
			value = "A list of points of the operand-geometry distant from each other to the float right-operand .",
			examples = { @example (
					value = " square(5) points_on(2)",
					equals = "a list of points belonging to the exterior ring of the square distant from each other of 2.",
					test = false) },
			see = { "closest_points_with", "farthest_point_to", "points_at" })
	@test ("line({0,0},{0,10}) points_on 5 = [{0.0,0.0,0.0},{0.0,5.0,0.0},{0.0,10.0,0.0}]")
	public static IList points_on(final IShape geom, final Double distance) {
		final IList<GamaPoint> locs = GamaListFactory.create(Types.POINT);
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
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.POINT })
	@doc (
			value = "A list of points along the operand-geometry given its location in terms of rate of distance from the starting points of the geometry.",
			examples = { @example (
					value = " line([{10,10},{80,80}]) points_along ([0.3, 0.5, 0.9])",
					equals = "the list of following points: [{31.0,31.0,0.0},{45.0,45.0,0.0},{73.0,73.0,0.0}]",
					test = false) },
			see = { "closest_points_with", "farthest_point_to", "points_at", "points_on" })
	@test ("line({0,0},{0,10}) points_along [0.50, 0.75] = [{0.0,5.0,0.0},{0.0,7.5,0.0}]")
	public static IList points_along(final IShape geom, final IList<Double> rates) {
		final IList<GamaPoint> locs = GamaListFactory.create(Types.POINT);
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
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.POINT })
	@doc (
			value = "A list of left-operand number of points located at a the right-operand distance to the agent location.",
			examples = { @example (
					value = "3 points_at(20.0)",
					equals = "returns [pt1, pt2, pt3] with pt1, pt2 and pt3 located at a distance of 20.0 to the agent location",
					test = false) },
			see = { "any_location_in", "any_point_in", "closest_points_with", "farthest_point_to" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList<GamaPoint> points_at(final IScope scope, final Integer nbLoc, final Double distance) {
		if (distance == null || nbLoc == null) // scope.setStatus(ExecutionStatus.failure);
			throw GamaRuntimeException.error("Impossible to compute points_at", scope);
		final IList<GamaPoint> locations = GamaListFactory.create(Types.POINT);
		final GamaPoint loc = scope.getAgent().getLocation();
		final double angle1 = scope.getRandom().between(0, 2 * Math.PI);

		for (int i = 0; i < nbLoc; i++) {
			final GamaPoint p =
					new GamaPoint(loc.getX() + distance * Math.cos(angle1 + (double) i / nbLoc * 2 * Math.PI),
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
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.POINT })
	@doc (
			value = "A list of two closest points between the two geometries.",
			examples = { @example (
					value = "geom1 closest_points_with(geom2)",
					equals = "[pt1, pt2] with pt1 the closest point of geom1 to geom2 and pt1 the closest point of geom2 to geom1",
					isExecutable = false) },
			see = { "any_location_in", "any_point_in", "farthest_point_to", "points_at" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static IList<GamaPoint> closest_points_with(final IShape a, final IShape b) {
		final Coordinate[] coors = DistanceOp.nearestPoints(a.getInnerGeometry(), b.getInnerGeometry());
		return GamaListFactory.wrap(Types.POINT, new GamaPoint(coors[0]), new GamaPoint(coors[1]));
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
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_RELATION,
					IConcept.POINT })
	@doc (
			value = "the farthest point of the left-operand to the left-point.",
			examples = { @example (
					value = "geom farthest_point_to(pt)",
					equals = "the farthest point of geom to pt",
					isExecutable = false) },
			see = { "any_location_in", "any_point_in", "closest_points_with", "points_at" })
	@no_test (Reason.IMPOSSIBLE_TO_TEST)
	public static GamaPoint farthest_point_to(final IShape g, final GamaPoint p) {
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
		return new GamaPoint(pt_max);
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
	public static GamaPoint _closest_point_to(final IShape pt, final IShape geom) {
		if (pt == null) return null;
		if (geom == null) return pt.getLocation();
		final Coordinate[] cp = new DistanceOp(geom.getInnerGeometry(), pt.getInnerGeometry()).nearestPoints();
		return new GamaPoint(cp[0]);
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
	public static GamaPoint _closest_point_to(final GamaPoint pt, final IShape geom) {
		if (pt == null) return null;
		if (geom == null) return pt;
		final PointPairDistance ppd = new PointPairDistance();
		DistanceToPoint.computeDistance(geom.getInnerGeometry(), pt, ppd);
		return new GamaPoint(ppd.getCoordinate(0));
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
	public static Double angleInDegreesBetween(final IScope scope, final GamaPoint p0, final GamaPoint p1,
			final GamaPoint p2) {
		final double Xa = p1.x - p0.x;
		final double Ya = p1.y - p0.y;
		final double Xb = p2.x - p0.x;
		final double Yb = p2.y - p0.y;
		final double Na = Maths.sqrt(scope, Xa * Xa + Ya * Ya);
		final double Nb = Maths.sqrt(scope, Xb * Xb + Yb * Yb);
		final double C = Maths.round((Xa * Xb + Ya * Yb) / (Na * Nb), 10);
		final double S = Xa * Yb - Ya * Xb;
		final double result = S > 0 ? Maths.acos(C) : -1 * Maths.acos(C);
		return Maths.checkHeading(result);
	}

}
