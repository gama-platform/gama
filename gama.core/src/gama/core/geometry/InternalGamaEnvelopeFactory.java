/*******************************************************************************************************
 *
 * InternalGamaEnvelopeFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.geometry;

import static gama.api.data.factories.GamaEnvelopeFactory.EMPTY;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import gama.api.data.factories.IEnvelopeFactory;
import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IList;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.types.GamaFileType;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.utils.PoolUtils;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GamaCoordinateSequenceFactory;
import gama.api.utils.geometry.IEnvelopeComputer;
import gama.api.utils.geometry.IEnvelopeProvider;

/**
 *
 */
public class InternalGamaEnvelopeFactory implements IEnvelopeFactory {

	/** The envelope computers. */
	private static List<IEnvelopeComputer> envelopeComputers = new ArrayList<>();

	/** The Constant POOL. */
	private static PoolUtils.ObjectPool<IEnvelope> POOL;

	/**
	 * Gets the pool.
	 *
	 * @return the pool
	 */
	private static PoolUtils.ObjectPool<IEnvelope> getPool() {
		if (POOL == null) {
			POOL = PoolUtils.create("Envelope 3D", true, GamaEnvelope::new, (from, to) -> to.set(from), null);
		}
		return POOL;
	}

	/**
	 * The Class Immutable.
	 */
	static class Immutable extends GamaEnvelope {

		/**
		 * Instantiates a new immutable.
		 *
		 * @param minX
		 *            the min X
		 * @param maxX
		 *            the max X
		 * @param minY
		 *            the min Y
		 * @param maxY
		 *            the max Y
		 */
		Immutable(final double minX, final double maxX, final double minY, final double maxY) {
			super.init(minX, maxX, minY, maxY);
		}

		@Override
		public void expandToInclude(final IPoint p) {}

		@Override
		public void dispose() {}

		@Override
		public void init(final double x1, final double x2, final double y1, final double y2, final double z1,
				final double z2) {}

		@Override
		public void init(final Coordinate p1, final Coordinate p2) {}

		@Override
		public void init(final Coordinate p) {}

		@Override
		public void init(final Envelope env) {}

		@Override
		public void init(final IEnvelope env) {}

		@Override
		public IEnvelope set(final IEnvelope env) {
			return this;
		}

		@Override
		public void setToNull() {}

		@Override
		public void expandToInclude(final Coordinate p) {}

		@Override
		public void expandBy(final double distance) {}

		@Override
		public void expandBy(final double deltaX, final double deltaY, final double deltaZ) {}

		@Override
		public void expandToInclude(final double x, final double y, final double z) {}

		@Override
		public IEnvelope translate(final double transX, final double transY, final double transZ) {
			return this;
		}

		@Override
		public void expandToInclude(final Envelope other) {}

		@Override
		public void expandToInclude(final IEnvelope ie) {}

		@Override
		public IEnvelope rotate(final AxisAngle rotation) {
			return this;
		}

		@Override
		public void init() {}

		@Override
		public void init(final double x1, final double x2, final double y1, final double y2) {}

		@Override
		public void expandBy(final double deltaX, final double deltaY) {}

		@Override
		public void expandToInclude(final double x, final double y) {}

		@Override
		public void translate(final double transX, final double transY) {}

	}

	/**
	 * Creates the.
	 *
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope create() {
		return getPool().get();
	}

	/**
	 * Of.
	 *
	 * @param g
	 *            the g
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope of(final Geometry g) {
		if (g instanceof GeometryCollection gc) return of(gc);
		final ICoordinates sq = GamaCoordinateSequenceFactory.pointsOf(g);
		return sq.getEnvelope();
	}

	/**
	 * Of.
	 *
	 * @param g
	 *            the g
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope of(final GeometryCollection g) {
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
	@Override
	public IEnvelope of(final List<IShape> list) {
		final int i = list.size();
		if (i == 0) return EMPTY;
		final IEnvelope result = of(list.get(0));
		for (int j = 1; j < i; j++) { result.expandToInclude(of(list.get(j))); }
		return result;
	}

	/**
	 * Of.
	 *
	 * @param s
	 *            the s
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope of(final IShape s) {
		return of(s.getInnerGeometry());
	}

	/**
	 * Of.
	 *
	 * @param s
	 *            the s
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope of(final IPoint s) {
		return of(s.toCoordinate());
	}

	/**
	 * Of.
	 *
	 * @param e
	 *            the e
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope of(final Envelope e) {
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
	@Override
	public IEnvelope of(final IEnvelope e) {
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
	@Override
	public IEnvelope withYNegated(final IEnvelope e) {
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
	@Override
	public IEnvelope of(final Coordinate p) {
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
	@Override
	public IEnvelope of(final double x1, final double x2, final double y1, final double y2, final double z1,
			final double z2) {
		final IEnvelope env = create();
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
	@Override
	public IEnvelope of(final double x1, final double x2, final double y1, final double y2) {
		return of(x1, x2, y1, y2, 0, 0);
	}

	/**
	 * Of immutable.
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
	@Override
	public IEnvelope ofImmutable(final double x1, final double x2, final double y1, final double y2) {
		return new Immutable(x1, x2, y1, y2);
	}

	/**
	 * @param envelope3d
	 */
	@Override
	public void release(final IEnvelope envelope3d) {
		getPool().release(envelope3d);

	}

	/**
	 * Adds the envelope computer.
	 *
	 * @param ec
	 *            the ec
	 */
	public static void addEnvelopeComputer(final IEnvelopeComputer ec) {
		envelopeComputers.add(ec);
	}

	// ---------------------------------------------------------------------------------------------
	// Thai.truongminh@gmail.com
	// Created date:24-Feb-2013: Process for SQL - MAP type
	// Modified: 03-Jan-2014

	/**
	 * Compute envelope from.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope computeEnvelopeFrom(final IScope scope, final Object obj) {

		switch (obj) {
			case IEnvelopeProvider ep:
				return ep.computeEnvelope(scope);
			case ISpecies s:
				return computeEnvelopeFrom(scope, s.getPopulation(scope));
			case Number n:
				double size = n.doubleValue();
				return of(0, size, 0, size, 0, size);
			case Envelope e:
				return of(e);
			case String s:
				return computeEnvelopeFrom(scope, GamaFileType.createFile(scope, s, false, null));
			case IList l: {
				IEnvelope result = null;
				for (final Object bounds : l) {
					final IEnvelope env = computeEnvelopeFrom(scope, bounds);
					if (result == null) {
						result = of(env);
					} else {
						result.expandToInclude(env);
					}
				}
				return result;
			}
			default:
				for (final IEnvelopeComputer ec : envelopeComputers) {
					IEnvelope result = ec.computeEnvelopeFrom(scope, obj);
					if (result != null) return result;
				}
				return null;
		}
	}

}
