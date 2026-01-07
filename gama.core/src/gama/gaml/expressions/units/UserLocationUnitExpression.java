/*******************************************************************************************************
 *
 * UserLocationUnitExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.units;

import gama.core.metamodel.shape.GamaPointFactory;
import gama.core.metamodel.shape.IPoint;
import gama.core.runtime.IScope;
import gama.gaml.types.Types;

/**
 * The Class UserLocationUnitExpression.
 */
public class UserLocationUnitExpression extends UnitConstantExpression {

	/**
	 * Instantiates a new user location unit expression.
	 *
	 * @param doc
	 *            the doc
	 */
	public UserLocationUnitExpression(final String name, final String doc) {
		super(GamaPointFactory.create(), Types.POINT, name, doc, null);
	}

	@Override
	public IPoint _value(final IScope scope) {
		return scope.getGui().getMouseLocationInModel();
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public boolean isContextIndependant() { return false; }

	@Override
	public boolean isAllowedInParameters() { return false; }
}
