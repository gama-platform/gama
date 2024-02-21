/*******************************************************************************************************
 *
 * CurrentExperimentExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.variables;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.Types;

/**
 * The Class MyselfExpression.
 */
public class CurrentExperimentExpression extends VariableExpression {

	/** The instance. */
	static CurrentExperimentExpression INSTANCE;
	
	/**
	 * Instantiates a new myself expression.
	 *
	 * @param type
	 *            the type
	 * @param definitionDescription
	 *            the definition description
	 */
	public CurrentExperimentExpression() {
		super(IKeyword.EXPERIMENT, Types.get(IKeyword.EXPERIMENT), true, null);
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
	public Doc getDocumentation() { return new ConstantDoc("Represents and gives acces to the current experiment"); }

	@Override
	protected Object _value(final IScope scope) {
		return scope == null ? null : scope.getExperiment();
	}

	/**
	* Creates the.
	*
	* @author Alexis Drogoul (alexis.drogoul@ird.fr)
	* @return the i expression
	* @date 25 janv. 2024
	*/
	public static IExpression create() {
	    if (INSTANCE == null) { INSTANCE = new CurrentExperimentExpression(); }
	    return INSTANCE;
	}
}