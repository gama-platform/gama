/*******************************************************************************************************
 *
 * BackItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
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
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class BackItem extends GlobalToolbarItem {

	/**
	 * @param toolbar
	 */
	BackItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		return toolbar.button("experiment/experiment.step.back", "Step Back", "Step back the experiment",
				e -> GAMA.stepBackFrontmostExperiment(false));

	}

	@Override
	public void reinit() {}

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() != null && GAMA.getExperimentAgent().canStepBack()) {
			setImage("experiment/experiment.step.back");
			show();
		} else {
			hide();
		}
	}

}
