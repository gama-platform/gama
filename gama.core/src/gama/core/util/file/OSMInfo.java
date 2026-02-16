/*******************************************************************************************************
 *
 * OSMInfo.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import gama.api.GAMA;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.types.geometry.IShape;
import gama.api.utils.StringUtils;
import gama.api.utils.files.AbstractFileMetaData;
import gama.core.topology.gis.GamaCRS;
import gama.dev.DEBUG;

/**
 * The Class OSMInfo.
 */
public class OSMInfo extends AbstractFileMetaData {

	/** The item number. */
	int itemNumber;

	/** The crs. */
	ICoordinateReferenceSystem crs;

	/** The width. */
	double width;

	/** The height. */
	double height;

	/** The attributes. */
	Map<String, String> attributes = new LinkedHashMap<>();

	/**
	 * Instantiates a new OSM info.
	 *
	 * @param file
	 *            the file
	 * @throws MalformedURLException
	 */
	public OSMInfo(final IFile file) throws MalformedURLException {
		super(file);
		createFrom(file.getLocationURI().toURL());
	}

	/**
	 * Instantiates a new OSM info.
	 *
	 * @param url
	 *            the url
	 */
	private void createFrom(final URL url) {
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
		}

	}

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
				crs = new GamaCRS(CRS.parseWKT(crsString));
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

	/**
	 * Append suffix.
	 *
	 * @param sb
	 *            the sb
	 */
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

	/**
	 * Gets the documentation.
	 *
	 * @return the documentation
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		final GamlRegularDocumentation sb = new GamlRegularDocumentation();
		if (hasFailed()) {
			sb.append("Unreadable OSM file").append(StringUtils.LN)
					.append("Decompress the file to an .osm file and retry");
		} else {
			sb.append("OSM file").append(StringUtils.LN);
			sb.append(String.valueOf(itemNumber)).append(" objects").append(StringUtils.LN);
			sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m")
					.append(StringUtils.LN);
			sb.append("Coordinate Reference System: ").append(crs == null || crs.isNull() ? "No CRS" : crs.getCode())
					.append(StringUtils.LN);
			if (!attributes.isEmpty()) {
				sb.append("Attributes: ").append(StringUtils.LN);
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

	/**
	 * To property string.
	 *
	 * @return the string
	 */
	@Override
	public String toPropertyString() {
		final String attributeNames = String.join(SUB_DELIMITER, attributes.keySet());
		final String types = String.join(SUB_DELIMITER, attributes.values());
		final String[] toSave =
				{ super.toPropertyString(), String.valueOf(itemNumber), crs == null ? "null" : crs.toString(),
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
