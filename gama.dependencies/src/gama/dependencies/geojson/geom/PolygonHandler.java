/*******************************************************************************************************
 *
 * PolygonHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.geom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * The Class PolygonHandler.
 */
public class PolygonHandler extends GeometryHandlerBase<Polygon> {

	/** The coordinates. */
	List<Coordinate> coordinates;

	/** The rings. */
	List<Coordinate[]> rings;

	/**
	 * Instantiates a new polygon handler.
	 *
	 * @param factory
	 *            the factory
	 */
	public PolygonHandler(final GeometryFactory factory) {
		super(factory);
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		if ("coordinates".equals(key)) { rings = new ArrayList<>(); }
		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		if (rings != null) {
			if (rings.isEmpty()) throw new IllegalArgumentException("Polygon specified with no rings.");

			LinearRing outer = factory.createLinearRing(rings.get(0));
			LinearRing[] inner = null;
			if (rings.size() > 1) {
				inner = new LinearRing[rings.size() - 1];
				for (int i = 1; i < rings.size(); i++) { inner[i - 1] = factory.createLinearRing(rings.get(i)); }
			}

			value = factory.createPolygon(outer, inner);
			rings = null;
		}
		return true;
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		if (coordinates == null) {
			coordinates = new ArrayList<>();
		} else if (ordinates == null) { ordinates = new ArrayList<>(); }
		return true;
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		if (ordinates != null) {
			Coordinate c = coordinate(ordinates);
			coordinates.add(c);
			ordinates = null;
		} else if (coordinates != null) {
			rings.add(coordinates(coordinates));
			coordinates = null;
		}
		return true;
	}
}
