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

import static gama.core.common.interfaces.IKeyword.LOCATION;

import gama.core.common.geometry.Rotation3D;
import gama.core.metamodel.shape.IPoint;
import gama.core.metamodel.shape.IPoint ;
import gama.core.runtime.IScope;
import gama.gaml.compilation.GAML;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.Types;

/**
 * The Class CameraDefinition. Holds and updates the position, target and lens of a camera from the GAML definition in
 * the "camera" statement.
 */
public class RotationDefinition extends AbstractDefinition {

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
	public Double getAngleDelta() { return initialAngleAttribute.get(); }

	/**
	 * Gets the current angle.
	 *
	 * @return the current angle
	 */
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
	public IPoint  getCenter() { return locationAttribute.get(); }

	/**
	 * Gets the axis.
	 *
	 * @return the axis
	 */
	public IPoint  getAxis() { return axisAttribute.get(); }

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
	public void setAngle(final double val) { angleAttribute = new ConstantAttribute<>(val); }

	/**
	 * Sets the dynamic.
	 *
	 * @param r
	 *            the new dynamic
	 */
	public void setDynamic(final boolean r) { dynamic = new ConstantAttribute<>(r); }

	@Override
	protected boolean getDefaultDynamicValue() { return false; }

}
