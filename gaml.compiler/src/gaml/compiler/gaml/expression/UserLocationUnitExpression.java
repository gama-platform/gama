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
package gaml.compiler.gaml.expression;

import gama.api.data.objects.IPoint;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.GamaPointFactory;

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
