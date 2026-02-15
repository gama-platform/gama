/*******************************************************************************************************
 *
 * LightDefinition.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.properties;

import gama.api.constants.IKeyword;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IPoint;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.ui.layers.ILightDefinition;
import gama.api.utils.color.GamaColorFactory;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

/**
 * The Class CameraDefinition. Holds and updates the position, target and lens of a camera from the GAML definition in
 * the "camera" statement.
 */
public class LightDefinition extends AbstractDefinition implements ILightDefinition {

	static {
		DEBUG.OFF();
	}

	/** The location. */
	final Attribute<IPoint> locationAttribute;

	/** The type attribute. */
	final Attribute<String> typeAttribute;

	/** The intensity attribute. */
	final Attribute<IColor> intensityAttribute;

	/** The target. */
	final Attribute<IPoint> directionAttribute;

	/** The angle attribute. */
	Attribute<Double> angleAttribute;

	/** The initial angle. */
	final Attribute<Double> attenuationAttribute;

	/** The initial angle. */
	final Attribute<Double> linearAttribute;

	/** The quadratic attribute. */
	final Attribute<Double> quadraticAttribute;

	/** The active attribute. */
	final Attribute<Boolean> activeAttribute;

	/** The draw attribute. */
	final Attribute<Boolean> drawAttribute;

	/** The id. */
	int id;

	/**
	 * Instantiates a new camera definition.
	 *
	 * @param lightStatement
	 *            the symbol
	 */
	@SuppressWarnings ("unchecked")
	public LightDefinition(final LightStatement lightStatement) {
		super(lightStatement);
		typeAttribute = create(IKeyword.TYPE, Types.STRING, ILightDefinition.direction);
		locationAttribute = create(IKeyword.LOCATION, Types.POINT, DEFAULT_LOCATION);
		directionAttribute = create("direction", Types.POINT, DEFAULT_DIRECTION);
		linearAttribute = create(IKeyword.LINEAR_ATTENUATION, Types.FLOAT, 0d);
		attenuationAttribute = create(IKeyword.CONSTANT_ATTENUATION, Types.FLOAT, 1d);
		quadraticAttribute = create(IKeyword.QUADRATIC_ATTENUATION, Types.FLOAT, 0d);
		angleAttribute = create("angle", Types.FLOAT, DEFAULT_ANGLE);
		drawAttribute = create("show", Types.BOOL, false);
		activeAttribute = create("active", Types.BOOL, true);
		Integer i = GamaPreferences.Displays.OPENGL_DEFAULT_LIGHT_INTENSITY.getValue();
		intensityAttribute = create(IKeyword.INTENSITY, (scope, exp) -> {
			if (exp.getGamlType() == Types.INT) {
				int v = Cast.asInt(scope, exp.value(scope));
				return GamaColorFactory.createWithRGBA(v, v, v, 255);
			}
			return GamaColorFactory.createFrom(scope, exp.value(scope));
		}, Types.COLOR, GamaColorFactory.createWithRGBA(i, i, i, 255));

	}

	@Override
	public Boolean isActive() { return activeAttribute.get() && ILightDefinition.super.isActive(); }

	@Override
	public Boolean isDrawing() { return drawAttribute.get(); }

	@Override
	public IPoint getDirection() { return directionAttribute.get(); }

	@Override
	public String getType() { return typeAttribute.get(); }

	@Override
	public IColor getIntensity() { return intensityAttribute.get(); }

	@Override
	public double getAngle() { return angleAttribute.get(); }

	@Override
	public String getName() { return symbol.getName(); }

	@Override
	public int getId() { return IKeyword.DEFAULT.equals(getName()) ? 0 : id; }

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	public void setId(final int id) { this.id = id; }

	@Override
	public IPoint getLocation() { return locationAttribute.get(); }

	@Override
	protected boolean getDefaultDynamicValue() { return true; }

	@Override
	protected boolean shouldRefresh() {
		return isDynamic();
	}

	@Override
	protected void update(final IScope scope) {}

	@Override
	protected void reset() {
		// Nothing to do for the moment
	}

	/**
	 * Gets the linear attenuation.
	 *
	 * @return the linear attenuation
	 */
	@Override
	public double getLinearAttenuation() { return linearAttribute.get(); }

	/**
	 * Gets the quadratic attenuation.
	 *
	 * @return the quadratic attenuation
	 */
	@Override
	public double getQuadraticAttenuation() { return quadraticAttribute.get(); }

	/**
	 * Gets the constant attenuation.
	 *
	 * @return the constant attenuation
	 */
	@Override
	public double getConstantAttenuation() { return attenuationAttribute.get(); }

}
