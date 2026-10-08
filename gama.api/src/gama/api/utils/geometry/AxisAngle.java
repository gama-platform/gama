/*******************************************************************************************************
 *
 * AxisAngle.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.api.utils.geometry;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;

/**
 * A four-element axis angle represented by double-precision floating point x,y,z,angle components. An axis angle is a
 * rotation of angle (degrees) about the vector (x,y,z).
 *
 * @adapted from Vecmath by A. Drogoul (2017)
 *
 */
public record AxisAngle(IPoint axis, double angle) implements java.io.Serializable {

	/** The Constant DEFAULT_AXIS. */
	private static final IPoint DEFAULT_AXIS = GamaPointFactory.createImmutable(Rotation3D.PLUS_K);

	/** The Constant DEFAULT_ANGLE. */
	private static final double DEFAULT_ANGLE = 0.0;

	/**
	 * Constructs and initializes an AxisAngle4 from the specified axis and angle. If the axis is null, the default
	 * PLUS_K is chosen
	 *
	 * @param axis
	 *            the axis
	 * @param angle
	 *            the angle of rotation in degrees
	 *
	 * @since vecmath 1.2
	 */
	public AxisAngle(final IPoint axis, final double angle) {
		if (axis != null) {
			this.axis = axis.clone();
		} else {
			this.axis = DEFAULT_AXIS;
		}
		this.angle = angle;
	}

	/**
	 * Instantiates a new axis angle.
	 *
	 * @param angle
	 *            the angle
	 */
	public AxisAngle(final Double angle) {
		this(DEFAULT_AXIS, angle == null ? DEFAULT_ANGLE : angle.doubleValue());
	}

	/**
	 * Instantiates a new axis angle.
	 */
	public AxisAngle() {
		this(GamaPointFactory.create(), DEFAULT_ANGLE);
	}

	/**
	 * Get the axis angle, in degrees.<br>
	 * An axis angle is a rotation angle about the vector (x,y,z).
	 *
	 * @return the angle, in degrees.
	 */
	public final double getAngle() { return angle; }

	/**
	 * Get value of <i>x</i> coordinate.
	 *
	 * @return the <i>x</i> coordinate.
	 *
	 */
	public double getX() { return axis.getX(); }

	/**
	 * Set a new value for <i>x</i> coordinate.
	 *
	 * @param x
	 *            the <i>x</i> coordinate.
	 *
	 */
	public final void setX(final double x) {
		axis.setX(x);
	}

	/**
	 * Get value of <i>y</i> coordinate.
	 *
	 * @return the <i>y</i> coordinate.
	 *
	 */
	public final double getY() { return axis.getY(); }

	/**
	 * Set a new value for <i>y</i> coordinate.
	 *
	 * @param y
	 *            the <i>y</i> coordinate.
	 *
	 */
	public final void setY(final double y) {
		axis.setY(y);
	}

	/**
	 * Get value of <i>z</i> coordinate.
	 *
	 * @return the <i>z</i> coordinate.
	 *
	 */
	public double getZ() { return axis.getZ(); }

	/**
	 * Set a new value for <i>z</i> coordinate.
	 *
	 * @param z
	 *            the <i>z</i> coordinate.
	 *
	 */
	public final void setZ(final double z) {
		axis.setZ(z);
	}

	/**
	 * Gets the Axis around which the rotation is done.
	 *
	 * @return the Axis around which the rotation is done
	 */
	public IPoint getAxis() { return axis; }

}
