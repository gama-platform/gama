/*******************************************************************************************************
 *
 * IProjection.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.topology;

import org.locationtech.jts.geom.Geometry;

import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IEnvelope;

/**
 * Class IProjection.
 *
 *
 * TODO find a way to remove the dependency on GeoTools in this interface
 *
 *
 * @author drogoul
 * @since 17 déc. 2013
 *
 */
public interface IProjection {

	/**
	 * Transform.
	 *
	 * @param g
	 *            the g
	 * @return the geometry
	 */
	Geometry transform(final Geometry g);

	/**
	 * Inverse transform.
	 *
	 * @param g
	 *            the g
	 * @return the geometry
	 */
	Geometry inverseTransform(final Geometry g);

	/**
	 * Gets the initial CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the initial CRS
	 */
	ICoordinateReferenceSystem getInitialCRS(IScope scope);

	/**
	 * Gets the target CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the target CRS
	 */
	ICoordinateReferenceSystem getTargetCRS(IScope scope);

	/**
	 * Gets the projected envelope.
	 *
	 * @return the projected envelope
	 */
	IEnvelope getProjectedEnvelope();

	/**
	 * @param geom
	 */
	void translate(Geometry geom);

	/**
	 * Inverse translate.
	 *
	 * @param geom
	 *            the geom
	 */
	void inverseTranslate(Geometry geom);

	/**
	 * Convert unit.
	 *
	 * @param geom
	 *            the geom
	 */
	void convertUnit(Geometry geom);

	/**
	 * Inverse convert unit.
	 *
	 * @param geom
	 *            the geom
	 */
	void inverseConvertUnit(Geometry geom);

}