/*******************************************************************************************************
 *
 * OSMInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.util.file.GamaOsmFile;
import gama.dev.DEBUG;
import gama.gaml.interfaces.IGamlDescription.Doc;
import gama.gaml.interfaces.IGamlDescription.RegularDoc;
import gama.gaml.operators.Strings;

/**
 * The Class OSMInfo.
 */
public class OSMInfo extends GamaFileMetaData {

	/** The item number. */
	int itemNumber;

	/** The crs. */
	CoordinateReferenceSystem crs;

	/** The width. */
	final double width;

	/** The height. */
	final double height;

	/** The attributes. */
	final Map<String, String> attributes = new LinkedHashMap<>();

	/**
	 * Instantiates a new OSM info.
	 *
	 * @param url
	 *            the url
	 * @param modificationStamp
	 *            the modification stamp
	 */
	public OSMInfo(final URL url, final long modificationStamp) {
		super(modificationStamp);
		CoordinateReferenceSystem crs = null;
		ReferencedEnvelope env2 = new ReferencedEnvelope();

		int number = 0;
		try {
			final File f = new File(url.toURI());
			final GamaOsmFile osmfile = new GamaOsmFile(null, f.getAbsolutePath());
			attributes.putAll(osmfile.getOSMAttributes(GAMA.getRuntimeScope()));

			final SimpleFeatureType TYPE = DataUtilities.createType("geometries", "geom:LineString");
			final ArrayList<SimpleFeature> list = new ArrayList<>();
			for (final IShape shape : osmfile.iterable(null)) {
				list.add(SimpleFeatureBuilder.build(TYPE, new Object[] { shape.getInnerGeometry() }, null));
			}
			final SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, list);
			final SimpleFeatureSource featureSource = DataUtilities.source(collection);

			env2 = featureSource.getBounds();
			number = osmfile.getNbObjects();
			crs = osmfile.getOwnCRS(null);
		} catch (final Exception e) {
			DEBUG.ERR("Error in reading metadata of " + url);
			setFailed(true);

		} finally {

			// approximation of the width and height in meters.
			width = env2 != null ? env2.getWidth() * (Math.PI / 180) * 6378137 : 0;
			height = env2 != null ? env2.getHeight() * (Math.PI / 180) * 6378137 : 0;
			itemNumber = number;
			this.crs = crs;
		}

	}

	/**
	 * Gets the crs.
	 *
	 * @return the crs
	 */
	public CoordinateReferenceSystem getCRS() { return crs; }

	/**
	 * Instantiates a new OSM info.
	 *
	 * @param propertiesString
	 *            the properties string
	 * @throws NoSuchAuthorityCodeException
	 *             the no such authority code exception
	 * @throws FactoryException
	 *             the factory exception
	 */
	public OSMInfo(final String propertiesString) throws NoSuchAuthorityCodeException, FactoryException {
		super(propertiesString);
		if (!hasFailed()) {
			final String[] segments = split(propertiesString);
			itemNumber = Integer.parseInt(segments[1]);
			final String crsString = segments[2];
			if ("null".equals(crsString)) {
				crs = null;
			} else {
				crs = CRS.parseWKT(crsString);
			}
			width = Double.parseDouble(segments[3]);
			height = Double.parseDouble(segments[4]);
			if (segments.length > 5) {
				final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], SUB_DELIMITER);
				final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], SUB_DELIMITER);
				for (int i = 0; i < names.length; i++) { attributes.put(names[i], types[i]); }
			}
		} else {
			itemNumber = 0;
			width = 0.0;
			height = 0.0;
			crs = null;
		}
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		if (hasFailed()) {
			sb.append("error: decompress the file to a .osm file");
			return;
		}
		sb.append(itemNumber).append(" object");
		if (itemNumber > 1) { sb.append("s"); }
		sb.append(SUFFIX_DEL);
		sb.append(Math.round(width)).append("m x ");
		sb.append(Math.round(height)).append("m");
	}

	@Override
	public Doc getDocumentation() {
		final RegularDoc sb = new RegularDoc();
		if (hasFailed()) {
			sb.append("Unreadable OSM file").append(Strings.LN).append("Decompress the file to an .osm file and retry");
		} else {
			sb.append("OSM file").append(Strings.LN);
			sb.append(String.valueOf(itemNumber)).append(" objects").append(Strings.LN);
			sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m").append(Strings.LN);
			sb.append("Coordinate Reference System: ").append(crs == null ? "No CRS" : crs.getName().getCode())
					.append(Strings.LN);
			if (!attributes.isEmpty()) {
				sb.append("Attributes: ").append(Strings.LN);
				attributes.forEach((k, v) -> sb.append("<li>").append(k).append(" (" + v + ")").append("</li>"));
			}
		}
		return sb;
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() { return attributes; }

	@Override
	public String toPropertyString() {
		final String attributeNames = String.join(SUB_DELIMITER, attributes.keySet());
		final String types = String.join(SUB_DELIMITER, attributes.values());
		final String[] toSave =
				{ super.toPropertyString(), String.valueOf(itemNumber), crs == null ? "null" : crs.toWKT(),
						String.valueOf(width), String.valueOf(height), attributeNames, types };
		return String.join(DELIMITER, toSave);
	}

	// Helper method to split string, as split() in GamaFileMetaData is protected and we are in a different package.
	// Wait, GamaFileMetaData is in gama.core.util.file.
	// OSMInfo is in gama.workspace.metadata.
	// So we cannot access protected members of GamaFileMetaData unless we are in the same package or subclass.
	// We are subclassing, so we can access protected members.

	// However, hasFailed is package private in GamaFileMetaData (based on previous read).
	// Let's check GamaFileMetaData again.
}
