/*******************************************************************************************************
 *
 * GeometryHandlerBase.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.geom;

import static gama.dependencies.geojson.GeoJSONUtil.addOrdinate;
import static gama.dependencies.geojson.GeoJSONUtil.createCoordinate;
import static gama.dependencies.geojson.GeoJSONUtil.createCoordinates;

import java.io.IOException;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import gama.dependencies.geojson.HandlerBase;
import gama.dependencies.geojson.IContentHandler;

/**
 * The Class GeometryHandlerBase.
 *
 * @param <G>
 *            the generic type
 */
public class GeometryHandlerBase<G extends Geometry> extends HandlerBase implements IContentHandler<G> {

	/** The factory. */
	protected GeometryFactory factory;

	/** The ordinates. */
	protected List<Object> ordinates;

	/** The value. */
	protected G value;

	/**
	 * Instantiates a new geometry handler base.
	 *
	 * @param factory
	 *            the factory
	 */
	public GeometryHandlerBase(final GeometryFactory factory) {
		this.factory = factory;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public G getValue() { return value; }

	/**
	 * Coordinate.
	 *
	 * @param ordinates
	 *            the ordinates
	 * @return the coordinate
	 * @throws ParseException
	 *             the parse exception
	 */
	protected Coordinate coordinate(final List ordinates) throws ParseException {
		return createCoordinate(ordinates);
	}

	/**
	 * Coordinates.
	 *
	 * @param coordinates
	 *            the coordinates
	 * @return the coordinate[]
	 */
	protected Coordinate[] coordinates(final List<Coordinate> coordinates) {
		return createCoordinates(coordinates);
	}

	/**
	 * Primitive.
	 *
	 * @param value
	 *            the value
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean primitive(final Object value) throws ParseException, IOException {
		// we could be receiving the "type" attribute value
		if (value instanceof Number) return addOrdinate(ordinates, value);
		return true;
	}
}
