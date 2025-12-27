/*******************************************************************************************************
 *
 * IProjection.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology.projection;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.locationtech.jts.geom.Geometry;

import gama.core.common.geometry.Envelope3D;
import gama.core.runtime.IScope;

/**
 * Class IProjection.
 *
 * @author drogoul
 * @since 17 déc. 2013
 *
 */
public interface IProjection {

	/**
	 * Creates the transformation.
	 *
	 * @param t
	 *            the t
	 */
	void createTransformation(final MathTransform t);

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
	CoordinateReferenceSystem getInitialCRS(IScope scope);

	/**
	 * Gets the target CRS.
	 *
	 * @param scope
	 *            the scope
	 * @return the target CRS
	 */
	CoordinateReferenceSystem getTargetCRS(IScope scope);

	/**
	 * Gets the projected envelope.
	 *
	 * @return the projected envelope
	 */
	Envelope3D getProjectedEnvelope();

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