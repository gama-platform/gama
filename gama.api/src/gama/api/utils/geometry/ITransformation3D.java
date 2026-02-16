/*******************************************************************************************************
 *
 * ITransformation3D.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import org.locationtech.jts.geom.CoordinateFilter;

import gama.api.types.geometry.IPoint;

/**
 * The Interface ITransformation3D.
 */
public interface ITransformation3D extends CoordinateFilter {

	/**
	 * Apply to.
	 *
	 * @param vertex
	 *            the vertex
	 */
	default void applyTo(final IPoint vertex) {
		filter(vertex.toCoordinate());
	}
}
