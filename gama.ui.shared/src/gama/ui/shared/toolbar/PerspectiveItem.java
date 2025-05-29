/*******************************************************************************************************
 *
 * PerspectiveItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.swt.widgets.ToolItem;

import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class PerspectiveItem extends GlobalToolbarItem {

	/**
	 * @param toolbar
	 */
	PerspectiveItem(final GamaToolbarSimple toolbar) {
		super(toolbar);
	}

	/**
	 * @param toolbar
	 * @return
	 */
	@Override
	public ToolItem createItem(final GamaToolbarSimple toolbar) {
		return toolbar.button("views/perspective.simulation", "Open simulation perspective",
				"Open simulation perspective", e -> {
					if (PerspectiveHelper.isModelingPerspective()
							&& PerspectiveHelper.getCurrentSimulationPerspective() != null) {
						PerspectiveHelper.switchToSimulationPerspective();
					} else {
						PerspectiveHelper.openModelingPerspective(true, true);
					}

				});

	}

	@Override
	public void reinit() {
		setImage("views/perspective.modeling");
		setTooltip("Switch to the editor");
		show();
	}

	@Override
	public void update() {
		if (PerspectiveHelper.isNextOrCurrentModelingPerspective()) {
			if (PerspectiveHelper.getCurrentSimulationPerspective() != null) {
				setImage("views/perspective.simulation");
				setTooltip("Switch to the experiment");
				show();
			} else {
				hide();
			}
		}
	}

}
