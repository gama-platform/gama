/*******************************************************************************************************
 *
 * GamaCRS.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.gis;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import gama.api.kernel.topology.ICoordinateReferenceSystem;

/**
 *
 */
public record GamaCRS(CoordinateReferenceSystem crs) implements ICoordinateReferenceSystem {

	@Override
	public String toString() {
		return crs.toWKT();
	}

	@Override
	public CoordinateReferenceSystem getCRS() { return crs; }

}
