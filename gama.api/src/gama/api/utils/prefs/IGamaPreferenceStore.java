/*******************************************************************************************************
 *
 * IGamaPreferenceStore.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

import java.util.Collection;
import java.util.Map;

/**
 * @param <T>
 */
public interface IGamaPreferenceStore {

	/**
	 * Put.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	<T> void putInStore(String key, T value);

	/**
	 * Gets the in store.
	 *
	 * @param key
	 *            the key
	 * @return the in store
	 */
	String getInStore(String key, String def);

	/**
	 * Destroys the preferences node (all preferences are removed and replaced by defaults
	 */
	void clear();

	/**
	 * Save to GAML.
	 *
	 * @param path
	 *            the path
	 */
	void saveToGAML(final String path);

	/**
	 * Exports the contents of the preferences as a properties (key = value) file, which can then be reloaded in another
	 * instance of GAMA
	 *
	 * @param path
	 */
	void saveToProperties(String path);

	/**
	 * Reads a properties file and sets the contents of the preferences to the values registered in the file
	 *
	 * @param path
	 */
	void loadFromProperties(String path);

	/**
	 * Write.
	 *
	 * @param gp
	 *            the gp
	 */
	void write(Pref gp);

	/**
	 * Register.
	 *
	 * @param gp
	 *            the gp
	 */
	void register(Pref<?> gp);

	/**
	 * @return
	 */
	Collection<String> getKeys();

	/**
	 * Gets the preferences.
	 *
	 * @return the preferences
	 */
	Collection<Pref> getPreferences();

	/**
	 * @param s
	 * @return
	 */
	Pref get(String s);

	/**
	 * Apply preferences from.
	 *
	 * @param path
	 *            the path
	 * @param modelValues
	 *            the model values
	 */
	void applyPreferencesFrom(final String path, final Map<String, Object> modelValues);
}