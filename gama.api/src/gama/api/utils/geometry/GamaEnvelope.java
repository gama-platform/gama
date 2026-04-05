/*******************************************************************************************************
 *
 * GamaEnvelope.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import static org.locationtech.jts.index.quadtree.IntervalSize.isZeroWidth;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;

// import org.opengis.geometry.MismatchedDimensionException;

/**
 * A 3D envelope that extends the 2D JTS Envelope.
 *
 * <p>
 * A {@code GamaEnvelope} is considered <em>2D (flat)</em> when {@code minz == maxz} (depth is zero). It becomes
 * <em>3D</em> as soon as a point or envelope with a differing Z coordinate is included via any of the
 * {@code expandToInclude} methods. All operations ({@link #getDepth()}, {@link #getVolume()},
 * {@link #minExtent()}, {@link #maxExtent()}, {@link #intersects}, {@link #covers}, {@link #distance}) are
 * coherent with respect to this 2D/3D distinction:
 * </p>
 * <ul>
 *   <li>{@link #isFlat()} returns {@code true} for both null envelopes and envelopes with depth zero.</li>
 *   <li>Plain JTS {@link Envelope} instances (which carry no Z information) are treated as flat (Z = [0, 0])
 *       in all operations that accept them.</li>
 *   <li>Re-initializing a {@code GamaEnvelope} from a plain JTS {@link Envelope} resets the Z range to [0, 0],
 *       preventing stale Z values from pooled instances from leaking.</li>
 * </ul>
 *
 * <h2>Z-coordinate semantics</h2>
 * <ul>
 *   <li>After {@link #setToNull()}: {@code minz = 0, maxz = -1} (standard "null" sentinel — depth = 0 since
 *       {@link #getDepth()} guards against null).</li>
 *   <li>After 2D initialization (4-arg {@code init} or factory methods that do not supply Z): {@code minz = maxz = 0}.</li>
 *   <li>After 3D initialization: {@code minz <= maxz} with at least one of them non-zero (or both non-zero).</li>
 * </ul>
 *
 * @author Niels Charlier
 * @adapted for GAMA by A. Drogoul
 */
public class GamaEnvelope extends Envelope implements IEnvelope {

	/**
	 * Instantiates a new gama envelope.
	 */
	GamaEnvelope() {}

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
	 * <p>
	 * {@link Double#NaN} values for Z are treated as {@code 0.0}, ensuring the envelope never holds NaN in its Z
	 * range (consistent with {@link GamaPoint}'s invariant that Z is never NaN).
	 * </p>
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
	 *            the first z-value (NaN is treated as 0.0)
	 * @param z2
	 *            the second z-value (NaN is treated as 0.0)
	 */
	@Override
	public void init(final double x1, final double x2, final double y1, final double y2, final double z1,
			final double z2) {
		init(x1, x2, y1, y2);
		final double sz1 = Double.isNaN(z1) ? 0.0d : z1;
		final double sz2 = Double.isNaN(z2) ? 0.0d : z2;
		if (sz1 < sz2) {
			minz = sz1;
			maxz = sz2;
		} else {
			minz = sz2;
			maxz = sz1;
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

	/**
	 * Initialize this envelope from a JTS {@link Envelope}.
	 *
	 * <p>
	 * If {@code env} is a {@link GamaEnvelope}, its Z range is preserved. Otherwise (plain JTS {@link Envelope},
	 * which carries no Z information), the Z range is explicitly reset to {@code [0, 0]}, preventing stale Z values
	 * from a previously used pooled instance from leaking into the new 2D envelope.
	 * </p>
	 *
	 * @param env
	 *            the source envelope; must not be {@code null}
	 */
	@Override
	public void init(final Envelope env) {
		super.init(env);
		if (env instanceof GamaEnvelope e) {
			this.minz = e.minz;
			this.maxz = e.maxz;
		} else {
			// Plain JTS Envelope has no Z information — treat it as flat (2D).
			this.minz = 0d;
			this.maxz = 0d;
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
	 *
	 * <p>
	 * After calling this method, {@code minz = 0} and {@code maxz = -1}, which are sentinels signalling the null
	 * state. {@link #isFlat()} returns {@code true} for a null envelope. {@link #getDepth()} guards against null and
	 * returns {@code 0}.
	 * </p>
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
	 * <p>
	 * For a 3D (non-flat) envelope, returns the minimum of width, height, and depth. For a flat (2D) envelope whose
	 * depth is zero, returns the minimum of width and height (depth is excluded since it is zero and would trivially
	 * dominate the result).
	 * </p>
	 *
	 * @return the minimum extent of this envelope
	 */
	@Override
	public double minExtent() {
		if (isNull()) return 0.0;
		if (isFlat()) return Math.min(getWidth(), getHeight());
		return Math.min(getWidth(), Math.min(getHeight(), getDepth()));
	}

	/**
	 * Gets the maximum extent of this envelope across all three dimensions.
	 *
	 * <p>
	 * For a 3D (non-flat) envelope, returns the maximum of width, height, and depth. For a flat (2D) envelope, returns
	 * the maximum of width and height only.
	 * </p>
	 *
	 * @return the maximum extent of this envelope
	 */
	@Override
	public double maxExtent() {
		if (isNull()) return 0.0;
		if (isFlat()) return Math.max(getWidth(), getHeight());
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
	 * Expands this envelope by a given distance in all three dimensions (X, Y and Z).
	 *
	 * <p>
	 * The expansion is always applied in all three dimensions, regardless of whether the envelope is flat (2D) or
	 * already 3D. This is required so that spatial search code (e.g. {@code GamaQuadTree.allAtDistance}) can use a
	 * distance-expanded envelope as a candidate set that captures agents at any Z level — including agents at Z=0 when
	 * the searching agent is at a non-zero Z, or vice versa. Restricting expansion to 2D would silently miss agents on
	 * the other Z level and break distance-based queries in mixed 2D/3D scenarios.
	 * </p>
	 *
	 * <p>
	 * Callers that need to keep an envelope explicitly 2D (e.g. to record a 2D bounding box) should use
	 * {@link #expandBy(double, double)} directly.
	 * </p>
	 *
	 * @param distance
	 *            the distance to expand the envelope in each axis
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
	 * Tests intersection considering only the X and Y dimensions (Z is ignored).
	 *
	 * <p>
	 * Overrides the default to delegate directly to the JTS {@link Envelope#intersects(Envelope)} method, which is
	 * purely 2D. This is correct because the quadtree partitions space in XY only; agents may carry non-zero Z in 3D
	 * environments, but those should still be found by any 2D proximity search.
	 * </p>
	 *
	 * @param env
	 *            the envelope to test against (Z is ignored)
	 * @return {@code true} if the XY projections overlap
	 */
	@Override
	public boolean intersects2D(final IEnvelope env) {
		if (env instanceof Envelope other) return super.intersects(other);
		// Fallback for non-Envelope IEnvelope implementations
		return super.intersects(new org.locationtech.jts.geom.Envelope(env.getMinX(), env.getMaxX(), env.getMinY(), env.getMaxY()));
	}

	/**
	 * Check if the region defined by <code>other</code> overlaps (intersects) the region of this <code>Envelope</code>
	 * in full 3D (X, Y and Z are all considered).
	 *
	 * @param other
	 *            the <code>Envelope</code> which this <code>Envelope</code> is being checked for overlapping
	 * @return <code>true</code> if the <code>Envelope</code>s overlap in all three dimensions
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
	 * Returns the maximum Z coordinate of the given envelope. Plain JTS {@link Envelope} instances, which carry no Z
	 * information, are treated as flat (Z = 0), consistent with the contract that a non-{@link GamaEnvelope} is a 2D
	 * envelope whose Z range is {@code [0, 0]}.
	 *
	 * @param other
	 *            the envelope to query; must not be {@code null}
	 * @return the maximum Z value, or {@code 0d} if {@code other} is not a {@link GamaEnvelope}
	 */
	private double getMaxZOf(final Envelope other) {
		if (other instanceof GamaEnvelope e) return e.maxz;
		return 0d;
	}

	/**
	 * Returns the minimum Z coordinate of the given envelope. Plain JTS {@link Envelope} instances, which carry no Z
	 * information, are treated as flat (Z = 0), consistent with the contract that a non-{@link GamaEnvelope} is a 2D
	 * envelope whose Z range is {@code [0, 0]}.
	 *
	 * @param other
	 *            the envelope to query; must not be {@code null}
	 * @return the minimum Z value, or {@code 0d} if {@code other} is not a {@link GamaEnvelope}
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
		return super.equals(other) && isZeroWidth(minz, otherEnvelope.getMinZ())
				&& isZeroWidth(maxz, otherEnvelope.getMaxZ());
	}

	/**
	 * Checks if this envelope is flat, i.e. has no extent in the Z dimension.
	 *
	 * <p>
	 * Returns {@code true} in two cases:
	 * </p>
	 * <ul>
	 *   <li>The envelope is <em>null</em> (empty geometry) — a null envelope has no extent at all, including in Z.</li>
	 *   <li>The envelope is <em>2D</em>, i.e. {@code minz == maxz} (depth is zero).</li>
	 * </ul>
	 *
	 * @return {@code true} if the envelope is null or has zero depth
	 */
	@Override
	public boolean isFlat() { return isNull() || minz == maxz; }

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