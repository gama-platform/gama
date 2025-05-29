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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.controls.StatusControl;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class StatusItem extends GlobalToolbarItem {

	/** The composite. */
	Composite composite;

	/** The normal control. */
	Control normalControl;

	/** The blank data. */
	GridData normalData;

	/**
	 * @param toolbar
	 */
	StatusItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		composite = new Composite(toolbar, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);
		normalControl = StatusControl.installOn(composite);
		normalData = GridDataFactory.fillDefaults().grab(true, true).create();
		normalControl.setLayoutData(normalData);
		return toolbar.control(composite, 400);
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
		normalData.widthHint = i;
		super.setWidth(i);
	}

}
