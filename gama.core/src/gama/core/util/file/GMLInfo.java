/*******************************************************************************************************
 *
 * GMLInfo.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.wfs.GML;
import org.geotools.wfs.GML.Version;
import org.xml.sax.SAXException;

import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.types.Types;
import gama.api.utils.StringUtils;
import gama.api.utils.files.AbstractFileMetaData;
import gama.dev.DEBUG;

/**
 * The Class GMLInfo.
 */
public class GMLInfo extends AbstractFileMetaData {

	/** The item number. */
	int itemNumber;

	/** The crs. */
	CoordinateReferenceSystem crs;

	/** The width. */
	double width;

	/** The height. */
	double height;

	/** The attributes. */
	Map<String, String> attributes = new LinkedHashMap<>();

	/**
	 * Instantiates a new GML info.
	 *
	 * @param file
	 *            the file
	 */
	public GMLInfo(final IFile file) {
		super(file);
		try (var is = file.getContents()) {
			createFrom(file.getName(), is);
		} catch (Exception e) {
			DEBUG.ERR("Error reading GML metadata for " + file.getName() + ": " + e.getMessage());
		}
	}

	/**
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 *             Instantiates a new GML info.
	 *
	 * @param modificationStamp
	 *            the modification stamp
	 * @param inputStream
	 *            the input stream
	 */
	public void createFrom(final String name, final InputStream inputStream)
			throws IOException, SAXException, ParserConfigurationException {
		ReferencedEnvelope env = new ReferencedEnvelope();
		CoordinateReferenceSystem crs1 = null;
		int number = 0;
		if (inputStream != null) {
			final GML gml = new GML(Version.GML3);
			final SimpleFeatureCollection features = gml.decodeFeatureCollection(inputStream);

			try {
				crs1 = features.getSchema().getCoordinateReferenceSystem();
			} catch (final Exception e) {
				DEBUG.ERR("Ignored exception in GMLInfo getCRS:" + e.getMessage());
			}
			env = features.getBounds();

			try {
				number = features.size();
			} catch (final Exception e) {
				DEBUG.ERR("Error in loading GML file: " + e.getMessage());
			}

			final java.util.List<AttributeDescriptor> att_list = features.getSchema().getAttributeDescriptors();
			for (final AttributeDescriptor desc : att_list) {
				String type;
				if (desc.getType() instanceof GeometryType) {
					type = "geometry";
				} else {
					type = Types.get(desc.getType().getBinding()).toString();
				}
				attributes.put(desc.getName().getLocalPart(), type);
			}
		}
		width = env.getWidth();
		height = env.getHeight();
		itemNumber = number;
		this.crs = crs1;
	}

	/**
	 * Instantiates a new GML info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public GMLInfo(final String propertyString) {
		super(propertyString);
		final String[] segments = split(propertyString);
		itemNumber = Integer.parseInt(segments[1]);
		final String crsString = segments[2];
		CoordinateReferenceSystem theCRS;
		if ("null".equals(crsString) || crsString.startsWith("Unknown")) {
			theCRS = null;
		} else {
			try {
				theCRS = CRS.parseWKT(crsString);
			} catch (final Exception e) {
				theCRS = null;
			}
		}
		crs = theCRS;
		width = Double.parseDouble(segments[3]);
		height = Double.parseDouble(segments[4]);
		if (segments.length > 5) {
			final String[] names = split(segments[5], SUB_DELIMITER);
			final String[] types = split(segments[6], SUB_DELIMITER);
			for (int i = 0; i < names.length; i++) { attributes.put(names[i], types[i]); }
		}
	}

	/**
	 * Split.
	 *
	 * @param s
	 *            the s
	 * @param delimiter
	 *            the delimiter
	 * @return the string[]
	 */
	private String[] split(final String s, final String delimiter) {
		return org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens(s, delimiter);
	}

	/**
	 * Append suffix.
	 *
	 * @param sb
	 *            the sb
	 */
	@Override
	public void appendSuffix(final StringBuilder sb) {
		sb.append(itemNumber).append(" object");
		if (itemNumber > 1) { sb.append("s"); }
		sb.append(SUFFIX_DEL);
		sb.append(crs == null ? "Unknown CRS" : crs.getName().getCode());
		sb.append(SUFFIX_DEL);
		sb.append(Math.round(width)).append("m x ");
		sb.append(Math.round(height)).append("m");
		sb.append(SUFFIX_DEL).append("GML");
	}

	/**
	 * Gets the documentation.
	 *
	 * @return the documentation
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		final GamlRegularDocumentation sb = new GamlRegularDocumentation();
		sb.append("GML File").append(StringUtils.LN);
		sb.append(String.valueOf(itemNumber)).append(" objects").append(StringUtils.LN);
		sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m").append(StringUtils.LN);
		sb.append("Coordinate Reference System: ").append(crs == null ? "Unknown CRS" : crs.getName().getCode())
				.append(StringUtils.LN);
		if (!attributes.isEmpty()) {
			sb.append("Attributes: ").append(StringUtils.LN);
			attributes.forEach((k, v) -> sb.append("<li>").append(k).append(" (" + v + ")").append("</li>"));
		}
		return sb;
	}

	/**
	 * To property string.
	 *
	 * @return the string
	 */
	@Override
	public String toPropertyString() {
		String system = crs == null ? "Unknown projection" : crs.toWKT();
		try {
			CRS.parseWKT(system);
		} catch (final Exception e) {
			String srs = CRS.toSRS(crs);
			if (srs == null && crs != null) { srs = crs.getName().getCode(); }
			system = "Unknown projection " + srs;
		}
		final String attributeNames = String.join(SUB_DELIMITER, attributes.keySet());
		final String types = String.join(SUB_DELIMITER, attributes.values());
		final String[] toSave = { super.toPropertyString(), String.valueOf(itemNumber), system, String.valueOf(width),
				String.valueOf(height), attributeNames, types };
		return String.join(DELIMITER, toSave);
	}

	/**
	 * Gets the item number.
	 *
	 * @return the item number
	 */
	public int getItemNumber() { return itemNumber; }

	/**
	 * Gets the crs.
	 *
	 * @return the crs
	 */
	public CoordinateReferenceSystem getCrs() { return crs; }

	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public double getWidth() { return width; }

	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public double getHeight() { return height; }

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() { return attributes; }

}