/*******************************************************************************************************
 *
 * InternalGamaPointFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import gama.api.data.factories.IPointFactory;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;

/**
 *
 */
public class InternalGamaPointFactory implements IPointFactory {

	/**
	 * The Class Immutable.
	 */
	public static class Immutable extends GamaPoint {

		/**
		 * Instantiates a new immutable.
		 *
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param z
		 *            the z
		 */
		public Immutable(final double x, final double y, final double z) {
			super(x, y, z);
		}

		/**
		 * Sets the location.
		 *
		 * @param al
		 *            the al
		 * @return the i point
		 */
		@Override
		public Immutable setLocation(final IPoint al) {
			return this;
		}

		@Override
		public Immutable setLocation(final double x, final double y, final double z) {
			return this;
		}

		@Override
		public void setCoordinate(final Coordinate c) {}

		@Override
		public void setOrdinate(final int i, final double v) {}

		@Override
		public void setX(final double xx) {}

		@Override
		public void setY(final double yy) {}

		@Override
		public void setZ(final double zz) {}

		/**
		 * Adds the.
		 *
		 * @param loc
		 *            the loc
		 * @return the i point
		 */
		@Override
		public Immutable add(final IPoint loc) {
			return this;
		}

		@Override
		public Immutable add(final double ax, final double ay, final double az) {
			return this;
		}

		/**
		 * Subtract.
		 *
		 * @param loc
		 *            the loc
		 * @return the i point
		 */
		@Override
		public Immutable subtract(final IPoint loc) {
			return this;
		}

		@Override
		public Immutable multiplyBy(final double value) {
			return this;
		}

		@Override
		public Immutable divideBy(final double value) {
			return this;
		}

		@Override
		public void setGeometry(final IShape g) {}

		@Override
		public void setInnerGeometry(final Geometry point) {

		}

		@Override
		public IPoint normalize() {
			return this;
		}

		@Override
		public void negate() {}

		@Override
		public void setDepth(final double depth) {}

	}

	/** The Constant NULL_POINT. */
	private static Immutable NULL_POINT = null;

	/**
	 * Gets the null point.
	 *
	 * @return the null point
	 */
	@Override
	public Immutable getNullPoint() {
		if (NULL_POINT == null) { NULL_POINT = createImmutable(0, 0, 0); }
		return NULL_POINT;
	}

	/**
	 * Creates a new GamaPoint object.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the immutable
	 */
	@Override
	public Immutable createImmutable(final double x, final double y, final double z) {
		return new Immutable(x, y, z);
	}

	/**
	 * Creates a new GamaPoint object.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the immutable
	 */
	@Override
	public Immutable createImmutable(final double x, final double y) {
		return createImmutable(x, y, 0);
	}

	/**
	 * Creates a new GamaPoint object.
	 *
	 * @param p
	 *            the p
	 * @return the immutable
	 */
	@Override
	public Immutable createImmutable(final IPoint p) {
		if (p == null) return NULL_POINT;
		return createImmutable(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Creates a new GamaPoint object.
	 *
	 * @param p
	 *            the p
	 * @return the immutable
	 */
	@Override
	public Immutable createImmutable(final Coordinate p) {
		if (p == null) return NULL_POINT;
		return createImmutable(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Creates the.
	 *
	 * @return the i point
	 */
	@Override
	public IPoint create() {
		return create(0, 0, 0);
	}

	/**
	 * Creates the.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the i point
	 */
	@Override
	public IPoint create(final double x, final double y) {
		return create(x, y, 0);
	}

	/**
	 * Creates the.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the i point
	 */
	@Override
	public IPoint create(final double x, final double y, final double z) {
		return new GamaPoint(x, y, z);
	}

	/**
	 * Creates the.
	 *
	 * @param p
	 *            the p
	 * @return the i point
	 */
	@Override
	public IPoint create(final IPoint p) {
		if (p == null) return create();
		return create(p.getX(), p.getY(), p.getZ());
	}

	/**
	 * Creates the.
	 *
	 * @param p
	 *            the p
	 * @return the i point
	 */
	@Override
	public IPoint create(final Coordinate p) {
		if (p == null) return create();
		return create(p.getX(), p.getY(), p.getZ());
	}

}
