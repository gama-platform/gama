/*******************************************************************************************************
 *
 * GamaCoordinateSequence.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import static com.google.common.collect.Iterators.forArray;
import static gama.api.utils.geometry.GamaGeometryFactory.isRing;
import static gama.api.utils.geometry.GamaGeometryFactory.signedArea;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import gama.api.data.json.IJson;
import gama.api.data.json.IJsonArray;
import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IPoint;

/**
 * Clockwise sequence of points. Supports several computations (rotation, etc.) and a cheap visitor pattern. Be aware
 * that CW property is not maintained if individual points are modified via the setOrdinate() or replaceWith() method
 * and if the sequence is not a ring. All other methods should however maintain it.
 *
 * @author A. Drogoul
 *
 */

public class GamaCoordinateSequence implements ICoordinates {

	/** The dimension. */
	final int dimension;

	/**
	 * The final array of GamaPoint, considered to be internally mutable (i.e. points can be changed inside)
	 */
	final IPoint[] points;

	/**
	 * Creates a sequence from an array of points. The points will be cloned before being added (to prevent side
	 * effects). The order of the points will not necessarily remain the same if the sequence is a ring (as this class
	 * enforces a clockwise direction of the sequence)
	 *
	 * @param points2
	 *            an array of points
	 */
	GamaCoordinateSequence(final int dimension, final Coordinate... points2) {
		this(dimension, true, points2);
	}

	/**
	 * Creates a sequence from an array of points. If copy is true, the points are cloned before being added to the
	 * sequence (to prevent side effects, for instance). The sequence will be modified to enforce a clockwise direction
	 * if the array represents a ring
	 *
	 * @param copy
	 *            whether or not to copy the points or to add them directly
	 * @param points2
	 *            an array of points
	 */
	GamaCoordinateSequence(final int dimension, final boolean copy, final Coordinate... points2) {
		this.dimension = dimension;
		if (copy) {
			final int size = points2.length;
			points = new IPoint[size];
			for (int i = 0; i < size; i++) { points[i] = GamaPointFactory.create(points2[i]); }
			ensureClockwiseness();
		} else {
			points = (IPoint[]) points2;
		}
	}

	/**
	 * Instantiates a new gama coordinate sequence.
	 *
	 * @param dimension
	 *            the dimension.
	 * @param copy
	 *            the copy
	 * @param points2
	 *            the points 2
	 */
	GamaCoordinateSequence(final int dimension, final boolean copy, final IPoint... points2) {
		this.dimension = dimension;
		if (copy) {
			final int size = points2.length;
			points = new IPoint[size];
			for (int i = 0; i < size; i++) { points[i] = GamaPointFactory.create(points2[i]); }
			ensureClockwiseness();
		} else {
			points = points2;
		}
	}

	/**
	 * Creates a sequence of points with a given size (that may be altered after)
	 *
	 * @param size
	 *            an int > 0 (negative sizes will be treated as 0)
	 */
	GamaCoordinateSequence(final int dimension, final int size) {
		this.dimension = dimension;
		points = new IPoint[size < 0 ? 0 : size];
		for (int i = 0; i < points.length; i++) { points[i] = GamaPointFactory.create(); }
	}

	/**
	 * Method getDimension(). Always 3 for these sequences
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#getDimension()
	 */
	@Override
	public int getDimension() { return dimension; }

	/**
	 * Makes a complete copy of this sequence (incl. cloning the points themselves)
	 */
	@Override
	public final GamaCoordinateSequence copy() {
		return new GamaCoordinateSequence(dimension, true, points);
	}

	@Override
	@Deprecated
	public GamaCoordinateSequence clone() {
		return copy();
	}

	@Override
	public String toString() {
		return Arrays.toString(points);
	}

	/**
	 * Method getCoordinate(). The coordinate is *not* a copy of the original one, so any modification to it will
	 * directly affect the sequence of points
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinate(int)
	 */
	@Override
	public GamaPoint getCoordinate(final int i) {
		return (GamaPoint) points[i];
	}

	/**
	 * Method getCoordinateCopy()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinateCopy(int)
	 */
	@Override
	public GamaPoint getCoordinateCopy(final int i) {
		return ((GamaPoint) points[i]).clone();
	}

	/**
	 * Method getCoordinate()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinate(int, org.locationtech.jts.geom.Coordinate)
	 */
	@Override
	public void getCoordinate(final int index, final Coordinate coord) {
		coord.setCoordinate(points[index].toCoordinate());
	}

	/**
	 * Method getX()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#getX(int)
	 */
	@Override
	public double getX(final int index) {
		return points[index].getX();
	}

	/**
	 * Method getY()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#getY(int)
	 */
	@Override
	public double getY(final int index) {
		return points[index].getY();
	}

	/**
	 * Method getOrdinate()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#getOrdinate(int, int)
	 */
	@Override
	public double getOrdinate(final int index, final int ordinateIndex) {
		return points[index].getOrdinate(ordinateIndex);
	}

	/**
	 * Method size()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#size()
	 */
	@Override
	public int size() {
		return points.length;
	}

	/**
	 * Method setOrdinate(). Be aware that CW property is not maintained in case of direct modifications like this
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
	 */
	@Override
	public void setOrdinate(final int index, final int ordinateIndex, final double value) {
		points[index].setOrdinate(ordinateIndex, value);
	}

	/**
	 * Method toCoordinateArray()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#toCoordinateArray()
	 */
	@Override
	public Coordinate[] toCoordinateArray() {
		final int size = points.length;
		final Coordinate[] result = new Coordinate[size];
		for (int i = 0; i < size; i++) {
			final IPoint p = points[i];
			result[i] = p instanceof GamaPoint g ? g.toCoordinate() : (Coordinate) p;
		}
		return result;
	}

	/**
	 * Method expandEnvelope()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequence#expandEnvelope(org.locationtech.jts.geom.Envelope)
	 */
	@Override
	public Envelope expandEnvelope(final Envelope env) {
		for (final IPoint p : points) { env.expandToInclude((Coordinate) p); }
		return env;
	}

	/**
	 * Expand envelope.
	 *
	 * @param env
	 *            the env
	 * @return the i envelope
	 */
	public IEnvelope expandEnvelope(final IEnvelope env) {
		for (final IPoint p : points) { env.expandToInclude(p); }
		return env;
	}

	@Override
	public Iterator<IPoint> iterator() {
		return forArray(points);
	}

	@Override
	public void addCenterTo(final IPoint other) {
		final int size = isRing(points) ? points.length - 1 : points.length;
		double x = 0, y = 0, z = 0;
		for (int i = 0; i < size; i++) {
			final IPoint p = points[i];
			x += p.getX();
			y += p.getY();
			z += p.getZ();
		}
		x /= size;
		y /= size;
		z /= size;
		other.add(x, y, z);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.core.common.util.ICoordinates#yNegated()
	 */
	@Override
	public ICoordinates yNegated() {
		final int size = points.length;
		final IPoint[] points2 = new IPoint[size];
		for (int i = 0; i < size; i++) {
			final IPoint p = points[size - i - 1];
			// CW property is ensured by reversing the resulting array
			points2[i] =
					p instanceof IPoint gp ? gp.yNegated() : GamaPointFactory.create(p.getX(), -p.getY(), p.getZ());
		}
		return new GamaCoordinateSequence(dimension, false, points2);
	}

	@Override
	public void visit(final IndexedVisitor v, final int max, final boolean clockwise) {
		final int limit = max < 0 || max > points.length ? points.length : max;
		final boolean reversed = isRing(points) && !clockwise;
		if (reversed) {
			reverseVisit(v, limit);
		} else {
			visit(v, limit);
		}
	}

	/**
	 * Visit.
	 *
	 * @param v
	 *            the v
	 * @param max
	 *            the max
	 */
	private void visit(final IndexedVisitor v, final int max) {
		for (int i = 0; i < max; i++) {
			final IPoint p = points[i];
			v.process(i, p.getX(), p.getY(), p.getZ());
		}
	}

	/**
	 * Reverse visit.
	 *
	 * @param v
	 *            the v
	 * @param max
	 *            the max
	 */
	private void reverseVisit(final IndexedVisitor v, final int max) {
		for (int i = max - 1, j = 0; i >= 0; i--, j++) {
			final IPoint p = points[i];
			v.process(j, p.getX(), p.getY(), p.getZ());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * gama.core.common.util.ICoordinates#visitConsecutive(gama.core.common.util.GamaCoordinateSequence.PairVisitor)
	 */
	@Override
	public void visit(final PairVisitor v) {
		for (int i = 0; i < points.length - 1; i++) { v.process(points[i], points[i + 1]); }
	}

	/**
	 * Computes the normal to this sequence of points based on Newell's algorithm, which has proved to be quite robust
	 * even with self-intersecting sequences or non-convex polygons. Its downside is that it processes all the points
	 * (instead of processing only 3 of them) but robustness has a price ! This algorithm only operates on rings (this
	 * is ensured in the code by processing the first point in case the sequence is not a ring).
	 *
	 * @param clockwise
	 *            whether to obtain the normal facing up (for clockwise sequences) or down.
	 * @param factor
	 *            the factor to multiply the unit normal vector with
	 * @param normal
	 *            the returned vector
	 */

	@Override
	public void getNormal(final boolean clockwise, final double factor, final IPoint normal) {
		normal.setLocation(0, 0, 0);
		if (points.length < 3) return;
		for (int i = 0; i < points.length - 1; i++) {
			final IPoint v0 = points[i];
			final IPoint v1 = points[i + 1];
			normal.add((v0.getY() - v1.getY()) * (v0.getZ() + v1.getZ()),
					(v0.getZ() - v1.getZ()) * (v0.getX() + v1.getX()),
					(v0.getX() - v1.getX()) * (v0.getY() + v1.getY()));
		}
		if (!isRing(points)) {
			final IPoint v0 = points[0];
			final IPoint v1 = points[1];
			normal.add((v0.getY() - v1.getY()) * (v0.getZ() + v1.getZ()),
					(v0.getZ() - v1.getZ()) * (v0.getX() + v1.getX()),
					(v0.getX() - v1.getX()) * (v0.getY() + v1.getY()));
		}
		final double norm = clockwise ? -normal.norm() : normal.norm();
		normal.divideBy(norm / factor);
	}

	/**
	 * Gets the envelope into.
	 *
	 * @param envelope
	 *            the envelope
	 * @return the envelope into
	 */
	@Override
	public IEnvelope getEnvelopeInto(final IEnvelope envelope) {
		envelope.setToNull();
		expandEnvelope(envelope);
		return envelope;
	}

	@Override
	public double averageZ() {
		double sum = 0d;
		if (points.length == 0) return sum;
		for (final IPoint p : points) { sum += p.getZ(); }
		return sum / points.length;
	}

	/**
	 * Sets the to.
	 *
	 * @param points2
	 *            the points 2
	 * @return the i coordinates
	 */
	@Override
	public ICoordinates setTo(final IPoint... points2) {
		final int size = Math.min(points2.length, points.length);
		for (int i = 0; i < size; i++) { points[i].setCoordinate(points2[i].toCoordinate()); }
		ensureClockwiseness();
		return this;
	}

	@Override
	public ICoordinates setTo(final int index, final double... points2) {
		final int size = Math.min(points2.length, points.length * 3);
		for (int i = index; i < size; i += 3) {
			final IPoint self = points[i / 3];
			self.setLocation(points2[i], points2[i + 1], points2[i + 2]);
		}
		ensureClockwiseness();
		return this;
	}

	@Override
	public IPoint directionBetweenLastPointAndOrigin() {
		final IPoint result = GamaPointFactory.create();
		final IPoint origin = points[0];
		for (int i = points.length - 1; i > 0; i--) {
			if (!points[i].equals(origin)) {
				result.setLocation(points[i]).subtract(origin).normalize();
				return result;
			}
		}
		// In case all points are equal (e.g. a point geometry treated as a polygon), we return a default direction
		result.setLocation(1, 0, 0);
		return result;
	}

	@Override
	public void applyRotation(final Rotation3D rotation) {
		for (final IPoint point : points) { rotation.applyTo(point); }
	}

	@Override
	public void replaceWith(final int i, final double x, final double y, final double z) {
		if (i < 0 || i >= points.length) return;
		points[i].setLocation(x, y, z);

	}

	@Override
	public boolean isHorizontal() {
		final double z = points[0].getZ();
		for (int i = 1; i < points.length; i++) { if (points[i].getZ() != z) return false; }
		return true;
	}

	@Override
	public double getLength() {
		double result = 0;
		for (int i = 1; i < points.length; i++) { result += points[i].euclidianDistanceTo(points[i - 1]); }
		return result;
	}

	@Override
	public void setAllZ(final double elevation) {
		for (IPoint point : points) { point.setZ(elevation); }

	}

	/**
	 * Checks if is covered by.
	 *
	 * @param env
	 *            the env
	 * @return true, if is covered by
	 */
	@Override
	public boolean isCoveredBy(final IEnvelope env) {
		for (final IPoint point : points) { if (!env.covers(point)) return false; }
		return true;
	}

	@Override
	public void visitClockwise(final VertexVisitor v) {
		final int max = isRing(points) ? points.length - 1 : points.length;
		for (int i = 0; i < max; i++) {
			final IPoint p = points[i];
			v.process(p.getX(), p.getY(), p.getZ());
		}

	}

	/**
	 * Same as clockwise, since it visits the coordinates with y-negated
	 */
	@Override
	public void visitYNegatedCounterClockwise(final VertexVisitor v) {
		final int max = isRing(points) ? points.length - 1 : points.length;
		for (int i = 0; i < max; i++) {
			final IPoint p = points[i];
			v.process(p.getX(), -p.getY(), p.getZ());
		}

	}

	@Override
	public boolean isClockwise() { return signedArea(points) > 0; }

	@Override
	public void completeRing() {
		points[points.length - 1] = points[0];
	}

	@Override
	public void translateBy(final double x, final double y, final double z) {
		for (final IPoint p : points) { p.add(x, y, z); }

	}

	/**
	 * Turns this sequence of coordinates into a clockwise orientation. Only done for rings (as it may change the
	 * definition of line strings)
	 *
	 * @param points
	 * @return
	 */
	@Override
	public void ensureClockwiseness() {
		if (!isRing(points)) return;
		if (signedArea(points) <= 0) { ArrayUtils.reverse(points); }
	}

	@Override
	public boolean equals(final Object object) {
		if (!(object instanceof GamaCoordinateSequence other)) return false;
		return Arrays.equals(points, other.points);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(points);
	}

	@Override
	public IPoint at(final int i) {
		if (i > size() || i < 0) return null;
		return points[i];
	}

	@Override
	public IPoint[] toPointsArray() {
		return points;
	}

	@Override
	public void reverse() {
		ArrayUtils.reverse(points);
	}

	/**
	 * Serialize to json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @return the json value
	 * @date 4 nov. 2023
	 */
	@Override
	public IJsonArray serializeToJson(final IJson json) {
		IJsonArray result = json.array();
		IPoint work = GamaPointFactory.create();
		for (int i = 0; i < size(); i++) {
			getCoordinate(i, work);
			result.add(json.array(work.getX(), work.getY(), work.getZ()));
		}
		return result;

	}

}
