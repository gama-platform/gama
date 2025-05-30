/*******************************************************************************************************
 *
 * ReloadItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
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
public class ReloadItem extends GlobalToolbarItem {

	/**
	 * @param toolbar
	 */
	ReloadItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		return toolbar.button("experiment/experiment.reload", "Reload Experiment",
				"Reload Experiment with the current parameters", e -> {
					GAMA.reloadFrontmostExperiment(false);
				});

	}

	@Override
	public void reinit() {
		show();
		setImage("experiment/experiment.reload");
		setTooltip("Reload Experiment with the current parameters");
	}

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() == null || PerspectiveHelper.isModelingPerspective()) { hide(); }
	}

}
