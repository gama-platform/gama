/*******************************************************************************************************
 *
 * Startup.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
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

/**
 * The Class Startup.
 */
public class Startup implements IStartup {

	static {
		DEBUG.OFF();
	}

	@Override
	public void earlyStartup() {
		DEBUG.OUT("Startup of ui plugin begins");
		CleanupHelper.run();
		GamaKeyBindings.install();
		DEBUG.OUT("Startup of ui plugin finished");
		if (GamaPreferences.Runtime.START_TESTS.getValue()) { TestsRunner.start(); }

	}

}
