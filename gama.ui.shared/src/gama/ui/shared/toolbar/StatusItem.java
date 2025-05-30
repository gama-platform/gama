/*******************************************************************************************************
 *
 * StatusItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
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

import gama.ui.shared.controls.StatusControl;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class StatusItem extends GlobalToolbarCompoundItem {

	/**
	 * @param toolbar
	 */
	StatusItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	@Override
	public void update() {}

	@Override
	public void reinit() {}

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
		StatusControl.getInstance().setWidth(i);
		super.setWidth(i);
	}

	@Override
	protected Control createInnerControl(final Composite composite) {
		return StatusControl.installOn(composite);
	}

}
