/*******************************************************************************************************
 *
 * RotationDefinition.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.properties;

import static gama.api.constants.IKeyword.LOCATION;

import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.utils.geometry.IRotationDefinition;
import gama.api.utils.geometry.Rotation3D;

/**
 * The Class RotationDefinition. Holds and updates the location, axis and angle of a rotation from the GAML definition
 * in the "rotation" statement.
 */
public class RotationDefinition extends AbstractDefinition implements IRotationDefinition {

	static {
		// DEBUG.OFF();
	}

	/** The location. */
	final Attribute<IPoint> locationAttribute;

	/** The target. */
	final Attribute<IPoint> axisAttribute;

	/** The angle. Can be changed from outside to another value */
	Attribute<Double> angleAttribute;

	/** The initial angle. */
	final Attribute<Double> initialAngleAttribute;

	/**
	 * Instantiates a new camera definition.
	 *
	 * @param symbol
	 *            the symbol
	 */
	@SuppressWarnings ("unchecked")
	public RotationDefinition(final RotationStatement symbol) {
		super(symbol);
		IExpression expr =
				GAML.getExpressionFactory().createExpr(scope -> scope.getSimulation().getCentroid(), Types.POINT);
		locationAttribute =
				create(LOCATION, symbol.hasFacet(LOCATION) ? symbol.getFacet(LOCATION) : expr, Types.POINT, null);
		axisAttribute = create("axis", Types.POINT, Rotation3D.PLUS_K);
		angleAttribute = initialAngleAttribute = create("angle", Types.FLOAT, 0d);
	}

	@Override
	public void update(final IScope scope) {
		angleAttribute = new ConstantAttribute<>(angleAttribute.get() + initialAngleAttribute.get());
	}

	/**
	 * Gets the angle.
	 *
	 * @return the angle
	 */
	@Override
	public Double getAngleDelta() { return initialAngleAttribute.get(); }

	/**
	 * Gets the current angle.
	 *
	 * @return the current angle
	 */
	@Override
	public Double getCurrentAngle() { return angleAttribute.get(); }

	/**
	 * Checks if is dynamic.
	 *
	 * @return the boolean
	 */
	@Override
	public Boolean isDynamic() { return dynamic.get(); }

	/**
	 * Gets the center.
	 *
	 * @return the center
	 */
	@Override
	public IPoint getCenter() { return locationAttribute.get(); }

	/**
	 * Gets the axis.
	 *
	 * @return the axis
	 */
	@Override
	public IPoint getAxis() { return axisAttribute.get(); }

	/**
	 * Reset.
	 */
	@Override
	public void reset() {
		angleAttribute = initialAngleAttribute;
	}

	/**
	 * Sets the angle.
	 *
	 * @param val
	 *            the new angle
	 */
	@Override
	public void setAngle(final double val) { angleAttribute = new ConstantAttribute<>(val); }

	/**
	 * Sets the dynamic.
	 *
	 * @param r
	 *            the new dynamic
	 */
	@Override
	public void setDynamic(final boolean r) { dynamic = new ConstantAttribute<>(r); }

	@Override
	protected boolean getDefaultDynamicValue() { return false; }

}
