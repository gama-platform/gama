/*******************************************************************************************************
 *
 * GamaDevToolsActivator.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator for the GAMA Developer Tools plugin. This class controls the plug-in life cycle and provides
 * shared access to plugin-level resources such as the preference store and image registry.
 *
 * <p>
 * On startup, the activator records the plugin instance for later use by handlers, wizards, and preference pages.
 * </p>
 *
 * @author GAMA Development Team
 * @since 2026
 */
public class GamaDevToolsActivator extends AbstractUIPlugin {

	/**
	 * The unique plug-in identifier, matching the Bundle-SymbolicName in MANIFEST.MF.
	 */
	public static final String PLUGIN_ID = "gama.ui.devtools";

	/** The shared singleton instance of this activator. */
	private static GamaDevToolsActivator instance;

	/**
	 * Returns the shared singleton instance of this activator.
	 *
	 * @return the shared instance, or {@code null} if the plugin has not been started yet
	 */
	public static GamaDevToolsActivator getInstance() {
		return instance;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Stores the singleton instance and delegates to the parent {@link AbstractUIPlugin#start(BundleContext)}.
	 * </p>
	 *
	 * @param context
	 *            the OSGi bundle context
	 * @throws Exception
	 *             if the plugin cannot be started
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Clears the singleton reference and delegates to the parent {@link AbstractUIPlugin#stop(BundleContext)}.
	 * </p>
	 *
	 * @param context
	 *            the OSGi bundle context
	 * @throws Exception
	 *             if the plugin cannot be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

}
