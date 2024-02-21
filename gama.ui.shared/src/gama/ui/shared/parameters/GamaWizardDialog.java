/*******************************************************************************************************
 *
 * GamaWizardDialog.java, in gama.ui.shared.shared, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.shared.parameters;


import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import gama.core.util.IList;
import gama.core.util.IMap;

/**
 * The Class GamaWizardDialog.
 */
public class GamaWizardDialog extends WizardDialog{

	/** The wizard. */
	GamaWizard wizard;
	
	/**
	 * Instantiates a new gama wizard dialog.
	 *
	 * @param parentShell the parent shell
	 * @param newWizard the new wizard
	 */
	public GamaWizardDialog(Shell parentShell, GamaWizard newWizard) {
		super(parentShell, newWizard);
		this.wizard = newWizard;
	}
	
	/*@Override
	protected Point getInitialSize() {
		final var p = super.getInitialSize();
		return new Point(p.x * 2, p.y);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}*/

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public IMap<String,IMap<String, Object>> getValues() {
		return wizard.getValues();
	}
	
	@Override
	protected void cancelPressed() {
		super.cancelPressed();
		this.wizard.getValues().clear();
	}
}
