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

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import gama.api.data.factories.GamaColorFactory;
import gama.api.data.factories.GamaDateFactory;
import gama.api.data.factories.GamaFontFactory;
import gama.api.data.factories.GamaPointFactory;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IDate;
import gama.api.data.objects.IPoint;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.utils.StringUtils;
import gama.api.utils.files.GenericFile;
import gama.api.utils.files.IGamaFile;

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

	static {
		// DEBUG.OFF();
	}

	/** The Constant NODE_NAME. */
	public static final String NODE_NAME = "gama";

	/** The Constant DEFAULT_FONT. */
	private static final String DEFAULT_FONT = "Default";

	/** The store. */
	T store;

	/** The keys. */
	private final List<String> keys;

	/**
	 * Instantiates a new gama preference store.
	 *
	 * @param store
	 *            the store
	 */
	GamaPreferenceStore(final T store) {
		this.store = store;
		keys = computeKeys();
		flush();
	}

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	@Override
	public List<String> getKeys() { return keys; }

	/**
	 * Compute keys.
	 *
	 * @return the list
	 */
	protected abstract List<String> computeKeys();

	/**
	 * Put.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	@Override
	public abstract void put(final String key, final String value);

	/**
	 * Put int.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	@Override
	public abstract void putInt(final String key, final int value);

	/**
	 * Put double.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	@Override
	public abstract void putDouble(final String key, final Double value);

	/**
	 * Put boolean.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	@Override
	public abstract void putBoolean(final String key, final Boolean value);

	/**
	 * First searches if the preference is overriden in the system/VM properties/arguments, then looks into the store if
	 * not
	 *
	 * @param key
	 * @param def
	 * @return
	 */

	public final <E> E get(final String key, final Function<String, E> function, final E def) {
		String result = System.getProperty(key);
		return result == null ? def : function.apply(result);
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
	@Override
	public final String get(final String key, final String def) {
		return get(key, Function.identity(), getStringPreference(key, def));
	}

	/**
	 * Gets the string preference.
	 *
	 * @param key
	 *            the key
	 * @param def
	 *            the def
	 * @return the string preference
	 */
	protected abstract String getStringPreference(String key, String def);

	/**
	 * First searches if the preference is overriden in the system/VM properties/arguments, then looks into the store if
	 * not
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	@Override
	public final Integer getInt(final String key, final Integer def) {
		return get(key, Integer::valueOf, getIntPreference(key, def));
	}

	/**
	 * Gets the int preference.
	 *
	 * @param key
	 *            the key
	 * @param def
	 *            the def
	 * @return the int preference
	 */
	protected abstract Integer getIntPreference(String key, Integer def);

	/**
	 * First searches if the preference is overriden in the system/VM properties/arguments, then looks into the store if
	 * not
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	@Override
	public final Double getDouble(final String key, final Double def) {
		return get(key, Double::valueOf, getDoublePreference(key, def));
	}

	/**
	 * Gets the double preference.
	 *
	 * @param key
	 *            the key
	 * @param def
	 *            the def
	 * @return the double preference
	 */
	protected abstract Double getDoublePreference(String key, Double def);

	/**
	 * First searches if the preference is overriden in the system/VM properties/arguments, then looks into the store if
	 * not
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	@Override
	public final Boolean getBoolean(final String key, final Boolean def) {
		return get(key, Boolean::valueOf, getBooleanPreference(key, def));
	}

	/**
	 * Gets the boolean preference.
	 *
	 * @param key
	 *            the key
	 * @param def
	 *            the def
	 * @return the boolean preference
	 */
	protected abstract Boolean getBooleanPreference(String key, Boolean def);

	/**
	 * Makes sure preferences are kept in sync between GAMA runtime and the backend file
	 */

	@Override
	public abstract void flush();

	/**
	 * Destroys the preferences node (all preferences are removed and replaced by defaults
	 */
	@Override
	public abstract void clear();

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
			case IType.INT -> putInt(key, (Integer) value);
			case IType.FLOAT -> putDouble(key, (Double) value);
			case IType.BOOL -> putBoolean(key, (Boolean) value);
			case IType.STRING -> put(key, StringUtils.toJavaString((String) value));
			case IType.FILE -> put(key, value == null ? "" : ((IGamaFile) value).getPath(null));
			case IType.COLOR -> putInt(key, value == null ? 0 : ((IColor) value).getRGB());
			case IType.POINT -> put(key, value == null ? "{0,0}" : ((IPoint) value).stringValue(null));
			case IType.FONT -> put(key, value == null ? DEFAULT_FONT : value.toString());
			case IType.DATE -> put(key, value == null ? StringUtils.toJavaString(GamaDateFactory.EPOCH.toISOString())
					: StringUtils.toJavaString(((IDate) value).toISOString()));
			default -> put(key, (String) value);
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
		// final Object value = gp.getValue();
		if (getKeys().contains(key)) {
			Object val = get(key);
			switch (gp.getTypeId()) {
				case IType.POINT -> {
					gp.init(val == null ? (Supplier) gp.getValueProvider()
							: (Supplier) () -> GamaPointFactory.toPoint(scope, Cast.asString(scope, val), false));
				}
				case IType.INT -> gp
						.init(val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asInt(scope, val));
				case IType.FLOAT -> gp.init(
						val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asFloat(scope, val));
				case IType.BOOL -> gp.init(
						val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asBool(scope, val));
				case IType.STRING -> gp.init(val == null ? (Supplier) gp.getValueProvider()
						: (Supplier) () -> StringUtils.toJavaString(Cast.asString(scope, val)));
				case IType.FILE -> gp.init(val == null ? (Supplier) gp.getValueProvider()
						: (Supplier) () -> new GenericFile(get(key, Cast.asString(scope, val)), false));
				case IType.COLOR -> gp.init(val == null ? (Supplier) gp.getValueProvider()
						: (Supplier) () -> GamaColorFactory.get(Cast.asInt(scope, val)));
				case IType.FONT -> gp.init((Supplier) () -> {
					if (DEFAULT_FONT.equals(val)) return null;
					return val == null ? (Supplier) gp.getValueProvider()
							: (Supplier) () -> GamaFontFactory.createFontFrom(scope, Cast.asString(scope, val), false);
				});
				case IType.DATE -> gp
						.init(val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> GamaDateFactory
								.createFromISOString(StringUtils.toJavaString(Cast.asString(scope, val))));
				default -> gp.init(
						val == null ? (Supplier) gp.getValueProvider() : (Supplier) () -> Cast.asString(scope, val));
			}

		}
		flush();
	}

}