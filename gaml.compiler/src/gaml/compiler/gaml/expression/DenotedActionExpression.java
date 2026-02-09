/*******************************************************************************************************
 *
 * DenotedActionExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

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
	public DenotedActionExpression(final IDescription action) {
		super(action.getName(), Types.ACTION, true, action);
	}

	@Override
	public Object _value(final IScope scope) {
		return getDefinitionDescription();
	}

	@Override
	public String getTitle() { return getDefinitionDescription().getTitle(); }

	/**
	 * @see gama.api.gaml.expressions.IExpression#getDocumentation()
	 */
	@Override
	public IGamlDocumentation getDocumentation() { return getDefinitionDescription().getDocumentation(); }

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) {}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
