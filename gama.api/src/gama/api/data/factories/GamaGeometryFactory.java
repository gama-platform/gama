/*******************************************************************************************************
 *
 * GamaGeometryFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IPoint;

/**
 * A specific implementation of {@link GeometryFactory} optimized for GAMA. This class provides factory methods for
 * creating JTS {@link Geometry} objects (like Points, Polygons, LineStrings, etc.) using GAMA's coordinate sequence
 * factories. It also includes utility methods for geometry validation and modification (e.g., ensuring rings are
 * closed).
 */
public class GamaGeometryFactory extends GeometryFactory {

	/**
	 * The current coordinate sequence factory used by GAMA for creating internal coordinate sequences.
	 */
	// public static final ICoordinateSequenceFactory COORDINATES_FACTORY = GamaCoordinateSequenceFactory.getBuilder();

	/**
	 * The standard JTS coordinate sequence factory, kept for compatibility or specific operations.
	 */
	public static final CoordinateSequenceFactory JTS_COORDINATES_FACTORY = CoordinateArraySequenceFactory.instance();

	/**
	 * Constructs a new GamaGeometryFactory using the default GAMA coordinate sequence factory.
	 */
	public GamaGeometryFactory() {
		super(GamaCoordinateSequenceFactory.getBuilder());
	}

	/**
	 * Creates a new {@link GeometryCollection} from an array of geometries.
	 *
	 * @param geometries
	 *            the geometries to include in the collection.
	 * @return the created {@link GeometryCollection}.
	 */
	public GeometryCollection createCollection(final Geometry... geometries) {
		return new GeometryCollection(geometries, this);
	}

	/**
	 * Checks if an array of coordinates forms a valid ring (closed loop). A valid ring must have at least 4 points and
	 * the first point must equal the last.
	 *
	 * @param pts
	 *            the array of coordinates.
	 * @return true if the coordinates form a valid ring, false otherwise.
	 */
	public static boolean isRing(final Coordinate[] pts) {
		if (pts.length < 4 || !pts[0].equals(pts[pts.length - 1])) return false;
		return true;
	}

	/**
	 * Checks if an array of {@link IPoint} forms a valid ring.
	 *
	 * @param pts
	 *            the array of points.
	 * @return true if the points form a valid ring, false otherwise.
	 */
	public static boolean isRing(final IPoint[] pts) {
		if (pts.length < 4 || !pts[0].equals(pts[pts.length - 1])) return false;
		return true;
	}

	/**
	 * Checks if a list of {@link IPoint} forms a valid ring.
	 *
	 * @param pts
	 *            the list of points.
	 * @return true if the points form a valid ring, false otherwise.
	 */
	public static boolean isRing(final List<IPoint> pts) {
		final int size = pts.size();
		if (size < 4 || !pts.get(0).equals(pts.get(size - 1))) return false;
		return true;
	}

	/**
	 * Calculates the signed area of a ring defined by points using the Shoelace formula. The sign indicates the
	 * orientation (positive for counter-clockwise, usually).
	 *
	 * @param ring
	 *            the array of points forming the ring.
	 * @return the signed area.
	 */
	public static double signedArea(final IPoint[] ring) {
		if (ring.length < 3) return 0.0;
		double sum = 0.0;
		/*
		 * Based on the Shoelace formula. http://en.wikipedia.org/wiki/Shoelace_formula
		 */
		double x0 = ring[0].getX();
		for (int i = 1; i < ring.length - 1; i++) {
			double x = ring[i].getX() - x0;
			double y1 = ring[i + 1].getY();
			double y2 = ring[i - 1].getY();
			sum += x * (y2 - y1);
		}
		return sum / 2.0;
	}

	/**
	 * Creates a {@link LinearRing} from an array of coordinates. If the coordinates do not form a closed ring, the
	 * first point is appended to the end. No clockwiseness enforcement is performed here.
	 *
	 * @param coordinates
	 *            the vertices of the ring.
	 * @return the created {@link LinearRing}.
	 */
	@Override
	public LinearRing createLinearRing(final Coordinate[] coordinates) {
		Coordinate[] coords = coordinates;
		if (!isRing(coords)) { coords = ArrayUtils.add(coords, coords[0]); }
		return createLinearRing(JTS_COORDINATES_FACTORY.create(coords));
	}

	/**
	 * Creates a rectangular {@link Polygon} from a set of points (which should already form a rectangle).
	 *
	 * @param points
	 *            the vertices of the rectangle.
	 * @return the created {@link Polygon}.
	 */
	public Polygon createRectangle(final IPoint... points) {
		final ICoordinateSequenceFactory fact = GamaCoordinateSequenceFactory.getBuilder();
		final CoordinateSequence cs = fact.create(points);
		final LinearRing geom = createLinearRing(cs);
		return createPolygon(geom, null);
	}

	/**
	 * Creates a {@link Polygon} from a shell (outer boundary) and optional holes. This implementation ensures that the
	 * shell and holes are oriented clockwise as per GAMA/JTS conventions if necessary.
	 *
	 * @param shell
	 *            the outer boundary ring.
	 * @param holes
	 *            the inner hole rings (can be null).
	 * @return the created {@link Polygon}.
	 */
	@Override
	public Polygon createPolygon(final LinearRing shell, final LinearRing[] holes) {
		final LinearRing shellClockwise = turnClockwise(shell);
		if (holes != null) { for (int i = 0; i < holes.length; i++) { holes[i] = turnClockwise(holes[i]); } }
		return super.createPolygon(shellClockwise, holes);
	}

	/**
	 * Ensures a {@link LinearRing} is oriented clockwise.
	 *
	 * @param ring
	 *            the source ring.
	 * @return a clockwise version of the ring.
	 */
	private LinearRing turnClockwise(final LinearRing ring) {
		if (ring == null || ring.isEmpty()) return ring;
		return createLinearRing(GamaCoordinateSequenceFactory.getBuilder().create(ring.getCoordinateSequence()));
	}

	@Override
	public ICoordinateSequenceFactory getCoordinateSequenceFactory() {
		return GamaCoordinateSequenceFactory.getBuilder();
	}

	/**
	 * Creates a {@link LineString} from an array of {@link IPoint}.
	 *
	 * @param coordinates
	 *            the points forming the line.
	 * @param copyPoints
	 *            if true, the points are copied; otherwise, they might be referenced.
	 * @return the created {@link LineString}.
	 */
	public LineString createLineString(final IPoint[] coordinates, final boolean copyPoints) {
		return createLineString(GamaCoordinateSequenceFactory.getBuilder().create(coordinates, copyPoints));
	}

	/**
	 * Creates a polygon representing a "fat line" or "buffer" around a given geometry. It converts a line into a series
	 * of rectangles with a specified thickness and unions them.
	 *
	 * @param geometry
	 *            the source geometry (typically a LineString).
	 * @param thickness
	 *            the width of the resulting line.
	 * @return the resulting geometry (usually a Polygon or MultiPolygon).
	 */
	public Geometry createFatLine(final Geometry geometry, final double thickness) {
		ICoordinates c = GamaCoordinateSequenceFactory.pointsOf(geometry);
		Polygon[] rectangles = new Polygon[c.size() - 1];
		int[] index = { 0 };
		c.visit((p0, p1) -> {
			double x1 = p1.getX(), x0 = p0.getX(), y1 = p1.getY(), y0 = p0.getY();
			double dx = x1 - x0; // delta x
			double dy = y1 - y0; // delta y
			double linelength = p1.distance(p0);
			dx /= linelength;
			dy /= linelength;
			// Ok, (dx, dy) is now a unit vector pointing in the direction of the line
			// A perpendicular vector is given by (-dy, dx)
			double px = 0.5d * thickness * -dy; // perpendicular vector with lenght thickness * 0.5
			double py = 0.5d * thickness * dx;
			rectangles[index[0]] = createRectangle(GamaPointFactory.create(x0 + px, y0 + py),
					GamaPointFactory.create(x1 + px, y1 + py), GamaPointFactory.create(x1 - px, y1 - py),
					GamaPointFactory.create(x0 - px, y0 - py), GamaPointFactory.create(x0 + px, y0 + py));
			index[0] += 1;
		});
		GeometryCollection result = createGeometryCollection(rectangles);
		return result.union();
	}

}