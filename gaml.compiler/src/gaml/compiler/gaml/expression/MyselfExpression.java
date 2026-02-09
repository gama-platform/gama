/*******************************************************************************************************
 *
 * MyselfExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;

/**
 * The Class MyselfExpression.
 */
public class MyselfExpression extends TempVariableExpression {

	/**
	 * Instantiates a new myself expression.
	 *
	 * @param type
	 *            the type
	 * @param definitionDescription
	 *            the definition description
	 */
	public MyselfExpression(final IType<?> type, final IDescription definitionDescription) {
		super(IKeyword.MYSELF, type, definitionDescription);
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {}

	@Override
	public String getTitle() { return "pseudo variable " + getName() + " of type " + getGamlType().getName(); }

	@Override
	public IGamlDocumentation getDocumentation() {
		final IDescription desc = getDefinitionDescription();
		return new GamlConstantDocumentation("pseudo variable " + getName() + " of type " + getGamlType().getName()
				+ (desc == null ? "<br>Built in" : "<br>Defined in " + desc.getTitle()));
	}

}