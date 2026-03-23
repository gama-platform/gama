/*******************************************************************************************************
 *
 * SpatialProjections.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators.spatial;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;

import java.util.concurrent.ConcurrentHashMap;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.kernel.topology.IProjection;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.IGamaFile;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.core.util.file.GamaGisFile;

/**
 * The Class Projections.
 */
public class SpatialProjections {

	/** The Constant THE_CODE. */
	private static final String THE_CODE = "The code ";

	/**
	 * Thread-safe cache for decoded CoordinateReferenceSystem instances, keyed by EPSG code string.
	 * CRS.decode() is expensive (involves registry lookup and parsing) so results are cached.
	 */
	private static final ConcurrentHashMap<String, CoordinateReferenceSystem> CRS_CACHE = new ConcurrentHashMap<>();

	/**
	 * Thread-safe cache for MathTransform instances, keyed by "sourceCode->targetCode".
	 * CRS.findMathTransform() is expensive so results are cached.
	 */
	private static final ConcurrentHashMap<String, MathTransform> TRANSFORM_CACHE = new ConcurrentHashMap<>();

	/**
	 * Returns a cached CoordinateReferenceSystem for the given EPSG code, decoding it on first use.
	 *
	 * @param code
	 *            the EPSG code string
	 * @param scope
	 *            the current scope (for error reporting)
	 * @return the CoordinateReferenceSystem
	 */
	private static CoordinateReferenceSystem decodeCRS(final String code, final gama.api.runtime.scope.IScope scope) {
		try {
			return CRS_CACHE.computeIfAbsent(code, k -> {
				try {
					return CRS.decode(k);
				} catch (final FactoryException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (final RuntimeException e) {
			throw gama.api.exceptions.GamaRuntimeException.error(THE_CODE + code + " does not correspond to a known EPSG code", scope);
		}
	}

	/**
	 * Crs from file.
	 *
	 * @param scope
	 *            the scope
	 * @param gisFile
	 *            the gis file
	 * @return the string
	 */
	@operator (
			value = "crs",
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.FILE },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.FILE, IConcept.GIS })
	@doc (
			value = "the Coordinate Reference System (CRS) of the GIS file",
			examples = { @example (
					value = "crs(my_shapefile)",
					equals = "the crs of the shapefile",
					isExecutable = false) },
			see = {})
	@no_test
	public static String crsFromFile(final IScope scope, final IGamaFile gisFile) {
		if (!(gisFile instanceof GamaGisFile))
			throw GamaRuntimeException.error("Impossible to compute the CRS for this type of file", scope);
		final ICoordinateReferenceSystem crs = ((GamaGisFile) gisFile).getGis(scope).getInitialCRS(scope);
		if (crs == null || crs.isNull()) return null;
		try {
			return CRS.lookupIdentifier(crs.getCRS(), true);
		} catch (final FactoryException | NullPointerException e) {
			return null;
		}
	}

	/**
	 * Transform CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the i shape
	 */
	@operator (
			value = { "CRS_transform" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.GIS })
	@doc (
			usages = { @usage (
					value = "returns the geometry corresponding to the transformation of the given geometry by the current CRS (Coordinate Reference System), the one corresponding to the world's agent one",
					examples = { @example (
							value = "CRS_transform(shape)",
							equals = "a geometry corresponding to the agent geometry transformed into the current CRS",
							test = false) }) })
	@no_test
	public static IShape transform_CRS(final IScope scope, final IShape g) {
		final IProjection gis = scope.getSimulation().getProjectionFactory().getWorld();
		if (gis == null) return g.copy(scope);
		final IShape s = GamaShapeFactory.createFrom(gis.inverseTransform(g.getInnerGeometry()));
		if (g instanceof IPoint) return s.getLocation();
		return s;
	}

	/**
	 * To GAM A CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @return the i shape
	 */
	@operator (
			value = { "to_GAMA_CRS" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.GIS })
	@doc (
			usages = { @usage (
					value = "returns the geometry corresponding to the transformation of the given geometry to the GAMA CRS (Coordinate Reference System) assuming the given geometry is referenced by the current CRS, the one corresponding to the world's agent one",
					examples = { @example (
							value = "to_GAMA_CRS({121,14})",
							equals = "a geometry corresponding to the agent geometry transformed into the GAMA CRS",
							test = false) }) })
	@no_test
	public static IShape to_GAMA_CRS(final IScope scope, final IShape g) {
		final IProjection gis = scope.getSimulation().getProjectionFactory().getWorld();
		if (gis == null) return g.copy(scope);
		final IShape s = GamaShapeFactory.createFrom(gis.transform(g.getInnerGeometry()));
		if (g instanceof IPoint) return s.getLocation();
		return s;
	}

	/**
	 * To GAM A CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param code
	 *            the code
	 * @return the i shape
	 */
	@operator (
			value = { "to_GAMA_CRS" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = {})
	@doc (
			usages = { @usage (
					value = "returns the geometry corresponding to the transformation of the given geometry to the GAMA CRS (Coordinate Reference System) assuming the given geometry is referenced by given CRS",
					examples = { @example (
							value = "to_GAMA_CRS({121,14}, \"EPSG:4326\")",
							equals = "a geometry corresponding to the agent geometry transformed into the GAMA CRS",
							test = false) }) })
	@no_test
	public static IShape to_GAMA_CRS(final IScope scope, final IShape g, final String code) {
		IProjection gis;
		try {
			gis = scope.getSimulation().getProjectionFactory().forSavingWith(scope, code);
		} catch (final Exception e) {
			throw GamaRuntimeException.error(THE_CODE + code + " does not correspond to a known EPSG code", scope);
		}
		if (gis == null) return g.copy(scope);
		final IShape s = GamaShapeFactory.createFrom(gis.transform(g.getInnerGeometry()));
		if (g instanceof IPoint) return s.getLocation();
		return s;
	}

	/**
	 * Transform CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param code
	 *            the code
	 * @return the i shape
	 */
	@operator (
			value = { "CRS_transform" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.GIS })
	@doc (
			usages = { @usage (
					value = "returns the geometry corresponding to the transformation of the given geometry by the left operand CRS (Coordinate Reference System)",
					examples = { @example (
							value = "shape CRS_transform(\"EPSG:4326\")",
							equals = "a geometry corresponding to the agent geometry transformed into the EPSG:4326 CRS",
							test = false) }) })
	@no_test
	public static IShape transform_CRS(final IScope scope, final IShape g, final String code) {
		IProjection gis;
		try {
			gis = scope.getSimulation().getProjectionFactory().forSavingWith(scope, code);
		} catch (final Exception e) {
			throw GamaRuntimeException.error(THE_CODE + code + " does not correspond to a known EPSG code", scope);
		}
		if (gis == null) return g.copy(scope);
		final IShape s = GamaShapeFactory.createFrom(gis.inverseTransform(g.getInnerGeometry()));
		if (g instanceof IPoint) return s.getLocation();
		return s;
	}

	/**
	 * Transform CRS.
	 *
	 * @param scope
	 *            the scope
	 * @param g
	 *            the g
	 * @param sourceCode
	 *            the source code
	 * @param targetcode
	 *            the targetcode
	 * @return the i shape
	 */
	@operator (
			value = { "CRS_transform" },
			category = { IOperatorCategory.SPATIAL, IOperatorCategory.SP_TRANSFORMATIONS },
			concept = { IConcept.GEOMETRY, IConcept.SPATIAL_COMPUTATION, IConcept.SPATIAL_TRANSFORMATION,
					IConcept.GIS })
	@doc (
			usages = { @usage (
					value = "returns the geometry corresponding to the transformation of the given geometry from the first CRS to the second CRS (Coordinate Reference System)",
					examples = { @example (
							value = "{8.35,47.22} CRS_transform(\"EPSG:4326\",\"EPSG:4326\")",
							equals = "{929517.7481238344,5978057.894895313,0.0}",
							test = false) }) })
	@no_test
	public static IShape transform_CRS(final IScope scope, final IShape g, final String sourceCode,
			final String targetcode) {
		if (g == null) return g;
		final CoordinateReferenceSystem sourceCRS = decodeCRS(sourceCode, scope);
		final CoordinateReferenceSystem targetCRS = decodeCRS(targetcode, scope);

		final String transformKey = sourceCode + "->" + targetcode;
		MathTransform transform = TRANSFORM_CACHE.get(transformKey);
		if (transform == null) {
			try {
				transform = CRS.findMathTransform(sourceCRS, targetCRS);
				TRANSFORM_CACHE.put(transformKey, transform);
			} catch (final Exception e) {
				throw GamaRuntimeException.error("No transformation found from " + sourceCode + " to " + targetcode, scope);
			}
		}
		Geometry targetGeometry = null;
		try {
			targetGeometry = JTS.transform(g.getInnerGeometry(), transform);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("No transformation found from " + sourceCode + " to " + targetcode, scope);
		}
		if (targetGeometry == null) return null;
		final IShape s = GamaShapeFactory.createFrom(targetGeometry);
		if (g instanceof IPoint) return s.getLocation();
		return s;
	}
}
