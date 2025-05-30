/*******************************************************************************************************
 *
 * ExperimentControlsItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import gama.core.runtime.GAMA;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class ExperimentControlsItem extends GlobalToolbarCompoundItem {

	/** The stop item. */
	StopItem stopItem;

	/** The reload item. */
	ReloadItem reloadItem;

	/** The sync item. */
	SyncItem syncItem;

	/** The step item. */
	StepItem stepItem;

	/** The run item. */
	RunItem runItem;

	/** The back item. */
	BackItem backItem;

	/** The speed item. */
	SpeedItem speedItem;

	/**
	 * @param toolbar
	 */
	ExperimentControlsItem(final GamaToolbarSimple parent) {
		super(parent);
		GamaToolbarSimple toolbar = getInnerControl();
		items.add(backItem = new BackItem(toolbar));
		items.add(runItem = new RunItem(toolbar));
		items.add(stepItem = new StepItem(toolbar));
		items.add(speedItem = new SpeedItem(toolbar));
		items.add(syncItem = new SyncItem(toolbar));
		items.add(reloadItem = new ReloadItem(toolbar));
		items.add(stopItem = new StopItem(toolbar));
	}

	@Override
	public GamaToolbarSimple getInnerControl() { return (GamaToolbarSimple) super.getInnerControl(); }

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() == null) {
			hide();
		} else {
			show();
			super.update();

		}
	}

	@Override
	protected Control createInnerControl(final Composite composite) {
		return new GamaToolbarSimple(composite, SWT.NONE);
	}

}
