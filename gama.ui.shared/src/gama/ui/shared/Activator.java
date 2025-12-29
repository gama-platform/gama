/*******************************************************************************************************
 *
 * Activator.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import gama.core.common.preferences.GamaPreferences;
import gama.ui.shared.commands.TestsRunner;
import gama.ui.shared.utils.UICleanupTasks;

/**
 * The Class Activator.
 */
public class Activator extends AbstractUIPlugin {

	@Override
	public void start(final BundleContext c) throws Exception {
		super.start(c);
		UICleanupTasks.run();
		if (GamaPreferences.Runtime.START_TESTS.getValue()) { TestsRunner.start(); }
	}

}
