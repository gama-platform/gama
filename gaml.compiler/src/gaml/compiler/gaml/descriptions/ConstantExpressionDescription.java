/*******************************************************************************************************
 *
 * ConstantExpressionDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.dev.DEBUG;
import gaml.compiler.gaml.expression.ConstantExpression;

/**
 * The Class ConstantExpressionDescription.
 */
public class ConstantExpressionDescription extends ConstantExpression implements IExpressionDescription {

	static {
		DEBUG.OFF();
	}

	/**
	 * Instantiates a new constant expression description.
	 *
	 * @param object
	 *            the object
	 */
	public ConstantExpressionDescription(final Object object) {
		this(object, GamaType.of(object));
	}

	/**
	 * Instantiates a new constant expression description.
	 *
	 * @param object
	 *            the object
	 * @param t
	 *            the t
	 */
	public ConstantExpressionDescription(final Object object, final IType<?> t) {
		super(object, t);
	}

	@Override
	public boolean isConst() { return true; }

	@Override
	public void dispose() {}

	@Override
	public IExpression compile(final IDescription context) {
		return this;
	}

	@Override
	public void setExpression(final IExpression expr) {}

	@Override
	public IExpressionDescription cleanCopy() {
		return this;
	}

	@Override
	public IType<?> getDenotedType(final IDescription context) {
		return context.getTypeNamed(literalValue());
	}

	@Override
	public IExpression getExpression() { return this; }

	@Override
	public IExpressionDescription compileAsLabel() {
		return new LabelExpressionDescription(literalValue());
	}

	@Override
	public boolean equalsString(final String o) {
		return literalValue().equals(o);
	}

	@Override
	public EObject getTarget() { return null; }

	@Override
	public void setTarget(final EObject target) {}

	@Override
	public Set<String> getStrings(final IDescription context, final boolean skills) {
		return Collections.EMPTY_SET;
	}

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	public IType<?> getGamlType() { return type; }

}
