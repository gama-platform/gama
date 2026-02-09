/*******************************************************************************************************
 *
 * IPointFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import org.locationtech.jts.geom.Coordinate;

import gama.api.data.objects.IPoint;

/**
 *
 */
public interface IPointFactory extends IFactory<IPoint> {

	/**
	 * Returns the constant representing a null point (typically (0,0,0)).
	 *
	 * @return the null point
	 */
	IPoint getNullPoint();

	/**
	 * Creates an immutable 3D point.
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param z
	 *            z coordinate
	 * @return the immutable point
	 */
	IPoint createImmutable(double x, double y, double z);

	/**
	 * Creates an immutable 2D point (z is 0).
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return the immutable point
	 */
	IPoint createImmutable(double x, double y);

	/**
	 * Creates an immutable point as a copy of another point.
	 *
	 * @param p
	 *            the source point
	 * @return the immutable point
	 */
	IPoint createImmutable(IPoint p);

	/**
	 * Creates an immutable point from a JTS Coordinate.
	 *
	 * @param p
	 *            the JTS coordinate
	 * @return the immutable point
	 */
	IPoint createImmutable(Coordinate p);

	/**
	 * Creates a mutable point initialized to (0,0,0).
	 *
	 * @return the new point
	 */
	IPoint create();

	/**
	 * Creates a mutable 2D point.
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return the new point
	 */
	IPoint create(double x, double y);

	/**
	 * Creates a mutable 3D point.
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param z
	 *            z coordinate
	 * @return the new point
	 */
	IPoint create(double x, double y, double z);

	/**
	 * Creates a mutable point as a copy of another point.
	 *
	 * @param p
	 *            the source point
	 * @return the new point
	 */
	IPoint create(IPoint p);

	/**
	 * Creates a mutable point from a JTS Coordinate.
	 *
	 * @param p
	 *            the JTS coordinate
	 * @return the new point
	 */
	IPoint create(Coordinate p);

}