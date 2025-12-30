/*******************************************************************************************************
 *
 * GamaShapeFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static gama.core.runtime.GAMA.reportError;
import static gama.core.runtime.exceptions.GamaRuntimeException.create;
import static gama.core.runtime.exceptions.GamaRuntimeException.warning;
import static org.apache.commons.lang3.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Geometry;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.IConcept;
import gama.core.common.geometry.GamaGeometryFactory;
import gama.core.common.geometry.GeometryUtils;
import gama.core.common.preferences.GamaPreferences;
import gama.core.metamodel.shape.GamaGisGeometry;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.topology.projection.ProjectionFactory;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.dev.DEBUG;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

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
	 * The Class ShapeInfo.
	 */
	public static record ShapeInfo(int itemNumber, CoordinateReferenceSystem crs, double width, double height,
			Map<String, String> attributes) {}

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
	public static ShapeInfo createInfo(final IScope scope, final URL url, final long modificationStamp) {
		FileDataStore store = null;
		ReferencedEnvelope env = new ReferencedEnvelope();
		CoordinateReferenceSystem crs1 = null;
		int number = 0;
		Map<String, String> attributes = new HashMap<>();
		try {
			store = getDataStore(url);

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

			if (store != null) { store.dispose(); }
		}
		return new ShapeInfo(number, crs1, env.getWidth(), env.getHeight(), attributes);
	}

	/**
	 * Instantiates a new shape info.
	 *
	 * @param propertiesString
	 *            the properties string
	 */
	public static ShapeInfo fromPropertiesString(final String propertiesString) {
		final String[] segments = splitByWholeSeparatorPreserveAllTokens(propertiesString, IGamaFileMetaData.DELIMITER);
		int itemNumber = Integer.parseInt(segments[1]);
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
		CoordinateReferenceSystem crs = theCRS;
		double width = Double.parseDouble(segments[3]);
		double height = Double.parseDouble(segments[4]);
		Map<String, String> attributes = new HashMap<>();

		if (segments.length > 5) {
			final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], IGamaFileMetaData.SUB_DELIMITER);
			final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], IGamaFileMetaData.SUB_DELIMITER);
			for (int i = 0; i < names.length; i++) { attributes.put(names[i], types[i]); }
		}
		return new ShapeInfo(itemNumber, crs, width, height, attributes);
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

	@Override
	public IList<String> getAttributes(final IScope scope) {
		ShapeInfo s = null;
		final IFileMetaDataProvider p = scope.getGui().getMetaDataProvider();
		if (p != null) {
			final IGamaFileMetaData metaData = p.getMetaData(getFile(scope), false, true);
			if (metaData != null) { s = fromPropertiesString(metaData.toPropertyString()); }
		}
		if (s == null) {
			try {
				s = createInfo(scope, getFile(scope).toURI().toURL(), 0);
			} catch (final MalformedURLException e) {
				return GamaListFactory.EMPTY_LIST;
			}
		}
		return GamaListFactory.wrap(Types.STRING, s.attributes.keySet());
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
			store.setGeometryFactory(GeometryUtils.GEOMETRY_FACTORY);
			store.setMemoryMapped(GamaPreferences.Experimental.SHAPEFILES_IN_MEMORY.getValue());
			store.setBufferCachingEnabled(GamaPreferences.Experimental.SHAPEFILES_IN_MEMORY.getValue());
			store.setCharset(Charset.forName("UTF8"));
		}
		return fds;

	}

	@Override
	protected final void readShapes(final IScope scope) {
		ProgressCounter counter = new ProgressCounter(scope, "Reading " + getName(scope));
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
					GamaShape gt = new GamaGisGeometry(g, feature);
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
				try (ShapefileReader reader = new ShapefileReader(shp, false, false, GeometryUtils.GEOMETRY_FACTORY)) {
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
								GamaShape gt = new GamaGisGeometry(g.getGeometryN(i), null);
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
			query.getHints().put(Hints.JTS_COORDINATE_SEQUENCE_FACTORY, GamaGeometryFactory.COORDINATES_FACTORY);
			query.getHints().put(Hints.JTS_GEOMETRY_FACTORY, GeometryUtils.GEOMETRY_FACTORY);
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