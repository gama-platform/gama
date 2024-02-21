/*******************************************************************************************************
 *
 * ToggleDisplayTabs.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import static gama.ui.experiment.commands.ArrangeDisplayViews.collectAndPrepareDisplayViews;
import static gama.ui.experiment.commands.LayoutTreeConverter.convertCurrentLayout;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.application.workbench.SimulationPerspectiveDescriptor;

/**
 * The Class ToggleDisplayTabs.
 */
public class ToggleDisplayTabs extends AbstractHandler {

	// NOT YET READY FOR PRIME TIME
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final SimulationPerspectiveDescriptor sd = PerspectiveHelper.getActiveSimulationPerspective();
		if (sd != null) { sd.keepTabs(!sd.keepTabs()); }
		ArrangeDisplayViews.execute(convertCurrentLayout(collectAndPrepareDisplayViews()));
		return this;
	}

}
