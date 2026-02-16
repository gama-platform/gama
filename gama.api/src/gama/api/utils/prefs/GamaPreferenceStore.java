/*******************************************************************************************************
 *
 * GamaPreferenceStore.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.prefs;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.SystemInfo;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;
import gama.api.types.file.GenericFile;
import gama.api.types.file.IGamaFile;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.api.utils.StringUtils;
import one.util.streamex.StreamEx;

/**
 * A store that acts as a gateway with either the JREPreferenceStore preference store (global) or configuration-specific
 * preference stores (Eclipse). In addition, allows preferences to be overriden if they are passed as VM arguments to
 * GAMA (e.g. "-Dpref_use_pooling=true"), enabling different instances to set different values even if the store used is
 * global
 *
 * @author drogoul
 *
 * @param <T>
 */

@SuppressWarnings ({ "restriction", "unchecked", "rawtypes" })
public abstract class GamaPreferenceStore<T> implements IGamaPreferenceStore {

	/** The Constant NODE_NAME. */
	public static final String NODE_NAME = "gama";

	/** The Constant DEFAULT_FONT. */
	private static final String DEFAULT_FONT = "Default";

	/** The store. */
	T store;

	/** The cache. */
	Map<String, Pref> map = new LinkedHashMap<>();

	/** The overridden keys. */
	Set<String> overriddenKeys = Set.of();

	/**
	 * Instantiates a new gama preference store.
	 *
	 * @param store
	 *            the store
	 */
	GamaPreferenceStore(final T store) {
		this.store = store;
		flush();
	}

	/**
	 * Makes sure preferences are kept in sync between GAMA runtime and the backend file
	 */

	protected abstract void flush();

	/**
	 * Exports the contents of the preferences as a properties (key = value) file, which can then be reloaded in another
	 * instance of GAMA
	 *
	 * @param path
	 */
	@Override
	public abstract void saveToProperties(final String path);

	/**
	 * Reads a properties file and sets the contents of the preferences to the values registered in the file
	 *
	 * @param path
	 */
	@Override
	public abstract void loadFromProperties(final String path);

	/**
	 * Save preferences to GAML.
	 *
	 * @param path
	 *            the path
	 */
	@Override
	public void saveToGAML(final String path) {
		try (var os = new FileWriter(path)) {
			final var entries = StreamEx.ofValues(map).sortedBy(Pref::getName).toList();

			final var read = new StringBuilder(1000);
			final var write = new StringBuilder(1000);
			for (final Pref<?> e : entries) {
				if (e.isHidden() || !e.inGaml()) { continue; }
				read.append(StringUtils.TAB).append("//").append(e.getTitle()).append(StringUtils.LN);
				read.append(StringUtils.TAB).append("write sample(gama.").append(e.getName()).append(");")
						.append(StringUtils.LN).append(StringUtils.LN);
				write.append(StringUtils.TAB).append("//").append(e.getTitle()).append(StringUtils.LN);
				write.append(StringUtils.TAB).append("gama.").append(e.getName()).append(" <- ")
						.append(StringUtils.toGaml(e.getValue(), false)).append(";").append(StringUtils.LN)
						.append(StringUtils.LN);
			}
			os.append("// ").append(SystemInfo.VERSION).append(" __PREFS__ saved on ")
					.append(LocalDateTime.now().toString()).append(StringUtils.LN).append(StringUtils.LN);
			os.append("model preferences").append(StringUtils.LN).append(StringUtils.LN);
			os.append("experiment 'Display __PREFS__' type: gui {").append(StringUtils.LN);
			os.append("init {").append(StringUtils.LN);
			os.append(read);
			os.append("}").append(StringUtils.LN);
			os.append("}").append(StringUtils.LN).append(StringUtils.LN).append(StringUtils.LN);
			os.append("experiment 'Set __PREFS__' type: gui {").append(StringUtils.LN);
			os.append("init {").append(StringUtils.LN);
			os.append(write);
			os.append("}").append(StringUtils.LN);
			os.append("}").append(StringUtils.LN);
			os.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write.
	 *
	 * @param gp
	 *            the gp
	 */
	@Override
	public void write(final Pref gp) {
		final String key = gp.getKey();
		final Object value = gp.getValue();
		switch (gp.getTypeId()) {
			case IType.INT -> putInStore(key, value == null ? 0 : value.toString());
			case IType.FLOAT -> putInStore(key, value == null ? 0.0 : value.toString());
			case IType.BOOL -> putInStore(key, value == null ? false : value.toString());
			case IType.STRING -> putInStore(key, StringUtils.toJavaString((String) value));
			case IType.FILE -> putInStore(key, value == null ? "" : ((IGamaFile) value).getPath(null));
			case IType.COLOR -> putInStore(key, value == null ? 0 : ((IColor) value).getRGB());
			case IType.POINT -> putInStore(key, value == null ? "{0,0}" : ((IPoint) value).stringValue(null));
			case IType.FONT -> putInStore(key, value == null ? DEFAULT_FONT : value.toString());
			case IType.DATE -> putInStore(key,
					value == null ? StringUtils.toJavaString(GamaDateFactory.EPOCH.toISOString())
							: StringUtils.toJavaString(((IDate) value).toISOString()));
			default -> putInStore(key, (String) value);
		}

		flush();
	}

	/**
	 * Register.
	 *
	 * @param gp
	 *            the gp
	 */
	@Override
	public void register(final Pref<?> gp) {
		final IScope scope = null;
		final String key = gp.getKey();
		if (key == null) return;
		if (isOverriden(key)) {
			String val = getOverridenValue(key);
			switch (gp.getTypeId()) {
				case IType.POINT -> {
					gp.init(val == null ? (Supplier) gp.getValueProvider()
							: (Supplier) () -> GamaPointFactory.castToPoint(scope, Cast.asString(scope, val), false));
				}
				case IType.INT -> gp
						.init(val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asInt(scope, val));
				case IType.FLOAT -> gp.init(
						val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asFloat(scope, val));
				case IType.BOOL -> gp.init(
						val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asBool(scope, val));
				case IType.STRING -> gp.init(val == null ? (Supplier) gp.getValueProvider()
						: (Supplier) () -> StringUtils.toJavaString(Cast.asString(scope, val)));
				case IType.FILE -> gp.init(
						val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> new GenericFile(val, false));
				case IType.COLOR -> gp.init(val == null ? (Supplier) gp.getValueProvider()
						: (Supplier) () -> GamaColorFactory.get(Cast.asInt(scope, val)));
				case IType.FONT -> gp.init((Supplier) () -> {
					if (DEFAULT_FONT.equals(val)) return null;
					return val == null ? (Supplier) gp.getValueProvider()
							: (Supplier) () -> GamaFontFactory.castToFont(scope, Cast.asString(scope, val), false);
				});
				case IType.DATE -> gp
						.init(val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> GamaDateFactory
								.createFromISOString(StringUtils.toJavaString(Cast.asString(scope, val))));
				default -> gp.init(
						val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asString(scope, val));
			}

		}
		map.put(key, gp);
		flush();
	}

	/**
	 * @param key
	 * @param object
	 * @return
	 */
	protected String getOverridenValue(final String key) {
		if (isOverridenInSystemProperties(key)) return System.getProperty(key);
		if (isOverridenInStore(key)) return getInStore(key, null);
		return null;
	}

	/**
	 * @param key
	 * @return
	 */
	protected boolean isOverriden(final String key) {
		return isOverridenInSystemProperties(key) || isOverridenInStore(key);
	}

	/**
	 * Checks if is overriden in system properties.
	 *
	 * @param key
	 *            the key
	 * @return true, if is overriden in system properties
	 */
	protected boolean isOverridenInSystemProperties(final String key) {
		return System.getProperty(key) != null;
	}

	/**
	 * Checks if is overriden in store.
	 *
	 * @param key
	 *            the key
	 * @return true, if is overriden in store
	 */
	protected boolean isOverridenInStore(final String key) {
		return overriddenKeys.contains(key);
	}

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	@Override
	public Collection<String> getKeys() { return map.keySet(); }

	/**
	 * Gets the preferences.
	 *
	 * @return the preferences
	 */
	@Override
	public Collection<Pref> getPreferences() { return map.values(); }

	@Override
	public Pref get(final String key) {
		return map.get(key);
	}

	/**
	 * Apply preferences from.
	 *
	 * @param path
	 *            the path
	 * @param modelValues
	 *            the model values
	 */
	@Override
	public void applyPreferencesFrom(final String path, final Map<String, Object> modelValues) {
		// DEBUG.OUT("Apply preferences from " + path);
		loadFromProperties(path);
		for (final Pref<?> e : getPreferences()) {
			register(e);
			modelValues.put(e.getKey(), e.getValue());
		}
	}

}