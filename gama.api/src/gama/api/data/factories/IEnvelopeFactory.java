/*******************************************************************************************************
 *
 * IEnvelopeFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import gama.api.data.objects.IEnvelope;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IEnvelopeComputer;

/**
 *
 */
public interface IEnvelopeFactory extends IFactory<IEnvelope>, IEnvelopeComputer {

	/**
	 * Creates a new empty, 3D envelope.
	 *
	 * @return the new envelope
	 */
	IEnvelope create();

	/**
	 * Creates an envelope that bounds the given geometry.
	 *
	 * @param g
	 *            the geometry to bound
	 * @return the bounding envelope
	 */
	IEnvelope of(Geometry g);

	/**
	 * Creates an envelope that bounds the given geometry collection.
	 *
	 * @param g
	 *            the geometry collection
	 * @return the bounding envelope
	 */
	IEnvelope of(GeometryCollection g);

	/**
	 * Creates an envelope that bounds a list of shapes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param list
	 *            the list of shapes
	 * @return the bounding envelope
	 * @date 18 juil. 2023
	 */
	IEnvelope of(List<IShape> list);

	/**
	 * Creates an envelope that bounds a single shape.
	 *
	 * @param s
	 *            the shape
	 * @return the bounding envelope
	 */
	IEnvelope of(IShape s);

	/**
	 * Creates an envelope around a single point.
	 *
	 * @param s
	 *            the point
	 * @return the envelope containing the point
	 */
	IEnvelope of(IPoint s);

	/**
	 * Creates an IEnvelope from a JTS Envelope.
	 *
	 * @param e
	 *            the source JTS Envelope
	 * @return the IEnvelope
	 */
	IEnvelope of(Envelope e);

	/**
	 * copy or wrap an existing IEnvelope.
	 *
	 * @param e
	 *            the source envelope
	 * @return the new IEnvelope
	 */
	IEnvelope of(IEnvelope e);

	/**
	 * Return a new IEnvelope with the Y coordinates negated (often used for coordinate system conversion).
	 *
	 * @param e
	 *            the source envelope
	 * @return the new envelope with negated Y
	 */
	IEnvelope withYNegated(IEnvelope e);

	/**
	 * Creates an envelope around a specific coordinate.
	 *
	 * @param p
	 *            the coordinate
	 * @return the envelope
	 */
	IEnvelope of(Coordinate p);

	/**
	 * Creates an envelope defined by min/max values for x, y, and z.
	 *
	 * @param x1
	 *            min x
	 * @param x2
	 *            max x
	 * @param y1
	 *            min y
	 * @param y2
	 *            max y
	 * @param z1
	 *            min z
	 * @param z2
	 *            max z
	 * @return the defined envelope
	 */
	IEnvelope of(double x1, double x2, double y1, double y2, double z1, double z2);

	/**
	 * Creates a 2D envelope defined by min/max values for x and y (z is assumed 0 or ignored).
	 *
	 * @param x1
	 *            min x
	 * @param x2
	 *            max x
	 * @param y1
	 *            min y
	 * @param y2
	 *            max y
	 * @return the defined envelope
	 */
	IEnvelope of(double x1, double x2, double y1, double y2);

	/**
	 * Creates an immutable 2D envelope defined by min/max values.
	 *
	 * @param x1
	 *            min x
	 * @param x2
	 *            max x
	 * @param y1
	 *            min y
	 * @param y2
	 *            max y
	 * @return the immutable envelope
	 */
	IEnvelope ofImmutable(double x1, double x2, double y1, double y2);

	/**
	 * Releases an envelope back to the pool if pooling is supported.
	 *
	 * @param envelope3d
	 *            the envelope to release
	 */
	void release(IEnvelope envelope3d);

	/**
	 * Computes an envelope from an arbitrary object (list, shape, point, etc.) within a scope.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the object to verify boundaries for
	 * @return the computed envelope
	 */
	IEnvelope computeEnvelopeFrom(IScope scope, Object obj);

}