/*******************************************************************************************************
 *
 * GamaShapeFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static gama.api.GAMA.reportError;
import static gama.api.exceptions.GamaRuntimeException.create;
import static gama.api.exceptions.GamaRuntimeException.warning;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Geometry;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.file;
import gama.annotations.support.IConcept;
import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.ui.IProgressIndicator;
import gama.api.utils.files.IFileMetadataProvider;
import gama.api.utils.files.IGamaFileMetaData;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.geometry.GamaGisGeometry;
import gama.dev.DEBUG;

/**
 * Written by drogoul Modified on 13 nov. 2011
 *
 * @todo Description
 *
 */

/**
 * The Class GamaShapeFile.
 */

/**
 * The Class GamaShapeFile.
 */
@file (
		name = "shape",
		extensions = { "shp", "SHP" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.SHAPEFILE, IConcept.FILE },
		doc = @doc ("Represents a shape file as defined by the ESRI standard. See https://en.wikipedia.org/wiki/Shapefile for more information."))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaShapeFile extends GamaGisFile {

	static {
		DEBUG.OFF();
	}

	// FileDataStore store;

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
	public Map<String, String> getAttributesFromFile(final IScope scope, final URL url, final long modificationStamp) {
		FileDataStore store = null;
		Map<String, String> attributes = new HashMap<>();
		try {
			store = getDataStore(url);
			for (final AttributeDescriptor desc : store.getSchema().getAttributeDescriptors()) {
				attributes.put(desc.getName().getLocalPart(), desc.getType() instanceof GeometryType ? "geometry"
						: Types.get(desc.getType().getBinding()).toString());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (store != null) { store.dispose(); }
		}
		return attributes;
	}

	/**
	 * Instantiates a new shape info.
	 *
	 * @param propertiesString
	 *            the properties string
	 */
	public Map<String, String> getAttributesFromPropertiesString(final String propertiesString) {
		final String[] segments = splitByWholeSeparatorPreserveAllTokens(propertiesString, IGamaFileMetaData.DELIMITER);
		Map<String, String> attributes = new HashMap<>();
		if (segments.length > 5) {
			final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], IGamaFileMetaData.SUB_DELIMITER);
			final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], IGamaFileMetaData.SUB_DELIMITER);
			for (int i = 0; i < names.length; i++) { attributes.put(names[i], types[i]); }
		}
		return attributes;
	}

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 * @param pathName
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\");",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null);
	}

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code, as an int (epsg code)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"32648\");",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final Integer code) throws GamaRuntimeException {
		super(scope, pathName, code);
	}

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code (epg,...,), as a string",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"EPSG:32648\");",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final String code) throws GamaRuntimeException {
		super(scope, pathName, code);
	}

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", true);",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final boolean with3D) throws GamaRuntimeException {
		super(scope, pathName, (Integer) null, with3D);
	}

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code, as an int (epsg code) and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"32648\", true);",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final Integer code, final boolean with3D)
			throws GamaRuntimeException {
		super(scope, pathName, code, with3D);
	}

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Instantiates a new gama shape file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param with3D
	 *            the with 3 D
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@doc (
			value = "This file constructor allows to read a shapefile (.shp) file and specifying the coordinates system code (epg,...,), as a string and take a potential z value (not taken in account by default)",
			examples = { @example (
					value = "file f <- shape_file(\"file.shp\", \"EPSG:32648\",true);",
					isExecutable = false) })
	public GamaShapeFile(final IScope scope, final String pathName, final String code, final boolean with3D)
			throws GamaRuntimeException {
		super(scope, pathName, code, with3D);
	}

	/** The attributes. */
	private Map<String, String> attributes = null;

	@Override
	public IList<String> getAttributes(final IScope scope) {
		if (attributes == null) {
			final IFileMetadataProvider p = GAMA.getMetadataProvider();
			if (p != null) {
				final IGamaFileMetaData metaData = p.getMetaData(getFile(scope), false, true);
				if (metaData != null) { attributes = getAttributesFromPropertiesString(metaData.toPropertyString()); }
			} else {
				try {
					attributes = getAttributesFromFile(scope, getFile(scope).toURI().toURL(), 0);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					attributes = Collections.EMPTY_MAP;
				}
			}
		}
		return GamaListFactory.wrap(Types.STRING, attributes.keySet());
	}

	/**
	 * Gets the data store.
	 *
	 * @param url
	 *            the url
	 * @return the data store
	 */
	public static FileDataStore getDataStore(final URL url) {
		FileDataStore fds;
		try {
			fds = FileDataStoreFinder.getDataStore(url);
		} catch (IOException e) {
			return null;
		}
		if (fds instanceof ShapefileDataStore store) {
			store.setGeometryFactory(GeometryUtils.getGeometryFactory());
			store.setMemoryMapped(GamaPreferences.Experimental.SHAPEFILES_IN_MEMORY.getValue());
			store.setBufferCachingEnabled(GamaPreferences.Experimental.SHAPEFILES_IN_MEMORY.getValue());
			store.setCharset(Charset.forName("UTF8"));
		}
		return fds;

	}

	@Override
	protected final void readShapes(final IScope scope) {
		IProgressIndicator counter = scope.getGui().getProgressIndicator(scope, "Reading " + getName(scope));
		SimpleFeatureCollection collection = getFeatureCollection(scope);
		computeEnvelope(scope);
		int[] indexOfGeometry = { 0 };
		try {
			collection.accepts(feature -> {
				Geometry g = (Geometry) feature.getDefaultGeometryProperty().getValue();
				if (g != null && !g.isEmpty() /* Fix for Issue 725 && 677 */ ) {
					if (!with3D && g.getNumPoints() > 2) {
						try {
							if (!g.isValid()) { g = GeometryUtils.cleanGeometry(g); }
						} catch (Exception e) {
							g = GeometryUtils.cleanGeometry(g);
						}
					}
					g = gis.transform(g);
					if (!with3D) {
						g.apply(ZERO_Z);
						g.geometryChanged();
					}
					g = multiPolygonManagement(g);
					IShape gt = new GamaGisGeometry(g, feature);
					if (gt.getInnerGeometry() != null) { getBuffer().add(gt); }
				} else if (g == null) {
					// See Issue 725
					reportError(scope,
							warning("geometry #" + indexOfGeometry[0] + " from " + getName(scope)
									+ " could not be added as it is nil (identifier: " + feature.getIdentifier() + ")",
									scope),
							false);
				}
				indexOfGeometry[0]++;
			}, counter);
		} catch (final Exception ex) {
			try {
				getBuffer().clear();
				indexOfGeometry[0] = 0;
				ShpFiles shp = new ShpFiles(getFile(scope).toURI().toURL());
				try (ShapefileReader reader =
						new ShapefileReader(shp, false, false, GeometryUtils.getGeometryFactory())) {
					reader.setFlatGeometry(true);
					while (reader.hasNext()) {
						Record record = reader.nextRecord();
						Geometry g = (Geometry) record.shape();
						g = GeometryUtils.cleanGeometry((Geometry) record.shape());

						if (g != null && !g.isEmpty() /* Fix for Issue 725 && 677 */ ) {

							if (!with3D && g.getNumPoints() > 2) {
								try {
									if (!g.isValid()) { g = GeometryUtils.cleanGeometry(g); }
								} catch (Exception e) {
									g = GeometryUtils.cleanGeometry(g);
								}
							}
							g = gis.transform(g);
							if (!with3D) {
								g.apply(ZERO_Z);
								g.geometryChanged();
							}
							g = multiPolygonManagement(g);

							for (int i = 0; i < g.getNumGeometries(); i++) {
								IShape gt = new GamaGisGeometry(g.getGeometryN(i), null);
								if (gt.getInnerGeometry() != null) { getBuffer().add(gt); }
							}

						} else if (g == null) {
							// See Issue 725
							reportError(scope, warning("geometry #" + indexOfGeometry[0] + " from " + getName(scope)
									+ " could not be added as it is nil", scope), false);
						}
						indexOfGeometry[0]++;
					}
				}
			} catch (final IOException e2) {
				throw create(e2, scope);
			}
		}

	}

	@Override
	protected SimpleFeatureCollection getFeatureCollection(final IScope scope) {
		try {

			// if (store == null) { store = getDataStoreOld(getFile(scope).toURI().toURL()); }
			final SimpleFeatureSource source = getDataStore(getFile(scope).toURI().toURL()).getFeatureSource();
			// AD See Issue #3094. This constitutes a workaround
			Query query = new Query();
			// if (!with3D) { query.setHints(new Hints(Hints.FEATURE_2D, true)); }
			query.getHints().put(Hints.JTS_COORDINATE_SEQUENCE_FACTORY,
					GamaCoordinateSequenceFactory.getJTSCoordinateSequenceFactory());
			query.getHints().put(Hints.JTS_GEOMETRY_FACTORY, GeometryUtils.getGeometryFactory());
			// AD
			SimpleFeatureCollection collection = source.getFeatures(query);
			if (source.getDataStore() != null) { source.getDataStore().dispose(); }
			return collection;

		} catch (IOException e) {
			throw create(e, scope);
		}
	}

	@Override
	public int length(final IScope scope) {
		// This line deactivated because of issue #3525
		// if (getBuffer() == null) return getFeatureCollection(scope).size();
		return super.length(scope);
	}

}