/*******************************************************************************************************
 *
 * GeometryJSON.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.geom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geotools.api.geometry.BoundingBox;
import org.json.simple.JSONAware;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import gama.dependencies.geojson.GeoJSONUtil;
import gama.dependencies.geojson.IContentHandler;

/**
 * Reads and writes geometry objects to and from geojson.
 *
 * <p>
 *
 * <pre>
 * Point point = new Point(1,2);
 *
 * GeometryJSON g = new GeometryJSON();
 * g.writePoint(point, "point.json"));
 * Point point2 = g.readPoint("point.json");
 *
 * Geometry geometry = ...;
 * g.write(geometry, new File("geometry.json"));
 * geometry = g.read("geometry.json");
 *
 * </pre>
 *
 * @author Justin Deoliveira, OpenGeo
 */
public class GeometryJSON {

	/** The factory. */
	GeometryFactory factory = new GeometryFactory();

	/** The trace. */
	boolean trace = false;

	/** The decimals. */
	int decimals;

	/** The scale. */
	double scale;

	/** Constructs a geometry json instance. */
	public GeometryJSON() {
		this(4);
	}

	/**
	 * Constructs a geometry json instance specifying the number of decimals to use when encoding floating point
	 * numbers.
	 */
	public GeometryJSON(final int decimals) {
		this.decimals = decimals;
		this.scale = Math.pow(10, decimals);
	}

	/** Sets trace flag. */
	public void setTrace(final boolean trace) { this.trace = trace; }

	/**
	 * Tracing flag.
	 *
	 * <p>
	 * When this flag is set parsed documents will be echoed to stdout during parsing.
	 */
	public boolean isTrace() { return trace; }

	/**
	 * Writes a Geometry instance as GeoJSON.
	 *
	 * @param geometry
	 *            The geometry.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 */
	public void write(final Geometry geometry, final Object output) throws IOException {
		if (geometry == null || geometry.isEmpty()) {
			GeoJSONUtil.encode("null", output);
		} else {
			GeoJSONUtil.encode(create(geometry), output);
		}
	}

	/**
	 * Writes a Geometry instance as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #write(Geometry, Object)}
	 *
	 * @param geometry
	 *            The geometry.
	 * @param output
	 *            The output stream.
	 */
	public void write(final Geometry geometry, final OutputStream output) throws IOException {
		GeoJSONUtil.encode(create(geometry), output);
	}

	/**
	 * Writes a Geometry instance as GeoJSON returning the result as a string.
	 *
	 * @param geometry
	 *            The geometry.
	 * @return The geometry encoded as GeoJSON
	 */
	public String toString(final Geometry geometry) {
		StringWriter w = new StringWriter();
		try {
			write(geometry, w);
			return w.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates the.
	 *
	 * @param geometry
	 *            the geometry
	 * @return the map
	 */
	Map<String, Object> create(final Geometry geometry) {
		if (geometry instanceof Point) return createPoint((Point) geometry);

		if (geometry instanceof LineString) return createLine((LineString) geometry);

		if (geometry instanceof Polygon) return createPolygon((Polygon) geometry);

		if (geometry instanceof MultiPoint) return createMultiPoint((MultiPoint) geometry);

		if (geometry instanceof MultiLineString) return createMultiLine((MultiLineString) geometry);

		if (geometry instanceof MultiPolygon) return createMultiPolygon((MultiPolygon) geometry);

		if (geometry instanceof GeometryCollection) return createGeometryCollection((GeometryCollection) geometry);

		throw new IllegalArgumentException("Unable to encode object " + geometry);
	}

	/**
	 * Reads a Geometry instance from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The geometry instance.
	 */
	public Geometry read(final Object input) throws IOException {
		return parse(new GeometryHandler(factory), input);
	}

	/**
	 * Reads a Geometry instance from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #read(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The geometry instance.
	 */
	public Geometry read(final InputStream input) throws IOException {
		return read((Object) input);
	}

	/**
	 * Writes a Point as GeoJSON.
	 *
	 * @param point
	 *            The point.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public void writePoint(final Point point, final Object output) throws IOException {
		encode(createPoint(point), output);
	}

	/**
	 * Writes a Point as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #writePoint(Point, Object)}
	 *
	 * @param point
	 *            The point.
	 * @param output
	 *            The output stream.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public void writePoint(final Point point, final OutputStream output) throws IOException {
		writePoint(point, (Object) output);
	}

	/**
	 * Creates the point.
	 *
	 * @param point
	 *            the point
	 * @return the map
	 */
	Map<String, Object> createPoint(final Point point) {
		LinkedHashMap<String, Object> obj = new LinkedHashMap<>();

		obj.put("type", "Point");
		obj.put("coordinates", new CoordinateSequenceEncoder(point.getCoordinateSequence(), scale));
		return obj;
	}

	/**
	 * Reads a Point from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The point.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public Point readPoint(final Object input) throws IOException {
		return parse(new PointHandler(factory), input);
	}

	/**
	 * Reads a Point from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #readPoint(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The point.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public Point readPoint(final InputStream input) throws IOException {
		return readPoint((Object) input);
	}

	/**
	 * Writes a LineString as GeoJSON.
	 *
	 * @param line
	 *            The line string.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 */
	public void writeLine(final LineString line, final Object output) throws IOException {
		encode(createLine(line), output);
	}

	/**
	 * Writes a LineString as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #writeLine(LineString, Object)}
	 *
	 * @param line
	 *            The line string.
	 * @param output
	 *            The output stream.
	 */
	public void writeLine(final LineString line, final OutputStream output) throws IOException {
		writeLine(line, (Object) output);
	}

	/**
	 * Creates the line.
	 *
	 * @param line
	 *            the line
	 * @return the map
	 */
	Map<String, Object> createLine(final LineString line) {
		LinkedHashMap<String, Object> obj = new LinkedHashMap<>();

		obj.put("type", "LineString");
		obj.put("coordinates", new CoordinateSequenceEncoder(line.getCoordinateSequence(), scale));
		return obj;
	}

	/**
	 * Reads a LineString from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The line string.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public LineString readLine(final Object input) throws IOException {
		return parse(new LineHandler(factory), input);
	}

	/**
	 * Reads a LineString from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #readLine(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The line string.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public LineString readLine(final InputStream input) throws IOException {
		return readLine((Object) input);
	}

	/**
	 * Writes a Polygon as GeoJSON.
	 *
	 * @param poly
	 *            The polygon.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 */
	public void writePolygon(final Polygon poly, final Object output) throws IOException {
		encode(createPolygon(poly), output);
	}

	/**
	 * Writes a Polygon as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #writePolygon(Polygon, Object)}
	 *
	 * @param poly
	 *            The polygon.
	 * @param output
	 *            The output stream.
	 */
	public void writePolygon(final Polygon poly, final OutputStream output) throws IOException {
		writePolygon(poly, (Object) output);
	}

	/**
	 * Creates the polygon.
	 *
	 * @param poly
	 *            the poly
	 * @return the map
	 */
	Map<String, Object> createPolygon(final Polygon poly) {
		LinkedHashMap<String, Object> obj = new LinkedHashMap<>();

		obj.put("type", "Polygon");
		obj.put("coordinates", toList(poly));
		return obj;
	}

	/**
	 * Reads a Polygon from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The polygon.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public Polygon readPolygon(final Object input) throws IOException {
		return parse(new PolygonHandler(factory), input);
	}

	/**
	 * Reads a Polygon from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #readPolygon(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The polygon.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public Polygon readPolygon(final InputStream input) throws IOException {
		return readPolygon((Object) input);
	}

	/**
	 * Writes a MultiPoint as GeoJSON.
	 *
	 * @param mpoint
	 *            The multi point.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 */
	public void writeMultiPoint(final MultiPoint mpoint, final Object output) throws IOException {
		encode(createMultiPoint(mpoint), output);
	}

	/**
	 * Writes a MultiPoint as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #writeMultiPoint(MultiPoint, Object)}
	 *
	 * @param mpoint
	 *            The multi point.
	 * @param output
	 *            The output stream.
	 */
	public void writeMultiPoint(final MultiPoint mpoint, final OutputStream output) throws IOException {
		writeMultiPoint(mpoint, (Object) output);
	}

	/**
	 * Creates the multi point.
	 *
	 * @param mpoint
	 *            the mpoint
	 * @return the map
	 */
	Map<String, Object> createMultiPoint(final MultiPoint mpoint) {
		LinkedHashMap<String, Object> obj = new LinkedHashMap<>();

		obj.put("type", "MultiPoint");
		obj.put("coordinates", toList(mpoint));
		return obj;
	}

	/**
	 * Reads a MultiPoint from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The multi point.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public MultiPoint readMultiPoint(final Object input) throws IOException {
		return parse(new MultiPointHandler(factory), input);
	}

	/**
	 * Reads a MultiPoint from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #readMultiPoint(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The multi point.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public MultiPoint readMultiPoint(final InputStream input) throws IOException {
		return readMultiPoint((Object) input);
	}

	/**
	 * Writes a MultiLineString as GeoJSON.
	 *
	 * @param mline
	 *            The multi line string.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 */
	public void writeMultiLine(final MultiLineString mline, final Object output) throws IOException {
		encode(createMultiLine(mline), output);
	}

	/**
	 * Writes a MultiLineString as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #writeMultiLine(MultiLineString, Object)}
	 *
	 * @param mline
	 *            The multi line string.
	 * @param output
	 *            The output stream.
	 */
	public void writeMultiLine(final MultiLineString mline, final OutputStream output) throws IOException {
		writeMultiLine(mline, (Object) output);
	}

	/**
	 * Creates the multi line.
	 *
	 * @param mline
	 *            the mline
	 * @return the map
	 */
	Map<String, Object> createMultiLine(final MultiLineString mline) {
		LinkedHashMap<String, Object> obj = new LinkedHashMap<>();

		obj.put("type", "MultiLineString");
		obj.put("coordinates", toList(mline));
		return obj;
	}

	/**
	 * Reads a MultiLineString from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The multi line string.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public MultiLineString readMultiLine(final Object input) throws IOException {
		return parse(new MultiLineHandler(factory), input);
	}

	/**
	 * Reads a MultiLineString from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #readMultiLine(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The multi line string.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public MultiLineString readMultiLine(final InputStream input) throws IOException {
		return readMultiLine((Object) input);
	}

	/**
	 * Writes a MultiPolygon as GeoJSON.
	 *
	 * @param mpoly
	 *            The multi polygon.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 */
	public void writeMultiPolygon(final MultiPolygon mpoly, final Object output) throws IOException {
		encode(createMultiPolygon(mpoly), output);
	}

	/**
	 * Writes a MultiPolygon as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #writeMultiPolygon(MultiPolygon, Object)}
	 *
	 * @param mpoly
	 *            The multi polygon.
	 * @param output
	 *            The output stream.
	 */
	public void writeMultiPolygon(final MultiPolygon mpoly, final OutputStream output) throws IOException {
		writeMultiPolygon(mpoly, (Object) output);
	}

	/**
	 * Creates the multi polygon.
	 *
	 * @param mpoly
	 *            the mpoly
	 * @return the map
	 */
	Map<String, Object> createMultiPolygon(final MultiPolygon mpoly) {
		LinkedHashMap<String, Object> obj = new LinkedHashMap<>();

		obj.put("type", "MultiPolygon");
		obj.put("coordinates", toList(mpoly));
		return obj;
	}

	/**
	 * Reads a MultiPolygon from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The multi polygon.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public MultiPolygon readMultiPolygon(final Object input) throws IOException {
		return parse(new MultiPolygonHandler(factory), input);
	}

	/**
	 * Reads a MultiPolygon from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #readMultiPolygon(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The multi polygon.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public MultiPolygon readMultiPolygon(final InputStream input) throws IOException {
		return readMultiPolygon((Object) input);
	}

	/**
	 * Writes a GeometryCollection as GeoJSON.
	 *
	 * @param gcol
	 *            The geometry collection.
	 * @param output
	 *            The output. See {@link GeoJSONUtil#toWriter(Object)} for details.
	 */
	public void writeGeometryCollection(final GeometryCollection gcol, final Object output) throws IOException {
		encode(createGeometryCollection(gcol), output);
	}

	/**
	 * Writes a GeometryCollection as GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #writeGeometryCollection(GeometryCollection, Object)}
	 *
	 * @param gcol
	 *            The geometry collection.
	 * @param output
	 *            The output stream.
	 */
	public void writeGeometryCollection(final GeometryCollection gcol, final OutputStream output) throws IOException {
		writeGeometryCollection(gcol, (Object) output);
	}

	/**
	 * Creates the geometry collection.
	 *
	 * @param gcol
	 *            the gcol
	 * @return the map
	 */
	Map<String, Object> createGeometryCollection(final GeometryCollection gcol) {
		LinkedHashMap<String, Object> obj = new LinkedHashMap<>();

		ArrayList<Map<String, Object>> geoms = new ArrayList<>(gcol.getNumGeometries());
		for (int i = 0; i < gcol.getNumGeometries(); i++) { geoms.add(create(gcol.getGeometryN(i))); }

		obj.put("type", "GeometryCollection");
		obj.put("geometries", geoms);
		return obj;
	}

	/**
	 * Reads a GeometryCollection from GeoJSON.
	 *
	 * @param input
	 *            The input. See {@link GeoJSONUtil#toReader(Object)} for details.
	 * @return The geometry collection.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public GeometryCollection readGeometryCollection(final Object input) throws IOException {
		return parse(new GeometryCollectionHandler(factory), input);
	}

	/**
	 * Reads a GeometryCollection from GeoJSON.
	 *
	 * <p>
	 * This method calls through to {@link #readGeometryCollection(Object)}
	 *
	 * @param input
	 *            The input stream.
	 * @return The geometry collection.
	 * @throws IOException
	 *             In the event of a parsing error or if the input json is invalid.
	 */
	public GeometryCollection readGeometryCollection(final InputStream input) throws IOException {
		return readGeometryCollection((Object) input);
	}

	/**
	 * Writes an BoundingBox instance as GeoJSON returning the result as a string.
	 *
	 * @param bbox
	 *            The bounding box.
	 * @return The bounding box encoded as GeoJSON
	 */
	public String toString(final BoundingBox bbox) {
		return new StringBuffer().append("[").append(bbox.getMinX()).append(",").append(bbox.getMinY()).append(",")
				.append(bbox.getMaxX()).append(",").append(bbox.getMaxY()).append("]").toString();
	}

	/**
	 * Writes an Envelope instance as GeoJSON returning the result as a string.
	 *
	 * @param e
	 *            The envelope
	 * @return The envelope encoded as GeoJSON
	 */
	public String toString(final Envelope e) {
		return new StringBuffer().append("[").append(e.getMinX()).append(",").append(e.getMinY()).append(",")
				.append(e.getMaxX()).append(",").append(e.getMaxY()).append("]").toString();
	}

	/**
	 * Parses the.
	 *
	 * @param <G>
	 *            the generic type
	 * @param handler
	 *            the handler
	 * @param input
	 *            the input
	 * @return the g
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	<G extends Geometry> G parse(final IContentHandler<G> handler, final Object input) throws IOException {
		return GeoJSONUtil.parse(handler, input, trace);
	}

	/**
	 * Encode.
	 *
	 * @param obj
	 *            the obj
	 * @param output
	 *            the output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void encode(final Map<String, Object> obj, final Object output) throws IOException {
		GeoJSONUtil.encode(obj, output);
	}

	/**
	 * To list.
	 *
	 * @param poly
	 *            the poly
	 * @return the list
	 */
	List<CoordinateSequenceEncoder> toList(final Polygon poly) {
		ArrayList<CoordinateSequenceEncoder> list = new ArrayList<>();
		list.add(new CoordinateSequenceEncoder(poly.getExteriorRing().getCoordinateSequence(), scale));

		for (int i = 0; i < poly.getNumInteriorRing(); i++) {
			list.add(new CoordinateSequenceEncoder(poly.getInteriorRingN(i).getCoordinateSequence(), scale));
		}

		return list;
	}

	/**
	 * To list.
	 *
	 * @param mgeom
	 *            the mgeom
	 * @return the list
	 */
	List toList(final GeometryCollection mgeom) {
		ArrayList<Object> list = new ArrayList<>(mgeom.getNumGeometries());
		for (int i = 0; i < mgeom.getNumGeometries(); i++) {
			Geometry g = mgeom.getGeometryN(i);
			if (g instanceof Polygon) {
				list.add(toList((Polygon) g));
			} else if (g instanceof LineString) {
				list.add(new CoordinateSequenceEncoder(((LineString) g).getCoordinateSequence(), scale));
			} else if (g instanceof Point) {
				list.add(new CoordinateSequenceEncoder(((Point) g).getCoordinateSequence(), scale));
			}
		}
		return list;
	}

	/**
	 * The Class CoordinateSequenceEncoder.
	 */
	static class CoordinateSequenceEncoder implements JSONAware /* , JSONStreamAware */ {

		/**
		 * The min value at which the decimal notation is used (below it, the computerized scientific one is used
		 * instead)
		 */
		private static final double DECIMAL_MIN = Math.pow(10, -3);

		/**
		 * The max value at which the decimal notation is used (above it, the computerized scientific one is used
		 * instead)
		 */
		private static final double DECIMAL_MAX = Math.pow(10, 7);

		/** The seq. */
		CoordinateSequence seq;

		/** The scale. */
		double scale;

		/**
		 * Instantiates a new coordinate sequence encoder.
		 *
		 * @param seq
		 *            the seq
		 * @param scale
		 *            the scale
		 */
		CoordinateSequenceEncoder(final CoordinateSequence seq, final double scale) {
			this.seq = seq;
			this.scale = scale;
		}

		@Override
		public String toJSONString() {
			int size = seq.size();

			StringBuilder sb = new StringBuilder();
			if (size > 1) { sb.append("["); }

			for (int i = 0; i < seq.size(); i++) {
				Coordinate coord = seq.getCoordinate(i);
				sb.append("[");
				formatDecimal(coord.x, sb);

				sb.append(",");
				formatDecimal(coord.y, sb);

				if (!Double.isNaN(coord.getZ())) {
					sb.append(",");
					formatDecimal(coord.getZ(), sb);
				}
				sb.append("],");
			}
			sb.setLength(sb.length() - 1);

			if (size > 1) { sb.append("]"); }

			return sb.toString();
		}

		/**
		 * Write JSON string.
		 *
		 * @param out
		 *            the out
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public void writeJSONString(final Writer out) throws IOException {
			int size = seq.size();

			if (size > 1) { out.write("["); }

			for (int i = 0; i < seq.size(); i++) {
				Coordinate coord = seq.getCoordinate(i);
				out.write("[");
				out.write(String.valueOf(coord.x));
				out.write(",");
				out.write(String.valueOf(coord.y));
				if (!Double.isNaN(coord.getZ())) {
					out.write(",");
					out.write(String.valueOf(coord.getZ()));
				}
				out.write("]");
				if (i < seq.size() - 1) { out.write(","); }
			}

			if (size > 1) { out.write("]"); }
		}

		/**
		 * Format decimal.
		 *
		 * @param x
		 *            the x
		 * @param sb
		 *            the sb
		 */
		private void formatDecimal(double x, final StringBuilder sb) {
			if (Math.abs(x) >= DECIMAL_MIN && x < DECIMAL_MAX) {
				x = Math.floor(x * scale + 0.5) / scale;
				long lx = (long) x;
				if (lx == x) {
					sb.append(lx);
				} else {
					sb.append(x);
				}
			} else {
				sb.append(x);
			}
		}
	}
}
