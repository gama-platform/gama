/*******************************************************************************************************
 *
 * IBox2DPhysicalEntity.java, in gaml.extensions.physics, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.physics.box2d_version;

import org.jbox2d.common.Vec2;

import gama.core.metamodel.shape.GamaPoint;
import gama.extension.physics.common.IPhysicalEntity;
import gama.extension.physics.common.VectorUtils;

/**
 * The Interface IBox2DPhysicalEntity.
 */
public interface IBox2DPhysicalEntity extends IPhysicalEntity<Vec2> {

	/**
	 * Gets the scale.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the scale
	 * @date 1 oct. 2023
	 */
	float getScale();

	/**
	 * To vector.
	 *
	 * @param v
	 *            the v
	 * @return the vec 2
	 */
	@Override
	default Vec2 toVector(final GamaPoint v) {
		return VectorUtils.toBox2DVector(v, getScale());
	}

	/**
	 * To vector.
	 *
	 * @param v
	 *            the v
	 * @param to
	 *            the to
	 * @return the vec 2
	 */
	default Vec2 toVector(final GamaPoint v, final Vec2 to) {
		return VectorUtils.toBox2DVector(v, to, getScale());
	}

	/**
	 * To gama point.
	 *
	 * @param v
	 *            the v
	 * @return the gama point
	 */
	@Override
	default GamaPoint toGamaPoint(final Vec2 v) {
		return VectorUtils.toGamaPoint(v, getScale());
	}

	/**
	 * To gama point.
	 *
	 * @param v
	 *            the v
	 * @param result
	 *            the result
	 * @return the gama point
	 */
	default GamaPoint toGamaPoint(final Vec2 v, final GamaPoint result) {
		return VectorUtils.toGamaPoint(v, result, getScale());
	}

	/**
	 * To gama.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param value
	 *            the value
	 * @return the double
	 * @date 1 oct. 2023
	 */
	default double toGama(final float value) {
		return value / getScale();
	}

	/**
	 * To box 2 D.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param value
	 *            the value
	 * @return the float
	 * @date 1 oct. 2023
	 */
	default float toBox2D(final double value) {
		return (float) (value * getScale());
	}

}
