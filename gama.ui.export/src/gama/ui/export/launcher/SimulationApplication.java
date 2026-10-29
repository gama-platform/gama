package gama.ui.export.launcher;

import java.nio.file.Path;

import gama.api.utils.prefs.GamaPreferences;
import gama.export.SimulationLaunchContext;
import gama.export.SimulationPackage;
import gama.export.SimulationPackageLauncher;
import gama.ui.application.Application;
import gama.ui.application.workbench.ApplicationWorkbenchAdvisor;
import gama.ui.application.workbench.PerspectiveHelper;

/** Entry point selected only in exported simulation launchers. */
public final class SimulationApplication extends Application {

	@Override
	protected Object checkWorkspace() throws Exception {
		// The package bootstrap has already validated and selected the private workspace.
		return System.getProperty(SimulationPackageLauncher.CONTENT_PROPERTY) == null ? super.checkWorkspace() : null;
	}

	@Override
	protected Object initializeWorkspace() throws Exception {
		PerspectiveHelper.setHomePerspectiveId(LauncherHomePerspective.ID);
		final String content = System.getProperty(SimulationPackageLauncher.CONTENT_PROPERTY);
		if (content != null) {
			if (gama.api.additions.GamaBundleLoader.ERRORED)
				throw new java.io.IOException("GAMA language contributions could not be initialized for this package");
			final var extracted = SimulationPackage.Extracted.open(Path.of(content));
			return StartupModelHelper.getInstance().initialize(SimulationLaunchContext.fromPackage(extracted)) ? null : EXIT_OK;
		}
		if (!GamaPreferences.Interface.CORE_STARTUP_MODEL.getValue())
			throw new IllegalStateException("The simulation launcher requires a startup model");
		return StartupModelHelper.getInstance().initialize() ? null : EXIT_OK;
	}

	@Override
	protected ApplicationWorkbenchAdvisor createWorkbenchAdvisor() { return new SimulationWorkbenchAdvisor(); }
}
