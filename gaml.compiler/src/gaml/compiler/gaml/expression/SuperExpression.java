/*******************************************************************************************************
 *
 * SuperExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * The Class SuperExpression.
 */
public class SuperExpression extends VariableExpression {

	/**
	 * Instantiates a new super expression.
	 *
	 * @param type
	 *            the type
	 */
	public SuperExpression(final IType<?> type) {
		super(IKeyword.SUPER, type, true, null);
	}

	@Override
	public Object _value(final IScope scope) {
		return scope.getAgent();
	}

	@Override
	public String getTitle() { return "pseudo-variable super of type " + getGamlType().getTitle(); }

	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation("Represents the current agent, instance of species " + type.getTitle()
				+ ", indicating a redirection to the parent species in case of calling an action");
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
