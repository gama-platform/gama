/*******************************************************************************************************
 *
 * EachExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * The Class EachExpression.
 */
public class EachExpression extends VariableExpression {

	/**
	 * Instantiates a new each expression.
	 *
	 * @param argName
	 *            the arg name
	 * @param type
	 *            the type
	 */
	public EachExpression(final String argName, final IType<?> type) {
		super(argName, type, true, null);
	}

	@Override
	public Object _value(final IScope scope) {
		// see Issue #return scope.getVarValue(getName());
		// Issue #2521. Extra step to coerce the type of 'each' to what's expected by the expression (problem with ints
		// and floats)
		return type.cast(scope, scope.getEach(getName()), null, false);
	}

	@Override
	public String getTitle() { return "pseudo-variable " + getName() + " of type " + getGamlType().getName(); }

	/**
	 * @see gama.api.gaml.expressions.IExpression#getDocumentation()
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation("Represents the current object, of type " + type.getTitle() + ", in the iteration");
	}

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) {}

	@Override
	public boolean isConst() { return false; }

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
