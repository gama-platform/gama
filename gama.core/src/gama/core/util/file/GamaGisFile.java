/*******************************************************************************************************
 *
 * GamaGisFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import static gama.api.utils.geometry.GeometryUtils.getGeometryFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gama.api.GAMA;
import gama.api.data.factories.GamaCoordinateSequenceFactory;
import gama.api.data.factories.GamaEnvelopeFactory;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Types;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.kernel.topology.IProjection;
import gama.api.kernel.topology.IProjectionFactory;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IProgressIndicator;
import gama.api.utils.collections.Collector;
import gama.api.utils.geometry.GeometryUtils;
import gama.core.geometry.GamaGisGeometry;
import gama.core.geometry.GamaShape;
import gama.core.topology.gis.GamaCRS;
import gama.core.topology.gis.ProjectionFactory;

/**
 * Class GamaGisFile.
 *
 * @author drogoul
 * @since 12 déc. 2013
 *
 */
public abstract class GamaGisFile extends GamaGeometryFile {

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
	static Cache<String, ICoordinateReferenceSystem> CRSCache = CacheBuilder.newBuilder().concurrencyLevel(10)
			.expireAfterAccess(Duration.of(5, ChronoUnit.MINUTES)).build();

	/**
	 * Returns the CRS defined with this file (in a ".prj" file or passed by the user)
	 *
	 * @return
	 */
	protected final ICoordinateReferenceSystem getExistingCRS(final IScope scope) {
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
				ICoordinateReferenceSystem crs = getOwnCRS(scope);
				if ((crs == null || crs.isNull()) && scope != null) {
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
	protected ICoordinateReferenceSystem getOwnCRS(final IScope scope) {
		URL url;
		try {
			url = getFile(scope).toURI().toURL();
			CoordinateReferenceSystem internalCRS =
					getFeatureCollection(scope).getSchema().getCoordinateReferenceSystem();
			if (internalCRS == null) return ProjectionFactory.manageGoogleCRS(url);
			return new GamaCRS(internalCRS);
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
		IProgressIndicator counter = scope.getGui().getProgressIndicator(scope, "Reading " + getName(scope));
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
	protected void computeProjection(final IScope scope, final IEnvelope env) {
		if (scope == null) return;
		final ICoordinateReferenceSystem crs = getExistingCRS(scope);
		final IProjectionFactory pf;
		if (scope.getSimulation().isMicroSimulation()) {
			pf = scope.getExperiment().getPopulation().getHost().getSimulation().getProjectionFactory();
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
		if (!(geom instanceof MultiPolygon)) return geom;
		final Polygon gs[] = new Polygon[geom.getNumGeometries()];
		for (int i = 0; i < geom.getNumGeometries(); i++) {
			final Polygon p = (Polygon) geom.getGeometryN(i);
			final ICoordinates coords = GamaCoordinateSequenceFactory.pointsOf(p);
			final LinearRing lr = getGeometryFactory().createLinearRing(coords.toCoordinateArray());
			try (final Collector.AsList<LinearRing> holes = Collector.getList()) {
				for (int j = 0; j < p.getNumInteriorRing(); j++) {
					final LinearRing h = p.getInteriorRingN(j);
					if (!hasNullElements(h.getCoordinates())) { holes.add(h); }
				}
				LinearRing[] stockArr = new LinearRing[holes.size()];
				stockArr = holes.items().toArray(stockArr);
				gs[i] = getGeometryFactory().createPolygon(lr, stockArr);
			}
		}
		return getGeometryFactory().createMultiPolygon(gs);
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
		return GamaShapeFactory.geometriesToGeometry(scope, getBuffer());
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
	public IEnvelope computeEnvelope(final IScope scope) {
		if (gis == null) {
			final SimpleFeatureCollection collection = getFeatureCollection(scope);
			if (collection == null) return GamaEnvelopeFactory.EMPTY;
			final IEnvelope env = GamaEnvelopeFactory.of(collection.getBounds());
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
