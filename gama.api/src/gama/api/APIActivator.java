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
import gama.api.utils.prefs.GamaPreferences;
import gama.dependencies.GamaBundleActivator;
import gama.dev.DEBUG;

/**
 * The APIActivator class serves as the OSGi bundle activator for the GAMA API bundle.
 * 
 * <p>This activator is responsible for initializing core platform services when the gama.api
 * bundle is started. It extends {@link GamaBundleActivator} to integrate with GAMA's bundle
 * lifecycle management system.</p>
 * 
 * <p><b>Initialization Responsibilities:</b></p>
 * <ul>
 *   <li><strong>Preference System:</strong> Initializes the {@link GamaPreferences} system for
 *       platform-wide configuration management</li>
 *   <li><strong>Executor Service:</strong> Resets and prepares the {@link GamaExecutorService}
 *       for concurrent task execution</li>
 * </ul>
 * 
 * <p><b>Lifecycle:</b></p>
 * <p>The activator follows the standard OSGi bundle lifecycle:</p>
 * <ol>
 *   <li>Bundle is started by the OSGi framework</li>
 *   <li>{@link #initialize(BundleContext)} is called to setup platform services</li>
 *   <li>Platform services are now available for use</li>
 *   <li>Bundle can be stopped when the platform shuts down</li>
 * </ol>
 * 
 * <p><b>Thread Safety:</b></p>
 * <p>The initialization is called by the OSGi framework and should be thread-safe.
 * The services it initializes (preferences, executor service) are designed for
 * concurrent access once initialized.</p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see GamaBundleActivator
 * @see GamaPreferences
 * @see GamaExecutorService
 */
public class APIActivator extends GamaBundleActivator {

	static {
		DEBUG.OFF();
	}

	/**
	 * Initializes the GAMA API bundle and its core services.
	 * 
	 * <p>This method is called by the OSGi framework when the bundle is started.
	 * It performs the following initialization tasks:</p>
	 * <ul>
	 *   <li>Initializes the platform preference system via {@link GamaPreferences#initialize()}</li>
	 *   <li>Resets the executor service via {@link GamaExecutorService#reset()}</li>
	 * </ul>
	 * 
	 * @param context the OSGi bundle context for this bundle
	 * 
	 * @see GamaBundleActivator#initialize(BundleContext)
	 */
	@Override
	public void initialize(final BundleContext context) {
		DEBUG.OUT("Starting GAMA API Bundle");
		GamaPreferences.initialize();
		GamaExecutorService.reset();
	}

}
