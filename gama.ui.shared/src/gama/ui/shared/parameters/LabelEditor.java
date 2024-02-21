/*******************************************************************************************************
 *
 * LabelEditor.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import gama.core.kernel.experiment.IParameter;
import gama.core.metamodel.agent.IAgent;
import gama.ui.shared.interfaces.EditorListener;

/**
 * Edits arbitrary strings, not only GAML ones or well-formed expressions
 *
 * @author A. Drogoul
 *
 */
public class LabelEditor extends AbstractEditor<String> {

	/** The text box. */
	private Text textBox;

	/**
	 * Instantiates a new label editor.
	 *
	 */
	LabelEditor(final IAgent agent, final IParameter p, final EditorListener<String> whenModified) {
		super(agent, p, whenModified);

	}

	@Override
	public void modifyText(final ModifyEvent me) {
		if (internalModification) return;
		modifyValue(textBox.getText());
	}

	@Override
	protected Control createCustomParameterControl(final Composite comp) {
		textBox = new Text(comp, SWT.BORDER);
		textBox.addModifyListener(this);
		return textBox;
	}

	@Override
	protected void displayParameterValue() {
		String s = currentValue;
		if (s == null) { s = ""; }
		textBox.setText(s);
	}

	@Override
	protected int[] getToolItems() { return new int[] { REVERT }; }

}
