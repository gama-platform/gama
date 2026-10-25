/*******************************************************************************************************
 *
 * JavaHeaderSettings.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.preferences;

import java.time.Year;

import org.eclipse.jface.dialogs.IDialogSettings;

import gama.ui.devtools.GamaDevToolsActivator;

/** Shared by the bulk header command and the Java wizards. */
public final class JavaHeaderSettings {

	private JavaHeaderSettings() {}

	private static IDialogSettings settings() {
		GamaDevToolsActivator plugin = GamaDevToolsActivator.getInstance();
		return plugin == null ? null : plugin.getDialogSettings();
	}

	public static String version() {
		IDialogSettings settings = settings();
		String value = settings == null ? null : settings.get("javaHeader.version");
		return value == null ? "2025-03" : value;
	}

	public static String year() {
		IDialogSettings settings = settings();
		String value = settings == null ? null : settings.get("javaHeader.year");
		return value == null ? Year.now().toString() : value;
	}

	public static void save(final String version, final String year) {
		IDialogSettings settings = settings();
		if (settings != null) {
			settings.put("javaHeader.version", version);
			settings.put("javaHeader.year", year);
		}
	}
}
