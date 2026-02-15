/*******************************************************************************************************
 *
 * ProjectionFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.gis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;

import org.apache.commons.io.FilenameUtils;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.cs.CartesianCS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;

import gama.api.data.objects.IEnvelope;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.kernel.topology.IProjection;
import gama.api.kernel.topology.IProjectionFactory;
import gama.api.runtime.scope.IScope;
import gama.api.utils.map.GamaMapFactory;
import gama.api.utils.prefs.GamaPreferences;
import tech.units.indriya.unit.Units;

/**
 * Class ProjectionFactory.
 *
 * @author drogoul
 * @since 17 déc. 2013
 *
 */
public class ProjectionFactory implements IProjectionFactory {

	/** The Constant ALREADY_PROJECTED_CODE. */
	// The code to force reading the GIS data as already projected
	public static final int ALREADY_PROJECTED_CODE = 0;

	/** The Constant EPSGPrefix. */
	private static final String EPSGPrefix = "EPSG:";

	/** The Constant defaultTargetCRS. */
	private static final String defaultTargetCRS =
			String.valueOf(GamaPreferences.External.LIB_TARGET_CRS.getInitialValue(null));

	/** The Constant defaultSaveCRS. */
	private static final String defaultSaveCRS =
			String.valueOf(GamaPreferences.External.LIB_OUTPUT_CRS.getInitialValue(null));

	/** The CRS cache. */
	private static Map<String, ICoordinateReferenceSystem> CRSCache = GamaMapFactory.createUnordered();

	/** The world. */
	private IProjection world;

	/** The unit converter. */
	private UnitConverter unitConverter = null;

	/** The target CRS. */
	public ICoordinateReferenceSystem targetCRS;

	/** The epsg3857. */
	public static ICoordinateReferenceSystem EPSG3857 = null;

	static {
		// have to use a tmp variable because CRS.decode() throws a checked exception
		ICoordinateReferenceSystem tmp = null;
		try {
			tmp = new GamaCRS(CRS.decode("EPSG:3857"));
		} catch (FactoryException e) {}
		EPSG3857 = tmp;
	}

	/**
	 * Manage google CRS.
	 *
	 * @param url
	 *            the url
	 * @return the coordinate reference system
	 */
	// ugly method to manage Google CRS.... hoping that it is better managed by the next versions of Geotools
	public static ICoordinateReferenceSystem manageGoogleCRS(final URL url) {
		ICoordinateReferenceSystem crs = null;
		try {
			final String path = new File(url.toURI()).getAbsolutePath().replace(".shp", ".prj");
			if (Files.exists(Paths.get(path))) {
				final byte[] encoded = Files.readAllBytes(Paths.get(path));
				final String content = new String(encoded, StandardCharsets.UTF_8);
				if (content.contains("WGS 84 / Pseudo-Mercator")
						|| content.contains("WGS_1984_Web_Mercator_Auxiliary_Sphere")) {
					crs = EPSG3857;
				}
			}
		} catch (final IOException | URISyntaxException e) {}
		return crs;
	}

	/**
	 * Sets the world projection env.
	 *
	 * @param scope
	 *            the scope
	 * @param env
	 *            the env
	 */
	@Override
	public void setWorldProjectionEnv(final IScope scope, final IEnvelope env) {
		if (world != null) return;
		world = new WorldProjection(scope, null, env, this);
		// ((WorldProjection) world).updateTranslations(env);
	}

	/**
	 * Compute target CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param crs
	 *            the crs
	 * @param longitude
	 *            the longitude
	 * @param latitude
	 *            the latitude
	 */
	void computeTargetCRS(final IScope scope, final ICoordinateReferenceSystem crs, final double longitude,
			final double latitude) {
		// If we already know in which CRS we project the data in GAMA, no need to recompute it. This information is
		// normally wiped when an experiment is disposed
		if (targetCRS != null && !targetCRS.isNull()) return;
		try {
			if (!GamaPreferences.External.LIB_TARGETED.getValue()) {
				targetCRS = computeDefaultCRS(scope, GamaPreferences.External.LIB_TARGET_CRS.getValue(), true);
			} else if (crs.getCRS() instanceof DefaultProjectedCRS dpc) { // Temporary fix of issue 766... a better
				// solution
				final CartesianCS ccs = dpc.getCoordinateSystem();
				@SuppressWarnings ("unchecked") final Unit<Length> unitX = (Unit<Length>) ccs.getAxis(0).getUnit();
				if (unitX != null && !unitX.equals(Units.METRE)) { unitConverter = unitX.getConverterTo(Units.METRE); }
				targetCRS = crs;
			} else {
				final int index = (int) (0.5 + (longitude + 186.0) / 6);
				final boolean north = latitude > 0;
				final String newCode = EPSGPrefix + (32600 + index + (north ? 0 : 100));
				targetCRS = getCRS(scope, newCode);
			}
		} catch (final GamaRuntimeException e) {
			e.addContext(
					"The cause could be that you try to re-project already projected data (see Gama > __PREFS__... > External for turning the option to true)");
			throw e;
		}
	}

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @return the crs
	 */
	public static ICoordinateReferenceSystem getTargetCRSOrDefault(final IScope scope) {
		IProjection worldProjection = scope.getSimulation().getProjectionFactory().getWorld();
		return worldProjection == null ? ProjectionFactory.EPSG3857 : worldProjection.getTargetCRS(scope);
	}

	/**
	 * Save target CRS as PRJ file.
	 *
	 * @param scope
	 *            the scope
	 * @param path
	 *            the path
	 * @return true, if successful
	 */
	public static boolean saveTargetCRSAsPRJFile(final IScope scope, final String path) {
		ICoordinateReferenceSystem crs = getTargetCRSOrDefault(scope);
		try (FileWriter fw = new FileWriter(FilenameUtils.removeExtension(path) + ".prj")) {
			fw.write(crs.toString());
			return true;
		} catch (final IOException e) {
			return false;
		}
	}

	/**
	 * Gets the target CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the target CRS
	 */
	@Override
	public ICoordinateReferenceSystem getTargetCRS(final IScope scope) {
		if (targetCRS == null || targetCRS.isNull()) {
			try {
				return computeDefaultCRS(scope, GamaPreferences.External.LIB_TARGET_CRS.getValue(), true);
			} catch (final GamaRuntimeException e) {
				e.addContext(
						"The cause could be that you try to re-project already projected data (see Gama > __PREFS__... > External for turning the option to true)");
				throw e;
			}
		}
		return targetCRS;
	}

	/**
	 * Gets the save CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the save CRS
	 */
	@Override
	public ICoordinateReferenceSystem getSaveCRS(final IScope scope) {
		if (GamaPreferences.External.LIB_USE_DEFAULT.getValue()) return getWorld().getInitialCRS(scope);
		return computeDefaultCRS(scope, GamaPreferences.External.LIB_OUTPUT_CRS.getValue(), false);
	}

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @return the crs
	 */
	@Override
	public ICoordinateReferenceSystem getCRS(final IScope scope, final int code) {
		return getCRS(scope, code, true);
	}

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param longitudeFirst
	 *            the longitude first
	 * @return the crs
	 */
	@Override
	public ICoordinateReferenceSystem getCRS(final IScope scope, final int code, final boolean longitudeFirst) {
		if (code == ALREADY_PROJECTED_CODE) return getTargetCRS(scope);
		return getCRS(scope, EPSGPrefix + code, longitudeFirst);
	}

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @return the crs
	 */
	@Override
	public ICoordinateReferenceSystem getCRS(final IScope scope, final String code) {
		return getCRS(scope, code, true);
	}

	/**
	 * Gets the crs.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param longitudeFirst
	 *            the longitude first
	 * @return the crs
	 */
	@Override
	public ICoordinateReferenceSystem getCRS(final IScope scope, final String code, final boolean longitudeFirst)
			throws GamaRuntimeException {
		try {
			ICoordinateReferenceSystem crs = CRSCache.get(code);
			if (crs == null || crs.isNull()) {
				if (code.startsWith(EPSGPrefix) || code.startsWith("CRS:")) {
					crs = new GamaCRS(CRS.decode(code, longitudeFirst));
				} else if (code.startsWith("PROJCS") || code.startsWith("GEOGCS") || code.startsWith("COMPD_CS")) {
					crs = new GamaCRS(CRS.parseWKT(code));
				} else if (Character.isDigit(code.charAt(0))) {
					crs = new GamaCRS(CRS.decode(EPSGPrefix + code, longitudeFirst));
				}
				CRSCache.put(code, crs);
			}
			return crs;
		} catch (final NoSuchAuthorityCodeException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error(
					"The EPSG code " + code + " cannot be found. GAMA may be unable to load or save any GIS data",
					scope);
		} catch (final FactoryException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("An exception occured in trying to decode GIS data:" + e.getMessage(),
					scope);
		}
	}

	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	/*
	 * Thai.truongming@gmail.com ---------------begin date: 03-01-2014
	 */
	@Override
	public IProjection getWorld() { return world; }

	/**
	 * Compute default CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param target
	 *            the target
	 * @return the coordinate reference system
	 */
	/*
	 * thai.truongming@gmail.com -----------------end
	 */
	@Override
	public ICoordinateReferenceSystem computeDefaultCRS(final IScope scope, final int code, final boolean target) {
		ICoordinateReferenceSystem crs = getCRS(scope, code);
		if (crs == null || crs.isNull()) {
			crs = getCRS(scope, EPSGPrefix + (target ? defaultTargetCRS : defaultSaveCRS));
		}
		return crs;
	}

	/**
	 * From params.
	 *
	 * @param scope
	 *            the scope
	 * @param params
	 *            the params
	 * @param env
	 *            the env
	 * @return the i projection
	 */
	@Override
	public IProjection fromParams(final IScope scope, final Map<String, Object> params, final IEnvelope env) {
		final Boolean lonFirst = params.containsKey("longitudeFirst") ? (Boolean) params.get("longitudeFirst") : true;
		final Object crs = params.get("crs");
		if (crs instanceof String) return fromCRS(scope, getCRS(scope, (String) crs, lonFirst), env);
		final Object srid = params.get("srid");
		if (srid instanceof String) return fromCRS(scope, getCRS(scope, (String) srid, lonFirst), env);
		return fromCRS(scope, getDefaultInitialCRS(scope), env);
	}

	/**
	 * From CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param crs
	 *            the crs
	 * @param env
	 *            the env
	 * @return the i projection
	 */
	@Override
	public IProjection fromCRS(final IScope scope, final ICoordinateReferenceSystem crs, final IEnvelope env) {
		if (env != null) { testConsistency(scope, crs, env); }
		if (world != null) return new Projection(scope, world, crs, env, this);
		if (env != null) { computeTargetCRS(scope, crs, env.center().getX(), env.center().getY()); }
		world = new WorldProjection(scope, crs, env, this);
		return world;
	}

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param epsgCode
	 *            the epsg code
	 * @return the i projection
	 * @throws FactoryException
	 *             the factory exception
	 */
	@Override
	public IProjection forSavingWith(final IScope scope, final Integer epsgCode) {
		return forSavingWith(scope, epsgCode, true);
	}

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param epsgCode
	 *            the epsg code
	 * @param lonFirst
	 *            the lon first
	 * @return the i projection
	 * @throws FactoryException
	 *             the factory exception
	 */
	@Override
	public IProjection forSavingWith(final IScope scope, final Integer epsgCode, final boolean lonFirst) {
		return forSavingWith(scope, EPSGPrefix + epsgCode, lonFirst);
	}

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @return the i projection
	 * @throws FactoryException
	 *             the factory exception
	 */
	@Override
	public IProjection forSavingWith(final IScope scope, final String code) {
		return forSavingWith(scope, code, true);
	}

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param crs
	 *            the crs
	 * @return the i projection
	 * @throws FactoryException
	 *             the factory exception
	 */
	@Override
	public IProjection forSavingWith(final IScope scope, final ICoordinateReferenceSystem crs) {
		final Projection gis = new Projection(world, this);
		gis.initialCRS = crs;
		gis.computeProjectionAndCreateTransformation(scope);
		return gis;
	}

	/**
	 * For saving with.
	 *
	 * @param scope
	 *            the scope
	 * @param code
	 *            the code
	 * @param lonFirst
	 *            the lon first
	 * @return the i projection
	 * @throws FactoryException
	 *             the factory exception
	 */
	@Override
	public IProjection forSavingWith(final IScope scope, final String code, final boolean lonFirst) {
		ICoordinateReferenceSystem crs = null;
		try {
			crs = getCRS(scope, code, lonFirst);
		} catch (final Exception e) {
			crs = null;
		}
		if (crs == null || crs.isNull()) { crs = getSaveCRS(scope); }
		final Projection gis = new Projection(world, this);
		gis.initialCRS = crs;
		gis.computeProjectionAndCreateTransformation(scope);
		return gis;
	}

	/**
	 * Gets the default initial CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the default initial CRS
	 */
	@Override
	public ICoordinateReferenceSystem getDefaultInitialCRS(final IScope scope) {
		if (GamaPreferences.External.LIB_PROJECTED.getValue()) return getTargetCRS(scope);
		try {
			return getCRS(scope, GamaPreferences.External.LIB_INITIAL_CRS.getValue());
		} catch (final GamaRuntimeException e) {
			throw GamaRuntimeException.error("The code " + GamaPreferences.External.LIB_INITIAL_CRS.getValue()
					+ " does not correspond to a known EPSG code. Try to change it in Gama > Preferences... > External",
					scope);
		}
	}

	/**
	 * Test consistency.
	 *
	 * @param scope
	 *            the scope
	 * @param crs
	 *            the crs
	 * @param env
	 *            the env
	 */
	@Override
	public void testConsistency(final IScope scope, final ICoordinateReferenceSystem crs, final IEnvelope env) {
		if ((crs == null || !(crs.getCRS() instanceof DefaultProjectedCRS))
				&& (env.getHeight() > 180 || env.getWidth() > 180))
			throw GamaRuntimeException.error(
					"Inconsistency between the data and the CRS: The CRS " + crs
							+ " corresponds to a not projected one, whereas the data seem to be already projected.",
					scope);
	}

	/**
	 * Gets the unit converter.
	 *
	 * @return the unit converter
	 */
	@Override
	public UnitConverter getUnitConverter() { return unitConverter; }

}
