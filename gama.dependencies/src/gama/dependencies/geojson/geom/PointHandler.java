/*******************************************************************************************************
 *
 * PointHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
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

import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * The Class PointHandler.
 */
public class PointHandler extends GeometryHandlerBase<Point> {

	/**
	 * Instantiates a new point handler.
	 *
	 * @param factory
	 *            the factory
	 */
	public PointHandler(final GeometryFactory factory) {
		super(factory);
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		if ("coordinates".equals(key)) { ordinates = new ArrayList<>(); }

		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		if (ordinates != null) {
			Coordinate c = coordinate(ordinates);
			value = factory.createPoint(c);
			ordinates = null;
		}
		return true;
	}
}
