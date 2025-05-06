/*******************************************************************************************************
 *
 * ExpressionBasedEditor.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.ui.shared.interfaces.EditorListener;

/**
 * Class ExpressionBasedEditor.
 *
 * @author drogoul
 * @since 30 nov. 2014
 *
 */
public class ExpressionBasedEditor<T> extends AbstractEditor<T> {

	/** The expression. */
	protected ExpressionControl expression;

	/**
	 * Instantiates a new expression based editor.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param variable
	 *            the variable
	 * @param l
	 *            the l
	 */
	public ExpressionBasedEditor(final IAgent a, final IParameter variable, final EditorListener<T> l) {
		super(a, variable, l);
	}

	// In case the editor allows to edit the expression, should it be evaluated
	/**
	 * Evaluate expression.
	 *
	 * @return true, if successful
	 */
	// ?
	protected boolean evaluateExpression() {
		return true;
	}

	@Override
	public Control createCustomParameterControl(final Composite compo) {
		expression = new ExpressionControl(getScope(), compo, this, getAgent(), this.getExpectedType(),
				SWT.NONE | SWT.FLAT, evaluateExpression());
		return expression.getControl();
	}

	@Override
	protected void displayParameterValue() {
		internalModification = true;
		expression.displayValue(currentValue);
		internalModification = false;
	}

	@Override
	protected int[] getToolItems() { return new int[] { REVERT }; }

	/**
	 * Does the editor format the value to be displayed ? Default is false
	 */
	public boolean formatsValue() {
		return false;
	}

	/**
	 * @param currentValue
	 * @return
	 */
	public String valueFormatted(final Object currentValue) {
		return String.valueOf(currentValue);
	}

}
