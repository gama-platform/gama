/*******************************************************************************************************
 *
 * MultiLineHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
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
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

/**
 * The Class MultiLineHandler.
 */
public class MultiLineHandler extends GeometryHandlerBase<MultiLineString> {

	/** The coordinates. */
	List<Coordinate> coordinates;

	/** The lines. */
	List<Coordinate[]> lines;

	/**
	 * Instantiates a new multi line handler.
	 *
	 * @param factory
	 *            the factory
	 */
	public MultiLineHandler(final GeometryFactory factory) {
		super(factory);
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		if ("coordinates".equals(key)) { lines = new ArrayList<>(); }
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
			coordinates.add(coordinate(ordinates));
			ordinates = null;
		} else if (coordinates != null) {
			lines.add(coordinates(coordinates));
			coordinates = null;
		}

		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		if (lines != null) {
			LineString[] lineStrings = new LineString[lines.size()];
			for (int i = 0; i < lines.size(); i++) { lineStrings[i] = factory.createLineString(lines.get(i)); }
			value = factory.createMultiLineString(lineStrings);
			lines = null;
		}
		return true;
	}
}
