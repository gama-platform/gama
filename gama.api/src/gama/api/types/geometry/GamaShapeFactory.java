/*******************************************************************************************************
 *
 * GamaShapeFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.locationtech.jts.util.AssertionFailedException;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.IGamaFile;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.misc.IContainer;
import gama.api.types.pair.IPair;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.geometry.IEnvelope;

/**
 * A comprehensive factory for creating {@link IShape} objects. This class provides methods to create shapes from
 * existing geometries (JTS), primitive descriptions (circle, square, etc.), and other sources (envelopes, other
 * shapes). It handles 2D and 3D geometry creation. It is the primary way to instantiate shapes in GAMA.
 *
 * Transformations (translation, rotation, scaling) should typically be applied using methods on the {@link IShape}
 * instance itself after creation, or via factory methods where supported.
 */
public class GamaShapeFactory {

	/** The Internal factory. */
	static IShapeFactory InternalFactory;

	/** The Constant SHAPE_READER. */
	public static final WKTReader SHAPE_READER = new WKTReader();

	/**
	 * Sets the builder.
	 *
	 * @param factory
	 *            the new builder
	 */
	public static void setBuilder(final IShapeFactory factory) { InternalFactory = factory; }

	/**
	 * Creates a new {@link IShape} from a JTS {@link Geometry}.
	 *
	 * @param geom
	 *            the source JTS geometry.
	 * @return the new {@link IShape}.
	 */
	@SuppressWarnings ("deprecation")
	public static IShape createFrom(final Geometry geom) {
		return InternalFactory.createFrom(geom);
	}

	/**
	 * Creates an empty {@link IShape}.
	 *
	 * @return a new empty {@link IShape}.
	 */
	public static IShape create() {
		return InternalFactory.create();
	}

	/**
	 * Creates a new {@link IShape} representing the bounding box of a {@link IEnvelope}.
	 *
	 * @param env
	 *            the envelope.
	 * @return the new {@link IShape}.
	 */
	public static IShape createFrom(final IEnvelope env) {
		return InternalFactory.createFrom(env);
	}

	/**
	 * Creates a new {@link IShape} as a copy of another {@link IShape}. The inner geometry is copied, and attributes
	 * are typically preserved.
	 *
	 * @param source
	 *            the source shape.
	 * @return the new {@link IShape}.
	 */
	public static IShape createFrom(final IShape source) {
		return InternalFactory.createFrom(source);
	}

	/**
	 * Builds a polygon from a list of points. Ensures the ring is closed and valid.
	 *
	 * @param points
	 *            the list of vertices.
	 * @return the created polygon shape.
	 */
	public static IShape buildPolygon(final List<? extends IShape> points) {
		return InternalFactory.buildPolygon(points);
	}

	/**
	 * Builds a MultiPolygon from a list of list of points (each list forming a polygon).
	 *
	 * @param lpoints
	 *            the list of polygon vertices.
	 * @return the created multi-polygon shape.
	 */
	// A.G 28/05/2015 ADDED for gamanalyser
	public static IShape buildMultiPolygon(final List<List<IShape>> lpoints) {
		return InternalFactory.buildMultiPolygon(lpoints);
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
	public static IShape buildTriangle(final double base, final double height, final IPoint location) {
		return InternalFactory.buildTriangle(base, height, location);
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
	public static IShape buildTriangle(final double side_size, final IPoint location) {
		return InternalFactory.buildTriangle(side_size, location);
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
	public static IShape buildRectangle(final double width, final double height, final IPoint location) {
		return InternalFactory.buildRectangle(width, height, location);
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
	public static IShape buildPolyhedron(final List<IShape> points, final Double depth) {
		return InternalFactory.buildPolyhedron(points, depth);
	}

	/**
	 * Builds a line segment from the origin to a location.
	 *
	 * @param location2
	 *            the end point.
	 * @return the line shape.
	 */
	public static IShape buildLine(final IShape location2) {
		return InternalFactory.buildLine(location2);
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
	public static IShape buildLine(final IShape location1, final IShape location2) {
		return InternalFactory.buildLine(location1, location2);
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
	public static IShape buildLineCylinder(final IShape location1, final IShape location2, final double radius) {
		return InternalFactory.buildLineCylinder(location1, location2, radius);
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
	public static IShape buildPlan(final IShape location1, final IShape location2, final Double depth) {
		return InternalFactory.buildPlan(location1, location2, depth);
	}

	/**
	 * Builds a polyline from a list of points.
	 *
	 * @param points
	 *            the vertices of the polyline.
	 * @return the polyline shape.
	 */
	public static IShape buildPolyline(final List<? extends IShape> points) {
		return InternalFactory.buildPolyline(points);
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
	public static IShape buildPolylineCylinder(final List<IShape> points, final double radius) {
		return InternalFactory.buildPolylineCylinder(points, radius);
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
	public static IShape buildPolyplan(final List<IShape> points, final Double depth) {
		return InternalFactory.buildPolyplan(points, depth);
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
	public static IShape buildSquare(final double side_size, final IPoint location) {
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
	public static IShape buildCube(final double side_size, final IPoint location) {
		return InternalFactory.buildCube(side_size, location);
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
	public static IShape buildBox(final double width, final double height, final double depth, final IPoint location) {
		return InternalFactory.buildBox(width, height, depth, location);
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
	public static IShape buildHexagon(final double size, final double x, final double y) {
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
	public static IShape buildHexagon(final double size, final IPoint location) {
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
	public static IShape buildHexagon(final double sizeX, final double sizeY, final IPoint location) {
		return InternalFactory.buildHexagon(sizeX, sizeY, location);
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
	public static IShape buildCircle(final double radius, final IPoint location) {
		return InternalFactory.buildCircle(radius, location);
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
	public static IShape buildEllipse(final double xRadius, final double yRadius, final IPoint location) {
		return InternalFactory.buildEllipse(xRadius, yRadius, location);
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
	public static IShape buildSquircle(final double xRadius, final double power, final IPoint location) {
		return InternalFactory.buildSquircle(xRadius, power, location);
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
	public static IShape buildArc(final double xRadius, final double heading, final double amplitude,
			final boolean filled, final IPoint location) {
		return InternalFactory.buildArc(xRadius, heading, amplitude, filled, location);
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
	public static IShape buildCylinder(final double radius, final double depth, final IPoint location) {
		return InternalFactory.buildCylinder(radius, depth, location);
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
	public static IShape buildSphere(final double radius, final IPoint location) {
		return InternalFactory.buildSphere(radius, location);
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
	public static IShape buildCone3D(final double radius, final double depth, final IPoint location) {
		return InternalFactory.buildCone3D(radius, depth, location);
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
	public static IShape buildTeapot(final double size, final IPoint location) {
		return InternalFactory.buildTeapot(size, location);
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
	public static IShape buildPyramid(final double side_size, final IPoint location) {
		return InternalFactory.buildPyramid(side_size, location);
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
	public static IShape buildArrow(final IPoint head, final double size) {
		return InternalFactory.buildArrow(GamaPointFactory.create(), head, size, size, true);
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
	public static IShape buildArrow(final IPoint tail, final IPoint head, final double arrowWidth,
			final double arrowLength, final boolean closed) {
		return InternalFactory.buildArrow(tail, head, arrowWidth, arrowLength, closed);
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
	public static IShape buildLink(final IScope scope, final IShape source, final IShape target) {
		return InternalFactory.buildLink(scope, source, target);
	}

	/**
	 * Builds a MultiGeometry (collection) from a list of shapes.
	 *
	 * @param shapes
	 *            the list of shapes to combine.
	 * @return the combined multi-geometry shape.
	 */
	public static IShape buildMultiGeometry(final IList<IShape> shapes) {
		return InternalFactory.buildMultiGeometry(shapes);
	}

	/**
	 * Builds a MultiGeometry from variable arguments.
	 *
	 * @param shapes
	 *            the shapes to combine.
	 * @return the combined multi-geometry shape.
	 */
	public static IShape buildMultiGeometry(final IShape... shapes) {
		return InternalFactory.buildMultiGeometry(shapes);
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
	public static IShape buildCross(final Double xRadius, final Double width, final IPoint location) {
		return InternalFactory.buildCross(xRadius, width, location);
	}

	/**
	 * Creates the point.
	 *
	 * @param location
	 *            the location
	 * @return the gama shape
	 */
	public static IShape buildPoint(final IShape location) {
		return createFrom(GeometryUtils.getGeometryFactory().createPoint(location == null
				? GamaPointFactory.create(0, 0).toCoordinate() : location.getLocation().toCoordinate()));
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("unchecked")
	public static IShape castToShape(final IScope scope, final Object obj, final boolean copy)
			throws GamaRuntimeException {
		return switch (obj) {
			case null -> null;
			case IShape is -> copy ? is.copy(scope) : is;
			case ISpecies s -> geometriesToGeometry(scope, s.getPopulation(scope));
			case IPair p -> pairToGeometry(scope, p);
			case IGamaFile.WithGeometry f -> f.getGeometry(scope);
			case IContainer c -> isPoints(scope, c) ? pointsToGeometry(scope, c) : geometriesToGeometry(scope, c);
			case String s -> {
				try {
					yield createFrom(SHAPE_READER.read(s));
				} catch (final ParseException e) {
					GAMA.reportError(scope,
							GamaRuntimeException.warning("WKT Parsing exception: " + e.getMessage(), scope), false);
					yield null;
				}
			}
			default -> null;
		};
	}

	/**
	 * Pair to geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param p
	 *            the p
	 * @return the i shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IShape pairToGeometry(final IScope scope, final IPair p) throws GamaRuntimeException {
		final IShape first = castToShape(scope, p.key(), false);
		if (first == null) return null;
		final IShape second = castToShape(scope, p.value(), false);
		if (second == null) return null;
		return buildLink(scope, first, second);
	}

	/**
	 * Points to geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param coordinates
	 *            the coordinates
	 * @return the gama shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IShape pointsToGeometry(final IScope scope, final IContainer<?, IPoint> coordinates)
			throws GamaRuntimeException {
		if (coordinates != null && !coordinates.isEmpty(scope)) {
			final List<List<IPoint>> geoSimp = GamaListFactory.create(Types.LIST.of(Types.POINT));
			// WARNING The list of points is NOT recopied (verify side effects)
			geoSimp.add(coordinates.listValue(scope, Types.NO_TYPE, false));
			final List<List<List<IPoint>>> geomG = GamaListFactory.create(Types.LIST);
			geomG.add(geoSimp);
			final Geometry geom = GeometryUtils.buildGeometryJTS(geomG);
			return createFrom(geom);
		}
		return null;
	}

	/**
	 * Checks if a container holds only {@link IPoint} objects.
	 *
	 * @param scope
	 *            the execution scope.
	 * @param obj
	 *            the container to check.
	 * @return true if all elements are points, false otherwise.
	 */
	private static boolean isPoints(final IScope scope, final IContainer obj) {
		for (final Object o : obj.iterable(scope)) { if (!(o instanceof IPoint)) return false; }
		return true;
	}

	/**
	 * Geometries to geometry.
	 *
	 * @param scope
	 *            the scope
	 * @param ags
	 *            the ags
	 * @return the gama shape
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public static IShape geometriesToGeometry(final IScope scope, final IContainer<?, ? extends IShape> ags)
			throws GamaRuntimeException {
		if (ags == null || ags.isEmpty(scope)) return null;
		// final Geometry geoms[] = new Geometry[ags.length(scope)];
		final List<Geometry> geoms = new ArrayList<>(ags.length(scope));
		// int cpt = 0;
		boolean is_polygon = true;
		boolean is_polyline = true;
		for (final IShape ent : ags.iterable(scope)) {
			if (ent == null) { continue; }
			final Geometry geom = ent.getInnerGeometry();
			geoms.add(geom);
			if (is_polygon && !(geom instanceof Polygon)) { is_polygon = false; }
			if (is_polyline && !(geom instanceof LineString)) { is_polyline = false; }
			// cpt++;
		}
		if (geoms.size() == 1) return createFrom(geoms.get(0));
		try {
			if (is_polygon) {
				final Geometry geom = CascadedPolygonUnion.union(geoms);
				if (geom != null && !geom.isEmpty()) return createFrom(geom);
			} else if (is_polyline) {
				final LineMerger merger = new LineMerger();
				for (final Geometry g : geoms) { merger.add(g); }
				final Collection<LineString> collection = merger.getMergedLineStrings();

				Geometry geom = GeometryUtils.getGeometryFactory()
						.createGeometryCollection(collection.toArray(new Geometry[0]));
				geom = geom.union();
				if (!geom.isEmpty()) return createFrom(geom);

			} else {
				Geometry geom =
						GeometryUtils.getGeometryFactory().createGeometryCollection(geoms.toArray(new Geometry[0]));
				geom = geom.union();
				if (!geom.isEmpty()) return createFrom(geom);
			}
			// See Issue #3602
		} catch (final NullPointerException | AssertionFailedException | TopologyException
				| IllegalArgumentException e) {
			// Geometry gs[] = new Geometry[geoms.length];
			final List<Geometry> gs = new ArrayList<>(geoms.size());
			for (final Geometry g : geoms) { gs.add(g.buffer(0.0)); }
			try {
				final Geometry geom = CascadedPolygonUnion.union(gs);
				if (geom != null && !geom.isEmpty()) return createFrom(geom);
			} catch (final NullPointerException | AssertionFailedException | TopologyException
					| IllegalArgumentException f) {}

		}
		return null;
	}

	/**
	 * @return
	 */
	public static IShape getNullShape() { return buildPoint(GamaPointFactory.getNullPoint()); }

}
