/*******************************************************************************************************
 *
 * LabelExpressionDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.descriptions;

import java.util.HashSet;
import java.util.Set;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;

/**
 * The class LabelExpressionDescription.
 *
 * @author drogoul
 * @since 31 mars 2012
 *
 */
public class LabelExpressionDescription extends BasicExpressionDescription implements IExpression {

	@Override
	public IType<?> getDenotedType(final IDescription context) {
		return context.getTypeNamed(value);
	}

	/** The value. */
	final String value;

	/**
	 * Instantiates a new label expression description.
	 *
	 * @param label
	 *            the label
	 */
	public LabelExpressionDescription(final String label) {
		super((IExpression) null);
		value = StringUtils.unescapeJava(label);
	}

	@Override
	public IExpressionDescription cleanCopy() {
		return this;
	}

	@Override
	public IExpressionDescription compileAsLabel() {
		return this;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return value;
		// return StringUtils.toGamlString(value);
	}

	@Override
	public IExpression getExpression() {
		if (expression == null) { expression = this; }
		return expression;
	}

	@Override
	public IExpression compile(final IDescription context) {
		return getExpression();
	}

	@Override
	public boolean equalsString(final String o) {
		return value.equals(o);
	}

	// @Override
	// public void setTarget(final EObject newTarget) {
	// super.setTarget(newTarget);
	// }

	@Override
	public Set<String> getStrings(final IDescription context, final boolean skills) {
		// Assuming of the form [aaa, bbb]
		Set<String> result = new HashSet<>();
		final StringBuilder b = new StringBuilder();
		for (final char c : value.toCharArray()) {
			switch (c) {
				case '[':
				case ' ':
					break;
				case ']':
				case ',': {
					result.add(b.toString());
					b.setLength(0);
					break;
				}
				default:
					b.append(c);
			}
		}
		return result;
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation("Constant string: " + getName());
	}

	@Override
	public String getTitle() { return "constant string '" + getName() + "'"; }

	@Override
	public String getDefiningPlugin() { return null; }

	@Override
	public String getName() { return value; }

	@Override
	public void setName(final String newName) {}

	@Override
	public IType<String> getGamlType() { return Types.STRING; }

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		return value;
	}

	@Override
	public boolean isConst() { return true; }

	@Override
	public String literalValue() {
		return value;
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public boolean isLabel() { return true; }

}
