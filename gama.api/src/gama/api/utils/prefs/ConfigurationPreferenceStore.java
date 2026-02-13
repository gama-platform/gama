/*******************************************************************************************************
 *
 * ConfigurationPreferenceStore.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation
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
 *
 * A store for each instance of GAMA (shared across workspaces of this instance)
 *
 */

public class ConfigurationPreferenceStore extends GamaPreferenceStore<IEclipsePreferences> {

	/**
	 * Instantiates a new configuration.
	 *
	 * @param store
	 *            the store
	 */
	public ConfigurationPreferenceStore(final IEclipsePreferences store) {
		super(store);
		try {
			overriddenKeys = new LinkedHashSet<>(Arrays.asList(store.keys()));
		} catch (org.osgi.service.prefs.BackingStoreException e) {
			DEBUG.ERR("Impossible to read the preferences key from the global preferences store");
		}
	}

	@Override
	public void flush() {
		try {
			store.flush();
		} catch (org.osgi.service.prefs.BackingStoreException e) {}
	}

	@Override
	public void clear() {
		try {
			store.removeNode();
		} catch (org.osgi.service.prefs.BackingStoreException e) {}
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
	public void loadFromProperties(final String path) {
		try (final var is = new FileInputStream(path);) {
			Platform.getPreferencesService().importPreferences(is);
		} catch (final IOException | CoreException e) {
			e.printStackTrace();
		}
	}

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

	@Override
	public String getInStore(final String key, final String def) {
		return store.get(key, def);
	}

}