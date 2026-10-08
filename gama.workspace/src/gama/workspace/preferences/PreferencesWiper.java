/*******************************************************************************************************
 *
 * PreferencesWiper.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.preferences;

import java.util.prefs.Preferences;

import gama.api.utils.prefs.GamaPreferenceStore;

/**
 * The Class PreferencesWiper.
 */
public class PreferencesWiper { // NO_UCD (unused code)

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(final String[] args) {
		try {
			final var store = Preferences.userRoot().node(GamaPreferenceStore.NODE_NAME);
			store.removeNode();
			System.out.println("All GAMA preferences have been erased.");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}