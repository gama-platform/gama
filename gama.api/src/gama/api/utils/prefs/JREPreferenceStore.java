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
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import gama.dev.DEBUG;

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
		try {
			overriddenKeys = new LinkedHashSet<>(Arrays.asList(store.keys()));
		} catch (BackingStoreException e) {
			DEBUG.ERR("Impossible to read the preferences key from the global preferences store");
		}
	}

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

	/**
	 * Gets the in store.
	 *
	 * @param key
	 *            the key
	 * @param def
	 *            the def
	 * @return the in store
	 */
	@Override
	public String getInStore(final String key, final String def) {
		return store.get(key, def);
	}

}