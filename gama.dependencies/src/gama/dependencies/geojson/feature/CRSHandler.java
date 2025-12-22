/*******************************************************************************************************
 *
 * CRSHandler.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson.feature;

import java.io.IOException;

import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.json.simple.parser.ParseException;

import gama.dependencies.geojson.HandlerBase;
import gama.dependencies.geojson.IContentHandler;

/**
 * The Class CRSHandler.
 */
public class CRSHandler extends HandlerBase implements IContentHandler<CoordinateReferenceSystem> {

	/** The crs. */
	CoordinateReferenceSystem crs;

	/** The state. */
	int state = 0;

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
		if ("properties".equals(key)) {
			state = 1;
		} else if (("name".equals(key) || "code".equals(key)) && state == 1) { state = 2; }
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
		if (state == 2) {
			try {
				try {
					crs = CRS.decode(value.toString());
				} catch (NoSuchAuthorityCodeException e) {
					// try pending on EPSG
					try {
						crs = CRS.decode("EPSG:" + value.toString());
					} catch (Exception e1) {
						// throw the original
						throw e;
					}
				}
			} catch (Exception e) {
				throw (IOException) new IOException("Error parsing " + value + " as crs id").initCause(e);
			}
			state = -1;
		}

		return true;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public CoordinateReferenceSystem getValue() { return crs; }
}
