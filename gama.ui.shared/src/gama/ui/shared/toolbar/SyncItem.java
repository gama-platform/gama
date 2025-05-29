/*******************************************************************************************************
 *
 * SyncItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.swt.widgets.ToolItem;

import gama.core.runtime.GAMA;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class SyncItem extends GlobalToolbarItem {

	/**
	 * @param toolbar
	 */
	SyncItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		return toolbar.button("experiment/experiment.sync", "Synchronize Experiment",
				"Synchronize the experiment with its outputs", e -> {
					if (GAMA.isSynchronized()) {
						GAMA.desynchronizeFrontmostExperiment();
					} else {
						GAMA.synchronizeFrontmostExperiment();
					}
				});

	}

	@Override
	public void reinit() {
		show();
		setImage("experiment/experiment.sync");
		setTooltip("Synchronize the experiment with its outputs");
	}

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() == null || PerspectiveHelper.isModelingPerspective()) { hide(); }
	}

}
