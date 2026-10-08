/*******************************************************************************************************
 *
 * ConfigurationPreferenceStore.java, in gama.api, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import gama.dev.DEBUG;

/**
 * {@code ConfigurationPreferenceStore} is a {@link GamaPreferenceStore} implementation backed by an Eclipse
 * {@code IEclipsePreferences} configuration-scope node. Preferences stored here are shared across all workspaces
 * opened by the same Eclipse/GAMA installation (i.e. the same configuration directory), but are <em>not</em> shared
 * across different installations or different OS users. This makes it suitable for settings that should follow a
 * particular GAMA installation rather than the global user account.
 *
 * <p>
 * At construction time the store captures the set of keys already present in the underlying
 * {@code IEclipsePreferences} node into {@link GamaPreferenceStore#overriddenKeys}. During
 * {@link GamaPreferenceStore#register(Pref) registration} these keys are treated as overrides, meaning their
 * persisted values take precedence over the compiled-in defaults.
 * </p>
 *
 * <p>
 * Import and export use the Eclipse preferences service ({@link Platform#getPreferencesService()}) for import and
 * the {@link Properties} text format for export, matching the contract defined by
 * {@link IGamaPreferenceStore#loadFromProperties(String)} and
 * {@link IGamaPreferenceStore#saveToProperties(String)}.
 * </p>
 */
public class ConfigurationPreferenceStore extends GamaPreferenceStore<IEclipsePreferences> {

	/**
	 * Constructs a new {@code ConfigurationPreferenceStore} wrapping the given Eclipse {@code IEclipsePreferences}
	 * node. The existing keys in the node are captured into {@link GamaPreferenceStore#overriddenKeys} so that
	 * previously persisted values can override the compiled-in defaults during preference registration. If the
	 * backing store cannot be read, a diagnostic message is emitted via {@link DEBUG}.
	 *
	 * @param store
	 *            the Eclipse {@code IEclipsePreferences} node to use as the backing store; must not be {@code null}
	 */
	public ConfigurationPreferenceStore(final IEclipsePreferences store) {
		super(store);
		try {
			overriddenKeys = new LinkedHashSet<>(Arrays.asList(store.keys()));
		} catch (org.osgi.service.prefs.BackingStoreException e) {
			DEBUG.ERR("Impossible to read the preferences key from the global preferences store");
		}
	}

	/**
	 * Flushes all pending writes to the underlying Eclipse {@code IEclipsePreferences} node by calling its
	 * {@code flush()} method. Any {@code BackingStoreException} thrown by the OSGi preferences layer is silently
	 * swallowed, as failures here are non-critical (the in-memory state is still correct).
	 */
	@Override
	public void flush() {
		try {
			store.flush();
		} catch (org.osgi.service.prefs.BackingStoreException e) {}
	}

	/**
	 * Destroys the entire Eclipse {@code IEclipsePreferences} node by calling its {@code removeNode()} method, which
	 * deletes the node and all of its keys from the persistent backing store. After this call, subsequent reads will
	 * return default values. Any {@code BackingStoreException} is silently ignored.
	 */
	@Override
	public void clear() {
		try {
			store.removeNode();
		} catch (org.osgi.service.prefs.BackingStoreException e) {}
	}

	/**
	 * Writes a typed key/value pair into the Eclipse {@code IEclipsePreferences} node. The method dispatches on the
	 * runtime type of {@code value}:
	 * <ul>
	 * <li>{@code null} → stored as an empty string ({@code ""}).</li>
	 * <li>{@link String} → stored with {@code put(String, String)}.</li>
	 * <li>{@link Integer} → stored with {@code putInt(String, int)}.</li>
	 * <li>{@link Double} → stored with {@code putDouble(String, double)}.</li>
	 * <li>{@link Boolean} → stored with {@code putBoolean(String, boolean)}.</li>
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
	 * Imports preferences from a properties file located at the given path using the Eclipse preferences service
	 * ({@link Platform#getPreferencesService()#importPreferences(InputStream)}). Any {@link IOException} or
	 * {@link CoreException} thrown during the import is printed to the standard error stream.
	 *
	 * @param path
	 *            the absolute file-system path of the preferences file to import
	 */
	@Override
	public void loadFromProperties(final String path) {
		try (final var is = new FileInputStream(path);) {
			Platform.getPreferencesService().importPreferences(is);
		} catch (final IOException | CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Exports all key/value pairs stored in the Eclipse {@code IEclipsePreferences} node to a Java {@link Properties}
	 * text file at the given path. Each entry is written as {@code key=value}. The file is prefixed with the comment
	 * {@code "GAMA __PREFS__ <timestamp>"}. Any {@link IOException} or {@code BackingStoreException} is printed to
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
		} catch (final IOException | org.osgi.service.prefs.BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the raw string value stored for {@code key} in the Eclipse {@code IEclipsePreferences} node by
	 * delegating to its {@code get(String, String)} method, returning {@code def} if no entry exists.
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