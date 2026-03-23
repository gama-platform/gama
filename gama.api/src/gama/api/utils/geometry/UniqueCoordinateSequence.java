/*******************************************************************************************************
 *
 * UniqueCoordinateSequence.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import com.google.common.collect.Iterators;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonArray;

/**
 * A 'sequence' of points containing an unique point.
 *
 * @author drogoul
 *
 */
public class UniqueCoordinateSequence implements ICoordinates {

	/** The point. */
	final IPoint point;

	/** The dimension. */
	final int dimension;

	/**
	 * Instantiates a new unique coordinate sequence.
	 *
	 * @param dimension
	 *            the dimension
	 * @param coord
	 *            the coord
	 */
	public UniqueCoordinateSequence(final int dimension, final IPoint coord) {
		this.dimension = dimension;
		point = GamaPointFactory.create(coord);
	}

	/**
	 * Instantiates a new unique coordinate sequence.
	 *
	 * @param dimension
	 *            the dimension.
	 * @param coord
	 *            the coord
	 */
	public UniqueCoordinateSequence(final int dimension, final Coordinate coord) {
		this.dimension = dimension;
		point = GamaPointFactory.create(coord);
	}

	/**
	 * Instantiates a new unique coordinate sequence.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param ordinates
	 *            the ordinates
	 * @date 5 nov. 2023
	 */
	public UniqueCoordinateSequence(final double... ordinates) {
		this.dimension = ordinates.length;
		point = switch (dimension) {
			case 0 -> GamaPointFactory.create();
			case 1 -> GamaPointFactory.create(ordinates[0], ordinates[0]);
			case 2 -> GamaPointFactory.create(ordinates[0], ordinates[1]);
			default -> GamaPointFactory.create(ordinates[0], ordinates[1], ordinates[2]);
		};
	}

	/**
	 * Instantiates a new unique coordinate sequence.
	 *
	 * @param dimension
	 *            the dimension
	 * @param copy
	 *            the copy
	 * @param gamaPoint
	 *            the gama point
	 */
	public UniqueCoordinateSequence(final int dimension, final boolean copy, final IPoint gamaPoint) {
		this.dimension = dimension;
		point = gamaPoint;
	}

	@Override
	public int getDimension() { return dimension; }

	@Override
	public Coordinate getCoordinate(final int i) {
		return point.toCoordinate();
	}

	@Override
	public Coordinate getCoordinateCopy(final int i) {
		return GamaPointFactory.create(point).toCoordinate();
	}

	@Override
	public void getCoordinate(final int index, final Coordinate coord) {
		coord.x = point.getX();
		coord.y = point.getY();
		coord.z = point.getZ();

	}

	@Override
	public double getX(final int index) {
		return point.getX();
	}

	@Override
	public double getY(final int index) {
		return point.getY();
	}

	@Override
	public double getOrdinate(final int index, final int ordinateIndex) {
		return point.getOrdinate(ordinateIndex);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void setOrdinate(final int index, final int ordinateIndex, final double value) {
		point.setOrdinate(ordinateIndex, value);

	}

	@Override
	public Coordinate[] toCoordinateArray() {
		return new Coordinate[] { point.toCoordinate() };
	}

	@Override
	public Envelope expandEnvelope(final Envelope env) {
		env.expandToInclude(point.toCoordinate());
		return env;
	}

	@Override
	public Iterator<IPoint> iterator() {
		return Iterators.singletonIterator(point);
	}

	@Override
	public ICoordinates yNegated() {
		return new UniqueCoordinateSequence(dimension, false, point.yNegated());
	}

	@Override
	public final UniqueCoordinateSequence copy() {
		return new UniqueCoordinateSequence(dimension, GamaPointFactory.create(point));
	}

	@Override
	@Deprecated
	public UniqueCoordinateSequence clone() {
		return copy();
	}

	@Override
	public void visit(final IndexedVisitor v, final int max, final boolean reversed) {
		if (max == 0) return;
		v.process(0, point.getX(), point.getY(), point.getZ());
	}

	@Override
	public void visit(final PairVisitor v) {
		// Nothing to do here
	}

	/**
	 * Gets the normal.
	 *
	 * @param clockwise
	 *            the clockwise
	 * @param factor
	 *            the factor
	 * @param normal
	 *            the normal
	 * @return the normal
	 */
	@Override
	public void getNormal(final boolean clockwise, final double factor, final IPoint normal) {
		normal.setLocation(0, 0, clockwise ? -factor : factor);
	}

	@Override
	public double averageZ() {
		return point.getZ();
	}

	/**
	 * Sets the to.
	 *
	 * @param points
	 *            the points
	 * @return the i coordinates
	 */
	@Override
	public ICoordinates setTo(final IPoint... points) {
		if (points.length == 0) return this;
		final IPoint p = points[0];
		point.setLocation(p);
		return this;
	}

	@Override
	public ICoordinates setTo(final int index, final double... points) {
		if (index > 0 || points.length < 3) return this;
		point.setX(points[0]);
		point.setY(points[1]);
		point.setZ(points[2]);
		return this;
	}

	/**
	 * Adds the center to.
	 *
	 * @param other
	 *            the other
	 */
	@Override
	public void addCenterTo(final IPoint other) {
		other.add(point);
	}

	@Override
	public IEnvelope getEnvelopeInto(final IEnvelope envelope) {
		envelope.setToNull();
		envelope.expandToInclude(point);
		return envelope;
	}

	@Override
	public IPoint directionBetweenLastPointAndOrigin() {
		return GamaPointFactory.getNullPoint();
	}

	@Override
	public void applyRotation(final Rotation3D rotation) {
		rotation.applyTo(point);
	}

	@Override
	public void replaceWith(final int i, final double x, final double y, final double z) {
		if (i != 0) return;
		point.setLocation(x, y, z);

	}

	@Override
	public boolean isHorizontal() { return true; }

	@Override
	public double getLength() { return 0; }

	@Override
	public void setAllZ(final double elevation) {
		point.setZ(elevation);

	}

	@Override
	public boolean isCoveredBy(final IEnvelope env) {
		return env.covers(point);
	}

	@Override
	public void visitClockwise(final VertexVisitor v) {
		v.process(point.getX(), point.getY(), point.getZ());

	}

	@Override
	public void visitYNegatedCounterClockwise(final VertexVisitor v) {
		v.process(point.getX(), -point.getY(), point.getZ());

	}

	@Override
	public boolean isClockwise() { return true; }

	@Override
	public void completeRing() {}

	@Override
	public void translateBy(final double i, final double j, final double k) {
		point.add(i, j, k);
	}

	@Override
	public void ensureClockwiseness() {

	}

	@Override
	public IPoint at(final int i) {
		return point;
	}

	@Override
	public IPoint[] toPointsArray() {
		return new IPoint[] { point };
	}

	@Override
	public void reverse() {}

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
