/*******************************************************************************************************
 *
 * Startup.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared;

import org.eclipse.ui.IStartup;

import gama.core.common.preferences.GamaPreferences;
import gama.dev.DEBUG;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.commands.TestsRunner;
import gama.ui.shared.utils.CleanupHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class Startup.
 */
public class Startup implements IStartup {

	static {
		DEBUG.OFF();
	}

	@Override
	public void earlyStartup() {
		WorkbenchHelper.runInUI("Configuring GAMA UI", 0, e -> {
			CleanupHelper.run();
			GamaKeyBindings.install();
		});
		if (GamaPreferences.Runtime.START_TESTS.getValue()) { TestsRunner.start(); }

	}

}
