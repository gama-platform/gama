/*******************************************************************************************************
 *
 * IDelegatingShape.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.shape;

import org.locationtech.jts.geom.Geometry;

import gama.core.common.geometry.Envelope3D;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;

/**
 *
 */
public interface IDelegatingShape extends IShape {

	/**
	 * Gets the delegate shape.
	 *
	 * @return the delegate shape
	 */
	@Override
	IShape getGeometry();

	/**
	 * Sets the delegate shape.
	 *
	 * @param shape
	 *            the new delegate shape
	 */
	@Override
	void setGeometry(IShape shape);

	/**
	 * Checks if is delegate.
	 *
	 * @return true, if is delegate
	 */
	default boolean isDelegate() { return getGeometry() != null; }

	/***
	 * All the methods of IShape are delegated by default to getGeometry()
	 */

	/**
	 * Method getArea(). Simply delegates to the geometry
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getArea()
	 */
	@Override
	default Double getArea() { return getGeometry().getArea(); }

	/**
	 * Method getVolume(). Simply delegates to the geometry
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getVolume()
	 */
	@Override
	default Double getVolume() { return getGeometry().getVolume(); }

	/**
	 * Method getPerimeter()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getPerimeter()
	 */
	@Override
	default double getPerimeter() { return getGeometry().getPerimeter(); }

	/**
	 * Method getHoles()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getHoles()
	 */
	@Override
	default IList<GamaShape> getHoles() { return getGeometry().getHoles(); }

	/**
	 * Method getCentroid()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getCentroid()
	 */
	@Override
	default GamaPoint getCentroid() { return getGeometry().getCentroid(); }

	/**
	 * Method getExteriorRing()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getExteriorRing()
	 */
	@Override
	default GamaShape getExteriorRing(final IScope scope) {
		return getGeometry().getExteriorRing(scope);
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getWidth()
	 */
	@Override
	default Double getWidth() { return getGeometry().getWidth(); }

	/**
	 * Method getHeight()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getDepth()
	 */
	@Override
	default Double getHeight() { return getGeometry().getHeight(); }

	/**
	 * Method getDepth()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getDepth()
	 */
	@Override
	default Double getDepth() { return getGeometry().getDepth(); }

	/**
	 * Method getGeometricEnvelope()
	 *
	 * @see gama.core.metamodel.shape.IGeometricalShape#getGeometricEnvelope()
	 */
	@Override
	default GamaShape getGeometricEnvelope() { return getGeometry().getGeometricEnvelope(); }

	/**
	 * Gets the geometries.
	 *
	 * @return the geometries
	 */
	@Override
	default IList<? extends IShape> getGeometries() { return getGeometry().getGeometries(); }

	/**
	 * Method isMultiple()
	 *
	 * @see gama.core.metamodel.shape.IShape#isMultiple()
	 */
	@Override
	default boolean isMultiple() { return getGeometry().isMultiple(); }

	/**
	 * Checks if is point.
	 *
	 * @return true, if is point
	 */
	@Override
	default boolean isPoint() { return getGeometry().isPoint(); }

	/**
	 * Checks if is line.
	 *
	 * @return true, if is line
	 */
	@Override
	default boolean isLine() { return getGeometry().isLine(); }

	/**
	 * Gets the inner geometry.
	 *
	 * @return the inner geometry
	 */
	@Override
	default Geometry getInnerGeometry() { return getGeometry().getInnerGeometry(); }

	/**
	 * Returns the envelope of the geometry of the agent, or null if the geometry has not yet been defined
	 *
	 */
	@Override
	default Envelope3D getEnvelope() {
		final IShape g = getGeometry();
		return g == null ? null : g.getEnvelope();
	}

	/**
	 * Covers.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean covers(final IShape g) {
		return getGeometry().covers(g);
	}

	/**
	 * Euclidian distance to.
	 *
	 * @param g
	 *            the g
	 * @return the double
	 */
	@Override
	default double euclidianDistanceTo(final IShape g) {
		return getGeometry().euclidianDistanceTo(g);
	}

	/**
	 * Euclidian distance to.
	 *
	 * @param g
	 *            the g
	 * @return the double
	 */
	@Override
	default double euclidianDistanceTo(final GamaPoint g) {
		return getGeometry().euclidianDistanceTo(g);
	}

	/**
	 * Intersects.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean intersects(final IShape g) {
		return getGeometry().intersects(g);
	}

	/**
	 * Partially overlaps.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean partiallyOverlaps(final IShape g) {
		return getGeometry().partiallyOverlaps(g);
	}

	/**
	 * Touches.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean touches(final IShape g) {
		return getGeometry().touches(g);
	}

	/**
	 * Crosses.
	 *
	 * @param g
	 *            the g
	 * @return true, if successful
	 */
	@Override
	default boolean crosses(final IShape g) {
		return getGeometry().crosses(g);
	}

	/**
	 * @see gama.core.common.interfaces.IGeometry#setInnerGeometry(org.locationtech.jts.geom.Geometry)
	 */
	@Override
	default void setInnerGeometry(final Geometry geom) {
		getGeometry().setInnerGeometry(geom);
	}

	/**
	 * Method getPoints()
	 *
	 * @see gama.core.metamodel.shape.IShape#getPoints()
	 */
	@Override
	default IList<GamaPoint> getPoints() {
		if (getGeometry() == null) return GamaListFactory.EMPTY_LIST;
		return getGeometry().getPoints();
	}

	/**
	 * Sets the depth.
	 *
	 * @param depth
	 *            the new depth
	 */
	@Override
	default void setDepth(final double depth) {
		if (getGeometry() == null) return;
		getGeometry().setDepth(depth);
	}

	/**
	 * Sets the geometrical type.
	 *
	 * @param t
	 *            the new geometrical type
	 */
	@Override
	default void setGeometricalType(final Type t) {
		getGeometry().setGeometricalType(t);
	}

}
