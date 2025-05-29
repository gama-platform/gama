/*******************************************************************************************************
 *
 * RunItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class RunItem extends GlobalToolbarItem {

	/**
	 * @param toolbar
	 */
	RunItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		return toolbar.button("experiment/experiment.run", "Run Experiment", "Runs or pauses the current experiment",
				e -> GAMA.startPauseFrontmostExperiment(false));

	}

	@Override
	public void reinit() {
		if (GAMA.isPaused()) {
			setImage("experiment/experiment.run");
			setTooltip("Run Experiment (" + GamaKeyBindings.PLAY_STRING + ")");
		} else {
			setImage("experiment/experiment.pause");
			setTooltip("Pause Experiment (" + GamaKeyBindings.PLAY_STRING + ")");
		}
		show();
	}

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() == null || PerspectiveHelper.isModelingPerspective()) {
			hide();
		} else {
			reinit();
		}

	}

}
