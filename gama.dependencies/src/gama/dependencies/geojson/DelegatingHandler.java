/*******************************************************************************************************
 *
 * DelegatingHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

import gama.dependencies.geojson.feature.FeatureCollectionHandler;
import gama.dependencies.geojson.feature.FeatureHandler;
import gama.dependencies.geojson.geom.GeometryCollectionHandler;
import gama.dependencies.geojson.geom.LineHandler;
import gama.dependencies.geojson.geom.MultiLineHandler;
import gama.dependencies.geojson.geom.MultiPointHandler;
import gama.dependencies.geojson.geom.MultiPolygonHandler;
import gama.dependencies.geojson.geom.PointHandler;
import gama.dependencies.geojson.geom.PolygonHandler;

/**
 * The Class DelegatingHandler.
 *
 * @param <T>
 *            the generic type
 */
public abstract class DelegatingHandler<T> implements IContentHandler<T> {

	/** The handlers. */
	protected static HashMap<String, Class<? extends IContentHandler>> handlers = new HashMap<>();

	static {
		handlers.put("Point", PointHandler.class);
		handlers.put("LineString", LineHandler.class);
		handlers.put("Polygon", PolygonHandler.class);
		handlers.put("MultiPoint", MultiPointHandler.class);
		handlers.put("MultiLineString", MultiLineHandler.class);
		handlers.put("MultiPolygon", MultiPolygonHandler.class);
		handlers.put("GeometryCollection", GeometryCollectionHandler.class);

		handlers.put("Feature", FeatureHandler.class);
		handlers.put("FeatureCollection", FeatureCollectionHandler.class);
	}

	/** The null. */
	protected static NullHandler NULL = new NullHandler();

	/** The uninitialized. */
	protected static NullHandler UNINITIALIZED = new NullHandler();

	/** The null list. */
	protected static List<String> NULL_LIST = Collections.unmodifiableList(new ArrayList<>(0));

	/** The delegate. */
	protected ContentHandler delegate = NULL;

	/**
	 * Gets the delegate.
	 *
	 * @return the delegate
	 */
	public ContentHandler getDelegate() { return delegate; }

	@Override
	public void startJSON() throws ParseException, IOException {
		delegate.startJSON();
	}

	@Override
	public void endJSON() throws ParseException, IOException {
		delegate.endJSON();
	}

	@Override
	public boolean startObject() throws ParseException, IOException {
		return delegate.startObject();
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		return delegate.endObject();
	}

	@Override
	public boolean startObjectEntry(final String key) throws ParseException, IOException {
		return delegate.startObjectEntry(key);
	}

	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		return delegate.endObjectEntry();
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		return delegate.startArray();
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		return delegate.endArray();
	}

	@Override
	public boolean primitive(final Object value) throws ParseException, IOException {
		return delegate.primitive(value);
	}

	@Override
	@SuppressWarnings ("unchecked")
	public T getValue() {
		if (delegate instanceof IContentHandler) return (T) ((IContentHandler) delegate).getValue();
		return null;
	}

	/**
	 * Lookup delegate.
	 *
	 * @param type
	 *            the type
	 * @return the class<? extends content handler>
	 */
	protected Class<? extends ContentHandler> lookupDelegate(final String type) {
		return handlers.get(type);
	}

	/**
	 * Creates the delegate.
	 *
	 * @param clazz
	 *            the clazz
	 * @param args
	 *            the args
	 * @return the i content handler
	 */
	@SuppressWarnings ("unchecked")
	protected IContentHandler createDelegate(final Class clazz, final Object[] args) {
		try {
			if (args == null || args.length <= 0) return (IContentHandler) clazz.getDeclaredConstructor().newInstance();
			Class[] types = new Class[args.length];
			for (int i = 0; i < args.length; i++) { types[i] = args[i].getClass(); }

			return (IContentHandler) clazz.getConstructor(types).newInstance(args);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * The Class NullHandler.
	 */
	static class NullHandler implements ContentHandler {

		/**
		 * Start JSON.
		 *
		 * @throws ParseException
		 *             the parse exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		@Override
		public void startJSON() throws ParseException, IOException {}

		/**
		 * End JSON.
		 *
		 * @throws ParseException
		 *             the parse exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		@Override
		public void endJSON() throws ParseException, IOException {}

		/**
		 * End array.
		 *
		 * @return true, if successful
		 * @throws ParseException
		 *             the parse exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		@Override
		public boolean endArray() throws ParseException, IOException {
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
			return true;
		}

		/**
		 * Start array.
		 *
		 * @return true, if successful
		 * @throws ParseException
		 *             the parse exception
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		@Override
		public boolean startArray() throws ParseException, IOException {
			return true;
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
			return true;
		}
	}
}
