/*******************************************************************************************************
 *
 * DevToolsPreferences.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import gama.ui.devtools.GamaDevToolsActivator;

/**
 * The preferences of the developer tools, and their default values.
 */
public class DevToolsPreferences extends AbstractPreferenceInitializer {

	/** Vendor used by the new plugin wizard. */
	public static final String DEFAULT_VENDOR = "gama.devtools.defaultVendor";

	/** Prefix of the plugin ids proposed by the new plugin wizard. */
	public static final String DEFAULT_PREFIX = "gama.devtools.defaultPrefix";

	/** Whether "Clean and Rebuild" repairs the configuration of the plugins first. */
	public static final String REPAIR_BEFORE_CLEAN = "gama.devtools.repairBeforeClean";

	/** Whether "Clean and Rebuild" rebuilds the whole workspace (or only the GAMA plugins concerned). */
	public static final String REBUILD_WORKSPACE = "gama.devtools.rebuildWorkspace";

	/** Whether the configuration is checked (and markers created) after each "Clean and Rebuild". */
	public static final String CHECK_AFTER_REBUILD = "gama.devtools.checkAfterRebuild";

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = store();
		store.setDefault(DEFAULT_VENDOR, "UMMISCO");
		store.setDefault(DEFAULT_PREFIX, "gama.extension.");
		store.setDefault(REPAIR_BEFORE_CLEAN, true);
		store.setDefault(REBUILD_WORKSPACE, true);
		store.setDefault(CHECK_AFTER_REBUILD, true);
	}

	/** The preference store of the plugin. */
	public static IPreferenceStore store() {
		return GamaDevToolsActivator.getInstance().getPreferenceStore();
	}

	/** Shortcut for boolean preferences. */
	public static boolean is(final String key) {
		return store().getBoolean(key);
	}

	/** Shortcut for string preferences. */
	public static String get(final String key) {
		return store().getString(key);
	}

}
