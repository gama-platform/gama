/*******************************************************************************************************
 *
 * MultiPointHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
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
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * The Class MultiPointHandler.
 */
public class MultiPointHandler extends GeometryHandlerBase<MultiPoint> {

	/** The coordinates. */
	List<Coordinate> coordinates;

	/**
	 * Instantiates a new multi point handler.
	 *
	 * @param factory
	 *            the factory
	 */
	public MultiPointHandler(final GeometryFactory factory) {
		super(factory);
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		if ("coordinates".equals(key)) { coordinates = new ArrayList<>(); }
		return true;
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		if (ordinates == null) { ordinates = new ArrayList<>(); }

		return true;
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		if (ordinates != null) {
			coordinates.add(coordinate(ordinates));
			ordinates = null;
		}
		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		if (coordinates != null) {
			value = factory.createMultiPoint(new CoordinateArraySequence(coordinates(coordinates)));
			coordinates = null;
		}
		return true;
	}
}
