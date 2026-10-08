/*******************************************************************************************************
 *
 * ArrayHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;

import gama.dependencies.geojson.HandlerBase;
import gama.dependencies.geojson.IContentHandler;

/**
 * The Class ArrayHandler.
 */
public class ArrayHandler extends HandlerBase implements IContentHandler<List<Object>> {

	/** The values. */
	List<Object> values;

	/** The list. */
	List<Object> list;

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
		values = new ArrayList<>();
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
		if (values != null) {
			values.add(value);
			return true;
		}
		return super.primitive(value);
	}

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
		list = values;
		values = null;
		return true;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public List<Object> getValue() { return list; }
}
