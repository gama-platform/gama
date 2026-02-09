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
 * This interface represents the only link with GeoTools of the whole API
 */
@FunctionalInterface
public interface ICoordinateReferenceSystem {

	/**
	 * @return
	 */
	CoordinateReferenceSystem getCRS();

	/**
	 * Checks if is null.
	 *
	 * @return true, if is null
	 */
	default boolean isNull() { return getCRS() == null; }

	/**
	 * @return
	 */
	default String getCode() { return getCRS() == null ? null : getCRS().getName().getCode(); }

	/**
	 * Equals.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	default boolean equals(final ICoordinateReferenceSystem other) {
		if (other == null) return false;
		CoordinateReferenceSystem crs1 = getCRS();
		CoordinateReferenceSystem crs2 = other.getCRS();
		return crs1 == null && crs2 == null || crs1 != null && crs1.equals(crs2);
	}

}
