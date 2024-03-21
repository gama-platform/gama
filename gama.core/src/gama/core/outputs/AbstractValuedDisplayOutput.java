/*******************************************************************************************************
 *
 * AbstractValuedDisplayOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.GAML;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;

/**
 * The Class AbstractValuedDisplayOutput.
 */
public abstract class AbstractValuedDisplayOutput extends AbstractOutput {

	/** The expression text. */
	protected String expressionText = "";

	/** The value. */
	protected IExpression value;

	/** The last value. */
	protected Object lastValue = "";

	/**
	 * Instantiates a new abstract valued display output.
	 *
	 * @param desc
	 *            the desc
	 */
	public AbstractValuedDisplayOutput(final IDescription desc) {
		super(desc);
		setValue(getFacet(IKeyword.VALUE));
		expressionText = getValue() == null ? "" : getValue().serializeToGaml(false);
	}

	/**
	 * Gets the last value.
	 *
	 * @return the last value
	 */
	public Object getLastValue() { return lastValue; }

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public IExpression getValue() { return value; }

	/**
	 * Gets the expression text.
	 *
	 * @return the expression text
	 */
	public String getExpressionText() { return expressionText == null ? "" : expressionText; }

	/**
	 * Sets the new expression text.
	 *
	 * @param string
	 *            the string
	 * @return true, if successful
	 */
	public boolean setNewExpressionText(final String string) {
		expressionText = string;
		IExpression expr;
		try {
			expr = GAML.compileExpression(string, getScope().getSimulation(), true);
		} catch (GamaRuntimeException e1) {
			// The expression is maybe dedicated to experiments (and not simulations) ?
			expr = GAML.compileExpression(string, getScope().getExperiment(), true);
		}
		setValue(expr);
		return getScope().step(this).passed();
	}

	/**
	 * Sets the new expression.
	 *
	 * @param expr
	 *            the new new expression
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public void setNewExpression(final IExpression expr) throws GamaRuntimeException {
		expressionText = expr == null ? "" : expr.serializeToGaml(false);
		setValue(expr);
		getScope().step(this);
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	protected void setValue(final IExpression value) { this.value = value; }

}
