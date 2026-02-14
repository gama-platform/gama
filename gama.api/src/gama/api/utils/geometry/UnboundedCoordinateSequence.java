/*******************************************************************************************************
 *
 * UnboundedCoordinateSequence.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import static com.google.common.collect.Iterators.forArray;
import static com.google.common.collect.Iterators.limit;

import java.util.Arrays;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import gama.api.data.factories.GamaPointFactory;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonArray;
import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IPoint;

/**
 * The Class UnboundedCoordinateSequence.
 */
public class UnboundedCoordinateSequence implements ICoordinates {

	/** The dimension. */
	final int dimension;

	/** The Constant INITIAL_SIZE. */
	final static int INITIAL_SIZE = 1000;

	/** The points. */
	GamaPoint[] points = null;

	/** The nb points. */
	int nbPoints;

	/** The temp. */
	final IPoint temp = GamaPointFactory.create();

	/**
	 * Fill from.
	 *
	 * @param begin
	 *            the begin
	 */
	private void fillFrom(final int begin) {
		for (int i = begin; i < points.length; i++) { points[i] = (GamaPoint) GamaPointFactory.create(); }
	}

	/**
	 * Instantiates a new unbounded coordinate sequence.
	 */
	public UnboundedCoordinateSequence() {
		this(3);
	}

	/**
	 * Instantiates a new unbounded coordinate sequence.
	 *
	 * @param dimension
	 *            the dimension
	 */
	public UnboundedCoordinateSequence(final int dimension) {
		this.dimension = dimension;
		growTo(INITIAL_SIZE);
	}

	/**
	 * Grow to.
	 *
	 * @param size
	 *            the size
	 */
	private void growTo(final int size) {
		int begin = 0;
		if (points == null) {
			points = new GamaPoint[size];
		} else {
			if (size <= points.length) return;
			begin = points.length;
			points = Arrays.copyOf(points, Math.max(size, begin + begin / 2));
		}
		fillFrom(begin);
	}

	@Override
	public int getDimension() { return 3; }

	/**
	 * Instantiates a new unbounded coordinate sequence.
	 *
	 * @param dimension
	 *            the dimension
	 * @param copy
	 *            the copy
	 * @param size
	 *            the size
	 * @param points2
	 *            the points 2
	 */
	UnboundedCoordinateSequence(final int dimension, final boolean copy, final int size, final IPoint[] points2) {
		this.dimension = dimension;
		growTo(size);
		nbPoints = size;
		for (int i = 0; i < size; i++) { points[i].setLocation(points2[i]); }
		if (copy) { ensureClockwiseness(); }
	}

	@Override
	public final UnboundedCoordinateSequence copy() {
		return new UnboundedCoordinateSequence(dimension, true, nbPoints, points);
	}

	@Override
	@Deprecated
	public UnboundedCoordinateSequence clone() {
		return copy();
	}

	@Override
	public Coordinate getCoordinateCopy(final int i) {
		return points[i].clone();
	}

	@Override
	public void getCoordinate(final int index, final Coordinate coord) {
		coord.setCoordinate(points[index].toCoordinate());
	}

	@Override
	public double getX(final int index) {
		return points[index].getX();
	}

	@Override
	public double getY(final int index) {
		return points[index].getY();
	}

	@Override
	public double getOrdinate(final int index, final int ordinateIndex) {
		return points[index].getOrdinate(ordinateIndex);
	}

	@Override
	public int size() {
		return nbPoints;
	}

	@Override
	public void setOrdinate(final int index, final int ordinateIndex, final double value) {
		points[index].setOrdinate(ordinateIndex, value);

	}

	@Override
	public Envelope expandEnvelope(final Envelope env) {
		for (int i = 0; i < nbPoints - 1; i++) { env.expandToInclude(points[i].toCoordinate()); }
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
		for (int i = 0; i < nbPoints - 1; i++) { env.expandToInclude(points[i].toCoordinate()); }
		return env;
	}

	@Override
	public Iterator<IPoint> iterator() {
		return limit(forArray(points), nbPoints);
	}

	@Override
	public void addCenterTo(final IPoint other) {
		final int size = isRing() ? nbPoints - 1 : nbPoints;
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

	@Override
	public Coordinate getCoordinate(final int i) {
		return points[i].toCoordinate();
	}

	@Override
	public ICoordinates yNegated() {
		final int size = points.length;
		final IPoint[] points2 = new IPoint[size];
		for (int i = 0; i < size; i++) {
			// CW property is ensured by reversing the resulting array
			points2[i] = points[size - i - 1].yNegated();
		}
		return new GamaCoordinateSequence(dimension, false, points2);

	}

	@Override
	public Coordinate[] toCoordinateArray() {
		if (nbPoints == points.length) return points;
		return Arrays.copyOf(points, nbPoints);
	}

	@Override
	public void visit(final IndexedVisitor v, final int max, final boolean clockwise) {
		final int limit = max < 0 || max > nbPoints ? nbPoints : max;
		final boolean reversed = isRing() && !clockwise;
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
		v.process(0, points[0].getX(), points[0].getY(), points[0].getZ());
		for (int i = max - 1; i > 0; i--) {
			final IPoint p = points[i];
			v.process(i, p.getX(), p.getY(), p.getZ());
		}
	}

	@Override
	public void visitClockwise(final VertexVisitor v) {
		final int max = isRing() ? nbPoints - 1 : nbPoints;
		for (int i = 0; i < max; i++) {
			final IPoint p = points[i];
			v.process(p.getX(), p.getY(), p.getZ());
		}

	}

	@Override
	public void visitYNegatedCounterClockwise(final VertexVisitor v) {
		final int max = isRing() ? nbPoints - 1 : nbPoints;
		for (int i = 0; i < max; i++) {
			final IPoint p = points[i];
			v.process(p.getX(), -p.getY(), p.getZ());
		}

	}

	@Override
	public void visit(final PairVisitor v) {
		for (int i = 0; i < nbPoints - 1; i++) { v.process(points[i], points[i + 1]); }
	}

	@Override
	public void getNormal(final boolean clockwise, final double factor, final IPoint normal) {
		normal.setLocation(0, 0, 0);
		if (nbPoints < 3) return;
		for (int i = 0; i < nbPoints - 1; i++) {
			final IPoint v0 = points[i];
			final IPoint v1 = points[i + 1];
			normal.add((v0.getY() - v1.getY()) * (v0.getZ() + v1.getZ()),
					(v0.getZ() - v1.getZ()) * (v0.getX() + v1.getX()),
					(v0.getX() - v1.getX()) * (v0.getY() + v1.getY()));
		}
		if (!isRing()) {
			final IPoint v0 = points[0];
			final IPoint v1 = points[1];
			normal.add((v0.getY() - v1.getY()) * (v0.getZ() + v1.getZ()),
					(v0.getZ() - v1.getZ()) * (v0.getX() + v1.getX()),
					(v0.getX() - v1.getX()) * (v0.getY() + v1.getY()));
		}
		final double norm = clockwise ? -normal.norm() : normal.norm();
		normal.divideBy(norm / factor);
	}

	@Override
	public IEnvelope getEnvelopeInto(final IEnvelope envelope) {
		envelope.setToNull();
		expandEnvelope(envelope);
		return envelope;
	}

	@Override
	public double averageZ() {
		double sum = 0d;
		if (nbPoints == 0) return sum;
		for (int i = 0; i < nbPoints; i++) { sum += points[i].getZ(); }
		return sum / nbPoints;
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
		growTo(points2.length);
		nbPoints = points2.length;
		for (int i = 0; i < nbPoints; i++) { points[i].setLocation(points2[i]); }
		return this;
	}

	@Override
	public ICoordinates setTo(final int index, final double... points2) {
		growTo(points2.length / 3);
		nbPoints = points2.length / 3;
		for (int i = index / 3; i < nbPoints; i++) {
			points[i].setLocation(points2[i * 3], points2[i * 3 + 1], points2[i * 3 + 2]);
		}
		ensureClockwiseness();
		return this;
	}

	@Override
	public void replaceWith(final int i, final double x, final double y, final double z) {
		if (i < 0 || i >= nbPoints) return;
		points[i].setLocation(x, y, z);

	}

	@Override
	public IPoint directionBetweenLastPointAndOrigin() {
		final IPoint result = GamaPointFactory.create();
		final IPoint origin = points[0];
		for (int i = nbPoints - 1; i > 0; i--) {
			if (!points[i].equals(origin)) {
				result.setLocation(points[i]).subtract(origin).normalize();
				return result;
			}
		}
		return result;
	}

	@Override
	public void applyRotation(final Rotation3D rotation) {
		for (int i = 0; i < nbPoints; i++) { rotation.applyTo(points[i].toCoordinate()); }
	}

	@Override
	public boolean isHorizontal() {
		final double z = points[0].getZ();
		for (int i = 1; i < nbPoints; i++) { if (points[i].getZ() != z) return false; }
		return true;
	}

	@Override
	public double getLength() {
		double result = 0;
		for (int i = 1; i < nbPoints; i++) { result += points[i].euclidianDistanceTo(points[i - 1]); }
		return result;
	}

	@Override
	public void setAllZ(final double elevation) {
		for (int i = 0; i < nbPoints; i++) { points[i].setZ(elevation); }
	}

	@Override
	public boolean isCoveredBy(final IEnvelope envelope3d) {
		for (int i = 0; i < nbPoints; i++) { if (!envelope3d.covers(points[i].toCoordinate())) return false; }
		return true;
	}

	@Override
	public boolean isClockwise() { return signedArea() > 0; }

	@Override
	public void completeRing() {
		points[nbPoints++] = points[0];
	}

	@Override
	public void translateBy(final double i, final double j, final double k) {
		for (int index = 0; i < nbPoints; index++) { points[index].add(i, j, k); }

	}

	@Override
	public void ensureClockwiseness() {
		if (isRing() && signedArea() <= 0) { reverse(); }
	}

	/**
	 * Checks if is ring.
	 *
	 * @return true, if is ring
	 */
	public boolean isRing() {
		if (nbPoints < 4) return false;
		return points[0].equals(points[nbPoints - 1]);
	}

	/**
	 * Signed area.
	 *
	 * @return the double
	 */
	public double signedArea() {
		if (nbPoints < 3) return 0.0;
		double sum = 0.0;
		/**
		 * Based on the Shoelace formula. http://en.wikipedia.org/wiki/Shoelace_formula
		 */
		final double x0 = points[0].getX();
		for (int i = 1; i < nbPoints - 1; i++) {
			final double x = points[i].getX() - x0;
			final double y1 = points[i + 1].getY();
			final double y2 = points[i - 1].getY();
			sum += x * (y2 - y1);
		}
		return sum / 2.0;
	}

	/**
	 * Sets the to Y negated.
	 *
	 * @param other
	 *            the new to Y negated
	 */
	public void setToYNegated(final ICoordinates other) {
		growTo(other.size());
		nbPoints = other.size();
		int i = 0;
		for (final IPoint p : other) { points[i++].setLocation(p.getX(), -p.getY(), p.getZ()); }
		if (isRing()) { reverse(); }
	}

	/**
	 * Sets the to.
	 *
	 * @param other
	 *            the new to
	 */
	public void setTo(final ICoordinates other) {
		growTo(other.size());
		nbPoints = other.size();
		int i = 0;
		for (final IPoint p : other) { points[i++].setLocation(p); }
	}

	/**
	 * Reverse.
	 */
	@Override
	public void reverse() {
		for (int i = nbPoints - 1, j = 0; i >= nbPoints / 2; j++, i--) {
			temp.setLocation(points[i]);
			points[i].setLocation(points[j]);
			points[j].setLocation(temp);

		}
	}

	@Override
	public IPoint at(final int i) {
		if (i < 0 || i > nbPoints - 1) return null;
		return points[i];
	}

	@Override
	public IPoint[] toPointsArray() {
		return points;
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
