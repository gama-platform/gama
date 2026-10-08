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

import gama.api.utils.interfaces.IDisposable;

/**
 * The Interface IIntersectable.
 *
 * <p>
 * Defines the ability of a geometry or spatial object to test intersection with envelopes and coordinates.
 * </p>
 */
public interface IIntersectable extends IDisposable {

	/**
	 * Tests intersection in full 3D (X, Y and Z are all considered).
	 *
	 * @param env
	 *            the JTS envelope to test against
	 * @return {@code true} if this object intersects {@code env}
	 */
	boolean intersects(Envelope env);

	/**
	 * Tests intersection in full 3D (X, Y and Z are all considered).
	 *
	 * @param env
	 *            the envelope to test against
	 * @return {@code true} if this object intersects {@code env}
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

	/**
	 * Tests intersection considering only the X and Y dimensions (Z is ignored).
	 *
	 * <p>
	 * This is required by 2D spatial indexes (such as the quadtree) that partition space in XY only but store agents
	 * that may have non-zero Z coordinates (3D worlds). Using a full 3D intersection check in those contexts would
	 * incorrectly exclude agents whose Z range does not overlap the 2D search envelope's Z=[0,0].
	 * </p>
	 *
	 * <p>
	 * The default implementation delegates to {@link Envelope#intersects(Envelope)} (the JTS 2D method) by casting
	 * both objects to {@link Envelope}. Implementations may override this for efficiency.
	 * </p>
	 *
	 * @param env
	 *            the envelope to test against (treated as 2D — Z is ignored)
	 * @return {@code true} if the XY projections of this object and {@code env} overlap
	 */
	default boolean intersects2D(final IEnvelope env) {
		if (this instanceof Envelope self && env instanceof Envelope other) return self.intersects(other);
		// Fallback: use full 3D intersects (suboptimal but safe)
		return intersects(env);
	}

}
