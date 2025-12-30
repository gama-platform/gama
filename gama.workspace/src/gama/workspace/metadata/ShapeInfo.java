/*******************************************************************************************************
 *
 * ShapeInfo.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import gama.core.metamodel.topology.projection.ProjectionFactory;
import gama.core.runtime.IScope;
import gama.core.util.file.GamaShapeFile;
import gama.dev.DEBUG;
import gama.gaml.interfaces.IGamlDescription.Doc;
import gama.gaml.interfaces.IGamlDescription.RegularDoc;
import gama.gaml.operators.Strings;
import gama.gaml.types.Types;

/**
 * The Class ShapeInfo.
 */
public class ShapeInfo extends GamaFileMetaData {

	/** The item number. */
	final int itemNumber;

	/** The crs. */
	final CoordinateReferenceSystem crs;

	/** The width. */
	final double width;

	/** The height. */
	final double height;

	/** The attributes. */
	final Map<String, String> attributes = new LinkedHashMap<>();

	/**
	 * Instantiates a new shape info.
	 *
	 * @param scope
	 *            the scope
	 * @param url
	 *            the url
	 * @param modificationStamp
	 *            the modification stamp
	 */
	public ShapeInfo(final IScope scope, final URL url, final long modificationStamp) {
		super(modificationStamp);
		FileDataStore store = null;
		ReferencedEnvelope env = new ReferencedEnvelope();
		CoordinateReferenceSystem crs1 = null;
		int number = 0;
		try {
			store = GamaShapeFile.getDataStore(url);

			final SimpleFeatureSource source = store.getFeatureSource();
			final SimpleFeatureCollection features = source.getFeatures();
			try {
				crs1 = source.getInfo().getCRS();

			} catch (final Exception e) {
				DEBUG.ERR("Ignored exception in ShapeInfo getCRS:" + e.getMessage());
			}
			env = source.getBounds();
			if (crs1 == null) {
				crs1 = ProjectionFactory.manageGoogleCRS(url);
				if (crs1 != null) { env = new ReferencedEnvelope(env, crs1); }
			}

			if (crs1 != null) {
				try {
					env = env.transform(new ProjectionFactory().getTargetCRS(scope), true);
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
			this.crs = crs1;
			if (store != null) { store.dispose(); }
		}

	}

	/**
	 * Gets the crs.
	 *
	 * @return the crs
	 */
	public CoordinateReferenceSystem getCRS() { return crs; }

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
		crs = theCRS;
		width = Double.parseDouble(segments[3]);
		height = Double.parseDouble(segments[4]);
		if (segments.length > 5) {
			final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], SUB_DELIMITER);
			final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], SUB_DELIMITER);
			for (int i = 0; i < names.length; i++) { attributes.put(names[i], types[i]); }
		}
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		sb.append(itemNumber).append(" object");
		if (itemNumber > 1) { sb.append("s"); }
		sb.append(SUFFIX_DEL);
		sb.append(crs == null ? "Unknown CRS" : crs.getName().getCode());
		sb.append(SUFFIX_DEL);
		sb.append(Math.round(width)).append("m x ");
		sb.append(Math.round(height)).append("m");
	}

	@Override
	public Doc getDocumentation() {
		final RegularDoc sb = new RegularDoc();
		sb.append("Shapefile").append(Strings.LN);
		sb.append(String.valueOf(itemNumber)).append(" objects").append(Strings.LN);
		sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m").append(Strings.LN);
		sb.append("Coordinate Reference System: ").append(crs == null ? "Unknown CRS" : crs.getName().getCode())
				.append(Strings.LN);
		if (!attributes.isEmpty()) {
			sb.append("Attributes: ").append(Strings.LN);
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

	@Override
	public String toPropertyString() {
		// See Issue #1603: .toWKT() && pa can sometimes cause problem with
		// some CRS.
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
		final String[] toSave = { super.toPropertyString(), String.valueOf(itemNumber), system,
				String.valueOf(width), String.valueOf(height), attributeNames, types };
		return String.join(DELIMITER, toSave);
	}

}
