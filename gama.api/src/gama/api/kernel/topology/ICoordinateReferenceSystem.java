/*******************************************************************************************************
 *
 * ICoordinateReferenceSystem.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

/**
 * The Interface ICoordinateReferenceSystem.
 * 
 * <p>This functional interface provides a wrapper around GeoTools Coordinate Reference System (CRS) objects,
 * representing the only link with GeoTools in the GAMA API. A Coordinate Reference System defines how coordinates
 * are mapped to locations on Earth, including the projection and datum used.</p>
 * 
 * <p>This interface is used throughout GAMA to manage spatial projections and transformations when working with
 * geographic data such as shapefiles, rasters, and other georeferenced datasets.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Wraps GeoTools CRS objects in a GAMA-compatible interface</li>
 *   <li>Provides utility methods for CRS comparison and validation</li>
 *   <li>Supports extraction of CRS code identifiers</li>
 *   <li>Handles null CRS scenarios gracefully</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * 
 * @see IProjection
 * @see IProjectionFactory
 */
@FunctionalInterface
public interface ICoordinateReferenceSystem {

	/**
	 * Gets the underlying GeoTools Coordinate Reference System.
	 * 
	 * <p>This is the primary method of the functional interface. Implementations must provide
	 * the actual CRS object used for spatial transformations and coordinate operations.</p>
	 *
	 * @return the CoordinateReferenceSystem object from GeoTools, or null if no CRS is defined
	 */
	CoordinateReferenceSystem getCRS();

	/**
	 * Checks if the CRS is null.
	 * 
	 * <p>This is a convenience method to determine whether the underlying CRS object is defined.
	 * A null CRS typically indicates that no spatial projection has been set, which may mean
	 * the simulation is using default Cartesian coordinates without geographic reference.</p>
	 *
	 * @return true if the underlying CRS is null, false otherwise
	 */
	default boolean isNull() { return getCRS() == null; }

	/**
	 * Gets the code identifier of the CRS.
	 * 
	 * <p>Returns the code name of the Coordinate Reference System, typically an EPSG code
	 * (e.g., "EPSG:4326" for WGS84 latitude/longitude). This code uniquely identifies
	 * the projection and datum used by the CRS.</p>
	 * 
	 * <p>This is useful for debugging, logging, and displaying CRS information to users.</p>
	 *
	 * @return the CRS code as a String (e.g., "EPSG:4326"), or null if the CRS is null
	 */
	default String getCode() { return getCRS() == null ? null : getCRS().getName().getCode(); }

	/**
	 * Compares this CRS with another for equality.
	 * 
	 * <p>Two CRS objects are considered equal if:</p>
	 * <ul>
	 *   <li>Both underlying CRS objects are null, OR</li>
	 *   <li>Both underlying CRS objects are non-null and equal according to their own equals() method</li>
	 * </ul>
	 * 
	 * <p>This method is essential for verifying CRS compatibility when combining spatial data from
	 * different sources or when checking if a transformation is needed between coordinate systems.</p>
	 *
	 * @param other the other ICoordinateReferenceSystem to compare with
	 * @return true if the CRS objects are equal, false otherwise (including when other is null)
	 */
	default boolean equals(final ICoordinateReferenceSystem other) {
		if (other == null) return false;
		CoordinateReferenceSystem crs1 = getCRS();
		CoordinateReferenceSystem crs2 = other.getCRS();
		return crs1 == null && crs2 == null || crs1 != null && crs1.equals(crs2);
	}

}
