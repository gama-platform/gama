/*******************************************************************************************************
 *
 * IProjection.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import org.locationtech.jts.geom.Geometry;

import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IEnvelope;

/**
 * The Interface IProjection.
 * 
 * <p>This interface manages coordinate transformations between different Coordinate Reference Systems (CRS).
 * It handles the conversion of geographic data from one projection to another, which is essential when
 * working with spatial data from multiple sources that use different coordinate systems.</p>
 * 
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li><strong>Coordinate Transformation:</strong> Convert geometries between source and target CRS</li>
 *   <li><strong>Translation:</strong> Shift coordinates to align with simulation space</li>
 *   <li><strong>Unit Conversion:</strong> Convert between different measurement units (meters, degrees, etc.)</li>
 *   <li><strong>CRS Management:</strong> Track both initial (source) and target coordinate systems</li>
 * </ul>
 * 
 * <h3>Common Projection Scenarios:</h3>
 * <ul>
 *   <li>Loading a shapefile in WGS84 (lat/lon) and projecting to UTM (meters)</li>
 *   <li>Aligning multiple datasets with different projections</li>
 *   <li>Converting between geographic (degrees) and projected (meters) coordinates</li>
 *   <li>Saving simulation results in a specific projection for GIS compatibility</li>
 * </ul>
 * 
 * <h3>Transformation Pipeline:</h3>
 * <p>A typical transformation involves several steps:</p>
 * <ol>
 *   <li>Inverse unit conversion (to base units)</li>
 *   <li>Inverse translation (to original position)</li>
 *   <li>CRS transformation (projection change)</li>
 *   <li>Translation (to simulation space)</li>
 *   <li>Unit conversion (to target units)</li>
 * </ol>
 * 
 * <h3>Performance Notes:</h3>
 * <p>Projection transformations can be computationally expensive, especially for complex geometries
 * with many vertices. Implementations should cache transformation objects and minimize unnecessary
 * re-projections.</p>
 * 
 * <p><strong>Note:</strong> This interface has a dependency on GeoTools for CRS handling. Future versions
 * may abstract this dependency further.</p>
 *
 * @author Alexis Drogoul
 * @since 17 déc. 2013
 * 
 * @see ICoordinateReferenceSystem
 * @see IProjectionFactory
 */
public interface IProjection {

	/**
	 * Transforms a geometry from the initial CRS to the target CRS.
	 * 
	 * <p>Applies the complete forward transformation pipeline to convert the geometry's coordinates
	 * from the source coordinate system to the target coordinate system used by the simulation.</p>
	 * 
	 * <p>This typically involves:</p>
	 * <ol>
	 *   <li>Projecting from initial CRS to target CRS</li>
	 *   <li>Translating to simulation coordinate space</li>
	 *   <li>Converting units as needed</li>
	 * </ol>
	 * 
	 * <p>The input geometry is not modified; a new transformed geometry is returned.</p>
	 *
	 * @param g the geometry to transform (in initial CRS)
	 * @return the transformed geometry (in target CRS), or the original if no transformation is needed
	 */
	Geometry transform(final Geometry g);

	/**
	 * Inverse transforms a geometry from the target CRS back to the initial CRS.
	 * 
	 * <p>Applies the reverse transformation pipeline to convert coordinates from the simulation's
	 * target coordinate system back to the original source coordinate system. This is useful when
	 * saving simulation results in the original projection or when exporting data.</p>
	 * 
	 * <p>This operation reverses the forward transform:</p>
	 * <ol>
	 *   <li>Inverse unit conversion</li>
	 *   <li>Inverse translation</li>
	 *   <li>Reverse projection (target CRS → initial CRS)</li>
	 * </ol>
	 *
	 * @param g the geometry to inverse transform (in target CRS)
	 * @return the inverse transformed geometry (in initial CRS), or the original if no transformation is needed
	 */
	Geometry inverseTransform(final Geometry g);

	/**
	 * Gets the initial (source) Coordinate Reference System.
	 * 
	 * <p>Returns the CRS of the original data before any transformations. This is typically
	 * the CRS specified in shapefiles, GeoJSON, or other spatial data sources.</p>
	 *
	 * @param scope the current execution scope
	 * @return the initial CRS, or null if not defined
	 */
	ICoordinateReferenceSystem getInitialCRS(IScope scope);

	/**
	 * Gets the target Coordinate Reference System.
	 * 
	 * <p>Returns the CRS used by the simulation environment. All spatial data is transformed
	 * to this target CRS for consistent spatial operations within the model.</p>
	 *
	 * @param scope the current execution scope
	 * @return the target CRS, or null if using default Cartesian coordinates
	 */
	ICoordinateReferenceSystem getTargetCRS(IScope scope);

	/**
	 * Gets the projected envelope (bounding box).
	 * 
	 * <p>Returns the envelope (bounding rectangle) of the projection in target CRS coordinates.
	 * This defines the spatial extent of the simulation world after projection.</p>
	 *
	 * @return the projected envelope defining the simulation bounds
	 */
	IEnvelope getProjectedEnvelope();

	/**
	 * Translates (shifts) a geometry to align with simulation coordinates.
	 * 
	 * <p>Applies a translation offset to move the geometry from its original position to the
	 * simulation coordinate space. This is typically used to center or align spatial data
	 * within the simulation environment.</p>
	 * 
	 * <p>The geometry is modified in place.</p>
	 *
	 * @param geom the geometry to translate (modified in place)
	 */
	void translate(Geometry geom);

	/**
	 * Inverse translates a geometry back to its original position.
	 * 
	 * <p>Reverses the translation applied by {@link #translate}, moving the geometry from
	 * simulation coordinates back to its original geographic position. This is used when
	 * exporting or saving simulation results in their original spatial reference.</p>
	 * 
	 * <p>The geometry is modified in place.</p>
	 *
	 * @param geom the geometry to inverse translate (modified in place)
	 */
	void inverseTranslate(Geometry geom);

	/**
	 * Converts the geometry's coordinates to the target measurement units.
	 * 
	 * <p>Applies unit conversion to scale coordinates to the target unit system. For example,
	 * converting from degrees to meters, or from feet to meters. This ensures all spatial
	 * calculations use consistent units.</p>
	 * 
	 * <p>The geometry is modified in place.</p>
	 *
	 * @param geom the geometry to convert units (modified in place)
	 */
	void convertUnit(Geometry geom);

	/**
	 * Inverse converts the geometry's coordinates back to the original measurement units.
	 * 
	 * <p>Reverses the unit conversion applied by {@link #convertUnit}, scaling coordinates
	 * back to the original unit system. This is necessary when saving results in the
	 * original data format.</p>
	 * 
	 * <p>The geometry is modified in place.</p>
	 *
	 * @param geom the geometry to inverse convert units (modified in place)
	 */
	void inverseConvertUnit(Geometry geom);

}