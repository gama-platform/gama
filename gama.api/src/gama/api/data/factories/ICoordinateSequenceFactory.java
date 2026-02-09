/*******************************************************************************************************
 *
 * ICoordinateSequenceFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

import gama.api.data.objects.ICoordinates;
import gama.api.data.objects.IPoint;

/**
 *
 */
public interface ICoordinateSequenceFactory extends CoordinateSequenceFactory {

	/**
	 * Creates a new, empty ICoordinates object (sequence of coordinates).
	 *
	 * @return an empty ICoordinates object
	 */
	ICoordinates createEmpty();

	/**
	 * Creates an ICoordinates sequence from an array of JTS Coordinates.
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.Coordinate[])
	 */
	@Override
	ICoordinates create(final Coordinate[] coordinates);

	/**
	 * Creates an ICoordinates sequence from an array of IPoint objects.
	 *
	 * @param coordinates
	 *            the array of points
	 * @return the sequence of coordinates
	 */
	ICoordinates create(final IPoint[] coordinates);

	/**
	 * Creates an ICoordinates sequence from an array of IPoint objects, optionally copying them.
	 *
	 * @param coordinates
	 *            the array of points
	 * @param copy
	 *            whether to copy the points or use references if possible
	 * @return the sequence of coordinates
	 */
	ICoordinates create(final IPoint[] coordinates, final boolean copy);

	/**
	 * Creates an ICoordinates sequence from an existing JTS CoordinateSequence.
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.CoordinateSequence)
	 */
	@Override
	ICoordinates create(final CoordinateSequence cs);

	/**
	 * Creates a new sequence of a given size and dimension.
	 *
	 * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(int, int)
	 */
	@Override
	ICoordinates create(final int size, final int dimension);

}
