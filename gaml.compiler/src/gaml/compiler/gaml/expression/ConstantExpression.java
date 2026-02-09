/*******************************************************************************************************
 *
 * ConstantExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;

/**
 * ConstantValueExpr.
 *
 * @author drogoul 22 août 07
 */

public class ConstantExpression extends AbstractExpression {

	/** The value. */
	protected Object value;

	/**
	 * Instantiates a new constant expression.
	 *
	 * @param val
	 *            the val
	 * @param t
	 *            the t
	 * @param name
	 *            the name
	 */
	public ConstantExpression(final Object val, final IType<?> t, final String name) {
		value = val;
		type = t;
		setName(name);
	}

	/**
	 * Instantiates a new constant expression.
	 *
	 * @param val
	 *            the val
	 * @param t
	 *            the t
	 */
	public ConstantExpression(final Object val, final IType<?> t) {
		this(val, t, val == null ? "nil" : val.toString());
	}

	/**
	 * Instantiates a new constant expression.
	 *
	 * @param val
	 *            the val
	 */
	public ConstantExpression(final Object val) {
		this(val, GamaType.of(val));
	}

	@Override
	public Object _value(final IScope scope) {
		return value;
	}

	@Override
	public boolean isConst() { return true; }

	@Override
	public String toString() {
		return value == null ? "nil" : value.toString();
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return StringUtils.toGaml(value, includingBuiltIn);
	}

	/**
	 * @see gama.api.gaml.expressions.IExpression#getDocumentation()
	 */
	@Override
	public IGamlDocumentation getDocumentation() { return new GamlConstantDocumentation("Literal expression of type " + getGamlType().getName()); }

	@Override
	public String getTitle() { return literalValue(); }

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
