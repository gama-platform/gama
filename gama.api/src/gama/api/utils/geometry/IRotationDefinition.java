/*******************************************************************************************************
 *
 * IRotationDefinition.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.geometry;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;

/**
 *
 */
public interface IRotationDefinition {

	/**
	 * Update.
	 *
	 * @param scope
	 *            the scope
	 */
	void update(IScope scope);

	/**
	 * Gets the angle.
	 *
	 * @return the angle
	 */
	Double getAngleDelta();

	/**
	 * Gets the current angle.
	 *
	 * @return the current angle
	 */
	Double getCurrentAngle();

	/**
	 * Checks if is dynamic.
	 *
	 * @return the boolean
	 */
	Boolean isDynamic();

	/**
	 * Gets the center.
	 *
	 * @return the center
	 */
	IPoint getCenter();

	/**
	 * Gets the axis.
	 *
	 * @return the axis
	 */
	IPoint getAxis();

	/**
	 * Reset.
	 */
	void reset();

	/**
	 * Sets the angle.
	 *
	 * @param val
	 *            the new angle
	 */
	void setAngle(double val);

	/**
	 * Sets the dynamic.
	 *
	 * @param r
	 *            the new dynamic
	 */
	void setDynamic(boolean r);

	/**
	 * @param scope
	 */
	void refresh(IScope scope);

}