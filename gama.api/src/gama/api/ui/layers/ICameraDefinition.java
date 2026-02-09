/*******************************************************************************************************
 *
 * ICameraDefinition.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.layers;

import java.util.List;

import gama.api.data.objects.IPoint;
import gama.api.runtime.scope.IScope;
import gama.api.utils.INamed;
import gama.annotations.constant;
import gama.annotations.doc;
import gama.annotations.support.IOperatorCategory;

/**
 * The Interface ICameraDefinition. Defines the minimal set of information needed for cameras. All other attributes
 * (like follow etc.) should contribute to building these information.
 */
public interface ICameraDefinition extends INamed {

	/** The from top. */
	@constant (
			value = "from_above",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera, above the scene")) String from_above = "From above";

	/** The from left. */
	@constant (
			value = "from_left",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera, on the left of the scene")) String from_left =
					"From left";

	/** The from right. */
	@constant (
			value = "from_right",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera, on the right of the scene")) String from_right =
					"From right";

	/** The from up left. */
	@constant (
			value = "from_up_left",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera, on the left, slightly above the scene")) String from_up_left =
					"From up left";

	/** The from up right. */
	@constant (
			value = "from_up_right",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera on the right, slightly above the scene")) String from_up_right =
					"From up right";

	/** The from front. */
	@constant (
			value = "from_front",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera, in front of the scene")) String from_front =
					"From front";

	/** The from up front. */
	@constant (
			value = "from_up_front",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera, in front and slightly above the scene")) String from_up_front =
					"From up front";

	/** The from left. */
	@constant (
			value = "isometric",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the position of the camera, on the left of the scene")) String isometric =
					"Isometric";

	/** The presets. */
	String[] PRESETS = List
			.of(from_above, from_left, from_right, from_front, from_up_left, from_up_right, from_up_front, isometric)
			.toArray(new String[7]);

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	IPoint getLocation();

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	IPoint getTarget();

	/**
	 * Gets the lens.
	 *
	 * @return the lens
	 */
	Double getLens();

	/**
	 * Checks if is interacting.
	 *
	 * @return the boolean
	 */
	Boolean isLocked();

	/**
	 * Sets the interactive.
	 *
	 * @param b
	 *            the new interactive
	 */
	void setLocked(Boolean b);

	/**
	 * Sets the location.
	 *
	 * @param point
	 *            the point
	 * @return true, if successful
	 */
	boolean setLocation(IPoint point);

	/**
	 * Sets the target.
	 *
	 * @param point
	 *            the point
	 * @return true, if successful
	 */
	boolean setTarget(IPoint point);

	/**
	 * Sets the lens.
	 *
	 * @param cameraLens
	 *            the new lens
	 */
	void setLens(Double cameraLens);

	/**
	 * Sets the distance.
	 *
	 * @param distance
	 *            the distance
	 * @return true, if successful
	 */
	boolean setDistance(Double distance);

	/**
	 * Reset.
	 */
	void reset();

	/**
	 * Refresh. Does nothing by default
	 *
	 * @param scope
	 *            the scope
	 */
	default void refresh(final IScope scope) {}

	/**
	 * Gets the distance.
	 *
	 * @return the distance
	 */
	Double getDistance();

	/**
	 * Computes the location of a camera based on a symbolic position, a target and boundarises .
	 *
	 * @param pos
	 *            the symbolic position
	 * @param target
	 *            the target - y-negated already
	 * @param maxX
	 *            the dimension on the x axis > 0
	 * @param maxY
	 *            the dimension on the y axis > 0
	 * @param maxZ
	 *            the dimension on the z axis > 0
	 * @return the gama point
	 */
	IPoint computeLocation(final String pos, final IPoint target, final double maxX, final double maxY,
			final double maxZ);

	/**
	 * Checks if is dynamic.
	 *
	 * @return the boolean
	 */
	Boolean isDynamic();

}
