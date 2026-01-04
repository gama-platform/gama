/*******************************************************************************************************
 *
 * GamaEnvelopeFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.common.geometry;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import gama.core.common.util.PoolUtils;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;

/**
 *
 */
public class GamaEnvelopeFactory {

	/** The Constant POOL. */
	private static final PoolUtils.ObjectPool<Envelope3D> POOL =
			PoolUtils.create("Envelope 3D", true, Envelope3D::new, (from, to) -> to.set(from), null);

	/** The Constant EMPTY. */
	public static final IEnvelope EMPTY = create();

	/**
	 * Creates the.
	 *
	 * @return the envelope 3 D
	 */
	public static Envelope3D create() {
		return POOL.get();
	}

	/**
	 * Of.
	 *
	 * @param g
	 *            the g
	 * @return the envelope 3 D
	 */
	public static IEnvelope of(final Geometry g) {
		if (g instanceof GeometryCollection gc) return of(gc);
		final ICoordinates sq = GeometryUtils.getContourCoordinates(g);
		return sq.getEnvelope();
	}

	/**
	 * Of.
	 *
	 * @param g
	 *            the g
	 * @return the envelope 3 D
	 */
	public static IEnvelope of(final GeometryCollection g) {
		final int i = g.getNumGeometries();
		if (i == 0) return EMPTY;
		final IEnvelope result = of(g.getGeometryN(0));
		for (int j = 1; j < i; j++) { result.expandToInclude(of(g.getGeometryN(j))); }
		return result;
	}

	/**
	 * Of.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param list
	 *            the list
	 * @return the envelope 3 D
	 * @date 18 juil. 2023
	 */
	public static IEnvelope of(final List<IShape> list) {
		final int i = list.size();
		if (i == 0) return EMPTY;
		final IEnvelope result = of(list.get(0));
		for (int j = 1; j < i; j++) { result.expandToInclude(GamaEnvelopeFactory.of(list.get(j))); }
		return result;
	}

	/**
	 * Of.
	 *
	 * @param s
	 *            the s
	 * @return the envelope 3 D
	 */
	public static IEnvelope of(final IShape s) {
		return of(s.getInnerGeometry());
	}

	/**
	 * Of.
	 *
	 * @param s
	 *            the s
	 * @return the envelope 3 D
	 */
	public static IEnvelope of(final GamaPoint s) {
		return of((Coordinate) s);
	}

	/**
	 * Of.
	 *
	 * @param e
	 *            the e
	 * @return the envelope 3 D
	 */
	public static IEnvelope of(final Envelope e) {
		final IEnvelope env = create();
		env.init(e);
		return env;
	}

	/**
	 * Of.
	 *
	 * @param e
	 *            the e
	 * @return the i envelope
	 */
	public static IEnvelope of(final IEnvelope e) {
		final IEnvelope env = create();
		env.init(e);
		return env;
	}

	/**
	 * With Y negated.
	 *
	 * @param e
	 *            the e
	 * @return the envelope 3 D
	 */
	public static IEnvelope withYNegated(final IEnvelope e) {
		final IEnvelope env = create();
		env.init(e);
		env.init(env.getMinX(), env.getMaxX(), -env.getMinY(), -env.getMaxY(), env.getMinZ(), env.getMaxZ());
		return env;
	}

	/**
	 * Of.
	 *
	 * @param p
	 *            the p
	 * @return the envelope 3 D
	 */
	public static IEnvelope of(final Coordinate p) {
		final IEnvelope env = create();
		env.init(p);
		return env;
	}

	/**
	 * Of.
	 *
	 * @param x1
	 *            the x 1
	 * @param x2
	 *            the x 2
	 * @param y1
	 *            the y 1
	 * @param y2
	 *            the y 2
	 * @param z1
	 *            the z 1
	 * @param z2
	 *            the z 2
	 * @return the envelope 3 D
	 */
	public static Envelope3D of(final double x1, final double x2, final double y1, final double y2, final double z1,
			final double z2) {
		final Envelope3D env = create();
		env.init(x1, x2, y1, y2, z1, z2);
		return env;
	}

	/**
	 * Of.
	 *
	 * @param x1
	 *            the x 1
	 * @param x2
	 *            the x 2
	 * @param y1
	 *            the y 1
	 * @param y2
	 *            the y 2
	 * @return the i envelope
	 */
	public static IEnvelope of(final double x1, final double x2, final double y1, final double y2) {
		return of(x1, x2, y1, y2, 0, 0);
	}

	/**
	 * @param envelope3d
	 */
	public static void release(final Envelope3D envelope3d) {
		POOL.release(envelope3d);

	}

}
