/*******************************************************************************************************
 *
 * GamaGisFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static gama.core.common.geometry.GeometryUtils.GEOMETRY_FACTORY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gama.core.common.geometry.Envelope3D;
import gama.core.common.geometry.GeometryUtils;
import gama.core.common.geometry.ICoordinates;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.metamodel.shape.GamaGisGeometry;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.projection.IProjection;
import gama.core.metamodel.topology.projection.ProjectionFactory;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.Collector;
import gama.core.util.GamaListFactory;
import gama.gaml.types.GamaGeometryType;
import gama.gaml.types.Types;

/**
 * Class GamaGisFile.
 *
 * @author drogoul
 * @since 12 déc. 2013
 *
 */
public abstract class GamaGisFile extends GamaGeometryFile {

	/** The Constant ALREADY_PROJECTED_CODE. */
	// The code to force reading the GIS data as already projected
	public static final int ALREADY_PROJECTED_CODE = 0;

	/** The zero z. */
	static CoordinateFilter ZERO_Z = coord -> coord.setZ(0);

	/** The gis. */
	public IProjection gis;

	/** The initial CRS code. */
	protected Integer initialCRSCode = null;

	/** The initial CRS code str. */
	protected String initialCRSCodeStr = null;

	/** The with 3 D. */
	protected boolean with3D = false;

	// Faire les tests sur ALREADY_PROJECTED ET LE PASSER AUSSI A GIS UTILS ???

	/** The CRS cache. */
	static Cache<String, CoordinateReferenceSystem> CRSCache = CacheBuilder.newBuilder().concurrencyLevel(10)
			.expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES)).build();

	/**
	 * Returns the CRS defined with this file (in a ".prj" file or passed by the user)
	 *
	 * @return
	 */
	protected final CoordinateReferenceSystem getExistingCRS(final IScope scope) {
		try {
			return CRSCache.get(this.getPath(scope), () -> {
				if (initialCRSCode != null) {
					try {
						return scope.getSimulation().getProjectionFactory().getCRS(scope, initialCRSCode);
					} catch (final GamaRuntimeException e1) {
						throw GamaRuntimeException.error("The code " + initialCRSCode
								+ " does not correspond to a known EPSG code. GAMA is unable to load " + getPath(scope),
								scope);
					}
				}
				if (initialCRSCodeStr != null) {
					try {
						return scope.getSimulation().getProjectionFactory().getCRS(scope, initialCRSCodeStr);
					} catch (final GamaRuntimeException e2) {
						throw GamaRuntimeException.error("The code " + initialCRSCodeStr
								+ " does not correspond to a known CRS code. GAMA is unable to load " + getPath(scope),
								scope);
					}
				}
				CoordinateReferenceSystem crs = getOwnCRS(scope);
				if (crs == null && scope != null) {
					crs = scope.getSimulation().getProjectionFactory().getDefaultInitialCRS(scope);
				}
				return crs;
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
			return scope.getSimulation().getProjectionFactory().getDefaultInitialCRS(scope);
		}

	}

	/**
	 * @return
	 */
	protected CoordinateReferenceSystem getOwnCRS(final IScope scope) {
		URL url;
		try {
			url = getFile(scope).toURI().toURL();
			CoordinateReferenceSystem crs = getFeatureCollection(scope).getSchema().getCoordinateReferenceSystem();
			if (crs == null) { crs = ProjectionFactory.manageGoogleCRS(url); }
			return crs;
		} catch (MalformedURLException e) {
			return null;
		}

	}

	/**
	 * Gets the feature collection.
	 *
	 * @param scope
	 *            the scope
	 * @return the feature collection
	 */
	protected abstract SimpleFeatureCollection getFeatureCollection(final IScope scope);

	/**
	 * Read shapes.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void readShapes(final IScope scope) {
		ProgressCounter counter = new ProgressCounter(scope, "Reading " + getName(scope));
		SimpleFeatureCollection collection = getFeatureCollection(scope);
		computeEnvelope(scope);
		try {
			collection.accepts(feature -> {
				Geometry g = (Geometry) feature.getDefaultGeometryProperty().getValue();
				if (g != null && !g.isEmpty() /* Fix for Issue 725 && 677 */ ) {
					if (!with3D && !g.isValid()) { g = GeometryUtils.cleanGeometry(g); }
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
					GAMA.reportError(scope,
							GamaRuntimeException.warning(
									"geometry could not be added as it is " + "nil: " + feature.getIdentifier(), scope),
							false);
				}
			}, counter);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
		// finally {
		// if (store != null) { store.dispose(); }
		// }
		// if (size > list.size()) {
		// reportError(scope, warning("Problem with file " + getFile(scope) + ": only " + list.size() + " of the "
		// + size + " geometries could be added", scope), false);
		// }
	}

	/**
	 * Compute projection.
	 *
	 * @param scope
	 *            the scope
	 * @param env
	 *            the env
	 */
	protected void computeProjection(final IScope scope, final Envelope3D env) {
		if (scope == null) return;
		final CoordinateReferenceSystem crs = getExistingCRS(scope);
		final ProjectionFactory pf;
		if (scope.getSimulation().isMicroSimulation()) {
			pf = ((IExperimentAgent) scope.getExperiment().getPopulation().getHost()).getSimulation()
					.getProjectionFactory();
		} else {
			pf = scope.getSimulation() == null ? new ProjectionFactory() : scope.getSimulation().getProjectionFactory();
		}
		gis = pf.fromCRS(scope, crs, env);
	}

	/**
	 * Multi polygon management.
	 *
	 * @param geom
	 *            the geom
	 * @return the geometry
	 */
	protected Geometry multiPolygonManagement(final Geometry geom) {
		if (! (geom instanceof MultiPolygon)) {
			return geom;
		}
		final Polygon gs[] = new Polygon[geom.getNumGeometries()];
		for (int i = 0; i < geom.getNumGeometries(); i++) {
			final Polygon p = (Polygon) geom.getGeometryN(i);
			final ICoordinates coords = GeometryUtils.getContourCoordinates(p);
			final LinearRing lr = GEOMETRY_FACTORY.createLinearRing(coords.toCoordinateArray());
			try (final Collector.AsList<LinearRing> holes = Collector.getList()) {
				for (int j = 0; j < p.getNumInteriorRing(); j++) {
					final LinearRing h = p.getInteriorRingN(j);
					if (!hasNullElements(h.getCoordinates())) { holes.add(h); }
				}
				LinearRing[] stockArr = new LinearRing[holes.size()];
				stockArr = holes.items().toArray(stockArr);
				gs[i] = GEOMETRY_FACTORY.createPolygon(lr, stockArr);
			}
		}
		return GEOMETRY_FACTORY.createMultiPolygon(gs);
	}

	/**
	 * Checks for null elements.
	 *
	 * @param array
	 *            the array
	 * @return true, if successful
	 */
	protected static boolean hasNullElements(final Object[] array) {
		for (final Object element : array) { if (element == null) return true; }
		return false;
	}

	/**
	 * Instantiates a new gama gis file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param withZ
	 *            the with Z
	 */
	public GamaGisFile(final IScope scope, final String pathName, final Integer code, final boolean withZ) {
		super(scope, pathName);
		initialCRSCode = code;
		with3D = withZ;
	}

	/**
	 * Instantiates a new gama gis file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 */
	public GamaGisFile(final IScope scope, final String pathName, final Integer code) {
		super(scope, pathName);
		initialCRSCode = code;
	}

	/**
	 * Instantiates a new gama gis file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 */
	public GamaGisFile(final IScope scope, final String pathName, final String code) {
		super(scope, pathName);
		initialCRSCodeStr = code;
	}

	/**
	 * Instantiates a new gama gis file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param code
	 *            the code
	 * @param withZ
	 *            the with Z
	 */
	public GamaGisFile(final IScope scope, final String pathName, final String code, final boolean withZ) {
		super(scope, pathName);
		initialCRSCodeStr = code;
		with3D = withZ;
	}

	/**
	 * Instantiates a new gama gis file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param b
	 *            the b
	 */
	public GamaGisFile(final IScope scope, final String pathName, final boolean b) {
		super(scope, pathName, b);
	}

	/**
	 * Gets the gis.
	 *
	 * @param scope
	 *            the scope
	 * @return the gis
	 */
	public IProjection getGis(final IScope scope) {
		if (gis == null) { fillBuffer(scope); }
		return gis;
	}

	@Override
	protected IShape buildGeometry(final IScope scope) {
		return GamaGeometryType.geometriesToGeometry(scope, getBuffer());
	}

	/**
	 * @see gama.core.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		readShapes(scope);
	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		if (gis == null) {
			final SimpleFeatureCollection collection = getFeatureCollection(scope);
			if (collection == null) return Envelope3D.EMPTY;
			final Envelope3D env = Envelope3D.of(collection.getBounds());
			computeProjection(scope, env);
		}
		return gis.getProjectedEnvelope();

	}

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		gis = null;
		initialCRSCode = null;
		initialCRSCodeStr = null;
	}

}
