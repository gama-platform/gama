package gama.ui.export.launcher;

import gama.api.utils.prefs.GamaPreferences;
import gama.ui.application.Application;
import gama.ui.application.workbench.ApplicationWorkbenchAdvisor;
import gama.ui.application.workbench.PerspectiveHelper;

/** Entry point selected only in exported simulation launchers. */
public final class SimulationApplication extends Application {

	@Override
	protected Object initializeWorkspace() throws Exception {
		PerspectiveHelper.setHomePerspectiveId(LauncherHomePerspective.ID);
		if (!GamaPreferences.Interface.CORE_STARTUP_MODEL.getValue())
			throw new IllegalStateException("The simulation launcher requires a startup model");
		return StartupModelHelper.getInstance().initialize() ? null : EXIT_OK;
	}

	@Override
	protected ApplicationWorkbenchAdvisor createWorkbenchAdvisor() { return new SimulationWorkbenchAdvisor(); }
}
