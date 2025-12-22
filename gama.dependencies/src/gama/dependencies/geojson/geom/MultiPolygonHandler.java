/*******************************************************************************************************
 *
 * MultiPolygonHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
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
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * The Class MultiPolygonHandler.
 */
public class MultiPolygonHandler extends GeometryHandlerBase<MultiPolygon> {

	/** The coordinates. */
	List<Coordinate> coordinates;

	/** The rings. */
	List<Coordinate[]> rings;

	/** The polys. */
	List<List<Coordinate[]>> polys;

	/**
	 * Instantiates a new multi polygon handler.
	 *
	 * @param factory
	 *            the factory
	 */
	public MultiPolygonHandler(final GeometryFactory factory) {
		super(factory);
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		if ("coordinates".equals(key)) { polys = new ArrayList<>(); }

		return true;
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		if (rings == null) {
			rings = new ArrayList<>();
		} else if (coordinates == null) {
			coordinates = new ArrayList<>();
		} else if (ordinates == null) { ordinates = new ArrayList<>(); }
		return true;
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		if (ordinates != null) {
			coordinates.add(coordinate(ordinates));
			ordinates = null;
		} else if (coordinates != null) {
			rings.add(coordinates(coordinates));
			coordinates = null;
		} else if (rings != null) {
			polys.add(rings);
			rings = null;
		}

		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		if (polys != null) {
			Polygon[] polygons = new Polygon[polys.size()];
			for (int i = 0; i < polys.size(); i++) {
				List<Coordinate[]> rings = polys.get(i);
				if (rings.isEmpty()) { continue; }

				LinearRing outer = factory.createLinearRing(rings.get(0));
				LinearRing[] inner = rings.size() > 1 ? new LinearRing[rings.size() - 1] : null;
				for (int j = 1; j < rings.size(); j++) { inner[j - 1] = factory.createLinearRing(rings.get(j)); }

				polygons[i] = factory.createPolygon(outer, inner);
			}
			value = factory.createMultiPolygon(polygons);
			polys = null;
		}

		return true;
	}
}
