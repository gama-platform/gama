/*******************************************************************************************************
 *
 * AddMonitorHandler.java, in gama.ui.shared.experiment, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.experiment.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;

import gama.api.GAMA;
import gama.api.utils.prefs.GamaPreferences;
import gama.ui.experiment.views.inspectors.ExperimentParametersView;
import gama.ui.experiment.views.inspectors.MonitorView;
import gama.ui.shared.utils.ViewsHelper;

/**
 * The Class AddMonitorHandler.
 */
public class AddMonitorHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (GamaPreferences.Runtime.CORE_MONITOR_PARAMETERS.getValue()) {
			IViewPart view = ViewsHelper.findView(ExperimentParametersView.ID, null, true);
			if (view instanceof ExperimentParametersView paramsView) {
				paramsView.createNewMonitor();
			} else {
				// Parameters view not found; fall back to creating the monitor output directly
				// so it will appear the next time the Parameters view is opened.
				MonitorView.createNewMonitor(GAMA.getRuntimeScope());
			}
		} else {
			MonitorView.createNewMonitor(GAMA.getRuntimeScope());
		}
		return null;
	}

}
