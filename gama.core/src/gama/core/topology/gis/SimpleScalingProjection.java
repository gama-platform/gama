/*******************************************************************************************************
 *
 * SimpleScalingProjection.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.gis;

import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.Geometry;

import gama.api.kernel.topology.ICoordinateReferenceSystem;
import gama.api.kernel.topology.IProjection;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.IEnvelope;

/**
 * The Class SimpleScalingProjection.
 */
public class SimpleScalingProjection implements IProjection {

	/** The inverse scaling. */
	public CoordinateFilter scaling, inverseScaling;

	/**
	 * Instantiates a new simple scaling projection.
	 *
	 * @param scale
	 *            the scale
	 */
	public SimpleScalingProjection(final Double scale) {
		if (scale != null) { createScalingTransformations(scale); }

	}

	@Override
	public Geometry transform(final Geometry geom) {
		if (scaling != null) {
			geom.apply(scaling);
			geom.geometryChanged();
		}
		return geom;
	}

	@Override
	public Geometry inverseTransform(final Geometry geom) {
		if (inverseScaling != null) {
			geom.apply(inverseScaling);
			geom.geometryChanged();
		}
		return geom;
	}

	/**
	 * Creates the scaling transformations.
	 *
	 * @param scale
	 *            the scale
	 */
	public void createScalingTransformations(final Double scale) {
		scaling = coord -> {
			coord.x *= scale;
			coord.y *= scale;
			coord.z *= scale;
		};
		inverseScaling = coord -> {
			coord.x /= scale;
			coord.y /= scale;
			coord.z /= scale;
		};
	}

	@Override
	public ICoordinateReferenceSystem getInitialCRS(final IScope scope) {
		return null;
	}

	@Override
	public ICoordinateReferenceSystem getTargetCRS(final IScope scope) {
		return null;
	}

	@Override
	public IEnvelope getProjectedEnvelope() { return null; }

	@Override
	public void translate(final Geometry geom) {

	}

	@Override
	public void inverseTranslate(final Geometry geom) {

	}

	@Override
	public void convertUnit(final Geometry geom) {}

	@Override
	public void inverseConvertUnit(final Geometry geom) {

	}

}
