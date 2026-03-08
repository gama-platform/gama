/*******************************************************************************************************
 *
 * IProjectionFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import java.util.Map;

import javax.measure.UnitConverter;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IEnvelope;

/**
 * The Interface IProjectionFactory.
 * 
 * <p>This factory interface is responsible for creating and managing projection objects and
 * Coordinate Reference Systems (CRS) in GAMA. It serves as the central point for handling
 * all projection-related operations, from CRS lookup to projection creation.</p>
 * 
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li><strong>CRS Lookup:</strong> Retrieve CRS by EPSG code or WKT string</li>
 *   <li><strong>Projection Creation:</strong> Build projection objects for coordinate transformations</li>
 *   <li><strong>World Projection Management:</strong> Manage the global simulation projection</li>
 *   <li><strong>Save Projections:</strong> Create projections for data export</li>
 *   <li><strong>Unit Conversion:</strong> Provide unit converters for measurement transformations</li>
 * </ul>
 * 
 * <h3>Common CRS Operations:</h3>
 * <ul>
 *   <li>Get CRS by EPSG code (e.g., 4326 for WGS84, 3857 for Web Mercator)</li>
 *   <li>Get CRS by string code (e.g., "EPSG:4326")</li>
 *   <li>Control coordinate axis order (longitude-first vs latitude-first)</li>
 *   <li>Compute default CRS for simulation world</li>
 * </ul>
 * 
 * <h3>Projection Creation Workflows:</h3>
 * <ul>
 *   <li><strong>From Parameters:</strong> Create projection from map of configuration parameters</li>
 *   <li><strong>From CRS:</strong> Create projection from a specific CRS</li>
 *   <li><strong>For Saving:</strong> Create projection for exporting data in a specific format</li>
 * </ul>
 * 
 * <h3>Usage Pattern:</h3>
 * <pre>
 * // Get a CRS by EPSG code
 * ICoordinateReferenceSystem crs = factory.getCRS(scope, 4326);
 * 
 * // Create a projection for the simulation
 * IProjection projection = factory.fromCRS(scope, crs, envelope);
 * 
 * // Create a projection for saving results
 * IProjection saveProjection = factory.forSavingWith(scope, 3857);
 * </pre>
 * 
 * <p>This interface abstracts the complexity of working with GeoTools and provides a
 * GAMA-friendly API for projection management.</p>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.6
 * 
 * @see IProjection
 * @see ICoordinateReferenceSystem
 */
public interface IProjectionFactory {

	/**
	 * Sets the world projection envelope for the simulation.
	 * 
	 * <p>Defines the spatial extent (bounding box) of the simulation world in the target
	 * projection. This envelope is used to determine the coordinate bounds for the simulation
	 * and affects how spatial data is loaded and positioned.</p>
	 * 
	 * <p>This should be called during simulation initialization, typically after loading
	 * the first spatial dataset that defines the world extent.</p>
	 *
	 * @param scope the current execution scope
	 * @param env the envelope defining the world projection bounds
	 */
	void setWorldProjectionEnv(IScope scope, IEnvelope env);

	/**
	 * Gets the target CRS for the simulation world.
	 * 
	 * <p>Returns the Coordinate Reference System that the simulation uses as its target
	 * projection. All loaded spatial data is transformed to this CRS for consistent
	 * spatial operations.</p>
	 *
	 * @param scope the current execution scope
	 * @return the target CRS for the world
	 */
	ICoordinateReferenceSystem getTargetCRS(IScope scope);

	/**
	 * Gets the CRS to use when saving spatial data.
	 * 
	 * <p>Returns the Coordinate Reference System that should be used when exporting or
	 * saving simulation results to files. This may be different from the target CRS
	 * if results need to be saved in a specific projection format.</p>
	 *
	 * @param scope the current execution scope
	 * @return the CRS for saving data
	 */
	ICoordinateReferenceSystem getSaveCRS(IScope scope);

	/**
	 * Gets a CRS by EPSG code.
	 * 
	 * <p>Retrieves a Coordinate Reference System using its EPSG (European Petroleum Survey Group)
	 * numeric identifier. EPSG codes are standardized identifiers for coordinate systems.</p>
	 * 
	 * <p>Common EPSG codes:</p>
	 * <ul>
	 *   <li>4326: WGS84 (GPS latitude/longitude)</li>
	 *   <li>3857: Web Mercator (used by Google Maps, OpenStreetMap)</li>
	 *   <li>32633: UTM Zone 33N</li>
	 * </ul>
	 *
	 * @param scope the current execution scope
	 * @param code the EPSG numeric code
	 * @return the corresponding CRS, or null if not found
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, int code);

	/**
	 * Gets a CRS by EPSG code with axis order control.
	 * 
	 * <p>Similar to {@link #getCRS(IScope, int)}, but allows control over the coordinate
	 * axis order. Some CRS definitions specify latitude-first, but longitude-first is
	 * more intuitive and commonly used in GIS applications.</p>
	 *
	 * @param scope the current execution scope
	 * @param code the EPSG numeric code
	 * @param longitudeFirst true to force longitude-first axis order (X, Y), false for definition order
	 * @return the corresponding CRS with specified axis order
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, int code, boolean longitudeFirst);

	/**
	 * Gets a CRS by string code identifier.
	 * 
	 * <p>Retrieves a CRS using a string identifier, which can be:</p>
	 * <ul>
	 *   <li>An EPSG code string (e.g., "EPSG:4326")</li>
	 *   <li>A Well-Known Text (WKT) definition</li>
	 *   <li>Other CRS naming conventions</li>
	 * </ul>
	 *
	 * @param scope the current execution scope
	 * @param code the CRS code as a string
	 * @return the corresponding CRS, or null if not found
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, String code);

	/**
	 * Gets a CRS by string code with axis order control.
	 * 
	 * <p>Combines string-based CRS lookup with axis order specification.</p>
	 *
	 * @param scope the current execution scope
	 * @param code the CRS code as a string
	 * @param longitudeFirst true to force longitude-first axis order (X, Y), false for definition order
	 * @return the corresponding CRS with specified axis order
	 * @throws GamaRuntimeException if the CRS cannot be found or parsed
	 */
	ICoordinateReferenceSystem getCRS(IScope scope, String code, boolean longitudeFirst) throws GamaRuntimeException;

	/**
	 * Gets the world projection.
	 * 
	 * <p>Returns the current projection object used by the simulation world. This is the
	 * main projection that handles transformations between loaded data CRS and the
	 * simulation's target CRS.</p>
	 *
	 * @return the world projection, or null if not yet initialized
	 * @author Thai Truong Ming
	 * @date 03-01-2014
	 */
	/*
	 * Thai.truongming@gmail.com ---------------begin date: 03-01-2014
	 */
	IProjection getWorld();

	/**
	 * Computes a default CRS for the simulation world.
	 * 
	 * <p>Creates a CRS based on the provided EPSG code, using it either as the initial
	 * or target CRS depending on the target parameter. This is used to establish a
	 * default projection when none is explicitly specified.</p>
	 *
	 * @param scope the current execution scope
	 * @param code the EPSG code for the CRS
	 * @param target true to use as target CRS, false to use as initial CRS
	 * @return the computed default CRS
	 * @author Thai Truong Ming
	 * @date 03-01-2014
	 */
	/*
	 * thai.truongming@gmail.com -----------------end
	 */
	ICoordinateReferenceSystem computeDefaultCRS(IScope scope, int code, boolean target);

	/**
	 * Creates a projection from a parameter map.
	 * 
	 * <p>Builds a projection object using configuration parameters provided in a map.
	 * This allows flexible projection creation from various sources (files, user input, etc.).</p>
	 * 
	 * <p>Common parameters might include:</p>
	 * <ul>
	 *   <li>Source CRS identifier</li>
	 *   <li>Target CRS identifier</li>
	 *   <li>Translation offsets</li>
	 *   <li>Unit conversion factors</li>
	 * </ul>
	 *
	 * @param scope the current execution scope
	 * @param params map of projection configuration parameters
	 * @param env the envelope defining the projection extent
	 * @return a new projection configured according to the parameters
	 */
	IProjection fromParams(IScope scope, Map<String, Object> params, IEnvelope env);

	/**
	 * Creates a projection from a CRS.
	 * 
	 * <p>Builds a projection object that transforms from the given CRS to the simulation's
	 * target CRS. This is commonly used when loading spatial data with a known source CRS.</p>
	 *
	 * @param scope the current execution scope
	 * @param crs the source Coordinate Reference System
	 * @param env the envelope defining the projection extent
	 * @return a new projection from the source CRS to the target CRS
	 */
	IProjection fromCRS(IScope scope, ICoordinateReferenceSystem crs, IEnvelope env);

	/**
	 * Creates a projection for saving data with an EPSG code.
	 * 
	 * <p>Builds a projection that transforms simulation data to the specified target CRS
	 * for export or file saving. This allows saving results in a different projection
	 * than used during simulation.</p>
	 *
	 * @param scope the current execution scope
	 * @param epsgCode the EPSG code of the target CRS for saving
	 * @return a projection for saving data in the specified CRS
	 */
	IProjection forSavingWith(IScope scope, Integer epsgCode);

	/**
	 * Creates a projection for saving data with an EPSG code and axis order control.
	 * 
	 * <p>Similar to {@link #forSavingWith(IScope, Integer)}, but allows specification
	 * of the axis order in the output CRS.</p>
	 *
	 * @param scope the current execution scope
	 * @param epsgCode the EPSG code of the target CRS for saving
	 * @param lonFirst true for longitude-first axis order, false for definition order
	 * @return a projection for saving data in the specified CRS with specified axis order
	 */
	IProjection forSavingWith(IScope scope, Integer epsgCode, boolean lonFirst);

	/**
	 * Creates a projection for saving data with a string CRS code.
	 * 
	 * <p>Builds a projection using a string-based CRS identifier for the save operation.</p>
	 *
	 * @param scope the current execution scope
	 * @param code the CRS code as a string (e.g., "EPSG:4326")
	 * @return a projection for saving data in the specified CRS
	 */
	IProjection forSavingWith(IScope scope, String code);

	/**
	 * Creates a projection for saving data with a CRS object.
	 * 
	 * <p>Builds a projection using a CRS object directly for the save operation.</p>
	 *
	 * @param scope the current execution scope
	 * @param crs the target CRS for saving
	 * @return a projection for saving data in the specified CRS
	 */
	IProjection forSavingWith(IScope scope, ICoordinateReferenceSystem crs);

	/**
	 * Creates a projection for saving data with a string CRS code and axis order control.
	 * 
	 * <p>Combines string-based CRS specification with axis order control for save projections.</p>
	 *
	 * @param scope the current execution scope
	 * @param code the CRS code as a string
	 * @param lonFirst true for longitude-first axis order, false for definition order
	 * @return a projection for saving data in the specified CRS with specified axis order
	 */
	IProjection forSavingWith(IScope scope, String code, boolean lonFirst);

	/**
	 * Gets the default initial CRS for spatial data.
	 * 
	 * <p>Returns the CRS that is assumed for spatial data when no explicit CRS is specified.
	 * This is typically WGS84 (EPSG:4326) for geographic data.</p>
	 *
	 * @param scope the current execution scope
	 * @return the default initial CRS
	 */
	ICoordinateReferenceSystem getDefaultInitialCRS(IScope scope);

	/**
	 * Tests the consistency between a CRS and an envelope.
	 * 
	 * <p>Validates that the given envelope's coordinate values are reasonable for the
	 * specified CRS. For example, latitude values should be between -90 and 90 for
	 * geographic CRS.</p>
	 * 
	 * <p>This method may issue warnings or errors if inconsistencies are detected, helping
	 * to identify data loading problems early.</p>
	 *
	 * @param scope the current execution scope
	 * @param crs the CRS to test against
	 * @param env the envelope to validate
	 */
	void testConsistency(IScope scope, ICoordinateReferenceSystem crs, IEnvelope env);

	/**
	 * Gets the unit converter for measurement transformations.
	 * 
	 * <p>Returns a converter that can transform values between different measurement units
	 * (e.g., meters to feet, degrees to radians). This is used during projection operations
	 * to ensure consistent units throughout the simulation.</p>
	 * 
	 * <p>The converter is typically configured based on the source and target CRS unit definitions.</p>
	 *
	 * @return the unit converter for the current projection context
	 */
	UnitConverter getUnitConverter();

}