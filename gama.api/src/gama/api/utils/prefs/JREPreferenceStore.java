/*******************************************************************************************************
 *
 * JREPreferenceStore.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import gama.dev.DEBUG;

/**
 * {@code JREPreferenceStore} is a {@link GamaPreferenceStore} implementation backed by the standard JRE
 * {@link Preferences} API. Because the JRE {@code Preferences} node is stored in a location shared across all
 * applications that use the same JVM user account (typically the OS user-preference registry or
 * {@code ~/.java/.userPrefs}), preferences stored here are visible to every instance of GAMA run by the same OS user,
 * regardless of the GAMA version or workspace in use.
 *
 * <p>
 * At construction time the store collects the set of keys that already exist in the underlying {@link Preferences}
 * node into {@link GamaPreferenceStore#overriddenKeys}. During
 * {@link GamaPreferenceStore#register(Pref) registration} these keys are treated as overrides, meaning the persisted
 * values take precedence over the compiled-in defaults.
 * </p>
 *
 * <p>
 * Import and export of preferences use the XML-based serialization built into the JRE
 * {@link Preferences#importPreferences(java.io.InputStream)} and the {@link Properties} text format respectively.
 * </p>
 */
public class JREPreferenceStore extends GamaPreferenceStore<Preferences> {

	/**
	 * Constructs a new {@code JREPreferenceStore} wrapping the given JRE {@link Preferences} node. The existing keys
	 * in the node are captured into {@link GamaPreferenceStore#overriddenKeys} so that previously persisted values
	 * can override the compiled-in defaults during preference registration. If the backing store cannot be read a
	 * diagnostic message is emitted via {@link DEBUG}.
	 *
	 * @param store
	 *            the JRE {@link Preferences} node to use as the backing store; must not be {@code null}
	 */
	public JREPreferenceStore(final Preferences store) {
		super(store);
		try {
			overriddenKeys = new LinkedHashSet<>(Arrays.asList(store.keys()));
		} catch (BackingStoreException e) {
			DEBUG.ERR("Impossible to read the preferences key from the global preferences store");
		}
	}

	/**
	 * Writes a typed key/value pair into the JRE {@link Preferences} node. The method dispatches on the runtime type
	 * of {@code value}:
	 * <ul>
	 * <li>{@code null} → stored as an empty string ({@code ""}).</li>
	 * <li>{@link String} → stored with {@link Preferences#put(String, String)}.</li>
	 * <li>{@link Integer} → stored with {@link Preferences#putInt(String, int)}.</li>
	 * <li>{@link Double} → stored with {@link Preferences#putDouble(String, double)}.</li>
	 * <li>{@link Boolean} → stored with {@link Preferences#putBoolean(String, boolean)}.</li>
	 * <li>Any other type → its {@link Object#toString()} representation is stored as a string.</li>
	 * </ul>
	 * After writing, {@link #flush()} is called to guarantee the value is persisted immediately.
	 *
	 * @param <T>
	 *            the type of the value to store
	 * @param key
	 *            the preference key; must not be {@code null}
	 * @param value
	 *            the value to associate with the key
	 */
	@Override
	public <T> void putInStore(final String key, final T value) {
		switch (value) {
			case null -> store.put(key, "");
			case String s -> store.put(key, s);
			case Integer i -> store.putInt(key, i);
			case Double j -> store.putDouble(key, j);
			case Boolean k -> store.putBoolean(key, k);
			default -> {
				store.put(key, value.toString());
			}
		}
		flush();
	}

	/**
	 * Flushes all pending writes to the underlying JRE {@link Preferences} node by calling
	 * {@link Preferences#flush()}. Any {@link BackingStoreException} thrown by the JRE is silently swallowed, as
	 * failures here are non-critical (the in-memory state is still correct).
	 */
	@Override
	public void flush() {
		try {
			store.flush();
		} catch (BackingStoreException e) {}
	}

	/**
	 * Destroys the entire JRE {@link Preferences} node by calling {@link Preferences#removeNode()}, which deletes the
	 * node and all of its keys from the persistent backing store. After this call, subsequent reads will return
	 * default values. Any {@link BackingStoreException} is silently ignored.
	 */
	@Override
	public void clear() {
		try {
			store.removeNode();
		} catch (BackingStoreException e) {}
	}

	/**
	 * Imports preferences from a JRE XML preferences file (as produced by
	 * {@link Preferences#exportNode(java.io.OutputStream)}) located at the given path. The import is performed via
	 * {@link Preferences#importPreferences(java.io.InputStream)}, which restores all key/value entries into the
	 * appropriate preferences nodes. Any {@link IOException} or {@link InvalidPreferencesFormatException} is printed
	 * to the standard error stream.
	 *
	 * @param path
	 *            the absolute file-system path of the XML preferences file to import
	 */
	@Override
	public void loadFromProperties(final String path) {
		try (final var is = new FileInputStream(path);) {
			Preferences.importPreferences(is);
		} catch (final IOException | InvalidPreferencesFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Exports all key/value pairs stored in the JRE {@link Preferences} node to a Java {@link Properties} text file
	 * at the given path. Each entry is written as {@code key=value}. The file is prefixed with the comment
	 * {@code "GAMA __PREFS__ <timestamp>"}. Any {@link IOException} or {@link BackingStoreException} is printed to
	 * the standard error stream.
	 *
	 * @param path
	 *            the absolute file-system path at which to write the properties file
	 */
	@Override
	public void saveToProperties(final String path) {
		try (final var os = new FileOutputStream(path);) {
			Properties prop = new Properties();
			for (String key : store.keys()) { prop.setProperty(key, store.get(key, null)); }
			prop.store(os, "GAMA __PREFS__ " + LocalDateTime.now());
		} catch (final IOException | BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the raw string value stored for {@code key} in the JRE {@link Preferences} node by delegating to
	 * {@link Preferences#get(String, String)}, returning {@code def} if no entry exists.
	 *
	 * @param key
	 *            the preference key to look up; must not be {@code null}
	 * @param def
	 *            the default value to return if the key is absent
	 * @return the stored string value, or {@code def} if absent
	 */
	@Override
	public String getInStore(final String key, final String def) {
		return store.get(key, def);
	}

}