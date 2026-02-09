/*******************************************************************************************************
 *
 * Scaling3D.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import org.locationtech.jts.geom.Coordinate;

import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IPoint;

/**
 * The Class Scaling3D.
 */
@SuppressWarnings ("unchecked")
public abstract class Scaling3D implements ITransformation3D {

	/** The Constant IDENTITY. */
	public final static Scaling3D IDENTITY = new Uniform(1);

	/**
	 * Of.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the scaling 3 D
	 */
	public static Scaling3D of(final double x, final double y, final double z) {
		if (x == y && y == z) return of(x);
		return new Heterogeneous(x, y, z);
	}

	/**
	 * Of.
	 *
	 * @param p
	 *            the p
	 * @return the scaling 3 D
	 */
	public static Scaling3D of(final IPoint p) {
		if (p == null) return null;
		return of(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Of.
	 *
	 * @param factor
	 *            the factor
	 * @return the scaling 3 D
	 */
	public static Scaling3D of(final double factor) {
		if (factor == 1d) return IDENTITY;
		return new Uniform(factor);
	}

	/**
	 * The Class Uniform.
	 */
	public static class Uniform extends Scaling3D {

		/** The factor. */
		final double factor;

		/**
		 * Instantiates a new uniform.
		 *
		 * @param f
		 *            the f
		 */
		public Uniform(final double f) {
			factor = f;
		}

		/**
		 * Filter.
		 *
		 * @param coord
		 *            the coord
		 */
		@Override
		public void filter(final Coordinate coord) {
			((IPoint) coord).multiplyBy(factor);

		}

		@Override
		public Scaling3D asBoundingBoxIn(final IEnvelope env) {
			return of(factor / env.getWidth(), factor / env.getHeight(), env.isFlat() ? 1.0 : factor / env.getDepth());
		}

		@Override
		public double getZ() { return factor; }

		@Override
		public double getY() { return factor; }

		@Override
		public double getX() { return factor; }

		@Override
		public Scaling3D dividedBy(final double i) {
			return Scaling3D.of(factor / i);
		}
	}

	/**
	 * The Class Heterogeneous.
	 */
	public static class Heterogeneous extends Scaling3D {

		/** The z. */
		public double x, y, z;

		/**
		 * Instantiates a new heterogeneous.
		 *
		 * @param i
		 *            the i
		 * @param j
		 *            the j
		 * @param k
		 *            the k
		 */
		public Heterogeneous(final double i, final double j, final double k) {
			x = i;
			y = j;
			z = k;
		}

		/**
		 * Sets the to.
		 *
		 * @param i
		 *            the i
		 * @param j
		 *            the j
		 * @param k
		 *            the k
		 * @return the scaling 3 D
		 */
		public Scaling3D setTo(final double i, final double j, final double k) {
			x = i;
			y = j;
			z = k;
			return this;
		}

		/**
		 * Sets the to.
		 *
		 * @param i
		 *            the i
		 * @return the scaling 3 D
		 */
		public Scaling3D setTo(final double i) {
			x = y = z = i;
			return this;
		}

		/**
		 * Filter.
		 *
		 * @param coord
		 *            the coord
		 */
		@Override
		public void filter(final Coordinate coord) {
			coord.x *= x;
			coord.y *= y;
			coord.z *= z;
		}

		/**
		 * As bounding box in.
		 *
		 * @param env
		 *            the env
		 * @return the scaling 3 D
		 */
		@Override
		public Scaling3D asBoundingBoxIn(final IEnvelope env) {
			x /= env.getWidth();
			y /= env.getHeight();
			if (!env.isFlat()) {
				z /= env.getDepth();
			} else {
				z = 1.0;
			}
			return this;
		}

		@Override
		public double getZ() { return z; }

		@Override
		public double getY() { return y; }

		@Override
		public double getX() { return x; }

		@Override
		public Scaling3D dividedBy(final double i) {
			return Scaling3D.of(x / i, y / i, z / i);
		}
	}

	/**
	 * As bounding box in.
	 *
	 * @param env
	 *            the env
	 * @return the scaling 3 D
	 */
	public abstract Scaling3D asBoundingBoxIn(final IEnvelope env);

	/**
	 * Gets the z.
	 *
	 * @return the z
	 */
	public abstract double getZ();

	/**
	 * Gets the y.
	 *
	 * @return the y
	 */
	public abstract double getY();

	/**
	 * Gets the x.
	 *
	 * @return the x
	 */
	public abstract double getX();

	/**
	 * Divided by.
	 *
	 * @param i
	 *            the i
	 * @return the scaling 3 D
	 */
	public abstract Scaling3D dividedBy(double i);

	/**
	 * To gama point.
	 *
	 * @return the gama point
	 */
	public IPoint toGamaPoint() {
		return GamaPointFactory.create(getX(), getY(), getZ());
	}

}
