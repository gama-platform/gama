/*******************************************************************************************************
 *
 * ExperimentItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gama.core.runtime.GAMA;
import gama.ui.shared.controls.ExperimentControl;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class ExperimentItem extends GlobalToolbarCompoundItem {

	/**
	 * @param toolbar
	 */
	ExperimentItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
		hide();
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public Control createInnerControl(final Composite composite) {
		return ExperimentControl.installOn(composite);
	}

	@Override
	public void reinit() {}

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() == null) {
			hide();
		} else {
			show();
		}
	}

	@Override
	public int getDefaultWidth() { return 400; }

	/**
	 * Sets the width.
	 *
	 * @param i
	 *            the new width
	 */
	@Override
	public void setWidth(final int i) {
		// normalData.widthHint = i;
		ExperimentControl.getInstance().setWidth(i);
		super.setWidth(i);

	}

}
