/*******************************************************************************************************
 *
 * GamaEnvelopeFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import gama.api.gaml.types.GamaFileType;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.utils.PoolUtils;

/**
 * A static factory for creating {@link IEnvelope} instances. This class provides methods to create envelopes (bounding
 * boxes) from various geometries, shapes, and coordinate inputs. It delegates the creation to an
 * {@link IEnvelopeFactory} implementation.
 */
public class GamaEnvelopeFactory {

	/** The envelope computers. */
	private static List<IEnvelopeComputer> envelopeComputers = new ArrayList<>();

	/** The Constant POOL. */
	private static PoolUtils.ObjectPool<IEnvelope> POOL =
			PoolUtils.create("Envelope", true, GamaEnvelope::new, (from, to) -> to.set(from), null);

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

		/**
		 * Expand to include.
		 *
		 * @param p
		 *            the p
		 */
		@Override
		public void expandToInclude(final IPoint p) {}

		/**
		 * Dispose.
		 */
		@Override
		public void dispose() {}

		/**
		 * Inits the.
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
		 */
		@Override
		public void init(final double x1, final double x2, final double y1, final double y2, final double z1,
				final double z2) {}

		/**
		 * Inits the.
		 *
		 * @param p1
		 *            the p 1
		 * @param p2
		 *            the p 2
		 */
		@Override
		public void init(final Coordinate p1, final Coordinate p2) {}

		/**
		 * Inits the.
		 *
		 * @param p
		 *            the p
		 */
		@Override
		public void init(final Coordinate p) {}

		/**
		 * Inits the.
		 *
		 * @param env
		 *            the env
		 */
		@Override
		public void init(final Envelope env) {}

		/**
		 * Inits the.
		 *
		 * @param env
		 *            the env
		 */
		@Override
		public void init(final IEnvelope env) {}

		/**
		 * Sets the.
		 *
		 * @param env
		 *            the env
		 * @return the i envelope
		 */
		@Override
		public IEnvelope set(final IEnvelope env) {
			return this;
		}

		/**
		 * Sets the to null.
		 */
		@Override
		public void setToNull() {}

		/**
		 * Expand to include.
		 *
		 * @param p
		 *            the p
		 */
		@Override
		public void expandToInclude(final Coordinate p) {}

		/**
		 * Expand by.
		 *
		 * @param distance
		 *            the distance
		 */
		@Override
		public void expandBy(final double distance) {}

		/**
		 * Expand by.
		 *
		 * @param deltaX
		 *            the delta X
		 * @param deltaY
		 *            the delta Y
		 * @param deltaZ
		 *            the delta Z
		 */
		@Override
		public void expandBy(final double deltaX, final double deltaY, final double deltaZ) {}

		/**
		 * Expand to include.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param z
		 *            the z
		 */
		@Override
		public void expandToInclude(final double x, final double y, final double z) {}

		/**
		 * Translate.
		 *
		 * @param transX
		 *            the trans X
		 * @param transY
		 *            the trans Y
		 * @param transZ
		 *            the trans Z
		 * @return the i envelope
		 */
		@Override
		public IEnvelope translate(final double transX, final double transY, final double transZ) {
			return this;
		}

		/**
		 * Expand to include.
		 *
		 * @param other
		 *            the other
		 */
		@Override
		public void expandToInclude(final Envelope other) {}

		/**
		 * Expand to include.
		 *
		 * @param ie
		 *            the ie
		 */
		@Override
		public void expandToInclude(final IEnvelope ie) {}

		/**
		 * Rotate.
		 *
		 * @param rotation
		 *            the rotation
		 * @return the i envelope
		 */
		@Override
		public IEnvelope rotate(final AxisAngle rotation) {
			return this;
		}

		/**
		 * Inits the.
		 */
		@Override
		public void init() {}

		/**
		 * Inits the.
		 *
		 * @param x1
		 *            the x 1
		 * @param x2
		 *            the x 2
		 * @param y1
		 *            the y 1
		 * @param y2
		 *            the y 2
		 */
		@Override
		public void init(final double x1, final double x2, final double y1, final double y2) {}

		/**
		 * Expand by.
		 *
		 * @param deltaX
		 *            the delta X
		 * @param deltaY
		 *            the delta Y
		 */
		@Override
		public void expandBy(final double deltaX, final double deltaY) {}

		/**
		 * Expand to include.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 */
		@Override
		public void expandToInclude(final double x, final double y) {}

		/**
		 * Translate.
		 *
		 * @param transX
		 *            the trans X
		 * @param transY
		 *            the trans Y
		 */
		@Override
		public void translate(final double transX, final double transY) {}

	}

	/**
	 * A shared, empty {@link IEnvelope} instance.
	 */
	public static IEnvelope EMPTY = ofImmutable(0, 0, 0, 0);

	/**
	 * Creates a new, empty {@link IEnvelope}.
	 *
	 * @return a new empty envelope.
	 */
	public static IEnvelope create() {
		return POOL.get();
	}

	/**
	 * Creates an envelope encompassing the given JTS {@link Geometry}.
	 *
	 * @param g
	 *            the geometry to compute the envelope from.
	 * @return the geometry's envelope.
	 */
	public static IEnvelope of(final Geometry g) {
		if (g instanceof GeometryCollection gc) return of(gc);
		final ICoordinates sq = GamaCoordinateSequenceFactory.pointsOf(g);
		return sq.getEnvelope();
	}

	/**
	 * Creates an envelope encompassing the given JTS {@link GeometryCollection}.
	 *
	 * @param g
	 *            the geometry collection.
	 * @return the collection's envelope.
	 */
	public static IEnvelope of(final GeometryCollection g) {
		final int i = g.getNumGeometries();
		if (i == 0) return EMPTY;
		final IEnvelope result = of(g.getGeometryN(0));
		for (int j = 1; j < i; j++) { result.expandToInclude(of(g.getGeometryN(j))); }
		return result;
	}

	/**
	 * Creates an envelope that encompasses all shapes in the provided list.
	 *
	 * @param list
	 *            the list of shapes.
	 * @return the combined envelope of all shapes in the list.
	 */
	public static IEnvelope of(final List<IShape> list) {
		final int i = list.size();
		if (i == 0) return EMPTY;
		final IEnvelope result = of(list.get(0));
		for (int j = 1; j < i; j++) { result.expandToInclude(of(list.get(j))); }
		return result;
	}

	/**
	 * Creates an envelope from a GAMA {@link IShape}.
	 *
	 * @param s
	 *            the shape.
	 * @return the shape's envelope.
	 */
	public static IEnvelope of(final IShape s) {
		return of(s.getInnerGeometry());
	}

	/**
	 * Creates an envelope from a single {@link IPoint}.
	 *
	 * @param s
	 *            the point.
	 * @return an envelope containing only the point.
	 */
	public static IEnvelope of(final IPoint s) {
		return of(s.toCoordinate());
	}

	/**
	 * Creates an {@link IEnvelope} from a JTS {@link Envelope}.
	 *
	 * @param e
	 *            the source JTS envelope.
	 * @return the corresponding {@link IEnvelope} instance.
	 */
	public static IEnvelope of(final Envelope e) {
		final IEnvelope env = create();
		env.init(e);
		return env;
	}

	/**
	 * Creates a copy of an existing {@link IEnvelope}.
	 *
	 * @param e
	 *            the source envelope.
	 * @return a new copy of the envelope.
	 */
	public static IEnvelope of(final IEnvelope e) {
		final IEnvelope env = create();
		env.init(e);
		return env;
	}

	/**
	 * Creates a new envelope with the Y axis negated (useful for screen coordinates transformation).
	 *
	 * @param e
	 *            the source envelope.
	 * @return a new envelope with negated Y coordinates.
	 */
	public static IEnvelope withYNegated(final IEnvelope e) {
		final IEnvelope env = create();
		env.init(e);
		env.init(env.getMinX(), env.getMaxX(), -env.getMinY(), -env.getMaxY(), env.getMinZ(), env.getMaxZ());
		return env;
	}

	/**
	 * Creates an envelope from a single JTS {@link Coordinate}.
	 *
	 * @param p
	 *            the coordinate.
	 * @return an envelope containing the coordinate.
	 */
	public static IEnvelope of(final Coordinate p) {
		final IEnvelope env = create();
		env.init(p);
		return env;
	}

	/**
	 * Creates an envelope defined by the specified bounds (min/max for X, Y, Z).
	 *
	 * @param x1
	 *            the minimum X coordinate.
	 * @param x2
	 *            the maximum X coordinate.
	 * @param y1
	 *            the minimum Y coordinate.
	 * @param y2
	 *            the maximum Y coordinate.
	 * @param z1
	 *            the minimum Z coordinate.
	 * @param z2
	 *            the maximum Z coordinate.
	 * @return the defined {@link IEnvelope}.
	 */
	public static IEnvelope of(final double x1, final double x2, final double y1, final double y2, final double z1,
			final double z2) {
		final IEnvelope env = create();
		env.init(x1, x2, y1, y2, z1, z2);
		return env;
	}

	/**
	 * Creates a 2D envelope defined by the specified bounds (min/max for X, Y). Z coordinates are initialized to 0.
	 *
	 * @param x1
	 *            the minimum X coordinate.
	 * @param x2
	 *            the maximum X coordinate.
	 * @param y1
	 *            the minimum Y coordinate.
	 * @param y2
	 *            the maximum Y coordinate.
	 * @return the defined {@link IEnvelope}.
	 */
	public static IEnvelope of(final double x1, final double x2, final double y1, final double y2) {
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
	public static IEnvelope ofImmutable(final double x1, final double x2, final double y1, final double y2) {
		return new Immutable(x1, x2, y1, y2);
	}

	/**
	 * Releases an envelope back to the pool, if pooling is implemented.
	 *
	 * @param envelope3d
	 *            the envelope to release.
	 */
	public static void release(final IEnvelope envelope3d) {
		POOL.release(envelope3d);
	}

	/**
	 * Compute envelope from.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i envelope
	 */
	public static IEnvelope castToEnvelope(final IScope scope, final Object obj) {
		switch (obj) {
			case IEnvelopeProvider ep:
				return ep.computeEnvelope(scope);
			case ISpecies s:
				return castToEnvelope(scope, s.getPopulation(scope));
			case Number n:
				double size = n.doubleValue();
				return of(0, size, 0, size, 0, size);
			case Envelope e:
				return of(e);
			case String s:
				return castToEnvelope(scope, GamaFileType.createFile(scope, s, false, null));
			case IList l: {
				IEnvelope result = null;
				for (final Object bounds : l) {
					final IEnvelope env = castToEnvelope(scope, bounds);
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

	/**
	 * Adds the envelope computer.
	 *
	 * @param ec
	 *            the ec
	 */
	public static void addEnvelopeComputer(final IEnvelopeComputer ec) {
		envelopeComputers.add(ec);
	}

}