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

import java.util.List;

/**
 * @param <T>
 */
public interface IGamaPreferenceStore {

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	List<String> getKeys();

	/**
	 * Put.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	void put(String key, String value);

	/**
	 * Put int.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	void putInt(String key, int value);

	/**
	 * Put double.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	void putDouble(String key, Double value);

	/**
	 * Put boolean.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	void putBoolean(String key, Boolean value);

	/**
	 * Gets the.
	 *
	 * @param key
	 *            the key
	 * @return the object
	 */
	default Object get(final String key) {
		return get(key, null);
	}

	/**
	 * Gets the.
	 *
	 * @param key
	 *            the key
	 * @param def
	 *            the def
	 * @return the string
	 */
	String get(String key, String def);

	/**
	 * First searches if the preference is overriden in the system/VM properties/arguments, then looks into the store if
	 * not
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	Integer getInt(String key, Integer def);

	/**
	 * First searches if the preference is overriden in the system/VM properties/arguments, then looks into the store if
	 * not
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	Double getDouble(String key, Double def);

	/**
	 * First searches if the preference is overriden in the system/VM properties/arguments, then looks into the store if
	 * not
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	Boolean getBoolean(String key, Boolean def);

	/**
	 * Makes sure preferences are kept in sync between GAMA runtime and the backend file
	 */

	void flush();

	/**
	 * Destroys the preferences node (all preferences are removed and replaced by defaults
	 */
	void clear();

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

}