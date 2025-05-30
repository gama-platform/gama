/*******************************************************************************************************
 *
 * SpeedItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolItem;

import gama.core.runtime.GAMA;
import gama.ui.shared.controls.SimulationSpeedControl;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class SpeedItem extends GlobalToolbarItem {

	/** The composite. */
	Composite composite;

	/** The hidden control. */
	Control blankControl;

	/** The normal control. */
	Control normalControl;

	/** The blank data. */
	GridData blankData, normalData;

	/**
	 * @param toolbar
	 */
	SpeedItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
		hide();
	}

	@Override
	public void reinit() {
		SimulationSpeedControl.reinit();
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		composite = new Composite(toolbar, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);
		blankControl = new Label(composite, SWT.NONE);
		blankData = GridDataFactory.fillDefaults().grab(true, true).create();
		blankControl.setLayoutData(blankData);
		normalControl = SimulationSpeedControl.installOn(composite);
		normalData = GridDataFactory.fillDefaults().grab(true, true).create();
		normalControl.setLayoutData(normalData);
		return toolbar.control(composite, 132);
	}

	@Override
	public void hide() {
		if (normalControl == null || item.isDisposed()) return;
		if (blankData != null) { blankData.exclude = false; }
		if (blankControl != null && !blankControl.isDisposed()) { blankControl.setVisible(true); }
		if (normalData != null) { normalData.exclude = true; }
		if (normalControl != null && !normalControl.isDisposed()) { normalControl.setVisible(false); }
		composite.requestLayout();
	}

	@Override
	public void show() {
		if (normalControl == null || item.isDisposed()) return;
		if (blankData != null) { blankData.exclude = true; }
		if (blankControl != null && !blankControl.isDisposed()) { blankControl.setVisible(false); }
		if (normalData != null) { normalData.exclude = false; }
		if (normalControl != null && !normalControl.isDisposed()) { normalControl.setVisible(true); }
		composite.requestLayout();
	}

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() == null) {
			hide();
		} else {
			show();
		}
	}

	@Override
	public int getDefaultWidth() { return 132; }

}
