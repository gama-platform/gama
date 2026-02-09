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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

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
	}

	@Override
	protected List<String> computeKeys() {
		try {
			return Arrays.asList(store.keys());
		} catch (org.osgi.service.prefs.BackingStoreException e) {
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
		} catch (org.osgi.service.prefs.BackingStoreException e) {}
	}

	@Override
	public void clear() {
		try {
			store.removeNode();
		} catch (org.osgi.service.prefs.BackingStoreException e) {}
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

}