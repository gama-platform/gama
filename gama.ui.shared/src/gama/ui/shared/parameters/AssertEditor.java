/*******************************************************************************************************
 *
 * AssertEditor.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.statements.test.AbstractSummary;
import gama.gaml.statements.test.AssertionSummary;
import gama.gaml.statements.test.TestState;
import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.interfaces.EditorListener;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaColors.GamaUIColor;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.views.toolbar.Selector;

/**
 * The Class AssertEditor.
 */
public class AssertEditor extends AbstractStatementEditor<AbstractSummary<?>> {

	/**
	 * Instantiates a new assert editor.
	 *
	 * @param scope
	 *            the scope
	 * @param command
	 *            the command
	 */
	public AssertEditor(final IScope scope, final AbstractSummary<?> command) {
		super(scope, command, (EditorListener<Object>) null);
		isSubParameter = command instanceof AssertionSummary;
		name = command.getTitle();
	}

	@Override
	protected int[] getToolItems() { return new int[] { VALUE }; }

	@Override
	EditorToolbar createEditorToolbar() {
		editorToolbar = super.createEditorToolbar();
		if (isSubParameter) {
			editorToolbar.setHorizontalAlignment(SWT.LEAD);
			Label l = editorToolbar.getItem(VALUE);
			if (l != null && !l.isDisposed()) {
				l.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
				l.setText(name);
				l.setAlignment(SWT.LEAD);
			}
		}
		return editorToolbar;
	}

	@Override
	EditorLabel createEditorLabel() {
		editorLabel = new EditorLabel(this, parent, isSubParameter ? " " : name, isSubParameter);
		editorLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		return editorLabel;
	}

	@Override
	protected FlatButton createCustomParameterControl(final Composite composite) throws GamaRuntimeException {
		final AbstractSummary<?> summary = getStatement();
		String text = summary instanceof AssertionSummary && getStatement().getState() == TestState.ABORTED
				? getStatement().getState().toString() + ": " + ((AssertionSummary) getStatement()).getError()
				: getStatement().getState().toString();
		textBox = FlatButton.button(composite, null, text);
		textBox.addSelectionListener((Selector) e -> GAMA.getGui().editModel(getStatement().getURI()));
		return textBox;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	@Override
	Color getEditorControlBackground() {
		GamaUIColor color = GamaColors.get(getStatement().getColor(getScope()));
		if (color == null) { color = IGamaColors.NEUTRAL; }
		return color.color();
	}

}
