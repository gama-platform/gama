/*******************************************************************************************************
 *
 * DenotedActionExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.types;

import gama.core.runtime.IScope;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.variables.VariableExpression;
import gama.gaml.types.Types;

/**
 * The Class DenotedActionExpression.
 */
public class DenotedActionExpression extends VariableExpression {

	/**
	 * Instantiates a new denoted action expression.
	 *
	 * @param action
	 *            the action
	 */
	public DenotedActionExpression(final StatementDescription action) {
		super(action.getName(), Types.ACTION, true, action);
	}

	@Override
	public Object _value(final IScope scope) {
		return getDefinitionDescription();
	}

	@Override
	public String getTitle() { return getDefinitionDescription().getTitle(); }

	/**
	 * @see gama.gaml.expressions.IExpression#getDocumentation()
	 */
	@Override
	public Doc getDocumentation() { return getDefinitionDescription().getDocumentation(); }

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) {}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
