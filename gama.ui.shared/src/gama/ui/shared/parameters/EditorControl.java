/*******************************************************************************************************
 *
 * EditorControl.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import java.util.List;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gama.ui.shared.controls.FlatButton;
import gama.ui.shared.resources.GamaColors;

/**
 * The Class EditorControl.
 *
 * @param <T>
 *            the generic type
 */
public class EditorControl<T extends Control> {

	/** The control. */
	final T control;

	/** The editor. */
	final AbstractEditor<?> editor;

	/**
	 * Instantiates a new editor control.
	 *
	 * @param editor
	 *            the editor
	 * @param control
	 *            the control
	 */
	EditorControl(final AbstractEditor<?> editor, final T control) {
		this.editor = editor;
		this.control = control;
		control.setLayoutData(editor.getEditorControlGridData());
		Color back = editor.getEditorControlBackground();
		Color front = editor.getEditorControlForeground();
		if (control instanceof FlatButton button) {
			control.setBackground(GamaColors.get(back).color());
			button.setColor(GamaColors.get(back));
		} else {
			GamaColors.setBackAndForeground(back, front, control);
		}
	}

	/**
	 * Gets the control.
	 *
	 * @return the control
	 */
	T getControl() { return control; }

	/**
	 * Sets the layout data.
	 *
	 * @param data
	 *            the new layout data
	 */
	public void setLayoutData(final GridData data) {
		if (control.isDisposed()) return;
		control.setLayoutData(data);
	}

	/**
	 * Sets the text.
	 *
	 * @param s
	 *            the new text
	 */
	public void setText(final String s) {
		if (control == null || control.isDisposed()) return;
		switch (control) {
			case Text t -> t.setText(s);
			case Button b -> b.setText(s);
			case FlatButton f -> f.setText(s);
			case Label l -> l.setText(s);
			case CLabel c -> c.setText(s);
			case null, default -> {
			}
		}
	}

	/**
	 * Sets the active.
	 *
	 * @param b
	 *            the new active
	 */
	public void setActive(final boolean b) {
		if (control.isDisposed()) return;
		control.setEnabled(b);
	}

	/**
	 * Checks if is disposed.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is disposed
	 * @date 21 févr. 2024
	 */
	public boolean isDisposed() { return control != null && control.isDisposed(); }

	/**
	 * Display parameter value.
	 */
	public void displayParameterValue() {
		// Temporary
		if (control.isDisposed()) return;
		editor.displayParameterValue();
	}

	/**
	 * Update among values.
	 */
	public void updateAmongValues(final List possibleValues) {}

}
