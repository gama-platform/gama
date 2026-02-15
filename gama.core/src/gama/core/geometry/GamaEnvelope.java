/*******************************************************************************************************
 *
 * GamaEnvelope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;

import gama.api.data.factories.GamaEnvelopeFactory;
import gama.api.data.factories.GamaShapeFactory;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.utils.geometry.AxisAngle;
import gama.api.utils.geometry.GamaPointFactory;
import gama.gaml.operators.Comparison;

// import org.opengis.geometry.MismatchedDimensionException;

/**
 * A 3D envelope that extends the 2D JTS Envelope.
 *
 *
 * @author Niels Charlier
 * @adapted for GAMA by A. Drogoul
 *
 */
public class GamaEnvelope extends Envelope implements IEnvelope {

	@Override
	public void dispose() {
		setToNull();
		GamaEnvelopeFactory.release(this);
	}

	/**
	 * Serial number for compatibility with different versions.
	 */
	private static final long serialVersionUID = -3188702602373537163L;

	/**
	 * the minimum z-coordinate
	 */
	private double minz;

	/**
	 * the maximum z-coordinate
	 */
	private double maxz;

	/**
	 * Initialize an <code>Envelope</code> for a region defined by maximum and minimum values.
	 *
	 * @param x1
	 *            the first x-value
	 * @param x2
	 *            the second x-value
	 * @param y1
	 *            the first y-value
	 * @param y2
	 *            the second y-value
	 * @param z1
	 *            the first z-value
	 * @param z2
	 *            the second z-value
	 */
	@Override
	public void init(final double x1, final double x2, final double y1, final double y2, final double z1,
			final double z2) {
		init(x1, x2, y1, y2);
		if (z1 < z2) {
			minz = z1;
			maxz = z2;
		} else {
			minz = z2;
			maxz = z1;
		}
	}

	/**
	 * Initialize an <code>Envelope</code> to a region defined by two Coordinates.
	 *
	 * @param p1
	 *            the first Coordinate
	 * @param p2
	 *            the second Coordinate
	 */
	@Override
	public void init(final Coordinate p1, final Coordinate p2) {
		init(p1.x, p2.x, p1.y, p2.y, p1.z, p2.z);
	}

	/**
	 * Initialize an <code>Envelope</code> to a region defined by a single Coordinate.
	 *
	 * @param p
	 *            the coordinate
	 */
	@Override
	public void init(final Coordinate p) {
		init(p.x, p.x, p.y, p.y, p.z, p.z);
	}

	@Override
	public void init(final Envelope env) {
		super.init(env);
		if (env instanceof GamaEnvelope e) {
			this.minz = e.getMinZ();
			this.maxz = e.getMaxZ();
		}
	}

	/**
	 * Initialize an <code>Envelope</code> from an existing 3D Envelope.
	 *
	 * @param env
	 *            the 3D Envelope to initialize from
	 */
	@Override
	public void init(final IEnvelope env) {
		super.init(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY());
		this.minz = env.getMinZ();
		this.maxz = env.getMaxZ();
	}

	/**
	 * Sets the.
	 *
	 * @param env
	 *            the env
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope set(final IEnvelope env) {
		init(env);
		return this;
	}

	/**
	 * Makes this <code>Envelope</code> a "null" envelope, that is, the envelope of the empty geometry.
	 */
	@Override
	public void setToNull() {
		super.setToNull();
		minz = 0;
		maxz = -1;
	}

	/**
	 * Returns the difference between the maximum and minimum z values.
	 *
	 * @return max z - min z, or 0 if this is a null <code>Envelope</code>
	 */
	@Override
	public double getDepth() {
		if (isNull()) return 0;
		return maxz - minz;
	}

	/**
	 * Returns the <code>Envelope</code>s minimum z-value. min z > max z indicates that this is a null
	 * <code>Envelope</code>.
	 *
	 * @return the minimum z-coordinate
	 */
	@Override
	public double getMinZ() { return minz; }

	/**
	 * Returns the <code>Envelope</code>s maximum z-value. min z > max z indicates that this is a null
	 * <code>Envelope</code>.
	 *
	 * @return the maximum z-coordinate
	 */
	@Override
	public double getMaxZ() { return maxz; }

	/**
	 * Gets the volume of this envelope.
	 *
	 * @return the volume of the envelope
	 * @return 0.0 if the envelope is null
	 */
	@Override
	public double getVolume() {
		if (isNull()) return 0.0;
		return getWidth() * getHeight() * getDepth();
	}

	/**
	 * Gets the minimum extent of this envelope across all three dimensions.
	 *
	 * @return the minimum extent of this envelope
	 */
	@Override
	public double minExtent() {
		if (isNull()) return 0.0;
		return Math.min(getWidth(), Math.min(getHeight(), getDepth()));
	}

	/**
	 * Gets the maximum extent of this envelope across all three dimensions.
	 *
	 * @return the maximum extent of this envelope
	 */
	@Override
	public double maxExtent() {
		if (isNull()) return 0.0;
		return Math.max(getWidth(), Math.max(getHeight(), getDepth()));
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the given {@link Coordinate}. Has no effect if the point
	 * is already on or within the envelope.
	 *
	 * @param p
	 *            the Coordinate to expand to include
	 */
	@Override
	public void expandToInclude(final Coordinate p) {
		expandToInclude(p.x, p.y, p.z);
	}

	/**
	 * Expands this envelope by a given distance in all directions. Both positive and negative distances are supported.
	 *
	 * @param distance
	 *            the distance to expand the envelope
	 */
	@Override
	public void expandBy(final double distance) {
		expandBy(distance, distance, distance);
	}

	/**
	 * Expands this envelope by a given distance in all directions. Both positive and negative distances are supported.
	 *
	 * @param deltaX
	 *            the distance to expand the envelope along the the X axis
	 * @param deltaY
	 *            the distance to expand the envelope along the the Y axis
	 */
	@Override
	public void expandBy(final double deltaX, final double deltaY, final double deltaZ) {
		if (isNull()) return;
		minz -= deltaZ;
		maxz += deltaZ;
		expandBy(deltaX, deltaY);

		// check for envelope disappearing
		if (minz > maxz) { setToNull(); }
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the given point. Has no effect if the point is already on
	 * or within the envelope.
	 *
	 * @param x
	 *            the value to lower the minimum x to or to raise the maximum x to
	 * @param y
	 *            the value to lower the minimum y to or to raise the maximum y to
	 * @param z
	 *            the value to lower the minimum z to or to raise the maximum z to
	 */
	@Override
	public void expandToInclude(final double x, final double y, final double z) {
		if (isNull()) {
			expandToInclude(x, y);
			minz = z;
			maxz = z;
		} else {
			expandToInclude(x, y);
			if (z < minz) { minz = z; }
			if (z > maxz) { maxz = z; }
		}
	}

	/**
	 * Translates this envelope by given amounts in the X and Y direction. Returns the envelope
	 *
	 * @param transX
	 *            the amount to translate along the X axis
	 * @param transY
	 *            the amount to translate along the Y axis
	 * @param transZ
	 *            the amount to translate along the Z axis
	 */
	@Override
	public IEnvelope translate(final double transX, final double transY, final double transZ) {
		if (isNull()) return this;
		init(getMinX() + transX, getMaxX() + transX, getMinY() + transY, getMaxY() + transY, getMinZ() + transZ,
				getMaxZ() + transZ);
		return this;
	}

	/**
	 * Computes the coordinate of the centre of this envelope (as long as it is non-null
	 *
	 * @return the centre coordinate of this envelope <code>null</code> if the envelope is null
	 */
	@Override
	public Coordinate centre() {
		if (isNull()) return null;
		return center().toCoordinate();
	}

	/**
	 * Check if the region defined by <code>other</code> overlaps (intersects) the region of this <code>Envelope</code>.
	 *
	 * @param other
	 *            the <code>Envelope</code> which this <code>Envelope</code> is being checked for overlapping
	 * @return <code>true</code> if the <code>Envelope</code>s overlap
	 */
	@Override
	public boolean intersects(final Envelope other) {
		if (!super.intersects(other)) return false;
		return getMinZOf(other) <= maxz && getMaxZOf(other) >= minz;
	}

	@Override
	public boolean intersects(final IEnvelope other) {
		if (!(other instanceof Envelope env)) return false;
		return intersects(env);
	}

	/**
	 * Check if the point <code>(x, y)</code> overlaps (lies inside) the region of this <code>Envelope</code>.
	 *
	 * @param x
	 *            the x-ordinate of the point
	 * @param y
	 *            the y-ordinate of the point
	 * @param z
	 *            the z-ordinate of the point
	 * @return <code>true</code> if the point overlaps this <code>Envelope</code>
	 */
	private boolean intersects(final double x, final double y, final double z) {
		if (isNull()) return false;
		return intersects(x, y) && z >= minz && z <= maxz;
	}

	@Override
	public boolean intersects(final Coordinate p) {
		return intersects(p.x, p.y, p.z);
	}

	/**
	 * Tests if the given point lies in or on the envelope.
	 *
	 * @param x
	 *            the x-coordinate of the point which this <code>Envelope</code> is being checked for containing
	 * @param y
	 *            the y-coordinate of the point which this <code>Envelope</code> is being checked for containing
	 * @return <code>true</code> if <code>(x, y)</code> lies in the interior or on the boundary of this
	 *         <code>Envelope</code>.
	 */
	private boolean covers(final double x, final double y, final double z) {
		if (isNull()) return false;
		return covers(x, y) && z >= minz && z <= maxz;
	}

	@Override
	public boolean covers(final Coordinate p) {
		return covers(p.x, p.y, p.getZ());
	}

	/**
	 * Tests if the <code>Envelope other</code> lies wholely inside this <code>Envelope</code> (inclusive of the
	 * boundary).
	 *
	 * @param other
	 *            the <code>Envelope</code> to check
	 * @return true if this <code>Envelope</code> covers the <code>other</code>
	 */
	@Override
	public boolean covers(final Envelope other) {
		if (isNull() || other.isNull() || !super.covers(other)) return false;
		return getMinZOf(other) >= minz && getMaxZOf(other) <= maxz;
	}

	@Override
	public boolean covers(final IEnvelope other) {
		if (!(other instanceof Envelope env)) return false;
		return covers(env);
	}

	/**
	 * Computes the distance between this and another <code>Envelope</code>. The distance between overlapping Envelopes
	 * is 0. Otherwise, the distance is the Euclidean distance between the closest points.
	 */
	@Override
	public double distance(final Envelope env) {
		if (intersects(env)) return 0;

		double dx = 0.0;
		if (getMaxX() < env.getMinX()) {
			dx = env.getMinX() - getMaxX();
		} else if (getMinX() > env.getMaxX()) { dx = getMinX() - env.getMaxX(); }

		double dy = 0.0;
		if (getMaxY() < env.getMinY()) {
			dy = env.getMinY() - getMaxY();
		} else if (getMinY() > env.getMaxY()) { dy = getMinY() - env.getMaxY(); }

		double dz = 0.0;
		final double otherMinZ = getMinZOf(env);
		final double otherMaxZ = getMaxZOf(env);
		if (maxz < otherMinZ) {
			dz = otherMinZ - maxz;
		} else if (minz > otherMaxZ) { dz = minz - otherMaxZ; }

		// if either is zero, the envelopes overlap either vertically or
		// horizontally
		if (dx == 0.0 && dz == 0.0) return dy;
		if (dy == 0.0 && dz == 0.0) return dx;
		if (dx == 0.0 && dy == 0.0) return dz;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public double distance(final IEnvelope env) {
		if (!(env instanceof Envelope other)) return Double.NaN;
		return distance(other);
	}

	// ---------------------------------------------------------------------------------------------------------------

	/**
	 * Computes the intersection of two {@link Envelope}s.
	 *
	 * @param env
	 *            the envelope to intersect with
	 * @return a new Envelope representing the intersection of the envelopes (this will be the null envelope if either
	 *         argument is null, or they do not intersect
	 */
	@Override
	public GamaEnvelope intersection(final Envelope env) {
		if (isNull() || env.isNull() || !intersects(env)) return (GamaEnvelope) GamaEnvelopeFactory.create();
		final Envelope xyInt = super.intersection(env);
		final double otherMinZ = getMinZOf(env);
		final double intMinZ = minz > otherMinZ ? minz : otherMinZ;
		final double otherMaxZ = getMaxZOf(env);
		final double intMaxZ = maxz < otherMaxZ ? maxz : otherMaxZ;
		return (GamaEnvelope) GamaEnvelopeFactory.of(xyInt.getMinX(), xyInt.getMaxX(), xyInt.getMinY(), xyInt.getMaxY(),
				intMinZ, intMaxZ);
	}

	/**
	 * Intersection I.
	 *
	 * @param ie
	 *            the ie
	 * @return the i envelope
	 */
	@Override
	public IEnvelope intersection(final IEnvelope ie) {
		if (!(ie instanceof Envelope other)) return GamaEnvelopeFactory.EMPTY;
		return intersection(other);
	}

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the <code>other</code> Envelope. Has no effect if
	 * <code>other</code> is wholly on or within the envelope.
	 *
	 * @param other
	 *            the <code>Envelope</code> to expand to include
	 */
	@Override
	public void expandToInclude(final Envelope other) {
		if (other.isNull()) return;
		final double otherMinZ = getMinZOf(other);
		final double otherMaxZ = getMaxZOf(other);
		if (isNull()) {
			super.expandToInclude(other);
			minz = otherMinZ;
			maxz = otherMaxZ;
		} else {
			super.expandToInclude(other);
			if (otherMinZ < minz) { minz = otherMinZ; }
			if (otherMaxZ > maxz) { maxz = otherMaxZ; }
		}
	}

	@Override
	public void expandToInclude(final IEnvelope ie) {
		if (ie.isNull() || !(ie instanceof Envelope other)) return;
		expandToInclude(other);
	}

	/**
	 * @param other
	 * @return
	 */
	private double getMaxZOf(final Envelope other) {
		if (other instanceof GamaEnvelope e) return e.maxz;
		return 0d;
	}

	/**
	 * @param other
	 * @return
	 */
	private double getMinZOf(final Envelope other) {
		if (other instanceof GamaEnvelope e) return e.minz;
		return 0d;
	}

	/**
	 * Returns a hash value for this envelope. This value need not remain consistent between different implementations
	 * of the same class.
	 */
	@Override
	public int hashCode() {
		// Algorithm from Effective Java by Joshua Bloch [Jon Aquino]
		int result = super.hashCode();
		result = 37 * result + Coordinate.hashCode(minz);
		result = 37 * result + Coordinate.hashCode(maxz);
		return result ^ (int) serialVersionUID;
	}

	/**
	 * Compares the specified object with this envelope for equality.
	 */
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof final GamaEnvelope otherEnvelope)) return false;
		if (isNull()) return otherEnvelope.isNull();
		return super.equals(other) && Comparison.equal(minz, otherEnvelope.getMinZ())
				&& Comparison.equal(maxz, otherEnvelope.getMaxZ());
	}

	/**
	 * Checks if is flat.
	 *
	 * @return true, if is flat
	 */
	@Override
	public boolean isFlat() { return minz == maxz; }

	/**
	 * To geometry.
	 *
	 * @return the polygon
	 */
	@Override
	public Polygon toGeometry() {
		if (isFlat())
			return (Polygon) GamaShapeFactory.buildRectangle(getWidth(), getHeight(), center()).getInnerGeometry();
		return (Polygon) GamaShapeFactory.buildBox(getWidth(), getHeight(), getDepth(), center()).getInnerGeometry();
	}

	@Override
	public String toString() {
		return "Env[" + getMinX() + " : " + getMaxX() + ", " + getMinY() + " : " + getMaxY() + ",  " + minz + " : "
				+ maxz + "]";
	}

	/**
	 * Y negated.
	 *
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope yNegated() {
		return GamaEnvelopeFactory.of(getMinX(), getMaxX(), -getMaxY(), -getMinY(), minz, maxz);
	}

	/**
	 * Rotate.
	 *
	 * @param rotation
	 *            the rotation
	 * @return the envelope 3 D
	 */
	@Override
	public IEnvelope rotate(final AxisAngle rotation) {
		if (isNull()) return this;
		IShape source = GamaShapeFactory.createFrom(this);
		source = GamaShapeFactory.createFrom(source).withRotation(rotation).withLocation(source.getLocation());
		init(source.getEnvelope());
		return this;
	}

	@Override
	public IPoint center() {
		return GamaPointFactory.create((getMinX() + getMaxX()) / 2.0, (getMinY() + getMaxY()) / 2.0,
				(getMinZ() + getMaxZ()) / 2.0);
	}

	@Override
	public IShape toShape() {
		return GamaShapeFactory.createFrom(this);
	}

}