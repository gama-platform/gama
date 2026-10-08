/*******************************************************************************************************
 *
 * InternalGamaShapeFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.geometry;

import static gama.api.types.geometry.IShape.Type.BOX;
import static gama.api.types.geometry.IShape.Type.CONE;
import static gama.api.types.geometry.IShape.Type.CUBE;
import static gama.api.types.geometry.IShape.Type.CYLINDER;
import static gama.api.types.geometry.IShape.Type.LINECYLINDER;
import static gama.api.types.geometry.IShape.Type.PLAN;
import static gama.api.types.geometry.IShape.Type.POLYHEDRON;
import static gama.api.types.geometry.IShape.Type.POLYPLAN;
import static gama.api.types.geometry.IShape.Type.PYRAMID;
import static gama.api.types.geometry.IShape.Type.SPHERE;
import static gama.api.types.geometry.IShape.Type.TEAPOT;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.geometry.IShapeFactory;
import gama.api.types.geometry.IShape.Type;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.prefs.GamaPreferences;

/**
 * A comprehensive factory for creating {@link GamaShape} objects. This class provides methods to create shapes from
 * existing geometries (JTS), primitive descriptions (circle, square, etc.), and other sources (envelopes, other
 * shapes). It handles 2D and 3D geometry creation. It is the primary way to instantiate shapes in GAMA.
 *
 * Transformations (translation, rotation, scaling) should typically be applied using methods on the {@link GamaShape}
 * instance itself after creation, or via factory methods where supported.
 */
public class InternalGamaShapeFactory implements IShapeFactory {

	/**
	 * A predefined angle (approx 0.423 radians) often used for arrow head calculations.
	 */
	public static double theta = Math.tan(0.423d);

	/**
	 * Creates a new {@link GamaShape} from a JTS {@link Geometry}.
	 *
	 * @param geom
	 *            the source JTS geometry.
	 * @return the new {@link GamaShape}.
	 */
	@Override
	@SuppressWarnings ("deprecation")
	public GamaShape createFrom(final Geometry geom) {
		return new GamaShape(geom);
	}

	/**
	 * Creates an empty {@link GamaShape}.
	 *
	 * @return a new empty {@link GamaShape}.
	 */
	@Override
	public GamaShape create() {
		return createFrom((Geometry) null);
	}

	/**
	 * Creates a new {@link GamaShape} representing the bounding box of a {@link IEnvelope}.
	 *
	 * @param env
	 *            the envelope.
	 * @return the new {@link GamaShape}.
	 */
	@Override
	public GamaShape createFrom(final IEnvelope env) {
		return createFrom(env == null ? GamaEnvelopeFactory.EMPTY.toGeometry() : env.toGeometry());
	}

	/**
	 * Creates a new {@link GamaShape} as a copy of another {@link IShape}. The inner geometry is copied, and attributes
	 * are typically preserved.
	 *
	 * @param source
	 *            the source shape.
	 * @return the new {@link GamaShape}.
	 */
	@Override
	public IShape createFrom(final IShape source) {
		if (source == null) return create();
		return createFrom(source.getInnerGeometry().copy()).withAttributesOf(source);
	}

	/**
	 * Builds a polygon from a list of points. Ensures the ring is closed and valid.
	 *
	 * @param points
	 *            the list of vertices.
	 * @return the created polygon shape.
	 */
	@Override
	public IShape buildPolygon(final List<? extends IShape> points) {
		final int size = points.size();
		// AD 12/05/13 The dimensions of the points to create have been changed
		// to 3, otherwise the z coordinates could
		// be lost when copying this geometry
		CoordinateSequence cs = GamaCoordinateSequenceFactory.create(size, 3);
		for (int i = 0; i < size; i++) {
			final Coordinate p = points.get(i).getLocation().toCoordinate();
			cs.setOrdinate(i, 0, p.x);
			cs.setOrdinate(i, 1, p.y);
			cs.setOrdinate(i, 2, p.z);
		}
		cs = CoordinateSequences.ensureValidRing(GamaCoordinateSequenceFactory.getJTSCoordinateSequenceFactory(), cs);
		final LinearRing geom = GeometryUtils.getGeometryFactory().createLinearRing(cs);
		final Polygon p = GeometryUtils.getGeometryFactory().createPolygon(geom, null);
		// Commented out, see Issue 760, comment #15.
		// return GamaShapeFactory.createFrom(p.isValid() ? p.buffer(0.0) : p);
		// if ( p.isValid() ) { return GamaShapeFactory.createFrom(p.buffer(0.0)); } // Why
		// buffer (0.0) ???
		// return buildPolyline(points);
		// / ???

		return createFrom(p);
		// return GamaShapeFactory.createFrom(GeometryUtils.isClockWise(p) ? p :
		// GeometryUtils.changeClockWise(p));
	}

	/**
	 * Builds a MultiPolygon from a list of list of points (each list forming a polygon).
	 *
	 * @param lpoints
	 *            the list of polygon vertices.
	 * @return the created multi-polygon shape.
	 */
	// A.G 28/05/2015 ADDED for gamanalyser
	@Override
	public IShape buildMultiPolygon(final List<List<IShape>> lpoints) {
		final Polygon[] polys = new Polygon[lpoints.size()];
		for (int z = 0; z < lpoints.size(); z++) {
			final List<IShape> points = lpoints.get(z);
			final int size = points.size();
			// AD 12/05/13 The dimensions of the points to create have been
			// changed to 3, otherwise the z coordinates could
			// be lost when copying this geometry
			CoordinateSequence cs = GamaCoordinateSequenceFactory.create(size, 3);
			for (int i = 0; i < size; i++) {
				final Coordinate p = points.get(i).getLocation().toCoordinate();
				cs.setOrdinate(i, 0, p.x);
				cs.setOrdinate(i, 1, p.y);
				cs.setOrdinate(i, 2, p.z);
			}
			cs = CoordinateSequences.ensureValidRing(GamaCoordinateSequenceFactory.getJTSCoordinateSequenceFactory(),
					cs);
			final LinearRing geom = GeometryUtils.getGeometryFactory().createLinearRing(cs);
			final Polygon p = (Polygon) GeometryUtils.getGeometryFactory().createPolygon(geom, null).convexHull();
			polys[z] = p;
		}
		final MultiPolygon m = GeometryUtils.getGeometryFactory().createMultiPolygon(polys);

		// if ( m.isValid() ) { return GamaShapeFactory.createFrom(m.buffer(0.0)); } // Why
		// buffer (0.0) ???
		return createFrom(m.buffer(0.0));
	}

	/**
	 * Builds a triangle with specified base, height, and location.
	 *
	 * @param base
	 *            the length of the base.
	 * @param height
	 *            the height of the triangle.
	 * @param location
	 *            the center location of the triangle.
	 * @return the triangle shape.
	 */
	@Override
	public IShape buildTriangle(final double base, final double height, final IPoint location) {
		final Coordinate[] points = new Coordinate[4];
		final double z = location == null ? 0.0 : location.getZ();
		points[0] = GamaPointFactory.create(-base / 2.0, height / 2, z).toCoordinate();
		points[1] = GamaPointFactory.create(0, -height / 2, z).toCoordinate();
		points[2] = GamaPointFactory.create(base / 2.0, height / 2, z).toCoordinate();
		points[3] = points[0];
		final CoordinateSequence cs = GamaCoordinateSequenceFactory.create(points);
		final LinearRing geom = GeometryUtils.getGeometryFactory().createLinearRing(cs);
		final Polygon p = GeometryUtils.getGeometryFactory().createPolygon(geom, null);
		final IShape s = createFrom(p);
		if (location != null) { s.setLocation(location); }
		return s;
	}

	/**
	 * Builds an equilateral triangle defined by side length and location.
	 *
	 * @param side_size
	 *            the length of a side.
	 * @param location
	 *            the center location.
	 * @return the triangle shape.
	 */
	@Override
	public IShape buildTriangle(final double side_size, final IPoint location) {
		final double h = Math.sqrt(3) / 2 * side_size;
		final Coordinate[] points = new Coordinate[4];
		final double x = location == null ? 0 : location.getX();
		final double y = location == null ? 0 : location.getY();
		final double z = location == null ? 0 : location.getZ();
		points[0] = GamaPointFactory.create(x - side_size / 2.0, y + h / 3, z).toCoordinate();
		points[1] = GamaPointFactory.create(x, y - 2 * h / 3, z).toCoordinate();
		points[2] = GamaPointFactory.create(x + side_size / 2.0, y + h / 3, z).toCoordinate();
		points[3] = points[0];
		final CoordinateSequence cs = GamaCoordinateSequenceFactory.create(points);
		final LinearRing geom = GeometryUtils.getGeometryFactory().createLinearRing(cs);
		final Polygon p = GeometryUtils.getGeometryFactory().createPolygon(geom, null);
		return createFrom(p);
	}

	/**
	 * Builds a rectangle.
	 *
	 * @param width
	 *            the width of the rectangle.
	 * @param height
	 *            the height of the rectangle.
	 * @param location
	 *            the center location.
	 * @return the rectangle shape.
	 */
	@Override
	public IShape buildRectangle(final double width, final double height, final IPoint location) {
		final IPoint[] points = new IPoint[5];
		final double x = location == null ? 0 : location.getX();
		final double y = location == null ? 0 : location.getY();
		final double z = location == null ? 0 : location.getZ();
		points[4] = GamaPointFactory.create(x - width / 2.0, y + height / 2.0, z);
		points[3] = GamaPointFactory.create(x + width / 2.0, y + height / 2.0, z);
		points[2] = GamaPointFactory.create(x + width / 2.0, y - height / 2.0, z);
		points[1] = GamaPointFactory.create(x - width / 2.0, y - height / 2.0, z);
		points[0] = GamaPointFactory.create(x - width / 2.0, y + height / 2.0, z);
		return createFrom(GeometryUtils.getGeometryFactory().createRectangle(points));
	}

	/**
	 * Builds a polyhedron (3D polygon) with a specified depth.
	 *
	 * @param points
	 *            the base polygon vertices.
	 * @param depth
	 *            the extrusion depth.
	 * @return the polyhedron shape.
	 */
	@Override
	public IShape buildPolyhedron(final List<IShape> points, final Double depth) {
		final IShape g = buildPolygon(points);
		// if (!Spatial.ThreeD.isClockwise(null, g)) {
		// g = Spatial.ThreeD.changeClockwise(null, g);
		// }
		g.setDepth(depth);
		g.setGeometricalType(POLYHEDRON);
		return g;
	}

	/**
	 * Builds a line segment from the origin to a location.
	 *
	 * @param location2
	 *            the end point.
	 * @return the line shape.
	 */
	@Override
	public IShape buildLine(final IShape location2) {
		return buildLine(GamaPointFactory.create(), location2);
	}

	/**
	 * Builds a line segment between two locations.
	 *
	 * @param location1
	 *            the start point.
	 * @param location2
	 *            the end point.
	 * @return the line shape.
	 */
	@Override
	public IShape buildLine(final IShape location1, final IShape location2) {
		final IPoint coordinates[] =
				{ location1 == null ? GamaPointFactory.create(0, 0) : (IPoint) location1.getLocation(),
						location2 == null ? GamaPointFactory.create(0, 0) : (IPoint) location2.getLocation() };
		// WARNING Circumvents a bug in JTS 1.13, where a line built between two
		// identical points would return a null
		// centroid
		if (coordinates[0].equals(coordinates[1])) return GamaShapeFactory.buildPoint(coordinates[0]);
		return createFrom(GeometryUtils.getGeometryFactory().createLineString(coordinates, true));
	}

	/**
	 * Builds a cylinder following a line segment (like a pipe).
	 *
	 * @param location1
	 *            the start point.
	 * @param location2
	 *            the end point.
	 * @param radius
	 *            the radius of the cylinder.
	 * @return the line cylinder shape.
	 */
	@Override
	public IShape buildLineCylinder(final IShape location1, final IShape location2, final double radius) {
		final IShape g = buildLine(location1, location2);
		g.setDepth(radius);
		g.setGeometricalType(LINECYLINDER);
		return g;
	}

	/**
	 * Builds a planar shape from a line defined by two points, extruded by depth.
	 *
	 * @param location1
	 *            the start point.
	 * @param location2
	 *            the end point.
	 * @param depth
	 *            the extrusion depth (height of the plane).
	 * @return the plan shape.
	 */
	@Override
	public IShape buildPlan(final IShape location1, final IShape location2, final Double depth) {
		final IShape g = buildLine(location1, location2);
		g.setDepth(depth);
		g.setGeometricalType(PLAN);
		return g;
	}

	/**
	 * Builds a polyline from a list of points.
	 *
	 * @param points
	 *            the vertices of the polyline.
	 * @return the polyline shape.
	 */
	@Override
	public IShape buildPolyline(final List<? extends IShape> points) {
		final List<Coordinate> coordinates = new ArrayList<>();
		for (final IShape p : points) { coordinates.add(p.getLocation().toCoordinate()); }
		return createFrom(GeometryUtils.getGeometryFactory()
				.createLineString(coordinates.toArray(new Coordinate[coordinates.size()])));
	}

	/**
	 * Builds a cylinder following a polyline.
	 *
	 * @param points
	 *            the vertices of the polyline.
	 * @param radius
	 *            the radius of the cylinder.
	 * @return the polyline cylinder shape.
	 */
	@Override
	public IShape buildPolylineCylinder(final List<IShape> points, final double radius) {
		final IShape g = buildPolyline(points);
		g.setDepth(radius);
		g.setGeometricalType(LINECYLINDER);
		return g;
	}

	/**
	 * Builds a multi-segment plan (polyplan) from a list of points, extruded by depth.
	 *
	 * @param points
	 *            the vertices of the base polyline.
	 * @param depth
	 *            the extrusion depth (height).
	 * @return the polyplan shape.
	 */
	@Override
	public IShape buildPolyplan(final List<IShape> points, final Double depth) {
		final IShape g = buildPolyline(points);
		g.setDepth(depth);
		g.setGeometricalType(POLYPLAN);
		return g;
	}

	/**
	 * Builds a square.
	 *
	 * @param side_size
	 *            the side length.
	 * @param location
	 *            the center location.
	 * @return the square shape.
	 */
	@Override
	public IShape buildSquare(final double side_size, final IPoint location) {
		return buildRectangle(side_size, side_size, location);
	}

	/**
	 * Builds a cube.
	 *
	 * @param side_size
	 *            the side length of the cube.
	 * @param location
	 *            the center location.
	 * @return the cube shape.
	 */
	@Override
	public IShape buildCube(final double side_size, final IPoint location) {
		final IShape g = buildRectangle(side_size, side_size, location);
		g.setDepth(side_size);
		g.setGeometricalType(CUBE);
		return g;

	}

	/**
	 * Builds a 3D box.
	 *
	 * @param width
	 *            the width.
	 * @param height
	 *            the height.
	 * @param depth
	 *            the depth.
	 * @param location
	 *            the center location.
	 * @return the box shape.
	 */
	@Override
	public IShape buildBox(final double width, final double height, final double depth, final IPoint location) {
		final IShape g = buildRectangle(width, height, location);
		g.setDepth(depth);
		g.setGeometricalType(BOX);
		return g;
	}

	/**
	 * Builds a regular hexagon.
	 *
	 * @param size
	 *            the size (width/height approximation).
	 * @param x
	 *            the X center coordinate.
	 * @param y
	 *            the Y center coordinate.
	 * @return the hexagon shape.
	 */
	@Override
	public IShape buildHexagon(final double size, final double x, final double y) {
		return buildHexagon(size, GamaPointFactory.create(x, y));
	}

	/**
	 * Builds a regular hexagon.
	 *
	 * @param size
	 *            the size.
	 * @param location
	 *            the center location.
	 * @return the hexagon shape.
	 */
	@Override
	public IShape buildHexagon(final double size, final IPoint location) {
		return buildHexagon(size, size, location);
	}

	/**
	 * Builds a hexagon with specified width and height.
	 *
	 * @param sizeX
	 *            the width.
	 * @param sizeY
	 *            the height.
	 * @param location
	 *            the center location.
	 * @return the hexagon shape.
	 */
	@Override
	public IShape buildHexagon(final double sizeX, final double sizeY, final IPoint location) {
		final double x = location.getX();
		final double y = location.getY();
		final Coordinate[] coords = new Coordinate[7];
		coords[0] = (Coordinate) GamaPointFactory.create(x - sizeX / 2.0, y);
		coords[1] = (Coordinate) GamaPointFactory.create(x - sizeX / 4, y + sizeY / 2);
		coords[2] = (Coordinate) GamaPointFactory.create(x + sizeX / 4, y + sizeY / 2);
		coords[3] = (Coordinate) GamaPointFactory.create(x + sizeX / 2, y);
		coords[4] = (Coordinate) GamaPointFactory.create(x + sizeX / 4, y - sizeY / 2);
		coords[5] = (Coordinate) GamaPointFactory.create(x - sizeX / 4, y - sizeY / 2);
		coords[6] = (Coordinate) GamaPointFactory.create((IPoint) coords[0]);
		final Geometry g = GeometryUtils.getGeometryFactory()
				.createPolygon(GeometryUtils.getGeometryFactory().createLinearRing(coords), null);
		return createFrom(g);

	}

	/**
	 * Builds a circle.
	 *
	 * @param radius
	 *            the radius.
	 * @param location
	 *            the center location.
	 * @return the circle shape.
	 */
	@Override
	public IShape buildCircle(final double radius, final IPoint location) {
		final Geometry geom = GeometryUtils.getGeometryFactory()
				.createPoint(location == null ? GamaPointFactory.create(0, 0).toCoordinate() : location.toCoordinate());
		final Geometry g = geom.buffer(radius);
		if (location != null) {
			final Coordinate[] coordinates = g.getCoordinates();
			for (int i = 0; i < coordinates.length; i++) { coordinates[i].z = location.getZ(); }
		}
		IShape shape = createFrom(g);
		shape.setGeometricalType(Type.CIRCLE);
		return shape;
	}

	/**
	 * Builds an ellipse.
	 *
	 * @param xRadius
	 *            the horizontal radius (half-width).
	 * @param yRadius
	 *            the vertical radius (half-height).
	 * @param location
	 *            the center location.
	 * @return the ellipse shape.
	 */
	@Override
	public IShape buildEllipse(final double xRadius, final double yRadius, final IPoint location) {
		if (xRadius <= 0 && yRadius <= 0) return createFrom(location);
		final GeometricShapeFactory factory = new GeometricShapeFactory(GeometryUtils.getGeometryFactory());
		factory.setNumPoints(GamaPreferences.Displays.DISPLAY_SLICE_NUMBER.getValue());
		factory.setCentre(location.toCoordinate());
		factory.setWidth(xRadius);
		factory.setHeight(yRadius);
		final Geometry g = factory.createEllipse();
		final Coordinate[] coordinates = g.getCoordinates();
		for (int i = 0; i < coordinates.length; i++) { coordinates[i].z = location.getZ(); }
		return createFrom(g);
	}

	/**
	 * Builds a squircle (superellipse).
	 *
	 * @param xRadius
	 *            the radius/size.
	 * @param power
	 *            the exponent controlling corner roundness.
	 * @param location
	 *            the center location.
	 * @return the squircle shape.
	 */
	@Override
	public IShape buildSquircle(final double xRadius, final double power, final IPoint location) {
		if (xRadius <= 0) return createFrom(location);
		final GeometricShapeFactory factory = new GeometricShapeFactory(GeometryUtils.getGeometryFactory());
		factory.setNumPoints(GamaPreferences.Displays.DISPLAY_SLICE_NUMBER.getValue());
		factory.setCentre(location.toCoordinate());
		factory.setSize(xRadius);
		final Geometry g = factory.createSupercircle(power);
		final Coordinate[] coordinates = g.getCoordinates();
		for (int i = 0; i < coordinates.length; i++) { coordinates[i].z = location.getZ(); }
		return createFrom(g);

	}

	/**
	 * Builds an arc using JTS GeometricShapeFactory.
	 *
	 * @param xRadius
	 *            the radius.
	 * @param heading
	 *            the start angle in decimal degrees.
	 * @param amplitude
	 *            the angular extent in decimal degrees.
	 * @param filled
	 *            whether to create a sector (polygon) or just an arc (linestring).
	 * @param location
	 *            the center location.
	 * @return the arc shape.
	 */
	@Override
	public IShape buildArc(final double xRadius, final double heading, final double amplitude, final boolean filled,
			final IPoint location) {
		if (amplitude <= 0 || xRadius <= 0) return createFrom(location);
		final GeometricShapeFactory factory = new GeometricShapeFactory(GeometryUtils.getGeometryFactory());
		factory.setNumPoints(GamaPreferences.Displays.DISPLAY_SLICE_NUMBER.getValue());
		factory.setCentre(location.toCoordinate());
		factory.setSize(xRadius);
		double list_ampl = amplitude % 360;
		if (list_ampl < 0) { list_ampl += 360; }
		final double angExtent = Math.toRadians(list_ampl);
		double list_heading = (heading - list_ampl / 2) % 360;
		if (list_heading < 0) { list_heading += 360; }
		final double startAng = Math.toRadians(list_heading);

		Geometry g;
		if (filled) {
			g = factory.createArcPolygon(startAng, angExtent);
		} else {
			g = factory.createArc(startAng, angExtent);
		}
		final Coordinate[] coordinates = g.getCoordinates();
		for (int i = 0; i < coordinates.length; i++) { coordinates[i].z = location.getZ(); }
		return createFrom(g);

	}

	/**
	 * Builds a cylinder (vertical).
	 *
	 * @param radius
	 *            the radius.
	 * @param depth
	 *            the height/depth.
	 * @param location
	 *            the center location.
	 * @return the cylinder shape.
	 */
	@Override
	public IShape buildCylinder(final double radius, final double depth, final IPoint location) {
		final IShape g = buildCircle(radius, location);
		g.setDepth(depth);
		g.setGeometricalType(CYLINDER);
		return g;
	}

	/**
	 * Builds a sphere.
	 *
	 * @param radius
	 *            the radius.
	 * @param location
	 *            the center location.
	 * @return the sphere shape.
	 */
	// FIXME: Be sure that a buffer on a sphere returns a sphere.
	@Override
	public IShape buildSphere(final double radius, final IPoint location) {
		final IShape g = buildCircle(radius, location);
		g.setDepth(radius);
		g.setGeometricalType(SPHERE);
		return g;
	}

	/**
	 * Builds a 3D cone.
	 *
	 * @param radius
	 *            the base radius.
	 * @param depth
	 *            the height.
	 * @param location
	 *            the base center location.
	 * @return the cone shape.
	 */
	@Override
	public IShape buildCone3D(final double radius, final double depth, final IPoint location) {
		final IShape g = buildCircle(radius, location);
		g.setDepth(depth);
		g.setGeometricalType(CONE);
		return g;
	}

	/**
	 * Builds a teapot (useful for testing/visualization).
	 *
	 * @param size
	 *            the size scaling factor.
	 * @param location
	 *            the location.
	 * @return the teapot shape.
	 */
	@Override
	public IShape buildTeapot(final double size, final IPoint location) {
		final IShape g = buildCircle(size, location);
		g.setDepth(size);
		g.setGeometricalType(TEAPOT);
		return g;
	}

	/**
	 * Builds a pyramid based on a square base.
	 *
	 * @param side_size
	 *            the base side length.
	 * @param location
	 *            the center location.
	 * @return the pyramid shape.
	 */
	@Override
	public IShape buildPyramid(final double side_size, final IPoint location) {
		final IShape g = buildRectangle(side_size, side_size, location);
		g.setDepth(side_size);
		g.setGeometricalType(PYRAMID);
		return g;
	}

	/**
	 * Builds an arrow shape starting from (0,0,0) to target.
	 *
	 * @param head
	 *            the target point (tip of the arrow).
	 * @param size
	 *            parameter influencing arrow width/head size.
	 * @return the arrow shape.
	 */
	@Override
	public IShape buildArrow(final IPoint head, final double size) {
		return buildArrow(GamaPointFactory.create(), head, size, size, true);
	}

	/**
	 * Builds an arrow shape from tail to head.
	 *
	 * @param tail
	 *            the start point.
	 * @param head
	 *            the end point.
	 * @param arrowWidth
	 *            width of the arrow.
	 * @param arrowLength
	 *            length of the arrow head.
	 * @param closed
	 *            whether the arrow is a closed polygon or open lines.
	 * @return the arrow shape.
	 */
	@Override
	public IShape buildArrow(final IPoint tail, final IPoint head, final double arrowWidth, final double arrowLength,
			final boolean closed) {
		final IList<IPoint> points = GamaListFactory.createWithoutCasting(Types.POINT, head);
		// build the line vector
		final IPoint vecLine = head.minus(tail);
		// setup length parameters
		final double fLength = vecLine.norm();
		// Guard against degenerate segments (tail == head) which would produce NaN coordinates
		if (fLength == 0) return closed ? buildPolygon(points) : buildPolyline(points);
		// build the arrow base vector - normal to the line
		IPoint vecLeft = GamaPointFactory.create(-vecLine.getY(), vecLine.getX());
		if (vecLine.getY() == 0 && vecLine.getX() == 0) { vecLeft = GamaPointFactory.create(-vecLine.getZ(), 0, 0); }
		final double th = arrowWidth / (2.0d * fLength);
		final double ta = arrowLength / (2.0d * theta * fLength);
		// find the base of the arrow
		final IPoint base = head.minus(vecLine.times(ta));
		// build the points on the sides of the arrow
		if (closed) {
			points.add(base.plus(vecLeft.times(th)));
		} else {
			points.add(0, base.plus(vecLeft.times(th)));
		}
		points.add(base.minus(vecLeft.times(th)));
		return closed ? buildPolygon(points) : buildPolyline(points);
	}

	/**
	 * Builds a link (edge) between two shapes as a dynamic geometry.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param source
	 *            the source agent/shape.
	 * @param target
	 *            the target agent/shape.
	 * @return the dynamic link shape.
	 */
	@Override
	public IShape buildLink(final IScope scope, final IShape source, final IShape target) {
		return createFrom(new DynamicLineString(GeometryUtils.getGeometryFactory(), source, target));
	}

	/**
	 * Builds a MultiGeometry (collection) from a list of shapes.
	 *
	 * @param shapeList
	 *            the list of shapes to combine.
	 * @return the combined multi-geometry shape.
	 */
	@Override
	public IShape buildMultiGeometry(final List<IShape> shapeList) {
		if (shapeList.size() == 0) return null;
		if (shapeList.size() == 1) return shapeList.get(0);
		final Geometry geom = GeometryUtils.buildGeometryCollection(shapeList);
		if (geom == null) return null;
		return createFrom(geom);
	}

	/**
	 * Builds a MultiGeometry from variable arguments.
	 *
	 * @param shapes
	 *            the shapes to combine.
	 * @return the combined multi-geometry shape.
	 */
	@Override
	public IShape buildMultiGeometry(final IShape... shapes) {
		List<IShape> shapeList = new ArrayList<>();
		for (final IShape shape : shapes) { if (shape != null) { shapeList.add(shape); } }
		return buildMultiGeometry(shapeList);
	}

	/**
	 * Builds a cross shape.
	 *
	 * @param xRadius
	 *            the radius (half-size of the cross arms).
	 * @param width
	 *            the thickness of the arms.
	 * @param location
	 *            the center location.
	 * @return the cross shape.
	 */
	@Override
	public IShape buildCross(final Double xRadius, final Double width, final IPoint location) {
		if (xRadius <= 0 || width == null || width <= 0) return createFrom(location);
		final double val = xRadius / Math.sqrt(2);
		IShape line1 = buildLine(location.minus(val, val, 0), location.plus(val, val, 0));
		IShape line2 = buildLine(location.minus(-val, val, 0), location.minus(val, -val, 0));
		return createFrom(GeometryUtils.robustUnion(line1.getInnerGeometry().buffer(width),
				line2.getInnerGeometry().buffer(width)));
	}

}
