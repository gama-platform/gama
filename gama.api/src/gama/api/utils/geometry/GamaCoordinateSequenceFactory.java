/*******************************************************************************************************
 *
 * GamaCoordinateSequenceFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;

/**
 * A static factory for creating {@link ICoordinates} instances. This class acts as a global access point and wrapper
 * around an {@link ICoordinateSequenceFactory}, delegating all creation requests to it. It ensures that coordinate
 * sequences are created consistently across the platform.
 */
public class GamaCoordinateSequenceFactory {

	/** The JTS factory. */
	static CoordinateSequenceFactory JTSFactory;

	/** The empty. */
	static ICoordinates EMPTY = new GamaCoordinateSequence(3);

	/**
	 * A constant coordinate sequence representing a "keystone" identity, typically used for internal comparisons or
	 * default transformations.
	 */
	private static ICoordinates KEYSTONE_IDENTITY;

	/**
	 * Gets the keystone identity.
	 *
	 * @return the keystone identity
	 */
	public static ICoordinates getKeystoneIdentity() {
		if (KEYSTONE_IDENTITY == null) { KEYSTONE_IDENTITY = create(4, 3).setTo(0d, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0); }
		return KEYSTONE_IDENTITY;
	}
	//
	// /**
	// * Configures the internal factory used by this class and initializes static constants.
	// *
	// * @param factory
	// * the {@link ICoordinateSequenceFactory} implementation to use.
	// */
	// public static void setBuilder(final ICoordinateSequenceFactory factory) { InternalFactory = factory; }

	/**
	 * Creates an {@link ICoordinates} sequence from an array of JTS {@link Coordinate}.
	 *
	 * @param coordinates
	 *            the array of coordinates to wrap or copy.
	 * @return a new {@link ICoordinates} instance containing the given coordinates.
	 */
	public static ICoordinates create(final Coordinate[] coordinates) {
		if (coordinates.length == 1) return new UniqueCoordinateSequence(3, coordinates[0]);
		return new GamaCoordinateSequence(3, coordinates);
	}

	/**
	 * Creates an {@link ICoordinates} sequence from an existing JTS {@link CoordinateSequence}.
	 *
	 * @param coordSeq
	 *            the source coordinate sequence.
	 * @return a new {@link ICoordinates} instance wrapping or copying the source sequence.
	 */
	public static ICoordinates create(final CoordinateSequence cs) {
		if (cs.size() == 1) return new UniqueCoordinateSequence(cs.getDimension(), (IPoint) cs.getCoordinate(0));
		if (cs instanceof GamaCoordinateSequence gcs) return gcs.copy();
		return new GamaCoordinateSequence(cs.getDimension(), cs.toCoordinateArray());
	}

	/**
	 * Creates a new, empty {@link ICoordinates} sequence with the specified size and dimension.
	 *
	 * @param size
	 *            the number of coordinates in the sequence.
	 * @param dimension
	 *            the dimension of the coordinates (e.g., 2 or 3).
	 * @return a newly created {@link ICoordinates} instance.
	 */
	public static ICoordinates create(final int size, final int dimension) {
		if (size == 1) return new UniqueCoordinateSequence(dimension, GamaPointFactory.create());
		return new GamaCoordinateSequence(dimension, size);
	}

	/**
	 * Creates a new, empty {@link ICoordinates} sequence (size 0).
	 *
	 * @return a shared or new empty {@link ICoordinates} instance.
	 */
	public static ICoordinates getEmpty() { return EMPTY; }

	/**
	 * Creates an {@link ICoordinates} sequence from a variable number of {@link IPoint} instances.
	 *
	 * @param coordinates
	 *            a varargs list of points to include in the sequence.
	 * @return a new {@link ICoordinates} instance containing the points.
	 */
	public static ICoordinates create(final IPoint... coordinates) {
		if (coordinates.length == 1) return new UniqueCoordinateSequence(3, coordinates[0]);
		return new GamaCoordinateSequence(3, true, coordinates);
	}

	/**
	 * Creates an {@link ICoordinates} sequence from an array of {@link IPoint}, optionally copying them.
	 *
	 * @param coordinates
	 *            the array of points.
	 * @param copy
	 *            if true, the points are copied; otherwise, they might be referenced directly if supported.
	 * @return a new {@link ICoordinates} instance.
	 */
	public static ICoordinates create(final IPoint[] coordinates, final boolean copy) {
		if (coordinates.length == 1) return new UniqueCoordinateSequence(3, coordinates[0]);
		return new GamaCoordinateSequence(3, copy, coordinates);
	}

	/**
	 * Creates a new {@link ICoordinates} sequence of the specified length with a default dimension (usually 3).
	 *
	 * @param i
	 *            the number of coordinates to allocate.
	 * @return a new {@link ICoordinates} instance.
	 */
	public static ICoordinates ofLength(final int i) {
		return create(i, 3);
	}

	/**
	 * Gets the contour coordinates.
	 *
	 * @param g
	 *            the g
	 * @return the contour coordinates
	 */
	public static ICoordinates pointsOf(final Polygon g) {
		if (g.isEmpty()) return getEmpty();
		if (g.getExteriorRing().getCoordinateSequence() instanceof CoordinateArraySequence)
			return create(g.getExteriorRing().getCoordinates());
		return (ICoordinates) g.getExteriorRing().getCoordinateSequence();
	}

	/**
	 * Gets the contour coordinates.
	 *
	 * @param g
	 *            the g
	 * @return the contour coordinates
	 */
	public static ICoordinates pointsOf(final Geometry g) {
		return g == null || g.isEmpty() ? getEmpty() : switch (g) {
			case Polygon p -> pointsOf(p);
			case LineString l -> (ICoordinates) l.getCoordinateSequence();
			case Point pt -> (ICoordinates) pt.getCoordinateSequence();
			case GeometryCollection gc -> pointsOf(gc.convexHull());
			default -> getEmpty();
		};
	}

	/**
	 * @return
	 */
	public static CoordinateSequenceFactory getJTSCoordinateSequenceFactory() {
		if (JTSFactory == null) {
			JTSFactory = new CoordinateSequenceFactory() {

				@Override
				public CoordinateSequence create(final Coordinate[] coordinates) {
					return GamaCoordinateSequenceFactory.create(coordinates);
				}

				@Override
				public CoordinateSequence create(final CoordinateSequence coordSeq) {
					return GamaCoordinateSequenceFactory.create(coordSeq);
				}

				@Override
				public CoordinateSequence create(final int size, final int dimension) {
					return GamaCoordinateSequenceFactory.create(size, dimension);
				}
			};

		}
		return JTSFactory;
	}

}
