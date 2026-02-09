/*******************************************************************************************************
 *
 * JREPreferenceStore.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * A store for all the instances of GAMA (shared across versions and applications)
 *
 */
public class JREPreferenceStore extends GamaPreferenceStore<Preferences> {

	/**
	 * Instantiates a new jre.
	 *
	 * @param store
	 *            the store
	 */
	public JREPreferenceStore(final Preferences store) {
		super(store);
	}

	@Override
	protected List<String> computeKeys() {
		try {
			return Arrays.asList(store.keys());
		} catch (BackingStoreException e) {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public void put(final String key, final String value) {
		store.put(key, value);
		flush();
	}

	@Override
	public void putInt(final String key, final int value) {
		store.putInt(key, value);
		flush();
	}

	@Override
	public void putDouble(final String key, final Double value) {
		store.putDouble(key, value);
		flush();
	}

	@Override
	public void putBoolean(final String key, final Boolean value) {
		store.putBoolean(key, value);
		flush();
	}

	@Override
	public String getStringPreference(final String key, final String def) {
		return store.get(key, def);
	}

	@Override
	public Integer getIntPreference(final String key, final Integer def) {
		return store.getInt(key, def);
	}

	@Override
	public Double getDoublePreference(final String key, final Double def) {
		return store.getDouble(key, def);
	}

	@Override
	public Boolean getBooleanPreference(final String key, final Boolean def) {
		return store.getBoolean(key, def);
	}

	@Override
	public void flush() {
		try {
			store.flush();
		} catch (BackingStoreException e) {}
	}

	@Override
	public void clear() {
		try {
			store.removeNode();
		} catch (BackingStoreException e) {}
	}

	@Override
	public void loadFromProperties(final String path) {
		try (final var is = new FileInputStream(path);) {
			Preferences.importPreferences(is);
		} catch (final IOException | InvalidPreferencesFormatException e) {
			e.printStackTrace();
		}
	}

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
}