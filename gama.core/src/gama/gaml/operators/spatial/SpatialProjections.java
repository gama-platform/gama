package gama.gaml.operators.spatial;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.no_test;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.projection.IProjection;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.GamaFile;
import gama.core.util.file.GamaGisFile;

/**
 * The Class Projections.
 */
public class SpatialProjections {

	/** The Constant THE_CODE. */
	private static final String THE_CODE = "The code ";

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
	public static String crsFromFile(final IScope scope, final GamaFile gisFile) {
		if (!(gisFile instanceof GamaGisFile))
			throw GamaRuntimeException.error("Impossible to compute the CRS for this type of file", scope);
		final CoordinateReferenceSystem crs = ((GamaGisFile) gisFile).getGis(scope).getInitialCRS(scope);
		if (crs == null) return null;
		try {
			return CRS.lookupIdentifier(crs, true);
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
		if (g instanceof GamaPoint) return s.getLocation();
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
		if (g instanceof GamaPoint) return s.getLocation();
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
		} catch (final FactoryException e) {
			throw GamaRuntimeException.error(THE_CODE + code + " does not correspond to a known EPSG code", scope);
		}
		if (gis == null) return g.copy(scope);
		final IShape s = GamaShapeFactory.createFrom(gis.transform(g.getInnerGeometry()));
		if (g instanceof GamaPoint) return s.getLocation();
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
		} catch (final FactoryException e) {
			throw GamaRuntimeException.error(THE_CODE + code + " does not correspond to a known EPSG code", scope);
		}
		if (gis == null) return g.copy(scope);
		final IShape s = GamaShapeFactory.createFrom(gis.inverseTransform(g.getInnerGeometry()));
		if (g instanceof GamaPoint) return s.getLocation();
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
		CoordinateReferenceSystem sourceCRS;
		try {
			sourceCRS = CRS.decode(sourceCode);
		} catch (final FactoryException e) {
			throw GamaRuntimeException.error(THE_CODE + sourceCode + " does not correspond to a known EPSG code",
					scope);
		}
		CoordinateReferenceSystem targetCRS;
		try {
			targetCRS = CRS.decode(targetcode);
		} catch (final FactoryException e) {
			throw GamaRuntimeException.error(THE_CODE + targetcode + " does not correspond to a known EPSG code",
					scope);
		}

		MathTransform transform;
		Geometry targetGeometry = null;
		try {
			transform = CRS.findMathTransform(sourceCRS, targetCRS);
			targetGeometry = JTS.transform(g.getInnerGeometry(), transform);
		} catch (final Exception e) {
			throw GamaRuntimeException.error("No transformation found from " + sourceCode + " to " + targetcode,
					scope);
		}
		if (targetGeometry == null) return null;
		final IShape s = GamaShapeFactory.createFrom(targetGeometry);
		if (g instanceof GamaPoint) return s.getLocation();
		return s;
	}
}
