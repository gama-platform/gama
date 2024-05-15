/*******************************************************************************************************
 *
 * FixedValueEditorControl.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import static gama.core.common.util.StringUtils.toGaml;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;

/**
 * The Class FixedValueEditorControl.
 */
public class FixedValueEditorControl extends EditorControl<CLabel> {

	/**
	 * Constructor for building a read-only value control
	 *
	 * @param value
	 *            the original value to display
	 */
	@SuppressWarnings("rawtypes")
	FixedValueEditorControl(final AbstractEditor editor, final Composite parent) {
		super(editor, new CLabel(parent, SWT.READ_ONLY));
	}

	@Override
	public void setText(final String s) {
		if (control.isDisposed()) return;
		control.setText(s);
	}

	@Override
	public void displayParameterValue() {
		Object val = editor.getCurrentValue();
		setText(val instanceof String ? (String) val : toGaml(val, false));
	}

}
