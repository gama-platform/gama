/*******************************************************************************************************
 *
 * GeoJSON.java, in gama.dependencies, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.dependencies.geojson;

import java.io.IOException;

import org.geotools.api.feature.Feature;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.FeatureCollection;
import org.locationtech.jts.geom.Geometry;

import gama.dependencies.geojson.feature.FeatureJSON;
import gama.dependencies.geojson.geom.GeometryJSON;

/**
 * The Class GeoJSON.
 */
public class GeoJSON {

	/** The gjson. */
	static GeometryJSON gjson = new GeometryJSON();

	/** The fjson. */
	static FeatureJSON fjson = new FeatureJSON();

	/**
	 * Read.
	 *
	 * @param input
	 *            the input
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Object read(final Object input) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Write.
	 *
	 * @param obj
	 *            the obj
	 * @param output
	 *            the output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void write(final Object obj, final Object output) throws IOException {
		if (obj instanceof Geometry) {
			gjson.write((Geometry) obj, output);
		} else if (obj instanceof Feature || obj instanceof FeatureCollection
				|| obj instanceof CoordinateReferenceSystem) {

			if (obj instanceof SimpleFeature) {
				fjson.writeFeature((SimpleFeature) obj, output);
			} else if (obj instanceof FeatureCollection) {
				fjson.writeFeatureCollection((FeatureCollection) obj, output);
			} else if (obj instanceof CoordinateReferenceSystem) {
				fjson.writeCRS((CoordinateReferenceSystem) obj, output);
			} else
				throw new IllegalArgumentException("Unable able to encode object of type " + obj.getClass());
		}
	}
}
