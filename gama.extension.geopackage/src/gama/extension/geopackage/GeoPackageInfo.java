/*******************************************************************************************************
 *
 * GeoPackageInfo.java, in gama.extension.geopackage, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.geopackage;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.utils.StringUtils;
import gama.api.utils.files.AbstractFileMetaData;
import gama.api.utils.files.IGamaFileMetaData;
import gama.dev.DEBUG;

/**
 * Cached navigator metadata for GeoPackage feature layers.
 */
public class GeoPackageInfo extends AbstractFileMetaData {

	private int layerCount;
	private String layerName;
	private int featureCount;
	private String crs = "Unknown CRS";
	private double width;
	private double height;

	public GeoPackageInfo(final IFile file) {
		super(file);
		read(new File(URI.create(file.getLocationURI().toString())));
	}

	public GeoPackageInfo(final String properties) {
		super(properties);
		if (FAILED.equals(properties)) return;
		final String[] values = split(properties);
		if (values.length >= 7) {
			layerCount = Integer.parseInt(values[1]);
			layerName = values[2];
			featureCount = Integer.parseInt(values[3]);
			crs = values[4];
			width = Double.parseDouble(values[5]);
			height = Double.parseDouble(values[6]);
		}
	}

	static {
		org.geotools.util.factory.GeoTools.addClassLoader(org.geotools.geopkg.GeoPkgDataStoreFactory.class.getClassLoader());
	}

	private void read(final File file) {
		DataStore store = null;
		try {
			store = new org.geotools.geopkg.GeoPkgDataStoreFactory().createDataStore(java.util.Map.of("dbtype", "geopkg", "database", file));
			if (store == null) throw new IOException("No GeoPackage data store is available");
			final String[] layers = store.getTypeNames();
			layerCount = layers.length;
			if (layerCount == 0) return;
			layerName = layers[0];
			final var source = store.getFeatureSource(layerName);
			featureCount = source.getCount(new Query(layerName));
			if (featureCount < 0) { featureCount = source.getFeatures().size(); }
			final ReferencedEnvelope bounds = source.getBounds();
			width = bounds.getWidth();
			height = bounds.getHeight();
			final var coordinateReferenceSystem = source.getSchema().getCoordinateReferenceSystem();
			if (coordinateReferenceSystem != null) {
				final String code = CRS.toSRS(coordinateReferenceSystem);
				if (code != null) crs = code;
			}
		} catch (final Exception e) {
			setFailed(true);
			DEBUG.ERR("Unable to read GeoPackage metadata for " + file + ": " + e.getMessage());
		} finally {
			if (store != null) store.dispose();
		}
	}

	@Override
	public void appendSuffix(final StringBuilder suffix) {
		if (layerCount == 0) {
			suffix.append("No feature layers");
			return;
		}
		suffix.append(featureCount).append(featureCount == 1 ? " feature" : " features");
		suffix.append(IGamaFileMetaData.SUFFIX_DEL).append(layerCount);
		suffix.append(layerCount == 1 ? " layer" : " layers");
		suffix.append(IGamaFileMetaData.SUFFIX_DEL).append(crs);
		suffix.append(IGamaFileMetaData.SUFFIX_DEL).append(Math.round(width)).append(" x ")
				.append(Math.round(height));
	}

	@Override
	public GamlRegularDocumentation getDocumentation() {
		final GamlRegularDocumentation documentation = new GamlRegularDocumentation();
		documentation.append("GeoPackage").append(StringUtils.LN);
		documentation.append(layerCount + (layerCount == 1 ? " feature layer" : " feature layers"))
				.append(StringUtils.LN);
		if (layerName != null) {
			documentation.append("First layer: ").append(layerName).append(StringUtils.LN);
			documentation.append(featureCount + (featureCount == 1 ? " feature" : " features")).append(StringUtils.LN);
			documentation.append("Dimensions: ").append(Math.round(width) + " x " + Math.round(height))
					.append(StringUtils.LN);
			documentation.append("Coordinate Reference System: ").append(crs).append(StringUtils.LN);
		}
		return documentation;
	}

	@Override
	public String toPropertyString() {
		if (hasFailed()) return super.toPropertyString();
		return String.join(DELIMITER, super.toPropertyString(), String.valueOf(layerCount),
				layerName == null ? "" : layerName, String.valueOf(featureCount), crs, String.valueOf(width),
				String.valueOf(height));
	}
}
