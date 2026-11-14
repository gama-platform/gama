/*******************************************************************************************************
 *
 * PersistentVariableExpression.java, in gaml.compiler, is part of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.runtime.scope.PersistentLocalVariables;

/** An output-local binding keeps its declaring lexical scope when the execution target changes. */
public final class PersistentVariableExpression extends TempVariableExpression implements IVarExpression.Persistent {

	public PersistentVariableExpression(final String name, final IType<?> type, final IDescription owner) {
		super(name, type, owner);
	}

	@Override
	public Object _value(final IScope scope) {
		return PersistentLocalVariables.get(scope, getDefinitionDescription(), getName());
	}

	@Override
	public void setVal(final IScope scope, final Object value, final boolean create) {
		PersistentLocalVariables.set(scope, getDefinitionDescription(), getName(),
				getGamlType().cast(scope, value, null, false), create);
	}

	@Override
	public String getTitle() {
		return getDefinitionDescription().getKeyword() + " local variable " + getName()
				+ " of type " + getGamlType().getName();
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation(getTitle() + "<br>Initialized once per owning output instance.");
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) { return this; }
}
