/*******************************************************************************************************
 *
 * APIActivator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api;

import org.osgi.framework.BundleContext;

import gama.api.runtime.GamaExecutorService;
import gama.api.utils.prefs.GamaPreferenceMap;
import gama.api.utils.prefs.GamaPreferences;
import gama.dependencies.GamaBundleActivator;
import gama.dev.DEBUG;

/**
 * The Class APIActivator.
 */
public class APIActivator extends GamaBundleActivator {

	static {
		DEBUG.OFF();
	}

	@Override
	public void initialize(final BundleContext context) {
		DEBUG.OUT("Starting GAMA API Bundle");
		GAMA.setPreferencesRegistry(new GamaPreferenceMap());
		GamaPreferences.initialize();
		GamaExecutorService.reset();
	}

}
