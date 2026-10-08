/*******************************************************************************************************
 *
 * IEnvelope.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;

import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;

/**
 *
 */
public interface IEnvelope extends IIntersectable {

	/**
	 * Checks whether this envelope has no extent in the Z dimension.
	 *
	 * <p>
	 * Returns {@code true} when:
	 * </p>
	 * <ul>
	 *   <li>The envelope is <em>null</em> (empty geometry) — it has no extent in any dimension.</li>
	 *   <li>The envelope is <em>2D</em> — {@code minZ == maxZ} (depth is zero).</li>
	 * </ul>
	 *
	 * @return {@code true} if the envelope is null or has zero depth
	 */
	boolean isFlat();

	/**
	 * Returns {@code true} when this envelope has meaningful extent in the Z dimension, i.e. it is the logical
	 * complement of {@link #isFlat()}.
	 *
	 * <p>
	 * This convenience predicate is used to choose the right spatial index implementation: a {@code GamaOctTree}
	 * (which partitions all three axes) is appropriate when the world has genuine depth; a {@code GamaQuadTree}
	 * (XY-only) is sufficient for flat/2D worlds.
	 * </p>
	 *
	 * @return {@code true} if {@code getDepth() > 0}, {@code false} otherwise
	 */
	default boolean is3D() { return !isFlat(); }

	/**
	 * Tests intersection considering only the X and Y dimensions (Z is ignored).
	 *
	 * <p>
	 * Required for 2D spatial indexes (such as the quadtree) that partition space in XY only but may store agents
	 * with non-zero Z coordinates (3D worlds). A full 3D intersection check would incorrectly exclude those agents.
	 * </p>
	 *
	 * @param env
	 *            the envelope to test against (Z is ignored)
	 * @return {@code true} if the XY projections overlap
	 */
	boolean intersects2D(IEnvelope env);

	/**
	 * Checks if is null.
	 *
	 * @return true, if is null
	 */
	boolean isNull();

	/**
	 * Y negated.
	 *
	 * @return the i envelope
	 */
	IEnvelope yNegated();

	/**
	 * Rotate.
	 *
	 * @param rotation
	 *            the rotation
	 * @return the i envelope
	 */
	IEnvelope rotate(AxisAngle rotation);

	/**
	 * To geometry.
	 *
	 * @return the polygon
	 */
	Polygon toGeometry();

	/**
	 * Expand to include.
	 *
	 * @param other
	 *            the other
	 */
	void expandToInclude(final IEnvelope other);

	/**
	 * Enlarges this <code>Envelope</code> so that it contains the given {@link Coordinate}. Has no effect if the point
	 * is already on or within the envelope.
	 *
	 * @param p
	 *            the Coordinate to expand to include
	 */
	void expandToInclude(final Coordinate p);

	/**
	 * Expand to include.
	 *
	 * @param p
	 *            the p
	 */
	default void expandToInclude(final IPoint p) {
		expandToInclude(p.toCoordinate());
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
	void expandToInclude(final double x, final double y, final double z);

	/**
	 * Intersection I.
	 *
	 * @param ie
	 *            the ie
	 * @return the i envelope
	 */
	IEnvelope intersection(final IEnvelope ie);

	/**
	 * Distance.
	 *
	 * @param env
	 *            the env
	 * @return the double
	 */
	double distance(final IEnvelope env);

	/**
	 * Covers.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	boolean covers(final IEnvelope other);

	/**
	 * Covers.
	 *
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	boolean covers(final Coordinate p);

	/**
	 * Covers.
	 *
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	default boolean covers(final IPoint p) {
		return covers(p.toCoordinate());
	}

	/**
	 * Check if the point <code>p</code> overlaps (lies inside) the region of this <code>Envelope</code>.
	 *
	 * @param p
	 *            the <code>Coordinate</code> to be tested
	 * @return <code>true</code> if the point overlaps this <code>Envelope</code>
	 */
	@Override
	boolean intersects(final Coordinate p);

	/**
	 * Intersects.
	 *
	 * @param p
	 *            the p
	 * @return true, if successful
	 */
	default boolean intersects(final IPoint p) {
		return intersects(p.toCoordinate());
	}

	/**
	 * Intersects.
	 *
	 * @param other
	 *            the other
	 * @return true, if successful
	 */
	@Override
	boolean intersects(final IEnvelope other);

	/**
	 * Computes the coordinate of the centre of this envelope (as long as it is non-null
	 *
	 * @return the centre coordinate of this envelope <code>null</code> if the envelope is null
	 */
	IPoint center();

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
	IEnvelope translate(final double transX, final double transY, final double transZ);

	/**
	 * Expands this envelope by a given distance in all directions. Both positive and negative distances are supported.
	 *
	 * @param deltaX
	 *            the distance to expand the envelope along the the X axis
	 * @param deltaY
	 *            the distance to expand the envelope along the the Y axis
	 */
	void expandBy(final double deltaX, final double deltaY, final double deltaZ);

	/**
	 * Expands this envelope by a given distance in all directions. Both positive and negative distances are supported.
	 *
	 * @param distance
	 *            the distance to expand the envelope
	 */
	void expandBy(final double distance);

	/**
	 * Gets the maximum extent of this envelope across all three dimensions.
	 *
	 * @return the maximum extent of this envelope
	 */
	double maxExtent();

	/**
	 * Gets the minimum extent of this envelope across all three dimensions.
	 *
	 * @return the minimum extent of this envelope
	 */
	double minExtent();

	/**
	 * Gets the volume of this envelope.
	 *
	 * @return the volume of the envelope
	 * @return 0.0 if the envelope is null
	 */
	double getVolume();

	/**
	 * Returns the <code>Envelope</code>s maximum z-value. min z > max z indicates that this is a null
	 * <code>Envelope</code>.
	 *
	 * @return the maximum z-coordinate
	 */
	double getMaxZ();

	/**
	 * Returns the <code>Envelope</code>s minimum z-value. min z > max z indicates that this is a null
	 * <code>Envelope</code>.
	 *
	 * @return the minimum z-coordinate
	 */
	double getMinZ();

	/**
	 * Returns the difference between the maximum and minimum z values.
	 *
	 * @return max z - min z, or 0 if this is a null <code>Envelope</code>
	 */
	double getDepth();

	/**
	 * Makes this <code>Envelope</code> a "null" envelope, that is, the envelope of the empty geometry.
	 */
	void setToNull();

	/**
	 * Initialize an <code>Envelope</code> from an existing 3D Envelope.
	 *
	 * @param env
	 *            the 3D Envelope to initialize from
	 */
	void init(final IEnvelope env);

	/**
	 * Inits the.
	 *
	 * @param env
	 *            the env
	 */
	void init(final Envelope env);

	/**
	 * Initialize an <code>Envelope</code> to a region defined by a single Coordinate.
	 *
	 * @param p
	 *            the coordinate
	 */
	void init(final Coordinate p);

	/**
	 * Initialize an <code>Envelope</code> to a region defined by two Coordinates.
	 *
	 * @param p1
	 *            the first Coordinate
	 * @param p2
	 *            the second Coordinate
	 */
	void init(final Coordinate p1, final Coordinate p2);

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
	void init(final double x1, final double x2, final double y1, final double y2, final double z1, final double z2);

	/**
	 * @return
	 */
	double getMaxX();

	/**
	 * @return
	 */
	double getMaxY();

	/**
	 * @return
	 */
	double getMinX();

	/**
	 * @return
	 */
	double getMinY();

	/**
	 * Initialize an <code>Envelope</code> for a 2D region defined by X and Y bounds. The Z range is set to
	 * {@code [0, 0]}, producing a <em>flat</em> (2D) envelope. Calls {@link #init(double, double, double, double,
	 * double, double)} with {@code z1 = z2 = 0}.
	 *
	 * @param minX
	 *            the minimum X coordinate
	 * @param maxX
	 *            the maximum X coordinate
	 * @param minY
	 *            the minimum Y coordinate
	 * @param maxY
	 *            the maximum Y coordinate
	 */
	default void init(final double minX, final double maxX, final double minY, final double maxY) {
		init(minX, maxX, minY, maxY, 0.0, 0.0);
	}

	/**
	 * @return
	 */
	double getWidth();

	/**
	 * @return
	 */
	double getHeight();

	/**
	 * @return
	 */
	double getArea();

	/**
	 * @return
	 */
	IShape toShape();

	/**
	 * @param from
	 * @return
	 */
	IEnvelope set(IEnvelope from);

}
