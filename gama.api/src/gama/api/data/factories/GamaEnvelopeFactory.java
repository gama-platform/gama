/*******************************************************************************************************
 *
 * GamaEnvelopeFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
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

/**
 * A static factory for creating {@link IEnvelope} instances. This class provides methods to create envelopes (bounding
 * boxes) from various geometries, shapes, and coordinate inputs. It delegates the creation to an
 * {@link IEnvelopeFactory} implementation.
 */
public class GamaEnvelopeFactory implements IFactory<IEnvelope> {

	/**
	 * The internal factory used for creating envelope instances.
	 */
	private static IEnvelopeFactory InternalFactory;

	/**
	 * A shared, empty {@link IEnvelope} instance.
	 */
	public static IEnvelope EMPTY;

	/**
	 * Configures the internal factory and initializes the EMPTY constant.
	 *
	 * @param factory
	 *            the {@link IEnvelopeFactory} to be used.
	 */
	public static void setBuilder(final IEnvelopeFactory factory) {
		InternalFactory = factory;
		EMPTY = InternalFactory.ofImmutable(0, 0, 0, 0);
	}

	/**
	 * Creates a new, empty {@link IEnvelope}.
	 *
	 * @return a new empty envelope.
	 */
	public static IEnvelope create() {
		return InternalFactory.create();
	}

	/**
	 * Creates an envelope encompassing the given JTS {@link Geometry}.
	 *
	 * @param g
	 *            the geometry to compute the envelope from.
	 * @return the geometry's envelope.
	 */
	public static IEnvelope of(final Geometry g) {
		return InternalFactory.of(g);
	}

	/**
	 * Creates an envelope encompassing the given JTS {@link GeometryCollection}.
	 *
	 * @param g
	 *            the geometry collection.
	 * @return the collection's envelope.
	 */
	public static IEnvelope of(final GeometryCollection g) {
		return InternalFactory.of(g);
	}

	/**
	 * Creates an envelope that encompasses all shapes in the provided list.
	 *
	 * @param list
	 *            the list of shapes.
	 * @return the combined envelope of all shapes in the list.
	 */
	public static IEnvelope of(final List<IShape> list) {
		return InternalFactory.of(list);
	}

	/**
	 * Creates an envelope from a GAMA {@link IShape}.
	 *
	 * @param s
	 *            the shape.
	 * @return the shape's envelope.
	 */
	public static IEnvelope of(final IShape s) {
		return of(s.getInnerGeometry());
	}

	/**
	 * Creates an envelope from a single {@link IPoint}.
	 *
	 * @param s
	 *            the point.
	 * @return an envelope containing only the point.
	 */
	public static IEnvelope of(final IPoint s) {
		return of(s.toCoordinate());
	}

	/**
	 * Creates an {@link IEnvelope} from a JTS {@link Envelope}.
	 *
	 * @param e
	 *            the source JTS envelope.
	 * @return the corresponding {@link IEnvelope} instance.
	 */
	public static IEnvelope of(final Envelope e) {
		return InternalFactory.of(e);
	}

	/**
	 * Creates a copy of an existing {@link IEnvelope}.
	 *
	 * @param e
	 *            the source envelope.
	 * @return a new copy of the envelope.
	 */
	public static IEnvelope of(final IEnvelope e) {
		return InternalFactory.of(e);
	}

	/**
	 * Creates a new envelope with the Y axis negated (useful for screen coordinates transformation).
	 *
	 * @param e
	 *            the source envelope.
	 * @return a new envelope with negated Y coordinates.
	 */
	public static IEnvelope withYNegated(final IEnvelope e) {
		return InternalFactory.withYNegated(e);
	}

	/**
	 * Creates an envelope from a single JTS {@link Coordinate}.
	 *
	 * @param p
	 *            the coordinate.
	 * @return an envelope containing the coordinate.
	 */
	public static IEnvelope of(final Coordinate p) {
		return InternalFactory.of(p);
	}

	/**
	 * Creates an envelope defined by the specified bounds (min/max for X, Y, Z).
	 *
	 * @param x1
	 *            the minimum X coordinate.
	 * @param x2
	 *            the maximum X coordinate.
	 * @param y1
	 *            the minimum Y coordinate.
	 * @param y2
	 *            the maximum Y coordinate.
	 * @param z1
	 *            the minimum Z coordinate.
	 * @param z2
	 *            the maximum Z coordinate.
	 * @return the defined {@link IEnvelope}.
	 */
	public static IEnvelope of(final double x1, final double x2, final double y1, final double y2, final double z1,
			final double z2) {
		return InternalFactory.of(x1, x2, y1, y2, z1, z2);
	}

	/**
	 * Creates a 2D envelope defined by the specified bounds (min/max for X, Y). Z coordinates are initialized to 0.
	 *
	 * @param x1
	 *            the minimum X coordinate.
	 * @param x2
	 *            the maximum X coordinate.
	 * @param y1
	 *            the minimum Y coordinate.
	 * @param y2
	 *            the maximum Y coordinate.
	 * @return the defined {@link IEnvelope}.
	 */
	public static IEnvelope of(final double x1, final double x2, final double y1, final double y2) {
		return of(x1, x2, y1, y2, 0, 0);
	}

	/**
	 * Releases an envelope back to the pool, if pooling is implemented.
	 *
	 * @param envelope3d
	 *            the envelope to release.
	 */
	public static void release(final IEnvelope envelope3d) {
		InternalFactory.release(envelope3d);
	}

	/**
	 * Compute envelope from.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the i envelope
	 */
	public static IEnvelope computeEnvelopeFrom(final IScope scope, final Object obj) {
		return InternalFactory.computeEnvelopeFrom(scope, obj);
	}

}