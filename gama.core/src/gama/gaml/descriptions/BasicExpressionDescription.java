/*******************************************************************************************************
 *
 * BasicExpressionDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.util.StringUtils;
import gama.dev.DEBUG;
import gama.gaml.compilation.GAML;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.types.TypeExpression;
import gama.gaml.types.IType;
import gama.gaml.types.ITypesManager;
import gama.gaml.types.Types;

/**
 * The Class BasicExpressionDescription.
 */
public class BasicExpressionDescription implements IExpressionDescription {

	static {
		DEBUG.OFF();
	}

	/** The expression. */
	protected IExpression expression;

	/** The target. */
	protected EObject target;

	/**
	 * Instantiates a new basic expression description.
	 *
	 * @param expr
	 *            the expr
	 */
	public BasicExpressionDescription(final IExpression expr) {
		expression = expr;
	}

	/**
	 * Instantiates a new basic expression description.
	 *
	 * @param object
	 *            the object
	 */
	public BasicExpressionDescription(final EObject object) {
		target = object;
	}

	@Override
	public String toString() {
		return serializeToGaml(false);
	}

	/**
	 * To own string.
	 *
	 * @return the string
	 */
	public String toOwnString() {
		return target.toString();
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return expression == null ? toOwnString() : expression.serializeToGaml(includingBuiltIn);
	}

	@Override
	public boolean equals(final Object c) {
		if (c == null) return false;
		if (c == this) return true;
		if (c instanceof IExpressionDescription) return ((IExpressionDescription) c).equalsString(toString());
		return false;
	}

	@Override
	public IExpression getExpression() { return expression; }

	@Override
	public void dispose() {
		expression = null;
		target = null;
	}

	@Override
	public void setExpression(final IExpression expr) { expression = expr; }

	@Override
	public IExpression compile(final IDescription context) {
		if (expression == null) { expression = GAML.getExpressionFactory().createExpr(this, context); }
		return expression;
	}

	/**
	 * @see gama.gaml.descriptions.IExpressionDescription#compileAsLabel()
	 */
	@Override
	public IExpressionDescription compileAsLabel() {
		final IExpressionDescription newEd = LabelExpressionDescription.create(StringUtils.toJavaString(toString()));
		newEd.setTarget(getTarget());
		return newEd;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @see gama.gaml.descriptions.IExpressionDescription#equalsString(java.lang.String)
	 */
	@Override
	public boolean equalsString(final String o) {
		return o == null ? false : o.equals(toString());
	}

	/**
	 * @see org.eclipse.emf.common.notify.Adapter#getTarget()
	 */
	@Override
	public EObject getTarget() { return target; }

	/**
	 * @see org.eclipse.emf.common.notify.Adapter#setTarget(org.eclipse.emf.common.notify.Notifier)
	 */
	@Override
	public void setTarget(final EObject newTarget) {
		if (target == null) { target = newTarget; }
	}

	@Override
	public boolean isConst() { return false; }

	@Override
	public Collection<String> getStrings(final IDescription context, final boolean skills) {
		return Collections.EMPTY_SET;
	}

	@Override
	public IExpressionDescription cleanCopy() {
		final BasicExpressionDescription result = new BasicExpressionDescription(target);
		result.expression = expression;
		return result;
	}

	@Override
	public IType<?> getDenotedType(final IDescription context) {
		compile(context);
		if (expression == null) return Types.NO_TYPE;
		if (expression instanceof TypeExpression) return ((TypeExpression) expression).getDenotedType();
		IType type = expression.getGamlType();
		ModelDescription md = context.getModelDescription();
		if (md != null) {
			final ITypesManager tm = md.getTypesManager();
			final String s = expression.literalValue();
			return tm.get(s, type);
		}
		return type;
	}

}
