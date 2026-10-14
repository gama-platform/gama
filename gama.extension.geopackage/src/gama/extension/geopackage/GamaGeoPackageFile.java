/*******************************************************************************************************
 *
 * GamaGeoPackageFile.java, in gama.extension.geopackage, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.geopackage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.util.factory.Hints;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.test;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.core.util.file.GamaGisFile;

/**
 * Reads feature layers from GeoPackage files through GeoTools.
 */
@file (
		name = "geopackage",
		extensions = { "gpkg" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.GIS, IConcept.FILE },
		doc = @doc ("Represents a GeoPackage file. Files with multiple feature layers use the first layer by default; specify a layer name to select another."))
@test ("is_geopackage(\"features.gpkg\")")
@test ("is_geopackage(\"features.GPKG\")")
@test ("!is_geopackage(\"features.shp\")")
public class GamaGeoPackageFile extends GamaGisFile {

	static {
		// GeoTools discovers GeoPackage extensions through SPI, which needs this bundle's class loader in OSGi
		org.geotools.util.factory.GeoTools.addClassLoader(org.geotools.geopkg.GeoPkgDataStoreFactory.class.getClassLoader());
	}

	/** The layer name. */
	private final String layerName;

	/** The feature collection. */
	private SimpleFeatureCollection featureCollection;

	/**
	 * Creates a GeoPackage file using its first feature layer.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the GeoPackage path
	 */
	@doc (
			value = "Reads a GeoPackage (.gpkg) file, using its first feature layer.",
			examples = { @example (
					value = "file f <- geopackage_file(\"data.gpkg\");",
					isExecutable = false) })
	public GamaGeoPackageFile(final IScope scope, final String pathName) {
		this(scope, pathName, null);
	}

	/**
	 * Creates a GeoPackage file using the named feature layer.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the GeoPackage path
	 * @param layerName
	 *            the feature layer name
	 */
	@doc (
			value = "Reads a named feature layer from a GeoPackage (.gpkg) file.",
			examples = { @example (
					value = "file f <- geopackage_file(\"data.gpkg\", \"buildings\");",
					isExecutable = false) })
	public GamaGeoPackageFile(final IScope scope, final String pathName, final String layerName) {
		super(scope, pathName, (Integer) null);
		this.layerName = layerName;
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		final Map<String, String> attributes = new HashMap<>();
		final DataStore store = getDataStore(scope);
		try {
			for (final AttributeDescriptor descriptor : store.getFeatureSource(resolveLayerName(store, scope))
					.getSchema().getAttributeDescriptors()) {
				attributes.put(descriptor.getName().getLocalPart(), descriptor.getType() instanceof GeometryType
						? "geometry" : Types.get(descriptor.getType().getBinding()).toString());
			}
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			store.dispose();
		}
		return GamaListFactory.wrap(Types.STRING, attributes.keySet());
	}

	@Override
	protected SimpleFeatureCollection getFeatureCollection(final IScope scope) {
		if (featureCollection != null) return featureCollection;
		final DataStore store = getDataStore(scope);
		try {
			final String selectedLayer = resolveLayerName(store, scope);
			final SimpleFeatureSource source = store.getFeatureSource(selectedLayer);
			final Query query = new Query();
			query.getHints().put(Hints.JTS_COORDINATE_SEQUENCE_FACTORY,
					GamaCoordinateSequenceFactory.getJTSCoordinateSequenceFactory());
			query.getHints().put(Hints.JTS_GEOMETRY_FACTORY, GeometryUtils.getGeometryFactory());
			featureCollection = new ListFeatureCollection(source.getFeatures(query));
			return featureCollection;
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			store.dispose();
		}
	}

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		featureCollection = null;
	}

	/**
	 * Gets the data store.
	 *
	 * @param scope
	 *            the scope
	 * @return the data store
	 */
	private DataStore getDataStore(final IScope scope) {
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("dbtype", "geopkg");
		parameters.put("database", getFile(scope));
		try {
			final DataStore store = new org.geotools.geopkg.GeoPkgDataStoreFactory().createDataStore(parameters);
			if (store == null) throw GamaRuntimeException.error("Unable to open GeoPackage " + getPath(scope), scope);
			return store;
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	/**
	 * Resolve layer name.
	 *
	 * @param store
	 *            the store
	 * @param scope
	 *            the scope
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String resolveLayerName(final DataStore store, final IScope scope) throws IOException {
		final String[] layerNames = store.getTypeNames();
		if (layerName != null && !layerName.isBlank()) {
			for (final String availableLayer : layerNames) { if (layerName.equals(availableLayer)) return layerName; }
			throw GamaRuntimeException.error("GeoPackage layer '" + layerName + "' was not found. Available layers: "
					+ String.join(", ", layerNames), scope);
		}
		if (layerNames.length == 0)
			throw GamaRuntimeException.error("GeoPackage " + getPath(scope) + " contains no feature layers", scope);
		return layerNames[0];
	}
}
