/*******************************************************************************************************
 *
 * StepItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
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
public class StepItem extends GlobalToolbarItem {

	/**
	 * @param toolbar
	 */
	StepItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		return toolbar.button("experiment/experiment.step", "Step Experiment", "Runs the experiment one step at a time",
				e -> GAMA.stepFrontmostExperiment(false));

	}

	@Override
	public void reinit() {
		show();
		setImage("experiment/experiment.step");
		setTooltip("Runs the experiment one step at a time");
	}

	@Override
	public void update() {
		if (GAMA.getExperimentAgent() == null || PerspectiveHelper.isModelingPerspective()) { hide(); }

	}

}
