/*******************************************************************************************************
 *
 * ILightDefinition.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.properties;

import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.constant;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.IOperatorCategory;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.gaml.interfaces.INamed;

/**
 * The Interface ILightDefinition.
 */
public interface ILightDefinition extends INamed {

	/** The point. */
	@constant (
			value = "point",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the 'point' type of light")) String point = "Point light";
	/** The spot. */
	@constant (
			value = "spot",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the 'spot' type of light")) String spot = "Spot light";
	/** The ambient. */
	@constant (
			value = "ambient",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the 'ambient' type of light")) String ambient = "Ambient light";

	/** The direction. */
	@constant (
			value = "direction",
			category = IOperatorCategory.THREED,
			doc = @doc ("Represent the 'direction' type of light")) String direction = "Directional light";
	/** The light presets. */
	String[] LIGHT_PRESETS = List.of(point, direction, spot).toArray(new String[4]);

	/** The Constant DEFAULT_DIRECTION. */
	GamaPoint DEFAULT_DIRECTION = new GamaPoint(0.5, 0.5, -1);

	/** The Constant DEFAULT_LOCATION. */
	GamaPoint DEFAULT_LOCATION = new GamaPoint(0, 0, 1);

	/** The default angle. */
	Double DEFAULT_ANGLE = 45d;

	/** The default intensity. */
	// GamaColor DEFAULT_INTENSITY = new GamaColor(160, 160, 160, 255);

	/**
	 * Checks if is dynamic.
	 *
	 * @return the boolean
	 */
	default Boolean isDynamic() { return true; }

	/**
	 * Checks if is active.
	 *
	 * @return the boolean
	 */
	default Boolean isActive() { return !getIntensity().isZero(); }

	/**
	 * Checks if is drawing.
	 *
	 * @return the boolean
	 */
	default Boolean isDrawing() { return false; }

	/**
	 * Gets the direction.
	 *
	 * @return the direction
	 */
	default GamaPoint getDirection() { return DEFAULT_DIRECTION; }

	/**
	 * Gets the intensity.
	 *
	 * @return the intensity
	 */
	GamaColor getIntensity();

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	default String getType() { return ILightDefinition.direction; }

	/**
	 * Gets the angle.
	 *
	 * @return the angle
	 */
	default double getAngle() { return DEFAULT_ANGLE; }

	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	default GamaPoint getLocation() { return DEFAULT_LOCATION; }

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	String getName();

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	int getId();

	/**
	 * Gets the linear attenuation.
	 *
	 * @return the linear attenuation
	 */
	default double getLinearAttenuation() { return 0; }

	/**
	 * Gets the quadratic attenuation.
	 *
	 * @return the quadratic attenuation
	 */
	default double getQuadraticAttenuation() { return 0; }

	/**
	 * Gets the quadratic attenuation.
	 *
	 * @return the quadratic attenuation
	 */
	default double getConstantAttenuation() { return 1; }

	/**
	 * Refresh.
	 *
	 * @param scope
	 *            the scope
	 */
	default void refresh(final IScope scope) {}

}