/*******************************************************************************************************
 *
 * AbstractStatementEditor.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.utils.interfaces.IColored;
import gama.api.utils.interfaces.INamed;
import gama.core.experiment.parameters.InputParameter;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.interfaces.EditorListener;
import gama.ui.shared.resources.GamaColors;

/**
 * The Class AbstractStatementEditor.
 *
 * @param <T>
 *            the generic type
 */
public abstract class AbstractStatementEditor<T extends INamed & IColored> extends AbstractEditor<Object> {

	/** The text box. */
	protected FlatButton textBox;

	/** The statement. */
	T statement;

	/**
	 * Instantiates a new abstract statement editor.
	 *
	 * @param scope
	 *            the scope
	 * @param command
	 *            the command
	 * @param l
	 *            the l
	 */
	public AbstractStatementEditor(final IScope scope, final T command, final EditorListener<Object> l) {
		super(scope.getAgent(), new InputParameter(command.getName(), null), l);
		this.statement = command;
	}

	/**
	 * Gets the statement.
	 *
	 * @return the statement
	 */
	public T getStatement() { return statement; }

	@Override
	protected int[] getToolItems() { return new int[0]; }

	@Override
	protected final Object retrieveValueOfParameter(final boolean b) throws GamaRuntimeException {
		return null;
	}

	@Override
	Color getEditorControlBackground() {
		IColor color = getStatement().getColor(getScope());
		return color == null ? super.getEditorControlBackground() : GamaColors.get(color).color();
	}

	@Override
	protected abstract FlatButton createCustomParameterControl(Composite comp) throws GamaRuntimeException;

	@Override
	protected void updateToolbar() {}

	@Override
	protected final void displayParameterValue() {}

	@Override
	protected GridData getEditorControlGridData() {
		final var d = new GridData(SWT.FILL, SWT.CENTER, false, false);
		d.minimumWidth = 50;
		return d;
	}

}
