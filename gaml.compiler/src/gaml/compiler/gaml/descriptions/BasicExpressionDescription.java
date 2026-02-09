/*******************************************************************************************************
 *
 * BasicExpressionDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.utils.StringUtils;
import gama.dev.DEBUG;
import gaml.compiler.gaml.expression.TypeExpression;

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
	 * @see gama.api.gaml.expressions.IExpressionDescription#compileAsLabel()
	 */
	@Override
	public IExpressionDescription compileAsLabel() {
		final IExpressionDescription newEd =
				GAML.getExpressionDescriptionFactory().createLabel(StringUtils.toJavaString(toString()));
		newEd.setTarget(getTarget());
		return newEd;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * @see gama.api.gaml.expressions.IExpressionDescription#equalsString(java.lang.String)
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
		if (expression instanceof TypeExpression te) return te.getDenotedType();
		IType type = expression.getGamlType();
		IModelDescription md = context.getModelDescription();
		if (md != null) {
			final ITypesManager tm = md.getTypesManager();
			final String s = expression.literalValue();
			return tm.get(s, type);
		}
		return type;
	}

}
