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

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.common.geometry.GeometryUtils;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
		Geometry geom = null;
		final Geometry geom1 = g1.getInnerGeometry();
		final Geometry geom2 = g2.getInnerGeometry();
		try {

			geom = geom1.intersection(geom2);
		} catch (final Exception ex) {
			try {
				final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				geom = GeometryPrecisionReducer.reducePointwise(geom1, pm)
						.intersection(GeometryPrecisionReducer.reducePointwise(geom2, pm));
			} catch (final Exception e) {
				// AD 12/04/13 : Addition of a third method in case of
				// exception
				try {
					geom = geom1.buffer(0.01, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_FLAT)

							.intersection(geom2.buffer(0.01, BufferParameters.DEFAULT_QUADRANT_SEGMENTS,
									BufferParameters.CAP_FLAT));
				} catch (final Exception e2) {
					return null;
				}
			}
		}
		if (geom == null || geom.isEmpty()) return null;
		// WARNING The attributes of the left-hand shape are kept, but not
		// those of the right-hand shape
		final GamaShape result = GamaShapeFactory.createFrom(geom).withAttributesOf(g1);
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
			value = { "+", "union" },
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
		Geometry geom;
		try {
			geom = geom1.union(geom2);
		} catch (final Exception e) {
			try {
				final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				geom = GeometryPrecisionReducer.reducePointwise(geom1, pm)
						.intersection(GeometryPrecisionReducer.reducePointwise(geom2, pm));
			} catch (final Exception e1) {
				try {
					geom = SpatialTransformations.translated_by(scope, g2.copy(scope), new GamaPoint(0.01, 0))
							.getInnerGeometry().union(geom1);

				} catch (final Exception e2) {
					// AD 12/04/13 : Addition of a third method in case of
					// exception
					try {
						geom = geom1.buffer(0.01, 0, BufferParameters.CAP_SQUARE)
								.union(geom2.buffer(0.01, 0, BufferParameters.CAP_SQUARE));
					} catch (final Exception e3) {
						geom = SpatialTransformations.rotated_by(scope, g2.copy(scope), 0.1).getInnerGeometry()
								.union(geom1);
					}
				}
			}

		}
		if (geom == null || geom.isEmpty()) return null;
		final GamaShape result = GamaShapeFactory.createFrom(geom).withAttributesOf(g1);
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
			return Cast.asGeometry(scope, elements, false);
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
			final GamaShape result = GamaShapeFactory.createFrom(res).withAttributesOf(g1);
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
		Geometry geom1 = GeometryUtils.GEOMETRY_FACTORY.createGeometry(g1.getInnerGeometry());
		for (final IShape ag : agents.iterable(scope)) {
			if (ag != null && ag.getInnerGeometry() != null) {
				geom1 = difference(geom1, ag.getInnerGeometry());
				if (geom1 == null || geom1.isEmpty()) return null;
			}
		}
		if (geom1 == null || geom1.isEmpty()) return null;
		final GamaShape result = GamaShapeFactory.createFrom(geom1).withAttributesOf(g1);
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
								return g1.difference(g2.buffer(Math.min(0.01, g2.getArea() / 1000), 10,
										BufferParameters.CAP_FLAT));
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
	public static IShape add_point(final IScope scope, final IShape g, final GamaPoint p) {
		if (p == null || g == null) return g;
		final Coordinate point = p;
		final Geometry geometry = g.getInnerGeometry();
		Geometry geom_Tmp = null;
		if (geometry instanceof Point) {
			final Coordinate[] coord = new Coordinate[2];
			coord[0] = geometry.getCoordinate();
			coord[1] = point;
			geom_Tmp = GeometryUtils.GEOMETRY_FACTORY.createLineString(coord);
		} else if (geometry instanceof MultiPoint) {
			final Coordinate[] coordinates = new Coordinate[geometry.getNumPoints() + 1];
			coordinates[coordinates.length - 1] = p;
			geom_Tmp = GeometryUtils.GEOMETRY_FACTORY.createMultiPointFromCoords(coordinates);
		} else if (geometry instanceof LineString) {
			geom_Tmp = createLineStringWithPoint(geometry, point);
		} else if (geometry instanceof MultiLineString) {
			Geometry closestGeom = null;
			double distMin = Double.MAX_VALUE;
			int id = -1;
			final Point pt = GeometryUtils.GEOMETRY_FACTORY.createPoint(point);
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				final Geometry geom = geometry.getGeometryN(i);
				final double dist = geom.distance(pt);
				if (dist < distMin) {
					distMin = dist;
					closestGeom = geom;
					id = i;
				}
			}
			final LineString[] lineStrings = new LineString[geometry.getNumGeometries()];
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				if (i != id) {
					lineStrings[i] = (LineString) geometry.getGeometryN(i);
				} else {
					lineStrings[i] = (LineString) createLineStringWithPoint(closestGeom, point);
				}
			}
			geom_Tmp = GeometryUtils.GEOMETRY_FACTORY.createMultiLineString(lineStrings);
		} else if (geometry instanceof Polygon) {
			geom_Tmp = createPolygonWithPoint(geometry, point);
		} else if (geometry instanceof MultiPolygon) {
			Geometry closestGeom = null;
			double distMin = Double.MAX_VALUE;
			int id = -1;
			final Point pt = GeometryUtils.GEOMETRY_FACTORY.createPoint(point);
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				final Geometry geom = geometry.getGeometryN(i);
				final double dist = geom.distance(pt);
				if (dist < distMin) {
					distMin = dist;
					closestGeom = geom;
					id = i;
				}
			}
			final Polygon[] polygons = new Polygon[geometry.getNumGeometries()];
			for (int i = 0; i < geometry.getNumGeometries(); i++) {
				if (i != id) {
					polygons[i] = (Polygon) geometry.getGeometryN(i);
				} else {
					polygons[i] = (Polygon) createPolygonWithPoint(closestGeom, point);
				}
			}
			geom_Tmp = GeometryUtils.GEOMETRY_FACTORY.createMultiPolygon(polygons);
		}
		if (geom_Tmp != null) {
			final GamaShape result = GamaShapeFactory.createFrom(geom_Tmp).withAttributesOf(g);
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
		final int nbPts = ((Polygon) geometry).getExteriorRing().getCoordinates().length;
		for (int index = 0; index <= nbPts; index++) {
			final Coordinate[] coord = new Coordinate[nbPts + 1];
			for (int i = 0; i < index; i++) { coord[i] = geometry.getCoordinates()[i]; }
			coord[index] = point;
			for (int i = index + 1; i < coord.length; i++) { coord[i] = geometry.getCoordinates()[i - 1]; }
			final LinearRing[] lrs = new LinearRing[((Polygon) geometry).getNumInteriorRing()];
			for (int i = 0; i < lrs.length; i++) { lrs[i] = ((Polygon) geometry).getInteriorRingN(i); }
			final Geometry g = GeometryUtils.GEOMETRY_FACTORY
					.createPolygon(GeometryUtils.GEOMETRY_FACTORY.createLinearRing(coord), lrs);
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
		for (int index = 0; index <= geometry.getCoordinates().length; index++) {
			final Coordinate[] coord = new Coordinate[geometry.getCoordinates().length + 1];
			for (int i = 0; i < index; i++) { coord[i] = geometry.getCoordinates()[i]; }
			coord[index] = point;
			for (int i = index + 1; i < coord.length; i++) { coord[i] = geometry.getCoordinates()[i - 1]; }
			final Geometry g = GeometryUtils.GEOMETRY_FACTORY.createLineString(coord);
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
	 * private static int indexClosestSegment(final Geometry geom, final Coordinate coord) { int index = -1; final
	 * Point pt = GeometryUtils.GEOMETRY_FACTORY.createPoint(coord); double distMin = Double.MAX_VALUE; for (int i =
	 * 0; i < geom.getCoordinates().length - 1; i++) { final Coordinate cc = geom.getCoordinates()[i]; if
	 * (cc.equals(coord)) { return -1; } final Coordinate[] coordinates = new Coordinate[2]; coordinates[0] = cc;
	 * coordinates[1] = geom.getCoordinates()[i + 1]; final Geometry geom_Tmp =
	 * GeometryUtils.GEOMETRY_FACTORY.createLineString(coordinates); final double dist = geom_Tmp.distance(pt); if
	 * (dist < distMin) { distMin = dist; index = i; } } if (geom.getCoordinates()[geom.getCoordinates().length -
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
		final List<IShape> obst =
				obstacles == null ? new ArrayList<>() : obstacles.listValue(scope, Types.GEOMETRY, false);
		final GamaPoint location = a != null ? a.getLocation() : new GamaPoint(0, 0);
		final Geometry visiblePercept = GeometryUtils.GEOMETRY_FACTORY.createGeometry(source.getInnerGeometry());
		final boolean isPoint = source.isPoint();
		if (obstacles != null && !obstacles.isEmpty(scope)) {
			final Geometry pt = GeometryUtils.GEOMETRY_FACTORY.createPoint(location);
			final Geometry locG = pt.buffer(0.01).getEnvelope();
			double percepDist = 0;
			for (final GamaPoint p : source.getPoints()) {
				final double dist = location.euclidianDistanceTo(p);
				if (dist > percepDist) { percepDist = dist; }
			}
			final Geometry gbuff = pt.buffer(percepDist, precision / 4);
			final List<IShape> geoms = new ArrayList<>();
			for (int k = 1; k < gbuff.getNumPoints(); k++) {
				final IList coordinates = GamaListFactory.create(Types.POINT, 4);
				coordinates.add(location);
				coordinates.add(new GamaPoint(gbuff.getCoordinates()[k - 1]));
				coordinates.add(new GamaPoint(gbuff.getCoordinates()[k]));
				coordinates.add(location);
				final IShape gg =
						SpatialOperators.inter(scope, source, SpatialCreation.polygon(scope, coordinates));

				if (gg != null && (isPoint || !gg.isPoint())) {
					final IShape s = GamaShapeFactory
							.createFrom(GeometryUtils.geometryCollectionManagement(gg.getInnerGeometry()));
					geoms.add(s);
				}
			}
			final IList<IShape> geomsVisible = GamaListFactory.create();
			final PreparedGeometry ref = PreparedGeometryFactory.prepare(locG);

			for (final IShape geom : geoms) {
				if (!intersection(geom, obst)) {
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
			if (geomVisibleF.isEmpty(scope)) {
				return null;
			}
			IShape result = Cast.asGeometry(scope, geomVisibleF, false);
			if (result == null || result.getInnerGeometry() == null) {
				geomVisibleF.stream().forEach(g -> SpatialTransformations.enlarged_by(scope, g, 0.1));
				result = Cast.asGeometry(scope, geomVisibleF, false);
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
	 * Intersection.
	 *
	 * @param geom
	 *            the geom
	 * @param geoms
	 *            the geoms
	 * @return true, if successful
	 */
	private static boolean intersection(final IShape geom, final List<IShape> geoms) {
		if (geom == null) return false;
		for (final IShape g : geoms) { if (g != null && geom.intersects(g)) return true; }
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
	public static IList<IShape> split_at(final IShape geom, final GamaPoint pt) {
		final IList<IShape> lines = GamaListFactory.create(Types.GEOMETRY);
		List<Geometry> geoms = null;
		if (geom.getInnerGeometry() instanceof LineString) {
			final Coordinate[] coords = ((LineString) geom.getInnerGeometry()).getCoordinates();
			final Point pt1 = GeometryUtils.GEOMETRY_FACTORY.createPoint(pt.getLocation());
			final int nb = coords.length;
			int indexTarget = -1;
			double distanceT = Double.MAX_VALUE;
			for (int i = 0; i < nb - 1; i++) {
				final Coordinate s = coords[i];
				final Coordinate t = coords[i + 1];
				final Coordinate[] seg = { s, t };
				final Geometry segment = GeometryUtils.GEOMETRY_FACTORY.createLineString(seg);
				final double distT = segment.distance(pt1);
				if (distT < distanceT) {
					distanceT = distT;
					indexTarget = i;
				}
			}
			int nbSp = indexTarget + 2;
			final Coordinate[] coords1 = new Coordinate[nbSp];
			for (int i = 0; i <= indexTarget; i++) { coords1[i] = coords[i]; }
			coords1[indexTarget + 1] = new GamaPoint(pt.getLocation());

			nbSp = coords.length - indexTarget;
			final Coordinate[] coords2 = new Coordinate[nbSp];
			coords2[0] = new GamaPoint(pt.getLocation());
			int k = 1;
			for (int i = indexTarget + 1; i < coords.length; i++) {
				coords2[k] = coords[i];
				k++;
			}
			final List<Geometry> geoms1 = new ArrayList<>();
			geoms1.add(GeometryUtils.GEOMETRY_FACTORY.createLineString(coords1));
			geoms1.add(GeometryUtils.GEOMETRY_FACTORY.createLineString(coords2));
			geoms = geoms1;
		} else if (geom.getInnerGeometry() instanceof MultiLineString) {
			final Point point = GeometryUtils.GEOMETRY_FACTORY.createPoint(pt);
			final MultiLineString ml = (MultiLineString) geom.getInnerGeometry();
			Geometry geom2 = ml.getGeometryN(0);
			double distMin = geom2.distance(point);
			for (int i = 1; i < ml.getNumGeometries(); i++) {
				final Geometry gg = ml.getGeometryN(i);
				final double dist = gg.distance(point);
				if (dist <= distMin) {
					geom2 = gg;
					distMin = dist;
				}
			}
			final Coordinate[] coords = ((LineString) geom2).getCoordinates();
			final Point pt1 = GeometryUtils.GEOMETRY_FACTORY.createPoint(new GamaPoint(pt.getLocation()));
			final int nb = coords.length;
			int indexTarget = -1;
			double distanceT = Double.MAX_VALUE;
			for (int i = 0; i < nb - 1; i++) {
				final Coordinate s = coords[i];
				final Coordinate t = coords[i + 1];
				final Coordinate[] seg = { s, t };
				final Geometry segment = GeometryUtils.GEOMETRY_FACTORY.createLineString(seg);
				final double distT = segment.distance(pt1);
				if (distT < distanceT) {
					distanceT = distT;
					indexTarget = i;
				}
			}
			int nbSp = indexTarget + 2;
			final Coordinate[] coords1 = new Coordinate[nbSp];
			for (int i = 0; i <= indexTarget; i++) { coords1[i] = coords[i]; }
			coords1[indexTarget + 1] = new GamaPoint(pt.getLocation());

			nbSp = coords.length - indexTarget;
			final Coordinate[] coords2 = new Coordinate[nbSp];
			coords2[0] = new GamaPoint(pt.getLocation());
			int k = 1;
			for (int i = indexTarget + 1; i < coords.length; i++) {
				coords2[k] = coords[i];
				k++;
			}
			final List<Geometry> geoms1 = new ArrayList<>();
			geoms1.add(GeometryUtils.GEOMETRY_FACTORY.createLineString(coords1));
			geoms1.add(GeometryUtils.GEOMETRY_FACTORY.createLineString(coords2));
			geoms = geoms1;
		}
		if (geoms != null) { for (final Geometry g : geoms) { lines.add(GamaShapeFactory.createFrom(g)); } }
		for (final IShape li : lines) { li.copyAttributesOf(geom); }

		return lines;
	}
}
