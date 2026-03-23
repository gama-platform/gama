/*******************************************************************************************************
 *
 * ShapeInfo.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import gama.api.GAMA;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.types.Types;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.utils.StringUtils;
import gama.api.utils.files.AbstractFileMetaData;
import gama.core.topology.gis.GamaCRS;
import gama.core.topology.gis.ProjectionFactory;
import gama.dev.DEBUG;

/**
 * The Class ShapeInfo.
 */
public class ShapeInfo extends AbstractFileMetaData {

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
	 * Instantiates a new shape info.
	 *
	 * @param file
	 *            the file
	 * @throws MalformedURLException
	 *             the malformed URL exception
	 */
	public ShapeInfo(final IFile file) throws MalformedURLException {
		super(file);
		createFrom(file.getLocationURI().toURL());
	}

	/**
	 * Instantiates a new shape info.
	 *
	 * @param url
	 *            the url
	 * @param modificationStamp
	 *            the modification stamp
	 */
	private void createFrom(final URL url) {
		FileDataStore store = null;
		ReferencedEnvelope env = new ReferencedEnvelope();
		int number = 0;
		try {
			store = GamaShapeFile.getDataStore(url);

			final SimpleFeatureSource source = store.getFeatureSource();
			final SimpleFeatureCollection features = source.getFeatures();
			try {
				crs = new GamaCRS(source.getInfo().getCRS());
			} catch (final Exception e) {
				DEBUG.ERR("Ignored exception in ShapeInfo getCRS:" + e.getMessage());
			}
			env = source.getBounds();
			if (crs == null || crs.isNull()) {
				crs = ProjectionFactory.manageGoogleCRS(url);
				if (crs != null && !crs.isNull()) { env = new ReferencedEnvelope(env, crs.getCRS()); }
			}

			if (crs != null && !crs.isNull()) {
				try {
					env = env.transform(new ProjectionFactory().getTargetCRS(GAMA.getRuntimeScope()).getCRS(), true);
				} catch (final Exception e) {
					store.dispose();
					throw e;
				}
			}
			try {
				number = features.size();
			} catch (final Exception e) {

				store.dispose();
				DEBUG.ERR("Error in loading shapefile: " + e.getMessage());
			}
			final java.util.List<AttributeDescriptor> att_list = store.getSchema().getAttributeDescriptors();
			for (final AttributeDescriptor desc : att_list) {
				String type;
				if (desc.getType() instanceof GeometryType) {
					type = "geometry";
				} else {
					type = Types.get(desc.getType().getBinding()).toString();
				}
				attributes.put(desc.getName().getLocalPart(), type);
			}
		} catch (final Exception e) {
			DEBUG.ERR("Error in reading metadata of " + url);
			e.printStackTrace();

		} finally {
			width = env.getWidth();
			height = env.getHeight();
			itemNumber = number;
			if (store != null) { store.dispose(); }
		}

	}

	/**
	 * Gets the crs.
	 *
	 * @return the crs
	 */
	public ICoordinateReferenceSystem getCRS() { return crs; }

	/**
	 * Instantiates a new shape info.
	 *
	 * @param propertiesString
	 *            the properties string
	 */
	public ShapeInfo(final String propertiesString) {
		super(propertiesString);
		final String[] segments = split(propertiesString);
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
		crs = new GamaCRS(theCRS);
		width = Double.parseDouble(segments[3]);
		height = Double.parseDouble(segments[4]);
		if (segments.length > 5) {
			final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], SUB_DELIMITER);
			final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], SUB_DELIMITER);
			for (int i = 0; i < names.length; i++) { attributes.put(names[i], types[i]); }
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
		sb.append(itemNumber).append(" object");
		if (itemNumber > 1) { sb.append("s"); }
		sb.append(SUFFIX_DEL);
		sb.append(crs == null ? "Unknown CRS" : crs.getCode());
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
		sb.append("Shapefile").append(StringUtils.LN);
		sb.append(String.valueOf(itemNumber)).append(" objects").append(StringUtils.LN);
		sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m").append(StringUtils.LN);
		sb.append("Coordinate Reference System: ").append(crs == null || crs.isNull() ? "No CRS" : crs.getCode())
				.append(StringUtils.LN);
		if (!attributes.isEmpty()) {
			sb.append("Attributes: ").append(StringUtils.LN);
			attributes.forEach((k, v) -> sb.append("<li>").append(k).append(" (" + v + ")").append("</li>"));
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
		// See Issue #1603: .toWKT() && pa can sometimes cause problem with
		// some CRS.
		String system = crs == null || crs.isNull() ? "Unknown projection" : crs.toString();
		try {
			CRS.parseWKT(system);
		} catch (final Exception e) {
			String srs = CRS.toSRS(crs == null ? null : crs.getCRS());
			if (srs == null && crs != null && !crs.isNull()) { srs = crs.getCRS().getName().getCode(); }
			system = "Unknown projection " + srs;
		}
		final String attributeNames = String.join(SUB_DELIMITER, attributes.keySet());
		final String types = String.join(SUB_DELIMITER, attributes.values());
		final String[] toSave = { super.toPropertyString(), String.valueOf(itemNumber), system, String.valueOf(width),
				String.valueOf(height), attributeNames, types };
		return String.join(DELIMITER, toSave);
	}

}
