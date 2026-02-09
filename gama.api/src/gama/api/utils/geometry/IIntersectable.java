/*******************************************************************************************************
 *
 * IIntersectable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import gama.api.data.objects.IEnvelope;
import gama.api.utils.IDisposable;

/**
 * The Interface IIntersectable.
 */
public interface IIntersectable extends IDisposable {

	/**
	 * Intersects.
	 *
	 * @param env
	 *            the env
	 * @return true, if successful
	 */
	boolean intersects(Envelope env);

	/**
	 * Intersects.
	 *
	 * @param env
	 *            the env
	 * @return true, if successful
	 */
	boolean intersects(IEnvelope env);

	/**
	 * Intersects.
	 *
	 * @param env
	 *            the env
	 * @return true, if successful
	 */
	boolean intersects(Coordinate env);

}
