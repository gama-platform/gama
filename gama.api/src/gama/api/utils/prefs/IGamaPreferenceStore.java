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
 * {@code IGamaPreferenceStore} is the central abstraction for reading, writing, registering, and managing GAMA
 * preferences. A preference store acts as a persistent key/value repository that backs instances of {@link Pref},
 * providing the plumbing between the in-memory preference objects and the underlying storage mechanism (e.g. the JRE
 * {@link java.util.prefs.Preferences} API or an Eclipse {@code IEclipsePreferences} node).
 *
 * <p>
 * Typical usage involves three phases:
 * <ol>
 * <li><strong>Registration</strong> – a {@link Pref} is registered via {@link #register(Pref)}, which seeds it with
 * any persisted or overridden value.</li>
 * <li><strong>Read/Write</strong> – the current value of a registered preference is flushed to the backend via
 * {@link #write(Pref)}, and individual values can be read back through the {@link Pref} itself.</li>
 * <li><strong>Persistence export/import</strong> – the whole set of preferences can be exported to a
 * human-readable properties file (via {@link #saveToProperties(String)}) or to a GAML model file (via
 * {@link #saveToGAML(String)}), and later restored via {@link #loadFromProperties(String)}.</li>
 * </ol>
 * </p>
 */
public interface IGamaPreferenceStore {

	/**
	 * Persists a raw key/value pair directly into the backing store. This low-level method is used internally by
	 * {@link #write(Pref)} to convert a typed {@link Pref} value into the format expected by the backend.
	 *
	 * @param <T>
	 *            the type of the value to store (e.g. {@code String}, {@code Integer}, {@code Boolean})
	 * @param key
	 *            the preference key; must not be {@code null}
	 * @param value
	 *            the value to associate with the key
	 */
	<T> void putInStore(String key, T value);

	/**
	 * Retrieves the raw string representation of a preference value from the backing store.
	 *
	 * @param key
	 *            the preference key to look up; must not be {@code null}
	 * @param def
	 *            the default string value to return if no entry exists for {@code key}
	 * @return the stored string value, or {@code def} if the key is absent
	 */
	String getInStore(String key, String def);

	/**
	 * Destroys the entire preferences node, removing all stored preference entries and resetting them to their default
	 * values as defined by the registered {@link Pref} objects. This operation is typically used when the user requests
	 * a "revert to defaults" action.
	 */
	void clear();

	/**
	 * Exports all visible, GAML-accessible preferences as a GAML model file. The generated file contains two
	 * experiment definitions: one that reads and displays the current preference values ({@code Display __PREFS__}),
	 * and one that sets them ({@code Set __PREFS__}).
	 *
	 * @param path
	 *            the absolute file-system path at which to write the GAML file
	 */
	void saveToGAML(final String path);

	/**
	 * Exports all registered preference key/value pairs to a Java {@link java.util.Properties} file at the given path.
	 * The resulting file can be shared across GAMA instances and later loaded via {@link #loadFromProperties(String)}.
	 *
	 * @param path
	 *            the absolute file-system path at which to write the properties file
	 */
	void saveToProperties(String path);

	/**
	 * Reads a Java {@link java.util.Properties} file previously created by {@link #saveToProperties(String)} and
	 * restores the preference values it contains into the backing store. Registered {@link Pref} instances whose keys
	 * appear in the file will be updated accordingly.
	 *
	 * @param path
	 *            the absolute file-system path of the properties file to load
	 */
	void loadFromProperties(String path);

	/**
	 * Serializes the current value of the given {@link Pref} into the backing store. The method handles all supported
	 * GAML type identifiers (int, float, boolean, string, file, color, point, font, date) and writes the appropriate
	 * representation, then flushes the store to guarantee durability.
	 *
	 * @param gp
	 *            the preference whose value should be persisted; must not be {@code null}
	 */
	void write(Pref gp);

	/**
	 * Registers a {@link Pref} with this store so that it participates in persistence and override resolution. During
	 * registration the store checks whether the preference key has been overridden — either via a JVM system property
	 * ({@code -Dkey=value}) or by a previously persisted value — and initializes the preference accordingly. The
	 * preference is then added to the internal registry and the store is flushed.
	 *
	 * @param gp
	 *            the preference to register; must not be {@code null} and must have a non-{@code null} key
	 */
	void register(Pref<?> gp);

	/**
	 * Returns a snapshot of the set of keys for all preferences currently registered with this store.
	 *
	 * @return an unmodifiable or live {@link Collection} of preference key strings; never {@code null}
	 */
	Collection<String> getKeys();

	/**
	 * Returns a snapshot of all {@link Pref} instances currently registered with this store, in registration order.
	 *
	 * @return a {@link Collection} of registered preferences; never {@code null}
	 */
	Collection<Pref> getPreferences();

	/**
	 * Looks up a registered preference by its key.
	 *
	 * @param s
	 *            the preference key to look up
	 * @return the {@link Pref} associated with the given key, or {@code null} if no preference is registered under
	 *         that key
	 */
	Pref get(String s);

	/**
	 * Applies a set of preference overrides loaded from the properties file at {@code path} and merges their values
	 * into {@code modelValues}. This is typically called at model load time to allow a model to specify its own
	 * preference values that differ from the global defaults.
	 *
	 * @param path
	 *            the absolute file-system path of the properties file to load; may be {@code null} if only the map
	 *            should be populated from the currently registered preferences
	 * @param modelValues
	 *            a mutable map that will receive all current preference key/value pairs after the overrides have been
	 *            applied
	 */
	void applyPreferencesFrom(final String path, final Map<String, Object> modelValues);
}