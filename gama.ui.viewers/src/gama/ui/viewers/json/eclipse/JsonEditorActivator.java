/*******************************************************************************************************
 *
 * JsonEditorActivator.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JsonEditorActivator extends AbstractUIPlugin {

	/** The Constant PLUGIN_ID. */
	// The plug-in COMMAND_ID
	public static final String PLUGIN_ID = "gama.ui.viewers"; //$NON-NLS-1$

	/** The plugin. */
	// The shared instance
	private static JsonEditorActivator plugin;

	/** The color manager. */
	private final ColorManager colorManager;

	/**
	 * The constructor
	 */
	public JsonEditorActivator() {
		colorManager = new ColorManager();
	}

	/**
	 * Gets the color manager.
	 *
	 * @return the color manager
	 */
	public ColorManager getColorManager() { return colorManager; }

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		JsonEditorUtil.refreshParserSettings();
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		colorManager.dispose();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JsonEditorActivator getDefault() { return plugin; }

}
