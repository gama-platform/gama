/*******************************************************************************************************
 *
 * GeometryCollectionHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

import gama.dependencies.geojson.DelegatingHandler;
import gama.dependencies.geojson.RecordingHandler;

/**
 * The Class GeometryCollectionHandler.
 */
public class GeometryCollectionHandler extends DelegatingHandler<GeometryCollection> {

	/** The factory. */
	GeometryFactory factory;

	/** The geoms. */
	List<Geometry> geoms;

	/** The value. */
	GeometryCollection value;

	/** The proxy. */
	RecordingHandler proxy;

	/** The delegate class. */
	Class delegateClass;

	/**
	 * Instantiates a new geometry collection handler.
	 *
	 * @param factory
	 *            the factory
	 */
	public GeometryCollectionHandler(final GeometryFactory factory) {
		this.factory = factory;
	}

	/**
	 * Start object.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean startObject() throws ParseException, IOException {
		if (geoms != null) {
			// means start of a member geometry object
			delegate = UNINITIALIZED;
		}
		return true;
	}

	/**
	 * End object.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean endObject() throws ParseException, IOException {
		if (delegate instanceof GeometryHandlerBase) {
			// end of a member geometry
			delegate.endObject();
			Geometry geomObject = ((GeometryHandlerBase) delegate).getValue();
			if (geomObject != null) { geoms.add(geomObject); }
			delegate = NULL;
		} else {
			Geometry[] geometries = geoms.toArray(new Geometry[geoms.size()]);
			value = factory.createGeometryCollection(geometries);
			geoms = null;
		}

		return true;
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
		if ("coordinates".equals(key) && delegate == UNINITIALIZED) {
			/*
			 * case of specifying coordinates before the actual geometry type. create a proxy handler that will simply
			 * track calls until the type is actually specified
			 */
			proxy = new RecordingHandler();
			delegate = proxy;
			return super.startObjectEntry(key);
		}
		if ("type".equals(key) && delegate == proxy) {
			delegate = UNINITIALIZED;
		} else if ("geometries".equals(key)) {
			geoms = new ArrayList<>();
		} else if (geoms != null) { super.startObjectEntry(key); }

		return true;
	}

	/**
	 * End object entry.
	 *
	 * @return true, if successful
	 * @throws ParseException
	 *             the parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		if (delegateClass != null) {
			delegate = createDelegate(delegateClass, new Object[] { factory });
			delegateClass = null;
		}
		return true;
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
		/*
		 * handle special case of "type" belonging to one of the collection's geometries being found after "coordinates"
		 * for that geometry.
		 */
		if (geoms == null || !(value instanceof String) || delegate != UNINITIALIZED) return super.primitive(value);
		delegateClass = lookupDelegate(value.toString());
		if (proxy != null) {
			delegate = createDelegate(delegateClass, new Object[] { factory });
			delegateClass = null;
			proxy.replay(delegate);
			proxy = null;
		}

		return true;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public GeometryCollection getValue() { return value; }
}
