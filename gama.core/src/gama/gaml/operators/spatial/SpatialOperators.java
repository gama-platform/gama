/*******************************************************************************************************
 *
 * SpatialOperators.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.precision.EnhancedPrecisionOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.locationtech.jts.util.AssertionFailedException;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.utils.geometry.GeometryUtils;

/**
 * The Class Operators.
 */
public class SpatialOperators {

	/**
	 * Inter.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return the i shape
	 */
	@operator (
			value = { "inter", "intersection" },
			category = { IOperatorCategory.SPATIAL })
	@doc (
			value = "A geometry resulting from the intersection between the two geometries",
			special_cases = { "returns nil if one of the operands is nil" },
			examples = { @example (
					value = "square(10) inter circle(5)",
					equals = "circle(5)") },
			see = { "union", "+", "-" })
	public static IShape inter(final IScope scope, final IShape g1, final IShape g2) {
		if (g2 == null || g1 == null) return null;
		if (g2.isPoint() && g1.covers(g2.getLocation())) return g2.copy(scope);
		if (g1.isPoint() && g2.covers(g1.getLocation())) return g1.copy(scope);
		Geometry geom = GeometryUtils.robustIntersection(g1.getInnerGeometry(), g2.getInnerGeometry());
		if (geom == null || geom.isEmpty()) return null;
		// WARNING The attributes of the left-hand shape are kept, but not
		// those of the right-hand shape
		final IShape result = GamaShapeFactory.createFrom(geom).withAttributesOf(g1);
		result.losePredefinedProperty();
		return result;
	}

	/**
	 * Union.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return the i shape
	 */
	@operator (
			value = { IKeyword.PLUS, "union" },
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			usages = @usage (
					value = "if the right-operand is a point, a geometry or an agent, returns the geometry resulting from the union between both geometries",
					examples = @example (
							value = "geom1 + geom2",
							equals = "a geometry corresponding to union between geom1 and geom2",
							isExecutable = false)))
	@no_test // test already done in Spatial tests Models
	public static IShape union(final IScope scope, final IShape g1, final IShape g2) {
		if (g1 == null) {
			if (g2 == null) return null;
			return g2.copy(scope);
		}
		if (g2 == null) return g1.copy(scope);
		final Geometry geom1 = g1.getInnerGeometry();
		final Geometry geom2 = g2.getInnerGeometry();
		Geometry geom = GeometryUtils.robustUnion(geom1, geom2);
		if (geom == null || geom.isEmpty()) return null;
		final IShape result = GamaShapeFactory.createFrom(geom).withAttributesOf(g1);
		result.losePredefinedProperty();
		return result;
	}

	/**
	 * Union.
	 *
	 * @param scope
	 *            the scope
	 * @param elements
	 *            the elements
	 * @return the i shape
	 */
	@operator (
			value = { "union" },
			expected_content_type = { IType.POINT, IType.GEOMETRY, IType.AGENT },
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			usages = { @usage (
					value = "if the right-operand is a container of points, geometries or agents, returns the geometry resulting from the union all the geometries") },
			examples = { @example (
					value = "union([geom1, geom2, geom3])",
					equals = "a geometry corresponding to union between geom1, geom2 and geom3",
					isExecutable = false) })
	@no_test // test already done in Spatial tests Models
	public static IShape union(final IScope scope, final IContainer<?, IShape> elements) {
		try {
			return GamaShapeFactory.castToShape(scope, elements, false);
		} catch (final GamaRuntimeException e) {
			return null;
		}
	}

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return the i shape
	 */
	@operator (
			value = IKeyword.MINUS,
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			usages = @usage (
					value = "if both operands are a point, a geometry or an agent, returns the geometry resulting from the difference between both geometries",
					examples = @example (
							value = "geom1 - geom2",
							equals = "a geometry corresponding to difference between geom1 and geom2",
							isExecutable = false)))
	@no_test // test already done in Spatial tests Models
	public static IShape minus(final IScope scope, final IShape g1, final IShape g2) {
		if (g1 == null || g2 == null || g1.getInnerGeometry() == null || g2.getInnerGeometry() == null) return g1;
		final Geometry res = difference(g1.getInnerGeometry(), g2.getInnerGeometry());
		if (res != null && !res.isEmpty()) {
			final IShape result = GamaShapeFactory.createFrom(res).withAttributesOf(g1);
			result.losePredefinedProperty();
			return result;
		}
		return null;
	}

	/**
	 * Minus.
	 *
	 * @param scope
	 *            the scope
	 * @param g1
	 *            the g 1
	 * @param agents
	 *            the agents
	 * @return the i shape
	 */
	@operator (
			value = IKeyword.MINUS,
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			usages = @usage (
					value = "if the right-operand is a list of points, geometries or agents, returns the geometry resulting from the difference between the left-geometry and all of the right-geometries",
					examples = @example (
							value = "rectangle(10,10) - [circle(2), square(2)]",
							equals = "rectangle(10,10) - (circle(2) + square(2))")))
	public static IShape minus(final IScope scope, final IShape g1, final IContainer<?, IShape> agents) {
		if (g1 == null || agents == null || g1.getInnerGeometry() == null || agents.isEmpty(scope)) return g1;
		Geometry geom1 = GeometryUtils.getGeometryFactory().createGeometry(g1.getInnerGeometry());
		for (final IShape ag : agents.iterable(scope)) {
			if (ag != null && ag.getInnerGeometry() != null) {
				geom1 = difference(geom1, ag.getInnerGeometry());
				if (geom1 == null || geom1.isEmpty()) return null;
			}
		}
		if (geom1 == null || geom1.isEmpty()) return null;
		final IShape result = GamaShapeFactory.createFrom(geom1).withAttributesOf(g1);
		result.losePredefinedProperty();
		return result;
	}

	/**
	 * Difference.
	 *
	 * @param first
	 *            the first
	 * @param g2
	 *            the g 2
	 * @return the geometry
	 */
	private static Geometry difference(final Geometry first, final Geometry g2) {
		Geometry g1 = first;
		if (g2 instanceof GeometryCollection g2c) {
			final int nb = g2c.getNumGeometries();
			for (int i = 0; i < nb; i++) {
				g1 = difference(g1, g2c.getGeometryN(i));
				if (g1 == null || g1.isEmpty()) return null;
			}
			return g1;
		}
		try {
			return g1.difference(g2);
		} catch (AssertionFailedException | TopologyException e) {
			try {
				final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				return GeometryPrecisionReducer.reducePointwise(g1, pm)
						.difference(GeometryPrecisionReducer.reducePointwise(g2, pm));
			} catch (final RuntimeException e1) {
				try {
					return g1.buffer(0, 10, BufferParameters.CAP_FLAT)
							.difference(g2.buffer(0, 10, BufferParameters.CAP_FLAT));
				} catch (final TopologyException e2) {
					try {
						final PrecisionModel pm = new PrecisionModel(100000d);
						return GeometryPrecisionReducer.reduce(g1, pm)
								.difference(GeometryPrecisionReducer.reduce(g2, pm));
					} catch (final RuntimeException e3) {
						try {
							return EnhancedPrecisionOp.difference(g1, g2);
						} catch (final RuntimeException e4) {
							try {
								return g1.difference(
										g2.buffer(Math.min(0.01, g2.getArea() / 1000), 10, BufferParameters.CAP_FLAT));
							} catch (final RuntimeException last) {
								return null; // return g1; ??
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Adds the point.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param p
	 *            the p
	 * @return the i shape
	 */
	@operator (
			value = { "add_point" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.POINT },
			concept = { IConcept.POINT, IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION })
	@doc (
			value = "A new geometry resulting from the addition of the right point (coordinate) to the left-hand geometry. Note that adding a point to a line or polyline will always return a closed contour. Also note that the position at which the added point will appear in the geometry is not necessarily the last one, as points are always ordered in a clockwise fashion in geometries",
			examples = { @example (
					value = "polygon([{10,10},{10,20},{20,20}]) add_point {20,10}",
					returnType = "geometry",
					equals = "polygon([{10,10},{10,20},{20,20},{20,10}])") })
	public static IShape add_point(final IScope scope, final IShape g, final IPoint p) {
		if (p == null || g == null) return g;
		final Coordinate point = p.toCoordinate();
		final Geometry geometry = g.getInnerGeometry();
		final org.locationtech.jts.geom.GeometryFactory gf = GeometryUtils.getGeometryFactory();
		Geometry geom_Tmp = null;
		if (geometry instanceof Point) {
			final Coordinate[] coord = { geometry.getCoordinate(), point };
			geom_Tmp = gf.createLineString(coord);
		} else if (geometry instanceof MultiPoint) {
			final Coordinate[] existingCoords = geometry.getCoordinates();
			final Coordinate[] coordinates = new Coordinate[existingCoords.length + 1];
			System.arraycopy(existingCoords, 0, coordinates, 0, existingCoords.length);
			coordinates[existingCoords.length] = point;
			geom_Tmp = gf.createMultiPointFromCoords(coordinates);
		} else if (geometry instanceof LineString) {
			geom_Tmp = createLineStringWithPoint(geometry, point);
		} else if (geometry instanceof MultiLineString) {
			// find closest sub-geometry using a single shared Point object
			final Point pt = gf.createPoint(point);
			int id = -1;
			double distMin = Double.MAX_VALUE;
			final int numGeoms = geometry.getNumGeometries();
			for (int i = 0; i < numGeoms; i++) {
				final double dist = geometry.getGeometryN(i).distance(pt);
				if (dist < distMin) {
					distMin = dist;
					id = i;
				}
			}
			final LineString[] lineStrings = new LineString[numGeoms];
			for (int i = 0; i < numGeoms; i++) {
				final Geometry sub = geometry.getGeometryN(i);
				lineStrings[i] = i != id ? (LineString) sub : (LineString) createLineStringWithPoint(sub, point);
			}
			geom_Tmp = gf.createMultiLineString(lineStrings);
		} else if (geometry instanceof Polygon) {
			geom_Tmp = createPolygonWithPoint(geometry, point);
		} else if (geometry instanceof MultiPolygon) {
			// find closest sub-geometry using a single shared Point object
			final Point pt = gf.createPoint(point);
			int id = -1;
			double distMin = Double.MAX_VALUE;
			final int numGeoms = geometry.getNumGeometries();
			for (int i = 0; i < numGeoms; i++) {
				final double dist = geometry.getGeometryN(i).distance(pt);
				if (dist < distMin) {
					distMin = dist;
					id = i;
				}
			}
			final Polygon[] polygons = new Polygon[numGeoms];
			for (int i = 0; i < numGeoms; i++) {
				final Geometry sub = geometry.getGeometryN(i);
				polygons[i] = i != id ? (Polygon) sub : (Polygon) createPolygonWithPoint(sub, point);
			}
			geom_Tmp = gf.createMultiPolygon(polygons);
		}
		if (geom_Tmp != null) {
			final IShape result = GamaShapeFactory.createFrom(geom_Tmp).withAttributesOf(g);
			result.losePredefinedProperty();
			return result;
		}
		return g;
	}

	/**
	 * Creates the polygon with point.
	 *
	 * @param geometry
	 *            the geometry
	 * @param point
	 *            the point
	 * @return the geometry
	 */
	private static Geometry createPolygonWithPoint(final Geometry geometry, final Coordinate point) {
		double simpleMinLength = Double.MAX_VALUE;
		Geometry simpleMinGeom = null;
		double complexMinLength = Double.MAX_VALUE;
		Geometry complexMinGeom = null;
		final org.locationtech.jts.geom.GeometryFactory gf = GeometryUtils.getGeometryFactory();
		final Polygon poly = (Polygon) geometry;
		final Coordinate[] geomCoords = geometry.getCoordinates();
		final int nbPts = poly.getExteriorRing().getCoordinates().length;
		final int numRings = poly.getNumInteriorRing();
		final LinearRing[] lrs = new LinearRing[numRings];
		for (int i = 0; i < numRings; i++) { lrs[i] = poly.getInteriorRingN(i); }
		for (int index = 0; index <= nbPts; index++) {
			final Coordinate[] coord = new Coordinate[nbPts + 1];
			for (int i = 0; i < index; i++) { coord[i] = geomCoords[i]; }
			coord[index] = point;
			for (int i = index + 1; i < coord.length; i++) { coord[i] = geomCoords[i - 1]; }
			final Geometry g = gf.createPolygon(gf.createLinearRing(coord), lrs);
			if (g.isValid()) {
				if (simpleMinLength > g.getArea()) {
					simpleMinLength = g.getArea();
					simpleMinGeom = g;
				}
			} else if (complexMinLength > g.getArea()) {
				complexMinLength = g.getArea();
				complexMinGeom = g;
			}
		}
		if (simpleMinGeom != null) return simpleMinGeom;
		return complexMinGeom;
	}

	/**
	 * Creates the line string with point.
	 *
	 * @param geometry
	 *            the geometry
	 * @param point
	 *            the point
	 * @return the geometry
	 */
	private static Geometry createLineStringWithPoint(final Geometry geometry, final Coordinate point) {
		double simpleMinLength = Double.MAX_VALUE;
		Geometry simpleMinGeom = null;
		double complexMinLength = Double.MAX_VALUE;
		Geometry complexMinGeom = null;
		final org.locationtech.jts.geom.GeometryFactory gf = GeometryUtils.getGeometryFactory();
		final Coordinate[] geomCoords = geometry.getCoordinates();
		for (int index = 0; index <= geomCoords.length; index++) {
			final Coordinate[] coord = new Coordinate[geomCoords.length + 1];
			for (int i = 0; i < index; i++) { coord[i] = geomCoords[i]; }
			coord[index] = point;
			for (int i = index + 1; i < coord.length; i++) { coord[i] = geomCoords[i - 1]; }
			final Geometry g = gf.createLineString(coord);
			if (g.isValid()) {
				if (simpleMinLength > g.getLength()) {
					simpleMinLength = g.getLength();
					simpleMinGeom = g;
				}
			} else if (complexMinLength > g.getLength()) {
				complexMinLength = g.getLength();
				complexMinGeom = g;
			}
		}
		if (simpleMinGeom != null) return simpleMinGeom;
		return complexMinGeom;
	}

	/*
	 * private static int indexClosestSegment(final Geometry geom, final Coordinate coord) { int index = -1; final Point
	 * pt = GeometryUtils.GEOMETRY_FACTORY.createPoint(coord); double distMin = Double.MAX_VALUE; for (int i = 0; i <
	 * geom.getCoordinates().length - 1; i++) { final Coordinate cc = geom.getCoordinates()[i]; if (cc.equals(coord)) {
	 * return -1; } final Coordinate[] coordinates = new Coordinate[2]; coordinates[0] = cc; coordinates[1] =
	 * geom.getCoordinates()[i + 1]; final Geometry geom_Tmp =
	 * GeometryUtils.GEOMETRY_FACTORY.createLineString(coordinates); final double dist = geom_Tmp.distance(pt); if (dist
	 * < distMin) { distMin = dist; index = i; } } if (geom.getCoordinates()[geom.getCoordinates().length -
	 * 1].equals(coord)) { return -1; } return index; }
	 */

	/**
	 * Masked by.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param obstacles
	 *            the obstacles
	 * @param prec
	 *            the prec
	 * @return the i shape
	 */
	@operator (
			value = "masked_by",
			category = { IOperatorCategory.SPATIAL },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.OBSTACLE })
	@doc (
			examples = { @example (
					value = "perception_geom masked_by obstacle_list",
					equals = "the geometry representing the part of perception_geom visible from the agent position considering the list of obstacles obstacle_list.",
					isExecutable = false) })
	@no_test
	public static IShape masked_by(final IScope scope, final IShape source, final IContainer<?, IShape> obstacles,
			final Integer prec) {
		final int precision = prec == null ? 120 : prec;
		final IAgent a = scope.getAgent();
		final IPoint location = a != null ? a.getLocation() : GamaPointFactory.create(0, 0);
		final Geometry visiblePercept = GeometryUtils.getGeometryFactory().createGeometry(source.getInnerGeometry());
		final boolean isPoint = source.isPoint();
		if (obstacles != null && !obstacles.isEmpty(scope)) {
			// Defer list conversion to inside the non-null/non-empty branch
			final List<IShape> obst = obstacles.listValue(scope, Types.GEOMETRY, false);
			final Geometry pt = GeometryUtils.getGeometryFactory().createPoint(location.toCoordinate());
			final Geometry locG = pt.buffer(0.01).getEnvelope();
			double percepDist = 0;
			for (final IPoint p : source.getPoints()) {
				final double dist = location.euclidianDistanceTo(p);
				if (dist > percepDist) { percepDist = dist; }
			}
			final Geometry gbuff = pt.buffer(percepDist, precision / 4);
			// Pre-wrap all obstacles in PreparedGeometry to accelerate repeat intersection tests
			final List<PreparedGeometry> preparedObst = new ArrayList<>(obst.size());
			for (final IShape s : obst) {
				if (s != null && s.getInnerGeometry() != null) {
					preparedObst.add(PreparedGeometryFactory.prepare(s.getInnerGeometry()));
				}
			}
			final List<IShape> geoms = new ArrayList<>();
			final Coordinate[] gbuffCoords = gbuff.getCoordinates();
			for (int k = 1; k < gbuff.getNumPoints(); k++) {
				final IList<IPoint> coordinates = GamaListFactory.create(Types.POINT, 4);
				coordinates.add(location);
				coordinates.add(GamaPointFactory.create(gbuffCoords[k - 1]));
				coordinates.add(GamaPointFactory.create(gbuffCoords[k]));
				coordinates.add(location);
				final IShape gg = SpatialOperators.inter(scope, source, SpatialCreation.polygon(scope, coordinates));

				if (gg != null && (isPoint || !gg.isPoint())) {
					final IShape s = GamaShapeFactory
							.createFrom(GeometryUtils.geometryCollectionManagement(gg.getInnerGeometry()));
					geoms.add(s);
				}
			}
			final IList<IShape> geomsVisible = GamaListFactory.create();
			final PreparedGeometry ref = PreparedGeometryFactory.prepare(locG);

			for (final IShape geom : geoms) {
				if (!intersectionPrepared(geom, preparedObst)) {
					geomsVisible.addValue(scope, geom);
				} else {
					final IShape perceptReal = difference(scope, geom, obst, ref);

					if (perceptReal != null && (isPoint || !perceptReal.isPoint())) {
						geomsVisible.addValue(scope, perceptReal);
					}
				}
			}
			IList<IShape> geomVisibleF = GamaListFactory.create(Types.GEOMETRY);
			for (final IShape geom : geomsVisible) {
				if (geom.getGeometries().size() > 1) {
					for (IShape g : geom.getGeometries()) { if (g.intersects(location)) { geomVisibleF.add(g); } }

				} else {
					geomVisibleF.add(geom);
				}
			}
			boolean isPolygon = false;
			boolean isLine = false;
			for (final IShape geom : geomVisibleF) {
				isLine = isLine || geom.isLine();
				isPolygon = isPolygon || !geom.isPoint() && !geom.isLine();
			}
			final boolean isPolygonF = isPolygon;
			final boolean isLineF = isLine;

			geomVisibleF.removeIf(g -> (isPolygonF || isLineF) && g.isPoint() && isPolygonF && g.isLine());
			if (geomVisibleF.isEmpty(scope)) return null;
			IShape result = GamaShapeFactory.castToShape(scope, geomVisibleF, false);
			if (result == null || result.getInnerGeometry() == null) {
				geomVisibleF = geomVisibleF.stream().map(g -> SpatialTransformations.enlarged_by(scope, g, 0.1))
						.collect(GamaListFactory.toGamaList());
				result = GamaShapeFactory.castToShape(scope, geomVisibleF, false);
			}
			if (result == null || result.getInnerGeometry() == null) return null;
			if (result.getInnerGeometry() instanceof GeometryCollection) {

				result = SpatialTransformations.enlarged_by(scope, result, 0.1);
			}
			return result;
		}
		return GamaShapeFactory.createFrom(visiblePercept);
	}

	/**
	 * Intersection check using pre-built PreparedGeometry for each obstacle, avoiding per-call preparation overhead.
	 */
	private static boolean intersectionPrepared(final IShape geom, final List<PreparedGeometry> preparedGeoms) {
		if (geom == null) return false;
		final Geometry inner = geom.getInnerGeometry();
		if (inner == null) return false;
		for (final PreparedGeometry pg : preparedGeoms) { if (pg.intersects(inner)) return true; }
		return false;
	}

	/**
	 * Difference.
	 *
	 * @param scope
	 *            the scope
	 * @param geom
	 *            the geom
	 * @param geoms
	 *            the geoms
	 * @param ref
	 *            the ref
	 * @return the i shape
	 */
	private static IShape difference(final IScope scope, final IShape geom, final List<IShape> geoms,
			final PreparedGeometry ref) {
		if (geom == null) return null;
		IShape gR = GamaShapeFactory.createFrom(geom);
		for (final IShape g : geoms) {

			if (g != null && geom.intersects(g)) {
				gR = SpatialOperators.minus(scope, gR, g);
				if (gR == null) return null;
				if (gR.getGeometries().size() > 1) {
					for (final IShape sh : gR.getGeometries()) {
						if (!ref.disjoint(sh.getInnerGeometry())) {
							gR = sh;
							break;
						}
					}
				} else if (ref.disjoint(gR.getInnerGeometry())) return null;
			}
		}

		return gR;
	}

	/**
	 * Masked by.
	 *
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param obstacles
	 *            the obstacles
	 * @return the i shape
	 */
	@operator (
			value = "masked_by",
			category = { IOperatorCategory.SPATIAL },
			concept = {})
	@doc (
			examples = { @example (
					value = "perception_geom masked_by obstacle_list",
					equals = "the geometry representing the part of perception_geom visible from the agent position considering the list of obstacles obstacle_list.",
					isExecutable = false) })
	@no_test
	public static IShape masked_by(final IScope scope, final IShape source, final IContainer<?, IShape> obstacles) {
		return masked_by(scope, source, obstacles, null);
	}

	/**
	 * Split at.
	 *
	 * @param geom
	 *            the geom
	 * @param pt
	 *            the pt
	 * @return the i list
	 */
	@operator (
			value = "split_at",
			content_type = IType.GEOMETRY,
			category = { IOperatorCategory.SPATIAL })
	@doc (
			value = "The two part of the left-operand lines split at the given right-operand point",
			usages = { @usage ("if the left-operand is a point or a polygon, returns an empty list") },
			examples = { @example (
					value = "polyline([{1,2},{4,6}]) split_at {7,6}",
					equals = "[polyline([{1.0,2.0},{7.0,6.0}]), polyline([{7.0,6.0},{4.0,6.0}])]") })
	public static IList<IShape> split_at(final IShape geom, final IPoint pt) {
		final IList<IShape> lines = GamaListFactory.create(Types.GEOMETRY);
		List<Geometry> geoms = null;
		// Compute split coordinate once, shared between both branches
		final Coordinate splitCoord = pt.toCoordinate();
		if (geom.getInnerGeometry() instanceof LineString ls) {
			geoms = splitLineAt(ls.getCoordinates(), splitCoord);
		} else if (geom.getInnerGeometry() instanceof MultiLineString ml) {
			// Find the closest constituent LineString — create the Point only once
			final Point splitPt = GeometryUtils.getGeometryFactory().createPoint(splitCoord);
			Geometry closest = ml.getGeometryN(0);
			double distMin = closest.distance(splitPt);
			for (int i = 1; i < ml.getNumGeometries(); i++) {
				final Geometry sub = ml.getGeometryN(i);
				final double d = sub.distance(splitPt);
				if (d < distMin) {
					distMin = d;
					closest = sub;
				}
			}
			geoms = splitLineAt(((LineString) closest).getCoordinates(), splitCoord);
		}
		if (geoms != null) { for (final Geometry g : geoms) { lines.add(GamaShapeFactory.createFrom(g)); } }
		for (final IShape li : lines) { li.copyAttributesOf(geom); }
		return lines;
	}

	/**
	 * Builds the two sub-LineStrings that result from splitting <code>coords</code> at the given point. The closest
	 * segment is found using {@link org.locationtech.jts.algorithm.Distance#pointToSegment} to avoid allocating any JTS
	 * geometry object inside the hot loop.
	 */
	private static List<Geometry> splitLineAt(final Coordinate[] coords, final Coordinate split) {
		final int nb = coords.length;
		int indexTarget = -1;
		double distanceT = Double.MAX_VALUE;
		for (int i = 0; i < nb - 1; i++) {
			final double distT =
					org.locationtech.jts.algorithm.Distance.pointToSegment(split, coords[i], coords[i + 1]);
			if (distT < distanceT) {
				distanceT = distT;
				indexTarget = i;
			}
		}
		final org.locationtech.jts.geom.GeometryFactory gf = GeometryUtils.getGeometryFactory();
		final Coordinate[] coords1 = new Coordinate[indexTarget + 2];
		for (int i = 0; i <= indexTarget; i++) { coords1[i] = coords[i]; }
		coords1[indexTarget + 1] = split;

		final Coordinate[] coords2 = new Coordinate[nb - indexTarget];
		coords2[0] = split;
		for (int i = indexTarget + 1, k = 1; i < nb; i++, k++) { coords2[k] = coords[i]; }

		final List<Geometry> result = new ArrayList<>(2);
		result.add(gf.createLineString(coords1));
		result.add(gf.createLineString(coords2));
		return result;
	}

}