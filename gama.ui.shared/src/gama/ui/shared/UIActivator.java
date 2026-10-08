/*******************************************************************************************************
 *
 * UIActivator.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared;

import org.osgi.framework.BundleContext;

import gama.api.utils.prefs.GamaPreferences;
import gama.dependencies.GamaBundleActivator;
import gama.ui.shared.commands.TestsRunner;
import gama.ui.shared.utils.UICleanupTasks;

/**
 * The Class UIActivator.
 */
public class UIActivator extends GamaBundleActivator {

	/**
	 * Start.
	 *
	 * @param c
	 *            the c
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public void initialize(final BundleContext c) {
		UICleanupTasks.run();
		if (GamaPreferences.Runtime.START_TESTS.getValue()) { TestsRunner.start(); }
	}

}
