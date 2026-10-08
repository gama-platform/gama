/*******************************************************************************************************
 *
 * GeometryHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.geom;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import gama.dependencies.geojson.DelegatingHandler;
import gama.dependencies.geojson.RecordingHandler;

/**
 * The Class GeometryHandler.
 */
public class GeometryHandler extends DelegatingHandler<Geometry> {

	/** The factory. */
	GeometryFactory factory;

	/** The proxy. */
	RecordingHandler proxy;

	/**
	 * Instantiates a new geometry handler.
	 *
	 * @param factory
	 *            the factory
	 */
	public GeometryHandler(final GeometryFactory factory) {
		this.factory = factory;
	}

	/**
	 * Start object entry.
	 *
	 * @param key
	 *            the key
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		if ("type".equals(key) && (delegate == NULL || delegate == proxy)) {
			delegate = UNINITIALIZED;
			return true;
		}
		if ("coordinates".equals(key) && delegate == NULL) {
			// case of specifying coordinates before the actual geometry type, create a proxy
			// handler that will simply track calls until the type is actually specified
			proxy = new RecordingHandler();
			delegate = proxy;
			return super.startObjectEntry(key);
		}
		if ("geometries".equals(key) && delegate == NULL) {
			// geometry collection without type property first
			delegate = new GeometryCollectionHandler(factory);
		}
		return super.startObjectEntry(key);
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
		if (delegate != UNINITIALIZED) return super.primitive(value);
		delegate = createDelegate(lookupDelegate(value.toString()), new Object[] { factory });
		if (proxy != null) {
			proxy.replay(delegate);
			proxy = null;
		}
		return true;
	}
}
