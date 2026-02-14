/*******************************************************************************************************
 *
 * InternalGamaCoordinateSequenceFactory.java, in gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

import gama.api.data.factories.GamaCoordinateSequenceFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.factories.ICoordinateSequenceFactory;
import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IPoint;

/**
 * A factory for creating GamaCoordinateSequence objects.
 */
public class InternalGamaCoordinateSequenceFactory implements ICoordinateSequenceFactory {

	/** The empty. */
	static ICoordinates EMPTY = new GamaCoordinateSequence(3);

	/**
	 * Creates a sequence filled with {0,0,0} points of the given length
	 *
	 * @param length
	 *            the length of the sequence
	 * @return a new ICoordinates with the given length
	 */
	static ICoordinates ofLength(final int length) {
		return GamaCoordinateSequenceFactory.create(length, 3);
	}

	/**
	 * Creates a new GamaCoordinateSequence object.
	 *
	 * @return the i coordinates
	 */
	@Override
	public ICoordinates createEmpty() {
		return EMPTY;
	}

	/**
	 * Method create()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.Coordinate[])
	 */
	@Override
	public ICoordinates create(final Coordinate[] coordinates) {
		if (coordinates.length == 1) return new UniqueCoordinateSequence(3, coordinates[0]);
		return new GamaCoordinateSequence(3, coordinates);
	}

	/**
	 * Creates the.
	 *
	 * @param coordinates
	 *            the coordinates
	 * @return the i coordinates
	 */
	@Override
	public ICoordinates create(final IPoint[] coordinates) {
		if (coordinates.length == 1) return new UniqueCoordinateSequence(3, coordinates[0]);
		return new GamaCoordinateSequence(3, true, coordinates);
	}

	/**
	 * Creates the.
	 *
	 * @param coordinates
	 *            the coordinates
	 * @param copy
	 *            the copy
	 * @return the i coordinates
	 */
	@Override
	public ICoordinates create(final IPoint[] coordinates, final boolean copy) {
		if (coordinates.length == 1) return new UniqueCoordinateSequence(3, coordinates[0]);
		return new GamaCoordinateSequence(3, copy, coordinates);
	}

	/**
	 * Method create()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.CoordinateSequence)
	 */
	@Override
	public ICoordinates create(final CoordinateSequence cs) {
		if (cs.size() == 1) return new UniqueCoordinateSequence(cs.getDimension(), (IPoint) cs.getCoordinate(0));
		if (cs instanceof GamaCoordinateSequence gcs) return gcs.copy();
		return new GamaCoordinateSequence(cs.getDimension(), cs.toCoordinateArray());
	}

	/**
	 * Method create()
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(int, int)
	 */
	@Override
	public ICoordinates create(final int size, final int dimension) {
		if (size == 1) return new UniqueCoordinateSequence(dimension, GamaPointFactory.create());
		return new GamaCoordinateSequence(dimension, size);
	}

}