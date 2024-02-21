/*******************************************************************************************************
 *
 * CommandEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.statements.UserCommandStatement;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.interfaces.EditorListener;
import gama.ui.shared.interfaces.EditorListener.Command;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.IGamaColors;

/**
 * The Class CommandEditor.
 */
public class CommandEditor extends AbstractStatementEditor<UserCommandStatement> {

	/**
	 * Instantiates a new command editor.
	 *
	 * @param scope
	 *            the scope
	 * @param command
	 *            the command
	 * @param l
	 *            the l
	 */
	public CommandEditor(final IScope scope, final UserCommandStatement command, final EditorListener.Command l) {
		super(scope, command, l);
	}

	@Override
	protected EditorListener.Command getListener() { return (Command) super.getListener(); }

	@Override
	protected FlatButton createCustomParameterControl(final Composite composite) throws GamaRuntimeException {
		textBox = FlatButton.button(composite, null, "");
		textBox.setText(" " + getStatement().getName());
		textBox.addSelectionListener(getListener());
		return textBox;
	}

	@Override
	Color getEditorControlBackground() {
		GamaColor color = getStatement().getColor(getScope());
		return color == null ? IGamaColors.NEUTRAL.color() : GamaColors.get(color).color();
	}

	@Override
	EditorLabel createEditorLabel() {
		return new EditorLabel(this, parent, " ", isSubParameter);
	}

}
