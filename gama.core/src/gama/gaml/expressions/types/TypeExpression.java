/*******************************************************************************************************
 *
 * TypeExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.types;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.AbstractExpression;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Class TypeExpression.
 *
 * @author drogoul
 * @since 7 sept. 2013
 *
 */
public class TypeExpression extends AbstractExpression {

	/**
	 * Instantiates a new type expression.
	 *
	 * @param type
	 *            the type
	 */
	@SuppressWarnings ("rawtypes")
	public TypeExpression(final IType type) {
		this.type = type;
	}

	@Override
	public IType<?> _value(final IScope scope) throws GamaRuntimeException {
		// Normally never evaluated
		return getDenotedType();
	}

	@Override
	public String getDefiningPlugin() { return type.getDefiningPlugin(); }

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public boolean isConst() { return type.canCastToConst(); }

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return type.serializeToGaml(includingBuiltIn);
	}

	@Override
	public String getTitle() { return type.getTitle(); }

	/**
	 * Method getDocumentation()
	 *
	 * @see gama.gaml.interfaces.IGamlDescription#getDocumentation()
	 */
	@Override
	public Doc getDocumentation() { return type.getDocumentation(); }

	@Override
	public IType<?> getGamlType() { return Types.TYPE; }

	@Override
	public IType<?> getDenotedType() { return type; }

	@Override
	public String literalValue() {
		return type.serializeToGaml(false);
	}

	@Override
	public boolean isContextIndependant() { return isConst(); }

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

}
